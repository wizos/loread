package me.wizos.loread.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.ArrayMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kyleduo.switchbutton.SwitchButton;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.socks.library.KLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.adapter.ExpandableListAdapterS;
import me.wizos.loread.adapter.MainListViewAdapter;
import me.wizos.loread.adapter.MaterialSimpleListAdapter;
import me.wizos.loread.adapter.MaterialSimpleListItem;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithPref;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.Tag;
import me.wizos.loread.event.Sync;
import me.wizos.loread.net.Api;
import me.wizos.loread.net.DataApi;
import me.wizos.loread.net.InoApi;
import me.wizos.loread.service.MainService;
import me.wizos.loread.utils.NetworkUtil;
import me.wizos.loread.utils.ScreenUtil;
import me.wizos.loread.utils.SnackbarUtil;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.utils.ToastUtil;
import me.wizos.loread.view.ExpandableListViewS;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.ListView.ListViewS;
import me.wizos.loread.view.SwipeRefreshLayoutS;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.colorful.setter.ViewGroupSetter;

//import com.zhangyue.we.x2c.X2C;
//import com.zhangyue.we.x2c.ano.Xml;

/**
 * @author Wizos on 2016
 */
//@Xml(layouts = {R.layout.activity_main})
public class MainActivity extends BaseActivity implements SwipeRefreshLayoutS.OnRefreshListener {
    protected static final String TAG = "MainActivity";
    private IconFontView vPlaceHolder;
    private TextView vToolbarHint;
    private Toolbar toolbar;
    private SwipeRefreshLayoutS swipeRefreshLayoutS;
    private ListViewS articleListView;
    private MainListViewAdapter articleListAdapter;
    private IconFontView refreshIcon;
    private ExpandableListViewS tagListView;
    private ExpandableListAdapterS tagListAdapter;
    private View headerPinnedView;
    private int tagCount;

//    private View headerHomeView;
//    private ImageLoader imageLoader;
//    private boolean isFirstIn = true;
//    private int firstVisibleItemPosition;
//    private int lastVisibleItemPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
//        X2C.setContentView(this, R.layout.activity_main);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initToolbar();
        initIconView();
        initArtListView();
        initTagListView();
        initSwipeRefreshLayout(); // 必须要放在 initArtListView() 之后，不然无论 ListView 滚动到第几页，一下拉就会触发刷新
        initData();  // 获取文章列表数据为 App.articleList
//        KLog.i("列表数目：" + App.articleList.size() + "  当前状态：" + App.StreamState);
        autoMarkReaded = WithPref.i().isScrollMark();
        maHandler.postDelayed(heartbeatTask, WithPref.i().getAutoSyncFrequency() * 60000);

//        if (savedInstanceState != null) {
//            final int position = savedInstanceState.getInt("listItemFirstVisiblePosition");
//            slvSetSelection(position);
//        }
        Intent intent = getIntent();
        if ("firstSetupStart".equals(intent.getAction())) {
            startSyncService(Api.SYNC_ALL);
        }
        showAutoSwitchThemeSnackBar();
        super.onCreate(savedInstanceState);
    }


//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        outState.putInt("listItemFirstVisiblePosition", articleListView.getFirstVisiblePosition());
//        super.onSaveInstanceState(outState);
//    }


    @Override
    protected void onResume(){
        super.onResume();
        if (articleListAdapter != null) {
            articleListAdapter.notifyDataSetChanged();
            KLog.e("通知articleList数据有变化");
        }
//        tagCount = UnreadCountUtil.getUnreadCount(App.StreamId);
//        tagCount = App.articleList.size();
//        KLog.i("【onResume】" + App.StreamState + "---" + toolbar.getTitle() + "===" + App.StreamId);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveSyncResult(Sync sync) {
        int result = sync.result;
//        KLog.e( "接收到的数据为："+ result );
        switch (result) {
            case Sync.START:
                swipeRefreshLayoutS.setRefreshing(true);
                swipeRefreshLayoutS.setEnabled(false);
                break;
            case Sync.END:
                swipeRefreshLayoutS.setRefreshing(false);
                swipeRefreshLayoutS.setEnabled(true);
                toolbar.setSubtitle(null);
                SnackbarUtil.Long(articleListView, "有新文章")
                        .setAction("查看", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                refreshData();
                                IconFontView loadNewArticlesIcon = findViewById(R.id.main_bottombar_refresh_articles);
                                loadNewArticlesIcon.setVisibility(View.GONE);
                            }
                        }).show();
                IconFontView loadNewArticlesIcon = findViewById(R.id.main_bottombar_refresh_articles);
                loadNewArticlesIcon.setVisibility(View.VISIBLE);
                break;
            // 文章获取失败
            case Sync.ERROR:
                swipeRefreshLayoutS.setRefreshing(false);
                swipeRefreshLayoutS.setEnabled(true);
                toolbar.setSubtitle(null);
//                vToolbarHint.setText(String.valueOf(tagCount));
                break;
            case Sync.DOING:
//                KLog.e("接受的文字：" + sync.notice);
                toolbar.setSubtitle(sync.notice);
                break;
            default:
                toolbar.setSubtitle(null);
                break;
        }
    }


    private void showAutoSwitchThemeSnackBar() {
        if (App.hadAutoToggleTheme) {
            SnackbarUtil.Long(articleListView, "已自动切换主题")
//                                .above(bt_gravity_center,total,16,16)
                    .setAction("撤销", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            manualToggleTheme();
                        }
                    }).show();
            App.hadAutoToggleTheme = false;
        }
    }


    Runnable heartbeatTask = new Runnable() {
        @Override
        public void run() {
            if (!WithPref.i().isAutoSync()) {
                return;
            }
            if (WithPref.i().isAutoSyncOnWifi() && !NetworkUtil.isWiFiUsed()) {
                return;
            }
            startSyncService(Api.SYNC_HEARTBEAT);
            maHandler.postDelayed(this, WithPref.i().getAutoSyncFrequency() * 60000);
        }
    };

    private void initHeartbeat() {
//        KLog.e("时间间隔" + WithPref.i().getAutoSyncFrequency());
        maHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!WithPref.i().isAutoSync()) {
                    return;
                }
                if (WithPref.i().isAutoSyncOnWifi() && !NetworkUtil.isWiFiUsed()) {
                    return;
                }
                startSyncService(Api.SYNC_HEARTBEAT);
                initHeartbeat();
            }
        }, WithPref.i().getAutoSyncFrequency() * 60000);
    }


    protected void initIconView() {
        vToolbarHint = findViewById(R.id.main_toolbar_hint);
        vPlaceHolder = findViewById(R.id.main_placeholder);
        refreshIcon = findViewById(R.id.main_bottombar_refresh_articles);
    }

    public void clickSearchIcon(View view) {
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        startActivityForResult(intent, 0);
        overridePendingTransition(R.anim.in_from_bottom, R.anim.fade_out);
    }

    protected void initSwipeRefreshLayout() {
        swipeRefreshLayoutS = findViewById(R.id.main_swipe_refresh);
        if (swipeRefreshLayoutS == null) {
            return;
        }
        swipeRefreshLayoutS.setOnRefreshListener(this);
        //设置样式刷新显示的位置
        swipeRefreshLayoutS.setProgressViewOffset(true, 0, 120);
        swipeRefreshLayoutS.setViewGroup(articleListView);
    }

    @Override
    public void onRefresh() {
        if (!swipeRefreshLayoutS.isEnabled()) {
            return;
        }
        KLog.i("【刷新中】");
        startSyncService(Api.SYNC_ALL);
        swipeRefreshLayoutS.setRefreshing(true);
        swipeRefreshLayoutS.setEnabled(false);
    }

    // 按下back键时会调用onDestroy()销毁当前的activity，重新启动此activity时会调用onCreate()重建；
    // 而按下home键时会调用onStop()方法，并不销毁activity，重新启动时则是调用onResume()
    @Override
    protected void onDestroy() {
        // 如果参数为null的话，会将所有的Callbacks和Messages全部清除掉。
        // 这样做的好处是在Acticity退出的时候，可以避免内存泄露。因为 handler 内可能引用 Activity ，导致 Activity 退出后，内存泄漏。
        maHandler.removeCallbacksAndMessages(null);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    private void startSyncService(String action) {
        Intent intent = new Intent(this, MainService.class);
        intent.setAction(action);
        startService(intent);
        KLog.i("调用 SubService，开始 SYNC_ALL");
    }


    // 这里不必和 ArticleActivity 一样写成静态内部类，用来防止因持有外部的Activity而造成内存泄漏。
    // 因为MainActivity基本只有一个实例，且不会反复创建、销毁，所以不用担心回收造成的内存泄漏问题。
    private Handler maHandler = new Handler();

    /**
     * App.StreamState 包含 3 个状态：All，Unread，Stared
     * App.StreamId 至少包含 1 个状态： Reading-list
     * */
    protected void refreshData() { // 获取 App.articleList , 并且根据 App.articleList 的到未读数目
        KLog.e("refreshData：" + App.StreamId + "  " + " - " + App.StreamStatus + "   " + App.UserID);
        // 取消所有下载图片的任务，防止下载后去加载到imageview中
//        imageLoader.cancelAllLoadTask();
//        isFirstIn = true;
        getArtData();
        articleListAdapter = new MainListViewAdapter(this, App.articleList, articleListView);
        articleListView.setAdapter(articleListAdapter);
//        getTagData();
//        tagListAdapter.notifyDataSetChanged();
        loadViewByData();
        refreshIcon.setVisibility(View.GONE);
    }


    private void initData() {
        getArtData();
        articleListAdapter = new MainListViewAdapter(this, App.articleList, articleListView);
        articleListView.setAdapter(articleListAdapter);
        getTagData();
        tagListAdapter = new ExpandableListAdapterS(this, App.tagList, tagListView);
        tagListView.setAdapter(tagListAdapter);
        loadViewByData();
    }

    @SuppressWarnings("unchecked")
    private void getArtData() {
        if (App.StreamId.startsWith("user/")) {
            if (App.StreamId.contains(Api.U_READING_LIST)) {
                if (App.StreamStatus == Api.STARED) {
                    App.articleList = WithDB.i().getArtsStared();
                } else if (App.StreamStatus == Api.UNREAD) {
                    App.articleList = WithDB.i().getArtsUnread();
                } else {
                    App.articleList = WithDB.i().getArtsAll();
                }
            } else if (App.StreamId.contains(Api.U_NO_LABEL)) {
                if (App.StreamStatus == Api.STARED) {
                    App.articleList = WithDB.i().getArtsStaredNoTag();
                } else if (App.StreamStatus == Api.UNREAD) {
                    App.articleList = WithDB.i().getArtsUnreadNoTag();
                } else {
                    App.articleList = WithDB.i().getArtsAllNoTag();
                }
            } else {
                // TEST:  测试
                Tag theTag = WithDB.i().getTag(App.StreamId);
                if (App.StreamStatus == Api.STARED) {
                    App.articleList = WithDB.i().getArtsStaredInTag(theTag);
                } else if (App.StreamStatus == Api.UNREAD) {
                    App.articleList = WithDB.i().getArtsUnreadInTag(theTag);
                } else {
                    App.articleList = WithDB.i().getArtsAllInTag(theTag);
                }
            }
        } else if (App.StreamId.startsWith("feed/")) {
            if (App.StreamStatus == Api.STARED) {
                App.articleList = WithDB.i().getArtsStaredInFeed(App.StreamId);
            } else if (App.StreamStatus == Api.UNREAD) {
                App.articleList = WithDB.i().getArtsUnreadInFeed(App.StreamId);
            } else {
                App.articleList = WithDB.i().getArtsAllInFeed(App.StreamId);
            }
            App.StreamTitle = WithDB.i().getFeed(App.StreamId).getTitle();
        }
        startSyncService(Api.CLEAR);
    }

    private void getTagData() {
        long time = System.currentTimeMillis();
        Tag rootTag = new Tag();
        Tag noLabelTag = new Tag();
        long userID = WithPref.i().getUseId();
        rootTag.setTitle(getString(R.string.main_activity_title_all));
        noLabelTag.setTitle(getString(R.string.main_activity_title_untag));

        rootTag.setId("user/" + userID + Api.U_READING_LIST);
        rootTag.setSortid("00000000");
        rootTag.__setDaoSession(App.i().getDaoSession());

        noLabelTag.setId("user/" + userID + Api.U_NO_LABEL);
        noLabelTag.setSortid("00000001");
        noLabelTag.__setDaoSession(App.i().getDaoSession());

        List<Tag> tagListTemp = new ArrayList<>();
        tagListTemp.add(rootTag);
        tagListTemp.add(noLabelTag);
        tagListTemp.addAll(WithDB.i().getAllTag());

        App.i().updateTagList(tagListTemp);
        KLog.e("加载tag耗时：" + (System.currentTimeMillis() - time));
    }


    private void loadViewByData() {
//        KLog.i("【】" + App.StreamState + "--" + App.StreamTitle + "--" + App.StreamId + "--" + toolbar.getTitle() + App.articleList.size());
        if (StringUtil.isBlank(App.articleList)) {
            vPlaceHolder.setVisibility(View.VISIBLE);
            articleListView.setVisibility(View.GONE);
        }else {
            vPlaceHolder.setVisibility(View.GONE);
            articleListView.setVisibility(View.VISIBLE);
        }

        // 每次重新加载列表数据的时候，应该把 ArticleListAdapter 和 TagListAdapter 都更新一下。
        // adapter中的数据源集合或数组等必须是同一个数据源，也就是同一个对象。
        // 当数据源发生变化的时候，我们会调用adaper的notifyDataSetChanged()方法。
        // 当直接将从数据库或者其他方式获取的数据源集合或者数组直接赋值给当前数据源时，相当于当前数据源的对象发生了变化。
        // 当前对象已经不是adapter中的对象了，所以adaper调用notifyDataSetChanged()方法不会进行刷新数据和界面的操作。
        KLog.e("loadViewByData" + "=" + "=" + App.tagList.size());
        // 在setSupportActionBar(toolbar)之后调用toolbar.setTitle()的话。 在onCreate()中调用无效。在onStart()中调用无效。 在onResume()中调用有效。
        // toolbar.setTitle(App.StreamTitle);
        getSupportActionBar().setTitle(App.StreamTitle);
//        toolbar.setSubtitle(null);
//        KLog.e("loadViewByData","此时StreamId为：" + App.StreamId +  "   此时 Title 为：" +  App.StreamTitle );

//        tagCount = UnreadCountUtil.getUnreadCount(App.StreamId);
        tagCount = App.articleList.size();
        vToolbarHint.setText(String.valueOf(tagCount));
    }

    public void showTagDialog(final Tag tag) {
        // 重命名弹窗的适配器
        MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(MainActivity.this);
        adapter.add(new MaterialSimpleListItem.Builder(MainActivity.this)
                .content(R.string.main_tag_dialog_rename)
                .icon(R.drawable.dialog_ic_rename)
                .backgroundColor(Color.TRANSPARENT)
                .build());
        new MaterialDialog.Builder(MainActivity.this)
                .adapter(adapter, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        if (which == 0) {
                            new MaterialDialog.Builder(MainActivity.this)
                                    .title(R.string.rename)
                                    .inputType(InputType.TYPE_CLASS_TEXT)
                                    .inputRange(1, 22)
                                    .input(null, tag.getTitle(), new MaterialDialog.InputCallback() {
                                        @Override
                                        public void onInput(MaterialDialog dialog, CharSequence input) {
                                            renameTag(input.toString(), tag);
                                        }
                                    })
                                    .positiveText(R.string.confirm)
                                    .negativeText(android.R.string.cancel)
                                    .show();
                        }
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void renameTag(final String renamedTagTitle, Tag tag) {
        KLog.e("=====" + renamedTagTitle);
        if (renamedTagTitle.equals("") || tag.getTitle().equals(renamedTagTitle)) {
            return;
        }
        final String destTagId = tag.getId().replace(tag.getTitle(), renamedTagTitle);
        final String sourceTagId = tag.getId();
        DataApi.i().renameTag(sourceTagId, destTagId, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if (!response.body().equals("OK")) {
                    this.onError(response);
                    return;
                }
                Tag tag = WithDB.i().getTag(sourceTagId);
                if (tag == null) {
                    this.onError(response);
                    return;
                }
                WithDB.i().delTag(tag);
                tag.setId(destTagId);
                tag.setTitle(renamedTagTitle);
                WithDB.i().insertTag(tag);
                WithDB.i().updateFeedsCategoryId(sourceTagId, destTagId); // 由于改 tag 的名字，涉及到改 tag 的 id ，而每个 feed 自带的 tag 的 id 也得改过来。
                tagListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Response<String> response) {
                ToastUtil.showLong(getString(R.string.toast_rename_fail));
            }
        });
    }

    public void showFeedDialog(final ExpandableListAdapterS.ItemViewHolder itemView, final Feed feed) {
        if (feed == null) {
            return;
        }
        // 重命名弹窗的适配器
        MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(MainActivity.this);
        adapter.add(new MaterialSimpleListItem.Builder(MainActivity.this)
                .content(R.string.main_tag_dialog_rename)
                .icon(R.drawable.dialog_ic_rename)
                .backgroundColor(Color.TRANSPARENT)
                .build());
        adapter.add(new MaterialSimpleListItem.Builder(MainActivity.this)
                .content(R.string.main_tag_dialog_unsubscribe)
                .icon(R.drawable.dialog_ic_unsubscribe)
                .backgroundColor(Color.TRANSPARENT)
                .build());
        new MaterialDialog.Builder(MainActivity.this)
                .adapter(adapter, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        switch (which) {
                            case 0:
                                new MaterialDialog.Builder(MainActivity.this)
                                        .title(R.string.rename)
                                        .inputType(InputType.TYPE_CLASS_TEXT)
                                        .inputRange(1, 22)
                                        .input(null, feed.getTitle(), new MaterialDialog.InputCallback() {
                                            @Override
                                            public void onInput(MaterialDialog dialog, CharSequence input) {
                                                renameFeed(input.toString(), feed);
                                            }
                                        })
                                        .positiveText(R.string.confirm)
                                        .negativeText(android.R.string.cancel)
                                        .show();
                                break;
                            case 1:
                                DataApi.i().unsubscribeFeed(feed.getId(), new StringCallback() {
                                    @Override
                                    public void onSuccess(Response<String> response) {
                                        if (!response.body().equals("OK")) {
                                            this.onError(response);
                                            return;
                                        }
                                        WithDB.i().delFeed(feed);
//                                        KLog.e("移除" + itemView.groupPos + "  " + itemView.childPos );
//                                        tagListAdapter.removeChild(itemView.groupPos, itemView.childPos);
                                        tagListAdapter.removeChild(itemView.groupPos, feed);
                                        tagListAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onError(Response<String> response) {
                                        ToastUtil.showLong(getString(R.string.toast_unsubscribe_fail));
                                    }
                                });
                                break;
                            default:
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void renameFeed(final String renamedTitle, Feed feed) {
        final String feedId = feed.getId();
        KLog.e("=====" + renamedTitle + feedId);
        if (renamedTitle.equals("") || feed.getTitle().equals(renamedTitle)) {
            return;
        }
        DataApi.i().renameFeed(feedId, renamedTitle, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if (!response.body().equals("OK")) {
                    this.onError(response);
                    return;
                }
                Feed feed = WithDB.i().getFeed(feedId);
                if (feed == null) {
                    this.onError(response);
                    return;
                }
                feed.setTitle(renamedTitle);
                WithDB.i().updateFeed(feed);
                // 由于改了 feed 的名字，而每个 article 自带的 feed 名字也得改过来。
                WithDB.i().updateArtsFeedTitle(feed);
                tagListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Response<String> response) {
                ToastUtil.showLong(getString(R.string.toast_rename_fail));
            }
        });
    }



    public void onTagIconClicked1(View view) {
        getTagData();
//        tagListAdapter = new ExpandableListAdapterS(this, App.tagList, tagListView);
//        tagListView.setAdapter(tagListAdapter);
        tagListAdapter.notifyDataSetChanged();
        tagBottomSheetDialog.show();
        KLog.e("tag按钮被点击");
    }


    BottomSheetDialog tagBottomSheetDialog;
    RelativeLayout relativeLayout;
    public void initTagListView() {
        tagBottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        tagBottomSheetDialog.setContentView(R.layout.main_bottom_sheet_tag);
        View view = tagBottomSheetDialog.getWindow().findViewById(android.support.design.R.id.design_bottom_sheet);
        BottomSheetBehavior.from(view).setPeekHeight(ScreenUtil.getScreenHeight(this));

        relativeLayout = tagBottomSheetDialog.findViewById(R.id.sheet_tag);
        IconFontView iconFontView = tagBottomSheetDialog.findViewById(R.id.main_tag_close);
        iconFontView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagBottomSheetDialog.dismiss();
            }
        });
        tagListView = tagBottomSheetDialog.findViewById(R.id.main_tag_list_view);

        // 设置悬浮头部VIEW
        headerPinnedView = getLayoutInflater().inflate(R.layout.tag_expandable_item_group_header, tagListView, false);
        tagListView.setPinnedHeaderView(headerPinnedView);
        tagListView.setOnPinnedGroupClickListener(new ExpandableListViewS.OnPinnedGroupClickListener() {
            @Override
            public void onHeaderClick(ExpandableListView parent, View v, int pinnedGroupPosition) {
                if (parent.isGroupExpanded(pinnedGroupPosition)) {
                    tagListView.collapseGroup(pinnedGroupPosition);
                } else {
                    tagListView.expandGroup(pinnedGroupPosition);
                }
            }
        });

//        headerHomeView  = getLayoutInflater().inflate(R.layout.tag_expandable_item_header_home, tagListView, false);
//        tagListView.addHeaderView(headerHomeView);
//        headerHomeView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                KLog.e("点击了所有");
//                getArtDataAll();
//            }
//        });


        tagListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                tagBottomSheetDialog.dismiss();
                App.StreamId = App.tagList.get(groupPosition).getId().replace("\"", "");
                App.StreamTitle = App.tagList.get(groupPosition).getTitle();
//                if (App.StreamId == null || App.StreamId.equals("")) {
//                    App.StreamId = "user/" + App.UserID + "/state/com.google/reading-list";
//                }
//                KLog.e("【 TagList 被点击】" + App.StreamId + App.StreamState);
                WithPref.i().setStreamId(App.StreamId);
                refreshData();
                return true;
            }
        });

        tagListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//                KLog.e("子项被点击1：" + v + " - " + v.getTag() + "-" + groupPosition + "==" + childPosition + "=" + id);
                tagBottomSheetDialog.dismiss();
                Feed theFeed = App.tagList.get(groupPosition).getFeeds().get(childPosition);

                App.StreamId = theFeed.getId();
                App.StreamTitle = theFeed.getTitle();
                WithPref.i().setStreamId(App.StreamId);
                refreshData();

//                KLog.e("【子项被点击2】" + App.StreamId + App.StreamState);
                return true;
            }
        });

        tagListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
//                KLog.e("被长安，view的id是" + view.getId() + "，parent的id" + parent.getId() + "，Tag是" + view.getTag() + "，位置是" + tagListView.getPositionForView(view));

                ExpandableListAdapterS.ItemViewHolder itemView = (ExpandableListAdapterS.ItemViewHolder) view.getTag();
                if (InoApi.isTag(itemView.id) && position != 0 && position != 1) {
                    showTagDialog(WithDB.i().getTag(itemView.id));
                } else if (InoApi.isFeed(itemView.id)) {
                    showFeedDialog(itemView, WithDB.i().getFeed(itemView.id));
                }
                return true;
            }
        });
    }

    private boolean autoMarkReaded = false;
    private int lastAutoMarkPos = -1;

    private void showConfirmDialog(final int start, final int end) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(R.string.main_dialog_confirm_mark_article_list)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Integer[] index = new Integer[2];
                        index[0] = start;
                        index[1] = end;
                        new MarkListReadedAsyncTask().execute(index);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void initArtListView() {
        articleListView = findViewById(R.id.main_slv);
        if (articleListView == null) {
            return;
        }
//        imageLoader = new ImageLoader(this,articleListView);
        // 由于item内有view添加了onItemClickListener,所以事件被消耗，没有回调到ListView OnItemLongClick方法。
        articleListView.setOnListItemLongClickListener(new ListViewS.OnListItemLongClickListener() {
            @Override
            public void onListItemLongClick(View view, final int position) {
//                KLog.e("长按===");
                final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(MainActivity.this);
                adapter.add(new MaterialSimpleListItem.Builder(MainActivity.this)
                        .content(R.string.main_slv_dialog_mark_up)
                        .icon(R.drawable.dialog_ic_mark_up)
                        .backgroundColor(Color.WHITE)
                        .build());
                adapter.add(new MaterialSimpleListItem.Builder(MainActivity.this)
                        .content(R.string.main_slv_dialog_mark_down)
                        .icon(R.drawable.dialog_ic_mark_down)
                        .backgroundColor(Color.WHITE)
                        .build());
                adapter.add(new MaterialSimpleListItem.Builder(MainActivity.this)
                        .content(R.string.main_slv_dialog_mark_unread)
                        .icon(R.drawable.dialog_ic_mark_unread)
                        .backgroundColor(Color.WHITE)
                        .build());

                new MaterialDialog.Builder(MainActivity.this)
                        .adapter(adapter, new MaterialDialog.ListCallback() {
                            @Override
                            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                Integer[] index = new Integer[2];
                                switch (which) {
                                    case 0:
                                        index[0] = 0;
                                        index[1] = position + 1;
                                        MarkListReadedAsyncTask changeReadedList = new MarkListReadedAsyncTask();
                                        changeReadedList.execute(index);
                                        break;
                                    case 1:
                                        showConfirmDialog(position, App.articleList.size());
                                        break;
                                    case 2:
                                        Article article = App.articleList.get(position);
                                        if (article.getReadStatus() == Api.READED) {
                                            DataApi.i().markArticleUnread(article.getId(), null);
                                        }
                                        // 方法2
                                        WithDB.i().setUnreading(article);

                                        articleListAdapter.notifyDataSetChanged();
                                        break;
                                    default:
                                        break;
                                }

                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        });


        articleListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
//                if( scrollState==SCROLL_STATE_IDLE ){
//                    imageLoader.loadImages(firstVisibleItemPosition,lastVisibleItemPosition);
//                }
            }

            @Override
            public void onScroll(AbsListView absListView, final int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                KLog.e("滚动：" + firstVisibleItem+  "  " + visibleItemCount + "   " + totalItemCount);
                Integer[] index = new Integer[2];
                index[0] = firstVisibleItem;
                new MarkReadedAsyncTask().execute(index);

//                firstVisibleItemPosition = firstVisibleItem;
//                lastVisibleItemPosition = firstVisibleItem + visibleItemCount;
//                // 当首次显示listview时加载图片
//                if( isFirstIn && visibleItemCount>0 ){
//                    imageLoader.loadImages(firstVisibleItemPosition,lastVisibleItemPosition);
//                    isFirstIn = false;
//                }
//                // 取消滚出屏幕的下载任务
//                imageLoader.cancelLoadTask(firstVisibleItem-1);
            }
        });

        articleListView.setItemSlideListener(new ListViewS.OnItemSlideListener() {
            @Override
            public void onUpdate(View view, int position, float offset) {
                // 推测由于该函数 getView 已经生成了 View 所以不在更新了。使用 notifyDataSetChanged(); 也不行
//                KLog.e("观察" + offset + "  " + lastOffset);
//                SearchListViewAdapter.CustomViewHolder itemViewHolder;
//                int firstVisiblePosition = articleListView.getFirstVisiblePosition(); //屏幕内当前可以看见的第一条数据
//                if( position-firstVisiblePosition>=0){
//                    View itemView = articleListView.getChildAt(position - firstVisiblePosition);
//                    itemViewHolder = (SearchListViewAdapter.CustomViewHolder) itemView.getTag();
//
//                    if( offset < -0.6 && hadChanged==false ){
//                        if( (offset - lastOffset) >  0 ){ // 向右滑
//                            itemViewHolder.markLeft.setTextColor( getResources().getColor(R.color.crimson));
//                            hadChanged=true;
//                        }else {
//                            itemViewHolder.markLeft.setTextColor( getResources().getColor(R.color.colorPrimary));
//                            hadChanged=false;
//                        }
//                        KLog.e("变色" );
//
//                    }else if( offset > 0.6 && hadChanged==false ){
//                        KLog.e("变色" );
//                        if( ( offset - lastOffset ) >  0 ){ // 向左滑
//                            itemViewHolder.markLeft.setTextColor( getResources().getColor(R.color.crimson));
//                            hadChanged=true;
//                        }else {
//                            itemViewHolder.markLeft.setTextColor( getResources().getColor(R.color.colorPrimary));
//                            hadChanged=false;
//                        }
//                    }
//                }
//                lastOffset = offset;
            }

            @Override
            public void onCloseLeft(View view, int position, int direction) {
                KLog.e("onCloseLeft：" + position + "  ");
                Article article = App.articleList.get(position);
                toggleStarState(article);
            }

            @Override
            public void onCloseRight(View view, int position, int direction) {
                KLog.e("onCloseRight：" + position + "  ");
                Article article = App.articleList.get(position);
                toggleReadState(article);
            }

            // 由于父 listview 被重载，onClick 事件也被重写了。无法直接使用 setOnItemClickListener
            @Override
            public void onClick(View view, int position) {
                if (position == -1) {
                    return;
                }

                Intent intent = new Intent(MainActivity.this, ArticleActivity3.class);

//                String[] articleIDs = new String[App.articleList.size()];
//                for (int i=0, size = App.articleList.size(); i<size; i++){
//                    articleIDs[i] = App.articleList.get(i).getId();
//                }
//                intent.putExtra("articleIDs",articleIDs);
//                intent.putStringArrayListExtra("articleIDs",new ArrayList<String>(Arrays.asList(articleIDs)) );

                String articleID = App.articleList.get(position).getId();
                intent.putExtra("articleID", articleID);
                // 下标从 0 开始
                intent.putExtra("articleNo", position);
                intent.putExtra("articleCount", App.articleList.size());
                startActivityForResult(intent, 0);
                overridePendingTransition(R.anim.in_from_bottom, R.anim.fade_out);
                KLog.i("点击了" + articleID + "，位置：" + position + "，文章ID：" + articleID + "    " + App.articleList.size());
            }

            @Override
            public void log(String layout) {
                KLog.e(layout);
            }
        });
    }


    // Params, Progress, Result
    private class MarkReadedAsyncTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected Integer doInBackground(Integer... params) {
            int firstVisibleItemPos = params[0];

            if (autoMarkReaded
                    && lastAutoMarkPos != firstVisibleItemPos
                    && App.articleList.get(firstVisibleItemPos).getReadStatus() == Api.UNREAD) {

                DataApi.i().markArticleReaded(App.articleList.get(firstVisibleItemPos).getId(), null);
                // 方法2
                WithDB.i().setReaded(App.articleList.get(firstVisibleItemPos));
                lastAutoMarkPos = firstVisibleItemPos;
//              KLog.e("标记已读：" + lastAutoMarkPos);
            }

            //返回结果
            return 0;
        }
    }


    // Params, Progress, Result
    private class MarkListReadedAsyncTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected Integer doInBackground(Integer... params) {
            int startIndex, endIndex;
            startIndex = params[0];
            endIndex = params[1];
            List<Article> articleList = new ArrayList<>(endIndex - startIndex);
            List<String> articleIDs = new ArrayList<>(endIndex - startIndex);
            List<String> feedIDs = new ArrayList<>(endIndex - startIndex);
            ArrayMap<String, Integer> feedIDMap = new ArrayMap<>(endIndex - startIndex);

//            for (int i = endIndex - 1; i >= startIndex; i--) {
            Article article;
            for (int i = startIndex; i < endIndex; i++) {
                article = App.articleList.get(i);
                if (article.getReadStatus() == Api.UNREAD) {
                    article.setReadStatus(Api.READED);
                    articleList.add(article);
                    articleIDs.add(article.getId());
                    feedIDs.add(article.getOriginStreamId());

                    if (feedIDMap.containsKey(article.getOriginStreamId())) {
                        feedIDMap.put(article.getOriginStreamId(), feedIDMap.get(article.getOriginStreamId()) + 1);
                    } else {
                        feedIDMap.put(article.getOriginStreamId(), 1);
                    }
//                    WithDB.i().setReaded(App.articleList.get(i));
//                    DataApi.i().changeUnreadCount(App.articleList.get(i).getOriginStreamId(), -1);
                }
            }

            if (articleIDs.size() == 0) {
                onCancelled();
                return 0;
            }
            DataApi.i().markArticleListReaded(articleIDs, null);
            WithDB.i().saveArticles(articleList);


            List<Feed> feeds = WithDB.i().getFeeds(feedIDs);
            for (Feed feed : feeds) {
                feed.setUnreadCount(feed.getUnreadCount() - feedIDMap.get(feed.getId()));
            }
            WithDB.i().saveFeeds(feeds);

            //提交之后，会执行onProcessUpdate方法
            publishProgress(-articleIDs.size());
            //返回结果
            return 0;
        }

//        /**
//         * 在调用cancel方法后会执行到这里
//         */
//        @Override
//        protected void onCancelled() {
//        }
//
//        /**
//         * 在doInbackground之后执行
//         */
//        @Override
//        protected void onPostExecute(Integer args3) {
//        }
//
//        /**
//         * 在doInBackground之前执行
//         */
//        @Override
//        protected void onPreExecute() {
//        }

        /**
         * 特别赞一下这个多次参数的方法，特别方便
         *
         * @param progress
         */
        @Override
        protected void onProgressUpdate(Integer... progress) {
            articleListAdapter.notifyDataSetChanged();
            // 应该是去通知对应的那个 item 改变。
        }
    }

    private void showSearchResult(String keyword) {
//        List<Article> articles = WithDB.i().getSearchedArts( keyword );
//        App.i().updateArtList(WithDB.i().getSearchedArts(keyword));
        App.StreamId = Api.U_Search;
        App.StreamTitle = getString(R.string.main_toolbar_title_search) + keyword;
        App.articleList = WithDB.i().getSearchedArts(keyword);
        articleListAdapter = new MainListViewAdapter(this, App.articleList, articleListView);
        articleListView.setAdapter(articleListAdapter);
        loadViewByData();
    }



    private void toggleReadState(final Article article) {
        if (autoMarkReaded && article.getReadStatus() == Api.UNREAD) {
            WithDB.i().setUnreading(article);
        } else if (article.getReadStatus() == Api.READED) {
            DataApi.i().markArticleUnread(article.getId(), null);
            WithDB.i().setUnreading(article);
        }else {
            DataApi.i().markArticleReaded(article.getId(), null);
            WithDB.i().setReaded(article);
        }
        articleListAdapter.notifyDataSetChanged();
    }


    private void toggleStarState(final Article article) {
        if (article.getStarStatus() == Api.STARED) {
            article.setStarStatus(Api.UNSTAR);
            DataApi.i().markArticleUnstar(article.getId(), null);
        }else {
            article.setStarStatus(Api.STARED);
            DataApi.i().markArticleStared(article.getId(), null);
            article.setStarred(System.currentTimeMillis() / 1000);
        }
        WithDB.i().saveArticle(article);
        articleListAdapter.notifyDataSetChanged();
    }


    // TODO: 2018/3/4 改用观察者模式。http://iaspen.cn/2015/05/09/观察者模式在android%20上的最佳实践

    /**
     * 在android中从A页面跳转到B页面，然后B页面进行某些操作后需要通知A页面去刷新数据，
     * 我们可以通过startActivityForResult来唤起B页面，然后再B页面结束后在A页面重写onActivityResult来接收返回结果从而来刷新页面。
     * 但是如果跳转路径是这样的A->B->C->…..，C或者C以后的页面来刷新A，这个时候如果还是使用这种方法就会非常的棘手。
     * 使用这种方法可能会存在以下几个弊端：
     * 1、多个路径或者多个事件的传递处理起来会非常困难。
     * 2、数据更新不及时，往往需要用户去等待，降低系统性能和用户体验。
     * 3、代码结构混乱，不易编码和扩展。
     * 因此考虑使用观察者模式去处理这个问题。
     */
    @Override
    protected void onActivityResult(int requestCode , int resultCode , Intent intent){
//        KLog.e("------------------------------------------" + resultCode + requestCode);
        switch (resultCode){
            // 这一段应该用不到了
            case Api.ActivityResult_TagToMain:
                refreshData(); // TagToMain
                break;
            case Api.ActivityResult_ArtToMain:
                // 在文章页的时候读到了第几篇文章，好让列表也自动将该项置顶
                int articleNo = intent.getExtras().getInt("articleNo");
                if (articleNo > articleListView.getLastVisiblePosition()) {
                    slvSetSelection(articleNo);
                }
//                KLog.e("【onActivityResult】" + articleNo + "  " + articleListView.getLastVisiblePosition());
                break;
            case Api.ActivityResult_SearchLocalArtsToMain:
//                KLog.e("被搜索的词是" + intent.getExtras().getString("searchWord"));
                showSearchResult(intent.getExtras().getString("searchWord"));
                break;
            default:
                break;
        }
    }

    // 滚动到指定位置
    private void slvSetSelection(final int position) {
        articleListView.post(new Runnable() {
            @Override
            public void run() {
                articleListView.setSelection(position); // 不能直接用这个，无法滚动
            }
        });
    }

    public void clickRefreshIcon(View view) {
        refreshData();
    }

    private BottomSheetDialog quickSettingDialog;
    public void onQuickSettingIconClicked(View view) {
        quickSettingDialog = new BottomSheetDialog(MainActivity.this);
        quickSettingDialog.setContentView(R.layout.main_bottom_sheet_more);
//        quickSettingDialog.dismiss(); //dialog消失
//        quickSettingDialog.setCanceledOnTouchOutside(false);  //触摸dialog之外的地方，dialog不消失
//        quickSettingDialog.setCancelable(false); // dialog无法取消，按返回键都取消不了

        View moreSetting = quickSettingDialog.findViewById(R.id.more_setting);
        SwitchButton autoMarkWhenScrolling = quickSettingDialog.findViewById(R.id.auto_mark_when_scrolling_switch);
        SwitchButton downImgOnWifiSwitch = quickSettingDialog.findViewById(R.id.down_img_on_wifi_switch);
        RadioGroup radioGroup = quickSettingDialog.findViewById(R.id.article_list_state_radio_group);
        final RadioButton radioAll = quickSettingDialog.findViewById(R.id.radio_all);
        final RadioButton radioUnread = quickSettingDialog.findViewById(R.id.radio_unread);
        final RadioButton radioStarred = quickSettingDialog.findViewById(R.id.radio_starred);
        SwitchButton nightThemeWifiSwitch = quickSettingDialog.findViewById(R.id.night_theme_switch);

        autoMarkWhenScrolling.setChecked(WithPref.i().isScrollMark());
        downImgOnWifiSwitch.setChecked(WithPref.i().isDownImgOnlyWifi());
        nightThemeWifiSwitch.setChecked(WithPref.i().getThemeMode() == App.Theme_Night);

        quickSettingDialog.show();
        moreSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quickSettingDialog.dismiss();
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_bottom, R.anim.fade_out);
//                startActivityForResult(intent, 0);
            }
        });
        autoMarkWhenScrolling.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                KLog.e("onClickedAutoMarkWhenScrolling图标被点击");
                WithPref.i().setScrollMark(b);
                autoMarkReaded = b;
            }
        });
        downImgOnWifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                WithPref.i().setDownImgWifi(b);
            }
        });
        nightThemeWifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                quickSettingDialog.dismiss();
                manualToggleTheme();
//                Tool.setWebViewsBGColor();
            }
        });
//        if (App.StreamState.equals(Api.ART_STARED)) {
//            radioStarred.setChecked(true);
//        } else if (App.StreamState.equals(Api.ART_UNREAD)) {
//            radioUnread.setChecked(true);

        if (App.StreamStatus == Api.STARED) {
            radioStarred.setChecked(true);
        } else if (App.StreamStatus == Api.UNREAD) {
            radioUnread.setChecked(true);
        } else {
            radioAll.setChecked(true);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == radioStarred.getId()) {
//                    App.StreamState = Api.ART_STARED;
                    App.StreamStatus = Api.STARED;
                    toolbar.setNavigationIcon(R.drawable.state_star);
                } else if (i == radioUnread.getId()) {
//                    App.StreamState = Api.ART_UNREAD;
                    App.StreamStatus = Api.UNREAD;
                    toolbar.setNavigationIcon(R.drawable.state_unread);
                } else {
//                    App.StreamState = Api.ART_ALL;
                    App.StreamStatus = Api.ALL;
                    toolbar.setNavigationIcon(R.drawable.state_all);
                }
                WithPref.i().setStreamStatus(App.StreamStatus);
                refreshData();
                quickSettingDialog.dismiss();
            }
        });
    }


    @OnClick(R.id.main_toolbar)
    public void clickToolbar(View view) {
//        if(BuildConfig.DEBUG){
//            Intent intent = new Intent(this,TestActivity.class);
//            startActivity(intent);
//            return;
//        }
        if (maHandler.hasMessages(Api.MSG_DOUBLE_TAP)) {
            maHandler.removeMessages(Api.MSG_DOUBLE_TAP);
            articleListView.smoothScrollToPosition(0);
        } else {
            maHandler.sendEmptyMessageDelayed(Api.MSG_DOUBLE_TAP, ViewConfiguration.getDoubleTapTimeout());
        }
    }



    /**
     * 监听返回键，弹出提示退出对话框
     */
    @Override
    public boolean onKeyDown(int keyCode , KeyEvent event) {
        // 后者为短期内按下的次数
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            quitDialog();
            //返回真表示返回键被屏蔽掉
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void quitDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.main_dialog_esc_confirm)
                .setPositiveButton(R.string.main_dialog_esc_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                })
                .setNegativeButton(R.string.main_dialog_esc_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void initToolbar() {
        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setHomeButtonEnabled(true);
        // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

//        if (App.StreamState.equals(Api.ART_ALL)) {
//            toolbar.setNavigationIcon(R.drawable.state_all);
//        } else if (App.StreamState.equals(Api.ART_STARED)) {
        if (App.StreamStatus == Api.ALL) {
            toolbar.setNavigationIcon(R.drawable.state_all);
        } else if (App.StreamStatus == Api.STARED) {
            toolbar.setNavigationIcon(R.drawable.state_star);
        } else {
            toolbar.setNavigationIcon(R.drawable.state_unread);
        }

        // setDisplayShowHomeEnabled(true)   //使左上角图标是否显示，如果设成false，则没有程序图标，仅仅就个标题，否则，显示应用程序图标，对应id为android.R.id.home，对应ActionBar.DISPLAY_SHOW_HOME
        // setDisplayShowCustomEnabled(true)  // 使自定义的普通View能在title栏显示，即actionBar.setCustomView能起作用，对应ActionBar.DISPLAY_SHOW_CUSTOM
    }


    /**
     * 设置各个视图与颜色属性的关联
     */
    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        ViewGroupSetter artListViewSetter = new ViewGroupSetter(articleListView);
        // 绑定ListView的Item View中的news_title视图，在换肤时修改它的text_color属性
        artListViewSetter.childViewTextColor(R.id.main_slv_item_title, R.attr.lv_item_title_color);
        artListViewSetter.childViewTextColor(R.id.main_slv_item_summary, R.attr.lv_item_desc_color);
        artListViewSetter.childViewTextColor(R.id.main_slv_item_author, R.attr.lv_item_info_color);
        artListViewSetter.childViewTextColor(R.id.main_slv_item_time, R.attr.lv_item_info_color);
        artListViewSetter.childViewBgColor(R.id.main_slv_item_divider, R.attr.lv_item_divider);
        artListViewSetter.childViewBgColor(R.id.main_slv_item, R.attr.root_view_bg);
        artListViewSetter.childViewBgColor(R.id.main_list_item_surface, R.attr.root_view_bg);
        artListViewSetter.childViewBgColor(R.id.main_list_item_menu_left, R.attr.root_view_bg);
        artListViewSetter.childViewBgColor(R.id.main_list_item_menu_right, R.attr.root_view_bg);
        artListViewSetter.childViewBgColor(R.id.swipe_layout, R.attr.root_view_bg);

        ViewGroupSetter relative = new ViewGroupSetter(relativeLayout);
        relative.childViewBgColor(R.id.main_tag_close, R.attr.bottombar_bg);
        relative.childViewTextColor(R.id.main_tag_close, R.attr.bottombar_fg);
        relative.childViewBgColor(R.id.sheet_tag, R.attr.bottombar_bg);
        relative.childViewBgColor(R.id.main_tag_list_view, R.attr.bottombar_bg);

        // 绑定ListView的Item View中的news_title视图，在换肤时修改它的text_color属性
        ViewGroupSetter tagListViewSetter = new ViewGroupSetter(tagListView);
        tagListViewSetter.childViewBgColor(R.id.group_item, R.attr.bottombar_bg);  // 这个不生效，反而会影响底色修改
        tagListViewSetter.childViewTextColor(R.id.group_item_icon, R.attr.tag_slv_item_icon);
        tagListViewSetter.childViewTextColor(R.id.group_item_title, R.attr.lv_item_title_color);
        tagListViewSetter.childViewTextColor(R.id.group_item_count, R.attr.lv_item_desc_color);
        tagListViewSetter.childViewBgDrawable(R.id.group_item_count, R.attr.bubble_bg);

        tagListViewSetter.childViewBgColor(R.id.child_item, R.attr.bottombar_bg);  // 这个不生效，反而会影响底色修改
        tagListViewSetter.childViewTextColor(R.id.child_item_title, R.attr.lv_item_title_color);
        tagListViewSetter.childViewTextColor(R.id.child_item_count, R.attr.lv_item_desc_color);
        tagListViewSetter.childViewBgDrawable(R.id.child_item_count, R.attr.bubble_bg);

//        ViewGroupSetter headerHomeViewSetter = new ViewGroupSetter((ViewGroup) headerHomeView);
//        headerHomeViewSetter.childViewBgColor(R.id.header_home, R.attr.bottombar_bg);  // 这个不生效，反而会影响底色修改
//        headerHomeViewSetter.childViewTextColor(R.id.header_home_icon, R.attr.tag_slv_item_icon);
//        headerHomeViewSetter.childViewTextColor(R.id.header_home_title, R.attr.lv_item_title_color);
//        headerHomeViewSetter.childViewTextColor(R.id.header_home_count, R.attr.lv_item_desc_color);


        ViewGroupSetter headerPinnedViewSetter = new ViewGroupSetter((ViewGroup) headerPinnedView);
        headerPinnedViewSetter.childViewTextColor(R.id.header_item_icon, R.attr.tag_slv_item_icon);
        headerPinnedViewSetter.childViewTextColor(R.id.header_item_title, R.attr.lv_item_title_color);
        headerPinnedViewSetter.childViewTextColor(R.id.header_item_count, R.attr.lv_item_desc_color);
        headerPinnedViewSetter.childViewBgDrawable(R.id.header_item_count, R.attr.bubble_bg);
        headerPinnedViewSetter.childViewBgColor(R.id.header_item, R.attr.bottombar_bg);

        mColorfulBuilder
                // 这里做设置，实质都是直接生成了一个View（根据Activity的findViewById），并直接添加到 colorful 内的 mElements 中。
                .backgroundColor(R.id.main_swipe_refresh, R.attr.root_view_bg)
//                .backgroundColor(R.id.main_scroll_layout_bg, R.attr.bottombar_bg)
//                .textColor(R.id.main_scroll_layout_title, R.attr.bottombar_fg)
//                .backgroundColor(R.id.main_scrolllayout_divider, R.attr.bottombar_divider)


                .backgroundColor(R.id.main_tag_list_view, R.attr.bottombar_bg) // 这个不生效
//                .backgroundColor(R.id.main_tag_bg, R.attr.bottombar_bg) // 这个不生效

                .textColor(R.id.header_item_icon, R.attr.tag_slv_item_icon)
                .textColor(R.id.header_item_title, R.attr.lv_item_title_color)
                .textColor(R.id.header_item_count, R.attr.lv_item_desc_color)
                .backgroundColor(R.id.header_item, R.attr.bottombar_bg)

                // 设置 toolbar
                .backgroundColor(R.id.main_toolbar, R.attr.topbar_bg)
                .textColor(R.id.main_toolbar_hint, R.attr.topbar_fg)
//                .textColor(R.id.main_toolbar_readability, R.attr.topbar_fg)

                // 设置 bottombar
                .backgroundColor(R.id.main_bottombar, R.attr.bottombar_bg)
                // 设置中屏和底栏之间的分割线
                .backgroundColor(R.id.main_bottombar_divider, R.attr.bottombar_divider)
                .textColor(R.id.main_bottombar_search, R.attr.bottombar_fg)
//                .textColor(R.id.main_bottombar_articles_state, R.attr.bottombar_fg)
//                .textColor(R.id.main_bottombar_star, R.attr.bottombar_fg)
                .textColor(R.id.main_bottombar_setting, R.attr.bottombar_fg)
                .textColor(R.id.main_bottombar_tag, R.attr.bottombar_fg)
                .textColor(R.id.main_bottombar_refresh_articles, R.attr.bottombar_fg)

                // 设置 listview 背景色
                // 这里做设置，实质是将View（根据Activity的findViewById），并直接添加到 colorful 内的 mElements 中。
                .setter(relative)
                .setter(headerPinnedViewSetter)
//                .setter(headerHomeViewSetter)
                .setter(tagListViewSetter)
                .setter(artListViewSetter);
        return mColorfulBuilder;
    }
}
