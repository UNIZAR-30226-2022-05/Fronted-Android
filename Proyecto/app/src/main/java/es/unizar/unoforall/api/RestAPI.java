package es.unizar.unoforall.api;

import java.util.function.Consumer;

import es.unizar.unoforall.utils.CustomActivity;
import me.i2000c.web_utils.client.RestClient;

public class RestAPI{
    protected static String SERVER_URL = "http://192.168.1.100";

    private final CustomActivity activity;
    private RestClient restClient;

    public static void setServerIP(String serverIP){
        RestAPI.SERVER_URL = "http://" + serverIP;
    }

    public RestAPI(CustomActivity activity){
        this.restClient = new RestClient(SERVER_URL);
        this.activity = activity;

        restClient.setOnError(ex -> {
            ex.printStackTrace();
            close();
            activity.runOnUiThread(() ->
                    activity.mostrarMensaje("RestAPI: Se ha producido un error de conexión"));
        });
    }

    public void setOnError(Consumer<Exception> onError){
        restClient.setOnError(onError);
    }

    public <T> void addParameter(String key, T value){
        restClient.addParameter(key, value);
    }

    public void openConnection(String path){
        restClient.openConnection(path);
    }

    public <T> void receiveObject(Class<T> requestedClass, Consumer<T> consumer){
        restClient.receiveObject(requestedClass, object -> {
            activity.runOnUiThread(() -> consumer.accept(object));
        });
    }

    public void close(){
        restClient.close();
    }
}
