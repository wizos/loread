/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-02-09 05:14:20
 */

package me.wizos.loread.bean.jsonfeed;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;

public class JsonItem {
    String id;
    String url;
    @SerializedName(value = "external_url")
    String externalUrl;
    String title;
    @SerializedName(value = "content_html")
    String contentHtml;
    @SerializedName(value = "content_text")
    String contentText;
    String summary;
    String image;
    @SerializedName(value = "banner_image")
    String bannerImage;
    @SerializedName(value = "date_published")
    String datePublished;
    @SerializedName(value = "date_modified")
    String dateModified;
    Author author;
    List<String> tags;
    List<Attachment> attachments;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public void setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
    }

    public Instant getDatePublished() {
        return OffsetDateTime.parse(datePublished).toInstant();
    }

    public void setDatePublished(String datePublished) {
        this.datePublished = datePublished;
    }

    public Instant getDateModified() {
        return OffsetDateTime.parse(dateModified).toInstant();
    }

    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }



    @Override
    public String toString() {
        return "JsonItem{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", externalUrl='" + externalUrl + '\'' +
                ", title='" + title + '\'' +
                ", contentHtml='" + contentHtml + '\'' +
                ", contentText='" + contentText + '\'' +
                ", summary='" + summary + '\'' +
                ", image='" + image + '\'' +
                ", bannerImage='" + bannerImage + '\'' +
                ", datePublished='" + datePublished + '\'' +
                ", dateModified='" + dateModified + '\'' +
                ", author='" + author + '\'' +
                ", tags=" + tags +
                ", attachments=" + attachments +
                '}';
    }
}
