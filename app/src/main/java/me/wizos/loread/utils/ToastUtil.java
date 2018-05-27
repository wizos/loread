package me.wizos.loread.utils;

import android.widget.Toast;

import com.socks.library.KLog;

import me.wizos.loread.App;

/**
 * Created by xdsjs on 2015/11/27.
 */
public class ToastUtil {
    public static Toast toast;
    public static void showLong(String msg) {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        toast = Toast.makeText(App.i(), msg, Toast.LENGTH_LONG);
        KLog.e(msg);
        toast.show();
    }
    public static void showShort(String msg) {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        toast = Toast.makeText(App.i(), msg, Toast.LENGTH_SHORT);
        KLog.e(msg);
        toast.show();
    }
}
