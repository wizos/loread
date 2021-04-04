/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-03-14 11:48:47
 */

package me.wizos.loread.bean.collectiontree;

public class CollectionFeed extends Collection {
    private String feedUrl;
    private String htmlUrl;
    private String iconUrl;
    // 最近同步异常的原因
    private String lastSyncError;
    // 最近同步异常的次数
    private int lastErrorCount;
    // 针对单个 feed 的同步时间间隔；0 代表该值无效，需要使用 user 级别即全局设置的间隔时间，-1  代表禁止更新，正整数代表间隔的分钟
    private int syncInterval;

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getSyncInterval() {
        return syncInterval;
    }

    public void setSyncInterval(int syncInterval) {
        this.syncInterval = syncInterval;
    }

    public String getLastSyncError() {
        return lastSyncError;
    }

    public void setLastSyncError(String lastSyncError) {
        this.lastSyncError = lastSyncError;
    }

    // 大于三次才显示错误
    public int getLastErrorCount() {
        return lastErrorCount;
    }

    public void setLastErrorCount(int lastErrorCount) {
        this.lastErrorCount = lastErrorCount;
    }

    @Override
    public String toString() {
        return "CollectionFeed{" +
                "uid='" + uid + '\'' +
                ", id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", count=" + '\'' +
                ", feedUrl='" + feedUrl + '\'' +
                ", htmlUrl='" + htmlUrl + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", syncInterval='" + syncInterval + '\'' +
                ", lastSyncError='" + lastSyncError + '\'' +
                ", lastErrorCount=" + lastErrorCount +
                '}';
    }
}
