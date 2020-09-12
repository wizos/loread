package me.wizos.loread.bean.ttrss.request;

import com.google.gson.annotations.SerializedName;

import java.util.HashSet;
import java.util.List;

import me.wizos.loread.utils.StringUtils;

public class GetArticles {
    private String sid;
    private String op = "getArticle";
    @SerializedName("article_id")
    private String articleIds;

    public GetArticles(String sid) {
        this.sid = sid;
    }

    public void setArticleIds(String articleIds) {
        this.articleIds = articleIds;
    }
    public void setArticleIds(HashSet<String> articleIdSet) {
        this.articleIds = StringUtils.join(",",articleIdSet);
    }
    public void setArticleIds(List<String> articleIdList) {
        this.articleIds = StringUtils.join(",",articleIdList);
    }
    public void setSid(String sid) {
        this.sid = sid;
    }
    public String getSid() {
        return sid;
    }
}
