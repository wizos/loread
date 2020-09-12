package me.wizos.loread.view.slideback;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;

import java.lang.ref.WeakReference;

import me.wizos.loread.view.slideback.callback.SlideCallBack;
import me.wizos.loread.view.slideback.widget.SlideBackIconView;

import static me.wizos.loread.utils.ScreenUtil.dp2px;
import static me.wizos.loread.view.slideback.SlideBack.EDGE_BOTH;
import static me.wizos.loread.view.slideback.SlideBack.EDGE_LEFT;
import static me.wizos.loread.view.slideback.SlideBack.EDGE_RIGHT;


/**
 * @author ditclear on 16/7/12. 可滑动的layout extends FrameLayout
 * https://github.com/ditclear/TimeLine/blob/master/swipelayout/src/main/java/com/ditclear/swipelayout/SwipeDragLayout.java
 * 实现主页的左右滑动已读未读
 */
public class SlideLayout extends FrameLayout {
    private Context context;
    private int mScaledTouchSlop;
    private SlideBackIconView slideBackIconViewLeft;
    private SlideBackIconView slideBackIconViewRight;

    private SlideCallBack callBack;

    private float backViewHeight; // 控件高度
    private float arrowSize; // 箭头图标大小
    private int arrowColor; // 箭头图标大小
    private float maxSlideLength; // 最大拉动距离

    // FIXME: 2019/5/1
    private float leftViewTriggerStart; // 侧滑时开始响应的距离
    private float leftViewTriggerEnd;
    private float rightViewTriggerStart; // 侧滑时开始响应的距离
    private float rightViewTriggerEnd;

    private float sideSlideLength; // 侧滑响应距离
    private float dragRate; // 阻尼系数

    private boolean isAllowEdgeLeft; // 使用左侧侧滑
    private boolean isAllowEdgeRight; // 使用右侧侧滑


    public SlideLayout(Context context) {
        this(context, null);
    }

    public SlideLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        // 获取屏幕信息，初始化控件设置
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float screenWidth = dm.widthPixels;

        backViewHeight = dm.heightPixels / 4f; // 高度默认 屏高/4
        arrowSize = dp2px(5); // 箭头大小默认 5dp
        maxSlideLength = screenWidth / 12; // 最大宽度默认 屏宽/12

        sideSlideLength = maxSlideLength / 2; // 侧滑响应距离默认 控件最大宽度/2

        dragRate = 3; // 阻尼系数默认 3

        // 侧滑返回模式 默认:左
        isAllowEdgeLeft = true;
        isAllowEdgeRight = true;

        leftViewTriggerStart = sideSlideLength / 2;
        leftViewTriggerEnd = maxSlideLength * 2;
        rightViewTriggerStart = screenWidth - maxSlideLength * 2;
        rightViewTriggerEnd = screenWidth - sideSlideLength / 2;

        mAnimation = ValueAnimator.ofFloat(0f, 1f);
        mAnimation.setDuration(mDuration);
        mAnimation.setInterpolator(new DecelerateInterpolator());
        mAnimUpdateListener = new AnimUpdateListener(this);
        mAnimListenerAdapter = new AnimListenerAdapter(this);
    }


//    public SlideLayout setAllowEdgeLeft(boolean allowEdgeLeft) {
//        isAllowEdgeLeft = allowEdgeLeft;
//        slideBackIconViewLeft = new SlideBackIconView(context);
//        slideBackIconViewLeft.setBackViewHeight(backViewHeight);
//        slideBackIconViewLeft.setArrowSize(arrowSize);
//        slideBackIconViewLeft.setArrowColor(arrowColor);
//        slideBackIconViewLeft.setMaxSlideLength(maxSlideLength);
//        addView(slideBackIconViewLeft);
//        return this;
//    }
//
//    public SlideLayout setAllowEdgeRight(boolean allowEdgeRight) {
//        isAllowEdgeRight = allowEdgeRight;
//        slideBackIconViewRight = new SlideBackIconView(context);
//        slideBackIconViewRight.setBackViewHeight(backViewHeight);
//        slideBackIconViewRight.setArrowSize(arrowSize);
//        slideBackIconViewRight.setArrowColor(arrowColor);
//        slideBackIconViewRight.setMaxSlideLength(maxSlideLength);
//        // 右侧侧滑 需要旋转180°
//        slideBackIconViewRight.setRotationY(180);
//        addView(slideBackIconViewRight);
//        return this;
//    }

    /**
     * 回调 适用于新的左右模式
     */
    public SlideLayout callBack(SlideCallBack callBack) {
        this.callBack = callBack;
        return this;
    }


    /**
     * 控件高度 默认屏高/4
     */
    public SlideLayout viewHeight(float backViewHeightDP) {
        this.backViewHeight = dp2px(backViewHeightDP);
        return this;
    }

    /**
     * 箭头大小 默认5dp
     */
    public SlideLayout arrowSize(float arrowSizeDP) {
        this.arrowSize = dp2px(arrowSizeDP);
        return this;
    }

    /**
     * 箭头颜色
     */
    public SlideLayout arrowColor(@ColorInt int arrowColor) {
        this.arrowColor = arrowColor;
        return this;
    }


    /**
     * 最大拉动距离（控件最大宽度） 默认屏宽/12
     */
    public SlideLayout maxSlideLength(float maxSlideLengthDP) {
        this.maxSlideLength = dp2px(maxSlideLengthDP);
        return this;
    }

    /**
     * 侧滑响应距离 默认控件最大宽度/2
     */
    public SlideLayout sideSlideLength(float sideSlideLengthDP) {
        this.sideSlideLength = dp2px(sideSlideLengthDP);
        return this;
    }

    /**
     * 阻尼系数 默认3（越小越灵敏）
     */
    public SlideLayout dragRate(float dragRate) {
        this.dragRate = dragRate;
        return this;
    }

    /**
     * 边缘侧滑模式 默认左
     */
    public SlideLayout edgeMode(@SlideBack.EdgeMode int edgeMode) {
        switch (edgeMode) {
            case EDGE_LEFT:
                isAllowEdgeLeft = true;
                isAllowEdgeRight = false;
                break;
            case EDGE_RIGHT:
                isAllowEdgeLeft = false;
                isAllowEdgeRight = true;
                break;
            case EDGE_BOTH:
                isAllowEdgeLeft = true;
                isAllowEdgeRight = true;
                break;
            default:
                throw new RuntimeException("未定义的边缘侧滑模式值：EdgeMode = " + edgeMode);
        }
        return this;
    }


    /**
     * 需要使用滑动的页面注册
     */
    @SuppressLint("ClickableViewAccessibility")
    public void register() {
        if (isAllowEdgeLeft) {
            // 初始化SlideBackIconView 左侧
            slideBackIconViewLeft = new SlideBackIconView(context);
            slideBackIconViewLeft.setBackViewHeight(backViewHeight);
            slideBackIconViewLeft.setArrowSize(arrowSize);
            slideBackIconViewLeft.setArrowColor(arrowColor);
            slideBackIconViewLeft.setMaxSlideLength(maxSlideLength);
            addView(slideBackIconViewLeft);
        }
        if (isAllowEdgeRight) {
            // 初始化SlideBackIconView - Right
            slideBackIconViewRight = new SlideBackIconView(context);
            slideBackIconViewRight.setBackViewHeight(backViewHeight);
            slideBackIconViewRight.setArrowSize(arrowSize);
            slideBackIconViewRight.setArrowColor(arrowColor);
            slideBackIconViewRight.setMaxSlideLength(maxSlideLength);
            // 右侧侧滑 需要旋转180°
            slideBackIconViewRight.setRotationY(180);
            addView(slideBackIconViewRight);
        }
        //KLog.e(" 是否要添加箭头：" + isAllowEdgeLeft + " , " + isAllowEdgeRight);
    }


    private boolean isSideSlideLeft = false;  // 是否从左边边缘开始滑动
    private boolean isSideSlideRight = false;  // 是否从右边边缘开始滑动
    private float moveXLength = 0; // 位移的X轴距离

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: // 按下
                break;
            case MotionEvent.ACTION_MOVE: // 移动
                //KLog.e("响应手势，移动：" + isSideSlideLeft + " , "  +  isSideSlideRight  + " , " + isAllowEdgeLeft + " , " + isAllowEdgeRight);
                if (isSideSlideLeft || isSideSlideRight) {
                    // 从边缘开始滑动
                    // 获取X轴位移距离
                    moveXLength = Math.abs(ev.getRawX() - mDownX);
                    //KLog.e("响应手势，移动B：" + moveXLength+ " ,  " +  mDownX );
                    if (moveXLength / dragRate <= maxSlideLength) {
                        // 如果位移距离在可拉动距离内，更新SlideBackIconView的当前拉动距离并重绘，区分左右
                        if (isAllowEdgeLeft && isSideSlideLeft) {
                            slideBackIconViewLeft.updateSlideLength(moveXLength / dragRate);
                            //KLog.e("响应手势，移动B：" + slideBackIconViewLeft.getHeight()+ " ,  " +  slideBackIconViewLeft.getVisibility() );
                            callBack.onViewSlide(EDGE_LEFT, (int) (moveXLength / dragRate));
                        } else if (isAllowEdgeRight && isSideSlideRight) {
                            slideBackIconViewRight.updateSlideLength(moveXLength / dragRate);
                            callBack.onViewSlide(EDGE_RIGHT, (int) (moveXLength / dragRate));
                        }
                    }

                    // 根据Y轴位置给SlideBackIconView定位
                    if (isAllowEdgeLeft && isSideSlideLeft) {
                        setSlideBackPosition(slideBackIconViewLeft, (int) (ev.getY()));
                    } else if (isAllowEdgeRight && isSideSlideRight) {
                        setSlideBackPosition(slideBackIconViewRight, (int) (ev.getY()));
                    }
                }
                break;
            case MotionEvent.ACTION_UP: // 抬起
            case MotionEvent.ACTION_CANCEL:
                // 是从边缘开始滑动 且 抬起点的X轴坐标大于某值(默认3倍最大滑动长度) 且 回调不为空
                if ((isSideSlideLeft || isSideSlideRight) && moveXLength / dragRate >= maxSlideLength && null != callBack) {
                    // 区分左右
                    callBack.onSlide(isSideSlideLeft ? EDGE_LEFT : EDGE_RIGHT);
                }

                // 恢复SlideBackIconView的状态
                if (isAllowEdgeLeft && isSideSlideLeft) {
                    slideBackIconViewLeft.updateSlideLength(0);
                    callBack.onViewSlide(EDGE_LEFT, 0);
                } else if (isAllowEdgeRight && isSideSlideRight) {
                    slideBackIconViewRight.updateSlideLength(0);
                    callBack.onViewSlide(EDGE_RIGHT, 0);
                }

                // 从边缘开始滑动结束
                isSideSlideLeft = false;
                isSideSlideRight = false;
                break;
            default:
                break;
        }
        return isSideSlideLeft || isSideSlideRight;
    }

    private float mDownX = 0; // 按下的X轴坐标
    private float mDownY = 0; // 按下的X轴坐标

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: // 按下
                // 更新按下点的X轴坐标
                mDownX = ev.getRawX();
                mDownY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float offsetX = Math.abs(ev.getRawX() - mDownX);
                float offsetY = Math.abs(ev.getRawY() - mDownY);
                if (!(offsetX > mScaledTouchSlop * 2 && offsetY < mScaledTouchSlop)) {
                    break;
                }
                if (isAllowEdgeLeft && (mDownX >= leftViewTriggerStart && mDownX <= leftViewTriggerEnd)) {
                    isSideSlideLeft = true;
                    return true;
                } else if (isAllowEdgeRight && (mDownX >= rightViewTriggerStart && mDownX <= rightViewTriggerEnd)) {
                    isSideSlideRight = true;
                    return true;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 给SlideBackIconView设置topMargin，起到定位效果
     *
     * @param view     SlideBackIconView
     * @param position 触点位置
     */
    private void setSlideBackPosition(SlideBackIconView view, int position) {
        // 触点位置减去SlideBackIconView一半高度即为topMargin
        int topMargin = (int) (position - (view.getBackViewHeight() / 2));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(view.getLayoutParams());
        layoutParams.topMargin = topMargin;
        view.setLayoutParams(layoutParams);
    }


    private ValueAnimator mAnimation;
    private AnimUpdateListener mAnimUpdateListener;
    private AnimListenerAdapter mAnimListenerAdapter;
    private float mFactor; // 进度因子:0-1
    private boolean mIsRunning;
    private int mCurX, mCurY, mDst;
    private int mDuration = 250;
    private float mDampFactor = 0.6f; // 滑动阻尼系数

    static class AnimListenerAdapter extends AnimatorListenerAdapter {
        private final WeakReference<SlideLayout> reference;

        AnimListenerAdapter(SlideLayout view) {
            this.reference = new WeakReference<>(view);
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            if (isFinish()) {
                return;
            }
            SlideLayout view = reference.get();
            view.mFactor = 1;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (isFinish()) {
                return;
            }
            SlideLayout view = reference.get();
            view.mFactor = 1;
        }

        private boolean isFinish() {
            SlideLayout view = reference.get();
            if (view == null || view.getContext() == null
                    || view.getContext() instanceof Activity && ((Activity) view.getContext()).isFinishing()
                    || !view.mIsRunning) {
                return true;
            }
            return false;
        }
    }

    static class AnimUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private final WeakReference<SlideLayout> reference;

        AnimUpdateListener(SlideLayout view) {
            this.reference = new WeakReference<>(view);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            if (isFinish()) {
                return;
            }
            SlideLayout view = reference.get();
            view.mFactor = (float) animation.getAnimatedValue();
            if (view.mDst == -1) {
                float scrollY = view.mCurY - view.mCurY * view.mFactor;
                view.scrollTo(0, (int) scrollY);
            } else if (view.mDst == 1) {
                float scrollX = view.mCurX - view.mCurX * view.mFactor;
                view.scrollTo((int) scrollX, 0);
            }
            view.invalidate();
        }

        private boolean isFinish() {
            SlideLayout view = reference.get();
            if (view == null || view.getContext() == null
                    || view.getContext() instanceof Activity && ((Activity) view.getContext()).isFinishing()
                    || !view.mIsRunning) {
                return true;
            }
            return false;
        }
    }


}
