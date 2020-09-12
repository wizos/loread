package me.wizos.loread.bean.search;

import me.wizos.loread.bean.feedly.FeedItem;

/**
 * Created by Wizos on 2017/12/31.
 */

public class SearchFeedItem extends FeedItem {
    private long lastUpdated; // 可能用不到吧，和 Updated 字段类似
    private float score;
    private float coverage;
    private float coverageScore;
    private float averageReadTime;
    private String websiteTitle;
    // private int totalTagCount;
    // private ArrayList<> tagCounts;
    // private ArrayList<String> deliciousTags;

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getCoverage() {
        return coverage;
    }

    public void setCoverage(float coverage) {
        this.coverage = coverage;
    }

    public float getCoverageScore() {
        return coverageScore;
    }

    public void setCoverageScore(float coverageScore) {
        this.coverageScore = coverageScore;
    }

    public float getAverageReadTime() {
        return averageReadTime;
    }

    public void setAverageReadTime(float averageReadTime) {
        this.averageReadTime = averageReadTime;
    }

    public String getWebsiteTitle() {
        return websiteTitle;
    }

    public void setWebsiteTitle(String websiteTitle) {
        this.websiteTitle = websiteTitle;
    }
}
