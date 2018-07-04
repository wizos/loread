package me.wizos.loread.view;

import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by Wizos on 2018/6/10.
 */

public class TapHelper implements Handler.Callback {
    private Handler mHandler;
    private static final int MSG_WHAT_LONG_CLICK = 1;
    /* Handler 发送message需要延迟的时间 */
    private static final long CLICK_LONG_TRIGGER_TIME = 500;//1s
    private OnTapListener tapListener;
    private View view;
    private Message message;

    TapHelper(View view, OnTapListener tapListener) {
        this.view = view;
        this.tapListener = tapListener;
        mHandler = new Handler(this);
    }


    public void sendLongClickMessage() {
        if (!mHandler.hasMessages(MSG_WHAT_LONG_CLICK)) {
            message = Message.obtain();
            message.what = MSG_WHAT_LONG_CLICK;
            mHandler.sendMessageDelayed(message, CLICK_LONG_TRIGGER_TIME);
        }
    }

    public void removeLongClickMessage() {
        if (mHandler.hasMessages(MSG_WHAT_LONG_CLICK)) {
            mHandler.removeMessages(MSG_WHAT_LONG_CLICK);
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_WHAT_LONG_CLICK:
                //如果得到msg的时候state状态是Long Click的话
                //如果设置了监听器的话，就触发
                if (tapListener != null && isNotMove() && lastTime > downTime) {
                    tapListener.onLongTap((DragPhotoView) view);
                }
                break;
            default:
                break;
        }
        return true;
    }


    private int lastX;
    private int lastY;
    private int downX;
    private int downY;
    private long downTime = 0;
    private long lastTime = 0;
    /* 手指滑动的最短距离 */
    private int mShortestDistance = 25;

    /**
     * 上下左右不能超出50
     *
     * @return
     */
    private boolean isNotMove() {
        return (downX - lastX < mShortestDistance && downX - lastX > -mShortestDistance &&
                downY - lastY < mShortestDistance && downY - lastY > -mShortestDistance);
    }

    public void onTouch(MotionEvent event) {
//        KLog.e("触发：" +  event.getAction() );
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                downY = (int) event.getY();
                lastTime = 0;
                downTime = System.currentTimeMillis();
                removeLongClickMessage();
                sendLongClickMessage();
                break;
            // 这个是实现多点的关键，当屏幕检测到有多个手指同时按下之后，就触发了这个事件
            case MotionEvent.ACTION_POINTER_DOWN:
                removeLongClickMessage();
                break;
            case MotionEvent.ACTION_MOVE:
                lastX = (int) event.getX();
                lastY = (int) event.getY();
                lastTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_UP:
                removeLongClickMessage();
                break;
            case MotionEvent.ACTION_CANCEL:
                removeLongClickMessage();
                break;
            default:
                break;
        }
    }

    public interface OnTapListener {
        void onLongTap(DragPhotoView view);
    }

}
