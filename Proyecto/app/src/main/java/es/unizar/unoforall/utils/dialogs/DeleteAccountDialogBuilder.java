package es.unizar.unoforall.utils.dialogs;

import android.app.Activity;
import android.app.AlertDialog;

public class DeleteAccountDialogBuilder{
    private final Activity activity;

    private Runnable positiveRunnable;
    private Runnable negativeRunnable;

    public DeleteAccountDialogBuilder(Activity activity){
        this.activity = activity;

        this.positiveRunnable = () -> {};
        this.negativeRunnable = () -> {};
    }

    public void setPositiveRunnable(Runnable runnable){
        this.positiveRunnable = runnable;
    }

    public void setNegativeButton(Runnable runnable){
        this.negativeRunnable = runnable;
    }

    public void show(){
        AlertDialog.Builder builder2 = new AlertDialog.Builder(activity);
        builder2.setTitle("Confirmar borrado");
        builder2.setMessage("¿Está seguro?\nEsta acción no se puede deshacer");
        builder2.setPositiveButton("Borrar cuenta", (dialog, which) -> positiveRunnable.run());
        builder2.setNegativeButton("Cancelar", ((dialog, which) -> negativeRunnable.run()));

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Borrar cuenta");
        builder.setMessage("¿Quiere borrar su cuenta?");
        builder.setPositiveButton("Sí", ((dialog, which) -> builder2.show()));
        builder.setNegativeButton("No", ((dialog, which) -> negativeRunnable.run()));
        builder.setOnCancelListener(dialog -> negativeRunnable.run());

        builder.show();
    }
}
