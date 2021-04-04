package me.wizos.loread.network.api;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.webkit.WebSettings;

import androidx.annotation.NonNull;

import com.elvishew.xlog.XLog;
import com.hjq.toast.ToastUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.rometools.opml.feed.opml.Opml;
import com.rometools.opml.feed.opml.Outline;
import com.rometools.rome.io.WireFeedInput;
import com.rometools.rome.io.XmlReader;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLException;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.bean.FeedEntries;
import me.wizos.loread.bean.feedly.CategoryItem;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.FeedCategory;
import me.wizos.loread.network.HttpClientManager;
import me.wizos.loread.network.SyncWorker;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.Converter;
import me.wizos.loread.utils.EncryptUtils;
import me.wizos.loread.utils.FeedParserUtils;
import me.wizos.loread.utils.PagingUtils;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.utils.TriggerRuleUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static me.wizos.loread.utils.StringUtils.getString;


/**
 * Created by Wizos on 2019/2/8.
 */

public class LocalApi extends BaseApi {
    public LocalApi() {
    }
    int syncedFeedCount = 0;
    int newArticleCount = 0;
    // CyclicBarrier barrier;
    private Handler handler;
    private final Object mLock = new Object();
    private final int TIMEOUT = 1;

    @Override
    public void sync() {
        // TODO: 2021/2/1
        /*
         * 1、取出本地所有 feeds（符合同步时间）；feed 表要增加【上次同步时间，同步时间间隔】，category 要增加同步时间间隔，以及 user 要增加同步时间间隔，上次同步时间
         *  > 同步时间间隔可选项为：禁用更新、每15分钟、每30分钟、每1小时、每4小时、每12小时、每天、每周
         *  > 该任务多久运行一次？根据当前给feed、category、global设置的同步间隔中，最短的时间来。或者直接设置为每15分钟检查一次。
         *  > 符合同步时间代表：每个feed的上次同步时间 + 其时间间隔（根据feed、category、global优先级来取其时间间隔） < 当前时间
         *
         *  > 如何兼容每次都同步异常的 feed ？还有必要坚持其同步时间间隔吗？
         *    - 没有必要，纯粹浪费资源，那要立即停掉吗？
         *    - 不是，肯定要再试几次？那如何确定要再试几次，直到停止呢？重试到不再重试的判断标准？
         *    - 以异常连续持续的天数为标准？
         *    - 以异常连续持续的次数为标准？
         *    - 如果是站点频次比较高、比较低呢？
         *    - 重试的时间间隔如何确定？
         *    - 以同步的时间间隔为标准：15*2=30，30*2=60，60*2=120，120*2=240，
         *  > 如何界定是异常的 feed？
         *    - 1、无法访问：没网、墙、超时、ssl问题
         *    - 2、无法解析数据：xml格式异常、json格式异常、不支持rss输出了
         *    - 3、被限制访问：反爬虫
         *  > 如何处理有异常的 feed？
         *    - 需要给用户强提醒，该订阅源有异常。
         *
         * 2、
         */

        long startSyncTimeMillis = App.i().getLastShowTimeMillis(); //  + 3600_000
        String uid = App.i().getUser().getId();

        if(CoreDB.i().feedDao().getCount(uid) == 0){
            ToastUtils.show(R.string.please_add_a_feed_first);
            return;
        }

        List<Feed> needSyncFeeds = CoreDB.i().feedDao().getFeedsNeedSync(uid, App.i().getUser().getAutoSyncFrequency(), startSyncTimeMillis);
        XLog.e("需要同步的订阅源数量为：" + needSyncFeeds.size() + "， 全局同步时间间隔：" + App.i().getUser().getAutoSyncFrequency() + " , 当前时间：" +  startSyncTimeMillis);
        if(needSyncFeeds.size() == 0){
            return;
        }
        LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.sync_feed, "1", needSyncFeeds.size()));
        handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                XLog.d("处理同步超时：" + msg.what);
                if(msg.what != TIMEOUT){
                    return false; //返回true 不对msg进行进一步处理
                }

                synchronized(mLock) {
                    mLock.notify();
                }
                return true;
            }
        });

        for(Feed feed: needSyncFeeds){
            Request.Builder request = new Request.Builder().url(feed.getFeedUrl());
            request.header(Contract.USER_AGENT, WebSettings.getDefaultUserAgent(App.i()));

            Call call = HttpClientManager.i().simpleClient().newCall(request.build());
            // XLog.d("同步：" + feed.getTitle() + "（" + feed.getFeedUrl()+ "）" );
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    syncedFeedCount ++;
                    LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.sync_feed, syncedFeedCount, needSyncFeeds.size()));
                    XLog.d("同步异常：" + feed.getTitle() + "（" + feed.getFeedUrl()+ " ） => " + e.getClass() + ":" + e.getLocalizedMessage() + ", 线程：" + Thread.currentThread() + ", 已同步数量：" + syncedFeedCount);

                    if ( !(e instanceof ConnectException) && !(e instanceof SocketTimeoutException) && !(e instanceof SSLException)){
                        feed.setLastSyncError(e.getLocalizedMessage());
                        feed.setLastErrorCount(feed.getLastErrorCount() + 1);
                        feed.setLastSyncTime(System.currentTimeMillis());

                        // AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                        //     @Override
                        //     public void run() {
                        //         CoreDB.i().feedDao().update(feed);
                        //     }
                        // });
                    }

                    checkSyncEnd(feed, syncedFeedCount, needSyncFeeds.size());
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                        @Override
                        public void run() {
                            syncedFeedCount ++;
                            LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.sync_feed, syncedFeedCount, needSyncFeeds.size()));
                            XLog.d("同步成功：" + feed.getTitle() + " [" + feed.getFeedUrl()+ "] " + response.isSuccessful() + ", 已同步数量：" + syncedFeedCount);

                            if(response.isSuccessful()){
                                FeedEntries feedEntries = FeedParserUtils.parseResponseBody(App.i(), feed, response.body(), new Converter.ArticleConvertListener() {
                                    @Override
                                    public Article onEnd(Article article) {
                                        article.setCrawlDate(App.i().getLastShowTimeMillis());
                                        return article;
                                    }
                                });
                                if(feedEntries != null){
                                    if (feedEntries.isSuccess()) {
                                        // CoreDB.i().feedDao().update(feedEntries.getFeed());
                                        Map<String, Article> articleMap = feedEntries.getArticleMap();

                                        PagingUtils.slice(new ArrayList<>(articleMap.keySet()), 50, new PagingUtils.PagingListener<String>() {
                                            @Override
                                            public void onPage(@NotNull List<String> childList) {
                                                List<String> removeIds = CoreDB.i().articleDao().getIds(uid, childList);
                                                if(removeIds != null){
                                                    for (String id: removeIds){
                                                        articleMap.remove(id);
                                                        // XLog.e("重复文章：" + id);
                                                    }
                                                }
                                            }
                                        });
                                        ArrayList<Article> newArticles = new ArrayList<>(articleMap.values());
                                        for (Article article: newArticles){
                                            article.setCrawlDate(App.i().getLastShowTimeMillis());
                                        }
                                        // XLog.i("抓取了新文章：" + newArticles.size() + ", 已抓取：" + newArticleCount);
                                        // XLog.i("新文章：" + newArticles );
                                        newArticleCount = newArticleCount + newArticles.size();
                                        CoreDB.i().articleDao().insert(newArticles);
                                    }
                                }else {
                                    feed.setLastSyncTime(System.currentTimeMillis());
                                }
                            }else {
                                feed.setLastSyncError( StringUtils.isEmpty(response.message()) ? String.valueOf(response.code()) : (response.code() + ", " + response.message()) );
                                feed.setLastErrorCount(feed.getLastErrorCount() + 1);
                                feed.setLastSyncTime(System.currentTimeMillis());
                                // CoreDB.i().feedDao().update(feed);
                            }
                            response.close();
                            checkSyncEnd(feed, syncedFeedCount, needSyncFeeds.size());
                        }
                    });

                    // checkSyncEnd(syncedFeedCount, needSyncFeeds.size());
                }
            });
        }

        try {
            waitSyncTimeout(needSyncFeeds.size());
            synchronized (mLock) {
                mLock.wait(needSyncFeeds.size() * 20_000);
                XLog.d("最多等待时间：" + (needSyncFeeds.size() * 20_000));
            }
        }catch (Exception e){
            XLog.e("wait 异常：" + e.getLocalizedMessage());
            e.printStackTrace();
        }


        // AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
        //     @Override
        //     public void run() {
        //
        //         synchronized(mLock) {
        //             mLock.notify();
        //         }
        //     }
        // });
        // try {
        //     synchronized (mLock) {
        //         mLock.wait();
        //         XLog.d("最多等待时间 N 秒：");
        //     }
        // }catch (Exception e){
        //     XLog.e("wait 异常：" + e.getLocalizedMessage());
        //     e.printStackTrace();
        // }
        long time1 = System.currentTimeMillis();
        LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.clear_article));
        deleteExpiredArticles();
        long time2 = System.currentTimeMillis();
        XLog.i("清理文章耗时：" + (time2 - time1));

        // 获取文章全文
        LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.fetch_article_full_content));
        fetchReadability(uid, startSyncTimeMillis);
        time1 = System.currentTimeMillis();
        XLog.i("获取全文耗时：" + (time1 - time2));
        fetchIcon(uid);

        LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post(getString(R.string.execute_rules));
        // 执行文章自动处理脚本
        TriggerRuleUtils.exeAllRules(uid, startSyncTimeMillis);
        time2 = System.currentTimeMillis();
        XLog.i("执行规则耗时：" + (time2 - time1));

        handleDuplicateArticles(startSyncTimeMillis);
        time1 = System.currentTimeMillis();
        XLog.i("处理重复文章耗时：" + (time1 - time2));
        updateCollectionCount();
        time2 = System.currentTimeMillis();
        XLog.i("重置计数耗时A：" + (time2 - time1));
        handleArticleInfo();
        time1 = System.currentTimeMillis();
        XLog.i("重置文章爬取时间、赋值feedUrl、feedTitle耗时：" + (time1 - time2));
        XLog.e("重置爬取时间：" + startSyncTimeMillis + " = " + App.i().getLastShowTimeMillis());


        // 提示更新完成
        LiveEventBus.get(SyncWorker.NEW_ARTICLE_NUMBER).post(newArticleCount);
        LiveEventBus.get(SyncWorker.SYNC_PROCESS_FOR_SUBTITLE).post( null );
        newArticleCount = 0;
        syncedFeedCount = 0;
        waitSavedFeeds.clear();
    }


    private void waitSyncTimeout(int needSyncFeedCount){
        if (handler.hasMessages(TIMEOUT)) {
            handler.removeMessages(TIMEOUT);
        }
        handler.sendEmptyMessageDelayed(TIMEOUT, needSyncFeedCount * 20_000);
    }


    private void checkSyncEnd(int syncedFeedCount, int feedCount){
        // XLog.d("同步次数：" + syncedFeedCount + ", 总数：" + feedCount);
        if(syncedFeedCount != feedCount){
            waitSyncTimeout(feedCount - syncedFeedCount);
            return;
        }
        handler.removeCallbacksAndMessages(null);
        synchronized(mLock) {
            mLock.notify();
        }
    }

    private List<Feed> waitSavedFeeds = new ArrayList<>();
    private void checkSyncEnd(Feed feed, int syncedFeedCount, int feedCount){
        // XLog.d("同步次数：" + syncedFeedCount + ", 总数：" + feedCount);
        waitSavedFeeds.add(feed);
        if(waitSavedFeeds.size() == 50){
            CoreDB.i().feedDao().update(waitSavedFeeds);
            waitSavedFeeds.clear();
        }
        if(syncedFeedCount != feedCount){
            waitSyncTimeout(feedCount - syncedFeedCount);
            return;
        }
        if(waitSavedFeeds.size() != 0){
            CoreDB.i().feedDao().update(waitSavedFeeds);
            waitSavedFeeds.clear();
        }
        handler.removeCallbacksAndMessages(null);
        synchronized(mLock) {
            mLock.notify();
        }
    }

    @Override
    public void renameCategory(String categoryId, String targetName, CallbackX cb) {
        final String newCategoryId = EncryptUtils.MD5(targetName);
        CoreDB.i().categoryDao().updateName(App.i().getUser().getId(), categoryId, targetName);
        CoreDB.i().categoryDao().updateId(App.i().getUser().getId(), categoryId, newCategoryId);
        CoreDB.i().feedCategoryDao().updateCategoryId(App.i().getUser().getId(), categoryId, newCategoryId);
        cb.onSuccess(App.i().getString(R.string.success));
    }

    @Override
    public void deleteCategory(String categoryId, CallbackX cb) {
        List<Feed> feeds = CoreDB.i().feedDao().getByCategoryId(App.i().getUser().getId(), categoryId);
        CoreDB.i().feedDao().delete(feeds);

        CoreDB.i().articleDao().deleteUnsubscribeUnStar(App.i().getUser().getId());
        // PagingUtils.slice(feeds, 50, new PagingUtils.PagingListener<Feed>() {
        //     @Override
        //     public void onPage(@NotNull List<Feed> childFeeds) {
        //         for (Feed feed: childFeeds){
        //             CoreDB.i().articleDao().deleteUnStarByFeedId(App.i().getUser().getId(), feed.getId());
        //         }
        //     }
        // });

        Category category = CoreDB.i().categoryDao().getById(App.i().getUser().getId(), categoryId);
        CoreDB.i().categoryDao().delete(category);

        List<FeedCategory> feedCategories = CoreDB.i().feedCategoryDao().getByCategoryId(App.i().getUser().getId(), categoryId);
        CoreDB.i().feedCategoryDao().delete(feedCategories);
        cb.onSuccess(App.i().getString(R.string.success));
    }

    public void unsubscribeFeed(String feedId, CallbackX cb) {
        cb.onSuccess(App.i().getString(R.string.success));
    }

    @Override
    public void addFeed(FeedEntries feedEntries, CallbackX cb) {
        Feed feed = feedEntries.getFeed();
        feed.setUid(App.i().getUser().getId());
        feed.setSyncInterval(0);
        // 从搜索界面传过来的时候，因为没有考虑到其他api，所有没有feedId，要在这里组装
        feed.setId(EncryptUtils.MD5(feed.getFeedUrl()));
        CoreDB.i().feedDao().insert(feed);

        List<Article> articles = feedEntries.getArticles();
        if( articles != null && articles.size() > 0){
            CoreDB.i().articleDao().insert(articles);
        }

        List<FeedCategory> feedCategories = feedEntries.getFeedCategories();
        if( feedCategories!= null && feedCategories.size() > 0){
            for(FeedCategory feedCategory: feedCategories){
                feedCategory.setFeedId(feedEntries.getFeed().getId());
            }
            CoreDB.i().feedCategoryDao().insert(feedCategories);
        }
        updateCollectionCount();
        cb.onSuccess(App.i().getString(R.string.subscribe_success));
    }

    @Override
    public void renameFeed(String feedId, String targetName, CallbackX cb) {
        CoreDB.i().feedDao().updateName(App.i().getUser().getId(), feedId, targetName);
        cb.onSuccess(App.i().getString(R.string.success));
    }


    @Override
    public void importOPML(Uri uri, CallbackX cb) {
        String uid = App.i().getUser().getId();
        try{
            ContentResolver contentResolver = App.i().getContentResolver();
            XLog.i("导入路径为B：" + uri);
            WireFeedInput input = new WireFeedInput();
            Opml opml = (Opml) input.build(new XmlReader(contentResolver.openInputStream(uri)));
            List<Outline> outlines = opml.getOutlines();
            // XLog.i("输出为B：" + outlines);
            parserOutlines(uid, null, outlines);
            cb.onSuccess(App.i().getString(R.string.success));
        }catch (Throwable throwable){
            XLog.i("异常：" + throwable.getLocalizedMessage());
            throwable.printStackTrace();
            cb.onFailure(App.i().getString(R.string.failure));
        }
    }

    private void parserOutlines(String uid, Category root, List<Outline> outlines){
        List<Category> categories = new ArrayList<>();
        List<Feed> feeds = new ArrayList<>();
        List<FeedCategory> feedCategories = new ArrayList<>();
        for (Outline outline: outlines){
            if(StringUtils.isEmpty(outline.getType())){
                Category category = new Category();
                category.setUid(uid);
                if(!StringUtils.isEmpty(outline.getTitle())){
                    category.setTitle(outline.getTitle());
                }else if(!StringUtils.isEmpty(outline.getText())){
                    category.setTitle(outline.getText());
                }else {
                    continue;
                }
                category.setId(EncryptUtils.MD5(category.getTitle()));
                categories.add(category);
                parserOutlines(uid, category, outline.getChildren());
            }else if(outline.getType().toLowerCase().startsWith("rss") || outline.getType().toLowerCase().startsWith("atom")){
                if(StringUtils.isEmpty(outline.getXmlUrl())){
                    continue;
                }
                Feed feed = new Feed();
                feed.setUid(uid);
                if(!StringUtils.isEmpty(outline.getTitle())){
                    feed.setTitle(outline.getTitle());
                }else if(!StringUtils.isEmpty(outline.getText())){
                    feed.setTitle(outline.getText());
                }else {
                    feed.setTitle("");
                }
                feed.setSyncInterval(0);
                feed.setHtmlUrl(outline.getHtmlUrl());
                feed.setFeedUrl(outline.getXmlUrl());
                feed.setId(EncryptUtils.MD5(feed.getFeedUrl()));
                feeds.add(feed);
                if(root == null){
                    continue;
                }
                FeedCategory feedCategory = new FeedCategory(uid, feed.getId(), root.getId());
                feedCategories.add(feedCategory);
            }
        }
        CoreDB.i().categoryDao().insert(categories);
        CoreDB.i().feedDao().insert(feeds);
        CoreDB.i().feedCategoryDao().insert(feedCategories);
        // XLog.i("可以插入分类：" + categories);
        // XLog.i("可以插入订阅源：" + feeds);
    }


    @Override
    public void editFeedCategories(List<CategoryItem> lastCategoryItems, EditFeed editFeed, CallbackX cb) {
        cb.onSuccess(App.i().getString(R.string.success));
    }


    private void markArticles(int field, int mode, String articleIds, CallbackX cb) {
        cb.onSuccess(null);
    }


    public void markArticleListReaded(Collection<String> articleIds, CallbackX cb) {
        markArticles(2, 0, StringUtils.join(",", articleIds), cb);
    }

    public void markArticleReaded(String articleId, CallbackX cb) {
        markArticles(2, 0, articleId, cb);
    }

    public void markArticleUnread(String articleId, CallbackX cb) {
        markArticles(2, 1, articleId, cb);
    }

    public void markArticleStared(String articleId, CallbackX cb) {
        markArticles(0, 1, articleId, cb);
    }

    public void markArticleUnstar(String articleId,CallbackX cb) {
        markArticles(0, 0, articleId, cb);
    }
}
