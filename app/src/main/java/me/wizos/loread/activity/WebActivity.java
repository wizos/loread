package me.wizos.loread.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.elvishew.xlog.XLog;
import com.google.android.material.appbar.AppBarLayout;
import com.hjq.toast.ToastUtils;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.NestedScrollAgentWebView;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.BuildConfig;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.bridge.WebBridge;
import me.wizos.loread.config.HostBlockConfig;
import me.wizos.loread.config.header_useragent.UserAgentConfig;
import me.wizos.loread.config.url_rewrite.UrlRewriteConfig;
import me.wizos.loread.utils.ScreenUtils;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.webview.DownloadListenerS;
import me.wizos.loread.view.webview.LongClickPopWindow;

import static me.wizos.loread.Contract.SCHEMA_FEEDME;
import static me.wizos.loread.Contract.SCHEMA_HTTP;
import static me.wizos.loread.Contract.SCHEMA_HTTPS;
import static me.wizos.loread.Contract.SCHEMA_LOREAD;
import static me.wizos.loread.Contract.SCHEMA_PALABRE;
import static me.wizos.loread.Contract.SCHEMA_PDY;

/**
 * 内置的 webView 页面，用来相应 a，iframe 的跳转内容
 * @author Wizos
 */
public class WebActivity extends BaseActivity implements WebBridge {
    private AgentWeb agentWeb;
    private Toolbar mToolbar;
    private AppBarLayout appBarLayout;
    private CoordinatorLayout containerLayout;
    private String originalUrl;
    private String receivedUrl;
    private static Handler handler = new Handler();
    private int downX, downY;
    private boolean isPortrait = true; //是否为竖屏


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        containerLayout = this.findViewById(R.id.web_root);
        appBarLayout = this.findViewById(R.id.web_appBarLayout);
        mToolbar = this.findViewById(R.id.web_toolbar);
        mToolbar.setTitle(getString(R.string.loading));
        setSupportActionBar(mToolbar);
        // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setHomeButtonEnabled(true);
        // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if (savedInstanceState != null) {
            onRecoveryInstanceState(savedInstanceState);
        }

        originalUrl = getIntent().getDataString();
        // 补救，获取 originalUrl
        if (TextUtils.isEmpty(originalUrl)) {
            originalUrl = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        }

        if(StringUtils.isEmpty(originalUrl)){
            finish();
        }
        String newUrl = UrlRewriteConfig.i().getRedirectUrl(originalUrl);
        //XLog.i("获取到链接，准备跳转B：" + originalUrl + ", newUrl = " + newUrl);
        if (!TextUtils.isEmpty(newUrl)) {
            originalUrl =  newUrl;
        }

        mToolbar.setSubtitle(originalUrl);

        initWebView(originalUrl);

        mToolbar.setOnClickListener(view -> {
            if (handler.hasMessages(App.MSG_DOUBLE_TAP) && agentWeb != null) {
                handler.removeMessages(App.MSG_DOUBLE_TAP);
                agentWeb.getWebCreator().getWebView().scrollTo(0, 0);
            } else {
                handler.sendEmptyMessageDelayed(App.MSG_DOUBLE_TAP, ViewConfiguration.getDoubleTapTimeout());
            }
        });
    }


    @SuppressLint({"ClickableViewAccessibility", "SetJavaScriptEnabled"})
    private void initWebView(String link) {
        CoordinatorLayout.LayoutParams lp = new CoordinatorLayout.LayoutParams(-1, -1);
        lp.setBehavior(new AppBarLayout.ScrollingViewBehavior());

        agentWeb = AgentWeb.with(this)
                .setAgentWebParent(containerLayout, -1, lp)//lp记得设置behavior属性
                // .setAgentWebParent( containerLayout, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))//传入AgentWeb的父控件。
                .useDefaultIndicator(-1, 3)//设置进度条颜色与高度，-1为默认值，高度为2，单位为dp。
                .setWebView(new NestedScrollAgentWebView(this))
                .setWebViewClient(mWebViewClient)//WebViewClient ， 与 WebView 使用一致 ，但是请勿获取WebView调用setWebViewClient(webViewScroll)方法了,会覆盖AgentWeb DefaultWebClient,同时相应的中间件也会失效。
                .setWebChromeClient(mWebChromeClient) //WebChromeClient
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK) //严格模式 Android 4.2.2 以下会放弃注入对象 ，使用AgentWebView没影响。
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)//打开其他应用时，弹窗咨询用户是否前往其他应用
                .addJavascriptInterface(WebBridge.TAG, WebActivity.this)
                // .setAgentWebWebSettings(getSettings())//设置 IAgentWebSettings。
                // .setPermissionInterceptor(mPermissionInterceptor) //权限拦截 2.0.0 加入。
                // .setAgentWebUIController(new UIController(getActivity())) //自定义UI  AgentWeb3.0.0 加入。
                .setMainFrameErrorView(R.layout.agentweb_error_page, -1) //参数1是错误显示的布局，参数2点击刷新控件ID -1表示点击整个布局都刷新， AgentWeb 3.0.0 加入。
                // .setDownloadListener(mDownloadListener) // 4.0.0 删除该API//下载回调
                // .openParallelDownload()// 4.0.0删除该api 打开并行下载 , 默认串行下载。 请通过AgentWebDownloader#Extra实现并行下载
                // .setNotifyIcon(R.drawable.ic_file_download_black_24dp) 4.0.0删除该api //下载通知图标。4.0.0后的版本请通过AgentWebDownloader#Extra修改icon
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.DISALLOW)//打开其他页面时，弹窗质询用户前往其他应用 AgentWeb 3.0.0 加入。
                .interceptUnkownUrl() //拦截找不到相关页面的Url AgentWeb 3.0.0 加入。
                .createAgentWeb()//创建AgentWeb。
                .ready()//设置 WebSettings。
                .go(link); //WebView载入该url地址的页面并显示。

        WebSettings webSettings = agentWeb.getWebCreator().getWebView().getSettings();
        // webSettings.setHorizontalScrollBarEnabled(false);
        webSettings.setTextZoom(100);
        // 设置最小的字号，默认为8
        webSettings.setMinimumFontSize(10);
        // 设置最小的本地字号，默认为8
        webSettings.setMinimumLogicalFontSize(10);

        // 设置此属性，可任意比例缩放
        webSettings.setUseWideViewPort(true);
        // 缩放至屏幕的大小：如果webview内容宽度大于显示区域的宽度,那么将内容缩小,以适应显示区域的宽度, 默认是false
        webSettings.setLoadWithOverviewMode(true);
        // NARROW_COLUMNS 适应内容大小 ， SINGLE_COLUMN 自适应屏幕
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

        webSettings.setDefaultTextEncodingName(StandardCharsets.UTF_8.name());

        webSettings.setJavaScriptEnabled(true);
        // 支持通过js打开新的窗口
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);

        /* 缓存 */
        webSettings.setDomStorageEnabled(true); // 临时简单的缓存（必须保留，否则无法播放优酷视频网页，其他的可以）
        webSettings.setAppCacheEnabled(true); // 支持H5的 application cache 的功能
        // webSettings.setDatabaseEnabled(true);  // 支持javascript读写db
        // 根据cache-control获取数据。
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        // 通过 file url 加载的 Javascript 读取其他的本地文件 .建议关闭
        webSettings.setAllowFileAccessFromFileURLs(false);
        // 允许通过 file url 加载的 Javascript 可以访问其他的源，包括其他的文件和 http，https 等其他的源
        webSettings.setAllowUniversalAccessFromFileURLs(false);
        // 允许访问文件
        webSettings.setAllowFileAccess(true);

        // 保存表单数据
        webSettings.setSaveFormData(true);
        webSettings.setSavePassword(true);

        // 允许在Android 5.0上 Webview 加载 Http 与 Https 混合内容。作者：Wing_Li，链接：https://www.jianshu.com/p/3fcf8ba18d7f
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        CookieManager instance = CookieManager.getInstance();
        instance.setAcceptCookie(true);
        instance.setAcceptThirdPartyCookies(agentWeb.getWebCreator().getWebView(), true);
        CookieManager.setAcceptFileSchemeCookies(true);

        webSettings.setMediaPlaybackRequiresUserGesture(true);

        /*
         * https://www.jianshu.com/p/6e38e1ef203a
         * 让 WebView 支持文件下载，主要思路有：1、跳转浏览器下载；2、使用系统的下载服务；3、自定义下载任务
         */
        agentWeb.getWebCreator().getWebView().setDownloadListener(
                new DownloadListenerS(WebActivity.this).setWebView(agentWeb.getWebCreator().getWebView())
        );

        // String guessUserAgent = NetworkUserAgentConfig.i().guessUserAgentByUrl(link);
        // if (!TextUtils.isEmpty(guessUserAgent)) {
        //     webSettings.setUserAgentString(guessUserAgent);
        // }

        agentWeb.getWebCreator().getWebView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                downX = (int) arg1.getX();
                downY = (int) arg1.getY();
                return false;
            }
        });
        agentWeb.getWebCreator().getWebView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View webView) {
                final WebView.HitTestResult result = ((WebView) webView).getHitTestResult();
                if (null == result) {
                    return false;
                }
                int type = result.getType();
                if (type == WebView.HitTestResult.UNKNOWN_TYPE) {
                    return false;
                }

                // 这里可以拦截很多类型，我们只处理超链接就可以了
                new LongClickPopWindow(WebActivity.this, (WebView) webView, ScreenUtils.dp2px(WebActivity.this, 120), ScreenUtils.dp2px(WebActivity.this, 130), downX, downY + 10);
                return true;
            }
        });
    }


    // /**
    //  * 添加一个代理，用于所有的URL。本方法可以多次调用，添加多个规则。附加规则的优先级递减。
    //  * Proxy是一个格式为[scheme://]host[:port]的字符串。scheme是可选的，如果存在必须是HTTP、HTTPS或SOCKS，默认为HTTP。Host是带括号的IPv6文字、IPv4文字或一个或多个用句号分隔的标签中的一个。端口号是可选的，HTTP默认为80，HTTPS默认为443，SOCKS默认为1080。
    //  * 主机的正确语法由RFC 3986定义。
    //  */
    // private void setProxyRule() {
    //     if (WebViewFeature.isFeatureSupported(WebViewFeature.PROXY_OVERRIDE)) {
    //         XLog.i("WebView 支持代理");
    //
    //         ProxyConfig proxyConfig = new ProxyConfig.Builder()
    //                 // .addProxyRule("111.123.321.121:1234")
    //                 .addProxyRule("127.0.0.1:10808")
    //                 .addBypassRule("www.excluded.*")
    //                 .addDirect().build();
    //
    //         ProxyController.getInstance().setProxyOverride(proxyConfig, new Executor() {
    //             @Override
    //             public void execute(Runnable command) {
    //                 //do nothing
    //             }
    //         }, new Runnable() {
    //             @Override
    //             public void run() {
    //                 XLog.w( "WebView 代理改变");
    //             }
    //         });
    //     }else {
    //         XLog.i("WebView 不支持代理");
    //     }
    // }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 后者为短期内按下的次数
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (agentWeb.getWebCreator().getWebView().canGoBack()) {
                if (WebActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    portrait();
                }
                agentWeb.getWebCreator().getWebView().stopLoading();
                agentWeb.back();
            } else {
                exit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    private static final int UI_ANIMATION_DELAY = 300;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            XLog.i("屏幕：隐藏状态");

            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) appBarLayout.getChildAt(0).getLayoutParams();
            XLog.i("屏幕：隐藏状态" + containerLayout.getSystemUiVisibility());
            params.setScrollFlags(0);

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }

            // View.SYSTEM_UI_FLAG_LOW_PROFILE                    状态栏显示处于低能显示状态(low profile模式)，状态栏上一些图标显示会被隐藏。
            // View.SYSTEM_UI_FLAG_FULLSCREEN                     隐藏状态栏：全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态遮住。
            // View.SYSTEM_UI_FLAG_HIDE_NAVIGATION                隐藏导航栏
            // View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN              布局占用状态栏区域
            // View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION         布局占用导航栏区域
            // View.SYSTEM_UI_FLAG_LAYOUT_STABLE                  稳定布局，防止系统栏隐藏时内容区域大小发生变化
            // View.SYSTEM_UI_FLAG_IMMERSIVE                      沉浸式
            // View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY               粘性沉浸式：向内滑动的操作会让系统栏临时显示
            containerLayout.setFitsSystemWindows(false);
            containerLayout.setSystemUiVisibility(
                    // | View.SYSTEM_UI_FLAG_IMMERSIVE
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // landscape status bar
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // landscape nav bar
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) appBarLayout.getChildAt(0).getLayoutParams();
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS | AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
            containerLayout.setFitsSystemWindows(true);
            containerLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    };


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(Contract.ACTIVITY_IS_PORTRAIT, isPortrait);
        XLog.i("自动保存，是否为竖屏：" + isPortrait);
        super.onSaveInstanceState(outState);
    }

    private void onRecoveryInstanceState(@NonNull Bundle outState) {
        if (outState.getBoolean(Contract.ACTIVITY_IS_PORTRAIT, true)) {
            portrait();
        } else {
            landscape();
        }
    }

    /**
     * 切换为横屏
     */
    @SuppressLint("SourceLockedOrientationActivity")
    private void landscape() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        isPortrait = false;
        // Schedule a runnable to remove the status and navigation bar after a delay
        handler.removeCallbacks(mShowPart2Runnable);
        handler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * 切换为竖屏
     */
    @SuppressLint("SourceLockedOrientationActivity")
    private void portrait() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        isPortrait = true;
        // Schedule a runnable to display UI elements after a delay
        handler.removeCallbacks(mHidePart2Runnable);
        handler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }


    @Override
    public void log(String msg) {
        XLog.i(WebBridge.TAG, msg);
    }

    @Override
    public void toggleScreenOrientation() {
        XLog.d("切换屏幕方向");
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            landscape();
        } else {
            portrait();
        }
    }

    protected WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (!TextUtils.isEmpty(title)) {
                mToolbar.setTitle(title);
                receivedUrl = agentWeb.getWebCreator().getWebView().getUrl();
                mToolbar.setSubtitle(receivedUrl);
            }
        }
    };


    /**
     * webview socks5 代理
     * https://github.com/guardianproject/NetCipher/blob/master/sample-webviewclient/src/sample/netcipher/webviewclient/GenericWebViewClient.java
     *
     * 注意 WebResourceResponse.setStatusCodeAndReasonPhrase() 的方法（设置资源响应的状态代码和原因短语）会校验返回码
     * https://github.com/Ryan-Shz/FastWebView/blob/817bb89f7fbca2b3ec790f1f633aeb3f50ac53fa/fastwebview/src/main/java/com/ryan/github/view/loader/OkHttpResourceLoader.java
     */
    protected WebViewClient mWebViewClient = new WebViewClient() {
        // @Override
        // public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm){
        //     //身份验证（账号密码）
        //     handler.proceed("userName", "password");
        // }

        // @Override
        // public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        //     String scheme = request.getUrl().getScheme().trim();
        //     if (scheme.equalsIgnoreCase(HTTP) || scheme.equalsIgnoreCase(HTTPS)) {
        //         String url = request.getUrl().toString();
        //         if (HostBlockConfig.i().isAd(url)) {
        //             // 有广告的请求数据，我们直接返回空数据，注：不能直接返回null
        //             return new WebResourceResponse(null, null, null);
        //         }
        //
        //         String newUrl = UrlRewriteConfig.i().getRedirectUrl(url);
        //         // XLog.i("重定向地址：" + url + " -> " + newUrl);
        //
        //         if(App.i().socks5ProxyNode == null){
        //             XLog.i("webview 不走代理");
        //             if(!TextUtils.isEmpty(newUrl) && !url.equalsIgnoreCase(newUrl)){
        //                 return super.shouldInterceptRequest(view, new WebResourceRequest() {
        //                     @Override
        //                     public Uri getUrl() {
        //                         return Uri.parse(newUrl);
        //                     }
        //                     @SuppressLint("NewApi")
        //                     @Override
        //                     public boolean isRedirect(){
        //                         return true;
        //                     }
        //                     @SuppressLint("NewApi")
        //                     @Override
        //                     public boolean isForMainFrame() {
        //                         return request.isForMainFrame();
        //                     }
        //                     @SuppressLint("NewApi")
        //                     @Override
        //                     public boolean hasGesture() {
        //                         return request.hasGesture();
        //                     }
        //                     @SuppressLint("NewApi")
        //                     @Override
        //                     public String getMethod() {
        //                         return request.getMethod();
        //                     }
        //                     @SuppressLint("NewApi")
        //                     @Override
        //                     public Map<String, String> getRequestHeaders() {
        //                         return request.getRequestHeaders();
        //                     }
        //                 });
        //             }
        //             return super.shouldInterceptRequest(view, request);
        //         }else {
        //             XLog.i("webview 走代理：" + HttpClientManager.i().simpleClient().proxy() + url);
        //             if(!TextUtils.isEmpty(newUrl) && !url.equalsIgnoreCase(newUrl)){
        //                 url = newUrl;
        //             }
        //             try {
        //                 // Request.Builder builder = new Request.Builder().url(url).tag(TAG).method(request.getMethod(), null);
        //                 // for (Map.Entry<String, String> requestHeader : request.getRequestHeaders().entrySet()) {
        //                 //     builder.header(requestHeader.getKey(), requestHeader.getValue());
        //                 // }
        //                 //
        //                 // Response response = HttpClientManager.i().simpleClient().newCall(builder.build()).execute();
        //                 // ResponseBody responseBody = response.body();
        //                 // if(!response.isSuccessful()){
        //                 //     XLog.i("webview 走代理失败， 返回体为null: " +  response.code() + " ," + response.message());
        //                 //     return super.shouldInterceptRequest(view, request);
        //                 // }
        //                 //
        //                 // MediaType mediaType  = responseBody.contentType();
        //                 // String charset = null;
        //                 // String mimeType = "text/plain";
        //                 // if( mediaType != null ){
        //                 //     charset = DataUtil.getCharsetFromContentType(mediaType.toString());
        //                 //     mimeType = mediaType.toString();
        //                 // }
        //                 // XLog.i("webview 走代理， 内容编码：" + mimeType + " , " + charset + " , " + response.message() );
        //                 //
        //                 // InputStream in = new BufferedInputStream(responseBody.byteStream());
        //                 //
        //                 // Map<String, String> responseHeaders = new HashMap<>();
        //                 // for (Pair<? extends String, ? extends String> header: response.headers()) {
        //                 //     responseHeaders.put(header.getFirst(), header.getSecond());
        //                 // }
        //                 // if(TextUtils.isEmpty(charset)){
        //                 //     charset = responseHeaders.get("content-encoding");
        //                 //     XLog.i("webview 走代理，编码：" + mimeType + " , " + charset + " , " + response.message() );
        //                 // }
        //                 // if(isInterceptorThisRequest(response.code())){
        //                 //     XLog.i("webview 代理，由于响应码为，所以跳过：" + response.code() );
        //                 //     return super.shouldInterceptRequest(view, request);
        //                 // }
        //                 // return new WebResourceResponse(mimeType, charset, response.code(), "ok", responseHeaders, in);
        //
        //                 String urlString = url.split("#")[0];
        //                 Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(App.i().socks5ProxyNode.getServer(), App.i().socks5ProxyNode.getPort()));
        //                 HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection(proxy);
        //                 connection.setRequestMethod(request.getMethod());
        //                 for (Map.Entry<String, String> requestHeader : request.getRequestHeaders().entrySet()) {
        //                     connection.setRequestProperty(requestHeader.getKey(), requestHeader.getValue());
        //                 }
        //
        //                 // 将响应转换为网络资源响应参数所需的格式
        //                 // transform response to required format for WebResourceResponse parameters
        //                 InputStream in = new BufferedInputStream(connection.getInputStream());
        //                 String encoding = connection.getContentEncoding();
        //                 // connection.getHeaderFields();
        //                 Map<String, String> responseHeaders = new HashMap<>();
        //                 for (String key : connection.getHeaderFields().keySet()) {
        //                     responseHeaders.put(key, connection.getHeaderField(key));
        //                 }
        //
        //                 String mimeType = "text/plain";
        //                 if (connection.getContentType() != null && !connection.getContentType().isEmpty()) {
        //                     mimeType = connection.getContentType().split("; ")[0];
        //                 }
        //                 XLog.i("webview 走代理， 内容编码：" + mimeType + " , " + encoding + " , " + connection.getResponseMessage());
        //                 if(isInterceptorThisRequest(connection.getResponseCode())){
        //                     XLog.i("webview 代理，由于响应码为，所以跳过：" + connection.getResponseCode() );
        //                     return super.shouldInterceptRequest(view, request);
        //                 }
        //                 return new WebResourceResponse(mimeType, encoding, connection.getResponseCode(), connection.getResponseMessage(), responseHeaders, in);
        //                 // return new WebResourceResponse(mimeType, "binary", in);
        //             } catch (IOException e) {
        //                 e.printStackTrace();
        //                 XLog.e("无法加载：" + e.getMessage());
        //             }
        //             // failed doing proxied http request: return empty response
        //             XLog.i("webview 走代理失败");
        //             return super.shouldInterceptRequest(view, request);
        //             // return new WebResourceResponse("text/plain", "UTF-8", 204, "No Content", new HashMap<String, String>(), new ByteArrayInputStream(new byte[]{}));
        //         }
        //     }
        //
        //     return super.shouldInterceptRequest(view, request);
        // }
        // /**
        //  * references {@link android.webkit.WebResourceResponse} setStatusCodeAndReasonPhrase
        //  */
        // private boolean isInterceptorThisRequest(int code) {
        //     return (code < 100 || code > 599 || (code > 299 && code < 400));
        // }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, final WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (url.toLowerCase().startsWith(SCHEMA_HTTP) || url.toLowerCase().startsWith(SCHEMA_HTTPS)) {
                if (HostBlockConfig.i().isAd(url)) {
                    // 有广告的请求数据，我们直接返回空数据，注：不能直接返回null
                    return new WebResourceResponse(null, null, null);
                }
                // NOTE: 2021/1/24  由于会将小图转为大图，造成问题，所以不重定向。
                String newUrl = UrlRewriteConfig.i().getRedirectUrl(url);
                XLog.i("网页重定向：" + url + " -> " + newUrl);
                if(!TextUtils.isEmpty(newUrl) && !url.equalsIgnoreCase(newUrl)){
                    return super.shouldInterceptRequest(view, new WebResourceRequest() {
                        @Override
                        public Uri getUrl() {
                            return Uri.parse(newUrl);
                        }
                        @SuppressLint("NewApi")
                        @Override
                        public boolean isRedirect(){
                            return true;
                        }
                        @SuppressLint("NewApi")
                        @Override
                        public boolean isForMainFrame() {
                            return request.isForMainFrame();
                        }
                        @SuppressLint("NewApi")
                        @Override
                        public boolean hasGesture() {
                            return request.hasGesture();
                        }
                        @SuppressLint("NewApi")
                        @Override
                        public String getMethod() {
                            return request.getMethod();
                        }
                        @SuppressLint("NewApi")
                        @Override
                        public Map<String, String> getRequestHeaders() {
                            return request.getRequestHeaders();
                        }
                    });
                }
            }
            return super.shouldInterceptRequest(view, request);
        }

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, WebResourceRequest request) {
            // 优酷想唤起自己应用播放该视频，下面拦截地址返回true则会在应用内 H5 播放，禁止优酷唤起播放该视频。
            // 如果返回false ， DefaultWebClient 会根据intent协议处理该地址，首先匹配该应用存不存在，
            // 如果存在，唤起该应用播放，如果不存在，则跳到应用市场下载该应用.
            String url = request.getUrl().toString();

            XLog.i("地址：" + url);
            if (url.toLowerCase().startsWith(SCHEMA_HTTP) || url.startsWith(SCHEMA_HTTPS)) {
                return false;
            }

            String newUrl = UrlRewriteConfig.i().getRedirectUrl( url );
            if (!TextUtils.isEmpty(newUrl)) {
                // 创建一个新请求，并相应地修改它
                url = newUrl;
            }

            //其他的URL则会开启一个Acitity然后去调用原生APP
            final Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (url.startsWith(SCHEMA_LOREAD) || url.startsWith(SCHEMA_PDY) || url.startsWith(SCHEMA_PALABRE) || url.startsWith(SCHEMA_FEEDME)) {
                startActivity(in);
                finish();
            }else if (in.resolveActivity(getPackageManager()) != null) {
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                String name = null;
                try {
                    name = "" + getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(in.resolveActivity(getPackageManager()).getPackageName(), PackageManager.GET_META_DATA));
                } catch (PackageManager.NameNotFoundException e) {
                    XLog.i(R.string.unable_to_find_app);
                    e.printStackTrace();
                }
                if (TextUtils.isEmpty(name)) {
                    name = getString(R.string.corresponding);
                }
                new MaterialDialog.Builder(WebActivity.this)
                        .content(R.string.do_you_want_to_jump_the_application, name)
                        .negativeText(R.string.disagree)
                        .positiveText(R.string.agree)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                startActivity(in);
                                finish();
                            }
                        })
                        .show();
            }
            return true;
        }

        @Override
        public void onPageStarted(WebView webView, String url, Bitmap favicon) {
            XLog.i("onPageStarted = " + url);
            super.onPageStarted(webView, url, favicon);
            if (url.startsWith(SCHEMA_LOREAD) || url.startsWith(SCHEMA_PDY) || url.startsWith(SCHEMA_PALABRE) || url.startsWith(SCHEMA_FEEDME)) {
                Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(in);
                finish();
            }
            if (itemRefresh != null) {
                itemRefresh.setVisible(false);
            }
            if (itemStop != null) {
                itemStop.setVisible(true);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // 注入视频全屏js
            view.loadUrl(WebBridge.Video.fullScreenJsFun(url));
            // XLog.i("页面加载完成");
            // view.loadUrl(Distill.Bridge.COMMEND_PRINT_HTML);
            if (itemStop != null) {
                itemStop.setVisible(false);
            }
            if (itemRefresh != null) {
                itemRefresh.setVisible(true);
            }
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
            //XLog.i("接受http错误 = " + request.getUrl() + errorResponse);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            //XLog.i("接受Ssl错误 = " + view.getUrl() + error);
            handler.proceed();//接受证书
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            //XLog.i("接受错误 = " + request.getUrl() + error);
        }
    };

    MenuItem itemRefresh, itemStop, itemGetHtml;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_web, menu);
        itemRefresh = menu.findItem(R.id.web_menu_refresh);
        itemStop = menu.findItem(R.id.web_menu_stop);
        itemGetHtml = menu.findItem(R.id.web_menu_get_html);
        if(BuildConfig.DEBUG){
            itemGetHtml.setVisible(true);
        }
        return true;
    }

    int selected = 0;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //监听左上角的返回箭头
            case android.R.id.home:
                exit();
                break;
            case R.id.web_menu_user_agent:
                ArrayList<String> userAgentsTitle = new ArrayList<>();
                ArrayList<Pair<String, String>> userAgents = new ArrayList<>();
                //
                userAgents.add(new Pair<>(getString(R.string.default_x), WebSettings.getDefaultUserAgent(this)));
                userAgentsTitle.add(getString(R.string.default_x));

                for (Map.Entry<String, String> entry : UserAgentConfig.i().getUserAgents().entrySet()) {
                    userAgentsTitle.add(entry.getKey());
                    userAgents.add(new Pair<>(entry.getKey(), entry.getValue()));
                }

                if(selected >= userAgentsTitle.size()){
                    selected = 0;
                }
                new MaterialDialog.Builder(WebActivity.this)
                        .title(R.string.select_user_agent)
                        .items(userAgentsTitle)
                        .itemsCallbackSingleChoice(selected, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, final int which, CharSequence text) {
                                selected = which;
                                // agentWeb.getWebCreator().getWebView().getSettings().setUserAgentString(NetworkUserAgentConfig.i().guessUserAgentByUrl(receivedUrl));
                                agentWeb.getWebCreator().getWebView().getSettings().setUserAgentString(userAgents.get(which).second);
                                agentWeb.getWebCreator().getWebView().reload();
                                // XLog.i("默认的UA是：" + agentWeb.getWebCreator().getWebView().getSettings().getUserAgentString() );
                                dialog.dismiss();
                                return true;
                            }
                        })
                        .neutralText(R.string.custom_user_agent)
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                new MaterialDialog.Builder(WebActivity.this)
                                        .title(R.string.enter_user_agent)
                                        .inputType(InputType.TYPE_CLASS_TEXT)
                                        .inputRange(12, 200)
                                        .input(null, agentWeb.getWebCreator().getWebView().getSettings().getUserAgentString(), new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@NotNull MaterialDialog dialog, CharSequence input) {
                                                if(!UserAgentConfig.i().getUserAgents().containsKey(getString(R.string.custom))){
                                                    userAgentsTitle.add(getString(R.string.custom));
                                                }
                                                UserAgentConfig.i().putCustomUserAgent(getString(R.string.custom), input.toString());
                                                UserAgentConfig.i().save();
                                                selected = userAgentsTitle.size() -1;
                                                XLog.i("当前输入的是：" + input.toString());
                                                agentWeb.getWebCreator().getWebView().getSettings().setUserAgentString(input.toString());
                                                agentWeb.getWebCreator().getWebView().reload();
                                            }
                                        })
                                        .positiveText(R.string.confirm)
                                        .negativeText(android.R.string.cancel)
                                        .neutralText(R.string.remove_custom_user_agent)
                                        .neutralColor(WebActivity.this.getResources().getColor(R.color.material_red_400))
                                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                UserAgentConfig.i().getUserAgents().remove(getString(R.string.custom));
                                                UserAgentConfig.i().save();
                                            }
                                        })
                                        .show();
                            }
                        })
                        .show();
                break;

                //
                // final ArrayList<String> uaTitle = new ArrayList<>();
                // String holdUA = NetworkUserAgentConfig.i().getHoldUserAgent();
                // uaTitle.add(getString(R.string.default_x));
                // int i = 0;
                // for (Map.Entry<String, String> entry : NetworkUserAgentConfig.i().getUserAgents().entrySet()) {
                //     uaTitle.add(entry.getKey());
                //     if(entry.getKey().equals(holdUA)){
                //         selected = i;
                //     }
                //     i++;
                //     XLog.i("被选UA：" + entry.getKey());
                // }
                //
                // int finalSelected = selected;
                // new MaterialDialog.Builder(WebActivity.this)
                //         .title(R.string.select_user_agent)
                //         .items(uaTitle)
                //         .itemsCallbackSingleChoice(selected + 1, new MaterialDialog.ListCallbackSingleChoice() {
                //             @Override
                //             public boolean onSelection(MaterialDialog dialog, View view, final int which, CharSequence text) {
                //                 // 默认
                //                 if (which == 0) {
                //                     NetworkUserAgentConfig.i().setHoldUserAgent(null);
                //                 }
                //                 // 手动选择项
                //                 else if (which - 1 != finalSelected) {
                //                     NetworkUserAgentConfig.i().setHoldUserAgent(text.toString());
                //
                //                 }
                //                 NetworkUserAgentConfig.i().save();
                //                 agentWeb.getWebCreator().getWebView().getSettings().setUserAgentString(NetworkUserAgentConfig.i().guessUserAgentByUrl(receivedUrl));
                //                 agentWeb.getWebCreator().getWebView().reload();
                //                 // XLog.i("默认的UA是：" + agentWeb.getWebCreator().getWebView().getSettings().getUserAgentString() );
                //                 dialog.dismiss();
                //                 return true;
                //             }
                //         })
                //         .neutralText(R.string.custom_user_agent)
                //         .onNeutral(new MaterialDialog.SingleButtonCallback() {
                //             @Override
                //             public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                //                 new MaterialDialog.Builder(WebActivity.this)
                //                         .title(R.string.enter_user_agent)
                //                         .inputType(InputType.TYPE_CLASS_TEXT)
                //                         .inputRange(12, 200)
                //                         .input(null, agentWeb.getWebCreator().getWebView().getSettings().getUserAgentString(), new MaterialDialog.InputCallback() {
                //                             @Override
                //                             public void onInput(@NotNull MaterialDialog dialog, CharSequence input) {
                //                                 NetworkUserAgentConfig.i().setHoldUserAgent(getString(R.string.custom));
                //                                 NetworkUserAgentConfig.i().getUserAgents().put(getString(R.string.custom),input.toString());
                //                                 NetworkUserAgentConfig.i().save();
                //                                 XLog.i("当前输入的是：" + input.toString());
                //                                 agentWeb.getWebCreator().getWebView().getSettings().setUserAgentString(NetworkUserAgentConfig.i().guessUserAgentByUrl(receivedUrl));
                //                                 agentWeb.getWebCreator().getWebView().reload();
                //                             }
                //                         })
                //                         .positiveText(R.string.confirm)
                //                         .negativeText(android.R.string.cancel)
                //                         .neutralText(R.string.remove_custom_user_agent)
                //                         .neutralColor(WebActivity.this.getResources().getColor(R.color.material_red_400))
                //                         .onNeutral(new MaterialDialog.SingleButtonCallback() {
                //                             @Override
                //                             public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                //                                 NetworkUserAgentConfig.i().setHoldUserAgent(getString(R.string.custom));
                //                                 NetworkUserAgentConfig.i().getUserAgents().remove(getString(R.string.custom));
                //                                 NetworkUserAgentConfig.i().save();
                //                             }
                //                         })
                //                         .show();
                //             }
                //         })
                //         .show();
                // break;
            case R.id.web_menu_open_by_sys:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (!TextUtils.isEmpty(receivedUrl)) {
                    intent.setData(Uri.parse(receivedUrl));
                } else {
                    intent.setData(Uri.parse(originalUrl));
                }
                startActivity(intent);
                break;
            case R.id.web_menu_copy_link:
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mClipData;
                if (!TextUtils.isEmpty(receivedUrl)) {
                    // 创建普通字符型ClipData
                    mClipData = ClipData.newRawUri(agentWeb.getWebCreator().getWebView().getTitle(), Uri.parse(receivedUrl));
                } else {
                    // 创建普通字符型ClipData
                    mClipData = ClipData.newRawUri(agentWeb.getWebCreator().getWebView().getTitle(), Uri.parse(originalUrl));
                }
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
                ToastUtils.show(getString(R.string.copy_success));
                break;
            case R.id.web_menu_share:
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, mToolbar.getTitle());
                // sendIntent.putExtra(Intent.EXTRA_TEXT, mToolbar.getTitle() + " " +  webViewS.getUrl() );
                sendIntent.putExtra(Intent.EXTRA_TEXT, mToolbar.getTitle() + " " + agentWeb.getWebCreator().getWebView().getUrl());
                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share_to)));
                overridePendingTransition(R.anim.fade_in, R.anim.out_from_bottom);
                break;
            case R.id.web_menu_refresh:
                agentWeb.getWebCreator().getWebView().reload();
                break;
            case R.id.web_menu_stop:
                agentWeb.getWebCreator().getWebView().stopLoading();
                break;
            case R.id.web_menu_get_html:
                XLog.i("测试点击");
                agentWeb.getWebCreator().getWebView().loadUrl(COMMEND_PRINT_HTML);
                agentWeb.getJsAccessEntrace().callJs(COMMEND_PRINT_HTML);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    String COMMEND_PRINT_HTML = "javascript: function(){ console.log(document.documentElement.outerHTML); window.WebBridge.log(document.documentElement.outerHTML);} ";
    // String COMMEND = "javascript:window.onload = function(){ReadabilityBridge.getHtml(document.documentElement.outerHTML);void(0);}";

    private void exit() {
        this.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onPause() {
        super.onPause();
        agentWeb.getWebLifeCycle().onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        agentWeb.getWebLifeCycle().onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CookieManager.getInstance().flush();
        agentWeb.getWebCreator().getWebView().stopLoading();
        agentWeb.getWebCreator().getWebView().clearCache(true);
        agentWeb.getWebCreator().getWebView().clearHistory();
        agentWeb.getWebCreator().getWebView().getSettings().setJavaScriptEnabled(false);
        agentWeb.getWebCreator().getWebView().removeAllViews();
        agentWeb.getWebCreator().getWebParentLayout().removeAllViews();
        agentWeb.getWebLifeCycle().onDestroy();
    }

    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        mColorfulBuilder
                .backgroundColor(R.id.web_root, R.attr.root_view_bg)
                .backgroundColor(R.id.web_toolbar, R.attr.topbar_bg);
        return mColorfulBuilder;
    }
}
