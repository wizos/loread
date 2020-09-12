package me.wizos.loread.bean.ttrss.result;

public class TTRSSLoginResult {
    private String session_id;
    private int api_level;

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public int getApi_level() {
        return api_level;
    }

    public void setApi_level(int api_level) {
        this.api_level = api_level;
    }
}
