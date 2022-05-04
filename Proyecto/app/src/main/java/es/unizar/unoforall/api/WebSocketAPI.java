package es.unizar.unoforall.api;

import android.annotation.SuppressLint;
import android.app.Activity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import es.unizar.unoforall.utils.tasks.CancellableRunnable;
import es.unizar.unoforall.utils.tasks.Task;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class WebSocketAPI {
    private static String SERVER_URL = "ws://192.168.1.100/unoforall";
    private static final int CLIENT_HEARTBEAT_MS = 10000;
    private static final int SERVER_HEARTBEAT_MS = 10000;
    private static final int CONNECTION_TIMEOUT = 3000;
    private static final int CONNECTION_CHECK_TIME = 100;

    public static final int GLOBAL_ERROR = 0;
    public static final int SUBSCRIPTION_ERROR = 1;

    private final Map<String, Disposable> suscripciones;
    private final CompositeDisposable compositeDisposable;
    private StompClient client;
    private boolean closed;

    private BiConsumer<Throwable, Integer> onError;
    public void setOnError(BiConsumer<Throwable, Integer> onError){
        this.onError = onError;
    }

    public static void setServerIP(String serverIP){
        WebSocketAPI.SERVER_URL = "ws://" + serverIP + "/unoforall";
    }

    public WebSocketAPI(){
        this.suscripciones = new HashMap<>();
        this.compositeDisposable = new CompositeDisposable();
        this.client = null;
        this.closed = false;
        this.onError = (t, i) -> {
            t.printStackTrace();
            close();
        };
    }

    private CancellableRunnable runnable1, runnable2;
    public void openConnection(Activity activity, Runnable onConnectionRunnable){
        client = Stomp.over(Stomp.ConnectionProvider.JWS, SERVER_URL);
        client.withClientHeartbeat(CLIENT_HEARTBEAT_MS);
        client.withServerHeartbeat(SERVER_HEARTBEAT_MS);
        client.connect();

        // Comprobar si se ha conectado
        runnable1 = new CancellableRunnable() {
            @Override
            public void run() {
                if(client.isConnected()){
                    cancel();
                    runnable2.cancel();

                    activity.runOnUiThread(onConnectionRunnable);
                }
            }
        };

        // Comprobar si se ha agotado el timeout
        runnable2 = new CancellableRunnable() {
            @Override
            public void run() {
                if(!client.isConnected()){
                    cancel();
                    runnable1.cancel();

                    Throwable t = new TimeoutException("Tiempo de espera para conexión de WebSocket agotado");
                    onError.accept(t, GLOBAL_ERROR);
                }
            }
        };

        Task.runPeriodicTask(runnable1, 0, CONNECTION_CHECK_TIME);
        Task.runDelayedTask(runnable2, CONNECTION_TIMEOUT);
    }

    public <T> void subscribe(Activity activity, String topic, Class<T> expectedClass, Consumer<T> consumer){
        if(client == null){
            try{
                throw new IOException("No has abierto la conexión del API WebSocket");
            }catch(Exception ex){
                onError.accept(ex, GLOBAL_ERROR);
                return;
            }
        }

        if(!client.isConnected()){
            try{
                throw new IOException("No se ha podido conectar con el servidor de WebSocket");
            }catch(Exception ex){
                onError.accept(ex, GLOBAL_ERROR);
                return;
            }
        }

        Disposable suscripcion = client.topic(topic).subscribe(topicMessage -> {
            T t = Serializar.deserializar(topicMessage.getPayload(), expectedClass);
            activity.runOnUiThread(() -> consumer.accept(t));
        }, t -> onError.accept(t, SUBSCRIPTION_ERROR));
        suscripciones.put(topic, suscripcion);
        compositeDisposable.add(suscripcion);
    }

    public void unsubscribe(String topic){
        Disposable suscripcion = suscripciones.remove(topic);
        if(suscripcion != null){
            compositeDisposable.remove(suscripcion);
        }
    }

    @SuppressLint("CheckResult")
    public <T> void sendObject(String seccion, T object){
        if(!client.isConnected()){
            try{
                throw new IOException("No se ha podido conectar con el servidor de WebSocket");
            }catch(Exception ex){
                onError.accept(ex, GLOBAL_ERROR);
                return;
            }
        }

        client.send(seccion, Serializar.serializar(object))
                .subscribe(() -> {}, t -> onError.accept(t, GLOBAL_ERROR));
    }

    public synchronized void close(){
        if(closed){
           return;
        }

        client.disconnect();
        compositeDisposable.dispose();
        closed = true;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
