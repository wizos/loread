package me.wizos.loread.bean.fever;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

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
    private int isSaved; // 0 = false, 1 = true
    @SerializedName("is_read")
    private int isRead; // 0 = false, 1 = true

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

    @NotNull
    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", feedId=" + feedId +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", html='" + html + '\'' +
                ", url='" + url + '\'' +
                ", isSaved=" + isSaved +
                ", isRead=" + isRead +
                ", createdOnTime=" + createdOnTime +
                '}';
    }
}
