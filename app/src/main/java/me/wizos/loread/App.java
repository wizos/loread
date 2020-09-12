package me.wizos.loread;

import android.app.Application;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.webkit.WebView;

import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.carlt.networklibs.NetType;
import com.carlt.networklibs.NetworkManager;
import com.carlt.networklibs.annotation.NetWork;
import com.carlt.networklibs.utils.Constants;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.ToastAliPayStyle;
import com.lzy.okgo.OkGo;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.socks.library.KLog;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatCrashReporter;
import com.tencent.stat.StatService;
import com.tencent.stat.common.StatConstants;
import com.yhao.floatwindow.view.FloatWindow;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import me.wizos.loread.activity.SplashActivity;
import me.wizos.loread.adapter.ArticlePagedListAdapter;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.CorePref;
import me.wizos.loread.db.User;
import me.wizos.loread.network.api.AuthApi;
import me.wizos.loread.network.api.BaseApi;
import me.wizos.loread.network.api.FeedlyApi;
import me.wizos.loread.network.api.InoReaderApi;
import me.wizos.loread.network.api.LoreadApi;
import me.wizos.loread.network.api.OAuthApi;
import me.wizos.loread.network.api.TinyRSSApi;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.NetworkUtil;
import me.wizos.loread.utils.ScriptUtil;
import me.wizos.loread.utils.Tool;
import me.wizos.loread.view.WebViewS;

import static me.wizos.loread.utils.NetworkUtil.NETWORK_MOBILE;
import static me.wizos.loread.utils.NetworkUtil.NETWORK_NONE;
import static me.wizos.loread.utils.NetworkUtil.NETWORK_WIFI;

/**
 * 在Android中，可以通过继承Application类来实现应用程序级的全局变量，这种全局变量方法相对静态类更有保障，直到应用的所有Activity全部被destory掉之后才会被释放掉。
 * <p> App 类是全局的单例
 * Created by Wizos on 2015/12/24.
 */
public class App extends Application implements Thread.UncaughtExceptionHandler {
    private static String TAG = "App";
    private static App instance;
    public static final String CATEGORY_ALL = "/category/global.all";
    public static final String CATEGORY_UNCATEGORIZED = "/category/global.uncategorized";
    public static final String CATEGORY_TAG = "/category/global.tag";
    public static final String CATEGORY_SEARCH = "/category/global.search";

    public static final String CATEGORY_STARED = "/tag/global.saved";
    public static final String CATEGORY_MUST = "/category/global.must";

    public static final String Referer = "Referer";

    public static final String DISPLAY_RSS = "rss";
    public static final String DISPLAY_READABILITY = "readability";
    public static final String DISPLAY_LINK = "webpage";
    public static final int OPEN_MODE_RSS = 0;
    public static final int OPEN_MODE_LINK = 1;
    public static final int OPEN_MODE_READABILITY = 2;

    public static final int STATUS_NOT_FILED = 0;
    public static final int STATUS_TO_BE_FILED = 1;
    public static final int STATUS_IS_FILED = 2;

    public static final String NOT_FILED = "cache";
    public static final String TO_BE_FILED = "box";
    public static final String IS_FILED = "store";

    public static final int ActivityResult_LoginPageToProvider = 1;
    public static final int ActivityResult_ArtToMain = 2;
    public static final int ActivityResult_SearchLocalArtsToMain = 3;

    public static final int TYPE_GROUP = 0;
    public static final int TYPE_FEED = 1;

    // 状态为所有
    public static final int STATUS_ALL = 0;
    // 0 未读
    public static final int STATUS_UNREAD = 1;
    // 1 已读
    public static final int STATUS_READED = 2;
    // 00 强制未读
    public static final int STATUS_UNREADING = 3;
    // 为什么这里不用1和0？因为在用streamStatus时，会综合readStatus和starStatus，如果有重复的就会取不到
    // 类似 StreamIds 将 feed,category,tag 综合考虑，StreamStatus是将 readStatus,starStatus 综合在一起考虑
    public static final int STATUS_STARED = 4;
    public static final int STATUS_UNSTAR = 5;

    public static final int MSG_DOUBLE_TAP = -1;

    public int screenWidth;
    public int screenHeight;

    public final static int THEME_DAY = 0;
    public final static int THEME_NIGHT = 1;

    public ArticlePagedListAdapter articlesAdapter;

    // public boolean isSyncing = false;
    public static String webViewBaseUrl;

    public LinkedHashMap<String, Integer> articleProgress = new LinkedHashMap<String, Integer>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
            return size() > 16;
        }
    };


    public static App i() {
        if (instance == null) { // 双重锁定，只有在 withDB 还没被初始化的时候才会进入到下一行，然后加上同步锁
            synchronized (App.class) { // 同步锁，避免多线程时可能 new 出两个实例的情况
                if (instance == null) {
                    instance = new App();
                }
            }
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        initVar();

        ToastUtils.init(this, new ToastAliPayStyle(this));

        CoreDB.init(instance);

        // 【提前初始化 WebView 内核】由于其内部会调用 Looper ，不能放在子线程中
        // 链接：https://www.jianshu.com/p/fc7909e24178
        // 经过测试，采用Application要比采用Activity的context要少用20~30M左右的内存。但采用Application会影响在 webview 中打开对话框。
        WebViewS articleWebView = new WebViewS(this);
        articleWebView.destroy();

        if (BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        Logger.addLogAdapter(new AndroidLogAdapter());

        initCrashReport();

        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                FloatWindow.init(instance);
                // 初始化网络框架
                OkGo.getInstance().init(instance);
                // 监听子线程的报错
                Thread.setDefaultUncaughtExceptionHandler(instance);
                // 初始化统计&监控服务
                KLog.init(BuildConfig.DEBUG);
                // initLeakCanary();

                ScriptUtil.init();

                // 初始化网络状态
                NetworkUtil.getNetWorkState();

                // 尽可能早的进行这一步操作, 建议在 Application 中完成初始化操作
                NetworkManager.getInstance().init(instance);
                //注册
                NetworkManager.getInstance().registerObserver(instance);

                // 保险套
                // CondomProcess.installExceptDefaultProcess(instance);
            }
        });
    }

    /**
     * Application结束的时候会调用,由系统决定调用的时机
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
        NetworkManager.getInstance().unRegisterObserver(this);
    }


    //所有网络变化都会被调用，可以通过 NetType 来判断当前网络具体状态
    @NetWork(netType = NetType.AUTO)
    public void network(NetType netType) {
        switch (netType) {
            case WIFI:
                KLog.e(Constants.LOG_TAG, "wifi");
                NetworkUtil.setTheNetwork(NETWORK_WIFI);
                break;
            case CMNET:
            case CMWAP:
                KLog.e(Constants.LOG_TAG, "4G");
                NetworkUtil.setTheNetwork(NETWORK_MOBILE);
                break;
            case AUTO:
                KLog.e(Constants.LOG_TAG, "自动");
                break;
            case NONE:
                KLog.e(Constants.LOG_TAG, "无网络");
                NetworkUtil.setTheNetwork(NETWORK_NONE);
                break;
            default:
                break;
        }
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        KLog.e("线程意外报错");
        ex.printStackTrace();
    }


    /**
     * 程序在内存清理的时候执行
     * OnTrimMemory是Android在4.0之后加入的一个回调，任何实现了ComponentCallbacks2接口的类都可以重写实现这个回调方法．OnTrimMemory的主要作用就是指导应用程序在不同的情况下进行自身的内存释放，以避免被系统直接杀掉，提高应用程序的用户体验.
     * <p>
     * TRIM_MEMORY_COMPLETE (80)：内存不足，并且该进程在后台进程列表最后一个，马上就要被清理
     * TRIM_MEMORY_MODERATE (60)：内存不足，并且该进程在后台进程列表的中部。
     * TRIM_MEMORY_BACKGROUND (40)：内存不足，并且该进程是后台进程。
     * TRIM_MEMORY_UI_HIDDEN (20)：内存不足，并且该进程的UI已经不可见了。（应用程序的所有UI界面被隐藏了，即用户点击了Home键或者Back键导致应用的UI界面不可见）
     * <p>
     * 以上4个是4.0增加
     * TRIM_MEMORY_RUNNING_CRITICAL (15) ：内存不足(后台进程不足3个)，并且该进程优先级比较高，需要清理内存
     * TRIM_MEMORY_RUNNING_LOW (10) ：内存不足(后台进程不足5个)，并且该进程优先级比较高，需要清理内存
     * TRIM_MEMORY_RUNNING_MODERATE (5) ：内存不足(后台进程超过5个)，并且该进程优先级比较高，需要清理内存
     * 以上3个是4.1增加
     * <p>
     * 作者：Gracker
     * 链接：https://www.jianshu.com/p/5b30bae0eb49
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


    private void initCrashReport() {
        // 腾讯统计，[可选]设置是否打开debug输出，上线时请关闭，Logcat标签为"MtaSDK"
        StatConfig.setDebugEnable(BuildConfig.DEBUG);
        // 【基础统计API】由于其内部会调用 Looper.prepare() ，不能放在子线程中
        StatService.registerActivityLifecycleCallbacks(this);
        // 初始化并启动MTA：启动MTA线程，加载数据库配置信息，初始化环境，同时还对多版本SDK进行冲突检测
        try {
            // 第三个参数必须为：com.tencent.stat.common.StatConstants.VERSION
            StatService.startStatService(this, "AAI4F2S2LM1U", StatConstants.VERSION);
            KLog.d("MTA", "MTA初始化成功");
        } catch (MtaSDkException e) {
            // MTA初始化失败
            KLog.d("MTA", "MTA初始化失败" + e);
        }
        // 开启或禁用java异常捕获，初始化不会带来任何的流量和性能消耗。生效后，会注册DefaultUncaughtExceptionHandler，crash时捕获相关信息，存储在本地并上报。
        // 可通过添加StatCrashCallback监听Crash发生。
        StatCrashReporter.getStatCrashReporter(getApplicationContext()).setJavaCrashHandlerStatus(true);

        // 为了保证运营数据的准确性，建议不要在异步线程初始化Bugly。
        CrashReport.initCrashReport(getApplicationContext(), "900044326", BuildConfig.DEBUG);
        // 官网现在改用以上的方式
        //  Bugly.init(getApplicationContext(), "900044326", BuildConfig.DEBUG);
        // 在开发测试阶段，可以在初始化Bugly之前通过以下接口把调试设备设置成“开发设备”。
        CrashReport.setIsDevelopmentDevice(instance, BuildConfig.DEBUG);
    }

    public String getWebViewBaseUrl() {
        if (TextUtils.isEmpty(webViewBaseUrl)) {
            webViewBaseUrl = "file://" + getUserConfigPath();
        }
        return webViewBaseUrl;
    }

    private void initVar() {
        DisplayMetrics outMetrics = getResources().getDisplayMetrics();
        screenWidth = outMetrics.widthPixels;
        screenHeight = outMetrics.heightPixels;
    }



    public String getGlobalAssetsFilesDir() {
        return getExternalFilesDir(null) + File.separator + "assets" + File.separator;
    }

    public String getGlobalConfigPath() {
        return getExternalFilesDir(null) + File.separator + "config" + File.separator;
    }

    public String getUserFilesDir() {
        if (user == null) {
            KLog.e("用户为空");
            Tool.printCallStatck();
            return getExternalFilesDir(null) + "/";
        }
        return getExternalFilesDir(null) + File.separator + user.getId();
    }
    public String getUserConfigPath() {
        return getUserFilesDir() + File.separator + "config" + File.separator;
    }

    public String getUserCachePath() {
        return getUserFilesDir() + File.separator + "cache" + File.separator;
    }

    public String getUserBoxPath() {
        return getUserFilesDir() + File.separator + "box" + File.separator;
    }

    public String getUserStorePath() {
        return getUserFilesDir() + File.separator + "store" + File.separator;
    }


    public void clearApiData() {
        getKeyValue().getString(Contract.UID, null);
        OkGo.getInstance().cancelAll();
        WorkManager.getInstance(this).cancelAllWork();
        CoreDB.i().articleDao().clear(App.i().getUser().getId());
        CoreDB.i().feedDao().clear(App.i().getUser().getId());
        CoreDB.i().categoryDao().clear(App.i().getUser().getId());
        CoreDB.i().feedCategoryDao().clear(App.i().getUser().getId());
        CoreDB.i().userDao().delete(App.i().getUser().getId());
        FileUtil.deleteHtmlDir(new File(App.i().getUserFilesDir()));
    }

    private BaseApi api;
    public User user;


    public User getUser() {
        if (user == null) {
            String uid = getKeyValue().getString(Contract.UID, null);
            if (!TextUtils.isEmpty(uid)) {
                user = CoreDB.i().userDao().getById(uid);
            }
        }
        return user;
    }

    public CorePref getKeyValue() {
        return CorePref.i();
    }

    public AuthApi getAuthApi() {
        return (AuthApi) getApi();
    }

    public OAuthApi getOAuthApi() {
        return (OAuthApi) getApi();
    }

    public void setApi(BaseApi baseApi) {
        this.api = baseApi;
    }

    public BaseApi getApi() {
        if (api == null) {
            switch (getUser().getSource()) {
                case Contract.PROVIDER_TINYRSS:
                    TinyRSSApi tinyRSSApi = new TinyRSSApi();
                    tinyRSSApi.setAuthorization(getUser().getAuth());
                    api = tinyRSSApi;
                    break;
                case Contract.PROVIDER_LOREAD:
                    LoreadApi loreadApi = new LoreadApi();
                    loreadApi.setAuthorization(getUser().getAuth());
                    api = loreadApi;
                    break;
                case Contract.PROVIDER_INOREADER:
                    InoReaderApi inoReaderApi = new InoReaderApi();
                    inoReaderApi.setAuthorization(getUser().getAuth());
                    api = inoReaderApi;
                    break;
                case Contract.PROVIDER_FEEDLY:
                    FeedlyApi feedlyApi = new FeedlyApi();
                    feedlyApi.setAuthorization(getUser().getAuth());
                    api = feedlyApi;
                    break;
                case Contract.PROVIDER_LOCALRSS:
                    break;
            }
            KLog.i("初始化 " + getUser().getSource() + " = " + App.i().getUser().getAuth());
        }
        return api;
    }

    public void restartApp() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    //    //  内存泄漏检测工具
    //    private void initLeakCanary() {
    ////        if (LeakCanary.isInAnalyzerProcess(this)) {
    ////            return;
    ////        }
    //        LeakCanary.install(this);
    //    }
}
