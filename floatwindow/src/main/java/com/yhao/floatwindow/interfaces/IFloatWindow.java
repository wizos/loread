package com.yhao.floatwindow.interfaces;

import com.yhao.floatwindow.constant.Screen;

/**
 * Created by yhao on 2017/12/22.
 * https://github.com/yhaolpz
 */

public interface IFloatWindow {
    void showByUser();

    void hideByUser();

    boolean isShowing();

    int getX();

    int getY();

    void updateX(int x);

    void updateX(@Screen.screenType int screenType, float ratio);

    void updateY(int y);

    void updateY(@Screen.screenType int screenType, float ratio);

    void dismiss();
}
