package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.api.RestAPI;
import es.unizar.unoforall.api.Serializar;
import es.unizar.unoforall.utils.CodeConfirmDialogBuilder;
import es.unizar.unoforall.utils.HashUtils;


public class RegisterActivity extends AppCompatActivity{

    private EditText nombreUsuarioEditText;
    private EditText correoEditText;
    private EditText contrasennaEditText;
    private EditText contrasennaBisEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle(R.string.register);

        nombreUsuarioEditText = (EditText) findViewById(R.id.nombreEditTextRegistro);
        correoEditText = (EditText) findViewById(R.id.correoEditTextRegistro);
        contrasennaEditText = (EditText) findViewById(R.id.contrasennaEditTextRegistro);
        contrasennaBisEditText = (EditText) findViewById(R.id.contrasennabisEditTextRegistro);

        Button confirmRegister = (Button) findViewById(R.id.register);

        confirmRegister.setOnClickListener(view -> {
            String nombreUsuario = nombreUsuarioEditText.getText().toString();
            String correo = correoEditText.getText().toString();
            String contrasenna = contrasennaEditText.getText().toString();
            String contrasennaBis = contrasennaBisEditText.getText().toString();

            if(nombreUsuario.isEmpty()){
                nombreUsuarioEditText.setError(getString(R.string.campoVacio));
                return;
            }
            if(correo.isEmpty()){
                correoEditText.setError(getString(R.string.campoVacio));
                return;
            }
            if(contrasenna.isEmpty()){
                contrasennaEditText.setError(getString(R.string.campoVacio));
                return;
            }
            if(contrasennaBis.isEmpty()){
                contrasennaBisEditText.setError(getString(R.string.campoVacio));
                return;
            }

            if(!contrasenna.equals(contrasennaBis)){
                Toast.makeText(RegisterActivity.this, getString(R.string.errorContrasegnas), Toast.LENGTH_SHORT).show();
                return;
            }

            //envio de los datos al servidor
            BackendAPI api = new BackendAPI(this);
            api.register(nombreUsuario, correo, HashUtils.cifrarContrasenna(contrasenna));
        });
    }

}
