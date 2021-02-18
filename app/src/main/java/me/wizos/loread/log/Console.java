package me.wizos.loread.log;


import com.elvishew.xlog.XLog;

public class Console {
    private Listener listener;
    public Console() {

    }

    public void log(Object object){
        if(listener!=null){
            listener.onLog(object);
        }

        XLog.i(object);
    }

    public void v(Object object){
        XLog.v(object);
    }
    public void d(Object object){
        XLog.d(object);
    }
    public void i(Object object){
        XLog.i(object);
    }
    public void w(Object object){
        XLog.w(object);
    }
    public void e(Object object){
        XLog.e(object);
    }


    public Console setListener(Listener listener) {
        this.listener = listener;
        return this;
    }
    public interface Listener {
        void onLog(Object object);
    }
}
