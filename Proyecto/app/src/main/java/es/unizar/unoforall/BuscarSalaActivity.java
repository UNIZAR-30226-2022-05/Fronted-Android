package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;

import java.util.HashMap;
import java.util.UUID;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.salas.RespuestaSalas;
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

        sesionID = PrincipalActivity.getSesionID();

        BackendAPI api = new BackendAPI(this);
        api.obtenerSalasInicio(sesionID, misSalas -> {
            lasSalas = misSalas;
            salasIniciales = lasSalas.getSalas();

            informacionBusqueda = findViewById(R.id.salasEncontradas);
            MyAdapter adapter = new MyAdapter(salasIniciales);
            informacionBusqueda.setAdapter(adapter);
        });



    }
}