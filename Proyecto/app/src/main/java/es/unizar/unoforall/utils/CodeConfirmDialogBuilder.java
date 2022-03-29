package es.unizar.unoforall.utils;

import android.app.Activity;
import android.text.InputType;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import java.util.function.Consumer;

import es.unizar.unoforall.R;

//Fuente: https://stackoverflow.com/a/29048271

public class CodeConfirmDialogBuilder {
    private EditText codeEditText;
    private AlertDialog.Builder builder;

    public CodeConfirmDialogBuilder(Activity activity){
        this.codeEditText = new EditText(activity);
        this.codeEditText.setInputType(InputType.TYPE_NUMBER_VARIATION_NORMAL);

        this.builder = new AlertDialog.Builder(activity);
        this.builder.setTitle("Confirmación de código");
        this.builder.setMessage(activity.getString(R.string.mensajeConfirmar));
        this.builder.setView(codeEditText);
    }

    public void setPositiveButton(Consumer<Integer> consumer){
        builder.setPositiveButton("Confirmar", (dialog, which) -> {
            String codigoString = codeEditText.getText().toString();
            if(!codigoString.isEmpty()){
                Integer codigo = Integer.parseInt(codigoString);
                consumer.accept(codigo);
            }
        });
    }

    public void setNegativeButton(Runnable runnable){
        builder.setNegativeButton("Cancelar", (dialog, which) -> runnable.run());
        builder.setOnCancelListener(dialog -> runnable.run());
    }

    public void show(){
        builder.create().show();
    }
}
