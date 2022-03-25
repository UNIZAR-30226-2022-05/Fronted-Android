package es.unizar.unoforall.modelo;

import java.util.UUID;

public class RespuestaLogin {
    public boolean exito;
    public String errorInfo;
    public UUID sesionID;

    public RespuestaLogin(boolean exito, String errorInfo, UUID sessionID) {
        this.exito = exito;
        this.errorInfo = errorInfo;
        this.sesionID = sessionID;
    }
}
