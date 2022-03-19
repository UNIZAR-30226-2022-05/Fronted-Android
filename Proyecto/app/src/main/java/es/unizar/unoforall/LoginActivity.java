package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity{

    private EditText mailText;
    private EditText passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        setTitle(R.string.login);

        mailText = (EditText) findViewById(R.id.correo);
        passwordText = (EditText) findViewById(R.id.contrasenna);

        Button confirmLogin = (Button) findViewById(R.id.login);

        confirmLogin.setOnClickListener(view -> {
            setResult(RESULT_OK);
            //envio de los datos al servidor

            //recepcion de los datos y actuar en consecuencia
            //finish();
        });
    }
}
