package com.yhao.floatwindow.constant;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by yhao on 2017/12/22.
 * https://github.com/yhaolpz
 */

public class MoveType {
    public static final int fixed = 0;
    // 不可拖动
    public static final int inactive = 1;
    // 可拖动
    public static final int active = 2;
    // 可拖动，释放后自动贴边 （默认）
    public static final int slide = 3;
    // 可拖动，释放后自动回到原位置
    public static final int back = 4;

    @IntDef({fixed, inactive, active, slide, back})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MOVE_TYPE {
    }
}
