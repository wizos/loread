package me.wizos.loread.network.api;
import me.wizos.loread.network.callback.CallbackX;

public interface LoginInterface {
    void login(String accountId, String accountPd, CallbackX cb);
}
