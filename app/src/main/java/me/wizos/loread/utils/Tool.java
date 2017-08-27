package me.wizos.loread.utils;

import android.view.View;

import com.socks.library.KLog;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.data.WithSet;

/**
 * 一些比较杂的工具函数
 * Created by Wizos on 2016/11/1.
 */

public class Tool {
    public static void printCallStatck() {
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();
        if (stackElements != null) {
            KLog.e("-----------------------------------");
            for (StackTraceElement stackElement : stackElements) {
                KLog.e(stackElement.getClassName() + "_" + stackElement.getFileName() + "_" + stackElement.getLineNumber() + "_" + stackElement.getMethodName());
            }
            KLog.e("-----------------------------------");
        }
    }

    public static void setBackgroundColor(View object) {
        if (WithSet.i().getThemeMode() == App.theme_Night) {
            object.setBackgroundColor(App.i().getResources().getColor(R.color.article_dark_background));
        }
    }


}
