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


//    protected void savePref(String key,String value){
//        savePref(key, value ,"test");
//    }
//    protected void savePref(String key,String value,String table){
//        SharedPreferences mySharedPreferences = getSharedPreferences(table, Activity.MODE_PRIVATE); //实例化SharedPreferences对象（第一步）
//        SharedPreferences.Editor editor = mySharedPreferences.edit();//实例化SharedPreferences.Editor对象（第二步）
//        editor.putString(key, value); //用putString的方法保存数据
//        editor.apply(); //提交当前数据
//    }
//    protected void savePref(String key,boolean value){
//        SharedPreferences mySharedPreferences = getSharedPreferences("test", Activity.MODE_PRIVATE); //实例化SharedPreferences对象（第一步）
//        SharedPreferences.Editor editor = mySharedPreferences.edit();//实例化SharedPreferences.Editor对象（第二步）
//        editor.putBoolean(key, value); //用putString的方法保存数据
//        editor.apply(); //提交当前数据
//    }
//    protected boolean readPref(String key,boolean defaultValue ){
//        SharedPreferences mySharedPreferences = getSharedPreferences("test", Activity.MODE_PRIVATE);
//        return mySharedPreferences.getBoolean(key, defaultValue);
//    }
//
//    protected String readPref(String key){
//        return readPref(key, "", "test");
//    }
//    protected String readPref(String key,String defaultValue){
//        return readPref(key,defaultValue,"test");
//    }
//    protected String readPref(String key,String defaultValue,String table){
//        SharedPreferences mySharedPreferences = getSharedPreferences(table, Activity.MODE_PRIVATE);
//        return mySharedPreferences.getString(key, defaultValue);//getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值
//    }


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
