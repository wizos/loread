package me.wizos.loread.bean.ttrss.request;

public class GetUnreadItemIds {
    private String sid;
    private String op = "getUnreadItemIds";

    public GetUnreadItemIds(String sid) {
        this.sid = sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }
    public String getSid() {
        return sid;
    }
}
