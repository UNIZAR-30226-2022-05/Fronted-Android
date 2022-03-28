package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import es.unizar.unoforall.api.RestAPI;
import es.unizar.unoforall.model.RespuestaLogin;

public class RecuperarContrasennaActivity extends AppCompatActivity {

    private EditText emailConfirmarText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recuperar_contrasenna);
        setTitle(R.string.recuperarContraseña);

        emailConfirmarText = findViewById(R.id.correoConfirmacion);

        Button enviarCodigoBoton = findViewById(R.id.confirmEmail);
        enviarCodigoBoton.setOnClickListener(view -> {
            String correo = emailConfirmarText.getText().toString();

            RestAPI api = new RestAPI(this,"/api/reestablecercontrasennaStepOne");
            api.addParameter("correo", correo);
            api.openConnection();

            api.setOnObjectReceived(String.class, resp -> {
                if(resp == null){
                    Intent i = new Intent(this, PantallaPrincipalActivity.class);
                    startActivity(i);

                } else {
                    Toast.makeText(this, resp, Toast.LENGTH_SHORT).show();
                    return;
                }
            });
        });
    }
}