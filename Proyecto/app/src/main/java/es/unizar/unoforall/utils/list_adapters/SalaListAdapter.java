package es.unizar.unoforall.utils.list_adapters;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import es.unizar.unoforall.R;
import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.CustomActivity;

public class SalaListAdapter extends ArrayAdapter<Map.Entry<UUID, Sala>> {

    private final int resourceLayout;
    private final CustomActivity activity;

    public SalaListAdapter(CustomActivity activity, Map<UUID, Sala> salas){
        super(activity, R.layout.salas_row, new ArrayList<>(salas.entrySet()));
        this.resourceLayout = R.layout.salas_row;
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = convertView;

        if(view == null){
            view = LayoutInflater.from(activity).inflate(resourceLayout, null);
        }

        Map.Entry<UUID, Sala> entry = getItem(position);
        if(entry != null){
            String[] nombreModosJuego = activity.getResources().getStringArray(R.array.modo_juego_filtro_array);

            UUID uuid = entry.getKey();
            Sala sala = entry.getValue();
            int numParticipantes = sala.numParticipantes();
            int maxParticipantes = sala.getConfiguracion().getMaxParticipantes();
            ConfigSala.ModoJuego modoJuego = sala.getConfiguracion().getModoJuego();

            TextView textView1 = view.findViewById(R.id.text1);
            TextView textView2 = view.findViewById(R.id.text2);
            TextView textView3 = view.findViewById(R.id.text3);
            Button button = view.findViewById(R.id.entrar);

            textView1.setText("salaID: " + uuid);
            textView2.setText("Número de jugadores: " + numParticipantes + " / " + maxParticipantes);
            textView3.setText("Modo de juego: " + nombreModosJuego[modoJuego.ordinal()]);
            button.setOnClickListener(v -> {
                BackendAPI api = new BackendAPI(activity);
                api.iniciarUnirseSala(uuid);
            });
        }

        return view;
    }
}
