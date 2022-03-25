package es.unizar.unoforall;

import static es.unizar.unoforall.utils.HashUtils.cifrarContrasenna;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import es.unizar.unoforall.api.RestAPI;
import es.unizar.unoforall.database.UsuarioDbAdapter;
import es.unizar.unoforall.modelo.RespuestaLogin;

import java.util.UUID;

public class LoginActivity extends AppCompatActivity{

    private EditText mailText;
    private EditText passwordText;
    private UsuarioDbAdapter mDbHelper;
    private Long mRowId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        setTitle(R.string.login);

        mDbHelper = new UsuarioDbAdapter(this);
        mDbHelper.open();

        mailText = (EditText) findViewById(R.id.correo);
        passwordText = (EditText) findViewById(R.id.contrasenna);

        String mail = mailText.getText().toString();
        String contrasenna = passwordText.getText().toString();

        String contrasennaHash = cifrarContrasenna(contrasenna);

        Button confirmLogin = (Button) findViewById(R.id.login);

        confirmLogin.setOnClickListener(view -> {
            setResult(RESULT_OK);
            //envio de los datos al servidor
            RestAPI api = new RestAPI(this,"/api/login");
            api.addParameter("correo", mail);
            api.addParameter("contrasenna", contrasennaHash);
            api.openConnection();

            //recepcion de los datos y actuar en consecuencia
            api.setOnObjectReceived(RespuestaLogin.class, resp -> {
                if(resp.exito){
                    mRowId = mDbHelper.createUsuario(mail, contrasennaHash);

                    Intent i = new Intent(this, PantallaPrincipalActivity.class);
                    i.putExtra("sesionID", resp.sesionID);
                    api.close();
                    startActivity(i);

                } else {
                    Toast.makeText(LoginActivity.this, resp.errorInfo, Toast.LENGTH_SHORT).show();
                    return;
                }
            });

            //finish();
        });
    }
}
