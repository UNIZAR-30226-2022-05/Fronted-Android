package es.unizar.unoforall.utils.dialogs;

import androidx.appcompat.app.AlertDialog;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.partidas.Jugada;
import es.unizar.unoforall.model.partidas.PartidaJugada;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.CustomActivity;

public class MostrarResultadosDialogBuilder {

    private final CustomActivity activity;

    private Runnable positiveRunnable = () -> {};
    private Runnable negativeRunnable = () -> {};

    public MostrarResultadosDialogBuilder(CustomActivity activity, Sala sala){
        this.activity = activity;

        if(sala == null || sala.getUltimaPartidaJugada() == null){
            return;
        }

        PartidaJugada partidaJugada = sala.getUltimaPartidaJugada();
        // Generar view con los datos de partidaJugada
    }

    public void setPositiveButton(Runnable positiveRunnable){
        this.positiveRunnable = positiveRunnable;
    }

    public void setNegativeButton(Runnable negativeRunnable){
        this.positiveRunnable = negativeRunnable;
    }

    public void show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Resultados de la partida");
        //builder.setView(???);
        builder.setPositiveButton("Continuar", (dialog, which) -> positiveRunnable.run());
        builder.setNegativeButton("Salir", (dialog, which) -> negativeRunnable.run());
        builder.setOnCancelListener(dialog -> show());
        builder.show();
    }
}
