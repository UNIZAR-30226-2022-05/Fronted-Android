package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import java.util.UUID;

public class BuscarSalaActivity extends AppCompatActivity {

    private UUID sesionID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_sala);
        setTitle(R.string.busquedaDeSalas);

        sesionID = PantallaPrincipalActivity.getSesionID();
    }
}