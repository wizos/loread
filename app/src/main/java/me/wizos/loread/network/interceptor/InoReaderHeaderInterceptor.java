package me.wizos.loread.network.interceptor;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import me.wizos.loread.BuildConfig;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class InoReaderHeaderInterceptor implements Interceptor {
    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        builder.addHeader("AppId", BuildConfig.INOREADER_APP_ID);
        builder.addHeader("AppKey", BuildConfig.INOREADER_APP_KEY);
        return chain.proceed(builder.build());
    }
}
