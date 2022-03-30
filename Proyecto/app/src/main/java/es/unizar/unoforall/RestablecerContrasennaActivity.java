package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import es.unizar.unoforall.api.RestAPI;

public class RestablecerContrasennaActivity extends AppCompatActivity {

    private EditText correoEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restablecer_contrasenna);
        setTitle(R.string.restablecerContrasenna);

        correoEditText = findViewById(R.id.correoConfirmacion);

        Button enviarCodigoBoton = findViewById(R.id.confirmEmail);
        enviarCodigoBoton.setOnClickListener(view -> {
            String correo = correoEditText.getText().toString();

            if(correo.isEmpty()){
                correoEditText.setError(getString(R.string.campoVacio));
                return;
            }

            RestAPI api = new RestAPI(this,"/api/reestablecercontrasennaStepOne");
            api.addParameter("correo", correo);
            api.openConnection();

            api.setOnObjectReceived(String.class, resp -> {
                if(resp == null){
                    Intent i = new Intent(this, PantallaPrincipalActivity.class);
                    startActivity(i);

                } else {
                    Toast.makeText(this, resp, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}