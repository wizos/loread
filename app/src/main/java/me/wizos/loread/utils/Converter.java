/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-02-09 05:56:01
 */

package me.wizos.loread.utils;

import android.text.TextUtils;

import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndLink;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.bean.Enclosure;
import me.wizos.loread.bean.FeedEntries;
import me.wizos.loread.bean.SearchFeed;
import me.wizos.loread.bean.feedly.CategoryItem;
import me.wizos.loread.bean.feedly.Entry;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.bean.inoreader.itemContents.Item;
import me.wizos.loread.bean.jsonfeed.JsonFeed;
import me.wizos.loread.bean.jsonfeed.JsonItem;
import me.wizos.loread.bean.rssfinder.RSSFinderFeed;
import me.wizos.loread.bean.ttrss.result.ArticleItem;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.FeedCategory;

/**
 * 用于将各个渠道获取到的文章转换为本地的文章格式
 */
public class Converter {
    /**
     * 将 InoReader 的文章转为本地文章格式
     */
    public static Article from(Item item, ArticleConvertListener convertListener){
        Article article = new Article();
        // 返回的字段
        article.setId(item.getId());
        article.setAuthor(item.getAuthor());
        article.setPubDate(item.getPublished() * 1000);

        if (item.getCanonical() != null && item.getCanonical().size() > 0) {
            article.setLink(item.getCanonical().get(0).getHref());
        }
        if (item.getOrigin() != null) {
            article.setFeedId(item.getOrigin().getStreamId());
            article.setFeedTitle(item.getOrigin().getTitle());
        }

        String tmpContent = ArticleUtils.getOptimizedContent(article.getLink(), item.getSummary().getContent());
        tmpContent = ArticleUtils.getOptimizedContentWithEnclosures(tmpContent, item.getEnclosure());
        article.setContent(tmpContent);

        article.setSummary(ArticleUtils.getOptimizedSummary(tmpContent));

        article.setTitle(ArticleUtils.getOptimizedTitle(item.getTitle(), article.getSummary()));

        article.setImage(ArticleUtils.getCoverUrl(article.getLink(), article.getContent()));

        // 自己设置的字段
        article.setSaveStatus(App.STATUS_NOT_FILED);
        if (convertListener != null) {
            convertListener.onEnd(article);
        }
        return article;
    }

    /**
     * 将 Feedly 的文章转为本地文章格式
     */
    public static Article from(Entry item, ArticleConvertListener convertListener) {
        Article article = new Article();

        article.setId(item.getId());
        article.setAuthor(item.getAuthor());
        article.setPubDate(item.getPublished());

        if (item.getCanonical() != null && item.getCanonical().size() > 0) {
            article.setLink(item.getCanonical().get(0).getHref());
        }
        if (item.getOrigin() != null) {
            article.setFeedId(item.getOrigin().getStreamId());
            article.setFeedTitle(item.getOrigin().getTitle());
        }

        String tmpContent = "";
        if (item.getContent() != null && !TextUtils.isEmpty(item.getContent().getContent())) {
            tmpContent = ArticleUtils.getOptimizedContent(article.getLink(), item.getContent().getContent());
        } else if (item.getSummary() != null && !TextUtils.isEmpty(item.getSummary().getContent())) {
            tmpContent = ArticleUtils.getOptimizedContent(article.getLink(), item.getSummary().getContent());
        }
        tmpContent = ArticleUtils.getOptimizedContentWithEnclosures(tmpContent, item.getEnclosure());
        article.setContent(tmpContent);

        article.setSummary(ArticleUtils.getOptimizedSummary(tmpContent));

        article.setTitle(ArticleUtils.getOptimizedTitle(item.getTitle(), article.getSummary()));

        article.setImage(ArticleUtils.getCoverUrl(article.getLink(), article.getContent()));

        // 自己设置的字段
        // KLog.i("【增加文章】" + article.getId());
        article.setSaveStatus(App.STATUS_NOT_FILED);
        if (convertListener != null) {
            convertListener.onEnd(article);
        }
        return article;
    }

    /**
     * 将 Fever 的文章转为本地文章格式
     */
    public static Article from(me.wizos.loread.bean.fever.Item item, ArticleConvertListener convertListener) {
        Article article = new Article();
        article.setId(String.valueOf(item.getId()));
        article.setAuthor(item.getAuthor());
        article.setPubDate(item.getCreatedOnTime() * 1000);

        article.setLink(item.getUrl());
        article.setFeedId(String.valueOf(item.getFeedId()));
        article.setFeedTitle(item.getAuthor());

        article.setContent(ArticleUtils.getOptimizedContent(item.getUrl(), item.getHtml()));

        article.setSummary(ArticleUtils.getOptimizedSummary(article.getContent()));

        article.setTitle(ArticleUtils.getOptimizedTitle(item.getTitle(),article.getSummary()));

        article.setImage(ArticleUtils.getCoverUrl(article.getLink(), article.getContent()));

        // 自己设置的字段
        if (item.getIsRead() == 0) {
            article.setReadStatus(App.STATUS_UNREAD);
        } else {
            article.setReadStatus(App.STATUS_READED);
        }
        if (item.getIsSaved() == 1) {
            article.setStarStatus(App.STATUS_STARED);
        } else {
            article.setStarStatus(App.STATUS_UNSTAR);
        }

        article.setSaveStatus(App.STATUS_NOT_FILED);
        if (convertListener != null) {
            convertListener.onEnd(article);
        }
        return article;
    }

    /**
     * 将 TinyTinyRSS 的文章转为本地文章格式
     */
    public static Article from(ArticleItem item, ArticleConvertListener convertListener) {
        Article article = new Article();

        article.setId(String.valueOf(item.getId()));
        article.setAuthor(item.getAuthor());
        article.setPubDate(item.getUpdated() * 1000);

        article.setLink(item.getLink());
        article.setFeedId(item.getFeed_id());
        article.setFeedTitle(item.getFeed_title());

        String tmpContent = ArticleUtils.getOptimizedContent(item.getLink(), item.getContent());
        tmpContent = ArticleUtils.getOptimizedContentWithEnclosures(tmpContent, item.getAttachments());
        article.setContent(tmpContent);

        article.setSummary(ArticleUtils.getOptimizedSummary(article.getContent()));

        article.setTitle(ArticleUtils.getOptimizedTitle(item.getTitle(), article.getSummary()));

        article.setImage(ArticleUtils.getCoverUrl(article.getLink(), article.getContent()));

        // 自己设置的字段
        article.setSaveStatus(App.STATUS_NOT_FILED);
        if (item.isUnread()) {
            article.setReadStatus(App.STATUS_UNREAD);
        } else {
            article.setReadStatus(App.STATUS_READED);
        }
        if (item.isMarked()) {
            article.setStarStatus(App.STATUS_STARED);
        } else {
            article.setStarStatus(App.STATUS_UNSTAR);
        }

        if (convertListener != null) {
            convertListener.onEnd(article);
        }
        return article;
    }

    /**
     * 将 SyndEntry 的文章转为本地文章格式
     */
    public static Article from(String uid, String feedId, String feedTitle, SyndEntry item){
        Article article = new Article();

        article.setUid(uid);
        article.setAuthor(item.getAuthor());
        if(item.getPublishedDate() != null){
            article.setPubDate(item.getPublishedDate().getTime());
        }

        article.setLink(item.getLink());
        article.setFeedId(feedId);
        article.setFeedTitle(feedTitle);

        if(item.getContents() != null && item.getContents().size() > 0 && !StringUtils.isEmpty(item.getContents().get(0).getValue())){
            article.setContent(ArticleUtils.getOptimizedContent(item.getLink(), item.getContents().get(0).getValue()));
        }else if(item.getDescription() != null && !StringUtils.isEmpty(item.getDescription().getValue())){
            article.setContent(ArticleUtils.getOptimizedContent(item.getLink(), item.getDescription().getValue()));
        }

        List<Enclosure> enclosures = new ArrayList<>();
        for(SyndEnclosure syndEnclosure: item.getEnclosures()){
            enclosures.add(Converter.from(syndEnclosure));
        }
        article.setContent( ArticleUtils.getOptimizedContentWithEnclosures(article.getContent(), enclosures) );

        article.setSummary(ArticleUtils.getOptimizedSummary(article.getContent()));

        article.setTitle(ArticleUtils.getOptimizedTitle(item.getTitle(), article.getSummary()));

        article.setImage(ArticleUtils.getCoverUrl(article.getLink(), article.getContent()));

        if(!StringUtils.isEmpty(article.getLink())) {
            article.setId(Objects.requireNonNull(EncryptUtils.MD5(article.getLink())));
        }else if(!StringUtils.isEmpty(article.getTitle())){
            article.setId(Objects.requireNonNull(EncryptUtils.MD5(article.getTitle())));
        }else if(!StringUtils.isEmpty(article.getSummary())){
            article.setId(Objects.requireNonNull(EncryptUtils.MD5(article.getSummary())));
        }else if(!StringUtils.isEmpty(article.getContent())){
            article.setId(Objects.requireNonNull(EncryptUtils.MD5(article.getContent())));
        }
        article.setSaveStatus(App.STATUS_NOT_FILED);
        article.setReadStatus(App.STATUS_UNREAD);
        article.setStarStatus(App.STATUS_UNSTAR);
        return article;
    }

    public static Enclosure from(SyndEnclosure syndEnclosure){
        Enclosure enclosure = new Enclosure();
        enclosure.setType(syndEnclosure.getType());
        enclosure.setHref(syndEnclosure.getUrl());
        enclosure.setLength(syndEnclosure.getLength());
        return enclosure;
    }


    /**
     * 将 JsonFeed 的文章转为本地文章格式
     */
    public static Article from(String uid, String feedId, String feedTitle, JsonItem item){
        Article article = new Article();

        article.setUid(uid);
        if(item.getAuthor()!=null){
            article.setAuthor(item.getAuthor().getName());
        }
        if(item.getDatePublished() != null){
            article.setPubDate(item.getDatePublished().toEpochMilli());
        }

        article.setLink(item.getUrl());
        article.setFeedId(feedId);
        article.setFeedTitle(feedTitle);

        if(!StringUtils.isEmpty(item.getContentHtml())){
            article.setContent(ArticleUtils.getOptimizedContent(item.getUrl(), item.getContentHtml()));
        }else if(!StringUtils.isEmpty(item.getContentText())){
            article.setContent(ArticleUtils.getOptimizedContent(item.getUrl(), item.getContentText()));
        }else if(!StringUtils.isEmpty(item.getSummary())){
            article.setContent(ArticleUtils.getOptimizedContent(item.getUrl(), item.getSummary()));
        }

        article.setSummary(ArticleUtils.getOptimizedSummary(article.getContent()));

        article.setTitle(ArticleUtils.getOptimizedTitle(item.getTitle(), article.getSummary()));

        article.setImage(ArticleUtils.getCoverUrl(article.getLink(), article.getContent()));

        if(!StringUtils.isEmpty(item.getId())){
            article.setId(Objects.requireNonNull(EncryptUtils.MD5(item.getId())));
        }else if(!StringUtils.isEmpty(article.getLink())) {
            article.setId(Objects.requireNonNull(EncryptUtils.MD5(article.getLink())));
        }else if(!StringUtils.isEmpty(article.getTitle())){
            article.setId(Objects.requireNonNull(EncryptUtils.MD5(article.getTitle())));
        }else if(!StringUtils.isEmpty(article.getSummary())){
            article.setId(Objects.requireNonNull(EncryptUtils.MD5(article.getSummary())));
        }else if(!StringUtils.isEmpty(article.getContent())){
            article.setId(Objects.requireNonNull(EncryptUtils.MD5(article.getContent())));
        }
        article.setSaveStatus(App.STATUS_NOT_FILED);
        article.setReadStatus(App.STATUS_UNREAD);
        article.setStarStatus(App.STATUS_UNSTAR);
        return article;
    }


    public static SearchFeed from(RSSFinderFeed feed) {
        SearchFeed searchFeed = new SearchFeed();
        searchFeed.setTitle(feed.getTitle());
        searchFeed.setFeedUrl(feed.getLink());
        return searchFeed;
    }
    public static SearchFeed from(Feed feed){
        SearchFeed searchFeed = new SearchFeed();
        searchFeed.setTitle(feed.getTitle());
        searchFeed.setFeedUrl(feed.getFeedUrl());
        searchFeed.setIconUrl(feed.getIconUrl());
        searchFeed.setSiteUrl(feed.getHtmlUrl());
        return searchFeed;
    }

    public static Feed from(SearchFeed searchFeed){
        Feed feed = new Feed();
        feed.setId(EncryptUtils.MD5(searchFeed.getFeedUrl()));
        feed.setTitle(searchFeed.getTitle());
        feed.setFeedUrl(searchFeed.getFeedUrl());
        feed.setIconUrl(searchFeed.getIconUrl());
        feed.setHtmlUrl(searchFeed.getSiteUrl());
        return feed;
    }

    public static EditFeed from(FeedEntries feedEntries){
        EditFeed editFeed = new EditFeed();
        editFeed.setId(Contract.SCHEMA_FEED + feedEntries.getFeed().getFeedUrl());
        editFeed.setTitle(feedEntries.getFeed().getTitle());
        ArrayList<CategoryItem> categoryItems = new ArrayList<>();
        if(feedEntries.getFeedCategories() != null){
            for (FeedCategory feedCategory: feedEntries.getFeedCategories()){
                CategoryItem categoryItem = new CategoryItem();
                categoryItem.setId(feedCategory.getCategoryId());
                categoryItems.add(categoryItem);
            }
        }
        editFeed.setCategoryItems(categoryItems);
        return editFeed;
    }


    public static Feed updateFrom(Feed localFeed, SyndFeed remoteFeed){
        localFeed.setTitle(remoteFeed.getTitle());

        if(!StringUtils.isEmpty(remoteFeed.getLink())){
            localFeed.setHtmlUrl(remoteFeed.getLink());
        }else if(null != remoteFeed.getLinks() && remoteFeed.getLinks().size() > 0){
            for (SyndLink link: remoteFeed.getLinks()){
                if("alternate".equalsIgnoreCase(link.getRel()) && (link.getType().contains("html") || link.getType().contains("text")) ){
                    localFeed.setHtmlUrl(link.getHref());
                    break;
                }
            }
        }

        if (null != remoteFeed.getIcon() && !remoteFeed.getIcon().getUrl().startsWith("data:") ){
            localFeed.setIconUrl(remoteFeed.getIcon().getUrl());
        }
        return localFeed;
    }

    public static Feed updateFrom(Feed localFeed, JsonFeed remoteFeed){
        localFeed.setTitle(remoteFeed.getTitle());
        localFeed.setFeedUrl(remoteFeed.getFeedUrl());
        localFeed.setHtmlUrl(remoteFeed.getHomePageUrl());
        if (null != remoteFeed.getIcon() && !remoteFeed.getIcon().startsWith("data:") ){
            localFeed.setIconUrl(remoteFeed.getIcon());
        }
        return localFeed;
    }




    public interface ArticleConvertListener {
        Article onEnd(Article article);
    }
}

