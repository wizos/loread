package me.wizos.loread.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.kyleduo.switchbutton.SwitchButton;
import com.socks.library.KLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.wizos.loread.App;
import me.wizos.loread.BuildConfig;
import me.wizos.loread.R;
import me.wizos.loread.bean.config.GlobalConfig;
import me.wizos.loread.data.WithPref;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.view.colorful.Colorful;

/**
 * @author Wizos on 2016/3/10.
 */

public class SettingActivity extends BaseActivity {
    protected static final String TAG = "SettingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        initToolbar();
        initView();
    }


    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        mColorfulBuilder
//                .backgroundDrawable(R.id.swipe_layout, R.attr.root_view_bg)
                // 设置view的背景图片
                .backgroundColor(R.id.setting_coordinator, R.attr.root_view_bg)
                // 设置 toolbar
                .backgroundColor(R.id.setting_toolbar, R.attr.topbar_bg)
//                .textColor(R.id.setting_toolbar_count, R.attr.topbar_fg)
                // 设置文章信息
//                .textColor(R.id.setting_sync_first_open_title, R.attr.setting_title)
//                .textColor(R.id.setting_sync_all_starred_title, R.attr.setting_title)
//                .textColor(R.id.setting_sync_frequency_title, R.attr.setting_title)
//                .textColor(R.id.setting_sync_frequency_summary, R.attr.setting_tips)
                .textColor(R.id.setting_clear_day_title, R.attr.setting_title)
                .textColor(R.id.setting_clear_day_summary, R.attr.setting_tips)
                .textColor(R.id.setting_down_img_title, R.attr.setting_title)

                .textColor(R.id.setting_proxy_title, R.attr.setting_title)

//                .textColor(R.id.setting_scroll_mark_title, R.attr.setting_title)
//                .textColor(R.id.setting_scroll_mark_tips, R.attr.setting_tips)
//                .textColor(R.id.setting_order_tagfeed_title, R.attr.setting_title)
//                .textColor(R.id.setting_order_tagfeed_tips, R.attr.setting_tips)
//                .textColor(R.id.setting_link_open_mode_tips, R.attr.setting_tips)
//                .textColor(R.id.setting_cache_path_starred_title, R.attr.setting_title)
//                .textColor(R.id.setting_cache_path_starred_summary, R.attr.setting_tips)
                .textColor(R.id.setting_link_open_mode_title, R.attr.setting_title)
                .textColor(R.id.setting_license_title, R.attr.setting_title)
                .textColor(R.id.setting_license_summary, R.attr.setting_tips)
                .textColor(R.id.setting_about_title, R.attr.setting_title)
                .textColor(R.id.setting_about_summary, R.attr.setting_tips);
        return mColorfulBuilder;
    }


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

//    TextView startTimeTextView;
//    TextView endTimeTextView;

    @BindView(R.id.setting_backup)
    TextView backup;

    @BindView(R.id.setting_restore)
    TextView restore;

    @BindView(R.id.setting_read_config)
    TextView readConfig;


//    @OnClick(R.id.setting_night_time_interval)
//    public void onClickNightTimeInterval(View view){
//        MaterialDialog dialog = new MaterialDialog.Builder(this)
//                .title("夜间时间段")
//                .customView(R.layout.select_night_time_interval_view, true)
//                .positiveText("确认")
//                .negativeText("取消")
//                .onPositive(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        WithPref.i().setNightThemeThemeStartTime(startTime);
//                        WithPref.i().setNightThemeEndTime(endTime);
//                    }
//                }).build();
//        dialog.show();
//
//        startTimeTextView = (TextView) dialog.findViewById(R.id.start);
//        endTimeTextView = (TextView) dialog.findViewById(R.id.end);
//
//        CircleAlarmTimerView circleAlarmTimerView = (CircleAlarmTimerView) dialog.findViewById(R.id.circletimerview);
//        circleAlarmTimerView.setOnTimeChangedListener(new CircleAlarmTimerView.OnTimeChangedListener() {
//            @Override
//            public void start(String starting) {
//                startTimeTextView.setText(starting);
//                startTime = starting;
//            }
//            @Override
//            public void end(String ending) {
//                endTimeTextView.setText(ending);
//                endTime = ending;
//            }
//        });
//
//        circleAlarmTimerView.getPaddingStart();
//    }
//    private String startTime;
//    private String endTime;


    private void toggleAutoSyncItem() {
        if (WithPref.i().isAutoSync()) {
            autoSyncSB.setChecked(true);
            autoSyncOnWifi.setVisibility(View.VISIBLE);
            autoSyncFrequency.setVisibility(View.VISIBLE);
            autoSyncOnWifiSB.setChecked(WithPref.i().isAutoSyncOnWifi());
        } else {
            autoSyncSB.setChecked(false);
            autoSyncOnWifi.setVisibility(View.GONE);
            autoSyncFrequency.setVisibility(View.GONE);
        }
    }

    private void initView(){
        SwitchButton downImgWifi, sysBrowserOpenLink, autoToggleTheme;
        toggleAutoSyncItem();
        autoSyncSB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                WithPref.i().setAutoSync(b);
                toggleAutoSyncItem();
            }
        });


        downImgWifi = findViewById(R.id.setting_down_img_sb);
        downImgWifi.setChecked(WithPref.i().isDownImgOnlyWifi());

        clearBeforeDaySummary = findViewById(R.id.setting_clear_day_summary);
        clearBeforeDaySummary.setText(getResources().getString(R.string.clear_day_summary, String.valueOf(WithPref.i().getClearBeforeDay())));

        int autoSyncFrequency = WithPref.i().getAutoSyncFrequency();
        if (autoSyncFrequency >= 60) {
            autoSyncFrequencySummary.setText(getResources().getString(R.string.xx_hour, autoSyncFrequency / 60 + ""));

        } else {
            autoSyncFrequencySummary.setText(getResources().getString(R.string.xx_minute, autoSyncFrequency + ""));
        }
        sysBrowserOpenLink = findViewById(R.id.setting_link_open_mode_sb);
        sysBrowserOpenLink.setChecked(WithPref.i().isSysBrowserOpenLink());
//        View openLinkMode = findViewById(R.id.setting_link_open_mode);
//        if (!BuildConfig.DEBUG) {
//            openLinkMode.setVisibility(View.GONE);
//        }

        autoToggleTheme = findViewById(R.id.setting_auto_toggle_theme_sb);
        autoToggleTheme.setChecked(WithPref.i().isAutoToggleTheme());

//        clearLog = (Button)findViewById(R.id.setting_clear_log_button);

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
//            TextView handleSavedPages = findViewById(R.id.setting_handle_saved_pages);
//            handleSavedPages.setVisibility(View.VISIBLE);

            backup.setVisibility(View.VISIBLE);
            restore.setVisibility(View.VISIBLE);
            readConfig.setVisibility(View.VISIBLE);
        }
    }


//    private void getClearBeforeDayIndex(){
////        CharSequence[] items = this.getResources().getTextArray(R.array.setting_clear_day_dialog_item_array);
//        final int[] dayValueItems = this.getResources().getIntArray(R.array.setting_clear_day_dialog_item_array);
//        int num = dayValueItems.length;
//        for(int i=0; i< num; i++){
//            if ( clearBeforeDay == dayValueItems[i] ){
//                clearBeforeDayIndex = i;
//            }
//        }
//        KLog.i( "读取默认的选项"+  clearBeforeDayIndex );
//    }


    public void onSBClick(View view){
        SwitchButton v = (SwitchButton)view;
        KLog.i("点击");
        switch (v.getId()) {
            case R.id.setting_link_open_mode_sb:
                WithPref.i().setSysBrowserOpenLink(v.isChecked());
                break;
            case R.id.setting_auto_toggle_theme_sb:
                WithPref.i().setAutoToggleTheme(v.isChecked());
                break;
            case R.id.setting_down_img_sb:
                WithPref.i().setDownImgWifi(v.isChecked());
                break;
            case R.id.setting_proxy_sb:
                WithPref.i().setInoreaderProxy(v.isChecked());
                if (v.isChecked()) {
                    showInoreaderProxyHostDialog();
                } else {
//                    App.i().readHost();
                }
                break;
//            case R.id.setting_sync_all_starred_sb_flyme:
//                WithPref.i().setSyncAllStarred(v.isChecked());
//                syncAllStarred();
//                break;
//            case R.id.setting_order_tagfeed_sb_flyme:
//                WithPref.i().setOrderTagFeed(v.isChecked());
//                break;
//            case R.id.setting_scroll_mark_sb_flyme:
//                WithPref.i().setScrollMark(v.isChecked());
//                break;
//            case R.id.setting_left_right_slide_sb_flyme:
//                WithPref.i().setLeftRightSlideArticle(v.isChecked());
//                break;
            default:
                break;
        }
//        KLog.i("Switch: " , v.isChecked() );
    }


    private void showInoreaderProxyHostDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.setting_proxy_title)
//                .content(R.string.setting_inoreader_proxy_dialog_title)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRange(8, 34)
                .input(null, WithPref.i().getInoreaderProxyHost(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        WithPref.i().setInoreaderProxyHost(input.toString());
//                        App.i().readHost();
                    }
                })
                .positiveText("保存")
                .show();
    }


    public void onClickAutoSyncFrequencySelect(View view) {
        final int[] minuteArray = this.getResources().getIntArray(R.array.setting_sync_frequency_minute);
        int preSelectTimeFrequency = WithPref.i().getAutoSyncFrequency();
        int preSelectTimeFrequencyIndex = -1;

        int num = minuteArray.length;
        CharSequence[] timeDescItems = new CharSequence[num];
        for (int i = 0; i < num; i++) {
            if (minuteArray[i] >= 60) {
                timeDescItems[i] = getResources().getString(R.string.xx_hour, minuteArray[i] / 60 + "");
            }else {
                timeDescItems[i] = getResources().getString(R.string.xx_minute, minuteArray[i] + "");
            }
            if (preSelectTimeFrequency == minuteArray[i]) {
                preSelectTimeFrequencyIndex = i;
            }
        }

        final CharSequence[] timeDescArray = timeDescItems;

        new MaterialDialog.Builder(this)
                .title(R.string.setting_sync_frequency_title)
                .items(timeDescArray)
                .itemsCallbackSingleChoice(preSelectTimeFrequencyIndex, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        WithPref.i().setAutoSyncFrequency(minuteArray[which]);
                        KLog.i("选择了" + which);
                        autoSyncFrequencySummary.setText(timeDescArray[which]);
                        dialog.dismiss();
                        return true; // allow selection
                    }
                })
                .show();
    }

    public void showClearBeforeDay(View view) {
        final int[] dayValueArray = this.getResources().getIntArray(R.array.setting_clear_day_dialog_item_array);
        int preSelectClearBeforeDay = WithPref.i().getClearBeforeDay();
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
                        WithPref.i().setClearBeforeDay(dayValueArray[which]);
                        clearBeforeDaySummary.setText(dayDescArray[which]);
                        KLog.i("选择了" + which);
                        dialog.dismiss();
                        return true;
                    }
                })
                .show();
    }


    public void showAbout(View view) {
        new MaterialDialog.Builder(this)
                .title(R.string.setting_about_dialog_title)
                .content(R.string.setting_about_dialog_content)
//                    .positiveText(R.string.agree)
//                    .negativeText(R.string.disagree)
//                    .positiveColorRes(R.color.material_red_400)
//                    .negativeColorRes(R.color.material_red_400)
//                    .positiveColor(Color.WHITE)
//                    .negativeColorAttr(android.R.attr.textColorSecondaryInverse)
//                    .titleGravity(GravityEnum.CENTER)
//                    .titleColorRes(R.color.material_red_400)
//                    .contentColorRes(android.R.color.white)
//                    .backgroundColorRes(R.color.material_blue_grey_800)
//                    .dividerColorRes(R.color.material_teal_a400)
//                    .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.POSITIVE)
//                    .theme(Theme.DARK)
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
    public boolean joinQQGroup(View view) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + "XPc8IGwXCDTPXItxM33eog5QLpLFdDrf"));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }


    private MaterialDialog materialDialog;

    public void onClickEsc(View view) {
        new MaterialDialog.Builder(SettingActivity.this)
                .content("确定要退出账号吗？\n退出后所有数据将被删除！")
                .negativeText("取消")
                .positiveText(R.string.agree)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        App.i().clearApiData();
                        Intent intent = new Intent(SettingActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        SettingActivity.this.finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.out_from_bottom);
                    }
                })
                .show();
    }


    public void onClickBackup(View view) {
        materialDialog = new MaterialDialog.Builder(this)
                .title("正在处理")
                .content("请耐心等待下")
                .progress(true, 0)
                .canceledOnTouchOutside(false)
                .progressIndeterminateStyle(false)
                .show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtil.backup();
                materialDialog.dismiss();
            }
        }).start();

    }

    public void onClickRestore(View view) {
        materialDialog = new MaterialDialog.Builder(this)
                .title("正在处理")
                .content("请耐心等待下")
                .progress(true, 0)
                .canceledOnTouchOutside(false)
                .progressIndeterminateStyle(false)
                .show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtil.restore();
                materialDialog.dismiss();
            }
        }).start();
    }

    public void onClickReadConfig(View view) {
        materialDialog = new MaterialDialog.Builder(this)
                .content("正在读取")
                .progress(true, 0)
                .canceledOnTouchOutside(false)
                .progressIndeterminateStyle(false)
                .show();
//        App.i().initFeedsConfig();
        GlobalConfig.i().reInit();
        materialDialog.dismiss();
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

    public void onClickFeedback(View view) {
        Intent intent = new Intent(SettingActivity.this, WebActivity.class);
        intent.setData(Uri.parse("https://support.qq.com/products/15424"));
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
