package me.wizos.loread.activity;

import android.content.Context;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.blueware.agent.android.util.OneapmWebViewClient;
import com.socks.library.KLog;

import java.io.File;
import java.util.ArrayList;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.Article;
import me.wizos.loread.bean.RequestLog;
import me.wizos.loread.bean.x.StringAndList;
import me.wizos.loread.bean.x.Strings;
import me.wizos.loread.dao.WithDB;
import me.wizos.loread.net.API;
import me.wizos.loread.net.Neter;
import me.wizos.loread.net.Parser;
import me.wizos.loread.utils.UFile;
import me.wizos.loread.utils.UString;
import me.wizos.loread.utils.UTime;
import me.wizos.loread.utils.UToast;

public class ArticleActivity extends BaseActivity implements Neter.LogRequest {
    private String label = "Article - ";
    protected WebView webView; // implements Html.ImageGetter
    protected Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        context = this;
        App.addActivity(this);
        mNeter = new Neter(handler,this);
        mNeter.setLogRequestListener(this);
        initView();
        initData();
    }

    protected Neter mNeter;
    protected Parser mParser = new Parser();

    @Override
    protected void onResume(){
        super.onResume();
    }


    @Override
    protected Context getActivity(){
        return context;
    }
    protected static final String TAG = "ArticleActivity";
    public String getTAG(){
        return TAG;
    }

    protected TextView vTitle ,vDate ,vTime ,vFeed;
    protected ImageView vStar , vRead;
    protected NestedScrollView vScrolllayout ;
    protected TextView vArticleNum;
//    protected String webUrl;
    private void initView() {
        initSystemBar();
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

        // Make arrow color white
//        Drawable upArrow = getResources().getDrawable(R.drawable.mz_ic_sb_back);
//        upArrow.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
//        getSupportActionBar().setHomeAsUpIndicator(upArrow); // 替换返回箭头
    }

    private void initWebView(){
        webView = (WebView) findViewById(R.id.article_content);
        WebSettings webSettings = webView.getSettings();
        webSettings.setUseWideViewPort(false);// 设置此属性，可任意比例缩放
        webSettings.setDisplayZoomControls(false); //隐藏webview缩放按钮
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 就是这句使自适应屏幕
        webSettings.setLoadWithOverviewMode(true);// 缩放至屏幕的大小
        setOneapmWebViewWatch();
    }

    private void setOneapmWebViewWatch(){
        /**
         * 为了监控 webView 的性能
         */
        OneapmWebViewClient client = new OneapmWebViewClient( webView){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }
        };
        webView.setWebViewClient(client);
    }

    protected String articleID="";
    private int numOfImgs,numOfGetImgs = 0 ,numOfFailureImg = 0 ,numOfFailure = 0 ,numOfFailures = 4;
    private String showContent = "";
    private Article article;
    protected int articleNum,articleCount;
    private void initData(){
        articleID = getIntent().getExtras().getString("articleID");
        articleNum = getIntent().getExtras().getInt("articleNum");
        articleCount = getIntent().getExtras().getInt("articleCount");
        article = WithDB.getInstance().getArticle(articleID);
        KLog.d("【article】" + articleID);
        if ( article == null ){ KLog.d("【article为空】");return; }
        String webUrl = article.getCanonical();
        sReadState = article.getReadState();
        sStarState = article.getStarState();
        Spanned titleWithUrl = Html.fromHtml("<a href=\"" + webUrl +"\">" + article.getTitle() + "</a>");
        vTitle.setText( titleWithUrl );
        vDate.setText(UTime.formatDate(article.getCrawlTimeMsec()));
//        vFeed.setText(article.getFeed().getTitle());
//        articleID = article.getId();
        numOfGetImgs = 0;
        String fileNameInMD5 = UString.stringToMD5(articleID);
        String content = UFile.readHtml( fileNameInMD5 );
        KLog.d( "【article状态为】" + sReadState + titleWithUrl );
        String imgState = article.getImgState();// 读取失败 imgSrc 的字段 , 如果读取的为 ok 代表加载完成，如果为空 or "" ，代表要提取正文与srcList加载图片  ,content

        String cssFileName = "normalize.css";
        String folderAbsolutePath = "file:"+ File.separator + File.separator + getExternalFilesDir(null)+ File.separator + "config" + File.separator;
        String cssPath = folderAbsolutePath + cssFileName;
        if(!UFile.isFileExists(cssPath)){
            cssPath = "file:///android_asset/" + cssFileName;
            KLog.d("自定义的 css 文件不存在");
        }

        String contentHeader = "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>" + "<link rel=\"stylesheet\" href=\"" + cssPath +"\" type=\"text/css\"/>" + "</head><body>";
        String contentFooter = "</body></html>";

        if(UString.isBlank(content)){
            KLog.d( "【文章内容被删，再去加载获取内容】" + webUrl);
            mNeter.postArticleContents(articleID);
        }else {
            if(imgState == null){
                StringAndList htmlAndSrcList = getSrcListAndNewHtml(content, fileNameInMD5);
                if( htmlAndSrcList!= null){
                    srcList =  htmlAndSrcList.getList();
                    content = htmlAndSrcList.getString();
                }
            }else if(imgState.equals("OK")){
            }else {
                String[] srcArray = imgState.split(",");
                srcList = Strings.asValues(srcArray);
            }
            showContent = contentHeader + content + contentFooter;
            numOfImgs = mNeter.getBitmapList(srcList);
//            vArticleNum.setText(String.valueOf(articleNum) + " / " + String.valueOf(articleCount));
            vArticleNum.setText( fileNameInMD5.substring(0,9) ); // FIXME: 2016/5/3 测试
            webView.loadDataWithBaseURL(null, showContent , "text/html", "utf-8", null);
        }

        initStateView();
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
            webView.loadDataWithBaseURL(null, showContent, "text/html", "utf-8", null);  //  contentView.reload();这种刷新方法无效
            KLog.d("【重载】");
        }
        logSrcList();
    }
    protected ArrayList<Strings> srcList = new ArrayList<>();
    protected Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String info = msg.getData().getString("res");
            String url = msg.getData().getString("url");
            String filePath ="";
            int imgNum;
            KLog.d("【handler】" +  msg.what + handler + url );
            switch (msg.what) {
                case API.S_EDIT_TAG:
                    long logTime = msg.getData().getLong("logTime");
                    delRequest(logTime);
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
                    imgNum = msg.getData().getInt("imgNum");
                    numOfGetImgs = numOfGetImgs + 1;
                    srcList.get(imgNum).stringA = "OK";
                    KLog.i("【 API.S_BITMAP 】" + numOfGetImgs + "--" + numOfImgs);
                    if(  numOfImgs == numOfGetImgs || numOfGetImgs % 5 == 0) {
                        KLog.i("【 重新加载 webView 】" + numOfGetImgs % 5 );
                        notifyDataChanged();
                    }
                    break;
                case API.F_BITMAP:
                    imgNum = msg.getData().getInt("imgNum");
                    numOfFailureImg = numOfFailureImg + 1;
                    if (numOfFailureImg > numOfFailures){
                        numOfGetImgs = numOfImgs-1;
                        handler.sendEmptyMessage(API.S_BITMAP);
                        break;}
                    if (numOfFailureImg == 1){
                        url = UFile.reviseSrc(url);
                    }
                    filePath = msg.getData().getString("filePath");
                    mNeter.getBitmap(url, filePath, imgNum);
                    break;
                case API.FAILURE_Request:
                case API.FAILURE_Response:
                    numOfFailure = numOfFailure + 1;
                    if (numOfFailure > 4){break;}
                    mNeter.forData(url,API.request,0);
                    break;
            }
            return false;
        }
    });


    private String sReadState= "";
    private String sStarState= "";
    protected void logSrcList(){
        String urlState = "";
        int num = srcList.size();
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<num; i++){
            urlState = srcList.get(i).getStringA();
            if(!urlState.equals("OK")){
                sb.append(urlState + ","+ srcList.get(i).getStringB());
            }
        }
        Article article = WithDB.getInstance().getArticle(articleID);
        article.setImgState(sb.toString());
        WithDB.getInstance().saveArticle(article);
        KLog.d( "【 logSrcList 】" + getActivity() );
    }

    private void saveSrcList(){

    }





    private StringAndList getSrcListAndNewHtml(String oldHtml,String fileNameInMD5) {
        if (UString.isBlank(oldHtml))
            return null;
        int num = 0;

        int testStart = oldHtml.indexOf("<img ", 0);
        if( testStart != -1 ){
            testStart = oldHtml.indexOf(" src=\"", testStart);
            if( testStart != -1 ){
                int testEnd = oldHtml.indexOf("\"", testStart + 6);
                if( testEnd != -1){
                    String testUrl = oldHtml.substring(testStart + 6, testEnd);
                    String testType = testUrl.substring(testUrl.length() - API.MyFileType.length() ,testUrl.length()) ;
//                    testType = testUrl.substring(path.lastIndexOf("."));
                    KLog.d(label + "【测试 src 是否修改 】" + testUrl + "==" + testType );
                    if (testType.equals(API.MyFileType)){
                        return null;
                    }
                }
            }
        }

        String newHtml = oldHtml;
        KLog.d(label + "【文章1】");
//        list.add(fileNameInMD5);
        int indexStart = newHtml.indexOf("<img ", 0);
        while (indexStart != -1) {
            indexStart = newHtml.indexOf(" src=\"", indexStart);
            if (indexStart != -1) {
                int indexEnd = newHtml.indexOf("\"", indexStart + 6);
                if (indexEnd != -1) {
                    String netSrc = newHtml.substring(indexStart + 6, indexEnd);
                    String fileType = UFile.getFileExtByUrl(netSrc);
                    KLog.d(label + "【文章13】" + fileType );
                    num++;
                    String localSrc = App.cacheAbsolutePath + fileNameInMD5  + File.separator + fileNameInMD5 + "_files" + File.separator + fileNameInMD5 + "_" + num + API.MyFileType;
                    String loadSrc = App.cacheRelativePath  + fileNameInMD5 + File.separator + fileNameInMD5  + "_files" + File.separator + fileNameInMD5 + "_" + num + API.MyFileType;
                    newHtml = newHtml.substring(0, indexStart + 6) + localSrc + "\" " + "netsrc=\"" + netSrc + newHtml.substring(indexEnd, newHtml.length());
//                    list.add(netSrc);
                    srcInLocalNetArray.add(new Strings(netSrc,loadSrc));
                    indexEnd = indexStart + 6 + localSrc.length() + netSrc.length() + 10;
                    indexStart = newHtml.indexOf("<img ", indexEnd);
                    KLog.d(label + "【文章14】" + localSrc + newHtml);
                }
            }
        }
        if(srcInLocalNetArray.size()==0){return null;}
        showContent = newHtml;
        StringAndList htmlAndImgSrcList = new StringAndList();
        htmlAndImgSrcList.setList(srcInLocalNetArray);
        htmlAndImgSrcList.setString(newHtml);
        KLog.d(label + "【文章2】");
        UFile.saveHtml(fileNameInMD5, newHtml);
        return htmlAndImgSrcList;
    }
    protected ArrayList<Strings> srcInLocalNetArray = new ArrayList<>();

    private static final int MSG_DOUBLE_TAP = 0;
    private Handler mHandler = new Handler();
    @Override
    public void onClick(View v) {
        KLog.d( label + "【 toolbar 是否双击 】" );
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






    public void onReadClick(View view){
        if(sReadState.equals(API.ART_READ)){
            changeReadIcon(API.ART_UNREAD);
            UToast.showLong("未读");
        }else {
            changeReadIcon(API.ART_READ);
            UToast.showLong("已读");
        }
    }
    public void onStarClick(View view){
        if(sStarState.equals(API.ART_UNSTAR)){
            changeStarState(API.ART_STAR);
            UToast.showLong("已收藏");
        }else {
            changeStarState(API.ART_UNSTAR);
            UToast.showLong("取消收藏");
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


    @Override
    public void addRequest(RequestLog requestLog){
    }
    @Override
    public void delRequest(long index){
    }
}
