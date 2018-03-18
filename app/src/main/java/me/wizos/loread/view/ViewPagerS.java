package me.wizos.loread.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * Created by Wizos on 2017/9/15.
 */

public class ViewPagerS extends ViewPager {

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


}
