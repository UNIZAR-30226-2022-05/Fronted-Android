package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.salas.RespuestaSalas;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.MyAdapter;

public class BuscarSalaActivity extends AppCompatActivity {

    private UUID sesionID;
    private HashMap salasIniciales;
    private RespuestaSalas lasSalas;
    private ListView informacionBusqueda;


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
        Set<UUID> claves = salasIniciales.keySet();
        ArrayList<UUID> listOfKeys = new ArrayList<UUID>(claves);
        Collection<Sala> values = salasIniciales.values();
        ArrayList<Sala> listOfValues = new ArrayList<>(values);

        informacionBusqueda = findViewById(R.id.salasEncontradas);
        MyAdapter adapter = new MyAdapter(listOfKeys, listOfValues);
        informacionBusqueda.setAdapter(adapter);

    }
}