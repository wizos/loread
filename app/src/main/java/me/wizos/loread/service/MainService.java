//package me.wizos.loreadx.service;
//
//import android.content.Context;
//import android.content.Intent;
//import android.os.AsyncTask;
//
//import androidx.annotation.NonNull;
//import androidx.core.app.JobIntentService;
//
//import com.carlt.networklibs.utils.NetworkUtils;
//import com.socks.library.KLog;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import me.wizos.loreadx.App;
//import me.wizos.loreadx.db.Article;
//import me.wizos.loreadx.db.CoreDB;
//import me.wizos.loreadx.utils.EncryptUtil;
//import me.wizos.loreadx.utils.FileUtil;
//
///**
// * 如果启动 IntentService 多次，那么每一个耗时操作会以工作队列的方式在 IntentService 的 onHandleIntent 回调方法中依次去执行，执行完自动结束。
// * 这样来避免事务处理阻塞主线程。执行完所一个Intent请求对象所对应的工作之后，如果没有新的Intent请求达到，则自动停止Service；
// * 否则执行下一个Intent请求所对应的任务。
// * Created by Wizos on 2019/3/30.
// */
//
//public class MainService extends JobIntentService {
//    public MainService() {
//        super();
//    }
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
////        EventBus.getDefault().register(this);
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
////        EventBus.getDefault().unregister(this);
//    }
//
//    /**
//     * 将工作加入此服务的快捷方法。
//     */
//    public static void enqueueWork(Context context, Intent work) {
//        //KLog.e("获取到新的任务：enqueueWork" );
//        enqueueWork(context, MainService.class, 100, work);
//    }
//
////    @Subscribe(threadMode = ThreadMode.MAIN)
////    public void onReceiveesult(Sync sync) {
////        int result = sync.result;
//////        KLog.e("接收到的数据为：" + status);
////        switch (result) {
////            case Sync.STOP:
////                stopSelf();
////                break;
////            default:
////                break;
////        }
////    }
//
//    @Override
//    protected void onHandleWork(@NonNull Intent intent) {
//        KLog.e("获取到新的任务，线程：" + Thread.currentThread() );
//        if (!NetworkUtils.isAvailable()) { //App.i().isSyncing ||
//            return;
//        }
//
//        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
//            @Override
//            public void run() {
//                String action = intent.getAction();
//                //KLog.e("获取到新的任务：" + action);
//                if (App.SYNC_ALL.equals(action) || App.SYNC_HEARTBEAT.equals(action)) {
//                    handleExpiredArticles();
//                    App.i().getApi().sync();
//                }
//            }
//        });
//    }
//
//    private void handleExpiredArticles() {
//        // EventBus.getDefault().post(new Sync(Sync.DOING, getString(R.string.clear_article)));
//
//        // 最后的 300 * 1000L 是留前5分钟时间的不删除 WithPref.i().getClearBeforeDay()
//        long time = System.currentTimeMillis() - App.i().getUser().getCachePeriod() * 24 * 3600 * 1000L - 300 * 1000L;
//        List<Article> boxReadArts = CoreDB.i().articleDao().getReadedUnstarBeFiledLtTime(App.i().getUser().getId(), time);
//        KLog.i("移动文章" + boxReadArts.size());
//
//        for (Article article : boxReadArts) {
//            article.setSaveStatus(App.STATUS_IS_FILED);
//            FileUtil.saveArticle(App.i().getUserBoxPath(), article);
//        }
//        CoreDB.i().articleDao().update(boxReadArts);
//
//        List<Article> storeReadArts = CoreDB.i().articleDao().getReadedStaredBeFiledLtTime(App.i().getUser().getId(), time);
//        KLog.i("移动文章" + storeReadArts.size());
//        for (Article article : storeReadArts) {
//            article.setSaveStatus(App.STATUS_IS_FILED);
//            FileUtil.saveArticle(App.i().getUserStorePath(), article);
//        }
//        CoreDB.i().articleDao().update(storeReadArts);
//
//        List<Article> expiredArticles = CoreDB.i().articleDao().getReadedUnstarLtTime(App.i().getUser().getId(), time);
//        ArrayList<String> idListMD5 = new ArrayList<>(expiredArticles.size());
//        for (Article article : expiredArticles) {
//            idListMD5.add(EncryptUtil.MD5(article.getId()));
//        }
//        KLog.i("清除A：" + time + "--" + expiredArticles.size());
//        FileUtil.deleteHtmlDirList(idListMD5);
//        CoreDB.i().articleDao().delete(expiredArticles);
//    }
//}
