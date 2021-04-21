package me.wizos.loread.db;

import androidx.lifecycle.LiveData;
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
import me.wizos.loread.bean.collectiontree.CollectionFeed;

@Dao
public interface FeedDao {
    @Query("SELECT count(*) FROM feed WHERE uid = :uid")
    int getCount(String uid);

    @Query("SELECT * FROM feed WHERE uid = :uid")
    List<Feed> getAll(String uid);

    @Query("SELECT * FROM feed WHERE uid = :uid AND id = :id LIMIT 1")
    LiveData<Feed> get(String uid, String id);

    @Query("SELECT * FROM feed WHERE uid = :uid AND id = :id LIMIT 1")
    Feed getById(String uid, String id);

    @Query("SELECT * FROM feed WHERE uid = :uid AND feedUrl = :feedUrl LIMIT 1")
    Feed getByFeedUrl(String uid, String feedUrl);

    @Query("SELECT feed.* FROM feed " +
            "LEFT JOIN FeedCategory ON (feed.uid = FeedCategory.uid AND feed.id = FeedCategory.feedId) " +
            "WHERE feed.uid = :uid " +
            "AND feedcategory.categoryId = :categoryId ")
    List<Feed> getByCategoryId(String uid, String categoryId);

    // @Query("SELECT * FROM feed " +
    //         "WHERE feed.uid = :uid " +
    //         "AND id IN ( SELECT feedid FROM feedcategory WHERE categoryId = :categoryId) " +
    //         "ORDER BY CASE WHEN feed.unreadCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // List<Feed> getByCategoryId(String uid, String categoryId);

    @Query("SELECT * FROM feed WHERE uid = :uid AND iconUrl IS NULL AND htmlUrl IS NOT NULL")
    List<Feed> getFeedsByNoIconUrl(String uid);

    @Query("SELECT * FROM feed WHERE uid = :uid AND (feedUrl like :keyword OR htmlUrl like :keyword)")
    List<Feed> getAllByFeedUrlLike(String uid, String keyword);

    @Query("SELECT * FROM feed WHERE uid = :uid AND title like :title")
    List<Feed> getAllByTitleLike(String uid, String title);

    // 包含上次同步时是正常的，以及不正常的
    @Query("SELECT * FROM feed " +
            "WHERE feed.uid = :uid " +
            "AND feed.lastErrorCount < 16 " +
            "AND ( " +
            "      (syncInterval = 0 AND (lastSyncTime + :globalSyncInterval * 60000 + lastErrorCount * lastErrorCount * lastErrorCount * 1000000) < :currentTimeMillis) " +
            "      OR " +
            "      (syncInterval > 0 AND (lastSyncTime + syncInterval * 60000 + lastErrorCount * lastErrorCount * lastErrorCount * 1000000) < :currentTimeMillis) " +
            "    ) " +
            "ORDER BY lastSyncTime ASC")
    List<Feed> getFeedsNeedSync(String uid, int globalSyncInterval, long currentTimeMillis);


    @Query("SELECT uid,id,title,feedUrl,htmlUrl,iconUrl,syncInterval,lastSyncError,lastErrorCount,unreadCount as count FROM feed " +
            "WHERE feed.uid = :uid " +
            "ORDER BY CASE WHEN feed.unreadCount > 0 THEN 0 WHEN feed.lastErrorCount > 0 THEN 1 ELSE 2 END, feed.title COLLATE NOCASE ASC")
    List<CollectionFeed> getFeedsUnreadCount(String uid);
    @Query("SELECT uid,id,title,feedUrl,htmlUrl,iconUrl,syncInterval,lastSyncError,lastErrorCount,starCount as count FROM feed " +
            "WHERE feed.uid = :uid " +
            "ORDER BY CASE WHEN feed.starCount > 0 THEN 0 WHEN feed.lastErrorCount > 0 THEN 1 ELSE 2 END, feed.title COLLATE NOCASE ASC")
    List<CollectionFeed> getFeedsStaredCount(String uid);
    @Query("SELECT uid,id,title,feedUrl,htmlUrl,iconUrl,syncInterval,lastSyncError,lastErrorCount,allCount as count FROM feed " +
            "WHERE feed.uid = :uid " +
            "ORDER BY CASE WHEN feed.allCount > 0 THEN 0 WHEN feed.lastErrorCount > 0 THEN 1 ELSE 2 END, feed.title COLLATE NOCASE ASC")
    List<CollectionFeed> getFeedsAllCount(String uid);

    // @Query("SELECT uid,id,title,feedUrl,htmlUrl,iconUrl,displayMode,state,syncInterval,lastSyncTime,lastSyncError,lastErrorCount,unreadCount as count,unreadCount,starCount,allCount FROM feed " +
    //         "WHERE feed.uid = :uid " +
    //         "ORDER BY CASE WHEN feed.unreadCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // List<Feed> getFeedsUnreadCount2(String uid);
    // @Query("SELECT *,starCount as count FROM feed " +
    //         "WHERE feed.uid = :uid " +
    //         "ORDER BY CASE WHEN feed.starCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // List<Feed> getFeedsStaredCount2(String uid);
    // @Query("SELECT *,allCount as count FROM feed " +
    //         "WHERE feed.uid = :uid " +
    //         "ORDER BY CASE WHEN feed.allCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // List<Feed> getFeedsAllCount2(String uid);

    // @Query("SELECT id,title,unreadCount as count FROM feed " +
    //         "WHERE feed.uid = :uid " +
    //         "ORDER BY CASE WHEN feed.unreadCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // LiveData<List<Collection>> getFeedsLiveDataUnreadCount(String uid);
    //
    // @Query("SELECT id,title,starCount as count FROM feed " +
    //         "WHERE feed.uid = :uid " +
    //         "ORDER BY CASE WHEN feed.starCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // LiveData<List<Collection>> getFeedsLiveDataStaredCount(String uid);
    //
    // @Query("SELECT id,title,allCount as count FROM feed " +
    //         "WHERE feed.uid = :uid " +
    //         "ORDER BY CASE WHEN feed.allCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // LiveData<List<Collection>> getFeedsLiveDataAllCount(String uid);


    // @Query("SELECT id,title,unreadCount as count FROM feed " +
    //         "WHERE feed.uid = :uid " +
    //         "AND id IN ( SELECT feedid FROM feedcategory WHERE categoryId = :categoryId) " +
    //         "ORDER BY CASE WHEN feed.unreadCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // List<Collection> getFeedsUnreadCountByCategoryId(String uid, String categoryId);
    // @Query("SELECT id,title,unreadCount as count FROM feed " +
    //         "WHERE feed.uid = :uid " +
    //         "AND id IN ( SELECT feedid FROM feedcategory WHERE categoryId = :categoryId) " +
    //         "ORDER BY CASE WHEN feed.unreadCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // LiveData<List<Collection>> getFeedsLiveDataUnreadCountByCategoryId(String uid, String categoryId);
    //
    // @Query("SELECT id,title,starCount as count FROM feed " +
    //         "WHERE feed.uid = :uid " +
    //         "AND id IN ( SELECT feedid FROM feedcategory WHERE categoryId = :categoryId) " +
    //         "ORDER BY CASE WHEN feed.starCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // List<Collection> getFeedsStarCountByCategoryId(String uid, String categoryId);
    // @Query("SELECT id,title,starCount as count FROM feed " +
    //         "WHERE feed.uid = :uid " +
    //         "AND id IN ( SELECT feedid FROM feedcategory WHERE categoryId = :categoryId) " +
    //         "ORDER BY CASE WHEN feed.starCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // LiveData<List<Collection>> getFeedsLiveDataStarCountByCategoryId(String uid, String categoryId);
    //
    // // sql一
    // @Query("SELECT id,title,allCount as count FROM feed " +
    //         "WHERE feed.uid = :uid " +
    //         "AND id IN ( SELECT feedid FROM feedcategory WHERE categoryId = :categoryId) " +
    //         "ORDER BY CASE WHEN feed.allCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // List<Collection> getFeedsAllCountByCategoryId2(String uid, String categoryId);
    // // sql二
    // @Query("SELECT id,title,allCount as count FROM feed " +
    //         "LEFT JOIN FeedCategory ON (feed.uid = FeedCategory.uid AND feed.id = FeedCategory.feedId) " +
    //         "WHERE feed.uid = :uid " +
    //         "AND feedcategory.categoryId = :categoryId " +
    //         "ORDER BY CASE WHEN feed.allCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // List<Collection> getFeedsAllCountByCategoryId(String uid, String categoryId);
    // @Query("SELECT id,title,allCount as count FROM feed " +
    //         "LEFT JOIN FeedCategory ON (feed.uid = FeedCategory.uid AND feed.id = FeedCategory.feedId) " +
    //         "WHERE feed.uid = :uid " +
    //         "AND feedcategory.categoryId = :categoryId " +
    //         "ORDER BY CASE WHEN feed.allCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // LiveData<List<Collection>> getFeedsLiveDataAllCountByCategoryId(String uid, String categoryId);
    //
    // @Query("SELECT id,title,unreadCount as count FROM feed " +
    //         "LEFT JOIN FeedCategory ON (feed.uid = FeedCategory.uid AND feed.id = FeedCategory.feedId) " +
    //         "WHERE feed.uid = :uid " +
    //         "AND (FeedCategory.categoryId = 0 OR FeedCategory.categoryId is Null) " +
    //         "ORDER BY CASE WHEN feed.unreadCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // List<Collection> getFeedsUnreadCountByUnCategory(String uid);
    // @Query("SELECT id,title,unreadCount as count FROM feed " +
    //         "LEFT JOIN FeedCategory ON (feed.uid = FeedCategory.uid AND feed.id = FeedCategory.feedId) " +
    //         "WHERE feed.uid = :uid " +
    //         "AND (FeedCategory.categoryId = 0 OR FeedCategory.categoryId is Null) " +
    //         "ORDER BY CASE WHEN feed.unreadCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // LiveData<List<Collection>> getFeedsLiveDataUnreadCountByUnCategory(String uid);
    //
    //
    // @Query("SELECT id,title,starCount as count FROM feed " +
    //         "LEFT JOIN FeedCategory ON (feed.uid = FeedCategory.uid AND feed.id = FeedCategory.feedId) " +
    //         "WHERE feed.uid = :uid " +
    //         "AND (FeedCategory.categoryId = 0 OR FeedCategory.categoryId is Null) " +
    //         "ORDER BY CASE WHEN feed.starCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // List<Collection> getFeedsStarCountByUnCategory(String uid);
    // @Query("SELECT id,title,starCount as count FROM feed " +
    //         "LEFT JOIN FeedCategory ON (feed.uid = FeedCategory.uid AND feed.id = FeedCategory.feedId) " +
    //         "WHERE feed.uid = :uid " +
    //         "AND (FeedCategory.categoryId = 0 OR FeedCategory.categoryId is Null) " +
    //         "ORDER BY CASE WHEN feed.starCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // LiveData<List<Collection>> getFeedsLiveDataStarCountByUnCategory(String uid);
    //
    // @Query("SELECT id,title,allCount as count FROM feed " +
    //         "LEFT JOIN FeedCategory ON (feed.uid = FeedCategory.uid AND feed.id = FeedCategory.feedId) " +
    //         "WHERE feed.uid = :uid " +
    //         "AND (FeedCategory.categoryId = 0 OR FeedCategory.categoryId is Null) " +
    //         "ORDER BY CASE WHEN feed.allCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // List<Collection> getFeedsAllCountByUnCategory(String uid);
    // @Query("SELECT id,title,allCount as count FROM feed " +
    //         "LEFT JOIN FeedCategory ON (feed.uid = FeedCategory.uid AND feed.id = FeedCategory.feedId) " +
    //         "WHERE feed.uid = :uid " +
    //         "AND (FeedCategory.categoryId = 0 OR FeedCategory.categoryId is Null) " +
    //         "ORDER BY CASE WHEN feed.allCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    // LiveData<List<Collection>> getFeedsLiveDataAllCountByUnCategory(String uid);
    //
    // @Query("SELECT count(1) FROM feed " +
    //         "LEFT JOIN FeedCategory ON (feed.uid = FeedCategory.uid AND feed.id = FeedCategory.feedId)" +
    //         "WHERE feed.uid = :uid " +
    //         "AND (FeedCategory.categoryId = 0 OR FeedCategory.categoryId is Null)")
    // int getFeedsCountByUnCategory(String uid);


    // @Query("SELECT * FROM FeedView WHERE uid = :uid" )
    // List<Feed> getFeedsRealTimeCount(String uid);

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Feed feed);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    void insert(List<Feed> feeds);

    @Transaction
    @Query("UPDATE feed SET title = :newName where uid = :uid AND id = :id")
    void updateName(String uid, String id, String newName);

    @Transaction
    @Query("UPDATE feed SET allCount = (select COUNT(*) from Article where Feed.uid = Article.uid AND Feed.id = Article.feedId) where uid = :uid")
    void updateAllCount(String uid);

    @Transaction
    @Query("UPDATE feed SET unreadCount = (select COUNT(*) from Article where Feed.uid = Article.uid AND Feed.id = Article.feedId AND readStatus IN (" + App.STATUS_UNREAD  + ", " + App.STATUS_UNREADING + ")) where uid = :uid")
    void updateUnreadCount(String uid);

    @Transaction
    @Query("UPDATE feed SET starCount = (select COUNT(*) from Article where Feed.uid = Article.uid AND Feed.id = Article.feedId AND starStatus IN (" + App.STATUS_STARED + ") ) where uid = :uid")
    void updateStarCount(String uid);

    @Transaction
    @Query("UPDATE Feed SET allCount = IFNULL( (SELECT allCount FROM FeedViewAllCount WHERE FeedViewAllCount.uid = :uid AND FeedViewAllCount.id = Feed.id), 0 ) WHERE uid = :uid")
    void updateAllCount2(String uid);

    @Transaction
    @Query("UPDATE Feed SET unreadCount = IFNULL( (SELECT unreadCount FROM FeedViewUnreadCount WHERE FeedViewUnreadCount.uid = :uid AND FeedViewUnreadCount.id = Feed.id), 0 ) WHERE uid = :uid")
    void updateUnreadCount2(String uid);

    @Transaction
    @Query("UPDATE Feed SET starCount = IFNULL( (SELECT starCount FROM FeedViewStarCount WHERE FeedViewStarCount.uid = :uid AND FeedViewStarCount.id = Feed.id), 0 ) WHERE uid = :uid")
    void updateStarCount2(String uid);

    @Transaction
    @Query("UPDATE feed SET lastPubDate = IFNULL( (select pubDate from Article where Feed.uid = Article.uid AND Feed.id = Article.feedId ORDER BY pubDate DESC LIMIT 1), 0) where uid = :uid")
    void updateLastPubDate(String uid);

    @Transaction
    @Query("UPDATE feed SET lastPubDate = IFNULL( (select pubDate from Article where Article.uid = Feed.uid AND Article.feedId = Feed.id ORDER BY pubDate DESC LIMIT 1), 0) where uid = :uid AND id = :id")
    void updateLastPubDate(String uid, String id);

    @Transaction
    @Query("UPDATE feed SET syncInterval = -1 where uid = :uid AND lastErrorCount = 16")
    void blockSync(String uid);

    @Update
    @Transaction
    void update(Feed... feeds);
    @Update
    @Transaction
    void update(List<Feed> feeds);

    @Delete
    @Transaction
    void delete(Feed... feeds);

    @Delete
    @Transaction
    void delete(List<Feed> feeds);

    @Transaction
    @Query("DELETE FROM feed WHERE uid = (:uid) AND id = :id")
    void deleteById(String uid, String id);

    @Transaction
    @Query("DELETE FROM feed WHERE uid = (:uid) AND feedUrl = :url")
    void deleteByFeedUrl(String uid, String url);

    @Transaction
    @Query("DELETE FROM feed WHERE uid = :uid")
    void clear(String uid);

    @Transaction
    @RawQuery(observedEntities = {Feed.class, FeedCategory.class})
    DataSource.Factory<Integer,Feed>  get(SupportSQLiteQuery query);
}
