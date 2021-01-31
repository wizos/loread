package me.wizos.loread.log;


public class Console {
    private Listener listener;
    public Console(Listener listener) {
        this.listener = listener;
    }

    public void log(Object object){
        listener.log(object);
    }

    public interface Listener {
        void log(Object object);
    }
}
