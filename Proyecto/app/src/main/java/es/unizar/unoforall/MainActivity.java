package es.unizar.unoforall;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;

import es.unizar.unoforall.pruebas_spring.PruebaClienteSpring;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);

        Button botonRegistro = (Button)findViewById(R.id.botonRegistro);
        Button botonLogin = (Button)findViewById(R.id.botonLogin);

        botonRegistro.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RegisterActivity.class)));
        botonLogin.setOnClickListener(v->startActivity(new Intent(MainActivity.this, LoginActivity.class)));
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Salir de UnoForAll");
        builder.setMessage("¿Quieres salir de UnoForAll?");
        builder.setPositiveButton("Sí", (dialog, which) -> {
            System.exit(0);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
           dialog.dismiss();
        });
        builder.create().show();
    }
}