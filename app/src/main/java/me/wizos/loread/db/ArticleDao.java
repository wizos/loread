package me.wizos.loread.db;

import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Transaction;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import java.util.List;

import me.wizos.loread.App;

/**
 * DataSource的三个子类：
 * PositionalDataSource: 主要用于加载数据可数有限的数据。比如加载本地数据库，这种情况下用户可以通过比如说像通讯录按姓的首字母查询的情况。能够跳转到任意的位置。
 * ItemKeyedDataSource:主要用于加载逐渐增加的数据。比如说网络请求的数据随着不断的请求得到的数据越来越多。然后它适用的情况就是通过N-1item的数据来获取Nitem数据的情况。比如说Github的api。
 * PageKeyedDataSource:这个和ItemKeyedDataSource有些相似，都是针对那种不断增加的数据。这里网络请求得到数据是分页的。比如说知乎日报的news的api。
 *
 * 从 Read 属性的4个值(Readed, UnRead, UnReading, All), Star 属性的3个类型(Stared, UnStar, All)中，抽出 UnRead(含UnReading), Stared, All 3个快捷状态，供用户在主页面切换时使用
 * 根据 StreamId 来获取文章，可从2个属性( Categories[针对Tag], OriginStreamId[针对Feed] )上，共4个变化上（All, TTRSSCategoryItem, NoTag, TTRSSFeedItem）来获取文章。
 * 据 StreamState 也是从2个属性(ReadState, StarState)的3个快捷状态 ( UnRead[含UnReading], Stared, All ) 来获取文章。
 * 所以文章列表页会有6种组合：某个 Categories 内的 UnRead[含UnReading], Stared, All。某个 OriginStreamId 内的 UnRead[含UnReading], Stared, All。
 */
@Dao
public interface ArticleDao {
    @Query("SELECT * FROM article WHERE uid = :uid AND id = :id LIMIT 1")
    Article getById(String uid, String id);

//    @Query("SELECT * FROM article WHERE uid = :uid " +
//            "ORDER BY crawlDate DESC,pubDate DESC")
//    Cursor getAll2(String uid);
//    @Query("SELECT * FROM article " +
//            "WHERE uid = :uid " +
//            "AND (article.readStatus = " + App.STATUS_UNREAD  + " OR article.readStatus = " + App.STATUS_UNREADING  + " OR (article.readStatus = " + App.STATUS_READED +" AND article.readUpdated > :timeMillis) ) " +
//            "ORDER BY crawlDate DESC,pubDate DESC")
//    DataSource.Factory<Integer,Article> getUnread2(String uid,long timeMillis);

    @Query("SELECT * FROM article " +
            "WHERE uid = :uid " +
            "AND crawlDate < :timeMillis " +
            "ORDER BY crawlDate DESC,pubDate DESC")
    DataSource.Factory<Integer,Article> getAll(String uid,long timeMillis);

    @Query("SELECT * FROM article " +
            "WHERE uid = :uid " +
            "AND crawlDate < :timeMillis " +
            "AND (article.starStatus = " + App.STATUS_STARED + "  OR (article.starStatus = " + App.STATUS_UNSTAR +" AND article.starUpdated > :timeMillis))" +
            "ORDER BY crawlDate DESC,pubDate DESC")
    DataSource.Factory<Integer,Article> getStared(String uid,long timeMillis);

    @Query("SELECT * FROM article " +
            "WHERE uid = :uid " +
            "AND crawlDate < :timeMillis " +
            "AND (article.readStatus = " + App.STATUS_UNREAD  + " OR article.readStatus = " + App.STATUS_UNREADING + " OR (article.readStatus = " + App.STATUS_READED +" AND article.readUpdated > :timeMillis)) " +
            "ORDER BY crawlDate DESC,pubDate DESC")
    DataSource.Factory<Integer,Article> getUnread(String uid,long timeMillis);

    @Query("SELECT article.* FROM article " +
            "LEFT JOIN FeedCategory ON (article.uid = FeedCategory.uid AND article.feedId = FeedCategory.feedId)" +
            "WHERE article.uid = :uid " +
            "AND article.crawlDate < :timeMillis " +
            "AND FeedCategory.categoryId = :categoryId " +
            "ORDER BY crawlDate DESC,pubDate DESC")
    DataSource.Factory<Integer,Article> getAllByCategoryId(String uid, String categoryId, long timeMillis);
    @Query("SELECT article.* FROM article " +
            "LEFT JOIN FeedCategory ON (article.uid = FeedCategory.uid AND article.feedId = FeedCategory.feedId)" +
            "WHERE article.uid = :uid " +
            "AND crawlDate < :timeMillis " +
            "AND FeedCategory.categoryId = :categoryId " +
            "AND (article.readStatus = " + App.STATUS_UNREAD  + " OR article.readStatus = " + App.STATUS_UNREADING + " OR (article.readStatus = " + App.STATUS_READED +" AND article.readUpdated > :timeMillis) ) " +
            "ORDER BY crawlDate DESC,pubDate DESC")
    DataSource.Factory<Integer,Article> getUnreadByCategoryId(String uid, String categoryId,long timeMillis);
    @Query("SELECT article.* FROM article " +
            "LEFT JOIN FeedCategory ON (article.uid = FeedCategory.uid AND article.feedId = FeedCategory.feedId)" +
            "WHERE article.uid = :uid " +
            "AND article.crawlDate < :timeMillis " +
            "AND FeedCategory.categoryId = :categoryId " +
            "AND (article.starStatus = " + App.STATUS_STARED  + " OR (article.starStatus = " + App.STATUS_UNSTAR +" AND article.starUpdated > :timeMillis) )" +
            "ORDER BY crawlDate DESC,pubDate DESC")
    DataSource.Factory<Integer,Article> getStaredByCategoryId(String uid, String categoryId, long timeMillis);


    @Query("SELECT article.* FROM article " +
            "LEFT JOIN ArticleTag ON (article.uid = ArticleTag.uid AND article.id = ArticleTag.articleId)" +
            "WHERE article.uid = :uid " +
            "AND article.crawlDate < :timeMillis " +
            "AND ArticleTag.tagId = :tagId " +
            "AND (article.starStatus = " + App.STATUS_STARED  + " OR (article.starStatus = " + App.STATUS_UNSTAR +" AND article.starUpdated > :timeMillis) )" +
            "ORDER BY crawlDate DESC,pubDate DESC")
    DataSource.Factory<Integer,Article> getStaredByTagId(String uid, String tagId, long timeMillis);

    @Query("SELECT article.* FROM article " +
            "LEFT JOIN FeedCategory ON (article.uid = FeedCategory.uid AND article.feedId = FeedCategory.feedId)" +
            "WHERE article.uid = :uid " +
            "AND article.crawlDate < :timeMillis " +
            "AND FeedCategory.categoryId is NULL " +
            "ORDER BY crawlDate,pubDate DESC")
    DataSource.Factory<Integer,Article> getAllByUncategory(String uid, long timeMillis);
    @Query("SELECT article.* FROM article " +
            "LEFT JOIN FeedCategory ON (article.uid = FeedCategory.uid AND article.feedId = FeedCategory.feedId)" +
            "WHERE article.uid = :uid " +
            "AND article.crawlDate < :timeMillis " +
            "AND FeedCategory.categoryId is NULL " +
            "AND (article.readStatus = " + App.STATUS_UNREAD  + " OR article.readStatus = " + App.STATUS_UNREADING + " OR (article.readStatus = " + App.STATUS_READED +" AND article.readUpdated > :timeMillis) ) " +
            "ORDER BY crawlDate,pubDate DESC")
    DataSource.Factory<Integer,Article> getUnreadByUncategory(String uid, long timeMillis);
    @Query("SELECT article.* FROM article " +
            "LEFT JOIN FeedCategory ON (article.uid = FeedCategory.uid AND article.feedId = FeedCategory.feedId)" +
            "WHERE article.uid = :uid " +
            "AND article.crawlDate < :timeMillis " +
            "AND FeedCategory.categoryId is NULL " +
            "AND (article.starStatus = " + App.STATUS_STARED + " OR (article.starStatus = " + App.STATUS_UNSTAR +" AND article.starUpdated > :timeMillis) ) " +
            "ORDER BY crawlDate DESC,pubDate DESC")
    DataSource.Factory<Integer,Article> getStaredByUncategory(String uid, long timeMillis);

    @Query("SELECT article.* FROM article " +
            "LEFT JOIN ArticleTag ON (article.uid = articletag.uid AND article.id = articletag.articleId)" +
            "WHERE article.uid = :uid " +
            "AND article.crawlDate < :timeMillis " +
            "AND articletag.tagId is NULL " +
            "AND (article.starStatus = " + App.STATUS_STARED + " OR (article.starStatus = " + App.STATUS_UNSTAR +" AND article.starUpdated > :timeMillis) ) " +
            "ORDER BY crawlDate DESC,pubDate DESC")
    DataSource.Factory<Integer,Article> getStaredByUnTag(String uid, long timeMillis);


    @Query("SELECT * FROM article " +
            "WHERE uid = :uid " +
            "AND article.crawlDate < :timeMillis " +
            "AND feedId = :feedId " +
            "ORDER BY crawlDate DESC,pubDate DESC")
    DataSource.Factory<Integer,Article> getAllByFeedId(String uid, String feedId, long timeMillis);
    @Query("SELECT * FROM article " +
            "WHERE uid = :uid " +
            "AND article.crawlDate < :timeMillis " +
            "AND feedId = :feedId " +
            "AND (readStatus = " + App.STATUS_UNREAD  + " OR readStatus = " + App.STATUS_UNREADING + " OR (article.readStatus = " + App.STATUS_READED +" AND article.readUpdated > :timeMillis) ) " +
            "ORDER BY crawlDate DESC,pubDate DESC")
    DataSource.Factory<Integer,Article> getUnreadByFeedId(String uid, String feedId, long timeMillis);
    @Query("SELECT * FROM article " +
            "WHERE uid = :uid " +
            "AND article.crawlDate < :timeMillis " +
            "AND feedId = :feedId " +
            "AND (starStatus = " + App.STATUS_STARED + " OR (article.starStatus = " + App.STATUS_UNSTAR +" AND article.starUpdated > :timeMillis) ) " +
            "ORDER BY crawlDate DESC,pubDate DESC")
    DataSource.Factory<Integer,Article> getStaredByFeedId(String uid, String feedId, long timeMillis);

    @Query("SELECT * FROM article " +
            "WHERE uid = :uid " +
            "AND title LIKE :keyword " +
            "ORDER BY crawlDate DESC,pubDate DESC")
    DataSource.Factory<Integer,Article> getAllByKeyword(String uid, String keyword);

//    @Query("DELETE FROM article WHERE uid = :uid AND pubDate < :timeMillis")
//    void clearPubDate(String uid,long timeMillis);

//    @Query("SELECT * FROM article " +
//            "WHERE uid = :uid " +
//            "AND (article.readStatus = " + App.STATUS_UNREAD  + " OR article.readStatus = " + App.STATUS_UNREADING  + " OR article.starStatus = " + App.STATUS_STARED  + ") " +
//            "ORDER BY crawlDate DESC,pubDate DESC")
//    List<Article> getValuable(String uid);

//    @Query("SELECT article.* FROM article " +
//            "LEFT JOIN FeedCategory ON (article.uid = FeedCategory.uid AND article.feedId = FeedCategory.feedId)" +
//            "WHERE article.uid = :uid " +
//            "AND FeedCategory.categoryId is NULL " +
//            "AND (article.readStatus = " + App.STATUS_UNREAD  + " OR article.readStatus = " + App.STATUS_UNREADING  + " OR article.starStatus = " + App.STATUS_STARED  + ") " +
//            "ORDER BY crawlDate,pubDate DESC")
//    Cursor getValuableByUnCategory(String uid);

//    @Query("SELECT article.* FROM article " +
//            "LEFT JOIN FeedCategory ON (article.uid = FeedCategory.uid AND article.feedId = FeedCategory.feedId)" +
//            "WHERE article.uid = :uid " +
//            "AND FeedCategory.categoryId = :categoryId " +
//            "AND (article.readStatus = " + App.STATUS_UNREAD  + " OR article.readStatus = " + App.STATUS_UNREADING  + " OR article.starStatus = " + App.STATUS_STARED  + ") " +
//            "ORDER BY crawlDate DESC,pubDate DESC")
//    Cursor getValuableByCategoryId(String uid, String categoryId);


    @Query("SELECT * FROM article WHERE uid = :uid")
    List<Article> getAllNoOrder(String uid);

    @Query("SELECT * FROM article " +
            "WHERE uid = :uid " +
            "AND crawlDate > :timeMillis ")
    List<Article> getAllNoOrder(String uid,long timeMillis);


    @Query("SELECT * FROM article " +
            "WHERE uid = :uid " +
            "AND article.starStatus = " + App.STATUS_STARED)
    List<Article> getStaredNoOrder(String uid);
    @Query("SELECT * FROM article " +
            "WHERE uid = :uid " +
            "AND (article.readStatus = " + App.STATUS_UNREAD  + " OR article.readStatus = " + App.STATUS_UNREADING  + ") ")
    List<Article>  getUnreadNoOrder(String uid);

    @Query("SELECT count(1) FROM article " +
            "WHERE uid = :uid " +
            "AND (article.readStatus = " + App.STATUS_UNREAD  + " OR article.readStatus = " + App.STATUS_UNREADING  + ") ")
    int getUnreadCount(String uid);

    @Query("SELECT count(1) FROM article " +
            "WHERE uid = :uid " +
            "AND article.starStatus = " + App.STATUS_STARED )
    int getStarCount(String uid);

    @Query("SELECT count(1) FROM article " +
            "WHERE uid = :uid " )
    int getAllCount(String uid);

    @Query("SELECT count(1) FROM article " +
            "LEFT JOIN FeedCategory ON (article.uid = FeedCategory.uid AND article.feedId = FeedCategory.feedId)" +
            "WHERE article.uid = :uid " +
            "AND FeedCategory.categoryId is NULL " +
            "AND (article.readStatus = " + App.STATUS_UNREAD  + " OR article.readStatus = " + App.STATUS_UNREADING  + ")")
    int getUncategoryUnreadCount(String uid);

    @Query("SELECT count(1) FROM article " +
            "LEFT JOIN FeedCategory ON (article.uid = FeedCategory.uid AND article.feedId = FeedCategory.feedId)" +
            "WHERE article.uid = :uid " +
            "AND FeedCategory.categoryId is NULL " +
            "AND article.starStatus = " + App.STATUS_STARED)
    int getUncategoryStarCount(String uid);

    @Query("SELECT count(1) FROM article " +
            "LEFT JOIN FeedCategory ON (article.uid = FeedCategory.uid AND article.feedId = FeedCategory.feedId)" +
            "WHERE article.uid = :uid " +
            "AND FeedCategory.categoryId is NULL ")
    int getUncategoryAllCount(String uid);

    @Query("SELECT readUpdated FROM article " +
            "WHERE uid = :uid " +
            "ORDER BY readUpdated DESC LIMIT 1")
    long getLastReadTimeMillis(String uid);

    @Query("SELECT starUpdated FROM article " +
            "WHERE uid = :uid " +
            "ORDER BY starUpdated DESC LIMIT 1")
    long getLastStarTimeMillis(String uid);

    @Query("SELECT * FROM article " +
            "WHERE uid = :uid " +
            "AND (article.readStatus = " + App.STATUS_UNREADING  + " OR article.saveStatus !=" + App.STATUS_NOT_FILED + ")")
    List<Article> getBackup(String uid);

    // TODO: 2020/5/1 文章要加上是否已经被 Readability 的标志
    @Query("SELECT article.* FROM article " +
            "LEFT JOIN Feed ON (article.uid = Feed.uid AND article.feedId = Feed.id) " +
            "WHERE article.uid = :uid " +
            "AND article.crawlDate = :timeMillis " +
            "AND Feed.displayMode = 1 ")
    List<Article> getNeedReadability(String uid, long timeMillis);

    @Query("SELECT article.* FROM article " +
            "LEFT JOIN ArticleTag ON (article.uid = articletag.uid AND article.id = articletag.articleId) " +
            "WHERE article.uid = :uid " +
            "AND article.crawlDate >= :timeMillis " +
            "AND article.starStatus = " + App.STATUS_STARED + " " +
            "AND article.feedId not Null " +
            "AND articletag.tagId is Null ")
    List<Article> getNotTagStar(String uid, long timeMillis);

    @Query("SELECT * FROM article " +
            "WHERE uid = :uid " +
            "AND id in (:ids)")
    List<Article> getArticles(String uid, List<String> ids);

    @RawQuery
    List<Article> getActionRuleArticlesRaw(SupportSQLiteQuery query);

    @RawQuery
    List<Entry> getActionRuleArticlesRaw2(SupportSQLiteQuery query);



//    @Query("SELECT * FROM article WHERE uid = :uid AND feedId = :feedId " +
//            "AND (readStatus = " + App.STATUS_UNREAD  + " OR readStatus = " + App.STATUS_UNREADING  + " OR article.starStatus = " + App.STATUS_STARED  + ") " +
//            "ORDER BY crawlDate DESC,pubDate DESC")
//    Cursor getValuableByFeedId(String uid, String feedId);

    @Query("SELECT * FROM article WHERE uid = :uid ORDER BY id DESC LIMIT 1")
    Article getLastArticle(String uid);

    @Query("SELECT link FROM article " +
            "WHERE uid = :uid " +
            "GROUP BY link HAVING COUNT(*) > 1") //,title
    List<String> getDuplicatesLink(String uid);

    @Query("SELECT * FROM article " +
            "WHERE uid = :uid " +
            "AND link = :link " +
            "ORDER BY crawlDate DESC")
    List<Article> getDuplicates(String uid, String link);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    void insert(Article... articles);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    void insert(List<Article> articles);

    @Update
    @Transaction
    void update(Article... articles);

    @Update
    @Transaction
    void update(List<Article> articles);

    /**
     * 将上次操作之后所有新同步文章的爬取时间都重置
     * @param uid
     * @param lastMarkTimeMillis
     * @param targetTimeMillis
     */
    @Query("UPDATE Article SET crawlDate = :targetTimeMillis WHERE uid = :uid AND crawlDate > :lastMarkTimeMillis ")
    void updateIdleCrawlDate(String uid,long lastMarkTimeMillis, long targetTimeMillis);

    @Query("DELETE FROM article WHERE uid = :uid AND feedId = :feedId AND starStatus = " + App.STATUS_UNSTAR)
    void deleteUnStarByFeedId(String uid, String feedId);

    @Delete
    @Transaction
    void delete(Article... articles);

    @Delete
    @Transaction
    void delete(List<Article> articles);

    @Query("SELECT * FROM article WHERE uid = :uid AND readStatus = " + App.STATUS_READED + " AND starStatus = " + App.STATUS_UNSTAR + " AND saveStatus = " + App.STATUS_TO_BE_FILED + " AND crawlDate < :time" )
    List<Article> getReadedUnstarBeFiledLtTime(String uid, long time);

    @Query("SELECT * FROM article WHERE uid = :uid AND readStatus = " + App.STATUS_READED + " AND starStatus = " + App.STATUS_STARED + " AND saveStatus = " + App.STATUS_TO_BE_FILED + " AND crawlDate < :time" )
    List<Article> getReadedStaredBeFiledLtTime(String uid, long time);

    @Query("SELECT * FROM article WHERE uid = :uid AND readStatus = " + App.STATUS_READED + " AND starStatus = " + App.STATUS_UNSTAR + " AND crawlDate < :time" )
    List<Article> getReadedUnstarLtTime(String uid, long time);

    @Query("DELETE FROM article WHERE uid = (:uid)")
    void clear(String... uid);
}
