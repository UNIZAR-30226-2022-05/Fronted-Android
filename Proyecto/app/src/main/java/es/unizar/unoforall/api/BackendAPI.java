package es.unizar.unoforall.api;

import android.content.Intent;
import android.database.Cursor;

import java.util.UUID;
import java.util.function.Consumer;

import es.unizar.unoforall.InicioActivity;
import es.unizar.unoforall.PrincipalActivity;
import es.unizar.unoforall.SalaActivity;
import es.unizar.unoforall.database.UsuarioDbAdapter;
import es.unizar.unoforall.model.ListaUsuarios;
import es.unizar.unoforall.model.RespuestaLogin;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partidas.EnvioEmoji;
import es.unizar.unoforall.model.partidas.Jugada;
import es.unizar.unoforall.model.partidas.RespuestaVotacionPausa;
import es.unizar.unoforall.model.salas.ConfigSala;
import es.unizar.unoforall.model.salas.NotificacionSala;
import es.unizar.unoforall.model.salas.RespuestaSala;
import es.unizar.unoforall.model.salas.Sala;
import es.unizar.unoforall.model.salas.RespuestaSalas;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.SalaReceiver;
import es.unizar.unoforall.utils.dialogs.CodeConfirmDialogBuilder;
import es.unizar.unoforall.utils.HashUtils;
import es.unizar.unoforall.utils.dialogs.DeleteAccountDialogBuilder;
import es.unizar.unoforall.utils.dialogs.ModifyAccountDialogBuilder;
import es.unizar.unoforall.utils.dialogs.ModifyAspectDialogBuilder;
import es.unizar.unoforall.utils.dialogs.PeticionAmistadDialogBuilder;
import es.unizar.unoforall.utils.dialogs.ResetPasswordDialogBuilder;
import es.unizar.unoforall.utils.dialogs.SalaIDSearchDialogBuilder;
import es.unizar.unoforall.utils.dialogs.SeleccionAmigoDialogBuilder;
import es.unizar.unoforall.utils.notifications.Notificaciones;
import es.unizar.unoforall.utils.tasks.Task;

public class BackendAPI{
    private static final String VACIO = "VACIO";

    private static String sesionID = null;
    private static WebSocketAPI wsAPI = null;

    private static UUID usuarioID = null;
    public static UUID getUsuarioID(){
        return usuarioID;
    }

    private static Sala salaActual = null;
    public static Sala getSalaActual(){
        return salaActual;
    }
    public static void setSalaActual(Sala sala){
        BackendAPI.salaActual = sala;
    }
    private static UUID salaActualID = null;
    public static UUID getSalaActualID(){
        return salaActualID;
    }
    public static void setSalaActualID(UUID salaID){
        BackendAPI.salaActualID = salaID;
    }

    private static CustomActivity currentActivity = null;
    public static void setCurrentActivity(CustomActivity currentActivity){
        BackendAPI.currentActivity = currentActivity;
    }
    public static CustomActivity getCurrentActivity(){
        return currentActivity;
    }

    private final CustomActivity activity;
    private final UsuarioDbAdapter usuarioDbAdapter;

    public BackendAPI(CustomActivity activity){
        this.activity = activity;
        this.usuarioDbAdapter = new UsuarioDbAdapter(this.activity).open();
    }

    //
    //  LOGIN, REGISTRO Y RECUPERACIÓN DE CONTRASEÑA
    //
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
                    activity.mostrarMensaje(t.getMessage());
                    t.printStackTrace();
                    closeWebSocketAPI();
                });
                wsAPI.openConnection(activity, () -> loginPaso2(respuestaLogin.getClaveInicio()));
            }else{
                activity.mostrarMensaje(respuestaLogin.getErrorInfo());
            }
        });
    }
    private void loginPaso2(UUID claveInicio){
        wsAPI.subscribe(activity, "/topic/conectarse/" + claveInicio, String.class, sesionID -> {
            wsAPI.unsubscribe("/topic/conectarse/" + claveInicio);
            BackendAPI.sesionID = sesionID;

            obtenerUsuarioVO(usuarioVO -> {
                // Suscribirse a las notificaciones de sala
                wsAPI.subscribe(activity, "/topic/notifSala/" + usuarioVO.getId(),
                        NotificacionSala.class, notificacionSala ->
                            Notificaciones.mostrarNotificacionSala(notificacionSala));

                // Suscribirse a las notificaciones de amigos
                wsAPI.subscribe(activity, "/topic/notifAmistad/" + usuarioVO.getId(),
                        UsuarioVO.class, usuarioVO2 -> {
                            if(usuarioVO2 != null){
                                Notificaciones.mostrarNotificacionAmigo(usuarioVO2);
                            }
                        });

                usuarioID = usuarioVO.getId();
                activity.mostrarMensaje("Hola " + usuarioVO.getNombre() + ", has iniciado sesión correctamente");

                // Iniciar la actividad principal
                Intent intent = new Intent(activity, PrincipalActivity.class);
                activity.startActivityForResult(intent, 0);
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
                activity.mostrarMensaje(resp);
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
                activity.mostrarMensaje(error);
            }
        });
    }
    private void registerCancel(String correo){
        RestAPI api = new RestAPI(activity, "/api/registerCancel");
        api.addParameter("correo", correo);
        api.openConnection();
        api.setOnObjectReceived(Boolean.class, exito -> activity.mostrarMensaje("Registro cancelado"));
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
                builder.setNegativeButton(() -> activity.mostrarMensaje("Operación cancelada"));
                builder.show();
            }else{
                activity.mostrarMensaje(resp);
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
                builder2.setNegativeButton(() -> activity.mostrarMensaje("Operación cancelada"));
                builder2.show();
            }else{
                activity.mostrarMensaje(error);
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
                activity.mostrarMensaje("Contraseña cambiada correctamente");
                login(correo, contrasennaHash);
            }else{
                activity.mostrarMensaje(error);
            }
        });
    }

    //
    //  OBTENCIÓN DE USUARIOS
    //
    public void obtenerUsuarioVO(Consumer<UsuarioVO> consumer){
        RestAPI api = new RestAPI(activity, "/api/sacarUsuarioVO");
        api.addParameter("sesionID", sesionID);
        api.openConnection();
        api.setOnObjectReceived(UsuarioVO.class, usuarioVO -> {
            if(usuarioVO.isExito()){
                consumer.accept(usuarioVO);
            }else{
                activity.mostrarMensaje("Se ha producido un error al obtener el usuario");
            }
        });
    }
    public void buscarUsuarioVO(String correo, Consumer<UsuarioVO> consumer){
        RestAPI api = new RestAPI(activity, "/api/buscarAmigo");
        api.addParameter("sesionID", sesionID);
        api.addParameter("amigo", correo);
        api.openConnection();
        api.setOnObjectReceived(ListaUsuarios.class, listaUsuarios -> {
            if(listaUsuarios.isExpirado() || (listaUsuarios.getError() != null && !listaUsuarios.getError().equals("null"))){
                activity.mostrarMensaje(listaUsuarios.getError());
            }else{
                consumer.accept(listaUsuarios.getUsuarios().get(0));
            }
        });
    }

    //
    //  GESTIÓN DE SALAS
    //
    public void crearSala(ConfigSala configSala){
        RestAPI api = new RestAPI(activity, "/api/crearSala");
        api.addParameter("sesionID", sesionID);
        api.addParameter("configuracion", configSala);
        api.openConnection();
        api.setOnObjectReceived(RespuestaSala.class, respuestaSala -> {
            if(respuestaSala.isExito()){
                // No ha habido errores
                unirseSala(respuestaSala.getSalaID());
            }else{
                activity.mostrarMensaje(respuestaSala.getErrorInfo());
            }
        });
    }

    public void unirseSalaPorID(){
        SalaIDSearchDialogBuilder builder = new SalaIDSearchDialogBuilder(activity);
        builder.setPositiveButton(salaID -> unirseSala(salaID));
        builder.setNegativeButton(() -> activity.mostrarMensaje("Búsqueda de sala por ID cancelada"));
        builder.show();
    }

    public void unirseSala(UUID salaID){
        wsAPI.subscribe(activity,"/topic/salas/" + salaID, Sala.class, sala -> {
            if(sala.isNoExiste()){
                // Se ha producido un error
                activity.mostrarMensaje(sala.getError());
                wsAPI.unsubscribe("/topic/salas/" + salaID);
                activity.finish();
            }else{
                salaActual = sala;
                if(salaActualID == null){
                    salaActualID = salaID;
                    Intent intent = new Intent(activity, SalaActivity.class);
                    activity.startActivityForResult(intent,0);
                    activity.mostrarMensaje("Te has unido a la sala");
                }

                if(currentActivity instanceof SalaReceiver){
                    ((SalaReceiver) currentActivity).manageSala(sala);
                }
            }
        });
        wsAPI.sendObject("/app/salas/unirse/" + salaID, VACIO);
    }
    public void listoSala(){
        wsAPI.sendObject("/app/salas/listo/" + salaActualID, VACIO);
    }
    public void salirSala(){
        if(salaActual == null){
            activity.mostrarMensaje("La sala no puede ser null");
        }else{
            cancelarSuscripcionCanalEmojis();
            cancelarSuscripcionCanalVotacionPausa();
            wsAPI.unsubscribe("/topic/salas/" + salaActualID);
            wsAPI.sendObject("/app/salas/salir/" + salaActualID, VACIO);
            activity.mostrarMensaje("Has salido de la sala");
            salaActual = null;
            salaActualID = null;
            Intent intent = new Intent(activity, PrincipalActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivityForResult(intent, 0);
        }
    }
    public void salirSalaDefinitivo(){
        if(salaActual == null){
            activity.mostrarMensaje("La sala no puede ser null");
        }else{
            cancelarSuscripcionCanalEmojis();
            cancelarSuscripcionCanalVotacionPausa();
            wsAPI.unsubscribe("/topic/salas/" + salaActualID);
            wsAPI.sendObject("/app/salas/salirDefinitivo/" + salaActualID, VACIO);
            activity.mostrarMensaje("Has salido de la sala");
            salaActual = null;
            salaActualID = null;
            Intent intent = new Intent(activity, PrincipalActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivityForResult(intent, 0);
        }
    }

    public void comprobarPartidaPausada(Consumer<Sala> consumer){
        RestAPI api = new RestAPI(activity, "/api/comprobarPartidaPausada");
        api.addParameter("sesionID", sesionID);
        api.openConnection();
        api.setOnObjectReceived(Sala.class, sala -> {
            if(sala.isNoExiste()){
                consumer.accept(null);
            }else{
                consumer.accept(sala);
            }
        });
    }

    public void obtenerSalasFiltro(ConfigSala filtro, Consumer<RespuestaSalas> consumer) {
        RestAPI api = new RestAPI(activity, "/api/filtrarSalas");
        api.addParameter("sesionID", sesionID);
        api.addParameter("configuracion", filtro);
        api.openConnection();
        api.setOnObjectReceived(RespuestaSalas.class, consumer);
    }

    //
    //  MODIFICACIÓN DE CUENTA
    //
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
            builder.setNegativeButton(() -> activity.mostrarMensaje("Operación cancelada"));
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
                activity.mostrarMensaje(error);
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
                closeWebSocketAPI();
                login(correo, contrasennaHash);
            }else{
                activity.mostrarMensaje(error);
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
        api.setOnObjectReceived(String.class, error -> activity.mostrarMensaje("Operación cancelada"));
    }

    //
    //  BORRADO DE CUENTA
    //
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
                        activity.mostrarMensaje("Cuenta borrada");
                        BackendAPI.closeWebSocketAPI();
                        Intent intent = new Intent(activity, InicioActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        activity.startActivityForResult(intent, 0);
                    }else{
                        activity.mostrarMensaje(error);
                    }
                });
            });
            builder.setNegativeButton(() -> activity.mostrarMensaje("Borrado cancelado"));
            builder.show();
        });
    }

    //
    //  GESTIÓN DE AMIGOS
    //
    public void obtenerAmigos(Consumer<ListaUsuarios> consumer){
        RestAPI api = new RestAPI(activity, "/api/sacarAmigos");
        api.addParameter("sesionID", sesionID);
        api.openConnection();
        api.setOnObjectReceived(ListaUsuarios.class, listaUsuarios -> {
            if(listaUsuarios.isExpirado()){
                activity.mostrarMensaje(listaUsuarios.getError());
            }else{
                consumer.accept(listaUsuarios);
            }
        });
    }
    public void obtenerPeticionesRecibidas(Consumer<ListaUsuarios> consumer){
        RestAPI api = new RestAPI(activity, "/api/sacarPeticionesRecibidas");
        api.addParameter("sesionID", sesionID);
        api.openConnection();
        api.setOnObjectReceived(ListaUsuarios.class, listaUsuarios -> {
            if(listaUsuarios.isExpirado()){
                activity.mostrarMensaje(listaUsuarios.getError());
            }else{
                consumer.accept(listaUsuarios);
            }
        });
    }
    public void obtenerPeticionesEnviadas(Consumer<ListaUsuarios> consumer){
        RestAPI api = new RestAPI(activity, "/api/sacarPeticionesEnviadas");
        api.addParameter("sesionID", sesionID);
        api.openConnection();
        api.setOnObjectReceived(ListaUsuarios.class, listaUsuarios -> {
            if(listaUsuarios.isExpirado()){
                activity.mostrarMensaje(listaUsuarios.getError());
            }else{
                consumer.accept(listaUsuarios);
            }
        });
    }

    public void enviarPeticion(Runnable runnable){
        PeticionAmistadDialogBuilder builder = new PeticionAmistadDialogBuilder(activity);
        builder.setPositiveButton(correo -> {
            obtenerUsuarioVO(usuarioPrincipal -> {
                if(correo.equals(usuarioPrincipal.getCorreo())){
                    builder.setError("No puedes enviarte una solicitud a ti mismo");
                    builder.show();
                }else{
                    enviarPeticion2(correo, runnable);
                }
            });
        });
        builder.setNegativeButton(() -> activity.mostrarMensaje("Envío cancelado"));
        builder.show();
    }
    private void enviarPeticion2(String correo, Runnable runnable){
        buscarUsuarioVO(correo, usuarioVO -> {
            wsAPI.sendObject("/app/notifAmistad/" + usuarioVO.getId(), VACIO);
            Task.runDelayedTask(() -> runnable.run(), 300);
        });
    }

    public void aceptarPeticion(UsuarioVO usuario){
        RestAPI api = new RestAPI(activity, "/api/aceptarPeticionAmistad");
        api.addParameter("sesionID", sesionID);
        api.addParameter("amigo", usuario.getId());
        api.openConnection();
        api.setOnObjectReceived(String.class, error -> {
            if(error != null){
                activity.mostrarMensaje(error);
            }else{
                activity.mostrarMensaje("Petición aceptada");
            }
        });
    }
    public void rechazarPeticion(UsuarioVO usuario){
        RestAPI api = new RestAPI(activity, "/api/cancelarPeticionAmistad");
        api.addParameter("sesionID", sesionID);
        api.addParameter("amigo", usuario.getId());
        api.openConnection();
        api.setOnObjectReceived(String.class, error -> {
            if(error != null){
                activity.mostrarMensaje(error);
            }else{
                activity.mostrarMensaje("Petición rechazada");
            }
        });
    }

    public void invitarAmigoSala(){
        SeleccionAmigoDialogBuilder builder = new SeleccionAmigoDialogBuilder(activity);
        builder.setPositiveButton(correo -> {
            buscarUsuarioVO(correo, usuarioVO -> {
                wsAPI.sendObject("/app/notifSala/" + usuarioVO.getId(), salaActualID);
                activity.mostrarMensaje("Invitación enviada");
            });
        });
        builder.show();
    }

    //
    //  GESTIÓN DE PARTIDAS
    //
    public void enviarJugada(Jugada jugada){
        wsAPI.sendObject("/app/partidas/turnos/" + salaActualID, jugada);
    }

    public void pulsarBotonUNO(){
        wsAPI.sendObject("/app/partidas/botonUNO/" + salaActualID, VACIO);
    }

    public void suscribirseCanalEmojis(Consumer<EnvioEmoji> consumer){
        wsAPI.subscribe(activity, "/topic/salas/" + salaActualID + "/emojis",
                EnvioEmoji.class, consumer);
    }
    public void cancelarSuscripcionCanalEmojis(){
        wsAPI.unsubscribe("/topic/salas/" + salaActualID + "/emojis");
    }
    public void enviarEmoji(int jugadorID, int emojiID){
        EnvioEmoji envioEmoji = new EnvioEmoji(emojiID, jugadorID, false);
        wsAPI.sendObject("/app/partidas/emojiPartida/" + salaActualID, envioEmoji);
    }

    public void suscribirseCanalVotacionPausa(Consumer<RespuestaVotacionPausa> consumer){
        wsAPI.subscribe(activity, "/topic/salas/" + salaActualID + "/votaciones",
                RespuestaVotacionPausa.class, respuestaVotacionPausa -> {
                    if(respuestaVotacionPausa != null){
                        consumer.accept(respuestaVotacionPausa);
                    }
                });
    }
    public void enviarVotacion(){
        wsAPI.sendObject("/app/partidas/votaciones/" + salaActualID, VACIO);
    }
    public void cancelarSuscripcionCanalVotacionPausa(){
        wsAPI.unsubscribe("/topic/salas/" + salaActualID + "/votaciones");
    }

    //
    // PERSONALIZACION DE ASPECTO (avatar, cartas y fondo de pantalla)
    //

    public void cambiarPersonalizacionStepOne(){
        obtenerUsuarioVO(usuarioVO -> {
            ModifyAspectDialogBuilder builder = new ModifyAspectDialogBuilder(activity, usuarioVO);
            builder.setPositiveButton((avatar, aspectoFondo, aspectoCartas) ->
                cambiarPersonalizacionStepTwo(avatar, aspectoFondo, aspectoCartas));
            builder.setNegativeButton(() -> activity.mostrarMensaje("Cambios cancelados"));
            builder.show();
        });
    }

    public void cambiarPersonalizacionStepTwo(int avatar, int aspectoFondo, int aspectoCartas){
        RestAPI api = new RestAPI(activity, "/api/cambiarAvatar");
        api.addParameter("sesionID", sesionID);
        api.addParameter("avatar", avatar);
        api.addParameter("aspectoFondo", aspectoFondo);
        api.addParameter("aspectoCartas", aspectoCartas);
        api.openConnection();
        api.setOnObjectReceived(String.class, error -> {
            if(error == null){
                activity.mostrarMensaje("Los cambios se han realizado correctamente");
            }else{
                activity.mostrarMensaje(error);
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
