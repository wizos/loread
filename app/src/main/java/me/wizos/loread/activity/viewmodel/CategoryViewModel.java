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
import me.wizos.loread.bean.StreamTree;
import me.wizos.loread.db.Collection;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.utils.Classifier;

// LiveData通常结合ViewModel一起使用。我们知道ViewModel是用来存放数据的，因此我们可以将数据库放在ViewModel中进行实例化。
// 但数据库在实例化的时候需要Context，而ViewModel不能传入任何带有Context引用的对象，所以应该用它的子类AndroidViewModel，它可以接受Application作为参数，用于数据库的实例化。
public class CategoryViewModel extends ViewModel {
    public LiveData<Integer> userLiveData;
    public void observeCategoriesAndFeeds(@NonNull LifecycleOwner owner, @NonNull Observer<Integer> observer){
        userLiveData = CoreDB.i().userDao().observeSize();
        userLiveData.observe(owner, observer);
        XLog.i("触发重新加载【分类树】");
    }

    public List<StreamTree> getCategoryFeeds(){
        String uid = App.i().getUser().getId();

        // 总分类
        StreamTree rootCategoryFeeds = new StreamTree();
        rootCategoryFeeds.setStreamId("user/" + uid + App.CATEGORY_ALL);
        rootCategoryFeeds.setStreamName(App.i().getString(R.string.all));
        rootCategoryFeeds.setStreamType(StreamTree.SMART);

        // 未分类
        // StreamTree unCategoryFeeds = new StreamTree();
        // unCategoryFeeds.setStreamId("user/" + uid + App.STREAM_UNSUBSCRIBED);
        // unCategoryFeeds.setStreamName(App.i().getString(R.string.un_category));
        // unCategoryFeeds.setStreamType(StreamTree.SMART);


        // 已退订的收藏文章
        StreamTree unsubscribeArticles = null;

        List<Collection> categoryCollection;
        List<Collection> feedCollection;
        int count = 0;
        if( App.i().getUser().getStreamStatus() == App.STATUS_UNREAD ){
            rootCategoryFeeds.setCount(CoreDB.i().articleDao().getUnreadCount(uid));
            categoryCollection = CoreDB.i().categoryDao().getCategoriesUnreadCount(uid);
            feedCollection = CoreDB.i().feedDao().getFeedsUnreadCount(uid);
            count = CoreDB.i().articleDao().getUnreadCountUnsubscribe(uid);
        }else if( App.i().getUser().getStreamStatus() == App.STATUS_STARED ){
            rootCategoryFeeds.setCount(CoreDB.i().articleDao().getStarCount(uid));
            categoryCollection = CoreDB.i().categoryDao().getCategoriesStarCount(uid);
            feedCollection = CoreDB.i().feedDao().getFeedsStaredCount(uid);
            count = CoreDB.i().articleDao().getStarCountUnsubscribe(uid);
        }else {
            rootCategoryFeeds.setCount(CoreDB.i().articleDao().getAllCount(uid));
            categoryCollection = CoreDB.i().categoryDao().getCategoriesAllCount(uid);
            feedCollection = CoreDB.i().feedDao().getFeedsAllCount(uid);
            count = CoreDB.i().articleDao().getAllCountUnsubscribe(uid);
        }

        if(count > 0){
            unsubscribeArticles = new StreamTree();
            unsubscribeArticles.setStreamId("user/" + uid + App.STREAM_UNSUBSCRIBED);
            unsubscribeArticles.setStreamName(App.i().getString(R.string.unsubscribed));
            unsubscribeArticles.setStreamType(StreamTree.SMART);
            unsubscribeArticles.setCount(count);
        }

        List<StreamTree> categoryFeedsList = Classifier.group3(categoryCollection, CoreDB.i().feedCategoryDao().getAll(uid), feedCollection);

        // XLog.i("获得的 A ：" + categoryCollection);
        // XLog.i("获得的 B ：" + CoreDB.i().feedCategoryDao().getAll(uid));
        // XLog.i("获得的 C ：" + feedCollection);
        // XLog.i("获得的 D ：" + categoryFeedsList);

        categoryFeedsList.add(0, rootCategoryFeeds);

        if(unsubscribeArticles != null){
            categoryFeedsList.add(1, unsubscribeArticles);
        }
        return categoryFeedsList;
    }
}
