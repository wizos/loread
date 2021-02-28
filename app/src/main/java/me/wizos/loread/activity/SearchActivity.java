package me.wizos.loread.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
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
import com.king.zxing.CameraScan;
import com.king.zxing.CaptureActivity;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.bean.FeedEntries;
import me.wizos.loread.bean.SearchFeed;
import me.wizos.loread.bean.rssfinder.RSSFinderFeed;
import me.wizos.loread.bean.rssfinder.RSSFinderResponse;
import me.wizos.loread.bean.search.SearchFeedItem;
import me.wizos.loread.bean.search.SearchFeeds;
import me.wizos.loread.config.Test;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.FeedCategory;
import me.wizos.loread.extractor.RSSSeeker;
import me.wizos.loread.network.HttpClientManager;
import me.wizos.loread.network.api.FeedlyApi;
import me.wizos.loread.network.api.FeedlyService;
import me.wizos.loread.network.api.RSSFinderService;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.Base64Utils;
import me.wizos.loread.utils.Converter;
import me.wizos.loread.utils.EncryptUtils;
import me.wizos.loread.utils.FeedParserUtils;
import me.wizos.loread.utils.HttpCall;
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
    private EditText searchValueEditText;
    private ListView listView;
    private SwipeRefreshLayoutS swipeRefreshLayoutS;

    private ArrayList<SearchFeed> searchFeedItems = new ArrayList<>();
    private SearchListViewAdapter listViewAdapter;
    private View resultCountHeaderView;
    private TextView feedCountView;
    private RequestOptions options;
    private RSSSeeker rssSeeker;
    private int callCount = 0;
    private static final int CALL_SIZE = 4;

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
        searchValueEditText = findViewById(R.id.search_toolbar_edittext);
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

        searchValueEditText.requestFocus();
        searchValueEditText.addTextChangedListener(new TextWatcher() {
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

        searchValueEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    onClickSearchFeeds(null);
                }
                return false;
            }
        });
    }

    public void onClickSearchFeeds(View view) {
        String keyword = searchValueEditText.getText().toString();
        if (TextUtils.isEmpty(keyword)) {
            ToastUtils.show(R.string.enter_keyword_or_url);
            return;
        }
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null) imm.hideSoftInputFromWindow(searchValueEditText.getWindowToken(), 0);

        searchValueEditText.clearFocus();
        swipeRefreshLayoutS.setRefreshing(true);
        listView.removeHeaderView(resultCountHeaderView);
        listViewAdapter.clear();
        listViewAdapter.notifyDataSetChanged();
        callCount = 0;
        if(rssSeeker != null){
            rssSeeker.destroy();
        }

        if(keyword.toLowerCase().startsWith(Contract.SCHEMA_HTTP) || keyword.toLowerCase().startsWith(Contract.SCHEMA_HTTPS)){
            // 1.检查是否为 RSS 网址
            checkRSSURL(keyword);

            // 2.检查该网页内是否有 RSS 声明
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

            // 3.通过 api.wizos.me 获取该网址的 RSS 远程规则
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(RSSFinderService.BASE_URL) // 设置网络请求的Url地址, 必须以/结尾
                    .addConverterFactory(GsonConverterFactory.create())  // 设置数据解析器
                    .client(HttpClientManager.i().searchClient())
                    .build();
            RSSFinderService rssFinderService = retrofit.create(RSSFinderService.class);
            Call<RSSFinderResponse> callSearchFeeds = rssFinderService.find(Base64Utils.getInstance().encode(searchValueEditText.getText().toString()), Test.i().rssFinderUser);
            callSearchFeeds.enqueue(new Callback<RSSFinderResponse>() {
                @Override
                public void onResponse(@NotNull Call<RSSFinderResponse> call, @NotNull Response<RSSFinderResponse> response) {
                    XLog.i("RSS Finder 成功 " + call.isCanceled());
                    if(call.isCanceled()){
                        return;
                    }
                    RSSFinderResponse RSSFinderResponse = response.body();
                    if (RSSFinderResponse != null && RSSFinderResponse.isOK()) {
                        List<RSSFinderFeed> remoteFeedList = RSSFinderResponse.getFeeds();
                        List<SearchFeed> feeds = new ArrayList<>();
                        for (RSSFinderFeed feed : remoteFeedList){
                            feeds.add(Converter.from(feed));
                        }
                        successUpdateResults(feeds);
                    }else {
                        failureUpdateResults();
                    }
                }

                @Override
                public void onFailure(@NotNull Call<RSSFinderResponse> call, @NotNull Throwable t) {
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
        Call<SearchFeeds> callSearchFeeds = feedlyService.getSearchFeeds(searchValueEditText.getText().toString(), 50);
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
                XLog.i("feedly 失败：" + call.isCanceled() + " , " + t);
                failureUpdateResults();
            }
        });
    }

    private void failureUpdateResults(){
        callCount++;
        if(callCount == CALL_SIZE){
            callCount = 0;
            swipeRefreshLayoutS.post(() -> {
                swipeRefreshLayoutS.setRefreshing(false);
                if(listViewAdapter.getCount() == 0){
                    ToastUtils.show(App.i().getString(R.string.no_search_results_found));
                }
            });
        }
        XLog.i("failureUpdateResults: " + callCount);
    }
    private void successUpdateResults(List<SearchFeed> list){
        callCount++;
        if(callCount == CALL_SIZE){
            callCount = 0;
            swipeRefreshLayoutS.post(() -> swipeRefreshLayoutS.setRefreshing(false));
        }
        XLog.d("successUpdateResults: " + callCount);
        listViewAdapter.addItems(list);
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

    private void successUpdateResults(FeedEntries feedEntries){
        callCount++;
        if( callCount == CALL_SIZE){
            swipeRefreshLayoutS.post(() -> swipeRefreshLayoutS.setRefreshing(false));
        }
        XLog.d("successUpdateResults: " + callCount);
        listViewAdapter.addItem(feedEntries);
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

    private void checkRSSURL(String url){
        HttpCall.get(url, new okhttp3.Callback() {
            @Override
            public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
                failureUpdateResults();
            }

            @Override
            public void onResponse(@NotNull okhttp3.Call call, @NotNull okhttp3.Response response) throws IOException {
                if(!response.isSuccessful()){
                    failureUpdateResults();
                    return;
                }

                Feed feed = new Feed();
                feed.setUid(App.i().getUser().getId());
                feed.setId(EncryptUtils.MD5(url));
                feed.setFeedUrl(url);
                FeedEntries feedEntries = FeedParserUtils.parseResponseBody(SearchActivity.this, feed, response.body());
                if(feedEntries == null || !feedEntries.isSuccess()){
                    failureUpdateResults();
                }else {
                    successUpdateResults(feedEntries);
                }
            }
        });
    }





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
        private ArrayMap<String, List<Article>> arrayMap = new ArrayMap<>();

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

        public void addItem(FeedEntries feedEntries){
            if(set.add(feedEntries.getFeed().getFeedUrl())){
                searchFeeds.add(Converter.from(feedEntries.getFeed()));
                arrayMap.put(feedEntries.getFeed().getFeedUrl(), feedEntries.getArticles());
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
            final SearchFeed searchFeed = this.getItem(position);
            CustomViewHolder cvh;
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
            cvh.feedTitle.setText(searchFeed.getTitle());
            if (!TextUtils.isEmpty(searchFeed.getDescription())) {
                cvh.feedSummary.setVisibility(View.VISIBLE);
                cvh.feedSummary.setText(searchFeed.getDescription());
            } else {
                cvh.feedSummary.setVisibility(View.GONE);
                cvh.feedSummary.setText("");
            }
            // XLog.i("当前view是：" + position +"  " + convertView.getId() + "");
            Glide.with(SearchActivity.this).load(searchFeed.getIconUrl()).apply(options).into(cvh.feedIcon);

            cvh.feedUrl.setText(searchFeed.getFeedUrl());
            // getResources().getQuantityString(R.plurals.search_result_followers, searchFeedItem.getSubscribers(), searchFeedItem.getSubscribers(), searchFeedItem.getVelocity() )
            if( searchFeed.getSubscribers() != 0 || searchFeed.getVelocity() != 0){
                cvh.feedSubsVelocity.setVisibility(View.VISIBLE);
                cvh.feedSubsVelocity.setText( getString(R.string.search_result_meta, searchFeed.getSubscribers(), String.format(Locale.getDefault(), "%.2f", searchFeed.getVelocity())) ); //  + getString(R.string.search_result_articles, searchFeedItem.getVelocity())
            }else {
                cvh.feedSubsVelocity.setVisibility(View.GONE);
            }

            if (searchFeed.getLastUpdated() != 0) {
                cvh.feedLastUpdated.setVisibility(View.VISIBLE);
                cvh.feedLastUpdated.setText(getString(R.string.search_result_last_update_time, TimeUtils.format(searchFeed.getLastUpdated(), "yyyy-MM-dd")));
            } else {
                cvh.feedLastUpdated.setVisibility(View.GONE);
                cvh.feedLastUpdated.setText("");
            }

            XLog.i("搜索订阅源：" + App.i().getUser().getId() + " = " +  searchFeed.getFeedUrl());
            cvh.feedSubState.setChecked( (CoreDB.i().feedDao().getByFeedUrl(App.i().getUser().getId(), searchFeed.getFeedUrl()) != null) );
            cvh.feedSubState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // XLog.i("是否勾选：" + cvh.feedSubState.isChecked());
                    if(cvh.feedSubState.isChecked()){
                        // 先将状态重置回未勾选状态，待点击确定后再勾选
                        cvh.feedSubState.setChecked(false);
                        // XLog.i("修改勾选状态");
                        FeedEntries feedEntries = new FeedEntries();
                        feedEntries.setFeed(Converter.from(searchFeed));
                        if(arrayMap.containsKey(searchFeed.getFeedUrl())){
                            feedEntries.setArticles(arrayMap.get(searchFeed.getFeedUrl()));
                        }

                        MaterialDialog feedSettingDialog = new MaterialDialog.Builder(SearchActivity.this)
                                .title(R.string.add_subscription)
                                .customView(R.layout.dialog_add_feed, true)
                                .negativeText(android.R.string.cancel)
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                })
                                .positiveText(R.string.confirm)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        // dialog.dismiss();
                                        view.setEnabled(false);
                                        cvh.feedSubState.setChecked(true);

                                        EditText feedNameEditText = (EditText) dialog.findViewById(R.id.dialog_feed_name_edittext);
                                        feedEntries.getFeed().setTitle(feedNameEditText.getText().toString());

                                        App.i().getApi().addFeed(feedEntries, new CallbackX() {
                                            @Override
                                            public void onSuccess(Object result) {
                                                ToastUtils.show(result.toString());
                                                view.setEnabled(true);
                                                cvh.feedSubState.setChecked(true);
                                            }

                                            @Override
                                            public void onFailure(Object error) {
                                                ToastUtils.show(getString(R.string.subscribe_fail, error));
                                                view.setEnabled(true);
                                                cvh.feedSubState.setChecked(false);
                                            }
                                        });
                                    }
                                })
                                .show();
                        EditText feedUrlEditText = (EditText) feedSettingDialog.findViewById(R.id.dialog_feed_url_edittext);
                        feedUrlEditText.setText(searchFeed.getFeedUrl());

                        EditText feedNameEditText = (EditText) feedSettingDialog.findViewById(R.id.dialog_feed_name_edittext);
                        feedNameEditText.setText(searchFeed.getTitle());

                        Spinner categoryNameSpinner = (Spinner) feedSettingDialog.findViewById(R.id.category_name_editspinner);
                        List<Category> categories = CoreDB.i().categoryDao().getAll(App.i().getUser().getId());
                        List<String> items = new ArrayList<>();
                        items.add(getString(R.string.un_category));
                        if(categories != null){
                            for (Category category:categories){
                                items.add(category.getTitle());
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(SearchActivity.this, android.R.layout.simple_spinner_dropdown_item, items);
                        categoryNameSpinner.setAdapter(adapter);
                        categoryNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                position = position - 1;
                                if(position >= 0 && categories != null){
                                    List<FeedCategory> feedCategories = new ArrayList<>();
                                    FeedCategory feedCategory = new FeedCategory(App.i().getUser().getId(), feedEntries.getFeed().getId(), categories.get(position).getId());
                                    feedCategories.add(feedCategory);
                                    feedEntries.setFeedCategories(feedCategories);
                                }
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });
                    }else {
                        // XLog.i("去勾选");
                        Feed feed = CoreDB.i().feedDao().getByFeedUrl(App.i().getUser().getId(), searchFeed.getFeedUrl());
                        if(feed == null){
                            return;
                        }
                        view.setEnabled(false);
                        App.i().getApi().unsubscribeFeed(feed.getId(), new CallbackX() {
                            @Override
                            public void onSuccess(Object result) {
                                CoreDB.i().feedDao().deleteByFeedUrl(App.i().getUser().getId(), searchFeed.getFeedUrl());
                                view.setEnabled(true);
                                cvh.feedSubState.setChecked(false);
                            }

                            @Override
                            public void onFailure(Object error) {
                                ToastUtils.show(getString(R.string.unsubscribe_failed,error));
                                view.setEnabled(true);
                                cvh.feedSubState.setChecked(true);
                            }
                        });
                    }
                }
            });
            // cvh.feedSubState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            //     @Override
            //     public void onCheckedChanged(CompoundButton view, boolean isChecked) {
            //
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


    public static final int REQUEST_CODE_SCAN = 1;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // //监听左上角的返回箭头
        switch (item.getItemId()) {
            case R.id.search_menu_qr:
                //跳转的默认扫码界面
                startActivityForResult(new Intent(this, CaptureActivity.class), REQUEST_CODE_SCAN);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(resultCode == RESULT_OK && intent!=null){
            switch (requestCode){
                case REQUEST_CODE_SCAN:
                    String result = CameraScan.parseScanResult(intent);
                    if(searchValueEditText != null){
                        searchValueEditText.setText(result);
                        onClickSearchFeeds(null);
                    };
                    break;
            }
        }
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
