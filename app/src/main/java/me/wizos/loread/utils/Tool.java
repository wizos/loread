package me.wizos.loread.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import com.elvishew.xlog.XLog;
import com.hjq.toast.ToastUtils;

import java.io.IOException;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.BuildConfig;
import me.wizos.loread.R;
import okhttp3.RequestBody;
import okio.Buffer;

/**
 * 一些比较杂的工具函数
 * Created by Wizos on 2016/11/1.
 */

public class Tool {

    public static void show(String msg) {
        if (BuildConfig.DEBUG) {
            XLog.e(msg);
            ToastUtils.show(msg);
        }
    }

    public static void printCallStack() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        Throwable ex = new Throwable();
        StackTraceElement[] stackElements = ex.getStackTrace();
        XLog.e("-----------------------------------");
        for (StackTraceElement stackElement : stackElements) {
            XLog.e(stackElement.getClassName() + "_" + stackElement.getFileName() + "_" + stackElement.getLineNumber() + "_" + stackElement.getMethodName());
        }
        XLog.e("-----------------------------------");
    }

    public static void printCallStack(Throwable e) {
        XLog.e(e.getMessage());
        StackTraceElement[] stackElements = e.getStackTrace();
        for (StackTraceElement stackElement : stackElements) {
            XLog.e(stackElement.getClassName() + "_" + stackElement.getFileName() + "_" + stackElement.getLineNumber() + "_" + stackElement.getMethodName());
        }
    }
    public static void printCallStack(Exception e) {
        if(BuildConfig.DEBUG)return;
        XLog.e(e.getMessage());
        StackTraceElement[] stackElements = e.getStackTrace();
        for (StackTraceElement stackElement : stackElements) {
            XLog.e(stackElement.getClassName() + "_" + stackElement.getFileName() + "_" + stackElement.getLineNumber() + "_" + stackElement.getMethodName());
        }
    }
    public static void setBackgroundColor(View object) {
        if (App.i().getUser().getThemeMode() == App.THEME_NIGHT) {
            object.setBackgroundColor(App.i().getResources().getColor(R.color.dark_background));
        } else {
            object.setBackgroundColor(App.i().getResources().getColor(R.color.white));
        }
    }


    /**
     * 包名判断是否为主进程
     *
     * @param context
     * @return
     */
    public static boolean isMainProcess(Context context) {
        return context.getPackageName().equals(getProcessName(context));
    }

    /**
     * 获取进程名称
     *
     * @param context
     * @return
     */
    public static String getProcessName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
            if (proInfo.pid == android.os.Process.myPid()) {
                if (proInfo.processName != null) {
                    return proInfo.processName;
                }
            }
        }
        return null;
    }

    /**
     * 判断某个Activity 界面是否在前台
     *
     * @param context
     * @param className 某个界面名称
     * @return
     */
    public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAppForeground(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return false;
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();

        if (appProcesses == null)
            return false;
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName) && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    public static String bodyToString(final RequestBody request){
        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            if(copy != null)
                copy.writeTo(buffer);
            else
                return "";
            return buffer.readUtf8();
        }
        catch (final IOException e) {
            return "did not work";
        }
    }
}
