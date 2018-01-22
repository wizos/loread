package me.wizos.loread.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;

/**
 * 下拉刷新控件的包装。解决下拉和左右滑动冲突的问题
 * Created by Wizos on 2016/3/30.
 */
public class SwipeRefreshLayoutS extends SwipeRefreshLayout {
    private View view;
//    private int scaleTouchSlop;

    // 方案2：解决下来刷新与左右滑动的冲突
    private int mTouchSlop;
    private float mPrevX;
    //

    public SwipeRefreshLayoutS(Context context) {
        super(context);
    }

    public SwipeRefreshLayoutS(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 触发移动事件的最短距离，如果小于这个距离就不触发移动控件
//        scaleTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        // 判断用户在进行滑动操作的最小距离
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setViewGroup(View view) {
        this.view = view;
    }

    /**
     *
     * 当SwipeRefreshLayout 不只有 listView一个子view时，向下滑动的时候就会出现还没有滑倒listview顶部就触发下拉刷新的动作。
     * 看SwipeRefreshLayout源码可以看到在onInterceptTouchEvent里面有这样的一段代码
     if (!isEnabled() || mReturningToStart || canChildScrollUp() || mRefreshing) {
     // Fail fast if we're not in a state where a swipe is possible
     return false;
     }

     其中有个canChildScrollUp方法，在往下看
     public boolean canChildScrollUp() {
     if (android.os.Build.VERSION.SDK_INT < 14) {
     if (mTarget instanceof AbsListView) {
     final AbsListView absListView = (AbsListView) mTarget;
     return absListView.getChildCount() > 0
     && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
     .getTop() < absListView.getPaddingTop());
     } else {
     return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
     }
     } else {
     return ViewCompat.canScrollVertically(mTarget, -1);
     }
     }
     决定子view 能否滑动就是在这里了，所以我们只有写一个类继承SwipeRefreshLayout，然后重写该方法即可
     *
     */
    @Override
    public boolean canChildScrollUp() {
        if (view != null && view instanceof AbsListView) {
            final AbsListView absListView = (AbsListView) view;
            return absListView.getChildCount() > 0
                    && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                    .getTop() < absListView.getPaddingTop());
        }
        return super.canChildScrollUp();
    }



    /**
     * 解决使用SwipeRefreshLayout下拉刷新和左右滑动事件冲突的问题
     * 使用 SwipeRefreshLayout， 左右滑动 listView item 会出现卡顿，停滞现象，究其原因，是左右滑动和下拉刷新（垂直）冲突导致，就是SwipeRefreshLayout对于Y轴的处理容差值很小，如果不是水平滑动，很轻易就会触发下拉刷新。
     * 为了解决该问题，需要重写SwipeRefreshLayout的onInterceptTouchEvent(MotionEvent ev)事件，在这里面进行处理，当X距离滑动大于某个值时，就认为是左右滑动，不执行下拉刷新操作。
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                mXDown = ev.getX();
//                mYDown = ev.getY();
//                break;
//            case MotionEvent.ACTION_MOVE:
////                float moveX = ev.getX();
////                float instanceX = Math.abs(moveX - preX);
////                KLog.i("refresh...","move: instanceX:" + instanceX + "=(moveX:" + moveX + " - preX:" + preX + ") , scaleTouchSlop:" + scaleTouchSlop);
//                // 容差值大概是24，再加上60
//                if (fingerLeftAndRightMove(ev)) {
//                    return false;
//                }
//                break;
//        }
//        return super.onInterceptTouchEvent(ev);


//        作者：秋天的雨滴
//        链接：https://www.jianshu.com/p/04d799608c2e
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = MotionEvent.obtain(ev).getX();
                break;

            case MotionEvent.ACTION_MOVE:
                final float eventX = ev.getX();
                //获取水平移动距离
                float xDiff = Math.abs(eventX - mPrevX);
                //当水平移动距离大于滑动操作的最小距离的时候就认为进行了横向滑动
                //不进行事件拦截,并将这个事件交给子View处理
                if (xDiff > mTouchSlop) {
                    return false;
                }
        }
        return super.onInterceptTouchEvent(ev);

    }

    /* 手指放下的坐标 */
    private float mXDown;
    private float mYDown;
    /* 手指滑动的最短距离 */
    private float mShortestDistance = 25f;

    /**
     * 手指左右移动：左右得超出50，上下不能超出50
     *
     * @param ev
     * @return
     */
    private boolean fingerLeftAndRightMove(MotionEvent ev) {
        return ((ev.getX() - mXDown > mShortestDistance || ev.getX() - mXDown < -mShortestDistance) &&
                ev.getY() - mYDown < mShortestDistance && ev.getY() - mYDown > -mShortestDistance);
    }
}
