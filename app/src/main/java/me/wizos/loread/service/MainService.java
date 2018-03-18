package me.wizos.loread.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.lzy.okgo.exception.HttpException;
import com.socks.library.KLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.activity.LoginActivity;
import me.wizos.loread.bean.gson.son.ItemRefs;
import me.wizos.loread.data.PrefUtils;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.Tag;
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
 * @author Wizos on 2017/1/7.
 */

public class MainService extends IntentService {
    private final static String TAG = "MainService";

    public MainService() {
        super("MainService");
    }

    // MainActivity 的 Handler
    private Handler maHandler;
    // 已同步了文章内容的数量
    private int alreadySyncedArtsNum = 0;
    private int readySyncArtsCapacity = 0;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    // Activity 与 Service 通信方式 总结：http://www.jianshu.com/p/cd69f208f395
    // 使用 startService(intent); 多次启动IntentService，但IntentService的实例只有一个，这跟传统的Service是一样的.
    // 最终IntentService会去调用onHandleIntent执行异步任务。这里可能我们还会担心for循环去启动任务，而实例又只有一个，那么任务会不会被覆盖掉呢？其实是不会的，因为IntentService真正执行异步任务的是HandlerThread+Handler

   /* 第一种模式通信：Binder */
    public class ServiceBinder extends Binder {
       public MainService getService() {
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

    public void regHandler(Handler mHandler) {
        this.maHandler = mHandler;
    }

    /**
     * TODO: 2018/2/15  可以采用类似 net.fred.feedex.REFRESH 的方式作为字符串
     */
    public final static String SYNC_ALL = "syncAll";
    public final static String SYNC_ALL_STARRED = "syncAllStarred";
    private final static String SYNC_ALL_UNREAD = "unreadArticle";

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        // 这个是在 SettingActivity 中发起的
        if (SYNC_ALL.equals(action)) {
            syncAll();
        } else if (SYNC_ALL_STARRED.equals(action)) {
            syncAllStarred();
        }
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
            PrefUtils.i().setHadSyncAllStarred(true);
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
        Tool.showShort("syncAllStarred完成");
    }

    private void syncAll() {
        if (App.i().isSyncing) {
            ToastUtil.showShort("当前正在同步中，请稍候再操作");
            return;
        }
        // note: 像这种没有网络的应该改为抛异常
        if (!HttpUtil.isNetworkAvailable()) {
            ToastUtil.showShort(getString(R.string.tips_no_net));
            return;
        }

        sendSyncStart();
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
            if (PrefUtils.i().isOrderTagFeed()) {
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
            clearArticles(PrefUtils.i().getClearBeforeDay());
        } catch (HttpException e) {
            e.printStackTrace();
            if (e.getMessage().equals("401")) {
                sendSyncNeedAuth();
            }
            sendSyncFailure();
        } catch (IOException e) {
            e.printStackTrace();
            sendSyncFailure();
        }

    }

    private void sendSyncNeedAuth() {
        ToastUtil.showShort(getString(R.string.toast_login_for_auth));
        Intent loginIntent = new Intent(MainService.this, LoginActivity.class);
        startActivity(loginIntent);
    }


    public void clearArticles(int days) {
        // 最后的 300 * 1000L 是留前5分钟时间的不删除
        long clearTime = System.currentTimeMillis() - days * 24 * 3600 * 1000L - 300 * 1000L;
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
    }


    private void sendSyncSuccess() {
        maHandler.sendEmptyMessage(Api.SYNC_SUCCESS);
        alreadySyncedArtsNum = 0;
        App.i().isSyncing = false;
//        App.i().lastSyncTime = System.currentTimeMillis();
    }

    private void sendSyncFailure() {
        maHandler.sendEmptyMessage(Api.SYNC_FAILURE);
        alreadySyncedArtsNum = 0;
        App.i().isSyncing = false;
        PrefUtils.i().setSyncAllStarred(false);
//        App.i().lastSyncTime = System.currentTimeMillis();
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
