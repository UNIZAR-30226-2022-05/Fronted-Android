package es.unizar.unoforall.utils.dialogs;

import android.app.Activity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import java.util.function.Consumer;

import es.unizar.unoforall.R;

public class PeticionDialogBuilder {
    private final Activity activity;
    private final EditText codeEditText;

    private Runnable positiveRunnable;
    private Runnable negativeRunnable;

    public PeticionDialogBuilder(Activity activity){
        this.activity = activity;

        this.codeEditText = new EditText(activity);
        this.codeEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        this.codeEditText.setHint("Correo del destinatario");

        this.positiveRunnable = () -> {};
        this.negativeRunnable = () -> {};
    }

    public void setError(String error){
        this.codeEditText.setError(error);
    }

    public void setPositiveButton(Consumer<String> consumer){
        this.positiveRunnable = () -> {
            String correo = codeEditText.getText().toString();
            if(correo.isEmpty()){
                codeEditText.setError(activity.getString(R.string.campoVacio));
                show();
            }else{
                consumer.accept(correo);
            }
        };
    }

    public void setNegativeButton(Runnable runnable){
        this.negativeRunnable = runnable;
    }

    public void show(){
        ViewParent parent = codeEditText.getParent();
        if(parent != null){
            ((ViewGroup) parent).removeView(codeEditText);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Enviar solicitud de amistad");
        builder.setMessage("Introduzca la dirección de correo del destinatario");
        builder.setView(codeEditText);
        builder.setPositiveButton("Enviar", (dialog, which) -> positiveRunnable.run());
        builder.setNegativeButton("Cancelar", (dialog, which) -> negativeRunnable.run());
        builder.setOnCancelListener(dialog -> negativeRunnable.run());

        builder.show();
    }
}
