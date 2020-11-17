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
import com.google.android.material.appbar.AppBarLayout;
import com.hjq.toast.ToastUtils;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.NestedScrollAgentWebView;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;
import com.socks.library.KLog;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.bridge.WebBridge;
import me.wizos.loread.config.AdBlock;
import me.wizos.loread.config.LinkRewriteConfig;
import me.wizos.loread.config.NetworkUserAgentConfig;
import me.wizos.loread.utils.ScreenUtil;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.utils.VideoInjectUtil;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.webview.DownloadListenerS;
import me.wizos.loread.view.webview.LongClickPopWindow;

import static me.wizos.loread.Contract.HTTP;
import static me.wizos.loread.Contract.HTTPS;
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
        String newUrl = LinkRewriteConfig.i().getRedirectUrl(originalUrl);
        //KLog.i("获取到链接，准备跳转B：" + originalUrl + ", newUrl = " + newUrl);
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
        // agentWeb.getWebCreator().getWebView().setHorizontalScrollBarEnabled(false);
        agentWeb.getWebCreator().getWebView().getSettings().setTextZoom(100);
        // 设置最小的字号，默认为8
        agentWeb.getWebCreator().getWebView().getSettings().setMinimumFontSize(10);
        // 设置最小的本地字号，默认为8
        agentWeb.getWebCreator().getWebView().getSettings().setMinimumLogicalFontSize(10);

        // 设置此属性，可任意比例缩放
        agentWeb.getWebCreator().getWebView().getSettings().setUseWideViewPort(true);
        // 缩放至屏幕的大小：如果webview内容宽度大于显示区域的宽度,那么将内容缩小,以适应显示区域的宽度, 默认是false
        agentWeb.getWebCreator().getWebView().getSettings().setLoadWithOverviewMode(true);
        // NARROW_COLUMNS 适应内容大小 ， SINGLE_COLUMN 自适应屏幕
        agentWeb.getWebCreator().getWebView().getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        //缩放操作
        agentWeb.getWebCreator().getWebView().getSettings().setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        agentWeb.getWebCreator().getWebView().getSettings().setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        agentWeb.getWebCreator().getWebView().getSettings().setDisplayZoomControls(false); //隐藏原生的缩放控件

        agentWeb.getWebCreator().getWebView().getSettings().setDefaultTextEncodingName(StandardCharsets.UTF_8.name());

        agentWeb.getWebCreator().getWebView().getSettings().setJavaScriptEnabled(true);
        // 支持通过js打开新的窗口
        agentWeb.getWebCreator().getWebView().getSettings().setJavaScriptCanOpenWindowsAutomatically(false);

        /* 缓存 */
        agentWeb.getWebCreator().getWebView().getSettings().setDomStorageEnabled(true); // 临时简单的缓存（必须保留，否则无法播放优酷视频网页，其他的可以）
        agentWeb.getWebCreator().getWebView().getSettings().setAppCacheEnabled(true); // 支持H5的 application cache 的功能
        // webSettings.setDatabaseEnabled(true);  // 支持javascript读写db
        //根据cache-control获取数据。
        agentWeb.getWebCreator().getWebView().getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        // 通过 file url 加载的 Javascript 读取其他的本地文件 .建议关闭
        agentWeb.getWebCreator().getWebView().getSettings().setAllowFileAccessFromFileURLs(false);
        // 允许通过 file url 加载的 Javascript 可以访问其他的源，包括其他的文件和 http，https 等其他的源
        agentWeb.getWebCreator().getWebView().getSettings().setAllowUniversalAccessFromFileURLs(false);
        // 允许访问文件
        agentWeb.getWebCreator().getWebView().getSettings().setAllowFileAccess(true);

        // 保存表单数据
        agentWeb.getWebCreator().getWebView().getSettings().setSaveFormData(true);
        agentWeb.getWebCreator().getWebView().getSettings().setSavePassword(true);

        // 允许在Android 5.0上 Webview 加载 Http 与 Https 混合内容。作者：Wing_Li，链接：https://www.jianshu.com/p/3fcf8ba18d7f
        agentWeb.getWebCreator().getWebView().getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        CookieManager instance = CookieManager.getInstance();
        instance.setAcceptCookie(true);
        instance.setAcceptThirdPartyCookies(agentWeb.getWebCreator().getWebView(), true);
        CookieManager.setAcceptFileSchemeCookies(true);

        agentWeb.getWebCreator().getWebView().getSettings().setMediaPlaybackRequiresUserGesture(true);

        /**
         * https://www.jianshu.com/p/6e38e1ef203a
         * 让 WebView 支持文件下载，主要思路有：1、跳转浏览器下载；2、使用系统的下载服务；3、自定义下载任务
         */
        agentWeb.getWebCreator().getWebView().setDownloadListener(
                new DownloadListenerS(WebActivity.this).setWebView(agentWeb.getWebCreator().getWebView())
        );

        String guessUserAgent = NetworkUserAgentConfig.i().guessUserAgentByUrl(link);
        if (!TextUtils.isEmpty(guessUserAgent)) {
            agentWeb.getWebCreator().getWebView().getSettings().setUserAgentString(guessUserAgent);
        }

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
                // final LongClickPopWindow webViewLongClickedPopWindow =
                //         new LongClickPopWindow(WebActivity.this, status, ScreenUtil.dp2px(WebActivity.this,120), ScreenUtil.dp2px(WebActivity.this,90));
                // webViewLongClickedPopWindow.showAtLocation(webView, Gravity.TOP|Gravity.LEFT, downX, downY + 10);
                new LongClickPopWindow(WebActivity.this, (WebView) webView, ScreenUtil.dp2px(WebActivity.this, 120), ScreenUtil.dp2px(WebActivity.this, 130), downX, downY + 10);
                return true;
            }
        });
    }


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
            KLog.e("屏幕：隐藏状态");

            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) appBarLayout.getChildAt(0).getLayoutParams();
            KLog.e("屏幕：隐藏状态" + containerLayout.getSystemUiVisibility());
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
        outState.putBoolean(Contract.isPortrait, isPortrait);
        KLog.i("自动保存，是否为竖屏：" + isPortrait);
        super.onSaveInstanceState(outState);
    }

    private void onRecoveryInstanceState(@NonNull Bundle outState) {
        if (outState.getBoolean(Contract.isPortrait, true)) {
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
        KLog.e(WebBridge.TAG, msg);
    }

    @Override
    public void toggleScreenOrientation() {
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


    protected WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, final WebResourceRequest request) {
            String scheme = request.getUrl().getScheme().trim();
            if (scheme.equalsIgnoreCase(HTTP) || scheme.equalsIgnoreCase(HTTPS)) {
                String url = request.getUrl().toString();
                if (AdBlock.i().isAd(url)) {
                    // 有广告的请求数据，我们直接返回空数据，注：不能直接返回null
                    return new WebResourceResponse(null, null, null);
                }

                String newUrl = LinkRewriteConfig.i().getRedirectUrl(url);
                KLog.e("重定向地址：" + url + " -> " + newUrl);
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

            KLog.i("地址：" + url);
            if (url.startsWith(SCHEMA_HTTP) || url.startsWith(SCHEMA_HTTPS)) {
                return false;
            }

            String newUrl = LinkRewriteConfig.i().getRedirectUrl( url );
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
                    KLog.e(R.string.unable_to_find_app);
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
            KLog.i("onPageStarted = " + url);
            super.onPageStarted(webView, url, favicon);

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
            view.loadUrl(VideoInjectUtil.fullScreenJsFun(url));
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
            //KLog.e("接受http错误 = " + request.getUrl() + errorResponse);
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            //KLog.e("接受Ssl错误 = " + view.getUrl() + error);
            handler.proceed();//接受证书
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            //KLog.e("接受错误 = " + request.getUrl() + error);
        }
    };

    MenuItem itemRefresh, itemStop;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_web, menu);
        itemRefresh = menu.findItem(R.id.web_menu_refresh);
        itemStop = menu.findItem(R.id.web_menu_stop);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //监听左上角的返回箭头
            case android.R.id.home:
                exit();
                break;
            case R.id.web_menu_user_agent:
                final ArrayList<String> uaTitle = new ArrayList<>();
                String holdUA = NetworkUserAgentConfig.i().getHoldUserAgent();
                uaTitle.add(getString(R.string.default_x));
                int i = 0;
                int selected = 0;
                for (Map.Entry<String, String> entry : NetworkUserAgentConfig.i().getUserAgents().entrySet()) {
                    uaTitle.add(entry.getKey());
                    if(entry.getKey().equals(holdUA)){
                        selected = i;
                    }
                    i++;
                    KLog.e("标题：" + entry.getKey());
                }

                int finalSelected = selected;
                new MaterialDialog.Builder(WebActivity.this)
                        .title(R.string.select_user_agent)
                        .items(uaTitle)
                        .itemsCallbackSingleChoice(selected + 1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, final int which, CharSequence text) {
                                // 默认
                                if (which == 0) {
                                    NetworkUserAgentConfig.i().setHoldUserAgent(null);
                                }
                                // 手动选择项
                                else if (which - 1 != finalSelected) {
                                    NetworkUserAgentConfig.i().setHoldUserAgent(text.toString());

                                }
                                NetworkUserAgentConfig.i().save();
                                agentWeb.getWebCreator().getWebView().getSettings().setUserAgentString(NetworkUserAgentConfig.i().guessUserAgentByUrl(receivedUrl));
                                agentWeb.getWebCreator().getWebView().reload();
                                // KLog.e("默认的UA是：" + agentWeb.getWebCreator().getWebView().getSettings().getUserAgentString() );
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
                                                NetworkUserAgentConfig.i().setHoldUserAgent(getString(R.string.custom));
                                                NetworkUserAgentConfig.i().getUserAgents().put(getString(R.string.custom),input.toString());
                                                NetworkUserAgentConfig.i().save();
                                                KLog.e("当前输入的是：" + input.toString());
                                                agentWeb.getWebCreator().getWebView().getSettings().setUserAgentString(NetworkUserAgentConfig.i().guessUserAgentByUrl(receivedUrl));
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
                                                NetworkUserAgentConfig.i().setHoldUserAgent(getString(R.string.custom));
                                                NetworkUserAgentConfig.i().getUserAgents().remove(getString(R.string.custom));
                                                NetworkUserAgentConfig.i().save();
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
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Android旋转屏幕不销毁Activity
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


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


    /**
     *
     作者：AmatorLee
     链接：https://www.jianshu.com/p/b008e04987e0
     來源：简书
     简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。

     #####二、仿魅族应用商店应用详情效果
     作为一个多年的魅族手机使用者，看起来魅族的应用商店也挺不错的，来看看要实现的效果（**注：实现效果而非实现实现界面**）

     ![sheet](http://upload-images.jianshu.io/upload_images/2605454-3b9fc78ca7aaaa87.gif?imageMogr2/auto-orient/strip)

     1. 思路一：使用Activity实现，但是这样需要解决的问题有：
     1.  Activity进场/出场动画
     2.     对于滑动的监听
     3. 对状态栏的动态改变

     2. 思路二：由于我们这边使用的是Behaviour，而系统给我们提供了一个```BottomSheetBehavior```应该可以完美的给我们解决滑动的问题，但是Activity方面的问题依然存在，然后找到了一个强大的Dialog(```BottomSheetDialog```)和一个DialogFragment(```BottomSheetDialogFragment```),,以我夜观天象应该这个就是实现了``````BottomSheetBehavior```的View，很好很强大。
     我们来看看我们是怎样是实现的：

     作者：AmatorLee
     链接：https://www.jianshu.com/p/b008e04987e0
     來源：简书
     简书著作权归作者所有，任何形式的转载都请联系作者获得授权并注明出处。
     */
    // private void test(){
    //
    //     /**
    //      * BottomSheetDialog
    //      */
    //     Button btnShowDialog = (Button) findViewById(R.id.bottom_pull_sheet);
    //     mDatas = new ArrayList<>();
    //     View inflate = getLayoutInflater().inflate(R.layout.dialog_bottom_sheet, null);
    //     //        mLeftIcon = inflate.findViewById(R.id.delete);
    //     //        mLeftIcon.setOnClickListener(new View.OnClickListener() {
    //     //            @Override
    //     //            public void onClick(View view) {
    //     //                if (mBottomSheetDialog != null && mBottomSheetDialog.isShowing()) {
    //     //                    mBottomSheetDialog.dismiss();
    //     //                }
    //     //            }
    //     //        });
    //     //        RecyclerView recyclerView = inflate.findViewById(R.id.recyclerView);
    //     //        mDatas = new ArrayList<>();
    //     //        for (int i = 0; i < 50; i++) {
    //     //            mDatas.add("这是第" + i + "个数据");
    //     //        }
    //     //        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    //     //        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    //     //        Adapter adapter = new Adapter();
    //     //        recyclerView.setAdapter(adapter);
    //
    //     BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
    //     mBottomSheetDialog.setContentView(inflate);
    //     View container = mBottomSheetDialog.getDelegate().findViewById(android.support.design.R.id.design_bottom_sheet);
    //     final BottomSheetBehavior containerBehaviour = BottomSheetBehavior.from(container);
    //     containerBehaviour.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
    //         @Override
    //         public void onStateChanged(@NonNull View bottomSheet, int newState) {
    //             KLog.e(TAG, "onStateChanged: newState === " + newState);
    //             if (newState == BottomSheetBehavior.STATE_HIDDEN) {
    //                 mBottomSheetDialog.dismiss();
    //                 containerBehaviour.setState(BottomSheetBehavior.STATE_COLLAPSED);
    //             } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
    //                 //强制修改弹出高度为屏幕高度的0.9倍，不做此操作仅仅有CollapSed/Expand两种，就是0.5和1倍展开的效果
    //                 containerBehaviour.setPeekHeight((int) (0.9 * height));
    //             }
    //         }
    //
    //         @Override
    //         public void onSlide(@NonNull View bottomSheet, float slideOffset) {
    //             KLog.d("BottomBahaviour", "onSlide: slideOffset====" + slideOffset);
    //             if (slideOffset == 1.0) {
    //                 //                    mLeftIcon.setImageResource(R.drawable.back);
    //                 // containerBehaviour.setPeekHeight(height + getStatusBarHeight());
    //                 //修改状态栏
    //                 mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    //                 if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    //                     mBottomSheetDialog.getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
    //                     try {
    //                         //修改魅族系统状态栏字体颜色
    //                         WindowManager.LayoutParams lp = mBottomSheetDialog.getWindow().getAttributes();
    //                         Field darkFlag = WindowManager.LayoutParams.class
    //                                 .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
    //                         Field meizuFlags = WindowManager.LayoutParams.class
    //                                 .getDeclaredField("meizuFlags");
    //                         darkFlag.setAccessible(true);
    //                         meizuFlags.setAccessible(true);
    //                         int bit = darkFlag.getInt(null);
    //                         int value = meizuFlags.getInt(lp);
    //                         value |= bit;
    //                         meizuFlags.setInt(lp, value);
    //                         mBottomSheetDialog.getWindow().setAttributes(lp);
    //
    //                     } catch (Exception e) {
    //
    //                     }
    //                 }
    //             } else {
    //
    //                 mLeftIcon.setImageResource(R.drawable.icon_delete);
    //             }
    //         }
    //     });
    //
    //     btnShowDialog.setOnClickListener(new View.OnClickListener() {
    //         @Override
    //         public void onClick(View view) {
    //             if (!mBottomSheetDialog.isShowing()) {
    //                 containerBehaviour.setPeekHeight((int) (0.9 * height));
    //                 mBottomSheetDialog.portrait();
    //             } else {
    //                 mBottomSheetDialog.dismiss();
    //             }
    //         }
    //     });
    // }


}
