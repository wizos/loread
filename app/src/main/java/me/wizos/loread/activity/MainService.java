package me.wizos.loread.activity;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.exception.HttpException;
import com.lzy.okgo.model.Response;
import com.socks.library.KLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.Article;
import me.wizos.loread.bean.Feed;
import me.wizos.loread.bean.Tag;
import me.wizos.loread.bean.gson.son.ItemRefs;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.net.Api;
import me.wizos.loread.net.DataApi;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.HttpUtil;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.utils.ToastUtil;
import me.wizos.loread.utils.Tool;

/**
 * FetcherService
 * 如果后台任务只有一个的话，onHandleIntent执行完，服务就会销毁，但如果后台任务有多个的话，onHandleIntent执行完最后一个任务时，服务才销毁。
 * 最后我们要知道每次执行一个后台任务就必须启动一次IntentService，而IntentService内部则是通过消息的方式发送给HandlerThread的，然后由Handler中的Looper来处理消息，而Looper是按顺序从消息队列中取任务的，也就是说IntentService的后台任务时顺序执行的，当有多个后台任务同时存在时，这些后台任务会按外部调用的顺序排队执行。
 *
 * 所有的请求都被一个单独的工作者线程处理--他们或许需要足够长的时间来处理（并且不会阻塞应用的主循环），但是同一时间只能处理一个请求。
 * Created by Wizos on 2017/1/7.
 */

public class MainService extends IntentService {
    private final static String TAG = "MainService";

    public MainService() {
        super("MainService");
    }

    private Handler maHandler; // MainActivityHandler
    //    private Handler msHandler; // MainServiceHandler
//    private Neter mNeter;
//    private Parser mParser;
//    private String continuation;
    private int alreadySyncedArtsNum = 0; // 同步了文章内容的数量
    private int readySyncArtsCapacity = 0;

    @Override
    public void onCreate() {
        super.onCreate();
//        msHandler = new Handler(Looper.myLooper(), new MSCallback());
//        mNeter = new Neter(msHandler);
//        App.mNeter = mNeter;
//        mNeter.setReord(recorder);
//        requestMap = new ArrayMap<>();
    }

    // Activity 与 Service 通信方式 总结：http://www.jianshu.com/p/cd69f208f395
    // 使用 startService(intent); 多次启动IntentService，但IntentService的实例只有一个，这跟传统的Service是一样的.
    // 最终IntentService会去调用onHandleIntent执行异步任务。这里可能我们还会担心for循环去启动任务，而实例又只有一个，那么任务会不会被覆盖掉呢？其实是不会的，因为IntentService真正执行异步任务的是HandlerThread+Handler
   /* 第一种模式通信：Binder */
    public class ServiceBinder extends Binder {
        MainService getService() {
            return MainService.this;
        }
    }

    public ServiceBinder mBinder = new ServiceBinder(); /* 数据通信的桥梁 */

    /* 重写Binder的onBind函数，返回派生类 */
    @Override
    public IBinder onBind(Intent arg0) {
        super.onBind(arg0);
        return mBinder;
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (action.equals("syncAllStarred")) {  // 这个是在 SettingActivity 中实现的
            syncAllStarred();
        } else if (action.equals("unreadArticle")) {
//            mNeter.markArticleUnread(intent.getExtras().getString("articleId"));
            DataApi.i().markArticleUnread(intent.getExtras().getString("articleId"), new StringCallback() {
                @Override
                public void onSuccess(Response<String> response) {
                }
            });
        } else if (action.equals("readedArticle")) {
//            mNeter.markArticleReaded(intent.getExtras().getString("articleId"));
            DataApi.i().markArticleReaded(intent.getExtras().getString("articleId"), new StringCallback() {
                @Override
                public void onSuccess(Response<String> response) {
                }
            });

        } else if (action.equals("sync")) {
            long intervalTime = (System.currentTimeMillis() - App.i().lastSyncTime) / (1000 * 60);// 间隔了多少分钟
            if (intervalTime < 3) {
                sync();
            } else if (intervalTime < 20) {
                syncFast();
            } else {
                sync();
            }
        }

        int result = intent.getFlags();
        KLog.e(" 开始处理 = 结果：" + result + "当前县城" + Thread.currentThread());
    }


    public void regHandler(Handler mHandler) {
        this.maHandler = mHandler;
    }


    public void syncAllStarred() {
        if (App.i().isSyncing) {
            ToastUtil.showShort(getString(R.string.tips_is_in_syncing));
            return;
        }
        sendSyncStart();
        DataApi.FetchNotice fetchNotice = new DataApi.FetchNotice() {
            @Override
            public void onProcessChanged(int had, int count) {
                alreadySyncedArtsNum += had;
                sendSyncProcess(getString(R.string.main_toolbar_hint_sync_all_stared_content, alreadySyncedArtsNum));
            }
        };

        DataApi.i().regFetchNotice(fetchNotice);
        try {
            DataApi.i().fetchAllStaredStreamContent();
            WithSet.i().setHadSyncAllStarred(true);
            sendSyncSuccess();
        } catch (HttpException e) {
            e.printStackTrace();
            if (e.getMessage().equals("401")) {
                sendSyncNeedAuth();
            }
            Tool.showShort("syncAllStarred时出了异常：HttpException");
            sendSyncFailure();
        } catch (IOException e) {
            e.printStackTrace();
            Tool.showShort("syncAllStarred时出了异常：IOException");
            sendSyncFailure();
        }
        Tool.showShort("syncAllStarred时出了异常");
//        sync();  // 最后再来走一波同步？
    }


    private void syncFast() {
        if (App.i().isSyncing) {
            ToastUtil.showShort(getString(R.string.tips_is_in_syncing));
            return;
        }
        sendSyncStart();
        if (!HttpUtil.isNetworkAvailable()) { // note: 像这种没有网络的应该改为抛异常
            ToastUtil.showShort(getString(R.string.tips_no_net));
            sendSyncFailure();
            return;
        }
        try {
            KLog.e("syncFast 3 - 同步未读信息");
            sendSyncProcess(getString(R.string.main_toolbar_hint_sync_unread_refs));
            List<ItemRefs> unreadRefsList = DataApi.i().fetchUnreadRefs();

            KLog.e("syncFast 2 - 同步收藏信息");
            sendSyncProcess(getString(R.string.main_toolbar_hint_sync_stared_refs));
            List<ItemRefs> staredRefsList = DataApi.i().fetchStaredRefs();

            KLog.e("syncFast 1 - 同步文章内容");
            ArrayList<List<ItemRefs>> refsList = DataApi.i().splitRefs(unreadRefsList, staredRefsList);
            List<ItemRefs> itemRefsList;
            readySyncArtsCapacity = refsList.get(0).size() + refsList.get(1).size() + refsList.get(2).size();
            DataApi.FetchNotice fetchNotice = new DataApi.FetchNotice() {
                @Override
                public void onProcessChanged(int had, int count) {
                    alreadySyncedArtsNum += had;
                    sendSyncProcess(getString(R.string.main_toolbar_hint_sync_article_content, alreadySyncedArtsNum, readySyncArtsCapacity));
                }
            };
            DataApi.i().regFetchNotice(fetchNotice);

//            Queue<String> ids = new LinkedList();
            List<String> ids;
            ids = new ArrayList<>();
            itemRefsList = refsList.get(0);
            for (ItemRefs itemRefs : itemRefsList) {
                ids.add(itemRefs.getId());
            }

            KLog.e("syncFast 栈的数量A:" + ids.size());
            DataApi.i().fetchContentsUnreadUnstar(ids);
            KLog.e("syncFast 栈的数量B:" + ids.size());


            ids = new ArrayList<>();
            itemRefsList = refsList.get(1);
            for (ItemRefs itemRefs : itemRefsList) {
                ids.add(itemRefs.getId());
            }
            KLog.e("syncFast 栈的数量C:" + ids.size());
            DataApi.i().fetchContentsReadStarred(ids);

            ids = new ArrayList<>();
            itemRefsList = refsList.get(2);
            for (ItemRefs itemRefs : itemRefsList) {
                ids.add(itemRefs.getId());
            }
            KLog.e("syncFast 栈的数量D:" + ids.size());
            DataApi.i().fetchContentsUnreadStarred(ids);
            sendSyncSuccess();
            moveArticles();
            clearArticles(WithSet.i().getClearBeforeDay());
        } catch (HttpException e) {
            e.printStackTrace();

            if (e.getMessage().equals("401")) {
                sendSyncNeedAuth();
            }
//            ToastUtil.showShort("syncFast 时出了异常HttpException");
            sendSyncFailure();
        } catch (IOException e) {
            e.printStackTrace();
//            ToastUtil.showShort("syncFast 时出了异常IO");
            sendSyncFailure();
        }
    }

    private void sync() {
        if (App.i().isSyncing) {
            ToastUtil.showShort("当前正在同步中，请稍候再操作");
            return;
        }
        sendSyncStart();
        if (!HttpUtil.isNetworkAvailable()) { // note: 像这种没有网络的应该改为抛异常
            ToastUtil.showShort(getString(R.string.tips_no_net));
            sendSyncFailure();
            return;
        }
        try {
//            sendSyncProcess(getString(R.string.main_toolbar_hint_sync_log));
//            DataApi.i().fetchUserInfo();
//            KLog.e("7 - 同步分组信息"); // （分组）
            KLog.e("5 - 同步订阅源信息");
            sendSyncProcess(getString(R.string.main_toolbar_hint_sync_tag));
            List<Tag> tagList = DataApi.i().fetchTagList();
            List<Feed> feedList = DataApi.i().fetchFeedList();

            KLog.e("4 - 同步排序信息");
            sendSyncProcess(getString(R.string.main_toolbar_hint_sync_tag_order));
            if (WithSet.i().isOrderTagFeed()) {
                tagList = DataApi.i().fetchStreamPrefs(tagList);
            } else {
                tagList = DataApi.i().orderTags(tagList);
            }

//            KLog.e("4 - 获取未读数目");
//            sendSyncProcess(getString(R.string.main_toolbar_hint_sync_unread_count));
//            DataApi.i().fetchUnreadCounts();

            KLog.e("3 - 同步未读信息");
            sendSyncProcess(getString(R.string.main_toolbar_hint_sync_unread_refs));
            List<ItemRefs> unreadRefsList = DataApi.i().fetchUnreadRefs();


            KLog.e("2 - 同步收藏信息");
            sendSyncProcess(getString(R.string.main_toolbar_hint_sync_stared_refs));
            List<ItemRefs> staredRefsList = DataApi.i().fetchStaredRefs();

            // 如果在获取到数据的时候就保存，那么到这里同步断了的话，可能系统内的文章就找不到响应的分组，所有放到这里保存。（比如在云端将文章移到的新的分组）
            WithDB.i().coverSaveTagList(tagList);
            WithDB.i().coverSaveFeeds(feedList);

            KLog.e("1 - 同步文章内容");
            ArrayList<List<ItemRefs>> refsList = DataApi.i().splitRefs(unreadRefsList, staredRefsList);
            List<ItemRefs> itemRefsList;
            readySyncArtsCapacity = refsList.get(0).size() + refsList.get(1).size() + refsList.get(2).size();
            DataApi.FetchNotice fetchNotice = new DataApi.FetchNotice() {
                @Override
                public void onProcessChanged(int had, int count) {
                    KLog.e("继续同步B" + had + "==" + count);
                    alreadySyncedArtsNum += had;
                    sendSyncProcess(getString(R.string.main_toolbar_hint_sync_article_content, alreadySyncedArtsNum, readySyncArtsCapacity));
                }
            };
            DataApi.i().regFetchNotice(fetchNotice);

            List<String> ids;
            ids = new ArrayList<>();
            itemRefsList = refsList.get(0);
            for (ItemRefs itemRefs : itemRefsList) {
                ids.add(itemRefs.getId());
            }
            KLog.e("栈的数量A:" + ids.size());
            DataApi.i().fetchContentsUnreadUnstar(ids);


            ids = new ArrayList<>();
            itemRefsList = refsList.get(1);
            for (ItemRefs itemRefs : itemRefsList) {
                ids.add(itemRefs.getId());
            }
            KLog.e("栈的数量C:" + ids.size());
            DataApi.i().fetchContentsReadStarred(ids);

            ids = new ArrayList<>();
            itemRefsList = refsList.get(2);
            for (ItemRefs itemRefs : itemRefsList) {
                ids.add(itemRefs.getId());
            }
            KLog.e("栈的数量D:" + ids.size());
            DataApi.i().fetchContentsUnreadStarred(ids);
            sendSyncSuccess();
            moveArticles();
            clearArticles(WithSet.i().getClearBeforeDay());
        } catch (HttpException e) {
            e.printStackTrace();
//            ToastUtil.showShort("sync时出了异常HttpException");
            if (e.getMessage().equals("401")) {
                sendSyncNeedAuth();
            }
            sendSyncFailure();
        } catch (IOException e) {
            e.printStackTrace();
//            ToastUtil.showShort("sync时出了异常IO");
            sendSyncFailure();
        }

    }

    private void sendSyncNeedAuth() {
        ToastUtil.showShort("请重新登录");
        Intent loginIntent = new Intent(MainService.this, LoginActivity.class);
        startActivity(loginIntent);
    }


    /**
     * 异步类: AsyncTask 和 Handler 对比
     * 1 ） AsyncTask实现的原理,和适用的优缺点
     * AsyncTask,是android提供的轻量级的异步类,可以直接继承AsyncTask,在类中实现异步操作,并提供接口反馈当前异步执行的程度(可以通过接口实现UI进度更新),最后反馈执行的结果给UI主线程.
     * 使用的优点: 1、简单,快捷；2、过程可控
     * 使用的缺点：在使用多个异步操作和并需要进行Ui变更时,就变得复杂起来.
     * 2 ）Handler异步实现的原理和适用的优缺点
     * 在Handler 异步实现时,涉及到 Handler, Looper, Message,Thread 四个对象，实现异步的流程是主线程启动 Thread（子线程）运行并生成Message-Looper获取Message并传递给HandleràHandler逐个获取Looper中的Message，并进行UI变更。
     * 使用的优点：1、结构清晰，功能定义明确；2、对于多个后台任务时，简单，清晰
     * 使用的缺点：在单个后台异步处理时，显得代码过多，结构过于复杂（相对性）
     * ----------
     * 采用线程 + Handler实现异步处理时，当每次执行耗时操作都创建一条新线程进行处理，性能开销会比较大。另外，如果耗时操作执行的时间比较长，就有可能同时运行着许多线程，系统将不堪重负。
     * 为了提高性能，我们可以使用AsynTask实现异步处理，事实上其内部也是采用线程 + Handler来实现异步处理的，只不过是其内部使用了线程池技术，有效的降低了线程创建数量及限定了同时运行的线程数。
     * ----------
     * 如果你不带参数的实例化：Handler handler = new Handler();那么这个会默认用当前线程的looper
     * 要刷新UI，handler要用到主线程的looper。那么在主线程 Handler handler = new Handler();，如果在其他线程，也要满足这个功能的话，要Handler handler = new Handler(Looper.getMainLooper());不用刷新ui,只是处理消息。 当前线程如果是主线程的话，Handler handler = new Handler();不是主线程的话，Looper.prepare(); Handler handler = new Handler();Looper.loop();或者Handler handler = new Handler(Looper.getMainLooper());
     * 若是实例化的时候用Looper.getMainLooper()就表示放到主UI线程去处理。
     * 如果不是的话，因为只有UI线程默认Loop.prepare();Loop.loop();过，其他线程需要手动调用这两个，否则会报错。
     */
//    private class MSCallback implements Handler.Callback {
//        @Override
//        public boolean handleMessage(Message msg) {
//            String info = msg.getData().getString("res");
//            String url = msg.getData().getString("url");
//            KLog.d("开始处理 = url：" + url);
//            KLog.d("开始处理 = 结果：" + msg.what);
//
//            if (info == null) {
//                info = "";
//            }
//            switch (msg.what) {
////                case Api.S_BITMAP:
////                    int imgNo;
////                    String articleID;
////                    articleID = msg.getData().getString("articleID");
////                    imgNo = msg.getData().getInt("imgNo");
////
////                    Img imgMeta = WithDB.i().getImg(articleID, imgNo);
////                    if (imgMeta == null) {
////                        KLog.e("【】【】【】【】【】【】【】" + "未找到图片");
////                        break;
////                    }
////                    imgMeta.setDownState(Api.ImgMeta_Downover);
////                    WithDB.i().saveImg(imgMeta);
////
////                    if (App.currentArticleID != null & App.currentArticleID.equals(articleID)) {
////                        Bundle bundle = msg.getData();
////                        bundle.putString("imgName", imgMeta.getName());
////                        Message message = Message.obtain();
////                        message.what = Api.S_BITMAP;
////                        message.setData(bundle);
////                        App.artHandler.sendMessage(message);
////                    }
////                    break;
////                case Api.F_BITMAP:
////                    articleID = msg.getData().getString("articleID");
//////                    imgNo = msg.getData().getInt("imgNo");
//////                    imgMeta = WithDB.i().getImg(articleID, imgNo);
////                    if (App.currentArticleID != null & App.currentArticleID.equals(articleID)) {
////                        Bundle bundle = msg.getData();
////                        Message message = Message.obtain();
////                        message.what = Api.F_BITMAP;
////                        message.setData(bundle);
////                        App.artHandler.sendMessage(message);
////                    }
////                    break;
//                    // Note:  图片下载失败的重试逻辑应该放到 neter 中。此处只管结果“下载失败”了，通知具体的 activity 去更换占位图。
//                    // TODO: 2017/6/6
//            }
//            return false;
//        }
//    }
    public void clearArticles(int days) {
        long clearTime = System.currentTimeMillis() - days * 24 * 3600 * 1000L - 300 * 1000L;// 后者是留5分钟的预留时间
        List<Article> allArtsBeforeTime = WithDB.i().getArtInReadedUnstarLtTime(clearTime);
        KLog.i("清除A：" + clearTime + "--" + allArtsBeforeTime.size() + "--" + days);
        if (allArtsBeforeTime.size() == 0) {
            return;
        }
        ArrayList<String> idListMD5 = new ArrayList<>(allArtsBeforeTime.size());
        for (Article article : allArtsBeforeTime) {
            idListMD5.add(StringUtil.stringToMD5(article.getId()));
        }
        KLog.i("清除B：" + clearTime + "--" + allArtsBeforeTime.size() + "--" + days);
        FileUtil.deleteHtmlDirList(idListMD5);
        WithDB.i().delArt(allArtsBeforeTime);
        WithDB.i().delArticleImgs(allArtsBeforeTime);
        System.gc();
    }

    /**
     * 移动“保存且已读”的文章至一个新的文件夹
     * test 这里可能有问题，因为在我手机中有出现，DB中文章已经不存在，但文件与文件夹还存在与Box文件夹内的情况。
     */
    public void moveArticles() {
        List<Article> boxReadArts = WithDB.i().getArtInReadedBox();
        List<Article> storeReadArts = WithDB.i().getArtInReadedStore();
        KLog.i("移动文章" + boxReadArts.size() + "=" + storeReadArts.size());

        for (Article article : boxReadArts) {
            FileUtil.moveFile(App.boxRelativePath + article.getTitle() + ".html", App.boxReadRelativePath + article.getTitle() + ".html");// 移动文件
            FileUtil.moveDir(App.boxRelativePath + article.getTitle() + "_files", App.boxReadRelativePath + article.getTitle() + "_files");// 移动目录
            article.setCoverSrc(FileUtil.getAbsoluteDir(Api.SAVE_DIR_BOXREAD) + article.getTitle() + "_files" + File.separator + StringUtil.getFileNameExtByUrl(article.getCoverSrc()));
            article.setSaveDir(Api.SAVE_DIR_BOXREAD);
            KLog.i("移动了A");
        }
        WithDB.i().saveArticleList(boxReadArts);
        for (Article article : storeReadArts) {
            FileUtil.moveFile(App.storeRelativePath + article.getTitle() + ".html", App.storeReadRelativePath + article.getTitle() + ".html");// 移动文件
            FileUtil.moveDir(App.storeRelativePath + article.getTitle() + "_files", App.storeReadRelativePath + article.getTitle() + "_files");// 移动目录
            article.setCoverSrc(FileUtil.getAbsoluteDir(Api.SAVE_DIR_STOREREAD) + article.getTitle() + "_files" + File.separator + StringUtil.getFileNameExtByUrl(article.getCoverSrc()));
            article.setSaveDir(Api.SAVE_DIR_STOREREAD);
            KLog.i("移动了B" + App.storeRelativePath + article.getTitle() + "_files |||| " + App.storeReadRelativePath + article.getTitle() + "_files");
        }
        WithDB.i().saveArticleList(storeReadArts);
    }


    private void sendSyncStart() {
        maHandler.sendEmptyMessage(Api.SYNC_START);
        alreadySyncedArtsNum = 0;
        App.i().isSyncing = true;
//        App.canSync = true;
    }


    private void sendSyncSuccess() {
        maHandler.sendEmptyMessage(Api.SYNC_SUCCESS);
        alreadySyncedArtsNum = 0;
        App.i().isSyncing = false;
        App.i().lastSyncTime = System.currentTimeMillis();
    }

    private void sendSyncFailure() {
        maHandler.sendEmptyMessage(Api.SYNC_FAILURE);
        alreadySyncedArtsNum = 0;
        App.i().isSyncing = false;
        App.i().lastSyncTime = System.currentTimeMillis();
    }

    private void sendSyncProcess(String tips) {
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putString("tips", tips);
        message.what = Api.SYNC_PROCESS;
        message.setData(bundle);
        maHandler.sendMessage(message);
    }


//    private ArrayMap<Long, RequestLog> requestMap;
//    private Neter.Record<RequestLog> recorder = new Neter.Record<RequestLog>() {
//        @Override
//        public void log(RequestLog entry) {
//            KLog.d(" 记录 log");
//            if (entry.getHeadParamString() != null && !entry.getHeadParamString().contains("c=")) {
//                requestMap.put(entry.getLogTime(), entry);
//            }
//        }
//    };
//
//    public void delRequestLog(long key) {
//        if (requestMap != null) {
//            if (requestMap.size() != 0) {
//                requestMap.remove(key); // 因为最后一次使用 handleMessage(100) 时也会调用
//            }
//        }
//    }
//
//    private void saveRequestLog(long logTime) {
//        KLog.i("【saveRequest1】");
//        if (requestMap == null) {
//            return;
//        }
//        KLog.i("【saveRequest2】" + requestMap.get(logTime));
//        WithDB.i().saveRequestLog(requestMap.get(logTime));
//    }

//    private boolean syncRequestLog() {
//        List<RequestLog> requestLogs = WithDB.i().loadRequestListAll();
//        if (requestLogs.size() == 0) {
//            return false;
//        }
//        sendSyncProcess(getString(R.string.main_toolbar_hint_sync_log));
//
//        if (requestMap.size() != requestLogs.size()) {
//            for (RequestLog requestLog : requestLogs) {
//                requestMap.put(requestLog.getLogTime(), requestLog);
//            }
//        }
//        // TODO: 2016/5/26 将这个改为 json 格式来持久化 RequestLog 对象 ？貌似也不好
//        for (RequestLog requestLog : requestLogs) {
//            requestMap.put(requestLog.getLogTime(), requestLog);
//            String headParamString = requestLog.getHeadParamString();
//            String bodyParamString = requestLog.getBodyParamString();
//            mNeter.addHeader(StringUtil.formStringToParamList(headParamString));
//            mNeter.addBody(StringUtil.formStringToParamList(bodyParamString));
//            KLog.d("同步错误：" + headParamString + " = " + bodyParamString);
//            if (requestLog.getMethod().equals("post")) {
//                mNeter.asyncPostWithAuth(requestLog.getUrl()); // , requestLog.getLogTime()
//            }
//        }
//        WithDB.i().delRequestListAll();  // TODO: 2016/10/20 不能先删除，可能删除后，手机退出，那么这些记录就丢失了
////        hadSyncLogRequest = true;
//        KLog.d("读取到的数目： " + requestLogs.size());
//        return true;
//    }

}
