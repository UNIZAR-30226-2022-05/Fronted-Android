package es.unizar.unoforall;

import androidx.appcompat.app.AlertDialog;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.database.UsuarioDbAdapter;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.HashUtils;
import es.unizar.unoforall.utils.Vibration;
import es.unizar.unoforall.utils.ActivityType;

public class LoginActivity extends CustomActivity {

    private ListView listaUsuarios;
    private EditText correoEditText;
    private EditText contrasennaEditText;
    private UsuarioDbAdapter mDbHelper;

    @Override
    public ActivityType getType(){
        return ActivityType.LOGIN;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.login);

        mDbHelper = new UsuarioDbAdapter(this);
        mDbHelper.open();

        correoEditText = findViewById(R.id.correoEditTextLogin);
        contrasennaEditText = findViewById(R.id.contrasennaEditTextLogin);


        TextView linkText = findViewById(R.id.textoMarcableLogin);
        linkText.setOnClickListener(view -> startActivityForResult(new Intent(this, RestablecerContrasennaActivity.class), 0));
        linkText.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setBackgroundColor(Color.LTGRAY);
                    view.invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    view.setBackgroundColor(Color.WHITE);
                    view.invalidate();
                    break;
            }
            return false;
        });

        Button confirmLogin = findViewById(R.id.login);
        confirmLogin.setOnClickListener(view -> {
            String correo = correoEditText.getText().toString();
            String contrasenna = contrasennaEditText.getText().toString();

            if(correo.isEmpty()){
                correoEditText.setError(getString(R.string.campoVacio));
                return;
            }
            if(contrasenna.isEmpty()){
                contrasennaEditText.setError(getString(R.string.campoVacio));
                return;
            }

            //envio de los datos al servidor
            BackendAPI api = new BackendAPI(this);
            api.login(correo, HashUtils.cifrarContrasenna(contrasenna));
        });

        contrasennaEditText.setOnKeyListener((view, keyCode, keyEvent) -> {
            if((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                confirmLogin.performClick();
                return true;
            }else{
                return false;
            }
        });

        if(mDbHelper.getNumUsuarios() <= 0){
            TextView tituloListaUsuarios = findViewById(R.id.textViewTituloListaUsuarios);
            tituloListaUsuarios.setVisibility(View.GONE);
            return;
        }

        listaUsuarios = findViewById(R.id.listaUsuarios);
        filldata();

        listaUsuarios.setOnItemClickListener((adapterView, view, pos, id) -> {
            Vibration.vibrate(this, 40);

            String correo = ((TextView) view).getText().toString();
            Cursor cursor = mDbHelper.buscarUsuario(correo);
            String contrasennaHash = cursor.getString(2);

            BackendAPI api = new BackendAPI(this);
            api.login(correo, contrasennaHash);
        });

        listaUsuarios.setOnItemLongClickListener((adapterView, view, pos, id) -> {
            Vibration.vibrate(this, 100);

            String correo = ((TextView) view).getText().toString();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Borrar usuario");
            builder.setMessage("¿Quieres borrar al usuario " + correo + " ?");
            builder.setPositiveButton("Aceptar", (dialog, which) ->  {
                mDbHelper.deleteUsuario(correo);
                filldata();
                mostrarMensaje("El usuario " + correo + " ha sido borrado");
            });
            builder.setNegativeButton("Cancelar", (dialog, which) -> {
               dialog.dismiss();
            });
            builder.create().show();
            return true;
        });
    }

    private void filldata(){
        Cursor usuariosCursor = mDbHelper.listarUsuarios();
        String[] from = new String[] { UsuarioDbAdapter.KEY_CORREO };
        int[] to = new int[] { R.id.usuario };
        SimpleCursorAdapter usuariosAdapter =
                new SimpleCursorAdapter(this, R.layout.usuarios_row, usuariosCursor, from, to);
        listaUsuarios.setAdapter(usuariosAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDbHelper.close();
    }
}
