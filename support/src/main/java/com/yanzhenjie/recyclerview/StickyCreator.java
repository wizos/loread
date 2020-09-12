//package com.yanzhenjie.recyclerview;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import android.view.View;
//
///**
// * Created by Wizos on 2019/4/20.
// */
//
//public interface StickyCreator<VH extends RecyclerView.ViewHolder> {
//    int STICKY_HEADER_GONE = 0;
//    int STICKY_HEADER_VISIBLE = 1;
//    int STICKY_HEADER_PUSHED_UP = 2;
//

//    // View setStickyHeaderView(ViewGroup view);
//    int getGroupCount();
//    int getStickyHeaderState(int firstVisibleGroupPosition, int firstVisibleChildPosition);
//    void onBindStickyHeader(View header, int groupPosition, int childPosition, int alpha);
//    //void onStickyHeaderClick(RecyclerView parent, View stickyHeaderView, int stickyGroupPosition);
//    int getGroupPosition(int adapterPosition);
//    int getChildPosition(int adapterPosition);
//    boolean isGroup(int adapterPosition);
//}
