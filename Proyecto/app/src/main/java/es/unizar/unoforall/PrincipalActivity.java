package es.unizar.unoforall;

import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.ActivityType;

public class PrincipalActivity extends CustomActivity {

    private static final int MODIFICAR_CUENTA_ID = 0;
    private static final int MODIFICAR_ASPECTO_ID = 1;
    private static final int GESTIONAR_AMIGOS_ID = 2;
    private static final int VER_ESTADISTICAS_ID = 3;
    private static final int VER_HISTORIAL_ID = 4;
    private static final int BORRAR_CUENTA_ID = 5;

    private static boolean sesionIniciada = false;

    private Button crearSalaButton;
    private Button buscarSalaButton;

    private void inicializarButtons(){
        crearSalaButton = findViewById(R.id.crearSalaButton);
        crearSalaButton.setOnClickListener(v -> startActivityForResult(new Intent(this, CrearSalaActivity.class), 0));

        buscarSalaButton = findViewById(R.id.buscarSalaPublicaButton);
        buscarSalaButton.setOnClickListener(v -> startActivityForResult(new Intent(this, BuscarSalaActivity.class), 0));
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

        inicializarButtons();
        setButtonsEnabled(true);
        sesionIniciada = true;
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
            case BORRAR_CUENTA_ID:
                new BackendAPI(this).borrarCuenta();
                break;
            case GESTIONAR_AMIGOS_ID:
                Intent intent = new Intent(this, AmigosActivity.class);
                startActivityForResult(intent, 0);
                break;
            default:
                mostrarMensaje("No implementado todavía");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
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