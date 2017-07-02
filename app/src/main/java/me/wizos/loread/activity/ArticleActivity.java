package me.wizos.loread.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
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
import me.wizos.loread.data.WithSet;
import me.wizos.loread.net.API;
import me.wizos.loread.net.Neter;
import me.wizos.loread.net.Parser;
import me.wizos.loread.presenter.adapter.ArticleAdapter;
import me.wizos.loread.presenter.adapter.MaterialSimpleListAdapter;
import me.wizos.loread.presenter.adapter.MaterialSimpleListItem;
import me.wizos.loread.utils.HttpUtil;
import me.wizos.loread.utils.UFile;
import me.wizos.loread.utils.UString;
import me.wizos.loread.utils.UToast;
import me.wizos.loread.utils.colorful.Colorful;
import me.wizos.loread.view.IconFontView;

@SuppressLint("SetJavaScriptEnabled")
public class ArticleActivity extends BaseActivity {
    protected static final String TAG = "ArticleActivity";
    private WebView webView;
    private Context context;
    private IconFontView vStar, vRead, vSave;
    //    private NestedScrollView vScrolllayout ; // 这个是为上级顶部，页面滑动至最顶层而做的。目前由于 webview 是动态添加，所以无法用到
    private TextView vArticleNum;

    //    private int numOfImgs = 0, numOfGetImgs = 0, numOfFailureImg = 0, numOfFailure = 0, numOfFailures = 4;
    private SparseIntArray failImgList;

    private Article article;
    private static String articleID = "";
    protected Neter mNeter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        context = this;
        App.addActivity(this);
        App.artHandler = artHandler;
        mNeter = new Neter(artHandler);
//        mParser = new Parser();
        initColorful(); // 初始化界面的主题色
        initView(); // 初始化界面上的 View，将变量映射到布局上。
        KLog.d("开始初始话数据");
        initData(); // 得到从MainActivity过来的，页面编号
//        initStateView();
//        initViewPager();
        initPager();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 如果参数为null的话，会将所有的Callbacks和Messages全部清除掉。
        // 这样做的好处是在 Acticity 退出的时候，可以避免内存泄露。因为 handler 内可能引用 Activity ，导致 Activity 退出后，内存泄漏
        KLog.e("onDestroy" + webView);
//        for ( WebView webView : mPagerAdapter.pageList ) {
////            if (webView != null) {
//            webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
//            webView.removeAllViews();
//            webView.clearHistory();
//            webView.destroy();
//            webView.setWebViewClient(null);
//            KLog.e("销毁" + webView );
//            webView = null;
////            }
//        }
        artHandler.removeCallbacksAndMessages(null);
        viewPager.removeAllViews();
        this.context = null;
    }


    private static String fileTitle = "";

    /**
     * articleID = getIntent().getExtras().getString("articleID");
     * KLog.d("【article】" + articleID);
     * article = WithDB.getInstance().getArticle(articleID);
     */
    private void initData() {
        KLog.d("【initData】" + articleID);
        articleNo = getIntent().getExtras().getInt("articleNo"); // 文章在列表中的位置编号，下标从 0 开始
        articleCount = getIntent().getExtras().getInt("articleCount"); // 列表中所有的文章数目
    }


//    class JSObject{
//    }

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
        Img imgMeta;
        imgMeta = WithDB.getInstance().getImg(articleID, imgNo);
        if (imgMeta == null) {
            UToast.showShort("没有找到图片1");
            return;
        }

//        if (!HttpUtil.canDownImg()) {
////            artHandler.sendEmptyMessage(API.F_Request);
//            return ;}
        if (WithSet.getInstance().isDownImgWifi() && !HttpUtil.isWiFiActive()) {
            UToast.showShort("你开启了省流量模式，非 Wifi 不能下图片啦");
            return;
        } else if (!WithSet.getInstance().isDownImgWifi() && !HttpUtil.isNetworkAvailable()) {
            UToast.showShort("小伙子，你的网络无法使用啊");
            return;
        }

        String savePath = UFile.getRelativeDir(article.getSaveDir()) + fileTitle + "_files" + File.separator + imgMeta.getName();
        KLog.i("图片的保存目录为1：" + savePath);
        KLog.i("图片的下载地址为：" + imgMeta.getSrc());
        App.mNeter.loadImg(articleID, imgNo, imgMeta.getSrc(), savePath);
    }


    private void replaceSrc(final int imgNo, final String localSrc) {
//        if (!ArticleActivity.articleID.equals(articleID)  || webView == null) {
//            return;
//        }
        final int no = imgNo-1; // 因为图片的标号是从 1 开始，而 DOM 中，要从 0 开始。
//        KLog.e("替换src" + no + article.getSaveDir() + article.getTitle() + "：" + localSrc);
        if (null == webView) {
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

//        final int pageIndex =  mPagerAdapter.pageArticleIdMap.keyAt( mPagerAdapter.pageArticleIdMap.indexOfValue( articleID ) );
//        mPagerAdapter.pageList.get( pageIndex ).post( new Runnable() {
//            @Override
//            public void run() {
//                if( null == mPagerAdapter.pageList.get( pageIndex ) ){
//                    return;
//                }
//                mPagerAdapter.pageList.get( pageIndex ).loadUrl("javascript:(function(){" +
//                        "imgList = document.getElementsByTagName(\"img\");" +
//                        "imgList[" + no + "].src = \"" + localSrc + "\"" +
//                        "})()");
//            }
//        });


    }


    @Override
    public void notifyDataChanged() {
    }
    // 非静态匿名内部类的实例，所以它持有外部类Activity的引用
    // 所以此处的 handler 会持有外部类 Activity 的引用，消息队列是在一个Looper线程中不断轮询处理消息。
    // 那么当这个Activity退出时消息队列中还有未处理的消息或者正在处理消息，而消息队列中的Message持有mHandler实例的引用，mHandler又持有Activity的引用，所以导致该Activity的内存资源无法及时回收，引发内存泄漏

    //    private Handler mHandler = new Handler();
    @Override
    public void onClick(View v) {
        KLog.d("【 toolbar 是否双击 】 vScrolllayout");
        switch (v.getId()) {
            case R.id.art_toolbar_num:
            case R.id.art_toolbar:
                if (artHandler.hasMessages(API.MSG_DOUBLE_TAP)) {
                    artHandler.removeMessages(API.MSG_DOUBLE_TAP);
//                    vScrolllayout.smoothScrollTo(0, 0);

                } else {
                    artHandler.sendEmptyMessageDelayed(API.MSG_DOUBLE_TAP, ViewConfiguration.getDoubleTapTimeout());
                }
                break;
        }
    }

    private final ArtHandler artHandler = new ArtHandler(this);

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
//                    mActivity.get().initData(); // 内容重载
                    break;

//
//                case API.S_BITMAP:
//                    articleID = msg.getData().getString("articleID");
//                    imgNo = msg.getData().getInt("imgNo");
//                    Img imgMeta = WithDB.getInstance().getImg( articleID, imgNo );
////                    Img imgMeta = App.lossImgListArray.get(articleID).get( imgNo );
//                    if( imgMeta != null ){
//                        imgMeta.setDownState(1);
//                        WithDB.getInstance().saveImg(imgMeta);
//                        mActivity.get().replaceSrc( imgNo, "./" + fileTitle + "_files" + File.separator + imgMeta.getName());
////                        App.lossImgListArray.get(articleID).remove( imgNo );
//                    }
//                    if( WithDB.getInstance().getLossImgs(articleID).size() == 0 ){
////                    if( App.lossImgListArray.get(articleID).size() == 0 ){
//                        KLog.i("【图片全部下载完成】" );
//                        mActivity.get().setImgState(1);
//                        UToast.showShort("图片下载完成");
//                    }
//
//                    KLog.i("【1】" + imgMeta);
//                    break;
//                case API.F_BITMAP:
//                    articleID = msg.getData().getString("articleID");
//                    imgNo = msg.getData().getInt("imgNo");
//                    filePath = msg.getData().getString("filePath");
//
//                    if (mActivity.get().failImgList == null) {
//                        mActivity.get().failImgList = new SparseIntArray(mActivity.get().numOfImgs);
//                    }
//                    mActivity.get().numOfFailureImg = mActivity.get().failImgList.get(imgNo, 0);
//                    mActivity.get().failImgList.put(imgNo, mActivity.get().numOfFailureImg + 1);
////                    numOfFailureImg = numOfFailureImg + 1;
//                    KLog.i("【 API.F_BITMAP 】" + imgNo + "=" + mActivity.get().numOfFailureImg + "--" + mActivity.get().numOfImgs);
//                    if (mActivity.get().numOfFailureImg > mActivity.get().numOfFailures) {
//                        UToast.showShort( "图片无法下载，请稍候再试" );
//                        break;
//                    }
//                    mNeter.loadImg(articleID, imgNo, url, filePath);

                case API.ReplaceImgSrc:
                    imgNo = msg.getData().getInt("imgNo");
                    String imgName = msg.getData().getString("imgName");
                    mActivity.get().replaceSrc(imgNo, "./" + fileTitle + "_files" + File.separator + imgName);

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
//                    mActivity.get().numOfFailure = mActivity.get().numOfFailure + 1;
//                    if (mActivity.get().numOfFailure < 3) {
//                        mNeter.forData(url,API.request,0);
//                        break;
//                    }
                    UToast.showShort("网络不好，中断");
                    break;
                case API.F_NoMsg:
                    break;
                case API.H_WEB:

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

    private void setImgState(String downState) {
        article.setImgState(downState);
        KLog.e("【储存的 setImgStatus 】" + downState);
        WithDB.getInstance().saveArticle(article);
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
//        KLog.d(articleHtml);
        }

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


    public void onReadClick(View view) {
        if (article.getReadState().equals(API.ART_READ)) {
            changeReadIcon(API.ART_READING);
            UToast.showShort("未读：" + article.getTitle());
        }else {
            changeReadIcon(API.ART_READ);
            UToast.showShort("已读：" + article.getTitle());
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
//            App.finishActivity(this);
            back();
            return true;//返回真表示返回键被屏蔽掉
        }
        return super.onKeyDown(keyCode, event);
    }

    private void back() {
        Intent data = new Intent();
        data.putExtra("articleNo", articleNo);
        ArticleActivity.this.setResult(3, data);//注意下面的RESULT_OK常量要与回传接收的Activity中onActivityResult（）方法一致
        App.finishActivity(this);//关闭当前activity
    }


    private int articleNo, articleCount;

    /**
     * 显示 page 的文章
     *
     * @param article
     */
    public void showingPageData(Article article, WebView webView, int position) {
        this.article = article;
        this.webView = webView;
        KLog.i("--------------------------------------------------------------------------");
        KLog.i("显示页面showArticle：" + article.getTitle() + "====" + webView);
        articleID = article.getId();
        App.currentArticleID = articleID;
        initIconState(article, position);

        if (article.getSaveDir().equals(API.SAVE_DIR_CACHE)) {
            fileTitle = UString.stringToMD5(article.getId());
        } else {
            fileTitle = article.getTitle();
        }
        handleWebViewImg();
    }

//    private int lossImgSize = 0;
//    private void showingWebView(){
//        KLog.i( "初始化所有图片占位符a" +  webView );
//        if (article.getImgState() == null ) { // 首次打开
//            webView.loadUrl("javascript:initImgPlaceholder()");
//        } else if (article.getImgState().equals( API.ImgState_Downing )) { // 未下载完
//
//            final SparseArray<Img> lossImgMap = WithDB.getInstance().getLossImg(articleID);
//            lossImgSize = lossImgMap.size();
//            if( lossImgSize == 0 ){
//                return;
//            }
//            KLog.d("相关信息：" + articleID + lossImgMap);
////            App.lossImgListArray.put(articleID, lossImgMap);
//            StringBuilder imgNoArray = new StringBuilder("");
//            for (int i = 0; i < lossImgSize; i++) {
//                imgNoArray.append(lossImgMap.keyAt(i) - 1); // imgState 里的图片下标是从1开始的
//                imgNoArray.append("_");
//            }
////            imgNoArray.deleteCharAt(imgNoArray.length()-1);
//            KLog.d("传递的值" + imgNoArray );
//            webView.loadUrl("javascript:appointImgPlaceholder(" + "\"" + imgNoArray.toString() + "\"" + ")");
//            artHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mNeter.downImgs(articleID, lossImgMap, UFile.getRelativeDir(article.getSaveDir()) + fileTitle + "_files" + File.separator);
//                }
//            }, 500);
////            mNeter.downImgs(articleID, lossSrcList, UFile.getRelativeDir(article.getSaveDir()) + fileTitle + "_files" + File.separator);
//        }
//    }


    private void handleWebViewImg() {
        if (article.getImgState() == null) { // 首次打开
//            KLog.i( "初始化所有图片占位符a" +  webView );
            webView.loadUrl("javascript:initImgPlaceholder()"); // 初始化占位图
        } else if (article.getImgState().equals(API.ImgState_Downing)) { // 未下载完
            final ArrayMap<Integer, Img> lossImgMap = WithDB.getInstance().getLossImgs(articleID);
            int lossImgNum = lossImgMap.size();
            if (lossImgNum == 0) {
                setImgState(API.ImgState_Down_Over);
                return;
            }
            KLog.i("相关信息：" + articleID + "===" + lossImgMap);
//            App.lossImgListArray.put(articleID, lossImgMap);
            StringBuilder imgNoArray = new StringBuilder("");
            for (int i = 0; i < lossImgNum; i++) {
                imgNoArray.append(lossImgMap.keyAt(i) - 1); // imgState 里的图片下标是从1开始的
                imgNoArray.append("_");
            }
            KLog.d("传递的值" + imgNoArray);
            webView.loadUrl("javascript:appointImgPlaceholder(" + "\"" + imgNoArray.toString() + "\"" + ")");
            artHandler.post(new Runnable() {
                @Override
                public void run() {
                    App.mNeter.downImgs(articleID, lossImgMap, UFile.getRelativeDir(article.getSaveDir()) + fileTitle + "_files" + File.separator);
                }
            });
//            mNeter.downImgs(articleID, lossSrcList, UFile.getRelativeDir(article.getSaveDir()) + fileTitle + "_files" + File.separator);
        }
    }

    private void initIconState(Article article, int position) {
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
        String numStr = " = " + String.valueOf(position + 1) + " / " + String.valueOf(articleCount);
        vArticleNum.setText(numStr);
        KLog.d("=====position" + position);
    }

    // Todo 从 FeedMe 中学习的
//    private GestureDetector gestureDetector;
//    public SparseArray<WebView> map = new SparseArray();
    public ViewPager viewPager;

    public void initPager() {
        viewPager = ((ViewPager) findViewById(R.id.art_viewpager));
        ArticleAdapter articleAdapter = new ArticleAdapter(this, viewPager, App.articleList);
        viewPager.setAdapter(articleAdapter);
        viewPager.setCurrentItem(articleNo, false); // 本句放到 ArticleAdapter 的构造器中是无效的。
    }

}
