package es.unizar.unoforall.utils.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;

import java.util.function.Consumer;

import es.unizar.unoforall.R;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.ReglasEspeciales;

public class ReglasViewDialogBuilder {
    private static final int PADDING = 25;
    private final Activity activity;

    private final View mainView;
    private final LinearLayout filtrosAdicionalesLinearLayout;

    private final RadioButton radio2;
    private final RadioButton radio3;
    private final RadioButton radio4;
    private final RadioButton radioCualquiera;

    private final Spinner modoJuegoSpinner;

    private final CheckBox rayosXCheckBox;
    private final CheckBox intercambioCheckBox;
    private final CheckBox x2CheckBox;

    private final CheckBox encadenar_2_4Checkbox;
    private final CheckBox redirigir_2_4CheckBox;
    private final CheckBox jugarVariasCartasCheckbox;
    private final CheckBox penalizacion_4_colorCheckbox;

    public ReglasViewDialogBuilder(Activity activity, ConfigSala configSala){
        this.activity = activity;

        this.mainView = LayoutInflater.from(activity).inflate(R.layout.dialog_busqueda_salas, null);

        TextView textView = this.mainView.findViewById(R.id.numParticipantesTextView);
        textView.setText("Número de participantes");
        textView = this.mainView.findViewById(R.id.modoJuegoTextView);
        textView.setText("Modo de juego");
        textView = this.mainView.findViewById(R.id.cartasEspecialesTextView);
        textView.setText("Cartas especiales");
        textView = this.mainView.findViewById(R.id.reglasAdicionalesTextView);
        textView.setText("Reglas adicionales");

        this.filtrosAdicionalesLinearLayout = this.mainView.findViewById(R.id.filtrosAdicionalesLinearLayout);
        this.filtrosAdicionalesLinearLayout.setVisibility(View.VISIBLE);

        this.radio2 = this.mainView.findViewById(R.id.buscarSala_radio_dos);this.radio2.setEnabled(false);
        this.radio3 = this.mainView.findViewById(R.id.buscarSala_radio_tres);this.radio3.setEnabled(false);
        this.radio4 = this.mainView.findViewById(R.id.buscarSala_radio_cuatro);this.radio4.setEnabled(false);
        this.radioCualquiera = this.mainView.findViewById(R.id.buscarSala_radio_cualquiera);
        this.radioCualquiera.setEnabled(false);
        this.radioCualquiera.setVisibility(View.GONE);

        this.modoJuegoSpinner = this.mainView.findViewById(R.id.modo_juego_spinner);
        this.modoJuegoSpinner.setEnabled(false);

        this.rayosXCheckBox = this.mainView.findViewById(R.id.buscarSala_checkbox_rayosX);this.rayosXCheckBox.setEnabled(false);
        this.intercambioCheckBox = this.mainView.findViewById(R.id.buscarSala_checkbox_intercambio);this.intercambioCheckBox.setEnabled(false);
        this.x2CheckBox = this.mainView.findViewById(R.id.buscarSala_checkbox_x2);this.x2CheckBox.setEnabled(false);

        this.encadenar_2_4Checkbox = this.mainView.findViewById(R.id.buscarSala_checkbox_encadenar_2_4);this.encadenar_2_4Checkbox.setEnabled(false);
        this.redirigir_2_4CheckBox = this.mainView.findViewById(R.id.buscarSala_checkbox_redirigir_2_4);this.redirigir_2_4CheckBox.setEnabled(false);
        this.jugarVariasCartasCheckbox = this.mainView.findViewById(R.id.buscarSala_checkbox_jugar_varias_cartas);this.jugarVariasCartasCheckbox.setEnabled(false);
        this.penalizacion_4_colorCheckbox = this.mainView.findViewById(R.id.buscarSala_checkbox_penalizacion_4_color);this.penalizacion_4_colorCheckbox.setEnabled(false);

        ToggleButton toggleButton = this.mainView.findViewById(R.id.filtrosAdicionalesToggleButton);
        toggleButton.setVisibility(View.GONE);

        switch(configSala.getMaxParticipantes()){
            case 2:
                radio2.setChecked(true);
                break;
            case 3:
                radio3.setChecked(true);
                break;
            case 4:
                radio4.setChecked(true);
                break;
            default:
                radioCualquiera.setChecked(true);
                break;
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity,
                R.array.modo_juego_filtro_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.modoJuegoSpinner.setAdapter(adapter);
        this.modoJuegoSpinner.setSelection(configSala.getModoJuego().ordinal());

        ReglasEspeciales reglasEspeciales = configSala.getReglas();
        this.rayosXCheckBox.setChecked(reglasEspeciales.isCartaRayosX());
        this.intercambioCheckBox.setChecked(reglasEspeciales.isCartaIntercambio());
        this.x2CheckBox.setChecked(reglasEspeciales.isCartaX2());

        this.encadenar_2_4Checkbox.setChecked(reglasEspeciales.isEncadenarRoboCartas());
        this.redirigir_2_4CheckBox.setChecked(reglasEspeciales.isRedirigirRoboCartas());
        this.jugarVariasCartasCheckbox.setChecked(reglasEspeciales.isJugarVariasCartas());
        this.penalizacion_4_colorCheckbox.setChecked(reglasEspeciales.isEvitarEspecialFinal());
    }

    public void show(){
        ViewParent parent = mainView.getParent();
        if(parent != null){
            ((ViewGroup) parent).removeView(mainView);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Reglas de la sala");
        builder.setView(this.mainView);
        builder.setPositiveButton("OK", (dialog, which) -> {});

        builder.show();
    }

}
