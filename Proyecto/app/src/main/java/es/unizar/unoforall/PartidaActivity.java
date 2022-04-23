package es.unizar.unoforall;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partidas.Carta;
import es.unizar.unoforall.model.partidas.Jugada;
import es.unizar.unoforall.model.partidas.Jugador;
import es.unizar.unoforall.model.partidas.Partida;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.ActivityType;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.ImageManager;
import es.unizar.unoforall.utils.SalaReceiver;
import es.unizar.unoforall.utils.dialogs.CartaRobadaDialogBuilder;
import es.unizar.unoforall.utils.dialogs.ReglasViewDialogBuilder;
import es.unizar.unoforall.utils.dialogs.SelectFourDialogBuilder;
import es.unizar.unoforall.utils.tasks.CancellableRunnable;
import es.unizar.unoforall.utils.tasks.Task;

public class PartidaActivity extends CustomActivity implements SalaReceiver {

    private static final int PAUSAR_ID = 0;
    private static final int ABANDONAR_ID = 1;
    private static final int VER_REGLAS_ID = 2;

    private static final int JUGADOR_ABAJO = 0;
    private static final int JUGADOR_IZQUIERDA = 1;
    private static final int JUGADOR_ARRIBA = 2;
    private static final int JUGADOR_DERECHA = 3;

    private static final int TURNO_ACTIVO_COLOR = Color.GREEN;
    private static final int TURNO_INACTIVO_COLOR = Color.WHITE;

    private static final int MAX_LONG_NOMBRE = 16;

    private LinearLayout[] layoutBarajasJugadores;
    private LinearLayout[] layoutJugadores;
    private ImageView sentido;
    private CircularProgressIndicator[] porcentajeJugadores;
    private ImageView[] imagenesJugadores;
    private TextView[] nombresJugadores;
    private TextView[] contadoresCartasJugadores;
    private ImageView cartaDelMedio;
    private ImageView mazoRobar;
    private ImageView botonUNO;

    private int jugadorActualID = -1;

    private boolean defaultMode;

    public static String getIAName(int jugadorID){
        return "IA_" + jugadorID;
    }

    public static String acortarNombre(String nombre){
        if(nombre.length() > MAX_LONG_NOMBRE){
            nombre = nombre.substring(0, MAX_LONG_NOMBRE-3) + "...";
        }
        return nombre;
    }

    @Override
    public ActivityType getType() {
        return ActivityType.PARTIDA;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partida);

        // Ocultar action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        layoutJugadores = new LinearLayout[] {
                findViewById(R.id.layoutJugadorAbajo),
                findViewById(R.id.layoutJugadorIzquierda),
                findViewById(R.id.layoutJugadorArriba),
                findViewById(R.id.layoutJugadorDerecha)};

        layoutBarajasJugadores = new LinearLayout[] {
                findViewById(R.id.barajaJugadorAbajo),
                findViewById(R.id.barajaJugadorIzquierda),
                findViewById(R.id.barajaJugadorArriba),
                findViewById(R.id.barajaJugadorDerecha)};

        sentido = findViewById(R.id.sentido);

        ImageView botonMenu = findViewById(R.id.botonMenu);
        registerForContextMenu(botonMenu);
        botonMenu.setOnClickListener(view -> view.showContextMenu(view.getX(), view.getY()));

        botonUNO = findViewById(R.id.botonUNO);

        porcentajeJugadores = new CircularProgressIndicator[] {
                findViewById(R.id.porcentajeJugadorAbajo),
                findViewById(R.id.porcentajeJugadorIzquierda),
                findViewById(R.id.porcentajeJugadorArriba),
                findViewById(R.id.porcentajeJugadorDerecha)
        };

        imagenesJugadores = new ImageView[] {
                findViewById(R.id.imagenJugadorAbajo),
                findViewById(R.id.imagenJugadorIzquierda),
                findViewById(R.id.imagenJugadorArriba),
                findViewById(R.id.imagenJugadorDerecha)
        };

        nombresJugadores = new TextView[] {
                findViewById(R.id.nombreJugadorAbajo),
                findViewById(R.id.nombreJugadorIzquierda),
                findViewById(R.id.nombreJugadorArriba),
                findViewById(R.id.nombreJugadorDerecha)
        };

        contadoresCartasJugadores = new TextView[] {
                findViewById(R.id.contadorCartasJugadorAbajo),
                findViewById(R.id.contadorCartasJugadorIzquierda),
                findViewById(R.id.contadorCartasJugadorArriba),
                findViewById(R.id.contadorCartasJugadorDerecha)
        };

        cartaDelMedio = findViewById(R.id.cartaDelMedio);
        mazoRobar = findViewById(R.id.mazoRobar);

        mazoRobar.setOnClickListener(view -> {
            if(esTurnoDelJugadorActual()){
                new BackendAPI(this).enviarJugada(new Jugada());
                mostrarMensaje("Has robado una carta");
            }else{
                mostrarMensaje("Espera tu turno");
            }
        });

        // Borrar las cartas que están por defecto
        resetCartas();
        manageSala(BackendAPI.getSalaActual());
    }

    @Override
    public void manageSala(Sala sala){
        actualizarPantallaPartida(sala);
    }

    private void actualizarPantallaPartida(Sala sala){
        Partida partida = sala.getPartida();
        if(jugadorActualID == -1){
            jugadorActualID = partida.getIndiceJugador(BackendAPI.getUsuarioID());
            UsuarioVO usuarioActual = sala.getParticipante(BackendAPI.getUsuarioID());

            // Si defaultMode = true, se mostrará el aspecto por defecto de las cartas.
            //     Si no, se mostrará el aspecto alternativo.
            defaultMode = usuarioActual.getAspectoCartas() == 0;
            View mainView = findViewById(R.id.layoutPantallaPartida);
            ImageManager.setImagenFondo(mainView, usuarioActual.getAspectoTablero());
        }

        botonUNO.setOnClickListener(view -> {
            boolean sePuedePulsar = false;
            for(Jugador jugador : partida.getJugadores()){
                if(jugador.getMano().size() <= 2 && !jugador.isProtegido_UNO()){
                    sePuedePulsar = true;
                    break;
                }
            }
            if(sePuedePulsar){
                new BackendAPI(this).pulsarBotonUNO();
                mostrarMensaje("Has pulsado el botón UNO");
            }else {
                mostrarMensaje("El botón UNO no tuvo efecto alguno");
            }
        });

        int numJugadores = partida.getJugadores().size();
        
        // posicionesJugadores[i] es el ID del jugador al que le corresponde el hueco i
        //   en los layouts. Será -1 si ese hueco no debe ser rellenado
        int[] posicionesJugadores;
        if(numJugadores == 2){
            posicionesJugadores = new int[] {jugadorActualID, -1, 1-jugadorActualID, -1};
        }else{
            posicionesJugadores = new int[] {-1, -1, -1, -1};
            for(int i=0; i<numJugadores; i++){
                posicionesJugadores[i] = (jugadorActualID + i) % numJugadores;
            }
        }

        resetCartas();
        for(int i=0; i<posicionesJugadores.length; i++){
            int jugadorID = posicionesJugadores[i];
            if(jugadorID == -1){
                // Ocultar los layouts del jugador i
                mostrarLayoutJugador(i, false);
            }else{
                Jugador jugador = partida.getJugadores().get(jugadorID);
                int turnoActual = partida.getTurno();

                if(turnoActual == jugadorID){
                    mostrarTimerVisual(i);
                }

                if(jugador.isEsIA()){
                    setImagenJugador(i, ImageManager.IA_IMAGE_ID);
                    setNombreJugador(i, getIAName(jugadorID), turnoActual == jugadorID);
                }else{
                    UsuarioVO usuarioVO = sala.getParticipante(jugador.getJugadorID());
                    setImagenJugador(i, usuarioVO.getAvatar());
                    setNombreJugador(i, usuarioVO.getNombre(), turnoActual == jugadorID);
                }
                setNumCartas(i, jugador.getMano().size());
                if(jugadorActualID == jugadorID){
                    jugador.getMano().sort((carta1, carta2) -> {
                        boolean sePuedeUsarCarta1 = sePuedeUsarCarta(partida, carta1);
                        boolean sePuedeUsarCarta2 = sePuedeUsarCarta(partida, carta2);
                        if(sePuedeUsarCarta1 && !sePuedeUsarCarta2){
                            return -1;
                        }else if(!sePuedeUsarCarta1 && sePuedeUsarCarta2){
                            return 1;
                        }else{
                            return carta1.compareTo(carta2);
                        }
                    });
                }else{
                    jugador.getMano().sort((carta1, carta2) -> {
                        boolean sePuedeVerCarta1 = carta1.isVisiblePor(jugadorActualID);
                        boolean sePuedeVerCarta2 = carta2.isVisiblePor(jugadorActualID);
                        if(sePuedeVerCarta1 && !sePuedeVerCarta2){
                            return -1;
                        }else if(!sePuedeVerCarta1 && sePuedeVerCarta2){
                            return 1;
                        }else{
                            return carta1.compareTo(carta2);
                        }
                    });
                }

                for(Carta carta : jugador.getMano()){
                    addCarta(sala, i, jugadorID, carta);
                }
            }
        }
        
        setSentido(partida.isSentidoHorario());
        setCartaDelMedio(partida.getUltimaCartaJugada());
        setMazoRobar(esTurnoDelJugadorActual());

        if(esTurnoDelJugadorActual() && partida.isModoJugarCartaRobada()){
            CartaRobadaDialogBuilder builder =
                    new CartaRobadaDialogBuilder(this, partida.getCartaRobada(), defaultMode, sala);
            builder.show();
        }
    }

    private boolean esTurnoDelJugadorActual(){
        Sala sala = BackendAPI.getSalaActual();
        if(sala == null){
            return false;
        }
        Partida partida = sala.getPartida();
        if(partida == null){
            return false;
        }

        return jugadorActualID == partida.getTurno();
    }

    private void setSentido(boolean sentidoHorario){
        if(sentidoHorario){
            sentido.setImageResource(R.drawable.ic_sentido_horario);
        }else{
            sentido.setImageResource(R.drawable.ic_sentido_antihorario);
        }
    }

    private void setImagenJugador(int jugadorLayoutID, int imageID){
        ImageManager.setImagenPerfil(imagenesJugadores[jugadorLayoutID], imageID);
    }

    private void setNombreJugador(int jugadorLayoutID, String nombre, boolean turnoActivo){
        nombresJugadores[jugadorLayoutID].setText(acortarNombre(nombre));
        if(turnoActivo){
            nombresJugadores[jugadorLayoutID].setTextColor(TURNO_ACTIVO_COLOR);
        }else{
            nombresJugadores[jugadorLayoutID].setTextColor(TURNO_INACTIVO_COLOR);
        }
    }

    private void setNumCartas(int jugadorLayoutID, int numCartas){
        contadoresCartasJugadores[jugadorLayoutID].setText(numCartas + "");
    }

    private void setCartaDelMedio(Carta carta){
        ImageManager.setImagenCarta(cartaDelMedio, carta, defaultMode, false, true, false);
    }

    private void setMazoRobar(boolean isEnabled){
        ImageManager.setImagenMazoCartas(mazoRobar, defaultMode, isEnabled);
    }

    private boolean sePuedeUsarCarta(Partida partida, Carta carta){
        Carta cartaCentral = partida.getUltimaCartaJugada();
        return carta.getColor() == Carta.Color.comodin
                || cartaCentral.getTipo() == carta.getTipo()
                || cartaCentral.getColor() == carta.getColor();
    }

    private void addCarta(Sala sala, int jugadorLayoutID, int jugadorID, Carta carta){
        boolean isEnabled = jugadorID == jugadorActualID && esTurnoDelJugadorActual() && sePuedeUsarCarta(sala.getPartida(), carta);
        boolean isVisible = jugadorID == jugadorActualID || carta.isVisiblePor(jugadorActualID);

        ImageView imageView = new ImageView(this);
        ImageManager.setImagenCarta(imageView, carta, defaultMode, isEnabled, isVisible, jugadorID == jugadorActualID && isEnabled);
        if(jugadorID != JUGADOR_ABAJO){
            imageView.setLayoutParams(new LinearLayout.LayoutParams(150, -2));
        }

        if(jugadorID == jugadorActualID && isEnabled){
            imageView.setOnClickListener(view -> {
                if(esTurnoDelJugadorActual()){
                    if(carta.getColor() == Carta.Color.comodin){
                        SelectFourDialogBuilder builder = new SelectFourDialogBuilder(
                                this,carta, defaultMode,
                                jugada -> {
                                    new BackendAPI(this).enviarJugada(jugada);
                                    mostrarMensaje("Has jugado una carta comodín");
                                });
                        builder.show();
                    }else if(carta.getTipo() == Carta.Tipo.intercambio){
                        SelectFourDialogBuilder builder = new SelectFourDialogBuilder(
                                this, carta, sala,
                                jugada -> {
                                    new BackendAPI(this).enviarJugada(jugada);
                                    mostrarMensaje("Has jugado una carta de intercambio");
                                });
                        builder.show();
                    }else{
                        Jugada jugada = new Jugada(Arrays.asList(carta));
                        new BackendAPI(this).enviarJugada(jugada);
                        mostrarMensaje("Has jugado una carta");
                    }
                }else{
                    mostrarMensaje("Espera tu turno");
                }
            });
        }

        layoutBarajasJugadores[jugadorLayoutID].addView(imageView);
    }

    private void resetCartas(int jugadorLayoutID){
        layoutBarajasJugadores[jugadorLayoutID].removeAllViews();
    }

    private void resetCartas() {
        resetCartas(JUGADOR_ABAJO);
        resetCartas(JUGADOR_IZQUIERDA);
        resetCartas(JUGADOR_ARRIBA);
        resetCartas(JUGADOR_DERECHA);
    }

    private CancellableRunnable timerRunnable = null;
    private void mostrarTimerVisual(int jugadorLayoutID){
        if(timerRunnable != null){
            timerRunnable.cancel();
        }

        final int incremento = 100; // Incrementar el porcentaje cada 100 ms
        timerRunnable = new CancellableRunnable() {
            int i = 0;
            final int total = Partida.TIMEOUT_TURNO;
            @Override
            public void run() {
                if(isCancelled()){
                    return;
                }

                if(i <= total){
                    setPorcentaje(jugadorLayoutID, (int) ((i * 100.0) / total));
                }else{
                    cancel();
                }

                i += incremento;
            }
        };
        Task.runPeriodicTask(timerRunnable, 0, incremento);
    }
    private void setPorcentaje(int jugadorLayoutID, int porcentaje){
        for(int i=0; i<porcentajeJugadores.length; i++){
            if(i == jugadorLayoutID){
                porcentajeJugadores[i].setProgress(porcentaje, true);
            }else{
                porcentajeJugadores[i].setProgress(0, false);
            }
        }
    }

    private void mostrarLayoutJugador(int jugadorLayoutID, boolean isVisible){
        if(isVisible){
            layoutJugadores[jugadorLayoutID].setVisibility(View.VISIBLE);
            layoutBarajasJugadores[jugadorLayoutID].setVisibility(View.VISIBLE);
        }else{
            layoutJugadores[jugadorLayoutID].setVisibility(View.INVISIBLE);
            layoutBarajasJugadores[jugadorLayoutID].setVisibility(View.INVISIBLE);
        }
    }

    private void abandonarPartida(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Abandonar partida");
        builder.setMessage("¿Quieres abandonar la partida?");
        builder.setPositiveButton("Sí", (dialog, which) ->
                new BackendAPI(this).salirSala());
        builder.setNegativeButton("No", (dialog, which) ->
                dialog.dismiss());
        builder.create().show();
    }

    @Override
    public void onBackPressed(){
        abandonarPartida();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, PAUSAR_ID, Menu.NONE, "Pausar partida");
        menu.add(Menu.NONE, ABANDONAR_ID, Menu.NONE, "Abandonar partida");
        menu.add(Menu.NONE, VER_REGLAS_ID, Menu.NONE, "Ver reglas de la sala");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case PAUSAR_ID:
                mostrarMensaje("Pausar partida");
                return true;
            case ABANDONAR_ID:
                abandonarPartida();
                return true;
            case VER_REGLAS_ID:
                if(BackendAPI.getSalaActual() == null){
                    mostrarMensaje("La sala no puede ser null");
                    return false;
                }else{
                    new ReglasViewDialogBuilder(this, BackendAPI.getSalaActual().getConfiguracion()).show();
                    return true;
                }
        }
        return super.onContextItemSelected(item);
    }
}