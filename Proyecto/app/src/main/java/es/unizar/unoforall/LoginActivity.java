package es.unizar.unoforall;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import es.unizar.unoforall.api.RestAPI;
import es.unizar.unoforall.database.UsuarioDbAdapter;
import es.unizar.unoforall.model.RespuestaLogin;
import es.unizar.unoforall.utils.Vibration;

public class LoginActivity extends AppCompatActivity{

    private TextView linkText;
    private EditText mailText;
    private EditText passwordText;
    private UsuarioDbAdapter mDbHelper;
    private Long mRowId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.login);

        mDbHelper = new UsuarioDbAdapter(this);
        mDbHelper.open();

        mailText = findViewById(R.id.correoEditTextLogin);
        passwordText = findViewById(R.id.contrasennaEditTextLogin);

        ListView listaUsuarios = findViewById(R.id.listaUsuarios);

        Cursor usuariosCursor = mDbHelper.listarUsuarios();
        String[] from = new String[] { UsuarioDbAdapter.KEY_CORREO };
        int[] to = new int[] { R.id.usuario };
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.usuarios_row, usuariosCursor, from, to);
        listaUsuarios.setAdapter(notes);

        listaUsuarios.setOnItemClickListener((adapterView, view, pos, id) -> {
            Vibration.vibrate(this, 40);

            String correo = ((TextView) view).getText().toString();
            Cursor cursor = mDbHelper.buscarUsuario(correo);
            startManagingCursor(cursor);
            String contrasennaHash = cursor.getString(2);

            RestAPI api = new RestAPI(this,"/api/login");
            api.addParameter("correo", correo);
            api.addParameter("contrasenna", contrasennaHash);
            api.openConnection();

            api.setOnObjectReceived(RespuestaLogin.class, resp -> {
                if(resp.isExito()){

                    Intent i = new Intent(this, PantallaPrincipalActivity.class);
                    i.putExtra("sesionID", resp.getSesionID());
                    startActivity(i);

                } else {
                    Toast.makeText(LoginActivity.this, resp.getErrorInfo(), Toast.LENGTH_SHORT).show();
                    return;
                }
            });
        });

        listaUsuarios.setOnItemLongClickListener((adapterView, view, pos, id) -> {
            Vibration.vibrate(this, 100);

            String correo = ((TextView) view).getText().toString();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Borrar usuario");
            builder.setMessage("¿Quieres borrar al usuario " + correo + " ?");
            builder.setPositiveButton("Aceptar", (dialog, which) ->  {
                mDbHelper.deleteUsuario(correo);
                listaUsuarios.removeViewAt(pos);
                Toast.makeText(this, "El usuario " + correo + " ha sido borrado", Toast.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("Cancelar", (dialog, which) -> {
               dialog.dismiss();
            });
            builder.create().show();
            return true;
        });

        linkText = findViewById(R.id.textoMarcableLogin);
        linkText.setOnClickListener(v -> startActivity(new Intent(this, RecuperarContrasennaActivity.class)));

        Button confirmLogin = findViewById(R.id.login);
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
