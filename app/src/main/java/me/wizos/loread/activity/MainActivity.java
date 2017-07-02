package me.wizos.loread.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.util.ArrayMap;
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
import java.util.List;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.Article;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.net.API;
import me.wizos.loread.net.Neter;
import me.wizos.loread.presenter.adapter.MainSlvAdapter;
import me.wizos.loread.presenter.adapter.MaterialSimpleListAdapter;
import me.wizos.loread.presenter.adapter.MaterialSimpleListItem;
import me.wizos.loread.utils.UDensity;
import me.wizos.loread.utils.UFile;
import me.wizos.loread.utils.UString;
import me.wizos.loread.utils.UToast;
import me.wizos.loread.utils.colorful.Colorful;
import me.wizos.loread.utils.colorful.setter.ViewGroupSetter;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.SwipeRefresh;
import me.wizos.loread.view.common.color.ColorChooserDialog;

public class MainActivity extends BaseActivity implements SwipeRefresh.OnRefreshListener, ColorChooserDialog.ColorCallback {

    protected static final String TAG = "MainActivity";
    private Context context;
    private IconFontView vReadIcon, vStarIcon ,iconReadability;
    private ImageView vPlaceHolder;
    private TextView vToolbarCount,vToolbarHint;
    private Toolbar toolbar;
    private Menu mMenu;
    private SwipeRefresh mSwipeRefreshLayout;
    private SlideAndDragListView slv;

    public static String sListState;
    public static String sListTag;
    private boolean syncFirstOpen = true;
    //    private boolean hadSyncLogRequest = true;
//    private boolean isOrderTagFeed;
    private MainSlvAdapter mainSlvAdapter;
//    private List<Article> App.articleList;
//    private String sListTagCount = "";

    private Neter mNeter;

    /* 通过Binder，实现Activity与Service通信 */
//    private MainService.ServiceBinder mBinderService;
    // 先创建一个 ServiceConnection 匿名类，重写 onServiceConnected()、onServiceDisconnected()。这两个方法分别会在Activity与Service建立关联和解除关联的时候调用。
    // 在onServiceConnected()方法中，我们又通过向下转型得到了MyBinder的实例，有了这个实例，Activity和Service之间的关系就变得非常紧密了。
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            KLog.d("连接断开");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MainService.ServiceBinder mBinderService = (MainService.ServiceBinder) service;
            MainService mainService = mBinderService.getService();
            mainService.regHandler(mainHandler);//TODO:考虑内存泄露
            mNeter = mainService.getNeter();
//            mainService.move();
            KLog.d("连接开始");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this ;
//        UpdateDB.start(this); // 不会用
        UFile.setContext(this);
        App.addActivity(this);
        initToolbar();
        initSlvListener();
        initSwipe();
        initView();
        initColorful();
        initService();
        initData();
//        update();
//        KLog.i("【一】" + toolbar.getTitle() );
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
        App.mUserID = WithSet.getInstance().getUseId();
        sListState = WithSet.getInstance().getListState();
        sListTag = "user/" + App.mUserID + "/state/com.google/reading-list";
        syncFirstOpen = WithSet.getInstance().isSyncFirstOpen();
//        isOrderTagFeed = WithSet.getInstance().isOrderTagFeed();
        KLog.i("【 readSetting 】ATUH 为" + API.INOREADER_ATUH + syncFirstOpen + "【mUserID为】");
        KLog.i( WithSet.getInstance().getCachePathStarred() + WithSet.getInstance().getUseName() );
    }

    protected void initView(){
        vReadIcon = (IconFontView) findViewById(R.id.main_bottombar_read);
        vStarIcon = (IconFontView)findViewById(R.id.main_bottombar_star);
//        vToolbarCount = (TextView)findViewById(R.id.main_toolbar_count);
        vToolbarHint = (TextView)findViewById(R.id.main_toolbar_hint);
        vPlaceHolder = (ImageView)findViewById(R.id.main_placeholder);
        iconReadability = (IconFontView)findViewById(R.id.main_toolbar_readability);
        iconReadability.setVisibility(View.VISIBLE);
        iconReadability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThemeSelectDialog();
            }
        });
    }
    protected void initSwipe(){
        mSwipeRefreshLayout = (SwipeRefresh) findViewById(R.id.main_swipe);
        if (mSwipeRefreshLayout == null) return;
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setProgressViewOffset(true, 0, 120);//设置样式刷新显示的位置
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
//        Tool.printCallStatck();
        startSyncService();
        KLog.i("【刷新中】");
    }

    // 按下back键时会调用onDestroy()销毁当前的activity，重新启动此activity时会调用onCreate()重建；
    // 而按下home键时会调用onStop()方法，并不销毁activity，重新启动时则是调用onResume()
    @Override
    protected void onDestroy() {
        // 如果参数为null的话，会将所有的Callbacks和Messages全部清除掉。
        // 这样做的好处是在Acticity退出的时候，可以避免内存泄露。因为 handler 内可能引用 Activity ，导致 Activity 退出后，内存泄漏。
//        handler.removeCallbacksAndMessages(null);
        mainHandler.removeCallbacksAndMessages(null);
        unbindService(connection);
        super.onDestroy();
    }

    private void initService() {
        intent = new Intent(this, MainService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    private Intent intent;

    private void startSyncService() {
        intent.setAction("refresh");
        KLog.d("bindService");
        startService(intent);
    }

    @Override
    protected void notifyDataChanged(){
        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setEnabled(true);
        reloadData();
    }

    protected void initData(){
        readSetting();
        initBottombarIcon();
        reloadData();  // 先加载已有数据
        if (syncFirstOpen && App.articleList.size() != 0) {
            mSwipeRefreshLayout.setEnabled(false);
            UToast.showShort("首次开启同步");
//            handler.sendEmptyMessage(API.M_BEGIN_SYNC);
            startSyncService();
        }else {
            List<Article> allArts = WithDB.getInstance().loadArtAll();  //  速度更快，用时更短，这里耗时 43,43
            if (allArts.size() == 0) {
                // 显示一个没有内容正在加载的样子
                UToast.showShort("没有文章，开始同步");
//                handler.sendEmptyMessage(API.M_BEGIN_SYNC);
                startSyncService();
            }
        }
        KLog.i("列表数目：" + App.articleList.size() + "  当前状态：" + sListState);
    }

    /**
     * sListState 包含 3 个状态：All，Unread，Stared
     * sListTag 至少包含 1 个状态： Reading-list
     * */
    protected void reloadData() { // 获取 App.articleList , 并且根据 App.articleList 的到未读数目
        KLog.i("加载数据");
        if(sListTag.contains(API.U_NO_LABEL)){
            App.articleList = getNoLabelList();  // FIXME: 2016/5/7 这里的未分类暂时无法使用，因为在云端订阅源的分类是可能会变的，导致本地缓存的文章分类错误
            KLog.i("【API.U_NO_LABEL】");
        }else {
            if (sListState.equals(API.LIST_STAR)) {
                ;
                App.articleList = WithDB.getInstance().loadTagStar(sListTag);
                KLog.i("【API.LIST_STAR】");
            }else{
                App.articleList = WithDB.getInstance().loadTagRead(sListState, sListTag); // 590-55
//                App.articleList.clear();
//                App.articleList.addAll( WithDB.getInstance().loadTagRead(sListState,sListTag) );
                KLog.i("【API.loadTagRead】" + sListState + sListTag);
            }
        }
        KLog.i("【】" + App.articleList.size() + sListState + "--" + sListTag);

        if (UString.isBlank(App.articleList)) {
            vPlaceHolder.setVisibility(View.VISIBLE);
            slv.setVisibility(View.GONE);
            UToast.showShort("没有文章"); // 弹出一个提示框，询问是否同步
        }else {
            vPlaceHolder.setVisibility(View.GONE);
            slv.setVisibility(View.VISIBLE);
        }
        KLog.i("【notify1】" + sListState + sListTag + toolbar.getTitle() + App.articleList.size());
        mainSlvAdapter = new MainSlvAdapter(this, App.articleList);
        slv.setAdapter(mainSlvAdapter);
        mainSlvAdapter.notifyDataSetChanged();
        KLog.i("【notify2】" + App.articleList.size() + "--" + mainSlvAdapter.getCount());
        changeToolbarTitle();
        tagCount = App.articleList.size();
        setItemNum( tagCount );
    }


    private List<Article> getNoLabelList(){
        List<Article> all,part,exist;
        if( sListState.contains(API.LIST_STAR) ){
            all = WithDB.getInstance().loadStarAll();
            part = WithDB.getInstance().loadStarListHasLabel(App.mUserID);
            exist = WithDB.getInstance().loadStarNoLabel();
        }else {
            all = WithDB.getInstance().loadReadAll( sListState );
            part = WithDB.getInstance().loadReadListHasLabel(sListState, App.mUserID);
            exist = WithDB.getInstance().loadReadNoLabel();
       }

        ArrayList<Article> noLabel = new ArrayList<>( all.size() - part.size() );
        Map<String,Integer> map = new ArrayMap<>( part.size());
        String articleId;
        StringBuffer sb = new StringBuffer(10);

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
                sb = new StringBuffer( article.getCategories() );
//                sb.append(article.getCategories());
                sb.insert(sb.length() - 1, ", \"user/" + App.mUserID + API.U_NO_LABEL + "\"");
                article.setCategories( sb.toString() );
                noLabel.add( article );
            }
        }
        KLog.d( sb.toString() +" - "+  all.size() + " - "+ part.size());
        noLabel.addAll( exist );
        return noLabel;
    }


    // TEST:
    protected Handler mainHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String tips = msg.getData().getString("tips");
            KLog.i("【handler】" + msg.what + "---" + "---");
            switch (msg.what) {
                case API.SUCCESS:
                    //mSwipeRefreshLayout.setRefreshing(false);
                    //mSwipeRefreshLayout.setEnabled(true);
                    vToolbarHint.setText(String.valueOf(App.articleList.size()));
                    notifyDataChanged();
//                    KLog.i("【文章列表获取完成】" );
                    break;
                case API.FAILURE: // 文章获取失败
                    mSwipeRefreshLayout.setRefreshing(false);
                    mSwipeRefreshLayout.setEnabled(true);
                    vToolbarHint.setText(String.valueOf(App.articleList.size()));
                    UToast.showShort("同步失败");
                    break;
                case API.PROCESS:
                    vToolbarHint.setText(tips);
                    break;
                case 1000:
                    vToolbarHint.setText("升级完成");
                    break;
            }
            return false;
        }
    });




    private int tagCount;
    private void changeItemNum(int offset){
        tagCount = tagCount + offset;
        vToolbarHint.setText(String.valueOf( tagCount ));
    }
    private void setItemNum(int offset){
        tagCount = offset;
        vToolbarHint.setText(String.valueOf( tagCount ));
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
            vStarIcon.setText(getString(R.string.font_stared));
            vReadIcon.setText(getString(R.string.font_readed));

        }else if(sListState.equals(API.LIST_UNREAD)){
            vStarIcon.setText(getString(R.string.font_unstar));
            vReadIcon.setText(getString(R.string.font_unread));
        }else {
            vStarIcon.setText(getString(R.string.font_unstar));
            vReadIcon.setText(getString(R.string.font_readed));
        }
    }


    public void initSlvListener() {
        initSlvMenu();
        slv = (SlideAndDragListView)findViewById(R.id.main_slv);
        if(slv==null)return;
        slv.setMenu(mMenu);
        slv.setOnListItemClickListener(new SlideAndDragListView.OnListItemClickListener() {
            @Override
            public void onListItemClick(View v, int position) {
                if(position==-1){return;}
                String articleID = App.articleList.get(position).getId();
                Intent intent = new Intent(MainActivity.this , ArticleActivity.class);
                intent.putExtra("articleID", articleID);
                intent.putExtra("articleNo", position); // 下标从 0 开始
                intent.putExtra("articleCount", App.articleList.size());
                startActivity(intent);
            }
        });
        slv.setOnSlideListener(new SlideAndDragListView.OnSlideListener() {
            @Override
            public int onSlideOpen(View view, View parentView, int position, int direction) {
                if (position == -1) {
                    return Menu.ITEM_NOTHING;
                }
                Article article = App.articleList.get(position);
                switch (direction) {
                    case MenuItem.DIRECTION_LEFT:
                        changeStarState(article);
                        return Menu.ITEM_SCROLL_BACK;
                    case MenuItem.DIRECTION_RIGHT:
                        changeReadState(article);
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
                adapter.add(new MaterialSimpleListItem.Builder(MainActivity.this)
                        .content("标记为未读")
                        .icon(R.drawable.ic_vector_unread)
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
                                        num = App.articleList.size();
                                        artList = new ArrayList<>( num - position - 1 );
                                        break;
                                    case 2:
                                        Article article = App.articleList.get(position);
//                                        startAction("unreadArticle",article.getId());
                                        mNeter.postUnReadArticle( article.getId() );
                                        article.setReadState(API.ART_READING);
                                        WithDB.getInstance().saveArticle(article);
                                        mainSlvAdapter.notifyDataSetChanged();
                                        break;
                                }

                                for(int n = i; n< num; n++){
                                    if (App.articleList.get(n).getReadState().equals(API.ART_UNREAD)) {
                                        App.articleList.get(n).setReadState(API.ART_READ);
                                        artList.add(App.articleList.get(n));
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
        for (Article article : artList) {
            mNeter.postReadArticle(article.getId());
            changeItemNum(-1);
        }
        WithDB.getInstance().saveArticleList(artList);
        mainSlvAdapter.notifyDataSetChanged();
    }
    private void changeReadState(Article article){
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

    protected void changeStarState(Article article){
        if(article.getStarState().equals(API.ART_STAR)){
            article.setStarState(API.ART_UNSTAR);
            mNeter.postUnStarArticle(article.getId());
        }else {
            article.setStarState(API.ART_STAR);
            mNeter.postStarArticle(article.getId());
        }
        WithDB.getInstance().saveArticle(article);
        mainSlvAdapter.notifyDataSetChanged();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_toolbar:
                if (mainHandler.hasMessages(API.MSG_DOUBLE_TAP)) {
                    mainHandler.removeMessages(API.MSG_DOUBLE_TAP);
                    slv.smoothScrollToPosition(0);
                } else {
                    mainHandler.sendEmptyMessageDelayed(API.MSG_DOUBLE_TAP, ViewConfiguration.getDoubleTapTimeout());
                }
                break;
        }
    }



    private String tagName = "";
    @Override
    protected void onActivityResult(int requestCode , int resultCode , Intent intent){
        String tagId = "";
        int tagCount = 0 ;
        int articleNo = 0;
        switch (resultCode){
            case RESULT_OK:
                tagId = intent.getExtras().getString("tagId");
                tagCount = intent.getExtras().getInt("tagCount");
                tagName = intent.getExtras().getString("tagName");
                if (tagId != null && !tagId.equals("")) {
                    sListTag = tagId;
//                    sListTagCount = tagCount;
                    KLog.i("【onActivityResult】" + sListTag + sListState);
                    reloadData();
                }
                break;
            case 2:
                mNeter.getWithAuth(API.HOST + API.U_SUSCRIPTION_LIST);
                break;
            case 3:
                articleNo = intent.getExtras().getInt("articleNo");
                slv.smoothScrollToPosition(articleNo);
                break;
        }
//        KLog.i("【== onActivityResult 】" + tagId + "----" + sListTag);
    }

    /**
     * 设置各个视图与颜色属性的关联
     */
    protected void initColorful() {
        ViewGroupSetter listViewSetter = new ViewGroupSetter(slv);
        // 绑定ListView的Item View中的news_title视图，在换肤时修改它的text_color属性
        listViewSetter.childViewTextColor(R.id.main_slv_item_title, R.attr.lv_item_title_color);
        listViewSetter.childViewTextColor(R.id.main_slv_item_summary, R.attr.lv_item_desc_color);
        listViewSetter.childViewTextColor(R.id.main_slv_item_author, R.attr.lv_item_info_color);
        listViewSetter.childViewTextColor(R.id.main_slv_item_time, R.attr.lv_item_info_color);
        listViewSetter.childViewBgColor(R.id.main_slv_item, R.attr.root_view_bg);
        listViewSetter.childViewBgColor(R.id.main_slv_item_divider, R.attr.lv_item_divider);

        // 构建Colorful对象来绑定View与属性的对象关系
        mColorful = new Colorful.Builder(this)
                // 设置view的背景图片
                .backgroundColor(R.id.main_swipe, R.attr.root_view_bg)
                // 设置 toolbar
                .backgroundColor(R.id.main_toolbar, R.attr.topbar_bg)
                .textColor(R.id.main_toolbar_hint, R.attr.topbar_fg)
                .textColor(R.id.main_toolbar_readability, R.attr.topbar_fg)

                // 设置 bottombar
                .backgroundColor(R.id.main_bottombar, R.attr.bottombar_bg)
                .textColor(R.id.main_bottombar_read, R.attr.bottombar_fg)
                .textColor(R.id.main_bottombar_star, R.attr.bottombar_fg)
                .textColor(R.id.main_bottombar_setting, R.attr.bottombar_fg)
                .textColor(R.id.main_bottombar_tag, R.attr.bottombar_fg)

                // 设置 listview 背景色
                .setter(listViewSetter) // 手动设置setter
                .create(); // 创建Colorful对象
        autoToggleThemeSetting();
    }
    private int selectTheme;
//    @OnClick(R.id.main_icon_readability)
    public void showThemeSelectDialog() {
        selectTheme = UDensity.resolveColor( this,R.attr.colorPrimary);
        new ColorChooserDialog.Builder(this, R.string.readability_dialog_title)
                .titleSub(R.string.md_error_label)
                .preselect(selectTheme)
                .customColors(R.array.custom_colors, null)
                .show();
        KLog.d("主题选择对话框");
    }
    // Receives callback from color chooser dialog
    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int color) {
        selectTheme = color;
        toggleThemeSetting();
    }
    public void onSettingIconClicked(View view){
        Intent intent = new Intent(getActivity(),SettingActivity.class);
        startActivityForResult(intent, 0);
    }
    //定义一个startActivityForResult（）方法用到的整型值
    public void onTagIconClicked(View view){
        Intent intent = new Intent(MainActivity.this,TagActivity.class);
        intent.putExtra("ListState",sListState);
        intent.putExtra("ListTag",sListTag);
        intent.putExtra("ListCount", App.articleList.size());
//        intent.putExtra("NoLabelCount",getNoLabelList().size());
        startActivityForResult(intent, 0);
    }

    public void onStarIconClicked(View view){
        KLog.d( sListTag + sListState + tagName );
        KLog.d("收藏列表" + sListTag + sListState + tagName);
        if(sListState.equals(API.LIST_STAR)){
            UToast.showShort("已经在收藏列表了");
        }else {
            vStarIcon.setText(getString(R.string.font_stared));
            vReadIcon.setText(getString(R.string.font_readed));

            sListState = API.LIST_STAR;
            WithSet.getInstance().setListState(sListState);
            reloadData();
        }
    }
    public void onReadIconClicked(View view){
        KLog.d( sListTag + sListState + tagName );
        vStarIcon.setText(getString(R.string.font_unstar));
        if(sListState.equals(API.LIST_UNREAD)){
            vReadIcon.setText(getString(R.string.font_readed));
            sListState = API.LIST_ALL;
        }else {
            vReadIcon.setText(getString(R.string.font_unread));
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
            quitDialog();// 创建弹出的Dialog
            return true;//返回真表示返回键被屏蔽掉
        }
        return super.onKeyDown(keyCode, event);
    }

    private void quitDialog() {
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
        mMenu.getItemBackGroundDrawable();
    }

//
//    public boolean update() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                List<Article> arts = WithDB.getInstance().loadArtWhere();
//                KLog.d("升级文章数量：" + arts.size());
//                if (arts.size() == 0) {
//                    return;
//                }
//                String imgState, folder;
//                Pattern r;
//                Matcher m;
//                for (Article article : arts) {
//                    imgState = article.getImgState();
//                    if (imgState.equals("") || imgState.equals("OK") || imgState.equals("ok")) {
//                        continue;
//                    } else {
//                        // 验证规则
////                        String filter = ",\"saveSrc\":.*?loread\"";
////                        // 编译正则表达式
////                        pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE); // 忽略大小写的写法
////                        matcher = pattern.matcher( imgState );
////                        matcher.replaceAll(""); // 字符串是否与正则表达式相匹配
////
////                        pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE); // 忽略大小写的写法
////                        matcher = pattern.matcher( imgState );
////                        matcher.replaceAll(""); // 字符串是否与正则表达式相匹配
//                        if (imgState.contains("\"folder\":\"")) {
//                            continue;
//                        }
//                        if (article.getSaveDir() == null) {
//                            article.setSaveDir(API.SAVE_DIR_CACHE);
//                        }
//                        folder = UFile.getFolder(article.getSaveDir(), UString.stringToMD5(article.getId()), article.getTitle());
//
//                        r = Pattern.compile(",\"saveSrc\":.*?loread\"");// 创建 Pattern 对象
//                        m = r.matcher(imgState);// 现在创建 matcher 对象
//                        imgState = m.replaceAll("");
//
//                        r = Pattern.compile("\"localSrc\":.*?_files\\/(.*?)loread\"");// 创建 Pattern 对象
//                        m = r.matcher(imgState);// 现在创建 matcher 对象
//                        imgState = m.replaceAll("\"imgName\":\"$1loread\"");
//
////                        imgState.replaceAll(",\"saveSrc\":.*?loread\"", "" );
////                        imgState.replaceAll("\"localSrc\":.*?_files\\/(.*?)loread\"", "\"imgName\":\"$1loread\"" );
//                        KLog.d("升级文章含有：" + imgState.contains("\"imgStatus\":1,"));
//                        if (imgState.contains("\"imgStatus\":1,")) {
////                            int p = imgState.indexOf("\"imgStatus\":1,");
//                            imgState = imgState.replace("\"imgStatus\":1,", "\"imgStatus\":1,\"folder\":\"" + folder + "_files\",");
//                        }
//                        if (imgState.contains("\"imgStatus\":0,")) {
//                            imgState = imgState.replace("\"imgStatus\":0,", "\"imgStatus\":0,\"folder\":\"" + folder + "_files\",");
//                        }
//
//                        KLog.d("升级文章内容：" + imgState);
////                        try {
////                            Thread.sleep(21000);
////                        }catch (Exception e){
////
////                        }
////                        Gson gson = new Gson();
////                        Type type = new TypeToken<ExtraImg>() {}.getType();
////                        try {
////                            extraImg = gson.fromJson(imgState, type);
////                            if ( extraImg.getFolder()==null ){
////                                extraImg.setFolder( UFile.getFolder( article.getSaveDir(), UString.stringToMD5(article.getId()), article.getTitle() ) );
////                            }
////                            folder = extraImg.getFolder();
////                            lossSrcList = extraImg.getLossImgs();
////                            obtainSrcList = extraImg.getObtainImgs();
//////                    KLog.e("重新进入获取到的imgState记录" + imgState + extraImg +  lossSrcList + obtainSrcList);
////                        } catch (RuntimeException e) {
////                            continue;
////                        }
//                    }
//                    article.setImgState(imgState);
//                    WithDB.getInstance().saveArticle(article);
//                }
//                mainHandler.sendEmptyMessage(1000);
//            }
//        }).start();
////        KLog.d( "升级完成" );
//        return true;
//    }






}
