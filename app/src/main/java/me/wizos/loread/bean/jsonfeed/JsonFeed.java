/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-02-09 05:06:46
 */

package me.wizos.loread.bean.jsonfeed;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JsonFeed {
    String version = "https://jsonfeed.org/version/1";
    String title;
    @SerializedName(value = "home_page_url")
    String homePageUrl;
    @SerializedName(value = "feed_url")
    String feedUrl;
    String description;
    @SerializedName(value = "user_comment")
    String userComment;
    @SerializedName(value = "next_url")
    String nextUrl;
    String icon;
    String favicon;
    Author author;
    Boolean expired;
    List<Hub> hubs;
    List<JsonItem> items;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHomePageUrl() {
        return homePageUrl;
    }

    public void setHomePageUrl(String homePageUrl) {
        this.homePageUrl = homePageUrl;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserComment() {
        return userComment;
    }

    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public void setNextUrl(String nextUrl) {
        this.nextUrl = nextUrl;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getFavicon() {
        return favicon;
    }

    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Boolean getExpired() {
        return expired;
    }

    public void setExpired(Boolean expired) {
        this.expired = expired;
    }

    public List<Hub> getHubs() {
        return hubs;
    }

    public void setHubs(List<Hub> hubs) {
        this.hubs = hubs;
    }

    public List<JsonItem> getItems() {
        return items;
    }

    public void setItems(List<JsonItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "JsonFeed{" +
                "version='" + version + '\'' +
                ", title='" + title + '\'' +
                ", homePageUrl='" + homePageUrl + '\'' +
                ", feedUrl='" + feedUrl + '\'' +
                ", description='" + description + '\'' +
                ", userComment='" + userComment + '\'' +
                ", nextUrl='" + nextUrl + '\'' +
                ", icon='" + icon + '\'' +
                ", favicon='" + favicon + '\'' +
                ", author=" + author +
                ", expired=" + expired +
                ", hubs=" + hubs +
                ", items=" + items +
                '}';
    }
}
