package com.yhao.floatwindow.base;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.Surface;
import android.view.WindowManager;

import com.yhao.floatwindow.interfaces.LifecycleListener;
import com.yhao.floatwindow.interfaces.ResumedListener;
import com.yhao.floatwindow.util.ActivityCounter;

/**
 *
 * @author yhao
 * @date 17-12-1
 * 用于控制悬浮窗显示周期
 * 使用了三种方法针对返回桌面时隐藏悬浮按钮
 * 1.startCount计数，针对back到桌面可以及时隐藏
 * 2.resumeCount计时，针对一些只执行onPause不执行onStop的奇葩情况
 * 3.监听home键，从而及时隐藏
 */

public class FloatLifecycle extends BroadcastReceiver implements Application.ActivityLifecycleCallbacks {

    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private static final long delay = 300;
    private Handler mHandler;
    private Class[] activities;
    private boolean showFlag;
//    private int startCount;
//    private int resumeCount;
    private boolean appBackground;
    private LifecycleListener mLifecycleListener;
    private static ResumedListener sResumedListener;
    private static int num = 0;
    private WindowManager mWindowManager;

    public FloatLifecycle(Context applicationContext, boolean showFlag, Class[] activities, LifecycleListener lifecycleListener) {
        this.showFlag = showFlag;
        this.activities = activities;
        num++;
        mLifecycleListener = lifecycleListener;
        mHandler = new Handler();
        // FIXME: 2019/6/2 使用 ActivityLifecycleCallbacks 的方案是有瑕疵的，导致 FloatWindow 必须要在 App 的 onCreate 中初始化。如果是在 Activity 中 初始化，会导致根据当前计数的resume/stop状态的Activity不准确。
        ((Application) applicationContext).registerActivityLifecycleCallbacks(this);
        mWindowManager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
        IntentFilter configChangeFilter = new IntentFilter();
        configChangeFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        configChangeFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        applicationContext.registerReceiver(this, configChangeFilter);
    }

    public static void setResumedListener(ResumedListener resumedListener) {
        sResumedListener = resumedListener;
    }

    private boolean needShow(Activity activity) {
        if (activities == null) {
            return true;
        }
        for (Class a : activities) {
            if (a.isInstance(activity)) {
                return showFlag;
            }
        }
        return !showFlag;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null && action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                mLifecycleListener.onBackToDesktop();
            }
        } else if (action != null && action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
            switch (mWindowManager.getDefaultDisplay().getRotation()) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    //竖屏
                    mLifecycleListener.onPortrait();
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    //横屏
                    mLifecycleListener.onLandscape();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (sResumedListener != null) {
            num--;
            if (num == 0) {
                sResumedListener.onResumed();
                sResumedListener = null;
            }
        }
//        resumeCount++;
        if (needShow(activity)) {
            mLifecycleListener.onShow();
        } else {
            mLifecycleListener.onHide();
        }

        if (appBackground) {
            appBackground = false;
        }
    }

    @Override
    public void onActivityPaused(final Activity activity) {
//        resumeCount--;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if( ActivityCounter.isOnBackground() ){
                    mLifecycleListener.onBackToDesktop();
                }
//                if (resumeCount == 0) {
//                    appBackground = true;
//                    LogUtil.e("接onActivityPaused：" );
//                    mLifecycleListener.onBackToDesktop();
//                }
            }
        }, delay);

    }

    @Override
    public void onActivityStarted(Activity activity) {
//        startCount++;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if( ActivityCounter.isOnBackground() ){
            mLifecycleListener.onBackToDesktop();
        }
//        startCount--;
//        if (startCount == 0) {
//            LogUtil.e("接onActivityStopped：" );
//            mLifecycleListener.onBackToDesktop();
//        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
