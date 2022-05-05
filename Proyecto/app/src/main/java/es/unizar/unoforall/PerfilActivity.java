package es.unizar.unoforall;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.PartidasAcabadasVO;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partidas.Participante;
import es.unizar.unoforall.model.partidas.PartidaJugada;
import es.unizar.unoforall.model.partidas.PartidaJugadaCompacta;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.utils.ActivityType;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.ImageManager;
import es.unizar.unoforall.utils.list_adapters.PartidasJugadasAdapter;

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

        ImageView iconoUsuarioImageView = findViewById(R.id.iconoImageView);
        TextView nombreUsuarioTextView = findViewById(R.id.nombreUsuarioTextView);
        TextView correoUsuarioTextView = findViewById(R.id.correoUsuarioTextView);
        TextView jugadasTextView = findViewById(R.id.jugadasTextView);
        TextView ganadasTextView = findViewById(R.id.ganadasTextView);
        TextView puntosTextView = findViewById(R.id.puntosTextView);

        ImageManager.setImagenPerfil(iconoUsuarioImageView, currentUser.getAvatar());
        nombreUsuarioTextView.setText(currentUser.getNombre());
        correoUsuarioTextView.setText(currentUser.getCorreo());
        jugadasTextView.setText("Jugadas: " + currentUser.getTotalPartidas());
        ganadasTextView.setText("Ganadas: " + currentUser.getNumVictorias());
        puntosTextView.setText("Puntos: " + currentUser.getPuntos());

        TextView noHayDatosTextView = findViewById(R.id.noHayDatosTextView);
        ListView historialListView = findViewById(R.id.historialListView);

        new BackendAPI(this).obtenerHistorial(currentUser, partidasJugadas -> {
            if(partidasJugadas.isEmpty()){
                noHayDatosTextView.setVisibility(View.VISIBLE);
                historialListView.setVisibility(View.GONE);
            }else{
                noHayDatosTextView.setVisibility(View.GONE);
                historialListView.setVisibility(View.VISIBLE);
                historialListView.setAdapter(new PartidasJugadasAdapter(this, partidasJugadas));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentUser = null;
    }
}