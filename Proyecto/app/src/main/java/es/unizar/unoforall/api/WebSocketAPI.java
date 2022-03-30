package es.unizar.unoforall.api;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompMessage;

public class WebSocketAPI {
    private static final String SERVER_IP = "ws://192.168.1.100/unoforall";
    private static final int CLIENT_HEARTBEAT_MS = 1000;
    private static final int SERVER_HEARTBEAT_MS = 1000;

    public static final int GLOBAL_ERROR = 0;
    public static final int SUBSCRIPTION_ERROR = 1;

    private final Activity activity;
    private final Map<String, Disposable> suscripciones;
    private final CompositeDisposable compositeDisposable;
    private StompClient client;
    private boolean closed;

    private BiConsumer<Throwable, Integer> onError;
    public void setOnError(BiConsumer<Throwable, Integer> onError){
        this.onError = onError;
    }

    public WebSocketAPI(Activity activity){
        this.activity = activity;
        this.suscripciones = new HashMap<>();
        this.compositeDisposable = new CompositeDisposable();
        this.client = null;
        this.closed = false;
        this.onError = (t, i) -> {
            t.printStackTrace();
            close();
            Toast.makeText(activity, "RestAPI: Se ha producido un error de conexión", Toast.LENGTH_LONG).show();
        };
    }

    public void openConnection(){
        client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, SERVER_IP);
        client.withClientHeartbeat(CLIENT_HEARTBEAT_MS).withClientHeartbeat(SERVER_HEARTBEAT_MS);
        client.connect();
    }

    public void subscribe(String topic, Consumer<? super StompMessage> consumer){
        Disposable suscripcion = client.topic(topic).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(consumer);
        suscripciones.put(topic, suscripcion);
        compositeDisposable.add(suscripcion);
    }
    public <T> void subscribe(String topic, Class<T> expectedClass, Consumer<T> consumer){
        Disposable suscripcion = client.topic(topic).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(topicMessage -> {
            T t = Serializar.deserializar(topicMessage.getPayload(), expectedClass);
            consumer.accept(t);
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
