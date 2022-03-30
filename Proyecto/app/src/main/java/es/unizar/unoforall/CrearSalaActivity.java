package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
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

        configSala = new ConfigSala();
        reglasEspeciales = new ReglasEspeciales();
        configSala.setReglas(reglasEspeciales);

        Spinner spinner = findViewById(R.id.modo_juego_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.modo_juego_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                configSala.setModoJuego(ConfigSala.ModoJuego.values()[i]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        CheckBox checkBox = findViewById(R.id.checkbox_publica);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                configSala.setEsPublica(isChecked));


        checkBox = findViewById(R.id.checkbox_rayosX);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setCartaRayosX(isChecked));

        checkBox = findViewById(R.id.checkbox_intercambio);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setCartaIntercambio(isChecked));

        checkBox = findViewById(R.id.checkbox_x2);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setCartaX2(isChecked));


        checkBox = findViewById(R.id.checkbox_encadenar_2_4);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setEncadenarRoboCartas(isChecked));

        checkBox = findViewById(R.id.checkbox_redirigir_2_4);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setRedirigirRoboCartas(isChecked));

        checkBox = findViewById(R.id.checkbox_jugar_varias_cartas);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setJugarVariasCartas(isChecked));

        checkBox = findViewById(R.id.checkbox_penalizacion_4_color);
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                reglasEspeciales.setEvitarEspecialFinal(isChecked));

        Button confirmarSalaButton = findViewById(R.id.confirmarSalaButton);
        confirmarSalaButton.setOnClickListener(view -> {
            UUID miSesionID = null;//PantallaPrincipalActivity.getClaveIncio();

            RestAPI api = new RestAPI(this,"/api/crearSala");
            api.addParameter("sessionID", miSesionID);
            api.addParameter("configuracion", configSala);
            api.openConnection();

            api.setOnObjectReceived(UUID.class, idSala ->
                Toast.makeText(this, "idSala: " + idSala, Toast.LENGTH_SHORT).show());
            //Ir a la pantalla de vista de sala
        });

    }

    @SuppressLint("NonConstantResourceId")
    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
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
}