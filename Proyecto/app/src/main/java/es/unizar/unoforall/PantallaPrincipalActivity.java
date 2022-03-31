package es.unizar.unoforall;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.UUID;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.api.WebSocketAPI;
import es.unizar.unoforall.model.UsuarioVO;

public class PantallaPrincipalActivity extends AppCompatActivity {

    public static final String KEY_CLAVE_INICIO = "claveInicio";

    private static UUID sesionID;
    public static UUID getSesionID(){
        return sesionID;
    }
    public static void setSesionID(UUID sesionID){
        PantallaPrincipalActivity.sesionID = sesionID;
    }

    private static UsuarioVO usuario;
    public static UsuarioVO getUsuario(){
        return usuario;
    }
    public static void setUsuario(UsuarioVO usuario){
        PantallaPrincipalActivity.usuario = usuario;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_principal);

        UUID claveInicio = (UUID) this.getIntent().getSerializableExtra(KEY_CLAVE_INICIO);
        BackendAPI api = new BackendAPI(this);
        api.loginPaso2(claveInicio);

        Button crearSalaButton = findViewById(R.id.crearSalaButton);
        crearSalaButton.setOnClickListener(v -> startActivity(new Intent(this, CrearSalaActivity.class)));

        Button buscarSalaButton = findViewById(R.id.buscarSalaPublicaButton);
        buscarSalaButton.setOnClickListener(v -> startActivity(new Intent(this, BuscarSalaActivity.class)));
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cerrar sesión");
        builder.setMessage("¿Quieres cerrar sesión?");
        builder.setPositiveButton("Sí", (dialog, which) -> {
            BackendAPI.closeWebSocketAPI();
            Intent intent = new Intent(this, MainActivity.class);
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