package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import es.unizar.unoforall.api.RestAPI;
import es.unizar.unoforall.utils.HashUtils;


public class RegisterActivity extends AppCompatActivity{

    private EditText userNameText;
    private EditText mailText;
    private EditText passwordText;
    private EditText passBisText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        setTitle(R.string.register);

        userNameText = (EditText) findViewById(R.id.nombreEditTextRegistro);
        mailText = (EditText) findViewById(R.id.correoEditTextRegistro);
        passwordText = (EditText) findViewById(R.id.contrasennaEditTextRegistro);
        passBisText = (EditText) findViewById(R.id.contrasennabisEditTextRegistro);

        Button confirmRegister = (Button) findViewById(R.id.register);

        confirmRegister.setOnClickListener(view -> {
            String userName = userNameText.getText().toString();
            String mail = mailText.getText().toString();
            String password = passwordText.getText().toString();
            String passBis = passBisText.getText().toString();

            if(!password.equals(passBis)){
                Toast.makeText(RegisterActivity.this, getString(R.string.ErrorContrasegnas), Toast.LENGTH_SHORT).show();
                return;
            }
            //envio de los datos al servidor
            String contrasennaHash = HashUtils.cifrarContrasenna(password);

            RestAPI api = new RestAPI(this, "/api/registerStepOne");
            api.addParameter("correo", mail);
            api.addParameter("contrasenna", contrasennaHash);
            api.addParameter("nombre", userName);
            api.openConnection();

            //recepcion de los datos y actuar en consecuencia
            api.setOnObjectReceived(String.class, resp -> {
                if (resp == null){
                    //Usuario registrado y cambiamos a la pantalla de confirmacion
                    Intent i = new Intent(this, ConfirmEmailActivity.class);
                    i.putExtra("correo", mail);
                    i.putExtra("contrasenna", contrasennaHash);
                    startActivity(i);
                } else {
                    Toast.makeText(RegisterActivity.this, resp, Toast.LENGTH_SHORT).show();
                    return;
                }
            });

            //finish();
        });
    }

}
