package me.wizos.loread.db;

import android.annotation.SuppressLint;

import com.tencent.mmkv.MMKV;

/**
 * @author Wizos
 * @date 2016/4/30
 * 内部设置
 */
public class CorePref {
    private static CorePref corePref;
    private static MMKV globalPref = MMKV.defaultMMKV();

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

}
