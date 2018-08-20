package me.wizos.loread.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

@Entity
public class Article {

    @Id
    @NotNull
    private String id;
    // crawlTimeMsec和timetampusec是相同的日期，第一个是毫秒，第二个是微秒
    private Long crawlTimeMsec;
    private Long timestampUsec;
    private String categories;
    private String title;
    private Long published;
    private Long updated;
    private Long starred;
    private String enclosure;
    private String canonical;
    private String alternate;
    private String summary;
    private String content;
    private String author;
    private Integer readStatus;
    private Integer starStatus;
    private String readState;
    private String starState;
    private String saveDir;
    private String imgState;
    private String coverSrc;
    private String originStreamId;
    private String originTitle;
    private String originHtmlUrl;


    @Generated(hash = 159082649)
    public Article(@NotNull String id, Long crawlTimeMsec, Long timestampUsec,
                   String categories, String title, Long published, Long updated, Long starred,
                   String enclosure, String canonical, String alternate, String summary,
                   String content, String author, Integer readStatus, Integer starStatus,
                   String readState, String starState, String saveDir, String imgState,
                   String coverSrc, String originStreamId, String originTitle,
                   String originHtmlUrl) {
        this.id = id;
        this.crawlTimeMsec = crawlTimeMsec;
        this.timestampUsec = timestampUsec;
        this.categories = categories;
        this.title = title;
        this.published = published;
        this.updated = updated;
        this.starred = starred;
        this.enclosure = enclosure;
        this.canonical = canonical;
        this.alternate = alternate;
        this.summary = summary;
        this.content = content;
        this.author = author;
        this.readStatus = readStatus;
        this.starStatus = starStatus;
        this.readState = readState;
        this.starState = starState;
        this.saveDir = saveDir;
        this.imgState = imgState;
        this.coverSrc = coverSrc;
        this.originStreamId = originStreamId;
        this.originTitle = originTitle;
        this.originHtmlUrl = originHtmlUrl;
    }

    @Generated(hash = 742516792)
    public Article() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCrawlTimeMsec() {
        return this.crawlTimeMsec;
    }

    public void setCrawlTimeMsec(Long crawlTimeMsec) {
        this.crawlTimeMsec = crawlTimeMsec;
    }

    public Long getTimestampUsec() {
        return this.timestampUsec;
    }

    public void setTimestampUsec(Long timestampUsec) {
        this.timestampUsec = timestampUsec;
    }

    public String getCategories() {
        return this.categories;
    }

    public void setCategories(String categories) {
        this.categories = categories;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getPublished() {
        return this.published;
    }

    public void setPublished(Long published) {
        this.published = published;
    }

    public Long getUpdated() {
        return this.updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }

    public Long getStarred() {
        return this.starred;
    }

    public void setStarred(Long starred) {
        this.starred = starred;
    }

    public String getEnclosure() {
        return this.enclosure;
    }

    public void setEnclosure(String enclosure) {
        this.enclosure = enclosure;
    }

    public String getCanonical() {
        return this.canonical;
    }

    public void setCanonical(String canonical) {
        this.canonical = canonical;
    }

    public String getAlternate() {
        return this.alternate;
    }

    public void setAlternate(String alternate) {
        this.alternate = alternate;
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getReadState() {
        return this.readState;
    }

    public void setReadState(String readState) {
        this.readState = readState;
    }

    public String getStarState() {
        return this.starState;
    }

    public void setStarState(String starState) {
        this.starState = starState;
    }

    public String getSaveDir() {
        return this.saveDir;
    }

    public void setSaveDir(String saveDir) {
        this.saveDir = saveDir;
    }

    public String getImgState() {
        return this.imgState;
    }

    public void setImgState(String imgState) {
        this.imgState = imgState;
    }

    public String getCoverSrc() {
        return this.coverSrc;
    }

    public void setCoverSrc(String coverSrc) {
        this.coverSrc = coverSrc;
    }

    public String getOriginStreamId() {
        return this.originStreamId;
    }

    public void setOriginStreamId(String originStreamId) {
        this.originStreamId = originStreamId;
    }

    public String getOriginTitle() {
        return this.originTitle;
    }

    public void setOriginTitle(String originTitle) {
        this.originTitle = originTitle;
    }

    public String getOriginHtmlUrl() {
        return this.originHtmlUrl;
    }

    public void setOriginHtmlUrl(String originHtmlUrl) {
        this.originHtmlUrl = originHtmlUrl;
    }

    public Integer getReadStatus() {
        return this.readStatus;
    }

    public Integer getStarStatus() {
        return this.starStatus;
    }

    public void setReadStatus(Integer readStatus) {
        this.readStatus = readStatus;
    }

    public void setStarStatus(Integer starStatus) {
        this.starStatus = starStatus;
    }
}
