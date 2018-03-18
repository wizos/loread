package me.wizos.loread.activity;

import android.os.Bundle;

import com.socks.library.KLog;

import me.wizos.loread.App;
import me.wizos.loread.data.PrefUtils;
import me.wizos.loread.view.colorful.Colorful;

public class SplashActivity extends BaseActivity {
    protected static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_splash);
        if (PrefUtils.i().getAuth().equals("")) {
            KLog.e("当前的验证：" + PrefUtils.i().getAuth() + "  " + App.UserID);
            goTo(LoginActivity.TAG);
        }else {
            goTo(MainActivity.TAG);
        }
        this.finish();
    }


    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        return mColorfulBuilder;
    }
}
