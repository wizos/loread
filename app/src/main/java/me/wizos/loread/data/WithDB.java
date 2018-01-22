package me.wizos.loread.data;

import android.database.Cursor;
import android.support.v4.util.ArrayMap;

import com.socks.library.KLog;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.bean.Article;
import me.wizos.loread.bean.Feed;
import me.wizos.loread.bean.Img;
import me.wizos.loread.bean.RequestLog;
import me.wizos.loread.bean.Tag;
import me.wizos.loread.data.dao.ArticleDao;
import me.wizos.loread.data.dao.FeedDao;
import me.wizos.loread.data.dao.ImgDao;
import me.wizos.loread.data.dao.RequestLogDao;
import me.wizos.loread.data.dao.TagDao;
import me.wizos.loread.net.Api;

/**
 * Created by Wizos on 2016/3/12.
 */
public class WithDB {
    private static WithDB withDB;
    private TagDao tagDao;
    private FeedDao feedDao;
    private ArticleDao articleDao;
    private RequestLogDao requestLogDao;
    private ImgDao imgDao;

    private WithDB() {}


    public static WithDB i() {
        if (withDB == null) { // 双重锁定，只有在 withDB 还没被初始化的时候才会进入到下一行，然后加上同步锁
            synchronized (WithDB.class) { // 同步锁，避免多线程时可能 new 出两个实例的情况
                if (withDB == null) {
                    // All init here
                    withDB = new WithDB();
                    withDB.tagDao = App.i().getDaoSession().getTagDao();
                    withDB.feedDao = App.i().getDaoSession().getFeedDao();
                    withDB.articleDao = App.i().getDaoSession().getArticleDao();
                    withDB.requestLogDao = App.i().getDaoSession().getRequestLogDao();
                    withDB.imgDao = App.i().getDaoSession().getImgDao();
//                    withDB.statisticDao = App.i().getDaoSession().getStatisticDao();
                }
            }
        }
        return withDB;
    }

//    // 自己写的
//    public void saveStatisticList(List<Statistic> statistics) {
//        statisticDao.insertOrReplaceInTx(statistics);
//    }
//
//    public Statistic getStatistic(String id) {
//        List<Statistic> statistics = statisticDao.queryBuilder().where(StatisticDao.Properties.Id.eq(id)).listLazy();
//        if (statistics.size() != 0) {
//            return statistics.get(0);
//        } else {
//            return null;
//        }
//    }
//
//
//    public void saveStatistic(Statistic statistics) {
//        statisticDao.insertOrReplaceInTx(statistics);
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
    public void coverSaveTagList(List<Tag> tags) {
        tagDao.deleteAll();
        tagDao.insertOrReplaceInTx(tags);
    }

//    private List<Tag> getAllTags(List<Tag> tags) {
//        Tag rootTag = new Tag();
//        Tag noLabelTag = new Tag();
//        long userID = WithSet.i().getUseId();
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
        List<Tag> tags = tagDao.queryBuilder().where(TagDao.Properties.Id.eq(tagId)).listLazy();
        if (tags.size() != 0) {
            return tags.get(0);
        } else {
            return null;
        }
    }


    // 这里很慢
    public void saveImgs(ArrayMap<Integer, Img> imgMap) {
        for (Map.Entry<Integer, Img> entry : imgMap.entrySet()) {
            if (imgDao.queryBuilder().where(ImgDao.Properties.ArticleId.eq(entry.getValue().getArticleId()), ImgDao.Properties.No.eq(entry.getValue().getNo())).listLazy().size() == 0) {
                imgDao.insertOrReplace(entry.getValue());
            }
        }
    }

    public List<Feed> getFeeds() {
        return feedDao.loadAll();
    }
//    public int getFeedsByTag(Tag tag) {
//        QueryBuilder<Feed> q = feedDao.queryBuilder();
//        q.where(FeedDao.Properties.Categoryid.eq(tag.getId()));
//        QueryBuilder<Article> aq = articleDao.queryBuilder();
//        aq.where(ArticleDao.Properties.OriginStreamId.eq())
//        q.orderDesc();
//        return (int)q.buildCount().count();
//    }
//    public List<Feed> getFeedsOrderByArtsCount( Tag tag ) {
//        // select ta.* from ta left join tb on ta.id=tb.id where tb.id is null
//        String queryString = "SELECT * FROM " + FeedDao.TABLENAME
//                + " WHERE " + FeedDao.Properties.Categoryid.columnName + " = " + tag.getId()
//                + " ORDER BY SELECT COUNT(0) FROM " + ArticleDao.Properties.TimestampUsec.columnName + " DESC";
//        KLog.e("测getArtsStaredNoTag2：" + queryString );
//        Cursor cursor = articleDao.getDatabase().rawQuery(queryString, new String[]{});
//        if (cursor == null) {
//            return new ArrayList<>();
//        }
//        List<Article> articles = new ArrayList<>(cursor.getCount());
//        while (cursor.moveToNext()) {
//            articles.add(genArticle(new Article(), cursor));
//        }
//        cursor.close();
//        return articles;
//    }

    public int getFeedsCountByTag(Tag tag) {
        QueryBuilder<Feed> q = feedDao.queryBuilder();
        q.where(FeedDao.Properties.Categoryid.eq(tag.getId()));
        return (int) q.buildCount().count();
    }

    public Feed getFeed(String id) {
        List<Feed> feeds = feedDao.queryBuilder().where(FeedDao.Properties.Id.eq(id)).listLazy();
        if (feeds.size() != 0) {
            return feeds.get(0);
        } else {
            return null;
        }
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

    public void addFeed(Feed feed) {
        if (feed != null) {
            feedDao.insertInTx(feed);
        }
    }
    public void coverSaveFeeds(List<Feed> feeds) {
        feedDao.deleteAll();
        feedDao.insertOrReplaceInTx(feeds);
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



    public void saveImg(ArrayMap<Integer, Img> imgMap) {
        for (Map.Entry<Integer, Img> entry : imgMap.entrySet()) {
            imgDao.insertOrReplace(entry.getValue());
        }
    }

    public void saveImg(Img img) {
        imgDao.insertOrReplace(img);
    }

    public Img getImg(String articleId, int imgNo) {
        List<Img> imgs = imgDao.queryBuilder()
                .where(ImgDao.Properties.ArticleId.eq(articleId), ImgDao.Properties.No.eq(imgNo)).listLazy();
        if (imgs.size() != 0) {
            return imgs.get(0);
        } else {
            return null;
        }
    }

    public Img getImg(String articleId, String src) {
        List<Img> imgs = imgDao.queryBuilder()
                .where(ImgDao.Properties.ArticleId.eq(articleId), ImgDao.Properties.Src.eq(src)).listLazy();
        if (imgs.size() != 0) {
            return imgs.get(0);
        } else {
            return null;
        }
    }


    public List<Img> getObtainImgs(String articleId) {
        QueryBuilder<Img> q = imgDao.queryBuilder()
                .where(ImgDao.Properties.ArticleId.eq(articleId), ImgDao.Properties.DownState.eq(Api.ImgMeta_Downover)).orderAsc(ImgDao.Properties.No);
        return q.listLazy();
    }

    public List<Img> getLossImgs(String articleId) {
        QueryBuilder<Img> q = imgDao.queryBuilder()
                .where(ImgDao.Properties.ArticleId.eq(articleId), ImgDao.Properties.DownState.eq(Api.ImgMeta_Downing)).orderAsc(ImgDao.Properties.No);
        return q.listLazy();
    }

    private List<Img> getImgs(String articleId) { // ,int imgType
        QueryBuilder<Img> q = imgDao.queryBuilder()
                .where(ImgDao.Properties.ArticleId.eq(articleId));
        return q.listLazy();
    }

    public void saveArticle(Article article) {
        if (article.getId() != null) {
            articleDao.insertOrReplace(article);
        }
    }

    public void saveArticleList(List<Article> articleList) {
        if (articleList.size() != 0) { // new fetch
            articleDao.insertOrReplaceInTx(articleList);
        }
    }


    public Article getArticle(String articleId) {
//        if (articleID == null) {return null;}
        List<Article> articles = articleDao.queryBuilder().where(ArticleDao.Properties.Id.eq(articleId)).listLazy();
        if ( articles.size() != 0) {
            return articles.get(0);
        }else {
            return null;
        }
    }

    public boolean isArticleExists(String articleTitle) {
        List<Article> articles = articleDao.queryBuilder().where(ArticleDao.Properties.Title.like("%" + articleTitle + "%")).listLazy();
        return articles.size() != 0;
    }

    public Article getStarredArticle(String articleId) {
//        if (articleID == null) {return null;}
        List<Article> articles = articleDao.queryBuilder().where(ArticleDao.Properties.Id.eq(articleId), ArticleDao.Properties.StarState.eq(Api.ART_STARED)).list();
        if (articles.size() != 0) {
            return articles.get(0);
        } else {
            return null;
        }
    }

    public long getArticleEchoes(String title, String href) {
        return articleDao.queryBuilder().where(ArticleDao.Properties.Title.eq(title), ArticleDao.Properties.Canonical.eq(href)).buildCount().count();
    }

    public boolean hasTag(String id) {
        return tagDao.queryBuilder().where(TagDao.Properties.Id.eq(id)).listLazy().size() > 0;
    }
    public boolean hasFeed(String url) {
        return feedDao.queryBuilder()
                .where(FeedDao.Properties.Url.eq(url)).listLazy().size() > 0;
    }

    public void saveRequestJSON(String requestJson) {
        if (requestJson == null) {
            return;
        }
//        requestLogDao.insertOrReplaceInTx( requestJson );
    }

    public void saveRequestLog( RequestLog  requestLog) {
        if(requestLog == null ){
            return;
        }
        requestLogDao.insertOrReplaceInTx( requestLog );
    }
    public void saveRequestLogList(ArrayList<RequestLog> requestLogList) {
        if (requestLogList.size() != 0) { // new fetch
            requestLogDao.insertOrReplaceInTx(requestLogList);
        }
    }

    public List<RequestLog> loadRequestListAll(){
        return  requestLogDao.loadAll();
    }
    public void delRequestListAll(){
        requestLogDao.deleteAll();
    }
    public void delRequest(RequestLog requestLog){
        requestLogDao.delete(requestLog);
    }


   /**
    * 升序
    * Collections.sort(list,Collator.i(java.util.Locale.CHINA));//注意：是根据的汉字的拼音的字母排序的，而不是根据汉字一般的排序方法
    *
    * 降序
    * Collections.reverse(list);//不指定排序规则时，也是按照字母的来排序的
    **/
   public void delArt(List<Article> articles) {
        if (articles.size() != 0) { // new fetch
            articleDao.deleteInTx( articles );
        }
    }

    public void delArticleImgs(List<Article> articles) {
        List<Img> imgs;
        for (Article article : articles) {
            imgs = getImgs(article.getId());
            KLog.e(imgs.size());
            imgDao.deleteInTx(imgs);
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
        q.where(q.and(ArticleDao.Properties.ReadState.eq(Api.ART_READED), ArticleDao.Properties.StarState.eq(Api.ART_UNSTAR), ArticleDao.Properties.CrawlTimeMsec.lt(time)));
        return q.listLazy();
    }


    /**
     * 获取状态为已阅读，保存位置为Box的文章，用于移动文章位置
     *
     * @return 文章列表
     */
    public List<Article> getArtInReadedBox() {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.ReadState.eq(Api.ART_READED), ArticleDao.Properties.SaveDir.eq(Api.SAVE_DIR_BOX));
        return q.listLazy();
    }

    /**
     * 获取状态为已阅读，保存位置为 Store 的文章，用于移动文章位置
     *
     * @return 文章列表
     */
    public List<Article> getArtInReadedStore() {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.ReadState.eq(Api.ART_READED), ArticleDao.Properties.SaveDir.eq(Api.SAVE_DIR_STORE));
        return q.listLazy();
    }


    /*
     * 文章列表页会有12种组合：某个 Categories 内的 UnRead[含UnReading], Stared, All。某个 OriginStreamId 内的 UnRead[含UnReading], Stared, All。
     * 所有定下来去获取文章的函数也有6个：getArtsUnreadInTag(), getArtsStaredInTag(), getArtsAllInTag(),getUnreadArtsInFeed(), getStaredArtsInFeed(), getAllArtsInFeed()
     */
    public List<Article> getSearchedArts(String keyword) {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.Title.like("%" + keyword + "%")) // , ArticleDao.Properties.Summary.like("%" + keyword + "%")
                .orderDesc(ArticleDao.Properties.TimestampUsec);
        return q.listLazy();
    }

    /**
     * 获取所有文章
     */
    public List<Article> loadAllArts() { // 速度比要排序的全文更快
        return articleDao.loadAll();
    }

    public List<Article> getArtsAll() {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .orderDesc(ArticleDao.Properties.TimestampUsec);
        return q.listLazy();
    }

    /**
     * 获取所有加星的文章
     */
    public List<Article> getArtsStared() {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.StarState.eq(Api.ART_STARED))
                .orderDesc(ArticleDao.Properties.TimestampUsec);
        return q.listLazy();
    }

    public List<Article> getArtsUnread() {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.ReadState.like(Api.ART_UNREAD + "%"))
                .orderDesc(ArticleDao.Properties.TimestampUsec);
        return q.listLazy();
    }

    public List<Article> getArtsUnhandle() { // 速度比要排序的全文更快
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.ImgState.isNull());
        return q.listLazy();
    }


    public List<Article> getArtsUnreadInTag(Tag tag) {
        KLog.e("getArtsUnreadInTag2", Api.ART_UNREAD + tag.getId());
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.join(ArticleDao.Properties.OriginStreamId, Feed.class).where(FeedDao.Properties.Categoryid.eq(tag.getId()));
        q.where(ArticleDao.Properties.ReadState.like(Api.ART_UNREAD + "%"));
        q.orderDesc(ArticleDao.Properties.TimestampUsec);
        return q.listLazy();
    }

    public List<Article> getArtsStaredInTag(Tag tag) {
        KLog.e("getArtsStaredInTag2", Api.ART_STARED + tag.getId());
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.join(ArticleDao.Properties.OriginStreamId, Feed.class).where(FeedDao.Properties.Categoryid.eq(tag.getId()));
        q.where(ArticleDao.Properties.StarState.eq(Api.ART_STARED));
        q.orderDesc(ArticleDao.Properties.TimestampUsec);
        return q.listLazy();
    }

    public List<Article> getArtsAllInTag(Tag tag) {
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.join(ArticleDao.Properties.OriginStreamId, Feed.class).where(FeedDao.Properties.Categoryid.eq(tag.getId()));
        q.orderDesc(ArticleDao.Properties.TimestampUsec);
        return q.listLazy();
    }


    public List<Article> getArtsAllInFeed(String streamId) {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.OriginStreamId.eq(streamId))
                .orderDesc(ArticleDao.Properties.TimestampUsec);
        return q.listLazy();
    }

    public List<Article> getArtsUnreadInFeed(String streamId) {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.ReadState.like(Api.ART_UNREAD + "%"), ArticleDao.Properties.OriginStreamId.eq(streamId)) /** Creates an "equal ('=')" condition  for this property. */
                .orderDesc(ArticleDao.Properties.TimestampUsec);
        return q.listLazy();
    }

    public List<Article> getArtsStaredInFeed(String streamId) {
        QueryBuilder<Article> q = articleDao.queryBuilder()
                .where(ArticleDao.Properties.StarState.eq(Api.ART_STARED), ArticleDao.Properties.OriginStreamId.eq(streamId)) /** Creates an "equal ('=')" condition  for this property. */
                .orderDesc(ArticleDao.Properties.TimestampUsec);
        return q.listLazy();
    }


    public int getUnreadArtsCountByTag(Tag tag) {
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.join(ArticleDao.Properties.OriginStreamId, Feed.class).where(FeedDao.Properties.Categoryid.eq(tag.getId()));
        q.where(ArticleDao.Properties.ReadState.like(Api.ART_UNREAD + "%"));
        return (int) q.buildCount().count();
    }

    public int getStaredArtsCountByTag(Tag tag) {
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.join(ArticleDao.Properties.OriginStreamId, Feed.class).where(FeedDao.Properties.Categoryid.eq(tag.getId()));
        q.where(ArticleDao.Properties.StarState.eq(Api.ART_STARED));
        return (int) q.buildCount().count();
    }

    public int getAllArtsCountByTag(Tag tag) {
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.join(ArticleDao.Properties.OriginStreamId, Feed.class).where(FeedDao.Properties.Categoryid.eq(tag.getId()));
        return (int) q.buildCount().count();
    }


    public int getUnreadArtsCountByFeed(String streamId) {
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.where(ArticleDao.Properties.ReadState.like(Api.ART_UNREAD + "%"), ArticleDao.Properties.OriginStreamId.eq(streamId));
        return (int) q.buildCount().count();
    }

    public int getStaredArtsCountByFeed(String streamId) {
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.where(ArticleDao.Properties.StarState.eq(Api.ART_STARED), ArticleDao.Properties.OriginStreamId.eq(streamId));
        return (int) q.buildCount().count();
    }

    public int getAllArtsCountByFeed(String streamId) {
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.where(ArticleDao.Properties.OriginStreamId.eq(streamId));
        return (int) q.buildCount().count();
    }


    public int getUnreadArtsCount() {
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.where(ArticleDao.Properties.ReadState.like(Api.ART_UNREAD + "%"));
        return (int) q.buildCount().count();
    }

    public int getStaredArtsCount() {
        QueryBuilder<Article> q = articleDao.queryBuilder();
        q.where(ArticleDao.Properties.StarState.eq(Api.ART_STARED));
        return (int) q.buildCount().count();
    }

    public int getAllArtsCount() {
        QueryBuilder<Article> q = articleDao.queryBuilder();
        return (int) q.buildCount().count();
    }


    public int getUnreadArtsCountNoTag() {
        String queryString = "SELECT count(*) FROM "
                + ArticleDao.TABLENAME + " LEFT JOIN " + FeedDao.TABLENAME + " ON " + ArticleDao.TABLENAME + "." + ArticleDao.Properties.OriginStreamId.columnName + " = " + FeedDao.TABLENAME + "." + FeedDao.Properties.Id.columnName
                + " WHERE "
                + ArticleDao.TABLENAME + "." + ArticleDao.Properties.ReadState.columnName + " like \"" + Api.ART_UNREAD + "%\""
                + " AND " + FeedDao.TABLENAME + "." + FeedDao.Properties.Categoryid.columnName + " IS \"user/" + WithSet.i().getUseId() + Api.U_NO_LABEL + "\"";
        KLog.e("测getUnreadArtsCountNoTag：" + queryString);
        Cursor cursor = articleDao.getDatabase().rawQuery(queryString, new String[]{});
        if (cursor == null) {
            return 0;
        }
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getStaredArtsCountNoTag() {
        // SELECT count(*) FROM Article LEFT JOIN Feed ON Article.ORIGIN_STREAM_ID = Feed.Id WHERE Article.Star_State like "Stared"  AND ( Feed.Categoryid IS "user/1006097346/state/com.google/no-label" OR Feed.Categoryid IS NULL );
        String queryString = "SELECT count(*) FROM "
                + ArticleDao.TABLENAME + " LEFT JOIN " + FeedDao.TABLENAME + " ON " + ArticleDao.TABLENAME + "." + ArticleDao.Properties.OriginStreamId.columnName + " = " + FeedDao.TABLENAME + "." + FeedDao.Properties.Id.columnName
                + " WHERE "
                + ArticleDao.TABLENAME + "." + ArticleDao.Properties.StarState.columnName + " = \"" + Api.ART_STARED + "\" AND "
                + "(" + FeedDao.TABLENAME + "." + FeedDao.Properties.Id.columnName + " IS NULL OR " + FeedDao.TABLENAME + "." + FeedDao.Properties.Categoryid.columnName + " IS \"user/" + WithSet.i().getUseId() + Api.U_NO_LABEL + "\" )";
        KLog.e("测getStaredArtsCountNoTag：" + queryString);
        Cursor cursor = articleDao.getDatabase().rawQuery(queryString, new String[]{});
        if (cursor == null) {
            return 0;
        }
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }

    public int getAllArtsCountNoTag() {
        // 在未分类的文章有以下几种：
        // 1.有订阅源，但是订阅源没有tag（未读的时候，还有一个条件是文章的状态为unread）
        // 2.没有订阅源，但是是加星的（加星的时候）
        // （全部的时候是他们的合集）
        // 查询在一张表不在另外一张表的记录：http://blog.csdn.net/c840898727/article/details/42238363
        // select ta.* from ta left join tb on ta.id=tb.id where tb.id is null
        String queryString = "SELECT count(*) FROM "
                + ArticleDao.TABLENAME + " LEFT JOIN " + FeedDao.TABLENAME + " ON " + ArticleDao.TABLENAME + "." + ArticleDao.Properties.OriginStreamId.columnName + " = " + FeedDao.TABLENAME + "." + FeedDao.Properties.Id.columnName
                + " WHERE "
                + FeedDao.TABLENAME + "." + FeedDao.Properties.Id.columnName + " IS NULL OR " + FeedDao.TABLENAME + "." + FeedDao.Properties.Categoryid.columnName + " IS \"user/" + WithSet.i().getUseId() + Api.U_NO_LABEL + "\"";
        KLog.e("测getAllArtsCountNoTag：" + queryString);
        Cursor cursor = articleDao.getDatabase().rawQuery(queryString, new String[]{});
        if (cursor == null) {
            return 0;
        }
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }


    public List<Article> getArtsUnreadNoTag() {
        // select ta.* from ta left join tb on ta.id=tb.id where tb.id is null
        String queryString = "SELECT " + ArticleDao.TABLENAME + ".* FROM "
                + ArticleDao.TABLENAME + " LEFT JOIN " + FeedDao.TABLENAME + " ON " + ArticleDao.TABLENAME + "." + ArticleDao.Properties.OriginStreamId.columnName + " = " + FeedDao.TABLENAME + "." + FeedDao.Properties.Id.columnName
                + " WHERE "
                + ArticleDao.TABLENAME + "." + ArticleDao.Properties.ReadState.columnName + " like \"" + Api.ART_UNREAD + "%\" AND "
                + FeedDao.TABLENAME + "." + FeedDao.Properties.Categoryid.columnName + " IS \"user/" + WithSet.i().getUseId() + Api.U_NO_LABEL + "\""
                + " ORDER BY " + ArticleDao.Properties.TimestampUsec.columnName + " DESC";
        KLog.e("测getArtsStaredNoTag2：" + queryString);
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
        String queryString = "SELECT " + ArticleDao.TABLENAME + ".* FROM "
                + ArticleDao.TABLENAME + " LEFT JOIN " + FeedDao.TABLENAME + " ON " + ArticleDao.TABLENAME + "." + ArticleDao.Properties.OriginStreamId.columnName + " = " + FeedDao.TABLENAME + "." + FeedDao.Properties.Id.columnName
                + " WHERE "
                + ArticleDao.TABLENAME + "." + ArticleDao.Properties.StarState.columnName + " = \"" + Api.ART_STARED + "\" AND "
                + "(" + FeedDao.TABLENAME + "." + FeedDao.Properties.Id.columnName + " IS NULL OR " + FeedDao.Properties.Categoryid.columnName + " IS \"user/" + WithSet.i().getUseId() + Api.U_NO_LABEL + "\" )"
                + "ORDER BY " + ArticleDao.Properties.TimestampUsec.columnName + " DESC";
        KLog.e("测getArtsStaredNoTag2：" + queryString);
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
        String queryString = "SELECT " + ArticleDao.TABLENAME + ".* FROM "
                + ArticleDao.TABLENAME + " LEFT JOIN " + FeedDao.TABLENAME + " ON " + ArticleDao.TABLENAME + "." + ArticleDao.Properties.OriginStreamId.columnName + " = " + FeedDao.TABLENAME + "." + FeedDao.Properties.Id.columnName
                + " WHERE "
                + FeedDao.TABLENAME + "." + FeedDao.Properties.Id.columnName + " IS NULL OR " + FeedDao.TABLENAME + "." + FeedDao.Properties.Categoryid.columnName + " IS \"user/" + WithSet.i().getUseId() + Api.U_NO_LABEL + "\""
                + " ORDER BY " + ArticleDao.Properties.TimestampUsec.columnName + " DESC";
        KLog.e("测getArtsAllNoTag2：" + queryString);
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
        article.setAuthor(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.Author.columnName)));
        article.setReadState(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.ReadState.columnName)));
        article.setStarState(cursor.getString(cursor.getColumnIndex(ArticleDao.Properties.StarState.columnName)));
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
        requestLogDao.deleteAll();
        imgDao.deleteAll();
    }



//    public List<Article> getArtsByCategoriesOrderCrawlMsec(String... categories) {
//        QueryBuilder<Article> artQuery = articleDao.queryBuilder();
//        for (String category : categories) {
//            artQuery.where(ArticleDao.Properties.Categories.like(category));
//        }
//        artQuery.orderDesc(ArticleDao.Properties.CrawlTimeMsec);
//        return artQuery.listLazy();
//    }
//    public List<Article> getArtsByCategoriesOrderStaredMsec(String... categories) {
//        QueryBuilder<Article> artQuery = articleDao.queryBuilder();
//        for (String category : categories) {
//            artQuery.where(ArticleDao.Properties.Categories.like(category));
//        }
//        artQuery.orderDesc(ArticleDao.Properties.Starred);
//        return artQuery.listLazy();
//    }
//
//    public List<Article> loadStarAndNoTag() {
//        QueryBuilder<Article> artQuery = articleDao.queryBuilder();
//        artQuery.where(ArticleDao.Properties.Categories.like("%[user/" + App.UserID + "/state/com.google/starred]%"));
//        artQuery.where(ArticleDao.Properties.Categories.like("%[^user/" + App.UserID + "/label/]%"));
//        artQuery.orderDesc(ArticleDao.Properties.Starred);
//        return artQuery.listLazy();
//    }
//
//    public List<Article> loadUnreadAndNoTag() {
//        QueryBuilder<Article> artQuery = articleDao.queryBuilder();
//        artQuery.where(ArticleDao.Properties.Categories.like("%[^user/" + App.UserID + "/state/com.google/read]%"));
//        artQuery.where(ArticleDao.Properties.Categories.like("%[^user/" + App.UserID + "/label/]%"));
//        artQuery.orderDesc(ArticleDao.Properties.CrawlTimeMsec);
//        return artQuery.listLazy();
//    }

//    public List<Article> loadAllNoTag() {
//        QueryBuilder<Article> artQuery = articleDao.queryBuilder();
//        artQuery.where(ArticleDao.Properties.Categories.like("%[^user/" + App.UserID + "/label/]%"));
//        artQuery.orderDesc(ArticleDao.Properties.CrawlTimeMsec);
//        return artQuery.listLazy();
//    }
//
//    public List<Article> getArtsStarByStreamIdOrderStaredMsec(String streamId) {
//        QueryBuilder<Article> artQuery = articleDao.queryBuilder();
//        artQuery.where(ArticleDao.Properties.Categories.like("%[user/" + App.UserID + "/state/com.google/starred]%"));
//        artQuery.where(ArticleDao.Properties.OriginStreamId.eq(streamId));
//        artQuery.orderDesc(ArticleDao.Properties.Starred);
//        return artQuery.listLazy();
//    }
//
//    public List<Article> getArtsReadByStreamIdOrderCrawlMsec(String readState, String streamId) {
//        QueryBuilder<Article> q = articleDao.queryBuilder()
//                .where(ArticleDao.Properties.ReadState.like(readState + "%"), ArticleDao.Properties.OriginStreamId.eq(streamId)) /** Creates an "equal ('=')" condition  for this property. */
//                .orderDesc(ArticleDao.Properties.TimestampUsec);
//        return q.listLazy();
//    }


//
//    public List<Article> getArtsStaredNoTag() {
//        String queryString = "SELECT * FROM "
//                + ArticleDao.TABLENAME + " "
//                + "WHERE "
//                + ArticleDao.Properties.StarState.columnName + " = \"" + Api.ART_STARED + "\" AND "
//                + ArticleDao.Properties.Categories.columnName + " NOT LIKE \"" + "%" + Api.getLabelStreamFlag() + "%\" "
//                + "ORDER BY " + ArticleDao.Properties.TimestampUsec.columnName + " DESC";
//        Cursor cursor = articleDao.getDatabase().rawQuery(queryString, new String[]{});
//        if (cursor == null) {
//            return new ArrayList<>();
//        }
//
//        List<Article> articles = new ArrayList<>(cursor.getCount());
//        while (cursor.moveToNext()) {
//            articles.add(genArticle(new Article(), cursor));
//        }
//        cursor.close();
//        return articles;
//    }
//
//    public List<Article> getArtsUnreadNoTag() {
//        String queryString = "SELECT * FROM "
//                + ArticleDao.TABLENAME + " WHERE "
//                + ArticleDao.Properties.ReadState.columnName + " LIKE \"" + Api.ART_UNREAD + "%\" AND "
//                + ArticleDao.Properties.Categories.columnName + " NOT LIKE \"" + "%" + Api.getLabelStreamFlag() + "%\" "
//                + "ORDER BY " + ArticleDao.Properties.TimestampUsec.columnName + " DESC";
//        Cursor cursor = articleDao.getDatabase().rawQuery(queryString, new String[]{});
//        if (cursor == null) {
//            return new ArrayList<>();
//        }
//        List<Article> articles = new ArrayList<>(cursor.getCount());
//        while (cursor.moveToNext()) {
//            articles.add(genArticle(new Article(), cursor));
//        }
//        cursor.close();
//        return articles;
//    }
//    public List<Article> getArtsAllNoTag() {
//        String queryString = "SELECT * FROM "
//                + ArticleDao.TABLENAME + " WHERE "
//                + ArticleDao.Properties.Categories.columnName + " NOT LIKE \"" + "%" + Api.getLabelStreamFlag() + "%\""
//                + "ORDER BY " + ArticleDao.Properties.TimestampUsec.columnName + " DESC";
//        Cursor cursor = articleDao.getDatabase().rawQuery(queryString, new String[]{});
//        if (cursor == null) {
//            return new ArrayList<>();
//        }
//
//        List<Article> articles = new ArrayList<>(cursor.getCount());
//        while (cursor.moveToNext()) {
//            articles.add(genArticle(new Article(), cursor));
//        }
//        cursor.close();
//        return articles;
//    }




}
