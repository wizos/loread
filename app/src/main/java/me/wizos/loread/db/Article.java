package me.wizos.loread.db;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import org.jetbrains.annotations.NotNull;

import me.wizos.loread.App;
import me.wizos.loread.utils.ArticleUtils;

/**
 * //    private Integer preference = 0; // 偏好（点击）：0是初始状态，1是不喜欢，2是喜欢
 * //    private Integer predict = 0; //推断结果： 0是初始状态，1是不喜欢，2是喜欢
 * Created by Wizos on 2020/3/17.
 */
@Entity(primaryKeys = {"id","uid"},
        indices = {@Index({"id"}), @Index({"uid"}), @Index({"title"}),@Index({"feedId","uid"}),
                @Index({"uid","readStatus"}),@Index({"uid","starStatus"}),@Index({"uid","crawlDate"})
                // @Index({"readStatus"}),@Index({"starStatus"}),@Index({"saveStatus"})
                // @Index({"readUpdated"}), @Index({"starUpdated"}), @Index({"pubDate"}), @Index({"crawlDate"})
                },
        foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "uid")
        )
public class Article implements Cloneable{
    @NonNull
    private String uid;
    @NonNull
    private String id;
    private String guid;
    private String title;
    private String content;
    private String summary;
    private String image;
    private String enclosure; // 包含图片，视频等多媒体信息

    private String feedId;
    private String feedUrl;
    private String feedTitle;
    private String author;
    private String link = "";
    private long pubDate;
    private long crawlDate;
    private int readStatus = App.STATUS_UNREAD;
    private int starStatus = App.STATUS_UNSTAR;
    private int saveStatus = App.STATUS_NOT_FILED;
    private long readUpdated;
    private long starUpdated;


    @NotNull
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getEnclosure() {
        return this.enclosure;
    }

    public void setEnclosure(String enclosure) {
        this.enclosure = enclosure;
    }

    public String getFeedId() {
        return this.feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    public String getFeedTitle() {
        return this.feedTitle;
    }

    public void setFeedTitle(String feedTitle) {
        this.feedTitle = feedTitle;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public long getPubDate() {
        return this.pubDate;
    }

    public void setPubDate(long pubDate) {
        this.pubDate = pubDate;
    }

    public long getCrawlDate() {
        return this.crawlDate;
    }

    public void setCrawlDate(long crawlDate) {
        this.crawlDate = crawlDate;
    }

    public int getReadStatus() {
        return this.readStatus;
    }

    public void setReadStatus(int readStatus) {
        this.readStatus = readStatus;
    }

    public int getStarStatus() {
        return this.starStatus;
    }

    public void setStarStatus(int starStatus) {
        this.starStatus = starStatus;
    }

    public int getSaveStatus() {
        return this.saveStatus;
    }

    public void setSaveStatus(int saveStatus) {
        this.saveStatus = saveStatus;
    }

    public long getReadUpdated() {
        return readUpdated;
    }

    public void setReadUpdated(long readUpdated) {
        this.readUpdated = readUpdated;
    }

    public long getStarUpdated() {
        return starUpdated;
    }

    public void setStarUpdated(long starUpdated) {
        this.starUpdated = starUpdated;
    }

    public void updateContent(String content) {
        this.content = ArticleUtils.getOptimizedContent(link, content);;
        this.summary = ArticleUtils.getOptimizedSummary(content);
        this.image = ArticleUtils.getCoverUrl(link, content);
    }
    public void updateContent(String link, String content) {
        this.content = ArticleUtils.getOptimizedContent(link, content);;
        this.summary = ArticleUtils.getOptimizedSummary(content);
        this.image = ArticleUtils.getCoverUrl(link, content);
    }

    @NotNull
    @Override
    public Object clone(){
        try{
            return super.clone();   //浅复制
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return this;
    }
    @NotNull
    @Override
    public String toString() {
        return "Article{" +
                "id='" + id + '\'' +
                ", uid='" + uid + '\'' +
                ", guid='" + guid + '\'' +
                ", title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                ", image='" + image + '\'' +
                ", enclosure='" + enclosure + '\'' +
                ", feedId='" + feedId + '\'' +
                ", feedUrl='" + feedUrl + '\'' +
                ", feedTitle='" + feedTitle + '\'' +
                ", author='" + author + '\'' +
                ", link='" + link + '\'' +
                ", pubDate=" + pubDate +
                ", crawlDate=" + crawlDate +
                ", readStatus=" + readStatus +
                ", starStatus=" + starStatus +
                ", saveStatus=" + saveStatus +
                ", readUpdated=" + readUpdated +
                ", starUpdated=" + starUpdated +
                ", content='" + content +
                '}';
    }
}
