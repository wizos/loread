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
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.wizos.loread.bean.RequestLog;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.gson.SrcPair;
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
        getWithAuth(url);
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


//    //测试之用
//    public void getWithAuth3(String url, long logCode) {
//        KLog.d("【执行 getWithAuth 1 】" + url);
//        if(!isNetworkEnabled(context)){
//            handler.sendEmptyMessage(55);
//            return;}
//        Request.Builder builder = new Request.Builder();
//        builder.url(url);
//        addHeader("Authorization", API.INOREADER_ATUH);
//        addHeader("AppId", API.INOREADER_APP_ID);
//        addHeader("AppKey", API.INOREADER_APP_KEY);
//        for ( Parameter para : headParamList) {
//            builder.addHeader( para.getKey() , para.getValue() );
//        }
//        headParamList.clear();
//        Request request = builder.build();
//        forData(url, request,logCode);
//    }

    public void getWithAuth(String url) {
        KLog.d("【执行 getWithAuth 】" + url);
        if(!isNetworkEnabled(context)){
            headParamList.clear();
            handler.sendEmptyMessage(55);
            return;}

        Request.Builder builder = new Request.Builder();
        String paraString = "?";
        for ( String[] param : headParamList) {
            paraString = paraString + param[0] + "=" + param[1] + "&";
        }

        //just for InoreaderProxy
        if( WithSet.getInstance().isInoreaderProxy() ){
            paraString = paraString +  "action=" + convertGetUrlForProxy(url);
            url = API.proxySite;
        }

        headParamList.clear(); // headParamList = new ArrayList<>();
        if (paraString.equals("?")) {
            paraString = "";
        }
        url = url + paraString;
        builder.url(url)
                .addHeader("AppId", API.INOREADER_APP_ID)
                .addHeader("AppKey", API.INOREADER_APP_KEY);
        if( WithSet.getInstance().isInoreaderProxy() ){
            builder.addHeader("Auth", API.INOREADER_ATUH);
        }else {
            builder.addHeader("Authorization", API.INOREADER_ATUH);
        }
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
        toRequest(API.U_EDIT_TAG, "post", logTime, headParamList, bodyParamList);
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
        if(!isNetworkEnabled(context)){
            headParamList.clear();
            bodyParamList.clear();
            handler.sendEmptyMessage(55);
            return;}
        // 构建请求头
        Request.Builder headBuilder = new Request.Builder().url(url);
//        for ( Parameter para : headParamList) {
//            headBuilder.addHeader(para.getKey(), para.getValue());
//        }
        for ( String[] param : headParamList) {
            headBuilder.addHeader( param[0], param[1] );
        }
        headParamList.clear();

        // 构建请求体
        FormEncodingBuilder bodyBuilder = new FormEncodingBuilder();
        for ( String[] param : bodyParamList ) {
            bodyBuilder.add( param[0], param[1] );
//            KLog.d("【2】" + param[0] + param[1]);
        }
        bodyParamList.clear();
        //just for InoreaderProxy
        if( WithSet.getInstance().isInoreaderProxy() ){
            bodyBuilder.add( "action", convertPostUrlForProxy(url) );
            url = API.proxySite;
        }
        RequestBody body = bodyBuilder.build();
        Request request = headBuilder.post(body).build();
        forData(url, request, logTime);
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
    public void forData(final String url, final Request request ,final long logTime) {
        if( !isNetworkEnabled(context) ){
            handler.sendEmptyMessage(55);
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
                        makeMsg(API.FAILURE_Request, url, "noRequest",logTime);
                    }
                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            KLog.d("【响应失败】" + response.message());
                            API.request = request;
                            makeMsg(API.FAILURE_Response, url, response.message(),logTime);
                            return;
                        }
                        try {
                            String res = response.body().string();
                            KLog.d("【forData】" + res.length());
                            makeMsg( API.url2int(url), url, res,logTime);
                        }catch (IOException e){
                            KLog.d("【超时】");
                            API.request = request;
                            makeMsg( API.FAILURE_Response, url, response.message(), logTime);
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
// 有些可能 url 一样，但头部不一样。而我又是靠 url 来区分请求，从而进行下一步的。
//        if (res.equals("noRequest")) {
//            message.what = API.FAILURE_Request;
//        } else if (res.equals("noResponse")) {
//            message.what = API.FAILURE_Response;
//        } else {
//            bundle.putString("res", res);
//            message.what = API.url2int(url);
//        }
        message.what = msgCode;
        bundle.putString("res", res);
        message.setData(bundle);
        handler.sendMessage(message);
        KLog.d("【getData】" + url   + " -- ");
    }


//    public interface Call{
//        void onSuccess(long logTime);
//    }
//    public void post(final String url,final long logTime,final Call call) { // just for login
//        KLog.d("【执行 = " + url + "】");
//        if(!isNetworkEnabled(context)){
//            headParamList.clear();
//            bodyParamList.clear();
//            handler.sendEmptyMessage(55);
//            return;}
//        // 构建请求头
//        Request.Builder headBuilder = new Request.Builder().url(url);
//        for ( Parameter para : headParamList) {
//            headBuilder.addHeader(para.getKey(), para.getValue());
//        }
//        headParamList.clear();
//
//        // 构建请求体
//        FormEncodingBuilder bodyBuilder = new FormEncodingBuilder();
//        for ( Parameter para : bodyParamList ) {
//            bodyBuilder.add( para.getKey(), para.getValue());
//        }
//        bodyParamList.clear();
//
//        RequestBody body = bodyBuilder.build();
//        final Request request = headBuilder.post(body).build();
//
//        if( !isNetworkEnabled(context) ){
//            handler.sendEmptyMessage(55);
//            return ;}
//        KLog.d("【开始请求】 "+ logTime + url);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                HttpUtil.enqueue(request, new Callback() {
//                    @Override
//                    public void onFailure(Request request, IOException e) {
//                        KLog.d("【请求失败】" + url );
//                        API.request = request;
//                        makeMsg(url, "noRequest",logTime);
//                    }
//                    @Override
//                    public void onResponse(Response response) throws IOException {
//                        if (!response.isSuccessful()) {
//                            KLog.d("【响应失败】" + response);
//                            API.request = request;
//                            makeMsg(url, "noResponse",logTime);
//                            return;
//                        }
//                        try {
//                            String res = response.body().string();
//                            call.onSuccess(logTime);
//                            makeMsg(url, res,logTime);
//                        }catch (SocketTimeoutException e) {
//                            KLog.d("【超时】");
//                            API.request = request;
//                            makeMsg(url, "noResponse", logTime);
//                            e.printStackTrace();
//                        }
//
//                    }
//                });
//            }
//        }).start();
//    }



//    public static class Builder {
//        private ArrayList<String[]> headParamList;
//        private ArrayList<String[]> bodyParamList;
//        private Parameter parameter;
//
//        public Builder(ArrayList<String[]> headParams,ArrayList<String[]> bodyParams){
//            headParamList = headParams;
//            bodyParamList = bodyParams;
//        }
//        public Builder addHeader(ArrayList<String[]> params){
//            headParamList.addAll(params);
//            return this;
//        }
//        public Builder addHeader(String[] params){
//            headParamList.add(params);
//            return this;
//        }
//        public Builder addHeader(String key ,String value){
//            String[] params = new String[2];
//            params[0] = key;
//            params[1] = value;
//            headParamList.add(params);
//            return this;
//        }
//        public Builder addBody(ArrayList<String[]> params){
//            bodyParamList.addAll(params);
//            return this;
//        }
//        public Builder addBody(String[] params){
//            bodyParamList.add(params);
//            return this;
//        }
//        public Builder addBody(String key ,String value){
//            String[] params = new String[2];
//            params[0] = key;
//            params[1] = value;
//            bodyParamList.add(params);
//            return this;
//        }
//        public ArrayList<ArrayList> build() {
//            ArrayList<ArrayList> arrayList = new ArrayList<>();
//            arrayList.add(headParamList);
//            arrayList.add(bodyParamList);
//            return arrayList;
//        }
//    }


//    private ArrayList<Parameter> headParamList = new ArrayList<>();
//    private ArrayList<Parameter> bodyParamList = new ArrayList<>();
//    private ArrayList<String[]> paramList = new ArrayList<>();

    private ArrayList<String[]> headParamList = new ArrayList<>();
    private ArrayList<String[]> bodyParamList = new ArrayList<>();

    private Parameter parameter;
    public void addHeader(String key ,String value){
//        parameter = new Parameter(key,value);
//        headParamList.add(parameter);
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
//        parameter = new Parameter(key,value);
//        bodyParamList.add(parameter);
        String[] params = new String[2];
        params[0] = key;
        params[1] = value;
        bodyParamList.add(params);
    }
    public void addBody(ArrayList<String[]> params){
        bodyParamList.addAll(params);
    }
    private class Parameter {
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



    public void toRequest(String url, String method, long logTime, ArrayList<String[]> headParamList, ArrayList<String[]> bodyParamList){
        String headParamString = UString.formParamListToString(headParamList);
        String bodyParamString = UString.formParamListToString(bodyParamList);
        RequestLog requests = new RequestLog(logTime,url,method,headParamString,bodyParamString);
        if(logRequest==null){return;}
        logRequest.add(requests);
    }

//    private void toRequest(String url, String method,long logTime, ArrayList<Parameter> headParamList, ArrayList<Parameter> bodyParamList){
////        String file = "{\n\"items\": [{\n\"url\": \"" + url + "\",\n\"method\": \"" + method  + "\",";
//
//        StringBuilder sbHead = new StringBuilder("");
//        StringBuilder sbBody = new StringBuilder("");
//        if( headParamList!=null){
//            if(headParamList.size()!=0){
//                for(Parameter param:headParamList){
//                    sbHead.append(param.getKey() + ":" + param.getValue() + ",");
//                }
//                sbHead.deleteCharAt(sbHead.length() - 1);
//            }
//        }
//        if( bodyParamList!=null ){
//            if( bodyParamList.size()!=0 ){
//                for(Parameter param:bodyParamList){
//                    sbBody.append(param.getKey() + ":" + param.getValue() + ",");
//                }
//                sbBody.deleteCharAt(sbBody.length() - 1);
//            }
//        }
//        RequestLog requests = new RequestLog(url,method,logTime,sbHead.toString(),sbBody.toString());
//        logRequest.addRequest(requests);
//        KLog.d("【添加请求1】" + sbHead.toString());
//        KLog.d("【添加请求2】" + sbBody.toString());
//    }

    public void setLogRequestListener(Loger logRequest) {
        this.logRequest = logRequest;
    }
    private Loger logRequest ;
    public interface Loger<T> {
        void add(T entry);
        void del(long index);
    }




    public int getBitmapList(HashMap<Integer, SrcPair> imgSrcList){
        if(!isWifiEnabled(context)){
            handler.sendEmptyMessage(55);
            return 0;}
        if(imgSrcList == null || imgSrcList.size()==0){
            return 0;
        }
        int num = imgSrcList.size();
//        for(int i=1;i<num;i++){
//            getBitmap(imgSrcList.get(i).getNetSrc(), imgSrcList.get(i).getSaveSrc(), i );
//        }
//        SrcPair src;
        for(Map.Entry<Integer, SrcPair> entry: imgSrcList.entrySet()){
            getBitmap(entry.getValue().getNetSrc(), entry.getValue().getSaveSrc(),entry.getKey() );
            KLog.d("【获取图片数量为：" + entry.getKey() );
        }

        return num;
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
                                UFile.saveFromStream(inputStream, filePath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }finally {
                                response.body().close();
                            }
                            KLog.d("【成功保存图片】" + url + "==" + filePath);
                            makeMsgForImg(API.S_BITMAP, url, filePath,imgNo);
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


//    public static InputStream inputStream;
    private void makeMsgForImg(int msg, String url,String filePath ,int imgNo){
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putString("filePath", filePath);
        bundle.putInt("imgNo", imgNo);
        message.what = msg;
//        if(url != null){ message.what = API.F_BITMAP;}
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
