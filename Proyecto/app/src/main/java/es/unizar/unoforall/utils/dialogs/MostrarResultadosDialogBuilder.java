package es.unizar.unoforall.utils.dialogs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import es.unizar.unoforall.PartidaActivity;
import es.unizar.unoforall.R;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partidas.Participante;
import es.unizar.unoforall.model.partidas.PartidaJugada;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.ImageManager;

public class MostrarResultadosDialogBuilder {
    private final CustomActivity activity;

    private View mainView;

    private TextView[] textViewsNumerosPuestos;
    private TextView[] textViewsPuestos;
    private TextView[] textViewsPuntos;
    private ImageView[] imageViewsPuestos;

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

        textViewsNumerosPuestos = new TextView[] {
                this.mainView.findViewById(R.id.numeroPuesto0),
                this.mainView.findViewById(R.id.numeroPuesto1),
                this.mainView.findViewById(R.id.numeroPuesto2),
                this.mainView.findViewById(R.id.numeroPuesto3)
        };
        textViewsPuestos = new TextView[] {
                this.mainView.findViewById(R.id.textViewPuesto0),
                this.mainView.findViewById(R.id.textViewPuesto1),
                this.mainView.findViewById(R.id.textViewPuesto2),
                this.mainView.findViewById(R.id.textViewPuesto3),
        };
        textViewsPuntos = new TextView[] {
                this.mainView.findViewById(R.id.textViewPuntos0),
                this.mainView.findViewById(R.id.textViewPuntos1),
                this.mainView.findViewById(R.id.textViewPuntos2),
                this.mainView.findViewById(R.id.textViewPuntos3)
        };
        imageViewsPuestos = new ImageView[] {
                this.mainView.findViewById(R.id.imageViewPuesto0),
                this.mainView.findViewById(R.id.imageViewPuesto1),
                this.mainView.findViewById(R.id.imageViewPuesto2),
                this.mainView.findViewById(R.id.imageViewPuesto3)
        };

        boolean modoPorParejas = sala.getConfiguracion().getModoJuego() == ConfigSala.ModoJuego.Parejas;
        if(modoPorParejas){
            textViewsNumerosPuestos[0].setText("1º");textViewsNumerosPuestos[0].setTextColor(activity.getColor(R.color.color_primer_puesto));
            textViewsNumerosPuestos[1].setText("1º");textViewsNumerosPuestos[1].setTextColor(activity.getColor(R.color.color_primer_puesto));
            textViewsNumerosPuestos[2].setText("2º");textViewsNumerosPuestos[2].setTextColor(activity.getColor(R.color.color_segundo_puesto));
            textViewsNumerosPuestos[3].setText("2º");textViewsNumerosPuestos[3].setTextColor(activity.getColor(R.color.color_segundo_puesto));
        }

        for(int i=0; i<partidaJugada.getParticipantes().size(); i++){
            Participante participante = partidaJugada.getParticipantes().get(i);
            UsuarioVO usuario = participante.getUsuario();
            int puntos = participante.getPuntos();
            int puesto = participante.getPuesto();

            setDatosUsuario(puesto, puntos, usuario);
        }
    }

    private void setDatosUsuario(int puesto, int puntos, UsuarioVO usuario){
        if(usuario != null){
            ImageManager.setImagenPerfil(imageViewsPuestos[puesto-1], usuario.getAvatar());
            textViewsPuestos[puesto-1].setText(usuario.getNombre());
            textViewsPuntos[puesto-1].setText(puntos + " puntos");
        }else{
            ImageManager.setImagenPerfil(imageViewsPuestos[puesto-1], ImageManager.IA_IMAGE_ID);
            textViewsPuestos[puesto-1].setText(PartidaActivity.getIAName());
            textViewsPuntos[puesto-1].setVisibility(View.GONE);
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
