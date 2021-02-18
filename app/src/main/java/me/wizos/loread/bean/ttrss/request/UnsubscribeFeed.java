package me.wizos.loread.bean.ttrss.request;

import com.google.gson.annotations.SerializedName;

public class UnsubscribeFeed {
    private String sid;
    private String op = "unsubscribeFeed";
    @SerializedName("feed_id")
    private int feedId;

    public UnsubscribeFeed(String sid) {
        this.sid = sid;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public int getFeedId() {
        return feedId;
    }

    public void setFeedId(int feedId) {
        this.feedId = feedId;
    }

    @Override
    public String toString() {
        return "UnsubscribeFeed{" +
                "sid='" + sid + '\'' +
                ", op='" + op + '\'' +
                ", feedId=" + feedId +
                '}';
    }
}
