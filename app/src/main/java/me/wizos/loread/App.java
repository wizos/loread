package me.wizos.loread;

import android.app.Application;
import android.content.MutableContextWrapper;
import android.os.Handler;

import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.socks.library.KLog;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.data.PrefUtils;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Tag;
import me.wizos.loread.db.dao.DaoMaster;
import me.wizos.loread.db.dao.DaoSession;
import me.wizos.loread.net.Api;
import me.wizos.loread.net.InoApi;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.TimeUtil;
import me.wizos.loread.view.WebViewX;

//import com.squareup.leakcanary.LeakCanary;

/**
 * 在Android中，可以通过继承Application类来实现应用程序级的全局变量，这种全局变量方法相对静态类更有保障，直到应用的所有Activity全部被destory掉之后才会被释放掉。
 *
 * Created by Wizos on 2015/12/24.
 * */
public class App extends Application{
    /**
     * 此处的单例不会造成内存泄露，因为 App 本身就是全局的单例
     */
    public static App instance;
    public final static String APP_NAME_EN = "loread";
    public final static String DB_NAME = "loread_DB";
    public final static int theme_Day = 0;
    public final static int theme_Night = 1;
    private final static boolean isDebug = true;

    // 跟使用的 API 有关的 字段
    public static long UserID;
    public static String StreamId;
    public static String StreamTitle;
    public static String StreamState;
    // 这个只是从 Read 属性的4个类型(Readed, UnRead, UnReading, All), Star 属性的3个类型(Stared, UnStar, All)中，生硬的抽出 UnRead(含UnReading), Stared, All 3个快捷状态，供用户在主页面切换时使用
    // 由于根据 StreamId 来获取文章，可从2个属性( Categories[针对Tag], OriginStreamId[针对Feed] )上，共4个变化上（All, Tag, NoTag, Feed）来获取文章。
    // 根据 StreamState 也是从2个属性(ReadState, StarState)的3个快捷状态 ( UnRead[含UnReading], Stared, All ) 来获取文章。
    // 所以文章列表页会有6种组合：某个 Categories 内的 UnRead[含UnReading], Stared, All。某个 OriginStreamId 内的 UnRead[含UnReading], Stared, All。
    // 所有定下来去获取文章的函数也有6个：getArtsUnreadInTag(), getArtsStaredInTag(), getArtsAllInTag(),getUnreadArtsInFeed(), getStaredArtsInFeed(), getAllArtsInFeed()

    public static List<Article> articleList = new ArrayList<>();
    public static List<Tag> tagList = new ArrayList<>();
    //    public static ArrayList<Tag> tagFeedList = new ArrayList<>();
//    public static int theArtIndex;
//    public static int theTagIndex;
//    public static Article theArt;
//    public static Tag theTag;
//    public static boolean syncFirstOpen;
    public static String currentArticleID;
    public static Handler artHandler;
    public boolean isSyncing = false;
//    public long lastSyncTime = 0L;
//    public static boolean lastSyncResult = false; // True = Success, false = fail
//    public static String PROXY_ADDR;
//    public static int PROXY_PORT;


    public static String cacheRelativePath,cacheAbsolutePath ,boxRelativePath, boxAbsolutePath, storeRelativePath, storeAbsolutePath ;
    public static String boxReadRelativePath, storeReadRelativePath;
    public static String logRelativePath,logAbsolutePath;
    public static String externalFilesDir;

    private static DaoSession daoSession;

    public static App i() {
        return instance;
    }


    /**
     * 首次打开 ArticleActivity 时，将生成的 WebView 缓存在这里，从而再次打开 ArticleActivity 时，加快文章的渲染速度，效果明显。
     */
    public List<WebViewX> mWebViewCaches = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
//        DBHelper.startUpgrade(this);
        initConfig();
        initTheme();

        initLogAndCrash();
        InoApi.i().init();
        // 初始化网络框架
        OkGo.getInstance().init(this);
        // 链接：https://www.jianshu.com/p/fc7909e24178
        // 第一次打开 Web 页面 ， 使用 WebView 加载页面的时候特别慢 ，第二次打开就能明显的感觉到速度有提升 ，为什么 ？
        // 是因为在你第一次加载页面的时候 WebView 内核并没有初始化 ， 所以在第一次加载页面的时候需要耗时去初始化 WebView 内核 。
        // 提前初始化 WebView 内核 ，例如如下把它放到了 Application 里面去初始化 , 在页面里可以直接使用该 WebView
        // 但是这里会影响，从 webview 中打开对话框
        mWebViewCaches.add(new WebViewX(new MutableContextWrapper(this)));
        mWebViewCaches.add(new WebViewX(new MutableContextWrapper(this)));
        mWebViewCaches.add(new WebViewX(new MutableContextWrapper(this)));

//        JobManager.create(this).addJobCreator(new JobCreatorRouter());
        /*
         * 当我们在 debug 的时候，往往会把间隔时间调短从而可以马上看到效果。
         * 但是在 Android N 中，规定了定时任务间隔最少为 15 分钟，如果小于 15 分钟会得到一个错误：intervalMs is out of range
         * 这时，可以调用 JobConfig 的 setAllowSmallerIntervalsForMarshmallow(true) 方法在 debug 模式下避免这个问题。
         * 但在正式环境下一定要注意间隔时间设置为 15 分钟以上。
         */
//        JobConfig.setAllowSmallerIntervalsForMarshmallow(true);
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
        if (BuildConfig.DEBUG) {
            // 测试的时候设为 true
            CrashReport.initCrashReport(App.i(), "900044326", true);
        } else {
            CrashReport.initCrashReport(App.i(), "900044326", false);
        }
        if (isDebug) {
            KLog.init(true);
        } else {
            KLog.init(false);
        }
    }

//  内存泄漏检测工具
//    private void initLeakCanary() {
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            return;
//        }
//        LeakCanary.install(this);
//    }


    public void readHost() {
        if (!PrefUtils.i().isInoreaderProxy()) {
            Api.HOST = InoApi.HOST;
        }else {
            Api.HOST = PrefUtils.i().getInoreaderProxyHost();
        }
    }

    private void initConfig() {
        initApiConfig();

        externalFilesDir = getExternalFilesDir(null) + File.separator;

        cacheRelativePath = FileUtil.getRelativeDir(Api.SAVE_DIR_CACHE);
//        cacheAbsolutePath = FileUtil.getAbsoluteDir(Api.SAVE_DIR_CACHE); // 仅在储存于 html 时使用

        boxRelativePath = FileUtil.getRelativeDir(Api.SAVE_DIR_BOX);
        boxAbsolutePath = FileUtil.getAbsoluteDir(Api.SAVE_DIR_BOX);

        storeRelativePath = FileUtil.getRelativeDir(Api.SAVE_DIR_STORE);
        storeAbsolutePath = FileUtil.getAbsoluteDir(Api.SAVE_DIR_STORE);

        logRelativePath = FileUtil.getRelativeDir("log");
        logAbsolutePath = FileUtil.getAbsoluteDir("log");

        boxReadRelativePath = FileUtil.getRelativeDir("boxRead");
//        boxReadAbsolutePath = FileUtil.getAbsoluteDir( "boxRead" );
        storeReadRelativePath = FileUtil.getRelativeDir("storeRead");
//        storeReadAbsolutePath = FileUtil.getAbsoluteDir( "storeRead" );
    }

    private void initApiConfig() {
        // 读取当前的API

        // 读取代理配置
        readHost();
        // 读取验证
        InoApi.INOREADER_ATUH = PrefUtils.i().getAuth();
        // 读取uid
        UserID = PrefUtils.i().getUseId();
        StreamState = PrefUtils.i().getStreamState();
        StreamId = PrefUtils.i().getStreamId();
        KLog.e(StreamState + "  " + StreamId + "  ");
        if (StreamId == null || StreamId.equals("")) {
            StreamId = "user/" + UserID + "/state/com.google/reading-list";
        }
        if (StreamId.equals("user/" + UserID + "/state/com.google/reading-list")) {
            StreamTitle = getString(R.string.main_activity_title_all);
        } else if (StreamId.equals("user/" + UserID + "/state/com.google/no-label")) {
            StreamTitle = getString(R.string.main_activity_title_untag);
        } else if (StreamId.startsWith("user/")) {
            try {
                StreamTitle = WithDB.i().getTag(StreamId).getTitle();
            } catch (Exception e) {
                StreamId = "user/" + UserID + "/state/com.google/reading-list";
                StreamTitle = getString(R.string.main_activity_title_all);
            }

        } else {
            try {
                StreamTitle = WithDB.i().getFeed(StreamId).getTitle();
            } catch (Exception e) {
                StreamId = "user/" + UserID + "/state/com.google/reading-list";
                StreamTitle = getString(R.string.main_activity_title_all);
            }
        }

        KLog.e("此时StreamId为：" + StreamId);
    }


    public void clearApiData() {
        PrefUtils.i().clear();
        WithDB.i().clear();
        OkGo.getInstance().cancelAll();
    }




    protected void initTheme() {
        if (!PrefUtils.i().isAutoToggleTheme()) {
            return;
        }
        int hour = TimeUtil.getCurrentHour();
        if (hour >= 7 && hour <= 20) {
            PrefUtils.i().setThemeMode(App.theme_Day);
        } else {
            PrefUtils.i().setThemeMode(App.theme_Night);
        }
    }


    // 官方推荐将获取 DaoMaster 对象的方法放到 Application 层，这样将避免多次创建生成 Session 对象
    public DaoSession getDaoSession() {
        if (daoSession == null) {
            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(i(), DB_NAME, null);
            daoSession = new DaoMaster(helper.getWritableDb()).newSession();
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
