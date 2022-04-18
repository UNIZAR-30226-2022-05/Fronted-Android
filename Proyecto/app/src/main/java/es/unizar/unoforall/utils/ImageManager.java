package es.unizar.unoforall.utils;

import android.annotation.SuppressLint;
import android.widget.ImageView;

import androidx.appcompat.content.res.AppCompatResources;

import es.unizar.unoforall.R;
import es.unizar.unoforall.model.partidas.Carta;

public class ImageManager{

    public static int INVALID_RESOURCE_ID = -1;
    public static int IA_IMAGE_ID = -1;
    public static int DEFAULT_IMAGE_ID = -2;

    // Pre: -2 <= imageID <= 6
    @SuppressLint("UseCompatLoadingForDrawables")
    public static void setImagePerfil(ImageView imageView, int imageID){
        if(imageID < -2 || imageID > 6){
            throw new IllegalArgumentException("ID de imagen inválido: " + imageID + ". Debe estar entre -1 y 6");
        }

        int resourceID = INVALID_RESOURCE_ID;
        switch(imageID){
            case -1:
                resourceID = R.drawable.ic_perfil_ia;
                break;
            case -2:
                resourceID = R.drawable.ic_iconoperfil;
                break;
            case 0:
                resourceID = R.drawable.ic_perfil_0;
                break;
            case 1:
                resourceID = R.drawable.ic_perfil_1;
                break;
            case 2:
                resourceID = R.drawable.ic_perfil_2;
                break;
            case 3:
                resourceID = R.drawable.ic_perfil_3;
                break;
            case 4:
                resourceID = R.drawable.ic_perfil_4;
                break;
            case 5:
                resourceID = R.drawable.ic_perfil_5;
                break;
            case 6:
                resourceID = R.drawable.ic_perfil_6;
                break;
        }

        imageView.setImageResource(resourceID);
    }

    // Convendría guardar un Hashmap de Carta asociado a resourceID
    //      para que el acceso en tiempo sea menor
    public static void setImagenCarta(ImageView imageView, Carta carta, boolean normalMode){
        int resourceID = INVALID_RESOURCE_ID;
        switch(carta.getColor()){
            case comodin:
                resourceID = getResourceComodin(carta.getTipo(), normalMode);
                break;
            case rojo:
                resourceID = getResourceRojo(carta.getTipo(), normalMode);
                break;
            case azul:
                resourceID = getResourceAzul(carta.getTipo(), normalMode);
                break;
            case verde:
                resourceID = getResourceVerde(carta.getTipo(), normalMode);
                break;
            case amarillo:
                resourceID = getResourceAmarillo(carta.getTipo(), normalMode);
                break;
        }

        imageView.setImageResource(resourceID);
    }

    private static int getResourceRevesCarta(boolean normalMode){
        if(normalMode){
            return R.drawable.carta_reves;
        }else{
            return R.drawable.carta_alt_reves;
        }
    }

    private static int getResourceMazoCartas(boolean normalMode){
        if(normalMode){
            return R.drawable.carta_mazo;
        }else{
            return R.drawable.carta_alt_mazo;
        }
    }

    private static int getResourceComodin(Carta.Tipo tipo, boolean normalMode){
        if(normalMode){
            switch (tipo){
                case cambioColor: return R.drawable.comodin_cambio_color;
                case mas4: return R.drawable.comodin_mas4;
                default: return INVALID_RESOURCE_ID;
            }
        }else{
            switch (tipo){
                case cambioColor: return R.drawable.comodin_alt_cambio_color;
                case mas4: return R.drawable.comodin_alt_mas4;
                default: return INVALID_RESOURCE_ID;
            }
        }
    }

    private static int getResourceRojo(Carta.Tipo tipo, boolean normalMode){
        if(normalMode){
            switch (tipo){
                case n0: return R.drawable.rojo_0;
                case n1: return R.drawable.rojo_1;
                case n2: return R.drawable.rojo_2;
                case n3: return R.drawable.rojo_3;
                case n4: return R.drawable.rojo_4;
                case n5: return R.drawable.rojo_5;
                case n6: return R.drawable.rojo_6;
                case n7: return R.drawable.rojo_7;
                case n8: return R.drawable.rojo_8;
                case n9: return R.drawable.rojo_9;
                case mas2: return R.drawable.rojo_mas2;
                case salta: return R.drawable.rojo_saltar;
                case reversa: return R.drawable.rojo_cambio_sentido;
                case rayosX: return R.drawable.rojo_rayosx;
                case intercambio: return R.drawable.rojo_intercambio;
                case x2: return R.drawable.rojo_x2;
                case cambioColor: return R.drawable.rojo_cambio_color;
                case mas4: return R.drawable.rojo_mas4;
                default: return INVALID_RESOURCE_ID;
            }
        }else{
            switch (tipo){
                case n0: return R.drawable.rojo_alt_0;
                case n1: return R.drawable.rojo_alt_1;
                case n2: return R.drawable.rojo_alt_2;
                case n3: return R.drawable.rojo_alt_3;
                case n4: return R.drawable.rojo_alt_4;
                case n5: return R.drawable.rojo_alt_5;
                case n6: return R.drawable.rojo_alt_6;
                case n7: return R.drawable.rojo_alt_7;
                case n8: return R.drawable.rojo_alt_8;
                case n9: return R.drawable.rojo_alt_9;
                case mas2: return R.drawable.rojo_alt_mas2;
                case salta: return R.drawable.rojo_alt_saltar;
                case reversa: return R.drawable.rojo_alt_cambio_sentido;
                case rayosX: return R.drawable.rojo_alt_rayosx;
                case intercambio: return R.drawable.rojo_alt_intercambio;
                case x2: return R.drawable.rojo_alt_x2;
                case cambioColor: return R.drawable.rojo_alt_cambio_color;
                case mas4: return R.drawable.rojo_alt_mas4;
                default: return INVALID_RESOURCE_ID;
            }
        }
    }

    private static int getResourceAzul(Carta.Tipo tipo, boolean normalMode){
        if(normalMode){
            switch (tipo){
                case n0: return R.drawable.azul_0;
                case n1: return R.drawable.azul_1;
                case n2: return R.drawable.azul_2;
                case n3: return R.drawable.azul_3;
                case n4: return R.drawable.azul_4;
                case n5: return R.drawable.azul_5;
                case n6: return R.drawable.azul_6;
                case n7: return R.drawable.azul_7;
                case n8: return R.drawable.azul_8;
                case n9: return R.drawable.azul_9;
                case mas2: return R.drawable.azul_mas2;
                case salta: return R.drawable.azul_saltar;
                case reversa: return R.drawable.azul_cambio_sentido;
                case rayosX: return R.drawable.azul_rayosx;
                case intercambio: return R.drawable.azul_intercambio;
                case x2: return R.drawable.azul_x2;
                case cambioColor: return R.drawable.azul_cambio_color;
                case mas4: return R.drawable.azul_mas4;
                default: return INVALID_RESOURCE_ID;
            }
        }else{
            switch (tipo){
                case n0: return R.drawable.azul_alt_0;
                case n1: return R.drawable.azul_alt_1;
                case n2: return R.drawable.azul_alt_2;
                case n3: return R.drawable.azul_alt_3;
                case n4: return R.drawable.azul_alt_4;
                case n5: return R.drawable.azul_alt_5;
                case n6: return R.drawable.azul_alt_6;
                case n7: return R.drawable.azul_alt_7;
                case n8: return R.drawable.azul_alt_8;
                case n9: return R.drawable.azul_alt_9;
                case mas2: return R.drawable.azul_alt_mas2;
                case salta: return R.drawable.azul_alt_saltar;
                case reversa: return R.drawable.azul_alt_cambio_sentido;
                case rayosX: return R.drawable.azul_alt_rayosx;
                case intercambio: return R.drawable.azul_alt_intercambio;
                case x2: return R.drawable.azul_alt_x2;
                case cambioColor: return R.drawable.azul_alt_cambio_color;
                case mas4: return R.drawable.azul_alt_mas4;
                default: return INVALID_RESOURCE_ID;
            }
        }
    }

    private static int getResourceVerde(Carta.Tipo tipo, boolean normalMode){
        if(normalMode){
            switch (tipo){
                case n0: return R.drawable.verde_0;
                case n1: return R.drawable.verde_1;
                case n2: return R.drawable.verde_2;
                case n3: return R.drawable.verde_3;
                case n4: return R.drawable.verde_4;
                case n5: return R.drawable.verde_5;
                case n6: return R.drawable.verde_6;
                case n7: return R.drawable.verde_7;
                case n8: return R.drawable.verde_8;
                case n9: return R.drawable.verde_9;
                case mas2: return R.drawable.verde_mas2;
                case salta: return R.drawable.verde_saltar;
                case reversa: return R.drawable.verde_cambio_sentido;
                case rayosX: return R.drawable.verde_rayosx;
                case intercambio: return R.drawable.verde_intercambio;
                case x2: return R.drawable.verde_x2;
                case cambioColor: return R.drawable.verde_cambio_color;
                case mas4: return R.drawable.verde_mas4;
                default: return INVALID_RESOURCE_ID;
            }
        }else{
            switch (tipo){
                case n0: return R.drawable.verde_alt_0;
                case n1: return R.drawable.verde_alt_1;
                case n2: return R.drawable.verde_alt_2;
                case n3: return R.drawable.verde_alt_3;
                case n4: return R.drawable.verde_alt_4;
                case n5: return R.drawable.verde_alt_5;
                case n6: return R.drawable.verde_alt_6;
                case n7: return R.drawable.verde_alt_7;
                case n8: return R.drawable.verde_alt_8;
                case n9: return R.drawable.verde_alt_9;
                case mas2: return R.drawable.verde_alt_mas2;
                case salta: return R.drawable.verde_alt_saltar;
                case reversa: return R.drawable.verde_alt_cambio_sentido;
                case rayosX: return R.drawable.verde_alt_rayosx;
                case intercambio: return R.drawable.verde_alt_intercambio;
                case x2: return R.drawable.verde_alt_x2;
                case cambioColor: return R.drawable.verde_alt_cambio_color;
                case mas4: return R.drawable.verde_alt_mas4;
                default: return INVALID_RESOURCE_ID;
            }
        }
    }

    private static int getResourceAmarillo(Carta.Tipo tipo, boolean normalMode){
        if(normalMode){
            switch (tipo){
                case n0: return R.drawable.amarillo_0;
                case n1: return R.drawable.amarillo_1;
                case n2: return R.drawable.amarillo_2;
                case n3: return R.drawable.amarillo_3;
                case n4: return R.drawable.amarillo_4;
                case n5: return R.drawable.amarillo_5;
                case n6: return R.drawable.amarillo_6;
                case n7: return R.drawable.amarillo_7;
                case n8: return R.drawable.amarillo_8;
                case n9: return R.drawable.amarillo_9;
                case mas2: return R.drawable.amarillo_mas2;
                case salta: return R.drawable.amarillo_saltar;
                case reversa: return R.drawable.amarillo_cambio_sentido;
                case rayosX: return R.drawable.amarillo_rayosx;
                case intercambio: return R.drawable.amarillo_intercambio;
                case x2: return R.drawable.amarillo_x2;
                case cambioColor: return R.drawable.amarillo_cambio_color;
                case mas4: return R.drawable.amarillo_mas4;
                default: return INVALID_RESOURCE_ID;
            }
        }else{
            switch (tipo){
                case n0: return R.drawable.amarillo_alt_0;
                case n1: return R.drawable.amarillo_alt_1;
                case n2: return R.drawable.amarillo_alt_2;
                case n3: return R.drawable.amarillo_alt_3;
                case n4: return R.drawable.amarillo_alt_4;
                case n5: return R.drawable.amarillo_alt_5;
                case n6: return R.drawable.amarillo_alt_6;
                case n7: return R.drawable.amarillo_alt_7;
                case n8: return R.drawable.amarillo_alt_8;
                case n9: return R.drawable.amarillo_alt_9;
                case mas2: return R.drawable.amarillo_alt_mas2;
                case salta: return R.drawable.amarillo_alt_saltar;
                case reversa: return R.drawable.amarillo_alt_cambio_sentido;
                case rayosX: return R.drawable.amarillo_alt_rayosx;
                case intercambio: return R.drawable.amarillo_alt_intercambio;
                case x2: return R.drawable.amarillo_alt_x2;
                case cambioColor: return R.drawable.amarillo_alt_cambio_color;
                case mas4: return R.drawable.amarillo_alt_mas4;
                default: return INVALID_RESOURCE_ID;
            }
        }
    }
}
