package me.wizos.loread.bean.fever;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import me.wizos.loread.App;
import me.wizos.loread.db.Article;
import me.wizos.loread.network.api.BaseApi;
import me.wizos.loread.utils.ArticleUtils;

public class Item {
    @SerializedName("id")
    private int id;
    @SerializedName("feed_id")
    private int feedId;
    @SerializedName("title")
    private String title;
    @SerializedName("author")
    private String author;
    @SerializedName("html")
    private String html;

    @SerializedName("url")
    private String url;

    @SerializedName("is_saved")
    private int isSaved; // 0 = false, 1 = true
    @SerializedName("is_read")
    private int isRead; // 0 = false, 1 = true

    @SerializedName("created_on_time")
    private long createdOnTime;

    public int getId() {
        return id;
    }

    public int getFeedId() {
        return feedId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getHtml() {
        return html;
    }

    public String getUrl() {
        return url;
    }

    public int getIsSaved() {
        return isSaved;
    }

    public int getIsRead() {
        return isRead;
    }

    public long getCreatedOnTime() {
        return createdOnTime;
    }

    public Article convert(BaseApi.ArticleChanger articleChanger) {
        Article article = new Article();
        article.setId(String.valueOf(id));
        article.setAuthor(author);
        article.setPubDate(createdOnTime * 1000);

        article.setLink(url);
        article.setFeedId(String.valueOf(feedId));
        article.setFeedTitle(title);

        String tmpContent = ArticleUtils.getOptimizedContent(url, html);
        article.setContent(tmpContent);

        String tmpSummary = ArticleUtils.getOptimizedSummary(tmpContent);
        article.setSummary(tmpSummary);

        title = ArticleUtils.getOptimizedTitle(title,tmpSummary);
        article.setTitle(title);

        String coverUrl = ArticleUtils.getCoverUrl(article.getLink(),tmpContent);
        article.setImage(coverUrl);

        // 自己设置的字段
        //  KLog.i("【增加文章】" + article.getId());
        article.setSaveStatus(App.STATUS_NOT_FILED);
        if (isRead == 0) {
            article.setReadStatus(App.STATUS_UNREAD);
        } else {
            article.setReadStatus(App.STATUS_READED);
        }
        if (isSaved == 1) {
            article.setStarStatus(App.STATUS_STARED);
        } else {
            article.setStarStatus(App.STATUS_UNSTAR);
        }

        if (articleChanger != null) {
            articleChanger.change(article);
        }
        return article;
    }

    @NotNull
    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", feedId=" + feedId +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", html='" + html + '\'' +
                ", url='" + url + '\'' +
                ", isSaved=" + isSaved +
                ", isRead=" + isRead +
                ", createdOnTime=" + createdOnTime +
                '}';
    }
}
