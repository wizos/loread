package me.wizos.loread.bean.fever;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Feeds extends BaseResponse {
   @SerializedName("feeds")
   private List<Feed> feeds;
   @SerializedName("feeds_groups")
   private List<GroupFeeds> feedsGroups;

   public List<Feed> getFeeds() {
       return feeds;
   }

   public List<GroupFeeds> getFeedsGroups() {
       return feedsGroups;
   }

    @Override
    public String toString() {
        return "Feeds{" +
                "feeds=" + feeds +
                ", feedsGroups=" + feedsGroups +
                '}';
    }
}
