package me.wizos.loread.activity.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import me.wizos.loread.db.Article;
import me.wizos.loread.db.CoreDB;

//LiveData通常结合ViewModel一起使用。我们知道ViewModel是用来存放数据的，因此我们可以将数据库放在ViewModel中进行实例化。
// 但数据库在实例化的时候需要Context，而ViewModel不能传入任何带有Context引用的对象，所以应该用它的子类AndroidViewModel，它可以接受Application作为参数，用于数据库的实例化。
public class ArticleViewModel extends AndroidViewModel {
    private LiveData<Article> articleLiveData;

    public ArticleViewModel(@NonNull Application application) {
        super(application);
    }

    public void loadArticle(String uid, String articleId, @NonNull LifecycleOwner owner, @NonNull Observer<Article> observer){
        if(articleLiveData != null && articleLiveData.hasObservers()){
            articleLiveData.removeObservers(owner);
        }
        articleLiveData = CoreDB.i().articleDao().get(uid, articleId);
        articleLiveData.observe(owner, observer);
    }
}
