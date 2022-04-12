package es.unizar.unoforall;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import es.unizar.unoforall.api.RestAPI;
import es.unizar.unoforall.api.WebSocketAPI;
import es.unizar.unoforall.utils.dialogs.SetIPDialogBuilder;

public class InicioActivity extends AppCompatActivity {

    private static final int CAMBIAR_IP_ID = 0;
    private static final String CHANNEL_ID = NotificationChannel.DEFAULT_CHANNEL_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        setTitle(R.string.app_name);

        createNotificationChannel();

        Button botonRegistro = (Button)findViewById(R.id.botonRegistro);
        Button botonLogin = (Button)findViewById(R.id.botonLogin);

        botonRegistro.setOnClickListener(v -> startActivity(new Intent(InicioActivity.this, RegisterActivity.class)));
        botonLogin.setOnClickListener(v->startActivity(new Intent(InicioActivity.this, LoginActivity.class)));
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
                Toast.makeText(this, "IP cambiada con éxito a " + serverIP, Toast.LENGTH_SHORT).show();
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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channelName);
            String description = getString(R.string.channelDescription);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static String getChannelId() {
        return CHANNEL_ID;
    }
}