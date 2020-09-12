package me.wizos.loread.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hjq.toast.ToastUtils;
import com.socks.library.KLog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.feedly.CategoryItem;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.bean.search.SearchFeedItem;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.TimeUtil;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.SwipeRefreshLayoutS;
import me.wizos.loread.view.colorful.Colorful;
import me.wizos.loread.view.colorful.setter.ViewGroupSetter;


public class SearchActivity extends BaseActivity {
    protected static final String TAG = "SearchActivity";
    private EditText searchView;
    private ListView listView;
    private SwipeRefreshLayoutS swipeRefreshLayoutS;

    private ArrayList<SearchFeedItem> searchFeedItems = new ArrayList<>();
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

        wordHeaderView = getLayoutInflater().inflate(R.layout.activity_search_list_header_word, listView, false);
        resultCountHeaderView = getLayoutInflater().inflate(R.layout.activity_search_list_header_result_count, listView, false);

        listViewAdapter = new SearchListViewAdapter(SearchActivity.this, searchFeedItems);
        listView.setAdapter(listViewAdapter);

        searchView.requestFocus();
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                KLog.e("输入前确认执行该方法", "开始输入：" + s .toString() + startAnimation + "  " + after + "  " + count);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    listView.removeHeaderView(wordHeaderView);
                    listView.removeHeaderView(resultCountHeaderView);
                    swipeRefreshLayoutS.setRefreshing(false);
                    searchFeedItems.clear();
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
        swipeRefreshLayoutS.setRefreshing(true);
        listView.setEnabled(false);
        listView.removeHeaderView(wordHeaderView);
        listView.removeHeaderView(resultCountHeaderView);

//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl(FeedlyApi.HOST + "/") // 设置网络请求的Url地址, 必须以/结尾
//                .addConverterFactory(GsonConverterFactory.create())  // 设置数据解析器
//                .client(HttpClientManager.i().simpleClient())
//                .build();
//        FeedlyService feedlyService = retrofit.create(FeedlyService.class);
//
//        //对 发送请求 进行封装
//        Call<SearchFeeds> callSearchFeeds = feedlyService.getSearchFeeds(searchView.getText().toString(), 100);
//        callSearchFeeds.enqueue(new Callback<SearchFeeds>() {
//            @Override
//            public void onResponse(Call<SearchFeeds> call, retrofit2.Response<SearchFeeds> response) {
//                SearchFeeds searchResult = response.body();
//                KLog.e("成功：" + searchResult);
//                if (searchResult != null && searchResult.getResults() != null && searchResult.getResults().size() != 0) {
//                    searchFeedItems = searchResult.getResults();
//                    //  KLog.e("点击搜索" + searchView.getText().toString() + searchFeedItems.size());
//                    //  ToastUtil.show("已获取到" + searchFeedItems.size() + "个订阅源");
//                } else {
//                    searchFeedItems = new ArrayList<SearchFeedItem>();
//                }
//                listViewAdapter = new SearchListViewAdapter(SearchActivity.this, searchFeedItems);
//                TextView textView = resultCountHeaderView.findViewById(R.id.search_feeds_result_count);
//                textView.setText(getString(R.string.search_cloudy_feeds_result_count, searchFeedItems.size()));
//                listView.addHeaderView(resultCountHeaderView);
//                listView.setAdapter(listViewAdapter);
//                swipeRefreshLayoutS.setRefreshing(false);
//                listView.setEnabled(true);
//            }
//
//            @Override
//            public void onFailure(Call<SearchFeeds> call, Throwable t) {
//                KLog.e("失败：" + t);
//                ToastUtils.show(App.i().getString(R.string.fail_try));
//                swipeRefreshLayoutS.setRefreshing(false);
//                listView.setEnabled(true);
//                listView.addHeaderView(wordHeaderView);
//            }
//        });
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
                            KLog.e("点选了：" + i);
                        }
                        return true;
                    }
                })
                .positiveText(R.string.confirm)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        ArrayList<CategoryItem> categoryItemList = new ArrayList<>();
                        for (int i = 0; i < selectIndices.length; i++) {
                            CategoryItem categoryItem = new CategoryItem();
                            categoryItem.setId(categoryList.get(selectIndices[i]).getId());
                            categoryItemList.add(categoryItem);
                        }
                        editFeed.setCategoryItems(categoryItemList);
                        view.setClickable(false);
                        App.i().getApi().addFeed(editFeed, new CallbackX() {
                            @Override
                            public void onSuccess(Object result) {
                                KLog.e("添加成功");
                                ((IconFontView) view).setText(R.string.font_tick);
                                ToastUtils.show(R.string.subscribe_success);
                                view.setClickable(true);
                            }

                            @Override
                            public void onFailure(Object error) {
                                ToastUtils.show(getString(R.string.subscribe_fail));
                                view.setClickable(true);
                            }
                        });
//                        App.i().getApi().addFeed(editFeed).enqueue(new Callback() {
//                            @Override
//                            public void onResponse(Call call, retrofit2.Response response) {
//                                KLog.e("添加成功");
//                                ((IconFontView) view).setText(R.string.font_tick);
//                                ToastUtils.show(R.string.subscribe_success);
//                                view.setClickable(true);
//                            }
//
//                            @Override
//                            public void onFailure(Call call, Throwable t) {
//                                ToastUtils.show(getString(R.string.subscribe_fail));
//                                view.setClickable(true);
//                            }
//                        });
                    }
                }).show();
    }

    class SearchListViewAdapter extends ArrayAdapter<SearchFeedItem> {
        private List<SearchFeedItem> searchFeedItems;

        public SearchListViewAdapter(Context context, List<SearchFeedItem> feedList) {
            super(context, 0, feedList);
            this.searchFeedItems = feedList;
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
//            KLog.e("当前view是：" + position +"  " + convertView.getId() + "");
//            Glide.with(SearchActivity.this).load(searchFeedItem.getVisualUrl()).centerCrop().into(cvh.feedIcon);
            Glide.with(SearchActivity.this).load(searchFeedItem.getVisualUrl()).apply(options).into(cvh.feedIcon);

            cvh.feedUrl.setText(searchFeedItem.getFeedId().replaceFirst("feed/", ""));

            cvh.feedSubsVelocity.setText( getResources().getQuantityString(R.plurals.search_result_followers, searchFeedItem.getSubscribers(), searchFeedItem.getSubscribers() ) + getString(R.string.search_result_articles, searchFeedItem.getVelocity()) );
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
            ToastUtils.show(R.string.please_input_keyword);
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
        this.setResult(App.ActivityResult_SearchLocalArtsToMain, intent);
        this.finish();
        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_from_bottom);
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
