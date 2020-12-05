package me.wizos.loread.bean.ttrss.request;

import com.google.gson.annotations.SerializedName;

import java.util.Collection;

import me.wizos.loread.utils.StringUtils;

public class GetArticles {
    private String op = "getArticle";
    private String sid;
    @SerializedName("article_id")
    private String articleIds;

    public GetArticles(String sid) {
        this.sid = sid;
    }

    public void setArticleIds(Collection<String> articleIdList) {
        this.articleIds = StringUtils.join(",",articleIdList);
    }
    public void setSid(String sid) {
        this.sid = sid;
    }
    public String getSid() {
        return sid;
    }
}
