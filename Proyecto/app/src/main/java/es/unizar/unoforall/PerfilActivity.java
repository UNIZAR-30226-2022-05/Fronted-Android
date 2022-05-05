package es.unizar.unoforall;

import android.os.Bundle;

import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.utils.ActivityType;
import es.unizar.unoforall.utils.CustomActivity;

public class PerfilActivity extends CustomActivity{

    public static UsuarioVO currentUser = null;
    public static void setCurrentUser(UsuarioVO currentUser){
        PerfilActivity.currentUser = currentUser;
    }

    @Override
    public ActivityType getType(){
        return ActivityType.PERFIL;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        setTitle(R.string.perfil);

        if(currentUser == null){
            mostrarMensaje("El usuario no puede ser nulo");
            return;
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentUser = null;
    }
}