package me.wizos.loread.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import org.jetbrains.annotations.NotNull;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Created by Wizos on 2020/3/17.
 */
@Entity(primaryKeys = {"uid","articleId","tagId"},
        indices = {@Index({"uid"}),@Index({"uid","articleId"}),@Index({"uid","tagId"})},
        foreignKeys = {@ForeignKey(entity = User.class, parentColumns = "id", childColumns = "uid", onDelete = CASCADE) }
)
public class ArticleTag {
    @NonNull
    private String uid;
    @NonNull
    private String articleId;
    @NonNull
    private String tagId;

    public ArticleTag(@NonNull String uid, @NonNull String articleId, @NonNull String tagId){
        this.uid = uid;
        this.articleId = articleId;
        this.tagId = tagId;
    }

    @Override
    public String toString() {
        return "ArticleTag{" +
                "uid=" + uid +
                ", articleId='" + articleId + '\'' +
                ", tagId='" + tagId + '\'' +
                '}';
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    @NotNull
    public String getTagId() {
        return this.tagId;
    }


    public void setTagId(@NotNull String tagId) {
        this.tagId = tagId;
    }


    @NotNull
    public String getArticleId() {
        return this.articleId;
    }

    public void setArticleId(@NotNull String articleId) {
        this.articleId = articleId;
    }
}
