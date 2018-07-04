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
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

import java.util.HashSet;
import java.util.Set;

//import com.tencent.smtt.export.external.interfaces.IX5WebChromeClient;
//import com.tencent.smtt.sdk.WebView;

/**
 * @author cenxiaozhong
 */
public class VideoImpl { // implements IVideo, EventInterceptor
    private static final String TAG = VideoImpl.class.getSimpleName();
    private Activity mActivity;
    private WebView mWebView;
    private Set<Pair<Integer, Integer>> mFlags = null;
    private View videoView = null;
    private ViewGroup videoParentView = null;
    private WebChromeClient.CustomViewCallback mCallback;

    private boolean isPlaying = false;


    public VideoImpl(Activity mActivity, WebView webView) {
        this.mActivity = mActivity;
        this.mWebView = webView;
        mFlags = new HashSet<>();
    }


    //    @Override
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        Activity mActivity;
        if ((mActivity = this.mActivity) == null || mActivity.isFinishing()) {
            return;
        }
        // 横屏
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Window mWindow = mActivity.getWindow();
        Pair<Integer, Integer> mPair;
        // 保存当前屏幕的状态
        if ((mWindow.getAttributes().flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) == 0) {
            mPair = new Pair<>(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 0);
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            mFlags.add(mPair);
        }

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) && (mWindow.getAttributes().flags & WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED) == 0) {
            mPair = new Pair<>(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, 0);
            mWindow.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            mFlags.add(mPair);
        }


        if (videoView != null) {
            callback.onCustomViewHidden();
            return;
        }
//        KLog.e("设置" + mWebView  + "   "  + videoParentView);
        if (mWebView != null) {
            mWebView.setVisibility(View.GONE);
        }

        if (videoParentView == null) {
            FrameLayout mDecorView = (FrameLayout) mActivity.getWindow().getDecorView();
            videoParentView = new FrameLayout(mActivity);
            videoParentView.setBackgroundColor(Color.BLACK);
            videoParentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN); // 全屏
            mDecorView.addView(videoParentView);
        }

//        KLog.e("设置" + mWebView.getVisibility()  + "   "  + videoParentView);
        this.mCallback = callback;
        this.videoView = view;
        videoParentView.addView(videoView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        videoParentView.setVisibility(View.VISIBLE);
        isPlaying = true;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    //    @Override
    public void onHideCustomView() {
        if (videoView == null) {
            return;
        }
        if (mActivity != null && mActivity.getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (!mFlags.isEmpty()) {
            for (Pair<Integer, Integer> mPair : mFlags) {
                mActivity.getWindow().setFlags(mPair.second, mPair.first);
            }
            mFlags.clear();
        }

        videoView.setVisibility(View.GONE);
        if (videoParentView != null && videoView != null) {
            videoParentView.removeView(videoView);

        }
        if (videoParentView != null) {
            // 状态栏和Activity共存，Activity不全屏显示。也就是应用平常的显示画面
            videoParentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            videoParentView.setVisibility(View.GONE);
        }

        if (this.mCallback != null) {
            mCallback.onCustomViewHidden();
        }
        this.videoView = null;
        if (mWebView != null) {
            mWebView.setVisibility(View.VISIBLE);
        }
        isPlaying = false;
    }

}
