package me.wizos.loread.data;

import android.database.Cursor;

import com.socks.library.KLog;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.Tag;
import me.wizos.loread.db.dao.ArticleDao;
import me.wizos.loread.db.dao.DaoSession;
import me.wizos.loread.db.dao.FeedDao;
import me.wizos.loread.db.dao.TagDao;
import me.wizos.loread.net.Api;

/**
 * @author Wizos on 2016/3/12.
 */
public class WithDB<T> {
    private static WithDB withDB;
    public TagDao tagDao;
    public FeedDao feedDao;
    public ArticleDao articleDao;

    private WithDB() {}


    public static WithDB i() {
        if (withDB == null) { // 双重锁定，只有在 withDB 还没被初始化的时候才会进入到下一行，然后加上同步锁
            synchronized (WithDB.class) { // 同步锁，避免多线程时可能 new 出两个实例的情况
                if (withDB == null) {
                    withDB = new WithDB();
                    withDB.tagDao = App.i().getDaoSession().getTagDao();
                    withDB.feedDao = App.i().getDaoSession().getFeedDao();
                    withDB.articleDao = App.i().getDaoSession().getArticleDao();
                }
            }
        }
        return withDB;
    }

    public DaoSession getDaoSession() {
        return App.i().getDaoSession();
    }


    // 异步查询
//    public void query(final String streamId, final DBInterface.Query dbCallBack ){
//        AsyncSession asyncSession = App.i().getDaoSession().startAsyncSession();
//        asyncSession.setListenerMainThread(new AsyncOperationListener() {
//            @Override
//            public void onAsyncOperationCompleted(AsyncOperation operation) {
//                dbCallBack.onQuerySuccess( streamId, ((List) operation.getResult()).size());
//            }
//        });
//        Query<Article> query = articleDao.queryBuilder().where(ArticleDao.Properties.ReadState.notEq(Api.ART_READED), ArticleDao.Properties.OriginStreamId.eq(streamId)).build();
//        asyncSession.queryList(query);
//    }



    public void delTag(Tag tag) {
        if (tag == null) {
            return;
        }
        tagDao.delete(tag);
    }

    public void insertTag(Tag tag) {
        if (tag == null) {
            return;
        }
        tagDao.insertOrReplace(tag);
    }

    // 自己写的
    public void saveTag(Tag tag) {
        if (tag == null) {
            return;
        }
        if (tagDao.queryBuilder().where(TagDao.Properties.Id.eq(tag.getId())).listLazy().size() == 0) {
            tagDao.insertOrReplace(tag);
        } else { // already exist
            tagDao.update(tag);
        }
    }
    // 自己写的
    public void coverSaveTags(List<Tag> tags) {
        tagDao.deleteAll();
        tagDao.insertOrReplaceInTx(tags);
    }

//    private List<Tag> getAllTags(List<Tag> tags) {
//        Tag rootTag = new Tag();
//        Tag noLabelTag = new Tag();
//        long userID = WithPref.i().getUseId();
//        rootTag.setTitle("所有文章");
//        noLabelTag.setTitle("未分类");
//
//        rootTag.setId("\"user/" + userID + Api.U_READING_LIST + "\"");
//        rootTag.setSortid("00000000");
//        rootTag.__setDaoSession(App.i().getDaoSession());
//
//        noLabelTag.setId("\"user/" + userID + Api.U_NO_LABEL + "\"");
//        noLabelTag.setSortid("00000001");
//        noLabelTag.__setDaoSession(App.i().getDaoSession());
//
//        tags.add(0, noLabelTag);
//        tags.add(0, rootTag);
//
//        KLog.d("【listTag】 " + rootTag.toString());
//        return tags;
//    }




    public List<Tag> getTags() {
        List<Tag> tagList = tagDao.loadAll();
        Collections.sort(tagList, new Comparator<Tag>() {
            @Override
            public int compare(Tag o1, Tag o2) {
                return Integer.valueOf(o1.getSortid()).compareTo(Integer.valueOf(o2.getSortid())); // 待优化，可以直接把 Sortid字段改为int型
            }
        });
        return tagList;
    }
//    public List<Tag> getTags() {
//        QueryBuilder<Tag> tags = tagDao.queryBuilder().orderAsc(TagDao.Properties.Sortid);
//        return tags.listLazy();
//    }

    public Tag getTag(String tagId) {
        return tagDao.queryBuilder().where(TagDao.Properties.Id.eq(tagId)).unique();
    }



    public List<Feed> getFeeds() {
        return feedDao.loadAll();
    }

    public Feed getFeed(String id) {
        return feedDao.queryBuilder().where(FeedDao.Properties.Id.eq(id)).unique();
    }

    public List<Feed> getFeeds(List<String> ids) {
        return feedDao.queryBuilder().where(FeedDao.Properties.Id.in(ids)).build().listLazy();
    }

    public void delTags(List<Tag> tags) {
        if (tags == null) {
            return;
        }
        tagDao.deleteInTx(tags);
    }

    public void delFeeds(List<Feed> feeds) {
        if (feeds == null) {
            return;
        }
        feedDao.deleteInTx(feeds);
    }
    public void delFeed(String feedId) {
        if (feedId == null) {
            return;
        }
        feedDao.deleteByKeyInTx(feedId);
    }
    public void delFeed(Feed feed) {
        if (feed == null) {
            return;
        }
        feedDao.deleteInTx(feed);
    }

    public void unsubscribeFeed(Feed feed) {
        if (feed == null) {
            return;
        }
        List<Article> articles = getArtsInFeedToDel(feed.getId());
        articleDao.deleteInTx(articles);
        feedDao.deleteInTx(feed);
    }

    public void addFeed(Feed feed) {
        if (feed != null) {
            feedDao.insertInTx(feed);
        }
    }

    public void saveFeed(Feed feed) {
        feedDao.update(feed);
    }

    public void saveFeeds(List<Feed> feeds) {
        feedDao.insertOrReplaceInTx(feeds);
    }
    public void coverSaveFeeds(List<Feed> feeds) {
        feedDao.deleteAll();
        feedDao.insertOrReplaceInTx(feeds);
    }

    public void coverSaveFeeds2(List<Feed> feeds) {
        feedDao.updateInTx(feeds);
    }

    public void updateArtsFeedTitle(Feed feed) {
        if (feed == null) {
            return;
        }
        QueryBuilder q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.OriginStreamId.eq(feed.getId()));
        List<Article> articles = q.list();
        for (Article article : articles) {
            article.setOriginTitle(feed.getTitle());
        }
        articleDao.updateInTx(articles);
    }
    public void updateFeed(Feed feed) {
        if (feed != null) {
            feedDao.update(feed);
        }
    }

    public void updateFeedsCategoryId(String sourceTagId, String destTagId) {
        QueryBuilder q = feedDao.queryBuilder()
                .where(FeedDao.Properties.Categoryid.eq(sourceTagId));
        List<Feed> feeds = q.list();
        for (Feed feed : feeds) {
            feed.setCategoryid(destTagId);
        }
        feedDao.updateInTx(feeds);

//        String queryString = "SELECT " + ArticleDao.TABLENAME + ".* FROM "
//                + ArticleDao.TABLENAME + " LEFT JOIN " + FeedDao.TABLENAME + " ON " + ArticleDao.TABLENAME + "." + ArticleDao.Properties.OriginStreamId.columnName + " = " + FeedDao.TABLENAME + "." + FeedDao.Properties.Id.columnName
//                + " WHERE "
//                + ArticleDao.TABLENAME + "." + ArticleDao.Properties.StarState.columnName + " = \"" + Api.ART_STARED + "\" AND "
//                + "(" + FeedDao.TABLENAME + "." + FeedDao.Properties.Id.columnName + " IS NULL OR " + FeedDao.TABLENAME + "." + FeedDao.Properties.Categoryid.columnName + " IS NULL )"
//                + "ORDER BY " + ArticleDao.Properties.TimestampUsec.columnName + " DESC";
    }


    public void saveArticle(Article article) {
        if (article.getId() != null) {
            articleDao.insertOrReplace(article);
//            articleDao.update(article);
        }
    }

    public void updateArticle(Article article) {
        if (article.getId() != null) {
            articleDao.update(article);
        }
    }

    public void saveArticles(List<Article> articleList) {
        if (articleList != null && articleList.size() != 0) { // new fetch
            articleDao.insertOrReplaceInTx(articleList);
        }
    }


    public Article getArticle(String articleId) {
        KLog.e("要获取的文章是：" + articleId);
        return articleDao.queryBuilder().where(ArticleDao.Properties.Id.eq(articleId)).unique();
    }


    public void setReaded(Article article) {
        if (article == null) {
            return;
        }
//        KLog.e("未读数A：" + getFeed(article.getOriginStreamId()).getUnreadCount() + "   " + article.getReadStatus());
        article.setReadStatus(Api.READED);
        updateArticle(article);

//        KLog.e("未读数B：" + getFeed(article.getOriginStreamId()).getUnreadCount());
//        KLog.e("未读数C：" + getCount(article.getOriginStreamId()));
        Feed feed = getFeed(article.getOriginStreamId());
        if (feed == null) {
            return;
        }
        feed.setUnreadCount(feed.getUnreadCount() - 1);
        saveFeed(feed);

//        Tag tag = getTag(feed.getCategoryid());
//        if (tag == null) {
//            return;
//        }
//        tag.setUnreadCount(tag.getUnreadCount() - 1);
//        saveTag(tag);
    }

    public void setUnread(Article article) {
        if (article == null) {
            return;
        }

        article.setReadStatus(Api.UNREAD);
        updateArticle(article);

        Feed feed = getFeed(article.getOriginStreamId());
        if (feed == null) {
            return;
        }
        feed.setUnreadCount(feed.getUnreadCount() + 1);
        saveFeed(feed);

//        Tag tag = getTag(feed.getCategoryid());
//        if (tag == null) {
//            return;
//        }
//        tag.setUnreadCount(tag.getUnreadCount() + 1);
//        saveTag(tag);
    }


    public void setUnreading(Article article) {
        if (article == null) {
            return;
        }
        int offest = 1;
        if (article.getReadStatus() == Api.UNREAD || article.getReadStatus() == Api.UNREADING) {
            offest = 0;
        }

        article.setReadStatus(Api.UNREADING);
        updateArticle(article);

        Feed feed = getFeed(article.getOriginStreamId());
        if (feed == null) {
            return;
        }

        feed.setUnreadCount(feed.getUnreadCount() + offest);
        saveFeed(feed);


//        Tag tag = getTag(feed.getCategoryid());
//        if (tag == null) {
//            return;
//        }
//        KLog.e("未读数的数值为：" + tag.getUnreadCount() + "   " + offest);
//        tag.setUnreadCount(tag.getUnreadCount() + offest);
//        saveTag(tag);
    }


    /**
    * 【升序】Collections.sort(list,Collator.i(java.util.Locale.CHINA));//注意：是根据的汉字的拼音的字母排序的，而不是根据汉字一般的排序方法
    * 【降序】Collections.reverse(list);//不指定排序规则时，也是按照字母的来排序的
    **/
   public void delArt(List<Article> articles) {
        if (articles.size() != 0) { // new fetch
            articleDao.deleteInTx( articles );
        }
    }


    /**
     * 获取状态为已阅读，未加星，小于X时间的文章，用于清理文章
     *
     * @param time 爬取时间戳
     * @return 文章列表
     */
    public List<Article> getArtInReadedUnstarLtTime(long time) {
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.where(q.and(ArticleDao.Properties.ReadStatus.eq(Api.READED), ArticleDao.Properties.StarStatus.eq(Api.UNSTAR), ArticleDao.Properties.CrawlTimeMsec.lt(time)));
        return q.listLazy();
    }


    public List<Article> getArtInReadedBox(long time) {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.ReadStatus.eq(Api.READED), ArticleDao.Properties.SaveDir.eq(Api.SAVE_DIR_BOX), ArticleDao.Properties.CrawlTimeMsec.lt(time));
        return q.listLazy();
    }


    public List<Article> getArtInReadedStore(long time) {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.ReadStatus.eq(Api.READED), ArticleDao.Properties.SaveDir.eq(Api.SAVE_DIR_STORE), ArticleDao.Properties.CrawlTimeMsec.lt(time));
        return q.listLazy();
    }

    /*
     * 文章列表页会有12种组合：某个 Categories 内的 UnRead[含UnReading], Stared, All。某个 OriginStreamId 内的 UnRead[含UnReading], Stared, All。
     * 所有定下来去获取文章的函数也有6个：getArtsUnreadInTag(), getArtsStaredInTag(), getArtsAllInTag(),getUnreadArtsInFeed(), getStaredArtsInFeed(), getAllArtsInFeed()
     */
    public List<Article> getSearchedArts(String keyword) {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.Title.like("%" + keyword + "%")) // , ArticleDao.Properties.Summary.like("%" + keyword + "%")
                .orderDesc(ArticleDao.Properties.Updated, ArticleDao.Properties.Published);
        return q.listLazy();
    }

    /**
     * 获取所有文章
     */
    public List<Article> getArtsAllNoOrder() { // 速度比要排序的全文更快
        return articleDao.loadAll();
    }


    public List<Article> getArtsAll() {
        long time = System.currentTimeMillis() - WithPref.i().getClearBeforeDay() * 24 * 3600 * 1000L - 300 * 1000L;
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.whereOr(ArticleDao.Properties.ReadStatus.notEq(Api.READED), ArticleDao.Properties.StarStatus.eq(Api.STARED), q.and(ArticleDao.Properties.ReadStatus.eq(Api.READED), ArticleDao.Properties.CrawlTimeMsec.gt(time)));
        q.orderDesc(ArticleDao.Properties.Updated, ArticleDao.Properties.Published);
        return q.listLazy();
    }

    public List<Article> getArtsUnreading() {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.ReadStatus.eq(Api.UNREADING));
        return q.listLazy();
    }

    /**
     * 获取所有加星的文章
     */
    public List<Article> getArtsStared() {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.StarStatus.eq(Api.STARED))
                .orderDesc(ArticleDao.Properties.Updated, ArticleDao.Properties.Published);
        return q.listLazy();
    }

    public List<Article> getArtsUnread() {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.ReadStatus.notEq(Api.READED))
                .orderDesc(ArticleDao.Properties.Updated, ArticleDao.Properties.Published);
        return q.listLazy();
    }

    public List<Article> getArtsUnreadNoOrder() {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.ReadStatus.notEq(Api.READED));
        return q.listLazy();
    }


    public List<Article> getArtsUnreadInTag(Tag tag) {
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.join(ArticleDao.Properties.OriginStreamId, Feed.class).where(FeedDao.Properties.Categoryid.eq(tag.getId()));
        q.where(ArticleDao.Properties.ReadStatus.notEq(Api.READED));
        q.orderDesc(ArticleDao.Properties.Updated, ArticleDao.Properties.Published);
        return q.listLazy();
    }

    public List<Article> getArtsStaredInTag(Tag tag) {
        KLog.e("getArtsStaredInTag2", Api.STARED + tag.getId());
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.join(ArticleDao.Properties.OriginStreamId, Feed.class).where(FeedDao.Properties.Categoryid.eq(tag.getId()));
        q.where(ArticleDao.Properties.StarStatus.eq(Api.STARED));
        q.orderDesc(ArticleDao.Properties.Updated, ArticleDao.Properties.Published);
        return q.listLazy();
    }


    public List<Article> getArtsAllInTag(Tag tag) {
        // 最后的 300 * 1000L 是留前5分钟时间的不删除 WithPref.i().getClearBeforeDay()
        long time = System.currentTimeMillis() - WithPref.i().getClearBeforeDay() * 24 * 3600 * 1000L - 300 * 1000L;
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.join(ArticleDao.Properties.OriginStreamId, Feed.class).where(FeedDao.Properties.Categoryid.eq(tag.getId()));
        q.whereOr(ArticleDao.Properties.ReadStatus.notEq(Api.READED), ArticleDao.Properties.StarStatus.eq(Api.STARED), q.and(ArticleDao.Properties.ReadStatus.eq(Api.READED), ArticleDao.Properties.CrawlTimeMsec.gt(time)));
        q.orderDesc(ArticleDao.Properties.Updated, ArticleDao.Properties.Published);

        return q.listLazy();
    }


    public List<Article> getArtsAllInFeed(String streamId) {
        // 最后的 300 * 1000L 是留前5分钟时间的不删除 WithPref.i().getClearBeforeDay()
        long time = System.currentTimeMillis() - WithPref.i().getClearBeforeDay() * 24 * 3600 * 1000L - 300 * 1000L;
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.where(ArticleDao.Properties.OriginStreamId.eq(streamId));
        q.whereOr(ArticleDao.Properties.ReadStatus.notEq(Api.READED), ArticleDao.Properties.StarStatus.eq(Api.STARED), q.and(ArticleDao.Properties.ReadStatus.eq(Api.READED), ArticleDao.Properties.CrawlTimeMsec.gt(time)));
        q.orderDesc(ArticleDao.Properties.Updated, ArticleDao.Properties.Published);
        return q.listLazy();
    }

    public List<Article> getArtsInFeedToDel(String streamId) {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.StarStatus.eq(Api.UNSTAR), ArticleDao.Properties.OriginStreamId.eq(streamId));
        return q.listLazy();
    }

    public List<Article> getArtsUnreadInFeed(String streamId) {
        String queryString =
                "SELECT COUNT(1) AS UNREADCOUNT" +
                        "  FROM ARTICLE WHERE READ_STATUS != " + Api.READED + " AND ORIGIN_STREAM_ID = '" + streamId + "'";
        Cursor cursor = getDaoSession().getDatabase().rawQuery(queryString, new String[]{});
        if (cursor == null) {
            return null;
        }
        KLog.e("获取A：" + queryString);
        cursor.moveToNext();
        KLog.e("获取B：" + cursor.getInt(0));


        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.ReadStatus.notEq(Api.READED), ArticleDao.Properties.OriginStreamId.eq(streamId))
                .orderDesc(ArticleDao.Properties.Updated, ArticleDao.Properties.Published);
        KLog.e("获取C：" + q.listLazy().size());
        return q.listLazy();
    }

    public List<Article> getArtsStaredInFeed(String streamId) {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.StarStatus.eq(Api.STARED), ArticleDao.Properties.OriginStreamId.eq(streamId))
                .orderDesc(ArticleDao.Properties.Updated, ArticleDao.Properties.Published);
        return q.listLazy();
    }

    public ArrayList<Feed> getFeedsWithUnreadCount() {
        String queryString =
                "SELECT ID,TITLE,CATEGORYID,CATEGORYLABEL,SORTID,FIRSTITEMMSEC,URL,HTMLURL,ICONURL,OPEN_MODE,UNREADCOUNT,NEWEST_ITEM_TIMESTAMP_USEC" +
                        " FROM FEED_UNREAD_COUNT";
        Cursor cursor = getDaoSession().getDatabase().rawQuery(queryString, new String[]{});
        if (cursor == null) {
            return null;
        }

        ArrayList<Feed> feeds = new ArrayList<>();
        Feed feed;
        while (cursor.moveToNext()) {
            feed = new Feed();
            feed.setId(cursor.getString(0));
            feed.setTitle(cursor.getString(1));
            feed.setCategoryid(cursor.getString(2));
            feed.setCategorylabel(cursor.getString(3));
            feed.setSortid(cursor.getString(4));
            feed.setFirstitemmsec(cursor.getLong(5));
            feed.setUrl(cursor.getString(6));
            feed.setHtmlurl(cursor.getString(7));
            feed.setIconurl(cursor.getString(8));
            feed.setOpenMode(cursor.getString(9));
            feed.setUnreadCount(cursor.getInt(10));
            feed.setNewestItemTimestampUsec(cursor.getLong(11));
            feeds.add(feed);
        }
        cursor.close();
        return feeds;
    }


    public ArrayList<Feed> getFeedsWithStaredCount() {
        String queryString =
                "SELECT ID,TITLE,CATEGORYID,CATEGORYLABEL,SORTID,FIRSTITEMMSEC,URL,HTMLURL,ICONURL,OPEN_MODE,NEWEST_ITEM_TIMESTAMP_USEC,COUNT" +
                        "  FROM FEED" +
                        "  LEFT JOIN (SELECT COUNT(1) AS COUNT, ORIGIN_STREAM_ID" +
                        "  FROM ARTICLE WHERE STAR_STATUS == " + Api.STARED + " GROUP BY ORIGIN_STREAM_ID)" +
                        "  ON ID = ORIGIN_STREAM_ID";
        Cursor cursor = getDaoSession().getDatabase().rawQuery(queryString, new String[]{});
        if (cursor == null) {
            return null;
        }

        ArrayList<Feed> feeds = new ArrayList<>();
        Feed feed;
        while (cursor.moveToNext()) {
            feed = new Feed();
            feed.setId(cursor.getString(0));
            feed.setTitle(cursor.getString(1));
            feed.setCategoryid(cursor.getString(2));
            feed.setCategorylabel(cursor.getString(3));
            feed.setSortid(cursor.getString(4));
            feed.setFirstitemmsec(cursor.getLong(5));
            feed.setUrl(cursor.getString(6));
            feed.setHtmlurl(cursor.getString(7));
            feed.setIconurl(cursor.getString(8));
            feed.setOpenMode(cursor.getString(9));
            feed.setNewestItemTimestampUsec(cursor.getLong(10));
            feed.setUnreadCount(cursor.getInt(11));
            feeds.add(feed);
        }
        cursor.close();
        return feeds;
    }


    public ArrayList<Tag> getTagsWithUnreadCount() {
        String queryString = "SELECT ID,TITLE,SORTID,NEWEST_ITEM_TIMESTAMP_USEC,UNREADCOUNT FROM TAG_UNREAD_COUNT";
//        KLog.e("测getArtsAllNoTag2：" + queryString);
        Cursor cursor = getDaoSession().getDatabase().rawQuery(queryString, new String[]{});
        if (cursor == null) {
            return null;
        }
        ArrayList<Tag> tags = new ArrayList<>(cursor.getCount());
        Tag tag;
        while (cursor.moveToNext()) {
            tag = new Tag();
            tag.setId(cursor.getString(0));
            tag.setTitle(cursor.getString(1));
            tag.setSortid(cursor.getString(2));
            tag.setNewestItemTimestampUsec(cursor.getLong(3));
            tag.setUnreadCount(cursor.getInt(4));
            tag.__setDaoSession(App.i().getDaoSession());
            tags.add(tag);
            KLog.e("标题：" + tag.getTitle() + " , " + tag.getUnreadCount());
        }
        cursor.close();
        return tags;
    }


    public ArrayList<Tag> getTagsWithStaredCount() {
        String queryString = "SELECT ID,TITLE,SORTID,NEWEST_ITEM_TIMESTAMP_USEC,COUNT FROM TAG" +
                "  LEFT JOIN (SELECT CATEGORYID,SUM(UNREAD_COUNT) AS COUNT FROM FEED GROUP BY CATEGORYID )  ON ID = CATEGORYID";
        Cursor cursor = getDaoSession().getDatabase().rawQuery(queryString, new String[]{});
        if (cursor == null) {
            return null;
        }
        ArrayList<Tag> tags = new ArrayList<>(cursor.getCount());
        Tag tag;
        while (cursor.moveToNext()) {
            tag = new Tag();
            tag.setId(cursor.getString(0));
            tag.setTitle(cursor.getString(1));
            tag.setSortid(cursor.getString(2));
            tag.setNewestItemTimestampUsec(cursor.getLong(3));
            tag.setUnreadCount(cursor.getInt(4));
            tag.__setDaoSession(App.i().getDaoSession());
            tags.add(tag);
            KLog.e("标题：" + tag.getTitle() + " , " + tag.getUnreadCount());
        }
        cursor.close();
        return tags;
    }



    public int getUnreadArtsCount() {
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.where(ArticleDao.Properties.ReadStatus.notEq(Api.READED));
        return (int) q.buildCount().count();
    }

    public int getUnreadArtsCountNoTag() {
        String queryString = "SELECT count(*) FROM"
                + " ARTICLE LEFT JOIN FEED ON ARTICLE.ORIGIN_STREAM_ID = FEED.ID"
                + " WHERE ARTICLE.READ_STATUS != " + Api.READED + " AND"
                + " FEED.CATEGORYID = \"user/" + WithPref.i().getUseId() + Api.U_NO_LABEL + "\"";
//        KLog.e("测getUnreadArtsCountNoTag：" + queryString);
        Cursor cursor = articleDao.getDatabase().rawQuery(queryString, new String[]{});
        if (cursor == null) {
            return 0;
        }
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

//    public int getStaredArtsCountNoTag() {
//        // SELECT count(*) FROM Article LEFT JOIN Feed ON Article.ORIGIN_STREAM_ID = Feed.Id WHERE Article.Star_State like "Stared"  AND ( Feed.Categoryid IS "user/1006097346/state/com.google/no-label" OR Feed.Categoryid IS NULL );
//        String queryString = "SELECT count(*) FROM "
//                + ArticleDao.TABLENAME + " LEFT JOIN " + FeedDao.TABLENAME + " ON " + ArticleDao.TABLENAME + "." + ArticleDao.Properties.OriginStreamId.columnName + " = " + FeedDao.TABLENAME + "." + FeedDao.Properties.Id.columnName
//                + " WHERE "
//                + ArticleDao.TABLENAME + "." + ArticleDao.Properties.StarState.columnName + " = \"" + Api.ART_STARED + "\" AND "
//                + "(" + FeedDao.TABLENAME + "." + FeedDao.Properties.Id.columnName + " IS NULL OR " + FeedDao.TABLENAME + "." + FeedDao.Properties.Categoryid.columnName + " IS \"user/" + WithPref.i().getUseId() + Api.U_NO_LABEL + "\" )";
////        KLog.e("测getStaredArtsCountNoTag：" + queryString);
//        Cursor cursor = articleDao.getDatabase().rawQuery(queryString, new String[]{});
//        if (cursor == null) {
//            return 0;
//        }
//        cursor.moveToFirst();
//        int count = cursor.getInt(0);
//        cursor.close();
//        return count;
//    }
//
//    public int getAllArtsCountNoTag() {
//        // 在未分类的文章有以下几种：
//        // 1.有订阅源，但是订阅源没有tag（未读的时候，还有一个条件是文章的状态为unread）
//        // 2.没有订阅源，但是是加星的（加星的时候）
//        // （全部的时候是他们的合集）
//        // 查询在一张表不在另外一张表的记录：http://blog.csdn.net/c840898727/article/details/42238363
//        // select ta.* from ta left join tb on ta.id=tb.id where tb.id is null
//        String queryString = "SELECT count(*) FROM "
//                + ArticleDao.TABLENAME + " LEFT JOIN " + FeedDao.TABLENAME + " ON " + ArticleDao.TABLENAME + "." + ArticleDao.Properties.OriginStreamId.columnName + " = " + FeedDao.TABLENAME + "." + FeedDao.Properties.Id.columnName
//                + " WHERE "
//                + FeedDao.TABLENAME + "." + FeedDao.Properties.Id.columnName + " IS NULL OR " + FeedDao.TABLENAME + "." + FeedDao.Properties.Categoryid.columnName + " IS \"user/" + WithPref.i().getUseId() + Api.U_NO_LABEL + "\"";
////        KLog.e("测getAllArtsCountNoTag：" + queryString);
//        Cursor cursor = articleDao.getDatabase().rawQuery(queryString, new String[]{});
//        if (cursor == null) {
//            return 0;
//        }
//        cursor.moveToFirst();
//        int count = cursor.getInt(0);
//        cursor.close();
//        return count;
//    }

    /**
     * 获取文章是否有重复
     *
     * @return
     */
    public List<Article> getDuplicateArticle() {
        String queryString = "select * from ARTICLE group by CANONICAL,TITLE having count(*) > 1";
        Cursor cursor = articleDao.getDatabase().rawQuery(queryString, new String[]{});
        if (cursor == null) {
            return new ArrayList<>();
        }
        List<Article> articles = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            articles.add(genArticle(new Article(), cursor));
        }
        cursor.close();
        return articles;
    }

    public List<Article> getDuplicateArticle(String title, String href) {
        return articleDao.queryBuilder().
                where(ArticleDao.Properties.Title.eq(title), ArticleDao.Properties.Canonical.eq(href))
                .orderAsc(ArticleDao.Properties.Updated).list();

    }


    public List<Article> getArtsUnreadNoTag() {
        String queryString = "SELECT ARTICLE.* FROM "
                + "ARTICLE LEFT JOIN FEED ON ARTICLE.ORIGIN_STREAM_ID = FEED.ID"
                + " WHERE ARTICLE.READ_STATUS != " + Api.READED + " AND"
                + " FEED.CATEGORYID = \"user/" + WithPref.i().getUseId() + Api.U_NO_LABEL + "\""
                + " ORDER BY UPDATED,PUBLISHED DESC";
//        KLog.e("getArtsUnreadNoTag：" + queryString);
        Cursor cursor = articleDao.getDatabase().rawQuery(queryString, new String[]{});
        if (cursor == null) {
            return new ArrayList<>();
        }
        List<Article> articles = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            articles.add(genArticle(new Article(), cursor));
        }
        cursor.close();
        return articles;
    }


    // 为分类的加星文章包含两部分：1.是在订阅中，但是没有分类的加星文章；2.是不在订阅中，加星的文章
    public List<Article> getArtsStaredNoTag() {
        // select ta.* from ta left join tb on ta.id=tb.id where tb.id is null
        String queryString = "SELECT ARTICLE.* FROM "
                + "ARTICLE LEFT JOIN FEED ON ARTICLE.ORIGIN_STREAM_ID = FEED.ID"
                + " WHERE ARTICLE.STAR_STATUS = " + Api.STARED + " AND "
                + " (FEED.ID IS NULL OR FEED.CATEGORYID = \"user/" + WithPref.i().getUseId() + Api.U_NO_LABEL + "\" )"
                + " ORDER BY UPDATED,PUBLISHED DESC";
//        KLog.e("测getArtsStaredNoTag2：" + queryString);
        Cursor cursor = articleDao.getDatabase().rawQuery(queryString, new String[]{});
        if (cursor == null) {
            return new ArrayList<>();
        }
        List<Article> articles = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            articles.add(genArticle(new Article(), cursor));
        }
        cursor.close();
        return articles;
    }

    // 未分类的所有文章包含两部分：1.是在订阅中，但是没有分类的文章；2.是不在订阅中，加星的文章
    public List<Article> getArtsAllNoTag() {
        String queryString = "SELECT ARTICLE.* FROM "
                + "ARTICLE LEFT JOIN FEED ON ARTICLE.ORIGIN_STREAM_ID = FEED.ID "
                + "WHERE FEED.ID IS NULL OR FEED.CATEGORYID = \"user/" + WithPref.i().getUseId() + Api.U_NO_LABEL + "\" "
                + "ORDER BY UPDATED,PUBLISHED DESC";
//        KLog.e("测getArtsAllNoTag2：" + queryString);
        Cursor cursor = articleDao.getDatabase().rawQuery(queryString, new String[]{});
        if (cursor == null) {
            return new ArrayList<>();
        }
        List<Article> articles = new ArrayList<>(cursor.getCount());
        while (cursor.moveToNext()) {
            articles.add(genArticle(new Article(), cursor));
        }
        cursor.close();
        return articles;
    }




    private Article genArticle(Article article, Cursor cursor) {
        article.setId(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.Id.columnName)));
        article.setCrawlTimeMsec(cursor.getLong(cursor.getColumnIndex(ArticleDao.Properties.CrawlTimeMsec.columnName)));
        article.setTimestampUsec(cursor.getLong(cursor.getColumnIndex(ArticleDao.Properties.TimestampUsec.columnName)));
        article.setCategories(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.Categories.columnName)));
        article.setTitle(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.Title.columnName)));
        article.setPublished(cursor.getLong(cursor.getColumnIndex(ArticleDao.Properties.Published.columnName)));
        article.setUpdated(cursor.getLong(cursor.getColumnIndex(ArticleDao.Properties.Updated.columnName)));
        article.setStarred(cursor.getLong(cursor.getColumnIndex(ArticleDao.Properties.Starred.columnName)));
        article.setEnclosure(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.Enclosure.columnName)));
        article.setCanonical(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.Canonical.columnName)));
        article.setAlternate(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.Alternate.columnName)));
        article.setSummary(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.Summary.columnName)));
        article.setContent(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.Content.columnName)));
        article.setAuthor(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.Author.columnName)));
        article.setReadState(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.ReadState.columnName)));
        article.setStarState(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.StarState.columnName)));
        article.setReadStatus(cursor.getInt(cursor.getColumnIndex(ArticleDao.Properties.ReadStatus.columnName)));
        article.setStarStatus(cursor.getInt(cursor.getColumnIndex(ArticleDao.Properties.StarStatus.columnName)));
        article.setSaveDir(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.SaveDir.columnName)));
        article.setImgState(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.ImgState.columnName)));
        article.setCoverSrc(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.CoverSrc.columnName)));
        article.setOriginStreamId(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.OriginStreamId.columnName)));
        article.setOriginTitle(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.OriginTitle.columnName)));
        article.setOriginHtmlUrl(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.OriginHtmlUrl.columnName)));
        return article;
    }

    public void clear() {
        tagDao.deleteAll();
        feedDao.deleteAll();
        articleDao.deleteAll();
    }
}
