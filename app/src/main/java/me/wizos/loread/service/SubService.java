package me.wizos.loread.service;

import android.app.IntentService;
import android.content.Intent;

/**
 * 这个是为了能“为 WebActivity 提前生成一个进程”
 *
 * @author Wizos on 2018/7/24.
 */

public class SubService extends IntentService {
    private static final String TAG = "SubService";

    public SubService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        EventBus.getDefault().unregister(this);
    }

    //    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onReceiveesult(Sync sync) {
//    }
    @Override
    protected void onHandleIntent(Intent intent) {
    }
}
