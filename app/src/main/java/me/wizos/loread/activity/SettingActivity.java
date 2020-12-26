package me.wizos.loread.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.elvishew.xlog.XLog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.wizos.loread.App;
import me.wizos.loread.BuildConfig;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.CorePref;
import me.wizos.loread.db.User;
import me.wizos.loread.log.CoreLog;
import me.wizos.loread.view.colorful.Colorful;

/**
 * @author Wizos on 2016/3/10.
 */

public class SettingActivity extends BaseActivity {
    protected static final String TAG = "SettingActivity";

    private TextView clearBeforeDaySummary;

    @Nullable
    @BindView(R.id.setting_auto_sync_sb)
    SwitchButton autoSyncSB;

    @Nullable
    @BindView(R.id.setting_auto_sync_on_wifi_sb)
    SwitchButton autoSyncOnWifiSB;

    @BindView(R.id.setting_auto_sync_on_wifi)
    View autoSyncOnWifi;

    @Nullable
    @BindView(R.id.setting_auto_sync_frequency)
    View autoSyncFrequency;

    @BindView(R.id.setting_sync_frequency_summary)
    TextView autoSyncFrequencySummary;

    @BindView(R.id.setting_lab)
    TextView lab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        initToolbar();
        initView();
    }


    private void toggleAutoSyncItem() {
        if (App.i().getUser().isAutoSync()) {
            autoSyncSB.setChecked(true);
            autoSyncOnWifi.setVisibility(View.VISIBLE);
            autoSyncFrequency.setVisibility(View.VISIBLE);
            autoSyncOnWifiSB.setChecked(App.i().getUser().isAutoSyncOnlyWifi());
        } else {
            autoSyncSB.setChecked(false);
            autoSyncOnWifi.setVisibility(View.GONE);
            autoSyncFrequency.setVisibility(View.GONE);
        }
    }

    private void initView() {
        SwitchButton downImgWifi, openLinkMode, autoToggleTheme, enableLogging;
        // autoSyncSB = findViewById(R.id.setting_auto_sync_sb);
        // autoSyncOnWifi.findViewById(R.id.setting_auto_sync_on_wifi_sb);
        // autoSyncFrequency.findViewById(R.id.setting_auto_sync_on_wifi_sb);
        toggleAutoSyncItem();
        autoSyncSB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                User user = App.i().getUser();
                user.setAutoSync(b);
                CoreDB.i().userDao().update(user);
                toggleAutoSyncItem();
            }
        });


        downImgWifi = findViewById(R.id.setting_down_img_sb);
        downImgWifi.setChecked(App.i().getUser().isDownloadImgOnlyWifi());

        clearBeforeDaySummary = findViewById(R.id.setting_clear_day_summary);
        clearBeforeDaySummary.setText(getResources().getString(R.string.clear_day_summary, String.valueOf(App.i().getUser().getCachePeriod())));

        int autoSyncFrequency = App.i().getUser().getAutoSyncFrequency();
        if (autoSyncFrequency >= 60) {
            autoSyncFrequencySummary.setText(getResources().getString(R.string.xx_hour, autoSyncFrequency / 60 + ""));

        } else {
            autoSyncFrequencySummary.setText(getResources().getString(R.string.xx_minute, autoSyncFrequency + ""));
        }
        openLinkMode = findViewById(R.id.setting_link_open_mode_sb);
        openLinkMode.setChecked(App.i().getUser().isOpenLinkBySysBrowser());

        autoToggleTheme = findViewById(R.id.setting_auto_toggle_theme_sb);
        autoToggleTheme.setChecked(App.i().getUser().isAutoToggleTheme());

        enableLogging = findViewById(R.id.setting_enable_log_sb);
        enableLogging.setChecked(CorePref.i().globalPref().getBoolean(Contract.ENABLE_LOGGING, false));

        TextView versionSummary = findViewById(R.id.setting_about_summary);
        PackageManager manager = this.getPackageManager();
        String title;
        try {
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            title = info.versionName;
        } catch (PackageManager.NameNotFoundException unused) {
            title = "";
        }
        versionSummary.setText(title);


        if (BuildConfig.DEBUG) {
            lab.setVisibility(View.VISIBLE);
            lab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(SettingActivity.this, LabActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            });
        }
    }


    public void onSBClick(View view) {
        SwitchButton v = (SwitchButton) view;
        User user = App.i().getUser();
        switch (v.getId()) {
            case R.id.setting_link_open_mode_sb:
                user.setOpenLinkBySysBrowser(v.isChecked());
                break;
            case R.id.setting_auto_toggle_theme_sb:
                user.setAutoToggleTheme(v.isChecked());
                break;
            case R.id.setting_down_img_sb:
                user.setDownloadImgOnlyWifi(v.isChecked());
                break;
            case R.id.setting_enable_log_sb:
                CorePref.i().globalPref().putBoolean(Contract.ENABLE_LOGGING, v.isChecked()).commit();
                // XLog.i("日志：" + MMKV.defaultMMKV().putBoolean(Contract.ENABLE_LOGGING, v.isChecked()).commit());
                CoreLog.init(this, v.isChecked());
                break;
            default:
                break;
        }
        CoreDB.i().userDao().update(user);
    }


    public void onClickAutoSyncFrequencySelect(View view) {
        final int[] minuteArray = this.getResources().getIntArray(R.array.setting_sync_frequency_minute);
        int preSelectTimeFrequency = App.i().getUser().getAutoSyncFrequency();
        int preSelectTimeFrequencyIndex = -1;

        int num = minuteArray.length;
        CharSequence[] timeDescItems = new CharSequence[num];
        for (int i = 0; i < num; i++) {
            if (minuteArray[i] >= 60) {
                timeDescItems[i] = getResources().getString(R.string.xx_hour, minuteArray[i] / 60 + "");
            } else {
                timeDescItems[i] = getResources().getString(R.string.xx_minute, minuteArray[i] + "");
            }
            if (preSelectTimeFrequency == minuteArray[i]) {
                preSelectTimeFrequencyIndex = i;
            }
        }

        final CharSequence[] timeDescArray = timeDescItems;

        new MaterialDialog.Builder(this)
                .title(R.string.sync_frequency)
                .items(timeDescArray)
                .itemsCallbackSingleChoice(preSelectTimeFrequencyIndex, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        User user = App.i().getUser();
                        user.setAutoSyncFrequency(minuteArray[which]);
                        //App.i().getUserBox().put(user);
                        CoreDB.i().userDao().update(user);

                        XLog.i("选择了" + which);
                        autoSyncFrequencySummary.setText(timeDescArray[which]);
                        dialog.dismiss();
                        return true; // allow selection
                    }
                })
                .show();
    }

    public void showClearBeforeDay(View view) {
        final int[] dayValueArray = this.getResources().getIntArray(R.array.setting_clear_day_dialog_item_array);
        int preSelectClearBeforeDay = App.i().getUser().getCachePeriod();
        int preSelectClearBeforeDayIndex = -1;

        int num = dayValueArray.length;
        CharSequence[] dayDescArrayTemp = new CharSequence[num];
        for (int i = 0; i < num; i++) {
            dayDescArrayTemp[i] = getResources().getString(R.string.clear_day_summary, dayValueArray[i] + "");
            if (preSelectClearBeforeDay == dayValueArray[i]) {
                preSelectClearBeforeDayIndex = i;
            }
        }

        final CharSequence[] dayDescArray = dayDescArrayTemp;

        new MaterialDialog.Builder(this)
                .title(R.string.setting_clear_day_title)
                .items(dayDescArray)
                .itemsCallbackSingleChoice(preSelectClearBeforeDayIndex, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        User user = App.i().getUser();
                        user.setCachePeriod(dayValueArray[which]);
                       // App.i().getUserBox().put(user);
                        CoreDB.i().userDao().update(user);

                        clearBeforeDaySummary.setText(dayDescArray[which]);
                        XLog.i("选择了" + which);
                        dialog.dismiss();
                        return true;
                    }
                })
                .show();
    }


    public void showAbout(View view) {
        new MaterialDialog.Builder(this)
                .title(R.string.sigh_with_emotion)
                .content(R.string.sigh_with_emotion_content)
                // .positiveText(R.string.agree)
                // .negativeText(R.string.disagree)
                // .positiveColorRes(R.color.material_red_400)
                // .negativeColorRes(R.color.material_red_400)
                // .positiveColor(Color.WHITE)
                // .negativeColorAttr(android.R.attr.textColorSecondaryInverse)
                // .titleGravity(GravityEnum.CENTER)
                // .titleColorRes(R.color.material_red_400)
                // .contentColorRes(android.R.color.white)
                // .backgroundColorRes(R.color.material_blue_grey_800)
                // .dividerColorRes(R.color.material_teal_a400)
                // .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.POSITIVE)
                // .theme(Theme.DARK)
                .show();
    }


    /****************
     *
     * 发起添加群流程。群号：知微(106211435) 的 key 为： XPc8IGwXCDTPXItxM33eog5QLpLFdDrf
     * 调用 joinQQGroup(XPc8IGwXCDTPXItxM33eog5QLpLFdDrf) 即可发起手Q客户端申请加群 知微(106211435)
     *
     * @param view 对应的控件
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    public void joinQQGroup(View view) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + "XPc8IGwXCDTPXItxM33eog5QLpLFdDrf"));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("QQ", "106211435");
            // 将ClipData内容放到系统剪贴板里。
            assert cm != null;
            cm.setPrimaryClip(mClipData);
            ToastUtils.show(getString(R.string.copy_success));
        }
    }

    public void addAccount() {
        Intent intent = new Intent(this, ProviderActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.out_from_bottom);
    }

    public void escAccount() {
        new MaterialDialog.Builder(SettingActivity.this)
                .content(R.string.do_you_want_to_delete_data_of_this_account_after_logout)
                .neutralText(R.string.cancel)
                .negativeText(R.string.disagree)
                .positiveText(R.string.agree)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        CorePref.i().globalPref().remove(Contract.UID);
                        App.i().clearApiData();
                        //登出
                        MobclickAgent.onProfileSignOff();
                        Intent intent = new Intent(SettingActivity.this, ProviderActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        SettingActivity.this.finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.out_from_bottom);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        CorePref.i().globalPref().remove(Contract.UID);
                        Intent intent = new Intent(SettingActivity.this, ProviderActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        SettingActivity.this.finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.out_from_bottom);
                    }
                })
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    public void onClickSwitchUser(View view) {
        final List<User> users = CoreDB.i().userDao().loadAll();
        XLog.i("点击切换账号：" + users );
        // 弹窗的适配器
        MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(new MaterialSimpleListAdapter.Callback() {
            @Override
            public void onMaterialListItemSelected(MaterialDialog dialog, int index, MaterialSimpleListItem item) {
                dialog.dismiss();
                int count = users.size();
                if (index < count) {
                    User user = users.get(index);
                    CorePref.i().globalPref().putString(Contract.UID, user.getId());
                    //当用户使用第三方账号（如新浪微博）登录时，可以这样统计：
                    MobclickAgent.onProfileSignIn(user.getSource(), user.getUserId());
                    App.i().restartApp();
                } else if (index == count) {
                    addAccount();
                } else if (index == count + 1) {
                    escAccount();
                }
            }
        });
        int iconRefs = R.drawable.ic_rename;
        for (User user : users) {
            switch (user.getSource()) {
                case Contract.PROVIDER_FEEDLY:
                    iconRefs = R.drawable.logo_feedly;
                    break;
                case Contract.PROVIDER_INOREADER:
                    iconRefs = R.drawable.logo_inoreader;
                    break;
                case Contract.PROVIDER_TINYRSS:
                    iconRefs = R.drawable.logo_tinytinyrss;
                    break;
                case Contract.PROVIDER_FEVER:
                    iconRefs = R.drawable.logo_fever;
                    break;
            }
            adapter.add(new MaterialSimpleListItem.Builder(SettingActivity.this)
                    .content( user.getUserName())
                    .icon(iconRefs)
                    .backgroundColor(Color.TRANSPARENT)
                    .build());
        }

        adapter.add(new MaterialSimpleListItem.Builder(SettingActivity.this)
                .content(R.string.add_account)
                // .icon(R.drawable.ic_rename)
                .backgroundColor(Color.TRANSPARENT)
                .build());
        adapter.add(new MaterialSimpleListItem.Builder(SettingActivity.this)
                .content(R.string.esc_account)
                // .icon(R.drawable.ic_rename)
                .backgroundColor(Color.TRANSPARENT)
                .build());
        new MaterialDialog.Builder(this)
                .title(R.string.switch_account)
                .adapter(adapter, new LinearLayoutManager(this))
                .show();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // setDisplayShowHomeEnabled(true)   //使左上角图标是否显示，如果设成false，则没有程序图标，仅仅就个标题，否则，显示应用程序图标，对应id为android.R.id.home，对应ActionBar.DISPLAY_SHOW_HOME
        // setDisplayShowCustomEnabled(true)  // 使自定义的普通View能在title栏显示，即actionBar.setCustomView能起作用，对应ActionBar.DISPLAY_SHOW_CUSTOM
    }

    public void onClickSendLogFile(View view) {
        String fileName = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        File logFile = new File(getExternalCacheDir()  +"/log/" + fileName);
        if(logFile.exists()){
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(logFile.getAbsolutePath()));
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.send_log_file)));
        }else {
            ToastUtils.show(R.string.no_log_file_found);
        }
    }

    public void onClickFeedback(View view) {
        Intent intent = new Intent(SettingActivity.this, WebActivity.class);
        intent.setData(Uri.parse("https://support.qq.com/products/15424"));
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }


    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        mColorfulBuilder
                // .backgroundDrawable(R.id.swipe_layout, R.attr.root_view_bg)
                // 设置view的背景图片
                .backgroundColor(R.id.setting_coordinator, R.attr.root_view_bg)
                // 设置 toolbar
                .backgroundColor(R.id.setting_toolbar, R.attr.topbar_bg)
                // .textColor(R.id.setting_toolbar_count, R.attr.topbar_fg)
                // 设置文章信息
                // .textColor(R.id.setting_sync_first_open_title, R.attr.setting_title)
                // .textColor(R.id.setting_sync_all_starred_title, R.attr.setting_title)
                // .textColor(R.id.setting_sync_frequency_title, R.attr.setting_title)
                // .textColor(R.id.setting_sync_frequency_summary, R.attr.setting_tips)
                .textColor(R.id.setting_clear_day_title, R.attr.setting_title)
                .textColor(R.id.setting_clear_day_summary, R.attr.setting_tips)
                .textColor(R.id.setting_down_img_title, R.attr.setting_title)

                // .textColor(R.id.setting_scroll_mark_title, R.attr.setting_title)
                // .textColor(R.id.setting_scroll_mark_tips, R.attr.setting_tips)
                // .textColor(R.id.setting_order_tagfeed_title, R.attr.setting_title)
                // .textColor(R.id.setting_order_tagfeed_tips, R.attr.setting_tips)
                // .textColor(R.id.setting_link_open_mode_tips, R.attr.setting_tips)
                // .textColor(R.id.setting_cache_path_starred_title, R.attr.setting_title)
                // .textColor(R.id.setting_cache_path_starred_summary, R.attr.setting_tips)
                .textColor(R.id.setting_link_open_mode_title, R.attr.setting_title)
                // .textColor(R.id.setting_license_title, R.attr.setting_title)
                // .textColor(R.id.setting_license_summary, R.attr.setting_tips)
                .textColor(R.id.setting_about_title, R.attr.setting_title)
                .textColor(R.id.setting_about_summary, R.attr.setting_tips);
        return mColorfulBuilder;
    }
}
