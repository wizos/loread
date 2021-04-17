package me.wizos.loread.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.carlt.networklibs.utils.NetworkUtils;
import com.elvishew.xlog.XLog;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hjq.toast.ToastUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.king.zxing.util.CodeUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.enums.PopupAnimation;
import com.noober.background.BackgroundLibrary;
import com.umeng.analytics.MobclickAgent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.wizos.loread.App;
import me.wizos.loread.BuildConfig;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.activity.viewmodel.FeedViewModel;
import me.wizos.loread.bean.FeedEntries;
import me.wizos.loread.bean.feedly.CategoryItem;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.config.SaveDirectory;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.FeedCategory;
import me.wizos.loread.db.User;
import me.wizos.loread.network.Getting;
import me.wizos.loread.network.SyncWorker;
import me.wizos.loread.network.api.BaseApi;
import me.wizos.loread.network.api.LocalApi;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.BackupUtils;
import me.wizos.loread.utils.Converter;
import me.wizos.loread.utils.FeedParserUtils;
import me.wizos.loread.utils.InputStreamCache;
import me.wizos.loread.utils.PagingUtils;
import me.wizos.loread.utils.UriUtils;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.colorful.Colorful;

public class FeedActivity extends BaseActivity {
    @BindView(R.id.feed_toolbar)
    Toolbar toolbar;

    @BindView(R.id.feed_toolbar_layout)
    CollapsingToolbarLayout actionBar;

    @BindView(R.id.feed_fab)
    FloatingActionButton iconFab;

    @BindView(R.id.feed_article_count)
    TextView articleCountView;

    @BindView(R.id.feed_site_link)
    TextView siteLinkView;

    @BindView(R.id.feed_rss_link)
    TextView feedLinkView;

    @BindView(R.id.feed_site_link_edit)
    ImageView siteLinkEditButton;

    @BindView(R.id.feed_rss_link_edit)
    ImageView rssLinkEditButton;


    @BindView(R.id.feed_settings)
    LinearLayout feedSettingsLayout;

    @BindView(R.id.feed_remark)
    LinearLayout feedNameLayout;
    @BindView(R.id.feed_remark_value)
    TextView feedNameView;

    @BindView(R.id.feed_category)
    LinearLayout feedCategoryLayout;

    @BindView(R.id.feed_category_value)
    TextView feedsCategoryNameView;

    @BindView(R.id.feed_display_mode)
    LinearLayout feedDisplayModeLayout;
    @BindView(R.id.feed_display_mode_value)
    TextView feedDisplayModeView;

    @BindView(R.id.feed_save_folder)
    LinearLayout feedSaveFolderLayout;
    @BindView(R.id.feed_save_folder_value)
    TextView feedSaveFolderView;

    @BindView(R.id.feed_auto_sync_frequency)
    LinearLayout feedSyncFrequencyLayout;
    @BindView(R.id.feed_sync_frequency_summary)
    TextView feedSyncFrequencyView;

    @BindView(R.id.feed_view_info)
    TextView feedInfoButton;

    @BindView(R.id.feed_refetch)
    TextView refetchButton;

    @BindView(R.id.feed_view_rules)
    TextView viewRulesButton;



    Feed feed;
    String feedId;
    ArrayList<CategoryItem> preCategoryItems;

    FeedViewModel feedViewModel;
    RequestOptions options = new RequestOptions().circleCrop();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BackgroundLibrary.inject2(this);
        setContentView(R.layout.activity_feed);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        // actionBar = getSupportActionBar();
        // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setHomeButtonEnabled(true);
        // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        Bundle bundle;
        if (savedInstanceState != null) {
            bundle = savedInstanceState;
        } else {
            bundle = getIntent().getExtras();
        }

        if (bundle == null) {
            return;
        }

        feedId = bundle.getString("feedId");
        if (TextUtils.isEmpty(feedId)) {
            finish();
            return;
        }

        feedViewModel = new ViewModelProvider(this).get(FeedViewModel.class);
        MobclickAgent.onEvent(this, "enter_feed_activity");
        // feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(),feedId);
        // if( null == feed){
        //     finish();
        //     return;
        // }

        // XLog.i("展示feed的详情：" + feedId + ","  + " , " + feed);

        // initSettingView();
    }

    @Override
    protected void onResume(){
        super.onResume();
        loadData(feedId);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("feedId", feedId);
        super.onSaveInstanceState(outState);
    }

    private void loadData(String feedId){
        feedViewModel.loadFeed(App.i().getUser().getId(), feedId, this, new Observer<Feed>() {
            @Override
            public void onChanged(Feed feed) {
                if(null == feed){
                    finish();
                    // feedSettingsLayout.setVisibility(View.GONE);
                }else {
                    // feedSettingsLayout.setVisibility(View.VISIBLE);
                    FeedActivity.this.feed = feed;
                    initSettingView();
                }

            }
        });
    }



    private void initSettingView(){
        Glide.with(this).load(UriUtils.getFaviconUrl(feed.getHtmlUrl())).apply(options).into(iconFab);
        actionBar.setTitle(feed.getTitle());
        // 以下不生效
        // getSupportActionBar().setSubtitle(feed.getFeedUrl());
        // toolbar.setSubtitle(feed.getFeedUrl());
        articleCountView.setText(getString(R.string.article_count_detail, feed.getAllCount(), feed.getUnreadCount(), feed.getStarCount()));
        siteLinkView.setText(feed.getHtmlUrl());
        siteLinkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHtmlUrl();
            }
        });
        feedLinkView.setText(feed.getFeedUrl());
        feedLinkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFeedUrl();
            }
        });

        if(App.i().getApi() instanceof LocalApi){
            siteLinkEditButton.setVisibility(View.VISIBLE);
            rssLinkEditButton.setVisibility(View.VISIBLE);
            refetchButton.setVisibility(View.VISIBLE);
        }else {
            siteLinkEditButton.setVisibility(View.GONE);
            rssLinkEditButton.setVisibility(View.GONE);
            refetchButton.setVisibility(View.GONE);
        }

        refetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!UriUtils.isHttpOrHttpsUrl(feed.getFeedUrl())){
                    ToastUtils.show(R.string.invalid_url_hint);
                    return;
                }
                ToastUtils.show(R.string.fetching);
                Getting getting = new Getting(feed.getFeedUrl(), new Getting.Listener() {
                    @Override
                    public void onResponse(InputStreamCache inputStreamCache) {
                        FeedEntries feedEntries = FeedParserUtils.parseInputSteam(FeedActivity.this, feed, inputStreamCache, new Converter.ArticleConvertListener() {
                            @Override
                            public Article onEnd(Article article) {
                                article.setCrawlDate(App.i().getLastShowTimeMillis());
                                return article;
                            }
                        });
                        if(feedEntries == null){
                            ToastUtils.show(getString(R.string.fetch_failed));
                        }else if(!feedEntries.isSuccess()){
                            ToastUtils.show(getString(R.string.fetch_failed_with_reason, feedEntries.getFeed().getLastSyncError()));
                        }else {
                            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                                @Override
                                public void run() {
                                    Map<String, Article> articleMap = feedEntries.getArticleMap();
                                    String uid = App.i().getUser().getId();
                                    PagingUtils.slice(new ArrayList<>(articleMap.keySet()), 50, new PagingUtils.PagingListener<String>() {
                                        @Override
                                        public void onPage(@NotNull List<String> childList) {
                                            List<String> removeIds = CoreDB.i().articleDao().getIds(uid, childList);
                                            if(removeIds != null){
                                                for (String id: removeIds){
                                                    articleMap.remove(id);
                                                }
                                            }
                                        }
                                    });
                                    ArrayList<Article> newArticles = new ArrayList<>(articleMap.values());
                                    CoreDB.i().articleDao().insert(newArticles);
                                    BaseApi.updateCollectionCount();
                                    ToastUtils.show(getString(R.string.fetch_success_with_reason, newArticles.size()));
                                    if(newArticles.size() > 0){
                                        LiveEventBus.get(SyncWorker.NEW_ARTICLE_NUMBER).post(newArticles.size());
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(String msg) {
                        ToastUtils.show(getString(R.string.fetch_failed_with_reason, msg));
                    }
                });

                getting.policy(Getting.BOTH_OKHTTP_FIRST);
                getting.start();

                // Request.Builder request = new Request.Builder().url(feed.getFeedUrl());
                // request.header(Contract.USER_AGENT, WebSettings.getDefaultUserAgent(App.i()));
                // HttpClientManager.i().searchClient().newCall(request.build()).enqueue(new Callback() {
                //     @Override
                //     public void onFailure(@NotNull Call call, @NotNull IOException e) {
                //         ToastUtils.show(getString(R.string.fetch_failed_with_reason, e.getLocalizedMessage()));
                //     }
                //
                //     @Override
                //     public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                //         if(!response.isSuccessful()){
                //             ToastUtils.show(getString(R.string.fetch_failed_with_reason, StringUtils.isEmpty(response.message()) ? response.code(): (response.code() + "," + response.message())) );
                //         }else {
                //             ResponseBody responseBody = response.body();
                //             if(responseBody == null){
                //                 ToastUtils.show(getString(R.string.fetch_failed_with_reason, getString(R.string.return_data_exception)));
                //             }else {
                //                 FeedEntries feedEntries = FeedParserUtils.parseResponseBody(FeedActivity.this, feed, responseBody, new Converter.ArticleConvertListener() {
                //                     @Override
                //                     public Article onEnd(Article article) {
                //                         article.setCrawlDate(App.i().getLastShowTimeMillis());
                //                         return article;
                //                     }
                //                 });
                //                 if(feedEntries == null){
                //                     ToastUtils.show(getString(R.string.fetch_failed));
                //                 }else if(!feedEntries.isSuccess()){
                //                     ToastUtils.show(getString(R.string.fetch_failed_with_reason, feedEntries.getFeed().getLastSyncError()));
                //                 }else {
                //                     AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                //                         @Override
                //                         public void run() {
                //                             Map<String, Article> articleMap = feedEntries.getArticleMap();
                //                             String uid = App.i().getUser().getId();
                //                             PagingUtils.slice(new ArrayList<>(articleMap.keySet()), 50, new PagingUtils.PagingListener<String>() {
                //                                 @Override
                //                                 public void onPage(@NotNull List<String> childList) {
                //                                     List<String> removeIds = CoreDB.i().articleDao().getIds(uid, childList);
                //                                     if(removeIds != null){
                //                                         for (String id: removeIds){
                //                                             articleMap.remove(id);
                //                                         }
                //                                     }
                //                                 }
                //                             });
                //                             ArrayList<Article> newArticles = new ArrayList<>(articleMap.values());
                //                             for (Article article: newArticles){
                //                                 article.setCrawlDate(App.i().getLastShowTimeMillis());
                //                             }
                //                             CoreDB.i().articleDao().insert(newArticles);
                //                             BaseApi.updateCollectionCount();
                //                             ToastUtils.show(getString(R.string.fetch_success_with_reason, newArticles.size()));
                //                             if(newArticles.size() > 0){
                //                                 LiveEventBus.get(SyncWorker.NEW_ARTICLE_NUMBER).post(newArticles.size());
                //                             }
                //                         }
                //                     });
                //                 }
                //             }
                //         }
                //     }
                // });
            }
        });

        if (!TextUtils.isEmpty(feed.getTitle())) {
            feedNameView.setText(feed.getTitle());
        } else {
            feedNameView.setText(R.string.unknown);
        }

        feedNameLayout.setOnClickListener(v -> new MaterialDialog.Builder(FeedActivity.this)
                .title(R.string.site_remark)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRange(1, 56)
                .input(getString(R.string.site_remark), feed.getTitle(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if (!NetworkUtils.isAvailable()) {
                            ToastUtils.show(getString(R.string.network_not_connected_please_check_it));
                        } else {
                            renameFeed(input.toString(), feed);
                            dialog.dismiss();
                        }
                    }
                })
                .positiveText(R.string.confirm)
                .negativeText(android.R.string.cancel)
                .show());


        final EditFeed editFeed = new EditFeed(feed.getId());
        preCategoryItems = editFeed.getCategoryItems();
        final String[] preCategoryTitles = new String[preCategoryItems.size()];
        for (int i = 0, size = preCategoryItems.size(); i < size; i++) {
            preCategoryTitles[i] = preCategoryItems.get(i).getLabel();
        }
        String titles = TextUtils.join(" / ", preCategoryTitles);
        if (!TextUtils.isEmpty(titles)) {
            feedsCategoryNameView.setText(titles);
        } else {
            feedsCategoryNameView.setText(getString(R.string.un_category));
        }

        feedCategoryLayout.setOnClickListener(v -> {
            final List<Category> categoryList = CoreDB.i().categoryDao().getAll(App.i().getUser().getId());
            ArrayMap<String, Integer> categoryMap = new ArrayMap<>(categoryList.size());

            String[] categoryTitleArray = new String[categoryList.size()];
            for (int i = 0, size = categoryList.size(); i < size; i++) {
                categoryMap.put(categoryList.get(i).getId(), i);
                categoryTitleArray[i] = categoryList.get(i).getTitle();
            }

            Integer[] beforeSelectedIndices;
            if(preCategoryItems.size() == 0){
                beforeSelectedIndices = null;
            }else {
                beforeSelectedIndices = new Integer[preCategoryItems.size()];
                // XLog.i(preCategoryItems);
                // XLog.i(categoryMap);
                for (int i = 0, size = preCategoryItems.size(); i < size; i++) {
                    // String id = preCategoryItems.get(i).getId();
                    // Integer count  = categoryMap.get(id);
                    beforeSelectedIndices[i] = categoryMap.get(preCategoryItems.get(i).getId());
                }
            }

            new MaterialDialog.Builder(FeedActivity.this)
                    .title(getString(R.string.edit_category))
                    .items(categoryTitleArray)
                    .itemsCallbackMultiChoice(beforeSelectedIndices, new MaterialDialog.ListCallbackMultiChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, final Integer[] which, CharSequence[] text) {
                            final ArrayList<CategoryItem> selectedCategoryItems = new ArrayList<>();
                            ArrayList<String> selectedTitles = new ArrayList<>();
                            CategoryItem categoryItem;
                            for (int i : which) {
                                categoryItem = new CategoryItem();
                                categoryItem.setId(categoryList.get(i).getId());
                                categoryItem.setLabel(categoryList.get(i).getTitle());
                                selectedCategoryItems.add(categoryItem);
                                selectedTitles.add(categoryList.get(i).getTitle());
                            }
                            final String titles1 = TextUtils.join(" / ", selectedTitles);
                            editFeed.setCategoryItems(selectedCategoryItems);
                            ToastUtils.show(R.string.editing);
                            App.i().getApi().editFeedCategories(preCategoryItems, editFeed, new CallbackX<String,String>() {
                                @Override
                                public void onSuccess(String result) {
                                    ArrayList<FeedCategory> feedCategories = new ArrayList<>(selectedCategoryItems.size());
                                    FeedCategory feedCategory;
                                    for (CategoryItem categoryItem : selectedCategoryItems) {
                                        feedCategory = new FeedCategory(App.i().getUser().getId(), feed.getId(), categoryItem.getId());
                                        feedCategories.add(feedCategory);
                                    }
                                    feedsCategoryNameView.setText(titles1);
                                    CoreDB.i().coverFeedCategories(editFeed);
                                    preCategoryItems = selectedCategoryItems;
                                    ToastUtils.show(R.string.edit_success);
                                }

                                @Override
                                public void onFailure(String error) {
                                    ToastUtils.show(getString(R.string.edit_fail_with_reason, error));
                                }
                            });
                            return true;
                        }
                    })
                    .alwaysCallMultiChoiceCallback() // the callback will always be called, to check if selection is still allowed
                    .show();
        });


        int displayMode = feed.getDisplayMode();
        if(displayMode == App.OPEN_MODE_LINK){
            feedDisplayModeView.setText(R.string.original);
        }else if( displayMode == App.OPEN_MODE_READABILITY){
            feedDisplayModeView.setText(R.string.readability);
        }else {
            feedDisplayModeView.setText(R.string.rss);
        }
        feedDisplayModeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new XPopup.Builder(FeedActivity.this)
                        .isCenterHorizontal(false) //是否与目标水平居中对齐
                        // .offsetY(-10)
                        .hasShadowBg(true)
                        .popupAnimation(PopupAnimation.ScaleAlphaFromCenter)
                        .atView(feedDisplayModeView)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
                        .asAttachList(new String[]{getString(R.string.rss), getString(R.string.readability), getString(R.string.original)},
                                null,
                                (which, text) -> {
                                    feed.setDisplayMode(which);
                                    CoreDB.i().feedDao().update(feed);
                                    feedDisplayModeView.setText(text);
                                })
                        .show();
            }
        });


        if(BuildConfig.DEBUG){
            String optionName = SaveDirectory.i().getDirNameSettingByFeed(feed.getId());
            feedSaveFolderView.setText(optionName);
            feedSaveFolderLayout.setVisibility(View.VISIBLE);
            feedSaveFolderLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new XPopup.Builder(FeedActivity.this)
                            .isCenterHorizontal(false) //是否与目标水平居中对齐
                            .hasShadowBg(true)
                            .popupAnimation(PopupAnimation.ScaleAlphaFromCenter)
                            .atView(feedSaveFolderView)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
                            .asAttachList(SaveDirectory.i().getDirectoriesOptionName(),
                                    null,
                                    (which, text) -> {
                                        List<String> dirsValue = SaveDirectory.i().getDirectoriesOptionValue();
                                        SaveDirectory.i().setFeedDirectory(feed.getId(),dirsValue.get(which));
                                        SaveDirectory.i().save();
                                    })
                            .show();
                }
            });

            User user = App.i().getUser();
            if(user != null && App.i().getApi() instanceof LocalApi){
                feedSyncFrequencyLayout.setVisibility(View.VISIBLE);
                int feedSyncInterval = feed.getSyncInterval();
                if(feedSyncInterval == -1){
                    feedSyncFrequencyView.setText(getString(R.string.disable_sync_frequency));
                }else if(feedSyncInterval == 0){
                    feedSyncInterval = user.getAutoSyncFrequency();
                    if (feedSyncInterval >= 60) {
                        feedSyncFrequencyView.setText(getString(R.string.default_sync_frequency, getString(R.string.xx_hour, feedSyncInterval / 60)));
                    } else {
                        feedSyncFrequencyView.setText(getString(R.string.default_sync_frequency, getString(R.string.xx_minute, feedSyncInterval)));
                    }
                }else if (feedSyncInterval >= 60) {
                    feedSyncFrequencyView.setText(getString(R.string.xx_hour, feedSyncInterval / 60));
                } else {
                    feedSyncFrequencyView.setText(getString(R.string.xx_minute, feedSyncInterval));
                }

                feedSyncFrequencyLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int[] minuteArray = getResources().getIntArray(R.array.feed_sync_frequency_value);
                        int preSelectTimeFrequencyIndex = -1;
                        int num = minuteArray.length;
                        CharSequence[] list = new CharSequence[num];
                        int item;
                        for (int i = 0; i < num; i++) {
                            item = minuteArray[i];
                            if(item == -1){
                                list[i] = getString(R.string.disable_sync_frequency);
                            }else if(item == 0){
                                int userSyncInterval = user.getAutoSyncFrequency();
                                if (userSyncInterval >= 60) {
                                    list[i] = getString(R.string.default_sync_frequency, getString(R.string.xx_hour, userSyncInterval / 60));
                                } else {
                                    list[i] = getString(R.string.default_sync_frequency, getString(R.string.xx_minute, userSyncInterval));
                                }
                            }else if(item >= 60){
                                list[i] = getResources().getString(R.string.xx_hour, item / 60);
                            }else {
                                list[i] = getResources().getString(R.string.xx_minute, item);
                            }

                            if (feed.getSyncInterval() == minuteArray[i]) {
                                preSelectTimeFrequencyIndex = i;
                            }
                        }


                        new MaterialDialog.Builder(FeedActivity.this)
                                .title(R.string.sync_frequency)
                                .items(list)
                                .itemsCallbackSingleChoice(preSelectTimeFrequencyIndex, new MaterialDialog.ListCallbackSingleChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                        feed.setSyncInterval(minuteArray[which]);
                                        CoreDB.i().feedDao().update(feed);

                                        XLog.i("选择了" + which);
                                        feedSyncFrequencyView.setText(list[which]);
                                        dialog.dismiss();
                                        return true; // allow selection
                                    }
                                })
                                .show();
                    }
                });
            }


            feedInfoButton.setVisibility(View.VISIBLE);
            feedInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(FeedActivity.this)
                            .title(R.string.article_info)
                            .content(feed.toString())
                            .positiveText("复制")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    //获取剪贴板管理器：
                                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    // 创建普通字符型ClipData
                                    ClipData mClipData = ClipData.newPlainText("ArticleContent", feed.toString());
                                    // 将ClipData内容放到系统剪贴板里。
                                    cm.setPrimaryClip(mClipData);
                                    ToastUtils.show("已复制内容");
                                }
                            })

                            .positiveColorRes(R.color.material_red_400)
                            .titleGravity(GravityEnum.CENTER)
                            .titleColorRes(R.color.material_red_400)
                            .contentColorRes(android.R.color.white)
                            .backgroundColorRes(R.color.material_blue_grey_800)
                            .dividerColorRes(R.color.material_teal_a400)
                            // .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.POSITIVE)
                            .positiveColor(Color.WHITE)
                            .negativeColorAttr(android.R.attr.textColorSecondaryInverse)
                            .theme(Theme.DARK)
                            .show();
                }
            });
        }


        // createRuleButton.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {
        //         Intent intent = new Intent(FeedActivity.this, TriggerRuleEditActivity.class);
        //         intent.putExtra(Contract.TYPE, Contract.TYPE_FEED);
        //         intent.putExtra(Contract.TARGET_ID, feed.getId());
        //         // intent.putExtra(Contract.TARGET_NAME, feed.getTitle());
        //         startActivity(intent);
        //         overridePendingTransition(R.anim.in_from_bottom, R.anim.fade_out);
        //     }
        // });

        viewRulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FeedActivity.this, TriggerRuleManagerActivity.class);
                intent.putExtra(Contract.TYPE, Contract.TYPE_FEED);
                intent.putExtra(Contract.TARGET_ID, feed.getId());
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_bottom, R.anim.fade_out);
            }
        });
    }

    public void renameFeed(final String targetTitle, final Feed feed) {
        XLog.d("改名：" + targetTitle + feed.getId());
        if (targetTitle.equals("") || feed.getTitle().equals(targetTitle)) {
            return;
        }
        App.i().getApi().renameFeed(feed.getId(), targetTitle, new CallbackX() {
            @Override
            public void onSuccess(Object result) {
                feed.setTitle(targetTitle);
                CoreDB.i().feedDao().update(feed);
                ToastUtils.show(R.string.edit_success);
                XLog.d("改名成功");
            }

            @Override
            public void onFailure(Object error) {
                ToastUtils.show(App.i().getString(R.string.rename_failed_reason, error));
            }
        });
    }

    public void openIconUrl(@Nullable View view) {
        if(feed == null || TextUtils.isEmpty(feed.getIconUrl()) || !BuildConfig.DEBUG){
            return;
        }
        Intent intent = new Intent(FeedActivity.this, WebActivity.class);
        intent.setData(Uri.parse(feed.getIconUrl()));
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void openHtmlUrl() {
        if(feed == null || TextUtils.isEmpty(feed.getHtmlUrl())){
            return;
        }
        Intent intent = new Intent(FeedActivity.this, WebActivity.class);
        intent.setData(Uri.parse(feed.getHtmlUrl()));
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    public void editHtmlUrl(@Nullable View view) {
        if (feed==null || TextUtils.isEmpty(feed.getHtmlUrl())) {
            return;
        }
        new MaterialDialog.Builder(this)
                .title(R.string.edit)
                .inputType(InputType.TYPE_TEXT_VARIATION_URI)
                .inputRange(12, 240)
                .input("", feed.getHtmlUrl(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if(UriUtils.isHttpOrHttpsUrl(input.toString())){
                            feed.setHtmlUrl(input.toString());
                            CoreDB.i().feedDao().update(feed);
                            ToastUtils.show(R.string.success);
                        }else {
                            ToastUtils.show(R.string.invalid_url_hint);
                        }
                    }
                })
                .negativeText(android.R.string.cancel)
                .positiveText(R.string.confirm)
                .show();
    }
    public void copyHtmlUrl(@Nullable View view) {
        if (feed == null || TextUtils.isEmpty(feed.getHtmlUrl())) {
            return;
        }
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newRawUri(feed.getTitle(), Uri.parse(feed.getHtmlUrl()));
        if(cm == null){
            return;
        }
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
        ToastUtils.show(R.string.copy_success);
    }
    public void openFeedUrl() {
        if(feed == null || TextUtils.isEmpty(feed.getFeedUrl())){
            return;
        }
        Intent intent = new Intent(FeedActivity.this, WebActivity.class);
        intent.setData(Uri.parse(feed.getFeedUrl()));
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void editFeedUrl(@Nullable View view) {
        if (feed==null || TextUtils.isEmpty(feed.getFeedUrl())) {
            return;
        }
        new MaterialDialog.Builder(this)
                .title(R.string.edit)
                .inputType(InputType.TYPE_TEXT_VARIATION_URI)
                .inputRange(12, 240)
                .input("", feed.getFeedUrl(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        if(UriUtils.isHttpOrHttpsUrl(input.toString())){
                            feed.setFeedUrl(input.toString());
                            CoreDB.i().feedDao().update(feed);
                            ToastUtils.show(R.string.success);
                        }else {
                            ToastUtils.show(R.string.invalid_url_hint);
                        }
                    }
                })
                .negativeText(android.R.string.cancel)
                .positiveText(R.string.confirm)
                .show();
    }

    public void copyFeedUrl(@Nullable View view) {
        if (feed==null || TextUtils.isEmpty(feed.getFeedUrl())) {
            return;
        }
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newRawUri(feed.getTitle(), Uri.parse(feed.getFeedUrl()));
        // 将ClipData内容放到系统剪贴板里。
        if(cm == null){
            return;
        }
        cm.setPrimaryClip(mClipData);
        ToastUtils.show(R.string.copy_success);
    }

    public void createFeedUrlQRCode(View view){
        if (feed==null || TextUtils.isEmpty(feed.getFeedUrl())) {
            return;
        }

        MaterialDialog qrCodeDialog = new MaterialDialog.Builder(this)
                .customView(R.layout.dialog_qr_code, false).build();
        ImageView qrCodeImage = (ImageView) qrCodeDialog.findViewById(R.id.dialog_qr_code);

        //生成二维码
        qrCodeImage.setImageBitmap(CodeUtils.createQRCode(feed.getFeedUrl(), 600));

        qrCodeDialog.show();
    }

    private Integer[] selectIndices;
    public void showSelectFolder(final View view, Feed feed) {
        final List<Category> categoryList = CoreDB.i().categoryDao().getAll(App.i().getUser().getId());
        String[] categoryTitleArray = new String[categoryList.size()];
        for (int i = 0, size = categoryList.size(); i < size; i++) {
            categoryTitleArray[i] = categoryList.get(i).getTitle();
        }

        FeedEntries feedEntries = new FeedEntries();
        feedEntries.setFeed(feed);

        new MaterialDialog.Builder(this)
                .title(getString(R.string.select_category))
                .items(categoryTitleArray)
                .alwaysCallMultiChoiceCallback()
                .itemsCallbackMultiChoice(null, (dialog, which, text) -> {
                    FeedActivity.this.selectIndices = which;
                    for (int i : which) {
                        XLog.e("点选了：" + i);
                    }
                    return true;
                })
                .positiveText(R.string.confirm)
                .onPositive((dialog, which) -> {
                    ArrayList<FeedCategory> categoryItemList = new ArrayList<>();
                    for (Integer selectIndex : selectIndices) {
                        FeedCategory feedCategory = new FeedCategory(App.i().getUser().getId(), feed.getId(), categoryList.get(selectIndex).getId());
                        categoryItemList.add(feedCategory);
                    }
                    feedEntries.setFeedCategories(categoryItemList);
                    view.setClickable(false);
                    App.i().getApi().addFeed(feedEntries, new CallbackX() {
                        @Override
                        public void onSuccess(Object result) {
                            XLog.e("添加成功");
                            ((IconFontView) view).setText(R.string.font_tick);
                            ToastUtils.show(R.string.subscribe_success_plz_sync);
                            view.setClickable(true);
                        }

                        @Override
                        public void onFailure(Object error) {
                            ToastUtils.show(getString(R.string.subscribe_fail, (String)error));
                            view.setClickable(true);
                        }
                    });
                }).show();
    }

    public void clickUnsubscribe(final View view) {
        if (feed == null) {
            return;
        }
        // if (CoreDB.i().feedDao().getById(App.i().getUser().getId(), feed.getId()) == null) {
        //     showSelectFolder(view, feed);
        // } else {
        //
        // }
        new MaterialDialog.Builder(this)
                .title(R.string.warning)
                .content(R.string.are_you_sure_that_unsubscribe_this_feed_link)
                .positiveText(R.string.confirm)
                .negativeText(R.string.cancel)
                .positiveColor(Color.RED)
                .onPositive((dialog, which) -> App.i().getApi().unsubscribeFeed(feed.getId(), new CallbackX() {
                    @Override
                    public void onSuccess(Object result) {
                        // ((AppCompatButton) view).setText(R.string.subscribe);
                        // Drawable drawable = new DrawableCreator.Builder()
                        //         .setRipple(true, getResources().getColor(R.color.primary))
                        //         .setPressedSolidColor(getResources().getColor(R.color.primary), getResources().getColor(R.color.bluePrimary))
                        //         .setSolidColor(getResources().getColor(R.color.bluePrimary))
                        //         .setCornersRadius(ScreenUtils.dp2px(30))
                        //         .build();
                        // view.setBackground(drawable);

                        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                            @Override
                            public void run() {
                                long time = System.currentTimeMillis();
                                BackupUtils.exportUserUnsubscribeOPML(App.i().getUser(), feed);
                                CoreDB.i().feedCategoryDao().deleteByFeedId(feed.getUid(), feed.getId());
                                CoreDB.i().articleDao().deleteUnStarByFeedId(feed.getUid(), feed.getId());
                                CoreDB.i().deleteFeed(feed);
                                // CoreDB.i().articleDao().deleteUnsubscribeUnStar(feed.getUid());
                                ToastUtils.show(getString(R.string.unsubscribe_succeeded));
                                XLog.i("退订成功，数据库耗时：" + (System.currentTimeMillis() - time) );
                            }
                        });
                    }

                    @Override
                    public void onFailure(Object error) {
                        XLog.e("失败：" + error);
                        ToastUtils.show(getString(R.string.unsubscribe_failed, error));
                    }
                })).build().show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //监听左上角的返回箭头
        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        return mColorfulBuilder;
    }
}
