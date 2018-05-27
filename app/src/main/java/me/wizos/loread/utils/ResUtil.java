package me.wizos.loread.utils;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import me.wizos.loread.App;

/**
 * Created by Wizos on 2018/3/21.
 */

public class ResUtil {
    public static int getColor(@ColorRes int id) {
        return App.i().getResources().getColor(id);
    }


    public static int resolveColor(Context context, @AttrRes int attr, int fallback) {
        TypedArray a = context.getTheme().obtainStyledAttributes(new int[]{attr});
        try {
            return a.getColor(0, fallback);
        } finally {
            a.recycle();
        }
    }


    public static float getAttrFloatValue(Context context, @AttrRes int attrRes) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrRes, typedValue, true);
        return typedValue.getFloat();
    }

    public static int getAttrColor(Context context, @AttrRes int attrRes) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrRes, typedValue, true);
        return typedValue.data;
    }

    public static ColorStateList getAttrColorStateList(Context context, @AttrRes int attrRes) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attrRes, typedValue, true);
        return ContextCompat.getColorStateList(context, typedValue.resourceId);
    }

    public static Drawable getAttrDrawable(Context context, int attrRes) {
        int[] attrs = new int[]{attrRes};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        Drawable drawable = ta.getDrawable(0);
        ta.recycle();
        return drawable;
    }

//    public static int getAttrDimen(Context context, int attrRes){
//        TypedValue typedValue = new TypedValue();
//        context.getTheme().resolveAttribute(attrRes, typedValue, true);
//        return TypedValue.complexToDimensionPixelSize(typedValue.data, QMUIDisplayHelper.getDisplayMetrics(context));
//    }
}
