package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.graphics.Color;
import android.os.Bundle;
import android.system.ErrnoException;
import android.widget.ImageView;
import android.widget.LinearLayout;

import es.unizar.unoforall.model.partidas.Carta;
import es.unizar.unoforall.utils.ImageManager;
import es.unizar.unoforall.utils.tasks.CancellableRunnable;
import es.unizar.unoforall.utils.tasks.Task;

public class PartidaActivity extends AppCompatActivity {

    private static final int JUGADOR_ABAJO = 0;
    private static final int JUGADOR_IZQUIERDA = 1;
    private static final int JUGADOR_ARRIBA = 2;
    private static final int JUGADOR_DERECHA = 3;

    private LinearLayout[] layoutBarajasJugadores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partida);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        LinearLayout layoutAbajo = findViewById(R.id.barajaJugadorAbajo);
        LinearLayout layoutArriba = findViewById(R.id.barajaJugadorArriba);
        LinearLayout layoutDerecha = findViewById(R.id.barajaJugadorDerecha);
        LinearLayout layoutIzquierda = findViewById(R.id.barajaJugadorIzquierda);
        layoutBarajasJugadores = new LinearLayout[] {layoutAbajo, layoutIzquierda, layoutArriba, layoutDerecha};

        for(Carta.Tipo tipo : Carta.Tipo.values()){
            Carta carta = new Carta(tipo, Carta.Color.verde);
            addCarta(JUGADOR_ABAJO, carta, true, false, true);
            addCarta(JUGADOR_IZQUIERDA, carta, true, false, false);
            addCarta(JUGADOR_DERECHA, carta, true, false, false);
            addCarta(JUGADOR_ARRIBA, carta, true, false, false);
        }
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
}