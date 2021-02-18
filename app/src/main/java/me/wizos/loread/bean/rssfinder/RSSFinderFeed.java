package me.wizos.loread.bean.rssfinder;

import com.google.gson.annotations.SerializedName;

public class RSSFinderFeed {
    @SerializedName("title")
    String title;

    @SerializedName("link")
    String link;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "RSSFinderFeed{" +
                "title='" + title + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
