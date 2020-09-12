package me.wizos.loread.activity;

import android.os.Bundle;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.User;
import me.wizos.loread.view.colorful.Colorful;

/**
 * @author Wizos on 2016/3/12.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static String TAG = "BaseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showCurrentTheme();
    }

//    /**
//     * // 获取当前的内容供应商
//     * // 0是未登录
//     * // 1是本地rss
//     * // 2是inoreader
//     * // 3是feedly
//     * // 4是tinytinyrss
//     * 初始化Api，Contract，DB
//     */
//    public void route() {
//        Intent intent;
//        String uid = App.i().getKeyValue().getString(Contract.UID, null);
//        KLog.e("获取UID：" + uid );
//        if ( TextUtils.isEmpty(uid) ) {
//            intent = new Intent(this, ProviderActivity.class);
//        } else {
//            intent = new Intent(this, MainActivity.class);
//        }
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(intent);
//        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
//    }

    protected Colorful mColorful;

    protected abstract Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder);

    /**
     * 自动设置当前主题(设置各个视图与颜色属性的关联)
     */
    protected void showCurrentTheme() {
        Colorful.Builder mColorfulBuilder = new Colorful.Builder(this);
        mColorful = buildColorful(mColorfulBuilder).create();
        if (App.i().getUser() != null && App.i().getUser().getThemeMode() == App.THEME_NIGHT) {
            mColorful.setTheme(R.style.AppTheme_Night);
        } else {
            mColorful.setTheme(R.style.AppTheme_Day);
        }
    }

    /**
     * 手动切换主题并保存
     */
    protected void manualToggleTheme() {
        User user = App.i().getUser();
        if (App.i().getUser().getThemeMode() == App.THEME_DAY) {
            mColorful.setTheme(R.style.AppTheme_Night);
            user.setThemeMode(App.THEME_NIGHT);

        } else {
            mColorful.setTheme(R.style.AppTheme_Day);
            user.setThemeMode(App.THEME_DAY);
        }
        CoreDB.i().userDao().update(user);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 后者为短期内按下的次数
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            this.finish();
            //返回真表示返回键被屏蔽掉
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
