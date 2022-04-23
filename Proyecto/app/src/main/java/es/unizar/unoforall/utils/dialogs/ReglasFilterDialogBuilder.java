package es.unizar.unoforall.utils.dialogs;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;

import java.util.function.Consumer;

import es.unizar.unoforall.R;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.ReglasEspeciales;

public class ReglasFilterDialogBuilder {
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

    private Runnable positiveRunnable;
    private Runnable negativeRunnable;

    public ReglasFilterDialogBuilder(Activity activity, ConfigSala configSala){
        this.activity = activity;

        this.mainView = LayoutInflater.from(activity).inflate(R.layout.dialog_busqueda_salas, null);
        this.filtrosAdicionalesLinearLayout = this.mainView.findViewById(R.id.filtrosAdicionalesLinearLayout);

        this.radio2 = this.mainView.findViewById(R.id.buscarSala_radio_dos);
        this.radio3 = this.mainView.findViewById(R.id.buscarSala_radio_tres);
        this.radio4 = this.mainView.findViewById(R.id.buscarSala_radio_cuatro);
        this.radioCualquiera = this.mainView.findViewById(R.id.buscarSala_radio_cualquiera);

        this.modoJuegoSpinner = this.mainView.findViewById(R.id.modo_juego_spinner);

        this.rayosXCheckBox = this.mainView.findViewById(R.id.buscarSala_checkbox_rayosX);
        this.intercambioCheckBox = this.mainView.findViewById(R.id.buscarSala_checkbox_intercambio);
        this.x2CheckBox = this.mainView.findViewById(R.id.buscarSala_checkbox_x2);

        this.encadenar_2_4Checkbox = this.mainView.findViewById(R.id.buscarSala_checkbox_encadenar_2_4);
        this.redirigir_2_4CheckBox = this.mainView.findViewById(R.id.buscarSala_checkbox_redirigir_2_4);
        this.jugarVariasCartasCheckbox = this.mainView.findViewById(R.id.buscarSala_checkbox_jugar_varias_cartas);
        this.penalizacion_4_colorCheckbox = this.mainView.findViewById(R.id.buscarSala_checkbox_penalizacion_4_color);

        ToggleButton toggleButton = this.mainView.findViewById(R.id.filtrosAdicionalesToggleButton);
        toggleButton.setOnCheckedChangeListener((compoundButton, checked) -> {
            if(checked){
                this.filtrosAdicionalesLinearLayout.setVisibility(View.VISIBLE);
            }else{
                this.filtrosAdicionalesLinearLayout.setVisibility(View.GONE);
            }
        });

        switch(configSala.getMaxParticipantes()){
            case 2: radio2.setChecked(true); break;
            case 3: radio3.setChecked(true); break;
            case 4: radio4.setChecked(true); break;
            default: radioCualquiera.setChecked(true); break;
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity,
                R.array.modo_juego_filtro_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.modoJuegoSpinner.setAdapter(adapter);
        this.modoJuegoSpinner.setSelection(configSala.getModoJuego().ordinal());
        this.modoJuegoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(ConfigSala.ModoJuego.values()[i] == ConfigSala.ModoJuego.Parejas){
                    radio2.setEnabled(false);
                    radio3.setEnabled(false);
                    radio4.setEnabled(false);
                    radioCualquiera.setEnabled(false);
                }else{
                    radio2.setEnabled(true);
                    radio3.setEnabled(true);
                    radio4.setEnabled(true);
                    radioCualquiera.setEnabled(true);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        ReglasEspeciales reglasEspeciales = configSala.getReglas();
        this.rayosXCheckBox.setChecked(reglasEspeciales.isCartaRayosX());
        this.intercambioCheckBox.setChecked(reglasEspeciales.isCartaIntercambio());
        this.x2CheckBox.setChecked(reglasEspeciales.isCartaX2());

        this.encadenar_2_4Checkbox.setChecked(reglasEspeciales.isEncadenarRoboCartas());
        this.redirigir_2_4CheckBox.setChecked(reglasEspeciales.isRedirigirRoboCartas());
        this.jugarVariasCartasCheckbox.setChecked(reglasEspeciales.isJugarVariasCartas());
        this.penalizacion_4_colorCheckbox.setChecked(reglasEspeciales.isEvitarEspecialFinal());

        if(reglasEspeciales.isReglasValidas()){
            toggleButton.performClick();
        }

        this.positiveRunnable = () -> {};
        this.negativeRunnable = () -> {};
    }

    public void setPositiveButton(Consumer<ConfigSala> consumer){
        this.positiveRunnable = () -> {
            ConfigSala configSala = new ConfigSala();

            // Establecer el modo de juego
            int idModoJuego = this.modoJuegoSpinner.getSelectedItemPosition();
            configSala.setModoJuego(ConfigSala.ModoJuego.values()[idModoJuego]);

            // Establecer el número máximo de participantes
            if(configSala.getModoJuego() == ConfigSala.ModoJuego.Parejas){
                configSala.setMaxParticipantes(4);
            }else{
                if(this.radio2.isChecked()){
                    configSala.setMaxParticipantes(2);
                }else if(this.radio3.isChecked()){
                    configSala.setMaxParticipantes(3);
                }else if(this.radio4.isChecked()){
                    configSala.setMaxParticipantes(4);
                }else{
                    configSala.setMaxParticipantes(-1);
                }
            }

            // Establecer los filtros adicionales
            ReglasEspeciales reglasEspeciales = new ReglasEspeciales();

            reglasEspeciales.setCartaRayosX(this.rayosXCheckBox.isChecked());
            reglasEspeciales.setCartaIntercambio(this.intercambioCheckBox.isChecked());
            reglasEspeciales.setCartaX2(this.x2CheckBox.isChecked());

            reglasEspeciales.setEncadenarRoboCartas(this.encadenar_2_4Checkbox.isChecked());
            reglasEspeciales.setRedirigirRoboCartas(this.redirigir_2_4CheckBox.isChecked());
            reglasEspeciales.setJugarVariasCartas(this.jugarVariasCartasCheckbox.isChecked());
            reglasEspeciales.setEvitarEspecialFinal(this.penalizacion_4_colorCheckbox.isChecked());

            reglasEspeciales.setReglasValidas(this.filtrosAdicionalesLinearLayout.getVisibility() == View.VISIBLE);

            configSala.setReglas(reglasEspeciales);

            consumer.accept(configSala);
        };
    }

    public void setNegativeButton(Runnable runnable){
        this.negativeRunnable = runnable;
    }

    public void show(){
        ViewParent parent = mainView.getParent();
        if(parent != null){
            ((ViewGroup) parent).removeView(mainView);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Búsqueda Avanzada");
        builder.setMessage("Seleccione los filtros de búsqueda");
        builder.setView(mainView);
        builder.setPositiveButton("Confirmar", (dialog, which) -> positiveRunnable.run());
        builder.setNegativeButton("Cancelar", (dialog, which) -> negativeRunnable.run());
        builder.setOnCancelListener(dialog -> negativeRunnable.run());

        builder.show();
    }

}
