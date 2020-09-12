package me.wizos.loread.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FeedDao {
    @Query("SELECT * FROM feed WHERE uid = :uid")
    List<Feed> getAll(String uid);

    @Query("SELECT * FROM feed WHERE uid = :uid")
    LiveData<List<Feed>> getAllLiveData(String uid);


    @Query("SELECT * FROM feed WHERE uid = :uid AND id = :id LIMIT 1")
    Feed getById(String uid,String id);

//    @Query("SELECT feed.* FROM feed " +
//            "LEFT JOIN feedcategory ON (feed.uid = feedcategory.uid AND feed.id = feedcategory.feedId) " +
//            "WHERE feed.uid = :uid " +
//            "AND feedcategory.categoryId = :categoryId " +
//            "ORDER BY case when feed.unreadCount > 0 then 0 else 1 end, feed.title ASC")

    @Query("SELECT * FROM feed " +
            "WHERE feed.uid = :uid " +
            "AND id IN ( SELECT feedid FROM feedcategory WHERE categoryId = :categoryId) " +
            "ORDER BY CASE WHEN feed.unreadCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    List<Feed> getByCategoryId(String uid,String categoryId);

    @Query("SELECT id,title,unreadCount as count FROM feed " +
            "WHERE feed.uid = :uid " +
            "AND id IN ( SELECT feedid FROM feedcategory WHERE categoryId = :categoryId) " +
            "ORDER BY CASE WHEN feed.unreadCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    List<Collection> getFeedsUnreadCountByCategoryId(String uid, String categoryId);

    @Query("SELECT id,title,starCount as count FROM feed " +
            "WHERE feed.uid = :uid " +
            "AND id IN ( SELECT feedid FROM feedcategory WHERE categoryId = :categoryId) " +
            "ORDER BY CASE WHEN feed.starCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    List<Collection> getFeedsStarCountByCategoryId(String uid, String categoryId);

    @Query("SELECT id,title,allCount as count FROM feed " +
            "WHERE feed.uid = :uid " +
            "AND id IN ( SELECT feedid FROM feedcategory WHERE categoryId = :categoryId) " +
            "ORDER BY CASE WHEN feed.allCount > 0 THEN 0 ELSE 1 END, feed.title COLLATE NOCASE ASC")
    List<Collection> getFeedsAllCountByCategoryId(String uid, String categoryId);


    @Query("SELECT * FROM FeedView WHERE uid = :uid" )
    List<Feed> getFeedsRealTimeCount(String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    void insert(Feed... feeds);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    void insert(List<Feed> feeds);

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

    @Query("DELETE FROM feed WHERE uid = (:uid) AND id = :id")
    void deleteById(String uid, String id);

    @Query("DELETE FROM feed WHERE uid = (:uid)")
    void clear(String... uid);
}
