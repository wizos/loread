package me.wizos.loread.bean.feedly;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Wizos on 2019/2/8.
 */

public class Origin {
    @SerializedName("streamId")
    String streamId;
    @SerializedName("title")
    String title;
    @SerializedName("htmlUrl")
    String htmlUrl;

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
