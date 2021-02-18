package me.wizos.loread.bean.ttrss.result;

import com.google.gson.annotations.SerializedName;

public class SubscribeFeedResult {
    private int code;
    @SerializedName("feed_id")
    private int feedId;
}
