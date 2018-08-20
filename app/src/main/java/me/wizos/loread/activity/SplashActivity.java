package me.wizos.loread.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import me.wizos.loread.R;
import me.wizos.loread.data.WithPref;
import me.wizos.loread.view.colorful.Colorful;

public class SplashActivity extends BaseActivity {
    protected static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_splash);
        // 避免从桌面启动程序后，会重新实例化入口类的activity；https://blog.csdn.net/gufeilong/article/details/72900365
        // 例如第一次点击图标启动应用是启动首界面A，然后进入第二个界面B；按home键后，再次点击图标，进入的页面是A
        if (!this.isTaskRoot()) {
            Intent intent = getIntent();
            if (intent != null) {
                String action = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                    finish();
                    return;
                }
            }
        }

        if (TextUtils.isEmpty(WithPref.i().getAuth())) {
            goTo(LoginActivity.TAG);
        }else {
//            KLog.e("当前的验证：" + WithPref.i().getAuth() + "  " + App.UserID);
            goTo(MainActivity.TAG);
        }
        this.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }


    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        return mColorfulBuilder;
    }
}
