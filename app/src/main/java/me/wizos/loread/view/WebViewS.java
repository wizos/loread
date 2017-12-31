package me.wizos.loread.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.socks.library.KLog;
import com.tencent.bugly.crashreport.CrashReport;

import me.wizos.loread.App;
import me.wizos.loread.utils.Tool;

//import com.tencent.smtt.sdk.DownloadListener;
//import com.tencent.smtt.sdk.WebSettings;
//import com.tencent.smtt.sdk.WebView;
//import com.tencent.smtt.sdk.WebViewClient;

/**
 * Created by Wizos on 2017/7/3.
 */


public class WebViewS extends WebView {

    @SuppressLint("NewApi")
    public WebViewS(Activity activity) { // 传入 application context 来防止 activity 引用被滥用。
        super(activity);
        initWebViewSettings();
        Tool.setBackgroundColor(this); // 实现 webview 的背景颜色与当前主题色一致
        /**
         * 添加javascriptInterface
         * 第一个参数：这里需要一个与js映射的java对象
         * 第二个参数：该java对象被映射为js对象后在js里面的对象名，在js中要调用该对象的方法就是通过这个来调用
         */
        this.addJavascriptInterface(activity, "JSBridge");
        this.setDownloadListener(new WebViewDownLoadListener());
        // 自动注入，监控 Javascript
        this.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView webView, int progress) {
                // 增加Javascript异常监控
                CrashReport.setJavascriptMonitor(webView, true);
                super.onProgressChanged(webView, progress);
            }
        });
    }

    // 忽略 SetJavaScriptEnabled 的报错
    @SuppressLint("SetJavaScriptEnabled")
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
//        webSetting.setDatabaseEnabled(true); // 支持javascript读写db
//        webSetting.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
//        webSetting.setCacheMode(WebSettings.LOAD_DEFAULT);
//        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
//        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension

//        this.evaluateJavascript();//  Android 4.4之后使用evaluateJavascript调用有返回值的JS方法
    }

    private class WebViewDownLoadListener implements DownloadListener {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
//            KLog.e("Loread下载", "url=" + url + "   userAgent=" + userAgent + "   contentDisposition=" + contentDisposition);
//            KLog.e("Loread下载", "mimetype=" + mimetype);
            KLog.e("Loread下载", "contentLength=" + contentLength);
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            App.i().startActivity(intent);
        }
    }

    @Override
    public void destroy() {
        // 链接：http://www.jianshu.com/p/3e8f7dbb0dc7
        // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再 destory()
        ViewParent parent = this.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(this);
        }

        stopLoading();
//        // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
        getSettings().setJavaScriptEnabled(false);
        clearCache(false);
        clearHistory();
        loadUrl("about:blank");
        removeAllViews();

//        clearCache(false);
//        clearHistory();
//        removeAllViews();
//        setWebViewClient(null);
        super.destroy();
    }


    public void clear() {
        removeAllViews();
//        loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
//        clearHistory();
        delayTimestamp = 0;
    }

    public void exe(final String paramString, final String... paramVarArgs) {
        post(new Runnable() {
            public void run() {
                String str = "'" + TextUtils.join("','", paramVarArgs) + "'";
                str = "javascript:" + paramString + "(" + str + ");";
                WebViewS.this.loadUrl(str);
            }
        });
    }


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
