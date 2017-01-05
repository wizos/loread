package me.wizos.loread.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.utils.colorful.Colorful;

/**
 * Created by Wizos on 2016/3/12.
 */
public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener{
    /**
     * LOG打印标签
     * getClass()获得当前对象的类型
     * java中有Class类,用以描述类型信息.
     * 如用语句 Class theClass="hello".getClass();
     * 得到的就是字符串的类型.getSimpleName()返回源代码中给出的底层类的简称。
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        initSystemBar();
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    protected abstract Context getActivity();
    protected abstract String  getTAG();


    protected void goTo(String toActivity){
        goTo(toActivity,"");
    }

    protected void goTo(String toActivity,String notifyChange){
        Intent intent = new Intent();
        intent.putExtra("goToCode",notifyChange);

        if( getTAG().equals(toActivity)){
            System.out.println(getActivity().toString() + "【跳转无效，为当前页】" );
            return;
        }else if(toActivity.equals(MainActivity.TAG)){
            intent = new Intent(getActivity(),MainActivity.class);
        }else if(toActivity.equals(LoginActivity.TAG)){
            intent = new Intent(getActivity(),LoginActivity.class);
        }else if(toActivity.equals(TagActivity.TAG)){
            intent = new Intent(getActivity(),TagActivity.class);
        }else if(toActivity.equals(ArticleActivity.TAG)){
            intent = new Intent(getActivity(),ArticleActivity.class);
        }else {
            return;
        }
        startActivity(intent);
    }

    protected abstract void notifyDataChanged();

    public void onBottombarClicked(View view) {
    }





    Colorful mColorful;
    protected void autoToggleThemeSetting() {
        if ( WithSet.getInstance().getThemeMode()==WithSet.themeDay ) {
            mColorful.setTheme(R.style.AppTheme);
        } else {
            mColorful.setTheme(R.style.AppTheme_Dark);
        }
    }

    /**
     * 切换主题设置
     */
    protected void toggleThemeSetting() {
        if ( WithSet.getInstance().getThemeMode()==WithSet.themeDay ) {
            WithSet.getInstance().setThemeMode(WithSet.themeNight);
            mColorful.setTheme(R.style.AppTheme_Dark);
        } else {
            WithSet.getInstance().setThemeMode(WithSet.themeDay);
            mColorful.setTheme(R.style.AppTheme);
        }
    }
    /**
     * 设置各个视图与颜色属性的关联
     */
    protected abstract void initColorful();


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { // 后者为短期内按下的次数
            App.finishActivity(this);// 移除这个 Activity
            return true;//返回真表示返回键被屏蔽掉
        }
        return super.onKeyDown(keyCode, event);
    }


}
