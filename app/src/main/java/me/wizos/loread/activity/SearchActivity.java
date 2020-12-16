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
import java.util.Map;
import java.util.Set;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.bean.feedly.CategoryItem;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.bean.search.SearchFeedItem;
import me.wizos.loread.bean.search.SearchFeeds;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.extractor.RSSFinder;
import me.wizos.loread.network.HttpClientManager;
import me.wizos.loread.network.api.FeedlyApi;
import me.wizos.loread.network.api.FeedlyService;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.TimeUtil;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.SwipeRefreshLayoutS;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.colorful.setter.ViewGroupSetter;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class SearchActivity extends BaseActivity {
    protected static final String TAG = "SearchActivity";
    private EditText searchView;
    private ListView listView;
    private SwipeRefreshLayoutS swipeRefreshLayoutS;

    private ArrayList<SearchFeedItem> searchFeedItems = new ArrayList<>();
    private SearchListViewAdapter listViewAdapter;
    private View resultCountHeaderView;
    // private View wordHeaderView;
    private TextView feedCountView;
    private RequestOptions options;
    private RSSFinder rssFinder;
    private int CallCount = 0;

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

    public void showSelectFolder(final View view, final String feedId) {
        final List<Category> categoryList = CoreDB.i().categoryDao().getAll(App.i().getUser().getId());
        String[] categoryTitleArray = new String[categoryList.size()];
        for (int i = 0, size = categoryList.size(); i < size; i++) {
            categoryTitleArray[i] = categoryList.get(i).getTitle();
        }
        final EditFeed editFeed = new EditFeed();
        editFeed.setId(feedId);
        new MaterialDialog.Builder(this)
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
                        view.setClickable(false);
                        App.i().getApi().addFeed(editFeed, new CallbackX() {
                            @Override
                            public void onSuccess(Object result) {
                                XLog.i("添加成功");
                                ((IconFontView) view).setText(R.string.font_tick);
                                ToastUtils.show(R.string.subscribe_success);
                                view.setClickable(true);
                            }

                            @Override
                            public void onFailure(Object error) {
                                ToastUtils.show(getString(R.string.subscribe_fail, (String)error));
                                view.setClickable(true);
                            }
                        });
                    }
                }).show();
    }


    private CustomViewHolder cvh;
    public static class CustomViewHolder {
        TextView feedTitle;
        TextView feedSummary;
        TextView feedUrl;
        TextView feedSubsVelocity;
        TextView feedLastUpdated;
        IconFontView feedSubState;
        ImageView feedIcon;
    }

    class SearchListViewAdapter extends ArrayAdapter<SearchFeedItem> {
        private List<SearchFeedItem> searchFeedItems;
        private Set<String> set = new ArraySet<>();

        public SearchListViewAdapter(Context context, List<SearchFeedItem> feedList) {
            super(context, 0, feedList);
            this.searchFeedItems = feedList;
        }

        public void addItems(List<SearchFeedItem> items){
            for (SearchFeedItem item: items) {
                if(set.add(item.getFeedId())){
                    searchFeedItems.add(item);
                }
            }
        }

        @Deprecated
        @Override
        public void addAll(Collection collection) {
            super.addAll(collection);
        }

        public void clear(){
            searchFeedItems.clear();
        }
        @Override
        public int getCount() {
            return searchFeedItems.size();
        }

        @Override
        public SearchFeedItem getItem(int position) {
            return searchFeedItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("SetTextI18n")
        @NotNull
        @Override
        public View getView(final int position, View convertView, @NotNull final ViewGroup parent) {
            final SearchFeedItem searchFeedItem = this.getItem(position);
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
            cvh.feedTitle.setText(searchFeedItem.getTitle());
            if (!TextUtils.isEmpty(searchFeedItem.getDescription())) {
                cvh.feedSummary.setVisibility(View.VISIBLE);
                cvh.feedSummary.setText(searchFeedItem.getDescription());
            } else {
                cvh.feedSummary.setVisibility(View.GONE);
                cvh.feedSummary.setText("");
            }
            // XLog.i("当前view是：" + position +"  " + convertView.getId() + "");
            // Glide.with(SearchActivity.this).load(searchFeedItem.getVisualUrl()).centerCrop().into(cvh.feedIcon);
            Glide.with(SearchActivity.this).load(searchFeedItem.getVisualUrl()).apply(options).into(cvh.feedIcon);

            cvh.feedUrl.setText(searchFeedItem.getFeedId().replaceFirst(Contract.SCHEMA_FEED, ""));
            // getResources().getQuantityString(R.plurals.search_result_followers, searchFeedItem.getSubscribers(), searchFeedItem.getSubscribers(), searchFeedItem.getVelocity() )
            cvh.feedSubsVelocity.setText( getString(R.string.search_result_meta, searchFeedItem.getSubscribers(), String.format("%.2f",searchFeedItem.getVelocity())) ); //  + getString(R.string.search_result_articles, searchFeedItem.getVelocity())
            if (searchFeedItem.getLastUpdated() != 0) {
                cvh.feedLastUpdated.setText(getString(R.string.search_result_last_update_time, TimeUtil.format(searchFeedItem.getLastUpdated(), "yyyy-MM-dd")));
            } else {
                cvh.feedLastUpdated.setText("");
            }
            if (CoreDB.i().feedDao().getById(App.i().getUser().getId(), searchFeedItem.getFeedId()) != null) {
                cvh.feedSubState.setText(R.string.font_tick);
            } else {
                cvh.feedSubState.setText(R.string.font_add);
            }
            cvh.feedSubState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if (CoreDB.i().feedDao().getById(App.i().getUser().getId(), searchFeedItem.getFeedId()) != null) {
                        view.setClickable(false); // 防止重复点击
                        cvh.feedSubState.setText(R.string.font_tick);
                        App.i().getApi().unsubscribeFeed(searchFeedItem.getFeedId(), new CallbackX() {
                            @Override
                            public void onSuccess(Object result) {
                                CoreDB.i().feedDao().deleteById(App.i().getUser().getId(), searchFeedItem.getFeedId());
                                ((IconFontView) view).setText(R.string.font_add);
                                view.setClickable(true);
                            }

                            @Override
                            public void onFailure(Object error) {
                                ToastUtils.show(getString(R.string.unsubscribe_failed,error));
                                view.setClickable(true);
                            }
                        });
                    } else {
                        cvh.feedSubState.setText(R.string.font_add);
                        showSelectFolder(view, searchFeedItem.getFeedId());
                    }
                }
            });
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
        // listView.setEnabled(false);
        // listView.removeHeaderView(wordHeaderView);
        listView.removeHeaderView(resultCountHeaderView);
        listViewAdapter.clear();
        listViewAdapter.notifyDataSetChanged();
        CallCount = 0;
        if(rssFinder != null){
            rssFinder.cancel();
        }

        if(keyword.toLowerCase().startsWith(Contract.SCHEMA_HTTP) || keyword.toLowerCase().startsWith(Contract.SCHEMA_HTTPS)){
            rssFinder = new RSSFinder(keyword, new RSSFinder.Listener() {
                @Override
                public void onResponse(ArrayMap<String, String> rssMap) {
                    XLog.d("RSS Finder 成功"  );
                    List<SearchFeedItem> list = new ArrayList<>(rssMap.size());
                    for (Map.Entry<String,String> entry:rssMap.entrySet()) {
                        list.add(new SearchFeedItem(Contract.SCHEMA_FEED + entry.getKey(), entry.getValue()));
                    }
                    successUpdateResults(list);
                }

                @Override
                public void onFailure(String msg) {
                    XLog.d("RSS Finder 失败：" + msg );
                    failureUpdateResults();
                }
            });
            rssFinder.start();
        }


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(FeedlyApi.OFFICIAL_BASE_URL + "/") // 设置网络请求的Url地址, 必须以/结尾
                .addConverterFactory(GsonConverterFactory.create())  // 设置数据解析器
                .client(HttpClientManager.i().simpleClient())
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
                    successUpdateResults(searchResult.getResults());
                }else {
                    failureUpdateResults();
                }
            }

            @Override
            public void onFailure(@NotNull Call<SearchFeeds> call, @NotNull Throwable t) {
                XLog.i("feedly失败：" + call.isCanceled() + t);
                if(call.isCanceled()){
                    return;
                }
                failureUpdateResults();
            }
        });
    }

    private void failureUpdateResults(){
        CallCount++;
        XLog.d("failureUpdateResults: " + CallCount);
        if( CallCount == 2){
            swipeRefreshLayoutS.post(() -> {
                ToastUtils.show(App.i().getString(R.string.failed_please_try_again));
                swipeRefreshLayoutS.setRefreshing(false);
            });
        }
    }
    private void successUpdateResults(List<SearchFeedItem> list){
        CallCount++;
        XLog.d("successUpdateResults: " + CallCount);
        listViewAdapter.addItems(list);
        if( CallCount == 2){
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
        if(rssFinder != null){
            rssFinder.cancel();
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
        artListViewSetter.childViewTextColor(R.id.search_list_item_sub_state, R.attr.lv_item_title_color);

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
