package es.unizar.unoforall.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.InputType;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.Objects;

import es.unizar.unoforall.R;

public class ModifyAccountDialogBuilder{
    private static final int PADDING = 25;
    private final Activity activity;

    private final LinearLayout linearLayout;
    private final EditText nombreUsuarioEditText;
    private final EditText correoEditText;
    private final EditText passwordEditText;
    private final EditText passwordBisEditText;

    private Runnable positiveRunnable;
    private Runnable negativeRunnable;

    public ModifyAccountDialogBuilder(Activity activity){
        this.activity = activity;

        this.nombreUsuarioEditText = new EditText(activity);
        this.nombreUsuarioEditText.setHint("Nuevo nombre de usuario");

        this.correoEditText = new EditText(activity);
        this.correoEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        this.correoEditText.setHint("Nuevo correo");

        this.passwordEditText = new EditText(activity);
        this.passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        this.passwordEditText.setHint("(Contraseña sin modificar)");

        this.passwordBisEditText = new EditText(activity);
        this.passwordBisEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        this.passwordBisEditText.setHint("(Contraseña sin modificar)");

        this.linearLayout = new LinearLayout(activity);
        this.linearLayout.setOrientation(LinearLayout.VERTICAL);
        this.linearLayout.setPadding(PADDING, PADDING, PADDING, PADDING);
        this.linearLayout.addView(nombreUsuarioEditText);
        this.linearLayout.addView(correoEditText);
        this.linearLayout.addView(passwordEditText);
        this.linearLayout.addView(passwordBisEditText);

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
        ViewParent parent = linearLayout.getParent();
        if(parent != null){
            ((ViewGroup) parent).removeView(linearLayout);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Configuración de cuenta");
        builder.setMessage("Modifique los datos deseados y pulse en confirmar");
        builder.setView(linearLayout);
        builder.setPositiveButton("Confirmar", (dialog, which) -> positiveRunnable.run());
        builder.setNegativeButton("Cancelar", (dialog, which) -> negativeRunnable.run());
        builder.setOnCancelListener(dialog -> negativeRunnable.run());

        builder.show();
    }
}
