package me.wizos.loread.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.ArraySet;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.elvishew.xlog.XLog;
import com.hjq.toast.ToastUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.bean.SearchFeed;
import me.wizos.loread.bean.feedly.CategoryItem;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.bean.rssfinder.FindResponse;
import me.wizos.loread.bean.rssfinder.RSSFinderFeed;
import me.wizos.loread.bean.search.SearchFeedItem;
import me.wizos.loread.bean.search.SearchFeeds;
import me.wizos.loread.config.Test;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.extractor.RSSSeeker;
import me.wizos.loread.network.HttpClientManager;
import me.wizos.loread.network.api.FeedlyApi;
import me.wizos.loread.network.api.FeedlyService;
import me.wizos.loread.network.api.RSSFinderService;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.TimeUtils;
import me.wizos.loread.view.SwipeRefreshLayoutS;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.colorful.setter.ViewGroupSetter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SearchActivity extends BaseActivity {
    protected static final String TAG = "SearchActivity";
    private EditText searchView;
    private ListView listView;
    private SwipeRefreshLayoutS swipeRefreshLayoutS;

    private ArrayList<SearchFeed> searchFeedItems = new ArrayList<>();
    private SearchListViewAdapter listViewAdapter;
    private View resultCountHeaderView;
    // private View wordHeaderView;
    private TextView feedCountView;
    private RequestOptions options;
    private RSSSeeker rssSeeker;
    private int callCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initToolbar();
        initView();
        options = new RequestOptions()
                .placeholder(R.mipmap.ic_launcher)
                //.circleCrop()
                .centerCrop();
    }


    private void initView() {
        swipeRefreshLayoutS = findViewById(R.id.search_swipe_refresh);
        searchView = findViewById(R.id.search_toolbar_edittext);
        listView = findViewById(R.id.search_list_view);

        swipeRefreshLayoutS.setEnabled(false);
        // headerView
        // 热门搜索，最近搜索
        // 本地源、本地文章
        // 云端源、云端文章
        // 订阅
        // 特定站点：微博，微信，知乎，BiliBili，Ins，G+，Facebook，关键词订阅，

        // wordHeaderView = getLayoutInflater().inflate(R.layout.activity_search_list_header_shortcut_button, listView, false);
        resultCountHeaderView = getLayoutInflater().inflate(R.layout.activity_search_list_header_result_count, listView, false);
        feedCountView = resultCountHeaderView.findViewById(R.id.search_feeds_result_count);
        listViewAdapter = new SearchListViewAdapter(SearchActivity.this, searchFeedItems);
        listView.setAdapter(listViewAdapter);

        searchView.requestFocus();
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // XLog.i("输入前确认执行该方法", "开始输入：" + s .toString() + startAnimation + "  " + after + "  " + count);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    // listView.removeHeaderView(wordHeaderView);
                    listView.removeHeaderView(resultCountHeaderView);
                    swipeRefreshLayoutS.setRefreshing(false);
                    listViewAdapter.clear();
                } else if (listView.getHeaderViewsCount() == 0) {
                    XLog.i("直接变成搜索该关键词");
                    // listView.addHeaderView(wordHeaderView);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // XLog.i("输入结束执行该方法", "输入结束");
            }
        });

        searchView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    onClickSearchFeeds(null);
                }
                return false;
            }
        });
    }

    private Integer[] selectIndices;

    private CustomViewHolder cvh;
    public static class CustomViewHolder {
        TextView feedTitle;
        TextView feedSummary;
        TextView feedUrl;
        TextView feedSubsVelocity;
        TextView feedLastUpdated;
        CheckBox feedSubState;
        ImageView feedIcon;
    }

    class SearchListViewAdapter extends ArrayAdapter<SearchFeed> {
        private List<SearchFeed> searchFeeds;
        private Set<String> set = new ArraySet<>();

        public SearchListViewAdapter(Context context, List<SearchFeed> feedList) {
            super(context, 0, feedList);
            this.searchFeeds = feedList;
        }

        public void addItems(List<SearchFeed> items){
            for (SearchFeed item: items) {
                if(set.add(item.getFeedUrl())){
                    searchFeeds.add(item);
                }
            }
        }

        @Deprecated
        @Override
        public void addAll(Collection collection) {
            super.addAll(collection);
        }

        public void clear(){
            searchFeeds.clear();
        }
        @Override
        public int getCount() {
            return searchFeeds.size();
        }

        @Override
        public SearchFeed getItem(int position) {
            return searchFeeds.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("SetTextI18n")
        @NotNull
        @Override
        public View getView(final int position, View convertView, @NotNull final ViewGroup parent) {
            final SearchFeed item = this.getItem(position);
            if (convertView == null) {
                cvh = new CustomViewHolder();
                convertView = LayoutInflater.from(SearchActivity.this).inflate(R.layout.activity_search_list_item_feed, null);
                cvh.feedIcon = convertView.findViewById(R.id.search_list_item_icon);
                cvh.feedTitle = convertView.findViewById(R.id.search_list_item_title);
                cvh.feedSummary = convertView.findViewById(R.id.search_list_item_summary);
                cvh.feedUrl = convertView.findViewById(R.id.search_list_item_feed_url);
                cvh.feedSubsVelocity = convertView.findViewById(R.id.search_list_item_sub_velocity);
                cvh.feedLastUpdated = convertView.findViewById(R.id.search_list_item_last_updated);
                cvh.feedSubState = convertView.findViewById(R.id.search_list_item_sub_state);
                convertView.setTag(cvh);
            } else {
                cvh = (CustomViewHolder) convertView.getTag();
            }
            cvh.feedTitle.setText(item.getTitle());
            if (!TextUtils.isEmpty(item.getDescription())) {
                cvh.feedSummary.setVisibility(View.VISIBLE);
                cvh.feedSummary.setText(item.getDescription());
            } else {
                cvh.feedSummary.setVisibility(View.GONE);
                cvh.feedSummary.setText("");
            }
            // XLog.i("当前view是：" + position +"  " + convertView.getId() + "");
            Glide.with(SearchActivity.this).load(item.getIconUrl()).apply(options).into(cvh.feedIcon);

            cvh.feedUrl.setText(item.getFeedUrl());
            // getResources().getQuantityString(R.plurals.search_result_followers, searchFeedItem.getSubscribers(), searchFeedItem.getSubscribers(), searchFeedItem.getVelocity() )
            if( item.getSubscribers() != 0 || item.getVelocity() != 0){
                cvh.feedSubsVelocity.setVisibility(View.VISIBLE);
                cvh.feedSubsVelocity.setText( getString(R.string.search_result_meta, item.getSubscribers(), String.format(Locale.getDefault(), "%.2f",item.getVelocity())) ); //  + getString(R.string.search_result_articles, searchFeedItem.getVelocity())
            }else {
                cvh.feedSubsVelocity.setVisibility(View.GONE);
            }

            if (item.getLastUpdated() != 0) {
                cvh.feedLastUpdated.setVisibility(View.VISIBLE);
                cvh.feedLastUpdated.setText(getString(R.string.search_result_last_update_time, TimeUtils.format(item.getLastUpdated(), "yyyy-MM-dd")));
            } else {
                cvh.feedLastUpdated.setVisibility(View.GONE);
                cvh.feedLastUpdated.setText("");
            }

            cvh.feedSubState.setChecked( (CoreDB.i().feedDao().getByFeedUrl(App.i().getUser().getId(), item.getFeedUrl()) != null) );
            cvh.feedSubState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                    if(isChecked){
                        view.setChecked(false);
                        final List<Category> categoryList = CoreDB.i().categoryDao().getAll(App.i().getUser().getId());
                        String[] categoryTitleArray = new String[categoryList.size()];
                        for (int i = 0, size = categoryList.size(); i < size; i++) {
                            categoryTitleArray[i] = categoryList.get(i).getTitle();
                        }
                        final EditFeed editFeed = new EditFeed();
                        editFeed.setId(Contract.SCHEMA_FEED + item.getFeedUrl());
                        new MaterialDialog.Builder(SearchActivity.this)
                                .title(getString(R.string.select_category))
                                .items(categoryTitleArray)
                                .alwaysCallMultiChoiceCallback()
                                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                        SearchActivity.this.selectIndices = which;
                                        for (int i : which) {
                                            XLog.i("点选了：" + i);
                                        }
                                        return true;
                                    }
                                })
                                .positiveText(R.string.confirm)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        ArrayList<CategoryItem> categoryItemList = new ArrayList<>();
                                        for (Integer selectIndex : selectIndices) {
                                            CategoryItem categoryItem = new CategoryItem();
                                            categoryItem.setId(categoryList.get(selectIndex).getId());
                                            categoryItemList.add(categoryItem);
                                        }
                                        editFeed.setCategoryItems(categoryItemList);
                                        view.setEnabled(false);
                                        App.i().getApi().addFeed(editFeed, new CallbackX() {
                                            @Override
                                            public void onSuccess(Object result) {
                                                XLog.i("添加成功");
                                                ToastUtils.show(R.string.subscribe_success);
                                                view.setEnabled(true);
                                                view.setEnabled(true);
                                            }

                                            @Override
                                            public void onFailure(Object error) {
                                                ToastUtils.show(getString(R.string.subscribe_fail, error));
                                                view.setEnabled(true);
                                            }
                                        });
                                    }
                                })
                                .show();
                    }else {
                        view.setEnabled(false);
                        view.setChecked(true);
                        Feed feed = CoreDB.i().feedDao().getByFeedUrl(App.i().getUser().getId(), item.getFeedUrl());
                        if(feed == null){
                            return;
                        }
                        App.i().getApi().unsubscribeFeed(feed.getId(), new CallbackX() {
                            @Override
                            public void onSuccess(Object result) {
                                CoreDB.i().feedDao().deleteByFeedUrl(App.i().getUser().getId(), item.getFeedUrl());
                                view.setEnabled(true);
                                view.setChecked(false);
                            }

                            @Override
                            public void onFailure(Object error) {
                                ToastUtils.show(getString(R.string.unsubscribe_failed,error));
                                view.setEnabled(true);
                            }
                        });
                    }
                }
            });
            // cvh.feedSubState.setOnClickListener(new View.OnClickListener() {
            //     @Override
            //     public void onClick(final View view) {
            //         Feed feed = CoreDB.i().feedDao().getByFeedUrl(App.i().getUser().getId(), item.getFeedUrl());
            //         if ( feed != null) {
            //             view.setClickable(false); // 防止重复点击
            //             App.i().getApi().unsubscribeFeed(feed.getId(), new CallbackX() {
            //                 @Override
            //                 public void onSuccess(Object result) {
            //                     CoreDB.i().feedDao().deleteById(App.i().getUser().getId(), item.getFeedUrl());
            //                     ((CheckBox) view).setChecked(false);
            //                     view.setClickable(true);
            //                 }
            //
            //                 @Override
            //                 public void onFailure(Object error) {
            //                     ToastUtils.show(getString(R.string.unsubscribe_failed,error));
            //                     view.setClickable(true);
            //                 }
            //             });
            //         } else {
            //             // cvh.feedSubState.setText(R.string.font_add);
            //             // showSelectFolder(view, item.getFeedUrl());
            //             final List<Category> categoryList = CoreDB.i().categoryDao().getAll(App.i().getUser().getId());
            //             String[] categoryTitleArray = new String[categoryList.size()];
            //             for (int i = 0, size = categoryList.size(); i < size; i++) {
            //                 categoryTitleArray[i] = categoryList.get(i).getTitle();
            //             }
            //             final EditFeed editFeed = new EditFeed();
            //             editFeed.setId(Contract.SCHEMA_FEED + item.getFeedUrl());
            //             new MaterialDialog.Builder(SearchActivity.this)
            //                     .title(getString(R.string.select_category))
            //                     .items(categoryTitleArray)
            //                     .alwaysCallMultiChoiceCallback()
            //                     .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
            //                         @Override
            //                         public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
            //                             SearchActivity.this.selectIndices = which;
            //                             for (int i : which) {
            //                                 XLog.i("点选了：" + i);
            //                             }
            //                             return true;
            //                         }
            //                     })
            //                     .positiveText(R.string.confirm)
            //                     .onPositive(new MaterialDialog.SingleButtonCallback() {
            //                         @Override
            //                         public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
            //                             ArrayList<CategoryItem> categoryItemList = new ArrayList<>();
            //                             for (Integer selectIndex : selectIndices) {
            //                                 CategoryItem categoryItem = new CategoryItem();
            //                                 categoryItem.setId(categoryList.get(selectIndex).getId());
            //                                 categoryItemList.add(categoryItem);
            //                             }
            //                             editFeed.setCategoryItems(categoryItemList);
            //                             view.setClickable(false);
            //                             App.i().getApi().addFeed(editFeed, new CallbackX() {
            //                                 @Override
            //                                 public void onSuccess(Object result) {
            //                                     XLog.i("添加成功");
            //                                     ToastUtils.show(R.string.subscribe_success);
            //                                     ((CheckBox) view).setChecked(true);
            //                                     view.setClickable(true);
            //                                 }
            //
            //                                 @Override
            //                                 public void onFailure(Object error) {
            //                                     ToastUtils.show(getString(R.string.subscribe_fail, error));
            //                                     view.setClickable(true);
            //                                 }
            //                             });
            //                         }
            //                     }).show();
            //         }
            //     }
            // });
            return convertView;
        }
    }


    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // setDisplayShowHomeEnabled(true)   //使左上角图标是否显示，如果设成false，则没有程序图标，仅仅就个标题，否则，显示应用程序图标，对应id为android.R.id.home，对应ActionBar.DISPLAY_SHOW_HOME
        // setDisplayShowCustomEnabled(true)  // 使自定义的普通View能在title栏显示，即actionBar.setCustomView能起作用，对应ActionBar.DISPLAY_SHOW_CUSTOM
    }

    public void onClickSearchFeeds(View view) {
        String keyword = searchView.getText().toString();
        if (TextUtils.isEmpty(keyword)) {
            ToastUtils.show(R.string.please_input_keyword);
            return;
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null) imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);

        searchView.clearFocus();
        swipeRefreshLayoutS.setRefreshing(true);
        listView.removeHeaderView(resultCountHeaderView);
        listViewAdapter.clear();
        listViewAdapter.notifyDataSetChanged();
        callCount = 0;
        if(rssSeeker != null){
            rssSeeker.destroy();
        }

        if(keyword.toLowerCase().startsWith(Contract.SCHEMA_HTTP) || keyword.toLowerCase().startsWith(Contract.SCHEMA_HTTPS)){
            rssSeeker = new RSSSeeker(keyword, new RSSSeeker.Listener() {
                @Override
                public void onResponse(ArrayMap<String, String> rssMap) {
                    XLog.i("RSS Seeker 成功"  );
                    List<SearchFeed> list = new ArrayList<>(rssMap.size());
                    for (Map.Entry<String,String> entry:rssMap.entrySet()) {
                        list.add(new SearchFeed(entry.getKey(), entry.getValue()));
                    }
                    successUpdateResults(list);
                }

                @Override
                public void onFailure(String msg) {
                    XLog.i("RSS Seeker 失败：" + msg );
                    failureUpdateResults();
                }
            });
            rssSeeker.start();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(RSSFinderService.BASE_URL) // 设置网络请求的Url地址, 必须以/结尾
                    .addConverterFactory(GsonConverterFactory.create())  // 设置数据解析器
                    .client(HttpClientManager.i().searchClient())
                    .build();
            RSSFinderService rssFinderService = retrofit.create(RSSFinderService.class);

            // String user = TestConfig.i().rssFinderUser;
            Call<FindResponse> callSearchFeeds = rssFinderService.find(searchView.getText().toString(), Test.i().rssFinderUser);
            callSearchFeeds.enqueue(new Callback<FindResponse>() {
                @Override
                public void onResponse(@NotNull Call<FindResponse> call, @NotNull Response<FindResponse> response) {
                    XLog.i("RSS Finder 成功 " + call.isCanceled());
                    if(call.isCanceled()){
                        return;
                    }
                    FindResponse findResponse = response.body();
                    if (findResponse != null && findResponse.isOK()) {
                        List<RSSFinderFeed> items = findResponse.getFeeds();
                        List<SearchFeed> feeds = new ArrayList<>();
                        for (RSSFinderFeed item: items){
                            feeds.add(item.convert2SearchFeed());
                        }
                        successUpdateResults(feeds);
                    }else {
                        failureUpdateResults();
                    }
                }

                @Override
                public void onFailure(@NotNull Call<FindResponse> call, @NotNull Throwable t) {
                    XLog.i("RSS Finder 失败：" + call.isCanceled() + t);
                    failureUpdateResults();
                }
            });
        }


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(FeedlyApi.OFFICIAL_BASE_URL + "/") // 设置网络请求的Url地址, 必须以/结尾
                .addConverterFactory(GsonConverterFactory.create())  // 设置数据解析器
                .client(HttpClientManager.i().searchClient())
                .build();
        FeedlyService feedlyService = retrofit.create(FeedlyService.class);

        //对 发送请求 进行封装
        Call<SearchFeeds> callSearchFeeds = feedlyService.getSearchFeeds(searchView.getText().toString(), 100);
        callSearchFeeds.enqueue(new retrofit2.Callback<SearchFeeds>() {
            @Override
            public void onResponse(@NotNull Call<SearchFeeds> call, @NotNull Response<SearchFeeds> response) {
                XLog.i("feedly 成功" + call.isCanceled());
                if(call.isCanceled()){
                    return;
                }
                SearchFeeds searchResult = response.body();
                if (searchResult != null && searchResult.getResults() != null) {
                    List<SearchFeedItem> items = searchResult.getResults();
                    List<SearchFeed> feeds = new ArrayList<>();
                    for (SearchFeedItem item: items){
                        feeds.add(item.convert2SearchFeed());
                    }
                    successUpdateResults(feeds);
                }else {
                    failureUpdateResults();
                }
            }

            @Override
            public void onFailure(@NotNull Call<SearchFeeds> call, @NotNull Throwable t) {
                XLog.i("feedly失败：" + call.isCanceled() + " , " + t);
                failureUpdateResults();
            }
        });
    }

    private void failureUpdateResults(){
        callCount++;
        XLog.i("failureUpdateResults: " + callCount);
        if( callCount == 3){
            swipeRefreshLayoutS.post(() -> {
                ToastUtils.show(App.i().getString(R.string.failed_please_try_again));
                swipeRefreshLayoutS.setRefreshing(false);
            });
        }
    }
    private void successUpdateResults(List<SearchFeed> list){
        callCount++;
        XLog.d("successUpdateResults: " + callCount);
        listViewAdapter.addItems(list);
        if( callCount == 3){
            swipeRefreshLayoutS.post(() -> swipeRefreshLayoutS.setRefreshing(false));
        }
        swipeRefreshLayoutS.post(new Runnable() {
            @Override
            public void run() {
                listViewAdapter.notifyDataSetChanged();
                feedCountView.setText(getString(R.string.search_cloudy_feeds_result_count, listViewAdapter.getCount()));
                if(listView.getHeaderViewsCount() == 0){
                    listView.addHeaderView(resultCountHeaderView);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if(rssSeeker != null){
            rssSeeker.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        ViewGroupSetter artListViewSetter = new ViewGroupSetter(listView);
        // 绑定ListView的Item View中的news_title视图，在换肤时修改它的text_color属性
        artListViewSetter.childViewTextColor(R.id.search_list_item_title, R.attr.lv_item_title_color);
        artListViewSetter.childViewTextColor(R.id.search_list_item_summary, R.attr.lv_item_desc_color);
        artListViewSetter.childViewTextColor(R.id.search_list_item_feed_url, R.attr.lv_item_desc_color);
        // artListViewSetter.childViewTextColor(R.id.search_list_item_sub_state, R.attr.lv_item_title_color);
        artListViewSetter.childViewBgDrawable(R.id.search_list_item_sub_state, R.attr.lv_item_title_color);

        artListViewSetter.childViewTextColor(R.id.search_intent_feeds, R.attr.lv_item_desc_color);

        artListViewSetter.childViewTextColor(R.id.search_list_item_sub_velocity, R.attr.lv_item_info_color);
        artListViewSetter.childViewTextColor(R.id.search_list_item_last_updated, R.attr.lv_item_info_color);

        mColorfulBuilder
                .backgroundColor(R.id.search_root, R.attr.root_view_bg)
                // 设置 toolbar
                .backgroundColor(R.id.search_toolbar, R.attr.topbar_bg);
        return mColorfulBuilder;
    }
}
