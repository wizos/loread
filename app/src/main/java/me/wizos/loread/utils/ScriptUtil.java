package me.wizos.loread.utils;


import com.elvishew.xlog.XLog;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


public class ScriptUtil {
    private static ScriptUtil instance;
    private static ScriptEngine engine;
    private ScriptUtil() { }

    public static synchronized ScriptUtil init() {
        if (instance == null) {
            synchronized (ScriptUtil.class) {
                if (instance == null) {
                    instance = new ScriptUtil();
                    engine =  new ScriptEngineManager().getEngineByName("rhino");
                }
            }
        }
        return instance;
    }

    public static ScriptUtil i(){
        if( instance == null ){
            init();
        }
        return instance;
    }

    public boolean eval(String js, Bindings bindings){
        try {
            engine.eval(js, bindings);
            return true;
        } catch (ScriptException e) {
            XLog.e("脚本执行错误" + e.getMessage() + "," +e.getFileName()  + ","+ e.getColumnNumber()  + "," + e.getLineNumber() );
            e.printStackTrace();
            return false;
        }
    }
}
