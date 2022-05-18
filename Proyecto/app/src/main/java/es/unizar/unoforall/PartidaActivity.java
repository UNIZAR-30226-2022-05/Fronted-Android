package es.unizar.unoforall;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partidas.Carta;
import es.unizar.unoforall.model.partidas.Jugada;
import es.unizar.unoforall.model.partidas.Jugador;
import es.unizar.unoforall.model.partidas.Partida;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.ActivityType;
import es.unizar.unoforall.utils.AnimationManager;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.ImageManager;
import es.unizar.unoforall.utils.SalaReceiver;
import es.unizar.unoforall.utils.Vibration;
import es.unizar.unoforall.utils.dialogs.CartaRobadaDialogBuilder;
import es.unizar.unoforall.utils.dialogs.MostrarResultadosDialogBuilder;
import es.unizar.unoforall.utils.dialogs.PartidaDialogManager;
import es.unizar.unoforall.utils.dialogs.ReglasViewDialogBuilder;
import es.unizar.unoforall.utils.dialogs.SelectEmojiDialogBuilder;
import es.unizar.unoforall.utils.dialogs.SelectFourDialogBuilder;
import es.unizar.unoforall.utils.tasks.CancellableRunnable;
import es.unizar.unoforall.utils.tasks.Task;

public class PartidaActivity extends CustomActivity implements SalaReceiver {

    private static final int MAX_CARTAS = 20;

    private static final int PAUSAR_ID = 0;
    private static final int ABANDONAR_ID = 1;
    private static final int VER_REGLAS_ID = 2;
    private static final int CONFIGURAR_VIBRACION_ID = 3;

    private static final int JUGADOR_ABAJO = 0;
    private static final int JUGADOR_IZQUIERDA = 1;
    private static final int JUGADOR_ARRIBA = 2;
    private static final int JUGADOR_DERECHA = 3;

    private static final int TURNO_ACTIVO_COLOR = Color.GREEN;
    private static final int TURNO_INACTIVO_COLOR = Color.WHITE;

    private static final int MAX_LONG_NOMBRE = 13;

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

    private ImageView[] emojisJugadores;
    private ImageView chatEmojis;
    private ImageView alternarEmojis;

    private View fondoJugadorActual;
    private View mainView;

    private ImageButton confirmarJugadaButton;
    private ImageButton cancelarJugadaButton;

    private int jugadorActualID = -1;
    private final int[] numCartasAnteriores = {-1, -1, -1, -1};

    // Relaciona los IDs de los jugadores con los layout IDs correspondientes
    private final Map<Integer, Integer> jugadorIDmap = new HashMap<>();

    private boolean defaultMode;

    private boolean sePuedePulsarBotonUNO;
    private int turnoAnterior = -1;
    private boolean partidaFinalizada;

    private List<Carta> listaCartasEscalera;

    private static boolean emojisActivados = true;
    private static boolean vibracionActivada = true;
    private static final int DURACION_VIBRACION_MS = 100;
    
    private BackendAPI api;

    public static String getIAName(){
        return "IA";
    }
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
        
        api = new BackendAPI(this);

        partidaFinalizada = false;

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

        emojisJugadores = new ImageView[] {
                findViewById(R.id.emojiJugadorAbajo),
                findViewById(R.id.emojiJugadorIzquierda),
                findViewById(R.id.emojiJugadorArriba),
                findViewById(R.id.emojiJugadorDerecha)
        };
        for(ImageView imageView : emojisJugadores){
            imageView.setAlpha(0.0f);
        }

        chatEmojis = findViewById(R.id.chatEmojis);
        alternarEmojis = findViewById(R.id.alternarEmojis);
        ImageManager.setImagenEmojisActivados(alternarEmojis, emojisActivados);
        ImageManager.setImageViewClickable(alternarEmojis, true, false);
        ImageManager.setImageViewClickable(chatEmojis, emojisActivados, false);
        ImageManager.setImageViewEnable(chatEmojis, emojisActivados);
        alternarEmojis.setOnClickListener(view -> {
            emojisActivados = !emojisActivados;
            ImageManager.setImagenEmojisActivados(alternarEmojis, emojisActivados);
            ImageManager.setImageViewClickable(chatEmojis, emojisActivados, false);
            ImageManager.setImageViewEnable(chatEmojis, emojisActivados);
            if(emojisActivados){
                mostrarMensaje("Emojis activados");
            }else{
                mostrarMensaje("Emojis desactivados");
            }
        });
        chatEmojis.setOnClickListener(view -> {
            if(emojisActivados){
                SelectEmojiDialogBuilder builder = new SelectEmojiDialogBuilder(this);
                builder.setOnEmojiSelected(emojiID ->
                        api.enviarEmoji(jugadorActualID, emojiID));
                builder.show();
            }
        });
        api.suscribirseCanalEmojis(envioEmoji -> {
            if(!emojisActivados){
                return;
            }

            int jugadorID = envioEmoji.getEmisor();
            int emojiID = envioEmoji.getEmoji();
            if(emojiID == -1){
                return;
            }

            ImageView imageView = emojisJugadores[jugadorIDmap.get(jugadorID)];
            ImageManager.setImagenEmoji(imageView, envioEmoji.getEmoji());
            AnimationManager.animateFadeOut(imageView);
        });

        TextView textViewVotacionPausa = findViewById(R.id.textViewVotacionPausa);
        textViewVotacionPausa.setVisibility(View.INVISIBLE);
        api.suscribirseCanalVotacionPausa(respuestaVotacionPausa -> {
            int numVotos = respuestaVotacionPausa.getNumVotos();
            int numVotantes = respuestaVotacionPausa.getNumVotantes();

            if(numVotos > 0){
                textViewVotacionPausa.setText(String.format(Locale.ENGLISH,
                        "Votación pausa\n%d/%d", numVotos, numVotantes));
                textViewVotacionPausa.setVisibility(View.VISIBLE);
            }else{
                textViewVotacionPausa.setVisibility(View.INVISIBLE);
            }
        });

        cartaDelMedio = findViewById(R.id.cartaDelMedio);
        mazoRobar = findViewById(R.id.mazoRobar);

        mazoRobar.setOnClickListener(view -> {
            if(esTurnoDelJugadorActual()){
                api.enviarJugada(new Jugada());
            }else{
                mostrarMensaje("Espera tu turno");
            }
        });

        fondoJugadorActual = findViewById(R.id.fondoJugadorActual);

        listaCartasEscalera = new ArrayList<>();

        confirmarJugadaButton = findViewById(R.id.confirmarJugadaButton);
        cancelarJugadaButton = findViewById(R.id.cancelarJugadaButton);

        confirmarJugadaButton.setOnClickListener(view -> {
            Jugada jugada = new Jugada(listaCartasEscalera);
            Sala sala = BackendAPI.getSalaActual();
            if(sala != null){
                Partida partida = sala.getPartida();
                if(partida != null){
                    if(partida.validarJugada(jugada)){
                        api.enviarJugada(jugada);
                        mostrarMensaje("Has jugado un combo de " + listaCartasEscalera.size() + " carta(s)");
                    }else{
                        mostrarMensaje("Jugada inválida");
                    }
                }
            }
        });
        cancelarJugadaButton.setOnClickListener(view -> {
            // Volver a cargar las cartas del jugador actual
            Sala sala = BackendAPI.getSalaActual();
            if(sala != null){
                Partida partida = sala.getPartida();
                if(partida != null){
                    listaCartasEscalera.clear();
                    confirmarJugadaButton.setVisibility(View.GONE);
                    cancelarJugadaButton.setVisibility(View.GONE);

                    resetCartas(JUGADOR_ABAJO);
                    partida.getJugadorActual().getMano().forEach(carta ->
                            addCarta(sala, JUGADOR_ABAJO, jugadorActualID, carta));
                }
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
        if(partidaFinalizada){
            return;
        }

        Partida partida = sala.getPartida();
        if(sala.isEnPausa()){
            // Partida pausada
            Intent intent = new Intent(this, SalaActivity.class);
            this.startActivityForResult(intent,0);
            this.mostrarMensaje("Has vuelto a la sala");
            return;
        }

        int turnoActual = partida.getTurno();
        int numJugadores = partida.getJugadores().size();
        boolean esNuevoTurno = turnoActual != turnoAnterior || partida.isRepeticionTurno();

        if(jugadorActualID == -1){
            this.jugadorActualID = partida.getIndiceJugador(BackendAPI.getUsuario().getId());

            // Si defaultMode = true, se mostrará el aspecto por defecto de las cartas.
            //     Si no, se mostrará el aspecto alternativo.
            this.defaultMode = BackendAPI.getUsuario().getAspectoCartas() == 0;
            this.mainView = findViewById(R.id.layoutPantallaPartida);
            ImageManager.setImagenFondo(mainView, BackendAPI.getUsuario().getAspectoTablero());

            jugadorIDmap.clear();
            jugadorIDmap.put(jugadorActualID, JUGADOR_ABAJO);
            switch(numJugadores){
                case 2:
                    jugadorIDmap.put((jugadorActualID+1) % numJugadores, JUGADOR_ARRIBA);
                    mostrarLayoutJugador(JUGADOR_IZQUIERDA, false);
                    mostrarLayoutJugador(JUGADOR_DERECHA, false);
                    break;
                case 3:
                    jugadorIDmap.put((jugadorActualID+1) % numJugadores, JUGADOR_IZQUIERDA);
                    jugadorIDmap.put((jugadorActualID+2) % numJugadores, JUGADOR_ARRIBA);
                    mostrarLayoutJugador(JUGADOR_DERECHA, false);
                    break;
                case 4:
                    jugadorIDmap.put((jugadorActualID+1) % numJugadores, JUGADOR_IZQUIERDA);
                    jugadorIDmap.put((jugadorActualID+2) % numJugadores, JUGADOR_ARRIBA);
                    jugadorIDmap.put((jugadorActualID+3) % numJugadores, JUGADOR_DERECHA);
                    break;
            }
        }

        if(esNuevoTurno){
            if(vibracionActivada && esTurnoDelJugadorActual()){
                Vibration.vibrate(this, DURACION_VIBRACION_MS);
            }

            PartidaDialogManager.dismissCurrentDialog();
            listaCartasEscalera.clear();
            confirmarJugadaButton.setVisibility(View.GONE);
            cancelarJugadaButton.setVisibility(View.GONE);

            turnoAnterior = turnoActual;
            sePuedePulsarBotonUNO = true;
            botonUNO.setEnabled(true);
            ImageManager.setImageViewEnable(botonUNO, true);
            botonUNO.setOnClickListener(view -> {
                if(sePuedePulsarBotonUNO){
                    api.pulsarBotonUNO();
                    mostrarMensaje("Has pulsado el botón UNO");
                    ImageManager.setImageViewEnable(botonUNO, false);
                    botonUNO.setEnabled(false);
                }
                sePuedePulsarBotonUNO = false;
            });

            Jugada jugada = partida.getUltimaJugada();
            if(jugada != null && !jugada.isRobar()){
                List<Carta> cartasJugada = jugada.getCartas();
                // Es una jugada del jugador del turno anterior que no es robar
                int jugadorIDTurnoAnterior = partida.getTurnoUltimaJugada();

                AnimationManager.Builder builder = new AnimationManager.Builder((ViewGroup) mainView);
                builder
                        .withStartView(layoutJugadores[jugadorIDmap.get(jugadorIDTurnoAnterior)])
                        .withEndView(cartaDelMedio)
                        .withDefaultMode(defaultMode)
                        .withCartas(cartasJugada, true)
                        .withEndAction(() -> {
                            // Se vuelve a obtener la sala, porque podría estar desactualizada
                            Sala salaActual = BackendAPI.getSalaActual();
                            if(salaActual != null){
                                Partida partidaActual = salaActual.getPartida();
                                if(partidaActual != null){
                                    setCartaDelMedio(partidaActual.getUltimaCartaJugada());
                                }
                            }
                        })
                        .start();

                if(cartasJugada.get(0).getTipo() == Carta.Tipo.intercambio){
                    // Es un intercambio de cartas entre el jugador del turno anterior
                    //  y el jugador objetivo de la jugada

                    int jugadorIDObjetivo = jugada.getJugadorObjetivo();

                    // Mover las cartas del jugador anterior al jugador objetivo
                    AnimationManager.Builder builder1 = new AnimationManager.Builder((ViewGroup) mainView);
                    builder1
                            .withStartView(layoutJugadores[jugadorIDmap.get(jugadorIDTurnoAnterior)])
                            .withEndView(layoutJugadores[jugadorIDmap.get(jugadorIDObjetivo)])
                            .withDefaultMode(defaultMode)
                            .withCartas(partida.getJugadores().get(jugadorIDObjetivo).getMano(), false)
                            .start();

                    // Mover las cartas del jugador objetivo al jugador anterior
                    AnimationManager.Builder builder2 = new AnimationManager.Builder((ViewGroup) mainView);
                    builder2
                            .withStartView(layoutJugadores[jugadorIDmap.get(jugadorIDObjetivo)])
                            .withEndView(layoutJugadores[jugadorIDmap.get(jugadorIDTurnoAnterior)])
                            .withDefaultMode(defaultMode)
                            .withCartas(partida.getJugadores().get(jugadorIDTurnoAnterior).getMano(), false)
                            .start();
                }
            }else{
                setCartaDelMedio(partida.getUltimaCartaJugada());
            }
        }

        jugadorIDmap.forEach((jugadorID, jugadorLayoutID) -> {
            Jugador jugador = partida.getJugadores().get(jugadorID);

            if(sala.isEnPartida() && turnoActual == jugadorID && esNuevoTurno){
                mostrarTimerVisual(jugadorLayoutID);
            }

            String nombreJugador;
            int imageID;
            if(jugador.isEsIA()){
                imageID = ImageManager.IA_IMAGE_ID;
                nombreJugador = getIAName(jugadorID);
            }else{
                UsuarioVO usuarioVO = sala.getParticipante(jugador.getJugadorID());
                imageID = usuarioVO.getAvatar();
                nombreJugador = usuarioVO.getNombre();
            }
            setImagenJugador(jugadorLayoutID, imageID);
            setNombreJugador(jugadorLayoutID, nombreJugador, turnoActual == jugadorID);
            setNumCartas(jugadorLayoutID, jugador.getMano().size());

            int numCartasAntes = numCartasAnteriores[jugadorID];
            int numCartasAhora = jugador.getMano().size();
            if(numCartasAntes != -1 && numCartasAhora > numCartasAntes){
                if(partida.getUltimaCartaJugada().getTipo() != Carta.Tipo.intercambio){
                    int numCartasRobadas = numCartasAhora - numCartasAntes;
                    if(jugadorID == jugadorActualID){
                        mostrarMensaje("Has robado " + numCartasRobadas + " carta(s)");
                    }else{
                        mostrarMensaje(nombreJugador + " robó " + numCartasRobadas + " carta(s)");
                    }

                    // Mostrar animación de las cartas robadas
                    AnimationManager.Builder builder = new AnimationManager.Builder((ViewGroup) mainView);
                    builder
                            .withStartView(mazoRobar)
                            .withEndView(layoutJugadores[jugadorIDmap.get(jugadorID)])
                            .withDefaultMode(defaultMode)
                            .withCartasRobo(numCartasRobadas)
                            .start();
                }
            }
            numCartasAnteriores[jugadorID] = numCartasAhora;

            if(jugadorActualID == jugadorID){
                if(jugador.isPenalizado_UNO()){
                    mostrarMensaje("Has sido penalizado por no decir UNO");
                }

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

            if(jugadorID != jugadorActualID || listaCartasEscalera.isEmpty()){
                resetCartas(jugadorLayoutID);
                for(Carta carta : jugador.getMano()){
                    addCarta(sala, jugadorLayoutID, jugadorID, carta);
                }
            }
        });
        
        setSentido(partida.isSentidoHorario());
        setMazoRobar(esTurnoDelJugadorActual());

        if(esTurnoDelJugadorActual()){
            fondoJugadorActual.setVisibility(View.VISIBLE);

            if(partida.isModoJugarCartaRobada()){
                CartaRobadaDialogBuilder builder =
                        new CartaRobadaDialogBuilder(this, partida.getCartaRobada(), defaultMode, sala);
                builder.show();
            }else if(partida.isModoAcumulandoRobo()){
                boolean algunaCartaCompatible = false;
                for(Carta carta : partida.getJugadorActual().getMano()){
                    algunaCartaCompatible = partida.validarJugada(new Jugada(Collections.singletonList(carta)));
                    if(algunaCartaCompatible){
                        break;
                    }
                }

                if(!algunaCartaCompatible){
                    // Robar las cartas
                    api.enviarJugada(new Jugada());
                }
            }
        }else{
            fondoJugadorActual.setVisibility(View.INVISIBLE);
        }

        // Finalización de partida
        if(!sala.isEnPartida()){
            api.cancelarSuscripcionCanalEmojis();
            partidaFinalizada = true;
            resetPorcentajes();
            Snackbar.make(this, botonUNO, "PARTIDA FINALIZADA", BaseTransientBottomBar.LENGTH_INDEFINITE).show();

            // Mostrar dialog con los resultados
            MostrarResultadosDialogBuilder builder = new MostrarResultadosDialogBuilder(this, sala);
            builder.setPositiveButton(() -> {
                Intent intent = new Intent(this, SalaActivity.class);
                this.startActivityForResult(intent,0);
                this.mostrarMensaje("Has vuelto a la sala");
            });
            builder.setNegativeButton(() -> {
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
                builder2.setTitle("Abandonar partida");
                builder2.setMessage("¿Quieres abandonar la partida?");
                builder2.setPositiveButton("Sí", (dialog, which) -> api.salirSala());
                builder2.setNegativeButton("No", (dialog, which) -> builder.show());
                builder2.setOnCancelListener(dialogInterface -> builder2.show());
                builder2.create().show();
            });
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
        if(numCartas >= MAX_CARTAS){
            contadoresCartasJugadores[jugadorLayoutID].setTextColor(Color.RED);
        }else{
            contadoresCartasJugadores[jugadorLayoutID].setTextColor(Color.WHITE);
        }
        contadoresCartasJugadores[jugadorLayoutID].setText(numCartas + "");
    }

    private void setCartaDelMedio(Carta carta){
        ImageManager.setImagenCarta(cartaDelMedio, carta, defaultMode, false, true, false);
    }

    private void setMazoRobar(boolean isEnabled){
        ImageManager.setImagenMazoCartas(mazoRobar, defaultMode, isEnabled);
    }

    private boolean sePuedeUsarCarta(Partida partida, Carta carta){
        Jugada jugada = new Jugada(Collections.singletonList(carta));
        return partida.validarJugada(jugada);
    }

    private boolean comprobarEspecialFinal(Sala sala, Carta carta){
        if(esTurnoDelJugadorActual()){
            if(sala.getConfiguracion().getReglas().isEvitarEspecialFinal()){
                List<Carta> cartas = sala.getPartida().getJugadorActual().getMano();
                if(cartas.size() == 2){
                    int indiceCartaActual = cartas.indexOf(carta);
                    if(indiceCartaActual != -1){
                        int indiceOtraCarta = 1 - indiceCartaActual;
                        Carta otraCarta = cartas.get(indiceOtraCarta);
                        if(otraCarta.getColor() == Carta.Color.comodin){
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }


    private int getJugadorIDPareja(Sala sala, int jugadorID){
        if(sala.getConfiguracion().getModoJuego() == ConfigSala.ModoJuego.Parejas){
            return (jugadorID + 2) % 4;
        }else{
            return jugadorID;
        }
    }
    private void addCarta(Sala sala, int jugadorLayoutID, int jugadorID, Carta carta){
        boolean isEnabled = jugadorID == jugadorActualID && esTurnoDelJugadorActual() && sePuedeUsarCarta(sala.getPartida(), carta);
        boolean isVisible = jugadorID == jugadorActualID || jugadorID == getJugadorIDPareja(sala, jugadorActualID) || carta.isVisiblePor(jugadorActualID);

        ImageView imageView = new ImageView(this);
        if(isEnabled){
            imageView.setTag(false);    // Para indicar que no está seleccionada para el modo escalera
        }
        ImageManager.setImagenCarta(imageView, carta, defaultMode, isEnabled, isVisible, jugadorID == jugadorActualID && isEnabled);
        if(jugadorID != jugadorActualID){
            // Mostrar las cartas de los demás jugadores de tamaño pequeño
            imageView.setLayoutParams(new LinearLayout.LayoutParams(150, -2));
        }

        imageView.setOnClickListener(view -> {
            if(!listaCartasEscalera.isEmpty()){
                return;
            }

            if(jugadorID != jugadorActualID || !isEnabled){
                return;
            }

            if(esTurnoDelJugadorActual()){
                boolean esEspecialFinal = comprobarEspecialFinal(sala, carta);
                if(carta.getColor() == Carta.Color.comodin){
                    SelectFourDialogBuilder builder = new SelectFourDialogBuilder(
                            this,carta, defaultMode,
                            jugada -> {
                                api.enviarJugada(jugada);
                                if(esEspecialFinal){
                                    mostrarMensaje("Has sido penalizado por última carta comodín");
                                }else{
                                    mostrarMensaje("Has jugado una carta comodín");
                                }
                            });
                    builder.show();
                }else if(carta.getTipo() == Carta.Tipo.intercambio){
                    SelectFourDialogBuilder builder = new SelectFourDialogBuilder(
                            this, carta, sala,
                            jugada -> {
                                api.enviarJugada(jugada);
                                if(esEspecialFinal){
                                    mostrarMensaje("Has sido penalizado por última carta comodín");
                                }else{
                                    mostrarMensaje("Has jugado una carta de intercambio");
                                }
                            });
                    builder.show();
                }else{
                    Jugada jugada = new Jugada(Arrays.asList(carta));
                    api.enviarJugada(jugada);
                    if(esEspecialFinal){
                        mostrarMensaje("Has sido penalizado por última carta comodín");
                    }else{
                        mostrarMensaje("Has jugado una carta");
                    }
                }
            }else{
                mostrarMensaje("Espera tu turno");
            }
        });



        imageView.setOnLongClickListener(view -> {
            if(imageView.getTag() == null){
                return false;
            }

            if(!sala.getConfiguracion().getReglas().isJugarVariasCartas()){
                return false;
            }

            if(!Carta.esNumero(carta.getTipo())){
                return false;
            }

            Boolean estaSeleccionada = (Boolean) imageView.getTag();
            if(estaSeleccionada){
                // Ya estaba seleccionada
                mostrarMensaje("Pulsa el botón de cancelar para resetear tus cartas");
            }else{
                if(listaCartasEscalera.isEmpty()){
                    confirmarJugadaButton.setVisibility(View.VISIBLE);
                    cancelarJugadaButton.setVisibility(View.VISIBLE);

                    // Activar todas las cartas que sean números y desactivar las
                    //  que no lo sean
                    List<Carta> cartas = sala.getPartida().getJugadorActual().getMano();
                    for(int i=0;i<cartas.size();i++){
                        ImageView aux = (ImageView) layoutBarajasJugadores[jugadorLayoutID].getChildAt(i);
                        boolean esNumero = Carta.esNumero(cartas.get(i).getTipo());
                        ImageManager.setImageViewEnable(aux, esNumero);
                        ImageManager.setImageViewClickable(aux, esNumero, false);
                        if(esNumero){
                            aux.setTag(false);  // No se ha seleccionado aún, pero es seleccionable
                        }
                    }
                }

                // Marcar la carta como seleccionada
                listaCartasEscalera.add(carta);
                ImageManager.setImageViewColorFilter(imageView, ImageManager.SELECTED_CARD_COLOR);
                ImageManager.setImageViewClickable(imageView, false, false);
                imageView.setTag(true);
            }

            return true;
        });

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
    private void resetPorcentajes(){
        if(timerRunnable != null){
            timerRunnable.cancel();
        }

        setPorcentaje(-1, 0);
    }
    private void mostrarTimerVisual(int jugadorLayoutID){
        if(timerRunnable != null){
            timerRunnable.cancel();
        }

        final int incremento = 100; // Incrementar el porcentaje cada 100 ms
        timerRunnable = new CancellableRunnable() {
            int i = 0;
            final int total = Partida.TIMEOUT_TURNO-1000;
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
                porcentajeJugadores[i].setIndicatorColor(getColor(R.color.color_barra_progreso));
                porcentajeJugadores[i].setProgress(porcentaje, true);
            }else{
                porcentajeJugadores[i].setIndicatorColor(Color.BLACK);
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
                api.salirSala());
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
        menu.add(Menu.NONE, CONFIGURAR_VIBRACION_ID, Menu.NONE, "Configurar vibración");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case PAUSAR_ID:
                api.enviarVotacion();
                mostrarMensaje("Voto enviado");
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
            case CONFIGURAR_VIBRACION_ID:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Configurar vibración");
                String estadoVibracion = vibracionActivada ? "activada" : "desactivada";
                builder.setMessage("La vibración está " + estadoVibracion + ".\n¿Qué quieres hacer?");
                builder.setPositiveButton("Activarla", (dialog, which) -> vibracionActivada = true);
                builder.setNegativeButton("Desactivarla", (dialog, which) -> vibracionActivada = false);
                builder.show();
                return true;
        }
        return super.onContextItemSelected(item);
    }
}