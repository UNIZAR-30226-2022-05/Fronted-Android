package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;
import java.util.UUID;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import es.unizar.unoforall.api.WebSocketAPI;

public class PantallaPrincipalActivity extends AppCompatActivity {

    private static WebSocketAPI wsAPI;

    private static UUID miSesionID;

    public static UUID getMiSesionID(){
        return miSesionID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_principal);

        miSesionID = (UUID) this.getIntent().getSerializableExtra("sesionID");
        Toast.makeText(this, "miSesionID: " + miSesionID, Toast.LENGTH_SHORT).show();


        Button crearSalaButton = findViewById(R.id.crearSalaButton);
        crearSalaButton.setOnClickListener(v -> startActivity(new Intent(this, CrearSalaActivity.class)));
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }
}