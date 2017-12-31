package me.wizos.loread.view;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;


/**
 *
 * Created by Wizos on 2016/11/6.
 */

public class IconFontView extends AppCompatTextView {

    public IconFontView(Context context) {
        super(context);
        init();
    }

    public IconFontView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IconFontView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
//        super.setOnClickListener(new SenseClickListener());
        Typeface iconFont = Typeface.createFromAsset(getContext().getAssets(), "iconfont.ttf");
        this.setTypeface(iconFont);
    }


//    // 自加。实现双击。由于连续双击时并不会调用2次onGroupClick函数。导致该方法无效。
    // 该方法会使得 在layout 中写的 onClick 属性无效
//    /**
//     * @deprecated 由于该View已经实现了点击与双击的点击监听器 OnTapListener，所以请弃用原始的点击监听器
//     */
//    @Deprecated
//    @Override
//    public void setOnClickListener(OnClickListener listener){
//    }
//    private OnTapListener mOnTapListener;
//    public interface OnTapListener{
//        void onSingleClick( View v );
//        void onDoubleClick( View v );
//    }
//
//    public void setOnTapListener( OnTapListener listener) {
//        this.mOnTapListener = listener;
//    }
//    private class SenseClickListener implements OnClickListener {
//        @Override
//        public void onClick(View v){
//            if(mOnTapListener==null){
//                return;
//            }
//            if (v.getHandler().hasMessages(3608)) {
//                v.getHandler().removeMessages(3608);
//                mOnTapListener.onDoubleClick( v);
//            } else {
//                v.getHandler().sendMessageDelayed(getRunnableMsg(v), 300);// ViewConfiguration.getDoubleTapTimeout()
//            }
//        }
//    }
//    private Message getRunnableMsg( final View v) {
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
//                mOnTapListener.onSingleClick(v);
//            }
//        };
//        Message m = Message.obtain(v.getHandler(),r); // obtain() 从全局池中返回一个新的Message实例。在大多数情况下这样可以避免分配新的对象。
//        m.what = 3608;
//        return m;
//    }

}
