/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-03-14 01:32:55
 */

package me.wizos.loread.utils;

import android.content.Context;
import android.util.TypedValue;

public class ColorfulUtils {
    public static int getColor(Context mActivity, int mAttrResId){
        TypedValue typedValue = new TypedValue();
        mActivity.getTheme().resolveAttribute(mAttrResId, typedValue, true);
        return typedValue.data;
    }
}
