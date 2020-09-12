package com.yanzhenjie.recyclerview;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

/**
 * @author ditclear on 16/7/12. 可滑动的layout extends FrameLayout
 * https://github.com/ditclear/TimeLine/blob/master/swipelayout/src/main/java/com/ditclear/swipelayout/SwipeDragLayout.java
 * 实现主页的左右滑动已读未读
 */
public class SwipeDragLayout extends FrameLayout implements Controller{
    private SwipeDragLayout mCacheView;
    private View contentView;
    private ViewDragHelper mDragHelper;
    private Point originPos = new Point();
    private int right = 0;
    private int bottom = 0;
    private boolean isOpen, ios = true;
    // 偏移比率
    private float offsetRatio;
    //最小滑动距离的比例
    private float needOffset; // 默认值为 0.45
    private SwipeListener mListener;

    private LeftHorizontal mSwipeLeftHorizontal;
    private RightHorizontal mSwipeRightHorizontal;
    private View leftMenuView;
    private View rightMenuView;
    public static final int DIRECTION_LEFT = 1;
    public static final int DIRECTION_RIGHT = -1;

    public SwipeDragLayout(Context context) {
        this(context, null);
    }

    public SwipeDragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeDragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        needOffset = 0.7f;
        init();
    }

    // 初始化dragHelper，对拖动的view进行操作
    private void init() {
        // ViewDragHelper中拦截和处理事件时，需要会回调CallBack中的很多方法来决定一些事，比如：哪些子View可以移动、对个移动的View的边界的控制等等。
        mDragHelper = ViewDragHelper.create(this, 0.32f, new ViewDragHelper.Callback() {
            // 返回ture则表示可以捕获该view，你可以根据传入的第一个view参数决定哪些可以捕获
            // 决定child是否可被拖拽。返回true则进行拖拽。
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
                //Log.e("移动", " 距离：" + left + "  ,  "  +  dx );
                return left;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                //fix issue 6 List滚动和Item左右滑动有冲突
                // {@link https://github.com/ditclear/SwipeLayout/issues/6}
                // getParent().requestDisallowInterceptTouchEvent(true);
                return super.clampViewPositionVertical(child, top, dy);
            }

            // 要返回一个大于0的数，才会在在水平方向上对触摸到的View进行拖动。
            // 方法的返回值应当是该childView横向或者纵向的移动的范围，当前如果只需要一个方向移动，可以只复写一个。
            // 方法名为获取水平方向拖拽的范围，然而目前并没有用，该方法的返回值用来作为判断滑动方向的条件之一， 如果你想水平移动，那么该方法的返回值最好大于0
            @Override
            public int getViewHorizontalDragRange(View child) {
                super.getViewHorizontalDragRange(child);
                return 1;
            }
            /**
             * 当View移动的时候调用
             * @param changedView   当前移动的VIew
             * @param left  当前View移动之后最新的left(应该是相对初始点的偏移量)
             * @param top   当前View移动之后最新的top(应该是相对初始点的偏移量)
             * @param dx    距离上次调用onViewPositionChanged，left变化的值，即水平移动的距离
             * @param dy    垂直移动的距离
             */
            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                // 我们需要在x!=0的时候就行拦截。
//                if (dx != 0) {
//                    getParent().requestDisallowInterceptTouchEvent(true);
//                }

                final int childWidth = mSwipeRightHorizontal.getMenuWidth();
                offsetRatio = -(float) (left - getPaddingLeft()) / childWidth;
                //Log.e("侧滑", "滑动：" + left + "   " + top  + "   " + dx  + "   " + dy  + "  ==== " + offsetRatio + " , " + getPaddingLeft() + " , " + childWidth );
                //offsetRatio can callback here
//                if (mListener!=null){
//                    mListener.onUpdate(SwipeDragLayout.this, offsetRatio,left);
//                }
                // 兼容老版本
                 invalidate();
            }

            //手指释放的时候回调
            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
//                Log.e("侧滑", "释放：" + xvel + "   " + yvel );
                // Note: needOffset 最小偏移量 应该由左/右菜单View 的宽度来定。（）
                if (releasedChild == contentView) {
                        if (offsetRatio != 0 && offsetRatio < needOffset) {
                            if( Math.abs(offsetRatio) < needOffset ){
                                close();
                            }else {
                                openLeft();
                            }
//                             Log.e("侧滑","D：" + offsetRatio + "  " + needOffset);
                        } else if (offsetRatio == 0) {
                            close();
//                            Log.e("侧滑","E：" + offsetRatio + "  " + needOffset);
                        } else {
                            openRight();
//                            Log.e("侧滑","F：" + offsetRatio + "  " + isOpen + "  " + needOffset);
                        }
//                    }
                    invalidate();
                }
            }
        });
    }


    public void openRight() {
        mCacheView = SwipeDragLayout.this;
        // Log.e("","打开右侧"  + isOpen + mListener );
//        Log.d("Released and isOpen", "" + isOpen);
        closeRight();
    }

    public void openLeft() {
        mCacheView = SwipeDragLayout.this;
        // Log.e("侧滑","打开左侧"  + isOpen + mListener );
        closeLeft();
    }


    private void smoothClose(boolean smooth) {
        if (smooth) {
            mDragHelper.smoothSlideViewTo(contentView, getPaddingLeft(), getPaddingTop());
            // TODO: 2019/4/22 测试
            ViewCompat.postInvalidateOnAnimation(this);
//            postInvalidate();
        } else {
            contentView.layout(originPos.x, originPos.y, right, bottom);
        }
        isOpen = false;
        mCacheView = null;
    }



    public void close() {
        mDragHelper.settleCapturedViewAt(originPos.x, originPos.y);
        isOpen = false;
        mCacheView = null;
//        mListener.onClosed(SwipeDragLayout.this);
    }

    public void closeLeft() {
        // Log.e("侧滑" , "关闭左边" + contentView);
        if (contentView == null){
            return;
        }

        if (mListener != null) {
            mListener.onCloseLeft();
            mListener.onClose(leftMenuView,DIRECTION_LEFT);
        }
        try {
            mDragHelper.settleCapturedViewAt(originPos.x, originPos.y);
        }catch (Exception e){
            e.printStackTrace();
            Log.e("","报错");
        }
        isOpen = false;
        mCacheView = null;
    }
    public void closeRight() {
        // Log.e("侧滑" , "关闭右侧" + contentView);
        if (contentView == null){
            return;
        }

        if (mListener != null) {
            mListener.onCloseRight();
            mListener.onClose(rightMenuView,DIRECTION_RIGHT);
        }
        try {
            mDragHelper.settleCapturedViewAt(originPos.x, originPos.y);
        }catch (Exception e){
            e.printStackTrace();
            Log.e("","报错");
        }
        isOpen = false;
        mCacheView = null;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (contentView != null) {
            originPos.x = contentView.getLeft();
            originPos.y = contentView.getTop();
            this.right = contentView.getRight();
            this.bottom = contentView.getBottom();


            int contentViewWidth = contentView.getMeasuredWidthAndState();
            int contentViewHeight = contentView.getMeasuredHeightAndState();
            LayoutParams lp = (LayoutParams)contentView.getLayoutParams();
            int start1 = getPaddingLeft();
            int top1 = getPaddingTop() + lp.topMargin;
            contentView.layout(start1, top1, start1 + contentViewWidth, top1 + contentViewHeight);
        }

        if (mSwipeLeftHorizontal != null) {
            View leftMenu = mSwipeLeftHorizontal.getMenuView();
            int menuViewWidth = leftMenu.getMeasuredWidthAndState();
            int menuViewHeight = leftMenu.getMeasuredHeightAndState();
            LayoutParams lp = (LayoutParams)leftMenu.getLayoutParams();
            int top1 = getPaddingTop() + lp.topMargin;
//            leftMenu.layout(-menuViewWidth, top1, 0, top1 + menuViewHeight);
            leftMenu.layout(0, top1, menuViewWidth, top1 + menuViewHeight);
        }

        if (mSwipeRightHorizontal != null) {
            View rightMenu = mSwipeRightHorizontal.getMenuView();
            int menuViewWidth = rightMenu.getMeasuredWidthAndState();
            int menuViewHeight = rightMenu.getMeasuredHeightAndState();
            LayoutParams lp = (LayoutParams)rightMenu.getLayoutParams();
            int top1 = getPaddingTop() + lp.topMargin;

            int parentViewWidth = getMeasuredWidthAndState();
//            rightMenu.layout(parentViewWidth, top1, parentViewWidth + menuViewWidth, top1 + menuViewHeight);
            rightMenu.layout(parentViewWidth - menuViewWidth, top1, parentViewWidth, top1 + menuViewHeight);
        }
    }

    // 持续平滑动画 高频调用
    @Override
    public void computeScroll() {
        // 如果返回true，动画还需要继续
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
            // invalidate();
        }
    }

    // 当View中所有的子控件均被映射成xml后触发.当加载完成xml后，就会执行这个方法。
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        leftMenuView = findViewById(R.id.swipe_left);
        rightMenuView = findViewById(R.id.swipe_right);
        contentView = findViewById(R.id.swipe_content);
        mSwipeLeftHorizontal = new LeftHorizontal(leftMenuView);
        mSwipeRightHorizontal = new RightHorizontal(rightMenuView);
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


    public void setListener(SwipeListener listener) {
        mListener = listener;
    }

    //滑动监听
    public interface SwipeListener {
//        /**
//         * 拖动中，可根据offset 进行其他动画
//         * @param view
//         * @param offset 偏移量
//         */
//        void onUpdate(View view, float offsetRatio, int offset);

//        void onOpened(View view);

//        void onClosed(View view);
//        void onCloseLeft(View view);
//        void onCloseRight(View view);

        void onClose(View swipeMenu,int direction);
        void onCloseLeft();
        void onCloseRight();

//        /**
//         * 点击内容layout {@link #onFinishInflate()}
//         * @param view
//         */
//        void onClick(View view);

//        void log(String temp);
    }


//    private float mOpenPercent = 0.5f;
//    /**
//     * Set open percentage.
//     *
//     * @param openPercent such as 0.5F.
//     */
//    public void setOpenPercent(float openPercent) {
//        this.mOpenPercent = openPercent;
//    }
//    /**
//     * The duration of the set.
//     *
//     * @param scrollerDuration such as 500.
//     */
//    public void setScrollerDuration(int scrollerDuration) {
//        this.mScrollerDuration = scrollerDuration;
//    }

    @Override
    public boolean isMenuOpen() {
        return isLeftMenuOpen() || isRightMenuOpen();
    }

    public boolean isLeftMenuOpen() {
        return mSwipeLeftHorizontal != null && mSwipeLeftHorizontal.isMenuOpen(getScrollX());
    }

    public boolean isRightMenuOpen() {
        return mSwipeRightHorizontal != null && mSwipeRightHorizontal.isMenuOpen(getScrollX());
    }

    @Override
    public void smoothCloseMenu() {
        close();
    }


    public boolean hasLeftMenu() {
        return mSwipeLeftHorizontal != null && mSwipeLeftHorizontal.canSwipe();
    }

    public boolean hasRightMenu() {
        return mSwipeRightHorizontal != null && mSwipeRightHorizontal.canSwipe();
    }
}
