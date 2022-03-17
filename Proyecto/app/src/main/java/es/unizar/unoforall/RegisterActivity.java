package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class RegisterActivity extends AppCompatActivity{

    private EditText userNameText;
    private EditText mailText;
    private EditText passwordText;
    private EditText passBisText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitle(R.string.register);

        userNameText = (EditText) findViewById(R.id.nombre);
        mailText = (EditText) findViewById(R.id.correo);
        passwordText = (EditText) findViewById(R.id.contrasenna);
        passBisText = (EditText) findViewById(R.id.contrasennabis);

        Button confirmRegister = (Button) findViewById(R.id.register);

        confirmRegister.setOnClickListener(view -> {
            setResult(RESULT_OK);
            String password = passwordText.getText().toString();
            String passBis = passBisText.getText().toString();
            if(!password.equals(passBis)){
                Toast.makeText(RegisterActivity.this, getString(R.string.ErrorContrasegnas), Toast.LENGTH_SHORT).show();
                return;
            }
            //envio de los datos al servidor

            //recepcion de los datos y actuar en consecuencia
            //finish();
        });
    }

}
