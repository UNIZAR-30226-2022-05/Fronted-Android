package es.unizar.unoforall.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import es.unizar.unoforall.model.partidas.Carta;

public class AnimationManager{
    private static final float MIN_ALPHA = 0.0f;
    private static final float MAX_ALPHA = 1.0f;

    private static final long ALPHA_DURATION = 3000L;
    private static final long ALPHA_START_DELAY = 2000L;

    private static final long CARD_MOVEMENT_START_DELAY = 500L;
    private static final long CARD_MOVEMENT_DURATION = 1000L;

    public static void cancelAnimation(View view){
        view.clearAnimation();
    }

    public static void animateFadeIn(View view){
        cancelAnimation(view);
        view.setAlpha(MIN_ALPHA);
        view.animate()
                .alpha(MAX_ALPHA)
                .setDuration(ALPHA_DURATION)
                .setStartDelay(ALPHA_START_DELAY)
                .start();
    }

    public static void animateFadeOut(View view){
        cancelAnimation(view);
        view.setAlpha(MAX_ALPHA);
        view.animate()
                .alpha(MIN_ALPHA)
                .setDuration(ALPHA_DURATION)
                .setStartDelay(ALPHA_START_DELAY)
                .start();
    }

    private static void animateCardMovement(ViewGroup viewGroup, View startView, View endView,
                                            Carta carta, boolean isVisible, long startDelay, boolean defaultMode, Runnable endAction){
        if(startView == endView){
            return;
        }
        
        ImageView cartaView = new ImageView(viewGroup.getContext());
        viewGroup.addView(cartaView);
        cartaView.setX(startView.getX());
        cartaView.setY(startView.getY());
        cartaView.setLayoutParams(new FrameLayout.LayoutParams(150, -2));
        ImageManager.setImagenCarta(cartaView, carta, defaultMode, true, isVisible, false);
        cartaView.animate()
                .x(endView.getX())
                .y(endView.getY())
                .setDuration(CARD_MOVEMENT_DURATION)
                .withEndAction(() -> {
                    cartaView.setVisibility(View.GONE);
                    viewGroup.removeView(cartaView);
                    endAction.run();
                })
                .setStartDelay(startDelay)
                .start();
    }

    public static class Builder{
        private final ViewGroup viewGroup;
        private View startView;
        private View endView;

        private List<Carta> cartas;
        private boolean isVisible;
        private boolean defaultMode;

        private Runnable endAction;

        public Builder(ViewGroup viewGroup){
            this.viewGroup = viewGroup;
            this.endAction = () -> {};
        }

        public Builder withStartView(View startView){
            this.startView = startView;
            return this;
        }

        public Builder withEndView(View endView){
            this.endView = endView;
            return this;
        }

        public Builder withDefaultMode(boolean defaultMode){
            this.defaultMode = defaultMode;
            return this;
        }

        public Builder withCartasRobo(int numCartasRobo){
            this.isVisible = false;
            this.cartas = new ArrayList<>();
            for(int i=0; i<numCartasRobo; i++){
                this.cartas.add(null);
            }
            return this;
        }

        public Builder withCartas(List<Carta> cartas, boolean isVisible){
            this.isVisible = isVisible;
            this.cartas = cartas;
            return this;
        }

        public Builder withEndAction(Runnable endAction){
            this.endAction = endAction;
            return this;
        }

        public void start(){
            for(int i=0; i<this.cartas.size(); i++){
                animateCardMovement(viewGroup, startView, endView, this.cartas.get(i), isVisible,
                        i * CARD_MOVEMENT_START_DELAY, defaultMode, endAction);
            }
        }
    }
}
