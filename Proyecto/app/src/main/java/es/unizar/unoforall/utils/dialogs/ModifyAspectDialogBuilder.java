package es.unizar.unoforall.utils.dialogs;

import android.app.Activity;

import es.unizar.unoforall.model.UsuarioVO;

public class ModifyAspectDialogBuilder {

    private final Activity activity;
    private UsuarioVO usuarioVO;

    private Runnable positiveRunnable;
    private Runnable negativeRunnable;

    public ModifyAspectDialogBuilder(Activity activity, UsuarioVO usuarioVO) {
        this.activity = activity;
        this.usuarioVO = usuarioVO;
    }

}
