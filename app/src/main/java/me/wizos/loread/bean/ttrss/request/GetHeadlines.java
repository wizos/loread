package me.wizos.loread.bean.ttrss.request;

public class GetHeadlines {
    private String op = "getHeadlines";
    private String sid;
    /**
     * all_articles, unread, adaptive, marked, updated
     */
    private String view_mode = "unread";
    /**
     * -1 starred
     * -2 published
     * -3 fresh
     * -4 all articles
     * 0 - archived
     * IDs < -10 labels
     */
    private String feed_id = "-4";
    /**
     * date_reverse - oldest first
     * feed_dates - newest first, goes by feed date
     * (nothing) - default
     */
    private String order_by = "date_reverse";

    private int limit = 50;
    private int skip;
    private String since_id;
    private boolean is_cat = false;

    private boolean show_content = true;
    private boolean include_attachments = true;
    private boolean has_sandbox = true;



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

    public String getView_mode() {
        return view_mode;
    }

    public void setView_mode(String view_mode) {
        this.view_mode = view_mode;
    }

    public String getFeed_id() {
        return feed_id;
    }

    public void setFeed_id(String feed_id) {
        this.feed_id = feed_id;
    }

    public String getOrder_by() {
        return order_by;
    }

    public void setOrder_by(String order_by) {
        this.order_by = order_by;
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

    public String getSince_id() {
        return since_id;
    }

    public void setSince_id(String since_id) {
        this.since_id = since_id;
    }

    public boolean isIs_cat() {
        return is_cat;
    }

    public void setIs_cat(boolean is_cat) {
        this.is_cat = is_cat;
    }

    public boolean isShow_content() {
        return show_content;
    }

    public void setShow_content(boolean show_content) {
        this.show_content = show_content;
    }

    public boolean isInclude_attachments() {
        return include_attachments;
    }

    public void setInclude_attachments(boolean include_attachments) {
        this.include_attachments = include_attachments;
    }

    public boolean isHas_sandbox() {
        return has_sandbox;
    }

    public void setHas_sandbox(boolean has_sandbox) {
        this.has_sandbox = has_sandbox;
    }
}
