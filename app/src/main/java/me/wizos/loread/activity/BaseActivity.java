package me.wizos.loread.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import me.wizos.loread.R;

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
        initSystemBar();
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

    public void onBottombarClicked(View view){
        return;
    }

    protected void initSystemBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintColor(getResources().getColor(R.color.main_grey_dark));
            tintManager.setStatusBarTintEnabled(true);
        }
    }
    protected void setTranslucentStatus(boolean on) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window win = getWindow();
            WindowManager.LayoutParams winParams = win.getAttributes();
            final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            if (on) {
                winParams.flags |= bits;
            } else {
                winParams.flags &= ~bits;
            }
            win.setAttributes(winParams);
        }
    }

}
