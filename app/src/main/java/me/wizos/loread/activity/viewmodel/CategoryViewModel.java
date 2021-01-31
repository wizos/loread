package me.wizos.loread.activity.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.db.Collection;
import me.wizos.loread.db.CoreDB;

//LiveData通常结合ViewModel一起使用。我们知道ViewModel是用来存放数据的，因此我们可以将数据库放在ViewModel中进行实例化。
// 但数据库在实例化的时候需要Context，而ViewModel不能传入任何带有Context引用的对象，所以应该用它的子类AndroidViewModel，它可以接受Application作为参数，用于数据库的实例化。
public class CategoryViewModel extends ViewModel {
    // public ArrayMap<String, LiveData<List<Collection>>> feedsMap;
    public LiveData<List<Collection>> categoriesLiveData;
    public LiveData<List<Collection>> feedsLiveData;

    public LiveData<List<Collection>> loadCategories(){
        if( App.i().getUser().getStreamStatus() == App.STATUS_UNREAD ){
            categoriesLiveData = CoreDB.i().categoryDao().getCategoriesUnreadCountLiveData(App.i().getUser().getId());
        }else if( App.i().getUser().getStreamStatus() == App.STATUS_STARED ){
            categoriesLiveData = CoreDB.i().categoryDao().getCategoriesStarCountLiveData(App.i().getUser().getId());
        }else {
            categoriesLiveData = CoreDB.i().categoryDao().getCategoriesAllCountLiveData(App.i().getUser().getId());
        }
        return categoriesLiveData;
    }

    public LiveData<List<Collection>> loadFeeds(String categoryId){
        if( App.i().getUser().getStreamStatus() == App.STATUS_UNREAD ){
            if(categoryId.contains(App.CATEGORY_UNCATEGORIZED)){
                feedsLiveData = CoreDB.i().feedDao().getFeedsLiveDataUnreadCountByUnCategory(App.i().getUser().getId());
            }else {
                feedsLiveData = CoreDB.i().feedDao().getFeedsLiveDataUnreadCountByCategoryId(App.i().getUser().getId(), categoryId);
            }
        }else if( App.i().getUser().getStreamStatus() == App.STATUS_STARED ){
            if(categoryId.contains(App.CATEGORY_UNCATEGORIZED)){
                feedsLiveData = CoreDB.i().feedDao().getFeedsLiveDataStarCountByUnCategory(App.i().getUser().getId());
            }else {
                feedsLiveData = CoreDB.i().feedDao().getFeedsLiveDataStarCountByCategoryId(App.i().getUser().getId(), categoryId);
            }
        }else {
            if(categoryId.contains(App.CATEGORY_UNCATEGORIZED)){
                feedsLiveData = CoreDB.i().feedDao().getFeedsLiveDataAllCountByUnCategory(App.i().getUser().getId());
            }else {
                feedsLiveData = CoreDB.i().feedDao().getFeedsLiveDataAllCountByCategoryId(App.i().getUser().getId(), categoryId);
            }
        }
        return categoriesLiveData;
    }
}
