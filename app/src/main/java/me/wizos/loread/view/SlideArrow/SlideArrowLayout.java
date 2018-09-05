package me.wizos.loread.view.SlideArrow;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.socks.library.KLog;

import me.wizos.loread.R;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * @author Wizos on 2018/8/22.
 */

public class SlideArrowLayout extends FrameLayout {
    String TAG = "SwipeArrowLayout";

    public SlideArrowLayout(Context context) {
        this(context, null);
    }

    public SlideArrowLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideArrowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        onCreate();
    }

    boolean isLeftEage = false;//判断是否从左边缘划过来
    boolean isRightEage = false;//判断是否从左边缘划过来

    Activity activity;
    float shouldFinishPix = 0; // 结束取消拉伸
    public static float screenWidth = 0;
    float screenHeight = 0;
    int leftSlidingTriggerValue = 0;
    int rightSlidingTriggerValue = 0;

    // 滑动 触控区域
    int CANSLIDE_LENGTH = 56;//16

    View backView;
    View rightView;
    LeftSlideView leftSlideView;
    RightSlideView rightSlideView;
    WindowManager windowManager;
    WindowManager.LayoutParams leftLayoutParams;
    WindowManager.LayoutParams rightLayoutParams;

    float x;
    float y;
    float downX;

    protected void onCreate() {
        WindowManager manager = activity.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        screenWidth = outMetrics.widthPixels;
        screenHeight = outMetrics.heightPixels;
        shouldFinishPix = screenWidth / 3;

        //添加返回的View
        windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        leftLayoutParams = new WindowManager.LayoutParams();
        leftLayoutParams.width = dp2px(LeftSlideView.width);
        leftLayoutParams.height = 0;
        leftLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE; //设置window type,type是关键，这里的"2002" 表示系统级窗口，你也可以试试2003
        leftLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        leftLayoutParams.format = PixelFormat.RGBA_8888; // 设置图片格式，1  效果为背景透明
        leftLayoutParams.x = (int) (-screenWidth / 2);

        rightLayoutParams = new WindowManager.LayoutParams();
        rightLayoutParams.width = dp2px(RightSlideView.width);
        rightLayoutParams.height = 0;
        rightLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        rightLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        rightLayoutParams.format = PixelFormat.RGBA_8888;
        rightLayoutParams.x = (int) (screenWidth + screenWidth / 2);


        backView = LayoutInflater.from(activity).inflate(R.layout.left_slide_view, null);
        leftSlideView = backView.findViewById(R.id.slideBackView);


        rightView = LayoutInflater.from(activity).inflate(R.layout.right_slide_view, null);
        rightSlideView = rightView.findViewById(R.id.rightSlideView);

        leftSlidingTriggerValue = dp2px(CANSLIDE_LENGTH);
        rightSlidingTriggerValue = getResources().getDisplayMetrics().widthPixels - dp2px(CANSLIDE_LENGTH);

        mTouchSlop = ViewConfiguration.get(activity).getScaledPagingTouchSlop();
    }


    @Override
    public void addView(View view) {
        super.addView(view, 0);
    }

//    /**
//     * 作用是把触摸事件的分发方法，其返回值代表触摸事件是否被当前 View 处理完成(true/false)。
//     * @param ev
//     * @return
//     */
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
////        KLog.e("分配触摸事件：" + ev.getAction());
//        x = ev.getRawX();
//        y = ev.getRawY();
//        switch (ev.getAction()){
//            case MotionEvent.ACTION_DOWN:
////                downX = ev.getRawX();
//                KLog.e("按下位置：" + x + "  "  +  dp2px(CANSLIDE_LENGTH) );
//                if(x<=leftSlidingTriggerValue && slideListener.canSlideRight() ){
//                    isLeftEage = true;
//                    return onTouchEvent(ev);
//                }else if( x>=rightSlidingTriggerValue && slideListener.canSlideLeft() ){
//                    isRightEage = true;
//                    return onTouchEvent(ev);
//                }
//                break;
//
////            case MotionEvent.ACTION_MOVE:
////                float moveX = x - downX;
////                if(isLeftEage){
//////                    if(Math.abs(moveX)<=shouldFinishPix){
//////                        slideBackView.updateControlPoint(Math.abs(moveX)/2);
//////                    }
//////                    leftLayoutParams.y = (int) (ev.getRawY()-screenHeight/2);
//////                    windowManager.updateViewLayout(backView, leftLayoutParams);
////                }
////                break;
//            default:
//                break;
//        }
//        return super.dispatchTouchEvent(ev);
//    }


    private int mTouchSlop;
    private float mLastMotionX;
    private float mLastMotionY;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private int mActivePointerId = -1;
    private boolean mIsBeingDragged;
    private boolean mIsUnableToDrag;
//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        /*
//         * This method JUST determines whether we want to intercept the motion.
//         * If we return true, onMotionEvent will be called and we do the actual scrolling there.
//         * *这种方法只是确定我们是否要拦截运动。
//         * *如果返回true，将调用onTouchEvent，并在那里进行实际滚动。
//         */
//
//        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
//
//        // Always take care of the touch gesture being complete.
//        // 始终注意触摸手势是否完整。
//        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
//            // Release the drag.
//            KLog.e(TAG, "拦截完成");
////            resetTouch();
//            return false;
//        }
//
//        // Nothing more to do here if we have decided whether or not we are dragging.
//        // 如果我们已经决定是否要拖下去，这里就没什么可做的了。
//        if (action != MotionEvent.ACTION_DOWN) {
//            if (mIsBeingDragged) {
//                KLog.v(TAG, "Intercept returning true!");
//                return true;
//            }
//            if (mIsUnableToDrag) {
//                 KLog.v(TAG, "Intercept returning false!");
//                return false;
//            }
//        }
//
//        switch (action) {
//            case MotionEvent.ACTION_MOVE: {
//                /*
//                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
//                 * whether the user has moved far enough from his original down touch.
//                 * mIsBeingDragged == false，否则捷径会抓住它。检查用户是否已经离开他最初的向下触摸足够远。
//                 */
//
//                /*
//                * Locally do absolute value. mLastMotionY is set to the y value of the down event.
//                * 本地做绝对值。运动设置为向下事件的y值。
//                */
//                final int activePointerId = mActivePointerId;
//                if (activePointerId == -1) {
//                    // If we don't have a valid id, the touch down wasn't on content.
//                    // 如果我们没有有效的id，那么点击就不在内容上了。
//                    break;
//                }
//
//                final int pointerIndex = ev.findPointerIndex(activePointerId);
//                final float x = ev.getX(pointerIndex);
//                final float dx = x - mLastMotionX;
//                final float xDiff = Math.abs(dx);
//                final float y = ev.getY(pointerIndex);
//                final float yDiff = Math.abs(y - mInitialMotionY);
//                KLog.v(TAG, "Moved x to " + x + "," + y + " diff=" + xDiff + "," + yDiff);
//
//                //isGutterDrag是判断是否在两个页面之间的缝隙内移动
//                if (dx != 0 && !isGutterDrag(mLastMotionX, dx)
//                        && canScroll(this, false, (int) dx, (int) x, (int) y)) {
//                    // Nested view has scrollable area under this point. Let it be handled there.
//                    mLastMotionX = x;
//                    mLastMotionY = y;
//                    mIsUnableToDrag = true;
//                    return false;
//                }
//                if (xDiff > mTouchSlop && xDiff * 0.5f > yDiff) {
//                    if (DEBUG) Log.v(TAG, "Starting drag!");
//                    mIsBeingDragged = true;
//                    requestParentDisallowInterceptTouchEvent(true);
//                    setScrollState(SCROLL_STATE_DRAGGING);
//                    mLastMotionX = dx > 0
//                            ? mInitialMotionX + mTouchSlop : mInitialMotionX - mTouchSlop;
//                    mLastMotionY = y;
//                    setScrollingCacheEnabled(true);
//                } else if (yDiff > mTouchSlop) {
//                    // The finger has moved enough in the vertical
//                    // direction to be counted as a drag...  abort
//                    // any attempt to drag horizontally, to work correctly
//                    // with children that have scrolling containers.
//                    if (DEBUG) Log.v(TAG, "Starting unable to drag!");
//                    mIsUnableToDrag = true;
//                }
//                if (mIsBeingDragged) {
//                    // Scroll to follow the motion event
//                    if (performDrag(x)) {
//                        ViewCompat.postInvalidateOnAnimation(this);
//                    }
//                }
//                break;
//            }
//
//            case MotionEvent.ACTION_DOWN: {
//                /*
//                 * Remember location of down touch.
//                 * ACTION_DOWN always refers to pointer index 0.
//                 */
//                mLastMotionX = mInitialMotionX = ev.getX();
//                mLastMotionY = mInitialMotionY = ev.getY();
//                mActivePointerId = ev.getPointerId(0);
//                mIsUnableToDrag = false;
//
//                mIsScrollStarted = true;
//                mScroller.computeScrollOffset();
//                if (mScrollState == SCROLL_STATE_SETTLING
//                        && Math.abs(mScroller.getFinalX() - mScroller.getCurrX()) > mCloseEnough) {
//                    // Let the user 'catch' the pager as it animates.
//                    mScroller.abortAnimation();
//                    mPopulatePending = false;
//                    populate();
//                    mIsBeingDragged = true;
//                    requestParentDisallowInterceptTouchEvent(true);
//                    setScrollState(SCROLL_STATE_DRAGGING);
//                } else {
//                    completeScroll(false);
//                    mIsBeingDragged = false;
//                }
//
//                if (DEBUG) {
//                    Log.v(TAG, "Down at " + mLastMotionX + "," + mLastMotionY
//                            + " mIsBeingDragged=" + mIsBeingDragged
//                            + "mIsUnableToDrag=" + mIsUnableToDrag);
//                }
//                break;
//            }
//
//            case MotionEvent.ACTION_POINTER_UP:
//                onSecondaryPointerUp(ev);
//                break;
//            default:
//                break;
//        }
//
//        if (mVelocityTracker == null) {
//            mVelocityTracker = VelocityTracker.obtain();
//        }
//        mVelocityTracker.addMovement(ev);
//
//        /*
//         * The only time we want to intercept motion events is if we are in the
//         * drag mode.
//         */
//        return mIsBeingDragged;
//    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        KLog.e("拦截事件");
        // 如果我们已经决定是否要拖下去，这里就没什么可做的了。
        if (ev.getAction() != MotionEvent.ACTION_DOWN) {
            if (isLeftEage || isRightEage) {
                KLog.v(TAG, "Intercept returning true!");
                return true;
            }
        }
        x = ev.getRawX();
        y = ev.getRawY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
//                KLog.e("移动位置：" + x + "  " + dp2px(CANSLIDE_LENGTH));
                if (downX <= leftSlidingTriggerValue && (ev.getRawX() - downX) > mTouchSlop && slideListener.canSlideRight()) {
                    isLeftEage = true;
                    leftLayoutParams.height = dp2px(LeftSlideView.height);
                    leftLayoutParams.y = (int) (ev.getRawY() - screenHeight / 2);
                    windowManager.addView(backView, leftLayoutParams);
                    return true;
                } else if (downX >= rightSlidingTriggerValue && (downX - ev.getRawX()) > mTouchSlop && slideListener.canSlideLeft()) {
                    isRightEage = true;
                    rightLayoutParams.height = dp2px(RightSlideView.height);
                    rightLayoutParams.y = (int) (ev.getRawY() - screenHeight / 2);
                    windowManager.addView(rightView, rightLayoutParams);
                    return true;
                }
                break;
            default:
                break;
        }

        return onInterceptHoverEvent(ev);
    }
    /**
     *
     * 1、无论是对于 View 还是 ViewGroup来说，一个 触摸事件(MotionEvent 对象) 只要能传递给这个 View/ViewGroup ，
     * 那么这个 View/ViewGroup 的 dispatchTouchEvent(MotionEvent event) 就一定会被调用

     2、如果一个 View/ViewGroup 的 onTouchEvent(MotionEvent event) 方法被调用，
     那么其返回值代表这个 View/ViewGroup  有没有成功的处理这个事件，
     如果返回的 true，那么这个触摸事件接下来的一系列(直到手指松开之前) 都会传递给这个 View/ViewGroup 处理，
     但是这个过程中其父 ViewGroup 仍然可以通过 interceptTouchEvent(MotionEvent e) 方法拦截这个触摸事件，
     如果在传递的过程中被拦截了，那么久不会传递到这个 View/ViewGroup 上。

     3、无论何时，只要一个 View/ViewGroup 的 onTouchEvent(MotionEvent event) 方法返回了 false，
     证明这个 View/ViewGroup 没有处理完成这个触摸事件，
     那么接下来的一系列的触摸事件都不会传递给当前 View/ViewGroup 处理。
     */


    /**
     * 这个是 ViewGroup 控件处理触摸事件的方法，一般来说，ViewGroup 控件的触摸事件在这个方法中处理。
     * 如果这个方法返回 true，证明当前触摸事件被当前 ViewGroup 控件处理完成并消耗了。
     * 如果返回 false，证明当前触摸事件没有被当前 ViewGroup 控件处理完成。
     *
     * @param ev
     * @return
     */
    private float lastMoveX;

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//        KLog.e("事件传递到了 左右箭头 的 onTouchEvent，响应触摸事件：" + ev.getAction());
        x = ev.getRawX();
        y = ev.getRawY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float moveX = x - downX;
                if (isLeftEage) {
                    if (Math.abs(moveX) <= shouldFinishPix) {
                        leftSlideView.updateControlPoint(Math.abs(moveX) / 2);
                    }
                    leftLayoutParams.y = (int) (ev.getRawY() - screenHeight / 2);
                    windowManager.updateViewLayout(backView, leftLayoutParams);
                } else if (isRightEage) {
                    if (Math.abs(moveX) <= shouldFinishPix) {
                        rightSlideView.updateControlPoint(Math.abs(moveX) / 2);
                    }
                    rightLayoutParams.y = (int) (ev.getRawY() - screenHeight / 2);
                    windowManager.updateViewLayout(rightView, rightLayoutParams);
                }
                if ((lastMoveX < shouldFinishPix && Math.abs(moveX) >= shouldFinishPix)) {
                    Vibrator vibrator = (Vibrator) activity.getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(300);
                }
                lastMoveX = Math.abs(moveX);
                break;
            case MotionEvent.ACTION_UP:
                //从左边缘划过来，并且最后在屏幕的三分之一外
                if (isLeftEage) {
                    if (x >= shouldFinishPix) {
                        slideListener.slideRightSuccess();
                    }
                    windowManager.removeViewImmediate(backView);
                } else if (isRightEage) {
                    if (x <= screenWidth - shouldFinishPix) {
                        slideListener.slideLeftSuccess();
                    }
                    windowManager.removeViewImmediate(rightView);
                }
                isLeftEage = false;
                isRightEage = false;
                leftSlideView.cancelSlide();
                rightSlideView.cancelSlide();
                break;
            default:
                break;
        }
        return true;
    }

    SlideListener slideListener;

    public void setSlideListener(SlideListener slideListener) {
        this.slideListener = slideListener;
    }

    public interface SlideListener {
        boolean canSlideLeft();

        void slideLeftSuccess();

        boolean canSlideRight();

        void slideRightSuccess();
    }


    public int dp2px(final float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


}
