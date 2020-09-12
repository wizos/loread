package com.yhao.floatwindow.util;

import android.util.Log;


/**
 * Created by yhao on 2017/12/29.
 * https://github.com/yhaolpz
 */

public class LogUtil {

    private static final String TAG = "FloatWindow";

    public static void e(String message) {
        Log.e(TAG, message);
    }

    public static void d(String message) {
        Log.d(TAG, message);
    }
}
