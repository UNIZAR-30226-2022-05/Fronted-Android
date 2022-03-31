package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.salas.RespuestaSalas;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.MyAdapter;

public class BuscarSalaActivity extends AppCompatActivity {

    private UUID sesionID;
    private HashMap<UUID, Sala> salasIniciales;
    private RespuestaSalas lasSalas;
    private ListView informacionBusqueda;
    private EditText idSala;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_sala);
        setTitle(R.string.busquedaDeSalas);

        sesionID = PantallaPrincipalActivity.getSesionID();

        BackendAPI api = new BackendAPI(this);
        api.obtenerSalasInicio(sesionID, misSalas -> {
            lasSalas = misSalas;
        });

        //https://www.geeksforgeeks.org/how-to-convert-hashmap-to-arraylist-in-java/
        salasIniciales = lasSalas.getSalas();
        informacionBusqueda = findViewById(R.id.salasEncontradas);
        fillData();

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
                    fillData();
                }
            });
        });

        Button busquedaAvanzadaButton = findViewById(R.id.busquedaConFiltros);
        busquedaAvanzadaButton.setOnClickListener(view -> {

        });


    }

    private void fillData(){
        Set<UUID> claves = salasIniciales.keySet();
        ArrayList<UUID> listOfKeys = new ArrayList<UUID>(claves);
        Collection<Sala> values = salasIniciales.values();
        ArrayList<Sala> listOfValues = new ArrayList<>(values);

        MyAdapter adapter = new MyAdapter(listOfKeys, listOfValues, this);
        informacionBusqueda.setAdapter(adapter);
    }
}