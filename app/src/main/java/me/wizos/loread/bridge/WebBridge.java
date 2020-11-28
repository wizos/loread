package me.wizos.loread.bridge;

/**
 * @author by Wizos on 2018/6/29.
 */
public interface WebBridge {
    String TAG = "WebBridge";
    void log(String msg);
    void toggleScreenOrientation();

    class Video {
        /**
         * 注入全屏Js，对不同的视频网站分析相应的全屏控件——class标识
         *
         * @param url 加载的网页地址
         * @return 注入的js内容，若不是需要适配的网址则返回空javascript
         */
        public static String fullScreenJsFun(String url) {
            String fullScreenFlags = null;
            // http://v.qq.com/txp/iframe/player.html?vid=v0151eygqka、http://v.qq.com/iframe/player.html?vid=v0151eygqka
            if (url.contains("qq.com/txp/iframe/player.html")) {
                fullScreenFlags = "txp_btn_fullscreen";
            } else if (url.contains("sohu")) {
                fullScreenFlags = "x-fs-btn";
            } else if (url.contains("letv")) {
                fullScreenFlags = "hv_ico_screen";
            }
            if (null != fullScreenFlags) {
                return "javascript:document.getElementsByClassName('" + fullScreenFlags + "')[0].addEventListener('click',function(){" + WebBridge.TAG + ".toggleScreenOrientation();return false;});";
            } else {
                return "javascript:";
            }
        }
    }
}
