package me.wizos.loread.network.interceptor;

import android.text.TextUtils;

import java.io.IOException;

import me.wizos.loread.config.LinkRewriteConfig;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 域名重定向 拦截器
 *
 * @author Wizos
 * @version 1.0
 * @date 2019/4/2
 */

public class RedirectInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String newUrl = LinkRewriteConfig.i().getRedirectUrl( request.url().toString() );
        if (!TextUtils.isEmpty(newUrl)) {
            // 创建一个新请求，并相应地修改它
            request = request.newBuilder().url(newUrl).build();
        }
        return chain.proceed(request);
    }
}
