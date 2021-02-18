package me.wizos.loread.bean;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Wizos on 2019/2/17.
 */

public class Token {
    @SerializedName("access_token")
    private String accessToken;
    @SerializedName("refresh_token")
    private String refreshToken;
    @SerializedName("token_type")
    private String tokenType;
    // 秒
    @SerializedName("expires_in")
    private long expiresIn;

    @SerializedName("error")
    private String error;
    @SerializedName("auth")
    private String auth;


    /*
     * 1、refresh_token也有过期时间，如百度云平台会提供有效期为1个月的Access Token和有效期为10年的Refresh Token。
     * 2、当refresh_token过期的时候，则需要用户重新授权登录。
     * 3、每次登录后，会产生新token，原来的access_token与refresh_token自
     * 原文：https://blog.csdn.net/lvxiangan/article/details/78020674
     */
    // {
    //     "access_token": "[ACCESS_TOKEN]",
    //         "token_type": "Bearer",
    //         "expires_in": [EXPIRATION_IN_SECONDS],
    //     "refresh_token": "[REFRESH_TOKEN]",
    //         "scope": "read"
    // }
    // {
    //     "id": "c805fcbf-3acf-4302-a97e-d82f9d7c897f",
    //         "refresh_token": "AQAA7rJ7InAiOjEsImEiOiJmZWVk...",
    //         "access_token": "AQAAF4iTvPam_M4_dWheV_5NUL8E...",
    //         "expires_in": 3920,
    //         "token_type": "Bearer",
    //         "plan": "standard",
    //         "state": "..."
    // }


    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        // if (!TextUtils.isEmpty(tokenType)) {
        //     tokenType = tokenType.substring(0, 1).toUpperCase() + tokenType.substring(1);
        // }
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getAuth() {
        return tokenType + " " + accessToken;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }


    @NotNull
    @Override
    public String toString() {
        return "Token{" +
                "accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", error='" + error + '\'' +
                ", auth='" + auth + '\'' +
                '}';
    }
}
