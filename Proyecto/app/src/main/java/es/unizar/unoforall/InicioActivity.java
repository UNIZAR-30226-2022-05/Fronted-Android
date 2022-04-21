package es.unizar.unoforall;

import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.api.RestAPI;
import es.unizar.unoforall.api.WebSocketAPI;
import es.unizar.unoforall.model.partidas.Partida;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.dialogs.SetIPDialogBuilder;
import es.unizar.unoforall.utils.notifications.NotificationManager;
import es.unizar.unoforall.utils.ActivityType;

public class InicioActivity extends CustomActivity {

    private static final int CAMBIAR_IP_ID = 0;

    @Override
    public ActivityType getType(){
        return ActivityType.INICIO;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        setTitle(R.string.app_name);

        Button botonRegistro = findViewById(R.id.botonRegistro);
        Button botonLogin = findViewById(R.id.botonLogin);

        botonRegistro.setOnClickListener(v -> startActivityForResult(new Intent(InicioActivity.this, RegisterActivity.class), 0));
        botonLogin.setOnClickListener(v->startActivityForResult(new Intent(InicioActivity.this, LoginActivity.class), 0));

        NotificationManager.initialize(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, CAMBIAR_IP_ID, Menu.NONE, "Cambiar IP del servidor");
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == CAMBIAR_IP_ID){
            SetIPDialogBuilder builder = new SetIPDialogBuilder(this);
            builder.setPositiveButton(serverIP -> {
                RestAPI.setServerIP(serverIP);
                WebSocketAPI.setServerIP(serverIP);
                mostrarMensaje("IP cambiada con éxito a " + serverIP);
            });
            builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Salir de UnoForAll");
        builder.setMessage("¿Quieres salir de UnoForAll?");
        builder.setPositiveButton("Sí", (dialog, which) -> {
            finish();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
           dialog.dismiss();
        });
        builder.create().show();
    }

    @Override
    protected void onDestroy() {
        NotificationManager.close(this);
        BackendAPI.closeWebSocketAPI();
        super.onDestroy();
    }
}