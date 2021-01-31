package me.wizos.loread.bean.inoreader.itemContents;

import java.util.ArrayList;

import me.wizos.loread.App;
import me.wizos.loread.bean.Enclosure;
import me.wizos.loread.db.Article;
import me.wizos.loread.network.api.BaseApi;
import me.wizos.loread.utils.ArticleUtils;

/**
 * Stream content 和 Item content （貌似已被官方弃用）两个api返回指内的文章项
 * Created by Wizos on 2016/3/11.
 */
public class Item {
    private String id;
    private String title;
    private long published;
    private long updated;
    private long crawlTimeMsec;
    private long timestampUsec;
    private ArrayList<String> categories;

    private long starred; // 加星的时间
    private ArrayList<Enclosure> enclosure; // 附件：这个还不知道是什么用处，不过可以显示图片
    private ArrayList<Enclosure> canonical;
    private ArrayList<Enclosure> alternate;
    private Summary summary;
    private String author;
    private Origin origin;

//这应该是开启了社交后才会有的字段
//            "likingUsers": [],
//             "comments": [],
//             "commentsNum": -1,
//             "annotations": [],


    public long getCrawlTimeMsec() {
        return crawlTimeMsec;
    }

    public void setCrawlTimeMsec(long crawlTimeMsec) {
        this.crawlTimeMsec = crawlTimeMsec;
    }

    public long getTimestampUsec() {
        return timestampUsec;
    }

    public void setTimestampUsec(long timestampUsec) {
        this.timestampUsec = timestampUsec;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getPublished() {
        return published;
    }

    public void setPublished(long published) {
        this.published = published;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public long getStarred() {
        return starred;
    }

    public void setStarred(long starred) {
        this.starred = starred;
    }

    public ArrayList<Enclosure> getEnclosure() {
        return enclosure;
    }

    public void setEnclosure(ArrayList<Enclosure> enclosure) {
        this.enclosure = enclosure;
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

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }


    public Article convert(BaseApi.ArticleChanger articleChanger) {
        Article article = new Article();
        // 返回的字段
        article.setId(id);
        article.setAuthor(author);
        article.setPubDate(published * 1000);

        if (canonical != null && canonical.size() > 0) {
            article.setLink(canonical.get(0).getHref());
        }
        if (origin != null) {
            article.setFeedId(origin.getStreamId());
            article.setFeedTitle(origin.getTitle());
        }

        String tmpContent = ArticleUtils.getOptimizedContent(article.getLink(), summary.getContent());
        tmpContent = ArticleUtils.getOptimizedContentWithEnclosures(tmpContent,enclosure);
        article.setContent(tmpContent);

        String tmpSummary = ArticleUtils.getOptimizedSummary(tmpContent);
        article.setSummary(tmpSummary);

        title = ArticleUtils.getOptimizedTitle(title,tmpSummary);
        article.setTitle(title);

        String coverUrl = ArticleUtils.getCoverUrl(article.getLink(),tmpContent);
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
