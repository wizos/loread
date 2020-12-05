package me.wizos.loread.viewmodel;

import android.app.Application;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import me.wizos.loread.R;
import me.wizos.loread.activity.login.LoginFormState;
import me.wizos.loread.activity.login.LoginResult;

// LiveData通常结合ViewModel一起使用。我们知道ViewModel是用来存放数据的，因此我们可以将数据库放在ViewModel中进行实例化。
// 但数据库在实例化的时候需要Context，而ViewModel不能传入任何带有Context引用的对象，所以应该用它的子类AndroidViewModel，它可以接受Application作为参数，用于数据库的实例化。
public abstract class LoginViewModel extends AndroidViewModel {
    // Creates a PagedList object with 50 items per page.
    public LoginViewModel(@NonNull Application application) {
        super(application);
    }

    public abstract void login(String baseUrl, String username, String password);

    protected MutableLiveData<LoginFormState> loginFormLiveData = new MutableLiveData<>();
    protected MutableLiveData<LoginResult> loginResultLiveData = new MutableLiveData<>();
    public LiveData<LoginFormState> getLoginFormLiveData() {
        return loginFormLiveData;
    }
    public LiveData<LoginResult> getLoginResult() {
        return loginResultLiveData;
    }

    public void loginDataChanged(String host, String username, String password) {
        LoginFormState loginFormState = new LoginFormState();

        if (!isHostValid(host)) {
            loginFormState.setHostHint(R.string.invalid_site_url_hint);
        } else if (!isUserNameValid(username)) {
            loginFormState.setUsernameHint(R.string.invalid_username);
        } else if (!isPasswordValid(password)) {
            loginFormState.setPasswordHint(R.string.invalid_password);
        } else {
            loginFormState.setDataValid(true);
        }

        loginFormLiveData.setValue(loginFormState);
    }

    private boolean isHostValid(String host) {
        return !TextUtils.isEmpty(host) && Patterns.WEB_URL.matcher(host).matches();
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        return !TextUtils.isEmpty(username) && !username.trim().isEmpty();
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return !TextUtils.isEmpty(password) && password.trim().length() > 5;
    }
}
