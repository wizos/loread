package me.wizos.loread.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.socks.library.KLog;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.search.FeedlyFeed;
import me.wizos.loread.bean.search.FeedlyFeedsSearchResult;
import me.wizos.loread.bean.search.QuickAdd;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.net.Api;
import me.wizos.loread.net.DataApi;
import me.wizos.loread.net.SearchApi;
import me.wizos.loread.utils.TimeUtil;
import me.wizos.loread.utils.ToastUtil;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.SwipeRefreshLayoutS;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.colorful.setter.ViewGroupSetter;


public class SearchActivity extends BaseActivity {
    protected static final String TAG = "SearchActivity";
    private EditText searchView;
    private ListView listView;
    private SwipeRefreshLayoutS swipeRefreshLayoutS;

    private ArrayList<FeedlyFeed> feedlyFeeds = new ArrayList<>();
    private SearchListViewAdapter listViewAdapter;
    private View wordHeaderView, resultCountHeaderView;
    private RequestOptions options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        initToolbar();
        initView();
        options = new RequestOptions()
                .placeholder(R.mipmap.ic_launcher)
                .circleCrop()
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

        wordHeaderView = getLayoutInflater().inflate(R.layout.activity_search_list_header_word, listView, false);
        resultCountHeaderView = getLayoutInflater().inflate(R.layout.activity_search_list_header_result_count, listView, false);

        listViewAdapter = new SearchListViewAdapter(SearchActivity.this, feedlyFeeds);
        listView.setAdapter(listViewAdapter);

        searchView.requestFocus();
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                KLog.e("输入前确认执行该方法", "开始输入：" + s .toString() + start + "  " + after + "  " + count);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    listView.removeHeaderView(wordHeaderView);
                    listView.removeHeaderView(resultCountHeaderView);
                    swipeRefreshLayoutS.setRefreshing(false);
                    feedlyFeeds.clear();
                } else if (listView.getHeaderViewsCount() == 0) {
                    KLog.e("直接变成搜索该关键词");
                    listView.addHeaderView(wordHeaderView);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
//                KLog.e("输入结束执行该方法", "输入结束");
            }
        });

        searchView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    onSearchFeedsClicked(null);
                }
                return false;
            }
        });

//        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
////                    if (view.getLastVisiblePosition() == view.getCount() - 1) {
////                    }
//                }
//            }
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//            }
//        });
    }

    private void searchAndLoadFeedsData() {
//        if( searchView.getText().toString().startsWith("http://")){
//            searchView.setText( searchView.getText().toString().replaceFirst("^http:\\/\\/",""));
//        }
        swipeRefreshLayoutS.setRefreshing(true);
        listView.setEnabled(false);
        listView.removeHeaderView(wordHeaderView);
        listView.removeHeaderView(resultCountHeaderView);
//        OkGo.cancelTag();
        SearchApi.i().asyncFetchSearchResult(searchView.getText().toString(), new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                try {
                    FeedlyFeedsSearchResult searchResult = new Gson().fromJson(response.body(), FeedlyFeedsSearchResult.class);
                    if (searchResult != null && searchResult.getResults() != null && searchResult.getResults().size() != 0) {
                        feedlyFeeds = searchResult.getResults();
//                        KLog.e("点击搜索" + searchView.getText().toString() + feedlyFeeds.size());
//                        ToastUtil.show("已获取到" + feedlyFeeds.size() + "个订阅源");
                    } else {
                        feedlyFeeds = new ArrayList<FeedlyFeed>();
                    }
                    listViewAdapter = new SearchListViewAdapter(SearchActivity.this, feedlyFeeds);
                    TextView textView = resultCountHeaderView.findViewById(R.id.search_feeds_result_count);
                    textView.setText(getString(R.string.search_cloudy_feeds_result_count, feedlyFeeds.size()));
                    listView.addHeaderView(resultCountHeaderView);
                    listView.setAdapter(listViewAdapter);
                    swipeRefreshLayoutS.setRefreshing(false);
                    listView.setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
//                    KLog.e("报错","失败了");
                    onError(response);
                }
            }

            @Override
            public void onError(Response<String> response) {
                ToastUtil.showLong(App.i().getString(R.string.fail_try));
                swipeRefreshLayoutS.setRefreshing(false);
                listView.setEnabled(true);
                listView.addHeaderView(wordHeaderView);
            }
        });
    }


    private void addFeed(final View view, final String feedId) {
        view.setClickable(false);
        DataApi.i().addFeed(feedId, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                KLog.e(response.body());
                try {
                    QuickAdd quickAdd = new Gson().fromJson(response.body().trim(), QuickAdd.class);
                    if (quickAdd == null || quickAdd.getNumResults() == 0) {
                        this.onError(response);
                        return;
                    }
                    Feed feed = new Feed();
                    feed.setId(quickAdd.getStreamId());
                    feed.setTitle(quickAdd.getStreamName());
                    feed.setHtmlurl(quickAdd.getStreamId().replaceFirst("^feed\\/", ""));
                    WithDB.i().addFeed(feed);
                    ((IconFontView) view).setText(R.string.font_tick);
                    view.setClickable(true);
                } catch (Exception e) {
                    KLog.e(e);
                    this.onError(response);
                    return;
                }
            }

            @Override
            public void onError(Response<String> response) {
                ToastUtil.showLong(getString(R.string.toast_subscribe_fail));
                view.setClickable(true);
            }
        });
    }

    class SearchListViewAdapter extends ArrayAdapter<FeedlyFeed> {
        private List<FeedlyFeed> feedlyFeeds;

        public SearchListViewAdapter(Context context, List<FeedlyFeed> feedList) {
            super(context, 0, feedList);
            this.feedlyFeeds = feedList;
        }

        @Override
        public int getCount() {
            return feedlyFeeds.size();
        }

        @Override
        public FeedlyFeed getItem(int position) {
            return feedlyFeeds.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, @NotNull final ViewGroup parent) {
            final FeedlyFeed feedlyFeed = this.getItem(position);
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
            cvh.feedTitle.setText(feedlyFeed.getTitle());
            if (!TextUtils.isEmpty(feedlyFeed.getDescription())) {
                cvh.feedSummary.setVisibility(View.VISIBLE);
                cvh.feedSummary.setText(feedlyFeed.getDescription());
            } else {
                cvh.feedSummary.setVisibility(View.GONE);
                cvh.feedSummary.setText("");
            }
//            KLog.e("当前view是：" + position +"  " + convertView.getId() + "");
//            Glide.with(SearchActivity.this).load(feedlyFeed.getVisualUrl()).centerCrop().into(cvh.feedIcon);
            Glide.with(SearchActivity.this).load(feedlyFeed.getVisualUrl()).apply(options).into(cvh.feedIcon);

            cvh.feedUrl.setText(feedlyFeed.getFeedId().replaceFirst("feed/", ""));
            cvh.feedSubsVelocity.setText(getString(R.string.search_result_subs, feedlyFeed.getSubscribers(), feedlyFeed.getVelocity() + ""));
            if (feedlyFeed.getLastUpdated() != 0) {
                cvh.feedLastUpdated.setText(getString(R.string.search_result_last_update_time, TimeUtil.stampToTime(feedlyFeed.getLastUpdated(), "yyyy-MM-dd")));
            } else {
                cvh.feedLastUpdated.setText("");
            }
            if (WithDB.i().getFeed(feedlyFeed.getFeedId()) != null) {
                cvh.feedSubState.setText(R.string.font_tick);
            } else {
                cvh.feedSubState.setText(R.string.font_add);
            }
            cvh.feedSubState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    if (WithDB.i().getFeed(feedlyFeed.getFeedId()) != null) {
                        view.setClickable(false); // 防止重复点击
                        cvh.feedSubState.setText(R.string.font_tick);
                        DataApi.i().unsubscribeFeed(feedlyFeed.getFeedId(), new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {
                                if (!response.body().equals("OK")) {
                                    this.onError(response);
                                    return;
                                }
                                WithDB.i().delFeed(feedlyFeed.getFeedId());
                                ((IconFontView) view).setText(R.string.font_add);
                                view.setClickable(true);
                            }

                            @Override
                            public void onError(Response<String> response) {
                                ToastUtil.showLong(getString(R.string.toast_unsubscribe_fail));
                                view.setClickable(true);
                            }
                        });
                    } else {
                        cvh.feedSubState.setText(R.string.font_add);
                        addFeed(view, feedlyFeed.getFeedId());
                    }
                }
            });
            return convertView;
        }
    }


    private CustomViewHolder cvh;

    public class CustomViewHolder {
        TextView feedTitle;
        TextView feedSummary;
        TextView feedUrl;
        TextView feedSubsVelocity;
        TextView feedLastUpdated;
        IconFontView feedSubState;
        ImageView feedIcon;
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

    public void onSearchFeedsClicked(View view) {
        if (TextUtils.isEmpty(searchView.getText().toString())) {
            ToastUtil.showShort("请输入要搜索的词");
            return;
        }
        searchView.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        searchAndLoadFeedsData();
    }

    public void onSearchLocalArtsClicked(View view) {
        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
        KLog.e("要搜索的词是" + searchView.getText().toString());
        intent.putExtra("searchWord", searchView.getText().toString());
        this.setResult(Api.ActivityResult_SearchLocalArtsToMain, intent);
        this.finish();
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_from_bottom);
    }
//    public void OnSubFeedClicked(View view){
//        Tool.showLong("此处要订阅源");
//        addFeed( "feed/" + searchView.getText().toString() );
////        Intent intent = new Intent(SearchActivity.this, MainActivity.class);
////        KLog.e("要搜索的词是" +  searchWord );
////        intent.putExtra("searchWord", searchWord);
////        this.setResult(Api.ActivityResult_SearchLocalArtsToMain, intent);
////        this.finish();
//    }


    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        ViewGroupSetter artListViewSetter = new ViewGroupSetter(listView);
        // 绑定ListView的Item View中的news_title视图，在换肤时修改它的text_color属性
        artListViewSetter.childViewTextColor(R.id.search_list_item_title, R.attr.lv_item_title_color);
        artListViewSetter.childViewTextColor(R.id.search_list_item_summary, R.attr.lv_item_desc_color);
        artListViewSetter.childViewTextColor(R.id.search_list_item_feed_url, R.attr.lv_item_desc_color);
        artListViewSetter.childViewTextColor(R.id.search_list_item_sub_state, R.attr.lv_item_title_color);

        artListViewSetter.childViewTextColor(R.id.search_intent_feeds, R.attr.lv_item_desc_color);
        artListViewSetter.childViewTextColor(R.id.search_local_articles, R.attr.lv_item_desc_color);

        artListViewSetter.childViewTextColor(R.id.search_list_item_sub_velocity, R.attr.lv_item_info_color);
        artListViewSetter.childViewTextColor(R.id.search_list_item_last_updated, R.attr.lv_item_info_color);

        mColorfulBuilder
                .backgroundColor(R.id.search_root, R.attr.root_view_bg)
                // 设置 toolbar
                .backgroundColor(R.id.search_toolbar, R.attr.topbar_bg);
        return mColorfulBuilder;
    }
}
