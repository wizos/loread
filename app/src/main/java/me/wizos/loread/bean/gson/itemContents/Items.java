package me.wizos.loread.bean.gson.itemContents;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Stream content 和 Item content （貌似已被官方弃用）两个api返回指内的文章项
 * Created by Wizos on 2016/3/11.
 */
public class Items {

    @SerializedName("crawlTimeMsec")
    long crawlTimeMsec;

    @SerializedName("timestampUsec")
    long timestampUsec;

    @SerializedName("id")
    String id;

    @SerializedName("categories")
    ArrayList<String> categories;

    @SerializedName("title")
    String title;

    @SerializedName("published")
    long published;

    @SerializedName("updated")
    long updated;

    @SerializedName("starred") // 加星的时间
            long starred;

    // 附件：这个还不知道是什么用处，不过可以显示图片
    @SerializedName("enclosure")
    ArrayList<Enclosure> enclosure;

    @SerializedName("canonical")
    ArrayList<Canonical> canonical;

    @SerializedName("alternate")
    ArrayList<Alternate> alternate;

    @SerializedName("summary")
    Summary summary;

    @SerializedName("author")
    String author;

//这应该是开启了社交后才会有的字段
//            "likingUsers": [],
//             "comments": [],
//             "commentsNum": -1,
//             "annotations": [],

    @SerializedName("origin")
    Origin origin;

    public long getCrawlTimeMsec() {
        return crawlTimeMsec;
    }

    public void setCrawlTimeMsec(long crawlTimeMsec) {
        this.crawlTimeMsec = crawlTimeMsec;
    }

    public long getTimestampUsec() {
        return timestampUsec;
    }

    public void setTimestampUsec(long timestampUsec) {
        this.timestampUsec = timestampUsec;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getPublished() {
        return published;
    }

    public void setPublished(long published) {
        this.published = published;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public long getStarred() {
        return starred;
    }

    public void setStarred(long starred) {
        this.starred = starred;
    }

    public ArrayList<Enclosure> getEnclosure() {
        return enclosure;
    }

    public void setEnclosure(ArrayList<Enclosure> enclosure) {
        this.enclosure = enclosure;
    }

    public ArrayList<Canonical> getCanonical() {
        return canonical;
    }

    public void setCanonical(ArrayList<Canonical> canonical) {
        this.canonical = canonical;
    }

    public ArrayList<Alternate> getAlternate() {
        return alternate;
    }

    public void setAlternate(ArrayList<Alternate> alternate) {
        this.alternate = alternate;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }
}
