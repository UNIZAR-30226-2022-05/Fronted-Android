package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.UUID;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.FilterSearchDialogBuilder;
import es.unizar.unoforall.utils.SalaListAdapter;

public class BuscarSalaActivity extends AppCompatActivity {

    private UUID sesionID;
    private HashMap<UUID, Sala> salasIniciales;
    private ListView informacionBusqueda;
    private EditText idSala;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_sala);
        setTitle(R.string.busquedaDeSalas);

        sesionID = PrincipalActivity.getSesionID();

        BackendAPI api = new BackendAPI(this);
        api.obtenerSalasInicio(sesionID, respuestaSalas -> {
            salasIniciales = respuestaSalas.getSalas();

            informacionBusqueda = findViewById(R.id.salasEncontradas);
            SalaListAdapter adapter = new SalaListAdapter(this, salasIniciales);
            informacionBusqueda.setAdapter(adapter);
        });

        idSala = findViewById(R.id.busquedaIdSala);

        Button buscarButton = findViewById(R.id.buscarPorIdButton);
        buscarButton.setOnClickListener(view -> {
            String salaIdText = idSala.getText().toString();
            String sesionIdString = sesionID.toString();

            BackendAPI api1 = new BackendAPI(this);
            api1.buscarSala(sesionIdString, salaIdText, laSala -> {
                if(laSala.isNoExiste()){
                    Toast.makeText(this, "La sala no existe", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    UUID clave = UUID.fromString(salaIdText);
                    salasIniciales = new HashMap<UUID, Sala>();
                    salasIniciales.put(clave, laSala);
                    SalaListAdapter adapter = new SalaListAdapter(this, salasIniciales);
                    informacionBusqueda.setAdapter(adapter);
                }
            });
        });

        Button busquedaAvanzadaButton = findViewById(R.id.busquedaConFiltros);
        busquedaAvanzadaButton.setOnClickListener(view -> {
            BackendAPI api2 = new BackendAPI(this);
            FilterSearchDialogBuilder filtrado = new FilterSearchDialogBuilder(this);
            filtrado.setPositiveButton(configSala -> api2.obtenerSalasFiltro(sesionID, configSala, respuestaSalas -> {
                salasIniciales = respuestaSalas.getSalas();
                SalaListAdapter adapter = new SalaListAdapter(this, salasIniciales);
                informacionBusqueda.setAdapter(adapter);
            }));
            filtrado.setNegativeButton(() -> {});
            filtrado.show();
        });
    }
}