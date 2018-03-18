package me.wizos.loread.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import com.socks.library.KLog;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.wizos.loread.App;
import me.wizos.loread.BuildConfig;
import me.wizos.loread.R;
import me.wizos.loread.adapter.ExpandableListAdapterS;
import me.wizos.loread.adapter.MaterialSimpleListAdapter;
import me.wizos.loread.adapter.MaterialSimpleListItem;
import me.wizos.loread.bean.gson.Readability;
import me.wizos.loread.common.ImageBridge;
import me.wizos.loread.data.PrefUtils;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.Tag;
import me.wizos.loread.net.Api;
import me.wizos.loread.net.DataApi;
import me.wizos.loread.net.MercuryApi;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.HttpUtil;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.utils.ToastUtil;
import me.wizos.loread.utils.Tool;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.SwipeRefreshLayoutS;
import me.wizos.loread.view.ViewPagerS;
import me.wizos.loread.view.WebViewX;
import me.wizos.loread.view.colorful.Colorful;
import okhttp3.OkHttpClient;

@SuppressLint("SetJavaScriptEnabled")
public class ArticleActivity extends BaseActivity implements View.OnClickListener, ImageBridge {
    protected static final String TAG = "ArticleActivity";

    private WebViewX webView;
    private IconFontView vStar, vRead, vSave;
    private SwipeRefreshLayoutS swipeRefreshLayoutS;
    private TextView vArticleNum;
    private Article article;
    private ViewPagerS viewPager;
    private String articleID = "";
    private OkHttpClient imgHttpClient;
    private int articleNo, articleCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        App.artHandler = articleHandler;
        imgHttpClient = buildImgClient();


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

    private void initView() {
        vStar = findViewById(R.id.article_bottombar_star);
        vRead = findViewById(R.id.article_bottombar_read);
        vSave = findViewById(R.id.art_bottombar_save);
        vArticleNum = findViewById(R.id.art_toolbar_num);
//        vReadability = findViewById(R.id.art_bottombar_readability);
        swipeRefreshLayoutS = findViewById(R.id.art_swipe_refresh);
        swipeRefreshLayoutS.setEnabled(false);
        if (!BuildConfig.DEBUG) {
            vSave.setVisibility(View.GONE);
//            vReadability.setVisibility(View.GONE);
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
    }

    ViewPagerAdapter viewPagerAdapter;

    public void initViewPager() {
        viewPager = findViewById(R.id.art_viewpager);
        viewPager.clearOnPageChangeListeners();
        Tool.setBackgroundColor(viewPager);
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
        WebViewX webViewX;
        for (int i = 0; i < viewPager.getChildCount(); i++) {
            // 使用自己包装的 webview
            webViewX = (WebViewX) viewPager.getChildAt(i);
//            webViewX.destroy();
            webViewX.clear();
            App.i().mWebViewCaches.add(webViewX);
        }
        viewPager.removeAllViews();
        viewPager.clearOnPageChangeListeners();
        OkGo.cancelAll(imgHttpClient);

        super.onDestroy();
    }


//    /**
//     * 在JS中调用该方法
//     * @param imgNo 被点图片的序号
//     * @param src 被点图片的当前加载网址（内部的）
//     * @param netsrc  被点图片的原始下载网址
//     */
//    @JavascriptInterface
//    public void onImgClicked(final int imgNo, final String src, final String netsrc) {
//        // 下载图片
//        // 打开大图
//        KLog.e("图片被点击");
//        // TODO: 2018/3/4 不要用对话框，改为点击（初始，失败，无图）占位图就下载，正常图片就用内置的图片浏览器打开大图
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                // 不能使用 ApplicationContext，必须是 ActivityContext
//                final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter( ArticleActivity.this);
//                adapter.add(new MaterialSimpleListItem.Builder(ArticleActivity.this)
//                        .content(R.string.art_img_dialog_download_again)
//                        .icon(R.drawable.dialog_ic_redownload)
//                        .backgroundColor(Color.WHITE)
//                        .build());
//                adapter.add(new MaterialSimpleListItem.Builder(ArticleActivity.this)
//                        .content(R.string.art_img_dialog_open_in_new_window)
//                        .icon(R.drawable.dialog_ic_open_in_new_window)
//                        .backgroundColor(Color.WHITE)
//                        .build());
//                adapter.add(new MaterialSimpleListItem.Builder(ArticleActivity.this)
//                        .content(R.string.art_img_dialog_copy_link)
//                        .icon(R.drawable.dialog_ic_copy_link)
//                        .backgroundColor(Color.WHITE)
//                        .build());
//
//                try{
//                    new MaterialDialog.Builder(ArticleActivity.this)
//                            .adapter(adapter, new MaterialDialog.ListCallback() {
//                                @Override
//                                public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
//                                    switch (which) {
//                                        case 0:
//                                            KLog.e("重新下载：" + imgNo + " = " + src);
//                                            // 此时还要判断他的储存位置 是 box 还是 cache
//                                            restartDownloadImg(imgNo, src);
//                                            break;
//                                        case 1:
////                                        try {
//                                            String imgPath = FileUtil.getRelativeDir(article.getSaveDir()) + fileTitle + "_files" + File.separator + WithDB.i().getImg(articleID, imgNo + 1).getName();
//                                            File file = new File(imgPath);
//                                            KLog.e("打开大图：" + imgPath);
//                                            if (file.isFile()) {
//                                                Intent intent = new Intent();
//                                                intent.setAction(Intent.ACTION_VIEW);
//                                                intent.setDataAndType(Uri.fromFile(file), "image/*");
//                                                ArticleActivity.this.startActivity(Intent.createChooser(intent, "选择打开方式"));
//                                            }
////                                        }catch (Exception e){
////                                            ToastUtil.showLong("打开图片失败");
////                                        }
//                                            break;
//                                        case 2:
//                                            // 获取系统剪贴板
//                                            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
//                                            // 创建一个剪贴数据集，包含一个普通文本数据条目（需要复制的数据）
//                                            ClipData clipData = ClipData.newPlainText(null, netsrc);
//                                            // 把数据集设置（复制）到剪贴板
//                                            clipboard.setPrimaryClip(clipData);
//                                            KLog.e("图片链接复制成功" + src + " = " + netsrc);
//                                            ToastUtil.showShort(getString(R.string.toast_copy_img_src_success));
//                                            break;
//                                        default:
//                                            break;
//                                    }
//
//                                    dialog.dismiss();
//                                }
//                            })
//                            .show();
//                }catch (Exception e){
//                }
//            }
//        });
//    }


//    private void restartDownloadImg(int imgNo, final String url) {
//        imgNo = imgNo + 1 ;
//        KLog.e("loread", "restartDownloadImg" + imgNo);
//        Img imgMeta;
//        imgMeta = WithDB.i().getImg(articleID, imgNo);
//        if (imgMeta == null) {
//            ToastUtil.showShort(getString(R.string.toast_cant_find_img));
//            return;
//        }
//
//        String savePath = FileUtil.getRelativeDir(article.getSaveDir()) + fileTitle + "_files" + File.separator + imgMeta.getName();
//        KLog.i("图片的保存目录为：" + savePath + "  下载地址为：" + url );
//
//        // 先替换为加载中的占位图
//        webView.post(new Runnable() {
//            @Override
//            public void run() {
//                webView.loadUrl("javascript:onImageLoading('" + url + "');");
//            }
//        });
//
////        DataApi.i().downingImg(articleHandler, imgHttpClient, imgMeta, FileUtil.getRelativeDir(article.getSaveDir()) + fileTitle + "_files" + File.separator);
//        ImgDownloader imgDownloader = new ImgDownloader ( articleHandler, imgHttpClient, imgMeta, FileUtil.getRelativeDir(article.getSaveDir()) + fileTitle + "_files" + File.separator );
//        imgDownloader.go();
//    }


//    private void imgLoadSuccess(final String imgSrc, final String localSrc) {
////        KLog.e("loread", "替换imgLoadSuccess" + article.getSaveDir() + article.getTitle() + "：" + localSrc + "=" + webView);
//        if (null == webView) {
//            Tool.showShort("imgLoadSuccess此时webView尽然为空？");
//        } else {
//            webView.post(new Runnable() {
//                @Override
//                public void run() {
//                    webView.loadUrl("javascript:onImageLoadSuccess('" + imgSrc + "','" + localSrc + "');");
//                }
//            }); // , webView.getDelayTime()
//        }
//    }


    @JavascriptInterface
    @Override
    public void log(String paramString) {
        KLog.e("ImageBridge", "【log】" + paramString);
    }

    @JavascriptInterface
    @Override
    public void loadImage2(String articleId, String url, final String originalUrl) {
        // 去掉已经缓存了图片的 url
        KLog.e("ImageBridge1", "【log】" + articleId + "  " + url + "  " + originalUrl);
        if (!url.startsWith("file:///android_asset/") || TextUtils.isEmpty(articleId) || !articleId.equals(articleId)) {
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
        } else if (PrefUtils.i().isDownImgWifi() && !HttpUtil.isWiFiUsed()) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:onImageLoadNeedClick('" + originalUrl + "')");
                }
            });
        } else {
            downImage(articleId, originalUrl);
        }
    }

    @JavascriptInterface
    @Override
    public void openImage(String articleId, String urls, int index) {
        KLog.e("ImageBridge", "打开图片");
    }


    @JavascriptInterface
    @Override
    public void downImage(String articleId, final String originalUrl) {
        KLog.e("开始下载图片：" + articleId + " - " + originalUrl);
        final String filePath = App.externalFilesDir + "cache/" + StringUtil.stringToMD5(articleId) + "_files/";
        final String fileNameExt = StringUtil.getFileNameExtByUrl(originalUrl);
        FileCallback fileCallback = new FileCallback(filePath, fileNameExt + ".temp") {
            @Override
            public void onSuccess(Response<File> response) {
                new File(filePath + fileNameExt + ".temp").renameTo(new File(filePath + fileNameExt));
                KLog.e("下载成功：" + originalUrl + " - " + viewPager.findViewById(selectedPos) + " - " + filePath + fileNameExt);
                if (selectedPos != -1 && viewPager.findViewById(selectedPos) != null) {
                    ((WebViewX) viewPager.findViewById(selectedPos)).loadUrl("javascript:onImageLoadSuccess('" + originalUrl + "','" + filePath + fileNameExt + "')");
                }
            }

            // 该方法执行在主线程中
            @Override
            public void onError(Response<File> response) {
                KLog.e("下载失败：" + originalUrl + " - " + viewPager.findViewById(selectedPos) + " - " + filePath + fileNameExt);
                if (selectedPos != -1 && viewPager.findViewById(selectedPos) != null) {
                    ((WebViewX) viewPager.findViewById(selectedPos)).loadUrl("javascript:onImageLoadFailed('" + originalUrl + "')");
                }
                new File(filePath + fileNameExt + ".temp").delete();
            }
        };

        OkGo.<File>get(originalUrl)
                .tag(articleId)
                .client(imgHttpClient)
                .execute(fileCallback);
    }




    private int tapCount = 0;
    @Override
    public void onClick(View v) {
        KLog.e("loread", "【 toolbar 是否双击 】" + v);
        switch (v.getId()) {
            // debug
            case R.id.art_toolbar_num:
            case R.id.art_toolbar:
                break;
            default:
                break;
        }
    }


    public void debugArticleInfo(View view) {
        KLog.e("文章信息" + tapCount);
        if (tapCount == 0) {
            articleHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    tapCount = 0;
                }
            }, 1300);
        } else if (tapCount > 2) {
            showArticleInfo(article);
        }
        tapCount++;
    }

    public void showArticleInfo(Article article) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        article = WithDB.i().getArticle(article.getId());
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
                "Summary=" + article.getSummary() + "\n";
        new MaterialDialog.Builder(this)
                .title(R.string.article_about_dialog_title)
                .content(info)
                .positiveText(R.string.agree)
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

    public ArticleHandler articleHandler = new ArticleHandler(this);

    // 【笔记】
    // 问题：内部类 Handler 持有外部类的对象，容易导致 Activity 无法回收，造成内存泄漏。
    // 解决方法：静态类不持有外部类的对象，所以 Activity 可以随意被回收，不容易造成内存泄漏

    /**
     * 该 handler 用途：根据下载结果，更新文章的占位图
     */
    private static class ArticleHandler extends Handler {
        private final WeakReference<ArticleActivity> mActivity;

        ArticleHandler(ArticleActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(final Message msg) {
//            KLog.e("【handler】" + msg.what + mActivity.get());
            // 返回引用对象的引用
            if (mActivity.get() == null) {
                return;
            }

            switch (msg.what) {
//                case Api.S_BITMAP:
//                    String articleID = msg.getData().getString("articleID");
//                    String imgName = msg.getData().getString("imgName");
//                    // 如果图片下载成功，且该图片来源的文章 就是 当前还在阅读的文章，则进行更新图片
//                    if (App.currentArticleID != null & App.currentArticleID.equals(articleID)) {
//                        mActivity.get().imgLoadSuccess(msg.getData().getString("imgSrc"), "./" + fileTitle + "_files" + File.separator + imgName);
//                    }
////                    KLog.e("【图片】" + msg.getData().getString("imgSrc") + "=" + articleID + "=" + msg.getData().getInt("imgNo") + "=" + articleID);
//                    break;
//                case Api.F_BITMAP:
//                    articleID = msg.getData().getString("articleID");
//                    // 如果图片下载失败，且该图片来源的文章 就是 当前还在阅读的文章，则进行更新图片
//                    if (App.currentArticleID != null & App.currentArticleID.equals(articleID)) {
//                        if (null == mActivity.get().webView) {
//                            Tool.showShort("此时webView尽然为空？");
//                        } else {
//                            mActivity.get().webView.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    mActivity.get().webView.loadUrl("javascript:onImageLoadFailed('" + msg.getData().getString("imgSrc") + "');");
//                                }
//                            });
//                        }
//                    }
//                    break;
                case Api.INIT_IMAGWE_BRIDGE:
                    try {
                        KLog.e("当前的加载进度为：" + mActivity.get().webView.getProgress());
                    } catch (Exception e) {
                        KLog.e("当前的加载进度为：null");
                    }

                    this.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mActivity.get().webView != null && mActivity.get().webView.getProgress() >= 80) {
                                mActivity.get().webView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        KLog.e("ImageBridge", "触发初始化");
                                        mActivity.get().webView.loadUrl("javascript:setupImage('" + mActivity.get().article.getId() + "')");
                                    }
                                });
                            } else {
                                mActivity.get().articleHandler.sendEmptyMessage(Api.INIT_IMAGWE_BRIDGE);
                            }
                        }
                    }, 500);
                    break;
                default:
                    break;
            }
        }
    }


    public void onTagClick(View view) {
        final List<Tag> tagsList = WithDB.i().getTags();
        ArrayList<String> tags = new ArrayList<>(tagsList.size()) ;
        for( Tag tag: tagsList ) {
            tags.add(tag.getTitle());
        }
        new MaterialDialog.Builder(this)
                .title(R.string.article_choose_tag_dialog_title)
                .items( tags )
                .itemsCallbackSingleChoice( -1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        String tagId = tagsList.get(which).getId();
                        StringBuilder newCategories = new StringBuilder(  article.getCategories().length()  );
                        String[] cateArray = article.getCategories().replace("]", "").replace("[", "").split(",");
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
                                DataApi.i().articleRemoveTag(articleID, category, null);

                                KLog.i("【-】" + category);
                            }else {
                                newCategories.append(category);
                                newCategories.append(", ");
                            }
                        }
                        newCategories.append(tagId);
                        newCategories.append("]");
                        KLog.i("【==】" + newCategories + articleID);
                        article.setCategories( newCategories.toString() );
                        DataApi.i().articleAddTag(articleID, tagId, null);
                        dialog.dismiss();
                        return true; // allow selection
                    }
                })
                .show();
    }



    public void onStarClick(View view) {
//        articleStateManager.changeStarState(article);
        if (article.getStarState().equals(Api.ART_UNSTAR)) {
            vStar.setText(getString(R.string.font_stared));
            DataApi.i().markArticleStared(articleID, null);
            article.setStarState(Api.ART_STARED);
            article.setStarred(System.currentTimeMillis() / 1000);
            if (article.getSaveDir().equals(Api.SAVE_DIR_BOX) || article.getSaveDir().equals(Api.SAVE_DIR_BOXREAD)) {
                article.setSaveDir(Api.SAVE_DIR_STORE);
            }
        } else {
            vStar.setText(getString(R.string.font_unstar));
            DataApi.i().markArticleUnstar(articleID, null);
            article.setStarState(Api.ART_UNSTAR);
            if (article.getSaveDir().equals(Api.SAVE_DIR_STORE) || article.getSaveDir().equals(Api.SAVE_DIR_STOREREAD)) {
                article.setSaveDir(Api.SAVE_DIR_BOX);
            }
        }
        WithDB.i().saveArticle(article);
    }

    public void onReadClick(View view) {
//        articleStateManager.changeReadState(article);
        KLog.e("loread", "被点击的是：" + article.getTitle());
        if (article.getReadState().equals(Api.ART_READED)) {
            vRead.setText(getString(R.string.font_unread));
            DataApi.i().markArticleUnread(articleID, null);
            article.setReadState(Api.ART_UNREADING);
        } else {
            vRead.setText(getString(R.string.font_readed));
            DataApi.i().markArticleReaded(articleID, null);
            article.setReadState(Api.ART_READED);
        }
        WithDB.i().saveArticle(article);
    }
    public void onSaveClick(View view){
        KLog.e("loread", "保存文件被点击");
        if (article.getSaveDir().equals(Api.SAVE_DIR_CACHE)) {
            if (article.getStarState().equals(Api.ART_STARED)) {
                article.setSaveDir(Api.SAVE_DIR_STORE);
            } else {
                article.setSaveDir(Api.SAVE_DIR_STORE);
            }
            WithDB.i().saveArticle(article);
        } else {
            ToastUtil.showShort(getString(R.string.toast_file_saved));
//            moveArticleTo( article.getSaveDir(), Api.SAVE_DIR_CACHE ); // 暂时不能支持从 box/store 撤销保存回 cache，因为文章的正文是被加了修饰的，为 epub 文件做准备的
        }
        if (article.getSaveDir().equals(Api.SAVE_DIR_CACHE)) {
            vSave.setText(getString(R.string.font_unsave));
        } else {
            vSave.setText(getString(R.string.font_saved));
        }
    }

    // View view
    public void onReadabilityClick() {
        final String handledArticleId = article.getId();
        swipeRefreshLayoutS.setRefreshing(true);
        KLog.e("=====点击");
        ToastUtil.showLong(getString(R.string.toast_get_readability_ing));
        MercuryApi.fetchReadabilityContent(article.getCanonical(), new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if (!response.isSuccessful()) {
                    return;
                }
                if (handledArticleId != article.getId()) {
                    return;
                }
                swipeRefreshLayoutS.setRefreshing(false);
                Readability readability = new Gson().fromJson(response.body(), Readability.class);

//                webView.loadDataWithBaseURL(FileUtil.getAbsoluteDir(article.getSaveDir()), StringUtil.getArticleHeader(article) + readability.getContent() + StringUtil.getArticleFooter(), "text/html", "utf-8", null);
                webView.setData(FileUtil.getAbsoluteDir(article.getSaveDir()), StringUtil.getArticleHeader(article) + readability.getContent() + StringUtil.getArticleFooter());

//                String articleHtml = readability.getContent();
//                String sourceFileTitle;
//                if (article.getSaveDir().equals(Api.SAVE_DIR_CACHE)) {
//                    sourceFileTitle = StringUtil.stringToMD5(article.getId());
//                } else {
//                    sourceFileTitle = article.getTitle();
//                }
//                String sourceDirPath = FileUtil.getRelativeDir(article.getSaveDir());
//                FileUtil.saveHtml(sourceDirPath + sourceFileTitle + ".html", articleHtml);
//                KLog.e("=====" + sourceDirPath + sourceFileTitle + ".html");
//                article.setImgState(null);
//                WithDB.i().saveArticle(article);

                KLog.e("===== 更新");
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
        IconFontView feedConfig = dialog.findViewById(R.id.feed_config_icon);
        IconFontView openLinkByBrowser = dialog.findViewById(R.id.open_link_by_browser_icon);
        IconFontView getReadability = dialog.findViewById(R.id.get_readability_icon);
        IconFontView nightTheme = dialog.findViewById(R.id.article_night_theme_icon);
        TextView getReadabilityTitle = dialog.findViewById(R.id.get_readability_title);
        TextView nightThemeTitle = dialog.findViewById(R.id.article_night_theme_title);
//        ImageView feedLogo = dialog.findViewById(R.id.feed_logo);
//        TextView feedTitle = dialog.findViewById(R.id.feed_title);
//        TextView feedSummary = dialog.findViewById(R.id.feed_summary);
        final Feed feed = WithDB.i().getFeed(article.getOriginStreamId());
//        Glide.with(this).load(feed.getIconurl()).placeholder(R.mipmap.ic_launcher).into(feedLogo);
//        feedTitle.setText(feed.getTitle());
//        feedSummary.setText(feed.getUrl());
        if (Api.OPEN_READABILITY.equals(feed.getOpenMode())) {
            getReadability.setTextColor(getResources().getColor(R.color.colorPrimary));
            getReadabilityTitle.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
        if (App.theme_Night == PrefUtils.i().getThemeMode()) {
            nightTheme.setTextColor(getResources().getColor(R.color.colorPrimary));
            nightThemeTitle.setTextColor(getResources().getColor(R.color.colorPrimary));
        }


        dialog.show();

        feedConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                ToastUtil.showShort("配置该订阅源");
            }
        });
        openLinkByBrowser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
//                Intent intent = new Intent();
//                intent.setAction(android.content.Intent.ACTION_VIEW);
////                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.setData(Uri.parse(article.getCanonical()));
//                // 目前无法坐那那种可以选择打开方式的
//                ArticleActivity.this.startActivity(Intent.createChooser(intent, "选择打开方式"));
                Feed feedx = feed;
                if (feed.getOpenMode().equals(Api.OPEN_RSS)) {
                    feedx.setOpenMode(Api.OPEN_LINK);
                } else {
                    feedx.setOpenMode(Api.OPEN_RSS);
                }

                WithDB.i().updateFeed(feedx);
            }
        });

        getReadability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                onReadabilityClick();
            }
        });

        nightTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manualToggleTheme();
                dialog.dismiss();

            }
        });
    }


//    private void moveArticleTo(Article article, String targetDir) {
//        String sourceFileTitle;
//        String targetFileTitle;
//        String articleHtml;
//        String sourceDirPath = FileUtil.getRelativeDir(article.getSaveDir());
//        String targetDirPath = FileUtil.getRelativeDir(targetDir);
//
//        if (article.getSaveDir().equals(Api.SAVE_DIR_CACHE)) {
//            sourceFileTitle = StringUtil.stringToMD5(article.getId());
//            articleHtml = FileUtil.readHtml(sourceDirPath + sourceFileTitle + ".html");
//            articleHtml = StringUtil.reviseHtmlForBox(article.getTitle(), articleHtml);
//            FileUtil.saveHtml(sourceDirPath + sourceFileTitle + ".html", articleHtml);
//            targetFileTitle = StringUtil.getOptimizedNameForSave(article.getTitle());
//            article.setTitle(targetFileTitle);
//        } else {
//            sourceFileTitle = article.getTitle();
//            targetFileTitle = sourceFileTitle;
//        }
//
//        FileUtil.moveFile(sourceDirPath + sourceFileTitle + ".html", targetDirPath + targetFileTitle + ".html");
//        FileUtil.moveDir(sourceDirPath + sourceFileTitle + "_files", targetDirPath + targetFileTitle + "_files");
//
////        KLog.i(sourceFileTitle);
////        KLog.d("原来文件夹" + sourceDirPath + sourceTitle + "_files");
////        KLog.d("目标文件夹" + targetDirPath + article.getTitle() + "_files");
//        if (!TextUtils.isEmpty(article.getCoverSrc())) {  // (lossSrcList != null && lossSrcList.size() != 0) || ( obtainSrcList!= null && obtainSrcList.size() != 0)
//            article.setCoverSrc(targetDirPath + article.getTitle() + "_files" + File.separator + StringUtil.getFileNameExtByUrl(article.getCoverSrc()));
////            KLog.d("封面" + targetDirPath + article.getTitle() + "_files" + File.separator + StringUtil.getFileNameExtByUrl(article.getCoverSrc()));
//        }
//        fileTitle = targetFileTitle;
//        article.setSaveDir(targetDir);
//        WithDB.i().saveArticle(article);
//    }


    public void showFeedConfigDialog(final ExpandableListAdapterS.ItemViewHolder itemView, final Feed feed) {

        // 重命名弹窗的适配器
        MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(ArticleActivity.this);
        adapter.add(new MaterialSimpleListItem.Builder(ArticleActivity.this)
                .content(R.string.main_tag_dialog_rename)
                .icon(R.drawable.dialog_ic_rename)
                .backgroundColor(Color.TRANSPARENT)
                .build());
        adapter.add(new MaterialSimpleListItem.Builder(ArticleActivity.this)
                .content(R.string.main_tag_dialog_unsubscribe)
                .icon(R.drawable.dialog_ic_unsubscribe)
                .backgroundColor(Color.TRANSPARENT)
                .build());
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { // 后者为短期内按下的次数
//            App.finishActivity(this);
            back();
            return true;//返回真表示返回键被屏蔽掉
        }
        return super.onKeyDown(keyCode, event);
    }

    private void back() {
        Intent data = new Intent();
        data.putExtra("articleNo", viewPager.getCurrentItem());
        ArticleActivity.this.setResult(Api.ActivityResult_ArtToMain, data);//注意下面的RESULT_OK常量要与回传接收的Activity中onActivityResult（）方法一致
        this.finish();
    }



    private void setIconState(int position) {
        if (article.getReadState().equals(Api.ART_UNREAD)) {
            vRead.setText(getString(R.string.font_readed));
            article.setReadState(Api.ART_READED);
            WithDB.i().saveArticle(article);
            DataApi.i().markArticleReaded(articleID, null);
            KLog.d("【 ReadState 】" + WithDB.i().getArticle(article.getId()).getReadState());
        } else if (article.getReadState().equals(Api.ART_READED)) {
            vRead.setText(getString(R.string.font_readed));
        } else if (article.getReadState().equals(Api.ART_UNREADING)) {
            vRead.setText(getString(R.string.font_unread));
        }

        if (article.getStarState().equals(Api.ART_UNSTAR)) {
            vStar.setText(getString(R.string.font_unstar));
        } else {
            vStar.setText(getString(R.string.font_stared));
        }
        if (article.getSaveDir().equals(Api.SAVE_DIR_CACHE)) {
            vSave.setText(getString(R.string.font_unsave));
        } else {
            vSave.setText(getString(R.string.font_saved));
        }

        vArticleNum.setText((position + 1) + " / " + articleCount);
        KLog.d("=====position" + position);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("articleNo", viewPager.getCurrentItem());
        outState.putInt("articleCount", articleCount);
        super.onSaveInstanceState(outState);
    }

    private OkHttpClient buildImgClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.readTimeout(60000L, TimeUnit.MILLISECONDS);
        builder.writeTimeout(60000L, TimeUnit.MILLISECONDS);
        builder.connectTimeout(30000L, TimeUnit.MILLISECONDS);
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        builder.hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier);
        return builder.build();
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
                .textColor(R.id.article_bottombar_tag, R.attr.bottombar_fg)
                .textColor(R.id.art_bottombar_save, R.attr.bottombar_fg);
        return mColorfulBuilder;
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
        swipeRefreshLayoutS.setRefreshing(false);
        initSelectedArticle(position);
        initSelectedWebView(position);
//        webView = (WebViewX) viewPager.findViewById(position);
//        viewPagerAdapter.onPause(); // pause all webviews
//        viewPagerAdapter.onResume(); // resume the current webview
        KLog.e("位置：" + position + "   " + webView);
    }

    private int selectedPos;
    public void initSelectedArticle(int position) {
        this.article = App.articleList.get(position);
        // 取消之前那篇文章的图片下载
        OkGo.cancelTag(imgHttpClient, articleID);
        KLog.e("loread", "initSelectedArticle" + article.getTitle() + "====" + webView);
        selectedPos = position;
        articleID = article.getId();
        App.currentArticleID = articleID;
        setIconState(position);
    }


    private final SparseArray<Boolean> mLoadedViews = new SparseArray<>();
    public void initSelectedWebView(final int position) {
        if (viewPager.findViewById(position) != null) { // && dataList.get(pagePosition).getImgState()==null
            this.webView = viewPager.findViewById(position);
//            if (viewPager.getCurrentItem() == webView.getId()) {
////                initWebViewLazyLoad();
//            } else {
//                KLog.e("loread", "----------选择的不是当前的webView");
//            }

            final Article article = ((Article) webView.getTag());
            Feed feed = WithDB.i().getFeed(article.getOriginStreamId());
            if (Api.OPEN_LINK.equals(feed.getOpenMode()) && !mLoadedViews.get(position)) {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        mLoadedViews.put(position, true);
                        webView.loadUrl(article.getCanonical());
                    }
                });
            }
        } else {
            articleHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initSelectedWebView(position);
                }
            }, 100);
        }
    }

    class ViewPagerAdapter extends PagerAdapter { //  implements ViewPager.OnPageChangeListener
        private ArticleActivity activity;
        private List<Article> dataList;
        private final SparseArray<WebViewX> mEntryViews = new SparseArray<>();

        public ViewPagerAdapter(ArticleActivity context, List<Article> dataList) {
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
            final WebViewX webView;
            if (App.i().mWebViewCaches.size() > 0) {
                webView = App.i().mWebViewCaches.get(0);
                App.i().mWebViewCaches.remove(0);
                KLog.e("WebView复用" + webView);
            } else {
                webView = new WebViewX(activity);
                KLog.e("WebView创建" + webView);
            }

            webView.setWebViewClient(new WebViewClientX());
            webView.addJavascriptInterface(activity, "ImageBridge");
            mEntryViews.put(position, webView);
            mLoadedViews.put(position, false);
            Tool.setBackgroundColor(webView);
            KLog.e("WebView" + webView);
            // 方便在其他地方调用 viewPager.findViewById 来找到 webView
            webView.setId(position);
            // 方便在webView onPageFinished 的时候调用
            webView.setTag(dataList.get(position));
            container.addView(webView);

            // TODO: 2018/3/11 检查该订阅源默认显示什么。同一个webview展示的内容有4种模式
            // 【RSS，已读，保存的网页，原始网页】
            // 由于是在 ViewPager 中使用 WebView，WebView 的内容要提前加载好，让ViewPager在左右滑的时候可见到左右的界面的内容。（但是这样是不是太耗资源了）


            Feed feed = WithDB.i().getFeed(dataList.get(position).getOriginStreamId());
            // 填充加载中视图
            if (Api.OPEN_LINK.equals(feed.getOpenMode())) {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.setData(FileUtil.getAbsoluteDir(dataList.get(position).getSaveDir()), "");
                    }
                });
            } else {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.setData(FileUtil.getAbsoluteDir(dataList.get(position).getSaveDir()), StringUtil.getArticleHeader(dataList.get(position)) + StringUtil.initContent(dataList.get(position)) + StringUtil.getArticleFooter());
                    }
                });
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
//            ((WebViewX) object).destroy();
            ((WebViewX) object).clear();
            mLoadedViews.delete(position);
            App.i().mWebViewCaches.add((WebViewX) object);
        }

        @Override
        public int getCount() {
            return (null == dataList) ? 0 : dataList.size();
        }

    }


    class WebViewClientX extends WebViewClient {
        //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
//            KLog.e("重写url" + request.getUrl());
////            if (PrefUtils.i().isSysBrowserOpenLink()) {
//                Intent intent = new Intent();
//                intent.setAction(android.content.Intent.ACTION_VIEW);
//                intent.setData(request.getUrl());
//                ArticleActivity.this.startActivity(Intent.createChooser(intent, "选择打开方式")); // 目前无法坐那那种可以选择打开方式的
//                return true;
////            }
////            return super.shouldOverrideUrlLoading(view, request);
//        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            Intent intent = new Intent();
//            intent.setAction(android.content.Intent.ACTION_VIEW);
//            intent.setData(Uri.parse(url));
//            ArticleActivity.this.startActivity(Intent.createChooser(intent, "选择打开方式")); // 目前无法坐那那种可以选择打开方式的
//            return true;

//            链接：https://www.jianshu.com/p/45af72036e58
            if (url.startsWith("http") || url.startsWith("https")) { //http和https协议开头的执行正常的流程
                return false;
            } else {  //其他的URL则会开启一个Acitity然后去调用原生APP
                Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                if (in.resolveActivity(getPackageManager()) == null) {
                    //说明系统中不存在这个activity
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showShort("应用未安装");
//                            view.loadUrl(failUrl);
                        }
                    });

                } else {
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    startActivity(in);
                }
                return true;
            }
        }

    }


}
