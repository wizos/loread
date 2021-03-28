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

import me.wizos.loread.bean.collectiontree.Collection;

@Dao
public interface CategoryDao {
    @Query("SELECT count(*) FROM category WHERE uid = :uid")
    LiveData<Integer> getSize(String uid);

    @Query("SELECT * FROM category WHERE uid = :uid ORDER BY title COLLATE NOCASE ASC")
    List<Category> getAll(String uid);
    @Query("SELECT * FROM category WHERE uid = :uid ORDER BY title COLLATE NOCASE ASC")
    LiveData<List<Category>> getAllLiveData(String uid);

    @Query("SELECT uid,id,title,unreadCount as count FROM category WHERE uid = :uid ORDER BY title COLLATE NOCASE ASC")
    List<Collection> getCategoriesUnreadCount(String uid);
    @Query("SELECT uid,id,title,starCount as count FROM category WHERE uid = :uid ORDER BY title COLLATE NOCASE ASC")
    List<Collection> getCategoriesStarCount(String uid);
    @Query("SELECT uid,id,title,allCount as count FROM category WHERE uid = :uid ORDER BY title COLLATE NOCASE ASC")
    List<Collection> getCategoriesAllCount(String uid);

    // @Query("SELECT *,unreadCount as count FROM category WHERE uid = :uid ORDER BY title COLLATE NOCASE ASC")
    // List<Category> getCategoriesUnreadCount2(String uid);
    // @Query("SELECT *,starCount as count FROM category WHERE uid = :uid ORDER BY title COLLATE NOCASE ASC")
    // List<Category> getCategoriesStarCount2(String uid);
    // @Query("SELECT *,allCount as count FROM category WHERE uid = :uid ORDER BY title COLLATE NOCASE ASC")
    // List<Category> getCategoriesAllCount2(String uid);

    @Query("SELECT category.* FROM category " +
            "LEFT JOIN feedcategory ON (category.uid = feedcategory.uid AND category.id = feedcategory.categoryId) " +
            "WHERE category.uid = :uid AND FeedCategory.feedId = :feedId " +
            "ORDER BY title COLLATE NOCASE ASC")
    List<Category> getByFeedId(String uid, String feedId);

    @Query("SELECT * FROM category WHERE uid = :uid AND id = :id LIMIT 1")
    Category getById(String uid, String id);

    @Query("SELECT title FROM category WHERE uid = :uid AND id = :id LIMIT 1")
    String getTitleById(String uid, String id);

    @Query("SELECT * FROM categoryview WHERE uid = :uid" )
    List<Category> getCategoriesRealTimeCount(String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    void insert(Category... categories);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    void insert(List<Category> categories);

    @Transaction
    @Query("UPDATE category SET id = :newId where uid = :uid AND id = :oldId")
    void updateId(String uid, String oldId, String newId);

    @Transaction
    @Query("UPDATE category SET title = :newName where uid = :uid AND id = :id")
    void updateName(String uid, String id, String newName);

    @Update
    @Transaction
    void update(Category... categories);

    @Update
    @Transaction
    void update(List<Category> categories);

    @Delete
    @Transaction
    void delete(Category... categories);

    @Transaction
    @Query("DELETE FROM category WHERE uid = :uid")
    void clear(String uid);
}
