package me.wizos.loread.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.socks.library.KLog;

import me.wizos.loread.App;

//import com.tencent.smtt.sdk.DownloadListener;
//import com.tencent.smtt.sdk.WebSettings;
//import com.tencent.smtt.sdk.WebView;
//import com.tencent.smtt.sdk.WebViewClient;

/**
 * @author Wizos on 2017/7/3.
 */


public class WebViewX extends WebView {

    @SuppressLint("NewApi")
    public WebViewX(Context activity) {
        // 传入 application context 来防止 activity 引用被滥用。
        // 创建 WebView 传的是 Application ， Application 本身是无法弹 Dialog 的 。 所以只能无反应 ！
        // 这个问题解决方案只要你创建 WebView 时候传入 Activity ， 或者 自己实现 onJsAlert 方法即可。

        super(activity);
        initSettingsForWebPage();
//        // 实现 webview 的背景颜色与当前主题色一致
//        Tool.setBackgroundColor(this);


        /**
         * https://www.jianshu.com/p/6e38e1ef203a
         * 让 WebView 支持文件下载，主要思路有：1、跳转浏览器下载；2、使用系统的下载服务；3、自定义下载任务
         */
        this.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                KLog.e("跳转浏览器下载", "contentLength=" + contentLength);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(url));
                App.i().startActivity(intent);
            }
        });

//        // 自动注入，监控 Javascript
//        this.setWebChromeClient(new WebChromeClient() {
//            @Override
//            public void onProgressChanged(WebView webView, int progress) {
//                // 增加Javascript异常监控
//                CrashReport.setJavascriptMonitor(webView, true);
//                super.onProgressChanged(webView, progress);
//            }
//        });

//        this.setWebViewClient(new WebViewClientX(activity));
    }


    // 忽略 SetJavaScriptEnabled 的报错
    @SuppressLint("SetJavaScriptEnabled")
    /**
     * 将本 WEbView 设置为只可查看RSS/易读/WebPage
     */
    private void initSettingsForWebPage() {
        WebSettings webSetting = this.getSettings();
        webSetting.setJavaScriptEnabled(true);

        // 设置使用 宽 的 Viewpoint,默认是false
        // Android browser以及chrome for Android的设置是`true`，而WebView的默认设置是`false`
        // 如果设置为`true`,那么网页的可用宽度为`980px`,并且可以通过 meta data来设置，如果设置为`false`,那么可用区域和WebView的显示区域有关.
        // 设置此属性，可任意比例缩放
        webSetting.setUseWideViewPort(false);
        // 缩放至屏幕的大小，如果webview内容宽度大于显示区域的宽度,那么将内容缩小,以适应显示区域的宽度, 默认是false
        webSetting.setLoadWithOverviewMode(true);
        // 设置可以支持缩放
        webSetting.setSupportZoom(false);
        // 默认的缩放控制器
        webSetting.setBuiltInZoomControls(false);
        // 默认的+/-缩放控制
        webSetting.setDisplayZoomControls(false);
        // NARROW_COLUMNS 适应内容大小 ， SINGLE_COLUMN 自适应屏幕
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        // 设置渲染线程的优先级。该方法在 Api 18之后被废弃,优先级由WebView自己管理。不过任然建议将其设置为 HIGH,来提高页面渲染速度
        webSetting.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // 支持通过js打开新的窗口
        webSetting.setJavaScriptCanOpenWindowsAutomatically(false);
//        webSetting.setBlockNetworkImage(true);
        // 设置在页面装载完成之后再去加载图片
//        webSetting.setLoadsImagesAutomatically(false);
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


    @Override
    public void destroy() {
        // 链接：http://www.jianshu.com/p/3e8f7dbb0dc7
        // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再 destory()
        ViewParent parent = this.getParent();
        if (parent != null) {
            ((ViewGroup) parent).removeView(this);
        }

        stopLoading();
        // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
        getSettings().setJavaScriptEnabled(false);
        clearCache(false);
        clearHistory();
        loadData("", "text/html", "UTF-8");
        removeAllViews();
//        setWebViewClient(null);
        super.destroy();
    }


    public void clear() {
        removeJavascriptInterface("ImageBridge");
//        setWebViewClient(null);
        loadData("", "text/html", "UTF-8");
        removeAllViews();
        clearHistory();
    }


    public void setData(String baseUrl, String content) {
        // setBlockNetworkImage ( false )调用postSync，这需要时间，因此我们首先清理html，然后更改值
//        loadData("", "text/html", "UTF-8" );
//        getSettings().setBlockNetworkImage(false);
        // do not put 'null' to the base url...
        loadDataWithBaseURL(baseUrl, content, "text/html", "UTF-8", null);
    }


}
