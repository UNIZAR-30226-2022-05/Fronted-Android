package es.unizar.unoforall.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.Objects;

public class UsuarioDbAdapter {
    public static final String DATABASE_TABLE = "usuarios";

    public static final String KEY_CORREO = "correo";
    public static final String KEY_HASH_CONTRASENNA = "hash_contrasenna";
    public static final String KEY_ID = "_id";

    private DatabaseHelper miDbHelper;
    private SQLiteDatabase miDb;

    private final Context miContexto;

    /**
     * Constructor que usa el contexto para poder abrir / cerrar
     *      la base de datos
     *
     * @param ctx El contexto de trabajo
     */
    public UsuarioDbAdapter(Context ctx) {
        this.miContexto = ctx;
    }

    /**
     * Abre una conexión a la base de datos.
     * Si no se puede abrir, intenta crear una nueva instancia de la base de datos.
     * Si no se puede crear, lanza una excepción
     *
     * @return this (auto referencia)
     * @throws SQLException Si la base de datos no se pudo abrir ni crear
     */
    public UsuarioDbAdapter open() throws SQLException {
        miDbHelper = new DatabaseHelper(miContexto);
        miDb = miDbHelper.getWritableDatabase();
        return this;
    }

    /**
     * Cierra la conexión actual con la base de datos
     */
    public void close() {
        miDbHelper.close();
    }

    /**
     * Crea un nuevo usuario con un correo y un hash de contraseña
     *
     * @param correo El correo del usuario (distinto de null)
     * @param hashContrasenna El hash de la contraseña (distinto de null)
     * @return El ID del nuevo producto o -1 si falla
     */
    public long createUsuario(String correo, String hashContrasenna) {
        Objects.requireNonNull(correo);
        Objects.requireNonNull(hashContrasenna);

        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_CORREO, correo);
        initialValues.put(KEY_HASH_CONTRASENNA, hashContrasenna);

        return miDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Borra el usuario identificado por su ID
     *
     * @param idUsuario El ID del usuario a borrar
     * @return true si se ha eliminado correctamente y false en caso contrario
     */
    public boolean deleteUsuario(long idUsuario) {
        return miDb.delete(DATABASE_TABLE, KEY_ID + "=" + idUsuario, null) > 0;
    }
    /**
     * Borra el usuario identificado por su correo
     *
     * @param correo El correo del usuario a borrar
     * @return true si se ha eliminado correctamente y false en caso contrario
     */
    public boolean deleteUsuario(String correo) {
        return miDb.delete(DATABASE_TABLE, KEY_CORREO + "=" + correo, null) > 0;
    }

    /**
     * Devuelve un Cursor sobre la lista de todos los usuarios
     *      de la base de datos
     * @return El cursor apuntando a la lista de usuarios
     */
    public Cursor listarUsuarios() {
        return miDb.query(DATABASE_TABLE,
                null,
                null,
                null,
                null,
                null,
                KEY_ID + " asc");
    }

    /**
     * Devuelve un cursor apuntando al usuario con un ID concreto
     *
     * @param idUsuario ID del usuario a buscar
     * @return Cursor posicionado en el usuario solicitado, si existe
     * @throws SQLException Si no se encuentra dicho usuario
     */
    public Cursor buscarUsuario(long idUsuario) throws SQLException {
        Cursor cursor = miDb.query(true, DATABASE_TABLE, null,
                KEY_ID + "=" + idUsuario,
                null,
                null,
                null,
                null,
                null);
        if(cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
    /**
     * Devuelve un cursor apuntando al usuario con un correo concreto
     *
     * @param correo correo del usuario a buscar
     * @return Cursor posicionado en el usuario solicitado, si existe
     * @throws SQLException Si no se encuentra dicho usuario
     */
    public Cursor buscarUsuario(String correo) throws SQLException {
        Cursor cursor = miDb.query(true, DATABASE_TABLE, null,
                KEY_CORREO + "=" + correo,
                null,
                null,
                null,
                null,
                null);
        if(cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    /**
     * Actualiza los datos de un usuario existente
     *
     * @param idUsuario ID del usuario a actualizar
     * @param correo El nuevo correo del usuario
     * @param hashContrasenna El nuevo hash de contraseña del usuario
     *    Si algún parámetro a modificar es nulo, éste no se actualizará en la base de datos
     * @return true si el usuario se actualizó correctamente y false en caso contrario
     */
    public boolean modificarUsuario(long idUsuario, String correo, String hashContrasenna) {
        if(correo == null && hashContrasenna == null) {
            return false;
        }

        ContentValues args = new ContentValues();
        if(correo != null) {
            args.put(KEY_CORREO, correo);
        }
        if(hashContrasenna != null) {
            args.put(KEY_HASH_CONTRASENNA, hashContrasenna);
        }

        return miDb.update(DATABASE_TABLE, args, KEY_ID + "=" + idUsuario, null) > 0;
    }
}
