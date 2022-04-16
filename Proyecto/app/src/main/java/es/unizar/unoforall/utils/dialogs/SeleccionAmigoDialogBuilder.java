package es.unizar.unoforall.utils.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.List;
import java.util.function.Consumer;

import es.unizar.unoforall.AmigosActivity;
import es.unizar.unoforall.R;
import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.api.WebSocketAPI;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.list_adapters.AmigosAdapter;

public class SeleccionAmigoDialogBuilder {
    private static final int PADDING = 25;

    private final CustomActivity activity;

    private final LinearLayout linearLayout;

    private Consumer<String> positiveRunnable;
    private Runnable negativeRunnable;

    private Dialog dialog;

    public SeleccionAmigoDialogBuilder(CustomActivity activity){
        this.activity = activity;

        this.linearLayout = new LinearLayout(activity);
        this.linearLayout.setPadding(PADDING, PADDING, PADDING, PADDING);

        ListView listaAmigos = new ListView(activity);
        new BackendAPI(activity).obtenerAmigos(listaUsuarios -> {
            AmigosAdapter adapter = new AmigosAdapter(activity, listaUsuarios);
            if(listaUsuarios.getUsuarios().isEmpty()){
                TextView textView = new TextView(activity);
                textView.setText("No tienes amigos :(");
                this.linearLayout.addView(textView);
            }else{
                listaAmigos.setAdapter(adapter);
                this.linearLayout.addView(listaAmigos);
            }
        });
        listaAmigos.setOnItemClickListener((parent, view, position, id) -> {
            TextView correoTextView = view.findViewById(R.id.correo);
            String correo = correoTextView.getText().toString();
            this.positiveRunnable.accept(correo);

            dialog.dismiss();
        });

        this.positiveRunnable = correo -> {};
        this.negativeRunnable = () -> {};
    }

    public void setPositiveButton(Consumer<String> consumer){
        this.positiveRunnable = consumer;
    }

    public void setNegativeButton(Runnable runnable){
        this.negativeRunnable = runnable;
    }

    public void show(){
        ViewParent parent = linearLayout.getParent();
        if(parent != null){
            ((ViewGroup) parent).removeView(linearLayout);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Invitar a amigos a la sala");
        builder.setMessage("Selecciona el usuario al que deseas invitar");
        builder.setView(linearLayout);
        builder.setOnCancelListener(dialog -> negativeRunnable.run());

        dialog = builder.create();
        dialog.show();
    }
}
