package es.unizar.unoforall;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button botonRegistro = (Button)findViewById(R.id.botonRegistro);
        Button botonLogin = (Button)findViewById(R.id.botonLogin);

        botonRegistro.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProductPad.class)));
        botonLogin.setOnClickListener(v->startActivity(new Intent(MainActivity.this, OrderPad.class)));
    }


}