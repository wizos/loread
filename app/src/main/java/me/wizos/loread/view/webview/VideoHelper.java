/*
 * Copyright (C)  Justson(https://github.com/Justson/AgentWeb)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.wizos.loread.view.webview;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.core.util.Pair;

import java.util.HashSet;
import java.util.Set;

/**
 * https://www.jianshu.com/p/ed01d00809f4
 *
 * @author cenxiaozhong
 */
public class VideoHelper {
    private Activity mActivity;
    private WebView mWebView;
    private Set<Pair<Integer, Integer>> mFlags;
    private View videoView = null;
    private FrameLayout videoParentView = null;
    private WebChromeClient.CustomViewCallback mCallback;

    private boolean isFullScreen = false;
    // 全屏之前，是否是竖屏
    private boolean oldActivityIsPortrait;
    // 全屏时，是否是竖屏
    private boolean isPortrait;
    // 当前是翻转的竖屏
    private boolean isReversePortrait;
    // 当前是翻转的横屏
    private boolean isReverseLandscape;
    private SensorManager sm;
    private OrientationSensorListener sensorListener;

    public VideoHelper(Activity mActivity, WebView webView) {
        this.mActivity = mActivity;
        this.mWebView = webView;
        this.mFlags = new HashSet<>();
        // initSensor();
    }

    // https://github.com/pingerx/AndroidSample/blob/master/sample/src/main/java/com/pinger/sample/screenrotate/ScreenRotateUtils.kt
    // 初始化重力感引器
    private void initSensor(){
        // XLog.d("初始化重力感应器");
        // 获取传感器管理器
        sm = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
        // 获取传感器类型
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorListener = new OrientationSensorListener();
        // 初始化监听器
        sm.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_UI);
    }

    public void onDestroy(){
        if(sm != null && sensorListener != null){
            sm.unregisterListener(sensorListener);
            sm = null;
            sensorListener = null;
        }
    }

    private class OrientationSensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] values = event.values;
            long orientation = -1L;
            int DATA_X = 0;
            float x = -values[DATA_X];
            int DATA_Y = 1;
            float y = -values[DATA_Y];
            int DATA_Z = 2;
            float z = -values[DATA_Z];
            float magnitude = x * x + y * y;
            // Don't trust the angle if the magnitude is small compared to the y
            // value
            if (magnitude * 4 >= z * z) {
                // 屏幕旋转时
                orientation = 90 - Math.round(Math.atan2(-y, x) * 57.29577957855f);
                // normalize to 0 - 359 range
                while (orientation >= 360) {
                    orientation -= 360;
                }
                while (orientation < 0) {
                    orientation += 360;
                }
            }
            // XLog.d("全屏：" + isFullScreen + "，重力感应：" + orientation + " , 是否竖屏：" + isPortrait  + " = 竖屏翻转：" + isReversePortrait + " , 横屏翻转：" + isReverseLandscape);

            // 只有点了按钮时才需要根据当前的状态来更新状态
            if (isFullScreen) {
                /*
                 * 根据手机屏幕的朝向角度，来设置内容的横竖屏，并且记录状态
                 */
                if (orientation >= 46 && orientation <= 135) {
                    if(!isReverseLandscape && !isPortrait){
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                        isReverseLandscape = true;
                    }
                } else if (orientation >= 136 && orientation <= 225) {
                    if(!isReversePortrait && isPortrait){
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                        isReversePortrait = true;
                    }
                } else if (orientation >= 226 && orientation <= 315) {
                    if(isReverseLandscape && !isPortrait){
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        isReverseLandscape = false;
                    }
                } else if ((orientation >= 316 && orientation <= 360) || (orientation >= 1 && orientation <= 45)) {
                    if(isReversePortrait && isPortrait){
                        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        isReversePortrait = false;
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    /**
     * @param view
     * @param isPortrait 是否为竖屏
     * @param callback
     */
    public void onShowCustomView(View view, boolean isPortrait, WebChromeClient.CustomViewCallback callback) {
        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }
        initSensor();

        this.isPortrait = isPortrait;
        // XLog.d("全屏B：" + isFullScreen + " , 是否竖屏：" + isPortrait  + " = 竖屏翻转：" + isReversePortrait + " , 横屏翻转：" + isReverseLandscape);

        oldActivityIsPortrait = (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        if(isPortrait && !oldActivityIsPortrait){
            if(isReversePortrait){
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
            }else {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }else if(!isPortrait && oldActivityIsPortrait){
            if(isReverseLandscape){
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
            }else {
                mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
        // switchFullScreen(mActivity);

        Window mWindow = mActivity.getWindow();
        Pair<Integer, Integer> mPair;
        // 保持屏幕常亮
        if ((mWindow.getAttributes().flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) == 0) {
            mPair = new Pair<>(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 0);
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mFlags.add(mPair);
        }
        // 开启Window级别的硬件加速
        if ((mWindow.getAttributes().flags & WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED) == 0) {
            mPair = new Pair<>(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, 0);
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            mFlags.add(mPair);
        }

        // KLog.e("设置" + mWebView  + "   "  + videoParentView);
        if (mWebView != null) {
            mWebView.setVisibility(View.GONE);
        }

        videoView = view;

        if (videoParentView == null) {
            FrameLayout mDecorView = (FrameLayout) mActivity.getWindow().getDecorView();
            videoParentView = new FrameLayout(mActivity);
            videoParentView.setBackgroundColor(Color.BLACK);
            videoParentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN); // 全屏
            mDecorView.addView(videoParentView);
        }
        if (videoView.getParent() != null) {
            ((ViewGroup)this.videoView.getParent()).removeView(videoView);
        }

        videoParentView.addView(videoView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        videoParentView.setVisibility(View.VISIBLE);
        mCallback = callback;
        isFullScreen = true;
    }

    public void onHideCustomView() {
        if (videoView == null || mActivity == null || mActivity.isFinishing()) {
            return;
        }

        // switchFullScreen(mActivity);
        if (oldActivityIsPortrait && mActivity.getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            // KLog.i("ToVmp","横屏");
        } else if(!oldActivityIsPortrait && mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ){
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            // KLog.i("ToVmp","竖屏");
        }

        if (!mFlags.isEmpty()) {
            for (Pair<Integer, Integer> mPair : mFlags) {
                // mActivity.getWindow().setFlags(mPair.second, mPair.first);
                mActivity.getWindow().setFlags(mPair.first, mPair.second);
            }
            mFlags.clear();
        }

        if (videoParentView != null) {
            videoParentView.removeView(videoView);
            // 状态栏和Activity共存，Activity不全屏显示。也就是应用平常的显示画面
            videoParentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            videoParentView.setVisibility(View.GONE);
        }

        if (mCallback != null) {
            mCallback.onCustomViewHidden();
        }

        // videoView.setVisibility(View.GONE);
        videoView = null;
        if (mWebView != null) {
            mWebView.setVisibility(View.VISIBLE);
        }
        isFullScreen = false;
        onDestroy();
    }
}
