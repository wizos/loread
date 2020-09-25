package me.wizos.loread.network;

import com.lzy.okgo.https.HttpsUtils;

import java.util.concurrent.TimeUnit;

import me.wizos.loread.network.interceptor.InoreaderHeaderInterceptor;
import me.wizos.loread.network.interceptor.LoggerInterceptor;
import me.wizos.loread.network.interceptor.LoreadTokenInterceptor;
import me.wizos.loread.network.interceptor.RefererInterceptor;
import me.wizos.loread.network.interceptor.RelyInterceptor;
import me.wizos.loread.network.interceptor.TTRSSTokenInterceptor;
import me.wizos.loread.network.interceptor.TokenAuthenticator;
import okhttp3.OkHttpClient;

/**
 * @author Wizos on 2019/5/12.
 */

public class HttpClientManager {
    private HttpClientManager() {
    }

    private static HttpClientManager instance;
    private static OkHttpClient simpleOkHttpClient;
    private static OkHttpClient loreadHttpClient;
    private static OkHttpClient ttrssHttpClient;
    private static OkHttpClient inoreaderHttpClient;
    private static OkHttpClient feedlyHttpClient;

    private static OkHttpClient imageHttpClient;
    private static OkHttpClient glideHttpClient;

    public static HttpClientManager i() {
        if (instance == null) {
            synchronized (HttpClientManager.class) {
                if (instance == null) {
                    instance = new HttpClientManager();

                    loreadHttpClient = new OkHttpClient.Builder()
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                            .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                            .followRedirects(true)
                            .followSslRedirects(true)
                            .addInterceptor(new LoreadTokenInterceptor())
                            .build();
                    ttrssHttpClient = new OkHttpClient.Builder()
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                            .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                            .followRedirects(true)
                            .followSslRedirects(true)
//                            .authenticator(new TTRSSAuthenticator())
                            .addInterceptor(new TTRSSTokenInterceptor())
                            .build();
                    inoreaderHttpClient = new OkHttpClient.Builder()
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                            .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                            .followRedirects(true)
                            .followSslRedirects(true)
//                            .addInterceptor(new AuthorizationInterceptor())
                            .addInterceptor(new InoreaderHeaderInterceptor())
                            .addInterceptor(new LoggerInterceptor())
                            .authenticator(new TokenAuthenticator())
//                            .dns(new HttpDNS())
                            .build();
                    feedlyHttpClient = new OkHttpClient.Builder()
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                            .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                            .followRedirects(true)
                            .followSslRedirects(true)
//                            .addInterceptor(new AuthorizationInterceptor())
                            .addInterceptor(new LoggerInterceptor())
                            .authenticator(new TokenAuthenticator())
                            .build();
                    simpleOkHttpClient = new OkHttpClient.Builder()
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                            .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                            .followRedirects(true)
                            .followSslRedirects(true)
                            .addInterceptor(new RelyInterceptor())
                            .addInterceptor(new RefererInterceptor())
                            .build();
                    imageHttpClient = new OkHttpClient.Builder()
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                            .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                            .followRedirects(true)
                            .followSslRedirects(true)
                            .addInterceptor(new RelyInterceptor())
                            .build();
                    imageHttpClient.dispatcher().setMaxRequests(4);

                    glideHttpClient = new OkHttpClient.Builder()
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                            .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                            .followRedirects(true)
                            .followSslRedirects(true)
                            .addInterceptor(new RelyInterceptor())
                            .addInterceptor(new RefererInterceptor())
                            .build();
                    glideHttpClient.dispatcher().setMaxRequests(4);
                }
            }
        }
        return instance;
    }


    public OkHttpClient simpleClient() {
        return simpleOkHttpClient;
    }

    public OkHttpClient loreadHttpClient() {
        return loreadHttpClient;
    }

    public OkHttpClient ttrssHttpClient() {return ttrssHttpClient;}

    public OkHttpClient inoreaderHttpClient() {
        return inoreaderHttpClient;
    }

    public OkHttpClient feedlyHttpClient() {
        return feedlyHttpClient;
    }

    public OkHttpClient imageHttpClient() {
        return imageHttpClient;
    }

    public OkHttpClient glideHttpClient() {
        return glideHttpClient;
    }
}
