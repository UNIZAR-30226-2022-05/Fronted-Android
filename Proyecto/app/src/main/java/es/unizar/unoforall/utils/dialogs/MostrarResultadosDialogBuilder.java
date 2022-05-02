package es.unizar.unoforall.utils.dialogs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import org.w3c.dom.Text;

import java.util.Comparator;

import es.unizar.unoforall.PartidaActivity;
import es.unizar.unoforall.R;
import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.api.Serializar;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partidas.Jugada;
import es.unizar.unoforall.model.partidas.Participante;
import es.unizar.unoforall.model.partidas.PartidaJugada;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.ImageManager;

public class MostrarResultadosDialogBuilder {
    private final CustomActivity activity;

    private View mainView;

    private Runnable positiveRunnable = () -> {};
    private Runnable negativeRunnable = () -> {};

    public MostrarResultadosDialogBuilder(CustomActivity activity, Sala sala){
        this.activity = activity;

        if(sala == null || sala.getUltimaPartidaJugada() == null){
            return;
        }

        this.mainView = LayoutInflater.from(activity).inflate(R.layout.mostrar_resultados, null);

        PartidaJugada partidaJugada = sala.getUltimaPartidaJugada();

        // Generar view con los datos de partidaJugada
        LinearLayout[] layoutsPuestos = new LinearLayout[] {
                this.mainView.findViewById(R.id.layoutPuesto0),
                this.mainView.findViewById(R.id.layoutPuesto1),
                this.mainView.findViewById(R.id.layoutPuesto2),
                this.mainView.findViewById(R.id.layoutPuesto3)
        };

        switch(partidaJugada.getParticipantes().size()){
            case 2:
                layoutsPuestos[2].setVisibility(View.GONE);
            case 3:
                layoutsPuestos[3].setVisibility(View.GONE);
                break;
        }

        TextView[] textViewsPuestos = new TextView[] {
                this.mainView.findViewById(R.id.textViewPuesto0),
                this.mainView.findViewById(R.id.textViewPuesto1),
                this.mainView.findViewById(R.id.textViewPuesto2),
                this.mainView.findViewById(R.id.textViewPuesto3),
        };
        TextView[] textViewsPuntos = new TextView[] {
                this.mainView.findViewById(R.id.textViewPuntos0),
                this.mainView.findViewById(R.id.textViewPuntos1),
                this.mainView.findViewById(R.id.textViewPuntos2),
                this.mainView.findViewById(R.id.textViewPuntos3)
        };
        ImageView[] imageViewsPuestos = new ImageView[] {
                this.mainView.findViewById(R.id.imageViewPuesto0),
                this.mainView.findViewById(R.id.imageViewPuesto1),
                this.mainView.findViewById(R.id.imageViewPuesto2),
                this.mainView.findViewById(R.id.imageViewPuesto3)
        };

        for(int i=0; i<partidaJugada.getParticipantes().size(); i++){
            Participante participante = partidaJugada.getParticipantes().get(i);
            UsuarioVO usuario = participante.getUsuario();
            int puntos = participante.getPuntos();
            int puesto = participante.getPuesto();
            String nombre;
            int imageID;
            if(usuario == null){
                // Es una IA
                nombre = PartidaActivity.getIAName();
                imageID = ImageManager.IA_IMAGE_ID;
            }else{
                nombre = usuario.getNombre();
                imageID = usuario.getAvatar();
            }

            ImageManager.setImagenPerfil(imageViewsPuestos[puesto-1], imageID);
            textViewsPuestos[puesto-1].setText(nombre);
            if(usuario != null){
                textViewsPuntos[puesto-1].setText(puntos + " puntos");
            }else{
                textViewsPuntos[puesto-1].setVisibility(View.GONE);
            }
        }
    }

    public void setPositiveButton(Runnable positiveRunnable){
        this.positiveRunnable = positiveRunnable;
    }

    public void setNegativeButton(Runnable negativeRunnable){
        this.negativeRunnable = negativeRunnable;
    }

    public void show(){
        ViewParent parent = mainView.getParent();
        if(parent != null){
            ((ViewGroup) parent).removeView(mainView);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Resultados de la partida");
        builder.setView(mainView);
        builder.setPositiveButton("Continuar", (dialog, which) -> positiveRunnable.run());
        builder.setNegativeButton("Salir", (dialog, which) -> negativeRunnable.run());
        builder.setOnCancelListener(dialog -> show());
        builder.show();
    }
}
