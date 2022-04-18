package es.unizar.unoforall;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.utils.ActivityType;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.dialogs.ReglasFilterDialogBuilder;
import es.unizar.unoforall.utils.list_adapters.SalaListAdapter;

public class BuscarSalaActivity extends CustomActivity {

    private ListView listViewSalas;
    private SwipeRefreshLayout pullToRefresh;

    private BackendAPI api;
    private ConfigSala configSala;

    @Override
    public ActivityType getType(){
        return ActivityType.BUSCAR_SALA;
    }

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
            ReglasFilterDialogBuilder builder = new ReglasFilterDialogBuilder(this, this.configSala);
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
}