package es.unizar.unoforall;

import android.os.Bundle;
import android.widget.ListView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.utils.ActivityType;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.list_adapters.AmigosAdapter;

public class AmigosActivity extends CustomActivity {

    private ListView amigosListView;

    private SwipeRefreshLayout pullToRefresh;

    private BackendAPI api;

    @Override
    public ActivityType getType(){
        return ActivityType.AMIGOS;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amigos);
        setTitle(getString(R.string.gestionarAmigos));

        amigosListView = findViewById(R.id.amigosListView);

        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> refreshData());

        api = new BackendAPI(this);
        FloatingActionButton fab = findViewById(R.id.agregarAmigoButton);
        fab.setOnClickListener(view -> api.enviarPeticion(() -> refreshData()));

        refreshData();
    }

    private void refreshData(){
        pullToRefresh.setRefreshing(true);

        api.obtenerAmigos(listaAmigos -> {
            api.obtenerPeticionesEnviadas(listaAmigosPendientes -> {
                listaAmigosPendientes.getUsuarios().forEach(usuarioVO -> {
                    // Indicar que la solicitud está pendiente
                    usuarioVO.setAspectoCartas(-1);
                    listaAmigos.getUsuarios().add(usuarioVO);
                });

                AmigosAdapter adapter = new AmigosAdapter(this, listaAmigos);
                amigosListView.setAdapter(adapter);
                pullToRefresh.setRefreshing(false);
            });
        });
    }
}