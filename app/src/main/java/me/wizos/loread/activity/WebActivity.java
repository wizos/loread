package me.wizos.loread.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.NestedScrollAgentWebView;
import com.socks.library.KLog;

import java.util.ArrayList;

import me.wizos.loread.R;
import me.wizos.loread.bean.config.GlobalConfig;
import me.wizos.loread.bean.config.UserAgent;
import me.wizos.loread.utils.InjectUtil;
import me.wizos.loread.utils.ToastUtil;
import me.wizos.loread.utils.Tool;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.webview.AdBlock;
import me.wizos.loread.view.webview.DownloadListenerS;

/**
 * @author Wizos
 */
// 内置的webview页面，用来相应 a，iframe 的跳转内容
public class WebActivity extends BaseActivity {
    public static final String TAG = BaseActivity.class.getSimpleName();

    private AgentWeb agentWeb;
    private Toolbar mToolbar;
    private CoordinatorLayout containerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        containerLayout = this.findViewById(R.id.web_root);

        mToolbar = this.findViewById(R.id.web_toolbar);
        mToolbar.setTitle("加载中");
        setSupportActionBar(mToolbar);
        // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setHomeButtonEnabled(true);
        // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);


        String link = getIntent().getDataString();
        // 补救，获取 link
        if (TextUtils.isEmpty(link)) {
            link = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        }
        KLog.e("获取到链接，准备跳转" + link);

        initWebView(link);
    }


    private void initWebView(String link) {
        CoordinatorLayout.LayoutParams lp = new CoordinatorLayout.LayoutParams(-1, -1);
        lp.setBehavior(new AppBarLayout.ScrollingViewBehavior());

        agentWeb = AgentWeb.with(this)
                .setAgentWebParent(containerLayout, -1, lp)//lp记得设置behavior属性
//                .setAgentWebParent( containerLayout, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))//传入AgentWeb的父控件。
                .useDefaultIndicator(-1, 3)//设置进度条颜色与高度，-1为默认值，高度为2，单位为dp。
                .setWebView(new NestedScrollAgentWebView(this))
                .setWebViewClient(mWebViewClient)//WebViewClient ， 与 WebView 使用一致 ，但是请勿获取WebView调用setWebViewClient(xx)方法了,会覆盖AgentWeb DefaultWebClient,同时相应的中间件也会失效。
                .setWebChromeClient(mWebChromeClient) //WebChromeClient
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK) //严格模式 Android 4.2.2 以下会放弃注入对象 ，使用AgentWebView没影响。
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)//打开其他应用时，弹窗咨询用户是否前往其他应用
                .addJavascriptInterface("VideoBridge", new Object() {
                    @JavascriptInterface
                    public void toggleScreenOrientation() {
                        WebActivity.this.toggleScreenOrientation();
                    }

                    @JavascriptInterface
                    public void log(String paramString) {
                        KLog.e("VideoBridge", paramString);
                    }
                })
//                .interceptUnkownUrl() //拦截找不到相关页面的Scheme
//                .setAgentWebWebSettings(getSettings())//设置 IAgentWebSettings。
//                .setPermissionInterceptor(mPermissionInterceptor) //权限拦截 2.0.0 加入。
//                .setAgentWebUIController(new UIController(getActivity())) //自定义UI  AgentWeb3.0.0 加入。
                .setMainFrameErrorView(R.layout.agentweb_error_page, -1) //参数1是错误显示的布局，参数2点击刷新控件ID -1表示点击整个布局都刷新， AgentWeb 3.0.0 加入。
//                .setDownloadListener(mDownloadListener) // 4.0.0 删除该API//下载回调
//                .openParallelDownload()// 4.0.0删除该api 打开并行下载 , 默认串行下载。 请通过AgentWebDownloader#Extra实现并行下载
//                .setNotifyIcon(R.drawable.ic_file_download_black_24dp) 4.0.0删除该api //下载通知图标。4.0.0后的版本请通过AgentWebDownloader#Extra修改icon
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.DISALLOW)//打开其他页面时，弹窗质询用户前往其他应用 AgentWeb 3.0.0 加入。
                .interceptUnkownUrl() //拦截找不到相关页面的Url AgentWeb 3.0.0 加入。
                .createAgentWeb()//创建AgentWeb。
                .ready()//设置 WebSettings。
                .loadUrl(link); //WebView载入该url地址的页面并显示。

        /**
         * https://www.jianshu.com/p/6e38e1ef203a
         * 让 WebView 支持文件下载，主要思路有：1、跳转浏览器下载；2、使用系统的下载服务；3、自定义下载任务
         */
        agentWeb.getWebCreator().getWebView().setDownloadListener(
                new DownloadListenerS(WebActivity.this).setWebView(agentWeb.getWebCreator().getWebView())
        );

        if (GlobalConfig.i().getUserAgentIndex() == -2) {
            String guessUserAgent = GlobalConfig.i().guessUserAgentByUrl(link);
            if (!TextUtils.isEmpty(guessUserAgent)) {
                agentWeb.getWebCreator().getWebView().getSettings().setUserAgentString(guessUserAgent);
            }
        } else if (GlobalConfig.i().getUserAgentIndex() != -1) {
            agentWeb.getWebCreator().getWebView().getSettings().setUserAgentString(GlobalConfig.i().getUserAgentString());
        }
        agentWeb.getWebCreator().getWebView().getSettings().setUseWideViewPort(true);
        agentWeb.getWebCreator().getWebView().getSettings().setSupportZoom(true);
        agentWeb.getWebCreator().getWebView().getSettings().setTextZoom(100);
        agentWeb.getWebCreator().getWebView().getSettings().setBuiltInZoomControls(true);
        agentWeb.getWebCreator().getWebView().getSettings().setLoadWithOverviewMode(true);
        agentWeb.getWebCreator().getWebView().getSettings().setSavePassword(true);
        agentWeb.getWebCreator().getWebView().getSettings().setSaveFormData(true);// 保存表单数据

        Tool.setBackgroundColor(agentWeb.getWebCreator().getWebView());
    }

//    private WebViewS webViewS = new WebViewS(App.i());
//    private void initWebView2( String link ){
////        WebViewS webViewS = new WebViewS(App.i());
//        containerLayout.addView(webViewS);
//
//        webViewS.setWebViewClient( mWebViewClient );
//        webViewS.setWebChromeClient(mWebChromeClient );
//        webViewS.addJavascriptInterface( new Object(){
//            @JavascriptInterface
//            public void toggleScreenOrientation(){
//                WebActivity.this.toggleScreenOrientation();
//            }
//            @JavascriptInterface
//            public void log(String paramString) {
//                KLog.e("VideoBridge", paramString);
//            }
//        } , "VideoBridge");
//
//        webViewS.setDownloadListener(
//                new DownloadListenerS(WebActivity.this).setWebView(webViewS)
//        );
//
//
//        if( GlobalConfig.i().getUserAgentIndex() == -2 ){
//            String guessUserAgent = GlobalConfig.i().guessUserAgentByUrl(link);
//            if( !TextUtils.isEmpty(guessUserAgent) ){
//                webViewS.getSettings().setUserAgentString( guessUserAgent );
//            }
//        }else if( GlobalConfig.i().getUserAgentIndex()!= -1 ){
//            webViewS.getSettings().setUserAgentString( GlobalConfig.i().getUserAgentString() );
//        }
//        Tool.setBackgroundColor(webViewS);
//        webViewS.loadUrl(link);
//    }


    private void toggleScreenOrientation() {
        // 横排PORTRAIT, LANDSCAPE 竖排
        if (WebActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            WebActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } else {
            WebActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }
    protected WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (!TextUtils.isEmpty(title)) {
                mToolbar.setTitle(title);
                mToolbar.setSubtitle(agentWeb.getWebCreator().getWebView().getUrl());
//                mToolbar.setSubtitle( webViewS.getUrl());
            }
        }
    };


    protected WebViewClient mWebViewClient = new WebViewClient() {
        @Deprecated
        @SuppressLint("NewApi")
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (GlobalConfig.i().isBlockAD() && AdBlock.i().isAd(url)) {
                // 有广告的请求数据，我们直接返回空数据，注：不能直接返回null
                return new WebResourceResponse(null, null, null);
            }
            return super.shouldInterceptRequest(view, (url));
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return shouldInterceptRequest(view, request.getUrl() + "");
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return shouldOverrideUrlLoading(view, request.getUrl() + "");
        }
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            //优酷想唤起自己应用播放该视频 ， 下面拦截地址返回 true  则会在应用内 H5 播放 ，禁止优酷唤起播放该视频， 如果返回 false ， DefaultWebClient  会根据intent 协议处理 该地址 ， 首先匹配该应用存不存在 ，如果存在 ， 唤起该应用播放 ， 如果不存在 ， 则跳到应用市场下载该应用 .
            if (url.startsWith("http") || url.startsWith("https")) {
                return false;
            }
            //其他的URL则会开启一个Acitity然后去调用原生APP
            final Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (in.resolveActivity(getPackageManager()) != null) {
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                String name = "相应的";
                try {
                    name = "" + getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(in.resolveActivity(getPackageManager()).getPackageName(), PackageManager.GET_META_DATA));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                new MaterialDialog.Builder(WebActivity.this)
                        .content("是否跳转到「" + name + "」应用？")
                        .negativeText("取消")
                        .positiveText(R.string.agree)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                startActivity(in);
//                                overridePendingTransition(R.anim.fade_in, R.anim.out_from_bottom);
                            }
                        })
                        .show();
            } else {
                // TODO: 2018/4/25  说明系统中不存在这个activity。弹出一个Toast提示是否要用外部应用打开
                KLog.e("本地未安装能打开scheme链接的应用");
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // 注入视频全屏js
            view.loadUrl(InjectUtil.fullScreenJsFun(url));
        }

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_web, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.web_menu_user_agent:
                final ArrayList<String> uaTitle = new ArrayList<>();
                uaTitle.add("默认");
                for (UserAgent userAgent : GlobalConfig.i().getUserAgents()) {
                    uaTitle.add(userAgent.getName());
                    KLog.e("标题：" + userAgent.getName());
                }
                final int index = GlobalConfig.i().getUserAgentIndex();

//                KLog.e("菜单被点击1" + App.i().globalConfig );
                KLog.e("当前选择的是：" + index);

                new MaterialDialog.Builder(WebActivity.this)
                        .title("选择UA标识")
                        .items(uaTitle)
                        .itemsCallbackSingleChoice(index + 1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, final int which, CharSequence text) {
                                // 默认
                                if (which == 0) {
                                    GlobalConfig.i().setUserAgentIndex(which - 1);
                                    GlobalConfig.i().save();
//                                    webViewS.getSettings().setUserAgentString( null );
//                                    webViewS.reload();
                                    agentWeb.getWebCreator().getWebView().getSettings().setUserAgentString(null);
                                    agentWeb.getWebCreator().getWebView().reload();
//                                    KLog.e("默认的UA是：" + agentWeb.getWebCreator().getWebView().getSettings().getUserAgentString() );
                                }
                                // 手动选择项
                                else if (which - 1 != index) {
                                    GlobalConfig.i().setUserAgentIndex(which - 1);
                                    GlobalConfig.i().save();
//                                    webViewS.getSettings().setUserAgentString( GlobalConfig.i().getUserAgentString() );
//                                    webViewS.reload();
                                    agentWeb.getWebCreator().getWebView().getSettings().setUserAgentString(GlobalConfig.i().getUserAgentString());
                                    agentWeb.getWebCreator().getWebView().reload();
                                }
                                dialog.dismiss();
                                return true;
                            }
                        })
                        .neutralText("自定义UA")
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                new MaterialDialog.Builder(WebActivity.this)
                                        .title("输入UA标识")
                                        .inputType(InputType.TYPE_CLASS_TEXT)
                                        .inputRange(12, 200)
                                        .input(null, GlobalConfig.i().getUserAgentString(), new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                                GlobalConfig.i().setUserAgentIndex(GlobalConfig.i().getUserAgents().size());
                                                GlobalConfig.i().getUserAgents().add(new UserAgent("自定义", input.toString()));
                                                GlobalConfig.i().save();
                                                KLog.e("当前输入的是：" + input.toString());
//                                                webViewS.getSettings().setUserAgentString( GlobalConfig.i().getUserAgentString() );
//                                                webViewS.reload();
                                                agentWeb.getWebCreator().getWebView().getSettings().setUserAgentString(GlobalConfig.i().getUserAgentString());
                                                agentWeb.getWebCreator().getWebView().reload();
                                            }
                                        })
                                        .positiveText(R.string.confirm)
                                        .negativeText(android.R.string.cancel)
                                        .neutralText("删除自定义UA")
                                        .neutralColor(WebActivity.this.getResources().getColor(R.color.material_red_400))
                                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                if (GlobalConfig.i().getUserAgents().size() == 4) {
                                                    GlobalConfig.i().getUserAgents().remove(3);
                                                }
                                                if (GlobalConfig.i().getUserAgentIndex() == 3) {
                                                    GlobalConfig.i().setUserAgentIndex(-1);
                                                }
                                            }
                                        })
                                        .show();
                            }
                        })
                        .show();
                break;
            //监听左上角的返回箭头
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                break;
            case R.id.web_menu_open_by_sys:
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setData(Uri.parse(agentWeb.getWebCreator().getWebView().getUrl()));
//                intent.setData(Uri.parse(webViewS.getUrl()));
                startActivity(intent);
                break;
            case R.id.web_menu_copy_link:
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("url", agentWeb.getWebCreator().getWebView().getUrl());
//                ClipData mClipData = ClipData.newPlainText("url",webViewS.getUrl());
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
                ToastUtil.showLong("复制成功");
                break;
            case R.id.web_menu_share:
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, mToolbar.getTitle());
//                sendIntent.putExtra(Intent.EXTRA_TEXT, mToolbar.getTitle() + " " +  webViewS.getUrl() );
                sendIntent.putExtra(Intent.EXTRA_TEXT, mToolbar.getTitle() + " " + agentWeb.getWebCreator().getWebView().getUrl());
                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(sendIntent, "分享到"));
//                overridePendingTransition(R.anim.fade_in, R.anim.out_from_bottom);
                break;
            case R.id.web_menu_refresh:
//                webViewS.reload();
                agentWeb.getWebCreator().getWebView().reload();
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


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 后者为短期内按下的次数
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (agentWeb.getWebCreator().getWebView().canGoBack()) {
                agentWeb.back();

//            if( webViewS.canGoBack()){
//                webViewS.goBack();
            } else {
                this.finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onPause() {
        super.onPause();
        agentWeb.getWebLifeCycle().onPause();
//        webViewS.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        agentWeb.getWebLifeCycle().onResume();
//        webViewS.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        agentWeb.getWebLifeCycle().onDestroy();
//        webViewS.destroy();
    }

    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        mColorfulBuilder
                .backgroundColor(R.id.web_root, R.attr.root_view_bg)
                .backgroundColor(R.id.web_toolbar, R.attr.topbar_bg);
        return mColorfulBuilder;
    }
}
