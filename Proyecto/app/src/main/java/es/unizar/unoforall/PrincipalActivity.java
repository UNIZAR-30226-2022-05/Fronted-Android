package es.unizar.unoforall;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.UUID;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.UsuarioVO;

public class PrincipalActivity extends AppCompatActivity {

    public static final String KEY_CLAVE_INICIO = "claveInicio";

    private static final int MODIFICAR_CUENTA_ID = 0;
    private static final int MODIFICAR_ASPECTO_ID = 1;
    private static final int GESTIONAR_AMIGOS_ID = 2;
    private static final int VER_ESTADISTICAS_ID = 3;
    private static final int VER_HISTORIAL_ID = 4;

    private static UUID sesionID;
    public static UUID getSesionID(){
        return sesionID;
    }
    public static void setSesionID(UUID sesionID){
        PrincipalActivity.sesionID = sesionID;
    }

    private static UsuarioVO usuario;
    public static UsuarioVO getUsuario(){
        return usuario;
    }
    public static void setUsuario(UsuarioVO usuario){
        PrincipalActivity.usuario = usuario;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        UUID claveInicio = (UUID) this.getIntent().getSerializableExtra(KEY_CLAVE_INICIO);
        BackendAPI api = new BackendAPI(this);
        api.loginPaso2(claveInicio);

        Button crearSalaButton = findViewById(R.id.crearSalaButton);
        crearSalaButton.setOnClickListener(v -> startActivity(new Intent(this, CrearSalaActivity.class)));

        Button buscarSalaButton = findViewById(R.id.buscarSalaPublicaButton);
        buscarSalaButton.setOnClickListener(v -> startActivity(new Intent(this, BuscarSalaActivity.class)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MODIFICAR_CUENTA_ID, Menu.NONE, R.string.modificarCuenta);
        menu.add(Menu.NONE, MODIFICAR_ASPECTO_ID, Menu.NONE, R.string.modificarAspecto);
        menu.add(Menu.NONE, GESTIONAR_AMIGOS_ID, Menu.NONE, R.string.gestionarAmigos);
        menu.add(Menu.NONE, VER_ESTADISTICAS_ID, Menu.NONE, R.string.verEstadisticas);
        menu.add(Menu.NONE, VER_HISTORIAL_ID, Menu.NONE, R.string.verHistorial);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case MODIFICAR_CUENTA_ID:
                new BackendAPI(this).modificarCuenta(sesionID);
                break;
            default:
                Toast.makeText(this, "No implementado todavía", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cerrar sesión");
        builder.setMessage("¿Quieres cerrar sesión?");
        builder.setPositiveButton("Sí", (dialog, which) -> {
            BackendAPI.closeWebSocketAPI();
            Intent intent = new Intent(this, InicioActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BackendAPI.closeWebSocketAPI();
    }
}