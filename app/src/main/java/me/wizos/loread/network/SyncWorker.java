package me.wizos.loread.network;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.jeremyliao.liveeventbus.LiveEventBus;

import me.wizos.loread.App;

public class SyncWorker extends Worker  {
    public final static String TAG = "SyncWorker";
    public final static String SYNC_TASK_STATUS = "SyncStatus";
    public final static String SYNC_PROCESS_FOR_SUBTITLE = "SyncProcess";
    public final static String NEW_ARTICLE_NUMBER = "NewArticleNumber";
    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    private boolean running = false;

    @NonNull
    @Override
    public Result doWork() {
        //if(!App.i().getUser().isAutoSync() || (App.i().getUser().isAutoSyncOnlyWifi() && !NetworkUtil.isWiFiUsed()) ){
        //    return Result.success();
        //}
        if(running){
            return Result.failure();
        }
        running = true;
        LiveEventBus.get(SyncWorker.SYNC_TASK_STATUS).post(true);
        App.i().getApi().sync();
        LiveEventBus.get(SyncWorker.SYNC_TASK_STATUS).post(false);
        running = false;
        return Result.success();
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }
}
