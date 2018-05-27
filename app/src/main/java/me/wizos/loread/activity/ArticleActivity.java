package me.wizos.loread.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.MutableContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.gson.Gson;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.DefaultWebClient;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.socks.library.KLog;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.BuildConfig;
import me.wizos.loread.R;
import me.wizos.loread.bean.config.FeedConfig;
import me.wizos.loread.bean.gson.Readability;
import me.wizos.loread.common.ImageBridge;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithPref;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.Tag;
import me.wizos.loread.net.Api;
import me.wizos.loread.net.DataApi;
import me.wizos.loread.net.MercuryApi;
import me.wizos.loread.utils.HttpUtil;
import me.wizos.loread.utils.ScreenUtil;
import me.wizos.loread.utils.SnackbarUtil;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.utils.ToastUtil;
import me.wizos.loread.utils.Tool;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.SlowlyProgressBar;
import me.wizos.loread.view.ViewPagerS;
import me.wizos.loread.view.WebViewS;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.webview.IVideo;
import me.wizos.loread.view.webview.VideoImpl;

/**
 * @author Wizos on 2017
 */
@SuppressLint("SetJavaScriptEnabled")
public class ArticleActivity extends BaseActivity implements View.OnClickListener, ImageBridge {
    protected static final String TAG = "ArticleActivity";

    //    private SwipeRefreshLayoutS swipeRefreshLayoutS;
    private WebViewS webView;
    private SlowlyProgressBar slowlyProgressBar;
    private IconFontView vStar, vRead, vSave;
    private TextView vArticleNum;
    private Article selectedArticle;
    private ViewPagerS viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private int articleNo, articleCount;
    /**
     * Video 视频播放类
     */
    private IVideo mIVideo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        App.artHandler = articleHandler;

        if (savedInstanceState != null) {
            // setSelection 没有滚动效果，直接跳到指定位置。smoothScrollToPosition 有滚动效果的
            articleNo = savedInstanceState.getInt("articleNo");
            articleCount = savedInstanceState.getInt("articleCount");
        } else {
            // 文章在列表中的位置编号，下标从 0 开始
            articleNo = getIntent().getExtras().getInt("articleNo");
            // 列表中所有的文章数目
            articleCount = getIntent().getExtras().getInt("articleCount");
        }
        KLog.i("开始初始化数据" + articleNo + "==" + articleCount);
        initToolbar();
        initView(); // 初始化界面上的 View，将变量映射到布局上。
        initViewPager();
    }

//    @Override
//    public void onResume(){
//        if(webView!=null){
//            webView.onResume();
//        }
//        super.onResume();
//    }

//    @Override
//    public void onPause(){
//        if(webView!=null){
//            webView.onPause();
//        }
//        super.onPause();
//    }


//    private void testFragment( ViewGroup container){
//        TagFragment tagFragment = new TagFragment();
//        getFragmentManager().beginTransaction().add(R.id.container_framelayout, tagFragment).commit();
//    }

    private void initView() {
        vStar = findViewById(R.id.article_bottombar_star);
        vRead = findViewById(R.id.article_bottombar_read);
        vSave = findViewById(R.id.art_bottombar_save);
        vArticleNum = findViewById(R.id.art_toolbar_num);
//        swipeRefreshLayoutS = findViewById(R.id.art_swipe_refresh);
//        swipeRefreshLayoutS.setEnabled(false);
        if (!BuildConfig.DEBUG) {
            vSave.setVisibility(View.GONE);
        }
        vSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClick(v);
            }
        });
    }


    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.art_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setOnClickListener(this);
//        白色箭头
//        Drawable upArrow = getResources().getDrawable(R.drawable.mz_ic_sb_back);
//        upArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
//        getSupportActionBar().setHomeAsUpIndicator(upArrow); // 替换返回箭头
        slowlyProgressBar = new SlowlyProgressBar((ProgressBar) findViewById(R.id.article_progress_bar));
    }


    public void initViewPager() {
        viewPager = findViewById(R.id.art_viewpager);
        viewPager.clearOnPageChangeListeners();
//        viewPager.setVisibility(View.GONE);
//        Tool.setBackgroundColor(viewPager);
        viewPagerAdapter = new ViewPagerAdapter(this, App.articleList);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new PageChangeListener());
        // 本句放到 ViewPagerAdapter 的构造器中是无效的。
        viewPager.setCurrentItem(articleNo, false);
        // 当 setCurrentItem (0) 的时候，不会调用 onPageSelected 函数，导致无法触发 initSelectedPage 函数，所以这里要手动触发。
        if (articleNo == 0) {
            ArticleActivity.this.initSelectedPage(0);
        }
    }


    @Override
    protected void onDestroy() {
        // 如果参数为null的话，会将所有的Callbacks和Messages全部清除掉。
        // 这样做的好处是在 Acticity 退出的时候，可以避免内存泄露。因为 handler 内可能引用 Activity ，导致 Activity 退出后，内存泄漏
        KLog.e("loread", "onDestroy" + webView);
        articleHandler.removeCallbacksAndMessages(null);
        webView = null;
        WebViewS webViewS;
        for (int i = 0; i < viewPager.getChildCount(); i++) {
            // 使用自己包装的 webview
            webViewS = (WebViewS) viewPager.getChildAt(i);
//            webViewS.clear();
//            App.i().mWebViewCaches.add(webViewS);
            webViewS.destroy();
            KLog.e("创建新的webview");
            App.i().mWebViewCaches.add(new WebViewS(new MutableContextWrapper(this)));
        }
        if (agentWeb != null) {
            agentWeb.destroy();
        }
        viewPager.removeAllViews();
        viewPager.clearOnPageChangeListeners();
        OkGo.cancelAll(App.imgHttpClient);
        super.onDestroy();
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
        if (!HttpUtil.isNetworkAvailable()) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:onImageLoadFailed('" + originalUrl + "')");
                }
            });
        } else if (WithPref.i().isDownImgWifi() && !HttpUtil.isWiFiUsed()) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:onImageLoadNeedClick('" + originalUrl + "')");
                }
            });
        } else {
            downImage(articleId, index, originalUrl);
        }
    }

    @JavascriptInterface
    @Override
    public void openImage(String articleId, String urls, int index) {
        KLog.e("ImageBridge", "打开图片" + index);
    }


    @JavascriptInterface
    @Override
    public void downImage(String articleId, int index, final String originalUrl) {
//        final String filePath = App.externalFilesDir + "cache/" + StringUtil.str2MD5(articleId) + "_files/";
//        final String fileNameExt = index + "-" + StringUtil.getFileNameExtByUrl(originalUrl);
        String idInMD5 = StringUtil.str2MD5(articleId);
        final String filePath = App.externalFilesDir + "/cache/" + idInMD5 + "/" + idInMD5 + "_files/";
        final String fileNameExt = index + "-" + StringUtil.getFileNameExtByUrl(originalUrl);
        if (new File(filePath + fileNameExt + Api.EXT_TMP).exists()) {
            return;
        }
//        KLog.e("开始下载图片：" + articleId + " - " + originalUrl + "   " + System.currentTimeMillis() );

        FileCallback fileCallback = new FileCallback(filePath, fileNameExt + Api.EXT_TMP) {
            @Override
            public void onSuccess(Response<File> response) {
                new File(filePath + fileNameExt + Api.EXT_TMP).renameTo(new File(filePath + fileNameExt));
//                KLog.e("下载成功：" + originalUrl + " - " + viewPager.findViewById(selectedPos) + " - " + filePath + fileNameExt);
                if (selectedPos != -1 && viewPager.findViewById(selectedPos) != null) {
                    ((WebViewS) viewPager.findViewById(selectedPos)).loadUrl("javascript:onImageLoadSuccess('" + originalUrl + "','" + filePath + fileNameExt + "')");
                }
            }

            // 该方法执行在主线程中
            @Override
            public void onError(Response<File> response) {
//                KLog.e("下载失败：" + originalUrl + " - " + viewPager.findViewById(selectedPos) + " - " + filePath + fileNameExt);
                if (selectedPos != -1 && viewPager.findViewById(selectedPos) != null) {
                    ((WebViewS) viewPager.findViewById(selectedPos)).loadUrl("javascript:onImageLoadFailed('" + originalUrl + "')");
                }
                new File(filePath + fileNameExt + Api.EXT_TMP).delete();
            }
        };
        Request request = OkGo.<File>get(originalUrl)
                .tag(articleId)
                .client(App.imgHttpClient);
        Feed feed = WithDB.i().getFeed(selectedArticle.getOriginStreamId());
        if (feed != null && null != feed.getConfig()) {
            FeedConfig feedConfig = feed.getConfig();
            String referer = feedConfig.getReferer();
            if (TextUtils.isEmpty(referer)) {
            } else if (Api.Auto.equals(referer)) {
                request.headers(Api.Referer, selectedArticle.getCanonical());
            } else {
                request.headers(Api.Referer, referer);
            }
            String userAgent = feedConfig.getUserAgent();
            if (!TextUtils.isEmpty(userAgent)) {
                request.headers(Api.UserAgent, userAgent);
            }
        }
        request.execute(fileCallback);
//        KLog.e("下载：" + originalUrl + " 来源 " + selectedArticle.getCanonical() );
    }

    @JavascriptInterface
    @Override
    public void tryInitJs(String articleId) {
//        KLog.e("尝试初始化" +  viewPager.getCurrentItem() + "  "+  selectedArticle.getId() + "  " + articleId );
        if (!selectedArticle.getId().equals(articleId)) {
            return;
        }
        mWebViewLoadedJs.put(viewPager.getCurrentItem(), false);
        articleHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:setupImage('" + selectedArticle.getId() + "')");
                mWebViewLoadedJs.put(viewPager.getCurrentItem(), true);
            }
        }, 300);
    }

    @JavascriptInterface
    @Override
    public void openLink(String link) {
        Intent intent = new Intent(ArticleActivity.this, WebActivity.class);
//        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.putExtra("link", link);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_bottom, R.anim.exit_anim);
    }


    public ArticleHandler articleHandler = new ArticleHandler(this);

    // 【笔记】
    // 问题：内部类 Handler 持有外部类的对象，容易导致 Activity 无法回收，造成内存泄漏。
    // 解决方法：静态类不持有外部类的对象，所以 Activity 可以随意被回收，不容易造成内存泄漏
    private static class ArticleHandler extends Handler {
        private final WeakReference<ArticleActivity> mActivity;

        ArticleHandler(ArticleActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(final Message msg) {
            KLog.e("【handler】" + msg.what + mActivity.get());
            // 返回引用对象的引用
            if (mActivity.get() == null) {
                return;
            }
            switch (msg.what) {
                case Api.INIT_IMAGE_BRIDGE:
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
//        KLog.e("loread", "【 toolbar 是否双击 】" + v);
        switch (v.getId()) {
            case R.id.art_toolbar_num:
            case R.id.art_toolbar:
                break;
            default:
                break;
        }
    }

//    public void onClickFeedMeta(View view){
//    }


    public void debugArticleInfo(View view) {
        KLog.e("文章信息");
        showArticleInfo(selectedArticle);
    }

    public void showArticleInfo(final Article article) {
        if (!BuildConfig.DEBUG) {
            return;
        }
//        article = WithDB.i().getArticle(article.getId());

        String info = article.getTitle() + "\n" +
                "ID=" + article.getId() + "\n" +
                "ReadState=" + article.getReadState() + "\n" +
                "StarState=" + article.getStarState() + "\n" +
                "SaveDir=" + article.getSaveDir() + "\n" +
                "Author=" + article.getAuthor() + "\n" +
                "Published=" + article.getPublished() + "\n" +
                "Starred=" + article.getStarred() + "\n" +
                "Categories=" + article.getCategories() + "\n" +
                "CoverSrc=" + article.getCoverSrc() + "\n" +
                "OriginHtmlUrl=" + article.getOriginHtmlUrl() + "\n" +
                "OriginStreamId=" + article.getOriginStreamId() + "\n" +
                "OriginTitle=" + article.getOriginTitle() + "\n" +
                "Canonical=" + article.getCanonical() + "\n" +
                "Summary=" + article.getSummary() + "\n" +
                "Content=" + article.getContent() + "\n";

        new MaterialDialog.Builder(this)
                .title(R.string.article_about_dialog_title)
                .content(info)
                .positiveText(R.string.agree)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //获取剪贴板管理器：
                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        // 创建普通字符型ClipData
                        ClipData mClipData = ClipData.newPlainText("FeedId", article.getOriginStreamId());
                        // 将ClipData内容放到系统剪贴板里。
                        cm.setPrimaryClip(mClipData);
                        ToastUtil.showShort("已复制订阅源的 RSS 地址");
                    }
                })
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


    public void onStarClick(View view) {
//        articleStateManager.changeStarState(selectedArticle);
        if (selectedArticle.getStarState().equals(Api.ART_UNSTAR)) {
            vStar.setText(getString(R.string.font_stared));
            DataApi.i().markArticleStared(selectedArticle.getId(), null);
            selectedArticle.setStarState(Api.ART_STARED);
            selectedArticle.setStarred(System.currentTimeMillis() / 1000);
            if (selectedArticle.getSaveDir().equals(Api.SAVE_DIR_BOX) || selectedArticle.getSaveDir().equals(Api.SAVE_DIR_BOXREAD)) {
                selectedArticle.setSaveDir(Api.SAVE_DIR_STORE);
            }
        } else {
            vStar.setText(getString(R.string.font_unstar));
            DataApi.i().markArticleUnstar(selectedArticle.getId(), null);
            selectedArticle.setStarState(Api.ART_UNSTAR);
            if (selectedArticle.getSaveDir().equals(Api.SAVE_DIR_STORE) || selectedArticle.getSaveDir().equals(Api.SAVE_DIR_STOREREAD)) {
                selectedArticle.setSaveDir(Api.SAVE_DIR_BOX);
            }
        }
        WithDB.i().saveArticle(selectedArticle);
    }

    public void onReadClick(View view) {
//        articleStateManager.changeReadState(selectedArticle);
        KLog.e("loread", "被点击的是：" + selectedArticle.getTitle());

        if (selectedArticle.getReadState().equals(Api.ART_READED)) {
            vRead.setText(getString(R.string.font_unread));
            DataApi.i().markArticleUnread(selectedArticle.getId(), null);
            selectedArticle.setReadState(Api.ART_UNREADING);
            DataApi.i().changeUnreadCount(selectedArticle.getOriginStreamId(), 1);
        } else {
            vRead.setText(getString(R.string.font_readed));
            DataApi.i().markArticleReaded(selectedArticle.getId(), null);
            selectedArticle.setReadState(Api.ART_READED);
            DataApi.i().changeUnreadCount(selectedArticle.getOriginStreamId(), -1);
        }
        WithDB.i().saveArticle(selectedArticle);
    }

    public void onSaveClick(View view){
        KLog.e("loread", "保存文件被点击");
        if (selectedArticle.getSaveDir().equals(Api.SAVE_DIR_CACHE)) {
            if (selectedArticle.getStarState().equals(Api.ART_STARED)) {
                selectedArticle.setSaveDir(Api.SAVE_DIR_STORE);
            } else {
                selectedArticle.setSaveDir(Api.SAVE_DIR_BOX);
            }
            WithDB.i().saveArticle(selectedArticle);
        } else {
            ToastUtil.showShort(getString(R.string.toast_file_saved));
//            moveArticleTo( selectedArticle.getSaveDir(), Api.SAVE_DIR_CACHE ); // 暂时不能支持从 box/store 撤销保存回 cache，因为文章的正文是被加了修饰的，为 epub 文件做准备的
        }
        if (selectedArticle.getSaveDir().equals(Api.SAVE_DIR_CACHE)) {
            vSave.setText(getString(R.string.font_unsave));
        } else {
            vSave.setText(getString(R.string.font_saved));
        }
    }

    // View view
    public void onReadabilityClick() {
        final String handledArticleId = selectedArticle.getId();
//        swipeRefreshLayoutS.setRefreshing(true);
        KLog.e("=====点击");
        ToastUtil.showLong(getString(R.string.toast_get_readability_ing));
        MercuryApi.fetchReadabilityContent(selectedArticle.getCanonical(), new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                if (handledArticleId != selectedArticle.getId()) {
                    return;
                }
//                swipeRefreshLayoutS.setRefreshing(false);
                Readability readability = new Gson().fromJson(response.body(), Readability.class);
//                webView.loadDataWithBaseURL(FileUtil.getAbsoluteDir(selectedArticle.getSaveDir()), StringUtil.getHeader(selectedArticle) + readability.getHtml() + StringUtil.getFooter());
//                webView.loadDataWithBaseURL("file://" + App.externalCachesDir + "cache/" , StringUtil.getHeader(selectedArticle) + readability.getHtml() + StringUtil.getFooter(),App.APP_NAME_EN + "://" + selectedArticle.getOriginHtmlUrl());
//                webView.loadDataWithBaseURL("file://" + App.externalCachesDir + "cache/" , StringUtil.getHtml(selectedArticle,readability.getContent()));
                webView.loadArticlePage(selectedArticle, readability.getContent());
                KLog.e("===== 更新");
            }

            @Override
            public void onError(Response<String> response) {
                ToastUtil.showLong(getString(R.string.fail_try));
//                swipeRefreshLayoutS.setRefreshing(false);
            }
        });
    }

    private BottomSheetDialog dialog;

    public void onClickMore(View view) {
        dialog = new BottomSheetDialog(ArticleActivity.this);
        dialog.setContentView(R.layout.article_bottom_sheet_more);
//        dialog.dismiss(); //dialog消失
//        dialog.setCanceledOnTouchOutside(false);  //触摸dialog之外的地方，dialog不消失
//        dialog.setCancelable(false); // dialog无法取消，按返回键都取消不了
        View feedConfigView = dialog.findViewById(R.id.feed_config);
        IconFontView feedConfig = dialog.findViewById(R.id.feed_config_icon);
        IconFontView openLinkByBrowser = dialog.findViewById(R.id.open_link_by_browser_icon);
        IconFontView getReadability = dialog.findViewById(R.id.get_readability_icon);
        TextView getReadabilityTitle = dialog.findViewById(R.id.get_readability_title);

        final Feed feed = WithDB.i().getFeed(selectedArticle.getOriginStreamId());

//        ImageView feedLogo = dialog.findViewById(R.id.feed_logo);
//        TextView feedTitle = dialog.findViewById(R.id.feed_title);
//        TextView feedSummary = dialog.findViewById(R.id.feed_summary);


//        IconFontView nightTheme = dialog.findViewById(R.id.article_night_theme_icon);
//        TextView nightThemeTitle = dialog.findViewById(R.id.article_night_theme_title);
//        if (App.Theme_Night == WithPref.i().getThemeMode()) {
//            nightTheme.setTextColor(getResources().getColor(R.color.colorPrimary));
//            nightThemeTitle.setTextColor(getResources().getColor(R.color.colorPrimary));
//        }

        if (feed == null) {
            feedConfigView.setVisibility(View.GONE);
//            feedLogo.setVisibility(View.GONE);
//            feedTitle.setVisibility(View.GONE);
//            feedSummary.setVisibility(View.GONE);
        } else if (Api.DISPLAY_READABILITY.equals(feed.getDisplayMode())) {
//            Glide.with(this).load(feed.getIconurl()).placeholder(R.mipmap.ic_launcher).into(feedLogo);
//            feedTitle.setText(feed.getTitle());
//            feedSummary.setText(feed.getUrl());

            getReadability.setTextColor(getResources().getColor(R.color.colorPrimary));
            getReadabilityTitle.setTextColor(getResources().getColor(R.color.colorPrimary));
            getReadability.setClickable(false);
            getReadabilityTitle.setClickable(false);
        }


        dialog.show();

        feedConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
//                ToastUtil.showShort("配置该订阅源");
                FeedConfigDialog feedConfigerDialog = new FeedConfigDialog();
                feedConfigerDialog.setOnUnsubscribeFeedListener(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (!response.body().equals("OK")) {
                            this.onError(response);
                            return;
                        }
//                        App.StreamId = "user/" + WithPref.i().getUseId() + Api.U_READING_LIST;
//                        App.StreamTitle = getString(R.string.main_activity_title_all);
//                        Intent localIntent = new Intent(Api.SYNC_ALL);
//                        LocalBroadcastManager lbM = LocalBroadcastManager.getInstance(App.i());
//                        lbM.sendBroadcast(localIntent.putExtra(Api.NOTICE,Api.N_COMPLETED));
                        WithDB.i().unsubscribeFeed(feed);

                        // 返回 mainActivity 页面，并且跳到下一个 tag/feed
                        // KLog.e("移除" + itemView.groupPos + "  " + itemView.childPos );
                    }

                    @Override
                    public void onError(Response<String> response) {
                        ToastUtil.showLong(App.i().getString(R.string.toast_unsubscribe_fail));
                    }
                });
                feedConfigerDialog.showConfigFeedDialog(ArticleActivity.this, feed);
            }
        });
        openLinkByBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (WithPref.i().isSysBrowserOpenLink()) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse(selectedArticle.getCanonical()));
                    // 目前无法坐那那种可以选择打开方式的
                    ArticleActivity.this.startActivity(Intent.createChooser(intent, "选择打开方式"));
                } else {
                    openLinkByAgentWeb();
                }
            }
        });

        getReadability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                onReadabilityClick();
            }
        });

//        nightTheme.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                manualToggleTheme();
//                dialog.dismiss();
//            }
//        });
    }


    AgentWeb agentWeb;

    public void openLinkByAgentWeb() {
        final BottomSheetDialog webViewDialog = new BottomSheetDialog(ArticleActivity.this);
        webViewDialog.setContentView(R.layout.fragment_tag);
        View dialogView = webViewDialog.getWindow().findViewById(android.support.design.R.id.design_bottom_sheet);
        BottomSheetBehavior.from(dialogView).setPeekHeight(ScreenUtil.getScreenHeight(this));
        webViewDialog.setCancelable(false); // dialog无法取消，按返回键都取消不了
//        webViewDialog.setCanceledOnTouchOutside(false);  //触摸dialog之外的地方，dialog不消失
//        webViewDialog.dismiss(); //dialog消失
        FrameLayout frameLayout = webViewDialog.findViewById(R.id.fragment_tag_webview);

        agentWeb = AgentWeb.with(ArticleActivity.this)
                .setAgentWebParent(frameLayout, -1, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))//传入AgentWeb的父控件。
                .useDefaultIndicator(-1, 3)//设置进度条颜色与高度，-1为默认值，高度为2，单位为dp。
//                  .setAgentWebWebSettings(getSettings())//设置 IAgentWebSettings。
//                .setWebViewClient(new WebViewClientX())//WebViewClient ， 与 WebView 使用一致 ，但是请勿获取WebView调用setWebViewClient(xx)方法了,会覆盖AgentWeb DefaultWebClient,同时相应的中间件也会失效。
//                .setWebChromeClient(new WebChromeClientX()) //WebChromeClient
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK) //严格模式 Android 4.2.2 以下会放弃注入对象 ，使用AgentWebView没影响。
                .setMainFrameErrorView(R.layout.agentweb_error_page, -1) //参数1是错误显示的布局，参数2点击刷新控件ID -1表示点击整个布局都刷新， AgentWeb 3.0.0 加入。
                .setOpenOtherPageWays(DefaultWebClient.OpenOtherPageWays.DISALLOW)//打开其他页面时，弹窗质询用户前往其他应用 AgentWeb 3.0.0 加入。
                .interceptUnkownUrl() //拦截找不到相关页面的Url AgentWeb 3.0.0 加入。

                .createAgentWeb()//创建AgentWeb。
                .ready()//设置 WebSettings。
                // WebView载入该url地址的页面并显示。
//                .loadUrl("http://m.youku.com/video/id_XODEzMjU1MTI4.html");
//                .loadDataWithBaseURL(App.externalFilesDir, selectedArticle.getHtml(),"text/html", "UTF-8",null);
                .loadUrl(selectedArticle.getCanonical());
        Tool.setBackgroundColor(agentWeb.getWebCreator().getWebView());
        webViewDialog.show();


        IconFontView leftView = webViewDialog.findViewById(R.id.fragment_tag_left);
        IconFontView closeView = webViewDialog.findViewById(R.id.fragment_tag_close);
        IconFontView rightView = webViewDialog.findViewById(R.id.fragment_tag_right);
        leftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (agentWeb.getWebCreator().getWebView().canGoBack()) {
                    agentWeb.back();
                } else {
                    ToastUtil.showShort("无法后退");
                }
            }
        });
        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webViewDialog.dismiss();
            }
        });

        rightView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (agentWeb.getWebCreator().getWebView().canGoForward()) {
                    agentWeb.getWebCreator().getWebView().goForward();
                } else {
                    ToastUtil.showShort("无法前进");
                }
            }
        });
    }


    private void initIconState(int position) {
        if (selectedArticle.getReadState().equals(Api.ART_UNREAD)) {
            vRead.setText(getString(R.string.font_readed));
            final String lastArticleId = selectedArticle.getId();
            DataApi.i().markArticleReaded(selectedArticle.getId(), new StringCallback() {
                @Override
                public void onSuccess(Response<String> response) {
                }

                @Override
                public void onError(Response<String> response) {
                    Article article = WithDB.i().getArticle(lastArticleId);
                    if (article == null) {
                        return;
                    }
                    article.setReadState(Api.ART_UNREAD);
                    WithDB.i().saveArticle(article);
                    DataApi.i().changeUnreadCount(article.getOriginStreamId(), 1);
                    if (selectedArticle != null && selectedArticle.getId().equals(article.getId())) {
                        vRead.setText(getString(R.string.font_unread));
                    }
                }
            });
            selectedArticle.setReadState(Api.ART_READED);
            WithDB.i().saveArticle(selectedArticle);
            DataApi.i().changeUnreadCount(selectedArticle.getOriginStreamId(), -1);
            KLog.i("【 ReadState 】" + WithDB.i().getArticle(selectedArticle.getId()).getReadState());
        } else if (selectedArticle.getReadState().equals(Api.ART_READED)) {
            vRead.setText(getString(R.string.font_readed));
        } else if (selectedArticle.getReadState().equals(Api.ART_UNREADING)) {
            vRead.setText(getString(R.string.font_unread));
        }

        if (selectedArticle.getStarState().equals(Api.ART_UNSTAR)) {
            vStar.setText(getString(R.string.font_unstar));
        } else {
            vStar.setText(getString(R.string.font_stared));
        }
        if (selectedArticle.getSaveDir().equals(Api.SAVE_DIR_CACHE)) {
            vSave.setText(getString(R.string.font_unsave));
        } else {
            vSave.setText(getString(R.string.font_saved));
        }

        vArticleNum.setText((position + 1) + " / " + articleCount);
        KLog.i("=====position" + position);
    }


    // SimpleOnPageChangeListener 是 ViewPager 内部，用空方法实现 OnPageChangeListener 接口的一个类。
    // 主要是为了便于使用者，不用去实现 OnPageChangeListener 的每一个方法（没必要，好几个用不到），只需要直接继承 SimpleOnPageChangeListener 实现需要的方法即可。
    private class PageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        /**
         * 参数position，代表哪个页面被选中。
         * 当用手指滑动翻页的时候，如果翻动成功了（滑动的距离够长），手指抬起来就会立即执行这个方法，position就是当前滑动到的页面。
         * 如果直接setCurrentItem翻页，那position就和setCurrentItem的参数一致，这种情况在onPageScrolled执行方法前就会立即执行。
         * 泪奔，当 setCurrentItem (0) 的时候，不会调用该函数。
         * 点击进入 viewpager 时，该函数比 InstantiateItem 函数先执行。（之后滑动时，InstantiateItem 已经创建好了 view）
         * 所以，为了能在运行完 InstantiateItem ，有了 webView 之后再去执行 initSelectedPage 在首次进入时都不执行，放到 InstantiateItem 中。
         */
        @Override
        public void onPageSelected(final int pagePosition) {
            // 当知道页面被选中后：1.检查webView是否已经生成好了，若已生成则初始化所有数据（含页面的icon）；2.检查webView是否加载完毕，完毕则初始化懒加载
            initSelectedPage(pagePosition);
        }
    }

    public void initSelectedPage(int position) {
//        swipeRefreshLayoutS.setRefreshing(false);
        initSelectedArticle(position);
        initSelectedWebView(position);
//        KLog.e("initSelectedPage结束。位置：" + position + "   " + webView);
    }

    private SparseBooleanArray mWebViewLoadedJs = new SparseBooleanArray();
    private int selectedPos;

    public void initSelectedArticle(int position) {
        // 取消之前那篇文章的图片下载
        if (null != selectedArticle) {
            OkGo.cancelTag(App.imgHttpClient, selectedArticle.getId());
        }

        this.selectedArticle = App.articleList.get(position);
//        KLog.e("loread", "initSelectedArticle" + selectedArticle.getTitle() + "====" + webView);
        selectedPos = position;
        initIconState(position);
    }


    public void initSelectedWebView(final int position) {
//        KLog.e("获取WebView = " + viewPager.findViewById(position) );
        if (viewPager.findViewById(position) == null) {
//            KLog.i("重新获取" );
            articleHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initSelectedWebView(position);
                }
            }, 300);
            return;
        }
        this.webView = viewPager.findViewById(position);
        for (int i = 0, size = App.i().mWebViewCaches.size(); i < size; i++) {
            if (App.i().mWebViewCaches.get(i).getId() != position) {
                //让webview重新加载，用于停掉音视频的声音
                App.i().mWebViewCaches.get(i).reload();
                // 先重载webview再暂停webview，这时候才真正能够停掉音视频的声音，api 2.3.3 以上才能暂停
                // 暂停网页中正在播放的视频
                App.i().mWebViewCaches.get(i).onPause();
                KLog.e("暂停其他的webview = " + webView);
            }
        }
        initSelectedWebViewContent(position);
    }


    // （webview在实例化后，可能还在渲染html，不一定能执行js）
    private void initSelectedWebViewContent(final int position) {
        KLog.e("初始WebView = " + mWebViewLoadedJs.get(position));
        // 对于已经加载过的webview做过滤，防止重复加载
        if (mWebViewLoadedJs.get(position)) {
            return;
        }

        // 增加Javascript异常监控
        CrashReport.setJavascriptMonitor(webView, true);

        // 初始化视频处理类
        mIVideo = new VideoImpl(this, webView);

//        Article article = ((Article) webView.getTag());
        Article article = webView.getArticle();
        Feed feed = WithDB.i().getFeed(article.getOriginStreamId());
        if (feed != null && Api.DISPLAY_LINK.equals(feed.getDisplayMode())) {
            webView.loadUrl(article.getCanonical());
            mWebViewLoadedJs.put(position, true);
        } else {
            // 尝试执行一次，但是可能因为渲染html比较慢，导致补发成功执行。所以在每个webview渲染成功后会再去尝试调用一次初始化js
            tryInitJs(article.getId());
        }

//        webView.loadUrl("http://m.youku.com/video/id_XODEzMjU1MTI4.html");
        KLog.e("触发初始化" + mIVideo + "   " + webView);
    }


    private class ViewPagerAdapter extends PagerAdapter {
        private ArticleActivity activity;
        private List<Article> dataList;
        private final SparseArray<WebViewS> mEntryViews = new SparseArray<>();

        private ViewPagerAdapter(ArticleActivity context, List<Article> dataList) {
            if (null == dataList || dataList.isEmpty()) {
                return;
            }
            this.activity = context;
            this.dataList = dataList;
        }

        // 功能：该函数用来判断instantiateItem(ViewGroup, int)函数所返回来的Key与一个页面视图是否是代表的同一个视图(即它俩是否是对应的，对应的表示同一个View)
        // Note: 判断出去的view是否等于进来的view 如果为true直接复用
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        // 初始化一个 WebView 有以下几个步骤：
        // 1.设置 WebSettings
        // 2.设置 WebViewClient，①重载点击链接的时间，从而控制之后的跳转；②重载页面的开始加载事件和结束加载事件，从而控制图片和点击事件的加载
        // 3.设置 WebChromeClient，①重载页面的加载改变事件，从而注入bugly的js监控
        // 4.设置 DownloadListener，从而实现下载文件时使用系统的下载方法
        // 5.设置 JS通讯
        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            final WebViewS webView;
            if (App.i().mWebViewCaches.size() > 0) {
                webView = App.i().mWebViewCaches.get(0);
                App.i().mWebViewCaches.remove(0);
//                KLog.e("WebView" , "复用" + webView);
            } else {
                webView = new WebViewS(activity);
//                KLog.e("WebView" , "创建" + webView);
            }

            webView.setWebViewClient(new WebViewClientX());
            webView.setWebChromeClient(new WebChromeClientX());
            webView.addJavascriptInterface(activity, "ImageBridge");

//            KLog.e("WebView = " + webView);
            // 方便在其他地方调用 viewPager.findViewById 来找到 webView
            webView.setId(position);
            // 方便在webView onPageFinished 的时候调用
//            webView.setTag(dataList.get(position));
            webView.setArticle(dataList.get(position));
            container.addView(webView);

            mEntryViews.put(position, webView);
            mWebViewLoadedJs.put(position, false);

            // 检查该订阅源默认显示什么。
            // 【RSS，已读，保存的网页，原始网页】
            Feed feed = WithDB.i().getFeed(dataList.get(position).getOriginStreamId());
            // 填充加载中视图
//            KLog.e("保存的位置为：" + FileUtil.getAbsoluteDir(dataList.get(position).getSaveDir() ) );
//            KLog.e("测试正文为空的问题A", "空");
            if (feed != null && Api.DISPLAY_LINK.equals(feed.getDisplayMode())) {
//                webView.loadDataWithBaseURL("file://" + App.externalCachesDir + "cache/" , "","");
//                webView.loadDataWithBaseURL("file://" + App.externalCachesDir + "cache/", "");
                webView.loadHolderPage();
            } else {
//               webView.loadDataWithBaseURL(FileUtil.getAbsoluteDir(dataList.get(position).getSaveDir()), StringUtil.getHeader(dataList.get(position)) + StringUtil.initContent(dataList.get(position)) + StringUtil.getFooter());
//                webView.loadDataWithBaseURL("file://" + App.externalCachesDir + "cache/", StringUtil.getHtml(dataList.get(position)));
                webView.loadArticlePage(dataList.get(position));
            }
            return webView;
        }


        // Note: 销毁预加载以外的view对象, 会把需要销毁的对象的索引位置传进来就是position
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (object == null) {
                return;
            }
            container.removeView((View) object);
            mEntryViews.delete(position);
            mWebViewLoadedJs.delete(position);
            ((WebViewS) object).destroy();

            App.i().mWebViewCaches.add(new WebViewS(new MutableContextWrapper(activity)));
//            ((WebViewS) object).clear();
//            App.i().mWebViewCaches.add((WebViewS) object);
        }

        @Override
        public int getCount() {
            return (null == dataList) ? 0 : dataList.size();
        }
    }


    private class WebChromeClientX extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView webView, int progress) {
            if (slowlyProgressBar != null) {
                slowlyProgressBar.onProgressChange(progress);
            }
        }

        // 表示进入全屏的时候
        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            KLog.e("webview视频", "onShowCustomView");
            if (mIVideo != null) {
                mIVideo.onShowCustomView(view, callback);
            }
        }

        //表示退出全屏的时候
        @Override
        public void onHideCustomView() {
            KLog.e("webview视频", "onHideCustomView");
            if (mIVideo != null) {
                mIVideo.onHideCustomView();
            }
        }
    }


    private class WebViewClientX extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
//          【scheme链接打开本地应用】（https://www.jianshu.com/p/45af72036e58）
            //http和https协议开头的执行正常的流程
            if (url.startsWith("http") || url.startsWith("https")) {
                // 方法1
//                Intent intent = new Intent();
//                intent.setAction(android.content.Intent.ACTION_VIEW);
//                intent.loadDataWithBaseURL(Uri.parse(url));
//                ArticleActivity.this.startActivity(Intent.createChooser(intent, "选择打开方式")); // 目前无法坐那那种可以选择打开方式的
//                return true;

                // 方法2
//                if( !((WebViewS) webView).canGoBack()){
//                    ((WebViewS) webView).setFirstUrl(url);
//                    offsetPosition = webView.getScrollY();
//                    OkGo.cancelAll(App.imgHttpClient);
////                    KLog.e("无法回退" + offsetPosition  );
//                }
//                return false;

                // 方法3
                openLink(url);
                OkGo.cancelAll(App.imgHttpClient);
                offsetPosition = webView.getScrollY();
                return true;

            } else {  //其他的URL则会开启一个Acitity然后去调用原生APP
                final Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                if (in.resolveActivity(getPackageManager()) != null) {
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    KLog.e("应用星系：" + in.resolveActivity(getPackageManager()).getPackageName());
                    KLog.e("应用星系：" + in.resolveActivity(getPackageManager()));
                    String name = "相应的";
                    try {
//                        getPackageManager().getApplicationLabel(applicationInfo);
                        name = getPackageManager().getApplicationInfo(in.resolveActivity(getPackageManager()).getPackageName(), PackageManager.GET_META_DATA).toString();
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    SnackbarUtil.Long(viewPager, "跳转到“" + name + "”应用？")
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
        }

        @Override
        public void onPageFinished(final WebView webView, String url) {
            super.onPageFinished(webView, url);
            if (!webView.canGoBack()) {
//                KLog.e("页面加载完，无法回退" + offsetPosition);
                webView.scrollTo(0, offsetPosition);
            }
        }

        @Override
        public void onPageStarted(WebView webView, String var2, Bitmap var3) {
            super.onPageStarted(webView, var2, var3);
            if (slowlyProgressBar != null) {
                slowlyProgressBar.onProgressStart();
            }
        }
    }

    private int offsetPosition;


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 后者为短期内按下的次数
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (webView != null && webView.canGoBack()) {
                webView.goBack();
                return true;
            } else {
                Intent data = new Intent();
                data.putExtra("articleNo", viewPager.getCurrentItem());
                //注意下面的RESULT_OK常量要与回传接收的Activity中onActivityResult（）方法一致
                ArticleActivity.this.setResult(Api.ActivityResult_ArtToMain, data);
                this.finish();
            }
            // 返回真表示返回键被屏蔽掉
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

//    private void back() {
//        Intent data = new Intent();
//        data.putExtra("articleNo", viewPager.getCurrentItem());
//        //注意下面的RESULT_OK常量要与回传接收的Activity中onActivityResult（）方法一致
//        ArticleActivity.this.setResult(Api.ActivityResult_ArtToMain, data);
//        this.finish();
//    }
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        outState.putInt("articleNo", viewPager.getCurrentItem());
//        outState.putInt("articleCount", articleCount);
//        super.onSaveInstanceState(outState);
//    }


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
//                .textColor(R.id.article_bottombar_tag, R.attr.bottombar_fg)
                .textColor(R.id.art_bottombar_save, R.attr.bottombar_fg);
        return mColorfulBuilder;
    }


//    private void moveArticleTo(Article selectedArticle, String targetDir) {
//        String sourceFileTitle;
//        String targetFileTitle;
//        String articleHtml;
//        String sourceDirPath = FileUtil.getRelativeDir(selectedArticle.getSaveDir());
//        String targetDirPath = FileUtil.getRelativeDir(targetDir);
//
//        if (selectedArticle.getSaveDir().equals(Api.SAVE_DIR_CACHE)) {
//            sourceFileTitle = StringUtil.str2MD5(selectedArticle.getId());
//            articleHtml = FileUtil.readFile(sourceDirPath + sourceFileTitle + ".html");
//            articleHtml = StringUtil.reviseHtmlForBox(selectedArticle.getTitle(), articleHtml);
//            FileUtil.saveFile(sourceDirPath + sourceFileTitle + ".html", articleHtml);
//            targetFileTitle = StringUtil.getOptimizedNameForSave(selectedArticle.getTitle());
//            selectedArticle.setTitle(targetFileTitle);
//        } else {
//            sourceFileTitle = selectedArticle.getTitle();
//            targetFileTitle = sourceFileTitle;
//        }
//
//        FileUtil.moveFile(sourceDirPath + sourceFileTitle + ".html", targetDirPath + targetFileTitle + ".html");
//        FileUtil.moveDir(sourceDirPath + sourceFileTitle + "_files", targetDirPath + targetFileTitle + "_files");
//
////        KLog.i(sourceFileTitle);
////        KLog.d("原来文件夹" + sourceDirPath + sourceTitle + "_files");
////        KLog.d("目标文件夹" + targetDirPath + selectedArticle.getTitle() + "_files");
//        if (!TextUtils.isEmpty(selectedArticle.getCoverSrc())) {  // (lossSrcList != null && lossSrcList.size() != 0) || ( obtainSrcList!= null && obtainSrcList.size() != 0)
//            selectedArticle.setCoverSrc(targetDirPath + selectedArticle.getTitle() + "_files" + File.separator + StringUtil.getFileNameExtByUrl(selectedArticle.getCoverSrc()));
////            KLog.d("封面" + targetDirPath + selectedArticle.getTitle() + "_files" + File.separator + StringUtil.getFileNameExtByUrl(selectedArticle.getCoverSrc()));
//        }
//        fileTitle = targetFileTitle;
//        selectedArticle.setSaveDir(targetDir);
//        WithDB.i().saveArticle(selectedArticle);
//    }


    public void onTagClick(View view) {
        final List<Tag> tagsList = WithDB.i().getTags();
        ArrayList<String> tags = new ArrayList<>(tagsList.size());
        for (Tag tag : tagsList) {
            tags.add(tag.getTitle());
        }
        new MaterialDialog.Builder(this)
                .title(R.string.article_choose_tag_dialog_title)
                .items(tags)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        String tagId = tagsList.get(which).getId();
                        StringBuilder newCategories = new StringBuilder(selectedArticle.getCategories().length());
                        String[] cateArray = selectedArticle.getCategories().replace("]", "").replace("[", "").split(",");
                        StringBuilder tempCate = new StringBuilder();
                        for (String category : cateArray) {
                            tempCate.append(tempCate);
                            if (category.contains("user/" + App.UserID + "/label/")) {
                                if (category.substring(0, 1).equals("\"")) {
                                    category = category.substring(1, category.length());
                                }
                                if (category.substring(category.length() - 1, category.length()).equals("\"")) {
                                    category = category.substring(0, category.length() - 1);
                                }
                                DataApi.i().articleRemoveTag(selectedArticle.getId(), category, null);

                                KLog.i("【-】" + category);
                            } else {
                                newCategories.append(category);
                                newCategories.append(", ");
                            }
                        }
                        newCategories.append(tagId);
                        newCategories.append("]");
                        KLog.i("【==】" + newCategories + selectedArticle.getId());
                        selectedArticle.setCategories(newCategories.toString());
                        DataApi.i().articleAddTag(selectedArticle.getId(), tagId, null);
                        dialog.dismiss();
                        return true; // allow selection
                    }
                })
                .show();
    }


}
