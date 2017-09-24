package me.wizos.loread.net;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.ArrayMap;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.PostRequest;
import com.lzy.okgo.request.base.Request;
import com.socks.library.KLog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.bean.Img;
import me.wizos.loread.bean.RequestLog;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.utils.HttpUtil;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.utils.ToastUtil;

/**
 * Created by Wizos on 2016/3/10.
 */
public class Neter {
    /**
     * 线程池
     */
    private Handler handler;

    public Neter(Handler handler) {
//        KLog.i("【Neter构造函数】" + handler );
        this.handler = handler;
        this.taskMap = new ArrayMap<>();
        // 创建线程数
//        this.threadPool = Executors.newFixedThreadPool(10);
    }
//    private static ExecutorService threadPool;


    /**
     * 网络请求的结口分 3 层
     * 1，是最明确的，预置好参数的特制 getXX,postXX
     * 2，是仅预置好 AppId AppKey 的 getWithAuth , postWithAuth
     * 3，是最基本的 get post 方法，不带有参数
     */
    /**
     * HTTP 异步请求。
     *
     * @param mUserID  要获取的用户
     */
    public void getUnReadRefs( long mUserID ){
        addHeader("n", "200");
//        addHeader("ot","0");
        addHeader("xt","user/"+ mUserID+"/state/com.google/read");
        addHeader("s", "user/" + mUserID + "/state/com.google/reading-list");
        getWithAuth( API.HOST + API.U_ITEM_IDS );
    }

    public void getStarredRefs( long mUserID ){
        addHeader("n", "200");
//        addHeader("ot","0");
        addHeader("s", "user/" + mUserID + "/state/com.google/starred");
        getWithAuth( API.HOST + API.U_ITEM_IDS );
    }
    public void getStarredContents(){
        addHeader("n", "15");
        addHeader("r", "o");
        getWithAuth(API.HOST + API.U_STREAM_CONTENTS + API.U_STARRED);
    }

    public void articleRemoveTag(String articleID, String tagId) {
        addBody("r", tagId );
        addBody("i", articleID);
        postWithAuthLog( API.HOST + API.U_EDIT_TAG);
    }

    public void articleAddTag(String articleID, String tagId) {
        addBody("a", tagId );
        addBody("i", articleID);
        postWithAuthLog( API.HOST + API.U_EDIT_TAG);
    }

    public void getArticleContents(String articleID) {
        addBody("i", articleID);
        postWithAuthLog( API.HOST + API.U_ARTICLE_CONTENTS);
    }

    public void markArticleUnread(String articleID) {
        KLog.d("【post】1记录 " + headParamList.size());
        addBody("r", "user/-/state/com.google/read");
        addBody("i", articleID);
        postWithAuthLog( API.HOST + API.U_EDIT_TAG);
    }

    public void markArticleReaded(String articleID) {
        KLog.d("【post】2记录 " + headParamList.size());
        addBody("a", "user/-/state/com.google/read");
        addBody("i", articleID);
        postWithAuthLog( API.HOST + API.U_EDIT_TAG);
    }

    public void markArticleUnstar(String articleID) {
        addBody("r", "user/-/state/com.google/starred");
        addBody("i", articleID);
        postWithAuthLog( API.HOST + API.U_EDIT_TAG);
    }

    public void markArticleStared(String articleID) {
        addBody("a", "user/-/state/com.google/starred");
        addBody("i", articleID);
        postWithAuthLog( API.HOST + API.U_EDIT_TAG);
    }


    // 同步的步骤：
    // 第1步，同步分组信息， mNeter.getWithAuth(API.HOST + API.U_TAGS_LIST);，解析响应的返回Parser.instance().parseTagList(info);
    // 第2步，同步分组和feed 的排序信息mNeter.getWithAuth(API.HOST + API.U_STREAM_PREFS);// 有了这份数据才可以对 tagslist feedlist 进行排序，并储存下来


//    public void getWithAuth(String url) {
//        KLog.d("【执行 getWithAuth 】" + url);
//
//        String paraString = "?";
//        for ( String[] param : headParamList) {
//            paraString = paraString + param[0] + "=" + param[1] + "&";
//        }
//        headParamList.clear(); // headParamList = new ArrayList<>();
//
//        if (paraString.equals("?")) {
//            paraString = "";
//        }
//        url = url + paraString;
//
//        Request request = new Request.Builder().url(url)
//                .addHeader("AppId", API.INOREADER_APP_ID)
//                .addHeader("AppKey", API.INOREADER_APP_KEY)
//                .addHeader("Authorization", API.INOREADER_ATUH)
//                .build();
//        forData(url, request,0);
//    }

//    private void postWithAuthLog(final String url) {
//        long logTime = System.currentTimeMillis();
////        if(requestlogger!=null){
////            requestlogger.logRequest( toRequest( API.HOST + API.U_EDIT_TAG, "post", logTime, headParamList, bodyParamList) );
////        }
//        if (record != null) {
//            record.log(toRequest(API.HOST + API.U_EDIT_TAG, "post", logTime, headParamList, bodyParamList));
//        }
//        postWithAuth(url, logTime);
//    }
//    public void postWithAuth(final String url) {
//        postWithAuth(url, 0);
//    }

//    public void postWithAuth(String url, long logTime) { // just for login
//        KLog.d("【执行 = " + url + "】");
//
//        // 构建请求头
//        Request.Builder headBuilder = new Request.Builder().url(url);
//        for ( String[] param : headParamList) {
//            KLog.d(param[0] + param[1]);
//            headBuilder.addHeader( param[0], param[1] );
//        }
//        headParamList.clear();
//        headBuilder.addHeader("AppId", API.INOREADER_APP_ID);
//        headBuilder.addHeader("AppKey", API.INOREADER_APP_KEY);
//        headBuilder.addHeader("Authorization", API.INOREADER_ATUH);
//
//        KLog.d("----");
//        // 构建请求体
//        FormEncodingBuilder bodyBuilder = new FormEncodingBuilder();
//        for ( String[] param : bodyParamList ) {
//            bodyBuilder.add( param[0], param[1] );
//            KLog.d(param[0] + param[1]);
//        }
//        bodyParamList.clear();
//
//        RequestBody body = bodyBuilder.build();
//        Request request = headBuilder.post(body).build();
//        forData(url, request, logTime);
//    }
    // TEST:  测试新的okhttp3
//    public void forData(final String url, final Request request, final long logTime) {
//        if (!HttpUtil.isNetworkAvailable()) {
////            handler.sendEmptyMessage(API.F_NoMsg);
//            makeMsg(API.F_NoMsg, url, "noNet", logTime);
//            return;
//        }
//        KLog.d("【开始请求】 " + logTime + "--" + url);
//
//        HttpUtil.enqueue(request, new Callback() {
//            @Override
//            public void onFailure(Request request, IOException e) {
//                KLog.d("【请求失败】" + url);
//                API.request = request;
//                makeMsg(API.F_Request, url, "noRequest", logTime);
//            }
//
//            @Override
//            public void onResponse(Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    KLog.d("【响应失败】" + response.message() + response.body().string());
//                    forData(url, request, logTime); // 响应失败直接重试一次
//                    API.request = request;
//                    makeMsg(API.F_Response, url, response.message(), logTime);
//                    return;
//                }
//                try {
//                    String res = response.body().string();
//                    KLog.d("【forData】" + res.length());
//                    makeMsg(API.url2int(url), url, res, logTime);
//                } catch (Exception e) {
//                    KLog.d("【超时】");
//                    API.request = request;
//                    makeMsg(API.F_Response, url, response.message(), logTime);
//                    e.printStackTrace();
//                    response.body().close();
//                }
//            }
//        });
//    }



    private void postWithAuthLog(final String url) {
        long logTime = System.currentTimeMillis();
        if (record != null) {
            record.log(toRequest(API.HOST + API.U_EDIT_TAG, "post", logTime, headParamList, bodyParamList));
        }
        postNetDataWithAuth(url, logTime);
    }
    public void postWithAuth(final String url) {
        postNetDataWithAuth(url, 0);
    }

    public void getWithAuth(final String url) {
        getNetDataWithAuth(url, 0);
    }

    public void getNetDataWithAuth(final String url, final long logTime) {
        if (!HttpUtil.isNetworkAvailable()) {
            makeMsg(API.F_NoMsg, url, "noNet", logTime);
            return;
        }

        final GetRequest<String> get = OkGo.get(url);
//        get.addUrlParams()
        for ( String[] param : headParamList) {
            get.params(param[0], param[1], false);
        }
        headParamList.clear();
        get.headers("AppId", API.INOREADER_APP_ID)
                .headers("AppKey", API.INOREADER_APP_KEY)
                .headers("Authorization", API.INOREADER_ATUH);
        get.tag(url);
        get.execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                makeMsg(API.url2int(url), url, response.body().toString(), logTime);
            }

            @Override
            public void onError(Response<String> response) {
                API.request = get;
                makeMsg(API.F_Request, url, "onError", logTime);
            }
        });
    }

    private void postNetDataWithAuth(final String url, final long logTime) {
        if (!HttpUtil.isNetworkAvailable()) {
            makeMsg(API.F_NoMsg, url, "noNet", logTime);
            return;
        }

        final PostRequest<String> post = OkGo.post(url);
        for (String[] param : headParamList) {
            post.headers(param[0], param[1]);
        }
        headParamList.clear();
        post.headers("AppId", API.INOREADER_APP_ID)
                .headers("AppKey", API.INOREADER_APP_KEY)
                .headers("Authorization", API.INOREADER_ATUH);

        for (String[] param : bodyParamList) {
            post.params(param[0], param[1], false); // 添加参数的时候,最后一个isReplace为可选参数,默认为true，即代表相同key的时候，后添加的会覆盖先前添加的。
        }
        bodyParamList.clear();

        post.tag(url);
        post.execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                makeMsg(API.url2int(url), url, response.body().toString(), logTime);
            }

            @Override
            public void onError(Response<String> response) {
                API.request = post;
                makeMsg(API.F_Request, url, "onError", logTime);
            }
        });
    }

    //    public void getNetData(final String url){
//        if (!HttpUtil.isNetworkAvailable()) {
//            makeMsg(API.F_NoMsg, url, "noNet",0);
//            return;
//        }
//        exeRequest( packGetRequest( url ));
//    }
//
//    private Request packGetRequest( String url ){
//        final GetRequest<String> request = OkGo.<String>get(url);
//        for ( String[] param : headParamList) {
//            request.params(param[0], param[1]);
//        }
//        headParamList.clear();
//        request.headers("AppId", API.INOREADER_APP_ID)
//                .headers("AppKey", API.INOREADER_APP_KEY)
//                .headers("Authorization", API.INOREADER_ATUH);
//        request.tag(url);
//        return request;
//    }
//
//    public void packPostRequest(){
//
//    }
//
    public void exeRequest(final Request request, long logTime) {
        request.execute(new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                makeMsg(API.url2int(request.getUrl()), request.getUrl(), response.body().toString(), 0);
            }

            @Override
            public void onError(Response<String> response) {
                API.request = request;
                makeMsg(API.F_Request, request.getUrl(), "onError", 0);
            }
        });
    }

    private void makeMsg( int msgCode, String url, String res, long logTime) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putLong("logTime",logTime);

        message.what = msgCode;
        bundle.putString("res", res);
        message.setData(bundle);
        handler.sendMessage(message);
        KLog.d("【makeMsg】" + url   + " -- " );
    }


    private ArrayList<String[]> headParamList = new ArrayList<>();
    private ArrayList<String[]> bodyParamList = new ArrayList<>();
    public void addHeader(String key ,String value){
        String[] params = new String[2];
        params[0] = key;
        params[1] = value;
        headParamList.add(params);
    }
    public void addHeader(ArrayList<String[]> params){
        headParamList.addAll(params);
    }
    public void addHeader(String[] params){
        headParamList.add(params);
    }
    public void addBody(String key ,String value){
        String[] params = new String[2];
        params[0] = key;
        params[1] = value;
        bodyParamList.add(params);
    }
    public void addBody(ArrayList<String[]> params){
        bodyParamList.addAll(params);
    }



    /**
     * 此处设置了一个回调，使得调用者可以获得想要 cache 的 request。
     */


    // TODO: 2017/1/7  以一个接口形式做回调
    private Record record;

    public void setReord(Record record) {
        this.record = record;
    }

    public interface Record<T> {
        void log(T entry);
    }
    private RequestLog toRequest(String url, String method, long logTime, ArrayList<String[]> headParamList, ArrayList<String[]> bodyParamList){
        String headParamString = StringUtil.formParamListToString(headParamList);
        String bodyParamString = StringUtil.formParamListToString(bodyParamList);
        return new RequestLog(logTime,url,method,headParamString,bodyParamString);
    }

//    public void setRecordListener( RequestLogger requestlogger ) {
//        this.requestlogger = requestlogger;
//    }
//    private RequestLogger requestlogger ;
//    public interface RequestLogger<T> {
//        void logRequest(T entry);
//    }

    /**
     * 保存正在下载或等待下载的URL和相应失败下载次数（初始为0），防止滚动时多次下载
     */
//    private ArrayMap<String, Integer> taskList;
    private ArrayMap<String, String> taskMap;

//    /**
//     * 获取任务列表
//     */
//    public ArrayMap<String, Integer> getTaskList() {
//        return taskList;
//    }
//
//    /**
//     * 取消正在下载的任务
//     */
//    public synchronized void cancelTasks() {
//        if (threadPool != null) {
//            threadPool.shutdownNow();
//            threadPool = null;
//        }
//    }

//    /**
//     * 获取线程池的方法，因为涉及到并发的问题，我们加上同步锁
//     *
//     * @return
//     */
//    private ExecutorService getThreadPool() {
//        KLog.e("获取多线程池getThreadPool");
//        if (threadPool == null) {
//            synchronized (ExecutorService.class) {
//                if (threadPool == null) {
//                    //为了下载图片更加的流畅，我们用了2个线程来下载图片
//                    KLog.e("获取多线程池子");
//                    threadPool = Executors.newFixedThreadPool(3);
////                    threadPool = Executors.newCachedThreadPool();
//                }
//            }
//        }
//        return threadPool;
//    }

    public void downImg(String articleID, int imgNo, String imgUrl, String filePath) {
        KLog.i("下载图片的网址：" + imgUrl + "，保存路径：" + filePath + "，所属文章：" + articleID + "，编号：" + imgNo);
//        KLog.i(taskMap.get(filePath) + "====" + imgUrl.trim().indexOf("/"));
        if (taskMap.get(imgUrl) == null || imgUrl.trim().indexOf("/") != 0) {
            KLog.e("===");
            taskMap.put(imgUrl, articleID);
            // TEST:  测试新的okhttp3
//            img(articleID, imgNo, imgUrl, filePath);
//            getThreadPool().execute(new Task(articleID, imgNo, imgUrl, filePath));
//            new DownloadImgTask().execute(imgUrl,filePath,articleID,String.valueOf(imgNo));
        } else {
            ToastUtil.showShort("图片正在下载，请不要着急");
        }
    }

    public int downImgs2(String articleID, List<Img> imgList, String parentPath) {
        if (WithSet.i().isDownImgWifi() && !HttpUtil.isWiFiActive()) {
            return 0;
        } else if (!WithSet.i().isDownImgWifi() && !HttpUtil.isNetworkAvailable()) {
            return 0;
        }
        for (Img img : imgList) {
            downImgSrc(articleID, parentPath, img);
        }
        KLog.e("批量下图片B：" + imgList.size());
        return imgList.size();
    }

    private ArrayMap<String, String> articleImgTaskMap;
    public int downImgs(String articleID, ArrayMap<Integer, Img> imgMap, String parentPath) {
        KLog.e("批量下图片" + imgMap);

        if (WithSet.i().isDownImgWifi() && !HttpUtil.isWiFiActive()) {
            return 0;
        } else if (!WithSet.i().isDownImgWifi() && !HttpUtil.isNetworkAvailable()) {
            return 0;
        }

        int length = imgMap.size();
        for (ArrayMap.Entry<Integer, Img> entry : imgMap.entrySet()) {
            downImg(articleID, entry.getKey(), entry.getValue().getSrc(), parentPath + entry.getValue().getName());
            KLog.e("【获取图片的key为：" + entry.getKey());
        }
        KLog.e("批量下图片B：" + length);
        return length;
    }

//    private void img(final String articleID, final int imgNo, final String imgUrl, final String filePath) {
//        KLog.e("正真的开始下图");
//        Request.Builder builder = new Request.Builder();
//        builder.url(imgUrl);
//        final Request request = builder.build();
//
//        HttpUtil.i().exe(request, new Callback() {
//            @Override
//            public void onFailure(Request request, IOException e) {
//                KLog.e("【图片请求失败 = " + imgUrl + "】");
//                makeMsgForImg(articleID, imgUrl, imgNo, API.F_BITMAP);
//            }
//
//            @Override
//            public void onResponse(Response response) throws IOException {
//                if (!response.isSuccessful()) {
//                    KLog.e("【图片响应失败】" + response);
//                    makeMsgForImg(articleID, imgUrl, imgNo, API.F_BITMAP);
////                    if(response.code()==404){
////                    }
//                } else {
//                    InputStream inputStream;
//                    int state = API.S_BITMAP;
//                    try {
//                        inputStream = response.body().byteStream();
//                        if (!FileUtil.saveFromStream(inputStream, filePath)) {
//                            state = API.F_BITMAP;
//                        }
//                    } finally {
//                        try {
//                            response.body().close();
////                            KLog.e("【关闭图片响应】" + response);
//                        } catch (final IOException ex) {
////                            KLog.e("Problem while cleaning up.", ex);
//                        }
//                    }
//                    KLog.e("onResponse", "当前线程为：" + Thread.currentThread().getId() + Thread.currentThread().getName() + "==" + filePath);
////                        KLog.e("onResponse" ,"成功保存图片：" + imgUrl + "==" + filePath);
//                    makeMsgForImg(articleID, imgUrl, imgNo, state);
//                }
//            }
//        });
//    }


    public void downImgSrc(final String articleID, final String parentPath, final Img img) {
        OkGo.<File>get(img.getSrc())
                .tag(articleID)
                .execute(new FileCallback(parentPath, img.getName()) {
                    @Override
                    public void onSuccess(Response<File> response) {
//                        ToastUtil.showShort("图片文件保存成功" + parentPath + img.getName() );
                        makeMsgForImg(articleID, img.getSrc(), img.getNo(), API.S_BITMAP);
                        KLog.e("onSuccess", "当前线程为：" + Thread.currentThread().getId() + Thread.currentThread().getName());
                    }

                    // 该方法执行在主线程中
                    @Override
                    public void onError(Response<File> response) {
                        makeMsgForImg(articleID, img.getSrc(), img.getNo(), API.F_BITMAP);
                        KLog.e("onError", img.getSrc() + "当前线程为：" + Thread.currentThread().getId() + Thread.currentThread().getName());
                    }
                });
    }

    private void makeMsgForImg(String articleID, String imgSrc, int imgNo, int msg) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("articleID", articleID);
        bundle.putString("imgSrc", imgSrc);
//        bundle.putString("filePath", filePath);
        bundle.putInt("imgNo", imgNo);
        KLog.e("【】下载图片" + imgSrc + "==" + articleID);
        message.what = msg;
        message.setData(bundle);
        handler.sendMessage(message);
        taskMap.remove(imgSrc);
    }

//    public class DownloadImgTask extends AsyncTask<String, Integer, Void> {
//        public static final String TAG = "DownloadWebImgTask";
////        @Override
////        protected void onProgressUpdate(String... values) {
////            super.onProgressUpdate(values);
//////            handler.sendMessage();
////        }
////        @Override
////        protected void publishProgress(String string){
////        }
//
////        @Override
////        protected void onPostExecute(Void result) {
////            //这段代码只是确保所有图片都顺利的显示出来
////            webView.loadUrl("javascript:(function(){" +
////                    "var objs = document.getElementsByTagName(\"img\"); " +
////                    "for(var i=0;i<objs.length;i++)  " +
////                    "{"
////                    + "    var imgSrc = objs[i].getAttribute(\"src_link\"); " +
////                    "    objs[i].setAttribute(\"src\",imgSrc);" +
////                    "}" +
////                    "})()");
////            super.onPostExecute(result);
////        }
//
//        @Override
//        protected Void doInBackground(String... params){
//            int state = API.S_BITMAP;
//            try {
//                Request.Builder builder = new Request.Builder();
//                builder.url(params[0]);
//                final Request request = builder.build();
//                if(HttpUtil.enqueue(request).execute().isSuccessful()){
//                    InputStream inputStream;
//                    state = API.S_BITMAP;
//                    inputStream = HttpUtil.enqueue(request).execute().body().byteStream();
//                    if (!FileUtil.saveFromStream(inputStream, params[1])) {
//                        state = API.F_BITMAP;
//                    }
//                }
//            }catch (IOException e){
//            }
//            makeMsgForImg(params[2], params[0], Integer.valueOf(params[3]), state);
//            return null;
//        }
//    }
}
