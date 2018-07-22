package me.wizos.loread.db;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

import me.wizos.loread.db.dao.ArticleDao;
import me.wizos.loread.db.dao.DaoSession;
import me.wizos.loread.db.dao.FeedDao;


@Entity
public class Feed {

    @Id
    @NotNull
    private String id;

    @NotNull
    private String title;
    private String categoryid;
    private String categorylabel;
    private String sortid;
    private Long firstitemmsec;
    private String url;
    private String htmlurl;
    private String iconurl;
    private String openMode;
    private Integer unreadCount = 0;
    private Long newestItemTimestampUsec;

    @ToMany(joinProperties = {
            @JoinProperty(name = "id", referencedName = "categories")
    })
    @OrderBy("timestampUsec DESC")
    private List<Article> items;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 2085497664)
    private transient FeedDao myDao;

    @Generated(hash = 1043569664)
    public Feed(@NotNull String id, @NotNull String title, String categoryid, String categorylabel,
                String sortid, Long firstitemmsec, String url, String htmlurl, String iconurl,
                String openMode, Integer unreadCount, Long newestItemTimestampUsec) {
        this.id = id;
        this.title = title;
        this.categoryid = categoryid;
        this.categorylabel = categorylabel;
        this.sortid = sortid;
        this.firstitemmsec = firstitemmsec;
        this.url = url;
        this.htmlurl = htmlurl;
        this.iconurl = iconurl;
        this.openMode = openMode;
        this.unreadCount = unreadCount;
        this.newestItemTimestampUsec = newestItemTimestampUsec;
    }

    @Generated(hash = 1810414124)
    public Feed() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategoryid() {
        return this.categoryid;
    }

    public void setCategoryid(String categoryid) {
        this.categoryid = categoryid;
    }

    public String getCategorylabel() {
        return this.categorylabel;
    }

    public void setCategorylabel(String categorylabel) {
        this.categorylabel = categorylabel;
    }

    public String getSortid() {
        return this.sortid;
    }

    public void setSortid(String sortid) {
        this.sortid = sortid;
    }

    public Long getFirstitemmsec() {
        return this.firstitemmsec;
    }

    public void setFirstitemmsec(Long firstitemmsec) {
        this.firstitemmsec = firstitemmsec;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHtmlurl() {
        return this.htmlurl;
    }

    public void setHtmlurl(String htmlurl) {
        this.htmlurl = htmlurl;
    }

    public String getIconurl() {
        return this.iconurl;
    }

    public void setIconurl(String iconurl) {
        this.iconurl = iconurl;
    }

    public String getOpenMode() {
        return this.openMode;
    }

    public void setOpenMode(String openMode) {
        this.openMode = openMode;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1173003445)
    public List<Article> getItems() {
        if (items == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ArticleDao targetDao = daoSession.getArticleDao();
            List<Article> itemsNew = targetDao._queryFeed_Items(id);
            synchronized (this) {
                if (items == null) {
                    items = itemsNew;
                }
            }
        }
        return items;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1727286264)
    public synchronized void resetItems() {
        items = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 364457678)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getFeedDao() : null;
    }

    public Integer getUnreadCount() {
        return this.unreadCount;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Long getNewestItemTimestampUsec() {
        return this.newestItemTimestampUsec;
    }

    public void setNewestItemTimestampUsec(Long newestItemTimestampUsec) {
        this.newestItemTimestampUsec = newestItemTimestampUsec;
    }
}
