package me.wizos.loread.bean;

import com.google.gson.annotations.SerializedName;

public class BaseFeed {
    @SerializedName("title")
    public String title;

    @SerializedName("feed_url")
    public String feedUrl;

    @SerializedName("icon_url")
    public String iconUrl;

    @SerializedName("site_url")
    public String siteUrl;

    @SerializedName("description")
    public String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "BaseFeed{" +
                "title='" + title + '\'' +
                ", feedUrl='" + feedUrl + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", siteUrl='" + siteUrl + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
