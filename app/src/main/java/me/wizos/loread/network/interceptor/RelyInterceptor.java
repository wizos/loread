package me.wizos.loread.network.interceptor;

import android.text.TextUtils;
import android.webkit.CookieManager;

import androidx.annotation.NonNull;

import com.socks.library.KLog;

import java.io.IOException;

import me.wizos.loread.config.LinkRewriteConfig;
import me.wizos.loread.config.NetworkRefererConfig;
import me.wizos.loread.config.NetworkUserAgentConfig;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 依赖 拦截器
 * 用于给指定网站增加 referer，cookie，ua，重定向等
 *
 * @author Wizos
 * @version 1.0
 * @date 2019/4/2
 */

public class RelyInterceptor implements Interceptor {
    @Override
    @NonNull
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        String url = request.url().toString();
        boolean hasNew = false;
        String newUrl = LinkRewriteConfig.i().getRedirectUrl(url).trim();
        if (!TextUtils.isEmpty(newUrl)) {
            // 创建一个新请求，并相应地修改它
            builder.url(newUrl);
            url = newUrl;
            hasNew = true;
        }

        // 使用完整的url或者topPrivateDomain都可以获取到cookie
        String cookie = CookieManager.getInstance().getCookie(url);
        if (!TextUtils.isEmpty(cookie)) {
            builder.header("Cookie", cookie );
            hasNew = true;
        }

        String referer = NetworkRefererConfig.i().guessRefererByUrl(url);
        if (!TextUtils.isEmpty(referer)) {
            builder.header("Referer", referer );
            hasNew = true;
        }

        String ua = NetworkUserAgentConfig.i().guessUserAgentByUrl(url);
        if (!TextUtils.isEmpty(ua)) {
            builder.header("User-Agent", ua );
            hasNew = true;
        }
        KLog.i("拦截到依赖：" + url + " , " + newUrl + " , " + referer + " =  " + cookie  + " =  "  + ua );

        if(hasNew){
            return chain.proceed(builder.build());
        }
        return chain.proceed(request);
    }
}
