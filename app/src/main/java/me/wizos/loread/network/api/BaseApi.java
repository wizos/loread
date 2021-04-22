package me.wizos.loread.network.api;

import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.ArrayMap;

import androidx.collection.ArraySet;

import com.elvishew.xlog.XLog;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.wizos.loread.App;
import me.wizos.loread.bean.FeedEntries;
import me.wizos.loread.bean.feedly.CategoryItem;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.config.SaveDirectory;
import me.wizos.loread.config.url_rewrite.UrlRewriteConfig;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.FeedCategory;
import me.wizos.loread.extractor.Distill;
import me.wizos.loread.extractor.ExtractPage;
import me.wizos.loread.network.HttpClientManager;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.ArticleUtils;
import me.wizos.loread.utils.BackupUtils;
import me.wizos.loread.utils.EncryptUtils;
import me.wizos.loread.utils.FileUtils;
import me.wizos.loread.utils.HttpCall;
import me.wizos.loread.utils.PagingUtils;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.utils.UriUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by Wizos on 2019/2/10.
 */

public abstract class BaseApi {
    int fetchContentCntForEach = 20; // 每次获取内容的数量

    // 同步所有数据，此处应该传入一个进度监听器，或者直接用EventBus发消息
    abstract public void sync();

    // NOTE: 2021/2/10 要禁止修改特殊分类（例如全部，未分类），但由于数据库中并未实际存在该分类，所以当下无影响
    abstract public void renameCategory(String categoryId, String targetName, CallbackX cb);

    abstract public void deleteCategory(String categoryId, CallbackX cb);

    abstract public void editFeedCategories(List<CategoryItem> lastCategoryItems, EditFeed editFeed, CallbackX cb);

    abstract public void addFeed(FeedEntries feedEntries, CallbackX cb);

    abstract public void deleteFeed(String feedId, CallbackX cb);

    abstract public void renameFeed(String feedId, String targetName, CallbackX cb);

    abstract public void markArticleReaded(String articleId, CallbackX cb);

    abstract public void markArticleUnread(String articleId, CallbackX cb);

    abstract public void markArticleStared(String articleId, CallbackX cb);

    abstract public void markArticleUnstar(String articleId, CallbackX cb);

    abstract public void markArticleListReaded(Collection<String> articleIds, CallbackX cb);

    abstract public void importOPML(Uri uri, CallbackX cb);

    void coverSaveCategories(List<Category> cloudyCategories) {
        if(cloudyCategories == null){
            return;
        }
        String uid = App.i().getUser().getId();
        ArrayMap<String, Category> cloudyCategoriesTmp = new ArrayMap<>(cloudyCategories.size());
        for (Category category : cloudyCategories) {
            category.setUid(uid);
            cloudyCategoriesTmp.put(category.getId(), category);
        }

        List<Category> localCategories = CoreDB.i().categoryDao().getAll(uid);
        Iterator<Category> localCategoriesIterator = localCategories.iterator();
        Category tmpCategory;
        while (localCategoriesIterator.hasNext()) {
            tmpCategory = localCategoriesIterator.next();
            if (cloudyCategoriesTmp.get(tmpCategory.getId()) == null) {
                CoreDB.i().categoryDao().delete(tmpCategory);
                localCategoriesIterator.remove();
            } else {
                cloudyCategoriesTmp.remove(tmpCategory.getId());
            }
        }

        cloudyCategories.clear();
        for (Map.Entry<String, Category> entry : cloudyCategoriesTmp.entrySet()) {
            cloudyCategories.add(entry.getValue());
        }

        CoreDB.i().categoryDao().update(localCategories);
        CoreDB.i().categoryDao().insert(cloudyCategories);
    }

    void coverSaveFeeds(List<Feed> cloudyFeeds) {
        if(cloudyFeeds == null){
            return;
        }
        String uid = App.i().getUser().getId();
        ArrayMap<String, Feed> cloudyMap = new ArrayMap<>(cloudyFeeds.size());
        for (Feed feed : cloudyFeeds) {
            feed.setUid(uid);
            cloudyMap.put(feed.getId(), feed);
        }

        List<Feed> localFeeds = CoreDB.i().feedDao().getAll(uid);
        List<Feed> deleteFeeds = new ArrayList<>();
        Iterator<Feed> localFeedsIterator = localFeeds.iterator();
        Feed localFeed, commonFeed;
        while (localFeedsIterator.hasNext()) {
            localFeed = localFeedsIterator.next();
            commonFeed = cloudyMap.get(localFeed.getId());
            if (commonFeed == null) {
                CoreDB.i().feedCategoryDao().deleteByFeedId(localFeed.getUid(), localFeed.getId());
                CoreDB.i().articleDao().deleteUnStarByFeedId(localFeed.getUid(), localFeed.getId());
                CoreDB.i().feedDao().delete(localFeed);
                deleteFeeds.add(localFeed);
                localFeedsIterator.remove();// 删除后，这里只剩2者的交集
                // XLog.e("删除本地的feed：" + localFeed.getId() + " , " + localFeed.getTitle() + " , " + localFeed.getUnreadCount() );
            } else {
                localFeed.setTitle(commonFeed.getTitle());
                localFeed.setFeedUrl(commonFeed.getFeedUrl());
                localFeed.setHtmlUrl(commonFeed.getHtmlUrl());
                cloudyMap.remove(localFeed.getId());
            }
        }
        cloudyFeeds.clear();
        for (Map.Entry<String, Feed> entry : cloudyMap.entrySet()) {
            cloudyFeeds.add(entry.getValue());
        }

        CoreDB.i().feedDao().update(localFeeds);
        CoreDB.i().feedDao().insert(cloudyFeeds);
        BackupUtils.exportUserUnsubscribeOPML(App.i().getUser(), deleteFeeds);
    }

    void coverFeedCategory(List<FeedCategory> cloudyFeedCategories) {
        if(cloudyFeedCategories == null){
            return;
        }
        ArrayMap<String, FeedCategory> cloudyCategoriesTmp = new ArrayMap<>(cloudyFeedCategories.size());
        for (FeedCategory feedCategory : cloudyFeedCategories) {
            cloudyCategoriesTmp.put(feedCategory.getFeedId() + feedCategory.getCategoryId(), feedCategory);
        }

        List<FeedCategory> localFeedCategories = CoreDB.i().feedCategoryDao().getAll(App.i().getUser().getId());
        FeedCategory tmp;

        for (FeedCategory feedCategory : localFeedCategories) {
            tmp = cloudyCategoriesTmp.get(feedCategory.getFeedId() + feedCategory.getCategoryId());
            if (tmp == null) {
                CoreDB.i().feedCategoryDao().delete(feedCategory);
            } else {
                cloudyFeedCategories.remove(tmp);
            }
        }
        CoreDB.i().feedCategoryDao().insert(cloudyFeedCategories);
    }


    ArraySet<String> handleUnreadRefs(List<String> idList) {
        if(idList == null || idList.size() == 0){
            return new ArraySet<>();
        }
        String uid = App.i().getUser().getId();

        // 第1步，遍历数据量大的一方A，将其比对项目放入Map中
        List<String> localUnReadIds = CoreDB.i().articleDao().getUnreadArticleIds(uid);
        List<String> localReadIds = CoreDB.i().articleDao().getReadArticleIds(uid);
        Set<String> ids = new HashSet<>(idList);

        // XLog.i("本地未读ids:" + localUnReadIds);
        // XLog.i("本地已读ids:" + localReadIds);
        // XLog.i("云端获取的ids:" + ids);

        ArrayList<String> needMarkReadIds = new ArrayList<>();
        ArrayList<String> needMarkUnReadIds = new ArrayList<>();
        ArraySet<String> needRequestIds = new ArraySet<>(ids.size());

        // 第2步，遍历数据量小的一方B。到Map中找，是否含有b中的比对项。有则XX，无则YY
        for (String articleId : ids) {
            if(localUnReadIds.contains(articleId)){
                localUnReadIds.remove(articleId);
                // XLog.i("本地未读包含：" + articleId);
            }else if(localReadIds.contains(articleId)){
                localReadIds.remove(articleId);
                needMarkUnReadIds.add(articleId);
                // XLog.i("本地已读包含：" + articleId);
            }else {
                needRequestIds.add(articleId);
                // XLog.i("需要获取：" + articleId);
            }
        }
        // XLog.i("须拉取未读ids:" + needRequestIds);

        // 取消标记未读
        for (String entry : localUnReadIds) {
            if (entry != null) {
                needMarkReadIds.add(entry);
            }
        }

        PagingUtils.slice(needMarkUnReadIds, 100, new PagingUtils.PagingListener<String>() {
            @Override
            public void onPage(@NotNull List<String> childList) {
                CoreDB.i().articleDao().markArticlesUnread(uid, childList);
                // XLog.i("处理当前页：" + childList);
            }
        });

        PagingUtils.slice(needMarkReadIds, 100, new PagingUtils.PagingListener<String>() {
            @Override
            public void onPage(@NotNull List<String> childList) {
                CoreDB.i().articleDao().markArticlesRead(uid, childList);
                // XLog.i("处理当前页：" + childList);
            }
        });
        XLog.d("处理未读资源：" + ids.size() + "，需要拉取数量：" + needRequestIds.size() + "，需要标记未读：" + needMarkUnReadIds.size() + "，需要标记已读：" + needMarkReadIds.size());
        return needRequestIds;
    }


    ArraySet<String> handleStaredRefs(List<String> idList) {
        if(idList == null || idList.size() == 0){
            return new ArraySet<>();
        }
        String uid = App.i().getUser().getId();

        // 第1步，遍历数据量大的一方A，将其比对项目放入Map中
        List<String> localStarIds = CoreDB.i().articleDao().getStaredArticleIds(uid);
        List<String> localUnStarIds = CoreDB.i().articleDao().getUnStarArticleIds(uid);
        Set<String> ids = new HashSet<>(idList);

        ArrayList<String> needMarkStarIds = new ArrayList<>();
        ArrayList<String> needMarkUnStarIds = new ArrayList<>();
        ArraySet<String> needRequestIds = new ArraySet<>(ids.size());

        // 第2步，遍历数据量小的一方B。到Map中找，是否含有b中的比对项。有则XX，无则YY
        for (String articleId : ids) {
            if(localStarIds.contains(articleId)){
                localStarIds.remove(articleId);
            }else if(localUnStarIds.contains(articleId)){
                localUnStarIds.remove(articleId);
                needMarkStarIds.add(articleId);
            }else {
                needRequestIds.add(articleId);
            }
        }

        // 取消加星
        for (String entry : localStarIds) {
            if (entry != null) {
                needMarkUnStarIds.add(entry);
            }
        }

        PagingUtils.slice(needMarkStarIds, 100, childList -> CoreDB.i().articleDao().markArticlesStar(uid, childList));
        PagingUtils.slice(needMarkUnStarIds, 100, childList -> CoreDB.i().articleDao().markArticlesUnStar(uid, childList));

        XLog.d("处理加星资源：" + ids.size() + "，需要拉取数量：" + needRequestIds.size() + "，需要标记加星：" + needMarkStarIds.size() + "，需要标记无星：" + needMarkUnStarIds.size());
        return needRequestIds;
    }

    /**
     * 处理重复的文章，将最新的重复文章与最老的放在一起
     */
    void handleDuplicateArticles(long timeMillis) {
        Article articleSample;
        List<String> links = CoreDB.i().articleDao().getDuplicateLink(App.i().getUser().getId(), timeMillis);
        List<Article> articleList;
        for (String link : links) {
            articleList = CoreDB.i().articleDao().getDuplicateArticles(App.i().getUser().getId(), link);
            if (articleList == null || articleList.size() == 0) {
                continue;
            }
            // XLog.e("获取到的重复文章数量：" + articleList.size());
            // 获取第一个作为范例
            articleSample = articleList.get(0);
            articleList.remove(0);

            List<Article> articles = new ArrayList<>();
            for (Article article : articleList) {
                article.setCrawlDate(articleSample.getCrawlDate());
                article.setPubDate(articleSample.getPubDate());
                articles.add(article);
            }
            // XLog.w("需要更新的文章数量A：" + articles);
            CoreDB.i().articleDao().update(articles);
        }
    }

    // 优化在使用状态下多次同步到新文章时，这些文章的爬取时间
    void handleArticleInfo() {
        String uid = App.i().getUser().getId();
        // 兜底策略：删掉所有未订阅、无星标的文章
        CoreDB.i().articleDao().deleteUnsubscribeUnStar(uid);
        // 兜底策略
        CoreDB.i().feedCategoryDao().deleteRedundantByCategoryId(uid);
        CoreDB.i().feedCategoryDao().deleteRedundantByFeedId(uid);

        CoreDB.i().articleDao().updateIdleCrawlDate(uid, App.i().getLastShowTimeMillis());
        CoreDB.i().articleDao().updateFeedUrl(uid);
        CoreDB.i().articleDao().updateFeedTitle(uid);
        CoreDB.i().feedDao().blockSync(uid);
        CoreDB.i().feedDao().updateLastPubDate(uid);
        updateCollectionCount(uid);
    }

    public static void updateCollectionCount(String uid) {
        long time = System.currentTimeMillis();
        CoreDB.i().feedDao().updateStarCount2(uid);// 耗时
        XLog.i("重置计数耗时A：" + (System.currentTimeMillis() - time));
        CoreDB.i().feedDao().updateUnreadCount2(uid); // 耗时
        XLog.i("重置计数耗时B：" + (System.currentTimeMillis() - time));
        CoreDB.i().feedDao().updateAllCount2(uid);
        XLog.i("重置计数耗时C：" + (System.currentTimeMillis() - time));

        CoreDB.i().categoryDao().updateAllCount(uid);
        XLog.i("重置计数耗时D：" + (System.currentTimeMillis() - time));
        CoreDB.i().categoryDao().updateUnreadCount(uid);
        XLog.i("重置计数耗时E：" + (System.currentTimeMillis() - time));
        CoreDB.i().categoryDao().updateStarCount(uid);
        XLog.i("重置计数耗时F：" + (System.currentTimeMillis() - time));

        // CoreDB.i().feedDao().update(CoreDB.i().feedDao().getFeedsRealTimeCount(uid));
        // CoreDB.i().categoryDao().update(CoreDB.i().categoryDao().getCategoriesRealTimeCount(uid));
    }
    public static void updateCollectionCount2(String uid) {
        long time = System.currentTimeMillis();
        CoreDB.i().feedDao().updateStarCount(uid);// 耗时
        XLog.i("重置计数耗时A：" + (System.currentTimeMillis() - time));
        CoreDB.i().feedDao().updateUnreadCount(uid); // 耗时
        XLog.i("重置计数耗时B：" + (System.currentTimeMillis() - time));
        CoreDB.i().feedDao().updateAllCount(uid);
        XLog.i("重置计数耗时C：" + (System.currentTimeMillis() - time));

        CoreDB.i().categoryDao().updateAllCount(uid);
        XLog.i("重置计数耗时D：" + (System.currentTimeMillis() - time));
        CoreDB.i().categoryDao().updateUnreadCount(uid);
        XLog.i("重置计数耗时E：" + (System.currentTimeMillis() - time));
        CoreDB.i().categoryDao().updateStarCount(uid);
        XLog.i("重置计数耗时F：" + (System.currentTimeMillis() - time));

        // CoreDB.i().feedDao().update(CoreDB.i().feedDao().getFeedsRealTimeCount(uid));
        // CoreDB.i().categoryDao().update(CoreDB.i().categoryDao().getCategoriesRealTimeCount(uid));
    }

    public void deleteExpiredArticles() {
        // 最后的 300 * 1000L 是留前5分钟时间的不删除
        long time = System.currentTimeMillis() - App.i().getUser().getCachePeriod() * 24 * 3600 * 1000L - 60 * 1000L;
        String uid = App.i().getUser().getId();

        List<Article> boxReadArts = CoreDB.i().articleDao().getReadedUnstarBeFiledLtTime(uid, time);
        //XLog.i("移动文章" + boxReadArts.size());
        for (Article article : boxReadArts) {
            article.setSaveStatus(App.STATUS_IS_FILED);
            String dir = SaveDirectory.i().getSaveDir(article.getFeedId(), article.getId()) + "/";
            //XLog.d("保存目录：" + dir);
            ArticleUtils.saveArticle(App.i().getUserBoxPath() + dir, article);
        }
        CoreDB.i().articleDao().update(boxReadArts);

        List<Article> storeReadArts = CoreDB.i().articleDao().getReadedStaredBeFiledLtTime(uid, time);
        //XLog.i("移动文章" + storeReadArts.size());
        for (Article article : storeReadArts) {
            article.setSaveStatus(App.STATUS_IS_FILED);
            String dir = "/" + SaveDirectory.i().getSaveDir(article.getFeedId(), article.getId()) + "/";
            ArticleUtils.saveArticle(App.i().getUserStorePath() + dir, article);
        }
        CoreDB.i().articleDao().update(storeReadArts);

        List<String> expiredArticles = CoreDB.i().articleDao().getReadedUnstarIdsLtTime(uid, time);
        ArrayList<String> idListMD5 = new ArrayList<>(expiredArticles.size());
        for (String articleId : expiredArticles) {
            idListMD5.add(EncryptUtils.MD5(articleId));
        }
        // XLog.d("清除：" + time + "--" + expiredArticles);
        FileUtils.deleteHtmlDirList(idListMD5);
        PagingUtils.slice(expiredArticles, 100, childList -> CoreDB.i().articleDao().delete(uid, childList));
    }

    public static void deleteUnsubscribedArticles(String feedId) {
        // 最后的 300 * 1000L 是留前5分钟时间的不删除
        String uid = App.i().getUser().getId();

        List<Article> boxReadArts = CoreDB.i().articleDao().getUnStarPreFiled(uid, feedId);
        //XLog.i("移动文章" + boxReadArts.size());
        for (Article article : boxReadArts) {
            article.setSaveStatus(App.STATUS_IS_FILED);
            String dir = SaveDirectory.i().getSaveDir(article.getFeedId(), article.getId()) + "/";
            //XLog.d("保存目录：" + dir);
            ArticleUtils.saveArticle(App.i().getUserBoxPath() + dir, article);
        }
        CoreDB.i().articleDao().update(boxReadArts);

        List<Article> storeReadArts = CoreDB.i().articleDao().getStaredPreFiled(uid, feedId);
        //XLog.i("移动文章" + storeReadArts.size());
        for (Article article : storeReadArts) {
            article.setSaveStatus(App.STATUS_IS_FILED);
            String dir = "/" + SaveDirectory.i().getSaveDir(article.getFeedId(), article.getId()) + "/";
            ArticleUtils.saveArticle(App.i().getUserStorePath() + dir, article);
        }
        CoreDB.i().articleDao().update(storeReadArts);

        List<String> needDeleteArticles = CoreDB.i().articleDao().getUnStarIdsByFeedId(uid, feedId);
        ArrayList<String> idListMD5 = new ArrayList<>(needDeleteArticles.size());
        for (String articleId : needDeleteArticles) {
            idListMD5.add(EncryptUtils.MD5(articleId));
        }
        // XLog.d("清除：" + time + "--" + expiredArticles);
        FileUtils.deleteHtmlDirList(idListMD5);
        PagingUtils.slice(needDeleteArticles, 100, childList -> CoreDB.i().articleDao().delete(uid, childList));
    }

    public static void fetchReadability(String uid, long syncTimeMillis) {
        List<Article> articles = CoreDB.i().articleDao().getNeedReadability(uid, syncTimeMillis);
        XLog.i("开始获取易读文章 " + uid + " , " + syncTimeMillis);
        for (Article article : articles) {
            XLog.i("需要易读的文章：" + " , " + article.getTitle() + " , " + article.getLink());
            if (TextUtils.isEmpty(article.getLink())) {
                continue;
            }
            String keyword;
            if (App.i().articleFirstKeyword.containsKey(article.getId())) {
                keyword = App.i().articleFirstKeyword.get(article.getId());
            } else {
                keyword = ArticleUtils.getKeyword(article.getContent());
                App.i().articleFirstKeyword.put(article.getId(), keyword);
            }

            String url = UrlRewriteConfig.i().getRedirectUrl(article.getLink());
            if(StringUtils.isEmpty(url)){
                url = article.getLink();
            }

            String finalUrl = url;
            Distill distill = new Distill(url, article.getLink(), keyword, new Distill.Listener() {
                @Override
                public void onResponse(ExtractPage page) {
                    article.updateContent(finalUrl, page.getContent());
                    CoreDB.i().articleDao().update(article);
                }

                @Override
                public void onFailure(String msg) {
                    XLog.e("获取失败 - " + msg);
                }
            });
            distill.getContent();
        }
    }

    void fetchIcon(String uid) {
        List<Feed> feeds = CoreDB.i().feedDao().getFeedsByNoIconUrl(uid);
        for(Feed feed: feeds){
            // XLog.i("需要抓取 Icon 的 Feed：" + feed.getTitle() + " => " + feed.getFeedUrl());
            if(!UriUtils.isHttpOrHttpsUrl(feed.getHtmlUrl())){
                continue;
            }
            Request request = new Request.Builder().url(feed.getHtmlUrl()).build();
            Call call = HttpClientManager.i().simpleClient().newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if(!response.isSuccessful()){
                        response.close();
                        return;
                    }
                    ResponseBody responseBody = response.body();
                    if(responseBody == null){
                        response.close();
                        return;
                    }
                    Document document = Jsoup.parse(responseBody.string(), feed.getHtmlUrl());
                    Element iconEle = document.selectFirst("[rel='shortcut icon']"); // [rel='shortcut icon'], [rel*=icon]
                    if(iconEle == null){
                        iconEle = document.selectFirst("[rel='icon']");
                        if(iconEle == null){
                            iconEle = document.selectFirst("[rel*=icon]");
                        }
                    }

                    if(iconEle !=null){
                        feed.setIconUrl(iconEle.attr("abs:href"));
                        CoreDB.i().feedDao().update(feed);
                    }else {
                        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                            @Override
                            public void run() {
                                if(HttpCall.i().valid(UriUtils.getFaviconUrl(feed.getHtmlUrl()))){
                                    feed.setIconUrl(UriUtils.getFaviconUrl(feed.getHtmlUrl()));
                                    CoreDB.i().feedDao().update(feed);
                                }
                            }
                        });
                    }
                    // CoreDB.i().feedDao().update(feed);
                    response.close();
                }
            });
        }
    }
}
