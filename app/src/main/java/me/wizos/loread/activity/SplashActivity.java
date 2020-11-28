package me.wizos.loread.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.socks.library.KLog;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.db.CorePref;
import me.wizos.loread.view.colorful.Colorful;

public class SplashActivity extends BaseActivity {
    protected static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 0.防止在该app已经启动的情况下，点击桌面图标还会再次进入该页面
        // 例如第一次点击图标启动应用是启动首界面A，然后进入第二个界面B；按home键后，再次点击图标，进入的页面是A
        // https://blog.csdn.net/gufeilong/article/details/72900365
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
        // 1.检查是否需要更新，是则跳转只更新页面/弹出更新Dialog
        // 2.获取已登录的服务商
        // 2.1 未登录则跳转登录页(服务商选择页)
        // 2.2 检查登录授权时间，超时则跳转对应服务商的重新授权页
        // 2.3 加载一些相关参数、配置到内存？还是在用到的时候再去加载？
        // 3.跳转至文章列表页
        Intent intent;
        String uid = CorePref.i().globalPref().getString(Contract.UID, null);

        KLog.e("获取UID：" + uid + ", " + App.i().getUser().getId() );
        if ( TextUtils.isEmpty(uid) ) {
            intent = new Intent(this, ProviderActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    public Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        return mColorfulBuilder;
    }
}
