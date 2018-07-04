package me.wizos.loread.activity;

import android.os.Bundle;
import android.text.TextUtils;

import me.wizos.loread.data.WithPref;
import me.wizos.loread.view.colorful.Colorful;

public class SplashActivity extends BaseActivity {
    protected static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_splash);
        if (TextUtils.isEmpty(WithPref.i().getAuth())) {
            goTo(LoginActivity.TAG);
        }else {
//            KLog.e("当前的验证：" + WithPref.i().getAuth() + "  " + App.UserID);
            goTo(MainActivity.TAG);
        }
        this.finish();
    }


    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        return mColorfulBuilder;
    }
}
