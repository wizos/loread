package me.wizos.loread;

import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;
import android.webkit.WebView;

import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.socks.library.KLog;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.wizos.loread.activity.SplashActivity;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithPref;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Tag;
import me.wizos.loread.db.dao.DaoMaster;
import me.wizos.loread.db.dao.DaoSession;
import me.wizos.loread.db.dao.SQLiteOpenHelperS;
import me.wizos.loread.event.Sync;
import me.wizos.loread.net.Api;
import me.wizos.loread.net.InoApi;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.NetworkUtil;
import me.wizos.loread.utils.TimeUtil;
import me.wizos.loread.utils.Tool;


/**
 * 在Android中，可以通过继承Application类来实现应用程序级的全局变量，这种全局变量方法相对静态类更有保障，直到应用的所有Activity全部被destory掉之后才会被释放掉。
 *
 * Created by Wizos on 2015/12/24.
 * */
public class App extends Application implements Thread.UncaughtExceptionHandler {
    /**
     * 此处的单例不会造成内存泄露，因为 App 本身就是全局的单例
     */
    public static App instance;
    public final static int Theme_Day = 0;
    public final static int Theme_Night = 1;

    // 跟使用的 API 有关的 字段
    public static long UserID;
    public static String StreamId;
    public static String StreamTitle;
    public static int StreamStatus;
    // 这个只是从 Read 属性的4个类型(Readed, UnRead, UnReading, All), Star 属性的3个类型(Stared, UnStar, All)中，生硬的抽出 UnRead(含UnReading), Stared, All 3个快捷状态，供用户在主页面切换时使用
    // 由于根据 StreamId 来获取文章，可从2个属性( Categories[针对Tag], OriginStreamId[针对Feed] )上，共4个变化上（All, Tag, NoTag, Feed）来获取文章。
    // 根据 StreamState 也是从2个属性(ReadState, StarState)的3个快捷状态 ( UnRead[含UnReading], Stared, All ) 来获取文章。
    // 所以文章列表页会有6种组合：某个 Categories 内的 UnRead[含UnReading], Stared, All。某个 OriginStreamId 内的 UnRead[含UnReading], Stared, All。
    // 所有定下来去获取文章的函数也有6个：getArtsUnreadInTag(), getArtsStaredInTag(), getArtsAllInTag(),getUnreadArtsInFeed(), getStaredArtsInFeed(), getAllArtsInFeed()

    public static List<Article> articleList;
    public static List<Tag> tagList = new ArrayList<>();

    public boolean isSyncing = false;

    public static String cacheRelativePath, boxRelativePath, storeRelativePath;
    public static String externalFilesDir;
    public static String webViewBaseUrl;
    public LinkedHashMap<String, Integer> articleProgress = new LinkedHashMap<String, Integer>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
            return size() > 5;
        }
    };

    private static DaoSession daoSession;
    public static App i() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        getDaoSession();
        initVar();
        initAutoToggleTheme();

        // 【基础统计API】由于其内部会调用 Looper.prepare() ，不能放在子线程中
        StatService.registerActivityLifecycleCallbacks(this);
        // 【提前初始化 WebView 内核】由于其内部会调用 Looper ，不能放在子线程中
        // 链接：https://www.jianshu.com/p/fc7909e24178
        // 经过实际测试，在反复New和销毁WebView时，采用Application要比采用Activity的context要稳定地少用20~30M左右的内存。
        // 虽然他们的内存都维持在一个稳定的消耗水平，但总体看来，Application要比Activity少那么一点。
        // 但是采用Application会影响，在 webview 中打开对话框
        new WebView(this).destroy();

        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                // 初始化网络框架
                OkGo.getInstance().init(instance);
//                OkGo.getInstance().getOkHttpClient().dispatcher().setMaxRequestsPerHost(3);
                OkGo.getInstance().getOkHttpClient().dispatcher().setMaxRequests(3);
                // 初始化网络状态
                NetworkUtil.THE_NETWORK = NetworkUtil.getNetWorkState();
                // 监听子线程的报错
                Thread.setDefaultUncaughtExceptionHandler(instance);
                // 初始化统计&监控服务
                initLogAndCrash();
//                initLeakCanary();

                if (Tool.isMainProcess(instance)) {
                    // 提前初始化
                    WithDB.i();
                    initApiConfig();
                    InoApi.i().initAuthHeaders();
                }
            }
        });
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        KLog.e("子线程意外报错");
        ex.printStackTrace();
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        android.os.Process.killProcess(android.os.Process.myPid());
        startActivity(intent);
    }



    /**
     * 程序在内存清理的时候执行
     */
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        KLog.e("内存onTrimMemory：" + level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            Glide.get(this).clearMemory();
        }
        Glide.get(this).trimMemory(level);
    }

    /**
     * 内存低的时候执行
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        KLog.e("内存低");
        // 清理 Glide 的缓存
        Glide.get(this).clearMemory();
    }

    /**
     * 程序终止的时候执行
     */
    @Override
    public void onTerminate() {
        KLog.i("程序终止的时候执行");
        super.onTerminate();
    }

    public void updateTagList(List<Tag> temps) {
        tagList.clear();
        tagList.addAll(temps);
    }

    private void initLogAndCrash() {
//        CrashReport.initCrashReport(App.i(), "900044326",  BuildConfig.DEBUG);
        CrashReport.setIsDevelopmentDevice(instance, BuildConfig.DEBUG);
        Bugly.init(getApplicationContext(), "900044326", BuildConfig.DEBUG);
        KLog.init(BuildConfig.DEBUG);

        // 腾讯统计，[可选]设置是否打开debug输出，上线时请关闭，Logcat标签为"MtaSDK"
        StatConfig.setDebugEnable(BuildConfig.DEBUG);
    }


    //  内存泄漏检测工具
    private void initLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }




    private void initVar() {
        externalFilesDir = getExternalFilesDir(null) + "";
        cacheRelativePath = FileUtil.getRelativeDir(Api.SAVE_DIR_CACHE);
        boxRelativePath = FileUtil.getRelativeDir(Api.SAVE_DIR_BOX);
        storeRelativePath = FileUtil.getRelativeDir(Api.SAVE_DIR_STORE);
        webViewBaseUrl = "file://" + externalFilesDir + "/cache/";
    }

    private void initApiConfig() {
        // 读取当前的API

        // 读取代理配置
//        readHost();
        // 读取验证
        InoApi.INOREADER_ATUH = WithPref.i().getAuth();
        // 读取uid
        UserID = WithPref.i().getUseId();
//        StreamState = WithPref.i().getStreamState();
        StreamStatus = WithPref.i().getStreamStatus();

        StreamId = WithPref.i().getStreamId();
//        KLog.e(StreamState + "  " + StreamId + "  ");
        if (StreamId == null || StreamId.equals("")) {
            StreamId = "user/" + UserID + Api.U_READING_LIST;
        }
        if (StreamId.equals("user/" + UserID + Api.U_READING_LIST)) {
            StreamTitle = getString(R.string.main_activity_title_all);
        } else if (StreamId.equals("user/" + UserID + Api.U_NO_LABEL)) {
            StreamTitle = getString(R.string.main_activity_title_untag);
        } else if (StreamId.startsWith("user/")) {
            try {
                StreamTitle = WithDB.i().getTag(StreamId).getTitle();
            } catch (Exception e) {
                StreamId = "user/" + UserID + Api.U_READING_LIST;
                StreamTitle = getString(R.string.main_activity_title_all);
            }
        } else {
            try {
                StreamTitle = WithDB.i().getFeed(StreamId).getTitle();
            } catch (Exception e) {
                StreamId = "user/" + UserID + Api.U_READING_LIST;
                StreamTitle = getString(R.string.main_activity_title_all);
            }
        }
//        KLog.e("此时StreamId为：" + StreamId + "   此时 Title 为：" + StreamTitle);
    }



    public void clearApiData() {
        WithPref.i().clear();
        WithDB.i().clear();
        FileUtil.deleteHtmlDir(new File(getExternalFilesDir(null) + "/cache/"));
        OkGo.getInstance().cancelAll();
        EventBus.getDefault().post(new Sync(Sync.STOP));
    }


    public static boolean hadAutoToggleTheme = false;

    protected void initAutoToggleTheme() {
//        KLog.e(" 初始化主题" + WithPref.i().isAutoToggleTheme() + TimeUtil.getCurrentHour());
        if (!WithPref.i().isAutoToggleTheme()) {
            return;
        }
        int hour = TimeUtil.getCurrentHour();
        int lastThemeMode = WithPref.i().getThemeMode();
        if (hour >= 7 && hour < 20) {
            WithPref.i().setThemeMode(App.Theme_Day);
        } else {
            WithPref.i().setThemeMode(App.Theme_Night);
        }
        if (WithPref.i().getThemeMode() != lastThemeMode) {
            hadAutoToggleTheme = true;
        }
    }


    // 官方推荐将获取 DaoMaster 对象的方法放到 Application 层，这样将避免多次创建生成 Session 对象
    public DaoSession getDaoSession() {
        if (daoSession == null) {
//            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(i(), DB_NAME, null);
            // 此处是为了方便升级
            SQLiteOpenHelperS helper = new SQLiteOpenHelperS(i(), "loread_DB", null);
            daoSession = new DaoMaster(helper.getWritableDb()).newSession();
        }
        return daoSession;
    }

//    private String getUnclassifiedTagId() {
//        return "user/" + WithPref.i().getUseId() + Api.U_NO_LABEL;
//    }
//
//    private String getRootTagId() {
//        return "user/" + WithPref.i().getUseId() + Api.U_READING_LIST;
//    }


//    OkHttpClient httpClient;
//    public void buildHttpClient() {
//        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory();
//        OkHttpClient httpClient = new OkHttpClient.Builder()
//                .readTimeout(30000L, TimeUnit.MILLISECONDS) // 默认 10000
//                .writeTimeout(30000L, TimeUnit.MILLISECONDS) // 默认 10000
//                .connectTimeout(30000L, TimeUnit.MILLISECONDS) // 默认 10000
////                .sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
//                .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
//                .build();
//    }


}
