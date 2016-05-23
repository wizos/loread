package me.wizos.loread.dao;

import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.Query;
import me.wizos.loread.App;
import me.wizos.loread.bean.Article;
import me.wizos.loread.bean.Feed;
import me.wizos.loread.bean.RequestLog;
import me.wizos.loread.bean.Tag;
import me.wizos.loread.net.API;

/**
 * Created by Wizos on 2016/3/12.
 */
public class WithDB {
    private static WithDB withDB;
    private TagDao tagDao;
    private FeedDao feedDao;
    private ArticleDao articleDao;
    private RequestLogDao requestLogDao;

    private WithDB() {}

    public static WithDB getInstance() {
        if (withDB == null) { // 双重锁定，只有在 withDB 还没被初始化的时候才会进入到下一行，然后加上同步锁
            synchronized (WithDB.class) { // 同步锁，避免多线程时可能 new 出两个实例的情况
                if (withDB == null) {
                    // All init here
                    withDB = new WithDB();
                    withDB.tagDao = App.getDaoSession().getTagDao();
                    withDB.feedDao = App.getDaoSession().getFeedDao();
                    withDB.articleDao = App.getDaoSession().getArticleDao();
                    withDB.requestLogDao = App.getDaoSession().getRequestLogDao();
                }
            }
        }
        return withDB;
    }

    // 自己写的
    public void saveTag(Tag tag) {
        if (tag.getId() != null) {return;}
        if (tagDao.queryBuilder().where(TagDao.Properties.Id.eq(tag.getId())).list().size() == 0) {
            tagDao.insertOrReplace(tag);
        }
        tagDao.insertOrReplace(tag);
    }
    // 自己写的
    public void saveTagList(ArrayList<Tag> tags) {
        tagDao.deleteAll();
        tagDao.insertOrReplaceInTx(tags);
    }
    public List<Tag> loadTags(){
        List<Tag> tagList = tagDao.loadAll();
        return tagList;
    }

    public void saveFeed(Feed feed) {
        if (feed.getId() == null) { // new fetch
            if (feedDao.queryBuilder().where(FeedDao.Properties.Title.eq(feed.getTitle())).list().size() == 0) {
                feedDao.insertOrReplace(feed);
            } else { // Has same

            }
        } else { // already exist
            feedDao.update(feed);
        }
    }
    public void saveArticle(Article article) {
        if (article.getId() != null) {
            articleDao.insertOrReplace(article);
        }
    }
    public void saveArticleList(ArrayList<Article> articleList) {
        if (articleList.size() != 0) { // new fetch
            articleDao.insertOrReplaceInTx(articleList);
        }
    }

    public void saveRequestLogList(ArrayList<RequestLog> requestLogList) {
        if (requestLogList.size() != 0) { // new fetch
            requestLogDao.insertOrReplaceInTx(requestLogList);
        }
    }


    public Article getArticle(String articleId) {
//        if (articleID == null) {return null;}
        List<Article> articles = articleDao.queryBuilder().where(ArticleDao.Properties.Id.eq(articleId)).list();
        if ( articles.size() != 0) {
            return articles.get(0);
        }else {
            return null;
        }
    }
    public boolean hasTag(String id) {
        return tagDao.queryBuilder().where(TagDao.Properties.Id.eq(id)).list().size() > 0;
    }
    public boolean hasFeed(String url) {
        return feedDao.queryBuilder()
                .where(FeedDao.Properties.Url.eq(url)).list().size() > 0;
    }






    public List<RequestLog> loadRequestListAll(){
        return  requestLogDao.loadAll();
    }
    public void delRequestListAll(){
        requestLogDao.deleteAll();
    }




   /**
    * 升序
    * Collections.sort(list,Collator.getInstance(java.util.Locale.CHINA));//注意：是根据的汉字的拼音的字母排序的，而不是根据汉字一般的排序方法
    *
    * 降序
    * Collections.reverse(list);//不指定排序规则时，也是按照字母的来排序的
    **/
   public void delArtList(ArrayList<String> arrayList){
       for (String item:arrayList){
           articleDao.deleteByKey(item);
       }
   }
    public void delArtAll(List<Article> articles){
        if (articles.size() != 0) { // new fetch
            articleDao.deleteInTx( articles );
        }
    }

   public List<Article> loadArtsBeforeTime(long time){
       Query query = articleDao.queryBuilder()
               .where(ArticleDao.Properties.ReadState.eq(API.ART_READ),ArticleDao.Properties.StarState.eq(API.ART_UNSTAR),ArticleDao.Properties.CrawlTimeMsec.lt(time)) /** Creates an "less than ('<')" condition  for this property. */
               .build();
       List<Article> articleList = query.list();
       return articleList;
   }

    public List<Article> loadReadList(String readState, String listTag){
        long xx = System.currentTimeMillis();
        Query query = articleDao.queryBuilder()
                .where(ArticleDao.Properties.ReadState.like(readState + "%"), ArticleDao.Properties.Categories.like("%" + listTag + "%")) /** Creates an "equal ('=')" condition  for this property. */
                .orderDesc(ArticleDao.Properties.TimestampUsec)
                .build();
        List<Article> articlelist = query.list();
        KLog.d("【loadReadList】用时"+ (System.currentTimeMillis() - xx) + "--" + articlelist.size()  + readState + listTag  );
        // 590-55 , 590-73,635-79, 34--612 , 82--612,83--612
        return articlelist;
    }

    public List<Article> loadReadAll(String readState){
        long xx = System.currentTimeMillis();
        Query query = articleDao.queryBuilder()
                .where(ArticleDao.Properties.ReadState.like(readState)) /** Creates an "equal ('=')" condition  for this property. */
                .orderDesc(ArticleDao.Properties.TimestampUsec)
                .build();
        List<Article> articleList = query.list();
        KLog.d("【loadReadListAll】用时"+ (System.currentTimeMillis() - xx) + "--" + articleList.size()  + readState );
        // 1473-25
        return articleList;
    }
    public List<Article> loadArtAll(){ // 速度比要排序的全文更快
        long xx = System.currentTimeMillis();
        List<Article> articleList = articleDao.loadAll();
        KLog.d("【加载所有文章无排序】用时" + (System.currentTimeMillis() - xx) +"--" +articleList.size());
        // 1473-45,1473-44,1523-67, 1527-155, 1527-49, 29--1527 , 59--1527,57--1527,65--1527,79--1527
        return articleList;
    }

    public List<Article> loadArtAllOrder(){
        long xx = System.currentTimeMillis();
        Query query = articleDao.queryBuilder()
                .where(ArticleDao.Properties.ReadState.like("%")) /** Creates an "equal ('=')" condition  for this property. */
                .orderDesc(ArticleDao.Properties.TimestampUsec)
                .build();
        List<Article> articleList = query.list();
        System.out.println("【加载所有文章】" + (System.currentTimeMillis() - xx) +"--" +articleList.size());
        return articleList;
    }

    public List<Article> loadStarList(String listTag){
        Query query = articleDao.queryBuilder()
                .where(ArticleDao.Properties.StarState.eq(API.LIST_STAR),ArticleDao.Properties.Categories.like("%"+ listTag + "%")) /**  Creates an "equal ('=')" condition  for this property. */
                .orderDesc(ArticleDao.Properties.TimestampUsec)
                .build();
        return query.list();
    }
    public List<Article> loadStarAllOrder(){
        Query query = articleDao.queryBuilder()
                .where(ArticleDao.Properties.StarState.eq(API.LIST_STAR)) /**  Creates an "equal ('=')" condition  for this property. */
                .orderDesc(ArticleDao.Properties.TimestampUsec)
                .build();
        KLog.d("【loadStarAll】" + query.list().size() +"--" );
        return query.list();
    }

    public List<Article> loadStarAll(){
        Query query = articleDao.queryBuilder()
                .where(ArticleDao.Properties.StarState.eq(API.LIST_STAR)) /**  Creates an "equal ('=')" condition  for this property. */
                .build();
        KLog.d("【loadStarAllNoOrder】" + query.list().size() +"--" );
        return query.list();
    }


    public List<Article> loadStarListHasLabel(long userId){
        Query query = articleDao.queryBuilder()
                .where(ArticleDao.Properties.StarState.eq(API.LIST_STAR),ArticleDao.Properties.Categories.like("%" + "user/"+ userId+ "/label/" + "%")) /**  Creates an "equal ('=')" condition  for this property. */
                .build();
        KLog.d("Star有标签：" + query.list().size() +" " + userId );
        return query.list();
    }
    public List<Article> loadReadListHasLabel(String readState,long userId){
        Query query = articleDao.queryBuilder()
                .where(ArticleDao.Properties.ReadState.like( readState ),ArticleDao.Properties.Categories.like("%" + "user/"+ userId+ "/label/" + "%")) /**  Creates an "equal ('=')" condition  for this property. */
                .build();
        KLog.d("Read有标签：" + query.list().size());
        return query.list();
    }

    public List<Article> loadReadNoLabel(){
        Query query = articleDao.queryBuilder()
                .where(ArticleDao.Properties.ReadState.eq( API.ART_UNREAD ),ArticleDao.Properties.Categories.like( "%" + API.U_NO_LABEL + "%" )) /**  Creates an "equal ('=')" condition  for this property. */
                .build();
        KLog.d("Read无标签：" + query.list().size());
        return query.list();
    }

    public List<Article> loadStarNoLabel(){
        Query query = articleDao.queryBuilder()
                .where(ArticleDao.Properties.StarState.eq(API.LIST_STAR),ArticleDao.Properties.Categories.like( "%" + API.U_NO_LABEL + "%" )) /**  Creates an "equal ('=')" condition  for this property. */
                .build();
        KLog.d("Star无标签：" + query.list().size());
        return query.list();
    }
}
