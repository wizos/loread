package me.wizos.loread.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

import com.socks.library.KLog;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import me.wizos.loread.App;
import me.wizos.loread.data.WithSet;

//import okhttp3.Callback;
//import okhttp3.Request;
//import okhttp3.Response;

/**
 * Created by mummyding on 15-11-22.<br>
 * Utils for Post Data via network and get network state of cell phone.
 * @author MummyDing
 * @version Leisure 1.0
 */
public class HttpUtil {
    private static final OkHttpClient mOkHttpClient = new OkHttpClient();
    static{
        mOkHttpClient.setConnectTimeout(60, TimeUnit.SECONDS); // 初始为30
        mOkHttpClient.setReadTimeout(120, TimeUnit.SECONDS);// 初始为30
        mOkHttpClient.setWriteTimeout(120, TimeUnit.SECONDS);// 初始为30
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

//    /**
//     * 判断 wifi 有没有打开
//     */
//    public static boolean isWifiEnabled() {
//        if (!isNetworkAvailable()) {
//            return false;
//        }
//        return ((WifiManager) App.getInstance().getSystemService(Context.WIFI_SERVICE)).isWifiEnabled();
//    }

    public static boolean canDownImg() {
        if (!isNetworkAvailable()) {
            return false;
        }
        if (WithSet.getInstance().isDownImgWifi() && isWiFiActive()) {
            KLog.d("只使用wifi下图片");
            return true;
        }
        return false;
    }

    /**
     * 判断网络是否可用
     */
    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivity = (ConnectivityManager) App.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivity.getActiveNetworkInfo();
        return (info != null && info.isConnected());
    }

    /**
     * 判断WIFI是否可用
     */
    public static boolean isWiFiActive() {
        ConnectivityManager connectivity = (ConnectivityManager) App.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            KLog.d("Wifi", ">=23");
            Network[] networks = connectivity.getAllNetworks();
            if (networks == null) {
                return false;
            }
            NetworkInfo networkInfo;
            for (Network network : networks) {
                networkInfo = connectivity.getNetworkInfo(network);
//                return wifiNetworkIsAvailable( networkInfo );
                if (networkInfo.getType() != ConnectivityManager.TYPE_WIFI) continue;
                KLog.e("Wifi==", networkInfo.isConnected());
                return networkInfo.isConnected();
            }
        } else {
            KLog.d("Wifi", "<23");
            NetworkInfo[] networkInfos = connectivity.getAllNetworkInfo();
            if (networkInfos == null) {
                return false;
            }
            for (NetworkInfo networkInfo : networkInfos) {
                //此处请务必使用NetworkInfo对象下的isAvailable（）方法，isConnected()是检测当前是否连接到了wifi
                if (networkInfo.getType() != ConnectivityManager.TYPE_WIFI) continue;
                KLog.e("Wifi==", networkInfo.isConnected());
                return networkInfo.isConnected();
            }
        }
        return true;
    }

}