package me.wizos.loread.net;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.exception.HttpException;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.PostRequest;
import com.lzy.okgo.request.base.Request;
import com.socks.library.KLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import me.wizos.loread.bean.Img;
import me.wizos.loread.bean.RequestLog;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.utils.StringUtil;
import me.wizos.loread.utils.Tool;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;

/**
 * Created by Wizos on 2017/10/12.
 */

public class WithHttp {
    private static WithHttp withHttp;

    private WithHttp() {
    }

    public static WithHttp i() {
        if (withHttp == null) {
            synchronized (WithHttp.class) {
                if (withHttp == null) {
                    withHttp = new WithHttp();
                }
            }
        }
        return withHttp;
    }

    public void asyncGetImg(OkHttpClient imgHttpClient, final Img img, FileCallback fileCallback) {
        OkGo.<File>get(img.getSrc())
                .tag(img.getArticleId())
                .client(imgHttpClient)
                .execute(fileCallback);
    }

    public String syncGet(String url, HttpParams httpParams, HttpHeaders httpHeaders, NetCallbackS cb) throws HttpException, IOException {
        KLog.e("开始同步网络" + url);
        GetRequest<String> get = OkGo.get(url);
        get.tag(url);
        get.params(httpParams);
        get.headers(httpHeaders);
        okhttp3.Response response = get.execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            if (response.code() == 401) {
                throw new HttpException("401");
            }
            throw new HttpException("");
        }
    }

    // 同步的获取数据
    public String syncPost(String url, FormBody.Builder bodyBuilder, HttpHeaders httpHeaders, NetCallbackS cb) throws HttpException, IOException {
        PostRequest<String> post = OkGo.post(url);
        post.tag(url);
        if (bodyBuilder != null) {
            post.upRequestBody(bodyBuilder.build());
        }
        post.headers(httpHeaders);
        okhttp3.Response response = post.execute();

        if (response.code() == 401) {
            throw new HttpException("401");
        }
        return response.body().string();
    }


    public void asyncGet(String url, HttpParams httpParams, HttpHeaders httpHeaders, StringCallback cb) {
        GetRequest<String> get = OkGo.get(url);
        get.tag(url);
        get.params(httpParams);
        get.headers(httpHeaders);
        get.execute(cb);
    }

    public void asyncPost(String url, FormBody.Builder bodyBuilder, HttpHeaders httpHeaders, StringCallback cb) {
        if (cb == null) {
            cb = new StringCallback() {
                @Override
                public void onSuccess(Response<String> response) {
                    if (!response.body().equals("OK")) {
                        this.onError(response);
                    }
                }

                @Override
                public void onError(Response<String> response) {
                    Tool.showShort("同步文章状态失败A" + response.body());
                }
            };
        }
        PostRequest<String> post = OkGo.post(url);
        post.tag(url);
        if (bodyBuilder != null) {
            post.upRequestBody(bodyBuilder.build());
        }
        post.headers(httpHeaders);
        post.execute(cb);
    }


    public void exeRequest(Request request, StringCallback cb) {
        KLog.e("执行exeRequest");
        request.execute(cb);
    }


//    /**
//     * 保存正在下载或等待下载的URL和相应失败下载次数（初始为0），防止滚动时多次下载
//     */
//    private ArrayMap<String, String> taskMap;
//
//    public int downImgs(OkHttpClient imgHttpClient, String articleID, List<Img> imgList, String parentPath) {
//        if (WithSet.i().isDownImgWifi() && !HttpUtil.isWiFiActive()) {
//            return 0;
//        } else if (!WithSet.i().isDownImgWifi() && !HttpUtil.isNetworkAvailable()) {
//            return 0;
//        }
//        for (Img img : imgList) {
//            downImgSrc(imgHttpClient, articleID, parentPath, img);
//        }
//        KLog.e("批量下图片B：" + imgList.size());
//        return imgList.size();
//    }
//
//    public void downImgSrc(OkHttpClient imgHttpClient, final String articleID, final String parentPath, final Img img) {
//        OkGo.<File>get(img.getSrc())
//                .tag(articleID)
//                .client(imgHttpClient)
//                .execute(new FileCallback(parentPath, img.getName()) {
//                    @Override
//                    public void onSuccess(Response<File> response) {
////                        ToastUtil.showShort("图片文件保存成功" + parentPath + img.getName() );
//                        makeMsgForImg(articleID, img.getSrc(), img.getNo(), Api.S_BITMAP);
//                        KLog.e("onSuccess", "当前线程为：" + Thread.currentThread().getId() + Thread.currentThread().getName());
//                    }
//
//                    // 该方法执行在主线程中
//                    @Override
//                    public void onError(Response<File> response) {
//                        makeMsgForImg(articleID, img.getSrc(), img.getNo(), Api.F_BITMAP);
//                        KLog.e("onError", img.getSrc() + "当前线程为：" + Thread.currentThread().getId() + Thread.currentThread().getName());
//                    }
//                });
//    }

//    private void makeMsgForImg(String articleID, String imgSrc, int imgNo, int msg) {
//        Message message = new Message();
//        Bundle bundle = new Bundle();
//        bundle.putString("articleID", articleID);
//        bundle.putString("imgSrc", imgSrc);
////        bundle.putString("filePath", filePath);
//        bundle.putInt("imgNo", imgNo);
//        KLog.e("【】下载图片" + imgSrc + "==" + articleID);
//        message.what = msg;
//        message.setData(bundle);
//        taskMap.remove(imgSrc);
//    }


    // TODO: 2017/1/7  以一个接口形式做回调
    private Record record;

    public void setReord(Record record) {
        this.record = record;
    }

    public interface Record<T> {
        void log(T entry);
    }

    private RequestLog toRequest(String url, String method, long logTime, ArrayList<String[]> headParamList, ArrayList<String[]> bodyParamList) {
        String headParamString = StringUtil.formParamListToString(headParamList);
        String bodyParamString = StringUtil.formParamListToString(bodyParamList);
        return new RequestLog(logTime, url, method, headParamString, bodyParamString);
    }


    private void logRequest(Request request) {
        Gson gson = new Gson();
        String requestJson = "";
        if (request instanceof GetRequest) {
            requestJson = gson.toJson(request);
        }

        WithDB.i().saveRequestJSON(requestJson);
    }


//    public String syncGet2( String url, ArrayMap<String,String> params, ArrayMap<String,String> header, NetCallbackS cb ) throws ExceptionS,IOException{
//        return syncGet(url,params,header,cb).body().string();
//    }


    //    // 同步的获取数据
//    private void syncGet(String url, ArrayMap<String,String> params, HttpHeaders httpHeaders, NetCallbackS cb) {
//        HttpParams httpParams = new HttpParams();
//        if( params!=null ){
//            for( Map.Entry<String, String> entry: params.entrySet()) {
//                httpParams.put(entry.getKey(),entry.getValue(), false);
//            }
//        }
//        syncGet(url,httpParams, httpHeaders, cb);
//    }
//
//    // 同步的获取数据
//    private void syncPost(String url, ArrayMap<String,String> body, HttpHeaders httpHeaders, NetCallbackS cb) {
//        // 创建一个FormBody.Builder
//        FormBody.Builder bodyBuilder = new FormBody.Builder();
//        if( body!=null ){
//            for( Map.Entry<String, String> entry: body.entrySet()) {
//                bodyBuilder.add(entry.getKey(), entry.getValue());
//            }
//        }
//        syncPost( url, bodyBuilder, httpHeaders, cb);
//    }
}


