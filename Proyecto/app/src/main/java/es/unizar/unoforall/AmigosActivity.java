package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.utils.ActivityType;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.list_adapters.AmigosAdapter;
import es.unizar.unoforall.utils.list_adapters.PeticionesEnviadasAdapter;
import es.unizar.unoforall.utils.list_adapters.PeticionesRecibidasAdapter;
import es.unizar.unoforall.utils.tasks.Task;

public class AmigosActivity extends CustomActivity {

    private ListView amigosListView;
    private ListView peticionesRecibidasListView;
    private ListView peticionesEnviadasListView;

    private SwipeRefreshLayout pullToRefresh;

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
        peticionesEnviadasListView = findViewById(R.id.peticionesEnviadasListView);
        peticionesRecibidasListView = findViewById(R.id.peticionesRecibidasListView);

        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> refreshData());

        FloatingActionButton fab = findViewById(R.id.agregarAmigoButton);
        fab.setOnClickListener(view -> new BackendAPI(this).enviarPeticion(() -> refreshData()));

        refreshData();
    }

    private void refreshData(){
        pullToRefresh.setRefreshing(true);

        BackendAPI api = new BackendAPI(this);
        api.obtenerAmigos(listaUsuarios -> {
            AmigosAdapter adapter = new AmigosAdapter(this, listaUsuarios);
            amigosListView.setAdapter(adapter);
        });
        api.obtenerPeticionesEnviadas(listaUsuarios -> {
            PeticionesEnviadasAdapter adapter = new PeticionesEnviadasAdapter(this, listaUsuarios);
            peticionesEnviadasListView.setAdapter(adapter);
        });
        api.obtenerPeticionesRecibidas(listaUsuarios -> {
            PeticionesRecibidasAdapter adapter = new PeticionesRecibidasAdapter(this, listaUsuarios, () -> refreshData());
            peticionesRecibidasListView.setAdapter(adapter);
            pullToRefresh.setRefreshing(false);
        });
    }
}