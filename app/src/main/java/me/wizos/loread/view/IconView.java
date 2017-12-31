package me.wizos.loread.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.wizos.loread.R;

/**
 * Created by Wizos on 2017/10/22.
 */

public class IconView extends RelativeLayout {
    public IconView(Context context) {
        super(context);
        initView(context);
    }

    public IconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTypedAttrs(context, attrs);
        initView(context);
    }

    public IconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTypedAttrs(context, attrs);
        initView(context);
    }

    private IconFontView icon;
    private TextView title;

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.cv_icon, this, true);
        icon = (IconFontView) findViewById(R.id.iv_icon);
        title = (TextView) findViewById(R.id.iv_title);
        icon.setText(mIconText);
        icon.setTextColor(mIconColor);
        icon.setTextSize(mIconSize);
        title.setText(mTitleText);
        title.setTextColor(mTitleColor);
        title.setTextSize(mTitleSize);
    }

    private int mIconColor = Color.BLUE;
    private int mTitleColor = Color.WHITE;
    private String mIconText;
    private String mTitleText;

    private float mIconSize;
    private float mTitleSize;

    private void initTypedAttrs(Context context, AttributeSet attrs) {
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.IconView);
        mIconText = mTypedArray.getString(R.styleable.IconView_icon_text);
        mIconColor = mTypedArray.getColor(R.styleable.IconView_icon_color, Color.BLACK);
        mIconSize = mTypedArray.getDimension(R.styleable.IconView_icon_size, 28);
        mTitleColor = mTypedArray.getColor(R.styleable.IconView_title_color, Color.BLACK);
        mTitleText = mTypedArray.getString(R.styleable.IconView_title_text);
        mTitleSize = mTypedArray.getDimension(R.styleable.IconView_icon_size, 12);
        //获取资源后要及时回收
        mTypedArray.recycle();
    }

}
