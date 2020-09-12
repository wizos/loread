package com.yhao.floatwindow.util;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by yhao on 2017/12/22.
 * https://github.com/yhaolpz
 */

public class DensityUtil {


    public static View inflate(Context applicationContext, int layoutId) {
        LayoutInflater inflate = (LayoutInflater) applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflate.inflate(layoutId, null);
    }

    private static Point sPoint;

    public static int getScreenWidth(Context context) {
//        if (sPoint == null) {
//            sPoint = new Point();
//            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//            wm.getDefaultDisplay().getSize(sPoint);
//        }
//        return sPoint.x;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
//        if (sPoint == null) {
//            sPoint = new Point();
//            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//            wm.getDefaultDisplay().getSize(sPoint);
//        }
//        return sPoint.y;
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    public static int getStatusBarHeight(Context context){
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * dp 转 px
     *
     * @param context context
     * @param dpValue dpValue
     * @return px
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    static boolean isViewVisible(View view) {
        return view.getGlobalVisibleRect(new Rect());
    }
}
