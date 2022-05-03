package es.unizar.unoforall;

import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.ActivityType;
import es.unizar.unoforall.utils.ImageManager;
import es.unizar.unoforall.utils.dialogs.ReglasViewDialogBuilder;

public class PrincipalActivity extends CustomActivity {

    private static final int MODIFICAR_CUENTA_ID = 0;
    private static final int MODIFICAR_ASPECTO_ID = 1;
    private static final int CERRAR_SESION_ID = 2;

    private static boolean sesionIniciada = false;

    private BackendAPI api;

    private LinearLayout layoutCrearBuscarSala;
    private LinearLayout layoutReanudarAbandonarSala;

    private Button crearSalaButton;
    private Button buscarSalaButton;
    private Button reanudarSalaButton;
    private Button abandonarSalaButton;

    private Sala salaPausada;

    private void inicializar(){
        layoutCrearBuscarSala = findViewById(R.id.layoutCrearBuscarSala);
        layoutReanudarAbandonarSala = findViewById(R.id.layoutReanudarAbandonarSala);

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
                layoutReanudarAbandonarSala.setVisibility(View.GONE);
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

        inicializar();
        setButtonsEnabled(true);
        sesionIniciada = true;

        ImageManager.setImagenFondo(findViewById(R.id.mainView), BackendAPI.getUsuario().getAspectoTablero());

        ImageView imageViewPerfil = findViewById(R.id.imageViewPerfil);
        ImageView imageViewConfiguracion = findViewById(R.id.imageViewConfiguracion);
        ImageView imageViewAmigos = findViewById(R.id.imageViewAmigos);
        ImageView imageViewNotificaciones = findViewById(R.id.imageViewNotificaciones);

        ImageManager.setImageViewClickable(imageViewPerfil, true, true);
        ImageManager.setImageViewClickable(imageViewConfiguracion, true, true);
        ImageManager.setImageViewClickable(imageViewAmigos, true, true);
        ImageManager.setImageViewClickable(imageViewNotificaciones, true, true);

        imageViewAmigos.setOnClickListener(view -> {
            Intent intent = new Intent(this, AmigosActivity.class);
            startActivityForResult(intent, 0);
        });

        registerForContextMenu(imageViewConfiguracion);
        imageViewConfiguracion.setOnClickListener(view -> view.showContextMenu(view.getX(), view.getY()));

        layoutCrearBuscarSala.setVisibility(View.GONE);
        layoutReanudarAbandonarSala.setVisibility(View.GONE);

        api.comprobarPartidaPausada(sala -> {
            salaPausada = sala;
            if(salaPausada != null){
                // Si hay una sala pausada
                layoutReanudarAbandonarSala.setVisibility(View.VISIBLE);
            }else{
                // Si no hay una sala pausada
                layoutCrearBuscarSala.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onBackPressed(){
        cerrarSesion();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, MODIFICAR_CUENTA_ID, Menu.NONE, R.string.modificarCuenta);
        menu.add(Menu.NONE, MODIFICAR_ASPECTO_ID, Menu.NONE, R.string.modificarAspecto);
        menu.add(Menu.NONE, CERRAR_SESION_ID, Menu.NONE, R.string.cerrarSesion);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case MODIFICAR_CUENTA_ID:
                api.modificarCuenta();
                break;
            case MODIFICAR_ASPECTO_ID:
                api.cambiarPersonalizacionStepOne();
                break;
            case CERRAR_SESION_ID:
                cerrarSesion();
                break;
        }
        return super.onContextItemSelected(item);
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