package es.unizar.unoforall.utils;

import android.app.Activity;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.ReglasEspeciales;

public class FilterSearchDialogBuilder {

    private final Activity activity;

    private final TextView participantes;
    private final RadioGroup grupo1;
    private final RadioButton dos;
    private final RadioButton tres;
    private final RadioButton cuatro;

    private final TextView privadaPublica;
    private final RadioGroup grupo2;
    private final RadioButton publica;
    private final RadioButton privada;

    private final TextView cartasEspeciales;
    private final LinearLayout linearLayout1;
    private final CheckBox rayosX;
    private final CheckBox intercambio;
    private final CheckBox x2;

    private final TextView modoJuego;
    private final Spinner seleccion;

    private final TextView reglasAdicionales;
    private final LinearLayout linearLayout2;
    private final CheckBox acumular;
    private final CheckBox redirigir;
    private final CheckBox apilarVarias;
    private final CheckBox penalizar;

    private ConfigSala configSala;
    private ReglasEspeciales reglasEspeciales;

    private Runnable positiveRunnable;
    private Runnable negativeRunnable;

    public FilterSearchDialogBuilder(Activity activity){
        this.activity = activity;

        this.participantes = new TextView(activity);
        this.participantes.setText("Participantes");

        this.grupo1 = new RadioGroup(activity);
        this.dos = new RadioButton(activity);
        this.dos.setText("2");
        this.tres = new RadioButton(activity);
        this.tres.setText("3");
        this.cuatro = new RadioButton(activity);
        this.cuatro.setText("4");
        this.grupo1.setOrientation(LinearLayout.HORIZONTAL);
        this.grupo1.addView(this.dos);
        this.grupo1.addView(this.tres);
        this.grupo1.addView(this.cuatro);

    }
}
