package me.wizos.loread.bean.ttrss.result;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import me.wizos.loread.App;
import me.wizos.loread.db.Feed;

public class FeedItem {
    @SerializedName("id")
    private int id;
    @SerializedName("title")
    private String title;
    @SerializedName("feed_url")
    private String feedUrl;
    @SerializedName("site_url")
    private String siteUrl;
    @SerializedName("unread")
    private int unread;
    @SerializedName("cat_id")
    private int catId;
    @SerializedName("order_id")
    private int orderId;
    @SerializedName("last_updated")
    private long lastUpdated;
    @SerializedName("has_icon")
    private boolean hasIcon;


    public String getFeedUrl() {
        return feedUrl;
    }
    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }
    public String getSiteUrl() {
        return siteUrl;
    }
    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getUnread() {
        return unread;
    }
    public void setUnread(int unread) {
        this.unread = unread;
    }
    public int getCatId() {
        return catId;
    }
    public void setCatId(int catId) {
        this.catId = catId;
    }
    public int getOrderId() {
        return orderId;
    }
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
    public long getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    public boolean isHasIcon() {
        return hasIcon;
    }
    public void setHasIcon(boolean hasIcon) {
        this.hasIcon = hasIcon;
    }


    public Feed convert() {
        Feed feed = new Feed();
        feed.setId(String.valueOf(id));
        feed.setTitle(title);
        feed.setFeedUrl(feedUrl);
        feed.setHtmlUrl(siteUrl);
        //feed.setIconUrl(visualUrl);
        feed.setDisplayMode(App.OPEN_MODE_RSS);
        return feed;
    }

    @NotNull
    @Override
    public String toString() {
        return "FeedItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", feedUrl='" + feedUrl + '\'' +
                ", siteUrl='" + siteUrl + '\'' +
                ", unread=" + unread +
                ", catId=" + catId +
                ", orderId=" + orderId +
                ", lastUpdated=" + lastUpdated +
                ", hasIcon=" + hasIcon +
                '}';
    }
}
