package me.wizos.loread.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebSettings;
import android.webkit.WebView;

import me.wizos.loread.App;
import me.wizos.loread.service.NetworkStatus;
import me.wizos.loread.view.webview.NestedScrollWebView;
import me.wizos.loread.view.webview.VideoImpl;

//import com.tencent.smtt.sdk.DownloadListener;
//import com.tencent.smtt.sdk.WebSettings;


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

//        作者：Wing_Li
//        链接：https://www.jianshu.com/p/3fcf8ba18d7f
        setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                HitTestResult result = ((WebViewS) v).getHitTestResult();
                if (null == result) {
                    return false;
                }
                int type = result.getType();
                if (type == WebView.HitTestResult.UNKNOWN_TYPE) {
                    return false;
                }

                // 这里可以拦截很多类型，我们只处理图片类型就可以了
                switch (type) {
                    case WebView.HitTestResult.PHONE_TYPE: // 处理拨号
                        break;
                    case WebView.HitTestResult.EMAIL_TYPE: // 处理Email
                        break;
                    case WebView.HitTestResult.GEO_TYPE: // 地图类型
                        break;
                    case WebView.HitTestResult.SRC_ANCHOR_TYPE: // 超链接
                        break;
                    case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                        break;
                    case WebView.HitTestResult.IMAGE_TYPE: // 处理长按图片的菜单项
                        // 获取图片的路径
                        String saveImgUrl = result.getExtra();

                        // 跳转到图片详情页，显示图片

                        break;
                    default:
                        break;
                }
                return true;
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
        WebSettings webSettings = this.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // 设置使用 宽 的 Viewpoint,默认是false
        // Android browser以及chrome for Android的设置是`true`，而WebView的默认设置是`false`
        // 如果设置为`true`,那么网页的可用宽度为`980px`,并且可以通过 meta data来设置，如果设置为`false`,那么可用区域和WebView的显示区域有关.
        // 设置此属性，可任意比例缩放
        webSettings.setUseWideViewPort(true);
        // 缩放至屏幕的大小，如果webview内容宽度大于显示区域的宽度,那么将内容缩小,以适应显示区域的宽度, 默认是false
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setTextZoom(100);
        // 设置可以支持缩放
        webSettings.setSupportZoom(true);
        // 默认的缩放控制器
        webSettings.setBuiltInZoomControls(true);
        // 默认的+/-缩放控制
        webSettings.setDisplayZoomControls(false);
        // NARROW_COLUMNS 适应内容大小 ， SINGLE_COLUMN 自适应屏幕
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        // 设置渲染线程的优先级。该方法在 Api 18之后被废弃,优先级由WebView自己管理。不过任然建议将其设置为 HIGH,来提高页面渲染速度
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        // 支持通过js打开新的窗口
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);

        /* 缓存 */
        webSettings.setDomStorageEnabled(true); // 临时简单的缓存（必须保留，否则无法播放优酷视频网页，其他的可以）
        webSettings.setAppCacheEnabled(true); // 支持H5的 application cache 的功能
        // webSettings.setDatabaseEnabled(true);  // 支持javascript读写db

        // 允许在Android 5.0上 Webview 加载 Http 与 Https 混合内容。作者：Wing_Li，链接：https://www.jianshu.com/p/3fcf8ba18d7f
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        /* 新增 */
        // 通过 file url 加载的 Javascript 读取其他的本地文件 .建议关闭
        webSettings.setAllowFileAccessFromFileURLs(false);
        // 允许通过 file url 加载的 Javascript 可以访问其他的源，包括其他的文件和 http，https 等其他的源
        webSettings.setAllowUniversalAccessFromFileURLs(false);
        // 允许访问文件
        webSettings.setAllowFileAccess(true);

        // 保存密码数据
        webSettings.setSavePassword(true);
        // 保存表单数据
        webSettings.setSaveFormData(true);

        if (App.networkStatus.equals(NetworkStatus.NETWORK_NONE)) {
            //没网，则从本地获取，即离线加载
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        } else {
            //根据cache-control获取数据。
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }

//        webSettings.setBlockNetworkImage(true);
        // 设置在页面装载完成之后再去加载图片
//        webSettings.setLoadsImagesAutomatically(false);
//        webSettings.setSupportMultipleWindows(true);
//        webSettings.setGeolocationEnabled(true);
//        webSettings.setAppCacheMaxSize(Long.MAX_VALUE);
//        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null); // 硬件加速
//        webSettings.setDatabaseEnabled(true); // 支持javascript读写db
//        webSettings.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
//        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
//        webSettings.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension
//        this.evaluateJavascript();//  Android 4.4之后使用evaluateJavascript调用有返回值的JS方法
    }


    @Override
    public void destroy() {
        // 链接：http://www.jianshu.com/p/3e8f7dbb0dc7
        // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再 destory()
        // 在关闭了Activity时，如果Webview的音乐或视频，还在播放。就必须销毁Webview
        // 但是注意：webview调用destory时,webview仍绑定在Activity上
        // 这是由于自定义webview构建时传入了该Activity的context对象
        // 因此需要先从父容器中移除webview,然后再销毁webview
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
//        loadData("", "text/html", "UTF-8"); // 不能用，会造成内存泄漏
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

    private VideoImpl videoState;

    public boolean isFullScreenPlaying() {
        if (videoState == null) {
            return false;
        }
        return videoState.isPlaying();
    }

    public void setVideoState(VideoImpl videoState) {
        this.videoState = videoState;
    }

    public void onHideCustomView() {
        if (videoState == null) {
            videoState.onHideCustomView();
        }
    }

    /**
     * 不要将 base url 设为 null
     */
    public void loadData(String htmlContent) {
        loadDataWithBaseURL(App.webViewBaseUrl, htmlContent, "text/html", "UTF-8", null);
    }

//    public void loadDataWithBaseURL(String baseUrl, String content) {
//        String initialUrl = null;
//        if (initialContent == null) {
//            this.initialBaseUrl = App.webViewBaseUrl;
//            this.initialContent = TextUtils.isEmpty(content) ? "" : content;
//            initialUrl = this.initialUrl;
//        }
//        loadDataWithBaseURL(App.webViewBaseUrl, content, "text/html", "UTF-8", initialUrl);
//    }
//    public void loadHolderPage() {
//        loadDataWithBaseURL(App.webViewBaseUrl, "", "text/html", "UTF-8", null);
//        clearHistory();
//    }

//    private Article article;

//    public void loadArticlePage(Article article) {
////        this.article = article;
//        loadDataWithBaseURL(App.webViewBaseUrl, StringUtil.getHtmlForDisplay(article), "text/html", "UTF-8", this.initialUrl);
//    }
//    public void loadArticlePage(Article article, String content) {
//        loadDataWithBaseURL(App.webViewBaseUrl, StringUtil.getHtmlForDisplay(article, content), "text/html", "UTF-8", this.initialUrl);
//    }

//    public Article getArticle() {
//        return article;
//    }
//    public void setArticle(Article article) {
//        this.article = article;
//    }
}
