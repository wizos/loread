package me.wizos.loread.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.socks.library.KLog;

import me.wizos.loread.App;
import me.wizos.loread.utils.Tool;

//import com.tencent.smtt.sdk.DownloadListener;
//import com.tencent.smtt.sdk.WebSettings;
//import com.tencent.smtt.sdk.WebView;
//import com.tencent.smtt.sdk.WebViewClient;

/**
 * Created by Wizos on 2017/7/3.
 */


public class X5WebView extends WebView {
    @SuppressLint("SetJavaScriptEnabled")
//    WeakReference<Activity> WRArticle;
//        WRArticle = new WeakReference<>(activity);
    public X5WebView(Activity activity) {
        super(activity);
        initWebViewSettings();
        Tool.setBackgroundColor(this); // 实现 webview 的背景颜色与当前主题色一致
        this.addJavascriptInterface(activity, "JSBridge");
        this.setDownloadListener(new WebViewDownLoadListener());
    }

    private void initWebViewSettings() {
        WebSettings webSetting = this.getSettings();
        webSetting.setJavaScriptEnabled(true);
        // 设置使用 宽 的 Viewpoint,默认是false
        // Android browser以及chrome for Android的设置是`true`，而WebView的默认设置是`false`
        // 如果设置为`true`,那么网页的可用宽度为`980px`,并且可以通过 meta data来设置，如果设置为`false`,那么可用区域和WebView的显示区域有关.
        webSetting.setUseWideViewPort(false);// 设置此属性，可任意比例缩放
        webSetting.setLoadWithOverviewMode(true); // 缩放至屏幕的大小，如果webview内容宽度大于显示区域的宽度,那么将内容缩小,以适应显示区域的宽度, 默认是false
        webSetting.setSupportZoom(false);// 设置可以支持缩放
        webSetting.setBuiltInZoomControls(false); // 默认的缩放控制器
        webSetting.setDisplayZoomControls(false); // 默认的+/-缩放控制
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // NARROW_COLUMNS 适应内容大小 ， SINGLE_COLUMN 自适应屏幕
        // 设置渲染线程的优先级。该方法在 Api 18之后被废弃,优先级由WebView自己管理。不过任然建议将其设置为 HIGH,来提高页面渲染速度
        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(false); // 支持通过js打开新的窗口
//        webSetting.setLoadsImagesAutomatically(false); // 设置在页面装载完成之后再去加载图片
//        webSetting.setAllowFileAccess(true); // 允许访问文件
//        webSetting.setSupportMultipleWindows(true);
//        webSetting.setGeolocationEnabled(true);
//        webSetting.setAppCacheEnabled(true); // 支持H5的 application cache 的功能
//        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
//        webSetting.setDomStorageEnabled(true); // Dom Storage（Web Storage）存储，临时简单的缓存
//        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null); // 硬件加速
        // webSetting.setDatabaseEnabled(true); // 支持javascript读写db
        // webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
//        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension

//        this.evaluateJavascript();//  Android 4.4之后使用evaluateJavascript调用有返回值的JS方法
    }

    private class WebViewDownLoadListener implements DownloadListener {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            KLog.e("Loread下载", "url=" + url + "   userAgent=" + userAgent + "   contentDisposition=" + contentDisposition);
            KLog.e("Loread下载", "mimetype=" + mimetype);
            KLog.e("Loread下载", "contentLength=" + contentLength);
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            App.i().startActivity(intent);
        }
    }


//    // 这段函数很耗时，会造成页面卡顿
//    private void initImgPlace(WebView webView) {
//        final ArrayMap<Integer, Img> imgMap = WithDB.i().getImgs((String) webView.getTag());
//        final ArrayMap<Integer, Img> lossImgMap = WithDB.i().getLossImgs((String) webView.getTag()); // dataList.get(webView.getId()).getId()
//        int lossImgSize = lossImgMap.size();
//
//        if (lossImgSize == imgMap.size()) {
//            webView.loadUrl("javascript:initImgPlaceholder()"); // 初始化占位图
//        } else {
//            StringBuilder imgNoArray = new StringBuilder("");
//            for (int i = 0; i < lossImgSize; i++) {
//                imgNoArray.append(lossImgMap.keyAt(i) - 1); // imgState 里的图片下标是从1开始的
//                imgNoArray.append("_");
//            }
//            KLog.i("传递的值" + imgNoArray + webView);
//            webView.loadUrl("javascript:appointImgPlaceholder(" + "\"" + imgNoArray.toString() + "\"" + ")");
//        }
//    }

    @Override
    public void destroy() {
        loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        clearHistory();
        removeAllViews();
        setWebViewClient(null);
        super.destroy();
    }


    public void clear() {
//        loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
//        clearHistory();
        delayTimestamp = 0;
    }

    public void exe(final String paramString, final String... paramVarArgs) {
        post(new Runnable() {
            public void run() {
                String str = "'" + TextUtils.join("','", paramVarArgs) + "'";
                str = "javascript:" + paramString + "(" + str + ");";
                X5WebView.this.loadUrl(str);
            }
        });
    }


    //    private static int duringTime = 300;
    private long delayTimestamp = 0; // 上一个执行（更新图片）的时间

    /**
     * 获取到延迟执行的时间
     *
     * @return Delayed 延迟执行的时间（毫秒）
     */
    public long getDelayTime() {
        long currentTimeMillis = System.currentTimeMillis();
        long delayTime;
        int duringTime = 2000;
        /* 方法一：细化为2个阶段
        // 1,currentTimeMillis - delayTimestamp > 0
         nextDelayTimestamp = currentTimeMillis + 800
         delayTime =  nextDelayTimestamp - currentTimeMillis = 800
        // 2,currentTimeMillis - delayTimestamp < 0
         nextDelayTimestamp = lastDelayTimestamp + 800
         delayTime =  nextDelayTimestamp - currentTimeMillis
         */
        if (currentTimeMillis > delayTimestamp) {
            delayTimestamp = currentTimeMillis + duringTime;
            delayTime = duringTime;
        } else {
            delayTimestamp = delayTimestamp + duringTime;
            delayTime = delayTimestamp - currentTimeMillis;
        }
        KLog.e("获取的延期时间：" + this.getId() + "***" + currentTimeMillis + " == " + delayTime + " -- " + delayTimestamp);
        return delayTime;

        /* 方法二：细化为3个阶段（感觉不用分这么细致，反而会增加计算量，所以暂时弃用）
        // 1,currentTimeMillis - delayTimestamp 结果 x > 800
         nextDelayTimestamp = currentTimeMillis + 800
         delayTime =  800
        // 2,currentTimeMillis - delayTimestamp 结果  0 < x < 800
         nextDelayTimestamp = lastDelayTimestamp + 800
         delayTime =  nextDelayTimestamp - currentTimeMillis
        // 3,currentTimeMillis - delayTimestamp 结果 x < 0
         nextDelayTimestamp = lastDelayTimestamp + 800
         delayTime =  nextDelayTimestamp - currentTimeMillis
         */
    }


}
