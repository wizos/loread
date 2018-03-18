package me.wizos.loread.bean.gson;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * 调用 Mercy 接口的返回实体
 * Created by Wizos on 2017/12/17.
 */

@Parcel
public class Readability {
    @SerializedName("title")
    String title;

    @SerializedName("author")
    String author;

    @SerializedName("date_published")
    String date_published;

    @SerializedName("dek")
    String dek;

    @SerializedName("lead_image_url")
    String lead_image_url;

    @SerializedName("content")
    String content;

    @SerializedName("next_page_url")
    String next_page_url;

    @SerializedName("url")
    String url;

    @SerializedName("domain")
    String domain;

    @SerializedName("excerpt")
    String excerpt;

    @SerializedName("word_count")
    int word_count;

    @SerializedName("direction")
    String direction;

    @SerializedName("total_pages")
    int total_pages;

    @SerializedName("rendered_pages")
    int rendered_pages;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate_published() {
        return date_published;
    }

    public void setDate_published(String date_published) {
        this.date_published = date_published;
    }

    public String getDek() {
        return dek;
    }

    public void setDek(String dek) {
        this.dek = dek;
    }

    public String getLead_image_url() {
        return lead_image_url;
    }

    public void setLead_image_url(String lead_image_url) {
        this.lead_image_url = lead_image_url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNext_page_url() {
        return next_page_url;
    }

    public void setNext_page_url(String next_page_url) {
        this.next_page_url = next_page_url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }

    public int getWord_count() {
        return word_count;
    }

    public void setWord_count(int word_count) {
        this.word_count = word_count;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getTotal_pages() {
        return total_pages;
    }

    public void setTotal_pages(int total_pages) {
        this.total_pages = total_pages;
    }

    public int getRendered_pages() {
        return rendered_pages;
    }

    public void setRendered_pages(int rendered_pages) {
        this.rendered_pages = rendered_pages;
    }
}
