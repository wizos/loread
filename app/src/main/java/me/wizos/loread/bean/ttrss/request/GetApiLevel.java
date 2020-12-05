package me.wizos.loread.bean.ttrss.request;

public class GetApiLevel {
    private String op = "getApiLevel";
    private String sid;

    public GetApiLevel(String sid) {
        this.sid = sid;
    }

    @Override
    public String toString() {
        return "GetApiLevel{" +
                "sid='" + sid + '\'' +
                '}';
    }
}
