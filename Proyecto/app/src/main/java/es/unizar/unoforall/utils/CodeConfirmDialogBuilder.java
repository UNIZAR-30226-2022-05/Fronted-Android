package es.unizar.unoforall.utils;

import android.app.Activity;
import android.text.InputFilter;
import android.text.InputType;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import java.util.function.Consumer;

import es.unizar.unoforall.R;

//Fuente: https://stackoverflow.com/a/29048271

public class CodeConfirmDialogBuilder {
    private static final int MAX_CODE_LENGTH = 6;

    private final Activity activity;
    private final EditText codeEditText;

    private Runnable positiveRunnable;
    private Runnable negativeRunnable;

    public CodeConfirmDialogBuilder(Activity activity){
        this.activity = activity;

        this.codeEditText = new EditText(activity);
        this.codeEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        this.codeEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(MAX_CODE_LENGTH) });
        this.codeEditText.setHint("Código");

        this.positiveRunnable = () -> {};
        this.negativeRunnable = () -> {};
    }

    public void setError(String error){
        this.codeEditText.setError(error);
    }

    public void setPositiveButton(Consumer<Integer> consumer){
        this.positiveRunnable = () -> {
            String codigoString = codeEditText.getText().toString();
            if(codigoString.isEmpty()){
                codeEditText.setError(activity.getString(R.string.campoVacio));
                show();
            }else{
                Integer codigo = Integer.parseInt(codigoString);
                consumer.accept(codigo);
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
        builder.setTitle("Confirmación de código");
        builder.setMessage(activity.getString(R.string.mensajeConfirmar));
        builder.setView(codeEditText);
        builder.setPositiveButton("Confirmar", (dialog, which) -> positiveRunnable.run());
        builder.setNegativeButton("Cancelar", (dialog, which) -> negativeRunnable.run());
        builder.setOnCancelListener(dialog -> negativeRunnable.run());

        builder.show();
    }
}
