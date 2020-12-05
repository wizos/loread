package me.wizos.loread.bean.ttrss.request;

public class GetUnreadItemIds {
    private String op = "getUnreadItemIds";
    private String sid;

    public GetUnreadItemIds(String sid) {
        this.sid = sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }
    public String getSid() {
        return sid;
    }

    @Override
    public String toString() {
        return "GetUnreadItemIds{" +
                "sid='" + sid + '\'' +
                '}';
    }
}
