package es.unizar.unoforall.api;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import es.unizar.unoforall.InicioActivity;
import es.unizar.unoforall.PrincipalActivity;
import es.unizar.unoforall.SalaActivity;
import es.unizar.unoforall.database.UsuarioDbAdapter;
import es.unizar.unoforall.model.ListaUsuarios;
import es.unizar.unoforall.model.RespuestaLogin;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.RespuestaSala;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.model.salas.RespuestaSalas;
import es.unizar.unoforall.utils.dialogs.CodeConfirmDialogBuilder;
import es.unizar.unoforall.utils.HashUtils;
import es.unizar.unoforall.utils.dialogs.DeleteAccountDialogBuilder;
import es.unizar.unoforall.utils.dialogs.ModifyAccountDialogBuilder;
import es.unizar.unoforall.utils.dialogs.ResetPasswordDialogBuilder;
import es.unizar.unoforall.utils.dialogs.SalaIDSearchDialogBuilder;

public class BackendAPI{
    private static final String VACIO = "VACIO";

    private static String sesionID = null;
    private static WebSocketAPI wsAPI = null;

    private final Activity activity;
    private final UsuarioDbAdapter usuarioDbAdapter;

    public BackendAPI(Activity activity){
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

                wsAPI = new WebSocketAPI();
                wsAPI.setOnError((t, i) -> {
                    t.printStackTrace();
                    closeWebSocketAPI();
                });
                wsAPI.openConnection(activity, () -> loginPaso2(respuestaLogin.getClaveInicio()));
            }else{
                mostrarMensaje(respuestaLogin.getErrorInfo());
            }
        });
    }
    private void loginPaso2(UUID claveInicio){
        wsAPI.subscribe(activity, "/topic/conectarse/" + claveInicio, String.class, sesionID -> {
            wsAPI.unsubscribe("/topic/conectarse/" + claveInicio);
            BackendAPI.sesionID = sesionID;

            obtenerUsuarioVO(usuarioVO -> {
                mostrarMensaje("Hola " + usuarioVO.getNombre() + ", has iniciado sesión correctamente");

                // Iniciar la actividad principal
                Intent intent = new Intent(activity, PrincipalActivity.class);
                activity.startActivity(intent);
            });
        });
        wsAPI.sendObject("/app/conectarse/" + claveInicio, VACIO);
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

    public void obtenerUsuarioVO(Consumer<UsuarioVO> consumer){
        RestAPI api = new RestAPI(activity, "/api/sacarUsuarioVO");
        api.addParameter("sesionID", sesionID);
        api.openConnection();
        api.setOnObjectReceived(UsuarioVO.class, usuarioVO -> {
            if(usuarioVO.isExito()){
                consumer.accept(usuarioVO);
            }else{
                mostrarMensaje("Se ha producido un error al obtener el usuario");
            }
        });
    }

    public void crearSala(ConfigSala configSala){
        RestAPI api = new RestAPI(activity, "/api/crearSala");
        api.addParameter("sesionID", sesionID);
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

    public void unirseSalaPorID(){
        SalaIDSearchDialogBuilder builder = new SalaIDSearchDialogBuilder(activity);
        builder.setPositiveButton(salaID -> iniciarUnirseSala(salaID));
        builder.setNegativeButton(() -> mostrarMensaje("Búsqueda de sala por ID cancelada"));
        builder.show();
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
                mostrarMensaje(sala.getError());
                wsAPI.unsubscribe("/topic/salas/" + salaID);
                activity.finish();
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

    public void obtenerSalasFiltro(ConfigSala filtro, Consumer<RespuestaSalas> consumer) {
        RestAPI api = new RestAPI(activity, "/api/filtrarSalas");
        api.addParameter("sesionID", sesionID);
        api.addParameter("configuracion", filtro);
        api.openConnection();
        api.setOnObjectReceived(RespuestaSalas.class, consumer);
    }

    public void modificarCuenta(){
        RestAPI api = new RestAPI(activity, "/api/sacarUsuarioVO");
        api.addParameter("sesionID", sesionID);
        api.openConnection();
        api.setOnObjectReceived(UsuarioVO.class, usuarioVO -> {
            ModifyAccountDialogBuilder builder = new ModifyAccountDialogBuilder(activity);
            builder.setNombreUsuario(usuarioVO.getNombre());
            builder.setCorreo(usuarioVO.getCorreo());
            builder.setPositiveButton((nombreUsuario, correo, contrasenna) -> {
                if(contrasenna == null){
                    // Si no se ha cambiado la contraseña,
                    //   se envía la anterior
                    modificarCuentaPaso2(nombreUsuario, correo, usuarioVO.getContrasenna(), builder);
                }else{
                    modificarCuentaPaso2(nombreUsuario, correo, HashUtils.cifrarContrasenna(contrasenna), builder);
                }
            });
            builder.setNegativeButton(() -> mostrarMensaje("Operación cancelada"));
            builder.show();
        });
    }
    private void modificarCuentaPaso2(String nombreUsuario, String correo, String contrasennaHash,
                                      ModifyAccountDialogBuilder builder){
        RestAPI api = new RestAPI(activity, "/api/actualizarCuentaStepOne");
        api.addParameter("sesionID", sesionID);
        api.addParameter("correoNuevo", correo);
        api.addParameter("nombre", nombreUsuario);
        api.addParameter("contrasenna", contrasennaHash);
        api.openConnection();
        api.setOnObjectReceived(String.class, error -> {
            if(error == null){
                // Si no ha habido error
                CodeConfirmDialogBuilder builder2 = new CodeConfirmDialogBuilder(activity);
                builder2.setPositiveButton(codigo -> modificarCuentaPaso3(codigo, correo, contrasennaHash, builder2));
                builder2.setNegativeButton(() -> cancelModificarCuenta());
                builder2.show();
            }else{
                mostrarMensaje(error);
                builder.show();
            }
        });
    }
    private void modificarCuentaPaso3(int codigo,
                                      String correo, String contrasennaHash,
                                      CodeConfirmDialogBuilder builder){
        RestAPI api = new RestAPI(activity, "/api/actualizarCuentaStepTwo");
        api.addParameter("sesionID", sesionID);
        api.addParameter("codigo", codigo);
        api.openConnection();
        api.setOnObjectReceived(String.class, error -> {
            if(error == null){
                // Cerrar sesión y volverla a iniciar
                activity.finish();
                login(correo, contrasennaHash);
            }else{
                mostrarMensaje(error);
                if(!error.equals("SESION_EXPIRADA")){
                    builder.setError("Código incorrecto");
                    builder.show();
                }
            }
        });
    }
    private void cancelModificarCuenta(){
        RestAPI api = new RestAPI(activity, "/api/actualizarCancel");
        api.addParameter("sesionID", sesionID);
        api.openConnection();
        api.setOnObjectReceived(String.class, error -> mostrarMensaje("Operación cancelada"));
    }

    public void borrarCuenta(){
        obtenerUsuarioVO(usuarioVO -> {
            DeleteAccountDialogBuilder builder = new DeleteAccountDialogBuilder(activity);
            builder.setPositiveRunnable(() -> {
                RestAPI api = new RestAPI(activity, "/api/borrarCuenta");
                api.addParameter("sesionID", sesionID);
                api.openConnection();
                api.setOnObjectReceived(String.class, error -> {
                    if(error == null || error.equals("BORRADA")){
                        // No ha habido error
                        usuarioDbAdapter.deleteUsuario(usuarioVO.getCorreo());
                        mostrarMensaje("Cuenta borrada");
                        BackendAPI.closeWebSocketAPI();
                        Intent intent = new Intent(activity, InicioActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        activity.startActivity(intent);
                    }else{
                        mostrarMensaje(error);
                    }
                });
            });
            builder.setNegativeButton(() -> mostrarMensaje("Borrado cancelado"));
            builder.show();
        });
    }

    public void obtenerAmigos(Consumer<ListaUsuarios> consumer){
        RestAPI api = new RestAPI(activity, "/api/sacarAmigos");
        api.addParameter("sesionID", sesionID);
        api.openConnection();
        api.setOnObjectReceived(ListaUsuarios.class, listaUsuarios -> {
            if(listaUsuarios.isExpirado()){
                mostrarMensaje(listaUsuarios.getError());
            }else{
                consumer.accept(listaUsuarios);
            }
        });
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
