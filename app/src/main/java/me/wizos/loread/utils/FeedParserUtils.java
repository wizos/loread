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

import me.wizos.loread.R;
import me.wizos.loread.bean.FeedEntries;
import me.wizos.loread.bean.jsonfeed.JsonFeed;
import me.wizos.loread.db.Feed;
import okhttp3.MediaType;
import okhttp3.ResponseBody;

public class FeedParserUtils {
    public static FeedEntries parseResponseBody(Context context, Feed feed, ResponseBody responseBody) throws IOException {
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
            if(content.startsWith("<?xml version")){
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
            }catch ( Exception e2 ){
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

    public static JsonFeed parseJsonFeed(String jsonText){
        return new Gson().fromJson(jsonText, JsonFeed.class);
    }
}
