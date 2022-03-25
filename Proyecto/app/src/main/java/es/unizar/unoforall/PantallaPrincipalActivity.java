package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;
import java.util.UUID;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

public class PantallaPrincipalActivity extends AppCompatActivity {

    private UUID miSesionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_principal);

        //Revisar esta conversion
        miSesionID = (UUID) this.getIntent().getSerializableExtra("sesionID");
        Toast.makeText(this, "miSesionID", Toast.LENGTH_SHORT).show();

        Button vuelta = (Button) findViewById(R.id.volver);

        vuelta.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
    }
}