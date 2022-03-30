package es.unizar.unoforall.utils;

//https://stackoverflow.com/questions/19466757/hashmap-to-listview

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import es.unizar.unoforall.R;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.Sala;

public class MyAdapter extends BaseAdapter {
    private ArrayList<UUID> claves;
    private ArrayList<Sala> valores;

    public MyAdapter(ArrayList<UUID> lista1, ArrayList<Sala> lista2) {
        claves = lista1;
        valores = lista2;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }


    @Override
    public long getItemId(int position) {
        // TODO implement you own logic with ID
        return 0;
    }

    public Sala getItemLista2(int position){
        return valores.get(position);
    }

    public UUID getItemLista1(int position){
        return claves.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View result;

        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.salas_row, parent, false);
        } else {
            result = convertView;
        }

        UUID dato1 = getItemLista1(position);
        Sala miSala = getItemLista2(position);
        int dato2 = miSala.configuracion.getMaxParticipantes();
        ConfigSala.ModoJuego dato3 = miSala.configuracion.getModoJuego();


        // TODO replace findViewById by ViewHolder
        ((TextView) result.findViewById(R.id.text1)).setText(dato1.toString());
        ((TextView) result.findViewById(R.id.text2)).setText(dato2);
        ((TextView) result.findViewById(R.id.text3)).setText(dato3.toString());
        Button botonEntrar = result.findViewById(R.id.entrar);
        botonEntrar.setOnClickListener(view -> {
            //abrir web sockets, solicitar unirse e ir a la sala de espera de jugadores
        });

        return result;
    }
}