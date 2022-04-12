package es.unizar.unoforall.utils;

import android.annotation.SuppressLint;
import android.widget.ImageView;

import androidx.appcompat.content.res.AppCompatResources;

import es.unizar.unoforall.R;

public class ImageManager{

    // Pre: -1 <= imageID <= 6
    @SuppressLint("UseCompatLoadingForDrawables")
    public static void setImage(ImageView imageView, int imageID){
        if(imageID < -1 || imageID > 6){
            throw new IllegalArgumentException("ID de imagen inválido: " + imageID + ". Debe estar entre -1 y 6");
        }

        int resourceID = -1;
        switch(imageID){
            case -1:
                resourceID = R.drawable.ic_perfil_ia;
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

        imageView.setBackground(imageView.getContext().getDrawable(resourceID));
    }
}
