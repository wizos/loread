package me.wizos.loread.net;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.ArrayMap;
import android.util.SparseIntArray;

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

import me.wizos.loread.bean.RequestLog;
import me.wizos.loread.bean.gson.SrcPair;
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
        this.taskList = new SparseIntArray();
        // 创建线程数
//        this.threadPool = Executors.newFixedThreadPool(10);
    }

    private static Neter mNeter;

    private Neter() {
    }

    private static Neter i() {
        if (mNeter != null) {
            return mNeter;
        }
        synchronized (Neter.class) {
            if (mNeter == null) {
                mNeter = new Neter();
            }
        }
        return mNeter;
    }


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
        addBody("r", "user/-/state/com.google/read");
        addBody("i", articleID);
        postWithAuthLog( API.HOST + API.U_EDIT_TAG);
        KLog.d("【post】1记录 "+ headParamList.size());
    }
    public void postReadArticle( String articleID ){
        addBody("a", "user/-/state/com.google/read");
        addBody("i", articleID);
        postWithAuthLog( API.HOST + API.U_EDIT_TAG);
        KLog.d("【post】2记录 "+ headParamList.size());
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
        if (!HttpUtil.isNetworkEnabled()) {
            headParamList.clear();
//            headersMap.clear();
            handler.sendEmptyMessage(API.F_NoMsg);
            return;}

        Request.Builder builder = new Request.Builder();
        String paraString = "?";
        for ( String[] param : headParamList) {
            paraString = paraString + param[0] + "=" + param[1] + "&";
        }
        headParamList.clear(); // headParamList = new ArrayList<>();


        if (paraString.equals("?")) {
            paraString = "";
        }
        url = url + paraString;
        builder.url(url)
                .addHeader("AppId", API.INOREADER_APP_ID)
                .addHeader("AppKey", API.INOREADER_APP_KEY);
        builder.addHeader("Authorization", API.INOREADER_ATUH);
        Request request = builder.build();
        forData(url, request,0);
    }

    public void postWithAuthLog(final String url) {
        addHeader("AppId", API.INOREADER_APP_ID);
        addHeader("AppKey", API.INOREADER_APP_KEY);
        addHeader("Authorization", API.INOREADER_ATUH);
        long logTime = System.currentTimeMillis();

//        if(requestlogger!=null){
//            requestlogger.logRequest( toRequest( API.HOST + API.U_EDIT_TAG, "post", logTime, headParamList, bodyParamList) );
//        }
        if (record != null) {
            record.log(toRequest(API.HOST + API.U_EDIT_TAG, "post", logTime, headParamList, bodyParamList));
        }
        post(url,logTime);
    }
    public void postWithAuth(final String url) {
        addHeader("AppId", API.INOREADER_APP_ID);
        addHeader("AppKey", API.INOREADER_APP_KEY);
        addHeader("Authorization", API.INOREADER_ATUH);
        post(url,System.currentTimeMillis());
    }
    public void post( String url, long logTime) { // just for login
        KLog.d("【执行 = " + url + "】");
        if (!HttpUtil.isNetworkEnabled()) {
            headParamList.clear();
            bodyParamList.clear();
//            headersMap.clear();
//            bodyParamsMap.clear();
            handler.sendEmptyMessage(API.F_NoMsg);
            return;}
        // 构建请求头
        Request.Builder headBuilder = new Request.Builder().url(url);
        for ( String[] param : headParamList) {
            KLog.d(param[0] + param[1]);
            headBuilder.addHeader( param[0], param[1] );
        }
        headParamList.clear();
//        for( Map.Entry<String,String> entry : headersMap.entrySet() ){
//            KLog.d( entry.getKey() + entry.getValue() );
//            headBuilder.addHeader( entry.getKey(), entry.getValue() );
//        }
//        headersMap.clear();

        KLog.d("----");
        // 构建请求体
        FormEncodingBuilder bodyBuilder = new FormEncodingBuilder();
        for ( String[] param : bodyParamList ) {
            bodyBuilder.add( param[0], param[1] );
            KLog.d(param[0] + param[1]);
        }
        bodyParamList.clear();

//        for( Map.Entry<String,String> entry : bodyParamsMap.entrySet() ){
//            KLog.d( entry.getKey() + entry.getValue() );
//            headBuilder.addHeader( entry.getKey(), entry.getValue() );
//        }
//        bodyParamsMap.clear();


        RequestBody body = bodyBuilder.build();
        Request request = headBuilder.post(body).build();
        forData(url, request, logTime);
    }


    public void forDatax(final String url, final Request request, final long logTime) {
        if (!HttpUtil.isNetworkEnabled()) {
            handler.sendEmptyMessage(API.F_NoMsg);
            return ;}
        KLog.d("【开始请求】 "+ logTime + "--" + url);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpUtil.enqueue(request, new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        KLog.d("【请求失败】" + url );
                        API.request = request;
                        makeMsg(API.F_Request, url, "noRequest",logTime);
                    }
                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            KLog.d("【响应失败】" + response.message() + response.body().string());
                            API.request = request;
                            makeMsg(API.F_Response, url, response.message(),logTime);
                            return;
                        }
                        try {
                            String res = response.body().string();
                            KLog.d("【forData】" + res.length());
                            makeMsg( API.url2int(url), url, res,logTime);
                        }catch (IOException e){
                            KLog.d("【超时】");
                            API.request = request;
                            makeMsg( API.F_Response, url, response.message(), logTime);
                            e.printStackTrace();
                            response.body().close();
                        }
//                        catch (SocketTimeoutException e) {
//                            KLog.d("【超时】");
//                            API.request = request;
//                            makeMsg(url, "noResponse", logTime);
//                            e.printStackTrace();
//                        }
                    }
                });
            }
        }).start();
    }

    public void forData(final String url, final Request request, final long logTime) {
        if (!HttpUtil.isNetworkEnabled()) {
            handler.sendEmptyMessage(API.F_NoMsg);
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
                    API.request = request;
                    makeMsg(API.F_Response, url, response.message(), logTime);
                    return;
                }
                try {
                    String res = response.body().string();
                    KLog.d("【forData】" + res.length());
                    makeMsg(API.url2int(url), url, res, logTime);
                } catch (IOException e) {
                    KLog.d("【超时】");
                    API.request = request;
                    makeMsg(API.F_Response, url, response.message(), logTime);
                    e.printStackTrace();
                    response.body().close();
                }
            }
        });
    }



    public void postCallback( String urlx, final long logTime) { // just for login
        KLog.d("【执行 = " + urlx + "】");
        if (!HttpUtil.isNetworkEnabled()) {
            headParamList.clear();
            bodyParamList.clear();
            handler.sendEmptyMessage(API.F_NoMsg);
            return;}

        // 构建请求头
        Request.Builder headBuilder = new Request.Builder().url(urlx);
        for ( String[] param : headParamList) {
            KLog.d(param[0] + param[1]);
            headBuilder.addHeader( param[0], param[1] );
        }
        headParamList.clear();

        KLog.d("----");
        // 构建请求体
        FormEncodingBuilder bodyBuilder = new FormEncodingBuilder();
        for ( String[] param : bodyParamList ) {
            bodyBuilder.add( param[0], param[1] );
            KLog.d(param[0] + param[1]);
        }
        bodyParamList.clear();


        RequestBody body = bodyBuilder.build();
        final Request request = headBuilder.post(body).build();
        KLog.d("【开始请求离线数据】 "+ logTime + "--" + urlx);

        final String url = urlx;

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpUtil.enqueue(request, new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        KLog.d("【请求失败2】" + url );
                        API.request = request;
                        makeMsg(API.F_Request, url, "noRequest",logTime);
                    }
                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            KLog.d("【响应失败2】" + response.message() + response.body().string());
                            API.request = request;
                            makeMsg(API.F_Response, url, response.message(),logTime);
                            return;
                        }
                        try {
                            String res = response.body().string();
                            KLog.d("【forData2】" + res.length());
                            handler.sendEmptyMessage(API.M_BEGIN_SYNC);
                        }catch (IOException e){
                            KLog.d("【超时2】");
                            API.request = request;
                            makeMsg( API.F_Response, url, response.message(), logTime);
                            e.printStackTrace();
                            response.body().close();
                        }
                    }
                });
            }
        }).start();
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
     *
     */
    public void setLogRequestListener( RequestLogger requestlogger ) {
        this.requestlogger = requestlogger;
    }
    private RequestLogger requestlogger ;
    public interface RequestLogger<T> {
        void logRequest(T entry);
    }
    private RequestLog toRequest(String url, String method, long logTime, ArrayList<String[]> headParamList, ArrayList<String[]> bodyParamList){
        String headParamString = UString.formParamListToString(headParamList);
        String bodyParamString = UString.formParamListToString(bodyParamList);
        return new RequestLog(logTime,url,method,headParamString,bodyParamString);
    }

    // TODO: 2017/1/7  以一个接口形式做回调
    public void setReord(Record record) {
        this.record = record;
    }

    private Record record;



    /**
     * 保存正在下载或等待下载的URL和相应失败下载次数（初始为0），防止滚动时多次下载
     */
    private SparseIntArray taskList;


    /**
     * 异步下载图片，并按指定宽度和高度压缩图片
     * <p>
     * 图片下载完成后调用接口
     */
    public void loadImg(String imgUrl, String filePath, int imgNo) {
        taskList.put(imgNo, 0);
        threadPool.execute(new Task(imgUrl, filePath, imgNo));
    }

    public int loadImg(ArrayMap<Integer, SrcPair> imgSrcList) {
        if (!HttpUtil.isWifiEnabled()) {
            handler.sendEmptyMessage(API.F_NoMsg);
            return 0;
        }
        if (imgSrcList == null || imgSrcList.size() == 0) {
            return 0;
        }
        int length = imgSrcList.size();
        for (ArrayMap.Entry<Integer, SrcPair> entry : imgSrcList.entrySet()) {
            loadImg(entry.getValue().getNetSrc(), entry.getValue().getSaveSrc(), entry.getKey());
            KLog.d("【获取图片的key为：" + entry.getKey() );
        }
        return length;
    }

    private class Task implements Runnable {
        String imgUrl, filePath;
        int imgNo;

        Task(String imgUrl, String filePath, int imgNo) {
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
                    KLog.d("【图片请求失败 = " + imgUrl + "】");
                    makeMsgForImg(API.F_BITMAP, imgUrl, filePath, imgNo);
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    if (!response.isSuccessful()) {
//                        KLog.d("【图片响应失败】" + response);
                        makeMsgForImg(API.F_BITMAP, imgUrl, filePath, imgNo);
                    } else {
                        InputStream inputStream = null;
                        int state = API.S_BITMAP;
                        try {
                            inputStream = response.body().byteStream();
                            if (!UFile.saveFromStream(inputStream, filePath)) {
                                state = API.F_BITMAP;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            response.body().close();
                        }

                        KLog.d("【成功保存图片】" + imgUrl + "==" + filePath);
                        makeMsgForImg(state, imgUrl, filePath, imgNo);
                    }
                }
            });
        }
    }

    private void makeMsgForImg(int msg, String url, String filePath, int imgNo) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putString("filePath", filePath);
        bundle.putInt("imgNo", imgNo);
        message.what = msg;
        message.setData(bundle);
        handler.sendMessage(message);
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

    /**
     * 获取任务列表
     */
    public SparseIntArray getTaskList() {
        return taskList;
    }


}
