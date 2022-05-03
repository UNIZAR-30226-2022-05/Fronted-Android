package es.unizar.unoforall;

import android.os.Bundle;

import es.unizar.unoforall.utils.ActivityType;
import es.unizar.unoforall.utils.CustomActivity;

public class PerfilActivity extends CustomActivity{

    @Override
    public ActivityType getType(){
        return ActivityType.PERFIL;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        setTitle(R.string.perfil);
    }
}