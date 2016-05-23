package me.wizos.loread.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.socks.library.KLog;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import me.wizos.loread.bean.RequestLog;
import me.wizos.loread.bean.x.Strings;
import me.wizos.loread.utils.HttpUtil;
import me.wizos.loread.utils.UFile;

/**
 * Created by Wizos on 2016/3/10.
 */
public class Neter {

    public Handler handler;
    private Context context;

    public Neter(Handler handler ,Context context) {
        KLog.i("【Neter构造函数】" + handler );
        this.handler = handler;
        this.context = context;
    }

    public static boolean isWifiEnabled(Context context) {
        if (!isNetworkEnabled(context)) {
            return false;
        }
        return ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).isWifiEnabled();
    }
    public static boolean isNetworkEnabled(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    /**
     * 网络请求的结口分 3 层
     * 1，是最明确的，预置好参数的特制 getXX,postXX
     * 2，是仅预置好 AppId AppKey 的 getWithAuth , postWithAuth
     * 3，是最基本的 get post 方法，不带有参数
     */

    public void getUnReadRefs(final String url,long mUserID){
        addHeader("n","160");
        addHeader("ot","0");
        addHeader("xt","user/"+ mUserID+"/state/com.google/read");
        addHeader("s", "user/" + mUserID + "/state/com.google/reading-list");
        getWithAuth(url);
    }

    public void getStarredRefs(final String url,long mUserID){
        addHeader("n","160");
        addHeader("ot","0");
        addHeader("s", "user/" + mUserID + "/state/com.google/starred");
//        System.out.println("【Header22】 " + headParamList.get(2).toString());
        getWithAuth(url);
    }
    public void getStarredContents(){
        addHeader("n","20");
        addHeader("ot", "0");
        getWithAuth(API.U_STREAM_CONTENTS + API.U_STARRED);
    }
    public void postArticleContents( String articleID ){
        addBody("i", articleID);
        long logTime = System.currentTimeMillis();
        logRequest(API.U_EDIT_TAG, "post", logTime, headParamList, bodyParamList);
        postWithAuth(API.U_ARTICLE_CONTENTS, logTime);
    }
//    public void postArticle( List<String> articleIDList ){
//        addBody("i", articleID);
//        long logTime = System.currentTimeMillis();
//        logRequest(API.U_EDIT_TAG, "post", logTime, headParamList, bodyParamList);
//        postWithAuth(API.U_ARTICLE_CONTENTS,logTime);
//    }


    public void postUnReadArticle( String articleID ){
        addBody("r", "user/-/state/com.google/read");
        addBody("i", articleID);
        long logTime = System.currentTimeMillis();
        logRequest(API.U_EDIT_TAG, "post", logTime, headParamList, bodyParamList);
        postWithAuth(API.U_EDIT_TAG,logTime);
    }
    public void postReadArticle( String articleID ){
        addBody("a", "user/-/state/com.google/read");
        addBody("i", articleID);
        long logTime = System.currentTimeMillis();
        logRequest(API.U_EDIT_TAG, "post", logTime, headParamList, bodyParamList);
        postWithAuth(API.U_EDIT_TAG,logTime);
    }
    public void postUnStarArticle( String articleID ){
        addBody("r", "user/-/state/com.google/starred");
        addBody("i", articleID);
        long logTime = System.currentTimeMillis();
        logRequest(API.U_EDIT_TAG, "post", logTime, headParamList, bodyParamList);
        postWithAuth(API.U_EDIT_TAG,logTime);
    }
    public void postStarArticle( String articleID ){
        addBody("a", "user/-/state/com.google/starred");
        addBody("i", articleID);
        long logTime = System.currentTimeMillis();
        logRequest(API.U_EDIT_TAG, "post", logTime, headParamList, bodyParamList);
        postWithAuth(API.U_EDIT_TAG, logTime);
    }


    //测试之用
    public void getWithAuth3(String url, long logCode) {
        System.out.println("【执行 getWithAuth 1 】" + url);
        if(!isNetworkEnabled(context)){
            handler.sendEmptyMessage(55);
            return;}
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        addHeader("Authorization", API.INOREADER_ATUH);
        addHeader("AppId", API.INOREADER_APP_ID);
        addHeader("AppKey", API.INOREADER_APP_KEY);
        for ( Parameter para : headParamList) {
            builder.addHeader( para.getKey() , para.getValue() );
        }
        headParamList.clear();
        Request request = builder.build();
        forData(url, request,logCode);
    }

    public void getWithAuth(String url) {
        System.out.println("【执行 getWithAuth 】" + url);
        if(!isNetworkEnabled(context)){
            handler.sendEmptyMessage(55);
            return;}

        Request.Builder builder = new Request.Builder();
        String paraString = "?";
        for ( Parameter para : headParamList) {
            paraString = paraString + para.getKey() + "=" + para.getValue() + "&";
        }
        headParamList.clear(); // headParamList = new ArrayList<>();

        if (paraString.equals("?")) {
            paraString = "";
        }
        url = url + paraString;
        builder.url(url)
                .addHeader("Authorization", API.INOREADER_ATUH)
                .addHeader("AppId", API.INOREADER_APP_ID)
                .addHeader("AppKey", API.INOREADER_APP_KEY);

        Request request = builder.build();
        forData(url, request,0);
    }


    public void postWithAuth(final String url) {
        postWithAuth(url,System.currentTimeMillis());
    }
    public void postWithAuth(final String url ,long logTime) {
        addHeader("AppId", API.INOREADER_APP_ID);
        addHeader("AppKey", API.INOREADER_APP_KEY);
        addHeader("Authorization", API.INOREADER_ATUH);
        post(url,logTime);
    }
    public void post(final String url){
        post(url,System.currentTimeMillis());
    }
    public void post(final String url, long logTime) { // just for login
        System.out.println("【执行 = " + url + "】");
        if(!isNetworkEnabled(context)){
            handler.sendEmptyMessage(55);
            return;}
        // 构建请求头
        Request.Builder headBuilder = new Request.Builder().url(url);
        for ( Parameter para : headParamList) {
            headBuilder.addHeader(para.getKey(), para.getValue());
        }
        headParamList.clear();

        // 构建请求体
        FormEncodingBuilder bodyBuilder = new FormEncodingBuilder();
        for ( Parameter para : bodyParamList ) {
            bodyBuilder.add( para.getKey(), para.getValue());
        }
        bodyParamList.clear();

        RequestBody body = bodyBuilder.build();
        Request request = headBuilder.post(body).build();
//        saveRequest(url,"post",headParamList, bodyParamList );
        forData(url, request,logTime);
    }


    public void forData(final String url, final Request request ,final long logTime) {
        if( !isNetworkEnabled(context) ){
            handler.sendEmptyMessage(55);
            return ;}
        KLog.d("【开始请求】 "+ logTime + url);

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpUtil.enqueue(request, new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        System.out.println("【请求失败】" + url );
                        API.request = request;
                        makeMsg(url, "noRequest",logTime);
                    }
                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            System.out.println("【响应失败】" + response);
                            API.request = request;
                            makeMsg(url, "noResponse",logTime);
                            return;
                        }
                        try {
                            String res = response.body().string();
                            System.out.println("【forData】" + res.length());
                            makeMsg(url, res,logTime);
                        }catch (SocketTimeoutException e) {
                            System.out.println("【超时】");
                            API.request = request;
                            makeMsg(url, "noResponse", logTime);
                            e.printStackTrace();
                        }

                    }
                });
            }
        }).start();
    }

    private void makeMsg(String url, String res , long logTime) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putLong("logTime",logTime);
//        bundle.putString("param", url); // 有些可能 url 一样，但头部不一样。而我又是靠 url 来区分请求，从而进行下一步的。所以立此参数，以作分辨
        if (res.equals("noRequest")) {
            message.what = API.FAILURE_Request;
        } else if (res.equals("noResponse")) {
            message.what = API.FAILURE_Response;
        } else {
            bundle.putString("res", res);
            message.what = API.url2int(url);
        }
        message.setData(bundle);
        handler.sendMessage(message);
        System.out.println("【getData】" +  message.what + " -- "+ url );
    }



    private ArrayList<Parameter> headParamList = new ArrayList<>();
    private ArrayList<Parameter> bodyParamList = new ArrayList<>();
    private Parameter parameter;
    public void addHeader(String key ,String value){
        parameter = new Parameter(key,value);
        headParamList.add(parameter);
//        System.out.println("【addHeader】 " + key + ": " + value);
    }

    public void addBody(String key ,String value){
        parameter = new Parameter(key,value);
        bodyParamList.add(parameter);
    }

    public class Parameter {
        private String key;
        private String value;

        Parameter(String key, String value){
            this.key = key;
            this.value = value;
        }
        public void setKey(String key){
            this.key = key;
        }
        public String getKey(){
            return key;
        }

        public void setValue(String value){
            this.value = value;
        }
        public String getValue(){
            return value;
        }
    }


    private void logRequest(String url, String method,long logTime, ArrayList<Parameter> headParamList, ArrayList<Parameter> bodyParamList){
        String file = "{\n\"items\": [{\n\"url\": \"" + url + "\",\n\"method\": \"" + method  + "\",";
        StringBuffer sbHead = new StringBuffer("");
        StringBuffer sbBody = new StringBuffer("");

        if( headParamList!=null){
            if(headParamList.size()!=0){
                for(Parameter param:headParamList){
                    sbHead.append(param.getKey() + ":" + param.getValue() + ",");
                }
                sbHead.deleteCharAt(sbHead.length() - 1);
            }
        }

        if( bodyParamList!=null ){
            if( bodyParamList.size()!=0 ){
                for(Parameter param:bodyParamList){
                    sbBody.append(param.getKey() + ":" + param.getValue() + ",");
                }
                sbBody.deleteCharAt(sbBody.length() - 1);
            }
        }

        RequestLog requests = new RequestLog(url,method,logTime,sbHead.toString(),sbBody.toString());
        logRequest.addRequest(requests);
        System.out.println("【添加请求】" + url + sbHead.toString());
    }

    public void setLogRequestListener(LogRequest logRequest) {
        this.logRequest = logRequest;
    }

    protected LogRequest logRequest ;
    public interface LogRequest {
        void addRequest(RequestLog requests);
        void delRequest(long index);
    }



    public int getBitmapList(ArrayList<Strings> imgSrcList){
        if(!isWifiEnabled(context)){
            handler.sendEmptyMessage(55);
            return 0;}
        if(imgSrcList == null || imgSrcList.size()==0){
            return 0;
        }
        int num = imgSrcList.size();
        for(int i=0;i<num;i++){
            getBitmap(imgSrcList.get(i).getStringA(), imgSrcList.get(i).getStringB(), i );
//            downImg( imgSrcList.get(i).getStringA(),imgSrcList.get(i).getStringB(),i  );
        }
        return num;
    }
    public void getBitmap(final String url ,final String filePath ,final int imgNum) {
        System.out.println("【获取图片 " + url + "】");
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        Request request = builder.build();
        HttpUtil.enqueue(request, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                System.out.println("【图片请求失败 = " + url + "】");
                makeMsgForImg(url, filePath ,imgNum );
            }
            @Override
            public void onResponse(Response response) throws IOException {
                if (!response.isSuccessful()) {
                    System.out.println("【图片响应失败】" + response);
                    makeMsgForImg(url, filePath ,imgNum);
                    return;
                }
                inputStream = response.body().byteStream();

                String fileTypeInResponse = "";
                MediaType mediaType = response.body().contentType();
                if (mediaType != null) {
                    fileTypeInResponse = "." + mediaType.subtype();
                }
                try {
                    UFile.saveFromStream(inputStream, filePath);
                } catch (IOException e) {
                    e.printStackTrace();
//                    java抛出异常的方法有很多，其中最常用的两个： System.out.println(e)，这个方法打印出异常，并且输出在哪里出现的异常. 不过它和另外一个e.printStackTrace()方法不同。后者也是打印出异常，但是它还将显示出更深的调用信息。
                }
                System.out.println("【成功保存图片】" + filePath + fileTypeInResponse);
                makeMsgForImg(null, null,imgNum);
            }
        });
    }

    public static InputStream inputStream;
    private void makeMsgForImg(String url,String filePath ,int imgNum){
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putString("filePath", filePath);
        bundle.putInt("imgNum", imgNum);
        message.what = API.S_BITMAP;
        if(url != null){message.what = API.F_BITMAP;}
        message.setData(bundle);
        handler.sendMessage(message);
    }


//    public void downImg(String url,String filePath ,final int imgNum){
//        String path = filePath.substring(filePath.lastIndexOf(File.separator));
//        String name = filePath.substring(filePath.lastIndexOf(File.separator),filePath.length());
//        OkHttpUtils
//                .get()
//                .url(url)
//                .build()
//                .execute(new FileCallBack(path, name)//
//                {
//                    @Override
//                    public void inProgress(float progress, long f) {
//                    }
//
//                    @Override
//                    public void onError(Call xx, Exception e) {
//                    }
//
//                    @Override
//                    public void onResponse(File file) {
//                        makeMsgForImg(null, null, imgNum);
//                    }
//                });
//    }



}
