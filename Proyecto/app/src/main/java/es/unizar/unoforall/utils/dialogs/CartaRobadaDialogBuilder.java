package es.unizar.unoforall.utils.dialogs;

import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import java.util.Arrays;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.partidas.Carta;
import es.unizar.unoforall.model.partidas.Jugada;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.ImageManager;

public class CartaRobadaDialogBuilder {
    private CustomActivity activity;
    private final ImageView imageView;
    private Carta carta;
    private boolean defaultMode;
    private Sala sala;

    public CartaRobadaDialogBuilder(CustomActivity activity, Carta carta, boolean defaultMode, Sala sala){
        this.activity = activity;
        this.carta = carta;
        this.defaultMode = defaultMode;
        this.sala = sala;

        imageView = new ImageView(activity);
        ImageManager.setImagenCarta(imageView, carta, defaultMode, true, true, false);
    }

    private final Runnable positiveRunnable = () -> {
        if(carta.getColor() == Carta.Color.comodin){
            SelectFourDialogBuilder builder2 = new SelectFourDialogBuilder(
                    activity, carta, defaultMode,
                    jugada -> {
                        new BackendAPI(activity).enviarJugada(jugada);
                        activity.mostrarMensaje("Has jugado una carta comodín");
                    });
            builder2.setNegativeButton(() -> show());
            builder2.show();
        }else if(carta.getTipo() == Carta.Tipo.intercambio){
            SelectFourDialogBuilder builder2 = new SelectFourDialogBuilder(
                    activity, carta, sala,
                    jugada -> {
                        new BackendAPI(activity).enviarJugada(jugada);
                        activity.mostrarMensaje("Has jugado una carta de intercambio");
                    });
            builder2.setNegativeButton(() -> show());
            builder2.show();
        }else{
            Jugada jugada = new Jugada(Arrays.asList(carta));
            new BackendAPI(activity).enviarJugada(jugada);
            activity.mostrarMensaje("Has jugado una carta");
        }
    };

    public void show(){
        ViewParent parent = imageView.getParent();
        if(parent != null){
            ((ViewGroup) parent).removeView(imageView);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Has robado una carta que puedes utilizar");
        builder.setMessage("¿Qué quieres hacer?");
        builder.setView(imageView);
        builder.setPositiveButton("Utilizarla", (dialog, which) -> positiveRunnable.run());
        builder.setNegativeButton("Guardarla", (dialog, which) ->
                    new BackendAPI(activity).enviarJugada(new Jugada()));
        builder.setOnCancelListener(dialog -> show());

        PartidaDialogManager.setCurrentDialog(builder.show());
    }
}
