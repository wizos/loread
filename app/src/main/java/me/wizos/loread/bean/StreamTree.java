/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-02-09 10:46:25
 */

package me.wizos.loread.bean;

import java.util.List;

import me.wizos.loread.db.Collection;

public class StreamTree {
    // public static final int ROOT = -2;
    // public static final int UNSUBSCRIBED = -1;
    public static final int SMART = -1;
    public static final int CATEGORY = 0;
    public static final int FEED = 1;

    private String streamId;
    private String streamName;
    private int streamType;
    private int count;
    private List<Collection> children;

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public int getStreamType() {
        return streamType;
    }

    public void setStreamType(int streamType) {
        this.streamType = streamType;
    }

    public List<Collection> getChildren() {
        return children;
    }

    public void setChildren(List<Collection> children) {
        this.children = children;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "StreamTree{" +
                "streamId='" + streamId + '\'' +
                ", streamName='" + streamName + '\'' +
                ", streamType=" + streamType +
                ", count=" + count +
                ", feeds=" + children +
                '}';
    }
}
