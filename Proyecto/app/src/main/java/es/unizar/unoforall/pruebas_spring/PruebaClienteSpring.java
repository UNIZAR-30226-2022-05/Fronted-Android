package es.unizar.unoforall.pruebas_spring;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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

        probarRestAPI();
        probarWebSockets();
    }

    private static void probarRestAPI(){
        AsyncTask.execute(() -> {
            try{
                // Create URL
                URL githubEndpoint = new URL("http://192.168.1.100/api/empleados");

                // Create connection
                HttpURLConnection myConnection =
                        (HttpURLConnection) githubEndpoint.openConnection();
                if (myConnection.getResponseCode() == 200) {
                    // Success
                    // Further processing here
                    InputStream responseBody = myConnection.getInputStream();
                    InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                    Gson gson = new Gson();
                    Empleado[] empleados = gson.fromJson(responseBodyReader, Empleado[].class);
                    for(Empleado e : empleados){
                        System.out.println(e.nombre + " " + e.apellido + " " + e.sueldo);
                    }
                } else {
                    // Error handling code goes here
                }
            }catch(Exception ex){
                ex.printStackTrace();
            }

        });
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



        //Mirar esto:
        //https://stackoverflow.com/questions/24346068/set-up-a-stomp-client-in-android-with-spring-framework-in-server-side/37224168#37224168
        //https://stackoverflow.com/questions/42367533/stomp-client-with-spring-server

        //Menos útil
        //https://www.pubnub.com/blog/java-websocket-programming-with-android-and-spring-boot/
        //https://www.google.com/search?q=spring+websockets+on+android&rlz=1C1CHWL_esES900ES900&ei=6hE1Yp7_Ns6dlwTk246YBw&ved=0ahUKEwienJrF5ND2AhXOzoUKHeStA3MQ4dUDCA4&uact=5&oq=spring+websockets+on+android&gs_lcp=Cgdnd3Mtd2l6EAM6BggAEAcQHjoECAAQHjoGCAAQCBAeOgYIABAFEB46CAgAEAcQBRAeOggIABAHEB4QEzoKCAAQBxAFEB4QEzoICAAQCBAeEBM6BggAEA0QHjoKCAAQCBANEAoQHkoECEEYAEoECEYYAFAAWO8eYP4faABwAXgAgAG2AYgB-Q6SAQQxLjEzmAEAoAEBwAEB&sclient=gws-wiz
    }
}
