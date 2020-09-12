package com.yanzhenjie.recyclerview;

import android.view.View;

/**
 * Created by Wizos on 2019/4/14.
 */

public interface OnItemSwipeListener {
    void onClose(View swipeMenu,int direction,int adapterPosition);
    void onCloseLeft(int adapterPosition);
    void onCloseRight(int adapterPosition);
}
