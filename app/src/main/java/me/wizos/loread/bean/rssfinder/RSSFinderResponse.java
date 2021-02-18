package me.wizos.loread.bean.rssfinder;

import java.util.List;

public class RSSFinderResponse {
    private int status;
    private List<RSSFinderFeed> feeds;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<RSSFinderFeed> getFeeds() {
        return feeds;
    }

    public void setFeeds(List<RSSFinderFeed> feeds) {
        this.feeds = feeds;
    }

    public boolean isOK(){
        return status == 200;
    }
}
