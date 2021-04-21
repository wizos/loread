package me.wizos.loread.activity.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.sqlite.db.SimpleSQLiteQuery;

import me.wizos.loread.App;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.utils.StringUtils;

// LiveData通常结合ViewModel一起使用。我们知道ViewModel是用来存放数据的，因此我们可以将数据库放在ViewModel中进行实例化。
// 但数据库在实例化的时候需要Context，而ViewModel不能传入任何带有Context引用的对象，所以应该用它的子类AndroidViewModel，它可以接受Application作为参数，用于数据库的实例化。
public class FeedListViewModel extends AndroidViewModel {
    public LiveData<PagedList<Feed>> feedsLiveData;

    public FeedListViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadFeeds(String uid, String categoryId, String keyword, String orderField, boolean defaultDirection, @NonNull LifecycleOwner owner, @NonNull Observer<PagedList<Feed>> articlesObserver){
        if(feedsLiveData != null && feedsLiveData.hasObservers()){
            feedsLiveData.removeObservers(owner);
            feedsLiveData = null;
        }
        String categorySQL = "";
        if(categoryId.contains(App.CATEGORY_ALL)){
            categorySQL = "SELECT feed.* FROM feed WHERE feed.uid = '" + uid + "' ";
        }else if(categoryId.contains(App.CATEGORY_UNCATEGORIZED)){
            categorySQL = "SELECT feed.* FROM feed LEFT JOIN feedCategory ON (feed.uid = FeedCategory.uid AND feed.id = FeedCategory.feedId) WHERE feed.uid = '" + uid + "' AND feedCategory.categoryId IS NULL ";
        }else {
            categorySQL = "SELECT feed.* FROM feed LEFT JOIN feedCategory ON (feed.uid = FeedCategory.uid AND feed.id = FeedCategory.feedId) WHERE feed.uid = '" + uid + "' AND feedCategory.categoryId ='" + categoryId + "' ";
        }

        String filterCondition = "";
        if(!StringUtils.isEmpty(keyword)){
            filterCondition = " AND (feed.title LIKE '%" + keyword + "%' OR feed.feedUrl  LIKE '%" + keyword + "%') ";
        }

        String orderSQL = "";
        if(orderField.equalsIgnoreCase("title")){
            orderSQL = " ORDER BY feed.title " + (defaultDirection?"ASC":"DESC");
        }else if(orderField.equalsIgnoreCase("all count")){
            orderSQL = " ORDER BY feed.allCount " + (defaultDirection?"DESC":"ASC") + ", feed.title";
        }else if(orderField.equalsIgnoreCase("unread count")){
            orderSQL = " ORDER BY feed.unreadCount " + (defaultDirection?"DESC":"ASC") + ", feed.title";
        }else if(orderField.equalsIgnoreCase("star count")){
            orderSQL = " ORDER BY feed.starCount " + (defaultDirection?"DESC":"ASC") + ", feed.title";
        }else if(orderField.equalsIgnoreCase("health state")){
            if(defaultDirection){
                orderSQL = " ORDER BY CASE WHEN feed.lastErrorCount > 0 THEN 1 ELSE 2 END, feed.title";
            }else {
                orderSQL = " ORDER BY CASE WHEN feed.lastErrorCount > 0 THEN 1 ELSE 0 END, feed.title";
            }
        }else if(orderField.equalsIgnoreCase("last publish")){
            orderSQL = " ORDER BY feed.lastPubDate " + (defaultDirection?"DESC":"ASC") + ", feed.title";
        }

        DataSource.Factory<Integer, Feed> factory = CoreDB.i().feedDao().get(new SimpleSQLiteQuery(categorySQL + filterCondition + orderSQL));
        // XLog.i("生成 getArticles ：" + streamId + "   " + timeMillis);

        feedsLiveData = new LivePagedListBuilder<>(factory, new PagedList.Config.Builder()
                .setInitialLoadSizeHint(20) // 第一次加载多少数据，必须是PageSize的倍数，默认为PageSize*3
                .setPageSize(20) // 每页加载多少数据，必须大于0，这里默认20
                .setPrefetchDistance(20) // 距底部还有几条数据时，加载下一页数据，默认为PageSize
                .setMaxSize(60) // 必须是 2*PrefetchDistance + PageSize
                .build()
        ).build();

        feedsLiveData.observe(owner, articlesObserver);
    }
}
