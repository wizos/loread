package me.wizos.loread.bean;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.socks.library.KLog;

/**
 * Created by Wizos on 2019/2/17.
 */

public class Token {
    /*
    1、refresh_token也有过期时间，如百度云平台会提供有效期为1个月的Access Token和有效期为10年的Refresh Token，
    2、当refresh_token过期的时候，则需要用户重新授权登录。 
    3、每次登录后，会产生新token，原来的access_token与refresh_token自然失效。
    4、refresh_token 仅能使用一次，使用一次后，将被废弃。
    原文：https://blog.csdn.net/lvxiangan/article/details/78020674
     */
    private String access_token;
    private String refresh_token;
    private String token_type;
    // 秒
    private long expires_in;

    @SerializedName("error")
    private String error;
    @SerializedName("auth")
    private String auth;


//    {
//        "access_token": "[ACCESS_TOKEN]",
//            "token_type": "Bearer",
//            "expires_in": [EXPIRATION_IN_SECONDS],
//        "refresh_token": "[REFRESH_TOKEN]",
//            "scope": "read"
//    }
//    {
//        "id": "c805fcbf-3acf-4302-a97e-d82f9d7c897f",
//            "refresh_token": "AQAA7rJ7InAiOjEsImEiOiJmZWVk...",
//            "access_token": "AQAAF4iTvPam_M4_dWheV_5NUL8E...",
//            "expires_in": 3920,
//            "token_type": "Bearer",
//            "plan": "standard",
//            "state": "..."
//    }


    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public String getToken_type() {
//        if(!TextUtils.isEmpty(token_type)){
//            token_type = token_type.substring(0, 1).toUpperCase() + token_type.substring(1);
//        }
        return token_type;
    }

    public void setToken_type(String token_type) {
        KLog.e("授权吗A：" + token_type);
        if (!TextUtils.isEmpty(token_type)) {
            token_type = token_type.substring(0, 1).toUpperCase() + token_type.substring(1);
        }
        this.token_type = token_type;
    }

    public long getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(long refresh_token) {
        this.expires_in = expires_in;
    }

    public String getAuth() {
        return token_type + " " + access_token;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }


    @Override
    public String toString() {
        return "Token{" +
                "access_token='" + access_token + '\'' +
                ", refresh_token='" + refresh_token + '\'' +
                ", token_type='" + token_type + '\'' +
                ", Auth='" + getAuth() + '\'' +
                ", expires_in=" + expires_in +
                ", error='" + error + '\'' +
                '}';
    }
}
