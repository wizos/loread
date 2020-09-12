package me.wizos.loread.network.api;

import java.io.IOException;

import me.wizos.loread.network.callback.CallbackX;

public abstract class OAuthApi<T, E> extends AuthApi<T, E> {

    abstract public String getOAuthUrl();

    /**
     * 获取access_token，refresh_token，expires_in
     *
     * @param authorizationCode
     * @return
     * @throws IOException
     */
    //abstract public String getAccessToken(String authorizationCode) throws IOException;
    abstract public void getAccessToken(String authorizationCode, CallbackX cb);
    /**
     * 刷新access_token，refresh_token，expires_in
     *
     * @return
     * @throws IOException
     */
    abstract public String refreshingAccessToken(String refreshToken) throws IOException;
    abstract public void refreshingAccessToken(String refreshToken, CallbackX cb);

}
