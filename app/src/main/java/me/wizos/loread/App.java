package me.wizos.loread;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.webkit.WebViewCompat;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.carlt.networklibs.NetType;
import com.carlt.networklibs.NetworkManager;
import com.carlt.networklibs.annotation.NetWork;
import com.cretin.www.cretinautoupdatelibrary.model.TypeConfig;
import com.cretin.www.cretinautoupdatelibrary.model.UpdateConfig;
import com.cretin.www.cretinautoupdatelibrary.utils.AppUpdateUtils;
import com.didichuxing.doraemonkit.DoraemonKit;
import com.elvishew.xlog.XLog;
import com.hjq.toast.ToastUtils;
import com.hjq.toast.style.ToastAliPayStyle;
import com.just.agentweb.AgentWebConfig;
import com.lzy.okgo.OkGo;
import com.oasisfeng.condom.CondomProcess;
import com.tencent.mmkv.MMKV;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.umcrash.UMCrash;
import com.umeng.umcrash.UMCrashCallback;
import com.yhao.floatwindow.view.FloatWindow;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.wizos.loread.activity.SplashActivity;
import me.wizos.loread.config.update.AppUpdateModel;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.CorePref;
import me.wizos.loread.db.User;
import me.wizos.loread.log.LogHelper;
import me.wizos.loread.network.api.AuthApi;
import me.wizos.loread.network.api.BaseApi;
import me.wizos.loread.network.api.FeedlyApi;
import me.wizos.loread.network.api.FeverApi;
import me.wizos.loread.network.api.FeverTinyRSSApi;
import me.wizos.loread.network.api.InoReaderApi;
import me.wizos.loread.network.api.LocalApi;
import me.wizos.loread.network.api.OAuthApi;
import me.wizos.loread.network.api.TinyRSSApi;
import me.wizos.loread.network.proxy.ProxyNodeSocks5;
import me.wizos.loread.service.TimeHandler;
import me.wizos.loread.utils.FileUtils;
import me.wizos.loread.utils.NetworkUtils;
import me.wizos.loread.utils.ScriptUtils;
import me.wizos.loread.utils.Tool;
import me.wizos.loread.utils.VersionUtils;
import me.wizos.loread.view.webview.WebViewS;

import static me.wizos.loread.utils.NetworkUtils.NETWORK_MOBILE;
import static me.wizos.loread.utils.NetworkUtils.NETWORK_NONE;
import static me.wizos.loread.utils.NetworkUtils.NETWORK_WIFI;


/**
 * 在Android中，可以通过继承Application类来实现应用程序级的全局变量，这种全局变量方法相对静态类更有保障，直到应用的所有Activity全部被destory掉之后才会被释放掉。
 * <p> App 类是全局的单例
 * Created by Wizos on 2015/12/24.
 */
public class App extends Application implements Thread.UncaughtExceptionHandler {
    private static String TAG = "App";
    private static App instance;
    public static final String CATEGORY_ALL = "/category/global.all";
    // public static final String CATEGORY_UNCATEGORIZED = "/category/global.uncategorized";
    // 已退订的文章
    public static final String STREAM_UNSUBSCRIBED = "/category/global.unsubscribed";
    // public static final String CATEGORY_TAG = "/category/global.tag";
    // public static final String CATEGORY_SEARCH = "/category/global.search";

    public static final String CATEGORY_STARED = "/tag/global.saved";
    public static final String CATEGORY_MUST = "/category/global.must";

    public static final int OPEN_MODE_RSS = 0;
    public static final int OPEN_MODE_READABILITY = 1;
    public static final int OPEN_MODE_LINK = 2;

    public static final int STATUS_NOT_FILED = 0;
    public static final int STATUS_TO_BE_FILED = 1;
    public static final int STATUS_IS_FILED = 2;

    // public static final int ActivityResult_LoginPageToProvider = 1;
    public static final int ActivityResult_ArtToMain = 2;

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

    public final static int THEME_DAY = 0;
    public final static int THEME_NIGHT = 1;

    public int screenWidth;
    public int screenHeight;

    public ProxyNodeSocks5 proxyNodeSocks5;

    // public ArticlePagedListAdapter articlesAdapter;

    private List<String> articleIds;

    public List<String> getArticleIds() {
        return articleIds;
    }

    public void setArticleIds(List<String> articleIds) {
        this.articleIds = articleIds;
    }

    public boolean isSyncing = false;
    public static String webViewBaseUrl;

    // 保存文章进度
    public LinkedHashMap<String, Integer> articleProgress = new LinkedHashMap<String, Integer>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
            return size() > 8;
        }
    };
    // 保存文章内容的收个关键词，用于提取全文时，给相似区域增加权重
    public LinkedHashMap<String, String> articleFirstKeyword = new LinkedHashMap<String, String>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > 8;
        }
    };
    // 保存提取全文后，之前的文章信息
    public LinkedHashMap<String, Article> oldArticles = new LinkedHashMap<String, Article>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Article> eldest) {
            return size() > 8;
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
    private long lastShowTimeMillis = 0;

    public long getLastShowTimeMillis() {
        return lastShowTimeMillis;
    }

    public void setLastShowTimeMillis(long lastShowTimeMillis) {
        this.lastShowTimeMillis = lastShowTimeMillis;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        MMKV.initialize(this);
        CoreDB.init(this);
        LogHelper.init(this, CorePref.i().globalPref().getBoolean(Contract.ENABLE_LOGGING, false));

        initVar();
        TimeHandler.init(this);
        ToastUtils.init(this, new ToastAliPayStyle(this));

        DoraemonKit.install(this, BuildConfig.DORAEMON_KIT_PRODUCT_ID);

        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE,"");
        // 打开统计SDK调试模式
        UMConfigure.setLogEnabled(BuildConfig.DEBUG);
        // 子进程是否支持自定义事件统计。参数：boolean 默认不使用
        UMConfigure. setProcessEvent(false);

        XLog.i("App 版本：" + VersionUtils.getVersionName(this));
        XLog.i("UMeng 信息：" + Arrays.toString(UMConfigure.getTestDeviceInfo(this)));
        if(BuildConfig.DEBUG){
            MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL);
        }else {
            // 选用AUTO页面采集模式
            MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        }

        // 【提前初始化 WebView 内核】由于其内部会调用 Looper ，不能放在子线程中
        // 链接：https://www.jianshu.com/p/fc7909e24178
        // 经过测试，采用Application要比采用Activity的context要少用20~30M左右的内存。但采用Application会影响在 webview 中打开对话框。
        new WebViewS(this).destroy();
        CorePref.i().globalPref().putString(Contract.USER_AGENT, WebSettings.getDefaultUserAgent(this));

        PackageInfo webViewPackageInfo = WebViewCompat.getCurrentWebViewPackage(this);
        XLog.i("WebView 版本: " + (webViewPackageInfo == null ? "" :webViewPackageInfo.versionName) );

        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        if(BuildConfig.DEBUG) AgentWebConfig.debug();

        // OrbotHelper是一个单例，用于管理在app与Orbot之间的大量的异步通信。
        // 它旨在app生命周期的早期进行初始化。 一种可能的选择是拥有一个自定义的Application子类，您可以在其中重写onCreate（）并设置OrbotHelper。
        // OrbotHelper.get(this).init();

        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                FloatWindow.init(instance);
                // 初始化网络框架
                OkGo.getInstance().init(instance);
                // 监听子线程的报错
                Thread.setDefaultUncaughtExceptionHandler(instance);

                initAppUpdate();

                ScriptUtils.init();

                // 初始化网络状态
                NetworkUtils.getNetWorkState();

                // 尽可能早的进行这一步操作, 建议在 Application 中完成初始化操作
                NetworkManager.getInstance().init(instance);
                //注册
                NetworkManager.getInstance().registerObserver(instance);

                // 保险套
                CondomProcess.installExceptDefaultProcess(instance);
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
                XLog.i("wifi");
                NetworkUtils.setTheNetwork(NETWORK_WIFI);
                break;
            case CMNET:
            case CMWAP:
                XLog.i("4G");
                NetworkUtils.setTheNetwork(NETWORK_MOBILE);
                break;
            case AUTO:
                XLog.i("自动");
                break;
            case NONE:
                XLog.i("无网络");
                NetworkUtils.setTheNetwork(NETWORK_NONE);
                break;
            default:
                break;
        }
    }

    @Override
    public void uncaughtException(@NotNull Thread thread, @NotNull Throwable ex) {
        XLog.e("线程意外报错：");
        Tool.printCallStack(ex);
        UMCrash.registerUMCrashCallback(new UMCrashCallback() {
            @Override
            public String onCallback() {
                return "崩溃：" + ex.getLocalizedMessage();
            }
        });
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
        Glide.get(this).trimMemory(level);
    }

    /**
     * 内存低的时候执行
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).clearMemory();
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

    private void initAppUpdate(){
        //当你希望使用配置请求链接的方式，让插件自己解析并实现更新
        UpdateConfig updateConfig = new UpdateConfig()
                .setDebug(BuildConfig.DEBUG)//是否是Debug模式
                .setBaseUrl("https://raw.githubusercontent.com/wizos/loread/master/config.json")//当dataSourceType为DATA_SOURCE_TYPE_URL时，配置此接口用于获取更新信息
                .setMethodType(TypeConfig.METHOD_GET)//当dataSourceType为DATA_SOURCE_TYPE_URL时，设置请求的方法
                .setDataSourceType(TypeConfig.DATA_SOURCE_TYPE_URL)//设置获取更新信息的方式
                .setShowNotification(true)//配置更新的过程中是否在通知栏显示进度
                .setNotificationIconRes(R.mipmap.ic_launcher)//配置通知栏显示的图标
                .setUiThemeType(TypeConfig.UI_THEME_G)//配置UI的样式，一种有12种样式可供选择
                .setRequestHeaders(null)//当dataSourceType为DATA_SOURCE_TYPE_URL时，设置请求的请求头
                .setRequestParams(null)//当dataSourceType为DATA_SOURCE_TYPE_URL时，设置请求的请求参数
                .setAutoDownloadBackground(false)//是否需要后台静默下载，如果设置为true，则调用checkUpdate方法之后会直接下载安装，不会弹出更新页面。当你选择UI样式为TypeConfig.UI_THEME_CUSTOM，静默安装失效，您需要在自定义的Activity中自主实现静默下载，使用这种方式的时候建议setShowNotification(false)，这样基本上用户就会对下载无感知了
                // .setCustomActivityClass(CustomActivity.class)//如果你选择的UI样式为TypeConfig.UI_THEME_CUSTOM，那么你需要自定义一个Activity继承自RootActivity，并参照demo实现功能，在此处填写自定义Activity的class
                .setNeedFileMD5Check(false)//是否需要进行文件的MD5检验，如果开启需要提供文件本身正确的MD5校验码，DEMO中提供了获取文件MD5检验码的工具页面，也提供了加密工具类Md5Utils
                // .setCustomDownloadConnectionCreator(new OkHttp3Connection.Creator(builder))//如果你想使用okhttp作为下载的载体，可以使用如下代码创建一个OkHttpClient，并使用demo中提供的OkHttp3Connection构建一个ConnectionCreator传入，在这里可以配置信任所有的证书，可解决根证书不被信任导致无法下载apk的问题
                .setModelClass(new AppUpdateModel());
        AppUpdateUtils.init(this, updateConfig);
    }

    public String getGlobalAssetsFilesDir() {
        return getExternalFilesDir(null) + File.separator + "assets" + File.separator;
    }

    public String getGlobalConfigPath() {
        return getExternalFilesDir(null) + File.separator + "config" + File.separator;
    }


    // XLog.e("路径1：" + new File(filePath).toURI()); // file:/storage/emulated/0/Android/data/me.wizos.loread/files
    // XLog.e("路径2：" + new File(filePath).getAbsolutePath());
    // XLog.e("路径3：" + App.i().getFilesDir()); // /data/user/0/me.wizos.loread/files
    // XLog.e("路径4：" + App.i().getExternalCacheDir()); // /storage/emulated/0/Android/data/me.wizos.loread/cache
    // XLog.e("路径5：" + App.i().getExternalFilesDir(null)); // /storage/emulated/0/Android/data/me.wizos.loread/files
    public String getUserFilesDir() {
        if (user == null) {
            XLog.e("用户为空");
            return getExternalFilesDir(null) + File.separator;
        }else {
            return getExternalFilesDir(null) + File.separator + user.getId();
        }
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
        CorePref.i().globalPref().getString(Contract.UID, null);
        OkGo.getInstance().cancelAll();
        String uid = App.i().getUser().getId();
        WorkManager.getInstance(this).cancelAllWork();
        CoreDB.i().articleDao().clear(uid);
        CoreDB.i().articleTagDao().clear(uid);
        CoreDB.i().tagDao().clear(uid);
        CoreDB.i().feedDao().clear(uid);
        CoreDB.i().categoryDao().clear(uid);
        CoreDB.i().feedCategoryDao().clear(uid);
        CoreDB.i().userDao().delete(uid);
        FileUtils.deleteHtmlDir(new File(App.i().getUserFilesDir()));
    }

    private BaseApi api;
    public User user;


    public User getUser() {
        if (user == null) {
            String uid = CorePref.i().globalPref().getString(Contract.UID, null);
            if (!TextUtils.isEmpty(uid)) {
                user = CoreDB.i().userDao().getById(uid);
            }
        }
        return user;
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
    public void resetApi(){
        api = null;
    }
    public BaseApi getApi() {
        if (api == null) {
            String source = getUser().getSource();
            switch (source) {
                case Contract.PROVIDER_TINYRSS:
                    TinyRSSApi tinyRSSApi = new TinyRSSApi(getUser().getHost());
                    tinyRSSApi.setAuthorization(getUser().getAuth());
                    api = tinyRSSApi;
                    break;
                case Contract.PROVIDER_FEVER_TINYRSS:
                    FeverTinyRSSApi feverTinyRSSApi = new FeverTinyRSSApi(getUser().getHost());
                    feverTinyRSSApi.setAuthorization(getUser().getAuth());
                    api = feverTinyRSSApi;
                    break;
                case Contract.PROVIDER_FEVER:
                    FeverApi feverApi = new FeverApi(getUser().getHost());
                    feverApi.setAuthorization(getUser().getAuth());
                    api = feverApi;
                    break;
                // case Contract.PROVIDER_LOREAD:
                //     LoreadApi loreadApi = new LoreadApi(getUser().getHost());
                //     loreadApi.setAuthorization(getUser().getAuth());
                //     api = loreadApi;
                //     break;
                case Contract.PROVIDER_INOREADER:
                    InoReaderApi inoReaderApi = new InoReaderApi(getUser().getHost());
                    inoReaderApi.setAuthorization(getUser().getAuth());
                    api = inoReaderApi;
                    break;
                case Contract.PROVIDER_FEEDLY:
                    FeedlyApi feedlyApi = new FeedlyApi();
                    feedlyApi.setAuthorization(getUser().getAuth());
                    api = feedlyApi;
                    break;
                case Contract.PROVIDER_LOCALRSS:
                    api = new LocalApi();
                    break;
            }
            // XLog.i("getApi = " + getUser().getSource() + " - " + App.i().getUser().getAuth());
        }
        return api;
    }

    public void restartApp() {
        App.i().user = null;
        Intent intent = new Intent(this, SplashActivity.class);
        // Intent.FLAG_ACTIVITY_CLEAR_TASK 要起作用，必须和 Intent.FLAG_ACTIVITY_NEW_TASK 配合使用。
        // 这两个 Flag 可以将原有任务栈清空,并将 intent 的目标 Activity 作为任务栈的根 Activity 。任务栈的 Id 没有变，如下所示，也就是说，并没有开辟新的任务栈。
        // 链接：https://www.jianshu.com/p/e34ee1978fce
        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }




    // private List<ThemeChangeObserver> mThemeChangeObserverStack; //  主题切换监听栈
    // /**
    //  * 获得observer堆栈
    //  * */
    // private List<ThemeChangeObserver> obtainThemeChangeObserverStack() {
    //     if (mThemeChangeObserverStack == null) mThemeChangeObserverStack = new ArrayList<>();
    //     return mThemeChangeObserverStack;
    // }
    // /**
    //  * 向堆栈中添加observer
    //  * */
    // public void registerObserver(ThemeChangeObserver observer) {
    //     if (observer == null || obtainThemeChangeObserverStack().contains(observer)) return ;
    //     obtainThemeChangeObserverStack().add(observer);
    // }
    // /**
    //  * 从堆栈中移除observer
    //  * */
    // public void unregisterObserver(ThemeChangeObserver observer) {
    //     if (observer == null || !(obtainThemeChangeObserverStack().contains(observer))) return ;
    //     obtainThemeChangeObserverStack().remove(observer);
    // }
    // /**
    //  * 向堆栈中所有对象发送更新UI的指令
    //  * */
    // public void notifyByThemeChanged() {
    //     List<ThemeChangeObserver> observers = obtainThemeChangeObserverStack();
    //     for (ThemeChangeObserver observer : observers) {
    //         observer.declareCurrentTheme();
    //         observer.notifyByThemeChanged();
    //     }
    // }

}
