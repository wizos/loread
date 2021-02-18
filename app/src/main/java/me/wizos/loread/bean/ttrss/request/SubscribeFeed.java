package me.wizos.loread.bean.ttrss.request;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

public class SubscribeFeed {
    private String sid;
    private String op = "subscribeToFeed";
    @SerializedName("category_id")
    private String categoryId;
    @SerializedName("feed_url")
    private String feedUrl;

    public SubscribeFeed(String sid) {
        this.sid = sid;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    @NotNull
    @Override
    public String toString() {
        return "SubscribeFeed{" +
                "sid='" + sid + '\'' +
                ", op='" + op + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", feedUrl='" + feedUrl + '\'' +
                '}';
    }
}
