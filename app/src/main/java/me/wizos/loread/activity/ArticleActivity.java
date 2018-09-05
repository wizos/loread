package me.wizos.loread.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.MutableContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.socks.library.KLog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.wizos.loread.App;
import me.wizos.loread.BuildConfig;
import me.wizos.loread.R;
import me.wizos.loread.bean.config.GlobalConfig;
import me.wizos.loread.common.ImageBridge;
import me.wizos.loread.contentextractor.Extractor;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithPref;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.Tag;
import me.wizos.loread.net.Api;
import me.wizos.loread.net.DataApi;
import me.wizos.loread.service.SubService;
import me.wizos.loread.utils.DataUtil;
import me.wizos.loread.utils.NetworkUtil;
import me.wizos.loread.utils.SnackbarUtil;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.utils.ToastUtil;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.SlideArrow.SlideArrowLayout;
import me.wizos.loread.view.SwipeRefreshLayoutS;
import me.wizos.loread.view.WebViewS;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.webview.AdBlock;
import me.wizos.loread.view.webview.SlowlyProgressBar;
import me.wizos.loread.view.webview.VideoImpl;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;
import top.zibin.luban.OnRenameListener;

/**
 * @author Wizos on 2017
 */
@SuppressLint("SetJavaScriptEnabled")
public class ArticleActivity extends BaseActivity implements ImageBridge, SlideArrowLayout.SlideListener {
    protected static final String TAG = "ArticleActivity";

    private SwipeRefreshLayoutS swipeRefreshLayoutS;
    //    private SmartRefreshLayout swipeRefreshLayoutS;
    private SlowlyProgressBar slowlyProgressBar;
    private IconFontView starView, readView, saveView;
    private WebViewS selectedWebView;
    private SlideArrowLayout entryView;
    private Toolbar toolbar;
    private VideoImpl video;

    private Article selectedArticle;
    private int articleNo;
    private String articleId;

    private TextView articleNumView;
    private int articleCount;

    public Handler articleHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 0 && selectedWebView != null && selectedWebView.getProgress() < 100) {
                selectedWebView.stopLoading();
                canSlide = true;
            }
            return false;
        }
    });

    /**
     * Video 视频播放类
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        Bundle bundle;
        if (savedInstanceState != null) {
            bundle = savedInstanceState;
        } else {
            bundle = getIntent().getExtras();
        }
        // setSelection 没有滚动效果，直接跳到指定位置。smoothScrollToPosition 有滚动效果的
        // 文章在列表中的位置编号，下标从 0 开始
        articleNo = bundle.getInt("articleNo");
        // 列表中所有的文章数目
        articleCount = bundle.getInt("articleCount");
        articleId = bundle.getString("articleID");
//        articleIDs = bundle.getStringArrayList("articleIDs");

//        KLog.e("开始初始化数据2" + articleNo + "==" + articleCount + "==" + articleId + " == " + articleIDs );
        initToolbar();
        initView(); // 初始化界面上的 View，将变量映射到布局上。
        initSelectedPage(articleNo);
        startService(new Intent(this, SubService.class));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    public void onResume() {
        selectedWebView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        selectedWebView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // 如果参数为null的话，会将所有的Callbacks和Messages全部清除掉。
        // 这样做的好处是在 Acticity 退出的时候，可以避免内存泄露。因为 handler 内可能引用 Activity ，导致 Activity 退出后，内存泄漏
        KLog.e("onDestroy：" + selectedWebView);
        OkGo.cancelAll(articleHttpClient);
        articleHandler.removeCallbacksAndMessages(null);
        entryView.removeAllViews();
        saveArticleProgress();
        selectedWebView.destroy();
        selectedWebView = null;
        super.onDestroy();
    }

    public void saveArticleProgress() {
        if (selectedWebView == null) {
            return;
        }
        int scrollY = selectedWebView.getScrollY();
        App.i().articleProgress.put(articleId, scrollY);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("articleNo", 0);
        outState.putInt("articleCount", 1);
        outState.putString("articleId", articleId);
        KLog.e("自动保存：" + articleNo + "==" + "==" + articleId);
        super.onSaveInstanceState(outState);
    }


    @JavascriptInterface
    @Override
    public void log(String paramString) {
        KLog.e("ImageBridge", "【log】" + paramString);
    }


    @JavascriptInterface
    @Override
    public void loadImage(String articleId, int index, String url, final String originalUrl) {
        // 去掉已经缓存了图片的 url
//        KLog.e("loadImage", "【log】" + articleId + "  "+  index + "  -   " + url + "  " + originalUrl);
        if (!url.startsWith("file:///android_asset/") || TextUtils.isEmpty(articleId) || !selectedArticle.getId().equals(articleId)) {
            return;
        }
        // 1.网络不可用 → 返回失败占位图
        if (!NetworkUtil.isNetworkAvailable()) {
            selectedWebView.post(new Runnable() {
                @Override
                public void run() {
                    selectedWebView.loadUrl("javascript:onImageLoadFailed('" + originalUrl + "')");
                }
            });
        } else if (WithPref.i().isDownImgOnlyWifi() && !NetworkUtil.isWiFiUsed()) {
            selectedWebView.post(new Runnable() {
                @Override
                public void run() {
                    selectedWebView.loadUrl("javascript:onImageLoadNeedClick('" + originalUrl + "')");
                }
            });
        } else {
            downImage(articleId, index, originalUrl);
        }
    }

    @JavascriptInterface
    @Override
    public void openImage(String articleId, String imageFilePath, int index) {
        KLog.e("ImageBridge", "打开图片" + imageFilePath + index);
        // 过滤空格回车标签

        Pattern P_COMPRESSED = Pattern.compile("me\\.wizos\\.loread/files/cache/(.*?)/compressed", Pattern.CASE_INSENSITIVE);
        Matcher m = P_COMPRESSED.matcher(imageFilePath);
        if (m.find()) {
            String id = m.group(1);
            imageFilePath = m.replaceFirst("me.wizos.loread/files/cache/" + id + "/" + id + "_files");
            if (!new File(imageFilePath).exists()) {
                imageFilePath = m.replaceFirst("me.wizos.loread/files/cache/" + id + "/original");
            }
        }

//        try {
//        }catch (IllegalStateException e){
//            KLog.e("ImageBridge", "打开图片报错"  );
//        }

//        KLog.e("ImageBridge", "打开图片：" + m.group(1) );

        // 直接打开内置图片浏览器
        Intent intent = new Intent(ArticleActivity.this, ImageActivity.class);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(Uri.fromFile(new File(imageFilePath)), "image/*");
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        // 调用系统默认的图片查看应用
//        Intent intentImage = new Intent(Intent.ACTION_VIEW);
//        intentImage.addCategory(Intent.CATEGORY_DEFAULT);
//        File file = new File(imageFilePath);
//        intentImage.setDataAndType(Uri.fromFile(file), "image/*");
//        startActivity(intentImage);

        // 每次都要选择打开方式
//        startActivity(Intent.createChooser(intentImage, "请选择一款"));

        // 调起系统默认的图片查看应用（带有选择为默认）
//        if(BuildConfig.DEBUG){
//            Intent openImageIntent = new Intent(Intent.ACTION_VIEW);
//            openImageIntent.addCategory(Intent.CATEGORY_DEFAULT);
//            openImageIntent.setDataAndType(Uri.fromFile(new File(imageFilePath)), "image/*");
//            getDefaultActivity(openImageIntent);
//        }
    }

//    // 获取默认的打开方式
//    public void getDefaultActivity(Intent intent) {
//        PackageManager pm = this.getPackageManager();
//        ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
//        // 如果本应用没有询问过是否要选择默认打开方式，并且没有默认的打开方式，打开默认方式选择狂
//        if (!WithPref.i().hadAskImageOpenMode() || info.activityInfo.packageName.equals("android")) {
//            WithPref.i().setHadAskImageOpenMode(true);
//            intent.setComponent(new ComponentName("android", "com.android.internal.app.ResolverActivity"));
//        }
//        startActivity(intent);
//        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//        KLog.i("打开方式", "默认打开方式信息 = " + info + ";pkgName = " + info.activityInfo.packageName);
//    }

    // 打开选择默认打开方式的弹窗
//    public void startChooseDialog() {
//        Intent intent = new Intent();
//        intent.setAction("android.intent.action.VIEW");
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        intent.setData(Uri.fromFile(new File(imageFilePath)));
//        intent.setComponent(new ComponentName("android","com.android.internal.app.ResolverActivity"));
//        startActivity(intent);
//    }

    @JavascriptInterface
    @Override
    public void openLink(String link) {
        if (WithPref.i().isSysBrowserOpenLink()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            startActivity(Intent.createChooser(intent, "选择打开方式"));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else {
            Intent intent = new Intent(ArticleActivity.this, WebActivity.class);
            intent.setData(Uri.parse(link));
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    /**
     * 尝试注入JS，执行setupImage()方法，但是可能因为渲染html比较慢，导致setupImage没有真正执行。
     * 其实主要是在js文件加载中，直接调用tryInitJs，并且只初始化当前页面的setupImage
     *
     * @param articleId
     */
    @JavascriptInterface
    @Override
    public void tryInitJs(String articleId) {
    }


    @JavascriptInterface
    @Override
    public void readability() {
        articleHandler.post(new Runnable() {
            @Override
            public void run() {
                onReadabilityClick();
            }
        });

    }

    private void initView() {
        starView = findViewById(R.id.article_bottombar_star);
        readView = findViewById(R.id.article_bottombar_read);
        saveView = findViewById(R.id.article_bottombar_save);
        swipeRefreshLayoutS = findViewById(R.id.art_swipe_refresh);
        swipeRefreshLayoutS.setEnabled(false);
        articleNumView = findViewById(R.id.art_toolbar_num);
//        swipeRefreshLayoutS.setOnRefreshListener(new OnRefreshListener() {
//            @Override
//            public void onRefresh(RefreshLayout refreshlayout) {
//                refreshlayout.finishRefresh(2000/*,false*/);//传入false表示刷新失败
//            }
//        });
        if (BuildConfig.DEBUG) {
            saveView.setVisibility(View.VISIBLE);
            saveView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSaveClick(view);
                }
            });
            articleNumView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showArticleInfo(view);
                }
            });
        }
        entryView = findViewById(R.id.test_framelayout);
        entryView.setSlideListener(this);
        entryView.setActivity(this);
    }


    private void initToolbar() {
        toolbar = findViewById(R.id.art_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        slowlyProgressBar = new SlowlyProgressBar((ProgressBar) findViewById(R.id.article_progress_bar));
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (articleHandler.hasMessages(Api.MSG_DOUBLE_TAP) && selectedWebView != null) {
                    articleHandler.removeMessages(Api.MSG_DOUBLE_TAP);
                    selectedWebView.scrollTo(0, 0);
                } else {
                    articleHandler.sendEmptyMessageDelayed(Api.MSG_DOUBLE_TAP, ViewConfiguration.getDoubleTapTimeout());
                }
            }
        });
    }

    @Override
    public void slideLeftSuccess() {
        if (articleNo + 1 >= App.articleList.size()) {
            return;
        }
        saveArticleProgress();
        articleNo = articleNo + 1;
        initSelectedPage(articleNo);
    }

    private boolean canSlide = true;

    @Override
    public void slideRightSuccess() {
        if (articleNo - 1 < 0) {
            return;
        }
        saveArticleProgress();
        articleNo = articleNo - 1;
        initSelectedPage(articleNo);
    }

    @Override
    public boolean canSlideLeft() {
        return !(articleNo + 1 >= App.articleList.size() || !canSlide);
    }

    @Override
    public boolean canSlideRight() {
        return !(articleNo - 1 < 0 || !canSlide);
    }

    public void initSelectedPage(int position) {
        swipeRefreshLayoutS.setRefreshing(false);
        reInitSelectedArticle(position);
        initSelectedWebViewContent(position);
//        KLog.e("initSelectedPage结束。位置：" + position + "   " + selectedWebView);
    }


    public void reInitSelectedArticle(int position) {
        // 取消之前那篇文章的图片下载。但是如果回到之前那篇文章，怎么恢复下载呢？
        OkGo.cancelTag(articleHttpClient, articleId);
        if (App.articleList != null) {
            selectedArticle = App.articleList.get(position);
            articleId = selectedArticle.getId();
        } else {
            selectedArticle = WithDB.i().getArticle(articleId);
        }
//        articleId = articleIDs.get(position);
        articleNo = position;

        initIconState(position);
        initFeedConfig();
    }


    // （webview在实例化后，可能还在渲染html，不一定能执行js）
    private void initSelectedWebViewContent(final int position) {
        if (selectedWebView == null) {
            selectedWebView = new WebViewS(new MutableContextWrapper(App.i()));
//            selectedWebView = App.i().articleWebView;
            entryView.addView(selectedWebView, 0);
            // 初始化视频处理类
            video = new VideoImpl(ArticleActivity.this, selectedWebView);
            selectedWebView.setWebChromeClient(new WebChromeClientX(video));
            selectedWebView.setWebViewClient(new WebViewClientX());
            selectedWebView.addJavascriptInterface(ArticleActivity.this, "ImageBridge");

            // 原本想放在选择 webview 页面的时候去加载，但可能由于那时页面内容已经加载所以无法设置下面这个JSInterface？
            if (WithPref.i().getThemeMode() == App.Theme_Day) {
                selectedWebView.getFastScrollDelegate().setThumbDrawable(ContextCompat.getDrawable(App.i(), R.drawable.scrollbar_light));
            } else {
                selectedWebView.getFastScrollDelegate().setThumbDrawable(ContextCompat.getDrawable(App.i(), R.drawable.scrollbar_dark));
            }
            selectedWebView.getFastScrollDelegate().setThumbDynamicHeight(false);
            selectedWebView.getFastScrollDelegate().setThumbSize(10, 32);
        }

        // 检查该订阅源默认显示什么。【RSS，已读，保存的网页，原始网页】
        KLog.e("要加载的位置为：" + position + "  " + selectedArticle.getTitle());

        Feed feed = WithDB.i().getFeed(selectedArticle.getOriginStreamId());

        if (feed != null) {
            toolbar.setTitle(feed.getTitle());
            if (Api.DISPLAY_LINK.equals(GlobalConfig.i().getDisplayMode(feed.getId()))) {
                selectedWebView.loadUrl(selectedArticle.getCanonical());
            } else if (Api.DISPLAY_READABILITY.equals(GlobalConfig.i().getDisplayMode(feed.getId()))) {
                onReadabilityClick();
            } else {
                KLog.e("加载文章：" + selectedArticle.getTitle());
                selectedWebView.loadData(StringUtil.getPageForDisplay(selectedArticle));
            }
        } else {
            toolbar.setTitle(selectedArticle.getOriginTitle());
            selectedWebView.loadData(StringUtil.getPageForDisplay(selectedArticle));
        }
        selectedWebView.requestFocus();
    }


    private class WebChromeClientX extends WebChromeClient {
        VideoImpl video;

        WebChromeClientX(VideoImpl video) {
            this.video = video;
        }

        @Override
        public void onProgressChanged(WebView webView, int progress) {
            // 增加Javascript异常监控，不能增加，会造成页面卡死
            // CrashReport.setJavascriptMonitor(webView, true);
            if (slowlyProgressBar != null) {
                slowlyProgressBar.onProgressChange(progress);
            }
        }

        // 表示进入全屏的时候
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (video != null) {
                video.onShowCustomView(view, callback);
            }
        }

        //表示退出全屏的时候
        @Override
        public void onHideCustomView() {
            if (video != null) {
                video.onHideCustomView();
            }
        }
    }


    @JavascriptInterface
    @Override
    public void downImage(String articleId, final int index, final String originalUrl) {
        final String articleIdInMD5 = StringUtil.str2MD5(articleId);
//        final String filePath = App.externalFilesDir + "/cache/" + articleIdInMD5 + "/" + articleIdInMD5 + "_files/";
        final String originalFileDir = App.externalFilesDir + "/cache/" + articleIdInMD5 + "/original/";
        final String fileNameExt = index + "-" + StringUtil.getFileNameExtByUrl(originalUrl);

        if (new File(originalFileDir + fileNameExt + Api.EXT_TMP).exists()) {
            return;
        }
        final String compressedFileDir = App.externalFilesDir + "/cache/" + articleIdInMD5 + "/compressed/";
//        if( !new File(compressedFileDir).getParentFile().exists()){
//            new File(compressedFileDir).getParentFile().mkdirs();
//        }

        FileCallback fileCallback = new FileCallback(originalFileDir, fileNameExt + Api.EXT_TMP) {
            @Override
            public void onSuccess(Response<File> response) {
                new File(originalFileDir + fileNameExt + Api.EXT_TMP).renameTo(new File(originalFileDir + fileNameExt));
                Luban.with(App.i())
                        .load(originalFileDir + fileNameExt)
                        .ignoreBy(512) // 忽略512kb以下的文件
                        // 缓存压缩图片路径
//                        .setTargetPath(compressedFileDir + fileNameExt)
                        .setTargetDir(compressedFileDir)
                        // 设置开启压缩条件。当路径为空或者为gif时，不压缩
                        .setRenameListener(new OnRenameListener() {
                            @Override
                            public String rename(String filePath) {
                                return fileNameExt;
                            }
                        })
                        .filter(new CompressionPredicate() {
                            @Override
                            public boolean apply(String preCompressedPath) {
                                KLog.e("压缩图片，忽略压缩：" + preCompressedPath);
                                return !(TextUtils.isEmpty(preCompressedPath) || preCompressedPath.toLowerCase().endsWith(".gif"));
                            }
                        })
                        .setCompressListener(new OnCompressListener() {
                            // TODO 压缩开始前调用，可以在方法内启动 loading UI
                            @Override
                            public void onStart() {
                            }

                            @Override
                            public void onSuccess(final File file) {
                                KLog.e("加载压缩图片成功1：" + Thread.currentThread() + "   " + file.getPath() + "   " + compressedFileDir);
                                selectedWebView.loadUrl("javascript:onImageLoadSuccess('" + originalUrl + "','" + file.getPath() + "')");
                            }

                            @Override
                            public void onError(Throwable e) {
                                KLog.e("压缩图片报错");
                                selectedWebView.loadUrl("javascript:onImageLoadSuccess('" + originalUrl + "','" + originalFileDir + fileNameExt + "')");
                            }
                        }).launch();

//                selectedWebView.loadUrl("javascript:onImageLoadSuccess('" + originalUrl + "','" + filePath + fileNameExt + "')");
                KLog.e("下载图片成功，准备加载" + originalUrl + "','" + originalFileDir + fileNameExt);
            }

            // 该方法执行在主线程中
            @Override
            public void onError(Response<File> response) {
                if (selectedWebView != null) {
                    selectedWebView.loadUrl("javascript:onImageLoadFailed('" + originalUrl + "')");
                }
                new File(originalFileDir + fileNameExt + Api.EXT_TMP).delete();
            }
        };
        Request request = OkGo.<File>get(originalUrl)
                .tag(articleId)
                .client(articleHttpClient);

        String referer = GlobalConfig.i().guessRefererByUrl(originalUrl);
//        KLog.e("图片链接是：" + originalUrl + "， 来源是：" + referer);
        if (!TextUtils.isEmpty(referer)) {
            request.headers(Api.Referer, referer);
        }

        request.execute(fileCallback);
//        KLog.e("下载：" + originalUrl + " 来源 " + selectedArticle.getCanonical() );
    }

    private class WebViewClientX extends WebViewClient {
        @Deprecated
        @SuppressLint("NewApi")
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (GlobalConfig.i().isBlockAD() && AdBlock.i().isAd(url)) {
                // 有广告的请求数据，我们直接返回空数据，注：不能直接返回null
                return new WebResourceResponse(null, null, null);
            }
            return super.shouldInterceptRequest(view, url);
        }

        /**
         * @param webView
         * @param url
         * @return 返回 true 表示你已经处理此次请求。
         * 返回 false 表示由webview自行处理（一般都是把此url加载出来）。
         * 返回super.shouldOverrideUrlLoading(view, url); 这个返回的方法会调用父类方法，也就是跳转至手机浏览器
         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            // 判断重定向的方式一
            // 作者：胡几手，链接：https://www.jianshu.com/p/7dfb8797f893
            // 解决在webView第一次加载的url重定向到了另一个地址时，也会走shouldOverrideUrlLoading回调的问题
            WebView.HitTestResult hitTestResult = webView.getHitTestResult();
            if (hitTestResult == null) {
                return false;
            } else if (hitTestResult.getType() == WebView.HitTestResult.UNKNOWN_TYPE) {
                return false;
            }
//            KLog.e("打开的链接为：" + url );
//            if (url.startsWith("//")) {
//                url = "http:" + url;
//            }
            //http和https协议开头的执行正常的流程
            if (url.startsWith("http") || url.startsWith("https")) {
                openLink(url);
                OkGo.cancelAll(articleHttpClient);
                return true;
            }

            /**
             * 【scheme链接打开本地应用】（https://www.jianshu.com/p/45af72036e58）
             */
            //其他的URL则会开启一个Acitity然后去调用原生APP
            final Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (in.resolveActivity(getPackageManager()) == null) {
                // TODO: 2018/4/25  说明系统中不存在这个activity。弹出一个Toast提示是否要用外部应用打开
                return true;
            }
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            String name = "相应的";
            try {
                name = "" + getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(in.resolveActivity(getPackageManager()).getPackageName(), PackageManager.GET_META_DATA));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            new MaterialDialog.Builder(ArticleActivity.this)
                    .content("是否跳转到「" + name + "」应用？")
                    .negativeText("取消")
                    .positiveText(R.string.agree)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            startActivity(in);
                            overridePendingTransition(R.anim.fade_in, R.anim.out_from_bottom);
                        }
                    })
                    .show();


            return true;
        }

        @Override
        public void onPageStarted(WebView webView, String url, Bitmap favicon) {
            super.onPageStarted(webView, url, favicon);
            canSlide = false;
            if (slowlyProgressBar != null) {
                slowlyProgressBar.onProgressStart();
            }
            Message m = Message.obtain();
            // 超时
            m.what = 0;
            // 5秒后如果没有加载完毕，则停止加载
            articleHandler.sendMessageDelayed(m, 5000);
        }


        /**
         * 不能直接在这里就初始化setupImage，因为在viewpager中预加载而生成webview的时候，这里的懒加载就被触发了
         * webView.loadUrl("javascript:setTimeout(\"setupImage()\",100)");
         */
        @Override
        public void onPageFinished(WebView webView, String url) {
            super.onPageFinished(webView, url);
//            webView.getSettings().setBlockNetworkImage(false);
            Integer process = App.i().articleProgress.get(articleId);
            KLog.e("页面加载完成：" + selectedArticle.getTitle() + "  " + articleId + "  " + process);
            if (process != null) {
                selectedWebView.scrollTo(0, process);
            }
            canSlide = true;
        }
    }


    private void initIconState(int position) {
        if (selectedArticle.getReadStatus() == Api.UNREAD) {
            readView.setText(getString(R.string.font_readed));
            DataApi.i().markArticleReaded(articleHttpClient, selectedArticle.getId(), null);
            // 方法2
            WithDB.i().setReaded(selectedArticle);
        } else if (selectedArticle.getReadStatus() == Api.READED) {
            readView.setText(getString(R.string.font_readed));
        } else if (selectedArticle.getReadStatus() == Api.UNREADING) {
            readView.setText(getString(R.string.font_unread));
        }

        if (selectedArticle.getStarStatus() == Api.UNSTAR) {
            starView.setText(getString(R.string.font_unstar));
        } else {
            starView.setText(getString(R.string.font_stared));
        }
        if (selectedArticle.getSaveDir().equals(Api.SAVE_DIR_CACHE)) {
            saveView.setText(getString(R.string.font_unsave));
        } else {
            saveView.setText(getString(R.string.font_saved));
        }

        articleNumView.setText((position + 1) + " / " + articleCount);
//        KLog.i("=====position" + position);
    }


    public void onReadClick(View view) {
//        KLog.e("loread", "被点击的是：" + selectedArticle.getTitle());
        if (selectedArticle.getReadStatus() == Api.READED) {
            readView.setText(getString(R.string.font_unread));
            DataApi.i().markArticleUnread(selectedArticle.getId(), null);
            WithDB.i().setUnreading(selectedArticle);
        } else {
            readView.setText(getString(R.string.font_readed));
            DataApi.i().markArticleReaded(selectedArticle.getId(), null);
            WithDB.i().setReaded(selectedArticle);
        }
    }


    public void onStarClick(View view) {
        if (selectedArticle.getStarStatus() == Api.UNSTAR) {
            starView.setText(getString(R.string.font_stared));
            DataApi.i().markArticleStared(selectedArticle.getId(), null);
            selectedArticle.setStarStatus(Api.STARED);
            selectedArticle.setStarred(System.currentTimeMillis() / 1000);
            if (selectedArticle.getSaveDir().equals(Api.SAVE_DIR_BOX)) {
                selectedArticle.setSaveDir(Api.SAVE_DIR_STORE);
            }
        } else {
            starView.setText(getString(R.string.font_unstar));
            DataApi.i().markArticleUnstar(selectedArticle.getId(), null);
            selectedArticle.setStarStatus(Api.UNSTAR);
            if (selectedArticle.getSaveDir().equals(Api.SAVE_DIR_STORE)) {
                selectedArticle.setSaveDir(Api.SAVE_DIR_BOX);
            }
        }
        WithDB.i().saveArticle(selectedArticle);
    }


    public void onSaveClick(View view) {
//        KLog.e("loread", "保存文件被点击");
        if (selectedArticle.getSaveDir().equals(Api.SAVE_DIR_CACHE)) {
            if (selectedArticle.getStarStatus() == Api.STARED) {
                selectedArticle.setSaveDir(Api.SAVE_DIR_STORE);
            } else {
                selectedArticle.setSaveDir(Api.SAVE_DIR_BOX);
            }
            saveView.setText(getString(R.string.font_saved));
        } else {
            selectedArticle.setSaveDir(Api.SAVE_DIR_CACHE);
            saveView.setText(getString(R.string.font_unsave));
        }
        WithDB.i().saveArticle(selectedArticle);
    }


    public void clickReadability(View view) {
        onReadabilityClick();
    }

    public void onReadabilityClick() {
        if (selectedWebView.isReadability()) {
            ToastUtil.showLong(getString(R.string.toast_cancel_readability));
            selectedWebView.loadData(StringUtil.getPageForDisplay(selectedArticle));
            selectedWebView.setReadability(false);
            return;
        }

        swipeRefreshLayoutS.setRefreshing(true);
        ToastUtil.showLong(getString(R.string.toast_get_readability_ing));
        final Handler handler = new Handler(Looper.getMainLooper());
        OkGo.get(selectedArticle.getCanonical()).client(articleHttpClient).getRawCall().enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayoutS.setRefreshing(false);
                        ToastUtil.showLong(getString(R.string.toast_get_readability_failure));
                    }
                });
            }

            // 在Android应用中直接使用上述代码进行异步请求，并且在回调方法中操作了UI，那么你的程序就会抛出异常，并且告诉你不能在非UI线程中操作UI。
            // 这是因为OkHttp对于异步的处理仅仅是开启了一个线程，并且在线程中处理响应。
            // OkHttp是一个面向于Java应用而不是特定平台(Android)的框架，那么它就无法在其中使用Android独有的Handler机制。
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    Document doc = Jsoup.parse(response.body().byteStream(), DataUtil.getCharsetFromContentType(response.body().contentType().toString()), selectedArticle.getCanonical());
                    final String content = Extractor.getContent(selectedArticle.getCanonical(), doc);

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            selectedWebView.loadData(StringUtil.getPageForDisplay(selectedArticle, content, Api.DISPLAY_READABILITY));
                            KLog.e("获取到的内容：" + content);
                            ToastUtil.showLong(getString(R.string.toast_get_readability_success));
                            selectedWebView.setReadability(true);

                            if (BuildConfig.DEBUG) {
                                SnackbarUtil.Long(swipeRefreshLayoutS, "保存 Readability 内容？")
                                        .setAction("同意", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                selectedArticle.setContent(content);
                                                WithDB.i().updateArticle(selectedArticle);
                                            }
                                        }).show();
                            }
                        }
                    });
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayoutS.setRefreshing(false);
                    }
                });
            }
        });
    }

    private String selectedFeedDisplayMode = Api.DISPLAY_RSS;
    private EditText feedNameEdit;

    private void initFeedConfig() {
        final Feed feed = WithDB.i().getFeed(selectedArticle.getOriginStreamId());
        final View feedConfigView = findViewById(R.id.article_feed_config);
        if (feed != null) {
            feedConfigView.setVisibility(View.VISIBLE);
            feedConfigView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showConfigFeedDialog(feed, feedConfigView);
                }
            });
        } else {
            feedConfigView.setVisibility(View.GONE);
        }
    }

    public void showConfigFeedDialog(final Feed feed, final View feedConfigView) {
        if (feed == null) {
            return;
        }

        final MaterialDialog feedConfigDialog = new MaterialDialog.Builder(this)
                .title("配置该源")
                .customView(R.layout.config_feed_view, true)
                .positiveText("确认")
                .negativeText("取消")
                .neutralText("退订")
                .neutralColor(Color.RED)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        DataApi.i().unsubscribeFeed(articleHttpClient, feed.getId(), new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {
                                if (!response.body().equals("OK")) {
                                    this.onError(response);
                                    return;
                                }
                                WithDB.i().unsubscribeFeed(feed);
                                ToastUtil.showLong("退订成功");
                                feedConfigView.setVisibility(View.GONE);
                                // 返回 mainActivity 页面，并且跳到下一个 tag/feed
                                // KLog.e("移除" + itemView.groupPos + "  " + itemView.childPos );
                            }

                            @Override
                            public void onError(Response<String> response) {
                                ToastUtil.showLong(App.i().getString(R.string.toast_unsubscribe_fail));
                            }
                        });

                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        KLog.e("显示模式：" + selectedFeedDisplayMode);
                        Feed feedx = feed;
                        renameFeed(feedNameEdit.getText().toString(), feedx);
                        feedx.update();

                        if (!selectedFeedDisplayMode.equals(Api.DISPLAY_RSS)) {
                            GlobalConfig.i().addDisplayRouter(feed.getId(), selectedFeedDisplayMode);
                        } else {
                            GlobalConfig.i().removeDisplayRouter(feed.getId());
                        }
                        GlobalConfig.i().save();

                        dialog.dismiss();
                    }
                }).build();

        feedConfigDialog.show();
        feedNameEdit = (EditText) feedConfigDialog.findViewById(R.id.feed_name_edit);
        feedNameEdit.setText(feed.getTitle());

        TextView feedLink = (TextView) feedConfigDialog.findViewById(R.id.feed_link);
        feedLink.setText(feed.getUrl());
        feedLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取剪贴板管理器：
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 创建普通字符型ClipData
                ClipData mClipData = ClipData.newPlainText("RSS Link", feed.getUrl());
                // 将ClipData内容放到系统剪贴板里。
                cm.setPrimaryClip(mClipData);
                ToastUtil.showShort("复制成功！");
            }
        });


        TextView feedOpenModeSelect = (TextView) feedConfigDialog.findViewById(R.id.feed_open_mode_select);
        if (TextUtils.isEmpty(GlobalConfig.i().getDisplayMode(feed.getId()))) {
            selectedFeedDisplayMode = Api.DISPLAY_RSS;
        } else {
            selectedFeedDisplayMode = GlobalConfig.i().getDisplayMode(feed.getId());
        }
        feedOpenModeSelect.setText(selectedFeedDisplayMode);
        feedOpenModeSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDisplayModePopupMenu(view);
            }
        });

        TextView feedCategoryView = (TextView) feedConfigDialog.findViewById(R.id.feed_tag_category);
        if (!TextUtils.isEmpty(feed.getCategoryid())) {
            feedCategoryView.setText(feed.getCategorylabel());
        } else {
            feedCategoryView.setText(getString(R.string.main_activity_title_untag));
        }

        feedCategoryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                feedConfigDialog.dismiss();
                final ArrayList<Tag> tags = WithDB.i().getTagsWithCount();
                final ArrayList<String> titles = new ArrayList<>(tags.size());
                int selectedIndex = -1;

                for (int i = 0, size = tags.size(); i < size; i++) {
                    titles.add(tags.get(i).getTitle());
                    if (tags.get(i).getId().equals(feed.getCategoryid())) {
                        selectedIndex = i;
                    }
                }

                final Integer[] preSelectedIndices;
                if (selectedIndex != -1) {
                    preSelectedIndices = new Integer[]{selectedIndex};
                } else {
                    preSelectedIndices = null;
                }

                new MaterialDialog.Builder(ArticleActivity.this)
                        .title("修改分组")
                        .items(titles)
                        .itemsCallbackMultiChoice(preSelectedIndices, new MaterialDialog.ListCallbackMultiChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, final Integer[] which, CharSequence[] text) {
                                FormBody.Builder builder = new FormBody.Builder();
                                builder.add("ac", "edit");
                                builder.add("s", feed.getId());
                                if (which.length == 0) {
                                    builder.add("r", feed.getCategoryid());
                                } else {
                                    builder.add("a", tags.get(which[0]).getId());
                                }
                                DataApi.i().editFeed(builder, new StringCallback() {
                                    @Override
                                    public void onSuccess(Response<String> response) {
                                        if (!response.body().equals("OK")) {
                                            this.onError(response);
                                            return;
                                        }

                                        if (which != null && which.length == 0) {
                                            feed.setCategoryid("");
                                            feed.setCategorylabel("");
                                        } else {
                                            feed.setCategoryid(tags.get(which[0]).getId());
                                            feed.setCategorylabel(tags.get(which[0]).getTitle());
                                        }
                                        feed.update();
                                        ToastUtil.showLong("修改分组成功！");
                                    }

                                    @Override
                                    public void onError(Response<String> response) {
                                        ToastUtil.showLong(App.i().getString(R.string.toast_rename_fail));
                                    }
                                });
                                return which.length <= 1;
                            }
                        })
                        .alwaysCallMultiChoiceCallback() // the callback will always be called, to check if selection is still allowed
                        .show();
            }
        });
    }


    public void renameFeed(final String renamedTitle, final Feed feedx) {
        KLog.e("=====" + renamedTitle + feedx.getId());
        if (renamedTitle.equals("") || feedx.getTitle().equals(renamedTitle)) {
            return;
        }
        DataApi.i().renameFeed(articleHttpClient, feedx.getId(), renamedTitle, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if (!response.body().equals("OK")) {
                    this.onError(response);
                    return;
                }
                Feed feed = feedx;
                feed.setTitle(renamedTitle);
                feed.update();
                // 由于改了 feed 的名字，而每个 article 自带的 feed 名字也得改过来。
                WithDB.i().updateArtsFeedTitle(feed);
//                KLog.e("改了名字" + renamedTitle );
            }

            @Override
            public void onError(Response<String> response) {
                ToastUtil.showLong(App.i().getString(R.string.toast_rename_fail));
            }
        });
    }

    public void showDisplayModePopupMenu(final View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.display_mode_rss:
                        selectedFeedDisplayMode = Api.DISPLAY_RSS;
                        break;
//                    case R.id.display_mode_readability:
//                        selectedFeedDisplayMode = Api.DISPLAY_READABILITY;
//                        break;
                    case R.id.display_mode_link:
                        selectedFeedDisplayMode = Api.DISPLAY_LINK;
                        break;
                    default:
                        selectedFeedDisplayMode = Api.DISPLAY_RSS;
                        break;
                }
                KLog.e("选择：" + selectedFeedDisplayMode);
                ((TextView) view).setText(menuItem.getTitle());
                return false;
            }
        });
        // 加载布局文件到菜单中去
        menuInflater.inflate(R.menu.menu_article_display_mode, popupMenu.getMenu());
        popupMenu.show();
    }


//    public void editFeed(final Feed newFeed){
//        final Feed localFeed = WithDB.i().getFeed(newFeed.getId());
//        if( localFeed != null){
//            return;
//        }
//
//        FormBody.Builder builder = new FormBody.Builder();
//        builder.add("ac", "edit"); // 可省略
//        builder.add("s", newFeed.getId());
//
//        if( !localFeed.getCategoryid().equals(newFeed.getCategoryid()) ){
//            builder.add("r", localFeed.getCategoryid());
//            builder.add("a", newFeed.getCategoryid());
//        }
//
//        if( !localFeed.getTitle().equals(newFeed.getTitle()) ){
//            builder.add("t", newFeed.getTitle());
//        }
//
//        DataApi.i().editFeed(articleHttpClient,builder,, new StringCallback() {
//            @Override
//            public void onSuccess(Response<String> response) {
//                if (!response.body().equals("OK")) {
//                    this.onError(response);
//                    return;
//                }
//                newFeed.update();
//                // 由于改了 feed 的名字，而每个 article 自带的 feed 名字也得改过来。
//                if( !localFeed.getTitle().equals(newFeed.getTitle()) ){
//                    WithDB.i().updateArtsFeedTitle(newFeed);
//                }
////                KLog.e("改了名字" + renamedTitle );
//            }
//
//            @Override
//            public void onError(Response<String> response) {
//                ToastUtil.showLong(App.i().getString(R.string.toast_rename_fail));
//            }
//        });
//    }


    public void showArticleInfo(View view) {
//        KLog.e("文章信息");
        if (!BuildConfig.DEBUG) {
            return;
        }
        String info = selectedArticle.getTitle() + "\n" +
                "ID=" + selectedArticle.getId() + "\n" +
                "ID-MD5=" + StringUtil.str2MD5(selectedArticle.getId()) + "\n" +
                "ReadState=" + selectedArticle.getReadState() + "\n" +
                "StarState=" + selectedArticle.getStarState() + "\n" +
                "SaveDir=" + selectedArticle.getSaveDir() + "\n" +
                "Author=" + selectedArticle.getAuthor() + "\n" +
                "Published=" + selectedArticle.getPublished() + "\n" +
                "Starred=" + selectedArticle.getStarred() + "\n" +
                "Categories=" + selectedArticle.getCategories() + "\n" +
                "CoverSrc=" + selectedArticle.getCoverSrc() + "\n" +
                "OriginHtmlUrl=" + selectedArticle.getOriginHtmlUrl() + "\n" +
                "OriginStreamId=" + selectedArticle.getOriginStreamId() + "\n" +
                "OriginTitle=" + selectedArticle.getOriginTitle() + "\n" +
                "Canonical=" + selectedArticle.getCanonical() + "\n" +
//                "Summary=" + selectedArticle.getSummary() + "\n" +
                "Content=" + selectedArticle.getContent() + "\n";

        new MaterialDialog.Builder(this)
                .title(R.string.article_about_dialog_title)
                .content(info)
//                .positiveText(R.string.agree)
//                .onPositive(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        //获取剪贴板管理器：
//                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                        // 创建普通字符型ClipData
//                        ClipData mClipData = ClipData.newPlainText("FeedId", selectedArticle.getOriginStreamId());
//                        // 将ClipData内容放到系统剪贴板里。
//                        cm.setPrimaryClip(mClipData);
//                        ToastUtil.show("已复制订阅源的 RSS 地址");
//                    }
//                })
                .positiveColorRes(R.color.material_red_400)
                .titleGravity(GravityEnum.CENTER)
                .titleColorRes(R.color.material_red_400)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey_800)
                .dividerColorRes(R.color.material_teal_a400)
                .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.POSITIVE)
                .positiveColor(Color.WHITE)
                .negativeColorAttr(android.R.attr.textColorSecondaryInverse)
                .theme(Theme.DARK)
                .show();
    }

    /**
     * 不能使用 onBackPressed，会导致overridePendingTransition转场动画失效
     * event.getRepeatCount() 后者为短期内重复按下的次数
     *
     * @return 返回真表示返回键被屏蔽掉
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (video != null && video.isPlaying()) {
                video.onHideCustomView();
                return true;
            }
            Intent data = new Intent();
            data.putExtra("articleNo", articleNo);
            //注意下面的RESULT_OK常量要与回传接收的Activity中onActivityResult()方法一致
            this.setResult(Api.ActivityResult_ArtToMain, data);
            this.finish();
            overridePendingTransition(R.anim.fade_in, R.anim.out_from_bottom);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        mColorfulBuilder
                .backgroundColor(R.id.art_root, R.attr.root_view_bg)
                // 设置 toolbar
                .backgroundColor(R.id.art_toolbar, R.attr.topbar_bg)
                .textColor(R.id.art_toolbar_num, R.attr.topbar_fg)
                // 设置中屏和底栏之间的分割线
                .backgroundColor(R.id.article_bottombar_divider, R.attr.bottombar_divider)
                // 设置 bottombar
                .backgroundColor(R.id.article_bottombar, R.attr.bottombar_bg)
                .textColor(R.id.article_bottombar_read, R.attr.bottombar_fg)
                .textColor(R.id.article_bottombar_star, R.attr.bottombar_fg)
                .textColor(R.id.article_feed_config, R.attr.bottombar_fg)
//                .textColor(R.id.article_bottombar_tag, R.attr.bottombar_fg)
                .textColor(R.id.article_bottombar_save, R.attr.bottombar_fg);
        return mColorfulBuilder;
    }


    private OkHttpClient articleHttpClient = new OkHttpClient.Builder()
            .readTimeout(60000L, TimeUnit.MILLISECONDS)
            .writeTimeout(60000L, TimeUnit.MILLISECONDS)
            .connectTimeout(30000L, TimeUnit.MILLISECONDS)
            .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
            .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier).build();


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //监听左上角的返回箭头
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.out_from_bottom);
            return true;
        }
//        switch (item.getItemId()) {
//            case R.id.config_feed:
//                openFeed();
//                break;
//        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_article_activity, menu);
//        return true;
//    }


    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }
}
