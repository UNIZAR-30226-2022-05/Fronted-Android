package es.unizar.unoforall.utils.notifications;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import es.unizar.unoforall.R;
import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.utils.CustomActivity;

public class NotificationManager {
    protected static final String CHANNEL_ID = "unoforall";
    protected static final String ACTION_1_KEY_ID = "ACTION_1";
    protected static final String ACTION_2_KEY_ID = "ACTION_2";
    protected static final String ACTION_3_KEY_ID = "ACTION_3";
    protected static final int NOTIFICATION_ID = 1;

    static HashMap<UUID, Function<CustomActivity, Boolean>> actionsMap;

    public static void initialize(Context context){
        createNotificationChannel(context);
        actionsMap = new HashMap<>();
    }

    public static void close(Context context){
        NotificationManagerCompat.from(context).cancelAll();
        if(actionsMap != null){
            actionsMap.clear();
            actionsMap = null;
        }
    }

    private static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Canal de notificaciones";
            String description = "Recibir notificaciones";
            int importance = android.app.NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            android.app.NotificationManager notificationManager = context.getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // https://stackoverflow.com/questions/11270898/how-to-execute-a-method-by-clicking-a-notification
    public static class NotificationActionService extends IntentService{
        public NotificationActionService() {
            super(NotificationActionService.class.getSimpleName());
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            CustomActivity customActivity = BackendAPI.getCurrentActivity();

            UUID keyAction1 = (UUID) intent.getSerializableExtra(ACTION_1_KEY_ID);
            UUID keyAction2 = (UUID) intent.getSerializableExtra(ACTION_2_KEY_ID);
            UUID keyAction3 = (UUID) intent.getSerializableExtra(ACTION_3_KEY_ID);

            Function<CustomActivity, Boolean> function1 = actionsMap.get(keyAction1);
            Function<CustomActivity, Boolean> function2 = actionsMap.get(keyAction2);
            Function<CustomActivity, Boolean> function3 = actionsMap.get(keyAction3);

            if(function1 != null){
                customActivity.runOnUiThread(() -> {
                    if(function1.apply(customActivity)){
                        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
                    }
                });
            }else if(function2 != null){
                customActivity.runOnUiThread(() -> {
                    if(function2.apply(customActivity)){
                        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
                    }
                });
            }else if(function3 != null){
                customActivity.runOnUiThread(() -> {
                    if(function3.apply(customActivity)){
                        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
                    }
                });
            }
        }
    }

    public static class Builder{
        private final Notificacion notificacion;

        public Builder(Context context){
            this.notificacion = new Notificacion(context);
        }

        public Builder withTitle(String title){
            notificacion.setTitle(title);
            return this;
        }

        public Builder withMessage(String message){
            notificacion.setMessage(message);
            return this;
        }

        public Builder withAction1(String actionTitle, Function<CustomActivity, Boolean> action){
            notificacion.setAction1(actionTitle, action);
            return this;
        }

        public Builder withAction2(String actionTitle, Function<CustomActivity, Boolean> action){
            notificacion.setAction2(actionTitle, action);
            return this;
        }

        public Builder withAction3(String actionTitle, Function<CustomActivity, Boolean> action){
            notificacion.setAction3(actionTitle, action);
            return this;
        }

        public Notificacion build(){
            return notificacion;
        }
    }
}
