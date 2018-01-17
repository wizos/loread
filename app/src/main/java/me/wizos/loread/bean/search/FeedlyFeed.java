package me.wizos.loread.bean.search;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Wizos on 2017/12/31.
 */

public class FeedlyFeed {

    @SerializedName("title")
    String title;

    @SerializedName("feedId")
    String feedId;

    @SerializedName("website")
    String website;

//    @SerializedName("iconUrl")
//    String iconUrl;

    @SerializedName("description")
    String description;

    @SerializedName("subscribers")
    int subscribers;

    @SerializedName("lastUpdated")
    long lastUpdated;

    @SerializedName("score")
    String score;

    @SerializedName("velocity") // 每周发布的文章的平均数量。 此号码每隔几天更新一次。
            String velocity;

    @SerializedName("coverage")
    String coverage;

    @SerializedName("coverageScore")
    String coverageScore;

    @SerializedName("visualUrl") // 此供稿的图标网址。
            String visualUrl;

    @SerializedName("deliciousTags")
    ArrayList<String> deliciousTags;

    @SerializedName("scheme")
    String scheme;

    @SerializedName("contentType")
    String contentType;

    @SerializedName("language")
    String language;

    @SerializedName("partial")
    boolean partial;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

//    public String getIconUrl() {
//        return iconUrl;
//    }
//
//    public void setIconUrl(String iconUrl) {
//        this.iconUrl = iconUrl;
//    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(int subscribers) {
        this.subscribers = subscribers;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getVelocity() {
        return velocity;
    }

    public void setVelocity(String velocity) {
        this.velocity = velocity;
    }

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public String getCoverageScore() {
        return coverageScore;
    }

    public void setCoverageScore(String coverageScore) {
        this.coverageScore = coverageScore;
    }

    public String getVisualUrl() {
        return visualUrl;
    }

    public void setVisualUrl(String visualUrl) {
        this.visualUrl = visualUrl;
    }

    public ArrayList<String> getDeliciousTags() {
        return deliciousTags;
    }

    public void setDeliciousTags(ArrayList<String> deliciousTags) {
        this.deliciousTags = deliciousTags;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isPartial() {
        return partial;
    }

    public void setPartial(boolean partial) {
        this.partial = partial;
    }
}
