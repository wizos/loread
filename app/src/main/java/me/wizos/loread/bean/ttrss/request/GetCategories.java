package me.wizos.loread.bean.ttrss.request;

import com.google.gson.annotations.SerializedName;

public class GetCategories {
    private String sid;
    private String op = "getCategories";
    @SerializedName("unread_only")
    private boolean unreadOnly = false;
    @SerializedName("include_empty")
    private boolean includeEmpty = true;

    public GetCategories(String sid) {
        this.sid = sid;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public boolean isUnreadOnly() {
        return unreadOnly;
    }

    public void setUnreadOnly(boolean unreadOnly) {
        this.unreadOnly = unreadOnly;
    }

    public boolean isIncludeEmpty() {
        return includeEmpty;
    }

    public void setIncludeEmpty(boolean includeEmpty) {
        this.includeEmpty = includeEmpty;
    }
}
