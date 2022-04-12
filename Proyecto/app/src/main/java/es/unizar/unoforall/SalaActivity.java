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
import android.widget.ImageView;
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
import es.unizar.unoforall.utils.ImageManager;

public class SalaActivity extends AppCompatActivity {

    public static final String KEY_SALA_ID = "id_sala";
    private static final int MAX_PARTICIPANTES_SALA = 4;

    private BackendAPI api;
    private UUID salaID;

    private TextView numUsuariosTextView;
    private TextView numUsuariosListosTextView;
    private LinearLayout[] layoutUsuarios;

    private TextView salaTipoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sala);
        setTitle(R.string.sala);

        salaID = (UUID) getIntent().getSerializableExtra(KEY_SALA_ID);

        TextView salaIDTextView = findViewById(R.id.salaIDTextView);
        salaIDTextView.setText(salaID.toString());

        salaTipoTextView = findViewById(R.id.salaTipoTextView);

        numUsuariosTextView = findViewById(R.id.numUsuariosTextView);
        numUsuariosListosTextView = findViewById(R.id.numUsuariosListosTextView);
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

        Button invitarAmigos = findViewById(R.id.invitarAmigosButton);
        invitarAmigos.setOnClickListener(view -> {
            new BackendAPI(this).invitarAmigoSala(salaID);
        });

        api = new BackendAPI(this);
        api.unirseSala(salaID, sala -> updateWidgets(sala));
    }

    @SuppressLint("SetTextI18n")
    private void updateWidgets(Sala sala){
        for(int i=0; i<MAX_PARTICIPANTES_SALA; i++){
            if(i < sala.getConfiguracion().getMaxParticipantes()){
                layoutUsuarios[i].setVisibility(View.VISIBLE);
            }else{
                layoutUsuarios[i].setVisibility(View.GONE);
            }
        }

        Map<UsuarioVO, Boolean> participantes = sala.getParticipantes();
        List<UsuarioVO> usuarios = new ArrayList<>(participantes.keySet());
        usuarios.sort(Comparator.comparing(UsuarioVO::getNombre));
        int i, numParticipantesListos = 0;
        for(i=0; i<sala.getConfiguracion().getMaxParticipantes(); i++){
            if(i < usuarios.size()){
                UsuarioVO usuario = usuarios.get(i);
                setUserData(i, usuario, participantes.get(usuario));
                if(participantes.get(usuario)){
                    numParticipantesListos++;
                }
            }else{
                setUserData(i, null, false);
            }
        }

        numUsuariosTextView.setText(sala.numParticipantes() + " / " + sala.getConfiguracion().getMaxParticipantes());
        numUsuariosListosTextView.setText(numParticipantesListos + " / " + sala.numParticipantes());

        if(sala.getConfiguracion().isEsPublica()){
            salaTipoTextView.setText("pública");
        }else{
            salaTipoTextView.setText("privada");
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
            }else if(view instanceof ImageView){
                if(usuario == null){
                    ImageManager.setImage((ImageView) view, ImageManager.DEFAULT_IMAGE_ID);
                }else{
                    ImageManager.setImage((ImageView) view, usuario.getAvatar());
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