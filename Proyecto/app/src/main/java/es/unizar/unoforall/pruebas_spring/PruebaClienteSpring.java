package es.unizar.unoforall.pruebas_spring;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import es.unizar.unoforall.api.RestAPI;
import es.unizar.unoforall.api.WebSocketAPI;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class PruebaClienteSpring {
    private static class Empleado {
        public String nombre;
        public String apellido;
        public int sueldo;

        public Empleado() {}

        public Empleado(String n, String a, int s) {
            nombre = n;
            apellido = a;
            sueldo = s;
        }

        @Override
        public String toString(){
            return nombre + " " + apellido + " " + sueldo;
        }
    }

    public static void test(){
        //Para permitir tráfico HTTP sin cifrar
        //https://stackoverflow.com/questions/45940861/android-8-cleartext-http-traffic-not-permitted
        //Rest API en Android
        //https://code.tutsplus.com/es/tutorials/android-from-scratch-using-rest-apis--cms-27117
        //GSON para obtener objetos codificados en JSON
        //https://stackoverflow.com/questions/20057695/parsing-json-array-using-gson
        // Para que funcione, es necesario añadir las siguientes dependencias en el build.gradle:
        //          implementation 'com.google.code.gson:gson:2.8.9'

        //Tutorial WebSockets Android con StompClient
        //https://github.com/NaikSoftware/StompProtocolAndroid
        //Código del cliente de ejemplo
        //https://github.com/NaikSoftware/StompProtocolAndroid/blob/master/example-client/src/main/java/ua/naiksoftware/stompclientexample/MainActivity.java
        // Para que funcione, es necesario añadir las siguientes dependencias en el build.gradle:
        //          implementation 'com.github.NaikSoftware:StompProtocolAndroid:1.6.6'
        //          implementation 'io.reactivex.rxjava2:rxjava:2.2.21'
        //          implementation 'io.reactivex.rxjava2:rxandroid:2.1.0'
        //
        // Además, hay que añadir los siguientes repositorios en el settings.gradle si no estaban ya:
        //          jcenter()
        //          maven { url "https://jitpack.io" }

        // Las versiones 2 son versiones mejoradas con respecto a las originales

        //probarRestAPI();
        probarRestAPI2();
        //probarWebSockets();
        probarWebSockets2();
    }

    private static void probarRestAPI2(){
        AsyncTask.execute(() -> {
            RestAPI api = new RestAPI("/api/empleados", false);
            try{
                api.openConnection();
                Empleado[] empleados = api.receiveObject(Empleado[].class);
                for(Empleado empleado : empleados){
                    System.out.println(empleado);
                }
                api.close();
            }catch(IOException ex){
                Log.e("REST_API_ERROR_BEGIN", "");
                ex.printStackTrace();
                Log.e("REST_API_ERROR_END", "");
            }
        });
    }

    private static void probarRestAPI(){
        AsyncTask.execute(() -> {
            try{
                // Create URL
                URL githubEndpoint = new URL("http://192.168.1.100/api/empleados");

                // Create connection
                HttpURLConnection conexion = (HttpURLConnection) githubEndpoint.openConnection();
                conexion.setDoOutput(true);
                conexion.setChunkedStreamingMode(128);

                if (conexion.getResponseCode() == 200) {
                    // Success
                    // Further processing here
                    InputStream responseBody = conexion.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                    Gson gson = new Gson();
                    Empleado[] empleados = gson.fromJson(responseBodyReader, Empleado[].class);
                    for(Empleado e : empleados){
                        System.out.println(e);
                    }
                } else {
                    // Error handling code goes here
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }

        });
    }


    private static void probarWebSockets2(){
        WebSocketAPI api = new WebSocketAPI();
        api.setOnError((t, errorType) -> {
            System.err.println("Se ha producido un error: " + errorType);
            t.printStackTrace();
        });
        api.openConnection();
        api.subscribe("/topic/greetings", Empleado.class, e -> {
            System.out.println(e);
            api.close();
        });
        api.sendObject("/app/hello", new Empleado("a", "b", 555));
    }
    private static void probarWebSockets(){
        StompClient client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://192.168.1.100/gs-guide-websocket");
        client.connect();

        client.topic("/topic/greetings").subscribe(topicMessage -> {
            Log.d("TAG", topicMessage.getPayload());
            Empleado empleado = new Gson().fromJson(topicMessage.getPayload(), Empleado.class);
            System.out.println(empleado.nombre + " " + empleado.apellido + " " + empleado.sueldo);
            client.disconnect();
        });

        Gson gson = new Gson();
        client.send("/app/hello", gson.toJson(new Empleado("a", "b", 555), Empleado.class)).subscribe();
    }
}
