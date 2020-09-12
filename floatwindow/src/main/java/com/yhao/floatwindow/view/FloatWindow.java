package com.yhao.floatwindow.view;

import android.animation.TimeInterpolator;
import android.app.Application;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yhao.floatwindow.constant.MoveType;
import com.yhao.floatwindow.constant.Screen;
import com.yhao.floatwindow.interfaces.IFloatWindow;
import com.yhao.floatwindow.interfaces.PermissionListener;
import com.yhao.floatwindow.interfaces.ViewStateListener;
import com.yhao.floatwindow.util.ActivityCounter;
import com.yhao.floatwindow.util.DensityUtil;
import com.yhao.floatwindow.util.LogUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yhao on 2017/12/22.
 * https://github.com/yhaolpz
 */

public class FloatWindow {
    public static int mSlideLeftMargin;
    public static int mSlideRightMargin;
    public static int mSlideTopMargin;
    public static int mSlideBottomMargin;

    private FloatWindow() {
    }
    // FIXME: 2019/06/02 监听 Activities 的生命周期，用于判断程序是否处于后台
    public static void init(Context applicationContext) {
        ((Application)applicationContext).registerActivityLifecycleCallbacks(new ActivityCounter());
    }

    private static final String mDefaultTag = "default_float_window_tag";
    private static Map<String, IFloatWindow> mFloatWindowMap;

    public static IFloatWindow get() {
        return get(mDefaultTag);
    }

    public static IFloatWindow get(@NonNull String tag) {
        return mFloatWindowMap == null ? null : mFloatWindowMap.get(tag);
    }

    @MainThread
    public static BuildFloatWindow with(@NonNull Context applicationContext) {
        return new BuildFloatWindow(applicationContext);
    }

    public static void destroy() {
        destroy(mDefaultTag);
    }

    public static void destroy(String tag) {
        if (mFloatWindowMap == null || !mFloatWindowMap.containsKey(tag)) {
            return;
        }
        mFloatWindowMap.get(tag).dismiss();
        mFloatWindowMap.remove(tag);
    }

    public static class BuildFloatWindow {
        Context mApplicationContext;
        View mView;
        private int mLayoutId;
        int mWidth = ViewGroup.LayoutParams.WRAP_CONTENT;
        int mHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        int gravity = Gravity.TOP | Gravity.START;
        int xOffset;
        int yOffset;
        boolean mShow = true;
        Class[] mActivities;
        int mMoveType = MoveType.slide;
        int screenWidth;
        int screenHeight;
        int slideMargin;
        long mDuration = 300;
        TimeInterpolator mInterpolator;
        private String mTag = mDefaultTag;
        boolean mDesktopShow;
        boolean mTouchable;
        PermissionListener mPermissionListener;
        ViewStateListener mViewStateListener;

        private BuildFloatWindow() {
        }

        BuildFloatWindow(Context applicationContext) {
            mApplicationContext = applicationContext;
            screenWidth = DensityUtil.getScreenWidth(mApplicationContext);
            screenHeight = DensityUtil.getScreenHeight(mApplicationContext);
        }

        public BuildFloatWindow setView(@NonNull View view) {
            mView = view;
            return this;
        }

        public BuildFloatWindow setView(@LayoutRes int layoutId) {
            mLayoutId = layoutId;
            return this;
        }

        public BuildFloatWindow setWidth(int width) {
            mWidth = DensityUtil.dip2px(mApplicationContext, width);
            slideMargin = width;
            return this;
        }

        public BuildFloatWindow setHeight(int height) {
            mHeight = DensityUtil.dip2px(mApplicationContext, height);
            return this;
        }

        public BuildFloatWindow setWidth(@Screen.screenType int screenType, float ratio) {
            mWidth = (int) ((screenType == Screen.width ? screenWidth : screenHeight) * ratio);
            //LogUtil.e("得到宽度：" + mWidth );
            return this;
        }

        public BuildFloatWindow setHeight(@Screen.screenType int screenType, float ratio) {
            mHeight = (int) ((screenType == Screen.width ? screenWidth : screenHeight) * ratio);
            return this;
        }

        public int getWidth() {
            return mWidth;
        }

        public int getHeight() {
            return mHeight;
        }

        public BuildFloatWindow setX(int x) {
            xOffset = x;
            return this;
        }

        public BuildFloatWindow setY(int y) {
            yOffset = y;
            return this;
        }

        public BuildFloatWindow setX(@Screen.screenType int screenType, float ratio) {
            xOffset = (int) ((screenType == Screen.width ? screenWidth : screenHeight) * ratio);
            return this;
        }

        public BuildFloatWindow setY(@Screen.screenType int screenType, float ratio) {
            yOffset = (int) ((screenType == Screen.width ? screenWidth : screenHeight) * ratio);
            return this;
        }

        /**
         * 设置 Activity 过滤器，用于指定在哪些界面显示悬浮窗，默认全部界面都显示
         *
         * @param show       　过滤类型,子类类型也会生效
         * @param activities 　过滤界面
         */
        public BuildFloatWindow setFilter(boolean show, @NonNull Class... activities) {
            mShow = show;
            mActivities = activities;
            return this;
        }

        public BuildFloatWindow setMoveType(@MoveType.MOVE_TYPE int moveType) {
            if (moveType == MoveType.slide) {
                // return setMoveType(moveType, -slideMargin / 2, -slideMargin / 2);
                return setMoveType(moveType, slideMargin / 2, slideMargin / 2, slideMargin / 2,slideMargin / 2);
            } else {
                // return setMoveType(moveType, 0, 0);
                return setMoveType(moveType, 0, 0,0,0);
            }
        }

        /**
         * 设置带边距的贴边动画，只有 moveType 为 MoveType.slide，设置边距才有意义，这个方法不标准，后面调整
         *
         * @param moveType         贴边动画 MoveType.slide
         * @param slideLeftMargin  贴边动画左边距，默认为 0
         * @param slideRightMargin 贴边动画右边距，默认为 0
         */
        public BuildFloatWindow setMoveType(@MoveType.MOVE_TYPE int moveType, int slideLeftMargin, int slideRightMargin) {
            mMoveType = moveType;
            mSlideLeftMargin = DensityUtil.dip2px(mApplicationContext, slideLeftMargin);
            mSlideRightMargin = DensityUtil.dip2px(mApplicationContext, slideRightMargin);
            //LogUtil.e("设置贴边距离：" + mSlideLeftMargin + " , " + mSlideRightMargin );
            return this;
        }

        public BuildFloatWindow setMoveType(@MoveType.MOVE_TYPE int moveType, int slideLeftMargin, int slideRightMargin, int slideTopMargin, int slideBottomMargin) {
            mMoveType = moveType;
            mSlideLeftMargin = DensityUtil.dip2px(mApplicationContext, slideLeftMargin);
            mSlideRightMargin = DensityUtil.dip2px(mApplicationContext, slideRightMargin);
            mSlideTopMargin = DensityUtil.dip2px(mApplicationContext, slideTopMargin);
            mSlideBottomMargin = DensityUtil.dip2px(mApplicationContext, slideBottomMargin);
            //LogUtil.e("设置贴边距离：" + mSlideLeftMargin + " , " + mSlideRightMargin );
            return this;
        }

        public BuildFloatWindow setMoveStyle(long duration, @Nullable TimeInterpolator interpolator) {
            mDuration = duration;
            mInterpolator = interpolator;
            return this;
        }

        public BuildFloatWindow setTag(@NonNull String tag) {
            mTag = tag;
            return this;
        }

        public BuildFloatWindow setDesktopShow(boolean show) {
            mDesktopShow = show;
            return this;
        }

        public BuildFloatWindow setPermissionListener(PermissionListener listener) {
            mPermissionListener = listener;
            return this;
        }

        public BuildFloatWindow setViewStateListener(ViewStateListener listener) {
            mViewStateListener = listener;
            return this;
        }

        public BuildFloatWindow setChildViewTouchable(Boolean touchable) {
            mTouchable = touchable;
            return this;
        }

        public void build() {
            if (mFloatWindowMap == null) {
                mFloatWindowMap = new HashMap<>(16);
            }
            if (mFloatWindowMap.containsKey(mTag)) {
                LogUtil.e("FloatWindow of this tag has been added, Please set a new tag for the new FloatWindow");
                return;
            }
            if (mView == null && mLayoutId == 0) {
                throw new IllegalArgumentException("View has not been set!");
            }
            if (mView == null) {
                mView = DensityUtil.inflate(mApplicationContext, mLayoutId);
            }
            IFloatWindow floatWindowImpl = new IFloatWindowImpl(this, mTouchable);
            mFloatWindowMap.put(mTag, floatWindowImpl);
        }
    }
}
