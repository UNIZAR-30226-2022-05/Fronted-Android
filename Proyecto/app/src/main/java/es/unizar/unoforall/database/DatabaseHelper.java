package es.unizar.unoforall.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Clase que representa la base de datos
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "sesion_de_usuarios";
    private static final int DATABASE_VERSION = 1;

    private static final String TAG = "DatabaseHelper";

    /**
     * Sentencia SQL para crear la tabla de usuarios
     */
    private static final String CREATE_TABLE_USUARIOS =
            String.format("CREATE TABLE %s (\n" +
                            "%s INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                            "%s TEXT UNIQUE NOT NULL,\n" +
                            "%s TEXT NOT NULL);",
                    UsuarioDbAdapter.DATABASE_TABLE,
                    UsuarioDbAdapter.KEY_ID,
                    UsuarioDbAdapter.KEY_CORREO,
                    UsuarioDbAdapter.KEY_HASH_CONTRASENNA);


    /**
     * Constructor a partir de un contexto
     *
     * @param context El contexto de trabajo
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Método que se ejecutará cuando se inicialice la base de datos
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USUARIOS);
    }

    /**
     * Método que se ejecutará cuando se actualice la base de datos
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        onCreate(db);
    }

    /**
     * Método que se ejecutará cada vez que se cree una nueva conexión con la base de datos
     * @param db
     */
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Activar el soporte de claves extranjeras en la base de datos
        db.execSQL("PRAGMA foreign_keys=ON");
    }
}
