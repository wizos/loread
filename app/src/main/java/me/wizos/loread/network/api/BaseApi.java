package me.wizos.loread.network.api;

import android.text.TextUtils;
import android.util.ArrayMap;

import androidx.collection.ArraySet;

import com.elvishew.xlog.XLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.bean.feedly.CategoryItem;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.config.SaveDirectory;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.FeedCategory;
import me.wizos.loread.extractor.Distill;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.ArticleUtils;
import me.wizos.loread.utils.BackupUtils;
import me.wizos.loread.utils.EncryptUtils;
import me.wizos.loread.utils.FileUtils;

/**
 * Created by Wizos on 2019/2/10.
 */

public abstract class BaseApi {
    int fetchContentCntForEach = 20; // 每次获取内容的数量

    // 同步所有数据，此处应该传入一个进度监听器，或者直接用EventBus发消息
    abstract public void sync();

    abstract public void renameTag(String sourceTagId, String destTagId, CallbackX cb);

    abstract public void editFeedCategories(List<CategoryItem> lastCategoryItems, EditFeed editFeed, CallbackX cb);

    abstract public void addFeed(EditFeed editFeed, CallbackX cb);

    abstract public void unsubscribeFeed(String feedId, CallbackX cb);

    abstract public void renameFeed(String feedId, String renamedTitle, CallbackX cb);

    abstract public void markArticleReaded(String articleId, CallbackX cb);

    abstract public void markArticleUnread(String articleId, CallbackX cb);

    abstract public void markArticleStared(String articleId, CallbackX cb);

    abstract public void markArticleUnstar(String articleId, CallbackX cb);

    abstract public void markArticleListReaded(Collection<String> articleIds, CallbackX cb);

    public interface ArticleChanger {
        Article change(Article article);
    }

    void coverSaveCategories(List<Category> cloudyCategories) {
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
        BackupUtils.backupUnsubscribeFeed(App.i().getUser(), deleteFeeds);
    }

    void coverFeedCategory(List<FeedCategory> cloudyFeedCategories) {
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


    ArraySet<String> handleUnreadRefs(List<String> ids) {
        String uid = App.i().getUser().getId();

        // 第1步，遍历数据量大的一方A，将其比对项目放入Map中
        List<String> localUnReadIds = CoreDB.i().articleDao().getUnreadArticleIds(uid);
        List<String> localReadIds = CoreDB.i().articleDao().getReadArticleIds(uid);

        ArrayList<String> needMarkReadIds = new ArrayList<>();
        ArrayList<String> needMarkUnReadIds = new ArrayList<>();
        ArraySet<String> needRequestIds = new ArraySet<>(ids.size());

        // 第2步，遍历数据量小的一方B。到Map中找，是否含有b中的比对项。有则XX，无则YY
        for (String articleId : ids) {
            if(localUnReadIds.contains(articleId)){
                localUnReadIds.remove(articleId);
            }else if(localReadIds.contains(articleId)){
                localReadIds.remove(articleId);
                needMarkUnReadIds.add(articleId);
            }else {
                needRequestIds.add(articleId);
            }
        }

        // 取消标记未读
        for (String entry : localUnReadIds) {
            if (entry != null) {
                needMarkReadIds.add(entry);
            }
        }

        CoreDB.i().articleDao().markArticlesUnread(uid, needMarkUnReadIds);
        CoreDB.i().articleDao().markArticlesRead(uid, needMarkReadIds);
        XLog.d("处理未读资源：" + ids.size() + "，需要拉取数量：" + needRequestIds.size() + "，需要标记未读：" + needMarkUnReadIds.size() + "，需要标记已读：" + needMarkReadIds.size());
        return needRequestIds;
    }


    ArraySet<String> handleStaredRefs(List<String> ids) {
        String uid = App.i().getUser().getId();

        // 第1步，遍历数据量大的一方A，将其比对项目放入Map中
        List<String> localStarIds = CoreDB.i().articleDao().getStaredArticleIds(uid);
        List<String> localUnStarIds = CoreDB.i().articleDao().getUnStarArticleIds(uid);

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

        CoreDB.i().articleDao().markArticlesStar(uid, needMarkStarIds);
        CoreDB.i().articleDao().markArticlesUnStar(uid, needMarkUnStarIds);
        XLog.d("处理加星资源：" + ids.size() + "，需要拉取数量：" + needRequestIds.size() + "，需要标记加星：" + needMarkStarIds.size() + "，需要标记无星：" + needMarkUnStarIds.size());
        return needRequestIds;
    }

    /**
     * 处理重复的文章，将最新的重复文章与最老的放在一起
     */
    void handleDuplicateArticles() {
        Article articleSample;
        List<String> links = CoreDB.i().articleDao().getDuplicateLink(App.i().getUser().getId());
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
            CoreDB.i().articleDao().update(articles);
        }
    }

    // 优化在使用状态下多次同步到新文章时，这些文章的爬取时间
    void handleCrawlDate2() {
        String uid = App.i().getUser().getId();
        long lastReadMarkTimeMillis = CoreDB.i().articleDao().getLastReadTimeMillis(uid);
        long lastStarMaskTimeMillis = CoreDB.i().articleDao().getLastStarTimeMillis(uid);
        long lastMarkTimeMillis = Math.max(lastReadMarkTimeMillis, lastStarMaskTimeMillis);
        CoreDB.i().articleDao().updateIdleCrawlDate(uid, lastMarkTimeMillis, System.currentTimeMillis());
    }
    void handleCrawlDate() {
        String uid = App.i().getUser().getId();
        long time = System.currentTimeMillis();
        CoreDB.i().articleDao().updateLastSyncArticlesCrawlDate(uid, time);

        long lastReadMarkTimeMillis = CoreDB.i().articleDao().getLastReadTimeMillis(uid);
        long lastStarMaskTimeMillis = CoreDB.i().articleDao().getLastStarTimeMillis(uid);
        long lastMarkTimeMillis = Math.max(lastReadMarkTimeMillis, lastStarMaskTimeMillis);
        CoreDB.i().articleDao().updateIdleCrawlDate(uid, lastMarkTimeMillis, time);
    }
    void updateCollectionCount() {
        String uid = App.i().getUser().getId();
        CoreDB.i().feedDao().update(CoreDB.i().feedDao().getFeedsRealTimeCount(uid));
        CoreDB.i().categoryDao().update(CoreDB.i().categoryDao().getCategoriesRealTimeCount(uid));
    }


    public void deleteExpiredArticles() {
        // 最后的 300 * 1000L 是留前5分钟时间的不删除 WithPref.i().getClearBeforeDay()
        long time = System.currentTimeMillis() - App.i().getUser().getCachePeriod() * 24 * 3600 * 1000L - 60 * 1000L;
        String uid = App.i().getUser().getId();

        List<Article> boxReadArts = CoreDB.i().articleDao().getReadedUnstarBeFiledLtTime(uid, time);
        //XLog.i("移动文章" + boxReadArts.size());
        for (Article article : boxReadArts) {
            article.setSaveStatus(App.STATUS_IS_FILED);
            String dir = SaveDirectory.i().getSaveDir(article.getFeedId(), article.getId()) + "/";
            //XLog.e("保存目录：" + dir);
            //FileUtil.saveArticle(App.i().getUserBoxPath() + dir, article);
            ArticleUtils.saveArticle(App.i().getUserBoxPath() + dir, article);
        }
        CoreDB.i().articleDao().update(boxReadArts);

        List<Article> storeReadArts = CoreDB.i().articleDao().getReadedStaredBeFiledLtTime(uid, time);
        //XLog.i("移动文章" + storeReadArts.size());
        for (Article article : storeReadArts) {
            article.setSaveStatus(App.STATUS_IS_FILED);
            String dir = "/" + SaveDirectory.i().getSaveDir(article.getFeedId(), article.getId()) + "/";
            //XLog.e("保存目录：" + dir);
            //FileUtil.saveArticle(App.i().getUserStorePath() + dir, article);
            ArticleUtils.saveArticle(App.i().getUserStorePath() + dir, article);
        }
        CoreDB.i().articleDao().update(storeReadArts);

        List<Article> expiredArticles = CoreDB.i().articleDao().getReadedUnstarLtTime(uid, time);
        ArrayList<String> idListMD5 = new ArrayList<>(expiredArticles.size());
        for (Article article : expiredArticles) {
            idListMD5.add(EncryptUtils.MD5(article.getId()));
        }
        //XLog.i("清除A：" + time + "--" + expiredArticles.size());
        FileUtils.deleteHtmlDirList(idListMD5);
        CoreDB.i().articleDao().delete(expiredArticles);
    }

    void fetchReadability(String uid, long syncTimeMillis) {
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
            Distill distill = new Distill(article.getLink(), keyword, new Distill.Listener() {
                @Override
                public void onResponse(String content) {
                    article.updateContent(content);
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
}
