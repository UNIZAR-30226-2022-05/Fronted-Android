package es.unizar.unoforall.utils.dialogs;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import java.util.function.Consumer;

import es.unizar.unoforall.R;
import es.unizar.unoforall.utils.CustomActivity;
import es.unizar.unoforall.utils.ImageManager;

public class SelectEmojiDialogBuilder {
    private final CustomActivity activity;

    private final View mainView;
    private final ImageView[] imageViewsEmojis;

    private Dialog dialog;

    private Consumer<Integer> emojiIDConsumer = emojiID -> {};
    private Runnable negativeRunnable = () -> {};

    public SelectEmojiDialogBuilder(CustomActivity activity){
        this.activity = activity;

        this.mainView = LayoutInflater.from(activity).inflate(R.layout.selector_emoji, null);

        this.imageViewsEmojis = new ImageView[] {
                mainView.findViewById(R.id.imageViewEmoji0),
                mainView.findViewById(R.id.imageViewEmoji1),
                mainView.findViewById(R.id.imageViewEmoji2),
                mainView.findViewById(R.id.imageViewEmoji3),
                mainView.findViewById(R.id.imageViewEmoji4)
        };

        for(int i=0; i<imageViewsEmojis.length; i++){
            ImageView imageView = imageViewsEmojis[i];
            ImageManager.setImageViewClickable(imageView, true, false);
            final int emojiID = i;
            imageView.setOnClickListener(view -> {
                dialog.dismiss();
                emojiIDConsumer.accept(emojiID);
            });
        }
    }

    public void setOnEmojiSelected(Consumer<Integer> consumer){
        this.emojiIDConsumer = consumer;
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
        builder.setTitle("Enviar emoji");
        builder.setMessage("Selecciona el emoji a enviar");
        builder.setView(mainView);
        builder.setNegativeButton("Cancelar", (dialog, which) -> negativeRunnable.run());
        builder.setOnCancelListener(dialog -> negativeRunnable.run());

        this.dialog = builder.create();
        this.dialog.show();
    }
}
