package me.wizos.loread.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user")
    List<User> loadAll();

    @Query("SELECT count(*) FROM user")
    int size();

    @Query("SELECT * FROM user WHERE id = :uid  LIMIT 1")
    User getById(String uid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    void insert(User... users);

    @Update
    @Transaction
    void update(User... Users);

    @Delete
    @Transaction
    void delete(User... users);

    @Query("DELETE FROM user WHERE id = :uid")
    @Transaction
    void delete(String uid);

    @Query("DELETE FROM user")
    @Transaction
    void clear();
}
