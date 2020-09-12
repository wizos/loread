package me.wizos.loread.bean.domain;

import java.util.ArrayList;

/**
 * Created by Wizos on 2019/5/15.
 */

public class OutTag {
    private String title;
    private ArrayList<OutFeed> outFeeds;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<OutFeed> getOutFeeds() {
        return outFeeds;
    }

    public void setOutFeeds(ArrayList<OutFeed> outFeeds) {
        this.outFeeds = outFeeds;
    }

    public OutTag addOutFeed(OutFeed outFeed) {
        if (outFeeds == null) {
            outFeeds = new ArrayList<>();
        }
        outFeeds.add(outFeed);
        return this;
    }


    @Override
    public String toString() {
        return "OutTag{" +
                "title='" + title + '\'' +
                ", outFeeds=" + outFeeds +
                '}';
    }
}
