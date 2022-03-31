package es.unizar.unoforall;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.api.WebSocketAPI;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.salas.Sala;

public class SalaActivity extends AppCompatActivity {

    public static final String KEY_SALA_ID = "id_sala";
    private static final int MAX_PARTICIPANTES_SALA = 4;

    private BackendAPI api;
    private UUID salaID;

    private TextView numUsuariosTextView;
    private LinearLayout[] layoutUsuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sala);
        setTitle(R.string.sala);

        salaID = (UUID) getIntent().getSerializableExtra(KEY_SALA_ID);

        TextView salaIDTextView = findViewById(R.id.salaIDTextView);
        salaIDTextView.setText(salaID.toString());

        numUsuariosTextView = findViewById(R.id.numUsuariosTextView);
        layoutUsuarios = new LinearLayout[] {
                findViewById(R.id.layoutUsuario1),
                findViewById(R.id.layoutUsuario2),
                findViewById(R.id.layoutUsuario3),
                findViewById(R.id.layoutUsuario4)
        };

        Button abandonarSalaButton = findViewById(R.id.abandonarSalaButton);
        abandonarSalaButton.setOnClickListener(view -> salirSala());

        Button listoSala = findViewById(R.id.listoSalaButton);
        listoSala.setOnClickListener(view -> {
            listoSala.setEnabled(false);
            listoSala.setBackgroundColor(Color.LTGRAY);
            api.listoSala(salaID);
        });

        api = new BackendAPI(this);
        api.unirseSala(salaID, sala -> updateWidgets(sala));
    }

    @SuppressLint("SetTextI18n")
    private void updateWidgets(Sala sala){
        numUsuariosTextView.setText(sala.numParticipantes() + " / " + sala.configuracion.getMaxParticipantes());

        for(int i=0; i<MAX_PARTICIPANTES_SALA; i++){
            if(i < sala.configuracion.getMaxParticipantes()){
                layoutUsuarios[i].setVisibility(View.VISIBLE);
            }else{
                layoutUsuarios[i].setVisibility(View.GONE);
            }
        }

        Map<UsuarioVO, Boolean> participantes = sala.getParticipantes();
        List<UsuarioVO> usuarios = new ArrayList<>(participantes.keySet());
        usuarios.sort(Comparator.comparing(UsuarioVO::getId));
        int i;
        for(i=0; i<sala.configuracion.getMaxParticipantes(); i++){
            if(i < usuarios.size()){
                UsuarioVO usuario = usuarios.get(i);
                setUserData(i, usuario, participantes.get(usuario));
            }else{
                setUserData(i, null, false);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void setUserData(int layoutID, UsuarioVO usuario, boolean listo){
        LinearLayout linearLayout = layoutUsuarios[layoutID];
        for(int i=0; i<linearLayout.getChildCount(); i++){
            View view = linearLayout.getChildAt(i);
            if(view instanceof CheckBox){
                ((CheckBox) view).setChecked(listo);
            }else if(view instanceof TextView){
                if(usuario == null){
                    ((TextView) view).setText("Esperando al jugador " + (layoutID+1) + "...");
                }else{
                    ((TextView) view).setText(usuario.getNombre());
                }
            }
        }
    }

    private void salirSala(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Salir de la sala");
        builder.setMessage("¿Quieres salir de la sala?");
        builder.setPositiveButton("Sí", (dialog, which) -> {
            api.salirSala(salaID);
            super.onBackPressed();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }

    @Override
    public void onBackPressed(){
        salirSala();
    }
}