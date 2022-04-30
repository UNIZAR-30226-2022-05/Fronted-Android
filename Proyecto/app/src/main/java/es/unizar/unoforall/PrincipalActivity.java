package es.unizar.unoforall;

import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.ActivityType;

public class PrincipalActivity extends CustomActivity {

    private static final int MODIFICAR_CUENTA_ID = 0;
    private static final int MODIFICAR_ASPECTO_ID = 1;
    private static final int GESTIONAR_AMIGOS_ID = 2;
    private static final int VER_ESTADISTICAS_ID = 3;
    private static final int VER_HISTORIAL_ID = 4;
    private static final int BORRAR_CUENTA_ID = 5;
    private static final int CERRAR_SESION_ID = 6;

    private static boolean sesionIniciada = false;

    private BackendAPI api;

    private Button crearSalaButton;
    private Button buscarSalaButton;
    private Button reanudarSalaButton;
    private Button abandonarSalaButton;

    private Sala salaPausada;

    private void inicializarButtons(){
        crearSalaButton = findViewById(R.id.crearSalaButton);
        crearSalaButton.setOnClickListener(v -> startActivityForResult(new Intent(this, CrearSalaActivity.class), 0));

        buscarSalaButton = findViewById(R.id.buscarSalaPublicaButton);
        buscarSalaButton.setOnClickListener(v -> startActivityForResult(new Intent(this, BuscarSalaActivity.class), 0));

        reanudarSalaButton = findViewById(R.id.reanudarSalaButton);
        reanudarSalaButton.setOnClickListener(view -> api.unirseSala(salaPausada.getSalaID()));

        abandonarSalaButton = findViewById(R.id.abandonarSalaButton);
        abandonarSalaButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Salir de la sala pausada");
            builder.setMessage("¿Quieres salir de la sala pausada?");
            builder.setPositiveButton("Sí", (dialog, which) -> {
                BackendAPI.setSalaActualID(salaPausada.getSalaID());
                BackendAPI.setSalaActual(salaPausada);
                api.salirSalaDefinitivo();
                crearSalaButton.setVisibility(View.VISIBLE);
                buscarSalaButton.setVisibility(View.VISIBLE);
                reanudarSalaButton.setVisibility(View.GONE);
                abandonarSalaButton.setVisibility(View.GONE);
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            builder.create().show();
        });
    }
    private void setButtonsEnabled(boolean enabled){
        crearSalaButton.setEnabled(enabled);
        buscarSalaButton.setEnabled(enabled);
        if(enabled){
            crearSalaButton.setBackgroundColor(Color.parseColor("#2EC322"));
            buscarSalaButton.setBackgroundColor(Color.parseColor("#2EC322"));
        }else{
            crearSalaButton.setBackgroundColor(Color.LTGRAY);
            buscarSalaButton.setBackgroundColor(Color.LTGRAY);
        }
    }

    @Override
    public ActivityType getType(){
        return ActivityType.PRINCIPAL;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        setTitle(R.string.pantallaPrincipal);

        api = new BackendAPI(this);

        inicializarButtons();
        setButtonsEnabled(true);
        sesionIniciada = true;

        api.comprobarPartidaPausada(sala -> {
            salaPausada = sala;
            if(salaPausada != null){
                // Si hay una sala pausada
                crearSalaButton.setVisibility(View.GONE);
                buscarSalaButton.setVisibility(View.GONE);
                reanudarSalaButton.setVisibility(View.VISIBLE);
                abandonarSalaButton.setVisibility(View.VISIBLE);
            }else{
                // Si no hay una sala pausada
                crearSalaButton.setVisibility(View.VISIBLE);
                buscarSalaButton.setVisibility(View.VISIBLE);
                reanudarSalaButton.setVisibility(View.GONE);
                abandonarSalaButton.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MODIFICAR_CUENTA_ID, Menu.NONE, R.string.modificarCuenta);
        menu.add(Menu.NONE, MODIFICAR_ASPECTO_ID, Menu.NONE, R.string.modificarAspecto);
        menu.add(Menu.NONE, GESTIONAR_AMIGOS_ID, Menu.NONE, R.string.gestionarAmigos);
        menu.add(Menu.NONE, VER_ESTADISTICAS_ID, Menu.NONE, R.string.verEstadisticas);
        menu.add(Menu.NONE, VER_HISTORIAL_ID, Menu.NONE, R.string.verHistorial);
        menu.add(Menu.NONE, BORRAR_CUENTA_ID, Menu.NONE, R.string.borrarCuenta);
        menu.add(Menu.NONE, CERRAR_SESION_ID, Menu.NONE, R.string.cerrarSesion);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(!sesionIniciada){
            return false;
        }

        switch(item.getItemId()){
            case MODIFICAR_CUENTA_ID:
                new BackendAPI(this).modificarCuenta();
                break;
            case MODIFICAR_ASPECTO_ID:
                new BackendAPI(this).cambiarPersonalizacionStepOne();
                break;
            case BORRAR_CUENTA_ID:
                new BackendAPI(this).borrarCuenta();
                break;
            case GESTIONAR_AMIGOS_ID:
                Intent intent = new Intent(this, AmigosActivity.class);
                startActivityForResult(intent, 0);
                break;
            case CERRAR_SESION_ID:
                cerrarSesion();
                break;
            default:
                mostrarMensaje("No implementado todavía");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        cerrarSesion();
    }

    private void cerrarSesion(){
        if(!sesionIniciada){
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cerrar sesión");
        builder.setMessage("¿Quieres cerrar sesión?");
        builder.setPositiveButton("Sí", (dialog, which) -> {
            BackendAPI.closeWebSocketAPI();
            Intent intent = new Intent(this, InicioActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent, 0);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }
}