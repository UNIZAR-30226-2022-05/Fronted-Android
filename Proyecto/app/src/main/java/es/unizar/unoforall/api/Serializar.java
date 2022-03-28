package es.unizar.unoforall.api;

import android.util.Log;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Serializar {
    private static final boolean DEBUG = false;

    public static <T> String serializar(T dato){
        return new Gson().toJson(dato);
    }

    public static <T> T deserializar(String mensaje, Class<T> expectedClass){
        if(DEBUG){
            Log.d("Mensaje recibido", mensaje);
        }
        return new Gson().fromJson(mensaje, expectedClass);
    }

    public static <T> T deserializar(InputStream inputStream, Class<T> expectedClass) throws IOException {
        InputStreamReader responseReader;
        if(DEBUG){
            StringBuilder message = new StringBuilder();
            byte[] buffer = new byte[1024];
            int bytesReaded;
            while((bytesReaded = inputStream.read(buffer)) > 0){
                message.append(new String(buffer, 0, bytesReaded));
            }

            Log.d("Mensaje recibido", message.toString());
            ByteArrayInputStream bais = new ByteArrayInputStream(message.toString().getBytes(StandardCharsets.UTF_8));
            responseReader = new InputStreamReader(bais);
        }else{
            responseReader = new InputStreamReader(inputStream);
        }

        return new Gson().fromJson(responseReader, expectedClass);
    }
}
