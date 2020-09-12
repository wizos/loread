package me.wizos.loread.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Feed 与 Category 是 多对多关系，即一个 Feed 可以存在与多个 Category 中，Category 也可以包含多个 Feed
 * Created by Wizos on 2020/3/17.
 // @Index({"id", "uid", "title", "feedUrl"})
 @Index({"id"}),@Index({"uid"}),
 */
@Entity(
        primaryKeys = {"id","uid"},
        indices = {@Index({"id"}),@Index({"uid"}),@Index({"title"}),@Index({"feedUrl"})},
        foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "uid", onDelete = CASCADE) )
public class Feed {
    @NonNull
    private String id;
    @NonNull
    private String uid;

    private String title;

    private String feedUrl;
    private String htmlUrl;
    private String iconUrl;

    // 0->rss, 1->readability, 2->link
    private int displayMode;

    private int unreadCount;
    private int starCount;
    private int allCount;

    // 记录该文feed什么时候被取消订阅。0为已订阅
    private long state = 0;

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


    public String getFeedUrl() {
        return this.feedUrl;
    }


    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }


    public String getHtmlUrl() {
        return this.htmlUrl;
    }


    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }


    public String getIconUrl() {
        return this.iconUrl;
    }


    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    // 0->rss, 1->readability, 2->link
    public int getDisplayMode() {
        return this.displayMode;
    }


    public void setDisplayMode(int displayMode) {
        this.displayMode = displayMode;
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


    public long getState() {
        return this.state;
    }


    public void setState(long state) {
        this.state = state;
    }

}
