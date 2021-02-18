package me.wizos.loread.bean.ttrss.request;

import com.google.gson.annotations.SerializedName;

public class GetHeadlines {
    private String op = "getHeadlines";
    private String sid;

    /**
     * all_articles, unread, adaptive, marked, updated
     */
    @SerializedName("view_mode")
    private String viewMode = "unread, marked";

    /**
     * -1 starred
     * -2 published
     * -3 fresh
     * -4 all articles
     * 0 - archived
     * IDs < -10 labels
     */
    @SerializedName("feed_id")
    private String feedId = "-4";
    /**
     * date_reverse - oldest first
     * feed_dates - newest first, goes by feed date
     * (nothing) - default
     */
    @SerializedName("order_by")
    private String orderBy = "date_reverse";

    private int limit = 20;
    private int skip;
    @SerializedName("since_id")
    private String sinceId;
    @SerializedName("is_cat")
    private boolean isCat = false;

    @SerializedName("show_content")
    private boolean showContent = true;
    @SerializedName("include_attachments")
    private boolean includeAttachments = true;
    @SerializedName("has_sandbox")
    private boolean hasSandbox = true;



    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getViewMode() {
        return viewMode;
    }

    public void setViewMode(String viewMode) {
        this.viewMode = viewMode;
    }

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public String getSinceId() {
        return sinceId;
    }

    public void setSinceId(String sinceId) {
        this.sinceId = sinceId;
    }

    public boolean isCat() {
        return isCat;
    }

    public void setCat(boolean cat) {
        this.isCat = cat;
    }

    public boolean isShowContent() {
        return showContent;
    }

    public void setShowContent(boolean showContent) {
        this.showContent = showContent;
    }

    public boolean isIncludeAttachments() {
        return includeAttachments;
    }

    public void setIncludeAttachments(boolean includeAttachments) {
        this.includeAttachments = includeAttachments;
    }

    public boolean isHasSandbox() {
        return hasSandbox;
    }

    public void setHasSandbox(boolean hasSandbox) {
        this.hasSandbox = hasSandbox;
    }

    @Override
    public String toString() {
        return "GetHeadlines{" +
                "op='" + op + '\'' +
                ", sid='" + sid + '\'' +
                ", viewMode='" + viewMode + '\'' +
                ", feedId='" + feedId + '\'' +
                ", orderBy='" + orderBy + '\'' +
                ", limit=" + limit +
                ", skip=" + skip +
                ", sinceId='" + sinceId + '\'' +
                ", isCat=" + isCat +
                ", showContent=" + showContent +
                ", includeAttachments=" + includeAttachments +
                ", hasSandbox=" + hasSandbox +
                '}';
    }
}
