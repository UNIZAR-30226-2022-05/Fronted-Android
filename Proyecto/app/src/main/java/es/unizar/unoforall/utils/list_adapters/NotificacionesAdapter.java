package es.unizar.unoforall.utils.list_adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Collection;

import es.unizar.unoforall.R;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.notifications.Notificacion;

public class NotificacionesAdapter extends ArrayAdapter<Notificacion> {

    private final int resourceLayout;
    private final CustomActivity activity;

    public NotificacionesAdapter(CustomActivity activity, Collection<Notificacion> notificaciones){
        super(activity, R.layout.salas_row, new ArrayList<>(notificaciones));
        this.resourceLayout = R.layout.notification_view;
        this.activity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = convertView;

        if(view == null){
            view = LayoutInflater.from(activity).inflate(resourceLayout, null);
        }

        Notificacion notificacion = getItem(position);
        notificacion.applyToNotificacionView(view);

        return view;
    }
}
