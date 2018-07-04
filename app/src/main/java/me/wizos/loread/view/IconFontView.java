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
        Typeface iconFont = Typeface.createFromAsset(getContext().getAssets(), "iconfont.ttf");
        this.setTypeface(iconFont);
    }
}
