package me.wizos.loread.utils;

/**
 * @author by Wizos on 2018/6/29.
 */

public class InjectUtil {
    /**
     * 注入全屏Js，对不同的视频网站分析相应的全屏控件——class标识
     *
     * @param url 加载的网页地址
     * @return 注入的js内容，若不是需要适配的网址则返回空javascript
     */
    public static String fullScreenJsFun2(String url) {
        String fullScreenFlags = null;
        if (url.contains("letv")) {
            fullScreenFlags = "hv_ico_screen";
        } else if (url.contains("youku")) {
            fullScreenFlags = "x-zoomin";
        } else if (url.contains("bilibili")) {
            fullScreenFlags = "icon-widescreen";
        } else if (url.contains("qq")) {
//            fullScreenFlags = "txp_btn_fullscreen";
            fullScreenFlags = "tvp_fullscreen_button";
        } else if (url.contains("sohu")) {
            fullScreenFlags = "x-fs-btn";
        }
        if (null != fullScreenFlags) {
            return "javascript:document.getElementsByClassName('" + fullScreenFlags + "')[0].addEventListener('click',function(){VideoBridge.toggleScreenOrientation();return false;});";
        } else {
            return "javascript:";
        }
    }

    public static String fullScreenJsFun(String url) {
        String command = null;
        if (url.contains("letv")) {
            command = toggleScreenOrientationCommand("hv_ico_screen");
        } else if (url.contains("youku")) {
            command = toggleScreenOrientationCommand("x-zoomin");
        } else if (url.contains("bilibili")) {
            command = toggleScreenOrientationCommand("icon-widescreen");
        } else if (url.contains("qq")) {
            command = toggleScreenOrientationCommand("txp_btn_fullscreen") + toggleScreenOrientationCommand("tvp_fullscreen_button");
        } else if (url.contains("sohu")) {
            command = toggleScreenOrientationCommand("x-fs-btn");
        }
        if (null != command) {
            return "javascript:" + command;
        } else {
            return "javascript:";
        }
    }

    private static String toggleScreenOrientationCommand(String fullScreenFlags) {
        return "document.getElementsByClassName('" + fullScreenFlags + "')[0].addEventListener('click',function(){VideoBridge.toggleScreenOrientation();return false;});";
    }
}
