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
public interface ArticleTagDao {
    @Query("SELECT * FROM articletag WHERE uid = :uid")
    List<ArticleTag> getAll(String uid);

    @Query("SELECT * FROM articletag WHERE uid = :uid AND articleId = :articleId")
    List<ArticleTag> getByArticleId(String uid, String articleId);

    @Query("SELECT * FROM articletag WHERE uid = :uid AND tagId = :tagId")
    List<ArticleTag> getByTagId(String uid, String tagId);

    @Query("SELECT articletag.* FROM articletag " +
            "LEFT JOIN Article ON (articletag.uid = article.uid AND articletag.articleId = article.id) " +
            "WHERE articletag.uid = :uid " +
            "AND tagId is Null")
    List<ArticleTag> getNotArticles(String uid);


    @Query("SELECT count(*) FROM articletag WHERE uid = :uid AND tagId = :tagId")
    int getCountByTagId(String uid, String tagId);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    void insert(ArticleTag... articleTags);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @Transaction
    void insert(List<ArticleTag> feedCategories);

    @Update
    @Transaction
    void update(ArticleTag... articleTags);

    @Update
    @Transaction
    void update(List<ArticleTag> articleTags);

    @Transaction
    @Query("UPDATE articletag SET tagId = :newTagId where  uid = :uid AND tagId = :oldTagId")
    void updateCategoryId(String uid, String oldTagId, String newTagId);


    @Delete
    @Transaction
    void delete(ArticleTag articleTag);

    @Delete
    @Transaction
    void delete(List<ArticleTag> articleTags);

    @Transaction
    @Query("DELETE FROM articletag WHERE uid = :uid AND articleId = :articleId")
    void deleteByArticleId(String uid, String articleId);

    @Transaction
    @Query("DELETE FROM articletag WHERE uid = (:uid) AND tagId = :tagId")
    void deleteByTagId(String uid, String tagId);

    @Transaction
    @Query("DELETE FROM articletag WHERE uid = (:uid)")
    void clear(String... uid);
}
