//package com.yydcdut.sdlv;
//
//import android.view.GestureDetector;
//import android.view.MotionEvent;
//
///**
// * Created by Wizos on 2016/6/14.
// */



//public void setGesture(Context context){
//        mGestureDetector = new GestureDetector(context,new ItemGesture()); //使用派生自OnGestureListener
//        }
//public class ItemGesture implements GestureDetector.OnGestureListener {
//    public boolean onDown(MotionEvent e) {
//        // TODO Auto-generated method stub
//        return false;
//    }
//
//    public void onShowPress(MotionEvent e) {
//        // TODO Auto-generated method stub 如果是按下的时间超过瞬间，而且在按下的时候没有松开或者是拖动的，那么onShowPress就会执行
//    }
//    public void onLongPress(MotionEvent e) {
//        // TODO Auto-generated method stub 长按触摸屏，超过一定时长，就会触发这个事件
//    }
//
////    触发顺序：
////    onDown->onShowPress->onLongPress
//
//    public boolean onSingleTapUp(MotionEvent e) {
//        // TODO Auto-generated method stub
//        return false;
//    }
//
//    public boolean onScroll(MotionEvent e1, MotionEvent e2,
//                            float distanceX, float distanceY) {
//        // TODO Auto-generated method stub
//        return false;
//    }
//
//
//    /**
//     * @param e1 第1个ACTION_DOWN MotionEvent
//     * @param e2 最后一个ACTION_MOVE MotionEvent
//     * @param velocityX X轴上的移动速度，像素/秒
//     * @param velocityY Y轴上的移动速度，像素/秒
//     * @return
//     */
//    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//        // TODO Auto-generated method stub 滑屏，用户按下触摸屏、快速移动后松开，由1个MotionEvent ACTION_DOWN, 多个ACTION_MOVE, 1个ACTION_UP触发
//        return false;
//    }
//}
