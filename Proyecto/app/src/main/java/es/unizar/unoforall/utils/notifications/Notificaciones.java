package es.unizar.unoforall.utils.notifications;

import es.unizar.unoforall.NotificacionesActivity;
import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.salas.NotificacionSala;
import es.unizar.unoforall.utils.CustomActivity;

public class Notificaciones {
    public static String ACEPTAR_TEXT = "Aceptar";
    public static String CANCELAR_TEXT = "Cancelar";

    public static Notificacion createNotificacionAmigo(UsuarioVO usuarioVO){
        CustomActivity activity = BackendAPI.getCurrentActivity();

        NotificationManager.Builder builder = new NotificationManager.Builder(activity);
        builder
                .withTitle("Nueva solicitud de amistad")
                .withMessage("El usuario " + usuarioVO.getNombre() + " quiere ser tu amigo")
                .withAction1(CANCELAR_TEXT, customActivity -> {
                    new BackendAPI(activity).rechazarPeticion(usuarioVO);

                    if(activity instanceof NotificacionesActivity){
                        ((NotificacionesActivity) activity).refreshData();
                    }
                    return true;
                })
                .withAction2(ACEPTAR_TEXT, customActivity -> {
                    new BackendAPI(activity).aceptarPeticion(usuarioVO);

                    if(activity instanceof NotificacionesActivity){
                        ((NotificacionesActivity) activity).refreshData();
                    }

                    return true;
                });
        return builder.build();
    }

    public static Notificacion createNotificacionSala(NotificacionSala notificacionSala){
        CustomActivity activity = BackendAPI.getCurrentActivity();

        NotificationManager.Builder builder = new NotificationManager.Builder(activity);
        builder
                .withTitle("Nueva solicitud para unirse a una sala")
                .withMessage("El usuario " + notificacionSala.getRemitente().getNombre() +
                        " te ha propuesto unirte a la sala " + notificacionSala.getSalaID())
                .withAction1(CANCELAR_TEXT, customActivity -> {
                    BackendAPI.removeNotificacionSala(builder.build());
                    if(activity instanceof NotificacionesActivity){
                        ((NotificacionesActivity) activity).refreshData();
                    }

                    return true;
                })
                .withAction2(ACEPTAR_TEXT, customActivity -> {
                    // Comprobar en que actividad se encuentra el usuario
                    //   y actuar en consecuencia
                    switch(BackendAPI.getCurrentActivity().getType()){
                        case INICIO:
                        case LOGIN:
                        case REGISTER:
                        case RESTABLECER_CONTRASENNA:
                            // Omitir porque no se ha iniciado sesión
                            return true;
                        case PRINCIPAL:
                        case AMIGOS:
                        case CREAR_SALA:
                        case BUSCAR_SALA:
                            // Unirse a la sala correspondiente
                            new BackendAPI(activity).unirseSala(notificacionSala.getSalaID(), exito -> {
                                BackendAPI.removeNotificacionSala(builder.build());
                                if(activity instanceof NotificacionesActivity){
                                    ((NotificacionesActivity) activity).refreshData();
                                }
                            });
                            return true;
                        case SALA:
                        case PARTIDA:
                            // Mostrar mensaje de error
                            activity.mostrarMensaje("No puedes unirte a esa sala ahora mismo");
                            BackendAPI.removeNotificacionSala(builder.build());
                            if(activity instanceof NotificacionesActivity){
                                ((NotificacionesActivity) activity).refreshData();
                            }
                            return false;
                        default:
                            return true;
                    }
                });
        return builder.build();
    }

    public static void mostrarNotificacionAmigo(UsuarioVO usuarioVO){
        CustomActivity activity = BackendAPI.getCurrentActivity();
        if(activity instanceof NotificacionesActivity){
            ((NotificacionesActivity) activity).refreshData();
        }
        createNotificacionAmigo(usuarioVO).show();
    }

    public static void mostrarNotificacionSala(NotificacionSala notificacionSala){
        Notificacion notificacion = createNotificacionSala(notificacionSala);
        BackendAPI.addNotificacionSala(notificacion);
        CustomActivity activity = BackendAPI.getCurrentActivity();
        if(activity instanceof NotificacionesActivity){
            ((NotificacionesActivity) activity).refreshData();
        }
        notificacion.show();
    }
}
