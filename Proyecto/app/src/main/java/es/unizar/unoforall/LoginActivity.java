package es.unizar.unoforall;

import static es.unizar.unoforall.utils.HashUtils.cifrarContrasenna;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import es.unizar.unoforall.api.RestAPI;
import es.unizar.unoforall.database.UsuarioDbAdapter;

import java.util.UUID;

public class LoginActivity extends AppCompatActivity{

    private EditText mailText;
    private EditText passwordText;
    private UsuarioDbAdapter mDbHelper;
    private Long mRowId;

    private class RespuestaLogin {
        public boolean exito;
        public String errorInfo;
        public UUID sesionID;

        public RespuestaLogin(boolean exito, String errorInfo, UUID sessionID) {
            this.exito = exito;
            this.errorInfo = errorInfo;
            this.sesionID = sessionID;
        }
    }

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
            RestAPI api = new RestAPI("/api/login");
            api.addParameter("Valor1", mail);
            api.addParameter("Valor2", contrasennaHash);
            api.openConnection();

            //recepcion de los datos y actuar en consecuencia
            RespuestaLogin resp = api.receiveObject(RespuestaLogin.class);
            if(resp.exito){
                mRowId = mDbHelper.createUsuario(mail, contrasennaHash);
                //satamos a la pantalla de menu principal del juego
            } else {
                Toast.makeText(LoginActivity.this, resp.errorInfo, Toast.LENGTH_SHORT).show();
                return;
            }
            //finish();
        });
    }
}
