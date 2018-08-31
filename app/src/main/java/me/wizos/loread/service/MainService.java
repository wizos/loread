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

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.activity.LoginActivity;
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
import me.wizos.loread.utils.NetworkUtil;
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
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveesult(Sync sync) {
        int result = sync.result;
//        KLog.e("接收到的数据为：" + result);
        switch (result) {
            case Sync.STOP:
                stopSelf();
                break;
            default:
                break;
        }
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        if (App.i().isSyncing || !NetworkUtil.isNetworkAvailable()) {
            return;
        }

        String action = intent.getAction();
        KLog.e("获取到新的任务：" + action);
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

//        else if(Api.MARK_READED.equals(action)){
//            markReaded( intent.getIntExtra("articleNo",-1) );
//        }else if(Api.MARK_UNREAD.equals(action)){
//            markUnreading( intent.getIntExtra("articleNo",-1) );
//        }else if(Api.MARK_STARED.equals(action)){
//            markStared( intent.getIntExtra("articleNo",-1) );
//        }else if(Api.MARK_UNSTAR.equals(action)){
//            markUnstar( intent.getIntExtra("articleNo",-1) );
//        }else if(Api.MARK_SAVED.equals(action)){
//            markSaved( intent.getIntExtra("articleNo",-1) );
//        }else if(Api.MARK_UNSAVE.equals(action)){
//            markUnsave( intent.getIntExtra("articleNo",-1) );
//        }
//
//        KLog.e("文章编号：" + intent.getIntExtra("articleNo",-1) );
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
//            coverSavedTagFeed(tagList, feedList);

            WithDB.i().coverSaveTags(tagList);
            WithDB.i().coverSaveFeeds(feedList);
//            saveTagFeedWithUnreadCount(tagList, feedList);

            KLog.e("3 - 同步未读信息");
            EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.main_toolbar_hint_sync_unread_refs)));
            HashSet<String> unreadRefsList = DataApi.i().fetchUnreadRefs2();


            KLog.e("2 - 同步收藏信息");
            EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.main_toolbar_hint_sync_stared_refs)));
            HashSet<String> staredRefsList = DataApi.i().fetchStaredRefs2();


//            KLog.e("1 - 同步文章内容");
            ArrayList<HashSet<String>> refsList = DataApi.i().splitRefs2(unreadRefsList, staredRefsList);
            int readySyncArtsCapacity = refsList.get(0).size() + refsList.get(1).size() + refsList.get(2).size();
            List<String> ids;
            int alreadySyncedArtsNum = 0, hadFetchCount, needFetchCount, num;
            ArrayList<Article> tempArticleList;

            ids = new ArrayList<>(refsList.get(0));
            needFetchCount = ids.size();
            hadFetchCount = 0;
//            KLog.e("栈的数量A:" + ids.size());
            final long syncTimeMillis = System.currentTimeMillis();
            while (needFetchCount > 0) {
                num = Math.min(needFetchCount, InoApi.i().fetchContentCntForEach);
//                tempArticleList = DataApi.i().fetchContentsUnreadUnstar2(ids.subList(hadFetchCount, hadFetchCount = hadFetchCount + num));
                tempArticleList = DataApi.i().parseItemContents(InoApi.i().syncItemContents(ids.subList(hadFetchCount, hadFetchCount = hadFetchCount + num)), new DataApi.ArticleChanger() {
                    @Override
                    public Article change(Article article) {
                        article.setReadStatus(Api.UNREAD);
                        article.setStarStatus(Api.UNSTAR);
                        article.setUpdated(syncTimeMillis);
                        return article;
                    }
                });
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
                num = Math.min(needFetchCount, InoApi.i().fetchContentCntForEach);
//                tempArticleList = DataApi.i().fetchContentsReadStarred2(ids.subList(hadFetchCount, hadFetchCount = hadFetchCount + num));
                tempArticleList = DataApi.i().parseItemContents(InoApi.i().syncItemContents(ids.subList(hadFetchCount, hadFetchCount = hadFetchCount + num)), new DataApi.ArticleChanger() {
                    @Override
                    public Article change(Article article) {
                        article.setReadStatus(Api.READED);
                        article.setStarStatus(Api.STARED);
                        article.setUpdated(syncTimeMillis);
                        return article;
                    }
                });

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
                num = Math.min(needFetchCount, InoApi.i().fetchContentCntForEach);
//                tempArticleList = DataApi.i().fetchContentsUnreadStarred2(ids.subList(hadFetchCount, hadFetchCount = hadFetchCount + num));
                tempArticleList = DataApi.i().parseItemContents(InoApi.i().syncItemContents(ids.subList(hadFetchCount, hadFetchCount = hadFetchCount + num)), new DataApi.ArticleChanger() {
                    @Override
                    public Article change(Article article) {
                        article.setReadStatus(Api.UNREAD);
                        article.setStarStatus(Api.STARED);
                        article.setUpdated(syncTimeMillis);
                        return article;
                    }
                });
                WithDB.i().saveArticles(tempArticleList);
                alreadySyncedArtsNum = alreadySyncedArtsNum + num;
                EventBus.getDefault().post(new Sync(Sync.DOING, App.i().getString(R.string.main_toolbar_hint_sync_article_content, alreadySyncedArtsNum, readySyncArtsCapacity)));
                needFetchCount = ids.size() - hadFetchCount;
            }


            // 如果没有同步过所有加薪文章就同步
            if (!WithPref.i().isHadSyncAllStarred()) {
                ToastUtil.showLong("同步耗时较长，请耐心等待");
                DataApi.i().fetchAllStaredStreamContent(new DataApi.ArticleChanger() {
                    @Override
                    public Article change(Article article) {
                        article.setReadStatus(Api.READED);
                        article.setStarStatus(Api.STARED);
                        article.setUpdated(syncTimeMillis);
                        return article;
                    }
                });
                WithPref.i().setHadSyncAllStarred(true);
            }
            updateFeedUnreadCount();

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

    private void updateFeedUnreadCount() {
        ArrayList<Feed> feedList = WithDB.i().getUnreadArtsCountByFeed3();
        WithDB.i().coverSaveFeeds(feedList);
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
            FileUtil.saveArticle(App.boxRelativePath, article);
            article.setSaveDir(Api.SAVE_DIR_CACHE);
        }
        WithDB.i().saveArticles(boxReadArts);

        List<Article> storeReadArts = WithDB.i().getArtInReadedStore(time);
        KLog.i("移动文章" + storeReadArts.size());
        for (Article article : storeReadArts) {
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
        FileUtil.deleteHtmlDirList(idListMD5);
        WithDB.i().delArt(allArtsBeforeTime);
        System.gc();
    }

}
