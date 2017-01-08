package me.wizos.loread.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Space;

import com.kyleduo.switchbutton.SwitchButton;
import com.socks.library.KLog;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.net.API;
import me.wizos.loread.net.Neter;
import me.wizos.loread.net.Parser;
import me.wizos.loread.utils.UToast;

/**
 * Created by Wizos on 2016/3/5.
 */
public class LoginActivity extends BaseActivity implements View.OnLayoutChangeListener{
    protected Context context;
    protected String mAccountID = "";
    protected String mAccountPD = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        App.addActivity(this);
        mNeter = new Neter(handler);
        KLog.d("【未登录】" + handler);
        forInput();
        initView();
//        EditText vID = (EditText)findViewById(R.id.edittext_id);
//        EditText vPD = (EditText)findViewById(R.id.edittext_pd);
//        vPD.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == R.id.ime_login || id == EditorInfo.IME_ACTION_DONE
//                        || id == EditorInfo.IME_NULL) {
//                    attemptStartAuth();
//                    return true;
//                }
//                return false;
//            }
//        });

    }
    protected Neter mNeter;
//    protected Parser mParser = new Parser();

    protected void initColorful(){}
    private SwitchButton inoreaderProxy;
    private void initView(){
        vID = (EditText)findViewById(R.id.edittext_id);
        vPD = (EditText)findViewById(R.id.edittext_pd);
        inoreaderProxy = (SwitchButton) findViewById(R.id.setting_inoreader_proxy_sb_flyme) ;
        inoreaderProxy.setChecked(WithSet.getInstance().isInoreaderProxy());
    }



    @Override
    protected Context getActivity(){
        return LoginActivity.this;
    }
    protected static final String TAG = "LoginActivity";
    @Override
    public String getTAG(){
        return TAG;
    }

    @Override
    protected void onDestroy() {
        // 如果参数为null的话，会将所有的Callbacks和Messages全部清除掉。
        // 这样做的好处是在Acticity退出的时候，可以避免内存泄露。因为 handler 内可能引用 Activity ，导致 Activity 退出后，内存泄漏。
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    private void recoverData(){
        mAccountID = WithSet.getInstance().getAccountID();
        mAccountPD = WithSet.getInstance().getAccountPD();
        if (!TextUtils.isEmpty(mAccountID)) {
            vID.setText(mAccountID);
        }
        if (!TextUtils.isEmpty(mAccountPD)) {
            vPD.setText(mAccountPD);
        }
    }



    protected Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String info = msg.getData().getString("res");
            String url = msg.getData().getString("url");
            KLog.d(getActivity().toString() + "【handler】开始" + msg.what + handler + url );
            switch (msg.what) {
                case API.S_CLIENTLOGIN:
                    if (info==null){ return false; }
                    API.INOREADER_ATUH = "GoogleLogin auth=" + info.split("Auth=")[1].replaceAll("\n", "");
                    WithSet.getInstance().setAuth(API.INOREADER_ATUH);
                    mNeter.getWithAuth(API.HOST + API.U_USER_INFO);
                    break;
                case API.S_USER_INFO:
                    long mUserID = Parser.instance().parseUserInfo(info);
                    WithSet.getInstance().setUseId(mUserID);
                    finish();
                    goTo(MainActivity.TAG,"syncAll");
                    break;
                case API.F_NoMsg:
                case API.F_Request:
                case API.F_Response:
                    KLog.d(info);
                    UToast.showShort("登录失败");
                    break;
            }
            return false;
        }
    });


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
    private void forInput(){
        activityRootView = findViewById(R.id.login_scroll);
        screenHeight = this.getWindowManager().getDefaultDisplay().getHeight();//获取屏幕高度
        keyHeight = screenHeight/3; //阀值设置为屏幕高度的1/3
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


    private EditText vID,vPD;
    public void onLoginClicked(View view) {
        mAccountID = vID.getText().toString();
        mAccountPD = vPD.getText().toString();

        if(mAccountID==null) {
            UToast.showShort("账号为空");
            return;
        }else if(mAccountID.length()<4)
            if(!mAccountID.contains("@") || mAccountID.contains(" ")) {
                UToast.showShort("账号输入错误");
                return;
            }
        if(mAccountPD==null) {
            UToast.showShort("密码为空");
            return;
        }else if(mAccountID.length()<4){
            UToast.showShort("密码输入错误");
            return;
        }

        mNeter.addBody("Email", mAccountID);
        mNeter.addBody("Passwd", mAccountPD);
        mNeter.post(API.HOST + API.U_CLIENTLOGIN ,0);
        KLog.d("【handler】" + mNeter + "-" );
    }


    @Override
    public void onClick(View v) {
    }

    public void onSBClick(View view){
        SwitchButton v = (SwitchButton)view;
        KLog.d( "点击" );
        switch (v.getId()) {
            case R.id.setting_inoreader_proxy_sb_flyme:
                WithSet.getInstance().setInoreaderProxy(v.isChecked());
                break;
        }
        KLog.d("Switch: " , v.isChecked() );
    }

    @Override
    protected void notifyDataChanged(){
    }


    private void onStartAuth() {
    }

    private void onAuthResponse(boolean successful, int result, boolean error) {
    }


}
