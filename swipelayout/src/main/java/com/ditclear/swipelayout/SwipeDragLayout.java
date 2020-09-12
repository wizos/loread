package com.ditclear.swipelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.customview.widget.ViewDragHelper;

/**
 * @author ditclear on 16/7/12. 可滑动的layout extends FrameLayout
 * https://github.com/ditclear/TimeLine/blob/master/swipelayout/src/main/java/com/ditclear/swipelayout/SwipeDragLayout.java
 * 实现主页的左右滑动已读未读
 */
public class SwipeDragLayout extends FrameLayout {
    private SwipeDragLayout mCacheView;
    private View contentView;
    private View leftMenuView;
    private View rightMenuView;
    private ViewDragHelper mDragHelper;
    private Point originPos = new Point();
    private boolean isOpen, ios, clickToClose;
    private float offsetRatio;
    private float needOffset; // 默认值为 0.45
    private SwipeListener mListener;

    public SwipeDragLayout(Context context) {
        this(context, null);
    }

    public SwipeDragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeDragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SwipeDragLayout);
        needOffset = array.getFloat(R.styleable.SwipeDragLayout_need_offset, 0.6f);
        //是否有回弹效果
        ios = array.getBoolean(R.styleable.SwipeDragLayout_ios, false);
        clickToClose = array.getBoolean(R.styleable.SwipeDragLayout_click_to_close, false);
        init();
        array.recycle();
    }

//    public static SwipeDragLayout getmCacheView() {
//        return mCacheView;
//    }

    // 初始化dragHelper，对拖动的view进行操作
    private void init() {
        // ViewDragHelper中拦截和处理事件时，需要会回调CallBack中的很多方法来决定一些事，比如：哪些子View可以移动、对个移动的View的边界的控制等等。
        mDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {

            // 返回ture则表示可以捕获该view，你可以根据传入的第一个view参数决定哪些可以捕获
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return child == contentView;
            }


            /**
             * 用来修正或者指定子View在水平方向上的移动
             * clampViewPositionHorizontal 可以在该方法中对child移动的边界进行控制，left 分别为即将移动到的位置。
             * 比如横向的情况下，我希望只在ViewGroup的内部移动，即：最小>=paddingleft，最大<=ViewGroup.getWidth()-paddingright-child.getWidth。
             * @param child 被拖动的view
             * @param left  是ViewDragHelper帮你计算好的View最新的left的值，left=view.getLeft()+dx
             * @param dx   本次水平移动的距离
             * @return  返回的值表示我们真正想让View的left变成的值
             */
            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
//                //滑动距离,如果启动效果，则可滑动3/2倍菜单宽度的距离
//                // 左边限 = -156
//                final int leftBound = getPaddingLeft() - (ios ? rightMenuView.getWidth() * 3 / 2 : rightMenuView.getWidth());
//                // 右边限 = 0
//                final int rightBound = getWidth() - child.getWidth();
//                final int newLeft = Math.min(Math.max(left, leftBound), rightBound);
////                mListener.log("clampViewPositionHorizontal：leftBound=" + leftBound + "  rightBound=" + rightBound +"  newLeft="+ newLeft + "  " + left );
////                return newLeft;
                return left;
            }


//            // 要返回一个大于0的数，才会在在水平方向上对触摸到的View进行拖动。
//            // 方法的返回值应当是该childView横向或者纵向的移动的范围，当前如果只需要一个方向移动，可以只复写一个。
//            // 方法名为获取水平方向拖拽的范围，然而目前并没有用，该方法的返回值用来作为判断滑动方向的条件之一， 如果你想水平移动，那么该方法的返回值最好大于0
            @Override
            public int getViewHorizontalDragRange(View child) {
//                mListener.log("getViewHorizontalDragRange" );
//                return contentView == child ? rightMenuView.getWidth() : 0;
                return 1;
            }
            /**
             * 当View移动的时候调用
             * @param changedView   当前移动的VIew
             * @param left  当前View移动之后最新的left(应该是相对初始点的偏移量)
             * @param top   当前View移动之后最新的top(应该是相对初始点的偏移量)
             * @param dx    水平移动的距离
             * @param dy    垂直移动的距离
             */
            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                Log.e("滑动", "滑动：" + left + "   " + top  + "   " + dx  + "   " + dy  );
                final int childWidth = rightMenuView.getWidth();
                offsetRatio = -(float) (left - getPaddingLeft()) / childWidth;
                //offsetRatio can callback here
                if (mListener!=null){
                    mListener.onUpdate(SwipeDragLayout.this, offsetRatio,left);
                }
//                mListener.log("onViewPositionChanged：" + offsetRatio + "  left:" + left + "   top" + top + "  dx" + dx + "   dy" + dy );
                // 兼容老版本
                invalidate();
            }

            //手指释放的时候回调
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                Log.e("滑动", "释放：" + xvel + "   " + yvel );
//                mListener.log("-------------------------------------------------------------手指释放" );
                // Note: needOffset 最小偏移量 应该由左/右菜单View 的宽度来定。（）
                if (releasedChild == contentView) {
                    if (isOpen()) {
                        if (offsetRatio != 1 && offsetRatio > (1 - needOffset)) {
                            openRight();
//                            mListener.log("A：" + offsetRatio + "  " + needOffset);
                        } else if (offsetRatio == 1) {
                            if (clickToClose) {
                                close();
                            }
//                            mListener.log("B：" + offsetRatio + "  " + clickToClose);
                        } else {
                            if( Math.abs(offsetRatio) < (1 - needOffset) ){
                                close();
                            }else {
                                openLeft();
                            }
//                            close();
//                            mListener.log("C：" + offsetRatio + "  " + clickToClose);
                        }
                    } else {
                        if (offsetRatio != 0 && offsetRatio < needOffset) {
                            if( Math.abs(offsetRatio) < needOffset ){
                                close();
                            }else {
                                openLeft();
                            }
//                            mListener.log("D：" + offsetRatio + "  " + needOffset);
                        } else if (offsetRatio == 0) {
                            getParent().requestDisallowInterceptTouchEvent(false);
//                            mListener.log("E：" + offsetRatio + "  " + needOffset);
                        } else {
                            openRight();
//                            mListener.log("F：" + offsetRatio + "  " + isOpen + "  " + needOffset);
                        }
                    }
                    invalidate();
                }
            }
        });
    }

    public void setClickToClose(boolean clickToClose) {
        this.clickToClose = clickToClose;
    }

    public void setIos(boolean ios) {
        this.ios = ios;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void openRight() {
        mCacheView = SwipeDragLayout.this;
        mDragHelper.settleCapturedViewAt(originPos.x - rightMenuView.getWidth(), originPos.y);
        isOpen = true;
//        mListener.log("打开右侧"  + isOpen );
//        Log.d("Released and isOpen", "" + isOpen);
        if (mListener != null) {
            mListener.onOpened(SwipeDragLayout.this);
            closeRight();
        }
    }

    public void openLeft() {
        mCacheView = SwipeDragLayout.this;
        mDragHelper.settleCapturedViewAt(originPos.x + leftMenuView.getWidth(), originPos.y);
        isOpen = true;
//        mListener.log("打开左侧" + isOpen );
        if (mListener != null) {
            mListener.onOpened(SwipeDragLayout.this);
            closeLeft();
        }
    }

//    public void smoothOpenRight(boolean smooth) {
//        mCacheView = SwipeDragLayout.this;
//        if (smooth) {
//            mDragHelper.smoothSlideViewTo(contentView, originPos.x - rightMenuView.getWidth(), originPos.y);
//        } else {
//            contentView.layout(originPos.x - rightMenuView.getWidth(), originPos.y, rightMenuView.getLeft(), rightMenuView.getBottom());
//        }
//    }
//    public void smoothOpenLeft(boolean smooth) {
//        mCacheView = SwipeDragLayout.this;
//        if (smooth) {
//            mDragHelper.smoothSlideViewTo(contentView, originPos.x + leftMenuView.getWidth(), originPos.y);
//        } else {
//            contentView.layout(originPos.x + leftMenuView.getWidth(), originPos.y, leftMenuView.getLeft(), leftMenuView.getBottom());
//        }
//    }

    private void smoothClose(boolean smooth) {
        if (smooth) {
            mDragHelper.smoothSlideViewTo(contentView, getPaddingLeft(), getPaddingTop());
            postInvalidate();
        } else {
            contentView.layout(originPos.x, originPos.y, rightMenuView.getRight(), rightMenuView.getBottom());
        }
        isOpen = false;
        mCacheView = null;

    }



    public void close() {
        mDragHelper.settleCapturedViewAt(originPos.x, originPos.y);
        isOpen = false;
        mCacheView = null;
        mListener.onClosed(SwipeDragLayout.this);
    }

    public void closeLeft() {
        if (contentView == null){
            return;
        }
        try {
            mDragHelper.settleCapturedViewAt(originPos.x, originPos.y);
        }catch (Exception e){
        }
        isOpen = false;
        mCacheView = null;
        mListener.onCloseLeft(SwipeDragLayout.this);
    }
    public void closeRight() {
        if (contentView == null){
            return;
        }
        try {
            mDragHelper.settleCapturedViewAt(originPos.x, originPos.y);
        }catch (Exception e){
        }
        isOpen = false;
        mCacheView = null;
        mListener.onCloseRight(SwipeDragLayout.this);
//        if (mListener != null) {
//            mListener.onCloseRight(SwipeDragLayout.this);
//        }
    }

    private int mLastX = 0;
    private int mLastY = 0;
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int eventX = (int) event.getX();
        int eventY = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getX();
                mLastY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                int offsetX = eventX - mLastX;
                int offsetY = eventY - mLastY;
                if ( Math.abs(offsetX) - Math.abs(offsetY) < ViewConfiguration.getTouchSlop()) {
                    break;
                }
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
        }
        mLastX = eventX;
        mLastY = eventY;
        return super.dispatchTouchEvent(event);
    }




    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        mListener.log("拦截触摸事件" + ev.getAction() );
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mCacheView != null) {
                    if (mCacheView != this) {
                        mCacheView.smoothClose(true);
                    }
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
        }
        // onInterceptTouchEvent中通过使用mDragger.shouldInterceptTouchEvent(event)来决定我们是否应该拦截当前的事件。
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event); // onTouchEvent中通过mDragger.processTouchEvent(event)处理事件。
        return true;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        originPos.x = contentView.getLeft();
        originPos.y = contentView.getTop();
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            invalidate();
        }
    }

    // 当View中所有的子控件均被映射成xml后触发.当加载完成xml后，就会执行这个方法。
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        leftMenuView = getChildAt(0);
        rightMenuView = getChildAt(1);
        contentView = getChildAt(2);

        FrameLayout.LayoutParams params2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params2.gravity = Gravity.CENTER_VERTICAL|Gravity.LEFT;
        leftMenuView.setLayoutParams(params2);

        FrameLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.RIGHT|Gravity.CENTER_VERTICAL;
        rightMenuView.setLayoutParams(params);


        //重写OnClickListener会导致关闭失效
        if (contentView != null){
            contentView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickToClose&&isOpen()){
                        smoothClose(true);
                        return;
                    }
                    if (mListener!=null){
                        mListener.onClick(SwipeDragLayout.this);
                    }

                }
            });
        }
    }

    // onDetachedFromWindow方法是在Activity destroy的时候被调用的，也就是act对应的window被删除的时候，且每个view只会被调用一次，父view的调用在后，也不论view的visibility状态都会被调用，适合做最后的清理操作；
    @Override
    protected void onDetachedFromWindow() {
        if (mCacheView == this) {
            mCacheView.smoothClose(false);
            mCacheView = null;
        }
        super.onDetachedFromWindow();
    }

    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = -1;

    public void addListener(SwipeListener listener) {
        mListener = listener;
    }

    //滑动监听
    public interface SwipeListener {
        /**
         * 拖动中，可根据offset 进行其他动画
         * @param view
         * @param offset 偏移量
         */
        void onUpdate(View  view, float offsetRatio,int offset);

        /**
         * 展开完成
         * @param view
         */
        void onOpened(View  view);

        /**
         * 关闭完成
         * @param view
         */
        void onClosed(View  view);
        void onCloseLeft(View  view);
        void onCloseRight(View  view);

        /**
         * 点击内容layout {@link #onFinishInflate()}
         * @param view
         */
        void onClick(View  view);

        void log(String temp);
    }

}
