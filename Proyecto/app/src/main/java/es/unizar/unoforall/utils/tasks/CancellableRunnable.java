package es.unizar.unoforall.utils.tasks;

public abstract class CancellableRunnable implements Runnable{
    private boolean cancelled;

    public CancellableRunnable(){
        this.cancelled = false;
    }

    public void cancel(){
        this.cancelled = true;
    }

    public boolean isCancelled(){
        return  this.cancelled;
    }
}