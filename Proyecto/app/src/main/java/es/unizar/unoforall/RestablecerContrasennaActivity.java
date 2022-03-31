package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import es.unizar.unoforall.api.BackendAPI;
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

            BackendAPI api = new BackendAPI(this);
            api.restablecerContrasenna(correo);
        });

        correoEditText.setOnKeyListener((view, keyCode, keyEvent) -> {
            if((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                enviarCodigoBoton.performClick();
                return true;
            }else{
                return false;
            }
        });
    }
}