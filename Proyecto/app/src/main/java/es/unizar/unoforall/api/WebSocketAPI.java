package es.unizar.unoforall.api;

import android.app.Activity;

import java.util.function.Consumer;

import me.i2000c.web_utils.client.RestClient;
import me.i2000c.web_utils.client.WebsocketClient;

public class WebSocketAPI {
    private WebsocketClient client;

    public void setOnError(Consumer<Exception> onError){
        client.setOnError(onError);
    }

    public static void setServerIP(String serverIP){
    }

    public WebSocketAPI(){
        this.client = new WebsocketClient(RestAPI.SERVER_URL);
        this.client.setOnError(ex -> {
            ex.printStackTrace();
            close();
        });
    }

    public void openConnection(String path){
        client.openConnection(path);
    }

    public <T> void subscribe(String topic, Class<T> expectedClass, Consumer<T> consumer) {
        client.subscribe(topic, expectedClass, consumer);
    }

    public <T> void subscribe(Activity activity, String topic, Class<T> expectedClass, Consumer<T> consumer){
        client.subscribe(topic, expectedClass, object -> {
            activity.runOnUiThread(() -> consumer.accept(object));
        });
    }

    public void unsubscribe(String topic){
        client.unsubscribe(topic);
    }

    public RestClient getRestClient() {
        return client.getRestClient();
    }

    public boolean isClosed() {
        return client.isClosed();
    }

    public void close(){
        client.close();
    }
}
