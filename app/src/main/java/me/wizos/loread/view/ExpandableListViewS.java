package me.wizos.loread.view;

import android.content.Context;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ExpandableListView;

import com.yinglan.scrolllayout.ScrollLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Wizos on 2017/9/17.
 */

public class ExpandableListViewS extends ExpandableListView {
    private final ExpandableListViewS.CompositeScrollListener compositeScrollListener = new ExpandableListViewS.CompositeScrollListener();

    public ExpandableListViewS(Context context) {
        super(context);
    }

    public ExpandableListViewS(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExpandableListViewS(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ExpandableListViewS(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    {
//        KLog.e("看看都是什么时候调用这个诡异的函数");
//        super.setOnGroupClickListener(new SenseGroupClickListener());
//        registerListener();
        super.setOnScrollListener(compositeScrollListener);

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewGroup.LayoutParams layoutParams = getLayoutParams();
                ViewParent parent = getParent();
                while (parent != null) {
                    if (parent instanceof ScrollLayout) {
                        int height = ((ScrollLayout) parent).getMeasuredHeight() - ((ScrollLayout) parent).minOffset;
                        if (layoutParams.height == height) {
                            return;
                        } else {
                            layoutParams.height = height;
                            break;
                        }
                    }
                    parent = parent.getParent();
                }
                setLayoutParams(layoutParams);
            }
        });
    }

    /**
     * 添加一个OnScrollListener,会取代已添加OnScrollListener
     * <p>
     * <b>Make sure call this on UI thread</b>
     * </p>
     *
     * @param listener the listener to add
     */
    @Override
    public void setOnScrollListener(final OnScrollListener listener) {
        addOnScrollListener(listener);
    }

    /**
     * 添加一个OnScrollListener,不会取代已添加OnScrollListener
     * <p>
     * <b>Make sure call this on UI thread</b>
     * </p>
     *
     * @param listener the listener to add
     */
    public void addOnScrollListener(final OnScrollListener listener) {
        throwIfNotOnMainThread();
        compositeScrollListener.addOnScrollListener(listener);
    }

    /**
     * 删除前一个添加scrollListener,只会删除完全相同的对象
     * <p>
     * <b>Make sure call this on UI thread.</b>
     * </p>
     *
     * @param listener the listener to remove
     */
    public void removeOnScrollListener(final OnScrollListener listener) {
        throwIfNotOnMainThread();
        compositeScrollListener.removeOnScrollListener(listener);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewParent parent = getParent();
        while (parent != null) {
            if (parent instanceof ScrollLayout) {
                ((ScrollLayout) parent).setAssociatedListView(this);
                break;
            }
            parent = parent.getParent();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void throwIfNotOnMainThread() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("Must be invoked from the main thread.");
        }
    }

    private class CompositeScrollListener implements OnScrollListener {
        private final List<OnScrollListener> scrollListenerList = new
                ArrayList<OnScrollListener>();

        public void addOnScrollListener(OnScrollListener listener) {
            if (listener == null) {
                return;
            }
            for (OnScrollListener scrollListener : scrollListenerList) {
                if (listener == scrollListener) {
                    return;
                }
            }
            scrollListenerList.add(listener);
        }

        public void removeOnScrollListener(OnScrollListener listener) {
            if (listener == null) {
                return;
            }
            Iterator<OnScrollListener> iterator = scrollListenerList.iterator();
            while (iterator.hasNext()) {
                OnScrollListener scrollListener = iterator.next();
                if (listener == scrollListener) {
                    iterator.remove();
                    return;
                }
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            List<OnScrollListener> listeners = new ArrayList<OnScrollListener>(scrollListenerList);
            for (OnScrollListener listener : listeners) {
                listener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            List<OnScrollListener> listeners = new ArrayList<OnScrollListener>(scrollListenerList);
            for (OnScrollListener listener : listeners) {
                listener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }
    }


//
//    // 自加。实现双击。由于连续双击时并不会调用2次onGroupClick函数。导致该方法无效
////    /**
////     * @deprecated 由于该View已经实现了点击与双击的点击监听器，所以请弃用原始的点击监听器
////     */
////    @Deprecated
////    @Override
////    public void setOnGroupClickListener(OnGroupClickListener onGroupClickListener){
////    }
//
//    public void setOnGroupTapListener(final OnGroupTapListener listener) {
//        this.mOnGroupTapListener = listener;
//    }
//    private OnGroupTapListener mOnGroupTapListener;
//    public interface OnGroupTapListener{
//        boolean onGroupSingleClick(ExpandableListView parent, View v, int groupPosition, long id);
//        boolean onGroupDoubleClick(ExpandableListView parent, View v, int groupPosition, long id);
//    }
//
//    private class SenseGroupClickListener implements OnGroupClickListener{
//        public boolean onGroupClick(final ExpandableListView parent, final View v, final int groupPosition, final long id){
//            if (v.getHandler().hasMessages(3608)) {
//                v.getHandler().removeMessages(3608);
//                mOnGroupTapListener.onGroupDoubleClick(parent, v, groupPosition, id);
//            } else {
//                v.getHandler().sendMessageDelayed(getRunnableMsg(parent, v, groupPosition, id), 3000);// ViewConfiguration.getDoubleTapTimeout()
//            }
//            return true; // 返回真，代表该次点击被处理了
//        }
//    }
//    private Message getRunnableMsg(final ExpandableListView parent, final View v, final int groupPosition, final long id) {
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
//                mOnGroupTapListener.onGroupSingleClick(parent, v, groupPosition, id);
//            }
//        };
//        Message m = Message.obtain(v.getHandler(),r); // obtain() 从全局池中返回一个新的Message实例。在大多数情况下这样可以避免分配新的对象。
//        m.what = 3608;
//        return m;
//    }


//
//
//    // 以下是分组可固定式的 ExpandableListView。具体见： https://github.com/itang01/PinnedHeaderExpandable1
//
//    /**
//     * Adapter 接口 . 列表必须实现此接口 .
//     */
//    public interface HeaderAdapter {
//        public static final int PINNED_HEADER_GONE = 0;
//        public static final int PINNED_HEADER_VISIBLE = 1;
//        public static final int PINNED_HEADER_PUSHED_UP = 2; // 这个具体是什么含义还不知道
//
//        /**
//         * 获取 Header 的状态
//         * @return PINNED_HEADER_GONE, PINNED_HEADER_VISIBLE, PINNED_HEADER_PUSHED_UP 其中之一
//         */
//        int getHeaderState(int groupPosition, int childPosition);
//
//        /**
//         * 配置 Header, 让 Header 知道显示的内容
//         */
//        void configureHeader(View header, int groupPosition, int childPosition, int alpha);
//
//        /**
//         * 设置组按下的状态
//         */
//        void setGroupClickStatus(int groupPosition, int status);
//
//        /**
//         * 获取组按下的状态
//         */
//        int getGroupClickStatus(int groupPosition);
//    }
//
//    private static final int MAX_ALPHA = 255; // alpha
//    private HeaderAdapter mAdapter;
//
//    /**
//     * 用于在列表头显示的 View,mHeaderViewVisible 为 true 才可见
//     */
//    private View mHeaderView;
//
//    /**
//     * 列表头是否可见
//     */
//    private boolean mHeaderViewVisible;
//    private int mHeaderViewWidth;
//    private int mHeaderViewHeight;
//
//    public void setHeaderView(View view) {
//        mHeaderView = view;
//        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//        view.setLayoutParams(lp);
//
//        if (mHeaderView != null) {
//            setFadingEdgeLength(0);
//        }
//        requestLayout();
//    }
//
//    /**
//     * 点击 HeaderView 触发的事件
//     */
//    private void headerViewClick() {
//        KLog.e("头部被点击" );
//        long packedPosition = getExpandableListPosition(this.getFirstVisiblePosition());
//        int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
//
//        if (mAdapter.getGroupClickStatus(groupPosition) == 1) {
//            this.collapseGroup(groupPosition);
//            mAdapter.setGroupClickStatus(groupPosition, 0);
//        } else {
//            this.expandGroup(groupPosition);
//            mAdapter.setGroupClickStatus(groupPosition, 1);
//        }
//
//        this.setSelectedGroup(groupPosition);
//    }
//
//    private float mDownX;
//    private float mDownY;
//
//
//    /**
//     * 如果 HeaderView 是可见的 , 此函数用于判断是否点击了 HeaderView, 并对做相应的处理 , 因为 HeaderView
//     * 是画上去的 , 所以设置事件监听是无效的 , 只有自行控制 .
//     */
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        if (mHeaderViewVisible) {
//            switch (ev.getAction()) {
//                case MotionEvent.ACTION_DOWN:
//                    mDownX = ev.getX();
//                    mDownY = ev.getY();
//                    if (mDownX <= mHeaderViewWidth && mDownY <= mHeaderViewHeight) {
//                        return true;
//                    }
//                    break;
//                case MotionEvent.ACTION_UP:
//                    float x = ev.getX();
//                    float y = ev.getY();
//                    float offsetX = Math.abs(x - mDownX);
//                    float offsetY = Math.abs(y - mDownY);
//                    // 如果 HeaderView 是可见的 , 点击在 HeaderView 内 , 那么触发 headerClick()
//                    if (x <= mHeaderViewWidth && y <= mHeaderViewHeight && offsetX <= mHeaderViewWidth
//                            && offsetY <= mHeaderViewHeight) {
//                        if (mHeaderView != null) {
//                            headerViewClick();
//                        }
//                        return true;
//                    }
//                    break;
//                default:
//                    break;
//            }
//        }
//
//        return super.onTouchEvent(ev);
//
//    }
//
//    @Override
//    public void setAdapter(ExpandableListAdapter adapter) {
//        super.setAdapter(adapter);
//        mAdapter = (HeaderAdapter) adapter;
//    }
//
//
//    private void registerListener() {
//        setOnScrollListener(new ScrollListenter() );
////        setOnGroupClickListener(new GroupClickListener());
//    }
//
//
//    private class ScrollListenter implements OnScrollListener{
//        @Override
//        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//            final long flatPos = getExpandableListPosition(firstVisibleItem);
//            int groupPos = ExpandableListView.getPackedPositionGroup(flatPos);
//            int childPos = ExpandableListView.getPackedPositionChild(flatPos);
//
//            configureHeaderView(groupPos, childPos);
//        }
//
//        @Override
//        public void onScrollStateChanged(AbsListView view, int scrollState) {
//        }
//    }
//
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        if (mHeaderView != null) {
//            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
//            mHeaderViewWidth = mHeaderView.getMeasuredWidth();
//            mHeaderViewHeight = mHeaderView.getMeasuredHeight();
//        }
//    }
//
//    private int mOldState = -1;
//
//    @Override
//    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout(changed, left, top, right, bottom);
//        final long flatPostion = getExpandableListPosition(getFirstVisiblePosition());
//        final int groupPos = ExpandableListView.getPackedPositionGroup(flatPostion);
//        final int childPos = ExpandableListView.getPackedPositionChild(flatPostion);
//        int state = mAdapter.getHeaderState(groupPos, childPos);
//        KLog.e("onLayout1：" +  state );
//        if (mHeaderView != null && mAdapter != null && state != mOldState) {
//            mOldState = state;
//            mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
//        }
//
//        KLog.e("onLayout2：" +  state );
//        configureHeaderView(groupPos, childPos);
//    }
//
//    public void configureHeaderView(int groupPosition, int childPosition) {
//        if (mHeaderView == null || mAdapter == null || ((ExpandableListAdapter) mAdapter).getGroupCount() == 0) {
//            return;
//        }
//        int state = mAdapter.getHeaderState(groupPosition, childPosition);
//        KLog.e("configureHeaderView：" +  state );
////        Tool.printCallStatck();
//        switch (state) {
//            case HeaderAdapter.PINNED_HEADER_GONE: {
//                mHeaderViewVisible = false;
//                break;
//            }
//
//            case HeaderAdapter.PINNED_HEADER_VISIBLE: {
//                mAdapter.configureHeader(mHeaderView, groupPosition, childPosition, MAX_ALPHA);
//
//                if (mHeaderView.getTop() != 0) {
//                    mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
//                }
//
//                mHeaderViewVisible = true;
//
//                break;
//            }
//
//            case HeaderAdapter.PINNED_HEADER_PUSHED_UP: { // 如果没有猜错，这个 pushed_up 是指 header 在滚动中被置顶时的状态。也有可能是group刚刚被滚动，此时header逐步显现的过程
//                View firstView = getChildAt(0);
//                KLog.e("当前的数量：" + getChildCount() );
//                int headerHeight = mHeaderView.getHeight();
//                int bottom = firstView.getBottom();
////                // 自加。为了解决 firstView 为空的问题
////                int bottom = headerHeight;
////                if(firstView!=null){
////                    bottom = firstView.getBottom();
////                }
////                // 自加结束。
////                // 原始  int bottom = firstView.getBottom();
//
//                int y;
//                int alpha;
//                if (bottom < headerHeight) {
//                    y = (bottom - headerHeight);
//                    alpha = MAX_ALPHA * bottom / headerHeight; // 原来是 headerHeight + y ，我换成 bottom
//                } else {
//                    y = 0;
//                    alpha = MAX_ALPHA;
//                }
//
//                mAdapter.configureHeader(mHeaderView, groupPosition, childPosition, alpha);
//
//                if (mHeaderView.getTop() != y) {
//                    mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight + y);
//                }
//
//                mHeaderViewVisible = true;
//                break;
//            }
//        }
//    }
//
//    /**
//     * 列表界面更新时调用该方法(如滚动时)
//     */
//    @Override
//    protected void dispatchDraw(Canvas canvas) {
//        super.dispatchDraw(canvas);
//        if (mHeaderViewVisible) {
//            // 分组栏是直接绘制到界面中，而不是加入到ViewGroup中
//            drawChild(canvas, mHeaderView, getDrawingTime());
//        }
//    }


}

