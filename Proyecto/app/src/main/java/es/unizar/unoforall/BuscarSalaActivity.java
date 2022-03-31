package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

<<<<<<< HEAD
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
=======
>>>>>>> Desarrollo
import java.util.HashMap;
import java.util.UUID;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.api.RestAPI;
import es.unizar.unoforall.model.salas.RespuestaSalas;
<<<<<<< HEAD
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.FilterSearchDialogBuilder;
=======
>>>>>>> Desarrollo
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

        sesionID = PrincipalActivity.getSesionID();

        BackendAPI api = new BackendAPI(this);
        api.obtenerSalasInicio(sesionID, misSalas -> {
            lasSalas = misSalas;
            salasIniciales = lasSalas.getSalas();

            informacionBusqueda = findViewById(R.id.salasEncontradas);
            MyAdapter adapter = new MyAdapter(salasIniciales, this);
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
                    MyAdapter adapter = new MyAdapter(salasIniciales, this);
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
                MyAdapter adapter = new MyAdapter(salasIniciales, this);
                informacionBusqueda.setAdapter(adapter);
            }));
            filtrado.setNegativeButton(() -> {});
            filtrado.show();
        });
    }
}