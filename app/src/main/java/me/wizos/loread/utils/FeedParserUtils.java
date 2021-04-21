/*
 * Copyright (c) 2021 wizos
 * 项目：Loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-02-06 04:21:06
 */

package me.wizos.loread.utils;

import android.content.Context;

import com.elvishew.xlog.XLog;
import com.google.gson.Gson;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.ParsingFeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.wizos.loread.R;
import me.wizos.loread.bean.FeedEntries;
import me.wizos.loread.bean.jsonfeed.JsonFeed;
import me.wizos.loread.db.Feed;

public class FeedParserUtils {
    public static final String UTF8_BOM = "\uFEFF";
    //
    // public static FeedEntries parseResponseBody(Context context, Feed feed, ResponseBody responseBody, Converter.ArticleConvertListener convertListener) {
    //     if(responseBody == null){
    //         return null;
    //     }
    //     FeedEntries feedEntries = new FeedEntries();
    //     feedEntries.setFeed(feed);
    //
    //     Charset charset;
    //     String content;
    //
    //     // 先将 inputStream 缓存起来
    //     InputStreamCache inputStreamCache = new InputStreamCache(responseBody.byteStream());
    //
    //     content = inputStreamCache.getSting();
    //
    //     if (StringUtils.isEmpty(content)) {
    //         feedEntries.setSuccess(false);
    //         feedEntries.getFeed().setLastSyncError(context.getString(R.string.content_of_feed_is_empty));
    //         feedEntries.getFeed().setLastErrorCount(feedEntries.getFeed().getLastErrorCount());
    //         feedEntries.getFeed().setLastSyncTime(System.currentTimeMillis());
    //         return feedEntries;
    //     }
    //
    //     content = content.trim();
    //     if (content.startsWith(UTF8_BOM)) {
    //         content = content.substring(1);
    //     }
    //     try {
    //         if(content.startsWith("<")){ // content.startsWith("<?xml version") || content.startsWith("<rss") content.startsWith("<feed")
    //             SyndFeed xmlFeed = new SyndFeedInput().build(new XmlReader(inputStreamCache.getInputStream()));
    //             feedEntries.setSuccess(true);
    //             feedEntries.getFeed().setLastSyncError(null);
    //             feedEntries.getFeed().setLastErrorCount(0);
    //             feedEntries.getFeed().setLastSyncTime(System.currentTimeMillis());
    //             feedEntries.from(feed, xmlFeed, convertListener);
    //         }else if(content.startsWith("{")){
    //             JsonFeed jsonFeed = parseJsonFeed(content);
    //             feedEntries.setSuccess(true);
    //             feedEntries.getFeed().setLastSyncError(null);
    //             feedEntries.getFeed().setLastErrorCount(0);
    //             feedEntries.getFeed().setLastSyncTime(System.currentTimeMillis());
    //             feedEntries.from(feed, jsonFeed, convertListener);
    //         }else {
    //             SyndFeed xmlFeed = new SyndFeedInput().build(new XmlReader(inputStreamCache.getInputStream()));
    //             feedEntries.setSuccess(true);
    //             feedEntries.getFeed().setLastSyncError(null);
    //             feedEntries.getFeed().setLastErrorCount(0);
    //             feedEntries.getFeed().setLastSyncTime(System.currentTimeMillis());
    //             feedEntries.from(feed, xmlFeed, convertListener);
    //         }
    //     }catch (ParsingFeedException e){
    //         try {
    //             MediaType contentType = responseBody.contentType();
    //             if (contentType != null) {
    //                 charset = contentType.charset(StandardCharsets.UTF_8);
    //             } else {
    //                 charset = getXMLCharset(inputStreamCache);
    //             }
    //             if(!StandardCharsets.UTF_8.displayName().equalsIgnoreCase(charset.displayName())){
    //                 content = inputStreamCache.getSting(charset);
    //             }
    //
    //             SyndFeed xmlFeed = new SyndFeedInput().build(new XmlReader(new ByteArrayInputStream(StringUtils.keepValidXMLChars(content).getBytes(charset))));
    //             feedEntries.setSuccess(true);
    //             feedEntries.getFeed().setLastSyncError(null);
    //             feedEntries.getFeed().setLastErrorCount(0);
    //             feedEntries.getFeed().setLastSyncTime(System.currentTimeMillis());
    //             feedEntries.from(feed, xmlFeed, convertListener);
    //         }catch (Exception e2){
    //             XLog.w("解析异常：" + e.getLocalizedMessage() + " => " + feed);
    //             Tool.printCallStack(e);
    //             e.printStackTrace();
    //             feedEntries.setSuccess(false);
    //             feedEntries.getFeed().setLastSyncError(e.getLocalizedMessage());
    //             feedEntries.getFeed().setLastErrorCount(feedEntries.getFeed().getLastErrorCount());
    //             feedEntries.getFeed().setLastSyncTime(System.currentTimeMillis());
    //         }
    //     }catch (FeedException | IOException e){
    //         XLog.w("解析异常：" + e.getLocalizedMessage() + " => " + feed);
    //         Tool.printCallStack(e);
    //         e.printStackTrace();
    //         feedEntries.setSuccess(false);
    //         feedEntries.getFeed().setLastSyncError(e.getLocalizedMessage());
    //         feedEntries.getFeed().setLastErrorCount(feedEntries.getFeed().getLastErrorCount());
    //         feedEntries.getFeed().setLastSyncTime(System.currentTimeMillis());
    //     }
    //     inputStreamCache.destroyCache();
    //     return feedEntries;
    // }




    public static FeedEntries parseInputSteam(Context context, Feed feed, InputStreamCache inputStreamCache, Converter.ArticleConvertListener convertListener) {
        FeedEntries feedEntries = new FeedEntries();
        feedEntries.setFeed(feed);
        feed.setLastSyncError(context.getString(R.string.content_of_feed_unable_to_get));
        if(inputStreamCache == null){
            return null;
        }

        Charset charset;
        String content;


        content = inputStreamCache.getSting();

        if (StringUtils.isEmpty(content)) {
            feedEntries.setSuccess(false);
            feedEntries.getFeed().setLastSyncError(context.getString(R.string.content_of_feed_is_empty));
            return feedEntries;
        }

        content = content.trim();
        if (content.startsWith(UTF8_BOM)) {
            content = content.substring(1);
        }
        try {
            if(content.startsWith("<?x") || content.startsWith("<rss") || content.startsWith("<feed")){
                SyndFeed xmlFeed = new SyndFeedInput().build(new XmlReader(inputStreamCache.getInputStream()));
                feedEntries.setSuccess(true);
                feedEntries.getFeed().setLastSyncError(null);
                feedEntries.getFeed().setLastErrorCount(0);
                feedEntries.from(feed, xmlFeed, convertListener);
            }else if(content.startsWith("{")){
                JsonFeed jsonFeed = parseJsonFeed(content);
                feedEntries.setSuccess(true);
                feedEntries.getFeed().setLastSyncError(null);
                feedEntries.getFeed().setLastErrorCount(0);
                feedEntries.from(feed, jsonFeed, convertListener);
            }else {
                feedEntries.setSuccess(false);
                feedEntries.getFeed().setLastSyncError(context.getString(R.string.content_of_feed_is_not_in_standard));
            }
        }catch (ParsingFeedException e){
            try {
                charset = inputStreamCache.getCharset();
                if(charset == null){
                    charset = getXMLCharset(inputStreamCache);
                }
                if(!StandardCharsets.UTF_8.displayName().equalsIgnoreCase(charset.displayName())){
                    content = inputStreamCache.getSting(charset);
                }

                SyndFeed xmlFeed = new SyndFeedInput().build(new XmlReader(new ByteArrayInputStream(StringUtils.keepValidXMLChars(content).getBytes(charset))));
                feedEntries.setSuccess(true);
                feedEntries.getFeed().setLastSyncError(null);
                feedEntries.getFeed().setLastErrorCount(0);
                feedEntries.from(feed, xmlFeed, convertListener);
            }catch (Exception e2){
                XLog.w("解析异常：" + e.getLocalizedMessage() + " => " + feed);
                XLog.w("异常原文：" + inputStreamCache.getCharset() + " -> " + content);
                Tool.printCallStack(e);
                e.printStackTrace();
                feedEntries.setSuccess(false);
                feedEntries.getFeed().setLastSyncError(e.getLocalizedMessage());
            }
        }catch (FeedException | IOException e){
            XLog.w("解析异常：" + e.getLocalizedMessage() + " => " + feed);
            Tool.printCallStack(e);
            e.printStackTrace();
            feedEntries.setSuccess(false);
        }

        feedEntries.getFeed().setLastSyncTime(System.currentTimeMillis());
        inputStreamCache.destroyCache();
        return feedEntries;
    }


    private static Pattern pattern = Pattern.compile("encoding=[\"'](\\S+)[\"']");
    public static Charset getXMLCharset(InputStreamCache inputStreamCache){
        Matcher matcher = pattern.matcher(inputStreamCache.getSting());
        if(matcher.find()){
            return Charset.forName(matcher.group(1));
        }
        return StandardCharsets.UTF_8;
    }


    public static JsonFeed parseJsonFeed(String jsonText){
        return new Gson().fromJson(jsonText, JsonFeed.class);
    }
}
