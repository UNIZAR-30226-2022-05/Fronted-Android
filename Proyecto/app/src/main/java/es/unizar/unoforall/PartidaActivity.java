package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class PartidaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_partida);

        LinearLayout l = findViewById(R.id.aaa);
        l.addView(LayoutInflater.from(this).inflate(R.layout.baraja, null));
    }
}