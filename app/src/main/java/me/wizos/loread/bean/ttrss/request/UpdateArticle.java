package me.wizos.loread.bean.ttrss.request;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class UpdateArticle {
    private String sid;
    private String op = "updateArticle";

    // 如果有多个请用“,”分割
    @SerializedName("article_ids")
    private String articleIds;

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

    public String getArticleIds() {
        return articleIds;
    }

    public void setArticleIds(String articleIds) {
        this.articleIds = articleIds;
    }

    public void addArticle_id(String article_id) {
        if (!TextUtils.isEmpty(articleIds)) {
            articleIds = articleIds + "," + article_id;
        } else {
            articleIds = article_id;
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

    @Override
    public String toString() {
        return "UpdateArticle{" +
                "sid='" + sid + '\'' +
                ", op='" + op + '\'' +
                ", articleIds='" + articleIds + '\'' +
                ", field=" + field +
                ", mode=" + mode +
                ", data='" + data + '\'' +
                '}';
    }
}
