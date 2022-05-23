package es.unizar.unoforall;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partidas.Jugador;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.ActivityType;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.ImageManager;
import es.unizar.unoforall.utils.SalaReceiver;
import es.unizar.unoforall.utils.dialogs.ReglasViewDialogBuilder;

public class SalaActivity extends CustomActivity implements SalaReceiver {

    private static final int MAX_PARTICIPANTES_SALA = 4;
    private static final int VER_REGLAS_ID = 0;

    private TextView numUsuariosTextView;
    private TextView numUsuariosListosTextView;
    private LinearLayout[] layoutUsuarios;

    private TextView salaTipoTextView;
    
    private BackendAPI api;

    @Override
    public ActivityType getType(){
        return ActivityType.SALA;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sala);

        api = new BackendAPI(this);
        ImageManager.setImagenFondo(findViewById(R.id.mainView), BackendAPI.getUsuario().getAspectoTablero());
        Sala salaActual = BackendAPI.getSalaActual();

        if(salaActual.isEnPausa()){
            setTitle(R.string.salaPausada);
        }else{
            setTitle(R.string.sala);
        }

        TextView salaIDTextView = findViewById(R.id.salaIDTextView);
        salaIDTextView.setText(BackendAPI.getSalaActualID().toString());

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
        
        Button invitarAmigosButton = findViewById(R.id.invitarAmigosButton);
        if(salaActual.isEnPausa()){
            invitarAmigosButton.setEnabled(false);
            invitarAmigosButton.setBackgroundColor(Color.LTGRAY);
        }else{
            invitarAmigosButton.setOnClickListener(view ->
                    api.invitarAmigoSala());
        }
        
        Button volverButton = findViewById(R.id.volverButton);
        if(salaActual.isEnPausa()){
            volverButton.setOnClickListener(view -> api.salirSala());
        }else{
            volverButton.setVisibility(View.INVISIBLE);
        }

        Button listoSala = findViewById(R.id.listoSalaButton);
        listoSala.setOnClickListener(view -> {
            listoSala.setEnabled(false);
            listoSala.setBackgroundColor(Color.LTGRAY);
            if(salaActual.isEnPausa()){
                volverButton.setEnabled(false);
                volverButton.setBackgroundColor(Color.LTGRAY);
            }
            api.listoSala();
        });

        manageSala(salaActual);
    }

    @Override
    public void manageSala(Sala sala){
        if(sala.isEnPartida()){
            Intent intent = new Intent(this, PartidaActivity.class);
            startActivityForResult(intent, 0);
        }else{
            updateWidgets(sala);
        }
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
        int i, numParticipantesListos = 0;

        if(sala.isEnPausa()){
            List<Jugador> listaJugadores = sala.getPartida().getJugadores();
            for(i=0; i<listaJugadores.size(); i++){
                Jugador jugador = listaJugadores.get(i);
                if(jugador.isEsIA()){
                    numParticipantesListos++;
                    setIAData(i);
                }else{
                    UsuarioVO usuario = sala.getParticipante(jugador.getJugadorID());
                    setUserData(i, usuario, participantes.get(usuario));
                    if(participantes.get(usuario)){
                        numParticipantesListos++;
                    }
                }
            }
        }else{
            List<UsuarioVO> usuarios = new ArrayList<>(participantes.keySet());
            usuarios.sort(Comparator.comparing(UsuarioVO::getNombre));
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
        }

        if(sala.isEnPausa()){
            numUsuariosTextView.setText(sala.getConfiguracion().getMaxParticipantes() + " / " + sala.getConfiguracion().getMaxParticipantes());
            numUsuariosListosTextView.setText(numParticipantesListos + " / " + sala.getConfiguracion().getMaxParticipantes());
        }else{
            numUsuariosTextView.setText(sala.numParticipantes() + " / " + sala.getConfiguracion().getMaxParticipantes());
            numUsuariosListosTextView.setText(numParticipantesListos + " / " + sala.numParticipantes());
        }

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
                    ImageManager.setImagenPerfil((ImageView) view, ImageManager.DEFAULT_IMAGE_ID);
                }else{
                    ImageManager.setImagenPerfil((ImageView) view, usuario.getAvatar());
                }
            }
        }
    }
    @SuppressLint("SetTextI18n")
    private void setIAData(int layoutID){
        LinearLayout linearLayout = layoutUsuarios[layoutID];
        for(int i=0; i<linearLayout.getChildCount(); i++){
            View view = linearLayout.getChildAt(i);
            if(view instanceof CheckBox){
                ((CheckBox) view).setChecked(true);
            }else if(view instanceof TextView){
                ((TextView) view).setText(PartidaActivity.getIAName(layoutID));
            }else if(view instanceof ImageView){
                ImageManager.setImagenPerfil((ImageView) view, ImageManager.IA_IMAGE_ID);
            }
        }
    }

    private void salirSala(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(BackendAPI.getSalaActual().isEnPausa()){
            builder.setTitle("Salir de la sala pausada");
            builder.setMessage("¿Quieres salir de la sala pausada?");
            builder.setPositiveButton("Sí", (dialog, which) -> api.salirSalaDefinitivo());
        }else{
            builder.setTitle("Salir de la sala");
            builder.setMessage("¿Quieres salir de la sala?");
            builder.setPositiveButton("Sí", (dialog, which) -> api.salirSala());
        }        
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, VER_REGLAS_ID, Menu.NONE, "Ver reglas de la sala");
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case VER_REGLAS_ID:
                ReglasViewDialogBuilder builder = new ReglasViewDialogBuilder(this, BackendAPI.getSalaActual().getConfiguracion());
                builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Sala salaActual = BackendAPI.getSalaActual();
        if(salaActual == null){
            super.onBackPressed();
            return;
        }

        if(salaActual.isEnPausa()){
            UsuarioVO usuarioVO = BackendAPI.getUsuario();
            boolean usuarioListo = salaActual.getParticipantes().getOrDefault(usuarioVO, false);
            if(usuarioListo){
                salirSala();
            }else{
                api.salirSala();
            }
        }else{
            salirSala();
        }
    }
}