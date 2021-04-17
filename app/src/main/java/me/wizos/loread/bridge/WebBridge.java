package me.wizos.loread.bridge;

import me.wizos.loread.Contract;

/**
 * @author by Wizos on 2018/6/29.
 */
public interface WebBridge {
    String TAG = "LoreadBridge";
    String COMMEND_PRINT_HTML = "javascript: (function(){" + TAG + ".log(document.documentElement.outerHTML);})(); ";
    String COMMEND_LOAD_MEDIA_CONTROLS = "javascript: (function(){ console.log('在页面加载结束后引入脚本');" +
            "var link = document.createElement('link');link.setAttribute('rel', 'stylesheet');link.setAttribute('type', '" + Contract.TEXT_CSS + "');link.setAttribute('href', '" + Contract.LOREAD_WIZOS_ME + Contract.PATH_PLYR_LITE+ "');document.head.appendChild(link);" +
            "var iconEl = document.createElement('script');iconEl.setAttribute('type', '" + Contract.TEXT_JS + "');iconEl.setAttribute('src', '" + Contract.LOREAD_WIZOS_ME + Contract.PATH_ICONFONT + "');document.body.appendChild(iconEl);" +
            "var zeptoEl = document.createElement('script');zeptoEl.setAttribute('type', '" + Contract.TEXT_JS + "');zeptoEl.setAttribute('src', '" + Contract.LOREAD_WIZOS_ME + Contract.PATH_ZEPTO + "');document.body.appendChild(zeptoEl);" +
            "var controlsEl = document.createElement('script');controlsEl.setAttribute('type', '" + Contract.TEXT_JS + "');controlsEl.setAttribute('src', '" + Contract.LOREAD_WIZOS_ME + Contract.PATH_MEDIA_CONTROLS + "');document.body.appendChild(controlsEl);" +
            "})(); ";

    // paramAnonymousWebView.loadUrl("javascript: (function(){var link = document.createElement('link');link.setAttribute('rel', 'stylesheet');link.setAttribute('type', 'text/css');link.setAttribute('href', 'https://loread.wizos.me/css/plyr_lite.css');document.head.appendChild(link); " +
    //         "var iconScript=document.createElement('script');iconScript.setAttribute('type', 'text/javascript');iconScript.setAttribute('src', 'https://loread.wizos.me/js/iconfont.js');document.body.appendChild(iconScript); " +
    //         "var zeptoScript=document.createElement('script');zeptoScript.setAttribute('type', 'text/javascript');zeptoScript.setAttribute('src', 'https://loread.wizos.me/js/zepto.min.js');document.body.appendChild(zeptoScript);" +
    //         "var resizeScript=document.createElement('script');resizeScript.setAttribute('type', 'text/javascript');resizeScript.setAttribute('src', 'https://loread.wizos.me/js/size-observer.js');document.body.appendChild(resizeScript);" +
    //         " var mediaScript=document.createElement('script');mediaScript.setAttribute('type', 'text/javascript');mediaScript.setAttribute('src', 'https://loread.wizos.me/js/media_controls.js');document.body.appendChild(mediaScript);})();");
    // String COMMEND = "javascript:window.onload = function(){ReadabilityBridge.getHtml(document.documentElement.outerHTML);void(0);}";


    void log(String msg);
    void postVideoPortrait(boolean isPortrait);
    void foundAudio(String src, long duration);
    void foundVideo(String src, long duration);
    void toggleScreenOrientation();

    // class Video {
    //     /**
    //      * 注入全屏Js，对不同的视频网站分析相应的全屏控件——class标识
    //      *
    //      * @param url 加载的网页地址
    //      * @return 注入的js内容，若不是需要适配的网址则返回空javascript
    //      */
    //     public static String fullScreenJsFun(String url) {
    //         String fullScreenFlags = null;
    //         // http://v.qq.com/txp/iframe/player.html?vid=v0151eygqka、http://v.qq.com/iframe/player.html?vid=v0151eygqka
    //         if (url.contains("qq.com/txp/iframe/player.html")) {
    //             fullScreenFlags = "txp_btn_fullscreen";
    //         } else if (url.contains("sohu")) {
    //             fullScreenFlags = "x-fs-btn";
    //         } else if (url.contains("letv")) {
    //             fullScreenFlags = "hv_ico_screen";
    //         }
    //         if (null != fullScreenFlags) {
    //             return "javascript:document.getElementsByClassName('" + fullScreenFlags + "')[0].addEventListener('click',function(){" + WebBridge.TAG + ".toggleScreenOrientation();return false;});";
    //         } else {
    //             return "javascript:";
    //         }
    //     }
    // }
}
