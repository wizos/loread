package me.wizos.loread.net;

import android.content.Context;
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

import me.wizos.loread.bean.RequestLog;
import me.wizos.loread.bean.gson.SrcPair;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.utils.HttpUtil;
import me.wizos.loread.utils.UFile;
import me.wizos.loread.utils.UString;

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
        addHeader("n","160");
        addHeader("ot","0");
        addHeader("xt","user/"+ mUserID+"/state/com.google/read");
        addHeader("s", "user/" + mUserID + "/state/com.google/reading-list");
        getWithAuth( API.U_ITEM_IDS );
    }

    public void getStarredRefs( long mUserID){
        addHeader("n","160");
        addHeader("ot","0");
        addHeader("s", "user/" + mUserID + "/state/com.google/starred");
        getWithAuth( API.U_ITEM_IDS );
    }
    public void getStarredContents(){
        addHeader("n","20");
        addHeader("ot", "0");
        getWithAuth(API.U_STREAM_CONTENTS + API.U_STARRED);
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
        postWithAuthLog(API.U_EDIT_TAG);
    }
    public void postAddArticleTags( String articleID ,String tagId ){
        addBody("a", tagId );
        addBody("i", articleID);
        postWithAuthLog(API.U_EDIT_TAG);
    }
    public void postArticleContents( String articleID ){
        addBody("i", articleID);
        postWithAuthLog(API.U_ARTICLE_CONTENTS);
    }
    public void postUnReadArticle( String articleID ){
        addBody("r", "user/-/state/com.google/read");
        addBody("i", articleID);
        postWithAuthLog(API.U_EDIT_TAG);
        KLog.d("【post】1记录 "+ headParamList.size());
    }
    public void postReadArticle( String articleID ){
        addBody("a", "user/-/state/com.google/read");
        addBody("i", articleID);
        postWithAuthLog(API.U_EDIT_TAG);
        KLog.d("【post】2记录 "+ headParamList.size());
    }
    public void postUnStarArticle( String articleID ){
        addBody("r", "user/-/state/com.google/starred");
        addBody("i", articleID);
        postWithAuthLog(API.U_EDIT_TAG);
    }
    public void postStarArticle( String articleID ){
        addBody("a", "user/-/state/com.google/starred");
        addBody("i", articleID);
        postWithAuthLog(API.U_EDIT_TAG);
    }



    public void getWithAuth(String url) {
        KLog.d("【执行 getWithAuth 】" + url);
        if(!HttpUtil.isNetworkEnabled(context)){
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

        //just for InoreaderProxy
        if( WithSet.getInstance().isInoreaderProxy() ){
            paraString = paraString +  "action=" + convertGetUrlForProxy(url);
            url = API.proxySite;
        }

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
        if( WithSet.getInstance().isInoreaderProxy() ){
            addHeader("Auth", API.INOREADER_ATUH);
        }else {
            addHeader("Authorization", API.INOREADER_ATUH);
        }
        long logTime = System.currentTimeMillis();

        if(requestlogger!=null){
            requestlogger.logRequest( toRequest(API.U_EDIT_TAG, "post", logTime, headParamList, bodyParamList) );
        }
        post(url,logTime);
    }
    public void postWithAuth(final String url) {
        addHeader("AppId", API.INOREADER_APP_ID);
        addHeader("AppKey", API.INOREADER_APP_KEY);
        if( WithSet.getInstance().isInoreaderProxy() ){
            addHeader("Auth", API.INOREADER_ATUH);
        }else {
            addHeader("Authorization", API.INOREADER_ATUH);
        }
        post(url,System.currentTimeMillis());
    }
    public void post( String url, long logTime) { // just for login
        KLog.d("【执行 = " + url + "】");
        if(!HttpUtil.isNetworkEnabled(context)){
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

        //just for InoreaderProxy
        if( WithSet.getInstance().isInoreaderProxy() ){
            bodyBuilder.add( "action", convertPostUrlForProxy(url) );
            url = API.proxySite;
        }
        RequestBody body = bodyBuilder.build();
        Request request = headBuilder.post(body).build();
        forData(url, request, logTime);
    }



    public void forData(final String url, final Request request ,final long logTime) {
        if( !HttpUtil.isNetworkEnabled(context) ){
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
                        }finally {
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

    private void makeMsg( int msgCode, String url, String res, long logTime) {
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putLong("logTime",logTime);

        message.what = msgCode;
        bundle.putString("res", res);
        message.setData(bundle);
        handler.sendMessage(message);
        KLog.d("【getData】" + url   + " -- " + res );
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



    public int getBitmapList(ArrayMap<Integer,SrcPair> imgSrcList){
        if(!HttpUtil.isWifiEnabled(context)){
            handler.sendEmptyMessage(API.F_NoMsg);
            return 0;}
        if(imgSrcList == null || imgSrcList.size()==0){
            return 0;
        }
        int length = imgSrcList.size();
        for(ArrayMap.Entry<Integer, SrcPair> entry: imgSrcList.entrySet()){
            getBitmap(entry.getValue().getNetSrc(), entry.getValue().getSaveSrc(),entry.getKey() );
            KLog.d("【获取图片的key为：" + entry.getKey() );
        }
        return length;
    }
    public void getBitmap(final String url ,final String filePath ,final int imgNo) {
        KLog.d("【获取图片 " + url + "】" + filePath );
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        final Request request = builder.build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpUtil.enqueue(request, new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        KLog.d("【图片请求失败 = " + url + "】");
                        makeMsgForImg(API.F_BITMAP, url, filePath ,imgNo );
                    }
                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            KLog.d("【图片响应失败】" + response);
                            makeMsgForImg(API.F_BITMAP,url, filePath ,imgNo);
                        }else {
                            InputStream inputStream = null;
                            int state = API.S_BITMAP;
                            try {
//                            is = response.body.byteStream();
//                            is.reset();
//                            BitmapFactory.Options ops = new BitmapFactory.Options();
//                            ops.inJustDecodeBounds = false;
//                            final Bitmap bm = BitmapFactory.decodeStream(is, null, ops);
//                            mDelivery.post(new Runnable() {
//                                @Override
//                                public void run() {
//                                    callBack.onResponse(bm);
//                                }
//                            })
                                inputStream = response.body().byteStream();
                                if( UFile.saveFromStream(inputStream, filePath) ){
                                    state = API.F_BITMAP;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                response.body().close();
                            }

                            KLog.d("【成功保存图片】" + url + "==" + filePath);
                            makeMsgForImg(state, url, filePath,imgNo);
                        //得到响应内容的文件格式
//                        String fileTypeInResponse = "";
//                        MediaType mediaType = response.body().contentType();
//                        if (mediaType != null) {
//                            fileTypeInResponse = "." + mediaType.subtype();
//                        }
                        }
                    }
                });

            }
        }).start();
    }


    private void makeMsgForImg(int msg, String url,String filePath ,int imgNo){
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putString("filePath", filePath);
        bundle.putInt("imgNo", imgNo);
        message.what = msg;
        message.setData(bundle);
        handler.sendMessage(message);
    }



    private String convertPostUrlForProxy( String url ){
        String action = "";
        if(url.equals(API.U_CLIENTLOGIN)){
            action = "login";
        }else if( url.equals(API.U_ITEM_CONTENTS) ){
            action = "item_contents";
        }else if( url.equals(API.U_EDIT_TAG) ){
            action = "edit_tag";
        }
        return action;
    }
    private String convertGetUrlForProxy( String url ){
        String action = "";
        if(url.equals(API.U_USER_INFO)){
            action = "user_info";
        }else if( url.equals(API.U_TAGS_LIST) ){
            action = "tag_list";
        }else if( url.equals(API.U_STREAM_PREFS) ){
            action = "stream_prefs";
        }else if( url.equals(API.U_SUSCRIPTION_LIST) ){
            action = "suscription_list";
        }else if( url.equals(API.U_UNREAD_COUNTS) ){
            action = "unread_counts";
        }else if( url.equals(API.U_ITEM_IDS) ){
            action = "item_ids";
        }else if( url.equals(API.U_ARTICLE_CONTENTS) ){
            action = "article_contents";
        }else if( url.equals(API.U_STREAM_CONTENTS) ){
            action = "stream_contents";
        }
        return action;
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
