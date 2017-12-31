package me.wizos.loread.bean.gson.itemContents;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Stream content 和 Item content （貌似已被官方弃用）两个api返回指内的文章项
 * Created by Wizos on 2016/3/11.
 */
public class Items {
    @SerializedName("crawlTimeMsec")
    private long crawlTimeMsec;

    @SerializedName("timestampUsec")
    private long timestampUsec;

    @SerializedName("id")
    private String id;

    @SerializedName("categories")
    private ArrayList<String> categories;

    @SerializedName("title")
    private String title;

    @SerializedName("published")
    private long published;

    @SerializedName("updated")
    private long updated;

    @SerializedName("starred") // 加星的时间
    private long starred;

    // 附件：这个还不知道是什么用处，不过可以显示图片
    @SerializedName("enclosure")
    private ArrayList<Enclosure> enclosure;

    @SerializedName("canonical")
    private ArrayList<Canonical> canonical;

    @SerializedName("alternate")
    private ArrayList<Alternate> alternate;

    @SerializedName("summary")
    private Summary summary;

    @SerializedName("author")
    private String author;

    @SerializedName("origin")
    private Origin origin;

//这应该是开启了社交后才会有的字段
//            "likingUsers": [],
//             "comments": [],
//             "commentsNum": -1,
//             "annotations": [],


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


//    // // TEST:  学习 FeedMe 中的写法。直接把解析写入实体类中
//    public static Items parse(String json){
//        return new Gson().fromJson(json, Items.class);
//    }
}
