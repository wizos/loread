package me.wizos.loread.net;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.exception.HttpException;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpParams;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.PostRequest;
import com.socks.library.KLog;

import java.io.IOException;

import me.wizos.loread.utils.Tool;
import okhttp3.FormBody;

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

    public String syncGet(String url, HttpParams httpParams, HttpHeaders httpHeaders) throws HttpException, IOException {
        KLog.e("开始同步网络" + url);
        GetRequest<String> get = OkGo.get(url);
        get.tag(url);
        get.params(httpParams);
        get.headers(httpHeaders);
        okhttp3.Response response = get.execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            throw new HttpException("");
        }
    }

    // 同步的获取数据
    public String syncPost(String url, FormBody.Builder bodyBuilder, HttpHeaders httpHeaders) throws HttpException, IOException {
        PostRequest<String> post = OkGo.post(url);
        post.tag(url);
        if (bodyBuilder != null) {
            post.upRequestBody(bodyBuilder.build());
        }
        post.headers(httpHeaders);
        okhttp3.Response response = post.execute();
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
                    Tool.showShort("asyncPost文章状态失败" + response.body());
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


//    public void exeRequest(Request request, StringCallback cb) {
//        KLog.e("执行exeRequest");
//        request.execute(cb);
//    }


//    public void asyncGetImg(OkHttpClient imgHttpClient, final Img img, FileCallback fileCallback) {
//        OkGo.<File>get(img.getSrc())
//                .tag(img.getArticleId())
//                .client(imgHttpClient)
//                .execute(fileCallback);
//    }

}


