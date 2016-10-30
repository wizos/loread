package me.wizos.loread.bean.gson;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Wizos on 2016/3/11.
 */
public class Sub {
    @SerializedName("id")
    String id;

    @SerializedName("title")
    String title;

    @SerializedName("categories")
    ArrayList<SubCategories> categories;

    @SerializedName("sortid")
    String sortid;

    @SerializedName("firstitemmsec")
    String firstitemmsec;

    @SerializedName("url")
    String url;

    @SerializedName("htmlUrl")
    String htmlUrl;

    @SerializedName("iconUrl")
    String iconUrl;


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<SubCategories> getCategories() {
        return categories;
    }
    public void setCategories(ArrayList<SubCategories> categories) {
        this.categories = categories;
    }

    public String getSortid() {
        return sortid;
    }
    public void setSortid(String sortid) {
        this.sortid = sortid;
    }


    public String getFirstitemmsec() {
        return firstitemmsec;
    }
    public void setFirstitemmsec(String firstitemmsec) {
        this.firstitemmsec = firstitemmsec;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }
    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
