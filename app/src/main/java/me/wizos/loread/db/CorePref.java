package me.wizos.loread.db;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.tencent.mmkv.MMKV;

import me.wizos.loread.App;
import me.wizos.loread.R;

/**
 * @author Wizos
 * @date 2016/4/30
 * 内部设置
 */
public class CorePref {
    private static final String TAG = "CorePref";
    private static CorePref corePref;
    private static SharedPreferences mySharedPreferences;
    private static SharedPreferences.Editor editor;

    private static MMKV globalPref = MMKV.defaultMMKV();
    private static MMKV userPref;

    // 迁移旧数据
    {
        SharedPreferences old_man = App.i().getSharedPreferences(App.i().getString(R.string.app_id), Activity.MODE_PRIVATE);
        globalPref.importFromSharedPreferences(old_man);
        old_man.edit().clear().commit();
    }

    private CorePref() {}

    @SuppressLint("CommitPrefEdits")
    public static CorePref i() {
        // 双重锁定，只有在 mySharedPreferences 还没被初始化的时候才会进入到下一行，然后加上同步锁
        if (corePref == null) {
            // 同步锁，避免多线程时可能 new 出两个实例的情况
            synchronized (CorePref.class) {
                if (corePref == null) {
                    corePref = new CorePref();
                }
            }
        }
        return corePref;
    }
    public MMKV globalPref(){
        return globalPref;
    }
    public MMKV userPref(){
        return userPref;
    }
    public static void init(Context context){
        MMKV.initialize(context);
    }

    public MMKV initUserPref(String userId){
        userPref = MMKV.mmkvWithID(userId);
        return userPref;
    }
}
