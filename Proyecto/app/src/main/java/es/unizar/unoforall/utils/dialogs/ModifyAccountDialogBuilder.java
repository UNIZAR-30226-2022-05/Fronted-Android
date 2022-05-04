package es.unizar.unoforall.utils.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.Objects;

import es.unizar.unoforall.R;
import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.TriConsumer;

public class ModifyAccountDialogBuilder{
    private final CustomActivity activity;

    private final View mainView;

    private final EditText nombreUsuarioEditText;
    private final EditText correoEditText;
    private final EditText passwordEditText;
    private final EditText passwordBisEditText;
    private final Button borrarCuentaButton;

    private Runnable positiveRunnable;
    private Runnable negativeRunnable;

    public ModifyAccountDialogBuilder(CustomActivity activity){
        this.activity = activity;

        this.mainView = LayoutInflater.from(activity).inflate(R.layout.modificacion_cuenta_layout, null);

        this.nombreUsuarioEditText = this.mainView.findViewById(R.id.nombreUsuarioEditText);
        this.correoEditText = this.mainView.findViewById(R.id.correoEditText);
        this.passwordEditText = this.mainView.findViewById(R.id.passwordEditText);
        this.passwordBisEditText = this.mainView.findViewById(R.id.passwordBisEditText);
        this.borrarCuentaButton = this.mainView.findViewById(R.id.borrarCuentaButton);

        this.borrarCuentaButton.setOnClickListener(view -> new BackendAPI(activity).borrarCuenta());

        this.positiveRunnable = () -> {};
        this.negativeRunnable = () -> {};
    }

    public void setNombreUsuario(String nombreUsuario){
        this.nombreUsuarioEditText.setText(nombreUsuario);
    }
    public void setCorreo(String correo) {
        this.correoEditText.setText(correo);
    }

    public void setPositiveButton(TriConsumer<String, String, String> consumer){
        this.positiveRunnable = () -> {
            String nombreUsuario = nombreUsuarioEditText.getText().toString();
            if(nombreUsuario.isEmpty()){
                nombreUsuarioEditText.setError(activity.getString(R.string.campoVacio));
                show();
                return;
            }

            String correo = correoEditText.getText().toString();
            if(correo.isEmpty()){
                correoEditText.setError(activity.getString(R.string.campoVacio));
                show();
                return;
            }

            String password = passwordEditText.getText().toString();
            String passwordBis = passwordBisEditText.getText().toString();
            if(!passwordBis.equals(password)){
                passwordBisEditText.setError(activity.getString(R.string.errorContrasegnas));
                show();
                return;
            }
            if(password.isEmpty()){
                password = null;
            }

            consumer.accept(nombreUsuario, correo, password);
        };
    }

    public void setNegativeButton(Runnable runnable){
        this.negativeRunnable = runnable;
    }

    public void show(){
        ViewParent parent = mainView.getParent();
        if(parent != null){
            ((ViewGroup) parent).removeView(mainView);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Modificar cuenta");
        builder.setMessage("Modifique los datos deseados y pulse en confirmar");
        builder.setView(mainView);
        builder.setPositiveButton("Confirmar", (dialog, which) -> positiveRunnable.run());
        builder.setNegativeButton("Cancelar", (dialog, which) -> negativeRunnable.run());
        builder.setOnCancelListener(dialog -> show());

        builder.show();
    }
}
