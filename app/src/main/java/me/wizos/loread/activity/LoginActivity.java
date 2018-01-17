package me.wizos.loread.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Space;

import com.kyleduo.switchbutton.SwitchButton;
import com.lzy.okgo.exception.HttpException;
import com.socks.library.KLog;

import java.io.IOException;

import me.wizos.loread.R;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.net.DataApi;
import me.wizos.loread.utils.ToastUtil;
import me.wizos.loread.utils.Tool;
import me.wizos.loread.view.colorful.Colorful;

/**
 * Created by Wizos on 2016/3/5.
 */
public class LoginActivity extends BaseActivity implements View.OnLayoutChangeListener{
    protected static final String TAG = "LoginActivity";
    protected String mAccountID = "";
    protected String mAccountPD = "";
    private EditText vID, vPD;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        KLog.d("【未登录】");
        forInput();
        initView();
        recoverData();
    }


    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        return mColorfulBuilder;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    private void initView(){
        vID = (EditText)findViewById(R.id.edittext_id);
        vPD = (EditText)findViewById(R.id.edittext_pd);
        loginButton = (Button) findViewById(R.id.login_button_login);
        SwitchButton inoreaderProxy = (SwitchButton) findViewById(R.id.setting_inoreader_proxy_sb_flyme);
        inoreaderProxy.setChecked(WithSet.i().isInoreaderProxy());
    }


    /**
     * 默认填充密码
     */
    private void recoverData(){
        mAccountID = WithSet.i().getAccountID();
        mAccountPD = WithSet.i().getAccountPD();
        if (!TextUtils.isEmpty(mAccountID)) {
            vID.setText(mAccountID);
        }
        if (!TextUtils.isEmpty(mAccountPD)) {
            vPD.setText(mAccountPD);
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
        Space space = (Space)findViewById(R.id.login_space_a);
        if(oldBottom != 0 && bottom != 0 &&(oldBottom - bottom > keyHeight)){
//            if(space.getVisibility() == View.VISIBLE){
//                space.setVisibility(View.GONE);
//            }
        }else if(oldBottom != 0 && bottom != 0 &&(bottom - oldBottom > keyHeight)){
//            if(space.getVisibility() == View.GONE){
//                space.setVisibility(View.VISIBLE);
//            }
        }
    }


    public void onLoginClicked(View view) {
        mAccountID = vID.getText().toString();
        mAccountPD = vPD.getText().toString();

        if(mAccountID==null) {
            ToastUtil.showShort(getString(R.string.tips_login_failure));
            return;
        }else if(mAccountID.length()<4)
            if(!mAccountID.contains("@") || mAccountID.contains(" ")) {
                ToastUtil.showShort(getString(R.string.tips_login_id_is_error));
                return;
            }
        if(mAccountPD==null) {
            ToastUtil.showShort(getString(R.string.tips_login_pd_is_empty));
            return;
        }else if(mAccountID.length()<4){
            ToastUtil.showShort(getString(R.string.tips_login_pd_is_error));
            return;
        }

        loginButton.setEnabled(false);

        startLogin();
        KLog.i("【handler】" + "-");
    }


    private void startLogin() {
        KLog.i("开始登录");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean loginResult = DataApi.i().clientLogin(mAccountID, mAccountPD);
                    if (!loginResult) {
                        ToastUtil.showShort(getString(R.string.tips_login_failure));
                    }
                    DataApi.i().fetchUserInfo();
                    Intent intentToActivity = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intentToActivity);
                    LoginActivity.this.finish();
                } catch (HttpException e) {
                    e.printStackTrace();
                    Tool.showShort("login时出了异常：HttpException");
                } catch (IOException e) {
                    e.printStackTrace();
                    Tool.showShort("login时出了异常：IOException");
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                    Tool.showShort("login时出了异常：IllegalStateException");
                } finally {
                    loginButton.post(new Runnable() {// 用 post 可以解决在非主线程运行，会报错
                        @Override
                        public void run() {
                            loginButton.setEnabled(true);
                        }
                    });
                }
            }
        }).start();

    }


    public void onSBClick(View view){
        SwitchButton v = (SwitchButton)view;
        KLog.d( "点击" );
        switch (v.getId()) {
            case R.id.setting_inoreader_proxy_sb_flyme:
                WithSet.i().setInoreaderProxy(v.isChecked());
                break;
        }
        KLog.d("Switch: " , v.isChecked() );
    }


}
