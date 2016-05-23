package me.wizos.loread.gson.itemContents;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
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

//    @SerializedName("enclosure")
//    Enclosure enclosure;

    @SerializedName("canonical")
    ArrayList<Canonical> canonical;

    @SerializedName("alternate")
    ArrayList<Alternate> alternate;

    @SerializedName("summary")
    Summary summary;

    @SerializedName("author")
    String author;

    @SerializedName("origin")
    Origin origin;





//    public Enclosure getEnclosure() {
//        return enclosure;
//    }
//    public void setEnclosure(Enclosure enclosure) {
//        this.enclosure = enclosure;
//    }

    public ArrayList<Alternate> getAlternate() {
        return alternate;
    }
    public void setAlternate(ArrayList<Alternate> alternate) {
        this.alternate = alternate;
    }

    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }

    public ArrayList<Canonical> getCanonical() {
        return canonical;
    }
    public void setCanonical(ArrayList<Canonical> canonical) {
        this.canonical = canonical;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public long getCrawlTimeMsec() {
        return crawlTimeMsec;
    }

    public void setCrawlTimeMsec(long crawlTimeMsec) {
        this.crawlTimeMsec = crawlTimeMsec;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    public long getPublished() {
        return published;
    }

    public void setPublished(long published) {
        this.published = published;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public long getTimestampUsec() {
        return timestampUsec;
    }

    public void setTimestampUsec(long timestampUsec) {
        this.timestampUsec = timestampUsec;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }



}
