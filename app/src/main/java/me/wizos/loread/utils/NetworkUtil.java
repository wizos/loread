package me.wizos.loread.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import me.wizos.loread.App;


/**
 * @author Wizos on 2018-06-09
 * @version 1.0
 */
public class NetworkUtil {
    public static final int NETWORK_NONE = 0;
    public static final int NETWORK_MOBILE = 1;
    public static final int NETWORK_WIFI = 2;
    private static int THE_NETWORK = 0;

    /**
     * 判断网络的状态
     */
    public static int getNetWorkState() {
        ConnectivityManager connectivityManager = (ConnectivityManager) App.i().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                THE_NETWORK = NETWORK_WIFI;
            } else if (networkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                THE_NETWORK = NETWORK_MOBILE;
            }
        } else {
            THE_NETWORK = NETWORK_NONE;
        }
        return THE_NETWORK;
    }

    public static boolean isNetworkAvailable() {
        return THE_NETWORK != NETWORK_NONE;
    }

    public static boolean isWiFiUsed() {
        return THE_NETWORK == NETWORK_WIFI;
    }

    public static void setTheNetwork(int network){
        THE_NETWORK = network;
    }

    public static boolean canDownImg() {
        if (!isNetworkAvailable()) {
            return false;
        }
        // 开启了仅Wifi情况下载，但是不处于wifi状态
        return !(App.i().getUser().isDownloadImgOnlyWifi() && !isWiFiUsed());
    }

}