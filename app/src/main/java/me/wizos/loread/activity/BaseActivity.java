package me.wizos.loread.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;

import com.umeng.analytics.MobclickAgent;

import org.jetbrains.annotations.NotNull;

import me.wizos.loread.App;
import me.wizos.loread.BuildConfig;
import me.wizos.loread.R;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.User;
import me.wizos.loread.view.colorful.Colorful;

/**
 * @author Wizos on 2016/3/12.
 */
public abstract class BaseActivity extends AppCompatActivity { //  implements ThemeChangeObserver
    private static String TAG = "BaseActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ((App) getApplication()).registerObserver(this);
        // declareCurrentTheme();
        super.onCreate(savedInstanceState);

        // // 设置透明状态栏
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        //     Window window = getWindow();
        //     WindowManager.LayoutParams attributes = window.getAttributes();
        //     attributes.flags |= WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        //     window.setAttributes(attributes);
        // }

        showCurrentTheme();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(BuildConfig.DEBUG) MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(BuildConfig.DEBUG) MobclickAgent.onResume(this);
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

        // if (App.i().getUser() != null) {
        //     if (!App.i().getUser().isAutoToggleTheme()) {
        //         if (App.i().getUser().getThemeMode() == App.THEME_NIGHT){
        //             mColorful.setTheme(R.style.AppTheme_Night);
        //         } else {
        //             mColorful.setTheme(R.style.AppTheme_Day);
        //         }
        //     }else {
        //         int hour = TimeUtils.getCurrentHour();
        //         if (hour >= 7 && hour < 20) {
        //             mColorful.setTheme(R.style.AppTheme_Day);
        //         } else {
        //             mColorful.setTheme(R.style.AppTheme_Night);
        //         }
        //     }
        // } else {
        //     mColorful.setTheme(R.style.AppTheme_Day);
        // }
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







    // @Override
    // public void declareCurrentTheme() {
    //     if (App.i().getUser() != null && App.i().getUser().getThemeMode() == App.THEME_NIGHT) {
    //         setTheme(R.style.AppTheme_Night);
    //     } else {
    //         setTheme(R.style.AppTheme_Day);
    //     }
    // }
    //
    // @Override
    // public void setContentView(@LayoutRes int layoutResID) {
    //     super.setContentView(layoutResID);
    //     // View status = findViewById(R.id.custom_id_title_status_bar);
    //     // if (status != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
    //     //     status.getLayoutParams().height = getStatusBarHeight();
    //     // }
    // }
    //
    // // /**
    // //  * */
    // // public int getStatusBarHeight() {
    // //     int result = 0;
    // //     int resourceId = getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
    // //     if (resourceId > 0) {
    // //         result = getContext().getResources().getDimensionPixelSize(resourceId);
    // //     }
    // //     return result;
    // // }
    //
    //
    // public void switchCurrentTheme() {
    //     User user = App.i().getUser();
    //     if (App.i().getUser().getThemeMode() == App.THEME_DAY) {
    //         user.setThemeMode(App.THEME_NIGHT);
    //     } else {
    //         user.setThemeMode(App.THEME_DAY);
    //     }
    //     CoreDB.i().userDao().update(user);
    //     declareCurrentTheme();
    // }
    //
    // // /**
    // //  * */
    // // public Context getContext() {
    // //     return BaseActivity.this;
    // // }
    //
    // @Override
    // protected void onDestroy() {
    //     ((App) getApplication()).unregisterObserver(this);
    //     super.onDestroy();
    // }
}
