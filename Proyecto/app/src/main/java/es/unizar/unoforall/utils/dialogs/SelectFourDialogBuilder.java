package es.unizar.unoforall.utils.dialogs;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.Arrays;
import java.util.function.Consumer;

import es.unizar.unoforall.PartidaActivity;
import es.unizar.unoforall.R;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partidas.Carta;
import es.unizar.unoforall.model.partidas.Jugada;
import es.unizar.unoforall.model.partidas.Jugador;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.ImageManager;

public class SelectFourDialogBuilder {
    private final CustomActivity activity;
    private static final Carta.Color[] COLORES = {
            Carta.Color.rojo,
            Carta.Color.amarillo,
            Carta.Color.azul,
            Carta.Color.verde};

    private boolean isColorMenu;

    private final View mainView;
    private final LinearLayout[] layoutElementos;
    private final ImageView[] imagenElementos;
    private final TextView[] textoElementos;

    private Runnable negativeRunnable = () -> {};
    private AlertDialog dialog;

    private SelectFourDialogBuilder(CustomActivity activity){
        this.activity = activity;

        mainView = LayoutInflater.from(activity).inflate(R.layout.selector_4_opciones, null);

        layoutElementos = new LinearLayout[] {
                mainView.findViewById(R.id.layoutElemento0),
                mainView.findViewById(R.id.layoutElemento1),
                mainView.findViewById(R.id.layoutElemento2),
                mainView.findViewById(R.id.layoutElemento3)
        };

        imagenElementos = new ImageView[] {
                mainView.findViewById(R.id.imagenElemento0),
                mainView.findViewById(R.id.imagenElemento1),
                mainView.findViewById(R.id.imagenElemento2),
                mainView.findViewById(R.id.imagenElemento3)
        };

        textoElementos = new TextView[]{
                mainView.findViewById(R.id.textoElemento0),
                mainView.findViewById(R.id.textoElemento1),
                mainView.findViewById(R.id.textoElemento2),
                mainView.findViewById(R.id.textoElemento3)
        };
    }

    // Constructor para seleccionar el jugador objetivo de la carta de intercambio.
    // El tipo de la carta debe ser 'intercambio'
    public SelectFourDialogBuilder(CustomActivity activity, Carta carta, Sala sala,
                                   Consumer<Jugada> onJugadaCreated){
        this(activity);
        this.isColorMenu = false;

        if(carta.getTipo() != Carta.Tipo.intercambio){
            throw new IllegalArgumentException("El tipo de la carta debe ser intercambio");
        }

        if(sala.isNoExiste()){
            throw new IllegalArgumentException("Esa sala no es válida");
        }

        if(!sala.isEnPartida()){
            throw new IllegalArgumentException("La sala no está en partida");
        }

        // Ocultar todos los layouts
        for(LinearLayout layoutElemento : layoutElementos){
            layoutElemento.setVisibility(View.INVISIBLE);
        }

        // Mostrar las imágenes y nombres de los jugadores
        for(int i=0; i<sala.getPartida().getJugadores().size(); i++){
            layoutElementos[i].setVisibility(View.VISIBLE);
            Jugador jugador = sala.getPartida().getJugadores().get(i);
            int imageID;
            String nombre;
            if(jugador.isEsIA()){
                imageID = ImageManager.IA_IMAGE_ID;
                nombre = PartidaActivity.getIAName(i);
            }else{
                UsuarioVO usuarioVO = sala.getParticipante(jugador.getJugadorID());
                imageID = usuarioVO.getAvatar();
                nombre = PartidaActivity.acortarNombre(usuarioVO.getNombre());
            }

            textoElementos[i].setText(nombre);
            ImageManager.setImagenPerfil(imagenElementos[i], imageID);
            ImageManager.setImageViewClickable(imagenElementos[i], true);
            final int jugadorID = i;
            imagenElementos[i].setOnClickListener(view -> {
                Jugada jugada = new Jugada(Arrays.asList(carta));
                jugada.setJugadorObjetivo(jugadorID);
                dialog.dismiss();
                onJugadaCreated.accept(jugada);
            });
        }
    }

    // Constructor para seleccionar el color de la carta comodín utilizada.
    // El color de la carta debe ser 'comodín'
    public SelectFourDialogBuilder(CustomActivity activity, Carta carta, boolean defaultMode,
                                   Consumer<Jugada> onJugadaCreated){
        this(activity);
        this.isColorMenu = true;

        if(carta.getColor() != Carta.Color.comodin){
            throw new IllegalArgumentException("El color de la carta debe ser comodín");
        }

        // Mostrar las imágenes y nombres de los colores
        for(int i=0; i<COLORES.length; i++){
            ImageManager.setImagenColor(imagenElementos[i], textoElementos[i], COLORES[i], defaultMode);
            final Carta.Color colorCarta = COLORES[i];
            imagenElementos[i].setOnClickListener(view -> {
                carta.setColor(colorCarta);
                Jugada jugada = new Jugada(Arrays.asList(carta));
                dialog.dismiss();
                onJugadaCreated.accept(jugada);
            });
        }
    }

    public void setNegativeButton(Runnable runnable){
        this.negativeRunnable = runnable;
    }

    public void show(){
        ViewParent parent = mainView.getParent();
        if(parent != null){
            ((ViewGroup) parent).removeView(mainView);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if(this.isColorMenu){
            builder.setTitle("Seleccionar color");
            builder.setMessage("Selecciona el color al que cambiar");
        }else{
            builder.setTitle("Seleccionar rival");
            builder.setMessage("Selecciona el jugador objetivo de la carta de intercambio");
        }

        builder.setView(mainView);
        builder.setNegativeButton("Cancelar", (dialog, which) -> negativeRunnable.run());
        builder.setOnCancelListener(dialog -> show());

        dialog = builder.create();
        PartidaDialogManager.setCurrentDialog(dialog);
        dialog.show();
    }
}
