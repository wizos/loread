package me.wizos.loread.bean.fever;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Feeds extends FeverResponse {
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

    @NotNull
    @Override
    public String toString() {
        return "Feeds{" +
                "feeds=" + feeds +
                ", feedsGroups=" + feedsGroups +
                '}';
    }
}
