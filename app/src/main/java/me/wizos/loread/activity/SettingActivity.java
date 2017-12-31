package me.wizos.loread.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kyleduo.switchbutton.SwitchButton;
import com.socks.library.KLog;

import java.io.File;
import java.util.List;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.Article;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.view.colorful.Colorful;


public class SettingActivity extends BaseActivity {
    protected static final String TAG = "SettingActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initToolbar();
        initView();

    }

//    @Override
//    protected void onResume(){
//        super.onResume();
//        getSupportActionBar().setTitle(R.string.setting_activity_title);
//    }

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
                .textColor(R.id.setting_sync_first_open_title, R.attr.setting_title)
                .textColor(R.id.setting_sync_first_open_tips, R.attr.setting_tips)
                .textColor(R.id.setting_sync_all_starred_title, R.attr.setting_title)
                .textColor(R.id.setting_sync_all_starred_tips, R.attr.setting_tips)
//                .textColor(R.id.setting_sync_frequency_title, R.attr.setting_title)
//                .textColor(R.id.setting_sync_frequency_summary, R.attr.setting_tips)
                .textColor(R.id.setting_clear_day_title, R.attr.setting_title)
                .textColor(R.id.setting_clear_day_summary, R.attr.setting_tips)
                .textColor(R.id.setting_down_img_title, R.attr.setting_title)
                .textColor(R.id.setting_down_img_tips, R.attr.setting_tips)

                .textColor(R.id.setting_inoreader_proxy_title, R.attr.setting_title)
                .textColor(R.id.setting_inoreader_proxy_tips, R.attr.setting_tips)
//                .textColor(R.id.setting_scroll_mark_title, R.attr.setting_title)
//                .textColor(R.id.setting_scroll_mark_tips, R.attr.setting_tips)
                .textColor(R.id.setting_order_tagfeed_title, R.attr.setting_title)
                .textColor(R.id.setting_order_tagfeed_tips, R.attr.setting_tips)
                .textColor(R.id.setting_link_open_mode_title, R.attr.setting_title)
                .textColor(R.id.setting_link_open_mode_tips, R.attr.setting_tips)
                .textColor(R.id.setting_cache_path_starred_title, R.attr.setting_title)
                .textColor(R.id.setting_cache_path_starred_summary, R.attr.setting_tips)
                .textColor(R.id.setting_license_title, R.attr.setting_title)
                .textColor(R.id.setting_license_summary, R.attr.setting_tips)
                .textColor(R.id.setting_about_title, R.attr.setting_title)
                .textColor(R.id.setting_about_summary, R.attr.setting_tips);
        return mColorfulBuilder;
    }


    //    private SwitchButton syncFirstOpen, downImgWifi, inoreaderProxy, orderTagFeed, syncAllStarred, sysBrowserOpenLink, autoToggleTheme; // , leftRightSlideArticle
//    scrollMark,
//    private Button clearLog;
    private TextView clearBeforeDaySummary;
    private int clearBeforeDayIndex, clearBeforeDay;

    private void initView(){
        SwitchButton syncFirstOpen, downImgWifi, inoreaderProxy, orderTagFeed, syncAllStarred, sysBrowserOpenLink, autoToggleTheme;
        syncFirstOpen = (SwitchButton) findViewById(R.id.setting_sync_first_open_sb_flyme);
        syncFirstOpen.setChecked(WithSet.i().isSyncFirstOpen());

        syncAllStarred = (SwitchButton) findViewById(R.id.setting_sync_all_starred_sb_flyme);
        syncAllStarred.setChecked(WithSet.i().isSyncAllStarred());

        downImgWifi = (SwitchButton) findViewById(R.id.setting_down_img_sb_flyme);
        downImgWifi.setChecked(WithSet.i().isDownImgWifi());

        inoreaderProxy = (SwitchButton) findViewById(R.id.setting_inoreader_proxy_sb_flyme);
        inoreaderProxy.setChecked(WithSet.i().isInoreaderProxy());
//        scrollMark = (SwitchButton) findViewById(R.id.setting_scroll_mark_sb_flyme);
//        scrollMark.setChecked(WithSet.i().isScrollMark());
        orderTagFeed = (SwitchButton) findViewById(R.id.setting_order_tagfeed_sb_flyme);
        orderTagFeed.setChecked(WithSet.i().isOrderTagFeed());

        clearBeforeDaySummary = (TextView) findViewById(R.id.setting_clear_day_summary);
        clearBeforeDay = WithSet.i().getClearBeforeDay();
        clearBeforeDaySummary.setText(getResources().getString(R.string.clear_day_summary, String.valueOf(clearBeforeDay)));


        sysBrowserOpenLink = (SwitchButton) findViewById(R.id.setting_link_open_mode_sb_flyme);
        sysBrowserOpenLink.setChecked(WithSet.i().isSysBrowserOpenLink());

        autoToggleTheme = (SwitchButton) findViewById(R.id.setting_auto_toggle_theme_sb_flyme);
        autoToggleTheme.setChecked(WithSet.i().isAutoToggleTheme());

//        leftRightSlideArticle = (SwitchButton) findViewById(R.id.setting_left_right_slide_sb_flyme);
//        leftRightSlideArticle.setChecked(WithSet.i().isLeftRightSlideArticle());

//        clearLog = (Button)findViewById(R.id.setting_clear_log_button);
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
        KLog.d( "点击" );
        switch (v.getId()) {
            case R.id.setting_sync_first_open_sb_flyme:
                WithSet.i().setSyncFirstOpen(v.isChecked());
                break;
            case R.id.setting_sync_all_starred_sb_flyme:
                WithSet.i().setSyncAllStarred(v.isChecked());
                syncAllStarred();
                break;
            case R.id.setting_down_img_sb_flyme:
                WithSet.i().setDownImgWifi(v.isChecked());
                break;
            case R.id.setting_inoreader_proxy_sb_flyme:
                WithSet.i().setInoreaderProxy(v.isChecked());
                if (v.isChecked()) {
                    showInoreaderProxyHostDialog();
                } else {
                    App.i().readHost();
                }
                break;
            case R.id.setting_order_tagfeed_sb_flyme:
                WithSet.i().setOrderTagFeed(v.isChecked());
                break;
            case R.id.setting_link_open_mode_sb_flyme:
                WithSet.i().setSysBrowserOpenLink(v.isChecked());
                break;
            case R.id.setting_auto_toggle_theme_sb_flyme:
                WithSet.i().setAutoToggleTheme(v.isChecked());
                break;
//            case R.id.setting_scroll_mark_sb_flyme:
//                WithSet.i().setScrollMark(v.isChecked());
//                break;
//            case R.id.setting_left_right_slide_sb_flyme:
//                WithSet.i().setLeftRightSlideArticle(v.isChecked());
//                break;
        }
//        KLog.d("Switch: " , v.isChecked() );
    }

    private void syncAllStarred() {
        KLog.i("【获取所有加星文章1】");
        if (WithSet.i().isSyncAllStarred()) { // !WithSet.i().isHadSyncAllStarred() &&
            Intent intent = new Intent(this, MainService.class);
            intent.setAction("syncAllStarred");
            startService(intent);
            KLog.i("【获取所有加星文章2】");
        }
    }

    private void showInoreaderProxyHostDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.setting_inoreader_proxy_dialog_title)
//                .content(R.string.setting_inoreader_proxy_dialog_title)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRange(8, 34)
                .input(null, WithSet.i().getInoreaderProxyHost(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        WithSet.i().setInoreaderProxyHost(input.toString());
                        App.i().readHost();
                    }
                })
                .positiveText("保存")
                .show();
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
        List<Article> allArts = WithDB.i().loadAllArts();
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
            String articleIdToMD5 = StringUtil.stringToMD5(item.getId());
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
                FileUtil.deleteHtmlDir(new File(App.cacheRelativePath + entry.getKey()));
            }
        }


    }

    public void showClearBeforeDay(View view) {
//        final CharSequence[] dayValueItems = this.getResources().getTextArray(R.array.setting_clear_day_dialog_item_array);
        final int[] dayValueItems = this.getResources().getIntArray(R.array.setting_clear_day_dialog_item_array);
        int num = dayValueItems.length;
        CharSequence[] dayDescItems = new CharSequence[num];
        for (int i = 0; i < num; i++) {
            dayDescItems[i] = getResources().getString(R.string.clear_day_summary, dayValueItems[i] + "");
            if (clearBeforeDay == dayValueItems[i]) {
                clearBeforeDayIndex = i;
            }
        }
//        getClearBeforeDayIndex();
//        final int[] dayValueItems = this.getResources().getIntArray(R.array.setting_clear_day_dialog_item_array);
//        int num = dayValueItems.length;
//        for(int i=0; i< num; i++){
//            if ( clearBeforeDay == dayValueItems[i] ){
//                clearBeforeDayIndex = i;
//            }
//        }

        new MaterialDialog.Builder(this)
                .title(R.string.setting_clear_day_dialog_title)
                .items(dayDescItems)
                .itemsCallbackSingleChoice( clearBeforeDayIndex, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        clearBeforeDay = dayValueItems[which];
                        clearBeforeDayIndex = which;
                        WithSet.i().setClearBeforeDay(clearBeforeDay);
                        KLog.i("选择了" + clearBeforeDayIndex);
                        clearBeforeDaySummary.setText(getResources().getString(R.string.clear_day_summary, clearBeforeDay + ""));
                        dialog.dismiss();
                        return true; // allow selection
                    }
                })
//                .positiveText(R.string.choose_label)
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

    public void onClickFeedback(View view) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        intent.setData(Uri.parse("http://support.qq.com/embed/15424"));
        startActivity(intent); // 目前无法坐那那种可以选择打开方式的
    }

    public void onClickEsc(View view) {
        App.i().clearApiData();
        Intent intent = new Intent(SettingActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        this.finish();
    }

    public void clearLog(View view) {
        WithDB.i().delRequestListAll();
    }


    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.setting_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true); // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // setDisplayShowHomeEnabled(true)   //使左上角图标是否显示，如果设成false，则没有程序图标，仅仅就个标题，否则，显示应用程序图标，对应id为android.R.id.home，对应ActionBar.DISPLAY_SHOW_HOME
        // setDisplayShowCustomEnabled(true)  // 使自定义的普通View能在title栏显示，即actionBar.setCustomView能起作用，对应ActionBar.DISPLAY_SHOW_CUSTOM
    }
}
