package me.wizos.loread.activity;

import android.os.Bundle;
import android.text.TextUtils;

import com.socks.library.KLog;

import me.wizos.loread.App;
import me.wizos.loread.data.WithPref;
import me.wizos.loread.view.colorful.Colorful;

public class SplashActivity extends BaseActivity {
    protected static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_splash);
        if (TextUtils.isEmpty(WithPref.i().getAuth())) {
            KLog.e("当前的验证：" + WithPref.i().getAuth() + "  " + App.UserID);
            goTo(LoginActivity.TAG);
        }else {
            KLog.e("升级", "升级文件");
            goTo(MainActivity.TAG);
        }
        this.finish();
    }


    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        return mColorfulBuilder;
    }
}
