package me.wizos.loread.network.interceptor;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.elvishew.xlog.XLog;

import java.io.IOException;

import me.wizos.loread.App;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.User;
import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * 处理 401 Unauthorized
 * Created by Wizos on 2019/4/2.
 */

public class TokenAuthenticator implements Authenticator {
    /**
     * 通过okhttp提供的Authenticator接口，只有在服务端返回HTTP的状态码为401时，才会使用Authenticator接口，如果服务端设计规范，可以尝试如下方法。
     * <p>
     * Feedly 在输入错误的 authorization 会报：
     * 1.请提供授权码 "errorMessage": "must provide authorization token"
     * 2.授权码过期 "errorMessage": "token expired: 1552176000000 (-2044594)"
     * <p>
     * Inoreader 则是返回空
     *
     * @param route
     * @param response
     * @return
     * @throws IOException
     */
    @Override
    public Request authenticate(Route route, @NonNull Response response) throws IOException {
        XLog.e("TokenAuthenticator授权过期");
        // 重试超过限制则放弃
        if (responseCount(response) >= 2) {
            return null;
        }
        //取出本地的refreshToken
        String refreshToken = App.i().getUser().getRefreshToken();
        XLog.e("TokenAuthenticator授权过期：刷新码 " + refreshToken);
        if (TextUtils.isEmpty(refreshToken)) {
            return null;
        }

        // 通过一个特定的接口获取新的token，此处要用到同步的retrofit请求
        String authorization = App.i().getOAuthApi().refreshingAccessToken(refreshToken);

        //要用retrofit的同步方式
        XLog.e("TokenAuthenticator授权过期：授权码 " + authorization);

        User user = App.i().getUser();
        if(user != null){
            user.setAuth(authorization);
            CoreDB.i().userDao().update(user);
        }
        return response.request().newBuilder()
                .addHeader("authorization", authorization)
                .build();
    }


    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

}
