package es.unizar.unoforall;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.UUID;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.api.RestAPI;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.ReglasEspeciales;

public class CrearSalaActivity extends AppCompatActivity {

    private ConfigSala configSala;
    private ReglasEspeciales reglasEspeciales;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_sala);
        setTitle(R.string.crearSala);

        configSala = new ConfigSala();
        reglasEspeciales = configSala.getReglas();

        Spinner spinner = findViewById(R.id.modo_juego_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.modo_juego_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                configSala.setModoJuego(ConfigSala.ModoJuego.values()[i]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        RadioButton button;
        if(configSala.esPublica()){
            button = findViewById(R.id.radio_publica);
        }else{
            button = findViewById(R.id.radio_privada);
        }
        button.setChecked(true);

        switch(configSala.getMaxParticipantes()){
            case 2:
                button = findViewById(R.id.radio_dos);
                break;
            case 3:
                button = findViewById(R.id.radio_tres);
                break;
            default:
                button = findViewById(R.id.radio_cuatro);
                break;
        }
        button.setChecked(true);

        CheckBox checkBox = findViewById(R.id.checkbox_rayosX);
        checkBox.setChecked(reglasEspeciales.isCartaRayosX());
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setCartaRayosX(isChecked));

        checkBox = findViewById(R.id.checkbox_intercambio);
        checkBox.setChecked(reglasEspeciales.isCartaIntercambio());
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setCartaIntercambio(isChecked));

        checkBox = findViewById(R.id.checkbox_x2);
        checkBox.setChecked(reglasEspeciales.isCartaX2());
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setCartaX2(isChecked));


        checkBox = findViewById(R.id.checkbox_encadenar_2_4);
        checkBox.setChecked(reglasEspeciales.isEncadenarRoboCartas());
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setEncadenarRoboCartas(isChecked));

        checkBox = findViewById(R.id.checkbox_redirigir_2_4);
        checkBox.setChecked(reglasEspeciales.isRedirigirRoboCartas());
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setRedirigirRoboCartas(isChecked));

        checkBox = findViewById(R.id.checkbox_jugar_varias_cartas);
        checkBox.setChecked(reglasEspeciales.isJugarVariasCartas());
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setJugarVariasCartas(isChecked));

        checkBox = findViewById(R.id.checkbox_penalizacion_4_color);
        checkBox.setChecked(reglasEspeciales.isEvitarEspecialFinal());
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setEvitarEspecialFinal(isChecked));

        Button confirmarSalaButton = findViewById(R.id.confirmarSalaButton);
        confirmarSalaButton.setOnClickListener(view -> {
            UUID sesionID = PantallaPrincipalActivity.getSesionID();
            BackendAPI api = new BackendAPI(this);
            api.crearSala(sesionID, configSala);
        });

    }

    @SuppressLint("NonConstantResourceId")
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.radio_publica:
                if (checked)
                    configSala.setEsPublica(true);
                    break;
            case R.id.radio_privada:
                if (checked)
                    configSala.setEsPublica(false);
                    break;
            case R.id.radio_dos:
                if (checked)
                    configSala.setMaxParticipantes(2);
                    break;
            case R.id.radio_tres:
                if (checked)
                    configSala.setMaxParticipantes(3);
                    break;
            case R.id.radio_cuatro:
                if (checked)
                    configSala.setMaxParticipantes(4);
                    break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Volver a la pantalla principal cuando se salga de la sala creada
        finish();
    }
}