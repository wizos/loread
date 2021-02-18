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
public interface FeedCategoryDao {
    @Query("SELECT count(*) FROM feedcategory WHERE uid = :uid")
    LiveData<Integer> getSize(String uid);

    @Query("SELECT * FROM feedcategory WHERE uid = :uid")
    List<FeedCategory> getAll(String uid);

    @Query("SELECT * FROM feedcategory WHERE uid = :uid AND categoryId = :categoryId")
    List<FeedCategory> getByCategory(String uid,String categoryId);

    @Query("SELECT categoryId FROM feedcategory WHERE uid = :uid AND feedId = :feedId")
    List<String> getCategoryId(String uid,String feedId);

    @Query("SELECT count(*) FROM feedcategory WHERE uid = :uid AND categoryId = :categoryId")
    int getCountByCategoryId(String uid,String categoryId);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    void insert(FeedCategory... feedCategories);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    void insert(List<FeedCategory> feedCategories);

    @Update
    @Transaction
    void update(FeedCategory... feedCategories);
    @Update
    @Transaction
    void update(List<FeedCategory> feedCategories);

    @Query("UPDATE feedcategory SET categoryId = :newCategoryId where  uid = :uid AND categoryId = :oldCategoryId")
    void updateCategoryId(String uid,String oldCategoryId, String newCategoryId);


    @Delete
    @Transaction
    void delete(FeedCategory feedCategory);

    @Query("DELETE FROM feedcategory WHERE uid = (:uid) AND feedId = :feedId")
    void deleteByFeedId(String uid, String feedId);

    @Query("DELETE FROM feedcategory WHERE uid = :uid")
    void clear(String uid);
}
