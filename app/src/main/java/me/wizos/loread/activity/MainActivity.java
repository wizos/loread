package me.wizos.loread.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.cretin.www.cretinautoupdatelibrary.interfaces.AppDownloadListener;
import com.cretin.www.cretinautoupdatelibrary.utils.AppUpdateUtils;
import com.elvishew.xlog.XLog;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.ToastUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.kyleduo.switchbutton.SwitchButton;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.enums.PopupAnimation;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.umeng.analytics.MobclickAgent;
import com.yanzhenjie.recyclerview.OnItemClickListener;
import com.yanzhenjie.recyclerview.OnItemLongClickListener;
import com.yanzhenjie.recyclerview.OnItemSwipeListener;
import com.yanzhenjie.recyclerview.SwipeMenu;
import com.yanzhenjie.recyclerview.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.SwipeMenuItem;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.activity.viewmodel.ArticleListViewModel;
import me.wizos.loread.activity.viewmodel.CategoryViewModel;
import me.wizos.loread.adapter.ArticlePagedListAdapter;
import me.wizos.loread.adapter.StreamsAdapter;
import me.wizos.loread.bean.FeedEntries;
import me.wizos.loread.bean.collectiontree.Collection;
import me.wizos.loread.bean.collectiontree.CollectionTree;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.CorePref;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.FeedCategory;
import me.wizos.loread.db.User;
import me.wizos.loread.network.SyncWorker;
import me.wizos.loread.network.api.LocalApi;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.network.proxy.ProxyNodeSocks5;
import me.wizos.loread.utils.EncryptUtils;
import me.wizos.loread.utils.FeedParserUtils;
import me.wizos.loread.utils.HttpCall;
import me.wizos.loread.utils.SnackbarUtils;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.utils.UriUtils;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.SwipeRefreshLayoutS;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.colorful.setter.ViewGroupSetter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


/**
 * @author Wizos on 2016‎年5‎月23‎日
 */
public class MainActivity extends BaseActivity implements SwipeRefreshLayoutS.OnRefreshListener {
    private static final String TAG = "MainActivity";
    private ImageView vToolbarAutoMark;
    private Toolbar toolbar;
    private SwipeRefreshLayoutS swipeRefreshLayoutS;
    private SwipeRecyclerView articleListView;
    private ArticlePagedListAdapter articlesAdapter;
    private IconFontView refreshIcon;

    // 方案1
    private SwipeRecyclerView categoryListView;
    // private CategoriesAdapter categoryListAdapter;
    private StreamsAdapter categoryListAdapter;
    private TextView createCategoryButton;

    // 方案2
    // private ExpandableRecyclerView tagListView;
    // private GroupedListAdapter tagListAdapter;

    // 方案3
    // private SwipeRecyclerView tagListView;
    // private GroupAdapter tagListAdapter;


    private TextView countTips;
    private Integer[] scrollIndex;
    private int scrollPositionEnd;
    private View articlesHeaderView;

    private BottomSheetDialog quickSettingDialog;
    private BottomSheetDialog tagBottomSheetDialog;
    private RelativeLayout tagBottomSheetLayout;
    private boolean autoMarkReaded = false;
    private static Handler maHandler = new Handler();

    private BasePopupView loadingPopupView;
    private ArticleListViewModel articleListViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initToolbar();
        initIconView();
        initArtListView();
        initTagListView();
        initSwipeRefreshLayout(); // 必须要放在 initArtListView() 之后，不然无论 ListView 滚动到第几页，一下拉就会触发刷新
        // showAutoSwitchThemeSnackBar();
        applyPermissions();
        super.onCreate(savedInstanceState);// 由于使用了自动换主题，所以要放在这里
        checkProxy();

        refreshArticlesData();  // 获取文章列表数据为 App.articleList
        initCategoriesData();
        autoMarkReaded = App.i().getUser().isMarkReadOnScroll();
        initWorkRequest();
        checkUpdate();
    }

    private void checkProxy(){
        if(CorePref.i().globalPref().getBoolean(Contract.ENABLE_PROXY,false)){
            String json = CorePref.i().globalPref().getString(Contract.SOCKS5_PROXY,"");
            if(!TextUtils.isEmpty(json)){
                App.i().proxyNodeSocks5 = new Gson().fromJson(json, ProxyNodeSocks5.class);
            }
        }
    }


    private void initWorkRequest(){
        WorkManager.getInstance(this).cancelAllWorkByTag(SyncWorker.TAG);

        Constraints.Builder builder = new Constraints.Builder();
        builder.setRequiredNetworkType(NetworkType.CONNECTED);
        builder.setRequiresStorageNotLow(true);

        Data inputData = new Data.Builder().putBoolean(SyncWorker.IS_AUTO_SYNC, true).build();
        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                .setInputData(inputData)
                .setConstraints(builder.build())
                .addTag(SyncWorker.TAG)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(SyncWorker.TAG, ExistingPeriodicWorkPolicy.KEEP, syncRequest);
        XLog.d("SyncWorker Id: " + syncRequest.getId());
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(syncRequest.getId())
                .observe(this, workInfo -> {
                    XLog.d("周期任务 workInfos 数量：" );
                        switch (workInfo.getState()){
                            case FAILED:
                                XLog.d("周期任务：FAILED");
                                break;
                            case RUNNING:
                                XLog.d("周期任务：RUNNING");
                                break;
                            case BLOCKED:
                                XLog.d("周期任务：BLOCKED");
                                break;
                            case CANCELLED:
                                XLog.d("周期任务：CANCELLED");
                                break;
                            case ENQUEUED:
                                XLog.d("周期任务：ENQUEUED");
                                break;
                            case SUCCEEDED:
                                XLog.d("周期任务：SUCCEEDED");
                                break;
                        }
                        Data progress = workInfo.getProgress();
                        String value = progress.getString(SyncWorker.TAG);
                        XLog.d("周期任务进度：" + value);
                        // Do something with progress
                });

        LiveEventBus.get(SyncWorker.SYNC_TASK_START, Boolean.class)
                .observeSticky(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean startSyncTask) {
                        if (!swipeRefreshLayoutS.isEnabled() || !startSyncTask) {
                            return;
                        }
                        XLog.d("同步中");
                        Constraints.Builder builder = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED);
                        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                                .setConstraints(builder.build())
                                .addTag(SyncWorker.TAG)
                                .build();
                        WorkManager.getInstance(MainActivity.this).enqueue(oneTimeWorkRequest);
                    }
                });
        LiveEventBus.get(SyncWorker.SYNC_TASK_STATUS,Boolean.class)
                .observeSticky(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean isSyncing) {
                        XLog.d("任务状态："  + isSyncing );
                        swipeRefreshLayoutS.setRefreshing(false);
                        if(isSyncing){
                            swipeRefreshLayoutS.setEnabled(false);
                        }else {
                            swipeRefreshLayoutS.setEnabled(true);
                        }
                    }
                });
        LiveEventBus
                .get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE, String.class)
                .observe(this, new Observer<String>() {
                    @Override
                    public void onChanged(@Nullable String tips) {
                        if(tips == null){
                            tips = "";
                        }
                        toolbar.setSubtitle( tips );
                    }
                });
        LiveEventBus.get(SyncWorker.NEW_ARTICLE_NUMBER,Integer.class)
                .observe(this, new Observer<Integer>() {
                    @Override
                    public void onChanged(Integer integer) {
                        if(integer == 0){
                            return;
                        }
                        SnackbarUtils.Long(articleListView, bottomBar, getResources().getQuantityString(R.plurals.has_new_articles,integer,integer) )
                                .setAction(getString(R.string.view), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        refreshArticlesData();
                                    }
                                }).show();
                        refreshIcon.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void checkUpdate(){
        if(!CorePref.i().globalPref().getBoolean(Contract.ENABLE_CHECK_UPDATE,true)){
            return;
        }
        AppUpdateUtils.getInstance()
                .addAppDownloadListener(new AppDownloadListener() {
                    @Override
                    public void downloading(int progress) {
                    }

                    @Override
                    public void downloadFail(String msg) {
                    }

                    @Override
                    public void downloadComplete(String path) {
                        File apkFile = new File(path);
                        if (!apkFile.exists()) {
                            ToastUtils.show(R.string.the_apk_does_not_exist);
                            return;
                        }

                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        if (apkFile.getName().endsWith(".apk")) {
                            try {
                                //兼容7.0
                                Uri uri;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 适配Android 7系统版本
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
                                    uri = FileProvider.getUriForFile(App.i(), App.i().getPackageName() + ".fileprovider", apkFile);//通过FileProvider创建一个content类型的Uri
                                } else {
                                    uri = Uri.fromFile(apkFile);
                                }
                                intent.setDataAndType(uri, "application/vnd.android.package-archive"); // 对应apk类型
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            ToastUtils.show(R.string.is_not_an_apk_file);
                        }
                        //弹出安装界面
                        MainActivity.this.startActivity(intent);
                    }

                    @Override
                    public void downloadStart() {
                    }

                    @Override
                    public void reDownload() {
                    }

                    @Override
                    public void pause() {
                    }
                });
    }

    private void applyPermissions() {
        XXPermissions.with(this)
                //.constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                //.permission(Permission.SYSTEM_ALERT_WINDOW) //支持请求6.0悬浮窗权限8.0请求安装权限
                .permission(Permission.Group.STORAGE) //不指定权限则自动获取清单中的危险权限
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        for (String id : permissions) {
                            XLog.w("无法获取权限：" + id);
                        }
                        ToastUtils.show(getString(R.string.plz_grant_permission_tips));
                    }
                });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (articlesAdapter != null) {
            articlesAdapter.notifyDataSetChanged();
        }
    }

    // private void showAutoSwitchThemeSnackBar() {
    //     if (!App.i().getUser().isAutoToggleTheme()) {
    //         return;
    //     }
    //     int hour = TimeUtils.getCurrentHour();
    //     int themeMode;
    //     if (hour >= 7 && hour < 20) {
    //         themeMode = App.THEME_DAY;
    //     } else {
    //         themeMode = App.THEME_NIGHT;
    //     }
    //     if (App.i().getUser().getThemeMode() == themeMode) {
    //         return;
    //     }
    //
    //     SnackbarUtils.Long(articleListView, bottomBar, getString(R.string.theme_switched_automatically))
    //             .setAction(getString(R.string.cancel), new View.OnClickListener() {
    //                 @Override
    //                 public void onClick(View v) {
    //                     manualToggleTheme();
    //                 }
    //             }).show();
    // }


    protected void initIconView() {
        vToolbarAutoMark = findViewById(R.id.main_toolbar_auto_mark);
        if (App.i().getUser().isMarkReadOnScroll()) {
            vToolbarAutoMark.setVisibility(View.VISIBLE);
        }
        //vPlaceHolder = findViewById(R.id.main_placeholder);
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
        XLog.i("下拉刷新");
        Data inputData = new Data.Builder().putBoolean(SyncWorker.IS_AUTO_SYNC, false).build();
        Constraints.Builder builder = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED);
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(SyncWorker.class)
                .setInputData(inputData)
                .setConstraints(builder.build())
                .addTag(SyncWorker.TAG)
                .build();
        // WorkManager.getInstance(this).enqueueUniqueWork(SyncWorker.TAG, ExistingWorkPolicy.KEEP, oneTimeWorkRequest);
        XLog.i("单期SyncWorker Id: " + oneTimeWorkRequest.getId());
        WorkManager.getInstance(this).enqueue(oneTimeWorkRequest);
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(oneTimeWorkRequest.getId())
                .observe(this, workInfo -> {
                    XLog.i("单次任务" );
                    switch (workInfo.getState()){
                        case FAILED:
                            XLog.i("单次任务：FAILED");
                            break;
                        case RUNNING:
                            XLog.i("单次任务：RUNNING");
                            break;
                        case BLOCKED:
                            XLog.i("单次任务：BLOCKED");
                            break;
                        case CANCELLED:
                            XLog.i("单次任务：CANCELLED");
                            break;
                        case ENQUEUED:
                            XLog.i("单次任务：ENQUEUED");
                            break;
                        case SUCCEEDED:
                            XLog.i("单次任务：SUCCEEDED");
                            break;
                    }
                    Data progress = workInfo.getProgress();
                    String value = progress.getString(SyncWorker.TAG);
                    XLog.i("周期任务进度：" + value);
                });
    }

    // 按下back键时会调用onDestroy()销毁当前的activity，重新启动此activity时会调用onCreate()重建；
    // 而按下home键时会调用onStop()方法，并不销毁activity，重新启动时则是调用onResume()
    @Override
    protected void onDestroy() {
        // 参数为null，会将所有的Callbacks和Messages全部清除掉。
        // 这样做的好处是在Activity退出的时候，可以避免内存泄露。因为 handler 内可能引用 Activity
        maHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /**
     * App.StreamState 包含 3 个状态：All，Unread，Stared
     * App.streamId 至少包含 1 个状态： Reading-list
     */
    protected void refreshArticlesData() { // 获取 App.articleList , 并且根据 App.articleList 的到未读数目
        loadArticlesData();
        refreshIcon.setVisibility(View.GONE);
    }




    private void loadArticlesData() {
        openLoadingPopupView();
        String uid = App.i().getUser().getId();
        int streamStatus = App.i().getUser().getStreamStatus();
        int streamType = App.i().getUser().getStreamType();
        String streamId = App.i().getUser().getStreamId();

        articleListViewModel.loadArticles(uid, streamId, streamType, streamStatus, this,
                articles -> {
                    if( articlesAdapter.getCurrentList() != null ){
                        XLog.d("获得文章列表，LoadedCount = " + articlesAdapter.getCurrentList().getLoadedCount() +  "，LastKey = " + articlesAdapter.getCurrentList().getLastKey()  + "，LastVisibleItem = " + (linearLayoutManager.findLastVisibleItemPosition()-1) );
                    }else {
                        XLog.d("获得文章列表，CurrentList 为 null");
                    }
                    // XLog.i("更新列表数据 c");
                    renderViewByArticlesData(App.i().getUser().getStreamTitle(), articles.size() );
                    articlesAdapter.submitList(articles);
                    dismissLoadingPopupView();
                },
                articleIds -> {
                    // XLog.d("获得文章Ids");
                    articlesAdapter.setArticleIds(articleIds);
                    App.i().setArticleIds(articleIds);
                });

        articleListView.scrollToPosition(0);
        articlesAdapter.setLastPos(0);
        XLog.d("加载文章数据" );
    }

    private void loadSearchedArticles(String keyword) {
        MobclickAgent.onEvent(this, "click_button_search_articles", keyword);
        openLoadingPopupView();
        articleListViewModel.loadArticles(App.i().getUser().getId(), keyword, this,
                articles -> {
                    renderViewByArticlesData( getString(R.string.title_search,keyword), articles.size() );
                    articlesAdapter.submitList(articles);
                    dismissLoadingPopupView();
                },
                articleIds -> {
                    articlesAdapter.setArticleIds(articleIds);
                    App.i().setArticleIds(articleIds);
                });
        articleListView.scrollToPosition(0);
        articlesAdapter.setLastPos(0);
    }

    private void renderViewByArticlesData(String toolBarTitle, int articleSize) {
        // String title = toolBarTitle;
        // if(App.i().getUser().getStreamType() == App.STATUS_STARED && !App.i().getUser().getStreamId().contains(App.STREAM_UNSUBSCRIBED)){
        //     title = getString(R.string.all);
        // }
        // XLog.w("更新列表数据 R");
        // 在setSupportActionBar(toolbar)之后调用toolbar.setTitle()的话。 在onCreate()中调用无效。在onStart()中调用无效。 在onResume()中调用有效。
        getSupportActionBar().setTitle(toolBarTitle);
        countTips.setText( getResources().getQuantityString(R.plurals.articles_count, articleSize, articleSize) );
    }

    // public void showCategoryMenuDialog1(final StreamTree category) {
    //     // 重命名弹窗的适配器
    //     MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(new MaterialSimpleListAdapter.Callback() {
    //         @Override
    //         public void onMaterialListItemSelected(MaterialDialog dialog, int index, MaterialSimpleListItem item) {
    //             if(index == 0){
    //                 new MaterialDialog.Builder(MainActivity.this)
    //                         .title(R.string.delete)
    //                         .positiveText(R.string.confirm)
    //                         .negativeText(android.R.string.cancel)
    //                         .onPositive(new MaterialDialog.SingleButtonCallback() {
    //                             @Override
    //                             public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
    //                                 App.i().getApi().deleteCategory(category.getStreamId(), new CallbackX() {
    //                                     @Override
    //                                     public void onSuccess(Object result) {
    //                                         ToastUtils.show((String)result);
    //                                     }
    //
    //                                     @Override
    //                                     public void onFailure(Object error) {
    //                                     }
    //                                 });
    //                             }
    //                         })
    //                         .show();
    //             } else if (index == 1) {
    //                 new MaterialDialog.Builder(MainActivity.this)
    //                         .title(R.string.edit_name)
    //                         .inputType(InputType.TYPE_CLASS_TEXT)
    //                         .inputRange(1, 22)
    //                         .input(null, category.getStreamName(), new MaterialDialog.InputCallback() {
    //                             @Override
    //                             public void onInput(@NotNull MaterialDialog dialog, CharSequence input) {
    //                                 String renamed = input.toString();
    //                                 XLog.i("分类重命名为：" + renamed);
    //                                 if (category.getStreamName().equals(renamed)) {
    //                                     return;
    //                                 }
    //                                 renameCategory(renamed, category);
    //                             }
    //                         })
    //                         .positiveText(R.string.confirm)
    //                         .negativeText(android.R.string.cancel)
    //                         .show();
    //             }else if(index == 2){
    //                 Intent intent = new Intent(MainActivity.this, TriggerRuleManagerActivity.class);
    //                 intent.putExtra(Contract.TYPE, Contract.TYPE_CATEGORY);
    //                 intent.putExtra(Contract.TARGET_ID, category.getStreamId());
    //                 startActivity(intent);
    //                 overridePendingTransition(R.anim.in_from_bottom, R.anim.fade_out);
    //             }
    //             dialog.dismiss();
    //         }
    //     });
    //     adapter.add(new MaterialSimpleListItem.Builder(MainActivity.this)
    //             .content(R.string.delete)
    //             .icon(R.drawable.ic_delete)
    //             .backgroundColor(Color.TRANSPARENT)
    //             .build());
    //     adapter.add(new MaterialSimpleListItem.Builder(MainActivity.this)
    //             .content(R.string.rename)
    //             .icon(R.drawable.ic_rename)
    //             .backgroundColor(Color.TRANSPARENT)
    //             .build());
    //     adapter.add(new MaterialSimpleListItem.Builder(MainActivity.this)
    //             .content(R.string.view_rule)
    //             .icon(R.drawable.ic_rule)
    //             .backgroundColor(Color.TRANSPARENT)
    //             .build());
    //
    //     new MaterialDialog.Builder(MainActivity.this)
    //             .adapter(adapter, new LinearLayoutManager(MainActivity.this))
    //             .show();
    // }
    public void showCategoryMenuDialog(final CollectionTree category) {
        // 重命名弹窗的适配器
        MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(new MaterialSimpleListAdapter.Callback() {
            @Override
            public void onMaterialListItemSelected(MaterialDialog dialog, int index, MaterialSimpleListItem item) {
                if(index == 0){
                    new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.delete)
                            .positiveText(R.string.confirm)
                            .negativeText(android.R.string.cancel)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    App.i().getApi().deleteCategory(category.getParent().getId(), new CallbackX() {
                                        @Override
                                        public void onSuccess(Object result) {
                                            ToastUtils.show((String)result);
                                        }

                                        @Override
                                        public void onFailure(Object error) {
                                        }
                                    });
                                }
                            })
                            .show();
                } else if (index == 1) {
                    new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.edit_name)
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .inputRange(1, 22)
                            .input(null, category.getParent().getTitle(), new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NotNull MaterialDialog dialog, CharSequence input) {
                                    String renamed = input.toString();
                                    XLog.i("分类重命名为：" + renamed);
                                    if (category.getParent().getTitle().equals(renamed)) {
                                        return;
                                    }
                                    renameCategory(renamed, category);
                                }
                            })
                            .positiveText(R.string.confirm)
                            .negativeText(android.R.string.cancel)
                            .show();
                }else if(index == 2){
                    Intent intent = new Intent(MainActivity.this, TriggerRuleManagerActivity.class);
                    intent.putExtra(Contract.TYPE, Contract.TYPE_CATEGORY);
                    intent.putExtra(Contract.TARGET_ID, category.getParent().getId());
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_bottom, R.anim.fade_out);
                }
                dialog.dismiss();
            }
        });
        adapter.add(new MaterialSimpleListItem.Builder(MainActivity.this)
                .content(R.string.delete)
                .icon(R.drawable.ic_delete)
                .backgroundColor(Color.TRANSPARENT)
                .build());
        adapter.add(new MaterialSimpleListItem.Builder(MainActivity.this)
                .content(R.string.rename)
                .icon(R.drawable.ic_rename)
                .backgroundColor(Color.TRANSPARENT)
                .build());
        adapter.add(new MaterialSimpleListItem.Builder(MainActivity.this)
                .content(R.string.view_rule)
                .icon(R.drawable.ic_rule)
                .backgroundColor(Color.TRANSPARENT)
                .build());

        new MaterialDialog.Builder(MainActivity.this)
                .adapter(adapter, new LinearLayoutManager(MainActivity.this))
                .show();
    }

    public void renameCategory(final String renamed, CollectionTree category) {
        App.i().getApi().renameCategory(category.getParent().getId(), renamed, new CallbackX<String,String>() {
            @Override
            public void onSuccess(String result) {
                categoryListAdapter.notifyDataSetChanged();
                ToastUtils.show(getString(R.string.edit_success));
            }

            @Override
            public void onFailure(String error) {
                ToastUtils.show(getString(R.string.edit_fail_with_reason, error));
            }
        });
    }
    // public void renameCategory(final String renamed, StreamTree category) {
    //     App.i().getApi().renameCategory(category.getStreamId(), renamed, new CallbackX<String,String>() {
    //         @Override
    //         public void onSuccess(String result) {
    //             categoryListAdapter.notifyDataSetChanged();
    //             ToastUtils.show(getString(R.string.edit_success));
    //         }
    //
    //         @Override
    //         public void onFailure(String error) {
    //             ToastUtils.show(getString(R.string.edit_fail_with_reason, error));
    //         }
    //     });
    // }

    public void showFeedActivity(String feedId) {
        if (StringUtils.isEmpty(feedId)) {
            return;
        }
        Intent intent = new Intent(MainActivity.this, FeedActivity.class);
        intent.putExtra("feedId", feedId);
        startActivity(intent);
        overridePendingTransition(R.anim.in_from_bottom, R.anim.fade_out);
    }


    LinearLayoutManager linearLayoutManager;
    public void initArtListView() {
        articleListView = findViewById(R.id.main_slv);
        linearLayoutManager = new LinearLayoutManager(this);
        articleListView.setLayoutManager(linearLayoutManager);

        // HeaderView。
        articlesHeaderView = getLayoutInflater().inflate(R.layout.main_item_header, articleListView, false);
        countTips = (TextView) articlesHeaderView.findViewById(R.id.main_header_title);
        ImageView eye = articlesHeaderView.findViewById(R.id.main_header_eye);
        eye.setOnClickListener(v -> ToastUtils.show(R.string.display_filter_is_under_development) );
        articleListView.addHeaderView(articlesHeaderView);

        articleListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final int position) {
                new XPopup.Builder(MainActivity.this)
                        .isCenterHorizontal(true) //是否与目标水平居中对齐
                        .offsetY(-view.getHeight() / 2)
                        .hasShadowBg(true)
                        // .isDarkTheme((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES)
                        .popupAnimation(PopupAnimation.ScaleAlphaFromCenter)
                        .atView(view)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
                        .asAttachList(
                                new String[]{getString(R.string.speak_article), getString(R.string.mark_above_as_read), getString(R.string.mark_below_as_read), getString(R.string.mark_as_unread)},
                                new int[]{R.drawable.ic_volume, R.drawable.ic_mark_up, R.drawable.ic_mark_down, R.drawable.ic_mark_unread},
                                new OnSelectListener() {
                                    @Override
                                    public void onSelect(int which, String text) {
                                        switch (which) {
                                            case 0:
                                                Intent intent = new Intent(MainActivity.this,TTSActivity.class);
                                                intent.putExtra("articleNo",position);
                                                intent.putExtra("isQueue",true);
                                                startActivity(intent);
                                                overridePendingTransition(R.anim.in_from_bottom, R.anim.fade_out);
                                                break;
                                            case 1:
                                                // Integer[] index = new Integer[2];
                                                // index[0] = position + 1;
                                                // index[1] = 0;
                                                // new MarkListReadedAsyncTask().execute(index);
                                                showConfirmDialog(position + 1,0);
                                                break;
                                            case 2:
                                                showConfirmDialog(position,articlesAdapter.getItemCount());
                                                break;
                                            case 3:
                                                Article article = articlesAdapter.getArticle(position);
                                                if( article == null ){
                                                    return;
                                                }

                                                if (article.getReadStatus() == App.STATUS_READED) {
                                                    int oldReadStatus = article.getReadStatus();
                                                    App.i().getApi().markArticleUnread(article.getId(), new CallbackX() {
                                                        @Override
                                                        public void onSuccess(Object result) {
                                                        }
                                                        @Override
                                                        public void onFailure(Object error) {
                                                            article.setReadStatus(oldReadStatus);
                                                            CoreDB.i().articleDao().update(article);
                                                        }
                                                    });
                                                }
                                                article.setReadStatus(App.STATUS_UNREADING);
                                                CoreDB.i().articleDao().update(article);
                                                // articlesAdapter.notifyItemChanged(position);
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                })
                        .show();
            }
        });

        articleListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            // 正在被外部拖拽,一般为用户正在用手指滚动 SCROLL_STATE_DRAGGING，自动滚动 SCROLL_STATE_SETTLING，正在滚动（SCROLL_STATE_IDLE）
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                articlesAdapter.setLastPos(linearLayoutManager.findLastVisibleItemPosition()-1);
                // XLog.i("【滚动】" + ((RecyclerView.LayoutParams) recyclerView.getChildAt(1).getLayoutParams()).getViewAdapterPosition() + " = "+  linearLayoutManager.findFirstVisibleItemPosition() + " , " + linearLayoutManager.findLastVisibleItemPosition());
                if (!autoMarkReaded) {
                    return;
                }
                // XLog.d("滚动：" + newState + " -> " + scrollIndex);
                // if(linearLayoutManager.findFirstVisibleItemPosition() == scrollPositionEnd +1 || linearLayoutManager.findFirstVisibleItemPosition() == scrollPositionEnd -1){
                //     scrollPositionEnd = linearLayoutManager.findFirstVisibleItemPosition();
                // }
                //  || RecyclerView.SCROLL_STATE_SETTLING == newState
                if (RecyclerView.SCROLL_STATE_DRAGGING == newState && scrollIndex == null) {
                    scrollIndex = new Integer[2];
                    scrollIndex[1] = linearLayoutManager.findFirstVisibleItemPosition();
                    // XLog.i("滚动开始：" + scrollIndex[1] );
                } else if (RecyclerView.SCROLL_STATE_IDLE == newState && scrollIndex != null) {
                    // scrollIndex[0] = scrollPositionEnd;
                    scrollIndex[0] = linearLayoutManager.findFirstVisibleItemPosition(); // ((RecyclerView.LayoutParams) recyclerView.getChildAt(0).getLayoutParams()).getViewAdapterPosition()
                    new MarkListReadAsyncTask().execute(scrollIndex);
                    // XLog.i("滚动结束：" + scrollPositionEnd );
                    scrollIndex = null;
                }
            }
        });

        // 创建菜单：
        SwipeMenuCreator mSwipeMenuCreator = new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu leftMenu, SwipeMenu rightMenu, int position) {
                Article article = articlesAdapter.getArticle(position);
                //XLog.e("创建菜单: " + position + ", " + (article==null) + ", " +  articlesAdapter.getCurrentList().getLastKey() + " , " + articlesAdapter.getCurrentList().getLoadedCount()  );
                if(article==null){
                    return;
                }

                int width = getResources().getDimensionPixelSize(R.dimen.dp_80);
                int margin = getResources().getDimensionPixelSize(R.dimen.dp_30);

                // XLog.e("添加左右菜单" + position );
                // 1. MATCH_PARENT 自适应高度，保持和Item一样高;  2. 指定具体的高，比如80; 3. WRAP_CONTENT，自身高度，不推荐;
                int height = ViewGroup.LayoutParams.MATCH_PARENT;

                SwipeMenuItem starItem = new SwipeMenuItem(MainActivity.this); // 各种文字和图标属性设置。
                if (article.getStarStatus() == App.STATUS_STARED) {
                    starItem.setImage(R.drawable.ic_state_unstar);
                } else {
                    starItem.setImage(R.drawable.ic_state_star);
                }
                starItem.setWeight(width);
                starItem.setHeight(height);
                starItem.setMargins(margin, 0, margin, 0);
                leftMenu.addMenuItem(starItem); // 在Item左侧添加一个菜单。

                SwipeMenuItem readItem = new SwipeMenuItem(MainActivity.this); // 各种文字和图标属性设置。
                if (article.getReadStatus() == App.STATUS_READED) {
                    readItem.setImage(R.drawable.ic_state_unread);
                } else {
                    readItem.setImage(R.drawable.ic_read);
                }
                readItem.setWeight(width);
                readItem.setHeight(height);
                readItem.setMargins(margin, 0, margin, 0);
                rightMenu.addMenuItem(readItem); // 在Item右侧添加一个菜单。
                // 注意：哪边不想要菜单，那么不要添加即可。
            }
        };
        articleListView.setSwipeMenuCreator(mSwipeMenuCreator);

        articleListView.setOnItemSwipeListener(new OnItemSwipeListener() {
            @Override
            public void onClose(View swipeMenu, int direction, int adapterPosition) { }

            @Override
            public void onCloseLeft(int position) {
                XLog.i("onCloseLeft：" + position + "  ");
                toggleStarState(position);
            }

            @Override
            public void onCloseRight(int position) {
                XLog.i("onCloseRight：" + position + "  ");
                toggleReadState(position);
            }
        });

        // OnItemMenuClickListener mItemMenuClickListener = new OnItemMenuClickListener() {
        //     @Override
        //     public void onItemClick(SwipeMenuBridge menuBridge, int position) {
        //         // 任何操作必须先关闭菜单，否则可能出现Item菜单打开状态错乱。
        //         menuBridge.closeMenu();
        //
        //         // 左侧还是右侧菜单：
        //         int direction = menuBridge.getDirection();
        //
        //         if (direction == SwipeRecyclerView.RIGHT_DIRECTION) {
        //             XLog.i("onItemClick  onCloseRight：" + position + "  ");
        //             if (position > -1) {
        //                 toggleReadState(position);
        //             }
        //         } else if (direction == SwipeRecyclerView.LEFT_DIRECTION) {
        //             XLog.i("onItemClick  onCloseLeft：" + position + "  ");
        //             if (position > -1) {
        //                 toggleStarState(position);
        //             }
        //         }
        //     }
        // };
        // // 菜单点击监听。
        // articleListView.setOnItemMenuClickListener(mItemMenuClickListener);

        articleListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position < 0) {
                    return;
                }
                Intent intent = new Intent(MainActivity.this, ArticleActivity.class);
                intent.putExtra("theme", App.i().getUser().getThemeMode());

                String articleId = articlesAdapter.getArticleId(position);

                XLog.i("进入文章详情页，位置：" + position + "，ID：" + articleId);
                intent.putExtra("articleId", articleId);
                intent.putExtra("articleNo", position); // 下标从 0 开始
                intent.putExtra("articleCount", articlesAdapter.getItemCount());
                startActivityForResult(intent, App.ActivityResult_ArtToMain);
                overridePendingTransition(R.anim.in_from_bottom, R.anim.fade_out);
            }
        });
        articleListViewModel = new ViewModelProvider(this).get(ArticleListViewModel.class);
        articlesAdapter = new ArticlePagedListAdapter();
        articleListView.setAdapter(articlesAdapter);
    }


   private CategoryViewModel categoryViewModel;
   public void onClickCategoryIcon(View view) {
       XLog.i("tag按钮被点击");
       tagBottomSheetDialog.show();
       categoryListAdapter.notifyDataSetChanged();
   }

    private void initCategoriesData(){
        categoryViewModel.observeCategoriesAndFeeds(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
                    @Override
                    public void run() {
                        // long time = System.currentTimeMillis();
                        categoryListAdapter.setGroups(categoryViewModel.getCategoryFeeds());
                        // XLog.i("重新加载 分类树 耗时：" + (System.currentTimeMillis() - time));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                categoryListAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                });
            }
        });
    }

    // StickyHeaderLayout stickyHeaderLayout;
    public void initTagListView() {
        tagBottomSheetDialog = new BottomSheetDialog(MainActivity.this);
        tagBottomSheetDialog.setContentView(R.layout.bottom_sheet_category);
        tagBottomSheetLayout = tagBottomSheetDialog.findViewById(R.id.sheet_tag);

        categoryListView = tagBottomSheetDialog.findViewById(R.id.main_tag_list_view);
        categoryListView.setLayoutManager(new LinearLayoutManager(this));
        // 还有另外一种方案，通过设置动画执行时间为0来解决问题：
        categoryListView.getItemAnimator().setChangeDuration(0);
        // 关闭默认的动画
        ((SimpleItemAnimator) categoryListView.getItemAnimator()).setSupportsChangeAnimations(false);
        // 设置悬浮头部VIEW
        // tagListView.setHeaderView(getLayoutInflater().inflate(R.layout.main_expandable_item_group_header, tagListView, false));

        // View headerView = getLayoutInflater().inflate(R.layout.tag_expandable_item_group, tagListView, false);
        // tagListView.addHeaderView(headerView);


        // categoryListAdapter = new CategoriesAdapter(this);
        categoryListAdapter = new StreamsAdapter(this);

        categoryListAdapter.setGroups(new ArrayList<>());
        categoryListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int adapterPosition) {
                tagBottomSheetDialog.dismiss();
                // 根据原position判断该item是否是parent item
                int groupPosition = categoryListAdapter.parentItemPosition(adapterPosition);
                User user = App.i().getUser();

                // if (categoryListAdapter.isParentItem(adapterPosition)) {
                //     StreamTree streamTree = categoryListAdapter.getGroup(groupPosition);
                //     user.setStreamId( streamTree.getStreamId().replace("\"", "")  );
                //     user.setStreamTitle( streamTree.getStreamName() );
                //     user.setStreamType( streamTree.getStreamType() );
                // } else {
                //     // 换取child position
                //     int childPosition = categoryListAdapter.childItemPosition(adapterPosition);
                //     Collection feed = categoryListAdapter.getChild(groupPosition, childPosition);
                //     user.setStreamId( feed.getId() );
                //     user.setStreamTitle( feed.getTitle() );
                //     user.setStreamType( App.TYPE_FEED );
                // }

                if (categoryListAdapter.isParentItem(adapterPosition)) {
                    CollectionTree streamTree = categoryListAdapter.getGroup(groupPosition);
                    user.setStreamId( streamTree.getParent().getId().replace("\"", "")  );
                    user.setStreamTitle( streamTree.getParent().getTitle() );
                    user.setStreamType( streamTree.getType() );
                } else {
                    // 换取child position
                    int childPosition = categoryListAdapter.childItemPosition(adapterPosition);
                    Collection feed = categoryListAdapter.getChild(groupPosition, childPosition);
                    user.setStreamId( feed.getId() );
                    user.setStreamTitle( feed.getTitle() );
                    user.setStreamType( App.TYPE_FEED );
                }
                refreshArticlesData();
                CoreDB.i().userDao().update(user);
            }
        });

        categoryListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, int adapterPosition) {
                MobclickAgent.onEvent(MainActivity.this, "click_categories_button");
                // XLog.e("被长安，view的id是" + allArticleHeaderView.getId() + "，parent的id" + parent.getId() + "，Tag是" + allArticleHeaderView.getCategoryById() + "，位置是" + tagListView.getPositionForView(allArticleHeaderView));
                // 根据原position判断该item是否是parent item

                // if (categoryListAdapter.isParentItem(adapterPosition)) {
                //     StreamTree streamTree = categoryListAdapter.getGroup(adapterPosition);
                //     if(streamTree.getStreamType() == StreamTree.FEED){
                //         showFeedActivity(streamTree.getStreamId());
                //     }else{
                //         int parentPosition = categoryListAdapter.parentItemPosition(adapterPosition);
                //         showCategoryMenuDialog(categoryListAdapter.getGroup(parentPosition));
                //     }
                // } else {
                //     // 换取child position
                //     int parentPosition = categoryListAdapter.parentItemPosition(adapterPosition);
                //     int childPosition = categoryListAdapter.childItemPosition(adapterPosition);
                //     Collection feed = categoryListAdapter.getChild(parentPosition, childPosition);
                //     showFeedActivity(feed.getId());
                // }

                if (categoryListAdapter.isParentItem(adapterPosition)) {
                    CollectionTree streamTree = categoryListAdapter.getGroup(adapterPosition);
                    if(streamTree.getType() == CollectionTree.FEED){
                        showFeedActivity(streamTree.getParent().getId());
                    }else{
                        int parentPosition = categoryListAdapter.parentItemPosition(adapterPosition);
                        showCategoryMenuDialog(categoryListAdapter.getGroup(parentPosition));
                    }
                } else {
                    // 换取child position
                    int parentPosition = categoryListAdapter.parentItemPosition(adapterPosition);
                    int childPosition = categoryListAdapter.childItemPosition(adapterPosition);
                    Collection feed = categoryListAdapter.getChild(parentPosition, childPosition);
                    showFeedActivity(feed.getId());
                }
            }
        });


        // tagListAdapter = new GroupedListAdapter(this);
        // tagListAdapter.setGroupClickListener(new GroupedListAdapter.OnGroupClickListener() {
        //     @Override
        //     public void onClick(View view, int groupPosition) {
        //         //XLog.i("【 TagList 被点击】" + App.i().getUser().toString());
        //         tagBottomSheetDialog.dismiss();
        //         User user = App.i().getUser();
        //         user.setStreamId( tagListAdapter.getGroup(groupPosition).getId().replace("\"", "")  );
        //         user.setStreamTitle( tagListAdapter.getGroup(groupPosition).getTitle() );
        //         user.setStreamType( App.TYPE_GROUP );
        //         CoreDB.i().userDao().update(user);
        //         refreshArticlesData();
        //     }
        //
        //     @Override
        //     public void onLongClick(View view, int groupPosition) {
        //         showTagMenuDialog(tagListAdapter.getGroup(groupPosition));
        //     }
        // });
        //
        // tagListAdapter.setChildClickListener(new GroupedListAdapter.OnChildClickListener() {
        //     @Override
        //     public void onClick(View view, int groupPosition, int childPosition) {
        //         tagBottomSheetDialog.dismiss();
        //         Collection feed = tagListAdapter.getChild(groupPosition, childPosition);
        //         User user = App.i().getUser();
        //         user.setStreamId( feed.getId() );
        //         user.setStreamTitle( feed.getTitle() );
        //         user.setStreamType( App.TYPE_FEED );
        //         CoreDB.i().userDao().update(user);
        //         refreshArticlesData();
        //     }
        //
        //     @Override
        //     public void onLongClick(View view, int groupPosition, int childPosition) {
        //         showFeedActivity(groupPosition, childPosition);
        //     }
        // });

        // 使用 GroupedRecyclerViewAdapter
        // stickyHeaderLayout = tagBottomSheetDialog.findViewById(R.id.sheet_tag_sticky_header);
        // stickyHeaderLayout.setSticky(true);
        // tagListAdapter = new GroupAdapter(this);
        // tagListAdapter.setOnHeaderClickListener(new GroupedRecyclerViewAdapter.OnHeaderClickListener() {
        //     @Override
        //     public void onHeaderClick(GroupedRecyclerViewAdapter adapter, BaseViewHolder holder, int groupPosition) {
        //         tagBottomSheetDialog.dismiss();
        //         Collection category = tagListAdapter.getGroup(groupPosition);
        //         User user = App.i().getUser();
        //         user.setStreamId( category.getId().replace("\"", "")  );
        //         user.setStreamTitle( category.getTitle() );
        //         user.setStreamType( App.TYPE_GROUP );
        //         CoreDB.i().userDao().update(user);
        //         refreshArticlesData();
        //     }
        // });
        // tagListAdapter.setOnChildClickListener(new GroupedRecyclerViewAdapter.OnChildClickListener() {
        //     @Override
        //     public void onChildClick(GroupedRecyclerViewAdapter adapter, BaseViewHolder holder, int groupPosition, int childPosition) {
        //         tagBottomSheetDialog.dismiss();
        //         Collection feed = tagListAdapter.getChild(groupPosition, childPosition);
        //         User user = App.i().getUser();
        //         user.setStreamId( feed.getId() );
        //         user.setStreamTitle( feed.getTitle() );
        //         user.setStreamType( App.TYPE_FEED );
        //         CoreDB.i().userDao().update(user);
        //         refreshArticlesData();
        //     }
        // });
        // tagListAdapter.setHeaderLongClickListener(new GroupAdapter.OnHeaderLongClickListener() {
        //     @Override
        //     public void onLongClick(View view, int groupPosition) {
        //         showTagMenuDialog(tagListAdapter.getGroup(groupPosition));
        //     }
        // });
        // tagListAdapter.setChildLongClickListener(new GroupAdapter.OnChildLongClickListener() {
        //     @Override
        //     public void onLongClick(View view, int groupPosition, int childPosition) {
        //         showFeedActivity(groupPosition, childPosition);
        //     }
        // });

        categoryListView.setAdapter(categoryListAdapter);
        categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);


        createCategoryButton = tagBottomSheetDialog.findViewById(R.id.main_tag_create_category);
        if(App.i().getApi() instanceof LocalApi){
            createCategoryButton.setVisibility(View.VISIBLE);
        }
        createCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(MainActivity.this)
                        .title(R.string.edit_name)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .inputRange(1, 22)
                        .input(null, null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NotNull MaterialDialog dialog, CharSequence input) {
                                Category category = new Category();
                                category.setUid(App.i().getUser().getId());
                                category.setTitle(input.toString());
                                category.setId(EncryptUtils.MD5(input.toString()));
                                CoreDB.i().categoryDao().insert(category);
                            }
                        })
                        .positiveText(R.string.confirm)
                        .negativeText(android.R.string.cancel)
                        .show();
            }
        });
    }

    private void showConfirmDialog(final int start, final int end) {
        int titleRes;
        if(start < end){
            titleRes = R.string.mark_the_below_articles_as_read;
        }else {
            titleRes = R.string.mark_the_above_articles_as_read;
        }

        new MaterialDialog.Builder(this)
                .title(titleRes)
                .positiveText(R.string.confirm)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Integer[] index = new Integer[2];
                        index[0] = start;
                        index[1] = end;
                        new MarkListReadAsyncTask().setIncludesForcedUnread(false).execute(index);
                    }
                })
                .neutralText(R.string.includes_forced_unread)
                .neutralColor(getResources().getColor(R.color.material_red_400) )
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Integer[] index = new Integer[2];
                        index[0] = start;
                        index[1] = end;
                        new MarkListReadAsyncTask().setIncludesForcedUnread(true).execute(index);
                    }
                })
                .negativeText(android.R.string.cancel)
                .show();
    }



    // 标记以上/以下为已读
    @SuppressLint("StaticFieldLeak")
    private class MarkListReadAsyncTask extends AsyncTask<Integer, Integer, Integer> {
       private boolean includesForcedUnread = false;
       protected MarkListReadAsyncTask setIncludesForcedUnread(boolean includesForcedUnread){
           this.includesForcedUnread = includesForcedUnread;
           return this;
       }
        @Override
        protected Integer doInBackground(Integer... params) {
            int startIndex = params[0];
            int endIndex = params[1];
            boolean desc = startIndex >= endIndex;

            List<String> articleIDs;
            if( desc ){
                articleIDs = new ArrayList<>(startIndex - endIndex);
                for (int i = startIndex - 1; i >= endIndex; i--){
                    articleIDs.add(articlesAdapter.getArticleId(i));
                }
            }else {
                articleIDs = new ArrayList<>(endIndex - startIndex);
                for (int i = startIndex; i < endIndex; i++){
                    articleIDs.add(articlesAdapter.getArticleId(i));
                }
            }

            if (articleIDs.size() == 0) {
                return 0;
            }

            int needCount = articleIDs.size();
            int hadCount = 0;
            int num = 0;

            while (needCount > 0) {
                num = Math.min(50, needCount);
                List<String> subArticleIDs = articleIDs.subList(hadCount, hadCount + num);
                hadCount = hadCount + num;
                needCount = articleIDs.size() - hadCount;

                markReadWithUnread(CoreDB.i().articleDao().getUnreadArticleIds(App.i().getUser().getId(), subArticleIDs));
                if(includesForcedUnread){
                    markReadWithUnreading(CoreDB.i().articleDao().getUnreadingArticleIds(App.i().getUser().getId(), subArticleIDs));
                }
            }
            //返回结果
            return 0;
        }

        private void markReadWithUnread(List<String> articleIDs){
            CoreDB.i().articleDao().markArticlesRead(App.i().getUser().getId(), articleIDs);
            App.i().getApi().markArticleListReaded(articleIDs, new CallbackX() {
                @Override
                public void onSuccess(Object result) {
                }

                @Override
                public void onFailure(Object error) {
                    CoreDB.i().articleDao().markArticlesUnread(App.i().getUser().getId(), articleIDs);
                }
            });
        }

        private void markReadWithUnreading(List<String> articleIDs){
            CoreDB.i().articleDao().markArticlesRead(App.i().getUser().getId(), articleIDs);
            App.i().getApi().markArticleListReaded(articleIDs, new CallbackX() {
                @Override
                public void onSuccess(Object result) {
                }

                @Override
                public void onFailure(Object error) {
                    CoreDB.i().articleDao().markArticlesUnreading(App.i().getUser().getId(), articleIDs);
                }
            });
        }
    }

    private void toggleReadState(final int position) {
        if (position < 0) {
            return;
        }
        // String articleId = articlesAdapter.getItem(position).getId();
        // Article article = CoreDB.i().articleDao().getById(App.i().getUser().getId(),articleId);
        XLog.i("切换已读状态" );
        Article article = articlesAdapter.getArticle(position);
        if(article == null){
            return;
        }
        if (autoMarkReaded && article.getReadStatus() == App.STATUS_UNREAD) {
            article.setReadStatus(App.STATUS_UNREADING);
            CoreDB.i().articleDao().update(article);
        } else if (article.getReadStatus() == App.STATUS_READED) {
            article.setReadStatus(App.STATUS_UNREADING);
            CoreDB.i().articleDao().update(article);
            App.i().getApi().markArticleUnread(article.getId(), new CallbackX() {
                @Override
                public void onSuccess(Object result) {
                }

                @Override
                public void onFailure(Object error) {
                    article.setReadStatus(App.STATUS_READED);
                    CoreDB.i().articleDao().update(article);
                    XLog.w("失败的原因是：" + error );
                }
            });
        } else {
            article.setReadStatus(App.STATUS_READED);
            CoreDB.i().articleDao().update(article);
            App.i().getApi().markArticleReaded(article.getId(), new CallbackX() {
                @Override
                public void onSuccess(Object result) {
                }

                @Override
                public void onFailure(Object error) {
                    article.setReadStatus(App.STATUS_UNREAD);
                    CoreDB.i().articleDao().update(article);
                }
            });
        }
    }


    private void toggleStarState(final int position) {
        if (position < 0) {
            return;
        }

        XLog.i("切换加星状态" );
        Article article = articlesAdapter.getArticle(position);
        if(article == null){
            return;
        }

        if (article.getStarStatus() == App.STATUS_STARED) {
            article.setStarStatus(App.STATUS_UNSTAR);
            CoreDB.i().articleDao().update(article);

            App.i().getApi().markArticleUnstar(article.getId(), new CallbackX() {
                 @Override
                 public void onSuccess(Object result) {
                 }
                 @Override
                 public void onFailure(Object error) {
                     article.setStarStatus(App.STATUS_STARED);
                     CoreDB.i().articleDao().update(article);
                 }
             });

        } else {
            article.setStarStatus(App.STATUS_STARED);
            CoreDB.i().articleDao().update(article);
            App.i().getApi().markArticleStared(article.getId(), new CallbackX() {
                 @Override
                 public void onSuccess(Object result) {
                 }

                 @Override
                 public void onFailure(Object error) {
                     article.setStarStatus(App.STATUS_UNSTAR);
                     CoreDB.i().articleDao().update(article);
                 }
             });
        }
    }


    // TODO: 2018/3/4 改用观察者模式。http://iaspen.cn/2015/05/09/观察者模式在android上的最佳实践
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
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        //XLog.i("------------------------------------------" + resultCode + requestCode);
        if (resultCode == App.ActivityResult_ArtToMain) {//在文章页的时候读到了第几篇文章，好让列表也自动将该项置顶
            int articleNo = intent.getExtras().getInt("articleNo");
            assert linearLayoutManager != null;
            if (articleNo > linearLayoutManager.findLastVisibleItemPosition() - 1) {
                listScrollPosition(articleNo);
            }
        }
    }

    // 滚动到指定位置
    private void listScrollPosition(final int position) {
        // 保证滚动到指定位置时，view至最顶端
        LinearSmoothScroller smoothScroller = new LinearSmoothScroller(this) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
        smoothScroller.setTargetPosition(position);
        Objects.requireNonNull(articleListView.getLayoutManager()).startSmoothScroll(smoothScroller);
    }

    public void onClickRefreshIcon(View view) {
        refreshArticlesData();
    }

    public void onClickQuickSettingIcon(View view) {
        quickSettingDialog = new BottomSheetDialog(MainActivity.this);
        quickSettingDialog.setContentView(R.layout.main_bottom_sheet_more);
        //quickSettingDialog.dismiss(); //dialog消失
        //quickSettingDialog.setCanceledOnTouchOutside(false);  //触摸dialog之外的地方，dialog不消失
        //quickSettingDialog.setCancelable(false); // dialog无法取消，按返回键都取消不了

        View moreSetting = quickSettingDialog.findViewById(R.id.more_setting);
        moreSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quickSettingDialog.dismiss();
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_bottom, R.anim.fade_out);
            }
        });

        SwitchButton autoMarkWhenScrolling = quickSettingDialog.findViewById(R.id.auto_mark_when_scrolling_switch);
        autoMarkWhenScrolling.setChecked(App.i().getUser().isMarkReadOnScroll());
        autoMarkWhenScrolling.setOnCheckedChangeListener((compoundButton, b) -> {
            XLog.i("onClickedAutoMarkWhenScrolling图标被点击");
            User user = App.i().getUser();
            user.setMarkReadOnScroll(b);
            CoreDB.i().userDao().update(user);
            autoMarkReaded = b;
            if (autoMarkReaded) {
                vToolbarAutoMark.setVisibility(View.VISIBLE);
            } else {
                vToolbarAutoMark.setVisibility(View.GONE);
            }
        });

        SwitchButton downImgOnWifiSwitch = quickSettingDialog.findViewById(R.id.down_img_on_wifi_switch);
        downImgOnWifiSwitch.setChecked(App.i().getUser().isDownloadImgOnlyWifi());
        downImgOnWifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                User user = App.i().getUser();
                user.setDownloadImgOnlyWifi(b);
                CoreDB.i().userDao().update(user);
            }
        });

        SwitchButton nightThemeWifiSwitch = quickSettingDialog.findViewById(R.id.night_theme_switch);
        nightThemeWifiSwitch.setChecked(App.i().getUser().getThemeMode() == App.THEME_NIGHT);
        nightThemeWifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                quickSettingDialog.dismiss();
                manualToggleTheme();
            }
        });

        RadioGroup radioGroup = quickSettingDialog.findViewById(R.id.article_list_state_radio_group);
        final RadioButton radioAll = quickSettingDialog.findViewById(R.id.radio_all);
        final RadioButton radioUnread = quickSettingDialog.findViewById(R.id.radio_unread);
        final RadioButton radioStarred = quickSettingDialog.findViewById(R.id.radio_starred);
        if (App.i().getUser().getStreamStatus() == App.STATUS_STARED) {
            radioStarred.setChecked(true);
        } else if (App.i().getUser().getStreamStatus() == App.STATUS_UNREAD) {
            radioUnread.setChecked(true);
        } else {
            radioAll.setChecked(true);
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                User user = App.i().getUser();
                if (i == radioStarred.getId()) {
                    user.setStreamStatus(App.STATUS_STARED);
                    toolbar.setNavigationIcon(R.drawable.ic_state_star);
                } else if (i == radioUnread.getId()) {
                    user.setStreamStatus(App.STATUS_UNREAD);
                    toolbar.setNavigationIcon(R.drawable.ic_state_unread);
                } else {
                    user.setStreamStatus(App.STATUS_ALL);
                    toolbar.setNavigationIcon(R.drawable.ic_state_all);
                }
                CoreDB.i().userDao().update(user);
                refreshArticlesData();
                // loadCategoriesData();
                quickSettingDialog.dismiss();
            }
        });

        IconFontView iconFontView = quickSettingDialog.findViewById(R.id.main_more_close);
        iconFontView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quickSettingDialog.dismiss();
            }
        });

        quickSettingDialog.show();
    }


    @OnClick(R.id.main_toolbar)
    public void clickToolbar(View view) {
        if (maHandler.hasMessages(App.MSG_DOUBLE_TAP)) {
            maHandler.removeMessages(App.MSG_DOUBLE_TAP);
            // maHandler.sendEmptyMessageDelayed(App.MSG_SCROLL_TIMEOUT, 1000);
            articleListView.smoothScrollToPosition(0);
        } else {
            maHandler.sendEmptyMessageDelayed(App.MSG_DOUBLE_TAP, ViewConfiguration.getDoubleTapTimeout());
        }
    }


    /**
     * 监听返回键，弹出提示退出对话框
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
                .setMessage(R.string.are_you_sure_you_want_to_exit_the_app)
                .setPositiveButton(R.string.exit_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                })
                .setNegativeButton(R.string.exit_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private RelativeLayout bottomBar;
    private void initToolbar() {
        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setHomeButtonEnabled(true);
        // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if (App.i().getUser().getStreamStatus() == App.STATUS_ALL) {
            toolbar.setNavigationIcon(R.drawable.ic_state_all);
        } else if (App.i().getUser().getStreamStatus() == App.STATUS_STARED) {
            toolbar.setNavigationIcon(R.drawable.ic_state_star);
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_state_unread);
        }
        // 左上角图标是否显示，false则没有程序图标，仅标题。否则显示应用程序图标，对应id为android.R.id.home，对应ActionBar.DISPLAY_SHOW_HOME
        // setDisplayShowHomeEnabled(true)
        // 使自定义的普通View能在title栏显示，即actionBar.setCustomView能起作用，对应ActionBar.DISPLAY_SHOW_CUSTOM
        // setDisplayShowCustomEnabled(true)

        bottomBar = findViewById(R.id.main_bottombar);
    }

    private void openLoadingPopupView(){
        loadingPopupView = new XPopup.Builder(MainActivity.this)
                .asLoading(getString(R.string.loading))
                .show();
    }
    private void dismissLoadingPopupView(){
        if(loadingPopupView != null) loadingPopupView.smartDismiss();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem disableProxyMenuItem = menu.findItem(R.id.main_menu_proxy_disable);
        MenuItem enableProxyMenuItem = menu.findItem(R.id.main_menu_proxy_enable);
        if(CorePref.i().globalPref().getBoolean(Contract.ENABLE_PROXY,false)){
            disableProxyMenuItem.setVisible(false);
            enableProxyMenuItem.setVisible(true);
        }else {
            disableProxyMenuItem.setVisible(true);
            enableProxyMenuItem.setVisible(false);
        }
        LiveEventBus.get(Contract.ENABLE_PROXY, Boolean.class)
                .observeSticky(this, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean enable) {
                        if(enable){
                            disableProxyMenuItem.setVisible(false);
                            enableProxyMenuItem.setVisible(true);
                        }else {
                            disableProxyMenuItem.setVisible(true);
                            enableProxyMenuItem.setVisible(false);
                        }
                    }
                });
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // //监听左上角的返回箭头
        switch (item.getItemId()) {
            case R.id.main_menu_search_articles:
                new MaterialDialog.Builder(this)
                        .title(R.string.search_articles)
                        .inputType(InputType.TYPE_CLASS_TEXT)
                        .inputRange(2, 18)
                        .input("", "", new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                loadSearchedArticles(input.toString());
                            }
                        })
                        .negativeText(android.R.string.cancel)
                        .positiveText(R.string.confirm)
                        .show();
                break;
            case R.id.main_menu_proxy_enable:
            case R.id.main_menu_proxy_disable:
                Intent intent = new Intent(this, ProxyActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_bottom, R.anim.fade_out);
                break;
            case R.id.main_menu_add_feed:
                new MaterialDialog.Builder(MainActivity.this)
                        .title(R.string.input_feed_url)
                        .inputType(InputType.TYPE_TEXT_VARIATION_URI)
                        .inputRange(1, 88)
                        .input(Contract.SCHEMA_HTTPS, null, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NotNull MaterialDialog dialog, CharSequence input) {
                                if(!UriUtils.isHttpOrHttpsUrl(input.toString())){
                                    ToastUtils.show(R.string.invalid_url_hint);
                                }else {
                                    MaterialDialog materialDialog = new MaterialDialog.Builder(MainActivity.this)
                                            .canceledOnTouchOutside(false)
                                            .content(R.string.loading).build();
                                    materialDialog.show();

                                    HttpCall.i().get(input.toString(), new Callback() {
                                        @Override
                                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                            materialDialog.dismiss();
                                            ToastUtils.show(getString(R.string.edit_fail_with_reason, e.getLocalizedMessage()));
                                        }

                                        @Override
                                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                                            materialDialog.dismiss();
                                            if(!response.isSuccessful()){
                                                return;
                                            }

                                            Feed feed = new Feed();
                                            feed.setUid(App.i().getUser().getId());
                                            feed.setId(EncryptUtils.MD5(input.toString()));
                                            feed.setFeedUrl(input.toString());
                                            FeedEntries feedEntries = FeedParserUtils.parseResponseBody(MainActivity.this, feed, response);
                                            if(feedEntries == null){
                                                return;
                                            }
                                            if(!feedEntries.isSuccess()){
                                                ToastUtils.show(feedEntries.getFeed().getLastSyncError());
                                            }else {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        MaterialDialog feedSettingDialog = new MaterialDialog.Builder(MainActivity.this)
                                                                .title(R.string.add_subscription)
                                                                .customView(R.layout.dialog_add_feed, true)
                                                                .negativeText(android.R.string.cancel)
                                                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                                                    @Override
                                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                        // InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                                        // if(imm != null) imm.hideSoftInputFromWindow(categoryNameSpinner.getWindowToken(), 0);
                                                                        dialog.dismiss();
                                                                    }
                                                                })
                                                                .positiveText(R.string.agree)
                                                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                                                    @Override
                                                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                                        // InputMethodManager imm = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                                        // if(imm != null) imm.hideSoftInputFromWindow(categoryNameSpinner.getWindowToken(), 0);
                                                                        EditText feedNameEditText = (EditText) dialog.findViewById(R.id.dialog_feed_name_edittext);
                                                                        feedEntries.getFeed().setTitle(feedNameEditText.getText().toString());
                                                                        feedEntries.getFeed().setSyncInterval(0);
                                                                        CoreDB.i().feedDao().insert(feedEntries.getFeed());
                                                                        List<Article> entries = feedEntries.getArticles();
                                                                        if( entries!= null && entries.size() > 0){
                                                                            CoreDB.i().articleDao().insert(entries);
                                                                        }
                                                                        CoreDB.i().feedCategoryDao().insert(feedEntries.getFeedCategories());

                                                                        // if(feedEntries.getFeedCategories() !=  null){
                                                                        //     for (FeedCategory feedCategory: feedEntries.getFeedCategories()){
                                                                        //         feedCategory.setFeedId(feedEntries.getFeed().getId());
                                                                        //     }
                                                                        //     CoreDB.i().feedCategoryDao().insert(feedEntries.getFeedCategories());
                                                                        // }else {
                                                                        //     FeedCategory feedCategory = new FeedCategory();
                                                                        //     feedCategory.setFeedId(feedEntries.getFeed().getId());
                                                                        //     CoreDB.i().feedCategoryDao().insert(feedEntries.getFeedCategories());
                                                                        // }
                                                                    }
                                                                })
                                                                .show();
                                                        EditText feedUrlEditText = (EditText) feedSettingDialog.findViewById(R.id.dialog_feed_url_edittext);
                                                        feedUrlEditText.setText(input.toString());


                                                        EditText feedNameEditText = (EditText) feedSettingDialog.findViewById(R.id.dialog_feed_name_edittext);
                                                        feedNameEditText.setText(feedEntries.getFeed().getTitle());

                                                        categoryNameSpinner = (Spinner) feedSettingDialog.findViewById(R.id.category_name_editspinner);
                                                        List<Category> categories = CoreDB.i().categoryDao().getAll(App.i().getUser().getId());
                                                        List<String> items = new ArrayList<>();
                                                        items.add(getString(R.string.un_category));
                                                        if(categories != null){
                                                            for (Category category:categories){
                                                                items.add(category.getTitle());
                                                            }
                                                        }
                                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, items);
                                                        categoryNameSpinner.setAdapter(adapter);
                                                        categoryNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                            @Override
                                                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                                position = position - 1;
                                                                if(position >= 0 && categories != null){
                                                                    FeedCategory feedCategory = new FeedCategory();
                                                                    feedCategory.setUid(App.i().getUser().getId());
                                                                    feedCategory.setCategoryId(categories.get(position).getId());
                                                                    List<FeedCategory> feedCategories = new ArrayList<>();
                                                                    feedCategories.add(feedCategory);
                                                                    feedEntries.setFeedCategories(feedCategories);
                                                                }
                                                            }

                                                            @Override
                                                            public void onNothingSelected(AdapterView<?> parent) {
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        })
                        .positiveText(R.string.confirm)
                        .negativeText(android.R.string.cancel)
                        .show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Spinner categoryNameSpinner;


    /**
     * 设置各个视图与颜色属性的关联
     */
    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        ViewGroupSetter articlesHeaderVS = new ViewGroupSetter((ViewGroup) articlesHeaderView);
        articlesHeaderVS.childViewBgColor(R.id.main_header, R.attr.root_view_bg);
        articlesHeaderVS.childViewTextColor(R.id.main_header_title, R.attr.lv_item_desc_color);
        // articlesHeaderVS.childViewBgDrawable(R.id.main_header_eye, R.attr.lv_item_desc_color);

        ViewGroupSetter artListViewSetter = new ViewGroupSetter(articleListView);
        // 绑定ListView的Item View中的news_title视图，在换肤时修改它的text_color属性
        // artListViewSetter.childViewBgColor(R.id.main_slv_item, R.attr.root_view_bg);
        artListViewSetter.childViewTextColor(R.id.main_slv_item_title, R.attr.lv_item_title_color);
        artListViewSetter.childViewTextColor(R.id.main_slv_item_summary, R.attr.lv_item_desc_color);
        artListViewSetter.childViewTextColor(R.id.main_slv_item_author, R.attr.lv_item_info_color);
        artListViewSetter.childViewTextColor(R.id.main_slv_item_time, R.attr.lv_item_info_color);
        // artListViewSetter.childViewBgColor(R.id.main_slv_item_divider, R.attr.lv_item_divider);
        artListViewSetter.childViewBgColor(R.id.main_list_item_surface, R.attr.root_view_bg);
        // artListViewSetter.childViewBgColor(R.id.main_list_item_menu_left, R.attr.root_view_bg);
        // artListViewSetter.childViewBgColor(R.id.main_list_item_menu_right, R.attr.root_view_bg);
        // artListViewSetter.childViewBgColor(R.id.swipe_layout, R.attr.root_view_bg);

        ViewGroupSetter relative = new ViewGroupSetter(tagBottomSheetLayout);
        relative.childViewBgColor(R.id.sheet_tag, R.attr.root_view_bg);
        // relative.childViewBgColor(R.id.sheet_tag_sticky_header,R.attr.root_view_bg);
        relative.childViewBgColor(R.id.main_tag_list_view, R.attr.root_view_bg);
        relative.childViewBgColor(R.id.main_tag_create_category, R.attr.root_view_bg);

        // 绑定ListView的Item View中的news_title视图，在换肤时修改它的text_color属性
        ViewGroupSetter tagListViewSetter = new ViewGroupSetter(categoryListView);
        tagListViewSetter.childViewBgColor(R.id.group_item, R.attr.root_view_bg);  // 这个不能生效！不然反而会影响底色修改
        // tagListViewSetter.childViewTextColor(R.id.group_item_icon, R.attr.tag_slv_item_icon);
        // tagListViewSetter.childViewBgDrawable(R.id.group_item_icon, R.attr.tag_slv_item_icon);
        tagListViewSetter.childViewTextColor(R.id.group_item_title, R.attr.lv_item_title_color);
        tagListViewSetter.childViewTextColor(R.id.group_item_count, R.attr.lv_item_desc_color);

        tagListViewSetter.childViewBgColor(R.id.child_item, R.attr.root_view_bg);  // 这个不生效，反而会影响底色修改
        tagListViewSetter.childViewTextColor(R.id.child_item_title, R.attr.lv_item_title_color);
        tagListViewSetter.childViewTextColor(R.id.child_item_count, R.attr.lv_item_desc_color);

        mColorfulBuilder
                // 设置 toolbar
                .backgroundColor(R.id.main_toolbar, R.attr.topbar_bg)
                // 这里做设置，实质都是直接生成了一个View（根据Activity的findViewById），并直接添加到 colorful 内的 mElements 中。
                .backgroundColor(R.id.main_swipe_refresh, R.attr.root_view_bg)
                //.textColor(R.id.main_toolbar_hint, R.attr.topbar_fg)

                .backgroundColor(R.id.sheet_tag, R.attr.root_view_bg)

                // 设置 bottombar
                .backgroundColor(R.id.main_bottombar, R.attr.bottombar_bg)
                // 设置中屏和底栏之间的分割线
                .backgroundColor(R.id.main_bottombar_divider, R.attr.bottombar_divider)
                .textColor(R.id.main_bottombar_search, R.attr.bottombar_fg)
                .textColor(R.id.main_bottombar_setting, R.attr.bottombar_fg)
                .textColor(R.id.main_bottombar_tag, R.attr.bottombar_fg)
                .textColor(R.id.main_bottombar_refresh_articles, R.attr.bottombar_fg)

                // 设置 listview 背景色
                // 这里做设置，实质是将View（根据Activity的findViewById），并直接添加到 colorful 内的 mElements 中。
                .setter(relative)
                .setter(articlesHeaderVS)
                .setter(artListViewSetter)
                .setter(tagListViewSetter);
        return mColorfulBuilder;
    }
}
