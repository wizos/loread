package me.wizos.loread.bean.ttrss.result;

import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.bean.Enclosure;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.network.api.BaseApi;
import me.wizos.loread.utils.ArticleUtil;

public class ArticleItem {
    private int id;
    private String guid;

    private boolean unread;
    private boolean marked;
    private boolean published;

    private long updated;
    private boolean is_updated;

    private String title;
    private String link;
    private String author;
    private String content;

    private List<Enclosure> attachments;
    private List<String> tags;
    private List<Object> labels;
    private String comments_link;
    private int comments_count;

    private String feed_id;
    private String feed_title;

    private String flavor_image;
    private String flavor_stream;
    private String lang = "zh";
    private String note = "";


    private int score;
    private boolean always_display_attachments;


    public Article convert(BaseApi.ArticleChanger articleChanger) {
        Article article = CoreDB.i().articleDao().getById(App.i().getUser().getId(),String.valueOf(id));
        if( article != null ){
            article.setLink(link);
            article.setFeedId(feed_id);
            return article;
        }
        //Article article = new Article();
        article = new Article();
        article.setId(String.valueOf(id));

        String tmpContent = ArticleUtil.getOptimizedContent(article.getLink(), content);
        tmpContent = ArticleUtil.getOptimizedContentWithEnclosures(tmpContent, attachments);
        article.setContent(tmpContent);

        String tmpSummary = ArticleUtil.getOptimizedSummary(tmpContent);
        article.setSummary(tmpSummary);

        title = ArticleUtil.getOptimizedTitle(title,tmpSummary);
        article.setTitle(title);

        article.setAuthor(author);
        article.setPubDate(updated * 1000);

        article.setLink(link);
        article.setFeedId(feed_id);
        article.setFeedTitle(feed_title);

        String coverUrl = ArticleUtil.getCoverUrl(article.getLink(),tmpContent);
        article.setImage(coverUrl);

        // 自己设置的字段
        //  KLog.i("【增加文章】" + article.getId());
        article.setSaveStatus(App.STATUS_NOT_FILED);
        if (unread) {
            article.setReadStatus(App.STATUS_UNREAD);
        } else {
            article.setReadStatus(App.STATUS_READED);
        }
        if (marked) {
            article.setStarStatus(App.STATUS_STARED);
        } else {
            article.setStarStatus(App.STATUS_UNSTAR);
        }

        if (articleChanger != null) {
            articleChanger.change(article);
        }
        return article;
    }

    @Override
    public String toString() {
        return "TTRSSArticleItem{" +
                "id=" + id +
                ", guid='" + guid + '\'' +
                ", unread=" + unread +
                ", marked=" + marked +
                ", published=" + published +
                ", updated=" + updated +
                ", is_updated=" + is_updated +
                ", title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", author='" + author + '\'' +
                ", content='" + content + '\'' +
                ", attachments=" + attachments +
                ", tags=" + tags +
                ", labels=" + labels +
                ", comments_link='" + comments_link + '\'' +
                ", comments_count=" + comments_count +
                ", feed_id='" + feed_id + '\'' +
                ", feed_title='" + feed_title + '\'' +
                ", flavor_image='" + flavor_image + '\'' +
                ", flavor_stream='" + flavor_stream + '\'' +
                ", lang='" + lang + '\'' +
                ", note='" + note + '\'' +
                ", score=" + score +
                ", always_display_attachments=" + always_display_attachments +
                '}';
    }

    public int getId() {
        return id;
    }

    public String getGuid() {
        return guid;
    }

    public boolean isUnread() {
        return unread;
    }

    public boolean isMarked() {
        return marked;
    }

    public boolean isPublished() {
        return published;
    }

    public long getUpdated() {
        return updated;
    }

    public boolean isIs_updated() {
        return is_updated;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public List<Enclosure> getAttachments() {
        return attachments;
    }

    public List<String> getTags() {
        return tags;
    }

    public List<Object> getLabels() {
        return labels;
    }

    public String getComments_link() {
        return comments_link;
    }

    public int getComments_count() {
        return comments_count;
    }

    public String getFeed_id() {
        return feed_id;
    }

    public String getFeed_title() {
        return feed_title;
    }

    public String getFlavor_image() {
        return flavor_image;
    }

    public String getFlavor_stream() {
        return flavor_stream;
    }

    public String getLang() {
        return lang;
    }

    public String getNote() {
        return note;
    }

    public int getScore() {
        return score;
    }

    public boolean isAlways_display_attachments() {
        return always_display_attachments;
    }
}
