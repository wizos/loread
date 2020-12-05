package me.wizos.loread.network.interceptor;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.elvishew.xlog.XLog;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import me.wizos.loread.BuildConfig;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

/**
 * Created by Wizos on 2019/4/3.
 */

public class LoggerInterceptor implements Interceptor {
    @NotNull
    @SuppressLint("DefaultLocale")
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        // 拦截请求，获取到该次请求的request
        Request request = chain.request();
        // 执行本次网络请求操作，返回response信息
        Response response = chain.proceed(request);

        if (BuildConfig.DEBUG) {
//            long t1 = System.nanoTime();
//            KLog.i(String.format("Sending request %s on %s%n%s", request.url(), chain.connection(), request.headers()));
//            long t2 = System.nanoTime();
//            KLog.i(String.format("Received response for %s in %.1fms%n%s", response.request().url(), (t2 - t1) / 1e6d, response.headers()));
//
//
//            if (!HttpHeaders.hasBody(response)) {
//                return response;
//            }

            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                return response;
            }
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();
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
                    XLog.d("body---------->" + bodyString.substring(0, 88));
                } else {
                    XLog.e("body---------->" + bodyString);
                }
            }
        }
        return response;
    }
}
