package me.wizos.loread.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

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


    /**
     * 组件Activity在manifest.xml文件中可以指定参数android:ConfigChanges，用于捕获手机状态的改变。
     * 添加后，在当所指定属性(Configuration Changes)发生改变时，通知程序调用onConfigurationChanged()函数。
     * Android旋转屏幕不销毁Activity
     */
    @Override
    public void onConfigurationChanged(@NotNull Configuration config) {
        super.onConfigurationChanged(config);
        // User user = App.i().getUser();
        // if (user != null && user.isAutoToggleTheme()) {
        //     int currentNightMode = config.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        //     if(currentNightMode == Configuration.UI_MODE_NIGHT_NO && user.getThemeMode() == App.THEME_NIGHT){
        //         XLog.d("夜间模式未启用，使用浅色主题");
        //         mColorful.setTheme(R.style.AppTheme_Day);
        //         user.setThemeMode(App.THEME_DAY);
        //     }else if(currentNightMode == Configuration.UI_MODE_NIGHT_YES && user.getThemeMode() == App.THEME_DAY){
        //         XLog.d("夜间模式启用，使用深色主题");
        //         mColorful.setTheme(R.style.AppTheme_Night);
        //         user.setThemeMode(App.THEME_NIGHT);
        //     }
        //     CoreDB.i().userDao().update(user);
        // }
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
