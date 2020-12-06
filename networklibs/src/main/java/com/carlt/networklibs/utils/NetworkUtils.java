package com.carlt.networklibs.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.carlt.networklibs.NetType;
import com.carlt.networklibs.NetworkManager;


/**
 * Description:
 * Company    : carlt
 * Author     : zhanglei
 * Date       : 2019/2/26 16:09
 */
public class NetworkUtils {
    /**
     * 网络是否可用
     * @return
     */
    @SuppressWarnings("MissingPermission")
    public static boolean isAvailable() {
        ConnectivityManager connmagr = (ConnectivityManager) NetworkManager.getInstance().getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connmagr == null) {
            return false;
        }
        NetworkInfo[] allNetworkInfo = connmagr.getAllNetworkInfo();
        if (allNetworkInfo != null) {
            for (NetworkInfo networkInfo : allNetworkInfo) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }


        return false;
    }

    /**
     * 获取网络类型
     * @return
     */
    public static NetType getNetType() {
        ConnectivityManager connmagr = (ConnectivityManager) NetworkManager.getInstance().getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connmagr == null) {
            return NetType.NONE;
        }
        NetworkInfo activeNetworkInfo = connmagr.getActiveNetworkInfo();
        if (activeNetworkInfo != null) {
            int type = activeNetworkInfo.getType();
            if (type == ConnectivityManager.TYPE_MOBILE) {
                String extraInfo = activeNetworkInfo.getExtraInfo();
                if (extraInfo != null && !extraInfo.isEmpty()) {
                    if (extraInfo.equalsIgnoreCase("cmnet")) {
                        return NetType.CMNET;
                    } else {
                        return NetType.CMWAP;
                    }
                }
            } else if (type == ConnectivityManager.TYPE_WIFI) {
                return NetType.WIFI;

            }
        }
        return NetType.NONE;
    }

    public static void openNetSetting(Activity context, int code) {
        Intent intent = new Intent("/");
        ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.WirelessSettings");
        intent.setComponent(componentName);
        context.startActivityForResult(intent, code);
    }
}
