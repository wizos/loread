package me.wizos.loread.bean.export;

/**
 * Created by Wizos on 2019/5/15.
 */

public class OutFeed {
    private String title;
    private String feedUrl;
    private String htmlUrl;

    public OutFeed(String title, String feedUrl, String htmlUrl) {
        this.title = title;
        this.feedUrl = feedUrl;
        this.htmlUrl = htmlUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    @Override
    public String toString() {
        return "OutFeed{" +
                "title='" + title + '\'' +
                ", feedUrl='" + feedUrl + '\'' +
                ", htmlUrl='" + htmlUrl + '\'' +
                '}';
    }
}
