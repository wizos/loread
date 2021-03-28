package me.wizos.loread.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import me.wizos.loread.bean.feedly.CategoryItem;

import static androidx.room.ForeignKey.CASCADE;

/**
 indices = {@Index({"id","uid","title"})} ,

 * Created by Wizos on 2020/3/17.
 */

@Entity(primaryKeys = {"id","uid"},
        indices = {@Index({"id"}),@Index({"uid"}),@Index({"title"})},
        foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "uid", onDelete = CASCADE))
public class Category {
    @NonNull
    private String uid;
    @NonNull
    private String id;
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
        return this.unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public int getStarCount() {
        return this.starCount;
    }

    public void setStarCount(int starCount) {
        this.starCount = starCount;
    }

    public int getAllCount() {
        return this.allCount;
    }

    public void setAllCount(int allCount) {
        this.allCount = allCount;
    }

    public CategoryItem convert2CategoryItem() {
        CategoryItem category = new CategoryItem();
        category.setId(id);
        category.setLabel(title);
        return category;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id='" + id + '\'' +
                ", uid='" + uid + '\'' +
                ", title='" + title + '\'' +
                ", unreadCount=" + unreadCount +
                ", starCount=" + starCount +
                ", allCount=" + allCount +
                '}';
    }
}
