package me.wizos.loread.view.MViewPager;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 本自定义 ViewPager 的目的是为了 解决ViewPager setCurrentItem 时闪烁问题，以修改原生ViewPager切换速度来解决
 * 见地址：http://www.open-open.com/lib/view/open1476773958602.html
 * Created by Wizos on 2017/2/24.
 * <p>
 * 在使用ViewPager的过程中，有需要直接跳转到某一个页面的情况，这个时候就需要用到ViewPager的setCurrentItem方法，它的意思是跳转到ViewPager的指定页面。
 * 但在使用这个方法的时候有个问题，跳转的时候有滑动效果，当需要从当前页面跳转到其它页面时，跳转页面跨度过大、或者ViewPager每个页面的视觉效果相差较大时，通过这种方式实现ViewPager跳转显得很不美观。
 * 怎么办呢，我们可以去掉在使用ViewPager的setCurrentItem方法时的滑屏速度
 */

public class MViewPager extends ViewPager {

    private MViewPageHelper helper;

    public MViewPager(Context context) {
        this(context, null);
    }

    public MViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        helper = new MViewPageHelper(this);
    }

    @Override
    public void setCurrentItem(int item) {
        setCurrentItem(item, true);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        MScroller scroller = helper.getScroller();
        if (Math.abs(getCurrentItem() - item) > 1) {
            scroller.setNoDuration(true);
            super.setCurrentItem(item, smoothScroll);
            scroller.setNoDuration(false);
        } else {
            scroller.setNoDuration(false);
            super.setCurrentItem(item, smoothScroll);
        }
    }

//    public boolean isCanScroll() {
//        return isCanScroll;
//    }
//
//    public void setCanScroll(boolean canScroll) {
//        isCanScroll = canScroll;
//    }

//    //禁止滑动
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        if (!isCanScroll) {
//            return true;
//        }
//        return super.onTouchEvent(ev);
//    }

    public boolean isCallScrollToLeft() {
        return isCallScrollToLeft;
    }

    public void setCallScrollToLeft(boolean callScrollToLeft) {
        isCallScrollToLeft = callScrollToLeft;
    }

    public boolean isCallScrollToRight() {
        return isCallScrollToRight;
    }

    public void setCallScrollToRight(boolean callScrollToRight) {
        isCallScrollToRight = callScrollToRight;
    }

    /**
     * 进禁止左/右滑动
     */
//    boolean isCanScroll = true ;
    private float beforeX; // 上一次x坐标
    boolean isCallScrollToLeft = true; // 可以手指左滑
    boolean isCallScrollToRight = true; // 可以手指右滑

    //-----禁止左滑-------左滑：上一次坐标 > 当前坐标
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isCallScrollToLeft && isCallScrollToRight) {
            return super.dispatchTouchEvent(ev);
        } else {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN://按下如果‘仅’作为‘上次坐标’，不妥，因为可能存在左滑，motionValue大于0的情况（来回滑，只要停止坐标在按下坐标的右边，左滑仍然能滑过去）
                    beforeX = ev.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float motionValue = ev.getX() - beforeX;
                    if ((motionValue < 0 && !isCallScrollToLeft) || (motionValue > 0 && !isCallScrollToRight)) { // 禁止手指左滑、禁止手指右滑
                        return false;
                    }
//                    KLog.d( "值：" + motionValue  );
                    beforeX = ev.getX();//手指移动时，再把当前的坐标作为下一次的‘上次坐标’，解决上述问
                    break;
                default:
                    break;
            }
            return super.dispatchTouchEvent(ev);
        }
    }


}
