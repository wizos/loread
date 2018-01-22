package me.wizos.loread.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.socks.library.KLog;
import com.yinglan.scrolllayout.ScrollLayout;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.adapter.ExpandableListAdapterS;
import me.wizos.loread.adapter.MainListViewAdapter;
import me.wizos.loread.adapter.MaterialSimpleListAdapter;
import me.wizos.loread.adapter.MaterialSimpleListItem;
import me.wizos.loread.bean.Article;
import me.wizos.loread.bean.Feed;
import me.wizos.loread.bean.Tag;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.net.Api;
import me.wizos.loread.net.DataApi;
import me.wizos.loread.net.InoApi;
import me.wizos.loread.utils.ScreenUtil;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.utils.ToastUtil;
import me.wizos.loread.view.ExpandableListViewS;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.ListViewS;
import me.wizos.loread.view.SwipeRefreshLayoutS;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.colorful.setter.ViewGroupSetter;
import me.wizos.loread.view.common.color.ColorChooserDialog;

public class MainActivity extends BaseActivity implements SwipeRefreshLayoutS.OnRefreshListener, ColorChooserDialog.ColorCallback, View.OnClickListener {
    protected static final String TAG = "MainActivity";
    private IconFontView vReadIcon, vStarIcon, vPlaceHolder;
    private TextView vToolbarHint;
    private Toolbar toolbar;
    private SwipeRefreshLayoutS swipeRefreshLayoutS;
    private ListViewS artListView;
    private MainListViewAdapter artListAdapter;
    private static int tagCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        initService();
        initToolbar();
        initIconView();
        initArtListView();
        initTagListView();
        initSwipeRefreshLayout(); // 必须要放在 initArtListView() 之后，不然无论 ListView 滚动到第几页，一下拉就会触发刷新
        initData();  // 获取文章列表数据为 App.articleList
        KLog.i("列表数目：" + App.articleList.size() + "  当前状态：" + App.StreamState);

        if (savedInstanceState != null) {
            final int position = savedInstanceState.getInt("listItemFirstVisiblePosition");
            slvSetSelection(position);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("listItemFirstVisiblePosition", artListView.getFirstVisiblePosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (artListAdapter != null) {
            artListAdapter.notifyDataSetChanged();
        }
        KLog.i("【onResume】" + App.StreamState + "---" + toolbar.getTitle() + "===" + App.StreamId);
    }

    protected void initIconView() {
        vReadIcon = (IconFontView) findViewById(R.id.main_bottombar_read);
        vStarIcon = (IconFontView)findViewById(R.id.main_bottombar_star);
//        vToolbarCount = (TextView)findViewById(R.id.main_toolbar_count);
        vToolbarHint = (TextView)findViewById(R.id.main_toolbar_hint);
        vPlaceHolder = (IconFontView) findViewById(R.id.main_placeholder);
        if (App.StreamState.equals(Api.ART_STARED)) {
            vStarIcon.setText(R.string.font_stared);
            vReadIcon.setText(R.string.font_readed);

        } else if (App.StreamState.equals(Api.ART_UNREAD)) {
            vStarIcon.setText(R.string.font_unstar);
            vReadIcon.setText(R.string.font_unread);
        } else {
            vStarIcon.setText(R.string.font_unstar);
            vReadIcon.setText(R.string.font_readed);
        }
        IconFontView iconReadability = (IconFontView) findViewById(R.id.main_toolbar_readability);
        iconReadability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualToggleTheme();
            }
        });

        IconFontView searchView = (IconFontView) findViewById(R.id.main_toolbar_search);
//        searchView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new MaterialDialog.Builder(MainActivity.this)
//                        .title(R.string.search)
//                        .inputType(InputType.TYPE_CLASS_TEXT)
//                        .inputRange(1, 22)
//                        .input(null, "", new MaterialDialog.InputCallback() {
//                            @Override
//                            public void onInput(MaterialDialog dialog, CharSequence input) {
//                                showSearchResult(input.toString());
//                            }
//                        })
//                        .positiveText(R.string.search)
//                        .negativeText(android.R.string.cancel)
//                        .show();
//            }
//        });
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivityForResult(intent, 0);
            }
        });

    }

    protected void initSwipeRefreshLayout() {
        swipeRefreshLayoutS = (SwipeRefreshLayoutS) findViewById(R.id.main_swipe_refresh);
        if (swipeRefreshLayoutS == null) return;
        swipeRefreshLayoutS.setOnRefreshListener(this);
        swipeRefreshLayoutS.setProgressViewOffset(true, 0, 120);//设置样式刷新显示的位置
        swipeRefreshLayoutS.setViewGroup(artListView);
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
        if (!swipeRefreshLayoutS.isEnabled()) {
            return;
        }
        startSyncService();
        KLog.i("【刷新中】");
    }

    // 按下back键时会调用onDestroy()销毁当前的activity，重新启动此activity时会调用onCreate()重建；
    // 而按下home键时会调用onStop()方法，并不销毁activity，重新启动时则是调用onResume()
    @Override
    protected void onDestroy() {
        // 如果参数为null的话，会将所有的Callbacks和Messages全部清除掉。
        // 这样做的好处是在Acticity退出的时候，可以避免内存泄露。因为 handler 内可能引用 Activity ，导致 Activity 退出后，内存泄漏。
        maHandler.removeCallbacksAndMessages(null);
        unbindService(connection);
        super.onDestroy();
    }


    private Intent intent;
    private void initService() {
        intent = new Intent(this, MainService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    // 先创建一个 ServiceConnection 匿名类，重写 onServiceConnected()、onServiceDisconnected()。这两个方法分别会在Activity与Service建立关联和解除关联的时候调用。
    // 在onServiceConnected()方法中，我们又通过向下转型得到了MyBinder的实例，有了这个实例，Activity和Service之间的关系就变得非常紧密了。
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            KLog.d("连接断开");
        }

        // onServiceConnected在绑定成功时进行回调，但不保证在执行binService后立马回调。
        // 我们在onCreate方法中绑定后立马获取service实例，但此时不保证onServiceConnected已经被回调。 也就是我们onCreate方法执行时onServiceConnected还没有别调用。此时当然mService还为空了。
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 通过Binder，实现Activity与Service通信
            MainService.ServiceBinder mBinderService = (MainService.ServiceBinder) service;
            MainService mainService = mBinderService.getService();
            mainService.regHandler(maHandler);//TODO:考虑内存泄露
            if (WithSet.i().isSyncFirstOpen() && !swipeRefreshLayoutS.isRefreshing()) {
                ToastUtil.showShort("开始同步");
                swipeRefreshLayoutS.setEnabled(false);
                startSyncService();
            }
            KLog.d("连接开始");
        }
    };

    private void startSyncService() {
        intent.setAction("sync");
        KLog.i("调用Service，开始同步");
        startService(intent);
    }


    // 这里不必和 ArticleActivity 一样写成静态内部类，用来防止因持有外部的Activity而造成内存泄漏。
    // 因为MainActivity基本只有一个实例，且不会反复创建、销毁，所以不用担心回收造成的内存泄漏问题。
    private Handler maHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String tips = msg.getData().getString("tips");
            KLog.i("【handler】" + msg.what + "---" + "---");
            switch (msg.what) {
                case Api.SYNC_START:
                    swipeRefreshLayoutS.setRefreshing(true);
                    swipeRefreshLayoutS.setEnabled(false);
                    KLog.e("开始同步");
                    break;
                case Api.SYNC_SUCCESS:
                    swipeRefreshLayoutS.setRefreshing(false);
                    swipeRefreshLayoutS.setEnabled(true);
                    initData();
                    break;
                case Api.SYNC_FAILURE: // 文章获取失败
                    swipeRefreshLayoutS.setRefreshing(false);
                    swipeRefreshLayoutS.setEnabled(true);
                    vToolbarHint.setText(String.valueOf(tagCount));
//                    ToastUtil.showShort("同步失败");
                    break;
                case Api.SYNC_PROCESS:
                    vToolbarHint.setText(tips);
                    break;
            }
            return false;
        }
    });


    /**
     * App.StreamState 包含 3 个状态：All，Unread，Stared
     * App.StreamId 至少包含 1 个状态： Reading-list
     * */
    protected void refreshData() { // 获取 App.articleList , 并且根据 App.articleList 的到未读数目
        KLog.e("refreshData：" + App.StreamId + "  " + App.StreamState + "   " + App.UserID);
        getArtData();
        getTagData();
        artListAdapter = new MainListViewAdapter(this, App.articleList, artListView);
        artListView.setAdapter(artListAdapter);
        tagListAdapter.notifyDataSetChanged();
        loadData();
    }

    private void initData() {
        getArtData();
        getTagData();
        artListAdapter = new MainListViewAdapter(this, App.articleList, artListView);
        artListView.setAdapter(artListAdapter);
        tagListAdapter = new ExpandableListAdapterS(this, App.tagList, tagListView);
        tagListView.setAdapter(tagListAdapter);
        loadData();
    }

    private void getArtData() {
        if (App.StreamId.startsWith("user/")) {
            if (App.StreamId.contains(Api.U_READING_LIST)) {
                if (App.StreamState.contains(Api.ART_STARED)) {
//                    App.articleList = WithDB.i().getArtsStared();
                    App.i().updateArtList(WithDB.i().getArtsStared());
                } else if (App.StreamState.contains(Api.ART_UNREAD)) {
//                    App.articleList = WithDB.i().getArtsUnread();
                    App.i().updateArtList(WithDB.i().getArtsUnread());
                } else {
//                    App.articleList = WithDB.i().getArtsAll();
                    App.i().updateArtList(WithDB.i().getArtsAll());
                }
            } else if (App.StreamId.contains(Api.U_NO_LABEL)) {
                if (App.StreamState.contains(Api.ART_STARED)) {
//                    App.articleList = WithDB.i().getArtsStaredNoTag();
                    App.i().updateArtList(WithDB.i().getArtsStaredNoTag());
                } else if (App.StreamState.contains(Api.ART_UNREAD)) {
//                    App.articleList = WithDB.i().getArtsUnreadNoTag();
                    App.i().updateArtList(WithDB.i().getArtsUnreadNoTag());
                } else {
//                    App.articleList = WithDB.i().getArtsAllNoTag();
                    App.i().updateArtList(WithDB.i().getArtsAllNoTag());
                }
            } else {
                // TEST:  测试
                Tag theTag = WithDB.i().getTag(App.StreamId);
                KLog.e("类：" + App.StreamId + App.StreamTitle + App.StreamState + theTag);
                if (App.StreamState.contains(Api.ART_STARED)) {
//                    App.articleList = WithDB.i().getArtsStaredInTag(theTag);
                    App.i().updateArtList(WithDB.i().getArtsStaredInTag(theTag));
                } else if (App.StreamState.contains(Api.ART_UNREAD)) {
//                    App.articleList = WithDB.i().getArtsUnreadInTag(theTag);
                    App.i().updateArtList(WithDB.i().getArtsUnreadInTag(theTag));
                } else {
//                    App.articleList = WithDB.i().getArtsAllInTag(theTag);
                    App.i().updateArtList(WithDB.i().getArtsAllInTag(theTag));
                }
            }
        } else if (App.StreamId.startsWith("feed/")) {
            if (App.StreamState.equals(Api.ART_STARED)) {
//                App.articleList = WithDB.i().getArtsStaredInFeed(App.StreamId);
                App.i().updateArtList(WithDB.i().getArtsStaredInFeed(App.StreamId));
            } else if (App.StreamState.contains(Api.ART_UNREAD)) {
//                App.articleList = WithDB.i().getArtsUnreadInFeed(App.StreamId); // TEST:  测试DB函数
                App.i().updateArtList(WithDB.i().getArtsUnreadInFeed(App.StreamId));
            } else {
//                App.articleList = WithDB.i().getArtsAllInFeed(App.StreamId); // 590-55
                App.i().updateArtList(WithDB.i().getArtsAllInFeed(App.StreamId));
            }
            KLog.i("【Api.getArts】" + App.StreamState + App.StreamId);
            App.StreamTitle = WithDB.i().getFeed(App.StreamId).getTitle();
        }
    }

    private void getTagData() {
        Tag rootTag = new Tag();
        Tag noLabelTag = new Tag();
        long userID = WithSet.i().getUseId();
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
        tagListTemp.addAll(WithDB.i().getTags());
        App.i().updateTagList(tagListTemp);

//        App.tagList = new ArrayList<>(tagListTemp.size()+2);
//        App.tagList.add(rootTag);
//        App.tagList.add(noLabelTag);
//        App.tagList.addAll(tagListTemp);
        KLog.d("【listTag】 " + rootTag.toString());
    }


    private void loadData() {
        KLog.i("【】" + App.StreamState + "--" + App.StreamTitle + "--" + App.StreamId + "--" + toolbar.getTitle() + App.articleList.size());
        if (StringUtil.isBlank(App.articleList)) {
            vPlaceHolder.setVisibility(View.VISIBLE);
            artListView.setVisibility(View.GONE);
        }else {
            vPlaceHolder.setVisibility(View.GONE);
            artListView.setVisibility(View.VISIBLE);
        }

        // 每次重新加载列表数据的时候，应该把 ArticleListAdapter 和 TagListAdapter 都更新一下。
//        adapter中的数据源集合或数组等必须是同一个数据源，也就是同一个对象。
//        当数据源发生变化的时候，我们会调用adaper的notifyDataSetChanged()方法。
//        当直接将从数据库或者其他方式获取的数据源集合或者数组直接赋值给当前数据源时，相当于当前数据源的对象发生了变化，当前对象已经不是adapter中的对象了，所以adaper调用notifyDataSetChanged()方法不会进行刷新数据和界面的操作。

        KLog.e("未触碰Group" + "=" + "=" + App.tagList.size());
        // 在setSupportActionBar(toolbar)之后调用toolbar.setTitle()的话。 在onCreate()中调用无效。在onStart()中调用无效。 在onResume()中调用有效。
        getSupportActionBar().setTitle(App.StreamTitle);
        tagCount = App.articleList.size();
        setToolbarHint(tagCount);
    }

    private void changeItemNum(int offset){
        tagCount = tagCount + offset;
        vToolbarHint.setText(String.valueOf( tagCount ));
    }

    private void setToolbarHint(int tagCount) {
        vToolbarHint.setText(String.valueOf( tagCount ));
    }


    private ScrollLayout mScrollLayout;
    private ExpandableListViewS tagListView;
    private ExpandableListAdapterS tagListAdapter;
    private View headerView;


    private ScrollLayout.OnScrollChangedListener mOnScrollChangedListener = new ScrollLayout.OnScrollChangedListener() {
        @Override
        public void onScrollProgressChanged(float currentProgress) {
            if (currentProgress >= 0) {
                float precent = 255 * currentProgress;
                if (precent > 255) {
                    precent = 255;
                } else if (precent < 0) {
                    precent = 0;
                }
                mScrollLayout.getBackground().setAlpha(255 - (int) precent);
            }
        }

        @Override
        public void onScrollFinished(ScrollLayout.Status currentStatus) {
        }

        @Override
        public void onChildScroll(int top) {
        }
    };


    public void initTagListView() {
        /**设置 setting*/
        mScrollLayout = (ScrollLayout) findViewById(R.id.scroll_down_layout);
//        mScrollLayout.setOnLog(new ScrollLayout.OnLog() {
//            @Override
//            public void e(String s) {
//                KLog.e(s);
//            }
//        });
        mScrollLayout.setMinOffset(0); // minOffset 关闭状态时最上方预留高度
        mScrollLayout.setMaxOffset((int) (ScreenUtil.getScreenHeight(this) * 0.6)); //打开状态时内容显示区域的高度
        mScrollLayout.setExitOffset(ScreenUtil.dip2px(this, 0)); //最低部退出状态时可看到的高度，0为不可见
        mScrollLayout.setIsSupportExit(true);
        mScrollLayout.setAllowHorizontalScroll(false);
        mScrollLayout.setToExit();
//        mScrollLayout.getBackground().setAlpha(0);
        mScrollLayout.setOnScrollChangedListener(mOnScrollChangedListener);


        IconFontView tagIcon = (IconFontView) findViewById(R.id.main_bottombar_tag);
        tagIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mScrollLayout.setToOpen();
                tagListAdapter.notifyDataSetChanged(); // 每次点击的时候更新一下 tagList
//                if (v.getHandler().hasMessages(3608)) {
//                    v.getHandler().removeMessages(3608);
//                    Intent intent = new Intent(MainActivity.this, TagActivity.class);
//                    intent.putExtra("ListState", App.StreamState);
//                    intent.putExtra("ListTag", App.StreamId);
//                    intent.putExtra("ListCount", App.articleList.size());
//                    startActivityForResult(intent, 0);
//                    overridePendingTransition(R.anim.in_from_bottom, R.anim.exit_anim );
//                } else {
//                    Runnable r = new Runnable() {
//                        @Override
//                        public void run() {
//                            mScrollLayout.setToOpen();
//                        }
//                    };
//                    Message m = Message.obtain(v.getHandler(), r); // obtain() 从全局池中返回一个新的Message实例。在大多数情况下这样可以避免分配新的对象。
//                    m.what = 3608;
//                    v.getHandler().sendMessageDelayed(m, 300);// ViewConfiguration.getDoubleTapTimeout()
//                    tagListAdapter.notifyDataSetChanged();
//                }
            }
        });

        tagListView = (ExpandableListViewS) findViewById(R.id.list_view);
        tagListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // 设置悬浮头部VIEW
        headerView = getLayoutInflater().inflate(R.layout.tag_expandable_item_group_header, tagListView, false);
        tagListView.setHeaderView(headerView);
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

        tagListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                mScrollLayout.setToExit();
                App.StreamId = App.tagList.get(groupPosition).getId().replace("\"", "");
                App.StreamTitle = App.tagList.get(groupPosition).getTitle();
//                if (App.StreamId == null || App.StreamId.equals("")) {
//                    App.StreamId = "user/" + App.UserID + "/state/com.google/reading-list";
//                }
                WithSet.i().setStreamId(App.StreamId);
                refreshData();
                KLog.i("【 TagList 被点击】" + App.StreamId + App.StreamState);
                return true;
            }
        });


        tagListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                KLog.e("子项被点击1：" + v + " - " + v.getTag() + "-" + groupPosition + "==" + childPosition + "=" + id);
                mScrollLayout.setToExit();
                Feed theFeed = App.tagList.get(groupPosition).getFeeds().get(childPosition);

                App.StreamId = theFeed.getId();
                App.StreamTitle = theFeed.getTitle();
                WithSet.i().setStreamId(App.StreamId);
                refreshData();

                KLog.e("【子项被点击2】" + App.StreamId + App.StreamState);
                return true;
            }
        });

        tagListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                KLog.e("被长安，view的id是" + view.getId() + "，parent的id" + parent.getId() + "，Tag是" + view.getTag() + "，位置是" + tagListView.getPositionForView(view));

                ExpandableListAdapterS.ItemViewHolder itemView = (ExpandableListAdapterS.ItemViewHolder) view.getTag();
                if (InoApi.isTag(itemView.id)) {
                    showTagDialog(WithDB.i().getTag(itemView.id));
                } else if (InoApi.isFeed(itemView.id)) {
                    showFeedDialog(itemView, WithDB.i().getFeed(itemView.id));
                }
                return true;
            }
        });
    }

    public void text(View view) {
        KLog.e("【】背景层是否被点击");
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
                        switch (which) {
                            case 0:
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
                                break;
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
                                        tagListAdapter.removeChild(itemView.groupPos, itemView.childPos);
                                        tagListAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onError(Response<String> response) {
                                        ToastUtil.showLong(getString(R.string.toast_unsubscribe_fail));
                                    }
                                });
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
                WithDB.i().updateArtsFeedTitle(feed); // 由于改了 feed 的名字，而每个 article 自带的 feed 名字也得改过来。
                tagListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Response<String> response) {
                ToastUtil.showLong(getString(R.string.toast_rename_fail));
            }
        });
    }


//    //定义一个startActivityForResult（）方法用到的整型值
//    public void onTagIconClicked(View view){
//        Intent intent = new Intent(MainActivity.this, TagActivity.class);
//        intent.putExtra("ListState", App.StreamState);
//        intent.putExtra("ListTag", App.StreamId);
//        intent.putExtra("ListCount", App.articleList.size());
////        intent.putExtra("NoTagCount", getNoTagList2().size());
//        startActivityForResult(intent, 0);
//        overridePendingTransition(R.anim.in_from_bottom, R.anim.exit_anim );
//    }


    public void initArtListView() {
//        initSlvMenu();
        artListView = (ListViewS) findViewById(R.id.main_slv);
//        artListView = (ListView) findViewById(R.id.main_slv);
//        artListView = (SlideAndDragListView) findViewById(R.id.main_slv);
        if (artListView == null) return;
//        artListView.setMenu(mMenu);
//        artListView.setOnListItemClickListener(new SlideAndDragListView.OnListItemClickListener() {
//            @Override
//            public void onListItemClick(View v, int position) {
////                KLog.e("【 TagList 被点击】");
//                if(position==-1){return;}
//                String articleID = App.articleList.get(position).getId();
//                Intent intent = new Intent(MainActivity.this , ArticleActivity.class);
//                intent.putExtra("articleID", articleID);
//                intent.putExtra("articleNo", position); // 下标从 0 开始
//                intent.putExtra("articleCount", App.articleList.size());
//                startActivityForResult(intent, 0);
////                KLog.i("点击了" + articleID + position + "-" + App.articleList.size());
//            }
//        });

//        artListView.setOnSlideListener(new SlideAndDragListView.OnSlideListener() {
//            @Override
//            public int onSlideOpen(View view, View parentView, int position, int direction) {
//                if (position == -1) {
//                    return Menu.ITEM_NOTHING;
//                }
//                Article article = App.articleList.get(position);
//                switch (direction) {
//                    case MenuItem.DIRECTION_LEFT:
//                        changeStarState(article);
//                        return Menu.ITEM_SCROLL_BACK;
//                    case MenuItem.DIRECTION_RIGHT:
//                        changeReadState(article);
//                        c = position;
//                        return Menu.ITEM_SCROLL_BACK;
//                }
//                return Menu.ITEM_NOTHING;
//            }
//
//            @Override
//            public void onSlideClose(View view, View parentView, int position, int direction) {
//            }
//        });
        artListView.setOnListItemLongClickListener(new ListViewS.OnListItemLongClickListener() {
            @Override
            public void onListItemLongClick(View view, final int position) {
                KLog.d("长按===");
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
                                ArrayList<Article> artList = new ArrayList<>();
                                int i = 0, num = 0;
                                switch (which) {
                                    case 0:
                                        i = 0;
                                        num = position + 1;
                                        artList = new ArrayList<>(position + 1);
                                        break;
                                    case 1:
                                        i = position;
                                        num = App.articleList.size();
                                        artList = new ArrayList<>(num - position - 1);
                                        break;
                                    case 2:
                                        Article article = App.articleList.get(position);
                                        DataApi.i().markArticleUnread(article.getId(), null);
                                        article.setReadState(Api.ART_UNREADING);
                                        WithDB.i().saveArticle(article);
                                        artListAdapter.notifyDataSetChanged();
                                        break;
                                }

                                for (int n = i; n < num; n++) {
                                    if (App.articleList.get(n).getReadState().equals(Api.ART_UNREAD)) {
                                        App.articleList.get(n).setReadState(Api.ART_READED);
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

        artListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
        artListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                KLog.d("长按===");
                final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter( MainActivity.this);
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
                                        DataApi.i().markArticleUnread(article.getId(), null);
                                        article.setReadState(Api.ART_UNREADING);
                                        WithDB.i().saveArticle(article);
                                        artListAdapter.notifyDataSetChanged();
                                        break;
                                }

                                for(int n = i; n< num; n++){
                                    if (App.articleList.get(n).getReadState().equals(Api.ART_UNREAD)) {
                                        App.articleList.get(n).setReadState(Api.ART_READED);
                                        artList.add(App.articleList.get(n));
                                    }
                                }
                                addReadedList(artList);
                                dialog.dismiss();
                            }
                        })
                        .show();
                return true;
            }
        });


        artListView.setOnAdapterSlideListenerProxy(new ListViewS.OnAdapterSlideListenerProxy() {
            @Override
            public void onUpdate(View view, int position, float offset) {
                // 推测由于该函数 getView 已经生成了 View 所以不在更新了。使用 notifyDataSetChanged(); 也不行
//                KLog.e("观察" + offset + "  " + lastOffset);
//                SearchListViewAdapter.CustomViewHolder itemViewHolder;
//                int firstVisiblePosition = artListView.getFirstVisiblePosition(); //屏幕内当前可以看见的第一条数据
//                if( position-firstVisiblePosition>=0){
//                    View itemView = artListView.getChildAt(position - firstVisiblePosition);
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
                changeStarState(article);
            }

            @Override
            public void onCloseRight(View view, int position, int direction) {
                KLog.e("onCloseRight：" + position + "  ");
                Article article = App.articleList.get(position);
                changeReadState(article);
            }

            @Override
            public void onClick(View view, int position) {
                KLog.e("onClick" + position);
                if (position == -1) {
                    return;
                }
                String articleID = App.articleList.get(position).getId();
                Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
                intent.putExtra("articleID", articleID);
                intent.putExtra("articleNo", position); // 下标从 0 开始
                intent.putExtra("articleCount", App.articleList.size());
                startActivityForResult(intent, 0);
//                KLog.i("点击了" + articleID + position + "-" + App.articleList.size());
            }

            @Override
            public void log(String layout) {
                KLog.e(layout);
            }
        });
    }


    private void showSearchResult(String keyword) {
//        List<Article> articles = WithDB.i().getSearchedArts( keyword );
//        App.i().updateArtList(WithDB.i().getSearchedArts(keyword));
        App.StreamId = Api.U_Search;
        App.StreamTitle = getString(R.string.main_toolbar_title_search) + keyword;
        App.articleList = WithDB.i().getSearchedArts(keyword);
        artListAdapter = new MainListViewAdapter(this, App.articleList, artListView);
        artListView.setAdapter(artListAdapter);
        loadData();
    }

    private void addReadedList(ArrayList<Article> artList){
        if(artList.size() == 0){return;}
        List<String> articleIDs = new ArrayList<>(artList.size());
        for (Article article : artList) {
            articleIDs.add(article.getId());
            article.setReadState(Api.ART_READED);
        }
        DataApi.i().markArticleListReaded(articleIDs, null);
        changeItemNum(-artList.size());

        WithDB.i().saveArticleList(artList);
        artListAdapter.notifyDataSetChanged();
    }

    private void changeReadState(final Article article) {
        if (article.getReadState().equals(Api.ART_READED)) {
            DataApi.i().markArticleUnread(article.getId(), null);
            article.setReadState(Api.ART_UNREADING);
            changeItemNum( + 1 );
        }else {
            DataApi.i().markArticleReaded(article.getId(), null);
            article.setReadState(Api.ART_READED);
            changeItemNum( - 1 );
        }
        WithDB.i().saveArticle(article);
        artListAdapter.notifyDataSetChanged();
    }


    private void changeStarState(final Article article) {
        if (article.getStarState().equals(Api.ART_STARED)) {
            article.setStarState(Api.ART_UNSTAR);
            DataApi.i().markArticleUnstar(article.getId(), null);
        }else {
            article.setStarState(Api.ART_STARED);
            DataApi.i().markArticleStared(article.getId(), null);
            article.setStarred(System.currentTimeMillis() / 1000);
        }
        WithDB.i().saveArticle(article);
        artListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_toolbar:
                if (maHandler.hasMessages(Api.MSG_DOUBLE_TAP)) {
                    maHandler.removeMessages(Api.MSG_DOUBLE_TAP);
                    artListView.smoothScrollToPosition(0);
                } else {
                    maHandler.sendEmptyMessageDelayed(Api.MSG_DOUBLE_TAP, ViewConfiguration.getDoubleTapTimeout());
                }
                break;
        }
    }



    @Override
    protected void onActivityResult(int requestCode , int resultCode , Intent intent){
//        KLog.e("------------------------------------------" + resultCode + requestCode);
        switch (resultCode){
            // 这一段应该用不到了
            case Api.ActivityResult_TagToMain:
                refreshData(); // TagToMain
                break;
            case Api.ActivityResult_ArtToMain:
                int articleNo = intent.getExtras().getInt("articleNo"); // 在文章页的时候读到了第几篇文章，好让列表也自动将该项置顶
                if (articleNo > artListView.getLastVisiblePosition()) {
                    slvSetSelection(articleNo);
                }
                KLog.e("【onActivityResult】" + articleNo + "  " + artListView.getLastVisiblePosition());
                break;
            case Api.ActivityResult_SearchLocalArtsToMain:
                KLog.e("被搜索的词是" + intent.getExtras().getString("searchWord"));
                showSearchResult(intent.getExtras().getString("searchWord"));
                break;
        }
    }

    // 滚动到指定位置
    private void slvSetSelection(final int position) {
        artListView.post(new Runnable() {
            @Override
            public void run() {
                artListView.setSelection(position); // 不能直接用这个，无法滚动
            }
        });
    }

    /**
     * 设置各个视图与颜色属性的关联
     */
    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        ViewGroupSetter artListViewSetter = new ViewGroupSetter(artListView);
        // 绑定ListView的Item View中的news_title视图，在换肤时修改它的text_color属性
        artListViewSetter.childViewTextColor(R.id.main_slv_item_title, R.attr.lv_item_title_color);
        artListViewSetter.childViewTextColor(R.id.main_slv_item_summary, R.attr.lv_item_desc_color);
        artListViewSetter.childViewTextColor(R.id.main_slv_item_author, R.attr.lv_item_info_color);
        artListViewSetter.childViewTextColor(R.id.main_slv_item_time, R.attr.lv_item_info_color);
        artListViewSetter.childViewBgColor(R.id.main_slv_item_divider, R.attr.lv_item_divider);
        artListViewSetter.childViewBgColor(R.id.main_slv_item, R.attr.root_view_bg);
        artListViewSetter.childViewBgColor(R.id.main_list_item_surface, R.attr.root_view_bg);

        ViewGroupSetter tagListViewSetter = new ViewGroupSetter(tagListView);
        // 绑定ListView的Item View中的news_title视图，在换肤时修改它的text_color属性
//        tagListViewSetter.childViewBgColor(R.id.group_item, R.attr.bottombar_bg);  // 这个不生效，反而会影响底色修改
        tagListViewSetter.childViewTextColor(R.id.group_item_icon, R.attr.tag_slv_item_icon);
        tagListViewSetter.childViewTextColor(R.id.group_item_title, R.attr.lv_item_title_color);
        tagListViewSetter.childViewTextColor(R.id.group_item_count, R.attr.lv_item_desc_color);

//        tagListViewSetter.childViewBgColor(R.id.child_item, R.attr.bottombar_bg);  // 这个不生效，反而会影响底色修改
        tagListViewSetter.childViewTextColor(R.id.child_item_title, R.attr.lv_item_title_color);
        tagListViewSetter.childViewTextColor(R.id.child_item_count, R.attr.lv_item_desc_color);

        ViewGroupSetter headerViewSetter = new ViewGroupSetter((ViewGroup) headerView);
        headerViewSetter.childViewTextColor(R.id.header_item_icon, R.attr.tag_slv_item_icon);
        headerViewSetter.childViewTextColor(R.id.header_item_title, R.attr.lv_item_title_color);
        headerViewSetter.childViewTextColor(R.id.header_item_count, R.attr.lv_item_desc_color);
        headerViewSetter.childViewBgColor(R.id.tag_group_header, R.attr.bottombar_bg);

        mColorfulBuilder
                // 设置view的背景图片
                .backgroundColor(R.id.main_swipe_refresh, R.attr.root_view_bg) // 这里做设置，实质都是直接生成了一个View（根据Activity的findViewById），并直接添加到 colorful 内的 mElements 中。

                .backgroundColor(R.id.main_scroll_layout_bg, R.attr.bottombar_bg)
                .textColor(R.id.main_scroll_layout_title, R.attr.bottombar_fg)
                .backgroundColor(R.id.main_scrolllayout_divider, R.attr.bottombar_divider)

                .textColor(R.id.header_item_icon, R.attr.tag_slv_item_icon)
                .textColor(R.id.header_item_title, R.attr.lv_item_title_color)
                .textColor(R.id.header_item_count, R.attr.lv_item_desc_color)
                .backgroundColor(R.id.tag_group_header, R.attr.bottombar_bg)

                // 设置 toolbar
                .backgroundColor(R.id.main_toolbar, R.attr.topbar_bg)
                .textColor(R.id.main_toolbar_hint, R.attr.topbar_fg)
//                .textColor(R.id.main_toolbar_readability, R.attr.topbar_fg)

                // 设置 bottombar
                .backgroundColor(R.id.main_bottombar, R.attr.bottombar_bg)
                .backgroundColor(R.id.main_bottombar_divider, R.attr.bottombar_divider)// 设置中屏和底栏之间的分割线
                .textColor(R.id.main_bottombar_read, R.attr.bottombar_fg)
                .textColor(R.id.main_bottombar_star, R.attr.bottombar_fg)
                .textColor(R.id.main_bottombar_setting, R.attr.bottombar_fg)
                .textColor(R.id.main_bottombar_tag, R.attr.bottombar_fg)

                // 设置 listview 背景色
                .setter(headerViewSetter) // 这里做设置，实质是将View（根据Activity的findViewById），并直接添加到 colorful 内的 mElements 中。
                .setter(tagListViewSetter)
                .setter(artListViewSetter);
        return mColorfulBuilder;
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int color) {
        KLog.e("被选择的颜色：" + color);
        manualToggleTheme();
    }
//    public void showSelectThemeDialog() {
////        selectTheme = ScreenUtil.resolveColor( this,R.attr.colorPrimary, 0);
//        new ColorChooserDialog.Builder(this, R.string.readability_dialog_title)
////                .titleSub(R.string.md_custom_tag)
//                .customColors(R.array.theme_colors, null)
//                .preselect(0) // 预先选择
//                .show();
//        KLog.d("主题选择对话框");
//    }

    public void onSettingIconClicked(View view){
        Intent intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, 0);
    }

    private View settingview;

    public void onSettingIconClickedForDialog() {
//        if(settingview==null){
//            settingview = (View)findViewById(R.id.main_settingview);
//        }
//        if(settingview.getVisibility()==View.GONE){
//            settingview.setVisibility(View.VISIBLE);
//        }else {
//            settingview.setVisibility(View.GONE);
//        }
    }

    public void OnIconClickedAutoSync(View view) {
        if (!swipeRefreshLayoutS.isEnabled()) {
            return;
        }
        startSyncService();
        KLog.e("OnIconClickedAutoSync图标被点击");
        settingview.setVisibility(View.GONE);
    }

    public void OnIconClickedAutoTheme(View view) {
        manualToggleTheme();
        view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        KLog.e("OnIconClickedAutoTheme图标被点击");
        settingview.setVisibility(View.GONE);
    }

    public void OnIconClickedAutoImg(View view) {
        KLog.e("OnIconClickedAutoImg图标被点击");
        if (WithSet.i().isDownImgWifi()) {
            WithSet.i().setDownImgWifi(false);
        } else {
            WithSet.i().setDownImgWifi(true);
        }
        settingview.setVisibility(View.GONE);
    }

    public void OnIconClickedMore(View view) {
        KLog.e("OnIconClickedMore图标被点击");
        Intent intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, 0);
        settingview.setVisibility(View.GONE);
    }

    public void onStarIconClicked(View view){
        KLog.d(App.StreamId + App.StreamState + App.StreamTitle);
        KLog.d("收藏列表" + App.StreamId + App.StreamState + App.StreamTitle);
        if (App.StreamState.equals(Api.ART_STARED)) {
            ToastUtil.showShort("已经在收藏列表了");
        }else {
            vStarIcon.setText(R.string.font_stared);
            vReadIcon.setText(R.string.font_readed);

            App.StreamState = Api.ART_STARED;
            WithSet.i().setStreamState(App.StreamState);
            refreshData(); // 点击StarIcon时
        }
    }
    public void onReadIconClicked(View view){
        vStarIcon.setText(R.string.font_unstar);
        if (App.StreamState.equals(Api.ART_UNREAD)) {
            vReadIcon.setText(R.string.font_readed);
            App.StreamState = Api.ART_ALL;
        }else {
            vReadIcon.setText(R.string.font_unread);
            App.StreamState = Api.ART_UNREAD;
        }
        WithSet.i().setStreamState(App.StreamState);
        refreshData();// 点击ReadIcon时
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
//            quitDialog();// 创建弹出的Dialog
            return true;//返回真表示返回键被屏蔽掉
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
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setDisplayHomeAsUpEnabled(false); // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setOnClickListener(this);
        // setDisplayShowHomeEnabled(true)   //使左上角图标是否显示，如果设成false，则没有程序图标，仅仅就个标题，否则，显示应用程序图标，对应id为android.R.id.home，对应ActionBar.DISPLAY_SHOW_HOME
        // setDisplayShowCustomEnabled(true)  // 使自定义的普通View能在title栏显示，即actionBar.setCustomView能起作用，对应ActionBar.DISPLAY_SHOW_CUSTOM
    }


//    public void initSlvMenu() {
////        menuResId = new ArrayMap<>();
////        menuResId.put()
//        mMenu = new Menu(new ColorDrawable(Color.WHITE), true, 0);//第2个参数表示滑动item是否能滑的过量(true表示过量，就像Gif中显示的那样；false表示不过量，就像QQ中的那样)
//        mMenu.addItem(new MenuItem.Builder().setWidth(ScreenUtil.get2Px(this, R.dimen.slv_menu_left_width))
//                .setBackground(new ColorDrawable(getResources().getColor(R.color.white)))
//                .setIcon(getResources().getDrawable(R.drawable.slv_menu_ic_star, null)) // 插入图片
////                .setTextSize((int) getResources().getDimension(R.dimen.txt_size))
////                .setTextColor(ScreenUtil.getColor(R.color.crimson))
//                .setText(getString(R.string.font_stared))
//                .build());
//        res = View.generateViewId();
//
//        mMenu.addItem(new MenuItem.Builder().setWidth(ScreenUtil.get2Px(this, R.dimen.slv_menu_right_width))
//                .setDirection(MenuItem.DIRECTION_RIGHT) // 设置是左或右
//                .setBackground(new ColorDrawable(getResources().getColor(R.color.white)))
//                .setIcon(getResources().getDrawable(R.drawable.slv_menu_ic_adjust, null))
////                .setTextColor(R.color.white)
////                .setTextSize(ScreenUtil.getDimen(this, R.dimen.txt_size))
//                .setText("已读")
//                .build());
//    }




}
