package es.unizar.unoforall.api;

import android.app.Activity;
import android.content.Intent;

import java.util.UUID;
import java.util.function.Consumer;

import es.unizar.unoforall.InicioActivity;
import es.unizar.unoforall.utils.ActivityType;
import es.unizar.unoforall.utils.CustomActivity;
import me.i2000c.web_utils.client.RestClient;
import me.i2000c.web_utils.client.WebsocketClient;

public class WebSocketAPI {
    private CustomActivity activity;
    private WebsocketClient client;

    public void setOnError(Consumer<Exception> onError){
        client.setOnError(onError);
    }

    public WebSocketAPI(){
        this.activity = BackendAPI.getCurrentActivity();
        this.client = new WebsocketClient(RestAPI.SERVER_URL);
        this.client.setOnError(ex -> {
            ex.printStackTrace();
            close();
        });
    }

    public UUID getSessionID(){
        return client.getSessionID();
    }

    public void openConnection(String path){
        client.openConnection(path);
    }

    public <T> void subscribe(Activity activity, String topic, Class<T> expectedClass, Consumer<T> consumer){
        if(consumer == null){
            return;
        }

        client.subscribe(topic, expectedClass, object -> {
            activity.runOnUiThread(() -> consumer.accept(object));
        });
    }

    public void unsubscribe(String topic){
        client.unsubscribe(topic);
    }

    public RestAPI getRestAPI() {
        RestAPI api = new RestAPI(activity, client.getRestClient());
        if(isClosed()){
            api.close();
        }
        return api;
    }

    public boolean isClosed() {
        return client.isClosed();
    }

    public void close(){
        client.close();
    }
}
