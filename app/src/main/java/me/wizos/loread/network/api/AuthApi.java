package me.wizos.loread.network.api;

import me.wizos.loread.network.callback.CallbackX;

public abstract class AuthApi extends BaseApi {
    private String authorization;
    public void setAuthorization(String authorization){
        this.authorization = authorization;
    }
    public String getAuthorization(){
        return authorization;
    }

    abstract public void fetchUserInfo(CallbackX cb);
}
