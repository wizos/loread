package me.wizos.loread.utils;

import java.io.IOException;
import java.util.Objects;

import me.wizos.loread.network.HttpClientManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;

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


    public static void get(String feedUrl, Callback callback){
        Request request = new Request.Builder().url(feedUrl).build();
        Call call = HttpClientManager.i().searchClient().newCall(request);
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
        try {
            return HttpClientManager.i().smallClient().newCall(new Request.Builder().url(url).head().build()).execute().isSuccessful();
        }catch (IOException | IllegalArgumentException | NullPointerException e){
            return false;
        }
    }
    public boolean valid(String url, Headers headers){
        try {
            return HttpClientManager.i().smallClient().newCall(new Request.Builder().url(url).get().headers(headers).build()).execute().isSuccessful();
        }catch (IOException | IllegalArgumentException | NullPointerException e){
            return false;
        }
    }
}
