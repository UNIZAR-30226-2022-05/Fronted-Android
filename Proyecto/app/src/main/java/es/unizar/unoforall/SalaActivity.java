package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.api.WebSocketAPI;
import es.unizar.unoforall.model.salas.Sala;

public class SalaActivity extends AppCompatActivity {

    public static final String KEY_SALA_ID = "id_sala";

    private BackendAPI api;
    private UUID salaID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sala);

        salaID = (UUID) getIntent().getSerializableExtra(KEY_SALA_ID);

        api = new BackendAPI(this);
        api.unirseSala(salaID, sala -> updateWidgets(sala));
    }

    private void updateWidgets(Sala sala){
        Toast.makeText(this, sala.getParticipantes().size() + "", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed(){
        api.salirSala(salaID);
        super.onBackPressed();
    }
}