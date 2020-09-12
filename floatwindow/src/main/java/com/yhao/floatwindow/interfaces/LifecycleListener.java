package com.yhao.floatwindow.interfaces;

/**
 *
 * @author yhao
 * @date 2017/12/22
 * https://github.com/yhaolpz
 */

public interface LifecycleListener {

    void onShow();

    void onHide();

    void onBackToDesktop();

    void onPortrait();

    void onLandscape();
}
