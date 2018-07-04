package me.wizos.loread.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import me.wizos.loread.App;
import me.wizos.loread.data.WithPref;
import me.wizos.loread.service.NetworkStatus;


/**
 * @author Wizos on 2018-06-09
 * @version 1.0
 */
public class NetworkUtil {
    /**
     * 判断网络的状态
     */
    public static NetworkStatus getNetWorkState() {
        ConnectivityManager connectivityManager = (ConnectivityManager) App.i().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                return NetworkStatus.NETWORK_WIFI;
            } else if (networkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                return NetworkStatus.NETWORK_MOBILE;
            }
        }
        return NetworkStatus.NETWORK_NONE;
    }

    public static boolean isNetworkAvailable() {
        return App.networkStatus != NetworkStatus.NETWORK_NONE;
    }

    public static boolean isWiFiUsed() {
        return App.networkStatus == NetworkStatus.NETWORK_WIFI;
    }


    public static boolean canDownImg() {
        // 网络不可用
        if (!isNetworkAvailable()) {
            return false;
        }
        // 开启了仅Wifi情况下载，但是不处于wifi状态
        return !(WithPref.i().isDownImgOnlyWifi() && App.networkStatus != NetworkStatus.NETWORK_WIFI);
    }

    public static boolean canDownImg1() {
        if (!WithPref.i().isDownImgOnlyWifi() && !NetworkUtil.isNetworkAvailable()) {
            return false;
        }
        return !(WithPref.i().isDownImgOnlyWifi() && !NetworkUtil.isWiFiUsed());
    }


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