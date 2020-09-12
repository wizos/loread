package com.yhao.floatwindow.interfaces;

/**
 * Created by yhao on 2018/5/5
 * https://github.com/yhaolpz
 */
public interface ViewStateListener {
    void onPositionUpdate(int x, int y);

    void onShow();

    void onHide();

    void onShowByUser();

    void onHideByUser();

    void onDismiss();

    void onMoveAnimStart();

    void onMoveAnimEnd();

    void onBackToDesktop();
}
