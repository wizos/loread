package me.wizos.loread.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;

/**
 * Created by Wizos on 2016/3/30.
 */
public class SwipeRefresh extends SwipeRefreshLayout {
    private View view;
    public SwipeRefresh(Context context) {
        super(context);
    }

    public SwipeRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setViewGroup(View view) {
        this.view = view;
    }

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
    * 自从Google推出SwipeRefreshLayout后相信很多人都开始使用它来实现listView的下拉刷新了
    * 但是在使用的时候，有一个很有趣的现象，当SwipeRefreshLayout只有listview一个子view的时候是没有任何问题的
    * 但如果不是得话就会出现问题了，向上滑动listview一切正常，向下滑动的时候就会出现还没有滑倒listview顶部就触发下拉刷新的动作了
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

}
