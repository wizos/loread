package me.wizos.loread.bean.fever;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 超级组 Kindling 不在该响应中，它由所有 is_spark = 0 的 feed 组成
 * 超级组 Sparks 不在该响应中，它由所有 is_spark = 1 的 feed 组成
 */

public class Groups extends BaseResponse {
    @SerializedName("groups")
    private List<Group> groups;

    @SerializedName("feeds_groups")
    private List<GroupFeeds> feedsGroups;

    public List<Group> getGroups() {
        return groups;
    }

    public List<GroupFeeds> getFeedsGroups() {
        return feedsGroups;
    }

}
