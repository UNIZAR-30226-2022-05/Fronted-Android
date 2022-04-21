package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import es.unizar.unoforall.api.RestAPI;
import es.unizar.unoforall.api.WebSocketAPI;
import es.unizar.unoforall.model.partidas.Carta;
import es.unizar.unoforall.utils.ActivityType;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.ImageManager;
import es.unizar.unoforall.utils.dialogs.SetIPDialogBuilder;
import es.unizar.unoforall.utils.tasks.CancellableRunnable;
import es.unizar.unoforall.utils.tasks.Task;

public class PartidaActivity extends CustomActivity {

    private static final int OPCION_0_ID = 0;
    private static final int OPCION_1_ID = 1;
    private static final int OPCION_2_ID = 2;

    private static final int JUGADOR_ABAJO = 0;
    private static final int JUGADOR_IZQUIERDA = 1;
    private static final int JUGADOR_ARRIBA = 2;
    private static final int JUGADOR_DERECHA = 3;

    private static final int TURNO_ACTIVO_COLOR = Color.GREEN;
    private static final int TURNO_INACTIVO_COLOR = Color.WHITE;

    private static final int MAX_LONG_NOMBRE = 16;

    private LinearLayout[] layoutBarajasJugadores;
    private ImageView sentido;
    private ImageView[] imagenesJugadores;
    private TextView[] nombresJugadores;
    private TextView[] contadoresCartasJugadores;

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

        layoutBarajasJugadores = new LinearLayout[] {
                findViewById(R.id.barajaJugadorAbajo),
                findViewById(R.id.barajaJugadorIzquierda),
                findViewById(R.id.barajaJugadorArriba),
                findViewById(R.id.barajaJugadorDerecha)};

        sentido = findViewById(R.id.sentido);

        ImageView botonMenu = findViewById(R.id.botonMenu);
        registerForContextMenu(botonMenu);
        botonMenu.setOnClickListener(view -> view.showContextMenu(view.getX(), view.getY()));

        ImageView botonUNO = findViewById(R.id.botonUNO);
        botonUNO.setOnClickListener(view -> {
            mostrarMensaje("Has pulsado el botón UNO");
        });

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

        test();
    }

    public void setSentido(boolean sentidoHorario){
        if(sentidoHorario){
            sentido.setImageResource(R.drawable.ic_sentido_horario);
        }else{
            sentido.setImageResource(R.drawable.ic_sentido_antihorario);
        }
    }

    public void setNombreJugador(int jugador, String nombre, boolean turnoActivo){
        if(nombre.length() > MAX_LONG_NOMBRE){
            nombre = nombre.substring(0, MAX_LONG_NOMBRE-3) + "...";
        }

        nombresJugadores[jugador].setText(nombre);
        if(turnoActivo){
            nombresJugadores[jugador].setTextColor(TURNO_ACTIVO_COLOR);
        }else{
            nombresJugadores[jugador].setTextColor(TURNO_INACTIVO_COLOR);
        }
    }

    public void setNumCartas(int jugador, int numCartas){
        contadoresCartasJugadores[jugador].setText(numCartas + "");
    }

    private void addCarta(int jugador, Carta carta, boolean defaultMode, boolean isDisabled, boolean isVisible){
        ImageView imageView = new ImageView(this);
        ImageManager.setImagenCarta(imageView, carta, defaultMode, isDisabled, isVisible);
        if(jugador != JUGADOR_ABAJO){
            imageView.setLayoutParams(new LinearLayout.LayoutParams(150, -2));
        }
        layoutBarajasJugadores[jugador].addView(imageView);
    }

    private void resetCartas(int jugador){
        layoutBarajasJugadores[jugador].removeAllViews();
    }

    private void resetCartas() {
        resetCartas(JUGADOR_ABAJO);
        resetCartas(JUGADOR_IZQUIERDA);
        resetCartas(JUGADOR_ARRIBA);
        resetCartas(JUGADOR_DERECHA);
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, OPCION_0_ID, Menu.NONE, "OPCIÓN 0");
        menu.add(Menu.NONE, OPCION_1_ID, Menu.NONE, "OPCIÓN 1");
        menu.add(Menu.NONE, OPCION_2_ID, Menu.NONE, "OPCIÓN 2");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        mostrarMensaje(item.getItemId() + "");
        switch(item.getItemId()) {
            case OPCION_0_ID:
                mostrarMensaje("OPCIÓN 0");
                return true;
            case OPCION_1_ID:
                mostrarMensaje("OPCIÓN 1");
                return true;
            case OPCION_2_ID:
                mostrarMensaje("OPCIÓN 2");
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void test(){
        // Para inicializar el HashMap es necesario usar al menos una carta
        ImageManager.setImagenCarta(new ImageView(this), new Carta(Carta.Tipo.n0, Carta.Color.verde), true, false, true);
        Task.runDelayedTask(new CancellableRunnable() {
            private final ArrayList<Carta> defaultCards = new ArrayList<>(ImageManager.getDefaultCardsMap().keySet());
            @Override
            public void run() {
                runOnUiThread(() -> {
                    resetCartas();
                    for(int i=0;i<5;i++){
                        Carta carta = defaultCards.get(new Random().nextInt(defaultCards.size()));
                        for(int j=0;j<4;j++) addCarta(j, carta, true, false, true);
                    }
                });
            }
        }, 0);
    }
}