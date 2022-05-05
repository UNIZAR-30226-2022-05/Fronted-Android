package es.unizar.unoforall.utils;

import java.text.SimpleDateFormat;
import java.sql.Date;
import java.util.Locale;

public class FechaUtils {
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ENGLISH);

    public static String formatDate(Long date){
        return formatDate(new Date(date));
    }

    public static String formatDate(Date date){
        return SDF.format(date);
    }
}
