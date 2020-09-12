package me.wizos.loread.bean.feedly;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import me.wizos.loread.App;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.Feed;

/**
 * Created by Wizos on 2019/2/8.
 */

public class Collection {
    @SerializedName("id")
    private String id;

    @SerializedName("label")
    private String label;

    @SerializedName("customizable")
    private boolean customizable;

    @SerializedName("enterprise")
    private boolean enterprise;

    @SerializedName("numFeeds")
    private int numFeeds;

    @SerializedName("feeds")
    private ArrayList<FeedItem> feedItems;

    // 可选，大部分时候没有
//    String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isCustomizable() {
        return customizable;
    }

    public void setCustomizable(boolean customizable) {
        this.customizable = customizable;
    }

    public boolean isEnterprise() {
        return enterprise;
    }

    public void setEnterprise(boolean enterprise) {
        this.enterprise = enterprise;
    }

    public int getNumFeeds() {
        return numFeeds;
    }

    public void setNumFeeds(int numFeeds) {
        this.numFeeds = numFeeds;
    }

    public ArrayList<FeedItem> getFeedItems() {
        return feedItems;
    }

    public void setFeedItems(ArrayList<FeedItem> feedItems) {
        this.feedItems = feedItems;
    }


    public CategoryItem getCategoryItem() {
        CategoryItem categoryItem = new CategoryItem();
        categoryItem.setId(id);
        categoryItem.setLabel(label);
        return categoryItem;
    }

    public Category getCategory() {
        Category category = new Category();
        category.setId(id);
        category.setTitle(label);
        return category;
    }

    public ArrayList<Feed> getFeeds() {
        ArrayList<Feed> feeds = new ArrayList<>(feedItems.size());
        Feed feed;
        for (FeedItem feedItem : feedItems) {
            feed = feedItem.convert2Feed();
            feed.setUid(App.i().getUser().getId());
            feeds.add(feed);
        }
        return feeds;
    }
}
