package es.unizar.unoforall;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
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

    private static final String AZURE_IP = "unoforall.westeurope.cloudapp.azure.com";
    private static final boolean MODO_PRODUCCION = false;

    private static final int CAMBIAR_IP_ID = 0;
    private static final int REQUEST_DISABLE_BATTERY_OPTIMIZATION_INTENT = 123;
    private static final int DISABLE_OK = -1;
    private static final int DISABLE_FAILED = 0;

    private Button botonRegistro;
    private Button botonLogin;

    @Override
    public ActivityType getType(){
        return ActivityType.INICIO;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        setTitle(R.string.app_name);

        botonRegistro = findViewById(R.id.botonRegistro);
        botonLogin = findViewById(R.id.botonLogin);

        botonRegistro.setEnabled(false);
        botonLogin.setEnabled(false);

        botonRegistro.setOnClickListener(v -> startActivityForResult(new Intent(InicioActivity.this, RegisterActivity.class), 0));
        botonLogin.setOnClickListener(v->startActivityForResult(new Intent(InicioActivity.this, LoginActivity.class), 0));

        NotificationManager.initialize(this);

        requestDisableBatteryOptimization();

        if(MODO_PRODUCCION){
            RestAPI.setServerIP(AZURE_IP);
            WebSocketAPI.setServerIP(AZURE_IP);
        }
    }

    private void requestDisableBatteryOptimization(){
        String pkg=getPackageName();
        PowerManager pm=getSystemService(PowerManager.class);
        if (pm.isIgnoringBatteryOptimizations(pkg)) {
            botonRegistro.setEnabled(true);
            botonLogin.setEnabled(true);
            return;
        }

        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Desactivar optimización de batería");
            builder.setMessage("UnoForAll requiere desactivar el modo " +
                    "de optimización de batería para funcionar correctamente." +
                    "\n" +
                    "¿Deseas desactivarlo?");
            builder.setPositiveButton("Sí", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                .setData(Uri.parse("package:" + pkg));
                startActivityForResult(intent, REQUEST_DISABLE_BATTERY_OPTIMIZATION_INTENT);
            });
            builder.setNegativeButton("No, salir de la aplicación", (dialog, which) -> {
                finish();
            });
            builder.setOnCancelListener(dialog -> builder.show());
            builder.show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_DISABLE_BATTERY_OPTIMIZATION_INTENT){
            switch(resultCode){
                case DISABLE_OK:
                    mostrarMensaje("Modo de optimización de batería desactivado");
                    botonRegistro.setEnabled(true);
                    botonLogin.setEnabled(true);
                    break;
                case DISABLE_FAILED:
                    requestDisableBatteryOptimization();
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        if(MODO_PRODUCCION){
            return super.onCreateOptionsMenu(menu);
        }

        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, CAMBIAR_IP_ID, Menu.NONE, "Cambiar IP del servidor");
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(MODO_PRODUCCION){
            return super.onOptionsItemSelected(item);
        }

        String pkg=getPackageName();
        PowerManager pm=getSystemService(PowerManager.class);
        if (!pm.isIgnoringBatteryOptimizations(pkg)) {
            return false;
        }

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