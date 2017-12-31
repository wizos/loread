package me.wizos.loread.data.dao;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import java.util.Map;

import me.wizos.loread.bean.Article;
import me.wizos.loread.bean.Feed;
import me.wizos.loread.bean.Img;
import me.wizos.loread.bean.RequestLog;
import me.wizos.loread.bean.Tag;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 *
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig tagDaoConfig;
    private final DaoConfig feedDaoConfig;
    private final DaoConfig articleDaoConfig;
    private final DaoConfig imgDaoConfig;
    private final DaoConfig requestLogDaoConfig;

    private final TagDao tagDao;
    private final FeedDao feedDao;
    private final ArticleDao articleDao;
    private final ImgDao imgDao;
    private final RequestLogDao requestLogDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        tagDaoConfig = daoConfigMap.get(TagDao.class).clone();
        tagDaoConfig.initIdentityScope(type);

        feedDaoConfig = daoConfigMap.get(FeedDao.class).clone();
        feedDaoConfig.initIdentityScope(type);

        articleDaoConfig = daoConfigMap.get(ArticleDao.class).clone();
        articleDaoConfig.initIdentityScope(type);

        imgDaoConfig = daoConfigMap.get(ImgDao.class).clone();
        imgDaoConfig.initIdentityScope(type);

        requestLogDaoConfig = daoConfigMap.get(RequestLogDao.class).clone();
        requestLogDaoConfig.initIdentityScope(type);

        tagDao = new TagDao(tagDaoConfig, this);
        feedDao = new FeedDao(feedDaoConfig, this);
        articleDao = new ArticleDao(articleDaoConfig, this);
        imgDao = new ImgDao(imgDaoConfig, this);
        requestLogDao = new RequestLogDao(requestLogDaoConfig, this);

        registerDao(Tag.class, tagDao);
        registerDao(Feed.class, feedDao);
        registerDao(Article.class, articleDao);
        registerDao(Img.class, imgDao);
        registerDao(RequestLog.class, requestLogDao);
    }
    
    public void clear() {
        tagDaoConfig.clearIdentityScope();
        feedDaoConfig.clearIdentityScope();
        articleDaoConfig.clearIdentityScope();
        imgDaoConfig.clearIdentityScope();
        requestLogDaoConfig.clearIdentityScope();
    }

    public TagDao getTagDao() {
        return tagDao;
    }

    public FeedDao getFeedDao() {
        return feedDao;
    }

    public ArticleDao getArticleDao() {
        return articleDao;
    }

    public ImgDao getImgDao() {
        return imgDao;
    }

    public RequestLogDao getRequestLogDao() {
        return requestLogDao;
    }

}
