package me.wizos.loread.network;

import com.lzy.okgo.https.HttpsUtils;

import java.util.concurrent.TimeUnit;

import me.wizos.loread.network.interceptor.InoreaderHeaderInterceptor;
import me.wizos.loread.network.interceptor.LoggerInterceptor;
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
    private static OkHttpClient searchHttpClient;
    private static OkHttpClient ttrssHttpClient;
    private static OkHttpClient feverHttpClient;
    private static OkHttpClient inoreaderHttpClient;
    private static OkHttpClient feedlyHttpClient;

    private static OkHttpClient imageHttpClient;
    private static OkHttpClient glideHttpClient;

    public static HttpClientManager i() {
        if (instance == null) {
            synchronized (HttpClientManager.class) {
                if (instance == null) {
                    instance = new HttpClientManager();
                    searchHttpClient = new OkHttpClient.Builder()
                            .readTimeout(10, TimeUnit.SECONDS)
                            .writeTimeout(10, TimeUnit.SECONDS)
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                            .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                            .followRedirects(true)
                            .followSslRedirects(true)
                            .addInterceptor(new RelyInterceptor())
                            .addInterceptor(new RefererInterceptor())
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
                            // .dns(new FastDNS())
                            .build();
                    ttrssHttpClient = new OkHttpClient.Builder()
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                            .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                            .followRedirects(true)
                            .followSslRedirects(true)
                            // .authenticator(new TTRSSAuthenticator())
                            .addInterceptor(new TTRSSTokenInterceptor())
                            // .dns(new FastDNS())
                            .build();
                    feverHttpClient = new OkHttpClient.Builder()
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                            .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                            .followRedirects(true)
                            .followSslRedirects(true)
                            // .dns(new FastDNS())
                            .build();

                    inoreaderHttpClient = new OkHttpClient.Builder()
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                            .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                            .followRedirects(true)
                            .followSslRedirects(true)
                            // .addInterceptor(new AuthorizationInterceptor())
                            .addInterceptor(new InoreaderHeaderInterceptor())
                            .addInterceptor(new LoggerInterceptor())
                            .authenticator(new TokenAuthenticator())
                            // .dns(new HttpDNS())
                            .build();
                    feedlyHttpClient = new OkHttpClient.Builder()
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .connectTimeout(15, TimeUnit.SECONDS)
                            .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                            .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                            .followRedirects(true)
                            .followSslRedirects(true)
                            // .addInterceptor(new AuthorizationInterceptor())
                            .addInterceptor(new LoggerInterceptor())
                            .authenticator(new TokenAuthenticator())
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
                            // .dns(new FastDNS())
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
                            // .dns(new FastDNS())
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

    public OkHttpClient searchClient() {
        return searchHttpClient;
    }

    public OkHttpClient ttrssHttpClient() {return ttrssHttpClient;}

    public OkHttpClient feverHttpClient() {return feverHttpClient;}

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
