package me.wizos.loread.net;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.ArrayMap;

import com.socks.library.KLog;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import me.wizos.loread.bean.Img;
import me.wizos.loread.bean.RequestLog;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.utils.HttpUtil;
import me.wizos.loread.utils.UFile;
import me.wizos.loread.utils.UString;

/**
 * Created by Wizos on 2016/3/10.
 */
public class Neter {
    /**
     * 线程池
     */
    private static ExecutorService threadPool = Executors.newFixedThreadPool(10);
    public Handler handler;
//    private Context context;

    public Neter(Handler handler) {
        KLog.i("【Neter构造函数】" + handler );
        this.handler = handler;
        this.taskMap = new ArrayMap<>();
        // 创建线程数
//        this.threadPool = Executors.newFixedThreadPool(10);
    }

//    private static Neter mNeter;

//    private Neter() {
//    }

//    private static Neter i() {
//        if (mNeter != null) {
//            return mNeter;
//        }
//        synchronized (Neter.class) {
//            if (mNeter == null) {
//                mNeter = new Neter();
//            }
//        }
//        return mNeter;
//    }


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

//    public void postArticle( List<String> articleIDList ){
//        addBody("i", articleID);
//        long logTime = System.currentTimeMillis();
//        logRequest(API.U_EDIT_TAG, "post", logTime, headParamList, bodyParamList);
//        postWithAuth(API.U_ARTICLE_CONTENTS,logTime);
//    }

    public void postRemoveArticleTags( String articleID ,String tagId ){
        addBody("r", tagId );
        addBody("i", articleID);
        postWithAuthLog( API.HOST + API.U_EDIT_TAG);
    }
    public void postAddArticleTags( String articleID ,String tagId ){
        addBody("a", tagId );
        addBody("i", articleID);
        postWithAuthLog( API.HOST + API.U_EDIT_TAG);
    }
    public void postArticleContents( String articleID ){
        addBody("i", articleID);
        postWithAuthLog( API.HOST + API.U_ARTICLE_CONTENTS);
    }
    public void postUnReadArticle( String articleID ){
        KLog.d("【post】1记录 " + headParamList.size());
        addBody("r", "user/-/state/com.google/read");
        addBody("i", articleID);
        postWithAuthLog( API.HOST + API.U_EDIT_TAG);
    }
    public void postReadArticle( String articleID ){
        KLog.d("【post】2记录 " + headParamList.size());
        addBody("a", "user/-/state/com.google/read");
        addBody("i", articleID);
        postWithAuthLog( API.HOST + API.U_EDIT_TAG);
    }
    public void postUnStarArticle( String articleID ){
        addBody("r", "user/-/state/com.google/starred");
        addBody("i", articleID);
        postWithAuthLog( API.HOST + API.U_EDIT_TAG);
    }
    public void postStarArticle( String articleID ){
        addBody("a", "user/-/state/com.google/starred");
        addBody("i", articleID);
        postWithAuthLog( API.HOST + API.U_EDIT_TAG);
    }



    public void getWithAuth(String url) {
        KLog.d("【执行 getWithAuth 】" + url);
//        if (!HttpUtil.isNetworkAvailable()) {
//            headParamList.clear();
//            handler.sendEmptyMessage(API.F_NoMsg);
//            return;}
        String paraString = "?";
        for ( String[] param : headParamList) {
            paraString = paraString + param[0] + "=" + param[1] + "&";
        }
        headParamList.clear(); // headParamList = new ArrayList<>();

        if (paraString.equals("?")) {
            paraString = "";
        }
        url = url + paraString;

        Request request = new Request.Builder().url(url)
                .addHeader("AppId", API.INOREADER_APP_ID)
                .addHeader("AppKey", API.INOREADER_APP_KEY)
                .addHeader("Authorization", API.INOREADER_ATUH)
                .build();
        forData(url, request,0);
    }

    private void postWithAuthLog(final String url) {
        long logTime = System.currentTimeMillis();
//        if(requestlogger!=null){
//            requestlogger.logRequest( toRequest( API.HOST + API.U_EDIT_TAG, "post", logTime, headParamList, bodyParamList) );
//        }
        if (record != null) {
            record.log(toRequest(API.HOST + API.U_EDIT_TAG, "post", logTime, headParamList, bodyParamList));
        }
        postWithAuth(url, logTime);
    }
    public void postWithAuth(final String url) {
        postWithAuth(url, 0);
    }

    public void postWithAuth(String url, long logTime) { // just for login
        KLog.d("【执行 = " + url + "】");
//        if (!HttpUtil.isNetworkAvailable()) {
//            headParamList.clear();
//            bodyParamList.clear();
//            handler.sendEmptyMessage(API.F_NoMsg);
//            return;}

        // 构建请求头
        Request.Builder headBuilder = new Request.Builder().url(url);
        for ( String[] param : headParamList) {
            KLog.d(param[0] + param[1]);
            headBuilder.addHeader( param[0], param[1] );
        }
        headParamList.clear();
        headBuilder.addHeader("AppId", API.INOREADER_APP_ID);
        headBuilder.addHeader("AppKey", API.INOREADER_APP_KEY);
        headBuilder.addHeader("Authorization", API.INOREADER_ATUH);

        KLog.d("----");
        // 构建请求体
        FormEncodingBuilder bodyBuilder = new FormEncodingBuilder();
        for ( String[] param : bodyParamList ) {
            bodyBuilder.add( param[0], param[1] );
            KLog.d(param[0] + param[1]);
        }
        bodyParamList.clear();

        RequestBody body = bodyBuilder.build();
        Request request = headBuilder.post(body).build();
        forData(url, request, logTime);
    }

//
//    public void forDatax(final String url, final Request request, final long logTime) {
//        if (!HttpUtil.isNetworkAvailable()) {
//            handler.sendEmptyMessage(API.F_NoMsg);
//            return ;}
//        KLog.d("【开始请求】 "+ logTime + "--" + url);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                HttpUtil.enqueue(request, new Callback() {
//                    @Override
//                    public void onFailure(Request request, IOException e) {
//                        KLog.d("【请求失败】" + url );
//                        API.request = request;
//                        makeMsg(API.F_Request, url, "noRequest",logTime);
//                    }
//                    @Override
//                    public void onResponse(Response response) throws IOException {
//                        if (!response.isSuccessful()) {
//                            KLog.d("【响应失败】" + response.message() + response.body().string());
//                            API.request = request;
//                            makeMsg(API.F_Response, url, response.message(),logTime);
//                            return;
//                        }
//                        try {
//                            String res = response.body().string();
//                            KLog.d("【forData】" + res.length());
//                            makeMsg( API.url2int(url), url, res,logTime);
//                        }catch (IOException e){
//                            KLog.d("【超时】");
//                            API.request = request;
//                            makeMsg( API.F_Response, url, response.message(), logTime);
//                            e.printStackTrace();
//                            response.body().close();
//                        }
////                        catch (SocketTimeoutException e) {
////                            KLog.d("【超时】");
////                            API.request = request;
////                            makeMsg(url, "noResponse", logTime);
////                            e.printStackTrace();
////                        }
//                    }
//                });
//            }
//        }).start();
//    }

    public void forData(final String url, final Request request, final long logTime) {
        if (!HttpUtil.isNetworkAvailable()) {
//            handler.sendEmptyMessage(API.F_NoMsg);
            makeMsg(API.F_NoMsg, url, "noNet", logTime);
            return;
        }
        KLog.d("【开始请求】 " + logTime + "--" + url);

        HttpUtil.enqueue(request, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                KLog.d("【请求失败】" + url);
                API.request = request;
                makeMsg(API.F_Request, url, "noRequest", logTime);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    KLog.d("【响应失败】" + response.message() + response.body().string());
                    forData(url, request, logTime); // 响应失败直接重试一次
                    API.request = request;
                    makeMsg(API.F_Response, url, response.message(), logTime);
                    return;
                }
                try {
                    String res = response.body().string();
                    KLog.d("【forData】" + res.length());
                    makeMsg(API.url2int(url), url, res, logTime);
                } catch (Exception e) {
                    KLog.d("【超时】");
                    API.request = request;
                    makeMsg(API.F_Response, url, response.message(), logTime);
                    e.printStackTrace();
                    response.body().close();
                }
            }
        });
    }


//
//    public void postCallback( String urlx, final long logTime) { // just for login
//        KLog.d("【执行 = " + urlx + "】");
//        if (!HttpUtil.isNetworkAvailable()) {
//            headParamList.clear();
//            bodyParamList.clear();
//            handler.sendEmptyMessage(API.F_NoMsg);
//            return;}
//
//        // 构建请求头
//        Request.Builder headBuilder = new Request.Builder().url(urlx);
//        for ( String[] param : headParamList) {
//            KLog.d(param[0] + param[1]);
//            headBuilder.addHeader( param[0], param[1] );
//        }
//        headParamList.clear();
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
//
//        RequestBody body = bodyBuilder.build();
//        final Request request = headBuilder.post(body).build();
//        KLog.d("【开始请求离线数据】 "+ logTime + "--" + urlx);
//
//        final String url = urlx;
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                HttpUtil.enqueue(request, new Callback() {
//                    @Override
//                    public void onFailure(Request request, IOException e) {
//                        KLog.d("【请求失败2】" + url );
//                        API.request = request;
//                        makeMsg(API.F_Request, url, "noRequest",logTime);
//                    }
//                    @Override
//                    public void onResponse(Response response) throws IOException {
//                        if (!response.isSuccessful()) {
//                            KLog.d("【响应失败2】" + response.message() + response.body().string());
//                            API.request = request;
//                            makeMsg(API.F_Response, url, response.message(),logTime);
//                            return;
//                        }
//                        try {
//                            String res = response.body().string();
//                            KLog.d("【forData2】" + res.length());
//                            handler.sendEmptyMessage(API.M_BEGIN_SYNC);
//                        }catch (IOException e){
//                            KLog.d("【超时2】");
//                            API.request = request;
//                            makeMsg( API.F_Response, url, response.message(), logTime);
//                            e.printStackTrace();
//                            response.body().close();
//                        }
//                    }
//                });
//            }
//        }).start();
//    }




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
     * 使用 map 替代原有的简单的 数组
     */
//    private ArrayMap<String,String> headersMap = new ArrayMap<>();
//    private ArrayMap<String,String> bodyParamsMap = new ArrayMap<>();
//    public void addHeader( String key ,String value ){
//        headersMap.put(key,value);
//    }
//    public void addHeader( Map<String,String> headers ){
//        headersMap.putAll( headers );
//    }
//    public void addBodyParam( String key ,String value ){
//        bodyParamsMap.put( key, value );
//    }
//    public void addBodyParam( Map<String,String> bodyParams ){
//        bodyParamsMap.putAll( bodyParams );
//    }




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
        String headParamString = UString.formParamListToString(headParamList);
        String bodyParamString = UString.formParamListToString(bodyParamList);
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
    private ArrayMap<String, Integer> taskList;
    private ArrayMap<String, String> taskMap;

    /**
     * 获取任务列表
     */
    public ArrayMap<String, Integer> getTaskList() {
        return taskList;
    }

    /**
     * 取消正在下载的任务
     */
    public synchronized void cancelTasks() {
        if (threadPool != null) {
            threadPool.shutdownNow();
            threadPool = null;
        }
    }

    public void loadImg(String articleID, int imgNo, String imgUrl, String filePath) {
        KLog.i("下载图片的网址：" + imgUrl + "，保存路径：" + filePath + "，所属文章：" + articleID + "，编号：" + imgNo);
        KLog.i(taskMap.get(filePath) + "====" + imgUrl.trim().indexOf("/"));

        if (taskMap.get(filePath) == null && imgUrl.trim().indexOf("/") != 0) {
            KLog.i("===");
            taskMap.put(filePath, articleID);
            threadPool.execute(new Task(articleID, imgNo, imgUrl, filePath));
        }
    }


    public int downImgs(String articleID, ArrayMap<Integer, Img> imgMap, String parentPath) {
        // article.getSaveDir() ) + imgsMeta.getFolder() + File.separator
        KLog.d("批量下图片" + imgMap);
//        if (!HttpUtil.canDownImg()) {
////            handler.sendEmptyMessage(API.F_NoMsg);
//            return 0;
//        }
        if (WithSet.getInstance().isDownImgWifi() && !HttpUtil.isWiFiActive()) {
            return 0;
        } else if (!WithSet.getInstance().isDownImgWifi() && !HttpUtil.isNetworkAvailable()) {
            return 0;
        }
//        if (imgMap == null || imgMap.size() == 0) {
//            return 0;
//        }
        int length = imgMap.size();
        for (ArrayMap.Entry<Integer, Img> entry : imgMap.entrySet()) {
            loadImg(articleID, entry.getKey(), entry.getValue().getSrc(), parentPath + entry.getValue().getName());
            KLog.d("【获取图片的key为：" + entry.getKey() );
        }
        KLog.d("批量下图片b" + length);
        return length;
    }


    private class Task implements Runnable {
        String articleID, imgUrl, filePath;
        int imgNo;

        Task(String articleID, int imgNo, String imgUrl, String filePath) {
            this.articleID = articleID;
            this.imgUrl = imgUrl;
            this.filePath = filePath;
            this.imgNo = imgNo;
        }

        @Override
        public void run() {
            Request.Builder builder = new Request.Builder();
            builder.url(imgUrl);
            final Request request = builder.build();
            HttpUtil.enqueue(request, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    KLog.i("【图片请求失败 = " + imgUrl + "】");
                    makeMsgForImg(articleID, imgNo, API.F_BITMAP);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) {
//                        KLog.d("【图片响应失败】" + response);
                        makeMsgForImg(articleID, imgNo, API.F_BITMAP);
                    } else {
                        InputStream inputStream = null;
                        int state = API.S_BITMAP;
                        try {
                            inputStream = response.body().byteStream();
                            if (!UFile.saveFromStream(inputStream, filePath)) {
                                state = API.F_BITMAP;
                            }
                        } finally {
                            try {
                                response.body().close();
                            } catch (final IOException ex) {
                                KLog.e("Problem while cleaning up.", ex);
                            }
                        }

                        KLog.i("【成功保存图片】" + imgUrl + "==" + filePath);
                        makeMsgForImg(articleID, imgNo, state);
                    }
                }
            });
        }
    }

    private void makeMsgForImg(String articleID, int imgNo, int msg) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("articleID", articleID);
//        bundle.putString("url", url);
//        bundle.putString("filePath", filePath);
        bundle.putInt("imgNo", imgNo);
        message.what = msg;
        message.setData(bundle);
        handler.sendMessage(message);
    }

//    private void makeMsgForImg(String articleID, String url, String filePath, int imgNo, int msg) {
//        Message message = new Message();
//        Bundle bundle = new Bundle();
//        bundle.putString("articleID", articleID);
////        bundle.putString("url", url);
////        bundle.putString("filePath", filePath);
//        bundle.putInt("imgNo", imgNo);
//        message.what = msg;
//        message.setData(bundle);
//        handler.sendMessage(message);
//    }



}
