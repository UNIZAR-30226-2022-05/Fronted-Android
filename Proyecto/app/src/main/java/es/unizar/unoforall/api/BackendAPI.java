package es.unizar.unoforall.api;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;
import java.util.function.Consumer;

import es.unizar.unoforall.PantallaPrincipalActivity;
import es.unizar.unoforall.SalaActivity;
import es.unizar.unoforall.database.UsuarioDbAdapter;
import es.unizar.unoforall.model.RespuestaLogin;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.RespuestaSala;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.model.salas.RespuestaSalas;
import es.unizar.unoforall.utils.CodeConfirmDialogBuilder;
import es.unizar.unoforall.utils.HashUtils;
import es.unizar.unoforall.utils.ResetPasswordDialogBuilder;

public class BackendAPI{
    private static final String VACIO = "VACIO";

    private static WebSocketAPI wsAPI = null;

    private final Activity activity;
    private final UsuarioDbAdapter usuarioDbAdapter;

    public BackendAPI(Activity activity){
        if(wsAPI == null){
            wsAPI = new WebSocketAPI();
            wsAPI.setOnError((t, i) -> {
                wsAPI.getOnError().accept(t, i);
                wsAPI = null;
            });
            wsAPI.openConnection();
        }

        this.activity = activity;
        this.usuarioDbAdapter = new UsuarioDbAdapter(this.activity).open();
    }

    private void mostrarMensaje(String mensaje){
        Toast.makeText(activity, mensaje, Toast.LENGTH_SHORT).show();
    }

    public void login(String correo, String contrasennaHash){
        RestAPI api = new RestAPI(activity,"/api/login");
        api.addParameter("correo", correo);
        api.addParameter("contrasenna", contrasennaHash);
        api.openConnection();
        api.setOnObjectReceived(RespuestaLogin.class, respuestaLogin -> {
            if(respuestaLogin.isExito()){
                Cursor cursor = usuarioDbAdapter.buscarUsuario(correo);
                if(cursor == null){
                    usuarioDbAdapter.createUsuario(correo, contrasennaHash);
                }else{
                    long usuarioID = cursor.getLong(0);
                    usuarioDbAdapter.modificarUsuario(usuarioID, correo, contrasennaHash);
                }

                Intent intent = new Intent(activity, PantallaPrincipalActivity.class);
                intent.putExtra(PantallaPrincipalActivity.KEY_CLAVE_INICIO, respuestaLogin.getClaveInicio());
                activity.startActivity(intent);
            }else{
                mostrarMensaje(respuestaLogin.getErrorInfo());
            }
        });
    }
    public void loginPaso2(UUID claveInicio){
        wsAPI.subscribe(activity, "/topic/conectarse/" + claveInicio, UUID.class, sesionID -> {
            wsAPI.unsubscribe("/topic/conectarse/" + claveInicio);
            PantallaPrincipalActivity.setSesionID(sesionID);
            obtenerUsuarioVO(sesionID, usuarioVO -> {
                PantallaPrincipalActivity.setUsuario(usuarioVO);
                Toast.makeText(activity, "Hola " + usuarioVO.getNombre() + ", has iniciado sesión correctamente", Toast.LENGTH_SHORT).show();
            });
        });
        wsAPI.sendObject("/app/conectarse/" + claveInicio, "VACIO");
    }

    public void register(String nombreUsuario, String correo, String contrasennaHash){
        RestAPI api = new RestAPI(activity, "/api/registerStepOne");
        api.addParameter("correo", correo);
        api.addParameter("contrasenna", contrasennaHash);
        api.addParameter("nombre", nombreUsuario);
        api.openConnection();
        api.setOnObjectReceived(String.class, resp -> {
            if(resp == null){
                //Si no ha habido error
                CodeConfirmDialogBuilder builder = new CodeConfirmDialogBuilder(activity);
                builder.setPositiveButton(codigo -> registerPaso2(correo, codigo, contrasennaHash, builder));
                builder.setNegativeButton(() -> registerCancel(correo));
                builder.show();
            }else{
                mostrarMensaje(resp);
            }
        });
    }
    private void registerPaso2(String correo, int codigo, String contrasennaHash, CodeConfirmDialogBuilder builder){
        RestAPI api = new RestAPI(activity, "/api/registerStepTwo");
        api.addParameter("correo", correo);
        api.addParameter("codigo", codigo);
        api.openConnection();
        api.setOnObjectReceived(String.class, error -> {
            if(error == null){
                //Si no ha habido error, hacer login
                login(correo, contrasennaHash);
            }else if(error.equals("Código incorrecto")){
                builder.setError(error);
                builder.show();
            }else{
                mostrarMensaje(error);
            }
        });
    }
    private void registerCancel(String correo){
        RestAPI api = new RestAPI(activity, "/api/registerCancel");
        api.addParameter("correo", correo);
        api.openConnection();
        api.setOnObjectReceived(Boolean.class, exito -> mostrarMensaje("Registro cancelado"));
    }

    public void restablecerContrasenna(String correo){
        RestAPI api = new RestAPI(activity,"/api/reestablecerContrasennaStepOne");
        api.addParameter("correo", correo);
        api.openConnection();

        api.setOnObjectReceived(String.class, resp -> {
            if(resp == null){
                //Si no ha habido error
                CodeConfirmDialogBuilder builder = new CodeConfirmDialogBuilder(activity);
                builder.setPositiveButton(codigo -> restablecerContrasennaPaso2(correo, codigo, builder));
                builder.setNegativeButton(() -> mostrarMensaje("Operación cancelada"));
                builder.show();
            }else{
                mostrarMensaje(resp);
            }
        });
    }
    private void restablecerContrasennaPaso2(String correo, int codigo, CodeConfirmDialogBuilder builder){
        RestAPI api = new RestAPI(activity, "/api/reestablecerContrasennaStepTwo");
        api.addParameter("correo", correo);
        api.addParameter("codigo", codigo);
        api.openConnection();
        api.setOnObjectReceived(String.class, error -> {
            if(error == null){
                // Si no ha habido error
                ResetPasswordDialogBuilder builder2 = new ResetPasswordDialogBuilder(activity);
                builder2.setPositiveButton(password -> restablecerContrasennaPaso3(correo, password));
                builder2.setNegativeButton(() -> mostrarMensaje("Operación cancelada"));
                builder2.show();
            }else{
                mostrarMensaje(error);
                builder.setError("Código incorrecto");
                builder.show();
            }
        });
    }
    private void restablecerContrasennaPaso3(String correo, String contrasenna){
        String contrasennaHash = HashUtils.cifrarContrasenna(contrasenna);
        RestAPI api = new RestAPI(activity, "/api/reestablecerContrasennaStepThree");
        api.addParameter("correo", correo);
        api.addParameter("contrasenna", contrasennaHash);
        api.openConnection();
        api.setOnObjectReceived(String.class, error -> {
            if(error == null){
                //Si no ha habido error
                mostrarMensaje("Contraseña cambiada correctamente");
                login(correo, contrasennaHash);
            }else{
                mostrarMensaje(error);
            }
        });
    }

    public void obtenerUsuarioVO(UUID sesionID, Consumer<UsuarioVO> consumer){
        RestAPI api = new RestAPI(activity, "/api/sacarUsuarioVO");
        api.addParameter("sessionID", sesionID.toString());
        api.openConnection();
        api.setOnObjectReceived(UsuarioVO.class, consumer);
    }

    public void crearSala(UUID sesionID, ConfigSala configSala){
        RestAPI api = new RestAPI(activity, "/api/crearSala");
        api.addParameter("sesionID", sesionID.toString());
        api.addParameter("configuracion", configSala);
        api.openConnection();
        api.setOnObjectReceived(RespuestaSala.class, respuestaSala -> {
            if(respuestaSala.isExito()){
                // No ha habido errores
                iniciarUnirseSala(respuestaSala.getSalaID());
            }else{
                mostrarMensaje(respuestaSala.getErrorInfo());
            }
        });
    }

    public void buscarSala(String sesionID, String salaID, Consumer<Sala> consumer){
        RestAPI api = new RestAPI(activity, "/api/buscarSalaID");
        api.addParameter("sesionID", sesionID);
        api.addParameter("salaID", salaID);
        api.openConnection();
        api.setOnObjectReceived(Sala.class, consumer);
    }

    public void iniciarUnirseSala(UUID salaID){
        Intent intent = new Intent(activity, SalaActivity.class);
        intent.putExtra(SalaActivity.KEY_SALA_ID, salaID);
        activity.startActivityForResult(intent,0);
    }

    public void unirseSala(UUID salaID, Consumer<Sala> consumer){
        wsAPI.subscribe(activity,"/topic/salas/" + salaID, Sala.class, sala -> {
            if(sala.isNoExiste()){
                // Se ha producido un error
                mostrarMensaje("Error al conectarse a la sala");
                wsAPI.unsubscribe("/topic/salas/" + salaID);
            }else{
                consumer.accept(sala);
            }
        });
        wsAPI.sendObject("/app/salas/unirse/" + salaID, VACIO);
        mostrarMensaje("Te has unido a la sala");
    }
    public void listoSala(UUID salaID){
        wsAPI.sendObject("/app/salas/listo/" + salaID, VACIO);
    }
    public void salirSala(UUID salaID){
        wsAPI.unsubscribe("/topic/salas/" + salaID);
        wsAPI.sendObject("/app/salas/salir/" + salaID, VACIO);
        mostrarMensaje("Has salido de la sala");
    }

    public void obtenerSalasInicio(UUID sesionID, Consumer<RespuestaSalas> consumer){
        RestAPI api = new RestAPI(activity, "/api/filtrarSalas");
        api.addParameter("sesionID", sesionID.toString());
        api.addParameter("configuracion", null);
        api.openConnection();
        api.setOnObjectReceived(RespuestaSalas.class, consumer);
    }

    public void obtenerSalasFiltro(UUID sesionID, ConfigSala filtro, Consumer<RespuestaSalas> consumer){
        RestAPI api = new RestAPI(activity, "/api/filtrarSalas");
        api.addParameter("sesionID", sesionID.toString());
        api.addParameter("configuracion", filtro.toString());
        api.setOnObjectReceived(RespuestaSalas.class, consumer);
    }

    public static synchronized void closeWebSocketAPI(){
        if(wsAPI != null){
            wsAPI.close();
            wsAPI = null;
        }
    }
    @Override
    protected void finalize() throws Throwable{
        usuarioDbAdapter.close();
        super.finalize();
    }
}
