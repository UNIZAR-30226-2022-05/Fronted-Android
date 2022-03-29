package es.unizar.unoforall.api;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import es.unizar.unoforall.PantallaPrincipalActivity;
import es.unizar.unoforall.database.UsuarioDbAdapter;
import es.unizar.unoforall.model.RespuestaLogin;
import es.unizar.unoforall.utils.CodeConfirmDialogBuilder;
import es.unizar.unoforall.utils.HashUtils;

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
                if(usuarioDbAdapter.buscarUsuario(correo) == null){
                    usuarioDbAdapter.createUsuario(correo, contrasennaHash);
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
        api.setOnObjectReceived(Boolean.class, exito -> {
            mostrarMensaje("Registro cancelado");
        });
    }

    @Override
    protected void finalize() throws Throwable{
        usuarioDbAdapter.close();
        super.finalize();
    }
}
