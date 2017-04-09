package me.wizos.loread.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.socks.library.KLog;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.Article;
import me.wizos.loread.bean.Img;
import me.wizos.loread.bean.Tag;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.net.API;
import me.wizos.loread.net.Neter;
import me.wizos.loread.net.Parser;
import me.wizos.loread.presenter.MWebViewClient;
import me.wizos.loread.presenter.adapter.MaterialSimpleListAdapter;
import me.wizos.loread.presenter.adapter.MaterialSimpleListItem;
import me.wizos.loread.utils.HttpUtil;
import me.wizos.loread.utils.UFile;
import me.wizos.loread.utils.UString;
import me.wizos.loread.utils.UToast;
import me.wizos.loread.utils.colorful.Colorful;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.MViewPager.MViewPager;

@SuppressLint("SetJavaScriptEnabled")
public class ArticleActivity extends BaseActivity {
    protected static final String TAG = "ArticleActivityView";
    private WebView webView; // implements Html.ImageGetter
    //    private LinearLayout mll;
    protected Context context;
    protected IconFontView vStar, vRead, vSave;
    protected NestedScrollView vScrolllayout ;
    protected TextView vArticleNum;

    private int numOfImgs = 0, numOfGetImgs = 0, numOfFailureImg = 0, numOfFailure = 0, numOfFailures = 4;
    private Article article;
    private String articleHtml = "";
    private static String articleID = "";
    //    private String sReadState = "";
//    private String sStarState = "";
    private ArrayMap<Integer, Img> lossSrcList;

    //    private ArrayMap<Integer,Img> obtainSrcList ;
//    private List<Img> imgList;
    private SparseIntArray failImgList;
//    private String imgState; // 根据此值可以判断文章是否有被打开：null = 未打开；"" = 无图；其他为有图，图的信息在 extraImg中
//    private ExtraImg extraImg;
//    protected Parser mParser;
//    protected TextView vTitle ,vDate ,vTime, vFeed;
//    private String SaveRelativePath = "";
//    private String fileNameInMD5 = "";

    protected Neter mNeter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        context = this;
        App.addActivity(this);
        mNeter = new Neter(artHandler);
//        mNeter.setLogRequestListener(this);
//        mParser = new Parser();
        initColorful(); // 初始化界面的主题色
        initView(); // 初始化界面上的 View，将变量映射到布局上。
        KLog.d("开始初始话数据");
//        initWebView();
        initData(); // 得到从MainActivity过来的，页面编号
//        initStateView();
        initViewPager();
//        KLog.d("【op2】适配器总数：" + mViewPager.getAdapter().getCount() + "，View 总数：" +  views.size()  + "，当前项："  + mViewPager.getCurrentItem() );
    }

    protected void initColorful(){
        mColorful = new Colorful.Builder(this)
                // 设置view的背景图片
                .backgroundColor(R.id.art_coordinator, R.attr.root_view_bg)
                // 设置 toolbar
                .backgroundColor(R.id.art_toolbar, R.attr.topbar_bg)
                .textColor(R.id.art_toolbar_num, R.attr.topbar_fg)

                // 设置 bottombar
                .backgroundColor(R.id.art_bottombar, R.attr.bottombar_bg)
                .textColor(R.id.art_bottombar_read, R.attr.bottombar_fg)
                .textColor(R.id.art_bottombar_star, R.attr.bottombar_fg)
                .textColor(R.id.art_bottombar_label, R.attr.bottombar_fg)
                .textColor(R.id.art_bottombar_save, R.attr.bottombar_fg)

                .create(); // 创建Colorful对象
        autoToggleThemeSetting();
    }

    @Override
    protected void onResume(){
        super.onResume();
//        KLog.d("【op3】适配器总数：" + mViewPager.getAdapter().getCount() + "，View 总数：" +  views.size()  + "，当前项："  + mViewPager.getCurrentItem() );
    }

    @Override
    protected void onDestroy() {
        // 如果参数为null的话，会将所有的Callbacks和Messages全部清除掉。
        // 这样做的好处是在 Acticity 退出的时候，可以避免内存泄露。因为 handler 内可能引用 Activity ，导致 Activity 退出后，内存泄漏
        for (WebView webView : mPagerAdapter.pageList) {
            if (webView != null) {
                webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
                webView.removeAllViews();
                webView.clearHistory();
                webView.destroy();
                webView = null;
            }
        }
        mHandler.removeCallbacksAndMessages(null);
        artHandler.removeCallbacksAndMessages(null);
        mViewPager.removeAllViews();
        this.context = null;
        super.onDestroy();
    }


    @Override
    protected Context getActivity(){
        return context;
    }
    public String getTAG(){
        return TAG;
    }

    private void initView() {
        initToolbar();
        vStar = (IconFontView) findViewById(R.id.art_bottombar_star);
        vRead = (IconFontView) findViewById(R.id.art_bottombar_read);
        vSave = (IconFontView) findViewById(R.id.art_bottombar_save);
        vArticleNum =  (TextView)findViewById(R.id.art_toolbar_num);
//        vScrolllayout = (NestedScrollView) findViewById(R.id.art_scroll);
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
//
//    private void initWebView(){
//        KLog.d("初始化 webView");
//        webView = new WebView( getApplicationContext() );
////        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE); // 默认不使用缓存
//        webView.getSettings().setUseWideViewPort(false);// 设置此属性，可任意比例缩放
//        webView.getSettings().setDisplayZoomControls(false); //隐藏webview缩放按钮
//        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 就是这句使自适应屏幕
//        webView.getSettings().setLoadWithOverviewMode(true);// 缩放至屏幕的大小
//        webView.getSettings().setJavaScriptEnabled(true);
//
//        // 添加js交互接口类，并起别名 imagelistner
//        webView.addJavascriptInterface(new JavaScriptInterface(this), "imagelistner");
//        webView.setWebViewClient(new MyWebViewClient());
//
////        mll = (LinearLayout) findViewById(R.id.article_webview);
////        mll.addView(webView);
////        initViewPager();
//    }

    private static String fileTitle = "";


    /**
     * articleID = getIntent().getExtras().getString("articleID");
     * KLog.d("【article】" + articleID);
     * article = WithDB.getInstance().getArticle(articleID);
     */

    private void initData() {
        KLog.d("【initData】" + articleID);
        articleNo = getIntent().getExtras().getInt("articleNum"); // 文章在列表中的位置编号
        articleCount = getIntent().getExtras().getInt("articleCount"); // 列表中所有的文章数目
    }


    @JavascriptInterface
    public void listen(final int imgNo, String src) {
        KLog.e(imgNo + " = " + src);
//            if (article.getImgState() == "") {
//                KLog.e( " = 图片正在下载中，开始重新下载" );
//            } else if (imgsMeta.getImgStatus() == ImgsMeta.DOWNLOAD_OVER) {
//                KLog.e( " = 图片下载完成，请选择是重新下载还是打开大图" );
//            }
//        openImgMenuDialog(imgNo);
        // 下载图片
        // 打开大图
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter( ArticleActivity.this);
                adapter.add(new MaterialSimpleListItem.Builder(ArticleActivity.this)
                        .content("重新下载")
                        .icon(R.drawable.ic_vector_mark_after)
                        .backgroundColor(Color.WHITE)
                        .build());
                adapter.add(new MaterialSimpleListItem.Builder(ArticleActivity.this)
                        .content("打开大图")
                        .icon(R.drawable.ic_vector_mark_before)
                        .backgroundColor(Color.WHITE)
                        .build());
                new MaterialDialog.Builder(ArticleActivity.this)
                        .adapter(adapter, new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                switch (which) {
                                    case 0:
                                        KLog.e( "重新下载" + imgNo );
                                        // 此时还要判断他的储存位置 是 box 还是 cache
                                        restartDownloadImg(imgNo);
                                        break;
                                    case 1:
                                        KLog.e( "打开大图" );
                                        break;
                                }

                                dialog.dismiss();
                            }
                        })
                        .show();

            }
        });
    }


    private void restartDownloadImg(int imgNo){
        imgNo = imgNo + 1 ;
        KLog.d(imgNo);
//        SrcPair imgSrc;
        Img imgMeta;
        if( lossSrcList!=null ){
            imgMeta = lossSrcList.get(imgNo);
            if (imgMeta == null) {
                UToast.showShort("没有找到图片1");
            }
        }else {
            UToast.showShort("lossSrcList与obtainSrcList都为null");
            KLog.d("--");
            return;
        }

        if (!HttpUtil.canDownImg()) {
            artHandler.sendEmptyMessage(API.F_Request);
            return ;}
//        String saveRelativePath = UFile.getRelativeFilePath( article.getSaveDir(),fileNameInMD5, article.getTitle() ) + "_files" + File.separator;
        String savePath = UFile.getRelativeDir(article.getSaveDir()) + fileTitle + "_files" + File.separator + imgMeta.getName();
        KLog.d("图片的保存目录为1：" + savePath);
//        KLog.d("图片的保存目录为2：" + saveRelativePath + UString.getFileNameExtByUrl(imgSrc.getSaveSrc()) );
        mNeter.loadImg(articleID, imgMeta.getSrc(), savePath, imgNo);
    }


    private void replaceSrc(String articleID, final int imgNo, final String localSrc) {
        if (!this.articleID.equals(articleID)) {
            return;
        }
        final int no = imgNo-1; // 因为图片的标号是从 1 开始，而 DOM 中，要从 0 开始。
        KLog.e("替换src" + no + article.getSaveDir() + article.getTitle() + "：" + localSrc);
        if (webView == null) {
            return;
        }
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:(function(){" +
                        "imgList = document.getElementsByTagName(\"img\");" +
                        "imgList[" + no + "].src = \"" + localSrc + "\"" +
                        "})()");
            }
        });
    }






    @Override
    public void notifyDataChanged() {
//        KLog.d("【重载】");
//        String html;
//        if( article.getSaveDir().equals(API.SAVE_DIR_CACHE)){
//            html = getShowHtmlHeader() + getHtmlContent( );
//        }else {
//            html = getShowHtmlHeader() + articleHtml;
//        }
    }
    // 非静态匿名内部类的实例，所以它持有外部类Activity的引用
    // 所以此处的 handler 会持有外部类 Activity 的引用，消息队列是在一个Looper线程中不断轮询处理消息。
    // 那么当这个Activity退出时消息队列中还有未处理的消息或者正在处理消息，而消息队列中的Message持有mHandler实例的引用，mHandler又持有Activity的引用，所以导致该Activity的内存资源无法及时回收，引发内存泄漏

    private final Handler artHandler = new ArtHandler(this);

    private static class ArtHandler extends Handler {
        private final WeakReference<ArticleActivity> mActivity;
        private final Neter mNeter;

        ArtHandler(ArticleActivity activity) {
            mActivity = new WeakReference<>(activity);
            mNeter = new Neter(this);
        }

        @Override
        public void handleMessage(Message msg) {
            KLog.d(msg);
            if (mActivity.get() == null) { // 返回引用对象的引用
                return;
            }
            String info = msg.getData().getString("res");
            String url = msg.getData().getString("url");
            String filePath ="";
            int imgNo;
            String articleID;
            if ( info == null ){
                info = "";
            }
            KLog.d("【handler】" + msg.what + url);
            switch (msg.what) {
                case API.S_EDIT_TAG:
                    long logTime = msg.getData().getLong("logTime");
                    if(!info.equals("OK")){
                        mNeter.forData(url,API.request,logTime);
                        KLog.d("【返回的不是 ok");
                    }
                    break;
                case API.S_ARTICLE_CONTENTS:
                    Parser.instance().parseArticleContents(info);
                    mActivity.get().initData(); // 内容重载
                    break;
                case API.S_BITMAP:
                    articleID = msg.getData().getString("articleID");
                    imgNo = msg.getData().getInt("imgNo");
//                    Img imgMeta = mActivity.get().lossSrcList.get(imgNo);
                    Img imgMeta = App.lossImgListArray.get(articleID).get(imgNo);
                    imgMeta.setDownState(1);
                    WithDB.getInstance().saveImg(imgMeta);
                    App.lossImgListArray.get(articleID).remove(imgNo);

                    mActivity.get().numOfGetImgs = mActivity.get().numOfGetImgs + 1;
                    KLog.i("【 API.S_BITMAP 】" + imgNo + "=" + mActivity.get().numOfGetImgs + "--" + mActivity.get().numOfImgs);
                    if (mActivity.get().numOfGetImgs >= mActivity.get().numOfImgs) { // || numOfGetImgs % 5 == 0
                        KLog.i("【图片全部下载完成】" + mActivity.get().numOfGetImgs + "=" + mActivity.get().numOfImgs);

                        mActivity.get().lossSrcList.clear();
//                        mActivity.get().setImgState(ImgsMeta.DOWNLOAD_OVER);
                        mActivity.get().setImgState(1);
                        UToast.showShort("图片下载完成");
//                        webView.notify();
                    } else {
//                        mActivity.get().setImgState(ImgsMeta.DOWNLOAD_ING);
                        mActivity.get().setImgState(0);
                    }
                    KLog.i("【1】" + imgMeta);
                    if (imgMeta != null) {
                        mActivity.get().replaceSrc(articleID, imgNo, "./" + fileTitle + "_files" + File.separator + imgMeta.getName());
                    }
                    break;
                case API.F_BITMAP:
                    articleID = msg.getData().getString("articleID");
                    imgNo = msg.getData().getInt("imgNo");
                    if (mActivity.get().failImgList == null) {
                        mActivity.get().failImgList = new SparseIntArray(mActivity.get().numOfImgs);
                    }
                    mActivity.get().numOfFailureImg = mActivity.get().failImgList.get(imgNo, 0);
                    mActivity.get().failImgList.put(imgNo, mActivity.get().numOfFailureImg + 1);
//                    numOfFailureImg = numOfFailureImg + 1;
                    KLog.i("【 API.F_BITMAP 】" + imgNo + "=" + mActivity.get().numOfFailureImg + "--" + mActivity.get().numOfImgs);
                    if (mActivity.get().numOfFailureImg > mActivity.get().numOfFailures) {
                        UToast.showShort( "图片无法下载，请稍候再试" );
                        break;
                    }
                    filePath = msg.getData().getString("filePath");
                    mNeter.loadImg(articleID, url, filePath, imgNo);
                    break;
                case API.F_Request:
                case API.F_Response:
                    if( info.equals("Authorization Required")){
                        UToast.showShort("没有Authorization，请重新登录");
//                        mActivity.get().finish();
                        App.finishActivity(mActivity.get());
                        mActivity.get().goTo(LoginActivity.TAG, "Login For Authorization");
                        break;
                    }
                    mActivity.get().numOfFailure = mActivity.get().numOfFailure + 1;
                    if (mActivity.get().numOfFailure < 3) {
                        mNeter.forData(url,API.request,0);
                        break;
                    }
                    UToast.showShort("网络不好，中断");
                    break;
                case API.F_NoMsg:
                    break;
            }
//            return false;
        }
    }


    /**
     * 在初次进入 html 获得 imgList 时，记录值 DOWNLOAD_ING。
     * 在每次成功下载到图片时，记录 DOWNLOAD_ING。
     * 在所有下载完成时，记录 DOWNLOAD_OVER。
     * @param downState 有两个值：DOWNLOAD_ING(下载中) 和 DOWNLOAD_OVER(下载完成)
     */
    private void setImgState(int downState) {
        article.setImgState(String.valueOf(downState));
        KLog.e("【储存的 setImgStatus 】" + downState);
        WithDB.getInstance().saveArticle(article);
    }

    private Handler mHandler = new Handler();
    @Override
    public void onClick(View v) {
        KLog.d( "【 toolbar 是否双击 】" );
        switch (v.getId()) {
            case R.id.art_toolbar_num:
            case R.id.art_toolbar:
                if (mHandler.hasMessages(API.MSG_DOUBLE_TAP)) {
                    mHandler.removeMessages(API.MSG_DOUBLE_TAP);
                    vScrolllayout.smoothScrollTo(0, 0);
                } else {
                    mHandler.sendEmptyMessageDelayed(API.MSG_DOUBLE_TAP, ViewConfiguration.getDoubleTapTimeout());
                }
                break;
        }
    }

    public void onLabelClick(View view){

        final List<Tag> tagsList = WithDB.getInstance().loadTags();
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
//                        String articleCategories = m.replaceAll( tagId );
//                        String articleCate = m.replaceFirst( tagId );
//                        KLog.d("【被选择文章的分类2】" + articleCategories + articleCate );
//                        String categories = article.getCategories();
//                        KLog.d("【被选择文章的分类1】" + categories );
                        StringBuilder newCategories = new StringBuilder(  article.getCategories().length()  );
                        String[] cateArray = article.getCategories().replace("]","").replace("[","").split(", ");
                        for (String cate:cateArray){
                            if (cate.contains("user/" + App.mUserID + "/label/")) {
                                mNeter.postRemoveArticleTags( articleID,cate);
                                KLog.d("【-】" + cate );
                            }else {
                                newCategories.append(cate);
                                newCategories.append(", ");
                            }
                        }
                        newCategories.append(tagId);
                        newCategories.append("]");
                        KLog.d("【==】" + newCategories + articleID);
                        article.setCategories( newCategories.toString() );
                        mNeter.postAddArticleTags(articleID,tagId);
                        mNeter.postStarArticle( articleID );
                        dialog.dismiss();
                        return true; // allow selection
                    }
                })
                .show();
//        WithDB.getInstance().saveArticle( article );
    }


    public void onStarClick(View view) {
        if (article.getStarState().equals(API.ART_UNSTAR)) {
            changeStarState(API.ART_STAR);
            UToast.showShort("已收藏");
            if (article.getSaveDir().equals(API.SAVE_DIR_BOX) || article.getSaveDir().equals(API.SAVE_DIR_BOXREAD)) {
                moveArticleDir(API.SAVE_DIR_STORE);
            }
        } else {
            changeStarState(API.ART_UNSTAR);
            UToast.showShort("取消收藏");
            if (article.getSaveDir().equals(API.SAVE_DIR_STORE) || article.getSaveDir().equals(API.SAVE_DIR_STOREREAD)) {
                moveArticleDir(API.SAVE_DIR_BOX);
            }
        }
        WithDB.getInstance().saveArticle(article);
    }

    public void onSaveClick(View view){
        if (article.getSaveDir().equals(API.SAVE_DIR_CACHE)) {
            if (article.getStarState().equals(API.ART_STAR)) {
                moveArticleDir(API.SAVE_DIR_STORE);
            } else {
                moveArticleDir(API.SAVE_DIR_BOX);
            }
        } else {
            UToast.showShort("文件已保存");
//            moveArticleDir( article.getSaveDir(), API.SAVE_DIR_CACHE ); // 暂时不能支持从 box/store 撤销保存回 cache，因为文章的正文是被加了修饰的，为 epub 文件做准备的
        }
        changeSaveState(article.getSaveDir());
    }


    private void moveArticleDir(String targetDir) {
        String sourceTitle = fileTitle;
        KLog.d(fileTitle);
        String sourceDirPath = UFile.getRelativeDir(article.getSaveDir());
        String targetDirPath = UFile.getRelativeDir(targetDir);

        if (article.getSaveDir().equals(API.SAVE_DIR_CACHE)) {
            String articleHtml;
            articleHtml = UFile.readHtml(sourceDirPath + fileTitle + ".html");
            articleHtml = UString.reviseHtmlForBox(article.getTitle(), articleHtml);
            UFile.saveHtml(sourceDirPath + fileTitle + ".html", articleHtml);
            fileTitle = article.getTitle();
        }
        KLog.d(articleHtml);

        UFile.moveFile(sourceDirPath + sourceTitle + ".html", targetDirPath + fileTitle + ".html");
        UFile.moveDir(sourceDirPath + sourceTitle + "_files", targetDirPath + fileTitle + "_files");

        KLog.d("原来文件夹" + sourceDirPath + sourceTitle + "_files");
        KLog.d("目标文件夹" + targetDirPath + article.getTitle() + "_files");
        if (article.getImgState() != null && !article.getImgState().equals("")) {  // (lossSrcList != null && lossSrcList.size() != 0) || ( obtainSrcList!= null && obtainSrcList.size() != 0)
            article.setCoverSrc(targetDirPath + article.getTitle() + "_files" + File.separator + UString.getFileNameExtByUrl(article.getCoverSrc()));
            KLog.d("封面" + targetDirPath + article.getTitle() + "_files" + File.separator + UString.getFileNameExtByUrl(article.getCoverSrc()));
        }
        article.setSaveDir(targetDir);
        WithDB.getInstance().saveArticle(article);
    }

//    private void moveDir( Article article, String sourceTitle, String targetTitle ) {
//        UFile.moveFile(sourceDir + sourceTitle + ".html", UFile.getRelativeDir(targetDir) + targetTitle + ".html");
//        UFile.moveDir(sourceDir + sourceTitle + "_files", UFile.getRelativeDir(targetDir) + targetTitle + "_files");
//    }





    public void onReadClick(View view) {
        if (article.getReadState().equals(API.ART_READ)) {
            changeReadIcon(API.ART_READING);
            UToast.showShort("未读");
        }else {
            changeReadIcon(API.ART_READ);
            UToast.showShort("已读");
        }
    }
    private void changeReadIcon(String iconState) {
        article.setReadState(iconState); // 在使用过程中，只会 涉及 read 与 reading 的转换。unread 仅作为用户未主动修改文章状态是的默认状态，reading 不参与勾选为已读
        if(iconState.equals(API.ART_READ)){
            vRead.setText(getString(R.string.font_readed));
            mNeter.postReadArticle(articleID);
            KLog.d("【 标为已读 】");
        }else {
            vRead.setText(getString(R.string.font_unread));
            mNeter.postUnReadArticle(articleID);
            KLog.d("【 标为未读 】");
        }
        WithDB.getInstance().saveArticle(article);
    }
    private void changeStarState(String iconState) {
        article.setStarState(iconState);
        if(iconState.equals(API.ART_STAR)){
            vStar.setText(getString(R.string.font_stared));
            mNeter.postStarArticle(articleID);
        }else {
            vStar.setText(getString(R.string.font_unstar));
            mNeter.postUnStarArticle(articleID);
        }
        WithDB.getInstance().saveArticle(article);
    }

    private void changeSaveState(String iconState) {
        if (iconState.equals(API.SAVE_DIR_CACHE)) {
            vSave.setText(getString(R.string.font_unsave));
        } else {
            vSave.setText(getString(R.string.font_saved));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { // 后者为短期内按下的次数
            App.finishActivity(this);
            return true;//返回真表示返回键被屏蔽掉
        }
        return super.onKeyDown(keyCode, event);
    }


    private int articleNo, articleCount;
    private MViewPager mViewPager;
    private MViewPagerAdapter mPagerAdapter;


    // 初始化ViewPager
    private void initViewPager() {
        mViewPager = (MViewPager) findViewById(R.id.art_viewpager);
        mPagerAdapter = new MViewPagerAdapter(this, mViewPager, App.articleList, articleNo); // 在这里面初始化了多个 webView
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(1, false); //false:不显示跳转过程的动画
        KLog.d("【initViewPager】适配器总数：" + mViewPager.getAdapter().getCount() + "，View 总数：" + "，当前项：" + mViewPager.getCurrentItem());
    }

    /**
     * 显示 page 的文章
     *
     * @param article
     */
    private void showingPageData(Article article, WebView webView) {
        this.article = article;
        this.webView = webView;
        KLog.d("-----------------------------------------------------------------------------------------------------");
        KLog.d("显示页面showArticle：" + article.getTitle() );
        articleID = article.getId();
        numOfGetImgs = 0;
        initIconState(article);

        if (article.getSaveDir().equals(API.SAVE_DIR_CACHE)) {
            fileTitle = UString.stringToMD5(article.getId());
        } else {
            fileTitle = article.getTitle();
        }

        // Page渲染完成之后，添加图片的Js点击监听函数
        webView.loadUrl("javascript:initImgClick()"); // 初始化图片的点击事件
        if (article.getImgState() == null) {
            webView.loadUrl("javascript:initImgPlaceholder()");
            KLog.d("初始化所有图片占位符a");
        } else if (article.getImgState().equals("0")) { // 未下载完
            lossSrcList = WithDB.getInstance().getLossImgs(articleID);
            KLog.d("相关信息：" + articleID + lossSrcList);
            App.lossImgListArray.put(articleID, lossSrcList);
            numOfImgs = lossSrcList.size();
//                    obtainSrcList = new ArrayMap<>(numOfImgs);
            StringBuilder imgNoArray = new StringBuilder("");
            for (int i = 0; i < numOfImgs; i++) {
                imgNoArray.append(lossSrcList.keyAt(i) - 1); // imgState 里的图片下标是从1开始的
                imgNoArray.append("_");
            }
//            imgNoArray.deleteCharAt(imgNoArray.length()-1);
            KLog.d("传递的值" + imgNoArray);
            webView.loadUrl("javascript:appointImgPlaceholder(" + "\"" + imgNoArray.toString() + "\"" + ")");
            mNeter.downImgs(articleID, lossSrcList, UFile.getRelativeDir(article.getSaveDir()) + fileTitle + "_files" + File.separator);
        }
    }


    private void initIconState(Article article) {
        if (article.getReadState().equals(API.ART_UNREAD)) {
            vRead.setText(getString(R.string.font_readed));
            article.setReadState(API.ART_READ);
            WithDB.getInstance().saveArticle(article);
            mNeter.postReadArticle(articleID);
            KLog.d("【 ReadState 】" + WithDB.getInstance().getArticle(article.getId()).getReadState());
        } else if (article.getReadState().equals(API.ART_READ)) {
            vRead.setText(getString(R.string.font_readed));
        } else if (article.getReadState().equals(API.ART_READING)) {
            vRead.setText(getString(R.string.font_unread));
        }

        if (article.getStarState().equals(API.ART_UNSTAR)) {
            vStar.setText(getString(R.string.font_unstar));
        } else {
            vStar.setText(getString(R.string.font_stared));
        }
        if (article.getSaveDir().equals(API.SAVE_DIR_CACHE)) {
            vSave.setText(getString(R.string.font_unsave));
        } else {
            vSave.setText(getString(R.string.font_saved));
        }
        String numStr = String.valueOf(App.articleList.indexOf(article) + 1) + " / " + String.valueOf(articleCount);
        vArticleNum.setText(numStr);
    }

    class MViewPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {
        private ArticleActivity context;
        private MViewPager viewPager;
        private SparseIntArray lastPageDataIndexMap; //（保存 page 和 data 的对应关系，防止在下次重新加载数据时重复）
        // 保存每个页面，上次展示的数据编号（如果相同，那么在 loadingPageData() 时就不改变）
        private int pageIndex, dataIndex, lastPagePosition = 1;
        private List<Article> dataList;
        private List<WebView> pageList;

        MViewPagerAdapter(ArticleActivity context, MViewPager viewPager, List<Article> dataList, int articleNo) {
            this.context = context;
            this.viewPager = viewPager;
            this.dataList = dataList;
            dataIndex = articleNo - 1;
            KLog.d("dataIndex是多少A：" + dataIndex);
            viewPager.clearOnPageChangeListeners();
            viewPager.addOnPageChangeListener(this);
            if (null == dataList || dataList.isEmpty()) return;
            initPages();
            KLog.d("dataIndex是多少B：" + dataIndex);
            loadingPageData(1, dataIndex);
            KLog.d("dataIndex是多少C：" + dataIndex);
        }

        /**
         * 初始化 pager 控件（根据data的数量，设置Page的数量）
         */
        private void initPages() {
            KLog.d("根据data的数量，设置Page的数量" + ", dataSize=" + dataList.size());
            int pageSize, dataSize = dataList.size();
            if (dataSize == 0) {
                // TODO: 2017/2/26 显示无文章占位图
                return;
            } else if (dataSize == 1) {
                pageSize = dataSize;
            } else if (dataSize == 2) {
                pageSize = dataSize;
            } else {
                pageSize = 5;
            }

            pageList = new ArrayList<>(pageSize);
            lastPageDataIndexMap = new SparseIntArray(pageSize);
            for (int pageIndex = 0; pageIndex < pageSize; pageIndex++) {
                pageList.add(getView());
                lastPageDataIndexMap.put(pageIndex, -1);
            }
            KLog.e("视图的数量2：" + pageList.size());
        }

        /**
         * get View
         */
        private WebView getView() {
            WebView webView = new WebView(context);
            webView.getSettings().setUseWideViewPort(false);// 设置此属性，可任意比例缩放
            webView.getSettings().setDisplayZoomControls(false); //隐藏webview缩放按钮
            webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 就是这句使自适应屏幕
            webView.getSettings().setLoadWithOverviewMode(true);// 缩放至屏幕的大小
            webView.getSettings().setJavaScriptEnabled(true);

            // 添加js交互接口类，并起别名 imagelistner
            webView.addJavascriptInterface(context, "imagelistner");
            webView.setWebViewClient(new MWebViewClient(context ) );
//            webView.setWebChromeClient( new MyWebChromeClient() );
            LayoutInflater inflater = getLayoutInflater();
            LinearLayout mll = (LinearLayout) inflater.inflate(R.layout.activity_article_mll, null);
//            mll = (LinearLayout) findViewById(R.id.article_webview);
            mll.addView(webView);
            return webView;
        }


        /**
         * 这个方法会在屏幕滚动过程中不断被调用。
         *
         * @param pagePosition         当用手指滑动时，如果手指按在页面上不动，position和当前页面index是一致的；
         *                              如果手指向左拖动时（页面向右翻动），position大部分时间和“当前页面”一致，只有翻页成功的情况下最后一次调用才会变为“目标页面”；
         *                              如果手指向右拖动时（页面向左翻动），position大部分时间和“目标页面”一致，只有翻页不成功的情况下最后一次调用才会变为“原页面”。
         *                              <p>
         *                              当直接设置setCurrentItem翻页时，如果是相邻的情况（比如现在是第二个页面，跳到第一或者第三个页面）：
         *                              如果页面向右翻动，大部分时间是和当前页面是一致的，只有最后才变成目标页面；
         *                              如果页面向左翻动，position和目标页面是一致的。这和用手指拖动页面翻动是基本一致的。
         *                              如果不是相邻的情况，比如我从第一个页面跳到第三个页面，position先是0，然后逐步变成1，然后逐步变成2；
         *                              我从第三个页面跳到第一个页面，position先是1，然后逐步变成0，并没有出现为2的情况。
         * @param positionOffset       当前页面滑动比例，如果页面向右翻动，这个值不断变大，最后在趋近1的情况后突变为0。如果页面向左翻动，这个值不断变小，最后变为0。
         * @param positionOffsetPixels 当前页面滑动像素，变化情况和positionOffset一致。
         */
        @Override
        public void onPageScrolled(int pagePosition, float positionOffset, int positionOffsetPixels) {
//            KLog.e( "onPageScrolled" , "position=" +  pagePosition + ", dataIndex=" + dataIndex  + "   " + currentPosition);
        }

        private int currentPosition = 0;
        private void judgeDirection(int pagePosition) {
            if (pagePosition > currentPosition) {
                KLog.e("方向", "右滑");
            } else if (pagePosition < currentPosition) {
                KLog.e("方向", "左滑");
            }
            currentPosition = pagePosition;
        }

        /**
         * 加载页面的数据（指定数据到指定的页面）
         *
         * @param pageIndex 页面的索引（真正那几个页面的索引）
         * @param dataIndex 数据的索引
         */
        private void loadingPageData(int pageIndex, int dataIndex) {
            KLog.e("加载页面数据loadingPageData：" + pageIndex + "   lastPagePosition=" + lastPageDataIndexMap.get(pageIndex) + "   dataIndex=" + dataIndex);

            if (lastPageDataIndexMap.get(pageIndex) == dataIndex || dataIndex < 0 || dataIndex > dataList.size() - 1) {
                KLog.e("相同数据，不更改：" + pageIndex + "   lastPagePosition=" + lastPageDataIndexMap.get(pageIndex) + "   dataIndex=" + dataIndex);
                return;
            } else {
                lastPageDataIndexMap.put(pageIndex, dataIndex);
            }
            KLog.d("加载文章：" + dataList.get(dataIndex).getTitle());
            KLog.i(pageIndex + "--- " + dataIndex + "浏览器" + pageList.get(pageIndex));
//            String nnn = UString.getHtmlHeader() + getArticleHtml(dataList.get(dataIndex));
//            KLog.e( nnn );
            pageList.get(pageIndex).loadDataWithBaseURL(UFile.getAbsoluteDir(dataList.get(dataIndex).getSaveDir()), UString.getHtmlHeader() + getArticleHtml(dataList.get(dataIndex)), "text/html", "utf-8", null);
        }


        /**
         * 获取文章正文（并修饰）
         *
         * @param article
         * @return
         */
        private String getArticleHtml(Article article) {
//            KLog.d("====" + article.getTitle() + "----"+  article.getSummary());
            if (article == null) {
                // TODO: 2017/2/19  加载没有正文的占位画面
                KLog.d("Article为null");
                return "";
            }
            if (article.getSummary().length() == 0) {
                // TODO: 2017/2/19  加载没有正文的占位画面
                KLog.d("正文内容为空");
                return "";
            }

            // 获取 文章的 fileTitle
            String fileTitle;
            if (article.getSaveDir().equals(API.SAVE_DIR_CACHE)) {
                fileTitle = UString.stringToMD5(article.getId());
            } else {
                fileTitle = article.getTitle();
            }

            String articleHtml = UFile.readHtml(UFile.getRelativeDir(article.getSaveDir()) + fileTitle + ".html");

            if (articleHtml.length() == 0) {
                // TODO: 2017/2/19  加载等待获取正文的占位画面
                // TODO: 2017/4/8 重新获取文章正文
                mNeter.postArticleContents(article.getId());
            } else if (article.getImgState() == null) { // 文章没有被打开过
                ArrayMap<Integer, Img> lossSrcList = UString.getListOfSrcAndHtml(article.getId(), articleHtml, fileTitle);
                articleHtml = lossSrcList.get(0).getSrc();
                articleHtml = UString.getModHtml(article, articleHtml);
                lossSrcList.remove(0);
                if (lossSrcList.size() != 0) {
                    article.setCoverSrc(UFile.getAbsoluteDir(API.SAVE_DIR_CACHE) + fileTitle + "_files" + File.separator + lossSrcList.get(1).getName());
                    WithDB.getInstance().saveImgs(lossSrcList);
                    article.setImgState("0");
                } else {
                    article.setImgState("");
                    KLog.d("为空");
                }
                KLog.d("获取文章正文getArticleHtml：" + article.getId() + lossSrcList);
                article.setTitle(UString.handleSpecialChar(article.getTitle()));

                String summary = Html.fromHtml(articleHtml).toString(); // 可以去掉标签
                article.setSummary(UString.getSummary(summary));

                UFile.saveCacheHtml(fileTitle, articleHtml);
                WithDB.getInstance().saveArticle(article);
            }
            return articleHtml;
        }

        /**
         * 参数position，代表哪个页面被选中。
         * 当用手指滑动翻页的时候，如果翻动成功了（滑动的距离够长），手指抬起来就会立即执行这个方法，position就是当前滑动到的页面。
         * 如果直接setCurrentItem翻页，那position就和setCurrentItem的参数一致，这种情况在onPageScrolled执行方法前就会立即执行。
         */

        @Override
        public void onPageSelected(int pagePosition) {
            KLog.d("==================================================================================================");
            KLog.d("页面被选中");
            KLog.d("当前要展示的页面位置" + pagePosition + "   lastPagePosition=" + lastPagePosition + "   当前要展示的文章序号：" + dataIndex );

            pageIndex = pagePosition;
            dataIndex = dataIndex + pagePosition - lastPagePosition;
            lastPagePosition = pagePosition;
            if (pagePosition == 0) {
                loadingPageData(2, dataIndex - 1);
                loadingPageData(4, dataIndex + 1);
                // 当视图在第一个时，将页面号设置为图片的最后一张。
                pageIndex = 3;
                lastPagePosition = 2;
            } else if (pagePosition == 1) {
                loadingPageData(0, dataIndex - 1);
                loadingPageData(2, dataIndex + 1);
                loadingPageData(3, dataIndex - 1);
            } else if (pagePosition == 2) {
                loadingPageData(1, dataIndex - 1);
                loadingPageData(3, dataIndex + 1);
            } else if (pagePosition == 3) {
                loadingPageData(1, dataIndex + 1);
                loadingPageData(2, dataIndex - 1);
                loadingPageData(4, dataIndex + 1);
            } else if (pagePosition == 4) {
                loadingPageData(0, dataIndex - 1);
                loadingPageData(2, dataIndex + 1);
                // 当视图在最后一个是,将页面号设置为图片的第一张。
                pageIndex = 1;
                lastPagePosition = 0;
            }
            if (pagePosition != pageIndex) {  // 产生跳转了
                lastPagePosition = pageIndex;
                viewPager.setCurrentItem(pageIndex, false);
                KLog.d("lastPagePosition：" + lastPagePosition + "   pageIndex：" + pageIndex);
            }
            // 设置页面在第一个数据和最后一个数据时，不可右滑和左滑
            if (dataIndex == 0) {
                viewPager.setCallScrollToRight(false);
            } else if (dataIndex == dataList.size() - 1) {
                viewPager.setCallScrollToLeft(false);
            } else {
                viewPager.setCallScrollToRight(true);
                viewPager.setCallScrollToLeft(true);
            }
            showingPageData(dataList.get(dataIndex), pageList.get(pageIndex));
//            ArticleActivity.this.webView = views.get(pageIndex);
//            KLog.d(  "fileTitle：" +  fileTitle + "   fileTitle：" + fileTitle   );
            KLog.d("pagePosition：" + pagePosition + "   pageIndex：" + pageIndex);
        }


        /**
         * state 有三种取值：
         * 0：什么都没做
         * 1：开始滑动
         * 2：滑动结束
         * 滑动过程的顺序，从滑动开始依次为：(1,2,0)
         */
        @Override
        public void onPageScrollStateChanged(int state) {
//            KLog.e( "onPageScrollStateChanged" ,  "currentPage=" + currentPage  );
        }

        @Override
        public int getCount() {
            return (null == pageList) ? 0 : pageList.size();
        }

        /**
         * 判断出去的view是否等于进来的view 如果为true直接复用
         */
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        /**
         * 做了两件事，第一：将当前视图添加到container中，第二：返回当前View
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
//        KLog.d("【instantiateItem】适配器总数：" + getCount() + "，View 总数：" +  views.size()  + "，当前position："  + position  + "，index："  + "，view对象：" + views.get(position) );
            if (null != pageList.get(position).getParent()) {
                ViewGroup viewGroup = (ViewGroup) pageList.get(position).getParent();
                viewGroup.removeView(pageList.get(position));
            }
            container.addView(pageList.get(position));
            return pageList.get(position);
        }

        /**
         * 销毁预加载以外的view对象, 会把需要销毁的对象的索引位置传进来就是position
         */
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//        KLog.d("【destroyItem】适配器总数：" + getCount() + "，View 总数：" +  views.size()  + "，当前position："  + position  + "，index："  + index + "，view对象：" + views.get(position) );
            container.removeView((View) object);
        }
    }

}
