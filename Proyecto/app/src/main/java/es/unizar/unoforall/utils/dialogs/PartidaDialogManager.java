package es.unizar.unoforall.utils.dialogs;

import androidx.appcompat.app.AlertDialog;

public class PartidaDialogManager {
    private static AlertDialog dialog = null;

    protected static void setCurrentDialog(AlertDialog dialog){
        PartidaDialogManager.dialog = dialog;
    }

    protected static AlertDialog getCurrentDialog(){
        return dialog;
    }

    public static void dismissCurrentDialog(){
        if(dialog != null && dialog.isShowing()){
            dialog.dismiss();
        }
    }
}
