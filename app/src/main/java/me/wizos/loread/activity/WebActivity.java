package me.wizos.loread.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.NestedScrollAgentWebView;
import com.socks.library.KLog;

import java.util.HashMap;

import me.wizos.loread.R;
import me.wizos.loread.utils.SnackbarUtil;

/**
 * @author Wizos
 */
// 内置的webview页面，用来相应 a，iframe，video 的跳转内容
public class WebActivity extends AppCompatActivity {
    protected static final String TAG = "WebActivity";

    private AgentWeb agentWeb;
    //    private NestedScrollAgentWebView agentWeb;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        CoordinatorLayout containerLayout = this.findViewById(R.id.web_root);
        mToolbar = this.findViewById(R.id.web_toolbar);
//        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setHomeButtonEnabled(true);
        // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
//        mTitleTextView = (TextView) this.findViewById(R.id.toolbar_title);
        String link = getIntent().getStringExtra("link");
        KLog.e("获取到链接，准备跳转" + link);

        CoordinatorLayout.LayoutParams lp = new CoordinatorLayout.LayoutParams(-1, -1);
        lp.setBehavior(new AppBarLayout.ScrollingViewBehavior());

        agentWeb = AgentWeb.with(this)
                .setAgentWebParent(containerLayout, 1, lp)//lp记得设置behavior属性
//                .setAgentWebParent( containerLayout, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))//传入AgentWeb的父控件。
                .useDefaultIndicator(-1, 3)//设置进度条颜色与高度，-1为默认值，高度为2，单位为dp。
                .setWebView(new NestedScrollAgentWebView(this))
                .setWebViewClient(mWebViewClient)//WebViewClient ， 与 WebView 使用一致 ，但是请勿获取WebView调用setWebViewClient(xx)方法了,会覆盖AgentWeb DefaultWebClient,同时相应的中间件也会失效。
                .setWebChromeClient(mWebChromeClient) //WebChromeClient
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK) //严格模式 Android 4.2.2 以下会放弃注入对象 ，使用AgentWebView没影响。
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.ASK)//打开其他应用时，弹窗咨询用户是否前往其他应用
                .interceptUnkownUrl() //拦截找不到相关页面的Scheme
//                .setAgentWebWebSettings(getSettings())//设置 IAgentWebSettings。
//                .setPermissionInterceptor(mPermissionInterceptor) //权限拦截 2.0.0 加入。
//                .setAgentWebUIController(new UIController(getActivity())) //自定义UI  AgentWeb3.0.0 加入。
                .setMainFrameErrorView(R.layout.agentweb_error_page, -1) //参数1是错误显示的布局，参数2点击刷新控件ID -1表示点击整个布局都刷新， AgentWeb 3.0.0 加入。

//                .setDownloadListener(mDownloadListener) 4.0.0 删除该API//下载回调
//                .openParallelDownload()// 4.0.0删除该api 打开并行下载 , 默认串行下载。 请通过AgentWebDownloader#Extra实现并行下载
//                .setNotifyIcon(R.drawable.ic_file_download_black_24dp) 4.0.0删除该api //下载通知图标。4.0.0后的版本请通过AgentWebDownloader#Extra修改icon
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.DISALLOW)//打开其他页面时，弹窗质询用户前往其他应用 AgentWeb 3.0.0 加入。
                .interceptUnkownUrl() //拦截找不到相关页面的Url AgentWeb 3.0.0 加入。
                .createAgentWeb()//创建AgentWeb。
                .ready()//设置 WebSettings。
                .loadUrl(link); //WebView载入该url地址的页面并显示。
    }


    protected WebChromeClient mWebChromeClient = new WebChromeClient() {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            //  super.onProgressChanged(view, newProgress);
            KLog.i(TAG, "onProgressChanged:" + newProgress + "  view:" + view);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            if (!TextUtils.isEmpty(title)) {
                mToolbar.setTitle(title);
            }
        }
    };

    protected WebViewClient mWebViewClient = new WebViewClient() {
        private HashMap<String, Long> timer = new HashMap<>();

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return shouldOverrideUrlLoading(view, request.getUrl() + "");
        }

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            KLog.i(TAG, "view:" + new Gson().toJson(view.getHitTestResult()));
            KLog.i(TAG, "mWebViewClient shouldOverrideUrlLoading:" + url);
            //优酷想唤起自己应用播放该视频 ， 下面拦截地址返回 true  则会在应用内 H5 播放 ，禁止优酷唤起播放该视频， 如果返回 false ， DefaultWebClient  会根据intent 协议处理 该地址 ， 首先匹配该应用存不存在 ，如果存在 ， 唤起该应用播放 ， 如果不存在 ， 则跳到应用市场下载该应用 .
            if (url.startsWith("http") || url.startsWith("https")) {
                return false;
            } else {  //其他的URL则会开启一个Acitity然后去调用原生APP
                final Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                if (in.resolveActivity(getPackageManager()) != null) {
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    KLog.e("应用星系：" + in.resolveActivity(getPackageManager()).getPackageName());
                    KLog.e("应用星系：" + in.resolveActivity(getPackageManager()));
                    String name = "相应的";
                    try {
                        name = "" + getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(in.resolveActivity(getPackageManager()).getPackageName(), PackageManager.GET_META_DATA));
//                        name = getPackageManager().getApplicationInfo(in.resolveActivity(getPackageManager()).getPackageName(), PackageManager.GET_META_DATA).labelRes();
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    SnackbarUtil.Long(mToolbar, "跳转到“" + name + "”应用？")
//                                .above(bt_gravity_center,total,16,16)
                            .setAction("允许", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    startActivity(in);
                                }
                            }).show();
                } else {
                    // TODO: 2018/4/25  说明系统中不存在这个activity。弹出一个Toast提示是否要用外部应用打开
                    KLog.e("本地未安装能打开scheme链接的应用");
                }
                return true;
            }

//            if (url.startsWith("intent://") && url.contains("com.youku.phone")) {
//                return true;
//            }
//            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            KLog.i(TAG, "mUrl:" + url + " onPageStarted  target:" + getIntent().getStringExtra("link"));
            timer.put(url, System.currentTimeMillis());
            if (url.equals(getIntent().getStringExtra("link"))) {
//                pageNavigator(View.GONE);
            } else {
//                pageNavigator(View.VISIBLE);
            }

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if (timer.get(url) != null) {
                long overTime = System.currentTimeMillis();
                Long startTime = timer.get(url);
                KLog.i(TAG, "  page mUrl:" + url + "  used time:" + (overTime - startTime));
            }

        }
        /*错误页回调该方法 ， 如果重写了该方法， 上面传入了布局将不会显示 ， 交由开发者实现，注意参数对齐。*/
	   /* public void onMainFrameError(AbsAgentWebUIController agentWebUIController, WebView view, int errorCode, String description, String failingUrl) {
            Log.i(TAG, "AgentWebFragment onMainFrameError");
            agentWebUIController.onMainFrameError(view,errorCode,description,failingUrl);

        }*/

        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
            super.onReceivedHttpError(view, request, errorResponse);
//			Log.i(TAG, "onReceivedHttpError:" + 3 + "  request:" + mGson.toJson(request) + "  errorResponse:" + mGson.toJson(errorResponse));
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
//			Log.i(TAG, "onReceivedError:" + errorCode + "  description:" + description + "  errorResponse:" + failingUrl);
        }
    };

    /**
     * Android旋转屏幕不销毁Activity
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (agentWeb.handleKeyEvent(keyCode, event)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
//    @Override
//    public void onBackPressed() {
//        if(agentWeb.getWebCreator().getWebView().canGoBack()){
//            agentWeb.back();
//        }else {
//            super.onBackPressed();
//        }
//    }

    @Override
    protected void onPause() {
        agentWeb.getWebLifeCycle().onPause();
        super.onPause();

    }

    @Override
    protected void onResume() {
        agentWeb.getWebLifeCycle().onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        agentWeb.getWebLifeCycle().onDestroy();
    }

//    private void pageNavigator(int tag) {
//        mBackImageView.setVisibility(tag);
//        mLineView.setVisibility(tag);
//    }
}
