package es.unizar.unoforall.utils.notifications;

import es.unizar.unoforall.api.BackendAPI;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.salas.NotificacionSala;
import es.unizar.unoforall.utils.CustomActivity;

public class Notificaciones {
    public static void mostrarNotificacionAmigo(UsuarioVO usuarioVO){
        CustomActivity activity = BackendAPI.getCurrentActivity();

        NotificationManager.Builder builder = new NotificationManager.Builder(activity);
        builder
                .withTitle("Nueva solicitud de amistad")
                .withMessage("El usuario " + usuarioVO.getNombre() + " quiere ser tu amigo")
                .withAction1("Cancelar", customActivity -> {
                    new BackendAPI(activity).rechazarPeticion(usuarioVO);
                    return true;
                })
                .withAction2("Aceptar", customActivity -> {
                    new BackendAPI(activity).aceptarPeticion(usuarioVO);
                    return true;
                }).build();
    }

    public static void mostrarNotificacionSala(NotificacionSala notificacionSala){
        CustomActivity activity = BackendAPI.getCurrentActivity();

        NotificationManager.Builder builder = new NotificationManager.Builder(activity);
        builder
                .withTitle("Nueva solicitud para unirse a una sala")
                .withMessage("El usuario " + notificacionSala.getRemitente().getNombre() +
                        " te ha propuesto unirse a la sala " + notificacionSala.getSalaID())
                .withAction1("Cancelar", customActivity -> true)
                .withAction2("Aceptar", customActivity -> {
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
                            new BackendAPI(activity).iniciarUnirseSala(notificacionSala.getSalaID());
                            return true;
                        case SALA:
                        case PARTIDA:
                            // Mostrar mensaje de error
                            activity.mostrarMensaje("No puedes unirte a esa sala ahora mismo");
                            return false;
                        default:
                            return true;
                    }
                }).build();
    }
}
