
package me.wizos.loread.utils;

import com.facebook.stetho.okhttp.StethoInterceptor;
import com.socks.library.KLog;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by mummyding on 15-11-22.<br>
 * Utils for Post Data via network and get network state of cell phone.
 * @author MummyDing
 * @version Leisure 1.0
 */
public class HttpUtil {
    private static final OkHttpClient mOkHttpClient = new OkHttpClient();
    static{
        mOkHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
        mOkHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
        mOkHttpClient.setWriteTimeout(30, TimeUnit.SECONDS);
    }
    /**
     * 该不会开启异步线程。
     * @param request
     * @return
     * @throws IOException
     */
    public static Response execute(Request request) throws IOException{
        return mOkHttpClient.newCall(request).execute();
    }
    /**
     * 开启异步线程访问网络
     * @param request
     * @param responseCallback
     */
    public static void enqueue(Request request, Callback responseCallback){
//        mOkHttpClient.networkInterceptors().add(new StethoInterceptor());
//        KLog.d("【】【】【】【】");
        mOkHttpClient.newCall(request).enqueue(responseCallback);
    }

    public static void xx(){
        mOkHttpClient.networkInterceptors().add(new StethoInterceptor());
        KLog.d("【1】【2】【1】【2】");
    }

    /**
     * 开启异步线程访问网络, 且不在意返回结果（实现空callback）
     * @param request
     */
    public static void enqueue(Request request){
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Response arg0) throws IOException {
            }
            @Override
            public void onFailure(Request arg0, IOException arg1) {

            }
        });
    }






}
