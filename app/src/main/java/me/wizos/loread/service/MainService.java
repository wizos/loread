package me.wizos.loread.service;

import android.app.IntentService;
import android.content.Intent;

import com.socks.library.KLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.activity.LoginActivity;
import me.wizos.loread.bean.gson.UnreadCounts;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithPref;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.Tag;
import me.wizos.loread.event.Login;
import me.wizos.loread.event.Sync;
import me.wizos.loread.net.Api;
import me.wizos.loread.net.DataApi;
import me.wizos.loread.net.InoApi;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.HttpUtil;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.utils.ToastUtil;

/**
 * FetcherService
 * 如果后台任务只有一个的话，onHandleIntent执行完，服务就会销毁，但如果后台任务有多个的话，onHandleIntent执行完最后一个任务时，服务才销毁。
 * 最后我们要知道每次执行一个后台任务就必须启动一次IntentService，而IntentService内部则是通过消息的方式发送给HandlerThread的，然后由Handler中的Looper来处理消息，而Looper是按顺序从消息队列中取任务的，也就是说IntentService的后台任务时顺序执行的，当有多个后台任务同时存在时，这些后台任务会按外部调用的顺序排队执行。
 *
 * 所有的请求都被一个单独的工作者线程处理--他们或许需要足够长的时间来处理（并且不会阻塞应用的主循环），但是同一时间只能处理一个请求。
 * @author Wizos on 2017/1/7.
 */

public class MainService extends IntentService {
//    public static final String SYNC_ALL = "me.wizos.loread.sync.all";
//    public final static String SYNC_STARRED = "me.wizos.loread.sync.starred";
//    public static final String TAG_sync_config = "me.wizos.loread.sync.config";
//    public static final String CLEAR = "me.wizos.loread.clear";

    public MainService() {
        super(Api.SYNC_ALL);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveesult(Sync sync) {
        int result = sync.result;
        KLog.e("接收到的数据为：" + result);
        switch (result) {
            case Sync.STOP:
                stopSelf();
                break;
            default:
                break;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        if (App.i().isSyncing || !HttpUtil.isNetworkAvailable()) {
            return;
        }

        String action = intent.getAction();
        if (Api.SYNC_ALL.equals(action) || Api.SYNC_HEARTBEAT.equals(action)) {
            syncAll();
        } else if (Api.CLEAR.equals(action)) {
            // 最后的 300 * 1000L 是留前5分钟时间的不删除 WithPref.i().getClearBeforeDay()
            long time = System.currentTimeMillis() - WithPref.i().getClearBeforeDay() * 24 * 3600 * 1000L - 300 * 1000L;
            handleSavedArticles(time);
            clearArticles(time);
        } else if (Api.LOGIN.equals(action)) {
            login(intent);
        }
    }


    private void login(Intent intent) {
        App.i().isSyncing = true;
        try {
            boolean loginResult = DataApi.i().clientLogin(intent.getStringExtra("accountID"), intent.getStringExtra("accountPW"));
            KLog.e("登录出错：", loginResult);
            if (!loginResult) {
                EventBus.getDefault().post(new Login(false, "Just do it"));
            }
            DataApi.i().fetchUserInfo();
            EventBus.getDefault().post(new Login(true));
            syncAll();
        } catch (Exception e) {
            EventBus.getDefault().post(new Login(false));
        }
        App.i().isSyncing = false;
    }


    private void syncAll() {
        App.i().isSyncing = true;
        EventBus.getDefault().post(new Sync(Sync.START));
        try {
            KLog.e("5 - 同步订阅源信息");
            EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.main_toolbar_hint_sync_tag)));
            List<Tag> tagList = DataApi.i().fetchTagList();
            List<Feed> feedList = DataApi.i().fetchFeedList();

            KLog.e("4 - 同步排序信息");
            EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.main_toolbar_hint_sync_tag_order)));
            if (WithPref.i().isOrderTagFeed()) {
                tagList = DataApi.i().fetchStreamPrefs(tagList);
            } else {
                tagList = DataApi.i().orderTags(tagList);
            }

            // 如果在获取到数据的时候就保存，那么到这里同步断了的话，可能系统内的文章就找不到响应的分组，所有放到这里保存。（比如在云端将文章移到的新的分组）
            coverSavedTagFeed(tagList, feedList);

//            sendSyncProcess(getString(R.string.main_toolbar_hint_sync_unread_count));
//            DataApi.i().fetchUnreadCounts();

            KLog.e("3 - 同步未读信息");
            EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.main_toolbar_hint_sync_unread_refs)));
            HashSet<String> unreadRefsList = DataApi.i().fetchUnreadRefs2();


            KLog.e("2 - 同步收藏信息");
            EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.main_toolbar_hint_sync_stared_refs)));
            HashSet<String> staredRefsList = DataApi.i().fetchStaredRefs2();


            KLog.e("1 - 同步文章内容");
            ArrayList<HashSet<String>> refsList = DataApi.i().splitRefs2(unreadRefsList, staredRefsList);
            int readySyncArtsCapacity = refsList.get(0).size() + refsList.get(1).size() + refsList.get(2).size();
            List<String> ids;
            int alreadySyncedArtsNum = 0, hadFetchCount, needFetchCount, num;
            ArrayList<Article> tempArticleList;

            ids = new ArrayList<>(refsList.get(0));
            needFetchCount = ids.size();
            hadFetchCount = 0;
            KLog.e("栈的数量A:" + ids.size());
            while (needFetchCount > 0) {
                num = Math.min(needFetchCount, InoApi.i().FETCH_CONTENT_EACH_CNT);
                tempArticleList = DataApi.i().fetchContentsUnreadUnstar2(ids.subList(hadFetchCount, hadFetchCount = hadFetchCount + num));
                WithDB.i().saveArticles(tempArticleList);
                alreadySyncedArtsNum = alreadySyncedArtsNum + num;
                EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.main_toolbar_hint_sync_article_content, alreadySyncedArtsNum, readySyncArtsCapacity)));
                needFetchCount = ids.size() - hadFetchCount;
            }


            ids = new ArrayList<>(refsList.get(1));
            needFetchCount = ids.size();
            hadFetchCount = 0;
//            KLog.e("栈的数量B:" + ids.size());
            while (needFetchCount > 0) {
                num = Math.min(needFetchCount, InoApi.i().FETCH_CONTENT_EACH_CNT);
                tempArticleList = DataApi.i().fetchContentsReadStarred2(ids.subList(hadFetchCount, hadFetchCount = hadFetchCount + num));
                WithDB.i().saveArticles(tempArticleList);
                alreadySyncedArtsNum = alreadySyncedArtsNum + num;
                EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.main_toolbar_hint_sync_article_content, alreadySyncedArtsNum, readySyncArtsCapacity)));
                needFetchCount = ids.size() - hadFetchCount;
            }


            ids = new ArrayList<>(refsList.get(2));
            needFetchCount = ids.size();
            hadFetchCount = 0;
//            KLog.e("栈的数量C:" + ids.size());
            while (needFetchCount > 0) {
                num = Math.min(needFetchCount, InoApi.i().FETCH_CONTENT_EACH_CNT);
                tempArticleList = DataApi.i().fetchContentsUnreadStarred2(ids.subList(hadFetchCount, hadFetchCount = hadFetchCount + num));
                WithDB.i().saveArticles(tempArticleList);
                alreadySyncedArtsNum = alreadySyncedArtsNum + num;
                EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.main_toolbar_hint_sync_article_content, alreadySyncedArtsNum, readySyncArtsCapacity)));
                needFetchCount = ids.size() - hadFetchCount;
            }


            // 如果没有同步过所有加薪文章就同步
            if (!WithPref.i().isHadSyncAllStarred()) {
                ToastUtil.showLong("同步耗时较长，请耐心等待");
                DataApi.i().fetchAllStaredStreamContent2();
                WithPref.i().setHadSyncAllStarred(true);
            }

            EventBus.getDefault().post(new Sync(Sync.END));
        } catch (IOException e) {
            e.printStackTrace();
            if (e.getMessage().equals("401")) {
                needAuth();
            }

            EventBus.getDefault().post(new Sync(Sync.ERROR));
        }
        App.i().isSyncing = false;
    }


    private void syncFast() {
        App.i().isSyncing = true;
        // 发送有两种方式，调用sendBroadcast或sendBroadcast方法。sendBroadcast方法不会立即处理广播，而是通过mHandler发送一个MSG_EXEC_PENDING_BROADCASTS的空消，然后在主线程异步处理。而sendBroadcast在调用时便处理广播，即同步处理。因此sendBroadcast不能在子线程中调用。

        try {
//            KLog.e("4 - 获取未读数目");
//            List<UnreadCounts> unreadCountList = DataApi.i().fetchUnreadCounts();
//            UnreadCounts unreadCounts = unreadCountList.get(0);
//            if(unreadCounts.getNewestItemTimestampUsec()== WithPref.i().getNewestItemTimestampUsec() ){
//                return;
//            }
//
//            // 更新本地的未读计数
//            Map<String, UnreadCounts> unreadCountMap = new ArrayMap<>(unreadCountList.size());
//            for (int i =0, size= unreadCountList.size(); i<size;i++){
//                unreadCountMap.put(unreadCountList.get(i).getId(),unreadCountList.get(i));
//            }
//
//            List<Tag> tagList = WithDB.i().getTags();
//            for (Tag tag:tagList) {
//                unreadCounts = unreadCountMap.get(tag.getId());
//                if( unreadCounts== null){
//                    continue;
//                }
//                if( tag.getNewestItemTimestampUsec()!= unreadCounts.getNewestItemTimestampUsec()){
//                    tag.setUnreadCount(unreadCounts.getCount());
//                    tag.setNewestItemTimestampUsec(unreadCounts.getNewestItemTimestampUsec());
//                }
//                // TODO: 2018/3/31 这里可以去检查一下这个tag是不是被删了？
//            }
//            WithDB.i().coverSaveTags(tagList);
//
//            // 不用吧所有的feed都拿出来，只需要把不在tag内的拿出来
//            List<Feed> feedList = WithDB.i().getFeeds();
//            for (Feed feed:feedList) {
//                unreadCounts = unreadCountMap.get(feed.getId());
//                if( unreadCounts== null){
//                    continue;
//                }
//                if( feed.getNewestItemTimestampUsec()!= unreadCounts.getNewestItemTimestampUsec()){
//                    feed.setUnreadCount(unreadCounts.getCount());
//                    feed.setNewestItemTimestampUsec(unreadCounts.getNewestItemTimestampUsec());
//                }
//                // TODO: 2018/3/31 这里可以去检查一下这个tag是不是被删了？
//            }
//            WithDB.i().saveFeeds(feedList);

            coverSavedTagFeed(WithDB.i().getTags(), WithDB.i().getFeeds());


            KLog.e("3 - 同步未读资源");
            HashSet<String> unreadRefsIDList = DataApi.i().fetchUnreadRefs2();

            List<String> ids = new ArrayList<>(unreadRefsIDList);
            int alreadySyncedArtsNum = 0, hadFetchCount = 0, needFetchCount = unreadRefsIDList.size(), num;
            ArrayList<Article> tempArticleList;

            KLog.e("栈的数量R:" + ids.size());
            while (needFetchCount > 0) {
                num = Math.min(needFetchCount, InoApi.i().FETCH_CONTENT_EACH_CNT);
                tempArticleList = DataApi.i().fetchContentsUnreadUnstar2(ids.subList(hadFetchCount, hadFetchCount = hadFetchCount + num));
                WithDB.i().saveArticles(tempArticleList);
                alreadySyncedArtsNum = alreadySyncedArtsNum + num;
                needFetchCount = ids.size() - hadFetchCount;
            }
            // 如果在获取到数据的时候就保存，那么到这里同步断了的话，可能系统内的文章就找不到响应的分组，所有放到这里保存。（比如在云端将文章移到的新的分组）

        } catch (IOException e) {
            e.printStackTrace();
            if (e.getMessage().equals("401")) {
                needAuth();
            }
        }
        App.i().isSyncing = false;
//        lbM.sendBroadcast( localIntent.putExtra(Api.NOTICE,Api.N_NEWS));
    }


    private void coverSavedTagFeed(List<Tag> tagList, List<Feed> feedList) {
        App.isSyncingUnreadCount = true;
        try {
            KLog.e("获取未读数目");
            List<UnreadCounts> unreadCountList = DataApi.i().fetchUnreadCounts();
            // 更新本地的未读计数
            for (int i = 0, size = unreadCountList.size(); i < size; i++) {
                App.unreadCountMap.put(unreadCountList.get(i).getId(), unreadCountList.get(i).getCount());
//                KLog.e("【】" +  unreadCountList.get(i).getId() + "  " +  unreadCountList.get(i).getCount() );
            }

            if (null != App.unreadOffsetMap) {
                for (Map.Entry<String, Integer> entry : App.unreadOffsetMap.entrySet()) {
                    if (entry.getKey() != null && App.unreadCountMap.containsKey(entry.getKey())) {
                        App.unreadCountMap.put(entry.getKey(), App.unreadCountMap.get(entry.getKey()) + entry.getValue());
                    }
                }
                App.unreadOffsetMap = null;
            }

        } catch (Exception e) {
            KLog.e("保存有错误");
            KLog.e(e);
            App.isSyncingUnreadCount = false;
        }

        WithDB.i().coverSaveTags(tagList);
        WithDB.i().coverSaveFeeds(feedList);
        App.i().initFeedsCategoryid();
        App.isSyncingUnreadCount = false;
    }

    private void needAuth() {
        ToastUtil.showShort(getString(R.string.toast_login_for_auth));
        Intent loginIntent = new Intent(MainService.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    /**
     * 移动“保存且已读”的文章至一个新的文件夹
     * test 这里可能有问题，因为在我手机中有出现，DB中文章已经不存在，但文件与文件夹还存在与Box文件夹内的情况。
     */
    private void handleSavedArticles(long time) {
        List<Article> boxReadArts = WithDB.i().getArtInReadedBox(time);
        KLog.i("移动文章" + boxReadArts.size());

        for (Article article : boxReadArts) {
            // 移动目录
//                    FileUtil.moveDir(App.cacheRelativePath + StringUtil.str2MD5(article.getId()) + "_files", App.boxRelativePath + article.getTitle() + "_files");
//                    FileUtil.saveBoxHtml( article.getTitle() , StringUtil.getModHtml( article ));
//                    FileUtil.saveArticle2Box(article);
            FileUtil.saveArticle(App.boxRelativePath, article);
            article.setSaveDir(Api.SAVE_DIR_CACHE);
        }
        WithDB.i().saveArticles(boxReadArts);

        List<Article> storeReadArts = WithDB.i().getArtInReadedStore(time);
        KLog.i("移动文章" + storeReadArts.size());
        for (Article article : storeReadArts) {
            // 移动目录
//                    FileUtil.moveDir(App.cacheRelativePath + StringUtil.str2MD5(article.getId()) + "_files", App.storeRelativePath + article.getTitle() + "_files");
//                    FileUtil.saveStoreHtml( article.getTitle() , StringUtil.getModHtml( article ));
//                    FileUtil.saveArticle2Store(article);
            FileUtil.saveArticle(App.storeRelativePath, article);
            article.setSaveDir(Api.SAVE_DIR_CACHE);
        }
        WithDB.i().saveArticles(storeReadArts);
    }


    public void clearArticles(long clearTime) {
        List<Article> allArtsBeforeTime = WithDB.i().getArtInReadedUnstarLtTime(clearTime);
        KLog.i("清除A：" + clearTime + "--" + allArtsBeforeTime.size());
        if (allArtsBeforeTime.size() == 0) {
            return;
        }
        ArrayList<String> idListMD5 = new ArrayList<>(allArtsBeforeTime.size());
        for (Article article : allArtsBeforeTime) {
            idListMD5.add(StringUtil.str2MD5(article.getId()));
        }
        KLog.i("清除B：" + clearTime + "--" + allArtsBeforeTime.size());
        FileUtil.deleteHtmlDirList2(idListMD5);
        WithDB.i().delArt(allArtsBeforeTime);
        System.gc();
    }


//    /**
//     * 移动“保存且已读”的文章至一个新的文件夹
//     * test 这里可能有问题，因为在我手机中有出现，DB中文章已经不存在，但文件与文件夹还存在与Box文件夹内的情况。
//     */
//    public void moveArticles() {
//        List<Article> boxReadArts = WithDB.i().getArtInReadedBox();
//        List<Article> storeReadArts = WithDB.i().getArtInReadedStore();
//        KLog.i("移动文章" + boxReadArts.size() + "=" + storeReadArts.size());
//
//        for (Article article : boxReadArts) {
//            FileUtil.moveFile(App.boxRelativePath + article.getTitle() + ".html", App.boxReadRelativePath + article.getTitle() + ".html");// 移动文件
//            FileUtil.moveDir(App.boxRelativePath + article.getTitle() + "_files", App.boxReadRelativePath + article.getTitle() + "_files");// 移动目录
//            article.setCoverSrc(FileUtil.getAbsoluteDir(Api.SAVE_DIR_BOXREAD) + article.getTitle() + "_files" + File.separator + StringUtil.getFileNameExtByUrl(article.getCoverSrc()));
//            article.setSaveDir(Api.SAVE_DIR_BOXREAD);
//            KLog.i("移动了A");
//        }
//        WithDB.i().saveArticles(boxReadArts);
//        for (Article article : storeReadArts) {
//            FileUtil.moveFile(App.storeRelativePath + article.getTitle() + ".html", App.storeReadRelativePath + article.getTitle() + ".html");// 移动文件
//            FileUtil.moveDir(App.storeRelativePath + article.getTitle() + "_files", App.storeReadRelativePath + article.getTitle() + "_files");// 移动目录
//            article.setCoverSrc(FileUtil.getAbsoluteDir(Api.SAVE_DIR_STOREREAD) + article.getTitle() + "_files" + File.separator + StringUtil.getFileNameExtByUrl(article.getCoverSrc()));
//            article.setSaveDir(Api.SAVE_DIR_STOREREAD);
//            KLog.i("移动了B" + App.storeRelativePath + article.getTitle() + "_files |||| " + App.storeReadRelativePath + article.getTitle() + "_files");
//        }
//        WithDB.i().saveArticles(storeReadArts);
//    }

}
