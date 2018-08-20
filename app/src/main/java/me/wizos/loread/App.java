package me.wizos.loread;

import android.app.Application;
import android.content.Intent;
import android.content.MutableContextWrapper;

import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.socks.library.KLog;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import me.wizos.loread.view.WebViewS;


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
    public final static String APP_NAME_EN = "loread";
    public final static String DB_NAME = "loread_DB";
    public final static int Theme_Day = 0;
    public final static int Theme_Night = 1;

    // 跟使用的 API 有关的 字段
    public static long UserID;
    public static String StreamId;
    public static String StreamTitle;
    public static String StreamState;
    public static int StreamStatus;
    // 这个只是从 Read 属性的4个类型(Readed, UnRead, UnReading, All), Star 属性的3个类型(Stared, UnStar, All)中，生硬的抽出 UnRead(含UnReading), Stared, All 3个快捷状态，供用户在主页面切换时使用
    // 由于根据 StreamId 来获取文章，可从2个属性( Categories[针对Tag], OriginStreamId[针对Feed] )上，共4个变化上（All, Tag, NoTag, Feed）来获取文章。
    // 根据 StreamState 也是从2个属性(ReadState, StarState)的3个快捷状态 ( UnRead[含UnReading], Stared, All ) 来获取文章。
    // 所以文章列表页会有6种组合：某个 Categories 内的 UnRead[含UnReading], Stared, All。某个 OriginStreamId 内的 UnRead[含UnReading], Stared, All。
    // 所有定下来去获取文章的函数也有6个：getArtsUnreadInTag(), getArtsStaredInTag(), getArtsAllInTag(),getUnreadArtsInFeed(), getStaredArtsInFeed(), getAllArtsInFeed()

    public static List<Article> articleList; // = new ArrayList<>()
    public static List<Tag> tagList = new ArrayList<>();

    public boolean isSyncing = false;

    public static String cacheRelativePath, boxRelativePath, storeRelativePath;
    public static String externalFilesDir;
    public static String webViewBaseUrl;

    private static DaoSession daoSession;

    public static App i() {
        return instance;
    }


    /**
     * 首次打开 ArticleActivity 时，将生成的 WebView 缓存在这里，从而再次打开 ArticleActivity 时，加快文章的渲染速度，效果明显。
     */
//    public List<WebViewS> mWebViewCaches = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        getDaoSession();

        // 初始化网络框架
        OkGo.getInstance().init(this);
        // 初始化网络状态
        NetworkUtil.THE_NETWORK = NetworkUtil.getNetWorkState();

        initVar();
        initAutoToggleTheme();
        initLogAndCrash();

        initLeakCanary();

        Thread.setDefaultUncaughtExceptionHandler(this);


        if (Tool.isMainProcess(this)) {
            // 提前初始化
            WithDB.i();
            initApiConfig();
            initWebView();
            InoApi.i().initAuthHeaders();
//            WithDB.i().coverSaveFeeds(WithDB.i().getUnreadArtsCountByFeed3());
        }



//        FileUtil.clear(this);
        // 使用handler不断的发送延时消息可以实现循环监听，内存占用也不大，https://www.cnblogs.com/benhero/p/4521727.html
//        JobManager.create(this).addJobCreator(new JobCreateRouter());
//        JobManager.instance().cancelAllForTag("job_sync");
        /*
         * 当我们在 debug 的时候，往往会把间隔时间调短从而可以马上看到效果。
         * 但是在 Android N 中，规定了定时任务间隔最少为 15 分钟，如果小于 15 分钟会得到一个错误：intervalMs is out of range
         * 这时，可以调用 JobConfig 的 setAllowSmallerIntervalsForMarshmallow(true) 方法在 debug 模式下避免这个问题。
         * 但在正式环境下一定要注意间隔时间设置为 15 分钟以上。
         */
//        JobConfig.setAllowSmallerIntervalsForMarshmallow(true);
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


    private void initWebView() {
        // 链接：https://www.jianshu.com/p/fc7909e24178
        // 第一次打开 Web 页面 ， 使用 WebView 加载页面的时候特别慢 ，第二次打开就能明显的感觉到速度有提升 ，为什么 ？
        // 是因为在你第一次加载页面的时候 WebView 内核并没有初始化 ， 所以在第一次加载页面的时候需要耗时去初始化 WebView 内核 。
        // 提前初始化 WebView 内核 ，例如如下把它放到了 Application 里面去初始化 , 在页面里可以直接使用该 WebView
        // 但是这里会影响，从 webview 中打开对话框
//        new WebViewS(new MutableContextWrapper(this)).destroy();
        //
        // 经过我的实际测试，在反复New和销毁WebView时，采用Application要比采用Activity的context要稳定地少用20~30M左右的内存。
        // 虽然他们的内存都维持在一个稳定的消耗水平，但总体看来，Application要比Activity少那么一点。

        new WebViewS(new MutableContextWrapper(this)).destroy();
//        mWebViewCaches.add(new WebViewS(new MutableContextWrapper(this)));
    }




    /**
     * 程序在内存清理的时候执行
     */
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
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
        // 清理 Glide 的缓存
        Glide.get(this).clearMemory();
    }

    /**
     * 程序终止的时候执行
     */
    @Override
    public void onTerminate() {
        KLog.i("程序终止的时候执行");
//        JobManager.instance().cancelAll();
        super.onTerminate();
    }

    public void updateArtList(List<Article> temps) {
        articleList.clear();
        articleList.addAll(temps);
    }

    public void updateTagList(List<Tag> temps) {
        tagList.clear();
        tagList.addAll(temps);
    }

    private void initLogAndCrash() {
        CrashReport.setIsDevelopmentDevice(this, BuildConfig.DEBUG);
//        CrashReport.initCrashReport(App.i(), "900044326",  BuildConfig.DEBUG);
        Bugly.init(getApplicationContext(), "900044326", BuildConfig.DEBUG);
        KLog.init(BuildConfig.DEBUG);

        // [可选]设置是否打开debug输出，上线时请关闭，Logcat标签为"MtaSDK"
        StatConfig.setDebugEnable(BuildConfig.DEBUG);
        // 基础统计API
        StatService.registerActivityLifecycleCallbacks(this);

    }


//  内存泄漏检测工具
private void initLeakCanary() {
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            return;
//        }
//        LeakCanary.install(this);
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

//    public void saveFeedsConfig() {
//        AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
//            @Override public void run() {
//                FileUtil.save(getExternalFilesDir(null) + "/config/feeds-config.json", new GsonBuilder().setPrettyPrinting().create().toJson(feedsConfigMap));
//            }
//        });
//    }



    public void clearApiData() {
        WithPref.i().clear();
        WithDB.i().clear();
        FileUtil.deleteHtmlDir(new File(getExternalFilesDir(null) + "/cache/"));
        OkGo.getInstance().cancelAll();
        EventBus.getDefault().post(new Sync(Sync.STOP));
    }


    public static boolean hadAutoToggleTheme = false;

    protected void initAutoToggleTheme() {
        KLog.e(" 初始化主题" + WithPref.i().isAutoToggleTheme() + TimeUtil.getCurrentHour());
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
            SQLiteOpenHelperS helper = new SQLiteOpenHelperS(i(), DB_NAME, null);
            daoSession = new DaoMaster(helper.getWritableDb()).newSession();

        }
        return daoSession;
    }


    private String getUnclassifiedTagId() {
        return "user/" + WithPref.i().getUseId() + Api.U_NO_LABEL;
    }

    private String getRootTagId() {
        return "user/" + WithPref.i().getUseId() + Api.U_READING_LIST;
    }


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
