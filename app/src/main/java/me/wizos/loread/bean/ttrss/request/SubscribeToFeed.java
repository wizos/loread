package me.wizos.loread.bean.ttrss.request;

public class SubscribeToFeed {
    private String sid;
    private String op = "subscribeToFeed";
    private String category_id;
    private String feed_url;

    public SubscribeToFeed(String sid) {
        this.sid = sid;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public String getFeed_url() {
        return feed_url;
    }

    public void setFeed_url(String feed_url) {
        this.feed_url = feed_url;
    }

    @Override
    public String toString() {
        return "SubscribeToFeed{" +
                "op='" + op + '\'' +
                ", sid='" + sid + '\'' +
                ", category_id='" + category_id + '\'' +
                ", feed_url='" + feed_url + '\'' +
                '}';
    }
}
