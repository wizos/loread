package me.wizos.loread.network.api;

import java.io.IOException;

import me.wizos.loread.network.callback.CallbackX;

public abstract class OAuthApi extends AuthApi {

    abstract public String getOAuthUrl();

    /**
     * 获取access_token，refresh_token，expires_in
     */
    abstract public void getAccessToken(String authorizationCode, CallbackX cb);
    /**
     * 刷新access_token，refresh_token，expires_in
     */
    abstract public String refreshingAccessToken(String refreshToken) throws IOException;
    abstract public void refreshingAccessToken(String refreshToken, CallbackX cb);
}
