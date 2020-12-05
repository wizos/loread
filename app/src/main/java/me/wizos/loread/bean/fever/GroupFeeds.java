package me.wizos.loread.bean.fever;

import com.google.gson.annotations.SerializedName;

public class GroupFeeds {
    @SerializedName("group_id")
    private int groupId;
    @SerializedName("feed_ids")
    private String feedIds;

    public int getGroupId() {
        return groupId;
    }

    public String getFeedIds() {
        return feedIds;
    }

    @Override
    public String toString() {
        return "GroupFeeds{" +
                "groupId=" + groupId +
                ", feedIds='" + feedIds + '\'' +
                '}';
    }
}
