package me.wizos.loread.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.MutableContextWrapper;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.socks.library.KLog;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.wizos.loread.App;
import me.wizos.loread.BuildConfig;
import me.wizos.loread.R;
import me.wizos.loread.bean.config.GlobalConfig;
import me.wizos.loread.bean.gson.Readability;
import me.wizos.loread.common.ImageBridge;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithPref;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Feed;
import me.wizos.loread.net.Api;
import me.wizos.loread.net.DataApi;
import me.wizos.loread.net.MercuryApi;
import me.wizos.loread.utils.NetworkUtil;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.utils.ToastUtil;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.SwipeRefreshLayoutS;
import me.wizos.loread.view.ViewPagerS;
import me.wizos.loread.view.WebViewS;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.webview.SlowlyProgressBar;
import me.wizos.loread.view.webview.VideoImpl;
import okhttp3.OkHttpClient;

/**
 * @author Wizos on 2017
 */
@SuppressLint("SetJavaScriptEnabled")
public class ArticleActivity extends BaseActivity implements ImageBridge {
    protected static final String TAG = "ArticleActivity";

    private SlowlyProgressBar slowlyProgressBar;
    private IconFontView vStar, vRead, vSave;
    private TextView vArticleNum;
    private WebViewS selectedWebView;
    private Article selectedArticle;
    private SparseBooleanArray mWebViewLoadedJs = new SparseBooleanArray();
    private ViewPagerS viewPager;
    private int articleNo, articleCount;
    private int selectedPosition;
    private String articleId;
    private SwipeRefreshLayoutS swipeRefreshLayoutS;

    /**
     * Video 视频播放类
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        if (savedInstanceState != null) {
            // setSelection 没有滚动效果，直接跳到指定位置。smoothScrollToPosition 有滚动效果的
            articleNo = savedInstanceState.getInt("articleNo");
            articleCount = savedInstanceState.getInt("articleCount");
            articleId = savedInstanceState.getString("articleId");
        } else {
            // 文章在列表中的位置编号，下标从 0 开始
            articleNo = getIntent().getExtras().getInt("articleNo");
            // 列表中所有的文章数目
            articleCount = getIntent().getExtras().getInt("articleCount");
            articleId = getIntent().getExtras().getString("articleId");
        }

//        KLog.e("开始初始化数据2" + articleNo + "==" + articleCount);
        initToolbar();
        initView(); // 初始化界面上的 View，将变量映射到布局上。
        initViewPager();
        buildImgClient();
    }

    @Override
    public void onResume() {
        if (selectedWebView != null) {
            selectedWebView.onResume();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (selectedWebView != null) {
            selectedWebView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("articleNo", articleNo);
        outState.putInt("articleCount", articleCount);
        outState.putString("articleId", articleId);
        super.onSaveInstanceState(outState);
    }

//    @Override
//    protected void onRestoreInstanceState(Bundle outState) {
//        super.onRestoreInstanceState(outState);
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
        swipeRefreshLayoutS = findViewById(R.id.art_swipe_refresh);
        swipeRefreshLayoutS.setEnabled(false);
        if (!BuildConfig.DEBUG) {
            vSave.setVisibility(View.GONE);
        } else {
            vSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onSaveClick(view);
                }
            });
            vArticleNum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showArticleInfo(view);
                }
            });
        }
    }


    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.art_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
//        白色箭头
//        Drawable upArrow = getResources().getDrawable(R.drawable.mz_ic_sb_back);
//        upArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
//        getSupportActionBar().setHomeAsUpIndicator(upArrow); // 替换返回箭头
        slowlyProgressBar = new SlowlyProgressBar((ProgressBar) findViewById(R.id.article_progress_bar));
    }


    public void initViewPager() {
        viewPager = findViewById(R.id.art_viewpager);
        viewPager.clearOnPageChangeListeners();
        if (App.articleList == null) {
            articleNo = 0;
            articleCount = 1;
            App.articleList.add(WithDB.i().getArticle(articleId));
        }
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, App.articleList);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new PageChangeListener());
        // 本句放到 ViewPagerAdapter 的构造器中是无效的。
        viewPager.setCurrentItem(articleNo, false);
        // 当 setCurrentItem (0) 的时候，不会调用 onPageSelected 函数，导致无法触发 initSelectedPage 函数，所以这里要手动触发。
        if (articleNo == 0) {
            this.initSelectedPage(0);
        }
    }


    @Override
    protected void onDestroy() {
        // 如果参数为null的话，会将所有的Callbacks和Messages全部清除掉。
        // 这样做的好处是在 Acticity 退出的时候，可以避免内存泄露。因为 handler 内可能引用 Activity ，导致 Activity 退出后，内存泄漏
        KLog.e("loread", "onDestroy" + selectedWebView);
        articleHandler.removeCallbacksAndMessages(null);
        selectedWebView = null;
        WebViewS webViewS;
        for (int i = 0; i < viewPager.getChildCount(); i++) {
            // 使用自己包装的 webview
            webViewS = (WebViewS) viewPager.getChildAt(i);
            webViewS.destroy();
//            KLog.e("创建新的webview");
            App.i().mWebViewCaches.add(new WebViewS(new MutableContextWrapper(App.i())));
        }

        viewPager.removeAllViews();
        viewPager.clearOnPageChangeListeners();
        OkGo.cancelAll(articleImgHttpClient);
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
        // 调用系统默认的图片查看应用
//        Intent intentImage = new Intent(Intent.ACTION_VIEW);
//        intentImage.addCategory(Intent.CATEGORY_DEFAULT);
//        File file = new File(imageFilePath);
//        intentImage.setDataAndType(Uri.fromFile(file), "image/*");
//        startActivity(intentImage);

        // 每次都要选择打开方式
//        startActivity(Intent.createChooser(intentImage, "请选择一款"));

        // 直接打开内置图片浏览器
        Intent intent = new Intent(ArticleActivity.this, ImageActivity.class);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setDataAndType(Uri.fromFile(new File(imageFilePath)), "image/*");
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

//        if(BuildConfig.DEBUG){
//            Intent openImageIntent = new Intent(Intent.ACTION_VIEW);
//            openImageIntent.addCategory(Intent.CATEGORY_DEFAULT);
//            openImageIntent.setDataAndType(Uri.fromFile(new File(imageFilePath)), "image/*");
//            getDefaultActivity(openImageIntent);
//        }
    }

    // 获取默认的打开方式
    public void getDefaultActivity(Intent intent) {
        PackageManager pm = this.getPackageManager();
        ResolveInfo info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        // 如果本应用没有询问过是否要选择默认打开方式，并且没有默认的打开方式，打开默认方式选择狂
        if (!WithPref.i().hadAskImageOpenMode() || info.activityInfo.packageName.equals("android")) {
            WithPref.i().setHadAskImageOpenMode(true);
            intent.setComponent(new ComponentName("android", "com.android.internal.app.ResolverActivity"));
        }
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        KLog.i("打开方式", "默认打开方式信息 = " + info + ";pkgName = " + info.activityInfo.packageName);
    }

    // 打开选择默认打开方式的弹窗
    public void startChooseDialog() {
//        Intent intent = new Intent();
//        intent.setAction("android.intent.action.VIEW");
//        intent.addCategory(Intent.CATEGORY_DEFAULT);
//        intent.setData(Uri.fromFile(new File(imageFilePath)));
//        intent.setComponent(new ComponentName("android","com.android.internal.app.ResolverActivity"));
//        startActivity(intent);
    }

    @JavascriptInterface
    @Override
    public void downImage(String articleId, final int index, final String originalUrl) {
        final String idInMD5 = StringUtil.str2MD5(articleId);
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
//                KLog.e("下载成功：" + originalUrl + " - " + viewPager.findViewById(selectedPosition) + " - " + filePath + fileNameExt);
                if (selectedPosition != -1 && viewPager.findViewById(selectedPosition) != null) {
                    ((WebViewS) viewPager.findViewById(selectedPosition)).loadUrl("javascript:onImageLoadSuccess('" + originalUrl + "','" + filePath + fileNameExt + "')");
                }
            }

            // 该方法执行在主线程中
            @Override
            public void onError(Response<File> response) {
//                KLog.e("下载失败：" + originalUrl + " - " + viewPager.findViewById(selectedPosition) + " - " + filePath + fileNameExt);
                if (selectedPosition != -1 && viewPager.findViewById(selectedPosition) != null) {
                    ((WebViewS) viewPager.findViewById(selectedPosition)).loadUrl("javascript:onImageLoadFailed('" + originalUrl + "')");
                }
                new File(filePath + fileNameExt + Api.EXT_TMP).delete();
            }
        };
        Request request = OkGo.<File>get(originalUrl)
                .tag(articleId)
                .client(articleImgHttpClient);

        String referer = GlobalConfig.i().guessRefererByUrl(originalUrl);
        KLog.e("图片链接是：" + originalUrl + "， 来源是：" + referer);
        if (!TextUtils.isEmpty(referer)) {
            request.headers(Api.Referer, referer);
        }


        request.execute(fileCallback);
//        KLog.e("下载：" + originalUrl + " 来源 " + selectedArticle.getCanonical() );
    }

    @JavascriptInterface
    @Override
    public void tryInitJs(String articleId) {
//        KLog.e("准备执行 tryInitJs：" +  viewPager.getCurrentItem() + "  "+  selectedArticle.getId() + "  " + articleId );
        if (!selectedArticle.getId().equals(articleId)) {
            return;
        }
        mWebViewLoadedJs.put(viewPager.getCurrentItem(), false);
        selectedWebView.postDelayed(new Runnable() {
            @Override
            public void run() {
//                KLog.e("开始执行 tryInitJs：" +  viewPager.getCurrentItem() + "  "+  selectedArticle.getId() + "  " + selectedWebView );
                selectedWebView.loadUrl("javascript:setupImage()");
                mWebViewLoadedJs.put(viewPager.getCurrentItem(), true);
            }
        }, 300);
    }


    @JavascriptInterface
    @Override
    public void openLink(String link) {
        if (WithPref.i().isSysBrowserOpenLink()) {
            Intent intent = new Intent();
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setData(Uri.parse(link));
            startActivity(Intent.createChooser(intent, "选择打开方式"));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else {
            Intent intent = new Intent(ArticleActivity.this, WebActivity.class);
            intent.setData(Uri.parse(link));
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }


    public Handler articleHandler = new Handler();


    public void showArticleInfo(View view) {
        KLog.e("文章信息");
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
                "Summary=" + selectedArticle.getSummary() + "\n" +
                "Content=" + selectedArticle.getContent() + "\n";

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
                        ClipData mClipData = ClipData.newPlainText("FeedId", selectedArticle.getOriginStreamId());
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
//        KLog.e("loread", "保存文件被点击");
        if (selectedArticle.getSaveDir().equals(Api.SAVE_DIR_CACHE)) {
            if (selectedArticle.getStarState().equals(Api.ART_STARED)) {
                selectedArticle.setSaveDir(Api.SAVE_DIR_STORE);
            } else {
                selectedArticle.setSaveDir(Api.SAVE_DIR_BOX);
            }
            vSave.setText(getString(R.string.font_saved));
        } else {
            selectedArticle.setSaveDir(Api.SAVE_DIR_CACHE);
            vSave.setText(getString(R.string.font_unsave));
        }
        WithDB.i().saveArticle(selectedArticle);
    }

    // View view
    public void onReadabilityClick() {
        final String handledArticleId = selectedArticle.getId();
        swipeRefreshLayoutS.setRefreshing(true);
//        KLog.e("=====点击");
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
                swipeRefreshLayoutS.setRefreshing(false);
                Readability readability = new Gson().fromJson(response.body(), Readability.class);
                selectedWebView.loadData(StringUtil.getHtmlForDisplay(selectedArticle, readability.getContent()));
//                KLog.e("===== 更新");
            }

            @Override
            public void onError(Response<String> response) {
                ToastUtil.showLong(getString(R.string.fail_try));
                swipeRefreshLayoutS.setRefreshing(false);
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
//        } else if (Api.DISPLAY_READABILITY.equals(feed.getDisplayMode())) {
        } else if (Api.DISPLAY_READABILITY.equals(GlobalConfig.i().getDisplayMode(feed.getId()))) {
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
                FeedConfigDialog feedConfigerDialog = new FeedConfigDialog();
                feedConfigerDialog.setOnUnsubscribeFeedListener(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        if (!response.body().equals("OK")) {
                            this.onError(response);
                            return;
                        }
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
                openLink(selectedArticle.getCanonical());
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


//    AgentWeb agentWeb;

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
//            KLog.i("【 ReadState 】" + WithDB.i().getArticle(selectedArticle.getId()).getReadState());
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
//        KLog.i("=====position" + position);
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
         * 所以，为了能在运行完 InstantiateItem ，有了 selectedWebView 之后再去执行 initSelectedPage 在首次进入时都不执行，放到 InstantiateItem 中。
         */
        @Override
        public void onPageSelected(final int pagePosition) {
            // 当知道页面被选中后：1.检查webView是否已经生成好了，若已生成则初始化所有数据（含页面的icon）；2.检查webView是否加载完毕，完毕则初始化懒加载
            initSelectedPage(pagePosition);
        }
    }

    public void initSelectedPage(int position) {
        swipeRefreshLayoutS.setRefreshing(false);
        initSelectedArticle(position);
        initSelectedWebView(position);
//        KLog.e("initSelectedPage结束。位置：" + position + "   " + selectedWebView);
    }


    public void initSelectedArticle(int position) {
        // 取消之前那篇文章的图片下载
        if (null != selectedArticle) {
            OkGo.cancelTag(articleImgHttpClient, selectedArticle.getId());
        }

        this.selectedArticle = App.articleList.get(position);
        selectedPosition = position;
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
            }, 128);
            return;
        }
        this.selectedWebView = viewPager.findViewById(position);
        for (int i = 0, size = App.i().mWebViewCaches.size(); i < size; i++) {
            if (App.i().mWebViewCaches.get(i).getId() != position) {
                // 先重载webview再暂停webview，这时候才真正能够停掉网页中正在播放de音视频，api 2.3.3 以上才能暂停
                App.i().mWebViewCaches.get(i).reload();
                App.i().mWebViewCaches.get(i).onPause();
//                KLog.e("暂停其他的webview = " + selectedWebView);
            }
        }
        initSelectedWebViewContent(position);
    }

    VideoImpl video;
    // （webview在实例化后，可能还在渲染html，不一定能执行js）
    private void initSelectedWebViewContent(final int position) {
//        KLog.e("初始WebView = " + mWebViewLoadedJs.get(position));
        // 对于已经加载过的webview做过滤，防止重复加载
        if (mWebViewLoadedJs.get(position)) {
            return;
        }

        // 增加Javascript异常监控
//        CrashReport.setJavascriptMonitor(selectedWebView, true);

        selectedWebView.setWebViewClient(new WebViewClientX());
//        // 初始化视频处理类
        video = new VideoImpl(ArticleActivity.this, selectedWebView);
//        selectedWebView.setVideoState(video);
        selectedWebView.setWebChromeClient(new WebChromeClientX(video));

        Article article = App.articleList.get(position);
        Feed feed = WithDB.i().getFeed(article.getOriginStreamId());
        // 直接在本窗口打开原链接
//        if (feed != null && Api.DISPLAY_LINK.equals(feed.getDisplayMode())) {
//            selectedWebView.loadUrl(article.getCanonical());
//        }
        if (feed != null && Api.DISPLAY_LINK.equals(GlobalConfig.i().getDisplayMode(feed.getId()))) {
            selectedWebView.loadUrl(article.getCanonical());
        } else {
//            KLog.e("执行加载被选择的webview：" + selectedWebView );
            // 尝试执行一次，但是可能因为渲染html比较慢，导致setupImage没有真正执行。其实主要是在js文件加载中，直接调用tryInitJs，并且只初始化当前页面的setupImage
            tryInitJs(article.getId());
        }
        selectedWebView.requestFocus();
        mWebViewLoadedJs.put(position, true);
//        KLog.e("触发初始化" );
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
    }


    private class ViewPagerAdapter extends PagerAdapter {
        private ArticleActivity activity;
        private List<Article> dataList;

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
//                KLog.e("WebView" , "复用" + selectedWebView);
            } else {
                webView = new WebViewS(activity);
//                KLog.e("WebView" , "创建" + selectedWebView);
            }

//            webView.setWebViewClient(new WebViewClientX());
//            VideoImpl video = new VideoImpl(ArticleActivity.this,webView);
//            webView.setVideoState(video);
//            webView.setWebChromeClient( new WebChromeClientX( video ) );
            // 原本想放在选择 webview 页面的时候去加载，但可能由于那时页面内容已经加载所以无法设置下面这个JSInterface？
            webView.addJavascriptInterface(activity, "ImageBridge");


            // 不能在这里加，不然一进该页面，iframe 类的 video 标签视频就会弹出下载框
            // webView.setDownloadListener(new DownloadListenerS(ArticleActivity.this));

            // 方便在其他地方调用 viewPager.findViewById 来找到 selectedWebView
            webView.setId(position);
            container.addView(webView);
            mWebViewLoadedJs.put(position, false);
//            time = System.currentTimeMillis();
//            KLog.e("加载webview文章", "开始生成1：" +  selectedWebView.getId() + "  " + dataList.get(position).getTitle() );

            // 检查该订阅源默认显示什么。【RSS，已读，保存的网页，原始网页】
            Feed feed = WithDB.i().getFeed(dataList.get(position).getOriginStreamId());
            // 填充加载中视图
//            if (feed != null && Api.DISPLAY_LINK.equals(feed.getDisplayMode())) {
            if (feed != null && Api.DISPLAY_LINK.equals(GlobalConfig.i().getDisplayMode(feed.getId()))) {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadData("");
                    }
                });
            } else {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadData(StringUtil.getHtmlForDisplay(dataList.get(position)));
//                        webView.loadUrl("https://jd.com");
                    }
                });
            }

            KLog.e("执行生成webview：" + webView + "  " + dataList.get(position).getTitle());
//            KLog.e("加载webview文章", "开始生成2：" +  selectedWebView.getId() + "  " + dataList.get(position).getTitle() + "  耗时：" + ( System.currentTimeMillis() - time) );
//        selectedWebView.loadUrl("http://player.bilibili.com/player.html?aid=25052736&cid=42366510");
//            selectedWebView.loadUrl("https://www.bilibili.com/blackboard/html5mobileplayer.html?aid=25052736&cid=42365557");
//        selectedWebView.loadUrl("http://v.qq.com/iframe/player.html?vid=o0318tp1ddw&tiny=0&auto=0");
//        selectedWebView.loadUrl("http://www.iqiyi.com/");
//        selectedWebView.loadUrl("http://tv.sohu.com/20140508/n399272261.shtml"); // 搜狐自带可以全屏播放的js
//        selectedWebView.loadUrl("http://m.youku.com/video/id_XODEzMjU1MTI4.html");
            return webView;
        }


        // Note: 销毁预加载以外的view对象, 会把需要销毁的对象的索引位置传进来就是position
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (object == null) {
                return;
            }
            container.removeView((View) object);
            mWebViewLoadedJs.delete(position);
            ((WebViewS) object).destroy();
            App.i().mWebViewCaches.add(new WebViewS(new MutableContextWrapper(App.i())));
        }

        @Override
        public int getCount() {
            return (null == dataList) ? 0 : dataList.size();
        }
    }

    private class WebChromeClientX extends WebChromeClient {
        VideoImpl video;

        WebChromeClientX(VideoImpl video) {
            this.video = video;
        }
        @Override
        public void onProgressChanged(WebView webView, int progress) {
            if (webView == ArticleActivity.this.selectedWebView && slowlyProgressBar != null) {
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


    private class WebViewClientX extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
//          【scheme链接打开本地应用】（https://www.jianshu.com/p/45af72036e58）
            //http和https协议开头的执行正常的流程
            if (url.startsWith("http") || url.startsWith("https")) {
                openLink(url);
                OkGo.cancelAll(articleImgHttpClient);
                return true;
            }

            //其他的URL则会开启一个Acitity然后去调用原生APP
            final Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            if (in.resolveActivity(getPackageManager()) == null) {
                // TODO: 2018/4/25  说明系统中不存在这个activity。弹出一个Toast提示是否要用外部应用打开
                KLog.e("本地未安装能打开scheme链接的应用");
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
        public void onPageStarted(WebView selectedWebView, String url, Bitmap favicon) {
            super.onPageStarted(selectedWebView, url, favicon);
            KLog.e("加载webview文章", "开始加载：" + selectedWebView.getId() + "  耗时：");
            if (slowlyProgressBar != null) {
                slowlyProgressBar.onProgressStart();
            }
        }
//        @Override
//        public void onPageFinished( WebView selectedWebView, String url) {
//            super.onPageFinished(selectedWebView, url);
//            if(!selectedWebView.getSettings().getLoadsImagesAutomatically()) {
//                selectedWebView.getSettings().setLoadsImagesAutomatically(true);
//            }
////            KLog.e("加载webview文章", "加载结束：" +  selectedWebView.getId()  + "  耗时：" + ( System.currentTimeMillis() - time) );
//            // 不能直接在这里就初始化了，因为在viewpager中预加载而生成webview的时候，这里的懒加载就被触发了
////            selectedWebView.loadUrl("javascript:setTimeout(\"setupImage()\",100)");
//        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        KLog.e("按下回退键"  );
        // 后者为短期内按下的次数
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if (video.isPlaying()) {
                video.onHideCustomView();
                // 返回真表示返回键被屏蔽掉
                return true;
            }
            Intent data = new Intent();
            data.putExtra("articleNo", viewPager.getCurrentItem());
            //注意下面的RESULT_OK常量要与回传接收的Activity中onActivityResult（）方法一致
            this.setResult(Api.ActivityResult_ArtToMain, data);
            this.finish();
            overridePendingTransition(R.anim.fade_in, R.anim.out_from_bottom);
            // 返回真表示返回键被屏蔽掉
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
//                .textColor(R.id.article_bottombar_tag, R.attr.bottombar_fg)
                .textColor(R.id.art_bottombar_save, R.attr.bottombar_fg);
        return mColorfulBuilder;
    }

    private OkHttpClient articleImgHttpClient;

    public void buildImgClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(60000L, TimeUnit.MILLISECONDS);
        builder.writeTimeout(60000L, TimeUnit.MILLISECONDS);
        builder.connectTimeout(30000L, TimeUnit.MILLISECONDS);
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        builder.hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier);
        articleImgHttpClient = builder.build();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //监听左上角的返回箭头
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.out_from_bottom);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


//    public void onTagClick(View view) {
//        final List<Tag> tagsList = WithDB.i().getTags();
//        ArrayList<String> tags = new ArrayList<>(tagsList.size());
//        for (Tag tag : tagsList) {
//            tags.add(tag.getTitle());
//        }
//        new MaterialDialog.Builder(this)
//                .title(R.string.article_choose_tag_dialog_title)
//                .items(tags)
//                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
//                    @Override
//                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
//                        String tagId = tagsList.get(which).getId();
//                        StringBuilder newCategories = new StringBuilder(selectedArticle.getCategories().length());
//                        String[] cateArray = selectedArticle.getCategories().replace("]", "").replace("[", "").split(",");
//                        StringBuilder tempCate = new StringBuilder();
//                        for (String category : cateArray) {
//                            tempCate.append(tempCate);
//                            if (category.contains("user/" + App.UserID + "/label/")) {
//                                if (category.substring(0, 1).equals("\"")) {
//                                    category = category.substring(1, category.length());
//                                }
//                                if (category.substring(category.length() - 1, category.length()).equals("\"")) {
//                                    category = category.substring(0, category.length() - 1);
//                                }
//                                DataApi.i().articleRemoveTag(selectedArticle.getId(), category, null);
//
//                                KLog.i("【-】" + category);
//                            } else {
//                                newCategories.append(category);
//                                newCategories.append(", ");
//                            }
//                        }
//                        newCategories.append(tagId);
//                        newCategories.append("]");
//                        KLog.i("【==】" + newCategories + selectedArticle.getId());
//                        selectedArticle.setCategories(newCategories.toString());
//                        DataApi.i().articleAddTag(selectedArticle.getId(), tagId, null);
//                        dialog.dismiss();
//                        return true; // allow selection
//                    }
//                })
//                .show();
//    }
}
