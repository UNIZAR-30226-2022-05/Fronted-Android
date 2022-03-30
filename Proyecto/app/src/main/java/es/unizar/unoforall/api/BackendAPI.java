package es.unizar.unoforall.api;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.widget.Toast;

import java.util.UUID;
import java.util.function.Consumer;

import es.unizar.unoforall.PantallaPrincipalActivity;
import es.unizar.unoforall.database.UsuarioDbAdapter;
import es.unizar.unoforall.model.RespuestaLogin;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.model.salas.RespuestaSalas;
import es.unizar.unoforall.utils.CodeConfirmDialogBuilder;
import es.unizar.unoforall.utils.HashUtils;
import es.unizar.unoforall.utils.ResetPasswordDialogBuilder;

public class BackendAPI{
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

                Intent i = new Intent(activity, PantallaPrincipalActivity.class);
                i.putExtra(PantallaPrincipalActivity.KEY_CLAVE_INICIO, respuestaLogin.getClaveInicio());
                activity.startActivity(i);
            }else{
                mostrarMensaje(respuestaLogin.getErrorInfo());
            }
        });
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

    public void obtenerSalasInicio(UUID sesionID, Consumer<RespuestaSalas> consumer){
        RestAPI api = new RestAPI(activity, "/api/filtrarSalas");
        api.addParameter("sesionID", sesionID.toString());
        api.addParameter("configuracion", null);
        api.setOnObjectReceived(RespuestaSalas.class, consumer);
    }

    @Override
    protected void finalize() throws Throwable{
        usuarioDbAdapter.close();
        super.finalize();
    }
}
