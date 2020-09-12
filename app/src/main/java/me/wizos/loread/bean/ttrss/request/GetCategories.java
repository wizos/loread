package me.wizos.loread.bean.ttrss.request;

public class GetCategories {
    private String sid;
    private String op = "getCategories";
    private boolean unread_only = false;
    private boolean include_empty = true;

    public GetCategories(String sid) {
        this.sid = sid;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public boolean isUnread_only() {
        return unread_only;
    }

    public void setUnread_only(boolean unread_only) {
        this.unread_only = unread_only;
    }

    public boolean isInclude_empty() {
        return include_empty;
    }

    public void setInclude_empty(boolean include_empty) {
        this.include_empty = include_empty;
    }
}
