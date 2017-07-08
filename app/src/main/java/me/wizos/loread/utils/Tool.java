package me.wizos.loread.utils;

import android.content.Context;
import android.view.View;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.data.WithSet;

/**
 * Created by Wizos on 2016/11/1.
 */

public class Tool {

    public Tool(Context context) {
    }

    public static void printCallStatck() {
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();
        if (stackElements != null) {
            for (int i = 0; i < stackElements.length; i++) {
                System.out.print(stackElements[i].getClassName() + "_");
                System.out.print(stackElements[i].getFileName() + "_");
                System.out.print(stackElements[i].getLineNumber() + "_");
                System.out.println(stackElements[i].getMethodName());
            }
            System.out.println("-----------------------------------");
        }
    }

    public static void setBackgroundColor(View object) {
        if (WithSet.i().getThemeMode() == App.theme_Night) {
            object.setBackgroundColor(App.i().getResources().getColor(R.color.article_dark_background));
        }
    }


}
