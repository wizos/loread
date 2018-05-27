package me.wizos.loread.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;

import com.socks.library.KLog;

import me.wizos.loread.App;
import me.wizos.loread.db.Article;
import me.wizos.loread.utils.StringUtil;


/**
 * @author Wizos on 2017/7/3.
 * NestedScroll
 */


public class WebViewS extends NestedScrollWebView {

    @SuppressLint("NewApi")
    public WebViewS(Context activity) {
        // 传入 application context 来防止 activity 引用被滥用。
        // 创建 WebView 传的是 Application ， Application 本身是无法弹 Dialog 的 。 所以只能无反应 ！
        // 这个问题解决方案只要你创建 WebView 时候传入 Activity ， 或者 自己实现 onJsAlert 方法即可。

        super(activity);
        initSettingsForWebPage();

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
//                activity继承了context重载了startActivity方法,如果使用acitvity中的startActivity，不会有任何限制。
//                而如果直接使用context的startActivity则会报上面的错误，根据错误提示信息,可以得知,如果要使用这种方式需要打开新的TASK。
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                App.i().startActivity(intent);
            }
        });
    }



    // 忽略 SetJavaScriptEnabled 的报错
    @SuppressLint("SetJavaScriptEnabled")
    private void initSettingsForWebPage() {

        // 实现 webview 的背景颜色与当前主题色一致
//        Tool.setBackgroundColor(this);
        // 先设置背景色为tranaparent 透明色
        this.setBackgroundColor(0);
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
        webSetting.setAllowFileAccess(true); // 允许访问文件
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
        webSetting.setDomStorageEnabled(true);//临时简单的缓存（必须保留，否则无法播放优酷视频网页，其他的可以）


//        webSetting.setBlockNetworkImage(true);
        // 设置在页面装载完成之后再去加载图片
//        webSetting.setLoadsImagesAutomatically(false);
//        webSetting.setAllowFileAccess(true); // 允许访问文件
//        webSetting.setSupportMultipleWindows(true);
//        webSetting.setGeolocationEnabled(true);
//        webSetting.setAppCacheEnabled(true); // 支持H5的 application cache 的功能
//        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
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
        removeAllViews();
//        loadData("", "text/html", "UTF-8");
//        setWebViewClient(null);
        super.destroy();
    }


    public void clear() {
        removeJavascriptInterface("ImageBridge");
        loadData("", "text/html", "UTF-8");
        removeAllViews();
        clearHistory();
//        setWebViewClient(null);
    }

    private String firstUrl;
    private String initialBaseUrl;
    private String initialContent;
    private final String initialUrl = "loread://initial.html";


    /**
     * 为了页面能正常回退到 加载自制内容的 页面，所以在第一次跳转url内容时，记录下url
     *
     * @param firstUrl
     */
    public void setFirstUrl(String firstUrl) {
        if (TextUtils.isEmpty(this.firstUrl)) {
            this.firstUrl = firstUrl;
        }
    }


    @Override
    public boolean canGoBack() {
        if (this.getUrl().equals(initialUrl)) {
            return false;
        }
        return super.canGoBack();
    }

    @Override
    public void goBack() {
        KLog.e("当前url：" + this.getUrl() + "   第一次跳出的url：" + firstUrl);
        if (this.getUrl().equals(firstUrl)) {
            loadArticlePage(article);
//            loadDataWithBaseURL(App.webViewBaseUrl, StringUtil.getHtml(article), "text/html", "UTF-8", this.initialUrl);
            firstUrl = null;
        } else {
            super.goBack();
        }
    }


    /**
     * 不要将 base url 设为 null
     */
    public void loadDataWithBaseURL(String baseUrl, String content) {
        String initialUrl = null;
        if (initialContent == null) {
            this.initialBaseUrl = App.webViewBaseUrl;
            this.initialContent = TextUtils.isEmpty(content) ? "" : content;
            initialUrl = this.initialUrl;
        }
        loadDataWithBaseURL(App.webViewBaseUrl, content, "text/html", "UTF-8", initialUrl);
    }


    public void loadHolderPage() {
        loadDataWithBaseURL(App.webViewBaseUrl, "", "text/html", "UTF-8", null);
        clearHistory();
    }

    private Article article;

    public void loadArticlePage(Article article) {
//        this.article = article;
        loadDataWithBaseURL(App.webViewBaseUrl, StringUtil.getHtml(article), "text/html", "UTF-8", this.initialUrl);
    }

    public void loadArticlePage(Article article, String content) {
        loadDataWithBaseURL(App.webViewBaseUrl, StringUtil.getHtml(article, content), "text/html", "UTF-8", this.initialUrl);
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }
}
