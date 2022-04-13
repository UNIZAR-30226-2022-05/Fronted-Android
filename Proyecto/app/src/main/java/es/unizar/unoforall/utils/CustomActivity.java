package es.unizar.unoforall.utils;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import es.unizar.unoforall.api.BackendAPI;

public abstract class CustomActivity extends AppCompatActivity {

    public abstract ActivityType getType();

    public void mostrarMensaje(String mensaje){
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
        BackendAPI.setCurrentActivity(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BackendAPI.setCurrentActivity(this);
    }

}
