package me.wizos.loread.network.interceptor;

import android.text.TextUtils;

import com.socks.library.KLog;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import me.wizos.loread.App;
import me.wizos.loread.activity.login.LoginResult;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.User;
import me.wizos.loread.network.api.LoreadApi;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * 自动刷新token的拦截器
 * https://www.jianshu.com/p/62ab11ddacc8
 *
 * @author Wizos
 * @version 1.0
 * @date 2019/4/2
 */

public class LoreadTokenInterceptor implements Interceptor {
    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response originalResponse = chain.proceed(request);

        /*通过如下的办法曲线取到请求完成的数据
         *
         * 原本想通过  originalResponse.body().string()
         * 去取到请求完成的数据,但是一直报错,不知道是okhttp的bug还是操作不当
         *
         * 然后去看了okhttp的源码,找到了这个曲线方法,取到请求完成的数据后,根据特定的判断条件去判断token过期
         */
        ResponseBody responseBody = originalResponse.body();
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.getBuffer(); // .buffer();
        Charset charset;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(StandardCharsets.UTF_8);
        }else {
            charset = StandardCharsets.UTF_8;
        }
        String bodyString = buffer.clone().readString(charset);
        if (!TextUtils.isEmpty(bodyString)) {
            if (bodyString.length() > 88) {
                KLog.i("body->" + bodyString.substring(0, 88));
            } else {
                KLog.i("body->" + bodyString);
            }
        }

        /***************************************/

        //根据和服务端的约定判断token过期
        if (bodyString.contains("\"error\":\"NOT_LOGGED_IN")) {
            User user = App.i().getUser();

            // 通过一个特定的接口获取新的token，此处要用到同步的retrofit请求
            LoginResult loginResult = ((LoreadApi)App.i().getApi()).login(user.getUserId(),user.getUserPassword());
            if( loginResult.isSuccess() ){
                KLog.e("TokenInterceptor授权过期：成功重新登录 " + loginResult.getData() );
                user.setAuth(loginResult.getData());
                CoreDB.i().userDao().update(user);
                App.i().getAuthApi().setAuthorization(user.getAuth());
                Request.Builder builder = request.newBuilder().header("authorization", user.getAuth());
                return chain.proceed(builder.build());
            }
        }
        // 否则，只需传递原始响应
        return originalResponse;
    }
}
