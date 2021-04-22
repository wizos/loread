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
import android.webkit.JavascriptInterface;
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
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.BuildConfig;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.bridge.WebBridge;
import me.wizos.loread.config.HostBlockConfig;
import me.wizos.loread.config.header_useragent.HeaderUserAgentConfig;
import me.wizos.loread.config.header_useragent.UserAgentConfig;
import me.wizos.loread.config.url_rewrite.UrlRewriteConfig;
import me.wizos.loread.db.CorePref;
import me.wizos.loread.utils.FileUtils;
import me.wizos.loread.utils.ScreenUtils;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.webview.DownloadListenerS;
import me.wizos.loread.view.webview.LongClickPopWindow;
import me.wizos.loread.view.webview.VideoHelper;

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
public class Web2Activity extends BaseActivity implements WebBridge{ //  implements WebBridge
    private Toolbar mToolbar;
    private AppBarLayout appBarLayout;
    private CoordinatorLayout containerLayout;
    private String originalUrl;
    private String receivedUrl;
    private static Handler handler = new Handler();
    private int downX, downY;
    private boolean isPortrait = true; //是否为竖屏

    private VideoHelper videoHelper;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web2);
        containerLayout = this.findViewById(R.id.web2_root);
        appBarLayout = this.findViewById(R.id.web2_appBarLayout);
        mToolbar = this.findViewById(R.id.web2_toolbar);
        mToolbar.setTitle(getString(R.string.loading));
        setSupportActionBar(mToolbar);
        // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setHomeButtonEnabled(true);
        // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        webView = findViewById(R.id.web2_webview);

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
        mToolbar.setOnClickListener(view -> {
            if (handler.hasMessages(App.MSG_DOUBLE_TAP) && webView != null) {
                handler.removeMessages(App.MSG_DOUBLE_TAP);
                webView.scrollTo(0, 0);
            } else {
                handler.sendEmptyMessageDelayed(App.MSG_DOUBLE_TAP, ViewConfiguration.getDoubleTapTimeout());
            }
        });

        // SniffingUtil.get().activity(this).callback(new SniffingUICallback() {
        //     @Override
        //     public void onSniffingStart(View webView, String url) {
        //         ToastUtils.show("开始嗅探");
        //     }
        //
        //     @Override
        //     public void onSniffingFinish(View webView, String url) {
        //         ToastUtils.show("嗅探结束");
        //     }
        //     @Override
        //     public void onSniffingSuccess(View webView, String url, List<SniffingVideo> videos) {
        //         ToastUtils.show("嗅探成功");
        //         XLog.i(videos);
        //     }
        //
        //     @Override
        //     public void onSniffingError(View webView, String url, int errorCode) {
        //         XLog.i("嗅探失败：" + errorCode);
        //     }
        // }).url(originalUrl).start();

        initWebView(originalUrl);
    }


    @SuppressLint({"ClickableViewAccessibility", "SetJavaScriptEnabled"})
    private void initWebView(String link) {
        WebSettings webSettings = webView.getSettings();
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
        instance.setAcceptThirdPartyCookies(webView, true);
        CookieManager.setAcceptFileSchemeCookies(true);

        webSettings.setMediaPlaybackRequiresUserGesture(true);

        webView.addJavascriptInterface(Web2Activity.this, WebBridge.TAG);

        /*
         * https://www.jianshu.com/p/6e38e1ef203a
         * 让 WebView 支持文件下载，主要思路有：1、跳转浏览器下载；2、使用系统的下载服务；3、自定义下载任务
         */
        webView.setDownloadListener(
                new DownloadListenerS(Web2Activity.this).setWebView(webView)
        );

        String guessUserAgent = HeaderUserAgentConfig.i().guessUserAgentByUrl(link);
        if (!TextUtils.isEmpty(guessUserAgent)) {
            webSettings.setUserAgentString(guessUserAgent);
        }

        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                downX = (int) arg1.getX();
                downY = (int) arg1.getY();
                return false;
            }
        });
        webView.setOnLongClickListener(new View.OnLongClickListener() {
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
                new LongClickPopWindow(Web2Activity.this, (WebView) webView, ScreenUtils.dp2px(Web2Activity.this, 120), ScreenUtils.dp2px(Web2Activity.this, 130), downX, downY + 10);
                return true;
            }
        });

        videoHelper = new VideoHelper(this, webView);
        webView.setWebViewClient(mWebViewClient);
        webView.setWebChromeClient(mWebChromeClient);
        webView.loadUrl(originalUrl);
    }




    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 后者为短期内按下的次数
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (videoHelper != null && videoHelper.isFullScreen()) {
                videoHelper.onHideCustomView();
                return true;
            }
            if (webView.canGoBack()) {
                webView.stopLoading();
                webView.goBack();
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

    private static boolean videoIsPortrait = false;
    @JavascriptInterface
    @Override
    public void postVideoPortrait(boolean portrait) {
        videoIsPortrait = portrait;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoIsPortrait = false;
            }
        });
    }

    @JavascriptInterface
    @Override
    public void foundAudio(String src, long duration) {
        XLog.i("发现音频：" + src + "  -> 时长：" + duration);
    }

    @JavascriptInterface
    @Override
    public void foundVideo(String src, long duration) {
        XLog.i("发现视频：" + src + "  -> 时长：" + duration);
    }

    @JavascriptInterface
    @Override
    public void log(String msg) {
        XLog.i(msg);
    }

    @JavascriptInterface
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
        WeakReference<VideoHelper> video;
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (!TextUtils.isEmpty(title)) {
                mToolbar.setTitle(title);
                receivedUrl = webView.getUrl();
                mToolbar.setSubtitle(receivedUrl);
            }
        }
        // 表示进入全屏的时候
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view,callback);
            if(video == null){
                video = new WeakReference<>(new VideoHelper(Web2Activity.this, webView));
            }
            if (video.get() != null) {
                video.get().onShowCustomView(view, videoIsPortrait, callback);
            }
            videoIsPortrait = false;
        }

        //表示退出全屏的时候
        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            // XLog.i("退出全屏");
            if(video == null){
                video = new WeakReference<>(new VideoHelper(Web2Activity.this, webView));
            }
            if (video.get() != null) {
                video.get().onHideCustomView();
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
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, final WebResourceRequest request) {
            String url = request.getUrl().toString().toLowerCase();
            XLog.d("拦截请求：" + url);
            if (!url.startsWith(SCHEMA_HTTP) && !url.startsWith(SCHEMA_HTTPS)) {
                return super.shouldInterceptRequest(view,request);

            }
            if (CorePref.i().globalPref().getBoolean(Contract.BLOCK_ADS, true) && HostBlockConfig.i().isAd(url)) {
                // 有广告的请求数据，我们直接返回空数据，注：不能直接返回null
                return new WebResourceResponse(null, null, null);
            }

            // 这里的嗅探并不多余，例如网易云的音频地址并不会出现在html中，但是网络请求中可以发现到。
            // https://music.163.com/outchain/player?type=2&id=1299293129&height=66

            if(url.contains(Contract.LOREAD_WIZOS_ME)){
                try {
                    if(url.contains(Contract.PATH_PLYR_LITE)){
                        return new WebResourceResponse("text/css", StandardCharsets.UTF_8.displayName(), FileUtils.getInputStreamFromLocalOrAssets(Web2Activity.this, Contract.PATH_PLYR_LITE));
                    }else if(url.contains(Contract.PATH_ICONFONT)){
                        return new WebResourceResponse("text/javascript", StandardCharsets.UTF_8.displayName(), FileUtils.getInputStreamFromLocalOrAssets(Web2Activity.this, Contract.PATH_ICONFONT));
                    }else if(url.contains(Contract.PATH_ZEPTO)){
                        return new WebResourceResponse("text/javascript", StandardCharsets.UTF_8.displayName(), FileUtils.getInputStreamFromLocalOrAssets(Web2Activity.this, Contract.PATH_ZEPTO));
                    }else if(url.contains(Contract.PATH_MEDIA_CONTROLS)){
                        return new WebResourceResponse("text/javascript", StandardCharsets.UTF_8.displayName(), FileUtils.getInputStreamFromLocalOrAssets(Web2Activity.this, Contract.PATH_MEDIA_CONTROLS));
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    XLog.e("加载错误：" + e.getLocalizedMessage());
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
                new MaterialDialog.Builder(Web2Activity.this)
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
            XLog.d("页面加载完成");
            if (CorePref.i().globalPref().getBoolean(Contract.IFRAME_LISTENER, true)) {
                view.loadUrl(WebBridge.COMMEND_LOAD_MEDIA_CONTROLS);
            }
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
                new MaterialDialog.Builder(Web2Activity.this)
                        .title(R.string.select_user_agent)
                        .items(userAgentsTitle)
                        .itemsCallbackSingleChoice(selected, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, final int which, CharSequence text) {
                                selected = which;
                                // webView.getSettings().setUserAgentString(NetworkUserAgentConfig.i().guessUserAgentByUrl(receivedUrl));
                                webView.getSettings().setUserAgentString(userAgents.get(which).second);
                                webView.reload();
                                // XLog.i("默认的UA是：" + webView.getSettings().getUserAgentString() );
                                dialog.dismiss();
                                return true;
                            }
                        })
                        .neutralText(R.string.custom_user_agent)
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                new MaterialDialog.Builder(Web2Activity.this)
                                        .title(R.string.enter_user_agent)
                                        .inputType(InputType.TYPE_CLASS_TEXT)
                                        .inputRange(12, 200)
                                        .input(null, webView.getSettings().getUserAgentString(), new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(@NotNull MaterialDialog dialog, CharSequence input) {
                                                if(!UserAgentConfig.i().getUserAgents().containsKey(getString(R.string.custom))){
                                                    userAgentsTitle.add(getString(R.string.custom));
                                                }
                                                UserAgentConfig.i().putCustomUserAgent(getString(R.string.custom), input.toString());
                                                UserAgentConfig.i().save();
                                                selected = userAgentsTitle.size() -1;
                                                XLog.i("当前输入的是：" + input.toString());
                                                webView.getSettings().setUserAgentString(input.toString());
                                                webView.reload();
                                            }
                                        })
                                        .positiveText(R.string.confirm)
                                        .negativeText(android.R.string.cancel)
                                        .neutralText(R.string.remove_custom_user_agent)
                                        .neutralColor(Web2Activity.this.getResources().getColor(R.color.material_red_400))
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
                    mClipData = ClipData.newRawUri(webView.getTitle(), Uri.parse(receivedUrl));
                } else {
                    // 创建普通字符型ClipData
                    mClipData = ClipData.newRawUri(webView.getTitle(), Uri.parse(originalUrl));
                }
                // 将ClipData内容放到系统剪贴板里。
                if(cm != null){
                    cm.setPrimaryClip(mClipData);
                    ToastUtils.show(R.string.copy_success);
                }else {
                    ToastUtils.show(R.string.copy_failure);
                }
                break;
            case R.id.web_menu_share:
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, mToolbar.getTitle());
                // sendIntent.putExtra(Intent.EXTRA_TEXT, mToolbar.getTitle() + " " +  webViewS.getUrl() );
                sendIntent.putExtra(Intent.EXTRA_TEXT, mToolbar.getTitle() + " " + webView.getUrl());
                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(sendIntent, getString(R.string.share_to)));
                overridePendingTransition(R.anim.fade_in, R.anim.out_from_bottom);
                break;
            case R.id.web_menu_refresh:
                webView.reload();
                break;
            case R.id.web_menu_stop:
                webView.stopLoading();
                break;
            case R.id.web_menu_get_html:
                XLog.i("测试点击");
                new MaterialDialog.Builder(this)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .input(null, COMMEND_PRINT_HTML, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                webView.loadUrl(input.toString());
                            }
                        })
                        .positiveText(R.string.execute)
                        .negativeText(android.R.string.cancel)
                        .show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exit() {
        this.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CookieManager.getInstance().flush();
        webView.stopLoading();
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setJavaScriptEnabled(false);
        webView.removeAllViews();
        containerLayout.removeAllViews();
        webView.destroy();
    }

    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        mColorfulBuilder
                .backgroundColor(R.id.web2_root, R.attr.root_view_bg)
                .backgroundColor(R.id.web2_toolbar, R.attr.topbar_bg);
        return mColorfulBuilder;
    }
}