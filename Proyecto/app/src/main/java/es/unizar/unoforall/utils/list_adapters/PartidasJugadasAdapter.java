package es.unizar.unoforall.utils.list_adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import es.unizar.unoforall.PartidaActivity;
import es.unizar.unoforall.R;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partidas.Participante;
import es.unizar.unoforall.model.partidas.PartidaJugadaCompacta;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.utils.FechaUtils;
import es.unizar.unoforall.utils.ImageManager;

public class PartidasJugadasAdapter extends ArrayAdapter<PartidaJugadaCompacta> {

    private final int resourceLayout;
    private final Activity activity;

    public PartidasJugadasAdapter(Activity activity, List<PartidaJugadaCompacta> partidasJugadas){
        super(activity, R.layout.historial_row, partidasJugadas);
        this.resourceLayout = R.layout.historial_row;
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = convertView;

        if(view == null){
            view = LayoutInflater.from(activity).inflate(resourceLayout, null);
        }

        PartidaJugadaCompacta partidaJugada = getItem(position);

        TextView modoJuegoTextView = view.findViewById(R.id.modoJuegoTextView);
        modoJuegoTextView.setText(partidaJugada.getModoJuego().name());
        TextView fechaInicioTextView = view.findViewById(R.id.fechaInicioTextView);
        fechaInicioTextView.setText(FechaUtils.formatDate(partidaJugada.getFechaInicio()));
        TextView fechaFinTextView = view.findViewById(R.id.fechaFinTextView);
        fechaFinTextView.setText(FechaUtils.formatDate(partidaJugada.getFechaFin()));

        LinearLayout[] layoutsPuestos = new LinearLayout[] {
                view.findViewById(R.id.layoutPuesto0),
                view.findViewById(R.id.layoutPuesto1),
                view.findViewById(R.id.layoutPuesto2),
                view.findViewById(R.id.layoutPuesto3)
        };

        System.err.println(partidaJugada.getParticipantes().size());
        System.err.println(partidaJugada.getModoJuego().name() + "\n");
        switch(partidaJugada.getParticipantes().size()){
            case 2:
                layoutsPuestos[2].setVisibility(View.GONE);
            case 3:
                layoutsPuestos[3].setVisibility(View.GONE);
                break;
        }

        TextView[] textViewsNumerosPuestos = new TextView[] {
                view.findViewById(R.id.numeroPuesto0),
                view.findViewById(R.id.numeroPuesto1),
                view.findViewById(R.id.numeroPuesto2),
                view.findViewById(R.id.numeroPuesto3)
        };
        TextView[] textViewsPuestos = new TextView[] {
                view.findViewById(R.id.textViewPuesto0),
                view.findViewById(R.id.textViewPuesto1),
                view.findViewById(R.id.textViewPuesto2),
                view.findViewById(R.id.textViewPuesto3),
        };
        ImageView[] imageViewsPuestos = new ImageView[] {
                view.findViewById(R.id.imageViewPuesto0),
                view.findViewById(R.id.imageViewPuesto1),
                view.findViewById(R.id.imageViewPuesto2),
                view.findViewById(R.id.imageViewPuesto3)
        };

        boolean modoPorParejas = partidaJugada.getModoJuego() == ConfigSala.ModoJuego.Parejas;
        if(modoPorParejas){
            textViewsNumerosPuestos[0].setText("1º");textViewsNumerosPuestos[0].setTextColor(activity.getColor(R.color.color_primer_puesto));
            textViewsNumerosPuestos[1].setText("1º");textViewsNumerosPuestos[1].setTextColor(activity.getColor(R.color.color_primer_puesto));
            textViewsNumerosPuestos[2].setText("2º");textViewsNumerosPuestos[2].setTextColor(activity.getColor(R.color.color_segundo_puesto));
            textViewsNumerosPuestos[3].setText("2º");textViewsNumerosPuestos[3].setTextColor(activity.getColor(R.color.color_segundo_puesto));
        }else{
            textViewsNumerosPuestos[0].setText("1º");textViewsNumerosPuestos[0].setTextColor(activity.getColor(R.color.color_primer_puesto));
            textViewsNumerosPuestos[1].setText("2º");textViewsNumerosPuestos[1].setTextColor(activity.getColor(R.color.color_segundo_puesto));
            textViewsNumerosPuestos[2].setText("3º");textViewsNumerosPuestos[2].setTextColor(activity.getColor(R.color.color_tercer_puesto));
            textViewsNumerosPuestos[3].setText("4º");textViewsNumerosPuestos[3].setTextColor(activity.getColor(R.color.color_cuarto_puesto));
        }

        for(int i=0; i<partidaJugada.getParticipantes().size(); i++){
            Participante participante = partidaJugada.getParticipantes().get(i);
            UsuarioVO usuario = participante.getUsuario();
            int puesto = participante.getPuesto();

            if(usuario != null){
                ImageManager.setImagenPerfil(imageViewsPuestos[puesto-1], usuario.getAvatar());
                textViewsPuestos[puesto-1].setText(usuario.getNombre());
            }else{
                ImageManager.setImagenPerfil(imageViewsPuestos[puesto-1], ImageManager.IA_IMAGE_ID);
                textViewsPuestos[puesto-1].setText(PartidaActivity.getIAName());
            }
        }

        return view;
    }

}
