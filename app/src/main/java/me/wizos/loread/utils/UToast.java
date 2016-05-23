package me.wizos.loread.utils;

import android.widget.Toast;

import me.wizos.loread.App;

/**
 * Created by xdsjs on 2015/11/27.
 */
public class UToast {
    public static Toast toast;
    public static void showLong(String msg) {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        toast = Toast.makeText(App.getContext(), msg, Toast.LENGTH_LONG);
        toast.show();
    }
    public static void showShort(String msg) {
        if (toast != null) {
            toast.cancel();
            toast = null;
        }
        toast = Toast.makeText(App.getContext(), msg, Toast.LENGTH_SHORT);
        toast.show();
    }
}
