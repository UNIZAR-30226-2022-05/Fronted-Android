package es.unizar.unoforall.utils.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import es.unizar.unoforall.R;
import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.utils.CustomActivity;

public class Notificacion{
    private final Context context;

    private String title;
    private String message;

    private String action1title;
    private String action2title;
    private String action3title;

    private Function<CustomActivity, Boolean> action1;
    private Function<CustomActivity, Boolean> action2;
    private Function<CustomActivity, Boolean> action3;

    public Notificacion(Context context){
        this.context = context;
    }

    public Context getContext(){
        return context;
    }

    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title = title;
    }

    public String getMessage(){
        return message;
    }
    public void setMessage(String message){
        this.message = message;
    }

    public String getAction1title(){
        return action1title;
    }
    public String getAction2title(){
        return action2title;
    }
    public String getAction3title(){
        return action3title;
    }

    public Function<CustomActivity, Boolean> getAction1(){
        return action1;
    }
    public Function<CustomActivity, Boolean> getAction2(){
        return action2;
    }
    public Function<CustomActivity, Boolean> getAction3(){
        return action3;
    }

    public void setAction1(String action1title, Function<CustomActivity, Boolean> action1){
        this.action1title = action1title;
        this.action1 = action1;
    }
    public void setAction2(String action2title, Function<CustomActivity, Boolean> action2){
        this.action2title = action2title;
        this.action2 = action2;
    }
    public void setAction3(String action3title, Function<CustomActivity, Boolean> action3){
        this.action3title = action3title;
        this.action3 = action3;
    }

    public void show(){
        NotificationManager.actionsMap.clear();

        if(action1 == null && action2 == null && action3 == null){
            throw new IllegalArgumentException("Al menos una de las 3 opciones debe ser distinta de null");
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationManager.CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_iconobarranotificaciones)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        UUID actionID1 = UUID.randomUUID();
        UUID actionID2 = UUID.randomUUID();
        UUID actionID3 = UUID.randomUUID();

        if(action1 != null){
            NotificationManager.actionsMap.put(actionID1, action1);

            Intent intent = new Intent(context, NotificationManager.NotificationActionService.class);
            intent.putExtra(NotificationManager.ACTION_1_KEY_ID, actionID1);
            PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            builder.addAction(new NotificationCompat.Action(R.drawable.ic_icono_foreground, action1title, pendingIntent));
        }
        if(action2 != null){
            NotificationManager.actionsMap.put(actionID2, action2);

            Intent intent = new Intent(context, NotificationManager.NotificationActionService.class);
            intent.putExtra(NotificationManager.ACTION_2_KEY_ID, actionID2);
            PendingIntent pendingIntent = PendingIntent.getService(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            builder.addAction(new NotificationCompat.Action(R.drawable.ic_icono_foreground, action2title, pendingIntent));
        }
        if(action3 != null){
            NotificationManager.actionsMap.put(actionID3, action3);

            Intent intent = new Intent(context, NotificationManager.NotificationActionService.class);
            intent.putExtra(NotificationManager.ACTION_3_KEY_ID, actionID3);
            PendingIntent pendingIntent = PendingIntent.getService(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            builder.addAction(new NotificationCompat.Action(R.drawable.ic_icono_foreground, action3title, pendingIntent));
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NotificationManager.NOTIFICATION_ID, builder.build());
    }

    private void setButtonColor(Button button){
        String text = button.getText().toString();
        if(text.equalsIgnoreCase(Notificaciones.ACEPTAR_TEXT)){
            button.setBackgroundColor(Color.parseColor("#2EC322"));
            button.setTextColor(Color.WHITE);
        }else if(text.equalsIgnoreCase(Notificaciones.CANCELAR_TEXT)){
            button.setBackgroundColor(Color.parseColor("#B61D1D"));
            button.setTextColor(Color.WHITE);
        }
    }

    public void applyToNotificacionView(View notificacionView){
        TextView tituloTextView = notificacionView.findViewById(R.id.tituloTextView);
        TextView mensajeTextView = notificacionView.findViewById(R.id.mensajeTextView);
        Button accion1Button = notificacionView.findViewById(R.id.accion1Button);
        Button accion2Button = notificacionView.findViewById(R.id.accion2Button);
        Button accion3Button = notificacionView.findViewById(R.id.accion3Button);

        tituloTextView.setText(this.title);
        mensajeTextView.setText(this.message);

        if(this.action1 != null){
            accion1Button.setText(this.action1title);
            setButtonColor(accion1Button);
            /*accion1Button.setOnClickListener(view ->
                action1.apply(BackendAPI.getCurrentActivity()));*/
            accion1Button.setOnClickListener(view -> {
                action1.apply(BackendAPI.getCurrentActivity());
                System.err.println("BOTÓN 1 PULSADO");
            });
        }else{
            accion1Button.setVisibility(View.GONE);
        }

        if(this.action2 != null){
            accion2Button.setText(this.action2title);
            setButtonColor(accion2Button);
            accion2Button.setOnClickListener(view ->
                action2.apply(BackendAPI.getCurrentActivity()));
        }else{
            accion2Button.setVisibility(View.GONE);
        }

        if(this.action3 != null){
            accion3Button.setText(this.action3title);
            setButtonColor(accion3Button);
            accion3Button.setOnClickListener(view ->
                action3.apply(BackendAPI.getCurrentActivity()));
        }else{
            accion3Button.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notificacion that = (Notificacion) o;
        return title.equals(that.title) && message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, message);
    }
}
