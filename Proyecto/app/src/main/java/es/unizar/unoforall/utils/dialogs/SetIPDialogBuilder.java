package es.unizar.unoforall.utils.dialogs;

import android.app.Activity;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import java.util.function.Consumer;

import es.unizar.unoforall.R;

public class SetIPDialogBuilder{
    private final Activity activity;
    private final EditText ipEditText;

    private Runnable positiveRunnable;
    private Runnable negativeRunnable;

    public SetIPDialogBuilder(Activity activity){
        this.activity = activity;

        this.ipEditText = new EditText(activity);

        this.positiveRunnable = () -> {};
        this.negativeRunnable = () -> {};
    }

    public void setPositiveButton(Consumer<String> consumer){
        this.positiveRunnable = () -> {
            String ip = ipEditText.getText().toString();
            if(ip.isEmpty()){
                ipEditText.setError(activity.getString(R.string.campoVacio));
                show();
            }else{
                consumer.accept(ip);
            }
        };
    }

    public void setNegativeButton(Runnable runnable){
        this.negativeRunnable = runnable;
    }

    public void show(){
        ViewParent parent = ipEditText.getParent();
        if(parent != null){
            ((ViewGroup) parent).removeView(ipEditText);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("IP del servidor");
        builder.setMessage("Introduzca la IP del servidor");
        builder.setView(ipEditText);
        builder.setPositiveButton("Confirmar", (dialog, which) -> positiveRunnable.run());
        builder.setNegativeButton("Cancelar", (dialog, which) -> negativeRunnable.run());
        builder.setOnCancelListener(dialog -> negativeRunnable.run());

        builder.show();
    }
}
