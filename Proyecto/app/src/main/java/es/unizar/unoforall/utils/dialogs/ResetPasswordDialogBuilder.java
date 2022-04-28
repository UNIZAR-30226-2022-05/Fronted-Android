package es.unizar.unoforall.utils.dialogs;

import android.app.Activity;
import android.text.InputType;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

import java.util.function.Consumer;

import es.unizar.unoforall.R;

public class ResetPasswordDialogBuilder{
    private static final int PADDING = 25;
    private final Activity activity;

    private final LinearLayout linearLayout;
    private final EditText passwordEditText;
    private final EditText passwordBisEditText;

    private Runnable positiveRunnable;
    private Runnable negativeRunnable;

    public ResetPasswordDialogBuilder(Activity activity){
        this.activity = activity;

        this.passwordEditText = new EditText(activity);
        this.passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        this.passwordEditText.setHint("Contraseña");

        this.passwordBisEditText = new EditText(activity);
        this.passwordBisEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        this.passwordBisEditText.setHint("Repetir la contraseña");

        this.linearLayout = new LinearLayout(activity);
        this.linearLayout.setOrientation(LinearLayout.VERTICAL);
        this.linearLayout.setPadding(PADDING, PADDING, PADDING, PADDING);
        this.linearLayout.addView(passwordEditText);
        this.linearLayout.addView(passwordBisEditText);

        this.positiveRunnable = () -> {};
        this.negativeRunnable = () -> {};
    }

    public void setErrorPassword(String error){
        this.passwordEditText.setError(error);
    }
    public void setErrorPasswordBis(String error){
        this.passwordBisEditText.setError(error);
    }

    public void setPositiveButton(Consumer<String> consumer){
        this.positiveRunnable = () -> {
            String password = passwordEditText.getText().toString();
            String passwordBis = passwordBisEditText.getText().toString();
            if(password.isEmpty()){
                setErrorPassword(activity.getString(R.string.campoVacio));
                show();
                return;
            }
            if(passwordBis.isEmpty()){
                setErrorPasswordBis(activity.getString(R.string.campoVacio));
                show();
                return;
            }
            if(!passwordBis.equals(password)){
                setErrorPasswordBis(activity.getString(R.string.errorContrasegnas));
                show();
                return;
            }

            consumer.accept(password);
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
        builder.setTitle("Nueva contraseña");
        builder.setMessage("Introduzca la nueva contraseña");
        builder.setView(linearLayout);
        builder.setPositiveButton("Confirmar", (dialog, which) -> positiveRunnable.run());
        builder.setNegativeButton("Cancelar", (dialog, which) -> negativeRunnable.run());
        builder.setOnCancelListener(dialog -> show());

        builder.show();
    }
}
