package com.yanzhenjie.recyclerview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.lang.reflect.Method;

/**
* Depiction:头部吸顶布局。只要用StickyHeaderLayout包裹{@link RecyclerView},
* 并且实现{@link StickyCreator },就可以实现列表头部吸顶功能。
* StickyHeaderLayout只能包裹RecyclerView，而且只能包裹一个RecyclerView。
* <p>
* Author:donkingliang  QQ:1043214265
* Dat:2017/11/14
*/
public class StickyHeaderLayout extends FrameLayout {
   private Context mContext;
   private RecyclerView mRecyclerView;

   //吸顶容器，用于承载吸顶布局。
   private FrameLayout mStickyLayout;

   //保存吸顶布局的缓存池。它以列表组头的viewType为key,ViewHolder为value对吸顶布局进行保存和回收复用。
   private final SparseArray<RecyclerView.ViewHolder> mStickyViews = new SparseArray<>();

   //用于在吸顶布局中保存viewType的key。
   private final int VIEW_TAG_TYPE = -101;

   //用于在吸顶布局中保存ViewHolder的key。
   private final int VIEW_TAG_HOLDER = -102;

   //记录当前吸顶的组。
   private int mCurrentStickyGroup = -1;

   //是否吸顶。
   private boolean isSticky = true;

   //是否已经注册了adapter刷新监听
   private boolean isRegisterDataObserver = false;

   public StickyHeaderLayout(@NonNull Context context) {
       super(context);
       mContext = context;
   }

   public StickyHeaderLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
       super(context, attrs);
       mContext = context;
   }

   public StickyHeaderLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
       super(context, attrs, defStyleAttr);
       mContext = context;
   }

   private boolean enable = false;
   private StickyCreator mAdapter;
   @Override
   public void addView(View child, int index, ViewGroup.LayoutParams params) {
       if (getChildCount() > 0 || !(child instanceof RecyclerView)) {
           //外界只能向StickyHeaderLayout添加一个RecyclerView,而且只能添加RecyclerView。
           throw new IllegalArgumentException("StickyHeaderLayout can host only one direct child --> RecyclerView");
       }
       super.addView(child, index, params);


       mRecyclerView = (RecyclerView) child;

       // addOnScrollListener();
       mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
           @Override
           public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
               // 在滚动的时候，需要不断的更新吸顶布局。
               Log.e("滚动","" + isSticky);
               updateStickyView();
           }
       });

       addStickyLayout();
   }

   /**
    * 添加滚动监听
    */
   private void addOnScrollListener() {
       Log.e("吸附顶部A","" + isSticky);
       mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
           @Override
           public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
               // 在滚动的时候，需要不断的更新吸顶布局。
               Log.e("滚动","" + isSticky);
               updateStickyView();
           }
       });
   }

   /**
    * 添加吸顶容器
    */
   private void addStickyLayout() {
       mStickyLayout = new FrameLayout(mContext);
       LayoutParams lp = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
       mStickyLayout.setLayoutParams(lp);
       super.addView(mStickyLayout, 1, lp);
   }



   public void setStickyHeaderView(View mHeaderView){
       mStickyHeaderView = mHeaderView;
       mStickyLayout.addView(mHeaderView);
   }

   /**
    * 用于在列表头显示的 View,mHeaderViewVisible 为 true 才可见
    */
   private View mStickyHeaderView;
   /**
    * 列表头是否可见
    */
   private boolean mHeaderViewVisible;
   private static final int MAX_ALPHA = 255;
   @Override
   protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
       super.onMeasure(widthMeasureSpec, heightMeasureSpec);
       if (mStickyHeaderView != null) {
           measureChild(mStickyHeaderView, widthMeasureSpec, heightMeasureSpec);
       }
       // Log.e("粘连布局：","宽：" + mHeaderViewWidth + " , 高：" + mHeaderViewHeight);
   }

   private int mOldState = -1;
   @Override
   protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
       super.onLayout(changed, left, top, right, bottom);
       RecyclerView.Adapter adapter = mRecyclerView.getAdapter();

       // Log.e("粘连布局：", "onLayoutA: " + adapter + " , " + mStickyHeaderView + " , " + mHeaderViewHeight + " , " + mHeaderViewWidth + " , " + mHeaderViewVisible);
       if ( adapter == null || !(adapter instanceof AdapterWrapper) ) {
           return;
       }
       StickyCreator mAdapter = (StickyCreator) ((AdapterWrapper)adapter).getOriginAdapter();
       if (mStickyHeaderView == null || mAdapter.getGroupCount() == 0) {
           return;
       }
       int firstVisibleItem = getFirstVisibleItem();
       //通过显示的第一个项的position获取它所在的组。
       int groupPos = mAdapter.getGroupPosition(firstVisibleItem);
       int childPos = mAdapter.getChildPosition(firstVisibleItem);
       int state = mAdapter.getStickyHeaderState(groupPos, childPos);
       Log.e("粘连布局：", "onLayoutB: " + groupPos + " , " + childPos + " , " + state + " , "  + " , " + mHeaderViewVisible);
       if (state != mOldState) {
           mOldState = state;
       }
       // updateStickyView();
   }

   private int lastVisibleItem = -1;
   public void updateStickyView() {
       RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
       if (!(adapter instanceof AdapterWrapper)) {
           return;
       }
       mAdapter = (StickyCreator) ((AdapterWrapper)adapter).getOriginAdapter();
       if (mStickyHeaderView == null || mAdapter.getGroupCount() == 0) {
           return;
       }
       registerAdapterDataObserver(adapter);


       //获取列表显示的第一个项。
       int firstVisibleItem = getFirstVisibleItem();
       //通过显示的第一个项的position获取它所在的组。
       int groupPosition = mAdapter.getGroupPosition(firstVisibleItem);
       int childPosition;
       if( mAdapter.isGroup(firstVisibleItem) ){
           childPosition = -1;
       }else {
           childPosition = mAdapter.getChildPosition(firstVisibleItem);
       }
       Log.e("检测", "   查" + firstVisibleItem + " ," + groupPosition + " , " + childPosition);

       lastVisibleItem = firstVisibleItem;

       int state = mAdapter.getStickyHeaderState(groupPosition, childPosition);

       switch (state) {
           case StickyCreator.STICKY_HEADER_GONE: {
               mHeaderViewVisible = false;
               mStickyHeaderView.setVisibility(INVISIBLE);
               //Log.e("粘连布局：", "忽略: "  + firstVisibleItem + " ["+ groupPosition + "," + childPosition + "] , " + mOldState + " , [" + mStickyHeaderView.getWidth() + "," + mStickyHeaderView.getHeight() + "]" );
               break;
           }
           case StickyCreator.STICKY_HEADER_VISIBLE: {
               mHeaderViewVisible = true;
               mStickyHeaderView.setVisibility(VISIBLE);
               mAdapter.onBindStickyHeader(mStickyHeaderView, groupPosition, childPosition, MAX_ALPHA);
               //Log.e("粘连布局：", "可见: "  + firstVisibleItem + " ["+ groupPosition + "," + childPosition + "] , " + mOldState + " , [" + mStickyHeaderView.getWidth() + "," + mStickyHeaderView.getHeight() + "]   "  + MAX_ALPHA + "  " + mStickyHeaderView.getTop());
               //Log.e("粘连布局：", "池 " + mOldState + " , [" + mStickyHeaderView.getWidth() + "," + mStickyHeaderView.getHeight() + "]   " + "  " + mStickyHeaderView.getTop() + ","+ mStickyHeaderView.getRight() + ","+ mStickyHeaderView.getBottom());
               mStickyHeaderView.setTranslationY(0);
               requestLayout();
               break;
           }
           case StickyCreator.STICKY_HEADER_PUSHED_UP: {
               mHeaderViewVisible = true;
               View firstView = mRecyclerView.getChildAt(0);

               int bottom = firstView.getBottom();
               int headerHeight = mStickyHeaderView.getHeight();
               int y;
               int alpha;
               if (bottom < headerHeight) {
                   y = (bottom - headerHeight);
                   alpha = MAX_ALPHA * (headerHeight + y) / headerHeight;
               } else {
                   y = 0;
                   alpha = MAX_ALPHA;
               }
               mAdapter.onBindStickyHeader(mStickyHeaderView, groupPosition, childPosition, alpha);
               mStickyHeaderView.setTranslationY(y);

               //Log.e("粘连布局：", "推动: "  + firstVisibleItem + " ["+ groupPosition + "," + childPosition + "] , " + mOldState + " , [" + mStickyHeaderView.getWidth() + "," + mStickyHeaderView.getHeight() + "]   "  + MAX_ALPHA + "  " + mStickyHeaderView.getTop());
               //Log.e("粘连布局：", "   ------底部："  + bottom + "，header高度："+ headerHeight + "，顶部位置：" + mStickyHeaderView.getTop() + "，距离：" + y );
               break;
           }

       }
   }

   /**
    * 注册adapter刷新监听
    */
   private void registerAdapterDataObserver(RecyclerView.Adapter adapter) {
       if (!isRegisterDataObserver) {
           isRegisterDataObserver = true;
           adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
               @Override
               public void onChanged() {
                   updateStickyViewDelayed();
               }

               @Override
               public void onItemRangeChanged(int positionStart, int itemCount) {
                   updateStickyViewDelayed();
               }

               @Override
               public void onItemRangeInserted(int positionStart, int itemCount) {
                   updateStickyViewDelayed();
               }

               @Override
               public void onItemRangeRemoved(int positionStart, int itemCount) {
                   updateStickyViewDelayed();
               }
           });
       }
   }

   private void updateStickyViewDelayed() {
       postDelayed(new Runnable() {
           @Override
           public void run() {
               updateStickyView();
           }
       }, 100);
   }

//    /**
//     * 判断是否需要先回收吸顶布局，如果要回收，则回收吸顶布局并返回null。
//     * 如果不回收，则返回吸顶布局的ViewHolder。
//     * 这样做可以避免频繁的添加和移除吸顶布局。
//     *
//     * @param viewType
//     * @return
//     */
//    private RecyclerView.ViewHolder recycleStickyView(int viewType) {
//        if (mStickyLayout.getChildCount() > 0) {
//            View view = mStickyLayout.getChildAt(0);
//            int type = (int) view.getTag(VIEW_TAG_TYPE);
//            if (type == viewType) {
//                return (RecyclerView.ViewHolder)view.getTag(VIEW_TAG_HOLDER);
//            } else {
//                recycle();
//            }
//        }
//        return null;
//    }
//
//    /**
//     * 回收并移除吸顶布局
//     */
//    private void recycle() {
//        if (mStickyLayout.getChildCount() > 0) {
//            View view = mStickyLayout.getChildAt(0);
//            mStickyViews.put((int) (view.getTag(VIEW_TAG_TYPE)), (RecyclerView.ViewHolder)view.getTag(VIEW_TAG_HOLDER));
//            mStickyLayout.removeAllViews();
//        }
//    }

   /**
    * 获取当前第一个显示的item .
    */
   private int getFirstVisibleItem() {
       int firstVisibleItem = -1;
       RecyclerView.LayoutManager layout = mRecyclerView.getLayoutManager();
       if (layout != null) {
           if (layout instanceof GridLayoutManager) {
               firstVisibleItem = ((GridLayoutManager) layout).findFirstVisibleItemPosition();
           } else if (layout instanceof LinearLayoutManager) {
               firstVisibleItem = ((LinearLayoutManager) layout).findFirstVisibleItemPosition();
           } else if (layout instanceof StaggeredGridLayoutManager) {
               int[] firstPositions = new int[((StaggeredGridLayoutManager) layout).getSpanCount()];
               ((StaggeredGridLayoutManager) layout).findFirstVisibleItemPositions(firstPositions);
               firstVisibleItem = getMin(firstPositions);
           }
       }

       return firstVisibleItem;
   }

   private int getMin(int[] arr) {
       int min = arr[0];
       for (int x = 1; x < arr.length; x++) {
           if (arr[x] < min)
               min = arr[x];
       }
       return min;
   }

   @Override
   protected int computeVerticalScrollOffset() {
       if (mRecyclerView != null) {
           try {
               Method method = View.class.getDeclaredMethod("computeVerticalScrollOffset");
               method.setAccessible(true);
               return (int) method.invoke(mRecyclerView);
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
       return super.computeVerticalScrollOffset();
   }


   @Override
   protected int computeVerticalScrollRange() {
       if (mRecyclerView != null) {
           try {
               Method method = View.class.getDeclaredMethod("computeVerticalScrollRange");
               method.setAccessible(true);
               return (int) method.invoke(mRecyclerView);
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
       return super.computeVerticalScrollRange();
   }

   @Override
   protected int computeVerticalScrollExtent() {
       if (mRecyclerView != null) {
           try {
               Method method = View.class.getDeclaredMethod("computeVerticalScrollExtent");
               method.setAccessible(true);
               return (int) method.invoke(mRecyclerView);
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
       return super.computeVerticalScrollExtent();
   }

   @Override
   public void scrollBy(int x, int y) {
       if (mRecyclerView != null) {
           mRecyclerView.scrollBy(x,y);
       } else {
           super.scrollBy(x, y);
       }
   }

   @Override
   public void scrollTo(int x, int y) {
       if (mRecyclerView != null) {
           mRecyclerView.scrollTo(x,y);
       } else {
           super.scrollTo(x, y);
       }
   }
}
