package me.wizos.loread.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.kyleduo.switchbutton.SwitchButton;
import com.socks.library.KLog;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.wizos.loread.R;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithSet;

public class SettingActivity extends BaseActivity {
    protected static final String TAG = "SettingActivity";
    private Context context;
    private Toolbar toolbar;

    private Thread mThread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        context = this ;
        ButterKnife.bind(this);
        initToolbar();
        initView();
        readSettingAndChangeView();
    }
    @Override
    protected Context getActivity(){
        return context;
    }
    public String getTAG(){
        return TAG;
    }
    @Override
    protected void notifyDataChanged(){
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mThread != null && !mThread.isInterrupted() && mThread.isAlive())
            mThread.interrupt();
    }
    @Override
    public void onClick(View v) {
    }


    private SwitchButton syncFirstOpen,syncAllStarred,downImgWifi,inoreaderProxy,scrollMark,orderTagFeed;
    private TextView clearBeforeDaySummary;
    private Button clearLog;
    private int clearBeforeDayIndex, clearBeforeDay;

    private void initView(){
        syncFirstOpen = (SwitchButton) findViewById(R.id.setting_sync_first_open_sb_flyme);
        syncAllStarred = (SwitchButton) findViewById(R.id.setting_sync_all_starred_sb_flyme);
        downImgWifi = (SwitchButton) findViewById(R.id.setting_down_img_sb_flyme);
        inoreaderProxy = (SwitchButton) findViewById(R.id.setting_inoreader_proxy_sb_flyme) ;
        scrollMark = (SwitchButton) findViewById(R.id.setting_scroll_mark_sb_flyme);
        orderTagFeed = (SwitchButton) findViewById(R.id.setting_order_tagfeed_sb_flyme);
        clearBeforeDaySummary = (TextView) findViewById(R.id.setting_clear_day_summary);
//        clearLog = (Button)findViewById(R.id.setting_clear_log_button);
    }

    protected void readSettingAndChangeView(){
        syncFirstOpen.setChecked(WithSet.getInstance().isSyncFirstOpen());
        syncAllStarred.setChecked(WithSet.getInstance().isSyncAllStarred());
        downImgWifi.setChecked(WithSet.getInstance().isDownImgWifi());
        inoreaderProxy.setChecked(WithSet.getInstance().isInoreaderProxy());
        scrollMark.setChecked(WithSet.getInstance().isScrollMark());
        orderTagFeed.setChecked(WithSet.getInstance().isOrderTagFeed());
        clearBeforeDay = WithSet.getInstance().getClearBeforeDay();
        changeViewSummary();
        KLog.d( "读取默认的选项"+  clearBeforeDayIndex );
    }

    private void changeViewSummary(){
        CharSequence[] items = this.context.getResources().getTextArray( R.array.setting_clear_day_dialog_item_array );
        int num = items.length;
        for(int i=0; i< num; i++){
            if (clearBeforeDay == Integer.valueOf(items[i].toString().replace(" 天",""))){
                clearBeforeDayIndex = i;
            }
        }
        clearBeforeDaySummary.setText( clearBeforeDay +" 天");
    }

    public void onSBClick(View view){
        SwitchButton v = (SwitchButton)view;
        KLog.d( "点击" );
        switch (v.getId()) {
            case R.id.setting_sync_first_open_sb_flyme:
                WithSet.getInstance().setSyncFirstOpen(v.isChecked());
                break;
            case R.id.setting_sync_all_starred_sb_flyme:
                WithSet.getInstance().setSyncAllStarred(v.isChecked());
                break;
            case R.id.setting_down_img_sb_flyme:
                WithSet.getInstance().setDownImgWifi(v.isChecked());
                break;
            case R.id.setting_inoreader_proxy_sb_flyme:
                WithSet.getInstance().setInoreaderProxy(v.isChecked());
                break;
            case R.id.setting_scroll_mark_sb_flyme:
                WithSet.getInstance().setScrollMark(v.isChecked());
                break;
            case R.id.setting_order_tagfeed_sb_flyme:
                WithSet.getInstance().setOrderTagFeed(v.isChecked());
                break;
        }
        KLog.d("Switch: " , v.isChecked() );
    }
    public void onClickUpdateArticle(View view){
//        Intent data = new Intent();
//        data.putExtra("source", "updateArticles" );
        SettingActivity.this.setResult( 2);//注意下面的RESULT_OK常量要与回传接收的Activity中onActivityResult（）方法一致
        SettingActivity.this.finish();
    }

    @OnClick(R.id.setting_clear_day_title) void showClearBeforeDay() {
        KLog.d( clearBeforeDayIndex );
        new MaterialDialog.Builder(this)
                .title(R.string.setting_clear_day_dialog_title)
                .items(R.array.setting_clear_day_dialog_item_array)
                .itemsCallbackSingleChoice( clearBeforeDayIndex, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
//                        System.out.println("选择了");
                        String temp = String.valueOf(text);
                        clearBeforeDay = Integer.valueOf(temp.replace(" 天",""));
                        clearBeforeDayIndex = which;
                        WithSet.getInstance().setClearBeforeDay( clearBeforeDay );
                        System.out.println("{][][]"+ clearBeforeDayIndex);
                        changeViewSummary();
                        dialog.dismiss();
                        return true; // allow selection
                    }
                })
//                .positiveText(R.string.choose_label)
                .show();
    }

    @OnClick(R.id.setting_about) void showAbout() {
            new MaterialDialog.Builder(this)
                    .title(R.string.setting_about_dialog_title)
                    .content(R.string.setting_about_dialog_content)
                    .positiveText(R.string.agree)
                    .negativeText(R.string.disagree)
                    .positiveColorRes(R.color.material_red_400)
                    .negativeColorRes(R.color.material_red_400)
                    .titleGravity(GravityEnum.CENTER)
                    .titleColorRes(R.color.material_red_400)
                    .contentColorRes(android.R.color.white)
                    .backgroundColorRes(R.color.material_blue_grey_800)
                    .dividerColorRes(R.color.material_teal_a400)
                    .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.POSITIVE)
                    .positiveColor(Color.WHITE)
                    .negativeColorAttr(android.R.attr.textColorSecondaryInverse)
                    .theme(Theme.DARK)
                    .show();
    }

    @OnClick(R.id.setting_clear_log_button) void clearLog() {
        WithDB.getInstance().delRequestListAll();
    }


    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setOnClickListener(this);
        // setDisplayShowHomeEnabled(true)   //使左上角图标是否显示，如果设成false，则没有程序图标，仅仅就个标题，否则，显示应用程序图标，对应id为android.R.id.home，对应ActionBar.DISPLAY_SHOW_HOME
        // setDisplayShowCustomEnabled(true)  // 使自定义的普通View能在title栏显示，即actionBar.setCustomView能起作用，对应ActionBar.DISPLAY_SHOW_CUSTOM
    }
}
