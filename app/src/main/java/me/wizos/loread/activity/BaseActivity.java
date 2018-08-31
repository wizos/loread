package me.wizos.loread.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.socks.library.KLog;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.data.WithPref;
import me.wizos.loread.view.colorful.Colorful;

/**
 * @author Wizos on 2016/3/12.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static String TAG = "";
    /*
     * LOG打印标签
     * getClass()获得当前对象的类型
     * java中有Class类,用以描述类型信息.
     * 如用语句 Class theClass="hello".getClass();
     * 得到的就是字符串的类型.getSimpleName()返回源代码中给出的底层类的简称。
     */

    /*
     * http://blog.csdn.net/sinat_31311947/article/details/50619467
     * 在实例子类的时候，如果父类的构造器中使用了this，其实这个this也还是指向子类这个this。
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showCurrentTheme();
    }


    protected void goTo(String toActivity) {
        Intent intent;
        if (TAG.equals(toActivity)) {
            KLog.i(this.toString() + "【跳转无效，为当前页】");
            return;
        }else if(toActivity.equals(MainActivity.TAG)){
            intent = new Intent(this, MainActivity.class);
        }else if(toActivity.equals(LoginActivity.TAG)){
            intent = new Intent(this, LoginActivity.class);
//        }else if(toActivity.equals(TagActivity.TAG)){
//            intent = new Intent(this, TagActivity.class);
        } else if (toActivity.equals(ArticleActivity3.TAG)) {
            intent = new Intent(this, ArticleActivity3.class);
        }else {
            return;
        }
        startActivity(intent);
    }


    protected Colorful mColorful;

    protected abstract Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder);
    /**
     * 自动设置当前主题(设置各个视图与颜色属性的关联)
     */
    protected void showCurrentTheme() {
        Colorful.Builder mColorfulBuilder = new Colorful.Builder(this);
        mColorful = buildColorful(mColorfulBuilder).create();
        if (WithPref.i().getThemeMode() == App.Theme_Day) {
            mColorful.setTheme(R.style.AppTheme_Day);
        } else {
            mColorful.setTheme(R.style.AppTheme_Night);
        }
    }
    /**
     * 手动切换主题并保存
     */
    protected void manualToggleTheme() {
        if (WithPref.i().getThemeMode() == App.Theme_Day) {
            mColorful.setTheme(R.style.AppTheme_Night);
            WithPref.i().setThemeMode(App.Theme_Night);
        } else {
            mColorful.setTheme(R.style.AppTheme_Day);
            WithPref.i().setThemeMode(App.Theme_Day);
        }
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
