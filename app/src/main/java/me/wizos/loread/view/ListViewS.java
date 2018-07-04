package me.wizos.loread.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ditclear.swipelayout.SwipeDragLayout;
import com.socks.library.KLog;

/**
 * Created by Wizos on 2017/12/24.
 */

public class ListViewS extends ListView implements Handler.Callback, SwipeDragLayout.SwipeListener {

    /* handler */
    private Handler mHandler;

    public ListViewS(Context context) {
        this(context, null);
    }

    public ListViewS(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListViewS(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mHandler = new Handler(this);
    }


    /**
     * 自己写的长点击事件
     */
    /* Handler 的 Message 信息 */
    private static final int MSG_WHAT_LONG_CLICK = 1;

    /* onTouch里面的状态 */
    private static final int STATE_NOTHING = -1;//抬起状态
    private static final int STATE_DOWN = 0;//按下状态
    private static final int STATE_LONG_CLICK = 1;//长点击状态
    private static final int STATE_SCROLL = 2;//SCROLL状态
    private static final int STATE_LONG_CLICK_FINISH = 3;//长点击已经触发完成
    private static final int STATE_MORE_FINGERS = 4;//多个手指
    private int mState = STATE_NOTHING;
    private OnListItemLongClickListener mOnListItemLongClickListener;
    public void setOnListItemLongClickListener(OnListItemLongClickListener listener) {
        mOnListItemLongClickListener = listener;
    }
    public interface OnListItemLongClickListener {
        void onListItemLongClick(View view, int position);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_WHAT_LONG_CLICK:
                //如果得到msg的时候state状态是Long Click的话
                if (mState == STATE_DOWN || mState == STATE_LONG_CLICK) {
                    //改为long click触发完成
                    mState = STATE_LONG_CLICK_FINISH;
                    //得到长点击的位置
                    int position = msg.arg1;
                    //找到那个位置的view
                    View view = getChildAt(position - getFirstVisiblePosition());
                    //如果设置了监听器的话，就触发
                    if (mOnListItemLongClickListener != null && position == pointToPosition(lastX, lastY)) {
                        mOnListItemLongClickListener.onListItemLongClick(view, position);
                        KLog.e("==" + msg.what);
//                        mVibrator.vibrate(100); // 触发震动
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    /* 手指放下的坐标 */
    private int downX;
    private int downY;
    /* Handler 发送message需要延迟的时间 */
    private static final long CLICK_LONG_TRIGGER_TIME = 500;//1s

    /**
     * remove message
     */
    private void removeLongClickMessage() {
        if (mHandler.hasMessages(MSG_WHAT_LONG_CLICK)) {
            mHandler.removeMessages(MSG_WHAT_LONG_CLICK);
        }
    }

    /**
     * send message
     */
    private void sendLongClickMessage(int position) {
        if (!mHandler.hasMessages(MSG_WHAT_LONG_CLICK)) {
            Message message = new Message();
            message.what = MSG_WHAT_LONG_CLICK;
            message.arg1 = position;
            mHandler.sendMessageDelayed(message, CLICK_LONG_TRIGGER_TIME);
        }
    }

//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        switch (ev.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                break;
//            case MotionEvent.ACTION_MOVE:
//                // 容差值大概是24，再加上60
//                if (fingerLeftAndRightMove(ev)) {
//                    return false;
//                }
//                break;
//        }
//        return super.onInterceptTouchEvent(ev);
//    }

    private int slideItemPosition = -1;
    private int lastX;
    private int lastY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        lastX = (int) ev.getX();
        lastY = (int) ev.getY();
        switch (ev.getAction()) { // & MotionEvent.ACTION_MASK
            case MotionEvent.ACTION_DOWN:
                //获取出坐标来
                downX = (int) ev.getX();
                downY = (int) ev.getY();
                //当前state状态为按下
                mState = STATE_DOWN;
                sendLongClickMessage(pointToPosition(downX, downY)); // FIXME: 2016/5/4 【添加】修复 长按 bug
                break;
            // 这个是实现多点的关键，当屏幕检测到有多个手指同时按下之后，就触发了这个事件
            case MotionEvent.ACTION_POINTER_DOWN:
                removeLongClickMessage();
//                mState = STATE_MORE_FINGERS;
                //消耗掉，不传递下去了
                return true;

            case MotionEvent.ACTION_MOVE:
                if (fingerNotMove(ev)) {//手指的范围在50以内
//                   removeLongClickMessage();
//                   return true;
                } else if (fingerLeftAndRightMove(ev)) {
                    removeLongClickMessage();
                    //将当前想要滑动哪一个传递给wrapperAdapter
                    int position = pointToPosition(downX, downY);
                    if (position != AdapterView.INVALID_POSITION) {
                        slideItemPosition = position;
                    }
                } else {
                    removeLongClickMessage();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                removeLongClickMessage();
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

//
//    /**
//     * 是不是向右滑动
//     *
//     * @return
//     */
//    private boolean isFingerMoving2Right(MotionEvent ev) {
//        return (ev.getX() - downX > mShortestDistance);
//    }
//
//    /**
//     * 是不是向左滑动
//     *
//     * @return
//     */
//    private boolean isFingerMoving2Left(MotionEvent ev) {
//        return (ev.getX() - downX < -mShortestDistance);
//    }

//    private boolean fingerUpAndDownMove(MotionEvent ev) {
//        return ((ev.getX() - downX < mShortestDistance && ev.getX() - downX > -mShortestDistance) &&
//                ev.getY() - downY > mShortestDistance || ev.getY() - downY < -mShortestDistance);
//    }


    /* 手指滑动的最短距离 */
    private int mShortestDistance = 25;

    /**
     * 上下左右不能超出50
     *
     * @param ev
     * @return
     */
    private boolean fingerNotMove(MotionEvent ev) {
        return (downX - ev.getX() < mShortestDistance && downX - ev.getX() > -mShortestDistance &&
                downY - ev.getY() < mShortestDistance && downY - ev.getY() > -mShortestDistance);
    }

    /**
     * 左右得超出50，上下不能超出50
     *
     * @param ev
     * @return
     */
    private boolean fingerLeftAndRightMove(MotionEvent ev) {
        return ((ev.getX() - downX > mShortestDistance || ev.getX() - downX < -mShortestDistance) &&
                ev.getY() - downY < mShortestDistance && ev.getY() - downY > -mShortestDistance);
    }

    /**
     * 设置列表项左右滑动时的监听器
     *
     * @param onItemSlideListener
     */
    public void setItemSlideListener(OnItemSlideListener onItemSlideListener) {
        mOnItemSlideListener = onItemSlideListener;
    }

    private OnItemSlideListener mOnItemSlideListener;

    public interface OnItemSlideListener {
        //        int onSlideOpen(View view, int position, int direction); // FIXME: 2016/5/4 【实现划开自动返回】把返回类型由 void 改为 int
        void onUpdate(View view, int position, float offset);
        void onCloseLeft(View view, int position, int direction);
        void onCloseRight(View view, int position, int direction);
        void onClick(View view, int position);
        void log(String layout);
    }


    @Override
    public void onUpdate(View view, float offset) {
        if (mOnItemSlideListener != null) {
            mOnItemSlideListener.onUpdate(view, slideItemPosition, offset);
        }
    }

    @Override
    public void onOpened(View view) {

    }

    @Override
    public void onClosed(View view) {

    }

    @Override
    public void onCloseLeft(View view) {
        if (mOnItemSlideListener != null) {
            mOnItemSlideListener.onCloseLeft(view, slideItemPosition, SwipeDragLayout.DIRECTION_LEFT);
        }
    }

    @Override
    public void onCloseRight(View view) {
        if (mOnItemSlideListener != null) {
//            KLog.e("关闭右侧" + (lastX - downX) + "==" + (lastY - downY));
            mOnItemSlideListener.onCloseRight(view, slideItemPosition, SwipeDragLayout.DIRECTION_RIGHT);
        }
    }

    @Override
    public void onClick(View view) {
        if (mOnItemSlideListener != null) {
            int position = pointToPosition(downX, downY);
            mOnItemSlideListener.onClick(view, position);
        }
    }

    @Override
    public void log(String layout) {
        mOnItemSlideListener.log(layout);
    }
}
