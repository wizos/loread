package me.wizos.loread.db;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;

import me.wizos.loread.App;
import me.wizos.loread.R;

/**
 * @author Wizos
 * @date 2016/4/30
 * 内部设置
 */
public class CorePref {
    private static final String TAG = "CorePref";
    private static CorePref coreSharedPreferences;
    private static SharedPreferences mySharedPreferences;
    private static SharedPreferences.Editor editor;
    private CorePref() {
    }

    @SuppressLint("CommitPrefEdits")
    public static CorePref i() {
        // 双重锁定，只有在 mySharedPreferences 还没被初始化的时候才会进入到下一行，然后加上同步锁
        if (coreSharedPreferences == null) {
            // 同步锁，避免多线程时可能 new 出两个实例的情况
            synchronized (CorePref.class) {
                if (coreSharedPreferences == null) {
                    coreSharedPreferences = new CorePref();
                    mySharedPreferences = App.i().getSharedPreferences(App.i().getString(R.string.app_id), Activity.MODE_PRIVATE);
                    editor = mySharedPreferences.edit();
                }
            }
        }
        return coreSharedPreferences;
    }

    public String getString(String key, String value) {
        return mySharedPreferences.getString(key, value);
    }
    public void putString(String key, String value){
        editor.putString(key, value); //用putString的方法保存数据
        editor.commit(); //提交当前数据
    }
    public void remove(String key){
        editor.remove(key).commit();
    }
}
