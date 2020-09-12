package me.wizos.loread.network.api;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.socks.library.KLog;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import me.wizos.loread.App;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.config.ArticleTags;
import me.wizos.loread.config.SaveDirectory;
import me.wizos.loread.config.Unsubscribe;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.ArticleTag;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.FeedCategory;
import me.wizos.loread.db.Tag;
import me.wizos.loread.network.HttpClientManager;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.ArticleUtil;
import me.wizos.loread.utils.EncryptUtil;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.StringUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;

/**
 * Created by Wizos on 2019/2/10.
 */

public abstract class BaseApi<T, E> {
    int fetchContentCntForEach = 20; // 每次获取内容的数量

    // 同步所有数据，此处应该传入一个进度监听器，或者直接用EventBus发消息
    abstract public void sync();

    abstract public void fetchUserInfo(CallbackX cb);

    abstract public void renameTag(String sourceTagId, String destTagId, CallbackX cb);

    abstract public void editFeedCategories(List<E> lastCategoryItems, EditFeed editFeed, CallbackX cb);

    abstract public void addFeed(EditFeed editFeed, CallbackX cb);

    abstract public void unsubscribeFeed(String feedId, CallbackX cb);

    abstract public void renameFeed(String feedId, String renamedTitle, CallbackX cb);

    abstract public void markArticleReaded(String articleId, CallbackX cb);

    abstract public void markArticleUnread(String articleId, CallbackX cb);

    abstract public void markArticleStared(String articleId, CallbackX cb);

    abstract public void markArticleUnstar(String articleId, CallbackX cb);

    abstract public void markArticleListReaded(List<String> articleIds, CallbackX cb);

    public interface ArticleChanger {
        Article change(Article article);
    }


    void deleteExpiredArticles() {
        // 最后的 300 * 1000L 是留前5分钟时间的不删除 WithPref.i().getClearBeforeDay()
        long time = System.currentTimeMillis() - App.i().getUser().getCachePeriod() * 24 * 3600 * 1000L - 300 * 1000L;
        String uid = App.i().getUser().getId();

        List<Article> boxReadArts = CoreDB.i().articleDao().getReadedUnstarBeFiledLtTime(uid, time);
        //KLog.i("移动文章" + boxReadArts.size());
        for (Article article : boxReadArts) {
            article.setSaveStatus(App.STATUS_IS_FILED);
            String dir = "/" + SaveDirectory.i().getSaveDir(article.getFeedId(),article.getId()) + "/";
            //KLog.e("保存目录：" + dir);
            FileUtil.saveArticle(App.i().getUserBoxPath() + dir, article);
        }
        CoreDB.i().articleDao().update(boxReadArts);

        List<Article> storeReadArts = CoreDB.i().articleDao().getReadedStaredBeFiledLtTime(uid, time);
        //KLog.i("移动文章" + storeReadArts.size());
        for (Article article : storeReadArts) {
            article.setSaveStatus(App.STATUS_IS_FILED);
            String dir = "/" + SaveDirectory.i().getSaveDir(article.getFeedId(),article.getId()) + "/";
            //KLog.e("保存目录：" + dir);
            FileUtil.saveArticle(App.i().getUserStorePath() + dir, article);
        }
        CoreDB.i().articleDao().update(storeReadArts);

        List<Article> expiredArticles = CoreDB.i().articleDao().getReadedUnstarLtTime(uid, time);
        ArrayList<String> idListMD5 = new ArrayList<>(expiredArticles.size());
        for (Article article : expiredArticles) {
            idListMD5.add(EncryptUtil.MD5(article.getId()));
        }
        //KLog.i("清除A：" + time + "--" + expiredArticles.size());
        FileUtil.deleteHtmlDirList(idListMD5);
        CoreDB.i().articleDao().delete(expiredArticles);
    }

    void fetchReadability(String uid, long syncTimeMillis){
        List<Article> articles = CoreDB.i().articleDao().getNeedReadability(uid,syncTimeMillis);
        for (Article article : articles) {
            //KLog.e("====获取：" + entry.getKey() + " , " + article.getTitle() + " , " + article.getLink());
            if (TextUtils.isEmpty(article.getLink())) {
                continue;
            }
            //KLog.e("====开始请求" );
            Request request = new Request.Builder().url(article.getLink()).build();
            Call call = HttpClientManager.i().simpleClient().newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, IOException e) {
                    KLog.e("获取失败");
                }

                // 在Android应用中直接使用上述代码进行异步请求，并且在回调方法中操作了UI，那么你的程序就会抛出异常，并且告诉你不能在非UI线程中操作UI。
                // 这是因为OkHttp对于异步的处理仅仅是开启了一个线程，并且在线程中处理响应。
                // OkHttp是一个面向于Java应用而不是特定平台(Android)的框架，那么它就无法在其中使用Android独有的Handler机制。
                @Override
                public void onResponse(@NotNull Call call, @NotNull okhttp3.Response response) throws IOException {
                    if (response.isSuccessful()) {
//                        Pattern pattern = Pattern.compile("</([a-zA-Z0-9]{1,10})>", Pattern.CASE_INSENSITIVE);
//                        String content = pattern.matcher(article.getContent()).replaceAll("_|_|_</$1>");
//                        content = Jsoup.parseBodyFragment(content).text();
//                        String[] flags = content.split("_\\|_\\|_");
//                        String flag = "";
//                        if(flags.length >= 1){
//                            flag = flags[0];
//                            if(flag.length() > 8){
//                                flag = flag.substring(0,8);
//                            }
//                        }
                        Article optimizedArticle = ArticleUtil.getReadabilityArticle(article,response.body());
                        CoreDB.i().articleDao().update(optimizedArticle);
                    }
                }
            });
        }
    }


    void handleNotTagStarArticles(String uid, long syncTimeMillis){
        List<Article> articles = CoreDB.i().articleDao().getNotTagStar(uid,syncTimeMillis);
        List<ArticleTag> articleTags = new ArrayList<>();
        Set<String> tagTitleSet = new HashSet<>();
        for (Article article: articles){
            if(StringUtils.isEmpty(article.getFeedId())){
                continue;
            }
            List<Category> categories = CoreDB.i().categoryDao().getByFeedId(uid,article.getFeedId());
            for (Category category:categories) {
                articleTags.add( new ArticleTag(uid, article.getId(), category.getId()) );
                tagTitleSet.add(category.getTitle());
            }
        }
        CoreDB.i().articleTagDao().insert(articleTags);

        List<Tag> tags = new ArrayList<>(tagTitleSet.size());
        for (String title:tagTitleSet) {
            Tag tag = new Tag();
            tag.setUid(uid);
            tag.setId(title);
            tag.setTitle(title);
            tags.add(tag);
            KLog.e("设置 Tag 数据：" + tag);
        }
        CoreDB.i().tagDao().insert(tags);
        CoreDB.i().articleTagDao().insert(articleTags);
        ArticleTags.i().addArticleTags(articleTags);
        ArticleTags.i().save();
    }

    void clearNotArticleTags(String uid){
        List<ArticleTag> articleTags = CoreDB.i().articleTagDao().getNotArticles(uid);
        for (ArticleTag articleTag:articleTags) {
            CoreDB.i().articleTagDao().delete(articleTag);
        }
    }

    void coverSaveCategories(List<Category> cloudyCategories) {
        String uid = App.i().getUser().getId();
        ArrayMap<String, Category> cloudyCategoriesTmp = new android.util.ArrayMap<>(cloudyCategories.size());
        for (Category category : cloudyCategories) {
            category.setUid(uid);
            cloudyCategoriesTmp.put(category.getId(), category);
        }

        List<Category> localCategories = CoreDB.i().categoryDao().getAll(uid);
        Iterator<Category> iterator = localCategories.iterator();
        Category tmpCategory;
        while(iterator.hasNext()){
            tmpCategory = iterator.next();
            if (cloudyCategoriesTmp.get(tmpCategory.getId()) == null) {
                CoreDB.i().categoryDao().delete(tmpCategory);
                iterator.remove();
            }else {
                cloudyCategoriesTmp.remove(tmpCategory.getId());
            }
        }

        cloudyCategories.clear();
        for (Map.Entry<String,Category> entry: cloudyCategoriesTmp.entrySet()) {
            cloudyCategories.add(entry.getValue());
        }

        CoreDB.i().categoryDao().insert(localCategories);
        CoreDB.i().categoryDao().insert(cloudyCategories);
    }

    void coverSaveFeeds(List<Feed> cloudyFeeds) {
        String uid = App.i().getUser().getId();
        ArrayMap<String, Feed> cloudyMap = new android.util.ArrayMap<>(cloudyFeeds.size());
        for (Feed feed : cloudyFeeds) {
            feed.setUid(uid);
            cloudyMap.put(feed.getId(), feed);
        }

        List<Feed> localFeeds = CoreDB.i().feedDao().getAll(uid);
        List<Feed> deleteFeeds = new ArrayList<>();
        Iterator<Feed> iterator = localFeeds.iterator();
        Feed localFeed, commonFeed;
        while(iterator.hasNext()){
            localFeed = iterator.next();
            commonFeed = cloudyMap.get(localFeed.getId());
            if ( commonFeed == null) {
                CoreDB.i().feedCategoryDao().deleteByFeedId(localFeed.getUid(), localFeed.getId());
                CoreDB.i().articleDao().deleteUnStarByFeedId(localFeed.getUid(), localFeed.getId());
                CoreDB.i().feedDao().delete(localFeed);
                deleteFeeds.add(localFeed);
                iterator.remove();// 删除后，这里只剩2者的交集
//                KLog.e("删除本地的feed：" + localFeed.getId() + " , " + localFeed.getTitle() + " , " + localFeed.getUnreadCount() );
            }else {
                localFeed.setTitle(commonFeed.getTitle());
                localFeed.setFeedUrl(commonFeed.getFeedUrl());
                localFeed.setHtmlUrl(commonFeed.getHtmlUrl());
                cloudyMap.remove(localFeed.getId());
            }
        }
        cloudyFeeds.clear();
        for (Map.Entry<String,Feed> entry: cloudyMap.entrySet()) {
            cloudyFeeds.add(entry.getValue());
        }

        CoreDB.i().feedDao().insert(localFeeds);
        CoreDB.i().feedDao().insert(cloudyFeeds);
        Unsubscribe.genBackupFile2(App.i().getUser(), deleteFeeds);
    }

    void coverFeedCategory(List<FeedCategory> cloudyFeedCategories) {
        ArrayMap<String, FeedCategory> cloudyCategoriesTmp = new ArrayMap<>(cloudyFeedCategories.size());
        for (FeedCategory feedCategory : cloudyFeedCategories) {
            cloudyCategoriesTmp.put(feedCategory.getFeedId() + feedCategory.getCategoryId(), feedCategory);
        }

        List<FeedCategory> localFeedCategories =  CoreDB.i().feedCategoryDao().getAll(App.i().getUser().getId());
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



    void handleDuplicateArticle() {
        // 清理重复的文章
        Article articleSample;
        List<String> links = CoreDB.i().articleDao().getDuplicatesLink(App.i().getUser().getId());
        List<Article> articleList;
        for (String link : links) {
            articleList = CoreDB.i().articleDao().getDuplicates(App.i().getUser().getId(), link);
            if (articleList == null || articleList.size() == 0) {
                continue;
            }
            // KLog.e("获取到的重复文章数量：" + articleList.size());
            // 获取第一个作为范例
            articleSample = articleList.get(0);
            articleList.remove(0);

            List<Article> articles = new ArrayList<>();
            for (Article article: articleList) {
                if( articleSample.getCrawlDate() != article.getCrawlDate()){
                    article.setCrawlDate(articleSample.getCrawlDate());
                    article.setPubDate(articleSample.getPubDate());
                    articles.add(article);
                }
            }
            CoreDB.i().articleDao().update(articles);
        }
    }

    // 优化在使用状态下多次同步到新文章时，这些文章的爬取时间
    void handleCrawlDate(){
        String uid = App.i().getUser().getId();
        long lastReadMarkTimeMillis = CoreDB.i().articleDao().getLastReadTimeMillis(uid);
        long lastStarMaskTimeMillis = CoreDB.i().articleDao().getLastStarTimeMillis(uid);
        long lastMarkTimeMillis = Math.max(lastReadMarkTimeMillis,lastStarMaskTimeMillis);
        CoreDB.i().articleDao().updateIdleCrawlDate(uid, lastMarkTimeMillis, System.currentTimeMillis());
    }

    void updateCollectionCount() {
        String uid = App.i().getUser().getId();
        CoreDB.i().feedDao().update( CoreDB.i().feedDao().getFeedsRealTimeCount(uid) );
        CoreDB.i().categoryDao().update( CoreDB.i().categoryDao().getCategoriesRealTimeCount(uid) );
    }
}
