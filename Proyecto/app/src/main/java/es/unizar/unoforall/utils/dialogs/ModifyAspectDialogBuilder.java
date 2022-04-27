package es.unizar.unoforall.utils.dialogs;

import android.app.AlertDialog;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import es.unizar.unoforall.R;
import es.unizar.unoforall.model.UsuarioVO;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.ImageManager;
import es.unizar.unoforall.utils.TriConsumer;

public class ModifyAspectDialogBuilder {
    private final CustomActivity activity;
    private final UsuarioVO usuarioVO;

    // En la posición i está el precio del icono i+1
    private static final int[] PRECIO_ICONOS = {0, 10, 30, 50, 100, 200, 500};

    private Runnable positiveRunnable;
    private Runnable negativeRunnable;

    private final View mainView;

    private int iconoSeleccionado;
    private int fondoSeleccionado;
    private int cartasPorDefecto;

    private int dpToPx(int dp) {
        float density = activity.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    public ModifyAspectDialogBuilder(CustomActivity activity, UsuarioVO usuarioVO) {
        this.activity = activity;
        this.usuarioVO = usuarioVO;

        this.mainView = LayoutInflater.from(activity).inflate(R.layout.personalizacion_layout, null);

        this.iconoSeleccionado = usuarioVO.getAvatar();
        this.fondoSeleccionado = usuarioVO.getAspectoTablero();
        this.cartasPorDefecto = usuarioVO.getAspectoCartas();

        // Procesar las acciones en las imágenes de perfil
        TextView textView = this.mainView.findViewById(R.id.eligeUnIconoTextView);
        textView.setText("Elige un icono de perfil. Tienes " + usuarioVO.getPuntos() + " puntos");

        LinearLayout layoutImagenesPerfil = this.mainView.findViewById(R.id.layoutIconosPerfil);
        layoutImagenesPerfil.removeAllViews();
        for(int imageID=ImageManager.IMAGEN_PERFIL_0_ID; imageID<=ImageManager.IMAGEN_PERFIL_6_ID; imageID++){
            boolean sePuedeComprar = usuarioVO.getPuntos() >= PRECIO_ICONOS[imageID];

            ImageView imageView = new ImageView(activity);
            ImageManager.setImagenPerfil(imageView, imageID);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(64), dpToPx(64)));
            final int imageIDfinal = imageID;
            imageView.setOnClickListener(view -> {
                if(!sePuedeComprar){
                    activity.mostrarMensaje("No tienes puntos suficientes");
                    return;
                }

                ImageManager.setImageViewColorFilter(imageView, ImageManager.SELECTED_IMAGEN_PERFIL_COLOR);
                activity.mostrarMensaje("Has seleccionado el icono " + (imageIDfinal+1));
                iconoSeleccionado = imageIDfinal;
                for(int j=ImageManager.IMAGEN_PERFIL_0_ID; j<=ImageManager.IMAGEN_PERFIL_6_ID; j++){
                    if(j != imageIDfinal){
                        LinearLayout linearLayout = (LinearLayout) layoutImagenesPerfil.getChildAt(j);
                        ImageView imageView1 = (ImageView) linearLayout.getChildAt(0);
                        ImageManager.setImageViewEnable(imageView1,
                                usuarioVO.getPuntos() >= PRECIO_ICONOS[j]);
                    }
                }
            });

            if(usuarioVO.getAvatar() == imageID){
                ImageManager.setImageViewColorFilter(imageView, ImageManager.SELECTED_IMAGEN_PERFIL_COLOR);
                ImageManager.setImageViewClickable(imageView, true, false);
            }else if(sePuedeComprar){
                ImageManager.setImageViewEnable(imageView, true);
                ImageManager.setImageViewClickable(imageView, true, false);
            }else{
                ImageManager.setImageViewEnable(imageView, false);
                ImageManager.setImageViewClickable(imageView, false, false);
            }

            TextView textView1 = new TextView(activity);
            textView1.setText(String.format(Locale.ENGLISH,
                    "Icono %d\n%d puntos\nnecesarios", imageID+1, PRECIO_ICONOS[imageID]));
            textView1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                                                    ViewGroup.LayoutParams.WRAP_CONTENT));

            LinearLayout linearLayout = new LinearLayout(activity);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.setPadding(dpToPx(3), dpToPx(3), dpToPx(3), dpToPx(3));

            linearLayout.addView(imageView);
            linearLayout.addView(textView1);

            layoutImagenesPerfil.addView(linearLayout);
        }

        // Procesar las acciones en los fondos
        LinearLayout[] layoutFondos = new LinearLayout[] {
                this.mainView.findViewById(R.id.layoutFondo0),
                this.mainView.findViewById(R.id.layoutFondo1),
                this.mainView.findViewById(R.id.layoutFondo2)
        };
        TextView[] textoFondos = new TextView[] {
                this.mainView.findViewById(R.id.textViewFondo0),
                this.mainView.findViewById(R.id.textViewFondo1),
                this.mainView.findViewById(R.id.textViewFondo2),
        };
        ImageView[] imageViewsFondo = new ImageView[] {
                this.mainView.findViewById(R.id.imageViewFondo0),
                this.mainView.findViewById(R.id.imageViewFondo1),
                this.mainView.findViewById(R.id.imageViewFondo2),
        };

        layoutFondos[fondoSeleccionado].setBackgroundColor(ImageManager.SELECTED_FONDO_COLOR);
        textoFondos[fondoSeleccionado].setTextColor(Color.WHITE);
        layoutFondos[(fondoSeleccionado+1) % 3].setBackgroundColor(Color.WHITE);
        textoFondos[(fondoSeleccionado+1) % 3].setTextColor(Color.BLACK);
        layoutFondos[(fondoSeleccionado+2) % 3].setBackgroundColor(Color.WHITE);
        textoFondos[(fondoSeleccionado+2) % 3].setTextColor(Color.BLACK);

        for(int i=0; i<3; i++){
            ImageManager.setImageViewClickable(imageViewsFondo[i], true, false);
            final int finalI = i;
            imageViewsFondo[i].setOnClickListener(view -> {
                activity.mostrarMensaje("Has seleccionado el fondo " + (finalI+1));
                fondoSeleccionado = finalI;
                layoutFondos[fondoSeleccionado].setBackgroundColor(ImageManager.SELECTED_FONDO_COLOR);
                textoFondos[fondoSeleccionado].setTextColor(Color.WHITE);
                layoutFondos[(fondoSeleccionado+1) % 3].setBackgroundColor(Color.WHITE);
                textoFondos[(fondoSeleccionado+1) % 3].setTextColor(Color.BLACK);
                layoutFondos[(fondoSeleccionado+2) % 3].setBackgroundColor(Color.WHITE);
                textoFondos[(fondoSeleccionado+2) % 3].setTextColor(Color.BLACK);
            });
        }

        // Procesar las acciones con las imágenes de los colores
        LinearLayout coloresPorDefectoLayout = this.mainView.findViewById(R.id.coloresPorDefectoLayout);
        TextView coloresPorDefectoTextView = this.mainView.findViewById(R.id.coloresPorDefectoTextView);
        ImageButton coloresPorDefectoImageButton = this.mainView.findViewById(R.id.coloresPorDefectoImageButton);
        ImageManager.setImageViewClickable(coloresPorDefectoImageButton, true, false);

        LinearLayout coloresAlternativosLayout = this.mainView.findViewById(R.id.coloresAlternativosLayout);
        TextView coloresAlternativosTextView = this.mainView.findViewById(R.id.coloresAlternativosTextView);
        ImageButton coloresAlternativosImageButton = this.mainView.findViewById(R.id.coloresAlternativosImageButton);
        ImageManager.setImageViewClickable(coloresAlternativosImageButton, true, false);

        if(cartasPorDefecto == 0){
            coloresPorDefectoLayout.setBackgroundColor(ImageManager.SELECTED_FONDO_COLOR);
            coloresPorDefectoTextView.setTextColor(Color.WHITE);
            coloresAlternativosLayout.setBackgroundColor(Color.WHITE);
            coloresAlternativosTextView.setTextColor(Color.BLACK);
        }else{
            coloresAlternativosLayout.setBackgroundColor(ImageManager.SELECTED_FONDO_COLOR);
            coloresAlternativosTextView.setTextColor(Color.WHITE);
            coloresPorDefectoLayout.setBackgroundColor(Color.WHITE);
            coloresPorDefectoTextView.setTextColor(Color.BLACK);
        }

        coloresPorDefectoImageButton.setOnClickListener(view -> {
            activity.mostrarMensaje("Has seleccionado los colores por defecto");
            cartasPorDefecto = 0;
            coloresPorDefectoLayout.setBackgroundColor(ImageManager.SELECTED_FONDO_COLOR);
            coloresPorDefectoTextView.setTextColor(Color.WHITE);
            coloresAlternativosLayout.setBackgroundColor(Color.WHITE);
            coloresAlternativosTextView.setTextColor(Color.BLACK);
        });

        coloresAlternativosImageButton.setOnClickListener(view -> {
            activity.mostrarMensaje("Has seleccionado los colores alternativos");
            cartasPorDefecto = 1;
            coloresAlternativosLayout.setBackgroundColor(ImageManager.SELECTED_FONDO_COLOR);
            coloresAlternativosTextView.setTextColor(Color.WHITE);
            coloresPorDefectoLayout.setBackgroundColor(Color.WHITE);
            coloresPorDefectoTextView.setTextColor(Color.BLACK);
        });

        this.positiveRunnable = () -> {};
        this.negativeRunnable = () -> {};
    }

    public void setPositiveButton(TriConsumer<Integer, Integer, Integer> consumer){
        this.positiveRunnable = () -> {
            consumer.accept(this.iconoSeleccionado, this.fondoSeleccionado, this.cartasPorDefecto);
        };
    }

    public void setNegativeButton(Runnable runnable){
        this.negativeRunnable = runnable;
    }

    public void show(){
        ViewParent parent = mainView.getParent();
        if(parent != null){
            ((ViewGroup) parent).removeView(mainView);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Personaliza tu cuenta");
        builder.setMessage("Cambia tu avatar, fondo o color de cartas");
        builder.setView(mainView);
        builder.setPositiveButton("Confirmar", (dialog, which) -> positiveRunnable.run());
        builder.setNegativeButton("Cancelar", (dialog, which) -> negativeRunnable.run());
        builder.setOnCancelListener(dialog -> show());

        builder.show();
    }

}
