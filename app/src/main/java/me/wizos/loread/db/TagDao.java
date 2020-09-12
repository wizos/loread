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
public interface TagDao {
    @Query("SELECT * FROM tag WHERE uid = :uid ORDER BY title COLLATE NOCASE ASC")
    List<Tag> getAll(String uid);

    @Query("SELECT * FROM tag WHERE uid = :uid ORDER BY title COLLATE NOCASE ASC")
    LiveData<List<Tag>> getAllLiveData(String uid);


    @Query("SELECT * FROM tag WHERE uid = :uid AND id = :id LIMIT 1")
    Tag getById(String uid, String id);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    void insert(Tag... tags);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    void insert(List<Tag> tags);

    @Update
    @Transaction
    void update(Tag... tags);

    @Update
    @Transaction
    void update(List<Tag> tags);

    @Delete
    @Transaction
    void delete(Tag... tags);

    @Query("DELETE FROM tag WHERE uid = (:uid)")
    void clear(String... uid);
}
