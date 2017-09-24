package me.wizos.loread;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;

import com.lzy.okgo.OkGo;
import com.socks.library.KLog;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.activity.BaseActivity;
import me.wizos.loread.bean.Article;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.data.dao.DaoMaster;
import me.wizos.loread.data.dao.DaoSession;
import me.wizos.loread.net.API;
import me.wizos.loread.net.Neter;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.TimeUtil;

/**
 * Created by Wizos on 2015/12/24.
 * 该类为 Activity 管理器，每个活动创建时都添加到该 list （销毁时便移除），可以实时收集到目前存在的 活动 ，方便要退出该应用时调用 finishAll() 来一次性关闭所有活动
 */
public class App extends Application{
    public static final String DB_NAME = "loread_DB";
    public static final int DB_VERSION = 4;
    public final static int theme_Day = 0;
    public final static int theme_Night = 1;

    public static String cacheRelativePath,cacheAbsolutePath ,boxRelativePath, boxAbsolutePath, storeRelativePath, storeAbsolutePath ;
    public static String boxReadRelativePath, storeReadRelativePath;
    public static String logRelativePath,logAbsolutePath;
    public static String externalFilesDir;
    public static long mUserID;
    public static List<Article> articleList;
    public static Handler artHandler;
    public static String currentArticleID;
    public static Neter mNeter;
    public static long time;

    private  static DaoSession daoSession;
    public static App instance; // 此处的单例不会造成内存泄露，因为 App 本身就是全局的单例

    public static synchronized App i() {
        return instance;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        App.instance = this;
        initConfig();
        initTheme();

//        initTBS();
//        initLeakCanary();
//        initDebug();
        initRelease(); // 测试时，注释掉
        OkGo.getInstance().init(this); // 初始化网络框架

    }


    private void initRelease() {
//         TEST，正式环境下应该启用
        KLog.init(false);
        CrashReport.initCrashReport(App.i(), "900044326", false); // 测试的时候设为 true
    }

    private void initDebug() {
//         TEST 环境下应该启用
        KLog.init(true);
        CrashReport.initCrashReport(App.i(), "900044326", true); // 测试的时候设为 true
//        initStetho(); // 用于浏览器调试网络请求
    }

//  内存泄漏检测工具
//    private void initLeakCanary() {
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            return;
//        }
//        LeakCanary.install(this);
//    }

    private void initStetho() {
        // TEST，正式环境应该注释掉
//        Stetho.initialize(
//                Stetho.newInitializerBuilder(this)
//                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
//                        .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
//                        .build());
    }


//    private void initTBS() {
//        //搜本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
//        KLog.e("初始化x5内核", " initTBS");
//        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {
//            @Override
//            public void onViewInitFinished(boolean arg0) {
//                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
//                KLog.e("初始化X5", " onViewInitFinished is " + arg0);
//            }
//            @Override
//            public void onCoreInitFinished() {
//                KLog.e("初始化X5", " onCoreInitFinished ");
//            }
//        };
//        //x5内核初始化接口
//        QbSdk.initX5Environment(getApplicationContext(), cb);
//    }

    private void initConfig() {
        if (!WithSet.i().isInoreaderProxy()) {
            API.HOST = API.HOST_OFFICIAL;
        }else {
            API.HOST = API.HOST_PROXY;
        }
        externalFilesDir = getExternalFilesDir(null) + File.separator;

        cacheRelativePath = FileUtil.getRelativeDir(API.SAVE_DIR_CACHE);
//        cacheAbsolutePath = FileUtil.getAbsoluteDir(API.SAVE_DIR_CACHE); // 仅在储存于 html 时使用

        boxRelativePath = FileUtil.getRelativeDir(API.SAVE_DIR_BOX);
        boxAbsolutePath = FileUtil.getAbsoluteDir(API.SAVE_DIR_BOX);

        storeRelativePath = FileUtil.getRelativeDir(API.SAVE_DIR_STORE);
        storeAbsolutePath = FileUtil.getAbsoluteDir(API.SAVE_DIR_STORE);

        logRelativePath = FileUtil.getRelativeDir("log");
        logAbsolutePath = FileUtil.getAbsoluteDir("log");

        boxReadRelativePath = FileUtil.getRelativeDir("boxRead");
//        boxReadAbsolutePath = FileUtil.getAbsoluteDir( "boxRead" );

        storeReadRelativePath = FileUtil.getRelativeDir("storeRead");
//        storeReadAbsolutePath = FileUtil.getAbsoluteDir( "storeRead" );
    }

    protected void initTheme() {
        if (!WithSet.i().isAutoToggleTheme()) {
            return;
        }
        int hour = TimeUtil.getCurrentHour();
        if (hour >= 7 && hour <= 20) {
//            mColorful.setTheme(R.style.AppTheme_Day);
            WithSet.i().setThemeMode(App.theme_Day);
        } else {
//            mColorful.setTheme(R.style.AppTheme_Night);
            WithSet.i().setThemeMode(App.theme_Night);
        }
    }
    //    private static WeakReference<BaseActivity> activities;
    public static List<WeakReference<BaseActivity>> activities = new ArrayList<>();

    public static void addActivity(BaseActivity activity) {
        WeakReference<BaseActivity> rArticle = new WeakReference<>(activity);
        activities.add(rArticle);
    }

    public static void finishActivity(Activity activity){
        activities.remove(activity);
        activity.finish();
    }
    public static void finishAll(){
        for (WeakReference<BaseActivity> activity : activities) {
            if (activity.get() != null && !activity.get().isFinishing()) {
                activity.get().finish();
            }
        }
        System.exit(0);
    }


    // 官方推荐将获取 DaoMaster 对象的方法放到 Application 层，这样将避免多次创建生成 Session 对象
    public static DaoSession getDaoSession() {
        if (daoSession == null) {
            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(i(), DB_NAME, null);
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
        }
        return daoSession;
    }

}
