package me.wizos.loread.bean.feedly;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import me.wizos.loread.App;
import me.wizos.loread.db.Feed;

/**
 * 以搜索知乎，feed/http://zhihurss.miantiao.me/section/id/2 源为例
 * Created by Wizos on 2019/2/8.
 */

public class FeedItem {
    private String id;
    public String feedId;
    public String title;
    private String description;
    private String website;
    private String iconUrl; // 可能为空，小图
    private String visualUrl; // 可能为空，大图
    private String language; // 值可能为：zh，en
    private int subscribers;
    private long updated;
    private float velocity; // 每周发布的文章的平均数量。 此号码每隔几天更新一次
    private boolean partial; // 可能为空；部分的; 偏爱的
    private String contentType; // 可能为空；可能为 article， longform
    private String state; // 可能为空。值可能为：dead.stale，dormant
    private ArrayList<String> topics; // 可能为空

    // 单独获取feed信息时可见(批量接口)
    // private int estimatedEngagement;

    // 搜索时可见
    // private long lastUpdated; // 可能用不到吧，和 Updated 字段类似
    // private float score;
    // private int coverage;
    // private int coverageScore;
    // private int averageReadTime;
    // private String websiteTitle;
    // private int totalTagCount;
    // private ArrayList<> tagCounts;
    // private ArrayList<String> deliciousTags;

    // 以下不常见到
    // String coverColor;
    // String logo;
    // String relatedLayout;
    // String relatedTarget;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getFeedId() {
        return feedId;
    }
    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getWebsite() {
        return website;
    }
    public void setWebsite(String website) {
        this.website = website;
    }
    public String getIconUrl() {
        return iconUrl;
    }
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
    public String getVisualUrl() {
        return visualUrl;
    }
    public void setVisualUrl(String visualUrl) {
        this.visualUrl = visualUrl;
    }
    public String getLanguage() {
        return language;
    }
    public void setLanguage(String language) {
        this.language = language;
    }
    public int getSubscribers() {
        return subscribers;
    }
    public void setSubscribers(int subscribers) {
        this.subscribers = subscribers;
    }
    public long getUpdated() {
        return updated;
    }
    public void setUpdated(long updated) {
        this.updated = updated;
    }
    public float getVelocity() {
        return velocity;
    }
    public void setVelocity(float velocity) {
        this.velocity = velocity;
    }
    public ArrayList<String> getTopics() {
        return topics;
    }
    public void setTopics(ArrayList<String> topics) {
        this.topics = topics;
    }
    public boolean isPartial() {
        return partial;
    }
    public void setPartial(boolean partial) {
        this.partial = partial;
    }
    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }

    public Feed convert2Feed() {
        Feed feed = new Feed();
        feed.setId(id);
        feed.setTitle(title);
        feed.setFeedUrl(id.substring(5));
        feed.setHtmlUrl(website);
        feed.setIconUrl(visualUrl);
        feed.setDisplayMode(App.OPEN_MODE_RSS);
        return feed;
    }

    @NotNull
    @Override
    public String toString() {
        return "TTRSSFeedItem{" +
                "id='" + id + '\'' +
                ", feedId='" + feedId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", website='" + website + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", visualUrl='" + visualUrl + '\'' +
                ", language='" + language + '\'' +
                ", subscribers=" + subscribers +
                ", updated=" + updated +
                ", velocity=" + velocity +
                ", partial=" + partial +
                ", contentType='" + contentType + '\'' +
                ", state='" + state + '\'' +
                ", topics=" + topics +
                '}';
    }
}
