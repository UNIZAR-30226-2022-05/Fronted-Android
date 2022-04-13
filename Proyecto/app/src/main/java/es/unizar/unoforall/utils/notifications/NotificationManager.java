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
    private static final String CHANNEL_ID = "unoforall";
    private static final String ACTION_1_KEY_ID = "ACTION_1";
    private static final String ACTION_2_KEY_ID = "ACTION_2";
    private static final String ACTION_3_KEY_ID = "ACTION_3";
    private static final int NOTIFICATION_ID = 1;

    private static HashMap<UUID, Function<CustomActivity, Boolean>> actionsMap;

    public static void initialize(Context context){
        createNotificationChannel(context);
        actionsMap = new HashMap<>();
    }

    public static void close(Context context){
        NotificationManagerCompat.from(context).cancelAll();
        actionsMap.clear();
        actionsMap = null;
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
        private final Context context;

        private String title;
        private String message;

        private String action1title;
        private String action2title;
        private String action3title;

        private Function<CustomActivity, Boolean> action1;
        private Function<CustomActivity, Boolean> action2;
        private Function<CustomActivity, Boolean> action3;

        public Builder(Context context){
            this.context = context;

            this.title = "";
            this.message = "";

            this.action1title = "";
            this.action2title = "";
            this.action3title = "";

            this.action1 = null;
            this.action2 = null;
            this.action3 = null;
        }

        public Builder withTitle(String title){
            this.title = title;
            return this;
        }

        public Builder withMessage(String message){
            this.message = message;
            return this;
        }

        public Builder withAction1(String actionTitle, Function<CustomActivity, Boolean> action){
            this.action1title = actionTitle;
            this.action1 = action;
            return this;
        }

        public Builder withAction2(String actionTitle, Function<CustomActivity, Boolean> action){
            this.action2title = actionTitle;
            this.action2 = action;
            return this;
        }

        public Builder withAction3(String actionTitle, Function<CustomActivity, Boolean> action){
            this.action3title = actionTitle;
            this.action3 = action;
            return this;
        }

        public void build(){
            actionsMap.clear();

            if(action1 == null && action2 == null && action3 == null){
                throw new IllegalArgumentException("Al menos una de las 3 opciones debe ser distinta de null");
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_iconobarranotificaciones)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            UUID actionID1 = UUID.randomUUID();
            UUID actionID2 = UUID.randomUUID();
            UUID actionID3 = UUID.randomUUID();

            if(action1 != null){
                actionsMap.put(actionID1, action1);

                Intent intent = new Intent(context, NotificationActionService.class);
                intent.putExtra(ACTION_1_KEY_ID, actionID1);
                PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                builder.addAction(new NotificationCompat.Action(R.drawable.ic_icono_foreground, action1title, pendingIntent));
            }
            if(action2 != null){
                actionsMap.put(actionID2, action2);

                Intent intent = new Intent(context, NotificationActionService.class);
                intent.putExtra(ACTION_2_KEY_ID, actionID2);
                PendingIntent pendingIntent = PendingIntent.getService(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                builder.addAction(new NotificationCompat.Action(R.drawable.ic_icono_foreground, action2title, pendingIntent));
            }
            if(action3 != null){
                actionsMap.put(actionID3, action3);

                Intent intent = new Intent(context, NotificationActionService.class);
                intent.putExtra(ACTION_3_KEY_ID, actionID3);
                PendingIntent pendingIntent = PendingIntent.getService(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                builder.addAction(new NotificationCompat.Action(R.drawable.ic_icono_foreground, action3title, pendingIntent));
            }

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}
