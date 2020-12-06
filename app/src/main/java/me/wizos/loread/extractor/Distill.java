package me.wizos.loread.extractor;

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
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.elvishew.xlog.XLog;
import com.just.agentweb.WebViewClient;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Selector;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.config.AdBlock;
import me.wizos.loread.config.ArticleExtractConfig;
import me.wizos.loread.config.LinkRewriteConfig;
import me.wizos.loread.config.article_extract_rule.ArticleExtractRule;
import me.wizos.loread.network.HttpClientManager;
import me.wizos.loread.utils.ArticleUtil;
import me.wizos.loread.utils.DataUtil;
import me.wizos.loread.utils.ScriptUtil;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.view.WebViewS;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static me.wizos.loread.Contract.HTTP;
import static me.wizos.loread.Contract.HTTPS;

public class Distill {
    private static final String TAG = "Distill";
    private static final int TIMEOUT = 15_000; // 30 秒 30_000
    private String url;
    private String keyword;
    private Listener dispatcher;

    private Call call;
    private WebViewS webViewS;
    private Handler handler;
    private Document document;

    private boolean isCancel = false;

    public Distill(@NotNull String url, @Nullable String keyword, @NotNull Listener callback) {
        this.url = url;
        this.keyword = keyword;
        this.dispatcher = new Listener() {
            @Override
            public void onResponse(String content) {
                handler.removeMessages(TIMEOUT);
                if(!isCancel) callback.onResponse(content);
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
                XLog.e("处理超时：" + msg.what);
                if(msg.what != TIMEOUT){
                    return false; //返回true 不对msg进行进一步处理
                }
                if(document == null){
                    dispatcher.onFailure(App.i().getString(R.string.timeout));
                }else {
                    try {
                        readability(document);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });
    }


    public void getContent(){
        handler.sendEmptyMessageDelayed(TIMEOUT, TIMEOUT);
        Request request = new Request.Builder().url(url).tag(TAG).build();
        call = HttpClientManager.i().simpleClient().newCall(request);

        XLog.d("开始用 OkHttp 获取全文");
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                XLog.d("OkHttp 获取全文失败，开始用 WebView 获取全文");
                dispatcher.onFailure(App.i().getString(R.string.not_responding));
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ResponseBody responseBody = response.body();
                if( response.isSuccessful() && responseBody != null){
                    MediaType mediaType  = responseBody.contentType();
                    String charset = null;
                    if( mediaType != null ){
                        charset = DataUtil.getCharsetFromContentType(mediaType.toString());
                    }
                    document = Jsoup.parse(responseBody.byteStream(), charset, url);
                    // document.getElementsByTag("script").remove();
                    XLog.d("OkHttp 获取全文成功：" + keyword + " = " +  document.body().html() );
                    if(!document.body().text().contains(keyword)){
                        getByWebView();
                    }else {
                        readability(document);
                    }
                }else {
                    dispatcher.onFailure(App.i().getString(R.string.not_responding));
                }
                response.close();
            }
        });
    }


    @SuppressLint("JavascriptInterface")
    private void getByWebView(){
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
                        XLog.d("WebView 获取全文成功：" + html );
                        if(!StringUtils.isEmpty(html)){
                            readability(Jsoup.parse(html, url));
                        }else {
                            dispatcher.onFailure(App.i().getString(R.string.not_responding));
                        }
                    }
                }, Bridge.TAG);
                webViewS.setWebViewClient(mWebViewClient);
                webViewS.loadUrl(url);
            }
        });
    }

    private void readability(Document doc) throws IOException{
        doc.outputSettings().prettyPrint(false);
        XLog.i("易读，keyword：" + keyword);
        ExtractPage extractPage =  getContentWithKeyword(url, doc, keyword);
        // XLog.e("获取易读，原文：" + content);
        if(!StringUtils.isEmpty(extractPage.getMsg())){
            dispatcher.onFailure( extractPage.getMsg() );
        }else if(StringUtils.isEmpty(extractPage.getContent())){
            dispatcher.onFailure( App.i().getString(R.string.no_text_found) );
        }else {
            dispatcher.onResponse( ArticleUtil.getOptimizedContent(url, extractPage.getContent()) );
        }
    }
    /*输入Jsoup的Document，获取正文文本*/
    public ExtractPage getContentWithKeyword(String url, Document doc, String keyword) throws MalformedURLException {
        URL uri = new URL(url);
        ArticleExtractRule rule;
        String content = null;
        ExtractPage extractPage = new ExtractPage();
        rule = ArticleExtractConfig.i().getRuleByDomain(uri.getHost());
        if(rule == null){
            rule = ArticleExtractConfig.i().getRuleByCssSelector(doc);
        }
        // XLog.i("抓取规则："  + uri.getHost() + " ==  " + rule );
        if(rule == null){
            Element newDoc = new Extractor(doc).getContentElementWithKeyword(keyword);
            if(newDoc == null){
                extractPage.setMsg(App.i().getString(R.string.no_text_found));
            }else {
                content = newDoc.html();
                if(StringUtils.isEmpty(content)){
                    extractPage.setMsg(App.i().getString(R.string.no_text_found));
                }else {
                    try {
                        ArticleExtractConfig.i().saveRuleByDomain(doc, uri, newDoc.cssSelector());
                    }catch (Selector.SelectorParseException | NullPointerException e){
                        e.printStackTrace();
                    }
                }
            }
        }else {
            content = getContentByRule(uri, doc, rule);
            if(StringUtils.isEmpty(content)){
                Element newDoc = new Extractor(doc).getContentElementWithKeyword(keyword);
                if(newDoc == null){
                    extractPage.setMsg(App.i().getString(R.string.no_text_found_by_rule_and_extractor, uri.getHost()));
                }else {
                    content = newDoc.html();
                    if(StringUtils.isEmpty(content)){
                        extractPage.setMsg(App.i().getString(R.string.no_text_found_by_rule_and_extractor, uri.getHost()));
                    }else {
                        try {
                            ArticleExtractConfig.i().saveRuleByDomain(doc, uri, newDoc.cssSelector());
                        }catch (Selector.SelectorParseException | NullPointerException e){
                            e.printStackTrace();
                        }
                    }
                }
                // extractPage.setMsg(App.i().getString(R.string.no_text_found_by_rule, uri.getHost()));
            }
        }
        extractPage.setContent(content);
        return extractPage;
    }

    private String getContentByRule(URL uri, Document doc, ArticleExtractRule rule) {
        if( !StringUtils.isEmpty(rule.getDocumentTrim()) ){
            Bindings bindings = new SimpleBindings();
            bindings.put("document", doc);
            bindings.put("uri", uri);
            ScriptUtil.i().eval(rule.getDocumentTrim(), bindings);
        }

        if( !StringUtils.isEmpty(rule.getContent()) ){
            // XLog.i("提取规则", "正文规则：" + rule.getContent() );
            Elements contentElements = doc.select(rule.getContent());
            if (!StringUtils.isEmpty(rule.getContentStrip())) {
                // XLog.i("提取规则", "正文过滤：" + rule.getContentStrip() );
                // 移除不需要的内容，注意规则为空
                contentElements.select(rule.getContentStrip()).remove();
            }

            if( !StringUtils.isEmpty(rule.getContentTrim()) ){
                Bindings bindings = new SimpleBindings();
                bindings.put("content", contentElements.html());
                ScriptUtil.i().eval(rule.getContentTrim(), bindings);
                // XLog.i("提取规则", "正文处理：" + rule.getContentTrim() );
                return (String)bindings.get("content");
            }
            return contentElements.html().trim();
        }
        return null;
    }




    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, final WebResourceRequest request) {
            String scheme = request.getUrl().getScheme();
            if (scheme.equalsIgnoreCase(HTTP) || scheme.equalsIgnoreCase(HTTPS)) {
                String url = request.getUrl().toString().toLowerCase();
                // XLog.e("重定向地址：" + url );
                // 有广告的请求数据，我们直接返回空数据，注：不能直接返回null
                if (AdBlock.i().isAd(url) || url.endsWith(".css")) { //
                    return new WebResourceResponse(null, null, null);
                }

                String newUrl = LinkRewriteConfig.i().getRedirectUrl(url);
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

    public void cancel(){
        isCancel = true;
        destroy();
    }


    public void destroy(){
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
        void onResponse(String content);
        void onFailure(String msg);
        // void onTimeout();
        // void onNotResponse();
        // void onNoTextFound();
    }

    public interface Bridge {
        String TAG = "ReadabilityBridge";
        String COMMEND = "javascript:ReadabilityBridge.getHtml(document.documentElement.outerHTML)";
        // void log(String msg);
        void getHtml(String html) throws IOException;
    }
}
