package es.unizar.unoforall.api;

import android.util.Log;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Serializar {
    private static final boolean DEBUG = true;

    public static <T> String serializar(T dato){
        String mensaje;
        if(dato instanceof String){
            mensaje = (String) dato;
        }else{
            mensaje = new Gson().toJson(dato);
        }

        if(DEBUG){
            Log.i("Mensaje enviado", mensaje);
        }

        return mensaje;
    }

    public static <T> T deserializar(String mensaje, Class<T> expectedClass){
        if(DEBUG){
            Log.i("Mensaje recibido", mensaje);
        }

        if(mensaje.equals("null") || mensaje.equals("nulo")){
            return expectedClass.cast(null);
        }

        if(expectedClass.equals(String.class)){
            return expectedClass.cast(mensaje);
        }else{
            return new Gson().fromJson(mensaje, expectedClass);
        }
    }

    public static <T> T deserializar(InputStream inputStream, Class<T> expectedClass) throws IOException {
        StringBuilder mensajeBuilder = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesReaded;
        while((bytesReaded = inputStream.read(buffer)) > 0){
            mensajeBuilder.append(new String(buffer, 0, bytesReaded));
        }

        String mensaje = mensajeBuilder.toString();
        return deserializar(mensaje, expectedClass);
    }
}
