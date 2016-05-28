package me.wizos.loread.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.socks.library.KLog;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.dao.WithSet;
import me.wizos.loread.net.API;
import me.wizos.loread.net.Neter;
import me.wizos.loread.net.Parser;
import me.wizos.loread.utils.UToast;

/**
 * Created by Wizos on 2016/3/5.
 */
public class LoginActivity extends BaseActivity{
    protected Context context;
    protected String mAccountID = "";
    protected String mAccountPD = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        App.addActivity(this);
        mNeter = new Neter(handler,this);
        KLog.d("【未登录】" + handler);

        vID = (EditText)findViewById(R.id.edittext_id);
        vPD = (EditText)findViewById(R.id.edittext_pd);
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
    protected Parser mParser = new Parser();

    @Override
    protected Context getActivity(){
        return LoginActivity.this;
    }
    protected static final String TAG = "LoginActivity";
    @Override
    public String getTAG(){
        return TAG;
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
                    mNeter.getWithAuth(API.U_USER_INFO);
                    break;
                case API.S_USER_INFO:
                    long mUserID = mParser.parseUserInfo(info);
                    WithSet.getInstance().setUseId(mUserID);
                    finish();
                    goTo(MainActivity.TAG,"syncAll");
                    break;
                case API.FAILURE:
                case API.FAILURE_Request:
                case API.FAILURE_Response:
                    UToast.showShort("登录失败");
                    break;
            }
            return false;
        }
    });


    private EditText vID,vPD;
    public void onLoginClicked(View view) {
        mAccountID = vID.getText().toString();
        mAccountPD = vPD.getText().toString();

        if(mAccountID==null) {
            UToast.showLong("账号为空");
            return;
        }else if(mAccountID.length()<4)
            if(!mAccountID.contains("@") || mAccountID.contains(" ")) {
                UToast.showLong("账号输入错误");
                return;
            }
        if(mAccountPD==null) {
            UToast.showLong("密码为空");
            return;
        }else if(mAccountID.length()<4){
            UToast.showLong("密码输入错误");
            return;
        }

        mNeter.addBody("Email", mAccountID);
        mNeter.addBody("Passwd", mAccountPD);
        mNeter.post(API.U_CLIENTLOGIN ,0);
        KLog.d("【handler】" + mNeter + "-" + mParser);
    }


    @Override
    public void onClick(View v) {
    }
    @Override
    protected void notifyDataChanged(){
    }


    private void onStartAuth() {
    }

    private void onAuthResponse(boolean successful, int result, boolean error) {
    }


}
