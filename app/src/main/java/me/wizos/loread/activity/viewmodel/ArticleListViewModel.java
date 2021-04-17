package me.wizos.loread.activity.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.bean.collectiontree.CollectionTree;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.ArticleDao;
import me.wizos.loread.db.CoreDB;

// LiveData通常结合ViewModel一起使用。我们知道ViewModel是用来存放数据的，因此我们可以将数据库放在ViewModel中进行实例化。
// 但数据库在实例化的时候需要Context，而ViewModel不能传入任何带有Context引用的对象，所以应该用它的子类AndroidViewModel，它可以接受Application作为参数，用于数据库的实例化。
public class ArticleListViewModel extends ViewModel {
    public LiveData<PagedList<Article>> articlesLiveData;
    public LiveData<List<String>> articleIdsLiveData;

    public void loadArticles(String uid, String streamId, int streamType, int streamStatus, @NonNull LifecycleOwner owner, @NonNull Observer<PagedList<Article>> articlesObserver, @NonNull Observer<List<String>> articleIdsObserver){
        if(articlesLiveData != null && articlesLiveData.hasObservers()){
            articlesLiveData.removeObservers(owner);
            articlesLiveData = null;
        }
        if(articleIdsLiveData != null && articleIdsLiveData.hasObservers()){
            articleIdsLiveData.removeObservers(owner);
            articleIdsLiveData = null;
        }

        ArticleDao articleDao = CoreDB.i().articleDao();
        long timeMillis = System.currentTimeMillis();
        App.i().setLastShowTimeMillis(timeMillis);

        DataSource.Factory<Integer, Article> articleFactory;
        // XLog.i("生成 getArticles ：" + streamId + "   " + timeMillis);

        if(streamType == CollectionTree.CATEGORY){
            if (streamStatus == App.STATUS_STARED) {
                articleFactory = articleDao.getStaredByCategoryId(uid, streamId, timeMillis);
                articleIdsLiveData = articleDao.getStaredIdsByCategoryId(uid, streamId, timeMillis);
            } else if (streamStatus == App.STATUS_UNREAD) {
                articleFactory = articleDao.getUnreadByCategoryId(uid, streamId, timeMillis);
                articleIdsLiveData = articleDao.getUnreadIdsByCategoryId(uid, streamId, timeMillis);
            } else {
                articleFactory = articleDao.getAllByCategoryId(uid, streamId, timeMillis);
                articleIdsLiveData = articleDao.getAllIdsByCategoryId(uid, streamId, timeMillis);
            }
        }else if(streamType == CollectionTree.FEED){
            if (streamStatus == App.STATUS_STARED) {
                articleFactory = articleDao.getStaredByFeedId(uid, streamId, timeMillis);
                articleIdsLiveData = articleDao.getStaredIdsByFeedId(uid, streamId, timeMillis);
            } else if (streamStatus == App.STATUS_UNREAD) {
                articleFactory = articleDao.getUnreadByFeedId(uid, streamId, timeMillis);
                articleIdsLiveData = articleDao.getUnreadIdsByFeedId(uid, streamId, timeMillis);
            } else {
                articleFactory = articleDao.getAllByFeedId(uid, streamId, timeMillis);
                articleIdsLiveData = articleDao.getAllIdsByFeedId(uid, streamId, timeMillis);
            }
        }else {
            if (streamStatus == App.STATUS_STARED){
                if (streamId.contains(App.CATEGORY_UNSUBSCRIBED)){
                    articleFactory = articleDao.getStaredByUnsubscribed(uid, timeMillis);
                    articleIdsLiveData = articleDao.getStaredIdsByUnsubscribed(uid,timeMillis);
                }else {
                    articleFactory = articleDao.getStared(uid, timeMillis);
                    articleIdsLiveData = articleDao.getStaredArticleIds(uid,timeMillis);
                }
            } else if (streamStatus == App.STATUS_UNREAD) {
                if (streamId.contains(App.CATEGORY_UNSUBSCRIBED)){
                    articleFactory = articleDao.getUnreadByUnsubscribed(uid, timeMillis);
                    articleIdsLiveData = articleDao.getUnreadIdsByUnsubscribed(uid,timeMillis);
                }else {
                    articleFactory = articleDao.getUnread(uid, timeMillis);
                    articleIdsLiveData = articleDao.getUnreadIds(uid,timeMillis);
                }
            } else {
                if (streamId.contains(App.CATEGORY_UNSUBSCRIBED)){
                    articleFactory = articleDao.getAllByUnsubscribed(uid, timeMillis);
                    articleIdsLiveData = articleDao.getAllIdsByUnsubscribed(uid,timeMillis);
                }else {
                    articleFactory = articleDao.getAll(uid, timeMillis);
                    articleIdsLiveData = articleDao.getAllIds(uid,timeMillis);
                }
            }
        }

        // setPageSize 指定每次分页加载的条目数量
        assert articleFactory != null;
        articlesLiveData = new LivePagedListBuilder<>(articleFactory, new PagedList.Config.Builder()
                .setInitialLoadSizeHint(20) // 第一次加载多少数据，必须是PageSize的倍数，默认为PageSize*3
                .setPageSize(20) // 每页加载多少数据，必须大于0，这里默认20
                .setPrefetchDistance(20) // 距底部还有几条数据时，加载下一页数据，默认为PageSize
                .setMaxSize(60) // 必须是 2*PrefetchDistance + PageSize
                .build()
        ).build();

        articlesLiveData.observe(owner, articlesObserver);
        articleIdsLiveData.observe(owner, articleIdsObserver);
    }

    public void loadArticles(String uid, String keyword, @NonNull LifecycleOwner owner, @NonNull Observer<PagedList<Article>> articlesObserver, @NonNull Observer<List<String>> articleIdsObserver){
        if(articlesLiveData != null && articlesLiveData.hasObservers()){
            articlesLiveData.removeObservers(owner);
            articlesLiveData = null;
        }
        if(articleIdsLiveData != null && articleIdsLiveData.hasObservers()){
            articleIdsLiveData.removeObservers(owner);
            articleIdsLiveData = null;
        }

        long timeMillis = System.currentTimeMillis();
        // setPageSize 指定每次分页加载的条目数量
        articlesLiveData = new LivePagedListBuilder<>(CoreDB.i().articleDao().getAllByKeyword(uid,keyword, timeMillis), new PagedList.Config.Builder()
                .setInitialLoadSizeHint(20)
                .setPageSize(20)
                .setPrefetchDistance(20)
                .setMaxSize(60)
                .build()
        ).build();
        articleIdsLiveData = CoreDB.i().articleDao().getAllIdsByKeyword(uid, keyword, timeMillis);

        articlesLiveData.observe(owner, articlesObserver);
        articleIdsLiveData.observe(owner, articleIdsObserver);
    }

    public void loadArticles(String uid, @NonNull LifecycleOwner owner, @NonNull Observer<PagedList<Article>> articlesObserver, @NonNull Observer<List<String>> articleIdsObserver){
        if(articlesLiveData != null && articlesLiveData.hasObservers()){
            articlesLiveData.removeObservers(owner);
            articlesLiveData = null;
        }
        if(articleIdsLiveData != null && articleIdsLiveData.hasObservers()){
            articleIdsLiveData.removeObservers(owner);
            articleIdsLiveData = null;
        }

        // setPageSize 指定每次分页加载的条目数量
        articlesLiveData = new LivePagedListBuilder<>(CoreDB.i().articleDao().getDuplicateArticles(uid), new PagedList.Config.Builder()
                .setInitialLoadSizeHint(20)
                .setPageSize(20)
                .setPrefetchDistance(20)
                .setMaxSize(60)
                .build()
        ).build();
        articleIdsLiveData = CoreDB.i().articleDao().getDuplicateArticleIds(uid);

        articlesLiveData.observe(owner, articlesObserver);
        articleIdsLiveData.observe(owner, articleIdsObserver);
    }
}
