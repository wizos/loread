package me.wizos.loread.activity;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.ArrayMap;

import com.socks.library.KLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.Article;
import me.wizos.loread.bean.Img;
import me.wizos.loread.bean.RequestLog;
import me.wizos.loread.bean.gson.Sub;
import me.wizos.loread.bean.gson.son.ItemRefs;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.net.API;
import me.wizos.loread.net.Neter;
import me.wizos.loread.net.Parser;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.utils.ToastUtil;

/**
 * Created by Wizos on 2017/1/7.
 */

public class MainService extends IntentService {
    private String TAG = "MainService";

    public MainService() {
        super("MainService");
    }

    private Handler mHandler; // MainHandler
    private Handler sHandler; // ChildHandler
//    private Handler mUIHandler = new Handler(Looper.getMainLooper());


    @Override
    public void onCreate() {
        super.onCreate();
        sHandler = new Handler(Looper.myLooper(), new ChildCallback());
        mNeter = new Neter(sHandler);
        App.mNeter = mNeter;

        mNeter.setReord(recorder);
        requestMap = new ArrayMap<>();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        if (action.equals("syncAllStarred")) {  // 这个是在 SettingActivity 中实现的
            syncAllStarred();
        } else if (action.equals("unreadArticle")) {
            mNeter.markArticleUnread(intent.getExtras().getString("articleId"));
        } else if (action.equals("readedArticle")) {
            mNeter.markArticleReaded(intent.getExtras().getString("articleId"));
        } else {
            refresh();
        }

        int result = intent.getFlags();
//        String info = intent.getStringExtra("res");
//        String url = intent.getStringExtra("url");
//        KLog.d("开始处理 = 内容：" + info);
//        KLog.d("开始处理 = url：" + url);
        KLog.e(" 开始处理 = 结果：" + result);
    }

    // 使用 startService(intent); 多次启动IntentService，但IntentService的实例只有一个，这跟传统的Service是一样的，最终IntentService会去调用onHandleIntent执行异步任务。这里可能我们还会担心for循环去启动任务，而实例又只有一个，那么任务会不会被覆盖掉呢？其实是不会的，因为IntentService真正执行异步任务的是HandlerThread+Handler
    public ServiceBinder mBinder = new ServiceBinder(); /* 数据通信的桥梁 */

    /* 第一种模式通信：Binder */
    public class ServiceBinder extends Binder {
        MainService getService() {
            return MainService.this;
        }
    }

    public Neter getNeter() {
        return mNeter;
    }

    public void regHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public void refresh() {
        sHandler.sendEmptyMessage(API.M_BEGIN_SYNC);
    }

    public void syncAllStarred() {
        sHandler.sendEmptyMessage(API.S_STREAM_CONTENTS_STARRED);
    }


    /* 重写Binder的onBind函数，返回派生类 */
    @Override
    public IBinder onBind(Intent arg0) {
        super.onBind(arg0);
        return mBinder;
    }


    private int urlState = 0;
    private int readySyncArtsCapacity; // 需要同步文章内容的数量
    private int alreadySyncedArtsNum = 0; // 同步了文章内容的数量
    private int numOfFailure = 0;
    //    private int unreadRefsSize, starredRefsSize;
    private ArrayList<ItemRefs> afterItemRefs = new ArrayList<>();
    private Neter mNeter;

    /**
     * 异步类: AsyncTask 和 Handler 对比
     * 1 ） AsyncTask实现的原理,和适用的优缺点
     * AsyncTask,是android提供的轻量级的异步类,可以直接继承AsyncTask,在类中实现异步操作,并提供接口反馈当前异步执行的程度(可以通过接口实现UI进度更新),最后反馈执行的结果给UI主线程.
     * 使用的优点: 1、简单,快捷；2、过程可控
     * 使用的缺点：在使用多个异步操作和并需要进行Ui变更时,就变得复杂起来.
     * 2 ）Handler异步实现的原理和适用的优缺点
     * 在Handler 异步实现时,涉及到 Handler, Looper, Message,Thread 四个对象，实现异步的流程是主线程启动 Thread（子线程）运行并生成Message-Looper获取Message并传递给HandleràHandler逐个获取Looper中的Message，并进行UI变更。
     * 使用的优点：1、结构清晰，功能定义明确；2、对于多个后台任务时，简单，清晰
     * 使用的缺点：在单个后台异步处理时，显得代码过多，结构过于复杂（相对性）
     * ----------
     * 采用线程 + Handler实现异步处理时，当每次执行耗时操作都创建一条新线程进行处理，性能开销会比较大。另外，如果耗时操作执行的时间比较长，就有可能同时运行着许多线程，系统将不堪重负。
     * 为了提高性能，我们可以使用AsynTask实现异步处理，事实上其内部也是采用线程 + Handler来实现异步处理的，只不过是其内部使用了线程池技术，有效的降低了线程创建数量及限定了同时运行的线程数。
     * ----------
     * 如果你不带参数的实例化：Handler handler = new Handler();那么这个会默认用当前线程的looper
     * 要刷新UI，handler要用到主线程的looper。那么在主线程 Handler handler = new Handler();，如果在其他线程，也要满足这个功能的话，要Handler handler = new Handler(Looper.getMainLooper());不用刷新ui,只是处理消息。 当前线程如果是主线程的话，Handler handler = new Handler();不是主线程的话，Looper.prepare(); Handler handler = new Handler();Looper.loop();或者Handler handler = new Handler(Looper.getMainLooper());
     * 若是实例化的时候用Looper.getMainLooper()就表示放到主UI线程去处理。
     * 如果不是的话，因为只有UI线程默认Loop.prepare();Loop.loop();过，其他线程需要手动调用这两个，否则会报错。
     */


    private class ChildCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            String info = msg.getData().getString("res");
            String url = msg.getData().getString("url");
            KLog.d("开始处理 = url：" + url);
            KLog.d("开始处理 = 结果：" + msg.what);

            if (info == null) {
                info = "";
            }
            switch (msg.what) {
                case API.M_BEGIN_SYNC:
                    if (syncRequestLog()) {
                        break;
                    }
                    // 为了得到分组名，及排序
                    sendProcess(getResources().getString(R.string.main_toolbar_hint_sync_tag));
//                    mHandler.sendEmptyMessage( 0 ); // TODO: 2017/1/8
                    mNeter.getWithAuth(API.HOST + API.U_TAGS_LIST);
                    KLog.i("【开始同步分组信息：TAGS_LIST】");
                    break;
                case API.S_TAGS_LIST: // 分组列表
                    Parser.instance().parseTagList2(info);
                    if (WithSet.i().isOrderTagFeed()) {
                        sendProcess(getResources().getString(R.string.main_toolbar_hint_sync_tag_order));
                        mNeter.getWithAuth(API.HOST + API.U_STREAM_PREFS);// TEST:  测试获取feed // 有了这份数据才可以对 tagslist feedlist 进行排序，并储存下来
                    } else {
                        Parser.instance().orderTags();
                        sendProcess(getResources().getString(R.string.main_toolbar_hint_sync_unread_count));
                        mNeter.getWithAuth(API.HOST + API.U_SUSCRIPTION_LIST);
//                        mNeter.getWithAuth(API.HOST + API.U_UNREAD_COUNTS);// TEST:  测试获取feed
                    }
                    break;
                case API.S_SUBSCRIPTION_LIST: // 订阅列表，这个目前应该没有被用到过 mNeter.getWithAuth(API.HOST + API.U_SUSCRIPTION_LIST);
                    ArrayList<Sub> subs = Parser.instance().parseSubscriptionList(info);
//                    Parser.instance().updateArticles(subs);
                    mNeter.getWithAuth(API.HOST + API.U_UNREAD_COUNTS);
                    break;
                case API.S_STREAM_PREFS:
                    Parser.instance().parseStreamPrefList(info, App.mUserID);
                    sendProcess(getResources().getString(R.string.main_toolbar_hint_sync_unread_count));
                    mNeter.getWithAuth(API.HOST + API.U_SUSCRIPTION_LIST);
//                    mNeter.getWithAuth(API.HOST + API.U_UNREAD_COUNTS); // TEST:  测试获取feed
                    break;
                case API.S_UNREAD_COUNTS:
                    Parser.instance().parseUnreadCounts(info);
                    sendProcess(getResources().getString(R.string.main_toolbar_hint_sync_unread_refs));
                    mNeter.getUnReadRefs(App.mUserID);
                    urlState = 1;
                    KLog.d("【未读数】");
                    break;
                case API.S_ITEM_IDS:
                    if (urlState == 1) {
                        String continuation = Parser.instance().parseItemIDsUnread(info);
                        if (continuation != null) {
                            mNeter.addHeader("c", continuation);
                            mNeter.getUnReadRefs(App.mUserID);
                            KLog.i("【获取 ITEM_IDS 还可继续】" + continuation);
                        } else {
                            urlState = 2;
                            sendProcess(getResources().getString(R.string.main_toolbar_hint_sync_stared_refs));
                            mNeter.getStarredRefs(App.mUserID);
                        }
                    } else if (urlState == 2) {
                        String continuation = Parser.instance().parseItemIDsStarred(info);
                        if (continuation != null) {
                            mNeter.addHeader("c", continuation);
                            mNeter.getStarredRefs(App.mUserID);
                        } else {
                            ArrayList<ItemRefs> unreadRefs = Parser.instance().reUnreadRefs();
                            ArrayList<ItemRefs> starredRefs = Parser.instance().reStarredRefs();
                            readySyncArtsCapacity = Parser.instance().reRefs(unreadRefs, starredRefs);
                            if (readySyncArtsCapacity == -1) {
//                                ToastUtil.showShort("同步时数据出错，请重试");
                                sendProcess("同步时数据出错，请重试");
                                sHandler.sendEmptyMessage(API.F_NoMsg);
                                break;
                            }
                            afterItemRefs = new ArrayList<>(readySyncArtsCapacity);
                            sHandler.sendEmptyMessage(API.S_ITEM_CONTENTS);// 开始获取所有列表的内容
                            urlState = 1;
                            KLog.i("【BaseActivity 获取 reUnreadList】");
                        }
                    }
                    break;
                case API.S_ITEM_CONTENTS:
                    KLog.i("【Main 解析 ITEM_CONTENTS 】" + urlState);
                    if (urlState == 1) {
                        afterItemRefs = Parser.instance().reUnreadUnstarRefs;
                        Parser.instance().parseItemContentsUnreadUnstar(info);
                    } else if (urlState == 2) {
                        afterItemRefs = Parser.instance().reUnreadStarredRefs;
                        Parser.instance().parseItemContentsUnreadStarred(info);
                    } else if (urlState == 3) {
                        afterItemRefs = Parser.instance().reReadStarredRefs;
                        Parser.instance().parseItemContentsReadStarred(info);
                    }

                    sendProcess(getResources().getString(R.string.main_toolbar_hint_sync_article_content, alreadySyncedArtsNum, readySyncArtsCapacity));

                    ArrayList<ItemRefs> beforeItemRefs = new ArrayList<>(afterItemRefs);
                    int num = beforeItemRefs.size();
//                    KLog.i("【获取 ITEM_CONTENTS 1】" + urlState +" - "+ afterItemRefs.size() + "--" + num);
                    if (num != 0) {
//                        if (beforeItemRefs.size() == 0) {
//                            return false;}
                        if (num > 50) {
                            num = 50;
                        }
                        String value;
                        for (int i = 0; i < num; i++) { // 给即将获取 item 正文 的请求构造包含 item 地址 的头部
                            value = beforeItemRefs.get(i).getId();
                            mNeter.addBody("i", value);
                            afterItemRefs.remove(0);
//                            KLog.i("【获取 ITEM_CONTENTS 3】" + num + "--" + afterItemRefs.size());
                        }
                        alreadySyncedArtsNum = alreadySyncedArtsNum + num;
                        mNeter.postWithAuth(API.HOST + API.U_ITEM_CONTENTS);
                    } else {
                        if (urlState == 0) {
                            urlState = 1;
                        } else if (urlState == 1) {
                            urlState = 2;
                        } else if (urlState == 2) {
                            urlState = 3;
                        } else if (urlState == 3) {
                            urlState = 0;
                            sHandler.sendEmptyMessage(API.SUCCESS);
                            return false;
                        }
                        sHandler.sendEmptyMessage(API.S_ITEM_CONTENTS);
                    }
                    break;
                case API.S_STREAM_CONTENTS_STARRED:
                    String continuation = Parser.instance().parseStreamContentsStarred(info); // 可能为空""
                    KLog.i("【解析所有加星文章】" + urlState + "---" + continuation);
                    if (continuation != null) {
                        mNeter.addHeader("c", continuation);
                        sendProcess(getResources().getString(R.string.main_toolbar_hint_sync_all_stared_content));
                        mNeter.getStarredContents();
                        KLog.i("【获取 StarredContents 】");
                    } else {
                        WithSet.i().setHadSyncAllStarred(true);
                    }
                    break;
                case API.S_EDIT_TAG:
                    long logTime = msg.getData().getLong("logTime");
//                    KLog.d("==" + logTime + info );
                    delRequestLog(logTime);
                    if (!info.equals("OK")) {
                        // TEST:  
//                        mNeter.forData(url, API.request, logTime);
                        mNeter.exeRequest(API.request, logTime);
                        KLog.i("返回的不是 ok");
                    }
//                    if (!hadSyncLogRequest && requestMap.size() == 0) {
//                        sHandler.sendEmptyMessage(API.M_BEGIN_SYNC);
//                        hadSyncLogRequest = true;
//                    }
                    break;
                // 这个似乎没有用到，可以弃用
                case API.S_Contents:
                    Parser.instance().parseStreamContents(info);
                    ToastUtil.showShort("API.S_Contents 似乎没有用到");
                    break;
                // 这个似乎没有用到，可以弃用
                case 88:
                    Parser.instance().parseStreamContents(info);
                    ToastUtil.showShort("API.88 似乎没有用到");
                    break;
                case API.F_Request:
                case API.F_Response:
                    if (info.equals("Authorization Required")) {
                        ToastUtil.showShort("没有Authorization，请重新登录");
                        Intent loginIntent = new Intent(MainService.this, LoginActivity.class);
                        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(loginIntent);
                        break;
                    }
                    sHandler.sendEmptyMessage(API.F_NoMsg);
                    alreadySyncedArtsNum = 0;
                    break;
                case API.F_NoMsg:
                    sendFailure();
                    if (url != null && url.contains(API.U_ARTICLE_CONTENTS)) {
                        ToastUtil.showShort("修改该文章的状态失败");
                        saveRequestLog(msg.getData().getLong("logTime"));
                    }
                    alreadySyncedArtsNum = 0;
                    break;
                case API.SUCCESS: // 文章获取完成
                    sendSuccess();
                    moveArticles();
                    clearArticles(WithSet.i().getClearBeforeDay());
                    alreadySyncedArtsNum = 0;
                    break;
                case API.S_BITMAP:
                    int imgNo;
                    String articleID;
                    articleID = msg.getData().getString("articleID");
                    imgNo = msg.getData().getInt("imgNo");

                    Img imgMeta = WithDB.i().getImg(articleID, imgNo);
                    if (imgMeta == null) {
                        KLog.e("【】【】【】【】【】【】【】" + "未找到图片1");
                        break;
                    }
                    imgMeta.setDownState(API.ImgMeta_Downover);
                    WithDB.i().saveImg(imgMeta);

                    if (App.currentArticleID != null & App.currentArticleID.equals(articleID)) {
                        Bundle bundle = msg.getData();
                        bundle.putString("imgName", imgMeta.getName());
                        Message message = Message.obtain();
                        message.what = API.S_BITMAP;
                        message.setData(bundle);
                        App.artHandler.sendMessage(message);
                    }
                    break;
                case API.F_BITMAP:
                    articleID = msg.getData().getString("articleID");
//                    imgNo = msg.getData().getInt("imgNo");
//                    imgMeta = WithDB.i().getImg(articleID, imgNo);
                    if (App.currentArticleID != null & App.currentArticleID.equals(articleID)) {
                        Bundle bundle = msg.getData();
                        Message message = Message.obtain();
                        message.what = API.F_BITMAP;
                        message.setData(bundle);
                        App.artHandler.sendMessage(message);
                    }
                    break;
                    // Note:  图片下载失败的重试逻辑应该放到 neter 中。此处只管结果“下载失败”了，通知具体的 activity 去更换占位图。
                    // TODO: 2017/6/6
            }
            return false;
        }
    }

    public void clearArticles(final int days) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long clearTime = System.currentTimeMillis() - days * 24 * 3600 * 1000L;
                List<Article> allArtsBeforeTime = WithDB.i().getArtInReadedUnstarLtTime(clearTime);
                KLog.i("清除" + clearTime + "--" + allArtsBeforeTime.size() + "--" + days);
//                ToastUtil.showShort("清除 " + days + " 天前的 " + allArtsBeforeTime.size() + " 篇文章");

                if (allArtsBeforeTime.size() == 0) {
                    return;
                }
                ArrayList<String> idListMD5 = new ArrayList<>(allArtsBeforeTime.size());
                for (Article article : allArtsBeforeTime) {
                    idListMD5.add(StringUtil.stringToMD5(article.getId()));
                }
                KLog.i("清除b" + clearTime + "--" + allArtsBeforeTime.size() + "--" + days);
                FileUtil.deleteHtmlDirList(idListMD5);
                WithDB.i().delArt(allArtsBeforeTime);
                WithDB.i().delArticleImgs(allArtsBeforeTime);
            }
        }).start();
    }

    /**
     * 移动“保存且已读”的文章至一个新的文件夹
     */
    public void moveArticles() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Article> boxReadArts = WithDB.i().getArtInReadedBox();
                List<Article> storeReadArts = WithDB.i().getArtInReadedStore();
                KLog.i("移动文章" + boxReadArts.size() + "=" + storeReadArts.size());

                for (Article article : boxReadArts) {
                    FileUtil.moveFile(App.boxRelativePath + article.getTitle() + ".html", App.boxReadRelativePath + article.getTitle() + ".html");// 移动文件
                    FileUtil.moveDir(App.boxRelativePath + article.getTitle() + "_files", App.boxReadRelativePath + article.getTitle() + "_files");// 移动目录
                    article.setCoverSrc(FileUtil.getAbsoluteDir("boxRead") + article.getTitle() + "_files" + File.separator + StringUtil.getFileNameExtByUrl(article.getCoverSrc()));
                    article.setSaveDir(API.SAVE_DIR_BOXREAD);
                    KLog.i("移动了A");
                }
                WithDB.i().saveArticleList(boxReadArts);
                for (Article article : storeReadArts) {
                    FileUtil.moveFile(App.storeRelativePath + article.getTitle() + ".html", App.storeReadRelativePath + article.getTitle() + ".html");// 移动文件
                    FileUtil.moveDir(App.storeRelativePath + article.getTitle() + "_files", App.storeReadRelativePath + article.getTitle() + "_files");// 移动目录
                    article.setCoverSrc(FileUtil.getAbsoluteDir("storeRead") + article.getTitle() + "_files" + File.separator + StringUtil.getFileNameExtByUrl(article.getCoverSrc()));
                    article.setSaveDir(API.SAVE_DIR_STOREREAD);
                    KLog.i("移动了B" + App.storeRelativePath + article.getTitle() + "_files |||| " + App.storeReadRelativePath + article.getTitle() + "_files");
                }
                WithDB.i().saveArticleList(storeReadArts);
            }
        }).start();
    }

    private void sendSuccess() {
//        Message message = new Message();
        Message message = mHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("tips", "");
        message.what = API.SUCCESS;
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    private void sendFailure() {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("tips", "");
        message.what = API.FAILURE;
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    private void sendProcess(String tips) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("tips", tips);
        message.what = API.PROCESS;
        message.setData(bundle);
        mHandler.sendMessage(message);
    }


    private ArrayMap<Long, RequestLog> requestMap;
    private Neter.Record<RequestLog> recorder = new Neter.Record<RequestLog>() {
        @Override
        public void log(RequestLog entry) {
            KLog.d(" 记录 log");
            if (entry.getHeadParamString() != null && !entry.getHeadParamString().contains("c=")) {
                requestMap.put(entry.getLogTime(), entry);
            }
        }
    };

    public void delRequestLog(long key) {
        if (requestMap != null) {
            if (requestMap.size() != 0) {
                requestMap.remove(key); // 因为最后一次使用 handleMessage(100) 时也会调用
            }
        }
    }

    private void saveRequestLog(long logTime) {
        KLog.i("【saveRequest1】");
        if (requestMap == null) {
            return;
        }
        KLog.i("【saveRequest2】" + requestMap.get(logTime));
        WithDB.i().saveRequestLog(requestMap.get(logTime));
    }

    private boolean syncRequestLog() {
        List<RequestLog> requestLogs = WithDB.i().loadRequestListAll();
        if (requestLogs.size() == 0) {
            return false;
        }
        sendProcess(getResources().getString(R.string.main_toolbar_hint_sync_log));

        if (requestMap.size() != requestLogs.size()) {
            for (RequestLog requestLog : requestLogs) {
                requestMap.put(requestLog.getLogTime(), requestLog);
            }
        }
        // TODO: 2016/5/26 将这个改为 json 格式来持久化 RequestLog 对象 ？貌似也不好
        for (RequestLog requestLog : requestLogs) {
            requestMap.put(requestLog.getLogTime(), requestLog);
            String headParamString = requestLog.getHeadParamString();
            String bodyParamString = requestLog.getBodyParamString();
            mNeter.addHeader(StringUtil.formStringToParamList(headParamString));
            mNeter.addBody(StringUtil.formStringToParamList(bodyParamString));
            KLog.d("同步错误：" + headParamString + " = " + bodyParamString);
            if (requestLog.getMethod().equals("post")) {
                mNeter.postWithAuth(requestLog.getUrl()); // , requestLog.getLogTime()
            }
        }
        WithDB.i().delRequestListAll();  // TODO: 2016/10/20 不能先删除，可能删除后，手机退出，那么这些记录就丢失了
//        hadSyncLogRequest = true;
        KLog.d("读取到的数目： " + requestLogs.size());
        return true;
    }

//    private boolean hadSyncLogRequest = true;

//    private ArrayMap<String,ArrayList> imgRecord;
//    private ArrayMap<Integer,SrcPair> lossSrcList, obtainSrcList ;
//
//    public void regImgRecord(String articleID, ExtraImg extraImg ){
//        ArrayList<ArrayMap> pair = new ArrayList<>(2);
//        pair.add(lossSrcList);
//        pair.add(obtainSrcList);
//        imgRecord.put( articleID, pair );
//    }
//
//    static ExtraImg extraImg = new ExtraImg();
////    static Article currArticle = new Article();
//    /**
//     * 在初次进入 html 获得 imgList 时，记录值 DOWNLOAD_ING。
//     * 在每次成功下载到图片时，记录 DOWNLOAD_ING。
//     * 在所有下载完成时，记录 DOWNLOAD_OVER。
//     * @param imgStatus 有两个值：DOWNLOAD_ING(下载中) 和 DOWNLOAD_OVER(下载完成)
//     */
//    private void logImgStatus( String articleID, int imgStatus){
////        if(extraImg==null){
////            extraImg = new ExtraImg();
////        }
//        extraImg.setImgStatus(imgStatus);
//        extraImg.setObtainImgs(obtainSrcList);
//        extraImg.setLossImgs(lossSrcList);
//        Article article = WithDB.i().getArticle(articleID);
//        article.setImgState( new Gson().toJson(extraImg) );
//        WithDB.i().saveArticle( article );
//        KLog.e("【储存的imgState】" +  new Gson().toJson(extraImg) );
//        WithDB.i().saveArticle(article);
//    }


}
