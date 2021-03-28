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
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FeedParserUtils {
    public static FeedEntries parseResponseBody2(Context context, Feed feed, ResponseBody responseBody) throws IOException {
        if(responseBody == null){
            return null;
        }
        FeedEntries feedEntries = new FeedEntries();
        feedEntries.setFeed(feed);

        Charset charset;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(StandardCharsets.UTF_8);
        }else {
            charset = StandardCharsets.UTF_8;
        }

        String content = responseBody.string();
        if (StringUtils.isEmpty(content)) {
            feedEntries.setSuccess(false);
            feedEntries.getFeed().setLastSyncError(context.getString(R.string.content_of_feed_is_empty));
            feedEntries.getFeed().setLastErrorCount(feedEntries.getFeed().getLastErrorCount());
            return feedEntries;
        }

        content = content.trim();

        try {
            if(content.startsWith("<?xml version") || content.startsWith("<rss")){
                SyndFeed xmlFeed = new SyndFeedInput().build(new XmlReader(new ByteArrayInputStream(content.getBytes(charset))));
                feedEntries.setSuccess(true);
                feedEntries.getFeed().setLastSyncError(null);
                feedEntries.getFeed().setLastErrorCount(0);
                feedEntries.from(feed, xmlFeed);
            }else if(content.startsWith("{")){
                JsonFeed jsonFeed = parseJsonFeed(content);
                feedEntries.setSuccess(true);
                feedEntries.getFeed().setLastSyncError(null);
                feedEntries.getFeed().setLastErrorCount(0);
                feedEntries.from(feed, jsonFeed);
            }
        }catch (ParsingFeedException e){
            try {
                SyndFeed xmlFeed = new SyndFeedInput().build(new XmlReader(new ByteArrayInputStream(StringUtils.keepValidXMLChars(content).getBytes(charset))));
                feedEntries.setSuccess(true);
                feedEntries.getFeed().setLastSyncError(null);
                feedEntries.getFeed().setLastErrorCount(0);
                feedEntries.from(feed, xmlFeed);
            }catch (Exception e2){
                XLog.w("解析异常：" + e.getLocalizedMessage());
                Tool.printCallStack(e);
                e.printStackTrace();
                feedEntries.setSuccess(false);
                feedEntries.getFeed().setLastSyncError(e.getLocalizedMessage());
                feedEntries.getFeed().setLastErrorCount(feedEntries.getFeed().getLastErrorCount());
            }
        }catch (FeedException | IOException e){
            XLog.w("解析异常：" + e.getLocalizedMessage());
            Tool.printCallStack(e);
            e.printStackTrace();
            feedEntries.setSuccess(false);
            feedEntries.getFeed().setLastSyncError(e.getLocalizedMessage());
            feedEntries.getFeed().setLastErrorCount(feedEntries.getFeed().getLastErrorCount());
        }
        return feedEntries;
    }


    public static FeedEntries parseResponseBody(Context context, Feed feed, Response response) throws IOException {
        if(response == null){
            return null;
        }
        FeedEntries feedEntries = new FeedEntries();
        feedEntries.setFeed(feed);

        Charset charset;
        String content;

        ResponseBody responseBody = response.body();
        if(responseBody == null){
            return null;
        }

        // MediaType contentType = responseBody.contentType();
        // if (contentType != null) {
        //     charset = contentType.charset(StandardCharsets.UTF_8);
        // }else {
        //     charset = StandardCharsets.UTF_8;
        // }
        // content = responseBody.string();

        // 先将 inputStream 缓存起来
        InputStreamCache inputStreamCache = new InputStreamCache(responseBody.byteStream());


        content = inputStreamCache.getSting();


        // try {
        //     content = responseBody.string();
        // }catch (IOException e){
        //     XLog.e("读取响应失败：" + feed);
        //     responseBody.close();
        //     response.close();
        //     e.printStackTrace();
        //     return null;
        // }

        if (StringUtils.isEmpty(content)) {
            feedEntries.setSuccess(false);
            feedEntries.getFeed().setLastSyncError(context.getString(R.string.content_of_feed_is_empty));
            feedEntries.getFeed().setLastErrorCount(feedEntries.getFeed().getLastErrorCount());
            return feedEntries;
        }

        content = content.trim();
        try {
            if(content.startsWith("<?xml version") || content.startsWith("<rss")){
                SyndFeed xmlFeed = new SyndFeedInput().build(new XmlReader(inputStreamCache.getInputStream()));
                feedEntries.setSuccess(true);
                feedEntries.getFeed().setLastSyncError(null);
                feedEntries.getFeed().setLastErrorCount(0);
                feedEntries.from(feed, xmlFeed);
            }else if(content.startsWith("{")){
                JsonFeed jsonFeed = parseJsonFeed(content);
                feedEntries.setSuccess(true);
                feedEntries.getFeed().setLastSyncError(null);
                feedEntries.getFeed().setLastErrorCount(0);
                feedEntries.from(feed, jsonFeed);
            }
        }catch (ParsingFeedException e){
            try {
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(StandardCharsets.UTF_8);
                } else {
                    // charset = StandardCharsets.UTF_8;
                    charset = getXMLCharset(inputStreamCache);
                }
                if(!StandardCharsets.UTF_8.displayName().equalsIgnoreCase(charset.displayName())){
                    content = inputStreamCache.getSting(charset);
                }

                SyndFeed xmlFeed = new SyndFeedInput().build(new XmlReader(new ByteArrayInputStream(StringUtils.keepValidXMLChars(content).getBytes(charset))));
                feedEntries.setSuccess(true);
                feedEntries.getFeed().setLastSyncError(null);
                feedEntries.getFeed().setLastErrorCount(0);
                feedEntries.from(feed, xmlFeed);
            }catch (Exception e2){
                XLog.w("解析异常：" + e.getLocalizedMessage());
                Tool.printCallStack(e);
                e.printStackTrace();
                feedEntries.setSuccess(false);
                feedEntries.getFeed().setLastSyncError(e.getLocalizedMessage());
                feedEntries.getFeed().setLastErrorCount(feedEntries.getFeed().getLastErrorCount());
            }
        }catch (FeedException | IOException e){
            XLog.w("解析异常：" + e.getLocalizedMessage());
            Tool.printCallStack(e);
            e.printStackTrace();
            feedEntries.setSuccess(false);
            feedEntries.getFeed().setLastSyncError(e.getLocalizedMessage());
            feedEntries.getFeed().setLastErrorCount(feedEntries.getFeed().getLastErrorCount());
        }
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
