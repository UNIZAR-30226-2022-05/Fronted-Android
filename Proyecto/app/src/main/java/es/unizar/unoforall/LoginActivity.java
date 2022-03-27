package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import es.unizar.unoforall.api.RestAPI;
import es.unizar.unoforall.database.UsuarioDbAdapter;
import es.unizar.unoforall.model.RespuestaLogin;

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

        mailText = (EditText) findViewById(R.id.correoEditTextLogin);
        passwordText = (EditText) findViewById(R.id.contrasennaEditTextLogin);

        Button confirmLogin = (Button) findViewById(R.id.login);

        confirmLogin.setOnClickListener(view -> {
            String mail = mailText.getText().toString();
            String contrasenna = passwordText.getText().toString();
            String contrasennaHash = contrasenna;//cifrarContrasenna(contrasenna);

            setResult(RESULT_OK);
            //envio de los datos al servidor
            RestAPI api = new RestAPI(this,"/api/login");
            api.addParameter("correo", mail);
            api.addParameter("contrasenna", contrasennaHash);
            api.openConnection();

            //recepcion de los datos y actuar en consecuencia
            api.setOnObjectReceived(RespuestaLogin.class, resp -> {
                if(resp.isExito()){
                    mRowId = mDbHelper.createUsuario(mail, contrasennaHash);

                    Intent i = new Intent(this, PantallaPrincipalActivity.class);
                    i.putExtra("sesionID", resp.getSesionID());
                    startActivity(i);

                } else {
                    Toast.makeText(LoginActivity.this, resp.getErrorInfo(), Toast.LENGTH_SHORT).show();
                    return;
                }
            });

            //finish();
        });
    }
}
