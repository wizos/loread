package me.wizos.loread.bean.fever;

import android.util.ArrayMap;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * 超级组 Kindling 不在该响应中，它由所有 is_spark = 0 的 feed 组成
 * 超级组 Sparks 不在该响应中，它由所有 is_spark = 1 的 feed 组成
 */

public class Groups extends FeverResponse {
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

    public Map<Integer,String> getFeedsGroupsMap() {
        ArrayMap<Integer,String> groupFeedsMap;
        if(feedsGroups != null){
            groupFeedsMap = new ArrayMap<>(feedsGroups.size());
            for (GroupFeeds groupFeeds:feedsGroups) {
                groupFeedsMap.put(groupFeeds.getGroupId(),groupFeeds.getFeedIds());
            }
        }else {
            groupFeedsMap = new ArrayMap<>(0);
        }
        return groupFeedsMap;
    }

    @NotNull
    @Override
    public String toString() {
        return "Groups{" +
                "groups=" + groups +
                ", feedsGroups=" + feedsGroups +
                '}';
    }
}
