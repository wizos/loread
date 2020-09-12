package me.wizos.loread.bean.ttrss.request;

public class GetSavedItemIds {
    private String sid;
    private String op = "getSavedItemIds";

    public GetSavedItemIds(String sid) {
        this.sid = sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }
    public String getSid() {
        return sid;
    }
}
