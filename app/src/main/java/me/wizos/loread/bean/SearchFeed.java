package me.wizos.loread.bean;

import com.google.gson.annotations.SerializedName;

public class SearchFeed extends BaseFeed {
    @SerializedName("subscribers")
    int subscribers;

    @SerializedName("velocity")
    float velocity; // 每周发布的文章的平均数量。 此号码每隔几天更新一次

    @SerializedName("last_updated")
    long lastUpdated;

    public SearchFeed() {
    }

    public SearchFeed(String feedUrl, String feedTitle) {
        this.feedUrl = feedUrl;
        this.title = feedTitle;
    }

    public int getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(int subscribers) {
        this.subscribers = subscribers;
    }

    public float getVelocity() {
        return velocity;
    }

    public void setVelocity(float velocity) {
        this.velocity = velocity;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "SearchFeed{" +
                "subscribers=" + subscribers +
                ", velocity=" + velocity +
                ", lastUpdated=" + lastUpdated +
                ", title='" + title + '\'' +
                ", feedUrl='" + feedUrl + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", siteUrl='" + siteUrl + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
