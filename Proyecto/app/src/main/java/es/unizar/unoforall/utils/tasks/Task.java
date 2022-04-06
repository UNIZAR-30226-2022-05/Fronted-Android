package es.unizar.unoforall.utils.tasks;

import android.os.Handler;
import android.os.Looper;

public class Task {
    public static void runDelayedTask(Runnable runnable, long milliseconds){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(runnable, milliseconds);
    }

    public static void runPeriodicTask(CancellableRunnable runnable, long prePeriod_ms, long period_ms){
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable aux = new Runnable() {
            @Override
            public void run() {
                if(runnable.isCancelled()){
                    return;
                }

                runnable.run();

                if(!runnable.isCancelled()){
                    handler.postDelayed(this, period_ms);
                }
            }
        };
        handler.postDelayed(aux, prePeriod_ms);
    }
}
