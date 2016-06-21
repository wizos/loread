package me.wizos.loread.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.socks.library.KLog;
import com.yydcdut.sdlv.Menu;
import com.yydcdut.sdlv.MenuItem;
import com.yydcdut.sdlv.SlideAndDragListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.adapter.MainSlvAdapter;
import me.wizos.loread.adapter.MaterialSimpleListAdapter;
import me.wizos.loread.adapter.MaterialSimpleListItem;
import me.wizos.loread.bean.Article;
import me.wizos.loread.bean.RequestLog;
import me.wizos.loread.data.UpdateDB;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.gson.ItemRefs;
import me.wizos.loread.net.API;
import me.wizos.loread.net.Neter;
import me.wizos.loread.net.Parser;
import me.wizos.loread.utils.UDensity;
import me.wizos.loread.utils.UFile;
import me.wizos.loread.utils.ULog;
import me.wizos.loread.utils.UString;
import me.wizos.loread.utils.UToast;
import me.wizos.loread.view.SwipeRefresh;

public class MainActivity extends BaseActivity implements SwipeRefresh.OnRefreshListener ,Neter.Loger<RequestLog> {

    protected static final String TAG = "MainActivity";
    private Context context;
    private ImageView vReadIcon, vStarIcon;
    private ImageView vPlaceHolder;
    private TextView vToolbarCount,vToolbarHint;
    private Toolbar toolbar;
    private Menu mMenu;
    private SwipeRefresh mSwipeRefreshLayout;
    private SlideAndDragListView slv;

    private String sListState;
    private String sListTag;
    private boolean hadSyncAllStarredList = false;
    private boolean syncAllStarredList = false;
    private boolean syncFirstOpen = true;
    private boolean hadSyncLogRequest = false;
    private boolean orderTagFeed;
    private int clearBeforeDay;
    private long mUserID;
    private MainSlvAdapter mainSlvAdapter;
    private List<Article> articleList;
    private boolean hadArticleSlvSummary = true;
//    private String sListTagCount = "";

    protected Neter mNeter;
    protected Parser mParser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this ;
        UpdateDB.upgrade(this);
        UFile.setContext(this);
        App.addActivity(this);
        mNeter = new Neter(handler,this);
        mNeter.setLogRequestListener(this);
        mParser = new Parser();
        initToolbar();
        initSlvListener();
        initSwipe();
        initView();
        KLog.i("【一】" + toolbar.getTitle() );
        initData();
        KLog.i("【二】" + toolbar.getTitle());
        initLogService();
    }

    private void initLogService(){
        Intent intent = new Intent(this, ULog.class);
        startService(intent);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (mainSlvAdapter != null) {
            mainSlvAdapter.notifyDataSetChanged();
        }
        KLog.i("【onResume】" + sListState + "---" + toolbar.getTitle() + sListTag );
    }
    @Override
    protected Context getActivity(){
        return context;
    }
    public String getTAG(){
        return TAG;
    }

    @Override
    protected void onStart(){
        super.onStart();
    }

    protected void readSetting(){
        API.INOREADER_ATUH = WithSet.getInstance().getAuth();
        mUserID = WithSet.getInstance().getUseId();
        sListState = WithSet.getInstance().getListState();
        sListTag = "user/" +  mUserID + "/state/com.google/reading-list";
        syncFirstOpen = WithSet.getInstance().isSyncFirstOpen();
        syncAllStarredList = WithSet.getInstance().isSyncAllStarred();
        hadSyncAllStarredList = WithSet.getInstance().getHadSyncAllStarred();
        clearBeforeDay = WithSet.getInstance().getClearBeforeDay();
        orderTagFeed = WithSet.getInstance().isOrderTagFeed();
        KLog.i("【 readSetting 】ATUH 为" + API.INOREADER_ATUH + syncFirstOpen + "【mUserID为】" + hadSyncAllStarredList );
        KLog.i( WithSet.getInstance().getCachePathStarred() + WithSet.getInstance().getUseName() );
    }

    protected void initView(){
        vReadIcon = (ImageView)findViewById(R.id.main_read);
        vStarIcon = (ImageView)findViewById(R.id.main_star);
        vToolbarCount = (TextView)findViewById(R.id.main_toolbar_count);
        vToolbarHint = (TextView)findViewById(R.id.main_toolbar_hint);
        vPlaceHolder = (ImageView)findViewById(R.id.main_placeholder);
    }
    protected void initSwipe(){
        mSwipeRefreshLayout = (SwipeRefresh) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setProgressViewOffset(true, 20, 150);//设置样式刷新显示的位置
        mSwipeRefreshLayout.setViewGroup(slv);
//        appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
//        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
//            @Override
//            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
//                if (verticalOffset >= 0) {
//                    mSwipeRefreshLayout.setEnabled(true);
//                } else {
//                    mSwipeRefreshLayout.setEnabled(false);
//                }
//            }
//        });
    }

    @Override
    public void onRefresh() {
        if(!mSwipeRefreshLayout.isEnabled()){return;}
        mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setRefreshing(false);  // 调用 setRefreshing(false) 去取消任何刷新的视觉迹象。如果活动只是希望展示一个进度条的动画，他应该条用 setRefreshing(true) 。 关闭手势和进度条动画，调用该 View 的 setEnable(false)

//        if( hadSyncLogRequest ){ // 防止在更新 logRequest 时又去开始刷新
//            handler.sendEmptyMessage(API.M_BEGIN_SYNC);
//        }
        handler.sendEmptyMessage(API.M_BEGIN_SYNC);
        KLog.i("【刷新中】" + hadSyncLogRequest);
    }
    @Override
    protected void notifyDataChanged(){
        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setEnabled(true);
        UToast.showLong("刷新完成");
        reloadData();
    }

    protected void initData(){
        readSetting();
        initBottombarIcon();
        reloadData();  // 先加载已有数据
        if( syncFirstOpen && articleList.size() !=0 ){
            mSwipeRefreshLayout.setEnabled(false);
            handler.sendEmptyMessage(API.M_BEGIN_SYNC);
            KLog.i("首次开启同步");
            UToast.showLong("首次开启同步");
        }else {
            List<Article> allArts = WithDB.getInstance().loadArtAll();  //  速度更快，用时更短，这里耗时 43,43
            if(allArts.size() == 0 && hadSyncLogRequest ){
                // 显示一个没有内容正在加载的样子
                handler.sendEmptyMessage(API.M_BEGIN_SYNC);
                UToast.showLong("首次同步");
            }
        }
        KLog.i("列表数目：" + articleList.size() + "  当前状态：" + sListState);
    }

    /**
     * sListState 包含 3 个状态：All，Unread，Stared
     * sListTag 至少包含 1 个状态： Reading-list
     * */
    protected void reloadData(){ // 获取 articleList , 并且根据 articleList 的到未读数目
        if(sListTag.contains(API.U_NO_LABEL)){
            articleList = getNoLabelList( );  // FIXME: 2016/5/7 这里的未分类暂时无法使用，因为在云端订阅源的分类是可能会变的，导致本地缓存的文章分类错误
        }else {
            if( sListState.equals(API.LIST_STAR) ){
                articleList = WithDB.getInstance().loadStarList(sListTag);
            }else{
                articleList = WithDB.getInstance().loadReadList(sListState,sListTag); // 590-55
            }
        }
        KLog.i("【】" + articleList.size() + sListState + "--" + sListTag);

        if(UString.isBlank(articleList)){
            vPlaceHolder.setVisibility(View.VISIBLE);
            slv.setVisibility(View.GONE);
            UToast.showLong("没有文章"); // 弹出一个提示框，询问是否同步
        }else {
            vPlaceHolder.setVisibility(View.GONE);
            slv.setVisibility(View.VISIBLE);
        }
        KLog.i("【notify1】" + sListState + sListTag  + toolbar.getTitle() + articleList.size());
        mainSlvAdapter = new MainSlvAdapter(this, articleList);
        slv.setAdapter(mainSlvAdapter);
        mainSlvAdapter.notifyDataSetChanged();
        KLog.i("【notify2】" + articleList.size() + "--" + mainSlvAdapter.getCount());
        changeToolbarTitle();
        tagCount = articleList.size();
        setItemNum( tagCount );
    }


    private List<Article> getNoLabelList(){
        List<Article> all,part,exist;
        if( sListState.contains(API.LIST_STAR) ){
            all = WithDB.getInstance().loadStarAll();
            part = WithDB.getInstance().loadStarListHasLabel(mUserID);
            exist = WithDB.getInstance().loadStarNoLabel();
        }else {
            all = WithDB.getInstance().loadReadAll( sListState );
            part = WithDB.getInstance().loadReadListHasLabel( sListState,mUserID);
            exist = WithDB.getInstance().loadReadNoLabel();
       }

        ArrayList<Article> noLabel = new ArrayList<>( all.size() - part.size() );
        Map<String,Integer> map = new HashMap<>( part.size());
        String articleId;
        StringBuffer sb = new StringBuffer(0);

        for( Article article: part ){
            articleId = article.getId();
            map.put(articleId,1);
        }
        for( Article article: all ){
            articleId = article.getId();
            Integer cc = map.get( articleId );
            if(cc!=null) {
                map.put( articleId , ++cc);
            }else {
                sb = new StringBuffer();
                sb.append(article.getCategories());
                sb.insert( sb.length()-1 , ", \"user/"+ mUserID + API.U_NO_LABEL +"\"");
                article.setCategories( sb.toString() );
                noLabel.add( article );
            }
        }
        KLog.d( sb.toString() +" - "+  all.size() + " - "+ part.size());
        noLabel.addAll( exist );
        return noLabel;
    }



    /**
     * 异步类: AsyncTask 和 Handler 对比
     * 1 ） AsyncTask实现的原理,和适用的优缺点
     * AsyncTask,是android提供的轻量级的异步类,可以直接继承AsyncTask,在类中实现异步操作,并提供接口反馈当前异步执行的程度(可以通过接口实现UI进度更新),最后反馈执行的结果给UI主线程.
     * 使用的优点: 1、简单,快捷；2、过程可控
     * 使用的缺点：在使用多个异步操作和并需要进行Ui变更时,就变得复杂起来.
     * 2 ）Handler异步实现的原理和适用的优缺点
     * 在Handler 异步实现时,涉及到 Handler, Looper, Message,Thread 四个对象，实现异步的流程是主线程启动 Thread（子线程）àthread(子线程)运行并生成Message-àLooper获取Message并传递给HandleràHandler逐个获取Looper中的Message，并进行UI变更。
     * 使用的优点：1、结构清晰，功能定义明确；2、对于多个后台任务时，简单，清晰
     * 使用的缺点：在单个后台异步处理时，显得代码过多，结构过于复杂（相对性）
     *
     *
     * 采用线程+Handler实现异步处理时，当每次执行耗时操作都创建一条新线程进行处理，性能开销会比较大。另外，如果耗时操作执行的时间比较长，就有可能同时运行着许多线程，系统将不堪重负。
     * 为了提高性能，我们可以使用AsynTask实现异步处理，事实上其内部也是采用线程+Handler来实现异步处理的，只不过是其内部使用了线程池技术，有效的降低了线程创建数量及限定了同时运行的线程数。
     */
    private int urlState = 0 ,capacity,getNumForArts = 0,numOfFailure = 0;
    private ArrayList<ItemRefs> afterItemRefs = new ArrayList<>();
    protected Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String info = msg.getData().getString("res");
            String url = msg.getData().getString("url");

//            long logTime = msg.getData().getLong("logTime");
            // 虽然可以根据 api 来判断一条请求，但还是需要 时间 logTime ，还有 指定码 code
            KLog.i("【handler】"  + msg.what +"---"  + handler +"---" + mParser );
            switch (msg.what) {
                case API.M_BEGIN_SYNC:
                    if( syncRequestLog()){
                        break;
                    }
                    KLog.i("【获取所有加星文章1】" + hadSyncAllStarredList + "---" + syncAllStarredList);
                    if( !hadSyncAllStarredList && syncAllStarredList ){
                        vToolbarHint.setText(R.string.main_toolbar_hint_sync_all_stared_content);
                        KLog.i("【获取所有加星文章2】" + hadSyncAllStarredList + "---" + msg.what);
                        mNeter.getStarredContents();
                        break;
                    }else {
                        vToolbarHint.setText(R.string.main_toolbar_hint_sync_tag);
                        mNeter.getWithAuth(API.U_TAGS_LIST);
                        KLog.i("【获取0】");
                    }
                    KLog.i("【获取1】");
                    break;
                case API.S_TAGS_LIST:
                    mParser.parseTagList(info);
                    if(orderTagFeed){
                        vToolbarHint.setText(R.string.main_toolbar_hint_sync_tag_order);
                        mNeter.getWithAuth(API.U_STREAM_PREFS);// 有了这份数据才可以对 tagslist feedlist 进行排序，并储存下来
                    }else {
                        mParser.orderTags();
                        vToolbarHint.setText(R.string.main_toolbar_hint_sync_unread_count);
                        mNeter.getWithAuth(API.U_UNREAD_COUNTS);
                    }
                    break;
                case API.S_STREAM_PREFS:
                    mParser.parseStreamPrefList(info, mUserID);
                    vToolbarHint.setText(R.string.main_toolbar_hint_sync_unread_count);
                    mNeter.getWithAuth(API.U_UNREAD_COUNTS);
                    break;
                case API.S_UNREAD_COUNTS:
                    mParser.parseUnreadCounts(info);
                    vToolbarHint.setText( R.string.main_toolbar_hint_sync_unread_refs );
                    mNeter.getUnReadRefs(API.U_ITEM_IDS, mUserID);
                    urlState = 1;
                    KLog.d("【未读数】");
                    break;
                case API.S_ITEM_IDS:
                    if (urlState == 1){
                        String continuation = mParser.parseItemIDsUnread(info);
                        if(continuation!=null){
                            mNeter.addHeader("c", continuation);
                            mNeter.getUnReadRefs(API.U_ITEM_IDS, mUserID);
                            KLog.i("【获取 ITEM_IDS 还可继续】" + continuation);
                        }else {
                            urlState = 2;
                            vToolbarHint.setText( R.string.main_toolbar_hint_sync_stared_refs);
                            mNeter.getStarredRefs(API.U_ITEM_IDS, mUserID);
                        }
                    }else if(urlState ==2){
                        String continuation = mParser.parseItemIDsStarred(info);
                        if(continuation!=null){
                            mNeter.addHeader("c", continuation);
                            mNeter.getStarredRefs(API.U_ITEM_IDS, mUserID);
                        }else {
                            ArrayList<ItemRefs> unreadRefs = mParser.reUnreadRefs();
                            ArrayList<ItemRefs> starredRefs = mParser.reStarredRefs();
                            capacity = mParser.reRefs(unreadRefs, starredRefs);
                            afterItemRefs = new ArrayList<>( capacity );
                            handler.sendEmptyMessage(API.S_ITEM_CONTENTS);// 开始获取所有列表的内容
                            urlState = 1;
                            KLog.i("【BaseActivity 获取 reUnreadList】");
                        }
                    }
                    break;
                case API.S_ITEM_CONTENTS:
                    KLog.i("【Main 解析 ITEM_CONTENTS 】" + urlState );
                    if(urlState == 0){
                    }else if(urlState == 1){
                        afterItemRefs = mParser.reUnreadUnstarRefs;
                        mParser.parseItemContentsUnreadUnstar(info);
                    }else if(urlState == 2){
                        afterItemRefs = mParser.reUnreadStarredRefs;
                        mParser.parseItemContentsUnreadStarred(info);
                    }else if(urlState == 3){
                        afterItemRefs = mParser.reReadStarredRefs;
                        mParser.parseItemContentsReadStarred(info);
                    }

                    vToolbarHint.setText(getString(R.string.main_toolbar_hint_sync_article_content,getNumForArts,capacity));
                    ArrayList<ItemRefs> beforeItemRefs = new ArrayList<>( afterItemRefs );
                    int num = beforeItemRefs.size();
//                    KLog.i("【获取 ITEM_CONTENTS 1】" + urlState +" - "+ afterItemRefs.size() + "--" + num);
                    if(num!=0){
                        if( beforeItemRefs.size()==0){return false;}
                        if(num>50){ num = 50; }
                        for(int i=0; i<num; i++){ // 给即将获取 item 正文 的请求构造包含 item 地址 的头部
                            String value = beforeItemRefs.get(i).getId();
                            mNeter.addBody("i", value);
                            afterItemRefs.remove(0);
//                            KLog.i("【获取 ITEM_CONTENTS 3】" + num + "--" + afterItemRefs.size());
                        }
                        getNumForArts = getNumForArts + num;
                        mNeter.postWithAuth(API.U_ITEM_CONTENTS);
                    }else {
                        if(urlState == 0){
                            urlState = 1;
                        }else if(urlState == 1){
                            urlState = 2;
                        }else if(urlState == 2){
                            urlState = 3;
                        }else if(urlState == 3){
                            urlState = 0;
                            handler.sendEmptyMessage(100);
                            return false;
                        }
                        handler.sendEmptyMessage(API.S_ITEM_CONTENTS);
                    }
                    break;
                case API.S_STREAM_CONTENTS_STARRED:
                    String continuation = mParser.parseStreamContentsStarred(info);
                    KLog.i("【解析所有加星文章1】" + urlState  + "---" + continuation);
                    if(continuation!=null){
                        mNeter.addHeader("c", continuation);
                        mNeter.getStarredContents();
                        KLog.i("【获取 StarredContents 】" );
                    }else {
                        hadSyncAllStarredList = true;
                        WithSet.getInstance().setHadSyncAllStarred( hadSyncAllStarredList );
                        vToolbarHint.setText(R.string.main_toolbar_hint_sync_tag);
                        mNeter.getWithAuth(API.U_TAGS_LIST); // 接着继续
                    }
                    break;
                case API.S_EDIT_TAG:
                    long logTime = msg.getData().getLong("logTime");
                    del(logTime);
                    if(!info.equals("OK")){
                        mNeter.forData(url,API.request,logTime);
                        KLog.i("返回的不是 ok");
                    }
                    if( !hadSyncLogRequest && requestMap.size()==0 ){
//                        handler.sendEmptyMessage(API.M_BEGIN_SYNC) ;
                        hadSyncLogRequest = true;}
                    break;
                case API.S_Contents:
                    mParser.parseStreamContents(info);
                    break;
                case API.FAILURE:
                case API.FAILURE_Request:
                case API.FAILURE_Response:
                    numOfFailure = numOfFailure + 1;
                    if (numOfFailure > 2){
                        handler.sendEmptyMessage(55);
                        break;
                    }
                    mNeter.forData(url, API.request, msg.getData().getLong("logTime"));
                    break;
                case 88:
                    mParser.parseStreamContents(info);
                    break;
                case 100:
                    clearArticles(clearBeforeDay);
                    notifyDataChanged();
                    getNumForArts = 0;
                    vToolbarHint.setText("");
                    KLog.i("【文章列表获取完成】" );
                    break;
                case 55:
                    mSwipeRefreshLayout.setRefreshing(false);
                    mSwipeRefreshLayout.setEnabled(true);
                    vToolbarHint.setText("");
                    saveRequestList();
                    KLog.i("【网络不好，中断】");
                    break;
            }
            return false;
        }
    });





    private Map<Long,RequestLog> requestMap = new HashMap<>();
    @Override
    public void add(RequestLog requestLog){
        if(!requestLog.getHeadParamString().contains("c=")){
            requestMap.put( requestLog.getLogTime(),requestLog );
        }
    }
    @Override
    public void del(long index){
        if( requestMap != null){
            if(requestMap.size()!=0){
                requestMap.remove(index); // 因为最后一次使用 handleMessage(100) 时也会调用
            }
        }
    }
    private void saveRequestList(){
        if(requestMap==null){return;}
        KLog.i("【saveRequestList0】" );
        ArrayList<RequestLog> commitRequestList = new ArrayList<>( requestMap.size() );
        for( Map.Entry<Long,RequestLog> entry : requestMap.entrySet()) {
            commitRequestList.add(entry.getValue());
        }
        WithDB.getInstance().saveRequestLogList(commitRequestList);
        requestMap = new HashMap<>();
    }

    private boolean syncRequestLog(){
        List<RequestLog> requestLogs = WithDB.getInstance().loadRequestListAll();
        if( requestLogs.size()==0){
            return false;
        }
        vToolbarHint.setText( R.string.main_toolbar_hint_sync_log );
        WithDB.getInstance().delRequestListAll();
        hadSyncLogRequest = false;
        // TODO: 2016/5/26 将这个改为 json 格式来持久化 RequestLog 对象 ？貌似也不好
        for(RequestLog requestLog:requestLogs){
            requestMap.put(requestLog.getLogTime(),requestLog);
            String headParamString = requestLog.getHeadParamString();
            String bodyParamString = requestLog.getBodyParamString();
            mNeter.addHeader( UString.formStringToParamList(headParamString));
            mNeter.addBody( UString.formStringToParamList(bodyParamString));
            KLog.d("同步错误：" + headParamString + " = " + bodyParamString);
            if( requestLog.getMethod().equals("post")){
                mNeter.post( requestLog.getUrl(), requestLog.getLogTime() );
            }
        }
        KLog.d("读取到的数目： " +  requestLogs.size());
        return true;
    }

    public void clearArticles(int days){
        long clearTime = System.currentTimeMillis() - days*24*3600*1000L;
        List<Article> allArtsBeforeTime = WithDB.getInstance().loadArtsBeforeTime(clearTime);
        KLog.i("清除" + clearTime + "--"+  allArtsBeforeTime.size()  + "--"+  days );
        if( allArtsBeforeTime.size()==0){return;}
        ArrayList<String> idListMD5 = new ArrayList<>( allArtsBeforeTime.size() );
        for(Article article:allArtsBeforeTime){
            idListMD5.add(UString.stringToMD5(article.getId()));
        }
        UFile.deleteHtmlDirList(idListMD5);
        WithDB.getInstance().delArtAll(allArtsBeforeTime);
    }

    private int tagCount;
    private void changeItemNum(int offset){
        tagCount = tagCount + offset;
        vToolbarCount.setText(String.valueOf( tagCount ));
    }
    private void setItemNum(int offset){
        tagCount = offset;
        vToolbarCount.setText(String.valueOf( tagCount ));
    }
    private void changeToolbarTitle(){
        if(sListTag.contains(API.U_READING_LIST)){
            if( sListState.equals(API.LIST_STAR) ){
                tagName = "所有加星";
            }else if(sListState.equals(API.LIST_UNREAD)){
                tagName = "所有未读";
            }else {
                tagName = "所有文章";
            }
        }else if(sListTag.contains(API.U_NO_LABEL)){
            if( sListState.equals(API.LIST_STAR) ){
                tagName = "加星未分类";
            }else if(sListState.equals(API.LIST_UNREAD)){
                tagName = "未读未分类";
            }else {
                tagName = "所有未分类";
            }
        }
        toolbar.setTitle(tagName);
        KLog.d( sListTag + sListState + tagName );
    }

    private void initBottombarIcon(){
        if( sListState.equals(API.LIST_STAR) ){
            vStarIcon.setImageDrawable(getDrawable(R.drawable.ic_vector_star));
            vReadIcon.setImageDrawable(getDrawable(R.drawable.ic_vector_all));
        }else if(sListState.equals(API.LIST_UNREAD)){
            vStarIcon.setImageDrawable(getDrawable(R.drawable.ic_vector_unstar));
            vReadIcon.setImageDrawable(getDrawable(R.drawable.ic_vector_unread));
        }else {
            vStarIcon.setImageDrawable(getDrawable(R.drawable.ic_vector_unstar));
            vReadIcon.setImageDrawable(getDrawable(R.drawable.ic_vector_all));
        }
    }


    public void initSlvListener() {
        initSlvMenu();
        slv = (SlideAndDragListView)findViewById(R.id.main_slv);
        slv.setMenu(mMenu);
        slv.setOnListItemClickListener(new SlideAndDragListView.OnListItemClickListener() {
            @Override
            public void onListItemClick(View v, int position) {
                if(position==-1){return;}
                String articleID = articleList.get( position ).getId();
                Intent intent = new Intent(MainActivity.this , ArticleActivity.class);
                intent.putExtra("articleID", articleID);
                intent.putExtra("articleNum", position + 1);
                intent.putExtra("articleCount", articleList.size());
                startActivity(intent);
            }
        });
        slv.setOnSlideListener(new SlideAndDragListView.OnSlideListener() {
            @Override
            public int onSlideOpen(View view, View parentView, int position, int direction) {
                Article article = articleList.get(position);
                switch (direction) {
                    case MenuItem.DIRECTION_LEFT:
                        addStarList(article);
                        return Menu.ITEM_SCROLL_BACK;
                    case MenuItem.DIRECTION_RIGHT:
                        addReadList(article);
                        return Menu.ITEM_SCROLL_BACK;
                }
                return Menu.ITEM_NOTHING;
            }

            @Override
            public void onSlideClose(View view, View parentView, int position, int direction) {
            }
        });
        slv.setOnListItemLongClickListener(new SlideAndDragListView.OnListItemLongClickListener() {
            @Override
            public void onListItemLongClick(View view,final int position) {
                KLog.d("长按===");
                final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter( MainActivity.this);
                adapter.add(new MaterialSimpleListItem.Builder(MainActivity.this)
                        .content("向上标记已读")
                        .icon(R.drawable.ic_vector_mark_after)
                        .backgroundColor(Color.WHITE)
                        .build());
                adapter.add(new MaterialSimpleListItem.Builder(MainActivity.this)
                        .content("向下标记已读")
                        .icon(R.drawable.ic_vector_mark_before)
                        .backgroundColor(Color.WHITE)
                        .build());


                new MaterialDialog.Builder(MainActivity.this)
                        .adapter(adapter, new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                ArrayList<Article> artList = new ArrayList<>();
                                int i = 0,num = 0;
                                switch (which) {
                                    case 0:
                                        i=0;
                                        num = position + 1;
                                        artList = new ArrayList<>( position + 1 );
                                        break;
                                    case 1:
                                        i= position;
                                        num = articleList.size();
                                        artList = new ArrayList<>( num - position - 1 );
                                        break;
                                }

                                for(int n = i; n< num; n++){
                                    if( articleList.get(n).getReadState().equals(API.ART_UNREAD)){
                                        articleList.get(n).setReadState(API.ART_READ);
                                        artList.add( articleList.get(n) );
                                    }
                                }
                                addReadedList(artList);
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }



    private void addReadedList(ArrayList<Article> artList){
        if(artList.size() == 0){return;}
        for(Article artId: artList){
            mNeter.postReadArticle(artId.getId());
            changeItemNum( - artList.size() );
        }
        WithDB.getInstance().saveArticleList(artList);
        mainSlvAdapter.notifyDataSetChanged();
    }
    private void addReadList(Article article){
        if(article.getReadState().equals(API.ART_READ)){
            article.setReadState(API.ART_READING);
            mNeter.postUnReadArticle(article.getId());
            changeItemNum( + 1 );
            UToast.showShort("标为未读");
        }else {
            article.setReadState(API.ART_READ);
            mNeter.postReadArticle(article.getId());
            changeItemNum( - 1 );
            UToast.showShort("标为已读");
        }
        WithDB.getInstance().saveArticle(article);
        mainSlvAdapter.notifyDataSetChanged();
    }

    protected void addStarList(Article article){
        if(article.getStarState().equals(API.ART_STAR)){
            article.setStarState(API.ART_UNSTAR);
            mNeter.postUnStarArticle(article.getId());
        }else {article.setStarState(API.ART_STAR);
            mNeter.postStarArticle(article.getId());
        }
        WithDB.getInstance().saveArticle(article);
        mainSlvAdapter.notifyDataSetChanged();
    }

    private static final int MSG_DOUBLE_TAP = 0;
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_toolbar:
                if (handler.hasMessages(MSG_DOUBLE_TAP)) {
                    handler.removeMessages(MSG_DOUBLE_TAP);
                    slv.smoothScrollToPosition(0);
                } else {
                    handler.sendEmptyMessageDelayed(MSG_DOUBLE_TAP, ViewConfiguration.getDoubleTapTimeout());
                }
                break;
        }
    }



    private String tagName = "";
    @Override
    protected void onActivityResult(int requestCode , int resultCode , Intent intent){
        String tagId = "";
        int tagCount = 0 ;
        switch (resultCode){
            case RESULT_OK:
                tagId = intent.getExtras().getString("tagId");
                tagCount = intent.getExtras().getInt("tagCount");
                tagName = intent.getExtras().getString("tagName");
                break;
        }
//        KLog.i("【== onActivityResult 】" + tagId + "----" + sListTag);
        if( tagId == null){
            return;
        }
        if( !tagId.equals("")){
            sListTag = tagId;
//            sListTagCount = tagCount;
            KLog.i("【onActivityResult】" + sListTag + sListState);
            reloadData();
        }
    }
    public void onSettingIconClicked(View view){
        Intent intent = new Intent(getActivity(),SettingActivity.class);
        startActivity(intent);
    }
    //定义一个startActivityForResult（）方法用到的整型值
    public void onTagIconClicked(View view){
        int requestCode = 0;

        Intent intent = new Intent(MainActivity.this,TagActivity.class);
        intent.putExtra("ListState",sListState);
        intent.putExtra("ListTag",sListTag);
        intent.putExtra("ListCount",articleList.size());
//        intent.putExtra("NoLabelCount",getNoLabelList().size());
        startActivityForResult(intent, requestCode);
    }

    public void onStarIconClicked(View view){
        KLog.d( sListTag + sListState + tagName );
        if(sListState.equals(API.LIST_STAR)){
            UToast.showLong("已经在收藏列表了");
        }else {
            vReadIcon.setImageDrawable(getDrawable(R.drawable.ic_vector_all));
            vStarIcon.setImageDrawable(getDrawable(R.drawable.ic_vector_star));
            sListState = API.LIST_STAR;
            WithSet.getInstance().setListState(sListState);
            reloadData();
        }
    }
    public void onReadIconClicked(View view){
        KLog.d( sListTag + sListState + tagName );
        vStarIcon.setImageDrawable(getDrawable(R.drawable.ic_vector_unstar));
        if(sListState.equals(API.LIST_UNREAD)){
            vReadIcon.setImageDrawable(getDrawable(R.drawable.ic_vector_all));
            sListState = API.LIST_ALL;
        }else {
            vReadIcon.setImageDrawable(getDrawable(R.drawable.ic_vector_unread));
            sListState = API.LIST_UNREAD;
        }
        WithSet.getInstance().setListState(sListState);
        reloadData();
    }



    /**
     * 监听返回键，弹出提示退出对话框
     */
    @Override
    public boolean onKeyDown(int keyCode , KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){ // 后者为短期内按下的次数
            createDialog();// 创建弹出的Dialog
            return true;//返回真表示返回键被屏蔽掉
        }
        return super.onKeyDown(keyCode, event);
    }

    private void createDialog() {
        new AlertDialog.Builder(this)
                .setMessage("确定退出app?")
                .setPositiveButton("好滴 ^_^",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        App.finishAll();
                    }
                })
                .setNegativeButton("不！", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setOnClickListener(this);
        // setDisplayShowHomeEnabled(true)   //使左上角图标是否显示，如果设成false，则没有程序图标，仅仅就个标题，否则，显示应用程序图标，对应id为android.R.id.home，对应ActionBar.DISPLAY_SHOW_HOME
        // setDisplayShowCustomEnabled(true)  // 使自定义的普通View能在title栏显示，即actionBar.setCustomView能起作用，对应ActionBar.DISPLAY_SHOW_CUSTOM
    }

    public void initSlvMenu() {
        mMenu = new Menu(new ColorDrawable(Color.WHITE), true, 0);//第2个参数表示滑动item是否能滑的过量(true表示过量，就像Gif中显示的那样；false表示不过量，就像QQ中的那样)
        mMenu.addItem(new MenuItem.Builder().setWidth(UDensity.get2Px(this, R.dimen.slv_menu_left_width))
                .setBackground(new ColorDrawable(getResources().getColor(R.color.white)))
                .setIcon(getResources().getDrawable(R.drawable.ic_vector_menu_star,null)) // 插入图片
//                .setTextSize((int) getResources().getDimension(R.dimen.txt_size))
//                .setTextColor(UDensity.getColor(R.color.crimson))
//                .setText("加星")
                .build());
        mMenu.addItem(new MenuItem.Builder().setWidth(UDensity.get2Px(this, R.dimen.slv_menu_right_width))
                .setBackground(new ColorDrawable(getResources().getColor(R.color.white)))
                .setIcon(getResources().getDrawable(R.drawable.ic_vector_menu_adjust,null))
                .setDirection(MenuItem.DIRECTION_RIGHT) // 设置是左或右
//                .setTextColor(R.color.white)
//                .setTextSize(UDensity.getDimen(this, R.dimen.txt_size))
//                .setText("已读")
                .build());
    }
}
