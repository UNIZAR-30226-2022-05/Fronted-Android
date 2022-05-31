package es.unizar.unoforall.api;

import android.content.Intent;
import android.database.Cursor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import es.unizar.unoforall.InicioActivity;
import es.unizar.unoforall.PrincipalActivity;
import es.unizar.unoforall.SalaActivity;
import es.unizar.unoforall.database.UsuarioDbAdapter;
import es.unizar.unoforall.model.ListaUsuarios;
import es.unizar.unoforall.model.RespuestaLogin;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.partidas.EnvioEmoji;
import es.unizar.unoforall.model.partidas.Jugada;
import es.unizar.unoforall.model.partidas.ListaPartidas;
import es.unizar.unoforall.model.partidas.PartidaJugada;
import es.unizar.unoforall.model.partidas.PartidaJugadaCompacta;
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
import es.unizar.unoforall.utils.notifications.Notificacion;
import es.unizar.unoforall.utils.notifications.Notificaciones;
import es.unizar.unoforall.utils.tasks.Task;
import me.i2000c.web_utils.client.RestClient;
import me.i2000c.web_utils.serialize.Serialize;

public class BackendAPI{
    private static final Object LOCK = new Object();
    private static final String VACIO = "VACIO";

    //private static String sesionID = null;
    private static WebSocketAPI wsAPI = null;

    private static UsuarioVO usuario = null;
    public static UsuarioVO getUsuario(){
        return usuario;
    }
    public static void setUsuario(UsuarioVO usuario){
        BackendAPI.usuario = usuario;
    }

    private static Sala salaActual = null;
    public static Sala getSalaActual(){
        synchronized(LOCK){
            return salaActual;
        }
    }
    public static void setSalaActual(Sala sala){
        synchronized(LOCK){
            BackendAPI.salaActual = sala;
        }
    }
    private static UUID salaActualID = null;
    public static UUID getSalaActualID(){
        return salaActualID;
    }
    public static void setSalaActualID(UUID salaID){
        BackendAPI.salaActualID = salaID;
    }

    private static Set<Notificacion> notificacionesSala = new LinkedHashSet<>();
    public static void addNotificacionSala(Notificacion notificacion){
        synchronized(LOCK){
            notificacionesSala.add(notificacion);
        }
    }
    public static void removeNotificacionSala(Notificacion notificacion){
        synchronized(LOCK){
            notificacionesSala.remove(notificacion);
        }
    }
    public static Set<Notificacion> getNotificacionesSala(){
        synchronized(LOCK){
            return notificacionesSala;
        }
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
        wsAPI = new WebSocketAPI();
        wsAPI.setOnError(ex -> {
            activity.runOnUiThread(() -> {
                activity.mostrarMensaje(ex.getMessage());
            });
            ex.printStackTrace();
            closeWebSocketAPI();
        });
        wsAPI.openConnection("/topic");

        RestClient client = wsAPI.getRestClient();
        client.addParameter("correo", correo);
        client.addParameter("contrasenna", contrasennaHash);
        client.openConnection("/api/login");
        client.receiveObject(RespuestaLogin.class, respuestaLogin -> {
            if(respuestaLogin.isExito()){
                Cursor cursor = usuarioDbAdapter.buscarUsuario(correo);
                if(cursor == null){
                    usuarioDbAdapter.createUsuario(correo, contrasennaHash);
                }else{
                    long usuarioID = cursor.getLong(0);
                    usuarioDbAdapter.modificarUsuario(usuarioID, correo, contrasennaHash);
                }

                loginPaso2();
            }else{
                activity.mostrarMensaje(respuestaLogin.getErrorInfo());
            }
        });
    }
    private void loginPaso2(){
        notificacionesSala.clear();

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

            usuario = usuarioVO;
            activity.mostrarMensaje("Hola " + usuarioVO.getNombre() + ", has iniciado sesión correctamente");

            // Iniciar la actividad principal
            Intent intent = new Intent(activity, PrincipalActivity.class);
            activity.startActivityForResult(intent, 0);
        });
    }

    public void register(String nombreUsuario, String correo, String contrasennaHash){
        RestAPI api = new RestAPI(activity);
        api.addParameter("correo", correo);
        api.addParameter("contrasenna", contrasennaHash);
        api.addParameter("nombre", nombreUsuario);
        api.openConnection("/api/registerStepOne");
        api.receiveObject(String.class, resp -> {
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
        RestAPI api = new RestAPI(activity);
        api.addParameter("correo", correo);
        api.addParameter("codigo", codigo);
        api.openConnection("/api/registerStepTwo");
        api.receiveObject(String.class, error -> {
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
        RestAPI api = new RestAPI(activity);
        api.addParameter("correo", correo);
        api.openConnection("/api/registerCancel");
        api.receiveObject(Boolean.class, exito -> activity.mostrarMensaje("Registro cancelado"));
    }

    public void restablecerContrasenna(String correo){
        RestAPI api = new RestAPI(activity);
        api.addParameter("correo", correo);
        api.openConnection("/api/reestablecerContrasennaStepOne");

        api.receiveObject(String.class, resp -> {
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
        RestAPI api = new RestAPI(activity);
        api.addParameter("correo", correo);
        api.addParameter("codigo", codigo);
        api.openConnection("/api/reestablecerContrasennaStepTwo");
        api.receiveObject(String.class, error -> {
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
        RestAPI api = new RestAPI(activity);
        api.addParameter("correo", correo);
        api.addParameter("contrasenna", contrasennaHash);
        api.openConnection("/api/reestablecerContrasennaStepThree");
        api.receiveObject(String.class, error -> {
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
        RestClient client = wsAPI.getRestClient();
        client.openConnection("/api/sacarUsuarioVO");
        client.receiveObject(UsuarioVO.class, usuarioVO -> {
            activity.runOnUiThread(() -> {
                if(usuarioVO.isExito()){
                    usuario = usuarioVO;
                    consumer.accept(usuarioVO);
                }else{
                    activity.mostrarMensaje("Se ha producido un error al obtener el usuario");
                }
            });
        });
    }
    public void buscarUsuarioVO(String correo, Consumer<UsuarioVO> consumer){
        RestClient client = wsAPI.getRestClient();
        client.addParameter("amigo", correo);
        client.openConnection("/api/buscarAmigo");
        client.receiveObject(ListaUsuarios.class, listaUsuarios -> {
            activity.runOnUiThread(() -> {
                if(listaUsuarios.isExpirado() || (listaUsuarios.getError() != null && !listaUsuarios.getError().equals("null"))){
                    activity.mostrarMensaje(listaUsuarios.getError());
                }else{
                    consumer.accept(listaUsuarios.getUsuarios().get(0));
                }
            });
        });
    }

    //
    //  GESTIÓN DE SALAS
    //
    public void crearSala(ConfigSala configSala){
        RestClient client = wsAPI.getRestClient();
        client.addParameter("configuracion", configSala);
        client.openConnection("/api/crearSala");
        client.receiveObject(RespuestaSala.class, respuestaSala -> {
            activity.runOnUiThread(() -> {
                if(respuestaSala.isExito()){
                    // No ha habido errores
                    unirseSala(respuestaSala.getSalaID());
                }else{
                    activity.mostrarMensaje(respuestaSala.getErrorInfo());
                }
            });
        });
    }

    public void unirseSalaPorID(){
        SalaIDSearchDialogBuilder builder = new SalaIDSearchDialogBuilder(activity);
        builder.setPositiveButton(salaID -> {
            RestAPI api = new RestAPI(activity);
            api.addParameter("salaID", salaID);
            api.openConnection("/api/buscarSalaID");
            api.receiveObject(Sala.class, sala -> {
                activity.runOnUiThread(() -> {
                    if(sala.isNoExiste()){
                        builder.setError(sala.getError());
                        builder.show();
                    }else{
                        unirseSala(salaID);
                    }
                });
            });
        });
        builder.setNegativeButton(() ->
                activity.runOnUiThread(() -> activity.mostrarMensaje("Búsqueda de sala por ID cancelada")));
        builder.show();
    }

    public void unirseSala(UUID salaID){
        unirseSala(salaID, exito -> {});
    }
    public void unirseSala(UUID salaID, Consumer<Boolean> consumer){
        comprobarUnirseSala(salaID, sePuedeUnir -> {
            activity.runOnUiThread(() -> {
                if(sePuedeUnir){
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

                            enviarSalaACK();
                            if(currentActivity instanceof SalaReceiver){
                                ((SalaReceiver) currentActivity).manageSala(sala);
                            }
                        }
                    });

                    RestClient client = wsAPI.getRestClient();
                    client.openConnection("/app/salas/unirse/" + salaID);
                    client.receiveObject(String.class, null);
                    consumer.accept(true);
                }else{
                    activity.mostrarMensaje("No puedes unirte a esa sala ahora mismo");
                    consumer.accept(false);
                }
            });
        });
    }
    private void enviarSalaACK(){
        if(salaActual != null){
            RestClient client = wsAPI.getRestClient();
            client.addParameter("salaID", salaActualID);
            client.openConnection("/api/ack");
            client.receiveObject(Boolean.class, exito -> {
                activity.runOnUiThread(() -> {
                    if(!exito){
                        activity.mostrarMensaje("Se ha producido un error al enviar el ACK");
                    }
                });
            });
        }
    }
    public void listoSala(){
        RestClient client = wsAPI.getRestClient();
        client.openConnection("/app/salas/listo/" + salaActualID);
        client.receiveObject(String.class, null);
    }
    public void salirSala(){
        activity.runOnUiThread(() -> {
            if(salaActual == null){
                activity.mostrarMensaje("La sala no puede ser null");
            }else{
                cancelarSuscripcionCanalEmojis();
                cancelarSuscripcionCanalVotacionPausa();
                wsAPI.unsubscribe("/topic/salas/" + salaActualID);
                RestClient client = wsAPI.getRestClient();
                client.openConnection("/app/salas/salir/" + salaActualID);
                client.receiveObject(String.class, null);
                activity.mostrarMensaje("Has salido de la sala");
                salaActual = null;
                salaActualID = null;
                Intent intent = new Intent(activity, PrincipalActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivityForResult(intent, 0);
            }
        });
    }
    public void salirSalaDefinitivo(){
        activity.runOnUiThread(() -> {
            if(salaActual == null){
                activity.mostrarMensaje("La sala no puede ser null");
            }else{
                cancelarSuscripcionCanalEmojis();
                cancelarSuscripcionCanalVotacionPausa();
                wsAPI.unsubscribe("/topic/salas/" + salaActualID);
                RestClient client = wsAPI.getRestClient();
                client.openConnection("/app/salas/salirDefinitivo/" + salaActualID);
                client.receiveObject(String.class, null);
                activity.mostrarMensaje("Has salido de la sala");
                salaActual = null;
                salaActualID = null;
                Intent intent = new Intent(activity, PrincipalActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                activity.startActivityForResult(intent, 0);
            }
        });
    }

    public void comprobarPartidaPausada(Consumer<Sala> consumer){
        RestClient client = wsAPI.getRestClient();
        client.openConnection("/api/comprobarPartidaPausada");
        client.receiveObject(Sala.class, sala -> {
            activity.runOnUiThread(() -> {
                if(sala.isNoExiste()){
                    consumer.accept(null);
                }else{
                    consumer.accept(sala);
                }
            });
        });
    }

    public void comprobarUnirseSala(UUID salaID, Consumer<Boolean> consumer){
        RestClient client = wsAPI.getRestClient();
        client.addParameter("salaID", salaID);
        client.openConnection("/api/comprobarUnirseSala");
        client.receiveObject(Boolean.class, b -> activity.runOnUiThread(() -> consumer.accept(b)));
    }

    public void obtenerSalasFiltro(ConfigSala filtro, Consumer<RespuestaSalas> consumer) {
        RestClient client = wsAPI.getRestClient();
        client.addParameter("configuracion", filtro);
        client.openConnection("/api/filtrarSalas");
        client.receiveObject(RespuestaSalas.class, respSalas -> activity.runOnUiThread(() -> consumer.accept(respSalas)));
    }

    //
    //  MODIFICACIÓN DE CUENTA
    //
    public void modificarCuenta(){
        RestClient client = wsAPI.getRestClient();
        client.openConnection("/api/sacarUsuarioVO");
        client.receiveObject(UsuarioVO.class, usuarioVO -> {
            activity.runOnUiThread(() -> {
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
        });
    }
    private void modificarCuentaPaso2(String nombreUsuario, String correo, String contrasennaHash,
                                      ModifyAccountDialogBuilder builder){
        RestClient client = wsAPI.getRestClient();
        client.addParameter("correoNuevo", correo);
        client.addParameter("nombre", nombreUsuario);
        client.addParameter("contrasenna", contrasennaHash);
        client.openConnection("/api/actualizarCuentaStepOne");
        client.receiveObject(String.class, error -> {
            activity.runOnUiThread(() -> {
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
        });
    }
    private void modificarCuentaPaso3(int codigo,
                                      String correo, String contrasennaHash,
                                      CodeConfirmDialogBuilder builder){
        RestClient client = wsAPI.getRestClient();
        client.addParameter("codigo", codigo);
        client.openConnection("/api/actualizarCuentaStepTwo");
        client.receiveObject(String.class, error -> {
            activity.runOnUiThread(() -> {
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
        });
    }
    private void cancelModificarCuenta(){
        RestClient client = wsAPI.getRestClient();
        client.openConnection("/api/actualizarCancel");
        client.receiveObject(String.class, error ->
                activity.runOnUiThread(() -> activity.mostrarMensaje("Operación cancelada")));
    }

    //
    //  BORRADO DE CUENTA
    //
    public void borrarCuenta(){
        obtenerUsuarioVO(usuarioVO -> {
            DeleteAccountDialogBuilder builder = new DeleteAccountDialogBuilder(activity);
            builder.setPositiveRunnable(() -> {
                RestClient client = wsAPI.getRestClient();
                client.openConnection("/api/borrarCuenta");
                client.receiveObject(String.class, error -> {
                    activity.runOnUiThread(() -> {
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
            });
            builder.show();
        });
    }

    //
    //  GESTIÓN DE AMIGOS
    //
    public void obtenerAmigos(Consumer<ListaUsuarios> consumer){
        RestClient client = wsAPI.getRestClient();
        client.openConnection("/api/sacarAmigos");
        client.receiveObject(ListaUsuarios.class, listaUsuarios -> {
            activity.runOnUiThread(() -> {
                if(listaUsuarios.isExpirado()){
                    activity.mostrarMensaje(listaUsuarios.getError());
                }else{
                    consumer.accept(listaUsuarios);
                }
            });
        });
    }
    public void obtenerPeticionesRecibidas(Consumer<ListaUsuarios> consumer){
        RestClient client = wsAPI.getRestClient();
        client.openConnection("/api/sacarPeticionesRecibidas");
        client.receiveObject(ListaUsuarios.class, listaUsuarios -> {
            activity.runOnUiThread(() -> {
                if(listaUsuarios.isExpirado()){
                    activity.mostrarMensaje(listaUsuarios.getError());
                }else{
                    consumer.accept(listaUsuarios);
                }
            });
        });
    }
    public void obtenerPeticionesEnviadas(Consumer<ListaUsuarios> consumer){
        RestClient client = wsAPI.getRestClient();
        client.openConnection("/api/sacarPeticionesEnviadas");
        client.receiveObject(ListaUsuarios.class, listaUsuarios -> {
            activity.runOnUiThread(() -> {
                if(listaUsuarios.isExpirado()){
                    activity.mostrarMensaje(listaUsuarios.getError());
                }else{
                    consumer.accept(listaUsuarios);
                }
            });
        });
    }

    public void enviarPeticion(Runnable runnable){
        PeticionAmistadDialogBuilder builder = new PeticionAmistadDialogBuilder(activity);
        builder.setPositiveButton(correo -> {
            obtenerUsuarioVO(usuarioPrincipal -> {
                activity.runOnUiThread(() -> {
                    if(correo.equals(usuarioPrincipal.getCorreo())){
                        builder.setError("No puedes enviarte una solicitud a ti mismo");
                        builder.show();
                    }else{
                        enviarPeticion2(correo, runnable);
                    }
                });
            });
        });
        builder.setNegativeButton(() -> activity.mostrarMensaje("Envío de solicitud cancelado"));
        builder.show();
    }
    private void enviarPeticion2(String correo, Runnable runnable){
        buscarUsuarioVO(correo, usuarioVO -> {
            activity.runOnUiThread(() -> {
                RestClient client = wsAPI.getRestClient();
                client.openConnection("/app/notifAmistad/" + usuarioVO.getId());
                client.receiveObject(String.class, null);
                Task.runDelayedTask(() -> runnable.run(), 300);
            });
        });
    }

    public void aceptarPeticion(UsuarioVO usuario){
        RestClient client = wsAPI.getRestClient();
        client.addParameter("amigo", usuario.getId());
        client.openConnection("/api/aceptarPeticionAmistad");
        client.receiveObject(String.class, error -> {
            activity.runOnUiThread(() -> {
                if(error != null){
                    activity.mostrarMensaje(error);
                }else{
                    activity.mostrarMensaje("Petición aceptada");
                }
            });
        });
    }
    public void rechazarPeticion(UsuarioVO usuario){
        RestClient client = wsAPI.getRestClient();
        client.addParameter("amigo", usuario.getId());
        client.openConnection("/api/cancelarPeticionAmistad");
        client.receiveObject(String.class, error -> {
            activity.runOnUiThread(() -> {
                if(error != null){
                    activity.mostrarMensaje(error);
                }else{
                    activity.mostrarMensaje("Petición rechazada");
                }
            });
        });
    }

    public void invitarAmigoSala(){
        SeleccionAmigoDialogBuilder builder = new SeleccionAmigoDialogBuilder(activity);
        builder.setPositiveButton(correo -> {
            buscarUsuarioVO(correo, usuarioVO -> {
                activity.runOnUiThread(() -> {
                    RestClient client = wsAPI.getRestClient();
                    client.addParameter("salaID", salaActualID);
                    client.openConnection("/app/notifSala/" + usuarioVO.getId());
                    client.receiveObject(String.class, null);
                    activity.mostrarMensaje("Invitación enviada");
                });
            });
        });
        builder.show();
    }

    //
    //  GESTIÓN DE PARTIDAS
    //
    public void enviarJugada(Jugada jugada){
        RestClient client = wsAPI.getRestClient();
        client.addParameter("jugada", jugada);
        client.openConnection("/app/partidas/turnos/" + salaActualID);
        client.receiveObject(String.class, null);
    }

    public void pulsarBotonUNO(){
        RestClient client = wsAPI.getRestClient();
        client.openConnection("/app/partidas/botonUNO/" + salaActualID);
        client.receiveObject(String.class, null);
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

        RestClient client = wsAPI.getRestClient();
        client.addParameter("emoji", envioEmoji);
        client.openConnection("/app/partidas/emojiPartida/" + salaActualID);
        client.receiveObject(String.class, null);
    }

    public void suscribirseCanalVotacionPausa(Consumer<RespuestaVotacionPausa> consumer){
        wsAPI.subscribe(activity, "/topic/salas/" + salaActualID + "/votaciones",
                RespuestaVotacionPausa.class, respuestaVotacionPausa -> {
                    if(respuestaVotacionPausa != null){
                        activity.runOnUiThread(() -> consumer.accept(respuestaVotacionPausa));
                    }
                });
    }
    public void enviarVotacion(){
        RestClient client = wsAPI.getRestClient();
        client.openConnection("/app/partidas/votaciones/" + salaActualID);
        client.receiveObject(String.class, null);
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
        RestClient client = wsAPI.getRestClient();
        client.addParameter("avatar", avatar);
        client.addParameter("aspectoFondo", aspectoFondo);
        client.addParameter("aspectoCartas", aspectoCartas);
        client.openConnection("/api/cambiarAvatar");
        client.receiveObject(String.class, error -> {
            activity.runOnUiThread(() -> {
                if(error == null){
                    // Cerrar sesión y volverla a iniciar
                    closeWebSocketAPI();
                    login(usuario.getCorreo(), usuario.getContrasenna());
                }else{
                    activity.mostrarMensaje(error);
                }
            });
        });
    }

    //
    // HISTORIAL
    //
    public void obtenerHistorial(UsuarioVO usuarioVO, Consumer<List<PartidaJugadaCompacta>> consumer){
        RestClient client = wsAPI.getRestClient();
        client.addParameter("usuarioID", usuarioVO.getId());
        client.openConnection("/api/sacarPartidasJugadas");
        client.receiveObject(ListaPartidas.class, listaPartidas -> {
            activity.runOnUiThread(() -> {
                if(listaPartidas.isExpirado()){
                    activity.mostrarMensaje(listaPartidas.getError());
                }else{
                    // Devuelve la lista de partidas jugadas en orden cronológico inverso
                    consumer.accept(listaPartidas.getPartidas().stream()
                            .map(PartidaJugada::getPartidaJugadaCompacta)
                            .sorted(((partidaJugadaCompacta1, partidaJugadaCompacta2) ->
                                    partidaJugadaCompacta2.getFechaInicio().compareTo(partidaJugadaCompacta1.getFechaInicio())))
                            .collect(Collectors.toList()));
                }
            });
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
