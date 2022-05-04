package es.unizar.unoforall.utils;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.Image;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

import es.unizar.unoforall.R;
import es.unizar.unoforall.model.partidas.Carta;

public class ImageManager{

    public static final int INVALID_RESOURCE_ID = -1;
    public static final int IA_IMAGE_ID = -1;
    public static final int DEFAULT_IMAGE_ID = -2;
    public static final int IMAGEN_PERFIL_0_ID = 0;
    public static final int IMAGEN_PERFIL_1_ID = 1;
    public static final int IMAGEN_PERFIL_2_ID = 2;
    public static final int IMAGEN_PERFIL_3_ID = 3;
    public static final int IMAGEN_PERFIL_4_ID = 4;
    public static final int IMAGEN_PERFIL_5_ID = 5;
    public static final int IMAGEN_PERFIL_6_ID = 6;

    public static final int IMAGEN_FONDO_0_ID = 0;
    public static final int IMAGEN_FONDO_1_ID = 1;
    public static final int IMAGEN_FONDO_2_ID = 2;

    public static final int IMAGEN_EMOJI_O_ID = 0;
    public static final int IMAGEN_EMOJI_1_ID = 1;
    public static final int IMAGEN_EMOJI_2_ID = 2;
    public static final int IMAGEN_EMOJI_3_ID = 3;
    public static final int IMAGEN_EMOJI_4_ID = 4;

    private static final HashMap<Carta, Integer> defaultCardsMap = new HashMap<>();
    private static final HashMap<Carta, Integer> altCardsMap = new HashMap<>();

    public static final int ENABLED_CARD_COLOR = Color.parseColor("#00000000");
    public static final int DISABLED_CARD_COLOR = Color.parseColor("#5550545c");
    public static final int SELECTED_CARD_COLOR = Color.parseColor("#5548a84c");
    public static final int SELECTED_IMAGEN_PERFIL_COLOR = Color.parseColor("#88147ec9");
    public static final int SELECTED_FONDO_COLOR = Color.parseColor("#99555555");

    // Pre: -2 <= imageID <= 6
    @SuppressLint("UseCompatLoadingForDrawables")
    public static void setImagenPerfil(ImageView imageView, int imageID){
        if(imageID < -2 || imageID > 6){
            throw new IllegalArgumentException("ID de imagen inválido: " + imageID + ". Debe estar entre -2 y 6");
        }

        int resourceID = INVALID_RESOURCE_ID;
        switch(imageID){
            case IA_IMAGE_ID:
                resourceID = R.drawable.ic_perfil_ia;
                break;
            case DEFAULT_IMAGE_ID:
                resourceID = R.drawable.ic_iconoperfil;
                break;
            case IMAGEN_PERFIL_0_ID:
                resourceID = R.drawable.ic_perfil_0;
                break;
            case IMAGEN_PERFIL_1_ID:
                resourceID = R.drawable.ic_perfil_1;
                break;
            case IMAGEN_PERFIL_2_ID:
                resourceID = R.drawable.ic_perfil_2;
                break;
            case IMAGEN_PERFIL_3_ID:
                resourceID = R.drawable.ic_perfil_3;
                break;
            case IMAGEN_PERFIL_4_ID:
                resourceID = R.drawable.ic_perfil_4;
                break;
            case IMAGEN_PERFIL_5_ID:
                resourceID = R.drawable.ic_perfil_5;
                break;
            case IMAGEN_PERFIL_6_ID:
                resourceID = R.drawable.ic_perfil_6;
                break;
        }

        imageView.setImageResource(resourceID);
    }

    // Pre: 0 <= fondoID <= 2
    public static void setImagenFondo(View view, int fondoID){
        if(fondoID < 0 || fondoID > 2){
            throw new IllegalArgumentException("ID de fondo inválido: " + fondoID + ". Debe estar entre 0 y 2");
        }

        int resourceID = INVALID_RESOURCE_ID;
        switch(fondoID){
            case IMAGEN_FONDO_0_ID:
                resourceID = R.drawable.fondo_0;
                break;
            case IMAGEN_FONDO_1_ID:
                resourceID = R.drawable.fondo_1;
                break;
            case IMAGEN_FONDO_2_ID:
                resourceID = R.drawable.fondo_2;
                break;
        }

        if(view instanceof ImageView){
            ImageView imageView = (ImageView) view;
            imageView.setImageResource(resourceID);
        }else{
            view.setBackgroundResource(resourceID);
        }
    }

    public static void setImagenColor(ImageView imageView, TextView textView, Carta.Color color, boolean defaultMode){
        setImageViewClickable(imageView, true, false);
        switch(color){
            case rojo:
                if(defaultMode){
                    imageView.setImageResource(R.drawable.color_rojo);
                    textView.setText("Rojo");
                }else{
                    imageView.setImageResource(R.drawable.color_rojo_alt);
                    textView.setText("Azul claro");
                }
                break;
            case amarillo:
                if(defaultMode){
                    imageView.setImageResource(R.drawable.color_amarillo);
                    textView.setText("Amarillo");
                }else{
                    imageView.setImageResource(R.drawable.color_amarillo_alt);
                    textView.setText("Azul oscuro");
                }
                break;
            case verde:
                if(defaultMode){
                    imageView.setImageResource(R.drawable.color_verde);
                    textView.setText("Verde");
                }else{
                    imageView.setImageResource(R.drawable.color_verde_alt);
                    textView.setText("Rosa");
                }
                break;
            case azul:
                if(defaultMode){
                    imageView.setImageResource(R.drawable.color_azul);
                    textView.setText("Azul");
                }else{
                    imageView.setImageResource(R.drawable.color_azul_alt);
                    textView.setText("Naranja");
                }
                break;

        }
    }

    // Pre: 0 <= emojiID <= 4
    public static void setImagenEmoji(ImageView imageView, int emojiID){
        if(emojiID < 0 || emojiID > 4){
            throw new IllegalArgumentException("ID de emoji inválido: " + emojiID + ". Debe estar entre 0 y 4");
        }

        int resourceID = INVALID_RESOURCE_ID;
        switch(emojiID){
            case IMAGEN_EMOJI_O_ID:
                resourceID = R.drawable.emoji_0;
                break;
            case IMAGEN_EMOJI_1_ID:
                resourceID = R.drawable.emoji_1;
                break;
            case IMAGEN_EMOJI_2_ID:
                resourceID = R.drawable.emoji_2;
                break;
            case IMAGEN_EMOJI_3_ID:
                resourceID = R.drawable.emoji_3;
                break;
            case IMAGEN_EMOJI_4_ID:
                resourceID = R.drawable.emoji_4;
        }

        imageView.setImageResource(resourceID);
    }
    public static void setImagenEmojisActivados(ImageView imageView, boolean emojisActivados){
        int resourceID;
        if(emojisActivados){
            resourceID = R.drawable.activar_emojis;
        }else{
            resourceID = R.drawable.desactivar_emojis;
        }

        imageView.setImageResource(resourceID);
    }

    public static void setImagenCarta(ImageView imageView, Carta carta, boolean defaultMode, boolean isEnabled, boolean isVisible, boolean isClickable){
        if(defaultCardsMap.isEmpty() || altCardsMap.isEmpty()){
            for(Carta.Color color : Carta.Color.values()){
                for(Carta.Tipo tipo : Carta.Tipo.values()){
                    Carta aux = new Carta(tipo, color);
                    int resourceIDdefault = getResourceCarta(aux, true);
                    int resourceIDalt = getResourceCarta(aux, false);
                    if(resourceIDdefault != INVALID_RESOURCE_ID){
                        defaultCardsMap.put(aux, resourceIDdefault);
                    }
                    if(resourceIDalt != INVALID_RESOURCE_ID){
                        altCardsMap.put(aux, resourceIDalt);
                    }
                }
            }
        }

        if(isVisible){
            if(defaultMode){
                imageView.setImageResource(defaultCardsMap.get(carta));
            }else{
                imageView.setImageResource(altCardsMap.get(carta));
            }

            setImageViewEnable(imageView, isEnabled);
            setImageViewClickable(imageView, isClickable, false);
        }else{
            imageView.setImageResource(getResourceRevesCarta(defaultMode));
        }
    }

    public static void setImagenMazoCartas(ImageView imageView, boolean defaultMode, boolean isEnabled){
        imageView.setImageResource(getResourceMazoCartas(defaultMode));
        setImageViewClickable(imageView, isEnabled, false);
        setImageViewEnable(imageView, isEnabled);
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void setImageViewClickable(ImageView imageView, boolean clickable, boolean responsive){
        if(clickable){
            imageView.setOnTouchListener((v, event) -> {

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        //overlay is black with transparency of 0x77 (119)
                        ((ImageView) v).setColorFilter(0x77000000);
                        //getDrawable().setColorFilter(, PorterDuff.Mode.SRC_ATOP);
                        ((ImageView) v).invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:
                        if(responsive){
                            ((ImageView) v).performClick();
                        }
                    case MotionEvent.ACTION_CANCEL: {
                        //clear the overlay
                        ((ImageView) v).setColorFilter(ENABLED_CARD_COLOR);
                        ((ImageView) v).invalidate();
                        break;
                    }
                }

                return responsive;
            });
        }else{
            imageView.setOnTouchListener(null);
        }
    }

    public static void setImageViewEnable(ImageView imageView, boolean isEnabled){
        if(isEnabled){
            imageView.setColorFilter(ENABLED_CARD_COLOR);
        }else{
            imageView.setColorFilter(DISABLED_CARD_COLOR);
        }
    }

    public static void setImageViewColorFilter(ImageView imageView, int colorFilter){
        imageView.setColorFilter(colorFilter);
    }
    public static void setImageViewColorFilter(ImageButton imageButton, int colorFilter){
        imageButton.setColorFilter(colorFilter);
    }

    private static int getResourceCarta(Carta carta, boolean defaultMode){
        switch(carta.getColor()){
            case comodin: return getResourceComodin(carta.getTipo(), defaultMode);
            case rojo: return getResourceRojo(carta.getTipo(), defaultMode);
            case azul: return getResourceAzul(carta.getTipo(), defaultMode);
            case verde: return getResourceVerde(carta.getTipo(), defaultMode);
            case amarillo: return getResourceAmarillo(carta.getTipo(), defaultMode);
            default: return INVALID_RESOURCE_ID;
        }
    }

    private static int getResourceRevesCarta(boolean defaultMode){
        if(defaultMode){
            return R.drawable.carta_reves;
        }else{
            return R.drawable.carta_alt_reves;
        }
    }

    private static int getResourceMazoCartas(boolean defaultMode){
        if(defaultMode){
            return R.drawable.carta_mazo;
        }else{
            return R.drawable.carta_alt_mazo;
        }
    }

    private static int getResourceComodin(Carta.Tipo tipo, boolean defaultMode){
        if(defaultMode){
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

    private static int getResourceRojo(Carta.Tipo tipo, boolean defaultMode){
        if(defaultMode){
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

    private static int getResourceAzul(Carta.Tipo tipo, boolean defaultMode){
        if(defaultMode){
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

    private static int getResourceVerde(Carta.Tipo tipo, boolean defaultMode){
        if(defaultMode){
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

    private static int getResourceAmarillo(Carta.Tipo tipo, boolean defaultMode){
        if(defaultMode){
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
