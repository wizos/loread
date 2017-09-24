package me.wizos.loread.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
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
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.socks.library.KLog;
import com.yinglan.scrolllayout.ScrollLayout;
import com.yydcdut.sdlv.Menu;
import com.yydcdut.sdlv.MenuItem;
import com.yydcdut.sdlv.SlideAndDragListView;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.adapter.ExpandableListAdapterS;
import me.wizos.loread.adapter.MainSlvAdapter;
import me.wizos.loread.adapter.MaterialSimpleListAdapter;
import me.wizos.loread.adapter.MaterialSimpleListItem;
import me.wizos.loread.bean.Article;
import me.wizos.loread.bean.Feed;
import me.wizos.loread.bean.Tag;
import me.wizos.loread.data.DBHelper;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.data.dao.DaoMaster;
import me.wizos.loread.net.API;
import me.wizos.loread.net.Neter;
import me.wizos.loread.utils.ScreenUtil;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.utils.ToastUtil;
import me.wizos.loread.view.ExpandableListViewS;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.SwipeRefreshLayoutS;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.colorful.setter.ViewGroupSetter;
import me.wizos.loread.view.common.color.ColorChooserDialog;

public class MainActivity extends BaseActivity implements SwipeRefreshLayoutS.OnRefreshListener, ColorChooserDialog.ColorCallback, View.OnClickListener {
    protected static final String TAG = "MainActivity";
    private IconFontView vReadIcon, vStarIcon;
    private ImageView vPlaceHolder;
    private TextView vToolbarHint;
    private Toolbar toolbar;
    private Menu mMenu;
    private SwipeRefreshLayoutS mSwipeRefreshLayoutSLayout;
    private SlideAndDragListView slv;

    private static String listTagId;
    private static String listTitle;
    public static String listTabState;
    private static int tagCount;


    /*
    Streams 可以是 feeds, tags (folders) 或者是 system types.
    feed/http://feeds.arstechnica.com/arstechnica/science - Feed.
    user/-/label/Tech - Tag (or folder).
    user/-/state/com.google/read - Read articles.已阅读文章
    user/-/state/com.google/starred - Starred articles.
    user/-/state/com.google/broadcast - Broadcasted articles.
    user/-/state/com.google/like - Likes articles.
    user/-/state/com.google/saved-web-pages - Saved web pages.
    user/-/state/com.google/reading-list.阅读列表(包括已读和未读)
     */
    /**
     * 从上面的API也可以知道，这些分类是很混乱的。
     * 本质上来说，Tag 或者 Feed 都是一组 Articles (最小单位) 的集合（Stream）。（Tag 是 Feed 形而上的抽离/集合）
     * 而我们用户对其中某些 Article 的 Read, Star, Save, Comment, Broadcast 等操作，无意中又生成了一组集合（Stream）
     * 所以为了以后的方便，我最好是抽离/包装出一套标准的 API。
     */
    private String StreamId;
    private String StreamState; // 这个只是从 Read 属性的4个类型(Readed, UnRead, UnReading, All), Star 属性的3个类型(Stared, UnStar, All)中，生硬的抽出 UnRead(含UnReading), Stared, All 3个快捷状态，供用户在主页面切换时使用

    private String StreamTitle;
    private int StreamCount;

    // 由于根据 StreamId 来获取文章，可从2个属性( Categories[针对Tag], OriginStreamId[针对Feed] )上，共4个变化上（All, Tag, NoTag, Feed）来获取文章。
    // 根据 StreamState 也是从2个属性(ReadState, StarState)的3个快捷状态 ( UnRead[含UnReading], Stared, All ) 来获取文章。
    // 所以文章列表页会有6种组合：某个 Categories 内的 UnRead[含UnReading], Stared, All。某个 OriginStreamId 内的 UnRead[含UnReading], Stared, All。
    // 所有定下来去获取文章的函数也有6个：getUnreadArtsInTag(), getStaredArtsInTag(), getAllArtsInTag(),getUnreadArtsInFeed(), getStaredArtsInFeed(), getAllArtsInFeed()


    private boolean syncFirstOpen;
    private MainSlvAdapter mainSlvAdapter;

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
            KLog.d("连接开始");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        initToolbar();
        initSlvListener();
        initSwipe();
        initView();
        initService();
        DaoMaster daoMaster = DBHelper.startUpgrade(this);
        if (daoMaster.getSchemaVersion() == 3) {
            KLog.e("数据库升级，修改数据");
            List<Article> arts = WithDB.i().getStaredArt();
            for (Article art : arts) {
                art.setStarred(art.getPublished());
            }
            WithDB.i().saveArticleList(arts);
        }


        initData();
        initScrollTag();
        if (savedInstanceState != null) {
            final int position = savedInstanceState.getInt("listItemFirstVisiblePosition");
            slvSetSelection(position);
//            mainHandler.post(new Runnable() {
//                @Override
//                public void run() {
//                    // setSelection 没有滚动效果，直接跳到指定位置。smoothScrollToPosition 有滚动效果的
//                    slv.setSelection(position);
//                }
//            });
//            slv.setSelection( savedInstanceState.getInt("listItemFirstVisiblePosition") ) ; // setSelection 没有滚动效果，直接跳到指定位置。smoothScrollToPosition 有滚动效果的
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("listItemFirstVisiblePosition", slv.getFirstVisiblePosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (mainSlvAdapter != null) {
            mainSlvAdapter.notifyDataSetChanged();
        }
        KLog.i("【onResume】" + listTabState + "---" + toolbar.getTitle() + listTagId);
    }


    protected void readSetting(){
        API.INOREADER_ATUH = WithSet.i().getAuth();
        App.mUserID = WithSet.i().getUseId();
        listTabState = WithSet.i().getListTabState();
        listTagId = WithSet.i().getListTagId();
        if (listTagId == null || listTagId.equals("")) {
            listTagId = "user/" + App.mUserID + "/state/com.google/reading-list";
        }
        syncFirstOpen = WithSet.i().isSyncFirstOpen();
//        isOrderTagFeed = WithSet.i().isOrderTagFeed();
        KLog.i("【 readSetting 】ATUH 为" + API.INOREADER_ATUH + syncFirstOpen + "【mUserID为】");
        KLog.i(WithSet.i().getUseName() + WithSet.i().getUseId());
    }

    protected void initView(){
        vReadIcon = (IconFontView) findViewById(R.id.main_bottombar_read);
        vStarIcon = (IconFontView)findViewById(R.id.main_bottombar_star);
//        vToolbarCount = (TextView)findViewById(R.id.main_toolbar_count);
        vToolbarHint = (TextView)findViewById(R.id.main_toolbar_hint);
        vPlaceHolder = (ImageView)findViewById(R.id.main_placeholder);
        IconFontView iconReadability = (IconFontView) findViewById(R.id.main_toolbar_readability);
        iconReadability.setVisibility(View.VISIBLE);
        iconReadability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualToggleTheme();
//                showSelectThemeDialog();
            }
        });
    }
    protected void initSwipe(){
        mSwipeRefreshLayoutSLayout = (SwipeRefreshLayoutS) findViewById(R.id.main_swipe);
        if (mSwipeRefreshLayoutSLayout == null) return;
        mSwipeRefreshLayoutSLayout.setOnRefreshListener(this);
        mSwipeRefreshLayoutSLayout.setProgressViewOffset(true, 0, 120);//设置样式刷新显示的位置
        mSwipeRefreshLayoutSLayout.setViewGroup(slv);
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
        if (!mSwipeRefreshLayoutSLayout.isEnabled()) {
            return;
        }
        mSwipeRefreshLayoutSLayout.setEnabled(false);
        mSwipeRefreshLayoutSLayout.setRefreshing(false);  // 调用 setRefreshing(false) 去取消任何刷新的视觉迹象。如果活动只是希望展示一个进度条的动画，他应该条用 setRefreshing(true) 。 关闭手势和进度条动画，调用该 View 的 setEnable(false)
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

    private Intent intent;
    private void initService() {
        intent = new Intent(this, MainService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }
    private void startSyncService() {
        intent.setAction("refresh");
        KLog.d("bindService");
        startService(intent);
    }

    @Override
    protected void notifyDataChanged(){
        mSwipeRefreshLayoutSLayout.setRefreshing(false);
        mSwipeRefreshLayoutSLayout.setEnabled(true);
        reloadData();
    }

    protected void initData(){
        readSetting();
        initBottombarIcon();
        reloadData();  // 先加载已有数据
        if (syncFirstOpen && !mSwipeRefreshLayoutSLayout.isRefreshing()) {
            ToastUtil.showShort("开始同步");
            mSwipeRefreshLayoutSLayout.setEnabled(false);
            startSyncService();
        }

        KLog.i("列表数目：" + App.articleList.size() + "  当前状态：" + listTabState);
    }

    /**
     * listTabState 包含 3 个状态：All，Unread，Stared
     * listTagId 至少包含 1 个状态： Reading-list
     * */
    protected void reloadData() { // 获取 App.articleList , 并且根据 App.articleList 的到未读数目
        KLog.i("reloadData", "加载数据");

//        if (listTagId.contains(API.U_NO_LABEL)) {
//            App.articleList = getNoTagList();  // FIXME: 2016/5/7 这里的未分类暂时无法使用，因为在云端订阅源的分类是可能会变的，导致本地缓存的文章分类错误
//            KLog.i("【API.U_NO_LABEL】");
//        } else if (listTabState.equals(API.LIST_STARED)) {
//            App.articleList = WithDB.i().getStaredArtInTag(listTagId);
//            KLog.i("【API.LIST_STARED】");
//        } else {
//            App.articleList = WithDB.i().getArt(listTabState, listTagId); // 590-55
//            KLog.i("【API.getArt】" + listTabState + listTagId);
//        }

        if (listTagId.contains("user/" + App.mUserID + "/")) {
            if (listTagId.contains(API.U_NO_LABEL)) {
                App.articleList = getNoTagList2();  // FIXME: 2016/5/7 这里的未分类暂时无法使用，因为在云端订阅源的分类是可能会变的，导致本地缓存的文章分类错误
                KLog.i("【API.U_NO_LABEL】");
            } else if (listTabState.equals(API.LIST_STARED)) {
//                App.articleList = WithDB.i().getStaredArtsInTag(listTagId); // Test 测试修改数据库的
                App.articleList = WithDB.i().getArtsByCategoriesOrderCrawlMsec("/state/com.google/starred", listTagId);
                KLog.i("【API.LIST_STARED】");
            } else {
                App.articleList = WithDB.i().getArt(listTabState, listTagId); // 590-55
                KLog.i("【API.getArt】" + listTabState + listTagId);
            }
        } else if (listTagId.indexOf("feed/") == 0) {
            if (listTabState.equals(API.LIST_STARED)) {
                App.articleList = WithDB.i().getArtsStar(listTagId);
                KLog.i("【API.LIST_STARED】");
            } else {
                App.articleList = WithDB.i().getArtsRead(listTabState, listTagId); // 590-55
                KLog.i("【API.getArt】" + listTabState + listTagId);
            }
        }

//        if(listTagId.contains("user/" + App.mUserID + "/") ){
//            if (listTagId.contains(API.U_READING_LIST)) {
//                if (listTabState.equals(API.LIST_STARED)) {
//                    listTitle = "所有加星";
//                    App.articleList = WithDB.i().getStaredArtsInTag(listTagId);
//                } else if (listTabState.equals(API.LIST_UNREAD)) {
//                    listTitle = "所有未读";
////                    App.articleList = WithDB.i().getArt(listTabState, listTagId);
//                    App.articleList = WithDB.i().getUnreadArtsInTag(listTagId); // TEST:  测试DB函数
//                }else {
//                    listTitle = "所有文章";
//                    App.articleList = WithDB.i().getArt(listTabState, listTagId);
//                }
//            } else if (listTagId.contains(API.U_NO_LABEL)) {
//                if (listTabState.equals(API.LIST_STARED)) {
//                    listTitle = "加星未分类";
//                    App.articleList = getNoTagList();
//                } else if (listTabState.equals(API.LIST_UNREAD)) {
//                    listTitle = "未读未分类";
//                    App.articleList = getNoTagList();
//                }else {
//                    listTitle = "所有未分类";
//                    App.articleList = getNoTagList();
//                }
//            } else {
//                App.articleList = WithDB.i().getArt(listTabState, listTagId);
//                listTitle = WithDB.i().getTag(listTagId).getTitle();
//            }
//        }else if( listTagId.indexOf("feed/") == 0 ){
//            if (listTabState.equals(API.LIST_STARED)) {
//                App.articleList = WithDB.i().getArtsStar(listTagId);
//                KLog.i("【API.LIST_STARED】");
//            } else {
//                App.articleList = WithDB.i().getArt(listTabState, listTagId); // 590-55
//                KLog.i("【API.getArt】" + listTabState + listTagId);
//            }
//            listTitle = WithDB.i().getFeed(listTagId).getTitle();
//        }


        KLog.i("【】" + listTabState + "--" + listTagId);

        if (StringUtil.isBlank(App.articleList)) {
            vPlaceHolder.setVisibility(View.VISIBLE);
            slv.setVisibility(View.GONE);
//            ToastUtil.showShort("没有文章"); // 弹出一个提示框，询问是否同步
        }else {
            vPlaceHolder.setVisibility(View.GONE);
            slv.setVisibility(View.VISIBLE);
        }
        KLog.i("【notify1】" + listTabState + listTagId + toolbar.getTitle() + App.articleList.size());
        mainSlvAdapter = new MainSlvAdapter(this, App.articleList);
        slv.setAdapter(mainSlvAdapter);
        mainSlvAdapter.notifyDataSetChanged();
        changeToolbarTitle();
//        getSupportActionBar().setTitle(listTitle);
        // 在setSupportActionBar(toolbar)之后调用toolbar.setTitle()的话。 在onCreate()中调用无效。在onStart()中调用无效。 在onResume()中调用有效。
        tagCount = App.articleList.size();
        setToolbarHint(tagCount);
    }
    private void changeToolbarTitle() {
        if (listTagId.contains(API.U_READING_LIST)) {
            if (listTabState.equals(API.LIST_STARED)) {
                listTitle = "所有加星";
            } else if (listTabState.equals(API.LIST_UNREAD)) {
                listTitle = "所有未读";
            } else {
                listTitle = "所有文章";
            }
        } else if (listTagId.contains(API.U_NO_LABEL)) {
            if (listTabState.equals(API.LIST_STARED)) {
                listTitle = "加星未分类";
            } else if (listTabState.equals(API.LIST_UNREAD)) {
                listTitle = "未读未分类";
            } else {
                listTitle = "所有未分类";
            }
        } else {
            listTitle = WithDB.i().getTag(listTagId).getTitle();
        }
        KLog.e("页面title是：" + listTitle);
        getSupportActionBar().setTitle(listTitle);
        // 在setSupportActionBar(toolbar)之后调用toolbar.setTitle()的话。 在onCreate()中调用无效。在onStart()中调用无效。 在onResume()中调用有效。
        KLog.d(listTagId + listTabState + listTitle);
    }

    public List<Article> getNoTagList2() {
        List<Article> noTags;
        if (listTabState.contains(API.LIST_STARED)) {
            noTags = WithDB.i().loadStarAndNoTag();
        } else if (listTabState.contains(API.LIST_UNREAD)) {
            noTags = WithDB.i().loadUnreadAndNoTag();
        }else {
            noTags = WithDB.i().loadAllNoTag();
        }
        return noTags;
    }


//    private List<Article> getNoTagList() {
//        List<Article> all,part,exist;
//        if (listTabState.contains(API.LIST_STARED)) {
//            all = WithDB.i().getStaredArt();
//            part = WithDB.i().loadStaredHasTag(App.mUserID);
//            exist = WithDB.i().getStarNoTag();
//        }else {
//            all = WithDB.i().getArt(listTabState);
//            part = WithDB.i().loadReadListHasTag(listTabState, App.mUserID);
//            exist = WithDB.i().getReadNoTag();
//       }
//
//        ArrayList<Article> noTag = new ArrayList<>(all.size() - part.size());
//        Map<String,Integer> map = new ArrayMap<>( part.size());
//        String articleId;
//        StringBuffer sb = new StringBuffer(10);
//
//        for( Article article: part ){
//            articleId = article.getId();
//            map.put(articleId,1);
//        }
//        for( Article article: all ){
//            articleId = article.getId();
//            Integer cc = map.get( articleId );
//            if(cc!=null) {
//                map.put( articleId , ++cc);
//            }else {
//                sb = new StringBuffer( article.getCategories() );
////                sb.append(article.getCategories());
//                sb.insert(sb.length() - 1, ", \"user/" + App.mUserID + API.U_NO_LABEL + "\"");
//                article.setCategories( sb.toString() );
//                noTag.add(article);
//            }
//        }
//        KLog.e( sb.toString() +" - "+  all.size() + " - "+ part.size());
//        noTag.addAll(exist);
//        return noTag;
//    }


    // TEST:
    protected Handler mainHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String tips = msg.getData().getString("tips");
            KLog.i("【handler】" + msg.what + "---" + "---");
            switch (msg.what) {
                case API.SUCCESS:
                    vToolbarHint.setText(String.valueOf(App.articleList.size()));
                    notifyDataChanged();
//                    KLog.i("【文章列表获取完成】" );
                    break;
                case API.FAILURE: // 文章获取失败
                    mSwipeRefreshLayoutSLayout.setRefreshing(false);
                    mSwipeRefreshLayoutSLayout.setEnabled(true);
                    vToolbarHint.setText(String.valueOf(App.articleList.size() + "\n同步失败"));
                    ToastUtil.showShort("同步失败");
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




    private void changeItemNum(int offset){
        tagCount = tagCount + offset;
        vToolbarHint.setText(String.valueOf( tagCount ));
    }

    private void setToolbarHint(int tagCount) {
        vToolbarHint.setText(String.valueOf( tagCount ));
    }



    private void initBottombarIcon(){
        if (listTabState.equals(API.LIST_STARED)) {
            vStarIcon.setText(getString(R.string.font_stared));
            vReadIcon.setText(getString(R.string.font_readed));

        } else if (listTabState.equals(API.LIST_UNREAD)) {
            vStarIcon.setText(getString(R.string.font_unstar));
            vReadIcon.setText(getString(R.string.font_unread));
        }else {
            vStarIcon.setText(getString(R.string.font_unstar));
            vReadIcon.setText(getString(R.string.font_readed));
        }
    }


    private ScrollLayout mScrollLayout;
    private ExpandableListViewS tagListView;

    public void initScrollTag() {
        /**设置 setting*/
        mScrollLayout = (ScrollLayout) findViewById(R.id.scroll_down_layout);
        mScrollLayout.setMinOffset(0); // minOffset 关闭状态时最上方预留高度
        mScrollLayout.setMaxOffset((int) (ScreenUtil.getScreenHeight(this) * 0.6)); //打开状态时内容显示区域的高度
        mScrollLayout.setExitOffset(ScreenUtil.dip2px(this, 0)); //最低部退出状态时可看到的高度，0为不可见
        mScrollLayout.setIsSupportExit(true);
        mScrollLayout.setAllowHorizontalScroll(false);
        mScrollLayout.setToExit();

        IconFontView tagIcon = (IconFontView) findViewById(R.id.main_bottombar_tag);
        tagIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getHandler().hasMessages(3608)) {
                    v.getHandler().removeMessages(3608);
                    Intent intent = new Intent(MainActivity.this, TagActivity.class);
                    intent.putExtra("ListState", listTabState);
                    intent.putExtra("ListTag", listTagId);
                    intent.putExtra("ListCount", App.articleList.size());
                    intent.putExtra("NoTagCount", getNoTagList2().size());
                    startActivityForResult(intent, 0);
                } else {
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            mScrollLayout.setToOpen();
                        }
                    };
                    Message m = Message.obtain(v.getHandler(), r); // obtain() 从全局池中返回一个新的Message实例。在大多数情况下这样可以避免分配新的对象。
                    m.what = 3608;
                    v.getHandler().sendMessageDelayed(m, 300);// ViewConfiguration.getDoubleTapTimeout()
                }
            }
        });

//        tagIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mScrollLayout.setToOpen();
////                slv.setCanTounch(false);
//            }
//        });

//        tagIcon.setOnTapListener(new IconFontView.OnTapListener() {
//            @Override
//            public void onSingleClick(View v) {
//                mScrollLayout.setToOpen();
////                slv.setCanTounch(false);
//            }
//
//            @Override
//            public void onDoubleClick(View v) {
//                Intent intent = new Intent(MainActivity.this,TagActivity.class);
//                intent.putExtra("ListState", listTabState);
//                intent.putExtra("ListTag", listTagId);
//                intent.putExtra("ListCount", App.articleList.size());
//                intent.putExtra("NoTagCount",getNoTagList().size());
//                startActivityForResult(intent, 0);
//            }
//        });

        getTagData();


        ExpandableListAdapterS expandableListAdapter = new ExpandableListAdapterS(this, tagList);
        tagListView = (ExpandableListViewS) findViewById(R.id.list_view);
        tagListView.setAdapter(expandableListAdapter);
        KLog.e("未触碰Group" + tagListView.isActivated() + "=" + tagListView.isClickable() + "=" + tagListView.isLongClickable() + "=" + "=" + tagListView.isEnabled() + "=" + tagListView.isInTouchMode() + "=" + tagListView.isFocused() + "=" + "=" + tagListView.isPressed() + "=" + tagListView.isSelected() + "=");
        // 设置悬浮头部VIEW
//        tagListView.setHeaderView(getLayoutInflater().inflate(R.layout.main_expandable_item_group_header, tagListView, false));


        tagListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                mScrollLayout.setToExit();
                listTagId = tagList.get(groupPosition).getId().replace("\"", "");
                listTitle = tagList.get(groupPosition).getTitle();
                if (listTagId == null || listTagId.equals("")) {
                    listTagId = "user/" + App.mUserID + "/state/com.google/reading-list";
                }
                WithSet.i().setListTagId(listTagId);
                reloadData();
                KLog.d("【 TagList 被点击】" + listTagId + listTabState);
                return true;
            }
        });

        tagListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                KLog.e("子项被点击1：" + v + " - " + v.getTag() + "-" + groupPosition + "==" + childPosition + "=" + id);
                mScrollLayout.setToExit();
                Feed theFeed = tagList.get(groupPosition).getFeeds().get(childPosition);

                listTagId = theFeed.getId();
                listTitle = theFeed.getTitle();
                WithSet.i().setListTagId(listTagId);
                reloadData();

                KLog.e("【子项被点击2】" + listTagId + listTabState);
                return true;
            }
        });

    }


    private ArrayList<Tag> tagList;

    private void getTagData() {
        Tag rootTag = new Tag();
        Tag noLabelTag = new Tag();
        long userID = WithSet.i().getUseId();
        rootTag.setTitle("所有文章");
        noLabelTag.setTitle("未分类");

        rootTag.setId("\"user/" + userID + API.U_READING_LIST + "\"");
        rootTag.setSortid("00000000");
        rootTag.setUnreadcount(App.articleList.size()); // test: 这句有问题
        rootTag.__setDaoSession(App.getDaoSession());

        noLabelTag.setId("\"user/" + userID + API.U_NO_LABEL + "\"");
        noLabelTag.setSortid("00000001");
        noLabelTag.setUnreadcount(0);// test: 这句有问题
        noLabelTag.__setDaoSession(App.getDaoSession());

        List<Tag> tagListTemp = WithDB.i().getTags();
        tagList = new ArrayList<>(tagListTemp.size());
        tagList.add(rootTag);
        tagList.add(noLabelTag);
        tagList.addAll(tagListTemp);

        KLog.d("【listTag】 " + rootTag.toString());
    }


    public void initSlvListener() {
        initSlvMenu();
        slv = (SlideAndDragListView)findViewById(R.id.main_slv);
        if(slv==null)return;
        slv.setMenu(mMenu);
        slv.setOnListItemClickListener(new SlideAndDragListView.OnListItemClickListener() {
            @Override
            public void onListItemClick(View v, int position) {
//                KLog.e("【 TagList 被点击】");
                if(position==-1){return;}
                String articleID = App.articleList.get(position).getId();
                Intent intent = new Intent(MainActivity.this , ArticleActivity.class);
                intent.putExtra("articleID", articleID);
                intent.putExtra("articleNo", position); // 下标从 0 开始
                intent.putExtra("articleCount", App.articleList.size());
                startActivityForResult(intent, 0);
//                KLog.i("点击了" + articleID + position + "-" + App.articleList.size());
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
                                        mNeter.markArticleUnread(article.getId());
                                        article.setReadState(API.ART_UNREADING);
                                        WithDB.i().saveArticle(article);
                                        mainSlvAdapter.notifyDataSetChanged();
                                        break;
                                }

                                for(int n = i; n< num; n++){
                                    if (App.articleList.get(n).getReadState().equals(API.ART_UNREAD)) {
                                        App.articleList.get(n).setReadState(API.ART_READED);
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
            mNeter.markArticleReaded(article.getId());
            changeItemNum(-1);
        }
        WithDB.i().saveArticleList(artList);
        mainSlvAdapter.notifyDataSetChanged();
    }
    private void changeReadState(Article article){
        if (article.getReadState().equals(API.ART_READED)) {
            article.setReadState(API.ART_UNREADING);
            mNeter.markArticleUnread(article.getId());
            changeItemNum( + 1 );
//            ToastUtil.showShort("标为未读");
        }else {
            article.setReadState(API.ART_READED);
            mNeter.markArticleReaded(article.getId());
            changeItemNum( - 1 );
//            ToastUtil.showShort("标为已读");
        }
        WithDB.i().saveArticle(article);
        mainSlvAdapter.notifyDataSetChanged();
    }

    protected void changeStarState(Article article){
        if (article.getStarState().equals(API.ART_STARED)) {
            article.setStarState(API.ART_UNSTAR);
            mNeter.markArticleUnstar(article.getId());
        }else {
            article.setStarState(API.ART_STARED);
            mNeter.markArticleStared(article.getId());
        }
        WithDB.i().saveArticle(article);
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



    @Override
    protected void onActivityResult(int requestCode , int resultCode , Intent intent){
//        KLog.e("------------------------------------------" + resultCode + requestCode);
        switch (resultCode){
            // 这一段应该用不到了
            case API.ActivityResult_TagToMain:
//                tagCount = intent.getExtras().getInt("tagCount");
                listTagId = intent.getExtras().getString("tagId");
                listTitle = intent.getExtras().getString("tagTitle");
                if (listTagId == null || listTagId.equals("")) {
                    listTagId = "user/" + App.mUserID + "/state/com.google/reading-list";
                }
                WithSet.i().setListTagId(listTagId);
                reloadData();
                break;
            case API.ActivityResult_ArtToMain:
                final int articleNo = intent.getExtras().getInt("articleNo");
                slvSetSelection(articleNo);
                break;
        }
//                KLog.e("【onActivityResult】" + listTagId + listTabState);
    }

    // 滚动到指定位置
    private void slvSetSelection(final int position) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                slv.setSelection(position);
            }
        });
        KLog.e("【setSelection】" + position);
    }

    /**
     * 设置各个视图与颜色属性的关联
     */
    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        ViewGroupSetter artListViewSetter = new ViewGroupSetter(slv);
        // 绑定ListView的Item View中的news_title视图，在换肤时修改它的text_color属性
        artListViewSetter.childViewTextColor(R.id.main_slv_item_title, R.attr.lv_item_title_color);
        artListViewSetter.childViewTextColor(R.id.main_slv_item_summary, R.attr.lv_item_desc_color);
        artListViewSetter.childViewTextColor(R.id.main_slv_item_author, R.attr.lv_item_info_color);
        artListViewSetter.childViewTextColor(R.id.main_slv_item_time, R.attr.lv_item_info_color);
        artListViewSetter.childViewBgColor(R.id.main_slv_item_divider, R.attr.lv_item_divider);
        artListViewSetter.childViewBgColor(R.id.main_slv_item, R.attr.root_view_bg);

        ViewGroupSetter tagListViewSetter = new ViewGroupSetter(tagListView);
        // 绑定ListView的Item View中的news_title视图，在换肤时修改它的text_color属性
//        tagListViewSetter.childViewTextColor(R.id.tag_slv_item_icon, R.attr.lv_item_title_color);
//        tagListViewSetter.childViewTextColor(R.id.tag_slv_item_title, R.attr.lv_item_title_color);
//        tagListViewSetter.childViewTextColor(R.id.tag_slv_item_count, R.attr.lv_item_desc_color);
//        tagListViewSetter.childViewBgColor(R.id.tag_slv_item,R.attr.bottombar_bg); // 这个不生效，反而会影响底色修改

        tagListViewSetter.childViewTextColor(R.id.header_item_icon, R.attr.tag_slv_item_icon);
        tagListViewSetter.childViewTextColor(R.id.header_item_title, R.attr.lv_item_title_color);
        tagListViewSetter.childViewTextColor(R.id.header_item_count, R.attr.lv_item_desc_color);
        tagListViewSetter.childViewBgColor(R.id.header_item, R.attr.bottombar_bg);

        tagListViewSetter.childViewTextColor(R.id.group_item_icon, R.attr.tag_slv_item_icon);
        tagListViewSetter.childViewTextColor(R.id.group_item_title, R.attr.lv_item_title_color);
        tagListViewSetter.childViewTextColor(R.id.group_item_count, R.attr.lv_item_desc_color);

        tagListViewSetter.childViewTextColor(R.id.child_item_title, R.attr.lv_item_title_color);
        tagListViewSetter.childViewTextColor(R.id.child_item_count, R.attr.lv_item_desc_color);


        mColorfulBuilder
                // 设置view的背景图片
                .backgroundColor(R.id.main_swipe, R.attr.root_view_bg)

                .backgroundColor(R.id.scrolllayout_bg, R.attr.bottombar_bg)
                .textColor(R.id.main_scrolllayout_title, R.attr.bottombar_fg)
                .backgroundColor(R.id.main_scrolllayout_divider, R.attr.bottombar_divider)

                // 设置 toolbar
                .backgroundColor(R.id.main_toolbar, R.attr.topbar_bg)
                .textColor(R.id.main_toolbar_hint, R.attr.topbar_fg)
                .textColor(R.id.main_toolbar_readability, R.attr.topbar_fg)

                // 设置 bottombar
                .backgroundColor(R.id.main_bottombar, R.attr.bottombar_bg)
                .backgroundColor(R.id.main_bottombar_divider, R.attr.bottombar_divider)// 设置中屏和底栏之间的分割线
                .textColor(R.id.main_bottombar_read, R.attr.bottombar_fg)
                .textColor(R.id.main_bottombar_star, R.attr.bottombar_fg)
                .textColor(R.id.main_bottombar_setting, R.attr.bottombar_fg)
                .textColor(R.id.main_bottombar_tag, R.attr.bottombar_fg)

                // 设置 listview 背景色
                .setter(tagListViewSetter)
                .setter(artListViewSetter);
        return mColorfulBuilder;
    }


    public void showSelectThemeDialog() {
//        selectTheme = ScreenUtil.resolveColor( this,R.attr.colorPrimary, 0);
        new ColorChooserDialog.Builder(this, R.string.readability_dialog_title)
//                .titleSub(R.string.md_custom_tag)
                .customColors(R.array.theme_colors, null)
                .preselect(0) // 预先选择
                .show();
        KLog.d("主题选择对话框");
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int color) {
//        selectTheme = color;
        KLog.e("被选择的颜色：" + color);
        manualToggleTheme();
    }

    public void onSettingIconClicked(View view){
        Intent intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, 0);
    }
    //定义一个startActivityForResult（）方法用到的整型值
    public void onTagIconClicked(View view){
//        Intent intent = new Intent(MainActivity.this,TagActivity.class);
//        intent.putExtra("ListState", listTabState);
//        intent.putExtra("ListTag", listTagId);
//        intent.putExtra("ListCount", App.articleList.size());
//        intent.putExtra("NoTagCount",getNoTagList().size());
//        startActivityForResult(intent, 0);
    }

    public void onStarIconClicked(View view){
        KLog.d(listTagId + listTabState + listTitle);
        KLog.d("收藏列表" + listTagId + listTabState + listTitle);
        if (listTabState.equals(API.LIST_STARED)) {
            ToastUtil.showShort("已经在收藏列表了");
        }else {
            vStarIcon.setText(getString(R.string.font_stared));
            vReadIcon.setText(getString(R.string.font_readed));

            listTabState = API.LIST_STARED;
            WithSet.i().setListTabState(listTabState);
            reloadData();
        }
    }
    public void onReadIconClicked(View view){
        KLog.d(listTagId + listTabState + listTitle);
        vStarIcon.setText(getString(R.string.font_unstar));
        if (listTabState.equals(API.LIST_UNREAD)) {
            vReadIcon.setText(getString(R.string.font_readed));
            listTabState = API.LIST_ALL;
        }else {
            vReadIcon.setText(getString(R.string.font_unread));
            listTabState = API.LIST_UNREAD;
        }
        WithSet.i().setListTabState(listTabState);
        reloadData();
    }



    /**
     * 监听返回键，弹出提示退出对话框
     */
    @Override
    public boolean onKeyDown(int keyCode , KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){ // 后者为短期内按下的次数
            if (mScrollLayout.getCurrentStatus() != ScrollLayout.Status.EXIT) {
                mScrollLayout.setToExit();
            } else {
                quitDialog();// 创建弹出的Dialog
            }
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
        mMenu.addItem(new MenuItem.Builder().setWidth(ScreenUtil.get2Px(this, R.dimen.slv_menu_left_width))
                .setBackground(new ColorDrawable(getResources().getColor(R.color.white)))
                .setIcon(getResources().getDrawable(R.drawable.ic_vector_menu_star, null)) // 插入图片
//                .setTextSize((int) getResources().getDimension(R.dimen.txt_size))
//                .setTextColor(ScreenUtil.getColor(R.color.crimson))
//                .setText("加星")
                .build());
        mMenu.addItem(new MenuItem.Builder().setWidth(ScreenUtil.get2Px(this, R.dimen.slv_menu_right_width))
                .setBackground(new ColorDrawable(getResources().getColor(R.color.white)))
                .setIcon(getResources().getDrawable(R.drawable.ic_vector_menu_adjust, null))
                .setDirection(MenuItem.DIRECTION_RIGHT) // 设置是左或右
//                .setTextColor(R.color.white)
//                .setTextSize(ScreenUtil.getDimen(this, R.dimen.txt_size))
//                .setText("已读")
                .build());
        mMenu.getItemBackGroundDrawable();
    }
}
