package me.wizos.loread.utils;

import android.webkit.WebSettings;

import java.io.IOException;
import java.util.Objects;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.network.HttpClientManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Wizos on 2019/5/12.
 */

public class HttpCall {
    private HttpCall() {}
    private static HttpCall instance;
    public static HttpCall i() {
        if (instance == null) {
            synchronized (HttpCall.class) {
                if (instance == null) {
                    instance = new HttpCall();
                }
            }
        }
        return instance;
    }


    public void get(String url, Callback callback){
        // Request request = new Request.Builder().url(feedUrl).build();
        Request.Builder request = new Request.Builder().url(url);
        request.header(Contract.USER_AGENT, WebSettings.getDefaultUserAgent(App.i()));
        Call call = HttpClientManager.i().searchClient().newCall(request.build());
        call.enqueue(callback);
    }
    public String get(String url){
        try {
            return Objects.requireNonNull(HttpClientManager.i().smallClient().newCall(new Request.Builder().url(url).get().build()).execute().body()).string();
        }catch (IOException | IllegalArgumentException | NullPointerException e){
            return null;
        }
    }
    public String get(String url, Headers headers){
        try {
            return Objects.requireNonNull(HttpClientManager.i().smallClient().newCall(new Request.Builder().url(url).get().headers(headers).build()).execute().body()).string();
        }catch (IOException | IllegalArgumentException | NullPointerException e){
            return null;
        }
    }
    public String post(String url, RequestBody requestBody){
        try {
            return Objects.requireNonNull(HttpClientManager.i().smallClient().newCall(new Request.Builder().url(url).post(requestBody).build()).execute().body()).string();
        }catch (IOException | IllegalArgumentException | NullPointerException e){
            return null;
        }
    }
    public String post(String url, RequestBody requestBody, Headers headers){
        try {
            return Objects.requireNonNull(HttpClientManager.i().smallClient().newCall(new Request.Builder().url(url).post(requestBody).headers(headers).build()).execute().body()).string();
        }catch (IOException | IllegalArgumentException | NullPointerException e){
            return null;
        }
    }
    public boolean valid(String url){
        Request.Builder request = new Request.Builder().url(url);
        request.header(Contract.USER_AGENT, WebSettings.getDefaultUserAgent(App.i()));
        try {
            Response response = HttpClientManager.i().smallClient().newCall(request.head().build()).execute();
            boolean successful = response.isSuccessful();
            response.close();
            return successful;
        }catch (IOException | IllegalArgumentException | NullPointerException e){
            return false;
        }
    }
    public boolean valid(String url, Headers headers){
        Request.Builder request = new Request.Builder().url(url);
        request.header(Contract.USER_AGENT, WebSettings.getDefaultUserAgent(App.i()));
        try {
            Response response = HttpClientManager.i().smallClient().newCall(request.headers(headers).head().build()).execute();
            boolean successful = response.isSuccessful();
            response.close();
            return successful;
        }catch (IOException | IllegalArgumentException | NullPointerException e){
            return false;
        }
    }
}
