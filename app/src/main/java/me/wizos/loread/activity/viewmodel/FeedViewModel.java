package me.wizos.loread.activity.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;

//LiveData通常结合ViewModel一起使用。我们知道ViewModel是用来存放数据的，因此我们可以将数据库放在ViewModel中进行实例化。
// 但数据库在实例化的时候需要Context，而ViewModel不能传入任何带有Context引用的对象，所以应该用它的子类AndroidViewModel，它可以接受Application作为参数，用于数据库的实例化。
public class FeedViewModel extends AndroidViewModel {
    private LiveData<Feed> feedLiveData;

    public FeedViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadFeed(String uid, String articleId, @NonNull LifecycleOwner owner, @NonNull Observer<Feed> observer){
        if(feedLiveData != null && feedLiveData.hasObservers()){
            feedLiveData.removeObservers(owner);
        }
        feedLiveData = CoreDB.i().feedDao().get(uid, articleId);
        feedLiveData.observe(owner, observer);
    }
}
