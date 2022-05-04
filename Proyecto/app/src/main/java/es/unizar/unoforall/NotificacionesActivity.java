package es.unizar.unoforall;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.utils.ActivityType;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.list_adapters.NotificacionesAdapter;
import es.unizar.unoforall.utils.notifications.Notificacion;
import es.unizar.unoforall.utils.notifications.Notificaciones;

public class NotificacionesActivity extends CustomActivity {

    @Override
    public ActivityType getType(){
        return ActivityType.NOTIFICACIONES;
    }

    private BackendAPI api;

    private ListView notificacionesAmigosListView;
    private ListView notificacionesSalaListView;
    private TextView notificacionesAmigosTextView;
    private TextView notificacionesSalaTextView;
    private TextView noHayNotificacionesPendientesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notificaciones);
        setTitle(R.string.notificaciones);

        this.api = new BackendAPI(this);

        this.notificacionesAmigosListView = findViewById(R.id.notificacionesAmigosListView);
        this.notificacionesSalaListView = findViewById(R.id.notificacionesSalaListView);
        this.noHayNotificacionesPendientesTextView = findViewById(R.id.noHayNotificacionesPendientesTextView);
        this.notificacionesAmigosTextView = findViewById(R.id.notificacionesAmigosTextView);
        this.notificacionesSalaTextView = findViewById(R.id.notificacionesSalaTextView);

        notificacionesAmigosListView.setVisibility(View.GONE);
        notificacionesSalaListView.setVisibility(View.GONE);
        notificacionesAmigosTextView.setVisibility(View.GONE);
        notificacionesSalaTextView.setVisibility(View.GONE);
        noHayNotificacionesPendientesTextView.setVisibility(View.VISIBLE);

        refreshData();
    }

    public void refreshData(){
        api.obtenerPeticionesRecibidas(listaUsuarios -> {
            Set<Notificacion> notificacionesSala = BackendAPI.getNotificacionesSala();

            if(listaUsuarios.getUsuarios().isEmpty() && notificacionesSala.isEmpty()){
                notificacionesAmigosListView.setVisibility(View.GONE);
                notificacionesSalaListView.setVisibility(View.GONE);
                notificacionesAmigosTextView.setVisibility(View.GONE);
                notificacionesSalaTextView.setVisibility(View.GONE);
                noHayNotificacionesPendientesTextView.setVisibility(View.VISIBLE);
            }else{
                noHayNotificacionesPendientesTextView.setVisibility(View.GONE);
                if(listaUsuarios.getUsuarios().isEmpty()){
                    notificacionesAmigosListView.setVisibility(View.GONE);
                    notificacionesAmigosTextView.setVisibility(View.GONE);
                }else{
                    notificacionesAmigosListView.setVisibility(View.VISIBLE);
                    notificacionesAmigosTextView.setVisibility(View.VISIBLE);
                    List<Notificacion> notificaciones = listaUsuarios.getUsuarios().stream()
                            .map(Notificaciones::createNotificacionAmigo)
                            .collect(Collectors.toList());
                    notificacionesAmigosListView.setAdapter(new NotificacionesAdapter(this, notificaciones));
                }
                if(notificacionesSala.isEmpty()){
                    notificacionesSalaListView.setVisibility(View.GONE);
                    notificacionesSalaTextView.setVisibility(View.GONE);
                }else{
                    notificacionesSalaListView.setVisibility(View.VISIBLE);
                    notificacionesSalaTextView.setVisibility(View.VISIBLE);
                    notificacionesSalaListView.setAdapter(new NotificacionesAdapter(this, notificacionesSala));
                }
            }
        });
    }
}