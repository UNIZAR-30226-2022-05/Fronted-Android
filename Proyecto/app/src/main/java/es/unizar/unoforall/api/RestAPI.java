package es.unizar.unoforall.api;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class RestAPI {
    private static final String SERVER_IP = "http://192.168.1.100";
    private static final int HTTP_OK = 200;
    private static final int CONNECTION_TIMEOUT_MS = 3000;

    private String fullIP;
    private boolean isPOST;
    private HttpURLConnection conexion;
    private boolean closed;

    private Gson gson = null;
    private Gson getGson(){
        if(gson == null){
            gson = new Gson();
        }
        return gson;
    }

    public RestAPI(String seccion, boolean isPOST){
        fullIP = SERVER_IP + seccion;
        this.isPOST = isPOST;
        conexion = null;
        closed = false;
    }

    public void openConnection() throws IOException {
        URL url = new URL(fullIP);
        conexion = (HttpURLConnection) url.openConnection();
        conexion.setConnectTimeout(CONNECTION_TIMEOUT_MS);
        if(isPOST){
            conexion.setDoOutput(true);
        }
        conexion.setChunkedStreamingMode(128);

        if(conexion.getResponseCode() != HTTP_OK){
            conexion.disconnect();
            throw new IOException(String.format("Obtained response code %d while connecting to %s",
                    conexion.getResponseCode(),
                    fullIP));
        }
    }

    public <T> T receiveObject(Class<T> requestedClass) throws IOException {
        InputStream responseBody = conexion.getInputStream();
        InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
        return getGson().fromJson(responseBodyReader, requestedClass);
    }

    public <T> void sendObject(T object) throws IOException {
        OutputStream output = conexion.getOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
        writer.write(getGson().toJson(object));
    }

    public void close(){
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

        gson = null;
        closed = true;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }
}
