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
import me.wizos.loread.utils.DensityUtil;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.utils.ToastUtil;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.SwipeRefresh;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.colorful.setter.ViewGroupSetter;
import me.wizos.loread.view.common.color.ColorChooserDialog;

public class MainActivity extends BaseActivity implements SwipeRefresh.OnRefreshListener, ColorChooserDialog.ColorCallback, View.OnClickListener {
    protected static final String TAG = "MainActivity";
    private IconFontView vReadIcon, vStarIcon ,iconReadability;
    private ImageView vPlaceHolder;
    private TextView vToolbarHint;
    private Toolbar toolbar;
    private Menu mMenu;
    private SwipeRefresh mSwipeRefreshLayout;
    private SlideAndDragListView slv;

    public static String listTabState;
    private String listTagId;
    private String listTitle;
    private int tagCount;
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
        initData();
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
        KLog.i(WithSet.i().getUseName());
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
        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setEnabled(true);
        reloadData();
    }

    protected void initData(){
        readSetting();
        initBottombarIcon();
        reloadData();  // 先加载已有数据
        if (syncFirstOpen && !mSwipeRefreshLayout.isRefreshing()) {
            ToastUtil.showShort("开始同步");
            mSwipeRefreshLayout.setEnabled(false);
            startSyncService();
        }

        KLog.i("列表数目：" + App.articleList.size() + "  当前状态：" + listTabState);
    }

    /**
     * listTabState 包含 3 个状态：All，Unread，Stared
     * listTagId 至少包含 1 个状态： Reading-list
     * */
    protected void reloadData() { // 获取 App.articleList , 并且根据 App.articleList 的到未读数目
        KLog.i("加载数据");

        if (listTagId.contains(API.U_NO_LABEL)) {
            App.articleList = getNoTagList();  // FIXME: 2016/5/7 这里的未分类暂时无法使用，因为在云端订阅源的分类是可能会变的，导致本地缓存的文章分类错误
            KLog.i("【API.U_NO_LABEL】");
        } else if (listTabState.equals(API.LIST_STARED)) {
            App.articleList = WithDB.i().getStaredArtInTag(listTagId);
            KLog.i("【API.LIST_STARED】");
        } else {
            App.articleList = WithDB.i().getArt(listTabState, listTagId); // 590-55
            KLog.i("【API.getArt】" + listTabState + listTagId);

        }
        KLog.i("【】" + App.articleList.size() + listTabState + "--" + listTagId);

        if (StringUtil.isBlank(App.articleList)) {
            vPlaceHolder.setVisibility(View.VISIBLE);
            slv.setVisibility(View.GONE);
            ToastUtil.showShort("没有文章"); // 弹出一个提示框，询问是否同步
        }else {
            vPlaceHolder.setVisibility(View.GONE);
            slv.setVisibility(View.VISIBLE);
        }
        KLog.i("【notify1】" + listTabState + listTagId + toolbar.getTitle() + App.articleList.size());
        mainSlvAdapter = new MainSlvAdapter(this, App.articleList);
        slv.setAdapter(mainSlvAdapter);
        mainSlvAdapter.notifyDataSetChanged();
        changeToolbarTitle();
        tagCount = App.articleList.size();
        setToolbarHint(tagCount);
    }

    private List<Article> getNoTagList() {
        List<Article> all,part,exist;
        if (listTabState.contains(API.LIST_STARED)) {
            all = WithDB.i().getStaredArt();
            part = WithDB.i().loadStaredHasTag(App.mUserID);
            exist = WithDB.i().loadStarNoTag();
        }else {
            all = WithDB.i().getArt(listTabState);
            part = WithDB.i().loadReadListHasTag(listTabState, App.mUserID);
            exist = WithDB.i().loadReadNoTag();
       }

        ArrayList<Article> noTag = new ArrayList<>(all.size() - part.size());
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
                noTag.add(article);
            }
        }
        KLog.d( sb.toString() +" - "+  all.size() + " - "+ part.size());
        noTag.addAll(exist);
        return noTag;
    }


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
                    mSwipeRefreshLayout.setRefreshing(false);
                    mSwipeRefreshLayout.setEnabled(true);
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
    private void changeToolbarTitle(){
        if (listTagId.contains(API.U_READING_LIST)) {
            if (listTabState.equals(API.LIST_STARED)) {
                listTitle = "所有加星";
            } else if (listTabState.equals(API.LIST_UNREAD)) {
                listTitle = "所有未读";
            }else {
                listTitle = "所有文章";
            }
        } else if (listTagId.contains(API.U_NO_LABEL)) {
            if (listTabState.equals(API.LIST_STARED)) {
                listTitle = "加星未分类";
            } else if (listTabState.equals(API.LIST_UNREAD)) {
                listTitle = "未读未分类";
            }else {
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
                startActivityForResult(intent, 0);
                KLog.i("点击了" + articleID + position + "-" + App.articleList.size());
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
            ToastUtil.showShort("标为未读");
        }else {
            if (article.getTitle().contains("有没有相同兴趣爱好")) {
                KLog.e("========DDD=======" + article.getReadState());
            }
            KLog.e("----------------[]------" + article.getReadState());
            article.setReadState(API.ART_READED);
            mNeter.markArticleReaded(article.getId());
            changeItemNum( - 1 );
            ToastUtil.showShort("标为已读");
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
            case 1:
//                tagCount = intent.getExtras().getInt("tagCount");
                listTagId = intent.getExtras().getString("tagId");
                listTitle = intent.getExtras().getString("tagTitle");
//                KLog.e("【onActivityResult】" + listTagId + listTabState);
                if (listTagId == null || listTagId.equals("")) {
                    listTagId = "user/" + App.mUserID + "/state/com.google/reading-list";
                }
                WithSet.i().setListTagId(listTagId);
                reloadData();
                break;
            case 2:
                mNeter.getWithAuth(API.HOST + API.U_SUSCRIPTION_LIST);
                break;
            case 3:
                final int articleNo = intent.getExtras().getInt("articleNo");
//                if ( Math.abs(slv.getFirstVisiblePosition() - articleNo) > 3 ){
//                }
                slvSetSelection(articleNo);
                break;
        }
//        KLog.i("【== onActivityResult 】" + tagId + "----" + listTagId);
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
        ViewGroupSetter listViewSetter = new ViewGroupSetter(slv);
        // 绑定ListView的Item View中的news_title视图，在换肤时修改它的text_color属性
        listViewSetter.childViewTextColor(R.id.main_slv_item_title, R.attr.lv_item_title_color);
        listViewSetter.childViewTextColor(R.id.main_slv_item_summary, R.attr.lv_item_desc_color);
        listViewSetter.childViewTextColor(R.id.main_slv_item_author, R.attr.lv_item_info_color);
        listViewSetter.childViewTextColor(R.id.main_slv_item_time, R.attr.lv_item_info_color);
        listViewSetter.childViewBgColor(R.id.main_slv_item, R.attr.root_view_bg);
        listViewSetter.childViewBgColor(R.id.main_slv_item_divider, R.attr.lv_item_divider);
        mColorfulBuilder
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
                .setter(listViewSetter);
        return mColorfulBuilder;
    }


    //    private int selectTheme;
    public void showThemeSelectDialog() {
//        selectTheme = DensityUtil.resolveColor( this,R.attr.colorPrimary, 0);
        new ColorChooserDialog.Builder(this, R.string.readability_dialog_title)
//                .titleSub(R.string.md_custom_tag)
                .customColors(R.array.theme_colors, null)
                .preselect(0) // 预先选择
                .show();
        KLog.d("主题选择对话框");
    }
    // Receives callback from color chooser dialog
    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int color) {
//        selectTheme = color;
        KLog.e("被选择的颜色：" + color);
        toggleThemeAutomatic();
    }

    public void onSettingIconClicked(View view){
        Intent intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, 0);
    }
    //定义一个startActivityForResult（）方法用到的整型值
    public void onTagIconClicked(View view){
        Intent intent = new Intent(MainActivity.this,TagActivity.class);
        intent.putExtra("ListState", listTabState);
        intent.putExtra("ListTag", listTagId);
        intent.putExtra("ListCount", App.articleList.size());
//        intent.putExtra("NoTagCount",getNoTagList().size());
        startActivityForResult(intent, 0);
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
        mMenu.addItem(new MenuItem.Builder().setWidth(DensityUtil.get2Px(this, R.dimen.slv_menu_left_width))
                .setBackground(new ColorDrawable(getResources().getColor(R.color.white)))
                .setIcon(getResources().getDrawable(R.drawable.ic_vector_menu_star, null)) // 插入图片
//                .setTextSize((int) getResources().getDimension(R.dimen.txt_size))
//                .setTextColor(DensityUtil.getColor(R.color.crimson))
//                .setText("加星")
                .build());
        mMenu.addItem(new MenuItem.Builder().setWidth(DensityUtil.get2Px(this, R.dimen.slv_menu_right_width))
                .setBackground(new ColorDrawable(getResources().getColor(R.color.white)))
                .setIcon(getResources().getDrawable(R.drawable.ic_vector_menu_adjust, null))
                .setDirection(MenuItem.DIRECTION_RIGHT) // 设置是左或右
//                .setTextColor(R.color.white)
//                .setTextSize(DensityUtil.getDimen(this, R.dimen.txt_size))
//                .setText("已读")
                .build());
        mMenu.getItemBackGroundDrawable();
    }
}
