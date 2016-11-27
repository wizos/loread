package me.wizos.loread.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
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

import java.io.File;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.Article;
import me.wizos.loread.colorful.Colorful;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.utils.UFile;
import me.wizos.loread.utils.UString;

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
        initColorful();
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


    protected void initColorful(){
        mColorful = new Colorful.Builder(this)
//                .backgroundDrawable(R.id.swipe_layout, R.attr.root_view_bg)
                // 设置view的背景图片
                .backgroundColor(R.id.setting_coordinator, R.attr.root_view_bg)
                // 设置 toolbar
                .backgroundColor(R.id.setting_toolbar, R.attr.topbar_bg)
                .textColor(R.id.setting_toolbar_count, R.attr.topbar_fg)
                // 设置文章信息
                .textColor(R.id.setting_sync_first_open_title, R.attr.setting_title)
                .textColor(R.id.setting_sync_first_open_tips, R.attr.setting_tips)
                .textColor(R.id.setting_sync_all_starred_title, R.attr.setting_title)
                .textColor(R.id.setting_sync_all_starred_tips, R.attr.setting_tips)
                .textColor(R.id.setting_sync_frequency_title, R.attr.setting_title)
                .textColor(R.id.setting_sync_frequency_summary, R.attr.setting_tips)
                .textColor(R.id.setting_clear_day_title, R.attr.setting_title)
                .textColor(R.id.setting_clear_day_summary, R.attr.setting_tips)
                .textColor(R.id.setting_down_img_title, R.attr.setting_title)
                .textColor(R.id.setting_down_img_tips, R.attr.setting_tips)

                .textColor(R.id.setting_inoreader_proxy_title, R.attr.setting_title)
                .textColor(R.id.setting_inoreader_proxy_tips, R.attr.setting_tips)
                .textColor(R.id.setting_scroll_mark_title, R.attr.setting_title)
                .textColor(R.id.setting_scroll_mark_tips, R.attr.setting_tips)
                .textColor(R.id.setting_order_tagfeed_title, R.attr.setting_title)
                .textColor(R.id.setting_order_tagfeed_tips, R.attr.setting_tips)
                .textColor(R.id.setting_cache_path_starred_title, R.attr.setting_title)
                .textColor(R.id.setting_cache_path_starred_summary, R.attr.setting_tips)
                .textColor(R.id.setting_license_title, R.attr.setting_title)
                .textColor(R.id.setting_license_summary, R.attr.setting_tips)
                .textColor(R.id.setting_about_title, R.attr.setting_title)
                .textColor(R.id.setting_about_summary, R.attr.setting_tips)


                .create(); // 创建Colorful对象
        autoToggleThemeSetting();
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
    public void onClickClearErrorCache(View view){
        clearAll();
    }
    private void clearAll(){
        List<Article> allArts = WithDB.getInstance().loadArtAll();
        KLog.i("清除" +  "--"+  allArts.size()  + "--" );
        if( allArts.size()==0){return;}

        File dir =  new File( App.cacheRelativePath ) ;
        File[] files = dir.listFiles();

        Map<String,Integer> map = new ArrayMap<>( allArts.size() + files.length );

        // 数据量大的一方
        for( File file : files ){
            map.put(file.getName(), 1);
            KLog.d( "存在文件："+ file.getName() ); // 存在3类文件 md5(历史遗留)、md5_files、md5.html
        }

        // 数据量小的一方
        for ( Article item : allArts ) {
            String articleIdToMD5 = UString.stringToMD5(item.getId());
            Integer cc = map.get( articleIdToMD5 + ".html" );
            if(cc!=null) {
                map.put( articleIdToMD5 + ".html" , ++cc);
                map.put( articleIdToMD5 + "_files" , 2 );  // 1，去掉
                KLog.d( "重复："+ articleIdToMD5 + "==" + cc);
            }else {
//                map.put( articleIdToMD5 , 0);
                KLog.d( "不重复："+ articleIdToMD5 );
            }
        }
        // 遍历结果
        for( Map.Entry<String, Integer> entry: map.entrySet()) {
            if(entry.getValue()==1) {
                // 删除
                KLog.d( "多余文件："+ entry.getKey() );
                UFile.deleteHtmlDir( new File( App.cacheRelativePath + entry.getKey())  );
            }
        }


    }

    @OnClick(R.id.setting_clear_day_title) void showClearBeforeDay() {

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
                        KLog.d( clearBeforeDayIndex );
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
