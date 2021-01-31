package me.wizos.loread.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.elvishew.xlog.XLog;

import me.wizos.loread.utils.NetworkUtils;


/**
 * Android 7.0 移除了三项隐式广播，因为隐式广播会在后台频繁启动已注册侦听这些广播的应用。删除这些广播可以显著提升设备性能和用户体验。
 * Android 7.0 以上不会收到 CONNECTIVITY_ACTION 广播，即使它们已有清单条目来请求接受这些事件的通知。
 * 在前台运行的应用如果使用 BroadcastReceiver 请求接收通知，则仍可以在主线程中侦听 CONNECTIVITY_CHANGE。
 * <p>
 * Android 7.0 为了后台优化，推荐使用 JobScheduler 代替 BroadcastReceiver 来监听网络变化。
 *
 * @author Wizos on 2018/6/5.
 */

public class NetworkStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        XLog.e("接收到网络变化" + intent.getAction() + " . " + NetworkUtils.getNetWorkState());
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            NetworkUtils.getNetWorkState();
        }
    }
}
