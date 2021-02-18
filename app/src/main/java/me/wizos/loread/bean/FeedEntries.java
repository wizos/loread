/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-02-06 06:03:03
 */

package me.wizos.loread.bean;


import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.bean.jsonfeed.JsonFeed;
import me.wizos.loread.bean.jsonfeed.JsonItem;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.FeedCategory;
import me.wizos.loread.utils.Converter;

public class FeedEntries {
    boolean success = false;

    Feed feed;
    List<FeedCategory> feedCategories;
    List<Article> articles;


    public void from(Feed feed, SyndFeed remoteFeed) {
        this.feed = Converter.updateFrom(feed, remoteFeed);
        this.articles = new ArrayList<>(remoteFeed.getEntries().size());
        for (SyndEntry entry: remoteFeed.getEntries()){
            articles.add( Converter.from(feed.getUid(), feed.getId(), feed.getTitle(), entry) );
        }
        success = true;
    }

    public void from(Feed feed, JsonFeed remoteFeed) {
        this.feed = Converter.updateFrom(feed, remoteFeed);
        this.articles = new ArrayList<>(remoteFeed.getItems().size());
        for (JsonItem entry: remoteFeed.getItems()){
            articles.add( Converter.from(feed.getUid(), feed.getId(), feed.getTitle(), entry) );
        }
        success = true;
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(Feed feed) {
        this.feed = feed;
    }

    public List<FeedCategory> getFeedCategories() {
        return feedCategories;
    }

    public void setFeedCategories(List<FeedCategory> feedCategories) {
        this.feedCategories = feedCategories;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articleList) {
        this.articles = articleList;
    }


    @NotNull
    @Override
    public String toString() {
        return "FeedEntries{" +
                "success=" + success +
                ", feed=" + feed +
                ", feedCategories=" + feedCategories +
                ", articles=" + articles +
                '}';
    }
}
