package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

import es.unizar.unoforall.model.partidas.Carta;
import es.unizar.unoforall.utils.ImageManager;
import es.unizar.unoforall.utils.tasks.CancellableRunnable;
import es.unizar.unoforall.utils.tasks.Task;

public class PartidaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partida);

        ImageView imageView = findViewById(R.id.cartaDePrueba);

        Task.runPeriodicTask(new CancellableRunnable() {
            private int tipoID = 0;
            private int colorID = 0;
            private boolean normalMode = true;
            private Carta carta = new Carta(Carta.Tipo.values()[tipoID], Carta.Color.values()[colorID]);
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if(tipoID >= Carta.Tipo.values().length){
                        tipoID = 0;
                        colorID++;
                        if(colorID == Carta.Color.comodin.ordinal()){
                            tipoID = Carta.Tipo.cambioColor.ordinal();
                        }
                        if(colorID >= Carta.Color.values().length){
                            colorID = 0;
                            normalMode = !normalMode;
                        }
                    }
                    carta.setTipo(Carta.Tipo.values()[tipoID]);
                    carta.setColor(Carta.Color.values()[colorID]);
                    ImageManager.setImagenCarta(imageView, carta, normalMode);
                    tipoID++;
                });
            }
        }, 0, 250);
    }
}