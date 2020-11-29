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
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.core.util.Pair;

import com.socks.library.KLog;

import java.util.HashSet;
import java.util.Set;

/**
 * https://www.jianshu.com/p/ed01d00809f4
 *
 * @author cenxiaozhong
 */
public class VideoImpl { // implements IVideo, EventInterceptor
    private static final String TAG = VideoImpl.class.getSimpleName();
    private Activity mActivity;
    private WebView mWebView;
    private Set<Pair<Integer, Integer>> mFlags = null;
    private View videoView = null;
    private FrameLayout videoParentView = null;
    private WebChromeClient.CustomViewCallback mCallback;

    private boolean isFullScreen = false;

    public VideoImpl(Activity mActivity, WebView webView) {
        this.mActivity = mActivity;
        this.mWebView = webView;
        this.mFlags = new HashSet<>();
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    private void switchFullScreen(Activity mActivity) {
        if (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            KLog.i("ToVmp","横屏");
        } else {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            KLog.i("ToVmp","竖屏");
        }
    }

    FrameLayout frameLayout;
    private boolean oldActivityIsPortrait;

    public void onShowCustomView(View view, boolean isPortrait, WebChromeClient.CustomViewCallback callback) {
        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }

        oldActivityIsPortrait = (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
        if(isPortrait && !oldActivityIsPortrait){
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else if(!isPortrait && oldActivityIsPortrait){
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        // switchFullScreen(mActivity);

        Window mWindow = mActivity.getWindow();
        Pair<Integer, Integer> mPair;
        // 保存当前屏幕的状态
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
        videoParentView.addView(videoView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        videoParentView.setVisibility(View.VISIBLE);

        frameLayout = (FrameLayout) view;
        View video = frameLayout.getFocusedChild();
        KLog.i("i 宽度：" + view.getWidth() + " ，高度：" + view.getHeight());
        KLog.i("k 宽度：" + video.getWidth() + " ，高度：" + video.getHeight());


        // KLog.e("设置" + mWebView.getVisibility()  + "   "  + videoParentView);
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
                mActivity.getWindow().setFlags(mPair.second, mPair.first);
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
    }
}
