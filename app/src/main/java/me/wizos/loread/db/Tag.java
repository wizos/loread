package me.wizos.loread.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Created by Wizos on 2020/5/25.
 */

@Entity(primaryKeys = {"id","uid"},
        indices = {@Index({"id"}),@Index({"uid"}),@Index({"title"})},
        foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "uid", onDelete = CASCADE))
public class Tag {
    @NonNull
    private String id;
    @NonNull
    private String uid;
    private String title;

    private int unreadCount;
    private int starCount;
    private int allCount;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public int getStarCount() {
        return starCount;
    }

    public void setStarCount(int starCount) {
        this.starCount = starCount;
    }

    public int getAllCount() {
        return allCount;
    }

    public void setAllCount(int allCount) {
        this.allCount = allCount;
    }

    public Category convert(){
        Category category = new Category();
        category.setId(id);
        category.setUid(uid);
        category.setTitle(title);
        category.setUnreadCount(unreadCount);
        category.setStarCount(starCount);
        category.setAllCount(allCount);
        return category;
    }
}
