package es.unizar.unoforall.api;

import android.app.Activity;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class RestAPI{
    private static final String SERVER_IP = "http://192.168.1.100";
    private static final int HTTP_OK = 200;
    private static final int CONNECTION_TIMEOUT_MS = 3000;

    private final Activity activity;
    private final Map<String, String> parameters;
    private String fullIP;
    private HttpURLConnection conexion;
    private boolean closed;
    private Consumer<Exception> onError = ex -> {ex.printStackTrace(); close();};

    public RestAPI(Activity activity, String seccion){
        this.activity = activity;
        parameters = new HashMap<>();
        fullIP = SERVER_IP + seccion;
        conexion = null;
        closed = false;
    }

    public void setOnError(Consumer<Exception> onError){
        this.onError = onError;
    }

    public <T> void addParameter(String key, T value){
        if(value instanceof String){
            parameters.put(key, (String) value);
        }else{
            parameters.put(key, Serializar.serializar(value));
        }
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
                conexion.setRequestMethod( "POST" );
                conexion.setConnectTimeout(CONNECTION_TIMEOUT_MS);
                conexion.setDoOutput(true);

                conexion.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                OutputStream output = conexion.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
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

    public void close(){
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
