package me.wizos.loread.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.carlt.networklibs.utils.NetworkUtils;
import com.elvishew.xlog.XLog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hjq.toast.ToastUtils;
import com.king.zxing.util.CodeUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.enums.PopupAnimation;
import com.noober.background.BackgroundLibrary;
import com.noober.background.drawable.DrawableCreator;

import java.util.ArrayList;
import java.util.List;

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
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.FeedCategory;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.BackupUtils;
import me.wizos.loread.utils.ScreenUtils;
import me.wizos.loread.utils.UriUtils;
import me.wizos.loread.view.IconFontView;
import me.wizos.loread.view.colorful.Colorful;

public class FeedActivity extends BaseActivity {
    @BindView(R.id.feed_toolbar)
    Toolbar toolbar;

    @BindView(R.id.feed_fab)
    FloatingActionButton iconFab;

    @BindView(R.id.feed_site_link)
    TextView siteLinkView;

    @BindView(R.id.feed_rss_link)
    TextView feedLinkView;

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

    // @BindView(R.id.feed_create_rule)
    // TextView createRuleButton;

    @BindView(R.id.feed_view_rules)
    TextView viewRulesButton;

    Feed feed;
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

        String feedId = bundle.getString("feedId");
        if (TextUtils.isEmpty(feedId)) {
            return;
        }

        feedViewModel = new ViewModelProvider(this).get(FeedViewModel.class);

        feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(),feedId);
        if( null == feed){
            finish();
            return;
        }

        XLog.i("展示feed的详情：" + feedId + ","  + " , " + feed);

        // initSettingView();
        loadData(feedId);
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
        toolbar.setTitle(feed.getTitle());
        toolbar.setSubtitle(feed.getFeedUrl());
        siteLinkView.setText(feed.getHtmlUrl());
        feedLinkView.setText(feed.getFeedUrl());
        iconFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(feed.getHtmlUrl())){
                    return;
                }
                Intent intent = new Intent(FeedActivity.this, WebActivity.class);
                intent.setData(Uri.parse(feed.getHtmlUrl()));
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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

            final Integer[] beforeSelectedIndices = new Integer[]{preCategoryItems.size()};
            for (int i = 0, size = preCategoryItems.size(); i < size; i++) {
                beforeSelectedIndices[i] = categoryMap.get(preCategoryItems.get(i).getId());
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
        if( displayMode == App.OPEN_MODE_LINK){
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

    public void copyIconUrl(@Nullable View view) {
        if (feed == null || TextUtils.isEmpty(feed.getIconUrl())) {
            return;
        }
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newRawUri(feed.getTitle(), Uri.parse(feed.getIconUrl()));
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
        ToastUtils.show(R.string.copy_success);
    }

    public void copyHtmlUrl(@Nullable View view) {
        if (feed == null || TextUtils.isEmpty(feed.getHtmlUrl())) {
            return;
        }
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // 创建普通字符型ClipData
        ClipData mClipData = ClipData.newRawUri(feed.getTitle(), Uri.parse(feed.getHtmlUrl()));
        // 将ClipData内容放到系统剪贴板里。
        cm.setPrimaryClip(mClipData);
        ToastUtils.show(R.string.copy_success);
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

    // public void showSelectFolder(final View view, final String feedId) {
    //     final List<Category> categoryList = CoreDB.i().categoryDao().getAll(App.i().getUser().getId());
    //     String[] categoryTitleArray = new String[categoryList.size()];
    //     for (int i = 0, size = categoryList.size(); i < size; i++) {
    //         categoryTitleArray[i] = categoryList.get(i).getTitle();
    //     }
    //
    //     final EditFeed editFeed = new EditFeed();
    //     editFeed.setId(feedId);
    //     new MaterialDialog.Builder(this)
    //             .title(getString(R.string.select_category))
    //             .items(categoryTitleArray)
    //             .alwaysCallMultiChoiceCallback()
    //             .itemsCallbackMultiChoice(null, (dialog, which, text) -> {
    //                 FeedActivity.this.selectIndices = which;
    //                 for (int i : which) {
    //                     XLog.e("点选了：" + i);
    //                 }
    //                 return true;
    //             })
    //             .positiveText(R.string.confirm)
    //             .onPositive((dialog, which) -> {
    //                 ArrayList<CategoryItem> categoryItemList = new ArrayList<>();
    //                 for (Integer selectIndex : selectIndices) {
    //                     CategoryItem categoryItem = new CategoryItem();
    //                     categoryItem.setId(categoryList.get(selectIndex).getId());
    //                     categoryItemList.add(categoryItem);
    //                 }
    //                 editFeed.setCategoryItems(categoryItemList);
    //                 view.setClickable(false);
    //                 App.i().getApi().addFeed(editFeed, new CallbackX() {
    //                     @Override
    //                     public void onSuccess(Object result) {
    //                         XLog.e("添加成功");
    //                         ((IconFontView) view).setText(R.string.font_tick);
    //                         ToastUtils.show(R.string.subscribe_success);
    //                         view.setClickable(true);
    //                     }
    //
    //                     @Override
    //                     public void onFailure(Object error) {
    //                         ToastUtils.show(getString(R.string.subscribe_fail, (String)error));
    //                         view.setClickable(true);
    //                     }
    //                 });
    //             }).show();
    // }

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
        if (CoreDB.i().feedDao().getById(App.i().getUser().getId(), feed.getId()) == null) {
            showSelectFolder(view, feed);
        } else {
            new MaterialDialog.Builder(this)
                    .title(R.string.warning)
                    .content(R.string.are_you_sure_that_unsubscribe_this_feed_link)
                    .positiveText(R.string.confirm)
                    .negativeText(R.string.cancel)
                    .positiveColor(Color.RED)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            App.i().getApi().unsubscribeFeed(feed.getId(), new CallbackX() {
                                @Override
                                public void onSuccess(Object result) {
                                    XLog.e("退订成功");
                                    ToastUtils.show(getString(R.string.unsubscribe_succeeded));
                                    ((AppCompatButton) view).setText(R.string.subscribe);
                                    Drawable drawable = new DrawableCreator.Builder()
                                            .setRipple(true, getResources().getColor(R.color.primary))
                                            .setPressedSolidColor(getResources().getColor(R.color.primary), getResources().getColor(R.color.bluePrimary))
                                            .setSolidColor(getResources().getColor(R.color.bluePrimary))
                                            .setCornersRadius(ScreenUtils.dp2px(30))
                                            .build();
                                    view.setBackground(drawable);

                                    List<Feed> feeds = new ArrayList<>();
                                    feeds.add(feed);
                                    BackupUtils.exportUserUnsubscribeOPML(App.i().getUser(), feeds);
                                    CoreDB.i().feedCategoryDao().deleteByFeedId(feed.getUid(), feed.getId());
                                    CoreDB.i().articleDao().deleteUnStarByFeedId(feed.getUid(), feed.getId());
                                    CoreDB.i().deleteFeed(feed);
                                }

                                @Override
                                public void onFailure(Object error) {
                                    XLog.e("失败：" + error);
                                    ToastUtils.show(getString(R.string.unsubscribe_failed, error));
                                }
                            });

                        }
                    }).build().show();
        }
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
