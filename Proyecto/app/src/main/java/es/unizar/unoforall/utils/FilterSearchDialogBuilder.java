package es.unizar.unoforall.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.function.Consumer;

import es.unizar.unoforall.R;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.ReglasEspeciales;

public class FilterSearchDialogBuilder{
    private static final int PADDING = 25;
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

    private final LinearLayout layoutGlobal;

    private ConfigSala configSala;
    private ReglasEspeciales reglasEspeciales;

    private Runnable positiveRunnable;
    private Runnable negativeRunnable;

    public FilterSearchDialogBuilder(Activity activity){
        this.activity = activity;

        this.configSala = new ConfigSala();
        this.reglasEspeciales = configSala.getReglas();

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

        this.privadaPublica = new TextView(activity);
        this.privadaPublica.setText("Sala pública o privada");

        this.grupo2 = new RadioGroup(activity);
        this.publica = new RadioButton(activity);
        this.publica.setText("pública");
        this.privada = new RadioButton(activity);
        this.privada.setText("privada");
        this.grupo2.setOrientation(LinearLayout.HORIZONTAL);
        this.grupo2.addView(this.publica);
        this.grupo2.addView(this.privada);

        this.cartasEspeciales = new TextView(activity);
        this.cartasEspeciales.setText("Pulsa para añadir cartas especiales");

        this.linearLayout1 = new LinearLayout(activity);
        this.linearLayout1.setOrientation(LinearLayout.HORIZONTAL);
        this.linearLayout1.setPadding(PADDING, PADDING, PADDING, PADDING);
        this.rayosX = new CheckBox(activity);
        this.rayosX.setText("RayosX");
        rayosX.setChecked(reglasEspeciales.isCartaRayosX());
        rayosX.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setCartaRayosX(isChecked));
        this.intercambio = new CheckBox(activity);
        this.intercambio.setText("Intercambio");
        intercambio.setChecked(reglasEspeciales.isCartaIntercambio());
        intercambio.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setCartaIntercambio(isChecked));
        this.x2 = new CheckBox(activity);
        this.x2.setText("x2");
        x2.setChecked(reglasEspeciales.isCartaX2());
        x2.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setCartaX2(isChecked));
        this.linearLayout1.addView(rayosX);
        this.linearLayout1.addView(intercambio);
        this.linearLayout1.addView(x2);

        this.modoJuego = new TextView(activity);
        this.modoJuego.setText("Elige un modo de juego");

        this.seleccion = new Spinner(activity);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity,
                R.array.modo_juego_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.seleccion.setAdapter(adapter);
        this.seleccion.setSelection(0);
        seleccion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                configSala.setModoJuego(ConfigSala.ModoJuego.values()[i]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        this.reglasAdicionales = new TextView(activity);
        this.reglasAdicionales.setText("Elige reglas adicionales");

        this.linearLayout2 = new LinearLayout(activity);
        this.linearLayout2.setOrientation(LinearLayout.VERTICAL);
        this.acumular = new CheckBox(activity);
        this.acumular.setText("Acumular efecto de +2 y +4");
        acumular.setChecked(reglasEspeciales.isEncadenarRoboCartas());
        acumular.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setEncadenarRoboCartas(isChecked));
        this.redirigir = new CheckBox(activity);
        this.redirigir.setText("Redirigir efecto de +2 y +4");
        redirigir.setChecked(reglasEspeciales.isRedirigirRoboCartas());
        redirigir.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setRedirigirRoboCartas(isChecked));
        this.apilarVarias = new CheckBox(activity);
        this.apilarVarias.setText("Permite escalera y jugar varias del mismo numero");
        apilarVarias.setChecked(reglasEspeciales.isJugarVariasCartas());
        apilarVarias.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setJugarVariasCartas(isChecked));
        this.penalizar = new CheckBox(activity);
        this.penalizar.setText("Penaliza si ultima carta es +4 o cambio de color");
        penalizar.setChecked(reglasEspeciales.isEvitarEspecialFinal());
        penalizar.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setEvitarEspecialFinal(isChecked));
        this.linearLayout1.addView(acumular);
        this.linearLayout1.addView(redirigir);
        this.linearLayout1.addView(apilarVarias);
        this.linearLayout1.addView(penalizar);

        this.layoutGlobal = new LinearLayout(activity);
        this.layoutGlobal.setOrientation(LinearLayout.VERTICAL);
        this.layoutGlobal.addView(participantes);
        this.layoutGlobal.addView(grupo1);
        this.layoutGlobal.addView(privadaPublica);
        this.layoutGlobal.addView(grupo2);
        this.layoutGlobal.addView(cartasEspeciales);
        this.layoutGlobal.addView(linearLayout1);
        this.layoutGlobal.addView(modoJuego);
        this.layoutGlobal.addView(seleccion);
        this.layoutGlobal.addView(reglasAdicionales);
        this.layoutGlobal.addView(linearLayout2);



        this.positiveRunnable = () -> {};
        this.negativeRunnable = () -> {};
    }

    public void setPositiveButton(Consumer<ConfigSala> consumer){
        this.positiveRunnable = () -> {

            if(publica.isChecked()){
                configSala.setEsPublica(true);
            }
            if(privada.isChecked()){
                configSala.setEsPublica(false);
            }

            if(dos.isChecked()){
                configSala.setMaxParticipantes(2);
            }
            if(tres.isChecked()){
                configSala.setMaxParticipantes(3);
            }
            if(cuatro.isChecked()){
                configSala.setMaxParticipantes(4);
            }

            configSala.setReglas(reglasEspeciales);

            consumer.accept(configSala);
        };
    }

    public void setNegativeButton(Runnable runnable){
        this.negativeRunnable = runnable;
    }

    public void show(){
        ViewParent parent = layoutGlobal.getParent();
        if(parent != null){
            ((ViewGroup) parent).removeView(layoutGlobal);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Busqueda Avanzada");
        builder.setMessage("Seleccione los filtros de búsqueda");
        builder.setView(layoutGlobal);
        builder.setPositiveButton("Confirmar", (dialog, which) -> positiveRunnable.run());
        builder.setNegativeButton("Cancelar", (dialog, which) -> negativeRunnable.run());
        builder.setOnCancelListener(dialog -> negativeRunnable.run());

        builder.show();
    }

}
