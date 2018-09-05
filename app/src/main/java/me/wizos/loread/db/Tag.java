package me.wizos.loread.db;

import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.JoinProperty;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

import me.wizos.loread.db.dao.DaoSession;
import me.wizos.loread.db.dao.FeedDao;
import me.wizos.loread.db.dao.TagDao;


/**
 * Entity mapped to table "TAG".
 */
@Entity
public class Tag {

    @Id
    @NotNull
    @Index
    @SerializedName("id")
    private String id;
    @NotNull
    @SerializedName("sortid")
    private String sortid;

    private String title;
    private Integer unreadCount;
    private Long newestItemTimestampUsec;


    @ToMany(joinProperties = {
            @JoinProperty(name = "id", referencedName = "categoryid")
    })
    @OrderBy("categoryid DESC")
    private List<me.wizos.loread.db.Feed> feeds;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 2076396065)
    private transient TagDao myDao;

    @Generated(hash = 316932467)
    public Tag(@NotNull String id, @NotNull String sortid, String title, Integer unreadCount,
               Long newestItemTimestampUsec) {
        this.id = id;
        this.sortid = sortid;
        this.title = title;
        this.unreadCount = unreadCount;
        this.newestItemTimestampUsec = newestItemTimestampUsec;
    }

    @Generated(hash = 1605720318)
    public Tag() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSortid() {
        return this.sortid;
    }

    public void setSortid(String sortid) {
        this.sortid = sortid;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1885689304)
    public List<Feed> getFeeds() {
        if (feeds == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            FeedDao targetDao = daoSession.getFeedDao();
            List<Feed> feedsNew = targetDao._queryTag_Feeds(id);
            synchronized (this) {
                if (feeds == null) {
                    feeds = feedsNew;
                }
            }
        }
        return feeds;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1224133853)
    public synchronized void resetFeeds() {
        feeds = null;
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
    @Generated(hash = 441429822)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTagDao() : null;
    }

}
