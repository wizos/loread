package me.wizos.loread.network.callback;

/**
 * @author Wizos
 * @date 2019/11/24
 */

public interface CallbackX<T,E> {
    void onSuccess(T result);
    void onFailure(E error);
}
