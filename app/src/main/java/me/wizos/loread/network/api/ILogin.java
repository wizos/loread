package me.wizos.loread.network.api;
import me.wizos.loread.network.callback.CallbackX;

/**
 * 之所以登录采用接口，而不是LoginAuthApi的方式，是因为 Inoreader 即需要OAuth，又有Login
 */
public interface ILogin {
    void login(String account, String password, CallbackX cb);
}
