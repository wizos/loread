package me.wizos.loread.bean.ttrss.request;

public class GetFeeds {
    private String sid;
    private String op = "getFeeds";

    public GetFeeds(String sid) {
        this.sid = sid;
    }

    /**
     * 0 Uncategorized
     * -1 Special (e.g. Starred, Published, Archived, etc.)
     * -2 Labels
     * -3 All feeds, excluding virtual feeds (e.g. Labels and such)
     * -4 All feeds, including virtual feeds
     */
    private int cat_id = -3;
    private boolean unread_only = false;


    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public int getCat_id() {
        return cat_id;
    }

    public void setCat_id(int cat_id) {
        this.cat_id = cat_id;
    }

    public boolean isUnread_only() {
        return unread_only;
    }

    public void setUnread_only(boolean unread_only) {
        this.unread_only = unread_only;
    }
}
