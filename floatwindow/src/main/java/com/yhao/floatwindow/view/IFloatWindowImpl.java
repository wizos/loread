package com.yhao.floatwindow.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;

import com.yhao.floatwindow.base.FloatLifecycle;
import com.yhao.floatwindow.constant.MoveType;
import com.yhao.floatwindow.constant.Screen;
import com.yhao.floatwindow.interfaces.FloatView;
import com.yhao.floatwindow.interfaces.IFloatWindow;
import com.yhao.floatwindow.interfaces.LifecycleListener;
import com.yhao.floatwindow.util.DensityUtil;
import com.yhao.floatwindow.util.LogUtil;

/**
 * @author yhao
 * @date 2017/12/22
 * https://github.com/yhaolpz
 */

public class IFloatWindowImpl implements IFloatWindow, LifecycleListener {

    private FloatWindow.BuildFloatWindow mBuildFloatWindow;
    private FloatView mFloatView;
    private ValueAnimator mAnimator;
    private TimeInterpolator mDecelerateInterpolator;
    private float downX, downY, upX, upY;
    private int mSlop;
    private int screenWidth;
    private int screenHeight;
    private boolean isShow = true;
    private boolean mClick;
    private boolean isHideByUser;
    private boolean isLandscape;
    private int statusBarHeight;

    IFloatWindowImpl(final FloatWindow.BuildFloatWindow buildFloatWindow, Boolean childViewTouchable) {
        mBuildFloatWindow = buildFloatWindow;

        int baseWidth = DensityUtil.getScreenWidth(mBuildFloatWindow.mApplicationContext);
        int baseHeight = DensityUtil.getScreenHeight(mBuildFloatWindow.mApplicationContext);
        statusBarHeight = DensityUtil.getStatusBarHeight(mBuildFloatWindow.mApplicationContext);
        // LogUtil.e("状态栏高度：" + statusBarHeight);
        screenWidth = baseWidth > baseHeight ? baseHeight : baseWidth;
        screenHeight = baseWidth > baseHeight ? baseWidth : baseHeight;
        isLandscape = baseWidth > baseHeight;

        mSlop = ViewConfiguration.get(mBuildFloatWindow.mApplicationContext).getScaledTouchSlop();
        if (mBuildFloatWindow.mMoveType == MoveType.fixed) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                mFloatView = new FloatPhone(buildFloatWindow.mApplicationContext, mBuildFloatWindow.mPermissionListener, childViewTouchable);
            } else {
                mFloatView = new FloatToast(buildFloatWindow.mApplicationContext, childViewTouchable);
            }
        } else {
            mFloatView = new FloatPhone(buildFloatWindow.mApplicationContext, mBuildFloatWindow.mPermissionListener, childViewTouchable);
            initTouchEvent();
        }

        mFloatView.setSize(mBuildFloatWindow.mWidth, mBuildFloatWindow.mHeight);
        mFloatView.setGravity(mBuildFloatWindow.gravity, mBuildFloatWindow.xOffset, mBuildFloatWindow.yOffset);
        mFloatView.setView(mBuildFloatWindow.mView);
        mFloatView.init();
        new FloatLifecycle(mBuildFloatWindow.mApplicationContext, mBuildFloatWindow.mShow, mBuildFloatWindow.mActivities, this);
    }

    @Override
    public void hideByUser() {
        if (!isShow) {
            return;
        }
        mBuildFloatWindow.mView.setVisibility(View.INVISIBLE);
        isShow = false;
        isHideByUser = true;
        if (mBuildFloatWindow.mViewStateListener != null) {
            mBuildFloatWindow.mViewStateListener.onHideByUser();
        }
    }

    @Override
    public void showByUser() {
        if (isShow) {
            return;
        }
        mBuildFloatWindow.mView.setVisibility(View.VISIBLE);
        isShow = true;
        isHideByUser = false;
        if (mBuildFloatWindow.mViewStateListener != null) {
            mBuildFloatWindow.mViewStateListener.onShowByUser();
        }
    }

    @Override
    public boolean isShowing() {
        return isShow;
    }

    /**
     *  这里不应该直接被使用者调用，因为 FloatWindow 内保存了所有悬浮窗信息，没有同步去掉
     */
    @Override
    public void dismiss() {
        mFloatView.dismiss();
        isShow = false;
        if (mBuildFloatWindow.mViewStateListener != null) {
            mBuildFloatWindow.mViewStateListener.onDismiss();
        }
    }

    @Override
    public void updateX(int x) {
        checkMoveType();
        mBuildFloatWindow.xOffset = x;
        mFloatView.updateX(x);
        LogUtil.e("设置updateX距离：" + x + " , "  );
    }

    @Override
    public void updateY(int y) {
        checkMoveType();
        mBuildFloatWindow.yOffset = y;
        mFloatView.updateY(y);
    }

    @Override
    public void updateX(int screenType, float ratio) {
        checkMoveType();
        mBuildFloatWindow.xOffset = (int) ((screenType == Screen.width ? screenWidth : screenHeight) * ratio);
        mFloatView.updateX(mBuildFloatWindow.xOffset);
        LogUtil.e("设置updateX距离22：" + mBuildFloatWindow.xOffset + " , "  );
    }

    @Override
    public void updateY(int screenType, float ratio) {
        checkMoveType();
        mBuildFloatWindow.yOffset = (int) ((screenType == Screen.width ? screenWidth : screenHeight) * ratio);
        mFloatView.updateY(mBuildFloatWindow.yOffset);
    }

    @Override
    public int getX() {
        return mFloatView.getX();
    }

    @Override
    public int getY() {
        return mFloatView.getY();
    }

    @Override
    public void onShow() {
        if (isShow || isHideByUser) {
            return;
        }
        mBuildFloatWindow.mView.setVisibility(View.VISIBLE);
        isShow = true;
        if (mBuildFloatWindow.mViewStateListener != null) {
            mBuildFloatWindow.mViewStateListener.onShow();
        }
    }

    @Override
    public void onHide() {
        if (!isShow || isHideByUser) {
            return;
        }
        mBuildFloatWindow.mView.setVisibility(View.INVISIBLE);
        isShow = false;
        if (mBuildFloatWindow.mViewStateListener != null) {
            mBuildFloatWindow.mViewStateListener.onHide();
        }
    }

    @Override
    public void onBackToDesktop() {
        if (!mBuildFloatWindow.mDesktopShow) {
            onHide();
        }
        if (mBuildFloatWindow.mViewStateListener != null) {
            mBuildFloatWindow.mViewStateListener.onBackToDesktop();
        }
    }

    @Override
    public void onPortrait() {
        double ratio = (double) getY() / screenWidth;
        int width = mBuildFloatWindow.getWidth();
        int x = getX() + mBuildFloatWindow.getWidth() / 2 < screenHeight / 2 ? + FloatWindow.mSlideLeftMargin : screenWidth - width - FloatWindow.mSlideRightMargin;
//        LogUtil.e("得到竖屏 x 位置：" + x );
        mFloatView.updateXY(x, (int) (ratio * screenHeight));
        isLandscape = false;
    }

    @Override
    public void onLandscape() {
        double ratio = (double) getY() / screenHeight;
        int width = mBuildFloatWindow.getWidth();
        int x = getX() + mBuildFloatWindow.getWidth() / 2 < screenWidth / 2 ? + FloatWindow.mSlideLeftMargin : screenHeight - width - FloatWindow.mSlideRightMargin;
//        LogUtil.e("得到横屏 x 位置：" + x );
        mFloatView.updateXY(x, (int) (ratio * screenWidth));
        isLandscape = true;
    }

    private void checkMoveType() {
        if (mBuildFloatWindow.mMoveType == MoveType.fixed) {
            throw new IllegalArgumentException("FloatWindow of this tag is not allowed to move!");
        }
    }

    private void initTouchEvent() {
        switch (mBuildFloatWindow.mMoveType) {
            case MoveType.inactive:
                break;
            default:
                mBuildFloatWindow.mView.setOnTouchListener(new View.OnTouchListener() {
                    float lastX, lastY, changeX, changeY;
                    int newX, newY;

                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                downX = event.getRawX();
                                downY = event.getRawY();
                                lastX = event.getRawX();
                                lastY = event.getRawY();
                                cancelAnimator();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                changeX = event.getRawX() - lastX;
                                changeY = event.getRawY() - lastY;
                                newX = (int) (getX() + changeX);
                                newY = (int) (getY() + changeY);
                                mFloatView.updateXY(newX, newY);
                                if (mBuildFloatWindow.mViewStateListener != null) {
                                    mBuildFloatWindow.mViewStateListener.onPositionUpdate(newX, newY);
                                }
                                lastX = event.getRawX();
                                lastY = event.getRawY();
                                break;
                            case MotionEvent.ACTION_UP:
                                upX = event.getRawX();
                                upY = event.getRawY();
                                mClick = (Math.abs(upX - downX) > mSlop) || (Math.abs(upY - downY) > mSlop);
                                onActionUp();
                                return mClick;
                            default:
                                break;
                        }
                        return false;
                    }
                });
        }
    }

    private void onActionUp() {
        PropertyValuesHolder pvhX;
        PropertyValuesHolder pvhY;
        switch (mBuildFloatWindow.mMoveType) {
            case MoveType.slide:
                int[] a = new int[2];
                mBuildFloatWindow.mView.getLocationOnScreen(a);
                int startX = a[0];
                int endX;
                int startY = a[1];
                int endY;
                int width = mBuildFloatWindow.getWidth();
                int height = mBuildFloatWindow.getHeight();
                if (isLandscape) {
                    endX = startX + width / 2 < screenHeight / 2 ? FloatWindow.mSlideLeftMargin : screenHeight - width - FloatWindow.mSlideRightMargin;
                    //endY = startY + height / 2 < screenWidth / 2 ? FloatWindow.mSlideTopMargin : screenWidth - height - FloatWindow.mSlideBottomMargin;
                    endY = startY < FloatWindow.mSlideTopMargin ? FloatWindow.mSlideTopMargin : startY > screenWidth - height - FloatWindow.mSlideBottomMargin - statusBarHeight ? screenWidth - height - FloatWindow.mSlideBottomMargin - statusBarHeight : startY;
                } else {
                    endX = startX + width / 2 < screenWidth / 2 ? FloatWindow.mSlideLeftMargin : screenWidth - width - FloatWindow.mSlideRightMargin;
                    endY = startY < FloatWindow.mSlideTopMargin ? FloatWindow.mSlideTopMargin : startY > screenHeight - height - FloatWindow.mSlideBottomMargin - statusBarHeight ? screenHeight - height - FloatWindow.mSlideBottomMargin - statusBarHeight : startY;
                }
//                LogUtil.e("得到贴边距离：" + FloatWindow.mSlideLeftMargin + " , " + FloatWindow.mSlideRightMargin );
//                LogUtil.e("得到endX距离：" + mBuildFloatWindow.getWidth() + " , " + endX );
                if (startX == endX) {
                    return;
                }

//                mAnimator = ObjectAnimator.ofInt(startX, endX);
//                mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                    @Override
//                    public void onAnimationUpdate(ValueAnimator animation) {
//                        int x = (int) animation.getAnimatedValue();
//                        mFloatView.updateX(x);
//                        if (mBuildFloatWindow.mViewStateListener != null) {
//                            mBuildFloatWindow.mViewStateListener.onPositionUpdate(x, (int) upY);
//                        }
//                    }
//                });

                pvhX = PropertyValuesHolder.ofInt("x", startX, endX);
                pvhY = PropertyValuesHolder.ofInt("y", startY, endY);
                mAnimator = ObjectAnimator.ofPropertyValuesHolder(pvhX, pvhY);
                mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int x = (int) animation.getAnimatedValue("x");
                        int y = (int) animation.getAnimatedValue("y");
                        mFloatView.updateXY(x, y);
                        if (mBuildFloatWindow.mViewStateListener != null) {
                            mBuildFloatWindow.mViewStateListener.onPositionUpdate(x, y);
                        }
                    }
                });
                startAnimator();
                break;
            case MoveType.back:
                pvhX = PropertyValuesHolder.ofInt("x", getX(), mBuildFloatWindow.xOffset);
                pvhY = PropertyValuesHolder.ofInt("y", getY(), mBuildFloatWindow.yOffset);
                mAnimator = ObjectAnimator.ofPropertyValuesHolder(pvhX, pvhY);
                mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int x = (int) animation.getAnimatedValue("x");
                        int y = (int) animation.getAnimatedValue("y");
                        mFloatView.updateXY(x, y);
                        if (mBuildFloatWindow.mViewStateListener != null) {
                            mBuildFloatWindow.mViewStateListener.onPositionUpdate(x, y);
                        }
                    }
                });
                startAnimator();
                break;
            default:
                break;
        }
    }

    private void startAnimator() {
        if (mBuildFloatWindow.mInterpolator == null) {
            mBuildFloatWindow.mInterpolator = mDecelerateInterpolator == null ? mDecelerateInterpolator = new DecelerateInterpolator() : mDecelerateInterpolator;
        }
        mAnimator.setInterpolator(mBuildFloatWindow.mInterpolator);
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAnimator.removeAllUpdateListeners();
                mAnimator.removeAllListeners();
                mAnimator = null;
                if (mBuildFloatWindow.mViewStateListener != null) {
                    mBuildFloatWindow.mViewStateListener.onMoveAnimEnd();
                }
            }
        });
        mAnimator.setDuration(mBuildFloatWindow.mDuration).start();
        if (mBuildFloatWindow.mViewStateListener != null) {
            mBuildFloatWindow.mViewStateListener.onMoveAnimStart();
        }
    }

    private void cancelAnimator() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
    }
}
