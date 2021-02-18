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
import me.wizos.loread.bean.CategoryFeeds;
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

    public List<CategoryFeeds> getCategoryFeeds(){
        String uid = App.i().getUser().getId();

        // 总分类
        CategoryFeeds rootCategoryFeeds = new CategoryFeeds();
        rootCategoryFeeds.setCategoryId("user/" + uid + App.CATEGORY_ALL);
        rootCategoryFeeds.setCategoryName(App.i().getString(R.string.all));

        // 未分类
        CategoryFeeds unCategoryFeeds = new CategoryFeeds();
        unCategoryFeeds.setCategoryId("user/" + uid + App.CATEGORY_UNCATEGORIZED);
        unCategoryFeeds.setCategoryName(App.i().getString(R.string.un_category));

        List<Collection> categoryCollection;
        List<Collection> feedCollection;

        if( App.i().getUser().getStreamStatus() == App.STATUS_UNREAD ){
            rootCategoryFeeds.setCount(CoreDB.i().articleDao().getUnreadCount(uid));
            unCategoryFeeds.setCount(CoreDB.i().articleDao().getUncategoryUnreadCount(uid));
            unCategoryFeeds.setFeeds(CoreDB.i().feedDao().getFeedsUnreadCountByUnCategory(App.i().getUser().getId()));
            categoryCollection = CoreDB.i().categoryDao().getCategoriesUnreadCount(uid);
            feedCollection = CoreDB.i().feedDao().getFeedsUnreadCount(uid);
        }else if( App.i().getUser().getStreamStatus() == App.STATUS_STARED ){
            rootCategoryFeeds.setCount(CoreDB.i().articleDao().getStarCount(uid));
            unCategoryFeeds.setCount(CoreDB.i().articleDao().getUncategoryStarCount(uid));
            unCategoryFeeds.setFeeds(CoreDB.i().feedDao().getFeedsStarCountByUnCategory(App.i().getUser().getId()));
            categoryCollection = CoreDB.i().categoryDao().getCategoriesStarCount(uid);
            feedCollection = CoreDB.i().feedDao().getFeedsStaredCount(uid);
        }else {
            rootCategoryFeeds.setCount(CoreDB.i().articleDao().getAllCount(uid));
            unCategoryFeeds.setCount(CoreDB.i().articleDao().getUncategoryAllCount(uid));
            unCategoryFeeds.setFeeds(CoreDB.i().feedDao().getFeedsAllCountByUnCategory(App.i().getUser().getId()));
            categoryCollection = CoreDB.i().categoryDao().getCategoriesAllCount(uid);
            feedCollection = CoreDB.i().feedDao().getFeedsAllCount(uid);
        }

        List<CategoryFeeds> categoryFeedsList = Classifier.group(categoryCollection, CoreDB.i().feedCategoryDao().getAll(uid), feedCollection);

        // XLog.i("获得的 A ：" + categoryCollection);
        // XLog.i("获得的 B ：" + CoreDB.i().feedCategoryDao().getAll(uid));
        // XLog.i("获得的 C ：" + feedCollection);
        // XLog.i("获得的 D ：" + categoryFeedsList);

        categoryFeedsList.add(0, rootCategoryFeeds);
        // XLog.i( "加载分类：" + CoreDB.i().feedDao().getFeedsCountByUnCategory(uid) + " -> " + unCategoryFeeds.getCount());

        if(CoreDB.i().feedDao().getFeedsCountByUnCategory(uid) != 0){
            categoryFeedsList.add(1, unCategoryFeeds);
        }

        return categoryFeedsList;
    }




    // public LiveData<List<Collection>> categoriesLiveData;
    // public LiveData<List<Collection>> feedsLiveData;
    // public MutableLiveData<List<CategoryFeeds>> groupedFeedsLiveData = new MutableLiveData<>();
    // public LiveData<List<Collection>> loadCategories(){
    //     if( App.i().getUser().getStreamStatus() == App.STATUS_UNREAD ){
    //         categoriesLiveData = CoreDB.i().categoryDao().getCategoriesUnreadCountLiveData(App.i().getUser().getId());
    //     }else if( App.i().getUser().getStreamStatus() == App.STATUS_STARED ){
    //         categoriesLiveData = CoreDB.i().categoryDao().getCategoriesStarCountLiveData(App.i().getUser().getId());
    //     }else {
    //         categoriesLiveData = CoreDB.i().categoryDao().getCategoriesAllCountLiveData(App.i().getUser().getId());
    //     }
    //     return categoriesLiveData;
    // }
    //
    //
    // public void loadGroupedFeeds(){
    //     if( App.i().getUser().getStreamStatus() == App.STATUS_UNREAD ){
    //         categoriesLiveData = CoreDB.i().categoryDao().getCategoriesUnreadCountLiveData(App.i().getUser().getId());
    //         feedsLiveData = CoreDB.i().feedDao().getFeedsLiveDataUnreadCount(App.i().getUser().getId());
    //     }else if( App.i().getUser().getStreamStatus() == App.STATUS_STARED ){
    //         categoriesLiveData = CoreDB.i().categoryDao().getCategoriesStarCountLiveData(App.i().getUser().getId());
    //         feedsLiveData = CoreDB.i().feedDao().getFeedsLiveDataStaredCount(App.i().getUser().getId());
    //     }else {
    //         categoriesLiveData = CoreDB.i().categoryDao().getCategoriesAllCountLiveData(App.i().getUser().getId());
    //         feedsLiveData = CoreDB.i().feedDao().getFeedsLiveDataAllCount(App.i().getUser().getId());
    //     }
    //     groupedFeedsLiveData.postValue(Classifier.group2(categoriesLiveData.getValue(), CoreDB.i().feedCategoryDao().getAll(App.i().getUser().getId()), feedsLiveData.getValue()));
    // }
    //
    // public LiveData<List<Collection>> loadFeeds(String categoryId){
    //     if( App.i().getUser().getStreamStatus() == App.STATUS_UNREAD ){
    //         if(categoryId.contains(App.CATEGORY_UNCATEGORIZED)){
    //             feedsLiveData = CoreDB.i().feedDao().getFeedsLiveDataUnreadCountByUnCategory(App.i().getUser().getId());
    //         }else {
    //             feedsLiveData = CoreDB.i().feedDao().getFeedsLiveDataUnreadCountByCategoryId(App.i().getUser().getId(), categoryId);
    //         }
    //     }else if( App.i().getUser().getStreamStatus() == App.STATUS_STARED ){
    //         if(categoryId.contains(App.CATEGORY_UNCATEGORIZED)){
    //             feedsLiveData = CoreDB.i().feedDao().getFeedsLiveDataStarCountByUnCategory(App.i().getUser().getId());
    //         }else {
    //             feedsLiveData = CoreDB.i().feedDao().getFeedsLiveDataStarCountByCategoryId(App.i().getUser().getId(), categoryId);
    //         }
    //     }else {
    //         if(categoryId.contains(App.CATEGORY_UNCATEGORIZED)){
    //             feedsLiveData = CoreDB.i().feedDao().getFeedsLiveDataAllCountByUnCategory(App.i().getUser().getId());
    //         }else {
    //             feedsLiveData = CoreDB.i().feedDao().getFeedsLiveDataAllCountByCategoryId(App.i().getUser().getId(), categoryId);
    //         }
    //     }
    //     return categoriesLiveData;
    // }
}
