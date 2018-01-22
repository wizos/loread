package me.wizos.loread;

import android.app.Application;
import android.os.Handler;

import com.bumptech.glide.Glide;
import com.lzy.okgo.OkGo;
import com.socks.library.KLog;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.bean.Article;
import me.wizos.loread.bean.Tag;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.data.dao.DaoMaster;
import me.wizos.loread.data.dao.DaoSession;
import me.wizos.loread.net.Api;
import me.wizos.loread.net.InoApi;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.TimeUtil;

//import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Wizos on 2015/12/24.
 * */
public class App extends Application{
    public static App instance; // 此处的单例不会造成内存泄露，因为 App 本身就是全局的单例
    public final static String DB_NAME = "loread_DB";
    //    public final static int DB_VERSION = 4;
    public final static int theme_Day = 0;
    public final static int theme_Night = 1;
    private final static boolean isDebug = false;

    // 跟使用的 API 有关的 字段
    public static long UserID;
    public static String StreamId;
    public static String StreamTitle;
    public static String StreamState; // 这个只是从 Read 属性的4个类型(Readed, UnRead, UnReading, All), Star 属性的3个类型(Stared, UnStar, All)中，生硬的抽出 UnRead(含UnReading), Stared, All 3个快捷状态，供用户在主页面切换时使用
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
    public static String currentArticleID;
    public static Handler artHandler;
    public static boolean syncFirstOpen;
    public static long time;
    public boolean isSyncing = false;
    public long lastSyncTime = 0L;
//    public static boolean lastSyncResult = false; // True = Success, false = fail
//    public static String PROXY_ADDR;
//    public static int PROXY_PORT;


    public static String cacheRelativePath,cacheAbsolutePath ,boxRelativePath, boxAbsolutePath, storeRelativePath, storeAbsolutePath ;
    public static String boxReadRelativePath, storeReadRelativePath;
    public static String logRelativePath,logAbsolutePath;
    public static String externalFilesDir;

    private static DaoSession daoSession;

    public static synchronized App i() {
        return instance;
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            Glide.get(this).clearMemory();
        }
        Glide.get(this).trimMemory(level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        // 内存低的时候，清理 Glide 的缓存
        Glide.get(this).clearMemory();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        App.instance = this;
        initConfig();
        initTheme();

        initLogAndCrash();
        InoApi.i().init();
        OkGo.getInstance().init(this); // 初始化网络框架
//        initLeakCanary();
    }

    public List<Article> updateArtList(List<Article> temps) {
        articleList.clear();
        articleList.addAll(temps);
        return articleList;
    }

    public List<Tag> updateTagList(List<Tag> temps) {
        tagList.clear();
        tagList.addAll(temps);
        return tagList;
    }

    private void initLogAndCrash() {
        CrashReport.setIsDevelopmentDevice(this, BuildConfig.DEBUG);
        if (BuildConfig.DEBUG) {
            CrashReport.initCrashReport(App.i(), "900044326", true); // 测试的时候设为 true
        } else {
            CrashReport.initCrashReport(App.i(), "900044326", false); // 测试的时候设为 true
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
        if (!WithSet.i().isInoreaderProxy()) {
            Api.HOST = InoApi.HOST;
        }else {
            Api.HOST = WithSet.i().getInoreaderProxyHost();
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
        InoApi.INOREADER_ATUH = WithSet.i().getAuth();
        // 读取uid
        UserID = WithSet.i().getUseId();
        StreamState = WithSet.i().getStreamState();
        StreamId = WithSet.i().getStreamId();
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
        WithSet.i().clear();
        WithDB.i().clear();
        OkGo.getInstance().cancelAll();
    }




    protected void initTheme() {
        if (!WithSet.i().isAutoToggleTheme()) {
            return;
        }
        int hour = TimeUtil.getCurrentHour();
        if (hour >= 7 && hour <= 20) {
            WithSet.i().setThemeMode(App.theme_Day);
        } else {
            WithSet.i().setThemeMode(App.theme_Night);
        }
    }


    // 官方推荐将获取 DaoMaster 对象的方法放到 Application 层，这样将避免多次创建生成 Session 对象
    public DaoSession getDaoSession() {
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
