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
        if(isClosed()){
            gotoPantallaInicial();
        }

        client.openConnection(path);
    }

    public <T> void subscribe(Activity activity, String topic, Class<T> expectedClass, Consumer<T> consumer){
        if(isClosed()){
            gotoPantallaInicial();
        }

        if(consumer == null){
            return;
        }

        client.subscribe(topic, expectedClass, object -> {
            activity.runOnUiThread(() -> consumer.accept(object));
        });
    }

    public void unsubscribe(String topic){
        if(isClosed()){
            gotoPantallaInicial();
        }

        client.unsubscribe(topic);
    }

    public RestAPI getRestAPI() {
        if(isClosed()){
            gotoPantallaInicial();
        }

        RestClient restClient = client.getRestClient();
        if(restClient == null){
            restClient = new RestClient("");
            restClient.close();
        }

        RestAPI api = new RestAPI(activity, restClient);
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

    private void gotoPantallaInicial(){
        Intent intent = new Intent(activity, InicioActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivityForResult(intent, 0);
        activity.mostrarMensaje("La sesión ha caducado");
    }
}
