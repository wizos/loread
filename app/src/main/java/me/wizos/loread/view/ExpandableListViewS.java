//package me.wizos.loreadx.view;
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AbsListView;
//import android.widget.ExpandableListAdapter;
//import android.widget.ExpandableListView;
//
//import com.socks.library.KLog;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
///**
// * @author Wizos on 2017/9/17.
// */
//
//public class ExpandableListViewS extends ExpandableListView implements AbsListView.OnScrollListener { // PinnedHeader
//    public ExpandableListViewS(Context context) {
//        super(context);
//        setOnScrollListener(this);
//    }
//
//    public ExpandableListViewS(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        setOnScrollListener(this);
//    }
//
//    public ExpandableListViewS(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        setOnScrollListener(this);
//    }
//
//
//    /**
//     * Adapter 接口 . 列表必须实现此接口 .
//     */
//    private HeaderAdapter mAdapter;
//
//    public interface HeaderAdapter {
//        int PINNED_HEADER_GONE = 0;
//        int PINNED_HEADER_VISIBLE = 1;
//        int PINNED_HEADER_PUSHED_UP = 2;
//
//        /**
//         * 获取 Header 的状态
//         *
//         * @return STICKY_HEADER_GONE, STICKY_HEADER_VISIBLE, STICKY_HEADER_PUSHED_UP 其中之一
//         */
//        int getHeaderState(int groupPosition, int childPosition);
//
//        /**
//         * 配置 Header, 让 Header 知道显示的内容
//         */
//        void configureHeader(View header, int groupPosition, int childPosition, int alpha);
//    }
//
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
//
//    private static final int MAX_ALPHA = 255;
//    private int mHeaderViewWidth;
//    private int mHeaderViewHeight;
//
//    public void setPinnedHeaderView(View view) {
//        mHeaderView = view;
//        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        view.setLayoutParams(lp);
//
//        if (mHeaderView != null) {
//            setFadingEdgeLength(0);
//        }
//        requestLayout();
//    }
//
//
//    private OnPinnedGroupClickListener mOnPinnedGroupClickListener;
//
//    public void setOnPinnedGroupClickListener(OnPinnedGroupClickListener onPinnedGroupClickListener) {
//        mOnPinnedGroupClickListener = onPinnedGroupClickListener;
//    }
//
//    public interface OnPinnedGroupClickListener {
//        void onHeaderClick(ExpandableListView parent, View v, int pinnedGroupPosition);
//    }
//
//    private float mDownX;
//    private float mDownY;
//
//    /**
//     * 如果 HeaderView 是可见的 , 此函数用于判断是否点击了 HeaderView, 并对做相应的处理 , 因为 HeaderView
//     * 是画上去的 , 所以设置事件监听是无效的 , 只有自行控制 .
//     */
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//
//        if (canScrollVertically(this)) {
//            getParent().requestDisallowInterceptTouchEvent(true);
//        }
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
//
//                    // 如果 HeaderView 是可见的 , 点击在 HeaderView 内 , 那么触发 headerClick()
//                    if (x <= mHeaderViewWidth && y <= mHeaderViewHeight && offsetX <= mHeaderViewWidth
//                            && offsetY <= mHeaderViewHeight) {
//                        if (mHeaderView != null && mOnPinnedGroupClickListener != null) {
//                            long packedPosition = getExpandableListPosition(this.getFirstVisiblePosition());
//                            int pinnedGroupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
//                            mOnPinnedGroupClickListener.onHeaderClick(this, mHeaderView, pinnedGroupPosition);
//                        }
//                        return true;
//                    }
//
//                    break;
//                default:
//                    break;
//            }
//        }
//        return super.onTouchEvent(ev);
//    }
//
//    @Override
//    public void setAdapter(ExpandableListAdapter adapter) {
//        super.setAdapter(adapter);
//        mAdapter = (HeaderAdapter) adapter;
//    }
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
//        if (mHeaderView != null && mAdapter != null && state != mOldState) {
//            mOldState = state;
//            mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
//        }
//        configureHeaderView(groupPos, childPos);
//    }
//
//    public void configureHeaderView(int groupPosition, int childPosition) {
//        if (mHeaderView == null || mAdapter == null || ((ExpandableListAdapter) mAdapter).getGroupCount() == 0) {
//            return;
//        }
//        int state = mAdapter.getHeaderState(groupPosition, childPosition);
//        switch (state) {
//            case HeaderAdapter.PINNED_HEADER_GONE: {
//                mHeaderViewVisible = false;
//                Log.e("粘连布局：", "忽略: " + " [" + groupPosition + "," + childPosition + "] , " + mOldState + " , [" + mHeaderViewHeight + "," + mHeaderViewWidth + "]");
//
//                break;
//            }
//            case HeaderAdapter.PINNED_HEADER_VISIBLE: {
//                mAdapter.configureHeader(mHeaderView, groupPosition, childPosition, MAX_ALPHA);
//                Log.e("粘连布局：", "可见: " + " [" + groupPosition + "," + childPosition + "] , " + mOldState + " , [" + mHeaderViewHeight + "," + mHeaderViewWidth + "]   " + MAX_ALPHA + "  " + mHeaderView.getTop());
//
//                if (mHeaderView.getTop() != 0) {
//                    mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
//                }
//                mHeaderViewVisible = true;
//                break;
//            }
//            case HeaderAdapter.PINNED_HEADER_PUSHED_UP: {
//                View firstView = getChildAt(0);
//                int bottom = firstView.getBottom();
////                int itemHeight = firstView.getHeight();
//                int headerHeight = mHeaderView.getHeight();
//                int y;
//                int alpha;
//                if (bottom < headerHeight) {
//                    y = (bottom - headerHeight);
//                    alpha = MAX_ALPHA * (headerHeight + y) / headerHeight;
//                } else {
//                    y = 0;
//                    alpha = MAX_ALPHA;
//                }
//                mAdapter.configureHeader(mHeaderView, groupPosition, childPosition, alpha);
//                if (mHeaderView.getTop() != y) {
//                    mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight + y);
//                }
//                Log.e("粘连布局：", "推动: " + " [" + groupPosition + "," + childPosition + "] , " + mOldState + " , [" + mHeaderViewHeight + "," + mHeaderViewWidth + "]   " + MAX_ALPHA + "  " + mHeaderView.getTop());
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
//        KLog.e("绘制到页面" + mHeaderViewVisible);
//        if (mHeaderViewVisible) {
//            // 分组栏是直接绘制到界面中，而不是加入到ViewGroup中
//            drawChild(canvas, mHeaderView, getDrawingTime());
//        }
//    }
//
//    @Override
//    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//        final long flatPos = getExpandableListPosition(firstVisibleItem);
//        int groupPosition = ExpandableListView.getPackedPositionGroup(flatPos);
//        int childPosition = ExpandableListView.getPackedPositionChild(flatPos);
//        Log.e("展开", "滚动：" + firstVisibleItem + ", " + flatPos + ", " + groupPosition + ", " + childPosition);
//        configureHeaderView(groupPosition, childPosition);
//    }
//
//    @Override
//    public void onScrollStateChanged(AbsListView view, int scrollState) {
//    }
//
//    public boolean canScrollVertically(AbsListView view) {
//        boolean canScroll = false;
//
//        if (view != null && view.getChildCount() > 0) {
//            boolean isOnTop = view.getFirstVisiblePosition() != 0 || view.getChildAt(0).getTop() != 0;
//            boolean isAllItemsVisible = isOnTop && view.getLastVisiblePosition() == view.getChildCount();
//
//            if (isOnTop || isAllItemsVisible) {
//                canScroll = true;
//            }
//        }
//
//        return canScroll;
//    }
//
//    public void setAssociatedListView(AbsListView listView) {
//        listView.setOnScrollListener(associatedListViewListener);
//        updateListViewScrollState(listView);
//    }
//
//    private final AbsListView.OnScrollListener associatedListViewListener =
//            new AbsListView.OnScrollListener() {
//                @Override
//                public void onScrollStateChanged(AbsListView view, int scrollState) {
//                    updateListViewScrollState(view);
//                }
//
//                @Override
//                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                    updateListViewScrollState(view);
//                }
//            };
//
//    private void updateListViewScrollState(AbsListView listView) {
//        if (listView.getChildCount() == 0) {
//            isDraggable = true;
//        } else {
//            if (listView.getFirstVisiblePosition() == 0) {
//                View firstChild = listView.getChildAt(0);
//                if (firstChild.getTop() == listView.getPaddingTop()) {
//                    isDraggable = true;
//                    return;
//                }
//            }
//            isDraggable = false;
//        }
//    }
//
//    private boolean isDraggable = true;
//    // 可拖动
//
//
//    // 以上都是 设置 PinnedGroup 的内容
//
//
//    /**
//     * 实现/接管 AbsListView.OnScrollListener 接口，让 setOnScrollListener 改造为 addOnScrollListener，可以添加更多的监听器。
//     * 因为要实现“悬停抽屉只有在其内部的listview到达最顶部的时候，才能下拉”和“PinnedGroup”功能，都要添加 OnScrollListener 监听器。
//     */
//    private final CompositeScrollListener compositeScrollListener = new CompositeScrollListener();
//
//    {
//        //    其实再类内部{}只是代表在调用构造函数之前在{}中初始化，static{}只在类加载时调用
//        //    new子类的对象时，先调用父类staic{}里的东西，在调用子类里的static{}，在调用父类{}的在调用父类构造方法，在调用子类构造方法
//        //    调用子类或者父类的静态方法时，先调用父类的static{}在调用子类的static{}
//        super.setOnScrollListener(compositeScrollListener);
//    }
//
//    /**
//     * 添加一个OnScrollListener,不会取代已添加OnScrollListener
//     * <p><b>Make sure call this on UI thread</b></p>
//     *
//     * @param listener the listener to add
//     */
//    @Override
//    public void setOnScrollListener(final OnScrollListener listener) {
//        addOnScrollListener(listener);
//    }
//
//    /**
//     * 添加一个OnScrollListener,不会取代已添加OnScrollListener
//     * <p><b>Make sure call this on UI thread</b></p>
//     *
//     * @param listener the listener to add
//     */
//    public void addOnScrollListener(final OnScrollListener listener) {
////        throwIfNotOnMainThread();
//        compositeScrollListener.addOnScrollListener(listener);
//    }
//
//    //
////    /**
////     * 删除前一个添加scrollListener,只会删除完全相同的对象
////     * <p><b>Make sure call this on UI thread.</b></p>
////     *
////     * @param listener the listener to remove
////     */
////    public void removeOnScrollListener(final OnScrollListener listener) {
//////        throwIfNotOnMainThread();
////        compositeScrollListener.removeOnScrollListener(listener);
////    }
//    private class CompositeScrollListener implements OnScrollListener {
//        private final List<OnScrollListener> scrollListenerList = new
//                ArrayList<OnScrollListener>();
//
//        public void addOnScrollListener(OnScrollListener listener) {
//            if (listener == null) {
//                return;
//            }
//            for (OnScrollListener scrollListener : scrollListenerList) {
//                if (listener == scrollListener) {
//                    return;
//                }
//            }
//            scrollListenerList.add(listener);
//        }
//
//        public void removeOnScrollListener(OnScrollListener listener) {
//            if (listener == null) {
//                return;
//            }
//            Iterator<OnScrollListener> iterator = scrollListenerList.iterator();
//            while (iterator.hasNext()) {
//                OnScrollListener scrollListener = iterator.next();
//                if (listener == scrollListener) {
//                    iterator.remove();
//                    return;
//                }
//            }
//        }
//
//        @Override
//        public void onScrollStateChanged(AbsListView view, int scrollState) {
//            List<OnScrollListener> listeners = new ArrayList<OnScrollListener>(scrollListenerList);
//            for (OnScrollListener listener : listeners) {
//                listener.onScrollStateChanged(view, scrollState);
//            }
//        }
//
//        @Override
//        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//            List<OnScrollListener> listeners = new ArrayList<OnScrollListener>(scrollListenerList);
//            for (OnScrollListener listener : listeners) {
//                listener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
//            }
//        }
//    }
//}
//
