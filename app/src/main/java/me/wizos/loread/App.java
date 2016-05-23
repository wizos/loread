package me.wizos.loread;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.dao.DaoMaster;
import me.wizos.loread.dao.DaoSession;

/**
 * Created by Wizos on 2015/12/24.
 * 该类为活动管理器，每个活动创建时都添加到该 list （销毁时便移除），可以实时收集到目前存在的 活动 ，方便要退出该应用时调用 finishAll() 来一次性关闭所有活动
 */
public class App extends Application{
    public static List<Activity> activities = new ArrayList<Activity>();
    public static Context context;
    //    private static App instance;   extends Application
    public static void addActivity(Activity activity){
        context = getContext();
        activities.add(activity);
    }
    @Override
    public void onCreate() {
        super.onCreate();
        App.context = getApplicationContext();
        instance = this;
//        Fresco.initialize(context);
        cacheRelativePath = getExternalFilesDir(null) + File.separator + "cache" + File.separator;
        cacheAbsolutePath = "file:"+ File.separator + File.separator + cacheRelativePath;
        // 官方推荐将获取 DaoMaster 对象的方法放到 Application 层，这样将避免多次创建生成 Session 对象
//        initDB();
    }
    public static Context getContext(){
        return context;}

    public static void removeActivity(Activity activity){
        activities.remove(activity);
        activity.finish();
    }
    public static void removeContext(Context context){
        Activity activity = (Activity) context;
        activities.remove(activity);
        activity.finish();
    }
    public static void finishAll(){
        for (Activity activity : activities){
            if (!activity.isFinishing()){
                activity.finish();
            }
        }
    }

//    public static Handler handler;
    public static String cacheRelativePath,cacheAbsolutePath;
    private static App instance;
    public static App getInstance() {
        return instance;
    }

    private  static DaoSession daoSession;
    private static final String DB_NAME = "lore_db";


    public static DaoSession getDaoSession() {
        if (daoSession == null) {
            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(getInstance(), DB_NAME, null);
            daoSession = new DaoMaster(helper.getWritableDatabase()).newSession();
//            // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
//            // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
//            // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
//            // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
//            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, DB_NAME, null);
//            db = helper.getWritableDatabase();
//            // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
//            daoMaster = new DaoMaster(db);
//            daoSession = daoMaster.newSession();
//
        }
        return daoSession;
    }



}
