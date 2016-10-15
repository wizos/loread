package me.wizos.loread.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.socks.library.KLog;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.Article;
import me.wizos.loread.bean.Feed;
import me.wizos.loread.bean.Tag;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.gson.ExtraImg;
import me.wizos.loread.gson.SrcPair;
import me.wizos.loread.net.API;
import me.wizos.loread.net.Neter;
import me.wizos.loread.net.Parser;
import me.wizos.loread.utils.UFile;
import me.wizos.loread.utils.UString;
import me.wizos.loread.utils.UTime;
import me.wizos.loread.utils.UToast;

@SuppressLint("SetJavaScriptEnabled")
public class ArticleActivity extends BaseActivity {
    protected static final String TAG = "ArticleActivity";
    protected WebView webView; // implements Html.ImageGetter
    protected Context context;
    protected Neter mNeter;
    protected Parser mParser;
    protected TextView vTitle ,vDate ,vTime, vFeed;
    protected ImageView vStar , vRead;
    protected NestedScrollView vScrolllayout ;
    protected TextView vArticleNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        context = this;
        App.addActivity(this);
        mNeter = new Neter(handler,this);
//        mNeter.setLogRequestListener(this);
        mParser = new Parser();
        initView();
        initData();
    }


    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected Context getActivity(){
        return context;
    }
    public String getTAG(){
        return TAG;
    }


//    protected String webUrl;
    private void initView() {
        initToolbar();
        initWebView();
        vTitle = (TextView) findViewById(R.id.article_title);
        vDate = (TextView) findViewById(R.id.article_date);
//        vTime = (TextView) findViewById(R.id.article_time);
        vFeed = (TextView) findViewById(R.id.article_feed);
        vStar = (ImageView) findViewById(R.id.art_star);
        vRead = (ImageView) findViewById(R.id.art_read);
        vArticleNum =  (TextView)findViewById(R.id.article_num);
        vScrolllayout = (NestedScrollView) findViewById(R.id.art_scroll);
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

    private void initWebView(){
//        webView = (WebView) findViewById(R.id.article_content);
        webView = new WebView( getApplicationContext() );
        LinearLayout mll = (LinearLayout) findViewById(R.id.article_webview);
        mll.addView(webView);

        WebSettings webSettings = webView.getSettings();
//        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 默认不使用缓存
        webSettings.setUseWideViewPort(false);// 设置此属性，可任意比例缩放
        webSettings.setDisplayZoomControls(false); //隐藏webview缩放按钮
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 就是这句使自适应屏幕
        webSettings.setLoadWithOverviewMode(true);// 缩放至屏幕的大小
        webSettings.setJavaScriptEnabled(true);
        // 添加js交互接口类，并起别名 imagelistner
//        webView.addJavascriptInterface(new JavascriptInterface(this), "imagelistner");
//        webView.setWebViewClient(new MyWebViewClient());
//        setOneapmWebViewWatch();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.removeAllViews();
        webView.destroy();
    }

    private int numOfImgs,numOfGetImgs = 0 ,numOfFailureImg = 0 ,numOfFailure = 0 ,numOfFailures = 4;
    private Article article;
    private int articleNo, articleCount;
    private ExtraImg extraImg;
    private String showContent = "";
    private String articleID = "";
    private String sReadState = "";
    private String sStarState = "";
    private String htmlState = "";
    private String fileNameInMD5 = "";
    private String baseUrl = "";
    private ArrayList<SrcPair> lossSrcList, obtainSrcList;

    private void initData(){
        articleID = getIntent().getExtras().getString("articleID");
        articleNo = getIntent().getExtras().getInt("articleNum"); // 文章在列表中的位置编号
        articleCount = getIntent().getExtras().getInt("articleCount"); // 列表中所有的文章数目

        KLog.d("【article】" + articleID);
        article = WithDB.getInstance().getArticle(articleID);
        if ( article == null ){ KLog.d("【article为空】");return; }
        sReadState = article.getReadState();
        sStarState = article.getStarState();
//        String articleUrl = article.getCanonical();
        Spanned titleWithUrl = Html.fromHtml("<a href=\"" + article.getCanonical() +"\">" + article.getTitle() + "</a>");
        vTitle.setText( titleWithUrl );
        vDate.setText(UTime.getFormatDate(article.getCrawlTimeMsec()));
        Feed feed = article.getFeed();
        if(feed!=null){
            vFeed.setText( feed.getTitle());
        }

        fileNameInMD5 = UString.stringToMD5(articleID);
        ArrayList<String> htmlMsg = UFile.readHtml( fileNameInMD5 ,article.getTitle());
        if(htmlMsg != null){
            htmlState = htmlMsg.get(0);
            showContent = htmlMsg.get(1);
        }

//        KLog.d( "【article状态为】" + sReadState + titleWithUrl );
        numOfGetImgs = 0;
        String imgState = article.getImgState();// 读取失败 imgSrc 的字段 , 有4类值：
        // 1，null（未打开）；2，"" （无图且被打开）； 3，ok（有图且加载完成）；4，src list (代表要提取正文与srcList加载图片)

        if(UString.isBlank(showContent)){
            KLog.d( "【文章内容被删，再去加载获取内容】" );
            mNeter.postArticleContents(articleID);
        }else {
            if( imgState == null){
                KLog.d( "【imgState为null】");
                ArrayList<SrcPair> listOfSrcAndHtml = UString.getListOfSrcAndHtml(showContent, fileNameInMD5);
                if( listOfSrcAndHtml!= null){
                    showContent = listOfSrcAndHtml.get(0).getLocalSrc();
                    listOfSrcAndHtml.remove(0);
                    lossSrcList = listOfSrcAndHtml;
                    if( lossSrcList.size()!=0){
                        article.setCoverSrc( lossSrcList.get(0).getLocalSrc());
                        obtainSrcList = new ArrayList<>(lossSrcList.size());
                    }
                    extraImg = new ExtraImg();
                    extraImg.setImgState(0);
                    extraImg.setLossImgs(lossSrcList);
                    article.setImgState( new Gson().toJson(extraImg) );
                    KLog.d( "【判断ImgState是否为空】" + article.getImgState()==null);
                    UFile.saveCacheHtml( fileNameInMD5, showContent );
                }else {
                    article.setImgState("");
                }
                WithDB.getInstance().saveArticle(article);
            }else if( !imgState.equals("")){
                Gson gson = new Gson();
                Type type = new TypeToken<ExtraImg>() {}.getType();
                extraImg = gson.fromJson(imgState, type);
                if(extraImg.getImgState()==0){
                    lossSrcList = extraImg.getLossImgs();
                }
            }

//            <img src="占位符图片" data="/static/images/logo.png" id="img">
//            <script type="text/javascript">
//                window.onload = function () {
//                var i = document.getElementById('img');
//                var img = new Image();
//                img.src = i.getAttribute('data');
//                img.onload = function () {
//                    alert(1);
//                    i.src = img.src;
//                }
//            }
//            </script>

            String script =
                    "<script type=\"text/javascript\">" +
                            "(function (){"+
                            "var imageList = document.getElementsByTagName(\"img\");"+
                            "for(var i=0; i<imageList.length; i++){"+
                            "var image = imageList[i];"+
                            "image.href = image.src;"+
                            "image.src = \"file:///android_asset/placeholder.png\";"+
                            "image.alt = \"点击加载图片\";"+
                            "image.onclick = function(){"+
                            "this.src = this.href;" +
                            "return false;"+
                            "}"+
                            "}"+
                            "}());"+
                            "</script>";
            // 加载内部css样式
            String cssPath = "file:"+ File.separator + File.separator + getExternalFilesDir(null)+ File.separator + "config" + File.separator + "article.css";
            if(!UFile.isFileExists(cssPath)){
                cssPath = "file:///android_asset/" + "article.css";
                KLog.d("自定义的 css 文件不存在");
            }
            String contentHeader = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>" + "<link rel=\"stylesheet\" href=\"" + cssPath +"\" type=\"text/css\"/>" +  "</head><body>";
            String contentFooter = "</body></html>";
            showContent = contentHeader + showContent + contentFooter;
            numOfImgs = mNeter.getBitmapList(lossSrcList);


            if (fileNameInMD5!= null){
                vArticleNum.setText( fileNameInMD5.substring(0,10) ); // FIXME: 2016/5/3 测试
            }else {
                String numStr = String.valueOf(articleNo) + " / " + String.valueOf(articleCount);
                vArticleNum.setText( numStr );
            }

            notifyDataChanged();
//            webView.loadDataWithBaseURL( getBaseUrl(htmlState) , showContent , "text/html", "utf-8", null);
        }
        initStateView();
    }

    private String getBaseUrl(String htmlState){
        if (htmlState.equals("cache") || htmlState.equals("cacheBox")){
            return App.cacheAbsolutePath;
        }else if(htmlState.equals("cacheFolder")){
            return App.cacheAbsolutePath + fileNameInMD5 + "/";
        }else if(htmlState.equals("box")){
            return App.boxAbsolutePath;
        }
        return null;
    }



    // 注入js函数监听
    private void addImageClickListner() {
        // 这段js函数的功能就是，遍历所有的img几点，并添加onclick函数，函数的功能是在图片点击的时候调用本地java接口并传递url过去
        String script =
                "<script type=\"text/javascript\">" +
                        "(function (){"+
                        "var imageList = document.getElementsByTagName(\"img\");"+
                        "for(var i=0; i<imageList.length; i++){"+
                        "var image = imageList[i];"+
                        "image.href = image.src;"+
                        "image.src = \"file:///android_asset/placeholder.png\";"+
                        "image.alt = \"点击加载图片\";"+
                        "image.onclick = function(){"+
                        "this.src = this.href;" +
                        "return false;"+
                        "}"+
                        "}"+
                        "}());"+
                        "</script>";

        webView.loadUrl("javascript:(function(){" +
                "var objs = document.getElementsByTagName(\"img\"); " +
                "for(var i=0;i<objs.length;i++)  " +
                "{"
                + "    objs[i].onclick=function()  " +
                "    {  "
                + "        window.imagelistner.openImage(this.src);  " +
                "    }  " +
                "}" +
                "})()");
    }

    // js通信接口
    public class JavascriptInterface {
        private Context context;
        public JavascriptInterface(Context context) {
            this.context = context;
        }
        public void openImage(String img) {
            KLog.e( img );
//            Intent intent = new Intent();
//            intent.putExtra("image", img);
//            intent.setClass(context, ShowWebImageActivity.class);
//            context.startActivity(intent);
//            KLog.e( img );
        }
    }

    // 监听
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
//            view.getSettings().setJavaScriptEnabled(true);
            super.onPageFinished(view, url);
            webView.getSettings().setBlockNetworkImage(true);
            // html加载完成之后，添加监听图片的点击js函数
            KLog.d("加载完成诸如js函数" );
            addImageClickListner();
        }
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            view.getSettings().setJavaScriptEnabled(true);
            super.onPageStarted(view, url, favicon);
            webView.getSettings().setBlockNetworkImage(false);
        }

    }

    private void initStateView(){
        if(sReadState.equals(API.ART_UNREAD)) {
            vRead.setImageDrawable(getDrawable(R.drawable.ic_vector_all));
            sReadState = API.ART_READ;
            article.setReadState(API.ART_READ);
            WithDB.getInstance().saveArticle(article);
            mNeter.postReadArticle(articleID);
            KLog.d("【 ReadState 】" + WithDB.getInstance().getArticle(articleID).getReadState());
        }else if(sReadState.equals(API.ART_READ)){
            vRead.setImageDrawable(getDrawable(R.drawable.ic_vector_all));
        }else if(sReadState.equals(API.ART_READING)){
            vRead.setImageDrawable(getDrawable(R.drawable.ic_vector_unread));
        }

        if(sStarState.equals(API.ART_UNSTAR)){
            vStar.setImageDrawable(getDrawable(R.drawable.ic_vector_unstar));
        } else {
            vStar.setImageDrawable(getDrawable(R.drawable.ic_vector_star));
        }
    }


    @Override
    protected void notifyDataChanged(){
        if(showContent==null || showContent.equals("")){
            KLog.d("【重载 initData 】" );
            initData();
        }else {
            webView.loadDataWithBaseURL(getBaseUrl(htmlState), showContent, "text/html", "utf-8", null);  //  contentView.reload();这种刷新方法无效
            KLog.d("【重载】");
        }
    }
    protected Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String info = msg.getData().getString("res");
            String url = msg.getData().getString("url");
            String filePath ="";
            int imgNo;
            KLog.d("【handler】" +  msg.what + handler + url );
            switch (msg.what) {
                case API.S_EDIT_TAG:
                    long logTime = msg.getData().getLong("logTime");
                    if(!info.equals("OK")){
                        mNeter.forData(url,API.request,logTime);
                        KLog.d("【返回的不是 ok");
                    }
                    break;
                case API.S_ARTICLE_CONTENTS:
                    mParser.parseArticleContents(info);
                    notifyDataChanged(); // 通知内容重载
                    break;
                case API.S_BITMAP:
                    imgNo = msg.getData().getInt("imgNo");
                    numOfGetImgs = numOfGetImgs + 1;
                    obtainSrcList.add( lossSrcList.get(imgNo) );
                    lossSrcList.set(imgNo,null);
                    KLog.i("【 API.S_BITMAP 】" + numOfGetImgs + "--" + numOfImgs);
                    if(  numOfImgs == numOfGetImgs ) { // || numOfGetImgs % 5 == 0
                        KLog.i("【 重新加载 webView 】" + numOfGetImgs  );
                        logImgState(1);
                        webView.clearCache(true);
                        lossSrcList.clear();
//                        webView.notify();
                        notifyDataChanged();// 通知内容重载
                    }
                    break;
                case API.F_BITMAP:
                    imgNo = msg.getData().getInt("imgNo");
                    numOfFailureImg = numOfFailureImg + 1;
                    if ( numOfFailureImg > numOfFailures ){
                        numOfGetImgs = numOfImgs-1;
                        handler.sendEmptyMessage(API.S_BITMAP);
                        break;
                    }
//                    if (numOfFailureImg == 1){
//                        url = UFile.reviseSrc(url);
//                    }
                    filePath = msg.getData().getString("filePath");
                    mNeter.getBitmap(url, filePath, imgNo);
                    break;
                case API.FAILURE_Request:
                case API.FAILURE_Response:
                    if( info.equals("Authorization Required")){
                        UToast.showShort("请重新登录");
                        break;
                    }
                    numOfFailure = numOfFailure + 1;
                    if (numOfFailure > 4){break;}
                    mNeter.forData(url,API.request,0);
//                    logSrcList();
                    break;
                case 55:
//                    logSrcList("");
                    KLog.i("【网络不好，中断】");
                    break;
            }
            return false;
        }
    });



    private void logImgState(int imgState){
        if (lossSrcList==null){return;}
        if (lossSrcList.size()!=0) {
            extraImg.setLossImgs(lossSrcList);
//            Gson gson = new Gson();
//            json = gson.toJson(lossSrcList);
        }
//        KLog.d( "【 logSrcList =】" + json );
        extraImg.setImgState(imgState);
        article.setImgState( new Gson().toJson(extraImg) );
        WithDB.getInstance().saveArticle(article);
    }



    private static final int MSG_DOUBLE_TAP = 0;
    private Handler mHandler = new Handler();
    @Override
    public void onClick(View v) {
        KLog.d( "【 toolbar 是否双击 】" );
        switch (v.getId()) {
            case R.id.article_num:
            case R.id.art_toolbar:
                if (mHandler.hasMessages(MSG_DOUBLE_TAP)) {
                    mHandler.removeMessages(MSG_DOUBLE_TAP);
                    vScrolllayout.smoothScrollTo(0, 0);
                } else {
                    mHandler.sendEmptyMessageDelayed(MSG_DOUBLE_TAP, ViewConfiguration.getDoubleTapTimeout());
                }
                break;
        }
    }

    public void onStarClick(View view){
        if(sStarState.equals(API.ART_UNSTAR)){
            changeStarState(API.ART_STAR);
            UToast.showShort("已收藏");
        }else {
            changeStarState(API.ART_UNSTAR);
            UToast.showShort("取消收藏");
        }
    }
    public void onLabelClick(View view){

        final List<Tag> tagsList = WithDB.getInstance().loadTags();
        ArrayList<String> tags = new ArrayList<>(tagsList.size()) ;
        for( Tag tag: tagsList ) {
            tags.add(tag.getTitle());
        }

//        Pattern regex = Pattern.compile("user/"+ MainActivity.mUserID + "/label/" + ".*?");
//        final Matcher m = regex.matcher(article.getCategories());

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
                            if (cate.contains( "user/"+ MainActivity.mUserID + "/label/" )){
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

    public void onSaveClick(View view){
        String fileName = article.getTitle();
//        KLog.e("【getExternalFilesDir】" + getExternalFilesDir(null) );

        ArrayList<String> htmlMsg = UFile.readHtml(fileNameInMD5 ,fileName);
        String content="", htmlState ="", filePath = "";
        if(htmlMsg!=null){
            htmlState = htmlMsg.get(0);
            content = htmlMsg.get(1);
        }
        if (htmlState.equals("cache")){
            filePath = App.cacheRelativePath + fileNameInMD5;
            moveTobox(filePath, fileName, content);
        }else if(htmlState.equals("cacheFolder")){
            filePath = App.cacheRelativePath + fileNameInMD5 + File.separator + fileNameInMD5;
            moveTobox(filePath, fileName, content);
        }else if(htmlState.equals("box")){
            UToast.showShort("文件已存在");
        }else if(htmlState.equals("cacheBox")){
            UFile.moveFile( App.cacheRelativePath + fileName + ".html", App.boxRelativePath  + fileName + ".html" );// 移动文件
            UFile.moveDir( App.cacheRelativePath + fileName + "_files" , App.boxRelativePath + fileName + "_files" );// 移动目录
            UToast.showShort("文件导出成功");
        }
    }

    private void moveTobox(String filePath,String fileName, String content){
        String boxHtml = UString.reviseHtmlForBox( content ,fileName ) ;
        String fileContent = String.format( getResources().getString(R.string.box_html_format), "UTF-8",fileName, article.getCanonical() ,article.getAuthor(), UTime.getFormatDate( article.getCrawlTimeMsec() ),  boxHtml );
        UFile.saveBoxHtml(  fileName, fileContent  );

        String sourceFilePath = filePath + ".html";
        File cacheHtmlfile = new File(sourceFilePath);
        cacheHtmlfile.delete();
//        String targetFilePath = App.boxRelativePath  + fileName + ".html";
//        UFile.moveFile( sourceFilePath, targetFilePath );

        String soureDir = filePath + "_files";
        String targetDir = App.boxRelativePath + fileName + "_files";
        UFile.moveDir( soureDir , targetDir );// 移动文件

        KLog.e( "目录" + htmlState + filePath );
        UToast.showShort("文件导出成功");
        article.setCoverSrc(  App.boxAbsolutePath + fileName + "_files" + File.separator + UFile.getFileNameExtByUrl(article.getCoverSrc()) );
        WithDB.getInstance().saveArticle(article);
    }



    public void onReadClick(View view){
        if(sReadState.equals(API.ART_READ)){
            changeReadIcon(API.ART_UNREAD);
            UToast.showShort("未读");
        }else {
            changeReadIcon(API.ART_READ);
            UToast.showShort("已读");
        }
    }
    private void changeReadIcon(String iconState){
        sReadState = iconState; // 在使用过程中，只会 涉及 read 与 reading 的转换。unread 仅作为用户未主动修改文章状态是的默认状态，reading 不参与勾选为已读
        if(iconState.equals(API.ART_READ)){
            vRead.setImageDrawable(getDrawable(R.drawable.ic_vector_all));

            article.setReadState(API.ART_READ);
            WithDB.getInstance().saveArticle(article);

            mNeter.postReadArticle(articleID);
            KLog.d("【 标为已读 】");
        }else {
            vRead.setImageDrawable(getDrawable(R.drawable.ic_vector_unread));
            article.setReadState(API.ART_READING);
            WithDB.getInstance().saveArticle(article);
            mNeter.postUnReadArticle(articleID);
            KLog.d("【 标为未读 】");
        }
    }
    private void changeStarState(String iconState){
        sStarState = iconState;
        if(iconState.equals(API.ART_STAR)){
            vStar.setImageDrawable(getDrawable(R.drawable.ic_vector_star));
            article.setStarState(API.ART_STAR);
            WithDB.getInstance().saveArticle(article);
            mNeter.postStarArticle(articleID);
        }else {
            vStar.setImageDrawable(getDrawable(R.drawable.ic_vector_unstar));
            article.setStarState(API.ART_UNSTAR);
            WithDB.getInstance().saveArticle(article);
            mNeter.postUnStarArticle(articleID);
        }
    }
//    /**
//     * 为了监控 webView 的性能
//     */
//    private void setOneapmWebViewWatch(){
//        OneapmWebViewClient client = new OneapmWebViewClient( webView){
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                super.onPageFinished(view, url);
//            }
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                return super.shouldOverrideUrlLoading(view, url);
//            }
//        };
//        webView.setWebViewClient(client);
//    }

}
