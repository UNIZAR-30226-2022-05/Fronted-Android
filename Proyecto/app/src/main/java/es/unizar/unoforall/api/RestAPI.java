package es.unizar.unoforall.api;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import es.unizar.unoforall.utils.CustomActivity;

public class RestAPI{
    private static String SERVER_URL = "http://192.168.1.100";
    private static final int HTTP_OK = 200;
    private static final int CONNECTION_TIMEOUT_MS = 3000;

    private final CustomActivity activity;
    private final Map<String, String> parameters;
    private final String fullIP;
    private HttpURLConnection conexion;
    private boolean closed;
    private Consumer<Exception> onError;

    public static void setServerIP(String serverIP){
        RestAPI.SERVER_URL = "http://" + serverIP;
    }

    public RestAPI(CustomActivity activity, String seccion){
        this.activity = activity;
        this.parameters = new HashMap<>();
        this.fullIP = SERVER_URL + seccion;
        this.conexion = null;
        this.closed = false;

        this.onError = ex -> {
            ex.printStackTrace();
            close();
            activity.runOnUiThread(() ->
                    activity.mostrarMensaje("RestAPI: Se ha producido un error de conexión"));
        };
    }

    public void setOnError(Consumer<Exception> onError){
        this.onError = onError;
    }
    public Consumer<Exception> getOnError(){
        return this.onError;
    }

    public <T> void addParameter(String key, T value){
        parameters.put(key, Serializar.serializar(value));
    }

    private static String getDataString(Map<String, String> params) throws UnsupportedEncodingException{
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    public void openConnection(){
        AsyncTask.execute(() -> {
            if(closed){
                return;
            }
            try{
                String data = getDataString(parameters);

                URL url = new URL(fullIP);
                conexion = (HttpURLConnection) url.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setConnectTimeout(CONNECTION_TIMEOUT_MS);
                conexion.setDoOutput(true);

                conexion.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStream output = conexion.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
                writer.write(data);
                writer.flush();

                if(conexion.getResponseCode() != HTTP_OK){
                    conexion.disconnect();
                    throw new IOException(String.format("Obtained response code %d while connecting to %s",
                            conexion.getResponseCode(),
                            fullIP));
                }
            }catch(Exception ex){
                onError.accept(ex);
            }
        });
    }

    public <T> void setOnObjectReceived(Class<T> requestedClass, Consumer<T> consumer){
        setOnObjectReceived(requestedClass, consumer, true);
    }
    public <T> void setOnObjectReceived(Class<T> requestedClass, Consumer<T> consumer, boolean autoClose){
        AsyncTask.execute(() -> {
            if(closed){
                return;
            }
            try {
                if(conexion == null){
                    throw new IOException("No has abierto la conexión del API rest");
                }

                InputStream responseBody = conexion.getInputStream();
                T dato = Serializar.deserializar(responseBody, requestedClass);
                activity.runOnUiThread(() -> {
                    consumer.accept(dato);
                    if(autoClose){
                        close();
                    }
                });
            }catch(Exception ex){
                onError.accept(ex);
            }
        });
    }

    public synchronized void close(){
        AsyncTask.execute(() -> {
            if(closed){
                return;
            }

            try{
                conexion.getInputStream().close();
            }catch(Exception ex){}
            try{
                conexion.getOutputStream().close();
            }catch(Exception ex){}
            try{
                conexion.disconnect();
            }catch(Exception ex){}

            closed = true;
        });
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
