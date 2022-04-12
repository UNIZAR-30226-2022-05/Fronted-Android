package es.unizar.unoforall.utils.list_adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import es.unizar.unoforall.R;
import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.ListaUsuarios;
import es.unizar.unoforall.model.UsuarioVO;

public class PeticionesRecibidasAdapter extends ArrayAdapter<UsuarioVO> {

    private final int resourceLayout;
    private final Activity activity;
    private final Runnable onUpdate;

    public PeticionesRecibidasAdapter(Activity activity, ListaUsuarios listaUsuarios, Runnable onUpdate){
        super(activity, R.layout.salas_row, listaUsuarios.getUsuarios());
        this.resourceLayout = R.layout.peticiones_rec_row;
        this.activity = activity;
        this.onUpdate = onUpdate;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View view = convertView;

        if(view == null){
            view = LayoutInflater.from(activity).inflate(resourceLayout, null);
        }

        UsuarioVO usuarioVO = getItem(position);

        String nombre = usuarioVO.getNombre();
        String correo = usuarioVO.getCorreo();

        TextView nombreTextView = view.findViewById(R.id.nombre);
        TextView correoTextView = view.findViewById(R.id.correo);
        ImageButton aceptarPeticionButton = view.findViewById(R.id.aceptarPeticionButton);
        ImageButton rechazarPeticionButton = view.findViewById(R.id.rechazarPeticionButton);

        nombreTextView.setText(nombre);
        correoTextView.setText(correo);

        aceptarPeticionButton.setOnClickListener(v -> {
            new BackendAPI(activity).aceptarPeticion(usuarioVO);
            onUpdate.run();
        });
        rechazarPeticionButton.setOnClickListener(v -> {
            new BackendAPI(activity).rechazarPeticion(usuarioVO);
            onUpdate.run();
        });

        return view;
    }
}
