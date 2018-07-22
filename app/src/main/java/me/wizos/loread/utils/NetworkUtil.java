package me.wizos.loread.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import me.wizos.loread.App;
import me.wizos.loread.data.WithPref;


/**
 * @author Wizos on 2018-06-09
 * @version 1.0
 */
public class NetworkUtil {
    public static final int NETWORK_NONE = 0;
    public static final int NETWORK_MOBILE = 1;
    public static final int NETWORK_WIFI = 2;
    public static int THE_NETWORK = 0;

    /**
     * 判断网络的状态
     */
    public static int getNetWorkState() {
        ConnectivityManager connectivityManager = (ConnectivityManager) App.i().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                return NETWORK_WIFI;
            } else if (networkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                return NETWORK_MOBILE;
            }
        }
        return NETWORK_NONE;
    }

    public static boolean isNetworkAvailable() {
        return THE_NETWORK != NETWORK_NONE;
    }

    public static boolean isWiFiUsed() {
        return THE_NETWORK == NETWORK_WIFI;
    }


    public static boolean canDownImg() {
        // 网络不可用
        if (!isNetworkAvailable()) {
            return false;
        }
        // 开启了仅Wifi情况下载，但是不处于wifi状态
        return !(WithPref.i().isDownImgOnlyWifi() && THE_NETWORK != NETWORK_WIFI);
    }

//    public static boolean canDownImg1() {
//        if (!WithPref.i().isDownImgOnlyWifi() && !NetworkUtil.isNetworkAvailable()) {
//            return false;
//        }
//        return !(WithPref.i().isDownImgOnlyWifi() && !NetworkUtil.isWiFiUsed());
//    }


//    /**
//     * 老版本
//     * 判断WIFI是否可用
//     */
//    public static boolean isWiFiUsed2() {
//        ConnectivityManager connectivity = (ConnectivityManager) App.i().getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (connectivity == null) {
//            return false;
//        }
//        if (Build.VERSION.SDK_INT >= 23) {
//            KLog.d("Wifi", ">=23");
//            Network[] networks = connectivity.getAllNetworks();
//            if (networks == null) {
//                return false;
//            }
//            NetworkInfo networkInfo;
//            for (Network network : networks) {
//                networkInfo = connectivity.getNetworkInfo(network);
////                return wifiNetworkIsAvailable( networkInfo );
//                if (networkInfo.getType() != ConnectivityManager.TYPE_WIFI) continue;
//                KLog.e("Wifi==", networkInfo.isConnected());
//                return networkInfo.isConnected();
//            }
//        } else {
//            KLog.d("Wifi", "<23");
//            NetworkInfo[] networkInfos = connectivity.getAllNetworkInfo();
//            if (networkInfos == null) {
//                return false;
//            }
//            for (NetworkInfo networkInfo : networkInfos) {
//                //此处请务必使用NetworkInfo对象下的isAvailable（）方法，isConnected()是检测当前是否连接到了wifi
//                if (networkInfo.getType() != ConnectivityManager.TYPE_WIFI) {
//                    continue;
//                }
//                KLog.i("Wifi==", networkInfo.isConnected());
//                return networkInfo.isConnected();
//            }
//        }
//        return true;
//    }


}