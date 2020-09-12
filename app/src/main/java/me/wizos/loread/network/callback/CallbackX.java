package me.wizos.loread.network.callback;

/**
 * Created by Wizos on 2019/11/24.
 */

public interface CallbackX<T,E> {
    void onSuccess(T result);
    void onFailure(E error);
}
