package com.yhao.floatwindow.util;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by Wizos on 2019/6/2.
 */

public class ActivityCounter implements Application.ActivityLifecycleCallbacks {
    private static int startCount;
    private static int resumeCount;

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(final Activity activity) {
        resumeCount--;
    }

    @Override
    public void onActivityStarted(Activity activity) {
        startCount++;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        startCount--;
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

    public static boolean isOnBackground() {
        return( resumeCount==0 || startCount == 0);
    }
}
