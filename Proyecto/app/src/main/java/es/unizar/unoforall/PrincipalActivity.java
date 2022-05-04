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

    ImageView imageViewPerfil;
    ImageView imageViewConfiguracion;
    ImageView imageViewAmigos;
    ImageView imageViewNotificaciones;

    private Sala salaPausada;

    private void setOpcionesEnabled(boolean enabled){
        ImageManager.setImageViewEnable(imageViewPerfil, enabled);
        ImageManager.setImageViewEnable(imageViewConfiguracion, true);
        ImageManager.setImageViewEnable(imageViewAmigos, enabled);
        ImageManager.setImageViewEnable(imageViewNotificaciones, enabled);
        ImageManager.setImageViewClickable(imageViewPerfil, enabled, false);
        ImageManager.setImageViewClickable(imageViewConfiguracion, true, false);
        ImageManager.setImageViewClickable(imageViewAmigos, enabled, false);
        ImageManager.setImageViewClickable(imageViewNotificaciones, enabled, false);
        imageViewPerfil.setEnabled(enabled);
        imageViewConfiguracion.setEnabled(true);
        imageViewAmigos.setEnabled(enabled);
        imageViewNotificaciones.setEnabled(enabled);
    }
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

        imageViewPerfil = findViewById(R.id.imageViewPerfil);
        imageViewConfiguracion = findViewById(R.id.imageViewConfiguracion);
        imageViewAmigos = findViewById(R.id.imageViewAmigos);
        imageViewNotificaciones = findViewById(R.id.imageViewNotificaciones);

        imageViewAmigos.setOnClickListener(view -> {
            Intent intent = new Intent(this, AmigosActivity.class);
            startActivityForResult(intent, 0);
        });
        registerForContextMenu(imageViewConfiguracion);
        imageViewConfiguracion.setOnClickListener(view -> view.showContextMenu(view.getX(), view.getY()));
        imageViewNotificaciones.setOnClickListener(view -> {
            Intent intent = new Intent(this, NotificacionesActivity.class);
            startActivityForResult(intent, 0);
        });
        imageViewPerfil.setOnClickListener(view -> {
            Intent intent = new Intent(this, PerfilActivity.class);
            startActivityForResult(intent, 0);
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

        layoutCrearBuscarSala.setVisibility(View.GONE);
        layoutReanudarAbandonarSala.setVisibility(View.GONE);

        api.comprobarPartidaPausada(sala -> {
            salaPausada = sala;
            if(salaPausada != null){
                // Si hay una sala pausada
                layoutReanudarAbandonarSala.setVisibility(View.VISIBLE);
                setOpcionesEnabled(false);
            }else{
                // Si no hay una sala pausada
                layoutCrearBuscarSala.setVisibility(View.VISIBLE);
                setOpcionesEnabled(true);
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
                if(salaPausada == null){
                    api.modificarCuenta();
                    return true;
                }else{
                    mostrarMensaje("No puedes modificar tu cuenta ahora mismo");
                    return false;
                }
            case MODIFICAR_ASPECTO_ID:
                if(salaPausada == null){
                    api.cambiarPersonalizacionStepOne();
                    return true;
                }else{
                    mostrarMensaje("No puedes personalizar tu aspecto ahora mismo");
                    return false;
                }
            case CERRAR_SESION_ID:
                cerrarSesion();
                return true;
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