package me.wizos.loread.network;

import android.text.TextUtils;

import com.elvishew.xlog.XLog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import me.wizos.loread.App;
import me.wizos.loread.network.interceptor.FeverTinyRSSTokenInterceptor;
import me.wizos.loread.network.interceptor.InoReaderTokenInterceptor;
import me.wizos.loread.network.interceptor.LoggerInterceptor;
import me.wizos.loread.network.interceptor.RefererInterceptor;
import me.wizos.loread.network.interceptor.RelyInterceptor;
import me.wizos.loread.network.interceptor.TinyRSSTokenInterceptor;
import me.wizos.loread.network.interceptor.TokenAuthenticator;
import me.wizos.loread.utils.HttpsUtils;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * @author Wizos on 2019/5/12.
 */

public class HttpClientManager {
    private static HttpClientManager instance;

    private static OkHttpClient simpleOkHttpClient;
    private static OkHttpClient smallOkHttpClient;
    private static OkHttpClient searchHttpClient;
    private static OkHttpClient ttrssHttpClient;
    private static OkHttpClient feverTinyRSSHttpClient;
    private static OkHttpClient feverHttpClient;
    private static OkHttpClient inoreaderHttpClient;
    private static OkHttpClient feedlyHttpClient;
    private static OkHttpClient imageHttpClient;

    private HttpClientManager() {
    }

    public static HttpClientManager i() {
        if (instance == null) {
            synchronized (HttpClientManager.class) {
                if (instance == null) {
                    instance = new HttpClientManager();
                    instance.init();
                }
            }
        }
        return instance;
    }

    public void init(){
        OkHttpClient.Builder searchBuilder;
        OkHttpClient.Builder simpleBuilder;
        OkHttpClient.Builder smallBuilder;
        OkHttpClient.Builder inoreaderBuilder;
        OkHttpClient.Builder feedlyBuilder;
        OkHttpClient.Builder feverBuilder;
        OkHttpClient.Builder tinyBuilder;
        OkHttpClient.Builder tinyFeverBuilder;
        OkHttpClient.Builder imageBuilder;

        searchBuilder = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
                .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                .retryOnConnectionFailure(false)
                .followRedirects(true)
                .followSslRedirects(true)
                .addInterceptor(new RelyInterceptor())
                .addInterceptor(new RefererInterceptor());
        simpleBuilder = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                .retryOnConnectionFailure(false)
                .followRedirects(true)
                .followSslRedirects(true)
                .addInterceptor(new RelyInterceptor())
                .addInterceptor(new RefererInterceptor());
        smallBuilder = new OkHttpClient.Builder()
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .callTimeout(30, TimeUnit.SECONDS)
                .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                // 用于模拟弱网的拦截器
                // .addNetworkInterceptor(new DoraemonWeakNetworkInterceptor())
                // 网络请求监控的拦截器
                // .addInterceptor(new DoraemonInterceptor())
                .retryOnConnectionFailure(false)
                .followRedirects(true)
                .followSslRedirects(true);
        tinyBuilder = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                .followRedirects(true)
                .followSslRedirects(true)
                .addInterceptor(new TinyRSSTokenInterceptor());
        tinyFeverBuilder = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                .followRedirects(true)
                .followSslRedirects(true)
                .addInterceptor(new FeverTinyRSSTokenInterceptor());
        feverBuilder = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                .followRedirects(true)
                .followSslRedirects(true);
        inoreaderBuilder = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                .followRedirects(true)
                .followSslRedirects(true)
                // .addInterceptor(new AuthorizationInterceptor())
                // .addInterceptor(new InoReaderHeaderInterceptor())
                .addInterceptor(new InoReaderTokenInterceptor())
                .authenticator(new TokenAuthenticator());
        feedlyBuilder = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                .followRedirects(true)
                .followSslRedirects(true)
                // .addInterceptor(new AuthorizationInterceptor())
                .addInterceptor(new LoggerInterceptor())
                .authenticator(new TokenAuthenticator());
        imageBuilder = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .sslSocketFactory(HttpsUtils.getSslSocketFactory().sSLSocketFactory, HttpsUtils.getSslSocketFactory().trustManager)
                .hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier)
                .followRedirects(true)
                .followSslRedirects(true)
                .addInterceptor(new RelyInterceptor());
        if(App.i().proxyNodeSocks5 == null){
            smallOkHttpClient = smallBuilder.build();
            simpleOkHttpClient = simpleBuilder.build();
            searchHttpClient = searchBuilder.build();
            ttrssHttpClient = tinyBuilder.build();
            feverTinyRSSHttpClient = tinyFeverBuilder.build();
            feverHttpClient = feverBuilder.build();
            feedlyHttpClient = feedlyBuilder.build();
            inoreaderHttpClient = inoreaderBuilder.build();
            imageHttpClient = imageBuilder.build();
        }else {
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(App.i().proxyNodeSocks5.getServer(), App.i().proxyNodeSocks5.getPort()));
            Authenticator authenticator = null;
            if( !TextUtils.isEmpty(App.i().proxyNodeSocks5.getUsername()) && !TextUtils.isEmpty(App.i().proxyNodeSocks5.getPassword())){
                authenticator = new Authenticator() {
                    @Nullable
                    @Override
                    public Request authenticate(@Nullable Route route, @NotNull Response response) {
                        XLog.w("代理鉴权失败");
                        if (response.request().header("Proxy-Authorization") != null) {
                            // Give up, we've already failed to authenticate.
                            return null;
                        }

                        String credential = Credentials.basic(App.i().proxyNodeSocks5.getUsername(), App.i().proxyNodeSocks5.getPassword());
                        return response.request().newBuilder()
                                .header("Proxy-Authorization", credential)
                                .build();
                    }
                };
                smallOkHttpClient = smallBuilder.proxy(proxy).proxyAuthenticator(authenticator).build();
                simpleOkHttpClient = simpleBuilder.proxy(proxy).proxyAuthenticator(authenticator).build();
                searchHttpClient = searchBuilder.proxy(proxy).proxyAuthenticator(authenticator).build();
                ttrssHttpClient = tinyBuilder.proxy(proxy).proxyAuthenticator(authenticator).build();
                feverTinyRSSHttpClient = tinyFeverBuilder.proxy(proxy).proxyAuthenticator(authenticator).build();
                feverHttpClient = feverBuilder.proxy(proxy).proxyAuthenticator(authenticator).build();
                feedlyHttpClient = feedlyBuilder.proxy(proxy).proxyAuthenticator(authenticator).build();
                inoreaderHttpClient = inoreaderBuilder.proxy(proxy).proxyAuthenticator(authenticator).build();
                imageHttpClient = imageBuilder.proxy(proxy).proxyAuthenticator(authenticator).build();
            }else {
                smallOkHttpClient = smallBuilder.proxy(proxy).build();
                simpleOkHttpClient = simpleBuilder.proxy(proxy).build();
                searchHttpClient = searchBuilder.proxy(proxy).build();
                ttrssHttpClient = tinyBuilder.proxy(proxy).build();
                feverTinyRSSHttpClient = tinyFeverBuilder.proxy(proxy).build();
                feverHttpClient = feverBuilder.proxy(proxy).build();
                feedlyHttpClient = feedlyBuilder.proxy(proxy).build();
                inoreaderHttpClient = inoreaderBuilder.proxy(proxy).build();
                imageHttpClient = imageBuilder.proxy(proxy).build();
            }
        }
        imageHttpClient.dispatcher().setMaxRequests(4);
        // simpleOkHttpClient.dispatcher().setMaxRequests(5);
        searchHttpClient.dispatcher().setMaxRequests(5);
        smallOkHttpClient.dispatcher().setMaxRequests(5);
    }

    public OkHttpClient smallClient() {
        return smallOkHttpClient;
    }

    public OkHttpClient simpleClient() {
        return simpleOkHttpClient;
    }

    public OkHttpClient searchClient() {
        return searchHttpClient;
    }


    public OkHttpClient ttrssHttpClient() {return ttrssHttpClient;}

    public OkHttpClient feverTinyRSSHttpClient() {return feverTinyRSSHttpClient;}

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

    // public OkHttpClient glideHttpClient() {
    //     return glideHttpClient;
    // }
}
