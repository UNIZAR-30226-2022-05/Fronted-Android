package es.unizar.unoforall.utils.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;

import java.util.UUID;
import java.util.function.Consumer;

import es.unizar.unoforall.R;

public class SalaIDSearchDialogBuilder{
    private final Activity activity;
    private final EditText salaIDEditText;

    private Runnable positiveRunnable;
    private Runnable negativeRunnable;

    public SalaIDSearchDialogBuilder(Activity activity){
        this.activity = activity;

        this.salaIDEditText = new EditText(activity);
        this.salaIDEditText.setHint("ID de la sala");

        this.positiveRunnable = () -> {};
        this.negativeRunnable = () -> {};
    }

    public void setError(String error){
        this.salaIDEditText.setError(error);
    }

    public void setPositiveButton(Consumer<UUID> consumer){
        this.positiveRunnable = () -> {
            String salaIDString = salaIDEditText.getText().toString();
            if(salaIDString.isEmpty()){
                setError(activity.getString(R.string.campoVacio));
                show();
            }else{
                try{
                    UUID salaID = UUID.fromString(salaIDString);
                    consumer.accept(salaID);
                }catch(IllegalArgumentException ex){
                    setError(activity.getString(R.string.salaIDinvalido));
                    show();
                }
            }
        };
    }

    public void setNegativeButton(Runnable runnable){
        this.negativeRunnable = runnable;
    }

    public void show(){
        ViewParent parent = salaIDEditText.getParent();
        if(parent != null){
            ((ViewGroup) parent).removeView(salaIDEditText);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Búsqueda de sala por ID");
        builder.setMessage("Introduzca el ID de la sala que desea buscar");
        builder.setView(salaIDEditText);
        builder.setPositiveButton("Buscar", (dialog, which) -> positiveRunnable.run());
        builder.setNegativeButton("Cancelar", (dialog, which) -> negativeRunnable.run());
        builder.setOnCancelListener(dialog -> negativeRunnable.run());

        builder.show();
    }
}
