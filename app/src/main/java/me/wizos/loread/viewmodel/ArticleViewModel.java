package me.wizos.loread.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.socks.library.KLog;

import me.wizos.loread.App;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.ArticleDao;
import me.wizos.loread.db.CoreDB;

//LiveData通常结合ViewModel一起使用。我们知道ViewModel是用来存放数据的，因此我们可以将数据库放在ViewModel中进行实例化。
// 但数据库在实例化的时候需要Context，而ViewModel不能传入任何带有Context引用的对象，所以应该用它的子类AndroidViewModel，它可以接受Application作为参数，用于数据库的实例化。
public class ArticleViewModel extends ViewModel {
    public LiveData<PagedList<Article>> articles;
    public LiveData<PagedList<Article>> getArticles(String uid, String streamId, int streamType, int streamStatus){
        //KLog.i("生成 getArticles ：" + streamId );
        ArticleDao articleDao = CoreDB.i().articleDao();
        long timeMillis = System.currentTimeMillis();
        DataSource.Factory<Integer, Article> articleFactory = null;

        if (streamType == App.TYPE_GROUP ) {
            if (streamId.contains(App.CATEGORY_ALL)) {
                if (streamStatus == App.STATUS_STARED) {
                    articleFactory = articleDao.getStared(uid, timeMillis);
                } else if (streamStatus == App.STATUS_UNREAD) {
                    articleFactory = articleDao.getUnread(uid, timeMillis);
                } else {
                    articleFactory = articleDao.getAll(uid, timeMillis);
                }
            } else if (streamId.contains(App.CATEGORY_UNCATEGORIZED)) {
                if (streamStatus == App.STATUS_STARED) {
                    articleFactory = articleDao.getStaredByUncategory(uid, timeMillis);
                } else if (streamStatus == App.STATUS_UNREAD) {
                    articleFactory = articleDao.getUnreadByUncategory(uid, timeMillis);
                } else {
                    articleFactory = articleDao.getAllByUncategory(uid, timeMillis);
                }
            } else {
                KLog.e("获取到的分类：" + streamId );
                if (streamStatus == App.STATUS_STARED) {
                    articleFactory = articleDao.getStaredByCategoryId(uid, streamId, timeMillis);
                } else if (streamStatus == App.STATUS_UNREAD) {
                    articleFactory = articleDao.getUnreadByCategoryId(uid, streamId, timeMillis);
                } else {
                    articleFactory = articleDao.getAllByCategoryId(uid, streamId, timeMillis);
                }
            }
        } else if (streamType == App.TYPE_FEED ) {
            if (streamStatus == App.STATUS_STARED) {
                articleFactory = articleDao.getStaredByFeedId(uid, streamId, timeMillis);
            } else if (streamStatus == App.STATUS_UNREAD) {
                articleFactory = articleDao.getUnreadByFeedId(uid, streamId, timeMillis);
            } else {
                articleFactory = articleDao.getAllByFeedId(uid, streamId, timeMillis);
            }
        }
        // setPageSize 指定每次分页加载的条目数量
        assert articleFactory != null;
        articles = new LivePagedListBuilder<>(articleFactory, new PagedList.Config.Builder()
                .setInitialLoadSizeHint(30)
                .setPageSize(30)
                .setPrefetchDistance(15)
                .build()
        ).build();
        return articles;
    }


//    public LiveData<PagedList<Article>> getArticles2(String uid, String streamId, int streamType, int streamStatus){
//        //KLog.i("生成 getArticles ：" + streamId );
//        ArticleDao articleDao = CoreDB.i().articleDao();
//        long timeMillis = System.currentTimeMillis();
//        DataSource.Factory<Integer, Article> articleFactory = null;
//        if(streamStatus == App.STATUS_STARED){
//            if(streamType == App.TYPE_GROUP){
//                if (streamId.contains(App.CATEGORY_ALL)) {
//                    articleFactory = articleDao.getStared(uid, timeMillis);
//                }else if (streamId.contains(App.CATEGORY_UNCATEGORIZED)) {
//                    articleFactory = articleDao.getStaredByUnTag(uid, timeMillis);
//                }else {
//                    KLog.e("加载列表：" + streamId + " , " + timeMillis);
//                    articleFactory = articleDao.getStaredByTagId(uid, streamId, timeMillis);
//                }
//            }else {
//                articleFactory = articleDao.getStaredByFeedId(uid, streamId, timeMillis);
//            }
//        }else if (streamStatus == App.STATUS_UNREAD) {
//            if(streamType == App.TYPE_GROUP){
//                if (streamId.contains(App.CATEGORY_ALL)) {
//                    articleFactory = articleDao.getUnread(uid, timeMillis);
//                }else if (streamId.contains(App.CATEGORY_UNCATEGORIZED)) {
//                    articleFactory = articleDao.getUnreadByUncategory(uid, timeMillis);
//                }else {
//                    articleFactory = articleDao.getUnreadByCategoryId(uid, streamId, timeMillis);
//                }
//            }else {
//                articleFactory = articleDao.getUnreadByFeedId(uid, streamId, timeMillis);
//            }
//        }else {
//            if(streamType == App.TYPE_GROUP){
//                if (streamId.contains(App.CATEGORY_ALL)) {
//                    articleFactory = articleDao.getAll(uid, timeMillis);
//                }else if (streamId.contains(App.CATEGORY_UNCATEGORIZED)) {
//                    articleFactory = articleDao.getAllByUncategory(uid, timeMillis);
//                }else {
//                    articleFactory = articleDao.getAllByCategoryId(uid, streamId, timeMillis);
//                }
//            }else {
//                articleFactory = articleDao.getAllByFeedId(uid, streamId, timeMillis);
//            }
//        }
//        // setPageSize 指定每次分页加载的条目数量
//        assert articleFactory != null;
//        articles = new LivePagedListBuilder<>(articleFactory, new PagedList.Config.Builder()
//                .setInitialLoadSizeHint(30)
//                .setPageSize(30)
//                .setPrefetchDistance(15)
//                .build()
//        ).build();
//
//        KLog.e("加载列表B：" + articles.getValue());
//        if( articles.getValue() != null){
//            KLog.e("加载列表C：" + articles.getValue().size());
//        }
//        return articles;
//    }

    public LiveData<PagedList<Article>> getAllByKeyword(String uid, String keyword){
        // setPageSize 指定每次分页加载的条目数量
        articles = new LivePagedListBuilder<>(CoreDB.i().articleDao().getAllByKeyword(uid,"%" + keyword + "%"), new PagedList.Config.Builder().setPageSize(30).setInitialLoadSizeHint(30).setPrefetchDistance(15).build()).build();
        return articles;
    }

//    public Article getItem(int position){
//        return articles.getValue().get(position);
////        return listData.get(position);
//    }
//    public int getItemCount(){
//        return articles.getValue().size();
////        return listData.size();
//    }

//    public void updateItem(Article article){
//        listData.update(article);
//    }

//    private ArticleLazyList listData;
//    public List<Article> getListData(String uid, String streamId, int streamType, int streamStatus){
//        KLog.e("生成ArticleViewModel 2  ：" + streamId );
//        ArticleDao articleDao = CoreDB.i().articleDao();
//        if (streamId.startsWith("user/") || streamType == App.TYPE_GROUP ) {
//            if (streamId.contains(App.CATEGORY_ALL)) {
//                if (streamStatus == App.STATUS_STARED) {
//                    listData = articleDao.getStared(uid);
//                } else if (streamStatus == App.STATUS_UNREAD) {
//                    listData = articleDao.getUnread(uid);
//                } else {
//                    listData = articleDao.getAll(uid);
//                }
//            } else if (streamId.contains(App.CATEGORY_UNCATEGORIZED)) {
//                if (streamStatus == App.STATUS_STARED) {
//                    listData = articleDao.getStaredByUncategory(uid);
//                } else if (streamStatus == App.STATUS_UNREAD) {
//                    listData = articleDao.getUnreadByUncategory(uid);
//                } else {
//                    listData = articleDao.getAllByUncategory(uid);
//                }
//            } else {
//                // TEST:  测试
//                //Category theCategory = WithDB.i().getCategoryById(streamId);
//                KLog.e("获取到的分类：" + streamId );
//                if (streamStatus == App.STATUS_STARED) {
//                    listData = articleDao.getStaredByCategoryId(uid, streamId);
//                } else if (streamStatus == App.STATUS_UNREAD) {
//                    listData = articleDao.getUnreadByCategoryId(uid, streamId);
//                } else {
//                    listData = articleDao.getAllByCategoryId(uid, streamId);
//                }
//            }
//        } else if (streamId.startsWith("feed/") || streamType == App.TYPE_FEED ) {
//            if (streamStatus == App.STATUS_STARED) {
//                listData = articleDao.getStaredByFeedId(uid, streamId);
//            } else if (streamStatus == App.STATUS_UNREAD) {
//                listData = articleDao.getUnreadByFeedId(uid, streamId);
//            } else {
//                listData = articleDao.getAllByFeedId(uid, streamId);
//            }
//        }
//        // setPageSize 指定每次分页加载的条目数量
//        return listData;
//    }


//    public List<Article> getListData2(String uid, String streamId, int streamType, int streamStatus){
//        ArticleDao articleDao = CoreDB.i().articleDao();
//        boolean includeValueless = App.i().getGlobalKV().getBoolean("including_valueless",false);
//        KLog.e("获取到的分类：" + streamId );
//        if (streamId.startsWith("user/") || streamType == App.TYPE_GROUP ) {
//            if (streamId.contains(App.CATEGORY_ALL)) {
//                if (includeValueless) {
//                    listData = articleDao.getAll(uid);
//                } else {
//                    listData = articleDao.getValuable(uid);
//                }
//            } else if (streamId.contains(App.CATEGORY_UNCATEGORIZED)) {
//                if (includeValueless) {
//                    listData = articleDao.getAllByUncategory(uid);
//                } else {
//                    listData = articleDao.getValuableByUnCategory(uid);
//                }
//            } else {
//                if (includeValueless) {
//                    listData = articleDao.getAllByCategoryId(uid, streamId);
//                } else {
//                    listData = articleDao.getValuableByCategoryId(uid, streamId);
//                }
//            }
//        } else if (streamId.startsWith("feed/") || streamType == App.TYPE_FEED ) {
//            if (includeValueless) {
//                listData = articleDao.getAllByFeedId(uid, streamId);
//            } else {
//                listData = articleDao.getValuableByFeedId(uid, streamId);
//            }
//        }
//        // setPageSize 指定每次分页加载的条目数量
//        return listData;
//    }

//    public List<Article> getListData3(String uid, String streamId, int streamType, int streamStatus){
//        ArticleDao articleDao = CoreDB.i().articleDao();
//        Cursor cursor = null;
//        if (streamId.startsWith("user/") || streamType == App.TYPE_GROUP ) {
//            if (streamId.contains(App.CATEGORY_ALL)) {
//                if (streamStatus == App.STATUS_STARED) {
//                    cursor = articleDao.getStared(uid);
//                } else if (streamStatus == App.STATUS_UNREAD) {
//                    cursor = articleDao.getUnread(uid);
//                } else {
//                    cursor = articleDao.getAll(uid);
//                }
//            } else if (streamId.contains(App.CATEGORY_UNCATEGORIZED)) {
//                if (streamStatus == App.STATUS_STARED) {
//                    cursor = articleDao.getStaredByUncategory(uid);
//                } else if (streamStatus == App.STATUS_UNREAD) {
//                    cursor = articleDao.getUnreadByUncategory(uid);
//                } else {
//                    cursor = articleDao.getAllByUncategory(uid);
//                }
//            } else {
//                // TEST:  测试
//                //Category theCategory = WithDB.i().getCategoryById(streamId);
//                KLog.e("获取到的分类：" + streamId );
//                if (streamStatus == App.STATUS_STARED) {
//                    cursor = articleDao.getStaredByCategoryId(uid, streamId);
//                } else if (streamStatus == App.STATUS_UNREAD) {
//                    cursor = articleDao.getUnreadByCategoryId(uid, streamId);
//                } else {
//                    cursor = articleDao.getAllByCategoryId(uid, streamId);
//                }
//            }
//        } else if (streamId.startsWith("feed/") || streamType == App.TYPE_FEED ) {
//            if (streamStatus == App.STATUS_STARED) {
//                cursor = articleDao.getStaredByFeedId(uid, streamId);
//            } else if (streamStatus == App.STATUS_UNREAD) {
//                cursor = articleDao.getUnreadByFeedId(uid, streamId);
//            } else {
//                cursor = articleDao.getAllByFeedId(uid, streamId);
//            }
//        }
//        if(listData != null){
//            ((ArticleLazyList)listData).close();
//        }
//        listData = new ArticleLazyList(cursor,true);
//        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
//            @Override
//            public void run() {
//                ((ArticleLazyList)listData).loadRemaining();
//            }
//        });
//        return listData;
//    }
//
//    public List<Article> getAllByKeyword(String uid, String keyword){
//        if(listData != null){
//            ((ArticleLazyList)listData).close();
//        }
//        listData = new ArticleLazyList(CoreDB.i().articleDao().getAllByKeyword(uid,"%" + keyword + "%"),true);
//        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
//            @Override
//            public void run() {
//                ((ArticleLazyList)listData).loadRemaining();
//            }
//        });
//        return listData;
//    }


//    public LiveData<PagedList<Article>> getArticles2(String uid, String streamId, int streamType, int streamStatus){
//        final long timeMillis = System.currentTimeMillis();
//        KLog.e("获取文章时间为：" + timeMillis );
//        articles = new LivePagedListBuilder<>(CoreDB.i().articleDao().getUnread(uid,timeMillis), new PagedList.Config.Builder().setPageSize(10).setInitialLoadSizeHint(10).setPrefetchDistance(5).build()).build();
//        return articles;
//    }

//    public LiveData<PagedList<Article>> getArticles(){
//        return articles;
//    }
//    public void queryArticles(String uid, String streamId, int streamType, int streamStatus){
//        ArticleDao articleDao = CoreDB.i().articleDao();
//        final long timeMillis = System.currentTimeMillis();
//        DataSource.Factory<Integer, Article> articleFactory = null;
//
//        if (streamId.startsWith("user/") || streamType == App.TYPE_GROUP ) {
//            if (streamId.contains(App.CATEGORY_ALL)) {
//                if (streamStatus == App.STATUS_STARED) {
//                    articleFactory = articleDao.getStared(uid, timeMillis);
//                } else if (streamStatus == App.STATUS_UNREAD) {
//                    articleFactory = articleDao.getUnread(uid, timeMillis);
//                } else {
//                    articleFactory = articleDao.getAll(uid);
//                }
//            } else if (streamId.contains(App.CATEGORY_UNCATEGORIZED)) {
//                if (streamStatus == App.STATUS_STARED) {
//                    articleFactory = articleDao.getStaredByUncategory(uid, timeMillis);
//                } else if (streamStatus == App.STATUS_UNREAD) {
//                    articleFactory = articleDao.getUnreadByUncategory(uid, timeMillis);
//                } else {
//                    articleFactory = articleDao.getAllByUncategory(uid);
//                }
//            } else {
//                // TEST:  测试
//                //Category theCategory = WithDB.i().getCategoryById(streamId);
//                KLog.e("获取到的分类：" + streamId );
//                if (streamStatus == App.STATUS_STARED) {
//                    articleFactory = articleDao.getStaredByCategoryId(uid, streamId, timeMillis);
//                } else if (streamStatus == App.STATUS_UNREAD) {
//                    articleFactory = articleDao.getUnreadByCategoryId(uid, streamId, timeMillis);
//                } else {
//                    articleFactory = articleDao.getAllByCategoryId(uid, streamId);
//                }
//            }
//        } else if (streamId.startsWith("feed/") || streamType == App.TYPE_FEED ) {
//            if (streamStatus == App.STATUS_STARED) {
//                articleFactory = articleDao.getStaredByFeedId(uid, streamId, timeMillis);
//            } else if (streamStatus == App.STATUS_UNREAD) {
//                articleFactory = articleDao.getUnreadByFeedId(uid, streamId, timeMillis);
//            } else {
//                articleFactory = articleDao.getAllByFeedId(uid, streamId);
//            }
//        }
//        // setPageSize 指定每次分页加载的条目数量
//        articles = new LivePagedListBuilder<>(articleFactory, new PagedList.Config.Builder().setPageSize(30).setInitialLoadSizeHint(10).setPrefetchDistance(5).build()).build();
//    }

}
