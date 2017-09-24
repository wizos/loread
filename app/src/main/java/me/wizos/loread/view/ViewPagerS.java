package me.wizos.loread.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Wizos on 2017/9/15.
 */

public class ViewPagerS extends ViewPager {
    public boolean isScroll = false;

    public boolean isScroll() {
        return isScroll;
    }

    public void setScroll(boolean scroll) {
        isScroll = scroll;
    }

    public ViewPagerS(Context context) {
        super(context);
    }

    public ViewPagerS(Context context, AttributeSet attrs) {
        super(context, attrs);
////        TypedArray array=context.obtainStyledAttributes(attrs, R.styleable.ViewPagerS);
////         /*获取布局中设置的属性*/
////        isScroll=array.getBoolean(R.styleable.MyViewPager_isScroll,false);
////        array.recycle();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isScroll) {
            return super.onTouchEvent(ev);
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isScroll) {
            return super.onInterceptTouchEvent(ev);
        }
        return false;

    }
}
