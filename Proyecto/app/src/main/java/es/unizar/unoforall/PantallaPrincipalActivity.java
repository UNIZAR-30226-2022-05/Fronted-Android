package es.unizar.unoforall;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.UUID;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.api.WebSocketAPI;
import es.unizar.unoforall.model.UsuarioVO;

public class PantallaPrincipalActivity extends AppCompatActivity {

    public static final String KEY_CLAVE_INICIO = "claveInicio";

    private static WebSocketAPI wsAPI;

    private static UUID sesionID;
    public static UUID getSesionID(){
        return sesionID;
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

        UUID claveIncio = (UUID) this.getIntent().getSerializableExtra(KEY_CLAVE_INICIO);
        wsAPI = new WebSocketAPI(this);
        wsAPI.openConnection();
        wsAPI.subscribe("/topic/conectarse/" + claveIncio, UUID.class, sesionID -> {
            wsAPI.unsubscribe("/topic/conectarse/");
            PantallaPrincipalActivity.sesionID = sesionID;

            BackendAPI api = new BackendAPI(this);
            api.obtenerUsuarioVO(sesionID, usuarioVO -> {
                usuario = usuarioVO;
                Toast.makeText(this, "Hola " + usuario.getNombre() + ", has iniciado sesión correctamente", Toast.LENGTH_SHORT).show();
            });
        });
        wsAPI.sendObject("/app/conectarse/" + claveIncio, "VACIO");

        Button crearSalaButton = findViewById(R.id.crearSalaButton);
        crearSalaButton.setOnClickListener(v -> startActivity(new Intent(this, CrearSalaActivity.class)));
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cerrar sesión");
        builder.setMessage("¿Quieres cerrar sesión?");
        builder.setPositiveButton("Sí", (dialog, which) -> {
            wsAPI.close();
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
        wsAPI.close();
    }
}