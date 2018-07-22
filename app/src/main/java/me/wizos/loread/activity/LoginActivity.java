package me.wizos.loread.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.kyleduo.switchbutton.SwitchButton;
import com.socks.library.KLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

import me.wizos.loread.R;
import me.wizos.loread.data.WithPref;
import me.wizos.loread.event.Login;
import me.wizos.loread.net.Api;
import me.wizos.loread.service.MainService;
import me.wizos.loread.utils.ToastUtil;
import me.wizos.loread.view.colorful.Colorful;

/**
 * @author Wizos on 2016/3/5.
 */
public class LoginActivity extends BaseActivity implements View.OnLayoutChangeListener{
    protected static final String TAG = "LoginActivity";
    protected String mAccountID = "";
    protected String mAccountPD = "";
    private EditText idEditText, pdEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        KLog.e("【未登录】");
        forInput();
        initView();
        recoverData();
//        initBroadcast();
        //注册
        EventBus.getDefault().register(this);
    }


    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        return mColorfulBuilder;
    }


    @Override
    public void onDestroy() {
        manager.unregisterReceiver(localReceiver);
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }


    /**
     * 事件响应方法
     * 接收消息
     *
     * @param loginEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Login loginEvent) {
        if (loginEvent.isSuccess()) {
            Intent intentToActivity = new Intent(LoginActivity.this, MainActivity.class);
            intentToActivity.setAction("firstSetupStart");
            startActivity(intentToActivity);
            LoginActivity.this.finish();
        } else {
            loginButton.setEnabled(true);
            idEditText.setEnabled(true);
            pdEditText.setEnabled(true);
            ToastUtil.showLong(getString(R.string.tips_login_failure));
        }
    }

    private void initView() {
        idEditText = findViewById(R.id.edittext_id);
        pdEditText = findViewById(R.id.edittext_pd);
        loginButton = findViewById(R.id.login_button_login);
        SwitchButton inoreaderProxy = findViewById(R.id.setting_proxy_sb);
        inoreaderProxy.setChecked(WithPref.i().isInoreaderProxy());
    }


    /**
     * 默认填充密码
     */
    private void recoverData() {
        mAccountID = WithPref.i().getAccountID();
        mAccountPD = WithPref.i().getAccountPD();
        if (!TextUtils.isEmpty(mAccountID)) {
            idEditText.setText(mAccountID);
        }
        if (!TextUtils.isEmpty(mAccountPD)) {
            pdEditText.setText(mAccountPD);
        }
    }

    private void forInput() {
        activityRootView = findViewById(R.id.login_scroll);
        screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();//获取屏幕高度
        keyHeight = screenHeight / 3; //阀值设置为屏幕高度的1/3
    }

    //Activity最外层的Layout视图
    private View activityRootView;
    //屏幕高度
    private int screenHeight = 0;
    //软件盘弹起后所占高度阀值
    private int keyHeight = 0;
    @Override
    protected void onResume() {
        super.onResume();
        //添加layout大小发生改变监听器
        activityRootView.addOnLayoutChangeListener(this);
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right,int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//        Space space = findViewById(R.id.login_space_a);
//        if(oldBottom != 0 && bottom != 0 &&(oldBottom - bottom > keyHeight)){
////            if(space.getVisibility() == View.VISIBLE){
////                space.setVisibility(View.GONE);
////            }
//        }else if(oldBottom != 0 && bottom != 0 &&(bottom - oldBottom > keyHeight)){
////            if(space.getVisibility() == View.GONE){
////                space.setVisibility(View.VISIBLE);
////            }
//        }
    }


    public void onLoginClicked(View view) {
        mAccountID = idEditText.getText().toString();
        mAccountPD = pdEditText.getText().toString();

        if(mAccountID==null) {
            ToastUtil.showLong(getString(R.string.tips_login_failure));
            return;
        }else if(mAccountID.length()<4)
            if(!mAccountID.contains("@") || mAccountID.contains(" ")) {
                ToastUtil.showLong(getString(R.string.tips_login_id_is_error));
                return;
            }
        if(mAccountPD==null) {
            ToastUtil.showLong(getString(R.string.tips_login_pd_is_empty));
            return;
        }else if(mAccountID.length() < 4) {
            ToastUtil.showLong(getString(R.string.tips_login_pd_is_error));
            return;
        }

        startLogin();
        KLog.i("【handler】" + "-");
    }


    private void startLogin() {
        loginButton.setEnabled(false);
        idEditText.setEnabled(false);
        pdEditText.setEnabled(false);

        Intent intent = new Intent(this, MainService.class);
        intent.setAction(Api.LOGIN);
        intent.putExtra("accountID", mAccountID);
        intent.putExtra("accountPW", mAccountPD);
        startService(intent);
    }

    public void clickRegister(View view) {
        Intent intent = new Intent(LoginActivity.this, WebActivity.class);
        intent.setData(Uri.parse("https://www.inoreader.com/?lang=" + Locale.getDefault()));
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

//        new MaterialDialog.Builder(this)
////                .title(R.string.article_about_dialog_title)
//                .content("请前往")
//                .show();
    }

    private LocalBroadcastManager manager;
    private BroadcastReceiver localReceiver;

    private void initBroadcast() {
        manager = LocalBroadcastManager.getInstance(this);
        // 先创建一个 BroadcastReceiver 实例
        localReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String data = intent.getStringExtra(Api.NOTICE);
//                String data = intent.getAction();
                KLog.e("接收到的数据为：", data);
                switch (data) {
                    case Api.N_COMPLETED:
                        Intent intentToActivity = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intentToActivity);
                        LoginActivity.this.finish();
                        overridePendingTransition(R.anim.in_from_bottom, R.anim.out_from_bottom);
                        break;
                    // 文章获取失败
                    case Api.N_ERROR:
                        ToastUtil.showLong(getString(R.string.tips_login_failure));
                        loginButton.setEnabled(true);
                        idEditText.setEnabled(true);
                        pdEditText.setEnabled(true);
                        break;
                    default:
                        break;
                }
            }
        };

        // 动态注册这个 receiver 实例，记得在不需要时注销   // Api.SYNC_ALL
        manager.registerReceiver(localReceiver, new IntentFilter(Api.SYNC_ALL));
    }

//    public void clickRegister(View view){
//        long time = System.currentTimeMillis();
//        WebViewS webViewS = new WebViewS(this);
//        KLog.e("耗时a：" + (System.currentTimeMillis() - time));
//
//        webViewS.destroy();
//
//        time = System.currentTimeMillis();
//        WebViewS webView2 = new WebViewS(this);
//        KLog.e("耗时b：" + (System.currentTimeMillis() - time));
//
//
//        webView2.destroy();
//
//        time = System.currentTimeMillis();
//        WebViewS webView3 = new WebViewS(this);
//        KLog.e("耗时c：" + (System.currentTimeMillis() - time));
//
//        webView3.destroy();
//    }

    public void onSBClick(View view){
        SwitchButton v = (SwitchButton)view;
        KLog.d( "点击" );
        switch (v.getId()) {
            case R.id.setting_proxy_sb:
                WithPref.i().setInoreaderProxy(v.isChecked());
                break;
        }
        KLog.i("Switch: ", v.isChecked() );
    }


}
