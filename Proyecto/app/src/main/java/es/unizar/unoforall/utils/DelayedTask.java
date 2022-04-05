package es.unizar.unoforall.utils;

import android.os.Handler;
import android.os.Looper;

public class DelayedTask{
    public static void runDelayedTask(Runnable runnable, long milliseconds){
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(runnable, milliseconds);
    }
}
