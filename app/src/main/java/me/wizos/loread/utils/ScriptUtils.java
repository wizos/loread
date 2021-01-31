package me.wizos.loread.utils;


import com.elvishew.xlog.XLog;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


public class ScriptUtils {
    private static ScriptUtils instance;
    private static ScriptEngine engine;
    private ScriptUtils() { }

    public static synchronized ScriptUtils init() {
        if (instance == null) {
            synchronized (ScriptUtils.class) {
                if (instance == null) {
                    instance = new ScriptUtils();
                    engine =  new ScriptEngineManager().getEngineByName("rhino");
                }
            }
        }
        return instance;
    }

    public static ScriptUtils i(){
        if( instance == null ){
            init();
        }
        return instance;
    }

    /**
     *
     * @param js js代码
     * @param bindings 可理解为JS引擎内的上下文。往bindings中设置的Java对象，可在JS运行时获取。它有一个实现类，SimpleBindings，内部就是一个map。
     * @return 返回是否执行成功
     */
    public boolean eval(String js, Bindings bindings){
        try {
            engine.eval(js, bindings);
            return true;
        } catch (ScriptException e) {
            XLog.e("脚本执行错误1：" + e.getMessage() + "," +e.getFileName()  + ","+ e.getColumnNumber()  + "," + e.getLineNumber() );
            e.printStackTrace();
            return false;
        } catch (Exception e){
            XLog.e("脚本执行错误2：" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void exe(String js, Bindings bindings) throws Exception{
        engine.eval(js, bindings);
    }
}
