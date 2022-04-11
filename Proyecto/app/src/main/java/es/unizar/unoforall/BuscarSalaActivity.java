package es.unizar.unoforall;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.UUID;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.dialogs.FilterSearchDialogBuilder;
import es.unizar.unoforall.utils.SalaListAdapter;

public class BuscarSalaActivity extends AppCompatActivity {

    private ListView listViewSalas;
    private SwipeRefreshLayout pullToRefresh;

    private BackendAPI api;
    private ConfigSala configSala;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buscar_sala);
        setTitle(R.string.busquedaDeSalas);

        listViewSalas = findViewById(R.id.listViewSalas);

        api = new BackendAPI(this);
        configSala = new ConfigSala();
        configSala.setModoJuego(ConfigSala.ModoJuego.Undefined);
        configSala.setMaxParticipantes(-1);
        BackendAPI api = new BackendAPI(this);

        Button buscarButton = findViewById(R.id.buscarPorIdButton);
        buscarButton.setOnClickListener(view -> api.unirseSalaPorID());

        Button busquedaAvanzadaButton = findViewById(R.id.busquedaConFiltros);
        busquedaAvanzadaButton.setOnClickListener(view -> {
            FilterSearchDialogBuilder builder = new FilterSearchDialogBuilder(this, this.configSala);
            builder.setPositiveButton(configSala -> {
                this.configSala = configSala;
                actualizarSalas();
            });
            builder.show();
        });

        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> actualizarSalas());

        actualizarSalas();
    }

    private void actualizarSalas(){
        pullToRefresh.setRefreshing(true);
        api.obtenerSalasFiltro(configSala, respuestaSalas -> {
            if(!respuestaSalas.isExito()){
                Toast.makeText(this, "Se ha producido un error al obtener las salas", Toast.LENGTH_SHORT).show();
            }else{
                SalaListAdapter adapter = new SalaListAdapter(this, respuestaSalas.getSalas());
                listViewSalas.setAdapter(adapter);
            }
            pullToRefresh.setRefreshing(false);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        actualizarSalas();
    }
}