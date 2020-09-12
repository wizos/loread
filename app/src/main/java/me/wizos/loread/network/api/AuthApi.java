package me.wizos.loread.network.api;

public abstract class AuthApi<T, E> extends BaseApi<T, E> {
    private String authorization;
    public void setAuthorization(String authorization){
        this.authorization = authorization;
    }
    public String getAuthorization(){
        return authorization;
    }
}
