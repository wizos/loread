package me.wizos.loread.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.socks.library.KLog;

import me.wizos.loread.App;
import me.wizos.loread.utils.Tool;
import me.wizos.loread.view.webview.NestedScrollWebView;

/**
 * @author Wizos on 2017/7/3.
 * NestedScroll
 */


public class WebViewS extends NestedScrollWebView {
    private boolean isReadability = false;

    @SuppressLint("NewApi")
    public WebViewS(Context context) {
        // 传入 application activity 来防止 activity 引用被滥用。
        // 创建 WebView 传的是 Application ， Application 本身是无法弹 Dialog 的 。 所以只能无反应 ！
        // 这个问题解决方案只要你创建 WebView 时候传入 Activity ， 或者 自己实现 onJsAlert 方法即可。

        super(context);
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
                        Tool.show("长按手机号");
                        break;
                    case WebView.HitTestResult.EMAIL_TYPE: // 处理Email
                        Tool.show("长按邮件");
                        break;
                    case WebView.HitTestResult.GEO_TYPE: // 地图类型
                        Tool.show("长按地图");
                        break;
                    case WebView.HitTestResult.SRC_ANCHOR_TYPE: // 超链接
                        Tool.show("长按超链接：" + result.getExtra());
                        break;
                    case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE: // 一个SRC_IMAGE_ANCHOR_TYPE类型表明了一个拥有图片为子对象的超链接。
                        Tool.show("长按图片：" + result.getExtra());
                        break;
                    case WebView.HitTestResult.IMAGE_TYPE: // 处理长按图片的菜单项
                        // 获取图片的路径
                        String saveImgUrl = result.getExtra();
                        Tool.show("长按图片：" + saveImgUrl);
                        // 跳转到图片详情页，显示图片
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

    }


    public boolean isReadability() {
        return isReadability;
    }

    public void setReadability(boolean readability) {
        isReadability = readability;
    }

    // 忽略 SetJavaScriptEnabled 的报错
    @SuppressLint("SetJavaScriptEnabled")
    private void initSettingsForWebPage() {
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        setScrollbarFadingEnabled(false);

        // 先设置背景色为tranaparent 透明色
        this.setBackgroundColor(0);
        WebSettings webSettings = this.getSettings();
        webSettings.setJavaScriptEnabled(true);

        // 设置自适应屏幕，两者合用
        // 设置使用 宽 的 Viewpoint,默认是false
        // Android browser以及chrome for Android的设置是`true`，而WebView的默认设置是`false`
        // 如果设置为`true`,那么网页的可用宽度为`980px`,并且可以通过 meta data来设置，如果设置为`false`,那么可用区域和WebView的显示区域有关.
        // 设置此属性，可任意比例缩放
        webSettings.setUseWideViewPort(true);
        // 缩放至屏幕的大小：如果webview内容宽度大于显示区域的宽度,那么将内容缩小,以适应显示区域的宽度, 默认是false
        webSettings.setLoadWithOverviewMode(true);

        webSettings.setTextZoom(100);
        // 设置最小的字号，默认为8
        webSettings.setMinimumFontSize(10);
        // 设置最小的本地字号，默认为8
        webSettings.setMinimumLogicalFontSize(10);

        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件


        webSettings.setDefaultTextEncodingName("UTF-8");
        // NARROW_COLUMNS 适应内容大小 ， SINGLE_COLUMN 自适应屏幕
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
//        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

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

        //根据cache-control获取数据。
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webSettings.setMediaPlaybackRequiresUserGesture(true);

        CookieManager instance = CookieManager.getInstance();
        instance.setAcceptCookie(true);
        instance.setAcceptThirdPartyCookies(this, true);


//        setLayerType(View.LAYER_TYPE_SOFTWARE, null);//开启软件加速（在我的MX5上，滑动时会卡顿）
//        setLayerType(View.LAYER_TYPE_HARDWARE, null);//开启硬件加速

        // 将图片下载阻塞，然后在浏览器的OnPageFinished事件中设置webView.getSettings().setBlockNetworkImage(false);
        // 通过图片的延迟载入，让网页能更快地显示。但是会造成懒加载失效，因为懒加载的脚本执行更早，所以认为所有未显示的图片都在同一屏幕内，要开始加载。
//        webSettings.setBlockNetworkImage(true);
        // 设置在页面装载完成之后自动去加载图片
        webSettings.setLoadsImagesAutomatically(true);
//        webSettings.setSupportMultipleWindows(true);
//        webSettings.setGeolocationEnabled(true);
//        webSettings.setAppCacheMaxSize(Long.MAX_VALUE);
//        webSettings.setDatabaseEnabled(true); // 支持javascript读写db
//        webSettings.setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);
//        webSettings.setPluginState(WebSettings.PluginState.ON_DEMAND);
        // this.getSettingsExtension().setPageCacheCapacity(IX5WebSettings.DEFAULT_CACHE_CAPACITY);//extension
//        this.evaluateJavascript();//  Android 4.4之后使用evaluateJavascript调用有返回值的JS方法
    }


    @Override
    public void destroy() {
        // 链接：http://www.jianshu.com/p/3e8f7dbb0dc7
        // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再 destory()。
        // 在关闭了Activity时，如果Webview的音乐或视频，还在播放。就必须销毁Webview。
        // 但注意：webview调用destory时，仍绑定在Activity上，这是由于webview构建时传入了Activity对象。
        // 因此需要先从父容器中移除webview，然后再销毁webview。

//        作者：听话哥
//        链接：https://www.jianshu.com/p/9293505c7f71
//        如果你在调用webview.destory();的时候，如果webview里面还有别的线程在操作，就会导致当前这个webview为空。这时候我们需要结束相应线程。例如我们项目中有一个广告拦截是通过在
//        public void onPageFinished(final WebView view, String url)
//        里面启用一个runnable去执行一串的js脚本，如果用户在你脚本没执行完成的时候就关闭了当前界面，系统就会抛出空指针异常。这时候就需要通过去onPageFinished获取webview对象

        try {
            // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
            getSettings().setJavaScriptEnabled(false);
            stopLoading();
            onPause();
            clearCache(true);
            clearHistory();
//            WebStorage.getInstance().deleteAllData();
            destroyDrawingCache();
            ViewGroup parent = (ViewGroup) this.getParent();
            if (parent != null) {
                parent.removeView(this);
            }
            removeAllViews();
        } catch (Exception e) {
            KLog.e("报错");
            e.printStackTrace();
        }
        super.destroy();
    }


    /**
     * 不要将 base url 设为 null
     */
    public void loadData(String htmlContent) {
        loadDataWithBaseURL(App.webViewBaseUrl, htmlContent, "text/html", "UTF-8", null);
        isReadability = false;
    }


//    private int displayMode = Api.RSS;
//    private String displayContent;
//    public void loadReadability(String htmlContent) {
//        displayMode = Api.READABILITY;
//        displayContent = htmlContent;
//        loadData(htmlContent);
//    }
//    public void loadRSS(String htmlContent) {
//        displayMode = Api.RSS;
//        displayContent = htmlContent;
//        loadData(htmlContent);
//    }
//    public void loadLink(String url) {
//        displayMode = Api.LINK;
//        displayContent = url;
//        loadUrl(url);
//    }


//    public void clear() {
//        ViewParent parent = this.getParent();
//        if (parent != null) {
//            ((ViewGroup) parent).removeView(this);
//        }
//        stopLoading();
//        clearCache(true);
//        clearHistory();
//        removeJavascriptInterface("ImageBridge");
//        post(new Runnable() {
//            @Override
//            public void run() {
//                loadData("", "text/html", "UTF-8");
//            }
//        });
//    }



}
