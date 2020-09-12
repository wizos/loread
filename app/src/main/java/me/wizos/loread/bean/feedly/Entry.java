package me.wizos.loread.bean.feedly;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import me.wizos.loread.App;
import me.wizos.loread.bean.Enclosure;
import me.wizos.loread.db.Article;
import me.wizos.loread.network.api.BaseApi;
import me.wizos.loread.utils.ArticleUtil;

/**
 * @author Wizos on 2019/2/8.
 */

public class Entry {
    /**
     * 文章的唯一的、不可变的ID。
     */
    private String id;
    /**
     * 在 RSS feed 中，这篇文章的唯一id (不一定是URL！也可能是文本数字)
     */
    private String originId;
    /**
     * 文章指纹。如果文章被更新，这个值可能会改变。
     */
    private String fingerprint;
    /**
     * 可选。文章的标题。此字符串不包含任何HTML标记。
     */
    private String title;

    /**
     * 可选。文章作者的名字
     */
    private String author;

    /**
     * 可选。文章内容的 object 。这个对象通常有两个值:“content”代表内容本身，“direction”(“ltr”代表从左到右，“rtl”代表从右到左)。内容本身包含经过净化的HTML标记。
     */
    private ContentDirection content;

    /**
     * 可选。文章概要的 object 。这个对象通常有两个值:“content”代表内容本身，“direction”(“ltr”代表从左到右，“rtl”代表从右到左)。内容本身包含经过净化的HTML标记。
     */
    private ContentDirection summary;

    /**
     * 这篇文章发表时的时间戳，单位为毫秒(通常不准确)。
     */
    private long published;
    /**
     * 可空。时间戳。这篇文章更新时的时间戳，单位为毫秒
     */
    private long updated;

    /**
     * 时间戳，单位为毫秒。当这篇文章被feedly Cloud服务器处理时，不可变的时间戳。
     */
    @SerializedName("crawled")
    private long crawled;

//    /**
//     * 可空。时间戳，以毫秒为单位。这篇文章被feedly Cloud服务器重新处理和更新时的时间戳。
//     */
//    private long recrawled;
    /**
     * 可空。origin对象是本文的爬取源。如果存在，“streamId”将包含feedId，“title”将包含feedTitle，“htmlUrl”将包含提要的网站。
     */
    private Origin origin;

    // 可能为空
    private String canonicalUrl;

//    // 可能为空，不知用处
//    @SerializedName("ampUrl")
//    private String ampUrl;
//
//    // 可能为空，不知用处
//    @SerializedName("cdnAmpUrl")
//    private String cdnAmpUrl;

    /**
     * 可空。链接对象数组。本文的原始(?)链接列表。
     */
    private ArrayList<Enclosure> canonical;

    /**
     * 可空。链接对象数组。本文的替代链接列表。每个链接对象包含一种媒体类型和一个URL。通常，存在单个对象，其中包含到原始网页的链接。
     */
    private ArrayList<Enclosure> alternate;

    /**
     * 可空。由feed提供的的媒体链接列表(视频、图像、声音等)。有些条目没有摘要或内容，只有媒体链接的集合。
     */
    private ArrayList<Enclosure> enclosure;

//    /**
//     * 视觉对象。这个条目的图像URL。如果存在，“url”将包含图像URL，“宽度”和“高度”其维度，“内容类型”其MIME类型。
//     */
//    private Visual visual;

    private ArrayList<String> keywords;

    /**
     * 这个条目被用户读取了吗？如果 header 中未提供 Authorization，这将始终返回false。如果提供了，它将反映用户是否读过该条目。
     */
    private boolean unread;

//    /**
//     * 貌似仅出现在 StreamContents 接口中
//     */
//    @SerializedName("categories")
//    private ArrayList<TTRSSCategoryItem> categories;
//    @SerializedName("tags")
//    private ArrayList<TTRSSCategoryItem> tags;

    /*
     * 其他可能的字段
     *
            "recrawled": 1549551157946,
            "updateCount": 1,

            "Visual": {
                "url": "none"
            },
            "unread": false,
            "categories": [
                {
                    "id": "user/12cc057f-9891-4ab3-99da-86f2dee7f2f5/category/1_博谈",
                    "label": "1_博谈"
                }
            ],
            "tags": [
                {
                    "id": "user/12cc057f-9891-4ab3-99da-86f2dee7f2f5/tag/global.read",
                    "label": ""
                }
            ],
            // 表明这个条目有多受欢迎。这个数字越高，越多的读者阅读、保存或分享了这个条目。
            "engagement": 15,
            "engagementRate": 15

            "thumbnail": [
                {
                    "url": "https://2.bp.blogspot.com/-Jdp88f-UJ5c/XHFxq_pvD7I/AAAAAAAADHI/4z0axErIqq0FAgbIaTlveAodRWBf2EfkACK4BGAYYCw/s72-c/usa-dollar.jpg",
                    "width": 72,
                    "height": 72
                }
            ],

     手动添加的文章，通过“/entries/.mget”接口会包含以下字段

        "createdBy": {
            "userAgent": "PostmanRuntime/7.4.0",
            "application": "Feedly Developer"
        },
        "canonicalUrl": "https://www.theverge.com/2013/4/17/4236096/nbc-heroes-may-get-a-second-lease-on-life-on-xbox-live",
        "ampUrl": "https://www.theverge.com/platform/amp/2013/4/17/4236096/nbc-heroes-may-get-a-second-lease-on-life-on-xbox-live",
        "cdnAmpUrl": "https://www-theverge-com.cdn.ampproject.org/c/s/www.theverge.com/platform/amp/2013/4/17/4236096/nbc-heroes-may-get-a-second-lease-on-life-on-xbox-live",

     */

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginId() {
        return originId;
    }

    public void setOriginId(String originId) {
        this.originId = originId;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

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

    public ContentDirection getContent() {
        return content;
    }

    public void setContent(ContentDirection content) {
        this.content = content;
    }

    public ContentDirection getSummary() {
        return summary;
    }

    public void setSummary(ContentDirection summary) {
        this.summary = summary;
    }

    public long getPublished() {
        return published;
    }

    public void setPublished(long published) {
        this.published = published;
    }

    public long getCrawled() {
        return crawled;
    }

    public void setCrawled(long crawled) {
        this.crawled = crawled;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    public void setCanonicalUrl(String canonicalUrl) {
        this.canonicalUrl = canonicalUrl;
    }

    public ArrayList<Enclosure> getCanonical() {
        return canonical;
    }

    public void setCanonical(ArrayList<Enclosure> canonical) {
        this.canonical = canonical;
    }

    public ArrayList<Enclosure> getAlternate() {
        return alternate;
    }

    public void setAlternate(ArrayList<Enclosure> alternate) {
        this.alternate = alternate;
    }

    public ArrayList<Enclosure> getEnclosure() {
        return enclosure;
    }

    public void setEnclosure(ArrayList<Enclosure> enclosure) {
        this.enclosure = enclosure;
    }

    public ArrayList<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(ArrayList<String> keywords) {
        this.keywords = keywords;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }

    public Article convert(BaseApi.ArticleChanger articleChanger) {
        Article article = new Article();
        article.setId(id);

        title = ArticleUtil.getOptimizedTitle(title);
        article.setTitle(title);

        article.setAuthor(author);
        article.setPubDate(published);

        if (alternate != null && alternate.size() > 0) {
            article.setLink(alternate.get(0).getHref());
        }
        if (origin != null) {
            article.setFeedId(origin.getStreamId());
            article.setFeedTitle(origin.getTitle());
        }

        String tmpContent = "";
        if (content != null && !TextUtils.isEmpty(content.getContent())) {
            tmpContent = ArticleUtil.getOptimizedContent(article.getLink(), content.getContent());
        } else if (summary != null && !TextUtils.isEmpty(summary.getContent())) {
            tmpContent = ArticleUtil.getOptimizedContent(article.getLink(), summary.getContent());
        }
        tmpContent = ArticleUtil.getOptimizedContentWithEnclosures(tmpContent,enclosure);
        article.setContent(tmpContent);

        String tmpSummary = ArticleUtil.getOptimizedSummary(tmpContent);
        article.setSummary(tmpSummary);

        String coverUrl = ArticleUtil.getCoverUrl(article.getLink(),tmpContent);
        article.setImage(coverUrl);

        // 自己设置的字段
        // KLog.i("【增加文章】" + article.getId());
        article.setSaveStatus(App.STATUS_NOT_FILED);
        if (articleChanger != null) {
            articleChanger.change(article);
        }
        return article;
    }
}
