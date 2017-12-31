package me.wizos.loread.activity;

import android.annotation.SuppressLint;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.https.HttpsUtils;
import com.socks.library.KLog;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import me.wizos.loread.App;
import me.wizos.loread.BuildConfig;
import me.wizos.loread.R;
import me.wizos.loread.adapter.MaterialSimpleListAdapter;
import me.wizos.loread.adapter.MaterialSimpleListItem;
import me.wizos.loread.adapter.ViewPagerAdapter;
import me.wizos.loread.bean.Article;
import me.wizos.loread.bean.Img;
import me.wizos.loread.bean.Tag;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.net.Api;
import me.wizos.loread.net.DataApi;
import me.wizos.loread.net.MercuryApi;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.HttpUtil;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.utils.ToastUtil;
import me.wizos.loread.utils.Tool;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.ViewPagerS;
import me.wizos.loread.view.WebViewS;
import me.wizos.loread.view.colorful.Colorful;
import okhttp3.OkHttpClient;

@SuppressLint("SetJavaScriptEnabled")
public class ArticleActivity extends BaseActivity implements View.OnClickListener {
    protected static final String TAG = "ArticleActivity";
    private WebViewS webView;
    private IconFontView vStar, vRead, vSave, vReadability;
    //    private NestedScrollView vScrolllayout ; // 这个是为上级顶部，页面滑动至最顶层而做的。目前由于 webview 是动态添加，所以无法用到
    private TextView vArticleNum;
    private Article article;
    public ViewPagerS viewPager;
    private static String articleID = "";
    //    private static Neter artNeter;
    private OkHttpClient imgHttpClient;
    private static String fileTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        App.artHandler = artHandler;
        imgHttpClient = buildImgClient();
        initView(); // 初始化界面上的 View，将变量映射到布局上。

        if (savedInstanceState != null) {
            articleNo = savedInstanceState.getInt("articleNo"); // setSelection 没有滚动效果，直接跳到指定位置。smoothScrollToPosition 有滚动效果的
            articleCount = savedInstanceState.getInt("articleCount");
        } else {
            articleNo = getIntent().getExtras().getInt("articleNo"); // 文章在列表中的位置编号，下标从 0 开始
            articleCount = getIntent().getExtras().getInt("articleCount"); // 列表中所有的文章数目
        }
        KLog.d("开始初始化数据" + articleNo + "==" + articleCount);

        initViewPager();
    }

    private void initView() {
        initToolbar();
        vStar = (IconFontView) findViewById(R.id.art_bottombar_star);
        vRead = (IconFontView) findViewById(R.id.art_bottombar_read);
        vSave = (IconFontView) findViewById(R.id.art_bottombar_save);
        vArticleNum =  (TextView)findViewById(R.id.art_toolbar_num);
        vReadability = (IconFontView) findViewById(R.id.art_bottombar_readability);
        if (!BuildConfig.DEBUG) {
            vSave.setVisibility(View.GONE);
            vReadability.setVisibility(View.GONE);
        }
        vSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClick(v);
            }
        });
    }
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.art_toolbar);
        setSupportActionBar(toolbar); // ActionBar
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setOnClickListener(this);
//        白色箭头
//        Drawable upArrow = getResources().getDrawable(R.drawable.mz_ic_sb_back);
//        upArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
//        getSupportActionBar().setHomeAsUpIndicator(upArrow); // 替换返回箭头
    }


    @Override
    protected void onDestroy() {
        // 如果参数为null的话，会将所有的Callbacks和Messages全部清除掉。
        // 这样做的好处是在 Acticity 退出的时候，可以避免内存泄露。因为 handler 内可能引用 Activity ，导致 Activity 退出后，内存泄漏
        KLog.e("loread", "onDestroy" + webView);
        artHandler.removeCallbacksAndMessages(null);
        webView.destroy();
        WebViewS webViewS;
        for (int i = 0; i < viewPager.getChildCount(); i++) {
            // 使用自己包装的 webview
            webViewS = (WebViewS) viewPager.getChildAt(i);
            webViewS.destroy();
        }
        viewPager.removeAllViews();
        viewPager.clearOnPageChangeListeners();
        OkGo.cancelAll(imgHttpClient);
        super.onDestroy();
    }




    @JavascriptInterface
    public void log(String paramString) {
        KLog.d("loread", "【JSLog】" + paramString);
    }

    @JavascriptInterface
    public void loadImage(int imgNo, String url, String localUrl) {
        KLog.e("loread", "====================loadImage", WithDB.i().getImg(articleID, url).getDownState() + "【下载图片】" + imgNo);
        // 这么检测是有问题的，有些在数据库中已经被标记为已下，但是实际没有下的（标记为已读后），相关的图片那下载记录没有删
        if (WithDB.i().getImg(articleID, url).getDownState() != Api.ImgMeta_Downover) {
            restartDownloadImg(imgNo, url);
        } else {
            imgLoadSuccess(url, localUrl);
        }
    }

    @JavascriptInterface
    public boolean canLoadImage() {
        if (WithSet.i().isDownImgWifi() && !HttpUtil.isWiFiActive()) {
            KLog.e("loread", "Loread", "能否加载图片false1");
            return false;
        } else if (!WithSet.i().isDownImgWifi() && !HttpUtil.isNetworkAvailable()) {
            KLog.e("loread", "Loread", "能否加载图片false2");
            return false;
        }
        KLog.e("loread", "Loread", "能否加载图片true");
        return true;
    }

    // 在JS中调用该方法
    @JavascriptInterface
    public void onImgClicked(final int imgNo, final String src, final String netsrc) {
        // 下载图片
        // 打开大图
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter( ArticleActivity.this);
                adapter.add(new MaterialSimpleListItem.Builder(ArticleActivity.this)
                        .content(R.string.art_img_dialog_download_again)
                        .icon(R.drawable.dialog_ic_redownload)
                        .backgroundColor(Color.WHITE)
                        .build());
                adapter.add(new MaterialSimpleListItem.Builder(ArticleActivity.this)
                        .content(R.string.art_img_dialog_open_in_new_window)
                        .icon(R.drawable.dialog_ic_open_in_new_window)
                        .backgroundColor(Color.WHITE)
                        .build());
                adapter.add(new MaterialSimpleListItem.Builder(ArticleActivity.this)
                        .content(R.string.art_img_dialog_copy_link)
                        .icon(R.drawable.dialog_ic_copy_link)
                        .backgroundColor(Color.WHITE)
                        .build());

                new MaterialDialog.Builder(ArticleActivity.this)
                        .adapter(adapter, new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                switch (which) {
                                    case 0:
                                        KLog.e("重新下载：" + imgNo + " = " + src);
                                        // 此时还要判断他的储存位置 是 box 还是 cache
                                        restartDownloadImg(imgNo, src);
                                        break;
                                    case 1:
//                                        try {
                                        String imgPath = FileUtil.getRelativeDir(article.getSaveDir()) + fileTitle + "_files" + File.separator + WithDB.i().getImg(articleID, imgNo + 1).getName();
                                        File file = new File(imgPath);
                                        KLog.e("打开大图：" + imgPath);
                                        if (file.isFile()) {
                                            Intent intent = new Intent();
                                            intent.setAction(Intent.ACTION_VIEW);
                                            intent.setDataAndType(Uri.fromFile(file), "image/*");
                                            ArticleActivity.this.startActivity(Intent.createChooser(intent, "选择打开方式"));
                                        }
//                                        }catch (Exception e){
//                                            ToastUtil.showLong("打开图片失败");
//                                        }
                                        break;
                                    case 2:
                                        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                        cm.setText(netsrc);
                                        KLog.e("图片链接复制成功" + src + " = " + netsrc);
                                        ToastUtil.showShort(getString(R.string.toast_copy_img_src_success));
                                        break;
                                }

                                dialog.dismiss();
                            }
                        })
                        .show();

            }
        });
    }

    // 在JS中调用该方法
    @JavascriptInterface
    public void onImgClick2Down(final int imgNo, String src) {
        KLog.e(imgNo + " = " + src);
        restartDownloadImg(imgNo, src);
    }


    private void restartDownloadImg(int imgNo, final String url) {
        imgNo = imgNo + 1 ;
        KLog.e("loread", "restartDownloadImg" + imgNo);
        Img imgMeta;
        imgMeta = WithDB.i().getImg(articleID, imgNo);
        if (imgMeta == null) {
            ToastUtil.showShort(getString(R.string.toast_cant_find_img));
            return;
        }

        String savePath = FileUtil.getRelativeDir(article.getSaveDir()) + fileTitle + "_files" + File.separator + imgMeta.getName();
        KLog.i("图片的保存目录为：" + savePath + "  下载地址为：" + imgMeta.getSrc());
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:onImageLoading('" + url + "');");
            }
        });

        DataApi.i().downingImg(artHandler, imgHttpClient, imgMeta, FileUtil.getRelativeDir(article.getSaveDir()) + fileTitle + "_files" + File.separator);
    }


    private void imgLoadSuccess(final String imgSrc, final String localSrc) {
        KLog.e("loread", "替换imgLoadSuccess" + article.getSaveDir() + article.getTitle() + "：" + localSrc + "=" + webView);
        if (null == webView) {
            Tool.showOnLocal("imgLoadSuccess此时webView尽然为空？");
        } else {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl("javascript:onImageLoadSuccess('" + imgSrc + "','" + localSrc + "');");
                }
            }); // , webView.getDelayTime()
        }
    }



    // 非静态匿名内部类的实例，所以它持有外部类Activity的引用
    // 所以此处的 handler 会持有外部类 Activity 的引用，消息队列是在一个Looper线程中不断轮询处理消息。
    // 那么当这个Activity退出时消息队列中还有未处理的消息或者正在处理消息，而消息队列中的Message持有mHandler实例的引用，mHandler又持有Activity的引用，所以导致该Activity的内存资源无法及时回收，引发内存泄漏

    private int tapCount = 0;
    @Override
    public void onClick(View v) {
        KLog.e("loread", "【 toolbar 是否双击 】" + v);
        switch (v.getId()) {
            // debug
            case R.id.art_toolbar_num:
            case R.id.art_toolbar:
                break;
        }
    }

    public void debug_article_info(View view) {
        KLog.e("文章信息" + tapCount);
        if (tapCount == 0) {
            artHandler.postDelayed(new Runnable() {
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

    public ArtHandler artHandler = new ArtHandler(this);
    // 静态类不持有外部类的对象，所以你的Activity可以随意被回收，不容易造成内存泄漏
    private static class ArtHandler extends Handler {
        private final WeakReference<ArticleActivity> mActivity;
        ArtHandler(ArticleActivity activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(final Message msg) {
            KLog.e("【handler】" + msg.what + mActivity.get());
            if (mActivity.get() == null) { // 返回引用对象的引用
                return;
            }

            switch (msg.what) {
                case Api.S_BITMAP:
//                    int imgNo;
                    String articleID = msg.getData().getString("articleID");
                    String imgName = msg.getData().getString("imgName");
//                    imgNo = msg.getData().getInt("imgNo");

//                    Img imgMeta = WithDB.i().getImg(articleID, imgNo);
//                    if (imgMeta == null) {
//                        KLog.e("【】【】【】【】【】【】【】" + "未找到图片");
//                        break;
//                    }
//                    imgMeta.setDownState(Api.ImgMeta_Downover);
//                    WithDB.i().saveImg(imgMeta);

                    if (App.currentArticleID != null & App.currentArticleID.equals(articleID)) {
                        mActivity.get().imgLoadSuccess(msg.getData().getString("imgSrc"), "./" + fileTitle + "_files" + File.separator + imgName);
                    }
                    KLog.e("【t图片】" + msg.getData().getString("imgSrc") + "=" + articleID + "=" + msg.getData().getInt("imgNo") + "=" + articleID);
                    break;
                case Api.F_BITMAP:
                    articleID = msg.getData().getString("articleID");
                    if (App.currentArticleID != null & App.currentArticleID.equals(articleID)) {
                        if (null == mActivity.get().webView) {
                            Tool.showOnLocal("此时webView尽然为空？");
                        } else {
                            mActivity.get().webView.post(new Runnable() {
                                @Override
                                public void run() {
                                    mActivity.get().webView.loadUrl("javascript:onImageLoadFailed('" + msg.getData().getString("imgSrc") + "');");
                                }
                            });
                        }
                    }
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
//                                App.mNeter.articleRemoveTag(articleID, category);
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
//                        App.mNeter.articleAddTag(articleID, tagId);
                        DataApi.i().articleAddTag(articleID, tagId, null);
                        dialog.dismiss();
                        return true; // allow selection
                    }
                })
                .show();
    }


//    ArticleStateManager articleStateManager;
//    private void initArticleStateManager(){
//        articleStateManager = new ArticleStateManager(new ArticleStateManager.StateListener() {
//            @Override
//            public void onReaded() {
//                vRead.setText(getString(R.string.font_readed));
//            }
//
//            @Override
//            public void onUnread() {
//                vRead.setText(getString(R.string.font_unread));
//            }
//
//            @Override
//            public void onStared() {
//                vStar.setText(getString(R.string.font_stared));
//            }
//
//            @Override
//            public void onUnstar() {
//                vStar.setText(getString(R.string.font_unstar));
//            }
//        });
//    }


    public void onStarClick(View view) {
//        articleStateManager.changeStarState(article);
        if (article.getStarState().equals(Api.ART_UNSTAR)) {
            vStar.setText(getString(R.string.font_stared));
            DataApi.i().markArticleStared(articleID, null);
            article.setStarState(Api.ART_STARED);
            article.setStarred(System.currentTimeMillis() / 1000);
            if (article.getSaveDir().equals(Api.SAVE_DIR_BOX) || article.getSaveDir().equals(Api.SAVE_DIR_BOXREAD)) {
                moveArticleTo(article, Api.SAVE_DIR_STORE);
            }
        } else {
            vStar.setText(getString(R.string.font_unstar));
            DataApi.i().markArticleUnstar(articleID, null);
            article.setStarState(Api.ART_UNSTAR);
            if (article.getSaveDir().equals(Api.SAVE_DIR_STORE) || article.getSaveDir().equals(Api.SAVE_DIR_STOREREAD)) {
                moveArticleTo(article, Api.SAVE_DIR_BOX);
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
                moveArticleTo(article, Api.SAVE_DIR_STORE);
            } else {
                moveArticleTo(article, Api.SAVE_DIR_BOX);
            }
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

    public void onReadabilityClick(View view) {
        KLog.e("=====点击");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String articleHtml = MercuryApi.fetchReadabilityContent(article.getCanonical(), null);
                    KLog.e("=====1111" + articleHtml);
                    String sourceFileTitle;
                    if (article.getSaveDir().equals(Api.SAVE_DIR_CACHE)) {
                        sourceFileTitle = StringUtil.stringToMD5(article.getId());
                    } else {
                        sourceFileTitle = article.getTitle();
                    }
                    String sourceDirPath = FileUtil.getRelativeDir(article.getSaveDir());
                    FileUtil.saveHtml(sourceDirPath + sourceFileTitle + ".html", articleHtml);
                    KLog.e("=====" + sourceDirPath + sourceFileTitle + ".html");
                    KLog.e("=====" + articleHtml);
                    article.setImgState(null);
                    WithDB.i().saveArticle(article);
                    webView.post(new Runnable() {
                        @Override
                        public void run() {
                            KLog.e("===== 更新");
                            webView.loadDataWithBaseURL(FileUtil.getAbsoluteDir(article.getSaveDir()), StringUtil.getHtmlHeader() + StringUtil.getArticleHtml(article) + StringUtil.getFooter(), "text/html", "utf-8", null);
                            initWebViewLazyLoad();
                        }
                    });
                } catch (IOException e) {
                    KLog.e("===== 报错" + e);
                }
            }
        }).start();
    }

    private void moveArticleTo(Article article, String targetDir) {
        String sourceFileTitle;
        String targetFileTitle;
        String articleHtml;
        String sourceDirPath = FileUtil.getRelativeDir(article.getSaveDir());
        String targetDirPath = FileUtil.getRelativeDir(targetDir);

        if (article.getSaveDir().equals(Api.SAVE_DIR_CACHE)) {
            sourceFileTitle = StringUtil.stringToMD5(article.getId());
            articleHtml = FileUtil.readHtml(sourceDirPath + sourceFileTitle + ".html");
            articleHtml = StringUtil.reviseHtmlForBox(article.getTitle(), articleHtml);
            FileUtil.saveHtml(sourceDirPath + sourceFileTitle + ".html", articleHtml);
            targetFileTitle = StringUtil.getOptimizedNameForSave(article.getTitle());
            article.setTitle(targetFileTitle);
        } else {
            sourceFileTitle = article.getTitle();
            targetFileTitle = sourceFileTitle;
        }

        FileUtil.moveFile(sourceDirPath + sourceFileTitle + ".html", targetDirPath + targetFileTitle + ".html");
        FileUtil.moveDir(sourceDirPath + sourceFileTitle + "_files", targetDirPath + targetFileTitle + "_files");

//        KLog.i(sourceFileTitle);
//        KLog.d("原来文件夹" + sourceDirPath + sourceTitle + "_files");
//        KLog.d("目标文件夹" + targetDirPath + article.getTitle() + "_files");
        if (!TextUtils.isEmpty(article.getCoverSrc())) {  // (lossSrcList != null && lossSrcList.size() != 0) || ( obtainSrcList!= null && obtainSrcList.size() != 0)
            article.setCoverSrc(targetDirPath + article.getTitle() + "_files" + File.separator + StringUtil.getFileNameExtByUrl(article.getCoverSrc()));
//            KLog.d("封面" + targetDirPath + article.getTitle() + "_files" + File.separator + StringUtil.getFileNameExtByUrl(article.getCoverSrc()));
        }
        article.setSaveDir(targetDir);
        fileTitle = targetFileTitle;
        WithDB.i().saveArticle(article);
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

    private int articleNo, articleCount;


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



    public void initViewPager() {
        viewPager = (ViewPagerS) findViewById(R.id.art_viewpager);
        viewPager.clearOnPageChangeListeners();
        Tool.setBackgroundColor(viewPager);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, App.articleList);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new PageChangeListener());
        viewPager.setCurrentItem(articleNo, false); // 本句放到 ViewPagerAdapter 的构造器中是无效的。
        if (articleNo == 0) {
            KLog.e("loread", "当articleNo为0时，试一次");
            ArticleActivity.this.initSelectedPage(0);
        }
    }


    // SimpleOnPageChangeListener 是 ViewPager 内部，用空方法实现 OnPageChangeListener 接口的一个类。
    // 主要是为了便于使用者，不用去实现 OnPageChangeListener 的每一个方法（没必要，好几个用不到），只需要直接继承 SimpleOnPageChangeListener 实现需要的方法即可。
    private class PageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        /**
         * 参数position，代表哪个页面被选中。
         * 当用手指滑动翻页的时候，如果翻动成功了（滑动的距离够长），手指抬起来就会立即执行这个方法，position就是当前滑动到的页面。
         * 如果直接setCurrentItem翻页，那position就和setCurrentItem的参数一致，这种情况在onPageScrolled执行方法前就会立即执行。
         * 泪奔，当 setCurrentItem (0) 的时候，不会调用该函数
         * <p>
         * 点击进入 viewpager 时，该函数比 InstantiateItem 函数先执行。（之后滑动时，InstantiateItem 已经创建好了 view）
         * 所以，为了能在运行完 InstantiateItem ，有了 webView 之后再去执行 initSelectedPage 在首次进入时都不执行，放到 InstantiateItem 中。
         */
        @Override
        public void onPageSelected(final int pagePosition) {
            // 当知道页面被选中后：1.检查webView是否已经生成好了，若已生成则初始化所有数据（含页面的icon）；2.检查webView是否加载完毕，完毕则初始化懒加载
            KLog.e("loread", "【initSelectedPage】 " + "当前position=" + pagePosition + "  之前position=" + "  " + viewPager.findViewById(pagePosition)); //+ this.map.size()
            ArticleActivity.this.initSelectedPage(pagePosition);
        }
    }

    public void initSelectedPage(int position) {
        initSelectedArticle(position);
        initSelectedWebView(position);
    }

    public void initSelectedArticle(int position) {
        this.article = App.articleList.get(position);
        OkGo.cancelTag(imgHttpClient, articleID); // 取消之前那篇文章的图片下载
        App.time = System.currentTimeMillis();
        KLog.e("loread", "initSelectedArticle" + article.getTitle() + "====" + webView);
        articleID = article.getId();
        App.currentArticleID = articleID;
        setIconState(position);

        if (article.getSaveDir().equals(Api.SAVE_DIR_CACHE)) {
            fileTitle = StringUtil.stringToMD5(article.getId());
        } else {
            fileTitle = article.getTitle();
        }
    }

    public void initSelectedWebView(final int position) {
        if (viewPager.findViewById(position) != null) { // && dataList.get(pagePosition).getImgState()==null
            this.webView = (WebViewS) viewPager.findViewById(position);
            if (viewPager.getCurrentItem() == webView.getId()) {
                initWebViewLazyLoad();
//                initHtml();
            } else {
                KLog.e("loread", "----------选择的不是当前的webView");
            }
        } else {
            artHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initSelectedWebView(position);
                }
            }, 100);
        }
    }

    private void initHtml() {
        File file = new File(FileUtil.getRelativeDir(article.getSaveDir()) + fileTitle + ".html");
        if (!file.isFile()) {
            webView.loadDataWithBaseURL(FileUtil.getAbsoluteDir(article.getSaveDir()), StringUtil.getHtmlHeader() + StringUtil.getArticleHtml(article) + StringUtil.getFooter(), "text/html", "utf-8", null);
        }
    }

    private void initWebViewLazyLoad() {
        if (webView.getProgress() == 100) {
//            webView.loadUrl("javascript:docReady()");
            initImgDown();
        } else {
            artHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initWebViewLazyLoad();
                }
            }, 100);
        }
    }

    private void initImgDown() {
        if (article.getImgState() != null && !article.getImgState().equals(Api.ImgState_Downing)) {
            return;
        }

        KLog.e("loread", "initImgDown" + artHandler);
        DataApi.i().downImgs(artHandler, imgHttpClient, WithDB.i().getLossImgs(articleID), FileUtil.getRelativeDir(article.getSaveDir()) + fileTitle + "_files" + File.separator);
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
        builder.connectTimeout(60000L, TimeUnit.MILLISECONDS);
        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
        builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        builder.hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier);
        return builder.build();
    }

    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        mColorfulBuilder
                .backgroundColor(R.id.art_coordinator, R.attr.root_view_bg)
                // 设置 toolbar
                .backgroundColor(R.id.art_toolbar, R.attr.topbar_bg)
                .textColor(R.id.art_toolbar_num, R.attr.topbar_fg)

                // 设置中屏和底栏之间的分割线
                .backgroundColor(R.id.art_bottombar_divider, R.attr.bottombar_divider)

                // 设置 bottombar
                .backgroundColor(R.id.art_bottombar, R.attr.bottombar_bg)
                .textColor(R.id.art_bottombar_read, R.attr.bottombar_fg)
                .textColor(R.id.art_bottombar_star, R.attr.bottombar_fg)
                .textColor(R.id.art_bottombar_tag, R.attr.bottombar_fg)
                .textColor(R.id.art_bottombar_save, R.attr.bottombar_fg);
        return mColorfulBuilder;
    }

}
