package me.wizos.loread.view.slideback.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * author : ParfoisMeng
 * time   : 2019/01/10
 * desc   : 处理事件拦截的Layout
 */
public class SlideBackInterceptLayout extends FrameLayout {
    // private float leftSideSlideLength = 0; // 边缘滑动响应距离
    // private float rightSideSlideLength = 0; // 边缘滑动响应距离

    public SlideBackInterceptLayout(Context context) {
        this(context, null);
    }

    public SlideBackInterceptLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideBackInterceptLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return ev.getAction() == MotionEvent.ACTION_DOWN && isMotionTrigger(ev);
    }
    // @Override
    // public boolean onInterceptTouchEvent(MotionEvent ev) {
    //     return ev.getAction() == MotionEvent.ACTION_DOWN && (ev.getRawX() <= leftSideSlideLength || ev.getRawX() >= rightSideSlideLength);
    // }

    // public void setSideSlideLength(float screenWidth, float sideSlideLength) {
    //     this.leftSideSlideLength = sideSlideLength;
    //     this.rightSideSlideLength = screenWidth - sideSlideLength;
    // }


    private ArrayList<float[]> zoneList;

    public void addXTriggerZone(float[] zone) {
        if (zoneList == null) {
            zoneList = new ArrayList<>();
        }
        float tmp;
        if (zone[0] > zone[1]) {
            tmp = zone[0];
            zone[0] = zone[1];
            zone[1] = tmp;
            zoneList.add(zone);
        } else if (zone[0] < zone[1]) {
            zoneList.add(zone);
        }
    }

    public void setXTriggerZone(float[]... zones) {
        zoneList = new ArrayList<>(zones.length);
        float tmp;
        for (float[] zone : zones) {
            if (zone[0] > zone[1]) {
                tmp = zone[0];
                zone[0] = zone[1];
                zone[1] = tmp;
                zoneList.add(zone);
            } else if (zone[0] < zone[1]) {
                zoneList.add(zone);
            }
        }
    }

    private boolean isMotionTrigger(MotionEvent ev) {
        for (float[] zone : zoneList) {
            if (zone[0] <= ev.getRawX() && ev.getRawX() <= zone[1]) {
                // XLog.d("事件成功：" + zone[0] + " , " + ev.getRawX() + " , " + zone[1] + " = " + ev.getAction());
                return true;
            }
        }
        // XLog.d("事件不成功：" + " , " + ev.getRawX() + " , " + " = " + ev.getAction());
        return false;
    }
}