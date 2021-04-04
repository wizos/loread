package me.wizos.loread.activity.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import com.elvishew.xlog.XLog;

import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.collectiontree.Collection;
import me.wizos.loread.bean.collectiontree.CollectionFeed;
import me.wizos.loread.bean.collectiontree.CollectionTree;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.utils.Classifier;

// LiveData通常结合ViewModel一起使用。我们知道ViewModel是用来存放数据的，因此我们可以将数据库放在ViewModel中进行实例化。
// 但数据库在实例化的时候需要Context，而ViewModel不能传入任何带有Context引用的对象，所以应该用它的子类AndroidViewModel，它可以接受Application作为参数，用于数据库的实例化。
public class CategoryViewModel extends ViewModel {
    public LiveData<Integer> userLiveData;
    public void observeCategoriesAndFeeds(@NonNull LifecycleOwner owner, @NonNull Observer<Integer> observer){
        if(userLiveData != null && userLiveData.hasObservers()){
            userLiveData.removeObservers(owner);
            userLiveData = null;
        }
        userLiveData = CoreDB.i().userDao().observeSize();
        userLiveData.observe(owner, observer);
        XLog.i("触发重新加载【分类树】");
    }

    public List<CollectionTree> getCategoryFeeds(){
        String uid = App.i().getUser().getId();

        // 总分类
        CollectionTree rootCategoryFeeds = new CollectionTree();
        Collection rootCollection = new Collection();
        rootCollection.setId("user/" + uid + App.CATEGORY_ALL);
        rootCollection.setTitle(App.i().getString(R.string.all));
        rootCategoryFeeds.setParent(rootCollection);
        rootCategoryFeeds.setType(CollectionTree.SMART);

        // 已退订的收藏文章
        CollectionTree unsubscribeArticles = null;

        List<Collection> categories;
        List<CollectionFeed> feeds;
        int count = 0;

        long time1 = System.currentTimeMillis();
        if( App.i().getUser().getStreamStatus() == App.STATUS_UNREAD ){
            categories = CoreDB.i().categoryDao().getCategoriesUnreadCount(uid);
            XLog.i("读取未读分类A，耗时：" + (System.currentTimeMillis() - time1));
            rootCollection.setCount(CoreDB.i().articleDao().getUnreadCount(uid));
            XLog.i("读取未读分类B，耗时：" + (System.currentTimeMillis() - time1));
            feeds = CoreDB.i().feedDao().getFeedsUnreadCount(uid);
            XLog.i("读取未读分类C，耗时：" + (System.currentTimeMillis() - time1));
            count = CoreDB.i().articleDao().getUnreadCountUnsubscribe(uid);
            XLog.i("读取未读分类D，耗时：" + (System.currentTimeMillis() - time1));
        }else if( App.i().getUser().getStreamStatus() == App.STATUS_STARED ){
            rootCollection.setCount(CoreDB.i().articleDao().getStarCount(uid));
            categories = CoreDB.i().categoryDao().getCategoriesStarCount(uid);
            feeds = CoreDB.i().feedDao().getFeedsStaredCount(uid);
            count = CoreDB.i().articleDao().getStarCountUnsubscribe(uid);
        }else {
            rootCollection.setCount(CoreDB.i().articleDao().getAllCount(uid));
            categories = CoreDB.i().categoryDao().getCategoriesAllCount(uid);
            feeds = CoreDB.i().feedDao().getFeedsAllCount(uid);
            count = CoreDB.i().articleDao().getAllCountUnsubscribe(uid);
        }

        if(count > 0){
            unsubscribeArticles = new CollectionTree();
            Collection unsubscribeCollection = new Collection();
            unsubscribeCollection.setId("user/" + uid + App.STREAM_UNSUBSCRIBED);
            unsubscribeCollection.setTitle(App.i().getString(R.string.unsubscribed));
            unsubscribeCollection.setCount(count);
            unsubscribeArticles.setType(CollectionTree.SMART);
            unsubscribeArticles.setParent(unsubscribeCollection);
        }

        long time = System.currentTimeMillis();
        List<CollectionTree> categoryFeedsList = Classifier.group2(categories, CoreDB.i().feedCategoryDao().getAll(uid), feeds);
        XLog.i("归类，耗时：" + (System.currentTimeMillis() - time));

        categoryFeedsList.add(0, rootCategoryFeeds);

        if(unsubscribeArticles != null){
            categoryFeedsList.add(1, unsubscribeArticles);
        }
        return categoryFeedsList;
    }
}
