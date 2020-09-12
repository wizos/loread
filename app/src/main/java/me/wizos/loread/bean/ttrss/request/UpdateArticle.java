package me.wizos.loread.bean.ttrss.request;

import android.text.TextUtils;

public class UpdateArticle {
    private String sid;
    private String op = "updateArticle";

    // 如果有多个请用“,”分割
    private String article_ids;

    // 0 - starred, 1 - published, 2 - unread, 3 - article note
    private int field;

    // 0 - set to false, 1 - set to true, 2 - toggle
    private int mode;

    // 设置 note
    private String data;


    public UpdateArticle(String sid) {
        this.sid = sid;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getArticle_ids() {
        return article_ids;
    }

    public void setArticle_ids(String article_ids) {
        this.article_ids = article_ids;
    }

    public void addArticle_id(String article_id) {
        if (!TextUtils.isEmpty(article_ids)) {
            article_ids = article_ids + "," + article_id;
        } else {
            article_ids = article_id;
        }
    }

    public int getField() {
        return field;
    }

    public void setField(int field) {
        this.field = field;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
