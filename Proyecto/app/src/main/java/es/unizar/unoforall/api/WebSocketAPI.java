package es.unizar.unoforall.api;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompMessage;

public class WebSocketAPI {
    private static final String SERVER_IP = "ws://localhost/gs-guide-websocket";

    private Map<String, Disposable> suscripciones;
    private StompClient client;
    private boolean closed;

    public WebSocketAPI(){
        client = null;
        closed = false;
    }

    public void openConnection(){
        Stomp.over(Stomp.ConnectionProvider.OKHTTP, SERVER_IP);
        client.connect();
    }

    public void subscribe(String topic, Consumer<? super StompMessage> consumer){
        Disposable suscripcion = client.topic(topic).subscribe(consumer);
        suscripciones.put(topic, suscripcion);
    }
    public <T> void subscribe(String topic, Class<T> expectedClass, Consumer<T> consumer){
        Disposable suscripcion = client.topic(topic).subscribe(topicMessage -> {
            Gson gson = new Gson();
            T t = gson.fromJson(topicMessage.getPayload(), expectedClass);
            consumer.accept(t);
        });
        suscripciones.put(topic, suscripcion);
    }

    public void unsubscribe(String topic){
        Disposable suscripcion = suscripciones.remove(topic);
        if(suscripcion != null){
            suscripcion.dispose();
        }
    }

    public <T> void sendObject(T object, String seccion){
        Gson gson = new Gson();
        client.send(seccion, gson.toJson(object));
    }

    public void close(){
        client.disconnect();
    }
}
