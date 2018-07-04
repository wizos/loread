package me.wizos.loread.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import me.wizos.loread.App;
import me.wizos.loread.utils.NetworkUtil;

/**
 * @author Wizos on 2018/6/5.
 */

public class NetworkStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            App.networkStatus = NetworkUtil.getNetWorkState();
        }
    }
}
