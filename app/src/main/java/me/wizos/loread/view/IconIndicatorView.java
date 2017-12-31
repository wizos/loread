package me.wizos.loread.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.wizos.loread.R;

/**
 * Created by Wizos on 2017/10/22.
 */

public class IconIndicatorView extends RelativeLayout {
    private final CompositeClickListener compositeClickListener = new CompositeClickListener();

    public IconIndicatorView(Context context) {
        super(context);
        initView(context);
    }

    public IconIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTypedArray(context, attrs);
        initView(context);
    }

    public IconIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTypedArray(context, attrs);
        initView(context);
    }

    {
//        super.setOnClickListener(compositeClickListener);
    }


    View indicator;
    IconFontView iconFontView;

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.cv_icon_indicator, this, true);
        iconFontView = (IconFontView) findViewById(R.id.tab_icon);
        indicator = findViewById(R.id.tab_indicator);

        iconFontView.setText(mIcon);
        iconFontView.setTextColor(mIconColor);
        // mTypedArray.getDimension(R.styleable.titleView_titleTextSize,0);返回的结果是dp/pt转px后的大小，但是titleView.setTextSize()是根据sp显示的。会导致显示的偏大，所以要在这里指定其单位为px
        iconFontView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mIconSize);
        indicator.setBackgroundColor(mIndicatorColor);
        addOnClickListener(defaultOnClickListener);
    }

    private String mIcon;
    private float mIconSize;
    private int mIconColor = Color.BLUE;
    private int mIndicatorColor = Color.BLUE;

    private void initTypedArray(Context context, AttributeSet attrs) {
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.IconIndicatorView);
        mIcon = mTypedArray.getString(R.styleable.IconIndicatorView_icon_text);
        mIconColor = mTypedArray.getColor(R.styleable.IconIndicatorView_icon_color, Color.BLACK);
        mIconSize = mTypedArray.getDimension(R.styleable.IconIndicatorView_icon_size, 12);
        mIndicatorColor = mTypedArray.getColor(R.styleable.IconIndicatorView_indicator_color, Color.BLACK);

        //获取资源后要及时回收
        mTypedArray.recycle();
    }


    @Override
    public void setOnClickListener(final OnClickListener listener) {
//        addOnClickListener(listener);
//        addOnClickListener(defaultOnClickListener);
    }

    OnClickListener defaultOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (indicator.getVisibility() != GONE) {
                indicator.setVisibility(GONE);
            } else {
                indicator.setVisibility(VISIBLE);
            }
        }
    };

    /**
     * 实现可以添加多个点击事件
     */
    public void addOnClickListener(OnClickListener listener) {
        compositeClickListener.addOnClickListener(listener);
    }

    public void removeOnClickListener(OnClickListener listener) {
        compositeClickListener.removeOnClickListener(listener);
    }

    private class CompositeClickListener implements OnClickListener {
        private final List<OnClickListener> clickListenerList = new ArrayList<>();

        public void addOnClickListener(OnClickListener listener) {
            if (listener == null) {
                return;
            }
            for (OnClickListener clickListener : clickListenerList) {
                if (listener == clickListener) {
                    return;
                }
            }
            clickListenerList.add(listener);
        }

        public void removeOnClickListener(OnClickListener listener) {
            if (listener == null) {
                return;
            }
            Iterator<OnClickListener> iterator = clickListenerList.iterator();
            while (iterator.hasNext()) {
                OnClickListener clickListener = iterator.next();
                if (listener == clickListener) {
                    iterator.remove();
                    return;
                }
            }
        }

        @Override
        public void onClick(View view) {
            List<OnClickListener> listeners = new ArrayList<>(clickListenerList);
            for (OnClickListener listener : listeners) {
                listener.onClick(view);
            }
        }
    }
}
