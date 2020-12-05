package me.wizos.loread.bean.ttrss.request;

public class GetVersion {
    private String op = "getVersion";
    private String sid;

    public GetVersion(String sid) {
        this.sid = sid;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    @Override
    public String toString() {
        return "GetVersion{" +
                "sid='" + sid + '\'' +
                '}';
    }
}
