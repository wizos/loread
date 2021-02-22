package me.wizos.loread.network;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.elvishew.xlog.XLog;
import com.jeremyliao.liveeventbus.LiveEventBus;

import me.wizos.loread.App;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.User;
import me.wizos.loread.network.api.FeverTinyRSSApi;
import me.wizos.loread.utils.NetworkUtils;

public class SyncWorker extends Worker  {
    public final static String TAG = "SyncWorker";
    public final static String SYNC_TASK_STATUS = "SyncStatus";
    public final static String SYNC_TASK_START = "SyncStart";
    public final static String SYNC_PROCESS_FOR_SUBTITLE = "SyncProcess";
    public final static String NEW_ARTICLE_NUMBER = "NewArticleNumber";

    public final static String IS_AUTO_SYNC = "auto_sync";

    // public final static boolean isAutoSync = false;
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

    }

    @NonNull
    @Override
    public Result doWork() {
        XLog.i("同步任务：" + this);
        //接收外面传递进来的数据
        boolean autoSync = getInputData().getBoolean(IS_AUTO_SYNC, false);

        if(App.i().isSyncing){
            XLog.i("------> 正在同步");
            return Result.failure();
        }

        User user = App.i().getUser();
        if(user == null){
            XLog.i("------> 用户为null");
            return Result.failure();
        }

        if(autoSync && !user.isAutoSync()){
            XLog.i("------> 自动同步未开启");
            return Result.failure();
        }

        if(autoSync && user.isAutoSyncOnlyWifi() && !NetworkUtils.isWiFiUsed()){
            XLog.i("------> 自动同步开启，但非wifi环境");
            return Result.failure();
        }

        XLog.i("最近一次需要同步的时间：" + (user.getAutoSyncFrequency() * 60_000 + user.getLastSyncTime()) + ", 当前时间：" + System.currentTimeMillis());
        if(autoSync && user.getAutoSyncFrequency() * 60_000 + user.getLastSyncTime() > System.currentTimeMillis()){
            XLog.i("------> 自动同步开启，但时间间隔不满足条件");
            return Result.failure();
        }

        App.i().isSyncing = true;
        LiveEventBus.get(SyncWorker.SYNC_TASK_STATUS).post(true);
        if(App.i().getApi() instanceof FeverTinyRSSApi){
            ((FeverTinyRSSApi) App.i().getApi()).setWorker(this);
        }
        App.i().getApi().sync();
        App.i().isSyncing = false;
        LiveEventBus.get(SyncWorker.SYNC_TASK_STATUS).post(false);

        user.setLastSyncTime(System.currentTimeMillis());
        CoreDB.i().userDao().update(App.i().getUser());
        XLog.i("同步成功结束");
        return Result.success();
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }
}
