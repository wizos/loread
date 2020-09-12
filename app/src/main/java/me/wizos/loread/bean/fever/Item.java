package me.wizos.loread.bean.fever;

import com.google.gson.annotations.SerializedName;

public class Item {
    @SerializedName("id")
    private int id;
    @SerializedName("feed_id")
    private int feedId;
    @SerializedName("title")
    private String title;
    @SerializedName("author")
    private String author;
    @SerializedName("html")
    private String html;

    @SerializedName("url")
    private String url;

    @SerializedName("is_saved")
    private int isSaved;
    @SerializedName("is_read")
    private int isRead;

    @SerializedName("created_on_time")
    private long createdOnTime;

    public int getId() {
        return id;
    }

    public int getFeedId() {
        return feedId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getHtml() {
        return html;
    }

    public String getUrl() {
        return url;
    }

    public int getIsSaved() {
        return isSaved;
    }

    public int getIsRead() {
        return isRead;
    }

    public long getCreatedOnTime() {
        return createdOnTime;
    }
}
