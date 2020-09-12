package me.wizos.loread.bean.inoreader;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import me.wizos.loread.App;
import me.wizos.loread.db.Feed;

/**
 * Created by Wizos on 2016/3/11.
 */
public class Subscription {
    @SerializedName("id")
    private String id;
    @SerializedName("title")
    private String title;
    @SerializedName("categories")
    private ArrayList<SubCategories> categories;
    @SerializedName("sortid")
    private String sortId;
    @SerializedName("firstitemmsec")
    private long firstItemMsec;
    @SerializedName("url")
    private String url;
    @SerializedName("htmlUrl")
    private String htmlUrl;
    @SerializedName("iconUrl")
    private String iconUrl;


    public Feed convert2Feed() {
        Feed feed = new Feed();
        feed.setId(id);
        feed.setTitle(title);
        feed.setFeedUrl(url);
        feed.setHtmlUrl(htmlUrl);
        feed.setIconUrl(iconUrl);
        feed.setDisplayMode(App.OPEN_MODE_RSS);
        return feed;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public ArrayList<SubCategories> getCategories() {
        return categories;
    }
    public void setCategories(ArrayList<SubCategories> categories) {
        this.categories = categories;
    }
    public String getSortId() {
        return sortId;
    }
    public void setSortId(String sortId) {
        this.sortId = sortId;
    }
    public long getFirstItemMsec() {
        return firstItemMsec;
    }
    public void setFirstItemMsec(long firstItemMsec) {
        this.firstItemMsec = firstItemMsec;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getHtmlUrl() {
        return htmlUrl;
    }
    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }
    public String getIconUrl() {
        return iconUrl;
    }
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

}
