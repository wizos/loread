/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-04-17 05:45:07
 */

package me.wizos.loread.network;

import android.annotation.SuppressLint;
import android.content.MutableContextWrapper;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.elvishew.xlog.XLog;
import com.just.agentweb.WebViewClient;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.config.HostBlockConfig;
import me.wizos.loread.config.url_rewrite.UrlRewriteConfig;
import me.wizos.loread.utils.DataUtils;
import me.wizos.loread.utils.InputStreamCache;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.utils.UriUtils;
import me.wizos.loread.view.webview.WebViewS;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static me.wizos.loread.Contract.SCHEMA_HTTP;
import static me.wizos.loread.Contract.SCHEMA_HTTPS;

public class Getting {
    private static final String TAG = "GettingBridge";
    private static final int TIMEOUT = 30_000; // 30 秒 30_000
    private static final int CALL_SIZE = 2;
    private int callCount = 0;

    private String url;

    private String keyword;
    private Listener dispatcher;

    private WebViewS webViewS;

    private Call call;
    private Request request;

    private int policy = ONLY_OKHTTP;
    private Handler handler;
    private InputStreamCache inputStreamTemp;

    private boolean isCancel = false;
    private boolean isRunning = false;

    public static final int ONLY_OKHTTP = 0;
    public static final int ONLY_WEBVIEW = 1;
    public static final int BOTH_OKHTTP_FIRST = 3;
    public static final int BOTH_FAST = 4;
    public static final int BOTH_WITH_KEYWORD = 5;

    public Getting(@NotNull String url, @NotNull Listener callback) {
        this.url = url;
        this.dispatcher = new Listener() {
            @Override
            public void onResponse(InputStreamCache inputStreamCache) {
                handler.removeMessages(TIMEOUT);
                if(!isCancel) callback.onResponse(inputStreamCache);
                handler.post(() -> destroy());
            }

            @Override
            public void onFailure(String msg) {
                handler.removeMessages(TIMEOUT);
                if(!isCancel) callback.onFailure(msg);
                handler.post(() -> destroy());
            }
        };
        this.handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                XLog.w("处理超时：" + msg.what);
                if(msg.what != TIMEOUT){
                    return false; //返回true 不对msg进行进一步处理
                }
                if(inputStreamTemp == null){
                    dispatcher.onFailure(App.i().getString(R.string.timeout));
                }else {
                    dispatcher.onResponse(inputStreamTemp);
                }
                destroy();
                return true;
            }
        });
    }


    public Getting request(Request request){
        this.request = request;
        return this;
    }

    public Getting policy(int policy){
        this.policy = policy;
        return this;
    }

    public Getting keyword(String keyword){
        this.keyword = keyword;
        return this;
    }

    public void start(){
        isRunning = true;
        handler.sendEmptyMessageDelayed(TIMEOUT, TIMEOUT);
        if(!UriUtils.isHttpOrHttpsUrl(url)){
            dispatcher.onFailure(App.i().getString(R.string.article_link_is_wrong_with_reason, url));
            return;
        }

        if(policy == ONLY_OKHTTP){
            byOkHttp();
        }else if(policy == ONLY_WEBVIEW){
            byWebview();
        }else if(policy == BOTH_OKHTTP_FIRST){
            byOkHttp();
        }else if(policy == BOTH_WITH_KEYWORD){
            byOkHttp();
            byWebview();
        }else if(policy == BOTH_FAST){
            byOkHttp();
            byWebview();
        }
    }

    private void byOkHttp(){
        if(request == null){
            Request.Builder requestBuilder = new Request.Builder().url(url).tag(TAG);
            requestBuilder.header(Contract.USER_AGENT, WebSettings.getDefaultUserAgent(App.i()));
            request = requestBuilder.build();
        }
        call = HttpClientManager.i().searchClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                XLog.w("OkHttp 获取 Html 失败：" + e.getLocalizedMessage());
                if(call.isCanceled()){
                    return;
                }
                echo(false, null, e.getLocalizedMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                XLog.d("OkHttp 获取 Html 响应：" + response.code() + " , " + response.message());
                if(call.isCanceled()){
                    return;
                }
                ResponseBody responseBody = response.body();
                if(!response.isSuccessful()){
                    echo(false, null, StringUtils.isEmpty(response.message()) ? String.valueOf(response.code()): (response.code() + ", " + response.message()) );
                    return;
                }

                if(responseBody == null){
                    echo(false, null, App.i().getString(R.string.original_text_exception_plz_check));
                    return;
                }

                MediaType mediaType  = responseBody.contentType();
                String charset = null;
                if( mediaType != null ){
                    charset = DataUtils.getCharsetFromContentType(mediaType.toString());
                }
                InputStreamCache inputStreamCache = new InputStreamCache(responseBody.byteStream());
                if(StringUtils.isEmpty(charset)){
                    inputStreamCache.setCharset(charset);
                }
                echo(true, inputStreamCache, null);
                response.close();
            }
        });
    }
    @SuppressLint("JavascriptInterface")
    private void byWebview(){
        // 为了防止调用者在非主线程调用与webview必须在主线程运行冲突，故这里使用handler抛到主线程中。
        handler.post(new Runnable() {
            @Override
            public void run() {
                webViewS = new WebViewS(new MutableContextWrapper(App.i()));
                webViewS.getSettings().setLoadsImagesAutomatically(false);//设置自动加载图片
                webViewS.getSettings().setBlockNetworkImage(true);//设置网页在加载的时候暂时不加载图片
                webViewS.addJavascriptInterface(new Bridge() {
                    @JavascriptInterface
                    @Override
                    public void getHtml(String html) throws IOException {
                        XLog.d("WebView 获取html成功：" + StringUtils.isEmpty(html) );
                        // XLog.d( html );
                        if(StringUtils.isEmpty(html)){
                            echo(false, null, App.i().getString(R.string.content_of_feed_is_empty));
                        }else {
                            if(html.contains("webkit-xml-viewer-source-xml\"><rss")){
                                html = html.substring(html.indexOf("webkit-xml-viewer-source-xml\">") + 30, html.indexOf("</rss>") + 6);
                            }else if(html.contains("webkit-xml-viewer-source-xml\"><feed")){
                                html = html.substring(html.indexOf("webkit-xml-viewer-source-xml\">") + 30, html.indexOf("</feed>") + 7);
                            }else if(html.contains("webkit-xml-viewer-source-xml")){
                                Document document = Jsoup.parse(html);
                                html = document.getElementById("webkit-xml-viewer-source-xml").html();
                            }
                            echo(true, new InputStreamCache(new ByteArrayInputStream(html.getBytes())), null);
                        }
                    }
                }, TAG);
                webViewS.setWebViewClient(mWebViewClient);
                webViewS.loadUrl(url);
            }
        });
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, final WebResourceRequest request) {
            String url = request.getUrl().toString();
            if (!url.toLowerCase().startsWith(SCHEMA_HTTP) && !url.toLowerCase().startsWith(SCHEMA_HTTPS)) {
                return super.shouldInterceptRequest(view,request);

            }

            // XLog.e("重定向地址：" + url );
            // 有广告的请求数据，我们直接返回空数据，注：不能直接返回null
            if (HostBlockConfig.i().isAd(url)
                    || url.endsWith(".css")
                    || url.endsWith(".jpg")
                    || url.endsWith(".jpeg")
                    || url.endsWith(".png")
                    || url.endsWith(".bmp")
                    || url.endsWith(".webp")
                    || url.endsWith(".woff")
                    || url.endsWith(".ttf")
                    || url.endsWith(".css?")
                    || url.endsWith(".jpg?")
                    || url.endsWith(".jpeg?")
                    || url.endsWith(".png?")
                    || url.endsWith(".bmp?")
                    || url.endsWith(".webp?")
                    || url.endsWith(".woff?")
                    || url.endsWith(".ttf?")) {
                return new WebResourceResponse(null, null, null);
            }

            String newUrl = UrlRewriteConfig.i().getRedirectUrl(url);
            // XLog.i("重定向地址：" + url + " -> " + newUrl);
            if(!TextUtils.isEmpty(newUrl) && !url.equalsIgnoreCase(newUrl)){
                return super.shouldInterceptRequest(view, new WebResourceRequest() {
                    @Override
                    public Uri getUrl() {
                        return Uri.parse(newUrl);
                    }
                    @SuppressLint("NewApi")
                    @Override
                    public boolean isRedirect(){
                        return true;
                    }
                    @SuppressLint("NewApi")
                    @Override
                    public boolean isForMainFrame() {
                        return request.isForMainFrame();
                    }
                    @SuppressLint("NewApi")
                    @Override
                    public boolean hasGesture() {
                        return request.hasGesture();
                    }
                    @SuppressLint("NewApi")
                    @Override
                    public String getMethod() {
                        return request.getMethod();
                    }
                    @SuppressLint("NewApi")
                    @Override
                    public Map<String, String> getRequestHeaders() {
                        return request.getRequestHeaders();
                    }
                });
            }

            return super.shouldInterceptRequest(view, request);
        }
        @Override
        public void onPageStarted(WebView webView, String url, Bitmap favicon) {
            super.onPageStarted(webView, url, favicon);
        }
        @Override
        public void onPageFinished(WebView webView, String url) {
            super.onPageFinished(webView, url);
            webView.loadUrl(Bridge.COMMEND);
        }
    };

    private void echo(boolean success, InputStreamCache inputStreamCache, String msg){
        callCount ++;
        if(policy == ONLY_OKHTTP || policy == ONLY_WEBVIEW){
            if(success){
                dispatcher.onResponse(inputStreamCache);
            }else {
                dispatcher.onFailure(msg);
            }
        }else if(policy == BOTH_OKHTTP_FIRST){
            if(success){
                dispatcher.onResponse(inputStreamCache);
            }else if(callCount != CALL_SIZE){
                byWebview();
            }else {
                dispatcher.onFailure(msg);
            }
        }else if(policy == BOTH_WITH_KEYWORD){
            if(success){
                if(StringUtils.isEmpty(keyword)){
                    dispatcher.onResponse(inputStreamCache);
                }else if(inputStreamCache.getSting().contains(keyword)){
                    dispatcher.onResponse(inputStreamCache);
                }else if(callCount != CALL_SIZE){
                    inputStreamTemp = inputStreamCache;
                }else {
                    dispatcher.onResponse(inputStreamCache);
                }
            }else if(callCount == CALL_SIZE){
                dispatcher.onFailure(msg);
            }
        }else if(policy == BOTH_FAST){
            if(success){
                dispatcher.onResponse(inputStreamCache);
            }else if(callCount == CALL_SIZE){
                dispatcher.onFailure(msg);
            }
        }
    }


    public void cancel(){
        isCancel = true;
        destroy();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void destroy(){
        isRunning = false;
        if(handler != null){
            handler.removeMessages(TIMEOUT);
        }
        if(call != null){
            call.cancel();
        }
        if(webViewS != null){
            webViewS.destroy();
            webViewS = null;
        }
    }
    public interface Listener {
        void onResponse(InputStreamCache inputStreamCache);
        void onFailure(String msg);
    }

    public interface Bridge {
        String TAG = "GettingBridge";
        String COMMEND = "javascript:GettingBridge.getHtml(document.documentElement.outerHTML)";
        // void log(String msg);
        void getHtml(String html) throws IOException;
    }
}
