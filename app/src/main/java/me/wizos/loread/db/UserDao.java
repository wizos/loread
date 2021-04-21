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
public interface UserDao {
    // 在不加WHERE限制条件的情况下，COUNT(*)与COUNT(COL)基本可以认为是等价的；
    // 但是在有WHERE限制条件的情况下，COUNT(*)会比COUNT(COL)快非常多；
    @Query("SELECT * FROM user")
    List<User> loadAll();

    @Query("SELECT count(*) FROM user")
    LiveData<Integer> getSize();


    @Query("select t1.num1+t2.num2+t3.num3+t4.num4 from" +
            "  (select count(*) num1 from user) AS t1," +
            "  (select count(*) num2 from category) AS t2," +
            "  (select count(*) num3 from Feed) AS t3," +
            "  (select count(*) num4 from feedcategory) AS t4")
    LiveData<Integer> observeSize();

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
