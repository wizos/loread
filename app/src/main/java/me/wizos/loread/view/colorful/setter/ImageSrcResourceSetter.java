package me.wizos.loread.view.colorful.setter;

import android.content.res.Resources.Theme;
import android.widget.ImageView;

/**
 * View的背景Drawabler Setter
 *
 * @author mrsimple
 */
public final class ImageSrcResourceSetter extends ViewSetter {

    public ImageSrcResourceSetter(ImageView targetView, int resId) {
        super(targetView, resId);
    }


    public ImageSrcResourceSetter(int viewId, int resId) {
        super(viewId, resId);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setValue(Theme newTheme, int themeId) {
        if (mView == null) {
            return;
        }
        ((ImageView)mView).setImageResource(mAttrResId);

        // TypedArray a = newTheme.obtainStyledAttributes(themeId, new int[]{mAttrResId});
        // int attributeResourceId = a.getResourceId(0, 0);
        // Drawable drawable = mView.getResources().getDrawable(attributeResourceId);
        // a.recycle();
        // ((ImageView)mView).setImageDrawable(drawable);
    }

}
