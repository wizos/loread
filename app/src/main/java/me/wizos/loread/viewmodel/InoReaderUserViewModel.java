package me.wizos.loread.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.activity.login.LoginResult;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.CorePref;
import me.wizos.loread.db.User;
import me.wizos.loread.db.UserDao;
import me.wizos.loread.network.api.InoReaderApi;
import me.wizos.loread.network.callback.CallbackX;

// LiveData通常结合ViewModel一起使用。我们知道ViewModel是用来存放数据的，因此我们可以将数据库放在ViewModel中进行实例化。
// 但数据库在实例化的时候需要Context，而ViewModel不能传入任何带有Context引用的对象，所以应该用它的子类AndroidViewModel，它可以接受Application作为参数，用于数据库的实例化。
public class InoReaderUserViewModel extends LoginViewModel {
    private UserDao userDao;
    // Creates a PagedList object with 50 items per page.
    public InoReaderUserViewModel(@NonNull Application application) {
        super(application);
        this.userDao = CoreDB.i().userDao();
    }

    public void login(String baseUrl, String username, String password) {
        InoReaderApi inoReaderApi = new InoReaderApi(baseUrl);
        inoReaderApi.login(username, password, new CallbackX<String,String>() {
            @Override
            public void onSuccess(String auth) {
                User user = new User();
                user.setSource(Contract.PROVIDER_INOREADER);
                user.setId(Contract.PROVIDER_INOREADER + "_" + username);
                user.setUserId(username);
                user.setUserName(username);
                user.setUserPassword(password);
                user.setAuth(auth);
                user.setExpiresTimestamp(0);
                user.setHost(baseUrl);
                inoReaderApi.setAuthorization(auth);
                CorePref.i().globalPref().putString(Contract.UID, user.getId());
                User userTmp = userDao.getById(user.getId());
                if (userTmp != null) {
                    CoreDB.i().userDao().update(user);
                }else {
                    CoreDB.i().userDao().insert(user);
                }

                LoginResult loginResult = new LoginResult().setSuccess(true).setData(auth);
                loginResultLiveData.postValue(loginResult);
            }

            @Override
            public void onFailure(String error) {
                LoginResult loginResult = new LoginResult().setSuccess(false).setData(App.i().getString(R.string.login_failed_reason, error));
                loginResultLiveData.postValue(loginResult);
            }
        });
    }


    // private MutableLiveData<LoginFormState> loginFormLiveData = new MutableLiveData<>();
    // private MutableLiveData<LoginResult> loginResultLiveData = new MutableLiveData<>();
    // public LiveData<LoginFormState> getLoginFormLiveData() {
    //     return loginFormLiveData;
    // }
    // public LiveData<LoginResult> getLoginResult() {
    //     return loginResultLiveData;
    // }
    //
    // public void loginDataChanged(String host, String username, String password) {
    //     LoginFormState loginFormState = new LoginFormState();
    //
    //     if (!isHostValid(host)) {
    //         loginFormState.setHostHint(R.string.invalid_site_url_hint);
    //     } else if (!isUserNameValid(username)) {
    //         loginFormState.setUsernameHint(R.string.invalid_username);
    //     } else if (!isPasswordValid(password)) {
    //         loginFormState.setPasswordHint(R.string.invalid_password);
    //     } else {
    //         loginFormState.setDataValid(true);
    //     }
    //
    //     loginFormLiveData.setValue(loginFormState);
    // }
    //
    // private boolean isHostValid(String host) {
    //     return !TextUtils.isEmpty(host) && Patterns.WEB_URL.matcher(host).matches();
    // }
    //
    // // A placeholder username validation check
    // private boolean isUserNameValid(String username) {
    //     return !TextUtils.isEmpty(username) && !username.trim().isEmpty();
    // }
    //
    // // A placeholder password validation check
    // private boolean isPasswordValid(String password) {
    //     return !TextUtils.isEmpty(password) && password.trim().length() > 5;
    // }
}
