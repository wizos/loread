package me.wizos.loread.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;


/**
 *
 * Created by Wizos on 2016/11/6.
 */

public class IconFontView extends TextView {

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
        Typeface iconfont = Typeface.createFromAsset(getContext().getAssets(), "iconfont.ttf");
        this.setTypeface(iconfont);
    }
}
