package me.wizos.loread.extractor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;

import androidx.annotation.NonNull;

import com.elvishew.xlog.XLog;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.network.HttpClientManager;
import me.wizos.loread.utils.DataUtils;
import me.wizos.loread.utils.StringUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RSSSeeker {
    private static final String TAG = "RSSSeeker";
    private static final int TIMEOUT = 5_000; // 30 秒 30_000
    private String url;
    private Listener dispatcher;

    private OkHttpClient okHttpClient;
    private Handler handler;
    private Document document;

    private ArrayMap<String, String> rssMap = new ArrayMap<>();
    private ArrayMap<String, String> unKnowRSSMap = new ArrayMap<>();
    private String[] feedSuffix = {"feed", "rss", "rss.xml", "atom.xml", "feed.xml", "?feed=rss2", "?feed=rss"};


    public RSSSeeker(@NotNull String url, @NotNull Listener callback) {
        this.url = url;
        this.okHttpClient = HttpClientManager.i().searchClient();
        this.okHttpClient.dispatcher().setMaxRequests(10);
        this.dispatcher = new Listener() {
            @Override
            public void onResponse(ArrayMap<String, String> rssMap) {
                handler.removeMessages(TIMEOUT);
                optimizeRSSMap(); callback.onResponse(rssMap);
                handler.post(() -> destroy());
            }

            @Override
            public void onFailure(String msg) {
                handler.removeMessages(TIMEOUT);
                callback.onFailure(msg);
                handler.post(() -> destroy());
            }
        };
        this.handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                XLog.i("处理超时：" + msg.what + (msg.what != TIMEOUT));
                if(msg.what != TIMEOUT){
                    return false; //返回true 不对msg进行进一步处理
                }
                dispatcher.onFailure(App.i().getString(R.string.timeout));
                return true;
            }
        });
    }

    public void optimizeRSSMap(){
        if(rssMap.size() > 0){
            String title = document.title();
            for (Map.Entry<String,String> entry:rssMap.entrySet()) {
                if(StringUtils.isEmpty(entry.getValue())){
                    entry.setValue(title);
                }
            }
        }
        XLog.d("所有获得的 RSS：" + rssMap);
    }

    public void start() {
        XLog.i("开始用 OkHttp 获取全文：" + url );
        handler.sendEmptyMessageDelayed(TIMEOUT, TIMEOUT);
        okHttpClient.newCall(new Request.Builder().url(url).tag(TAG).build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                if(call.isCanceled()){
                    return;
                }
                XLog.d("OkHttp 获取失败");
                dispatcher.onFailure(App.i().getString(R.string.not_responding_plz_try_again));
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(call.isCanceled()){
                    return;
                }
                ResponseBody responseBody = response.body();
                XLog.d("OkHttp 获取成功：" + " = " );
                if( response.isSuccessful() && responseBody != null){
                    MediaType mediaType  = responseBody.contentType();
                    String charset = null;
                    if( mediaType != null ){
                        charset = DataUtils.getCharsetFromContentType(mediaType.toString());
                    }
                    document = Jsoup.parse(responseBody.byteStream(), charset, url);
                    find(document);
                }else {
                    dispatcher.onFailure(App.i().getString(R.string.not_responding_plz_try_again));
                }
                response.close();
            }
        });
    }


    public void find(Document doc){
        doc.outputSettings().prettyPrint(false);
        XLog.i("RSS Seeker：" + url );
        getFeedsFromHeader(doc);
        XLog.d("发现 RSSMap (Header)：" + rssMap);
        if(rssMap.size() > 0){
            dispatcher.onResponse(rssMap);
            return;
        }
        getFeedsFromBody(doc);
        XLog.d("发现 RSSMap (Body)：" + rssMap);
        if(rssMap.size() > 0){
            dispatcher.onResponse(rssMap);
            return;
        }
        guessFeedsByKeyword(doc);
        XLog.d("发现 UnKnowRSSMap：" + unKnowRSSMap);
        if(unKnowRSSMap.size() > 0){
            checkUnKnowRSSMap();
        }
    }

    private void getFeedsFromHeader(Document doc){
        String tmp;
        Elements elements;
        elements = doc.getElementsByTag("link");
        for (Element ele: elements) {
            tmp = ele.attr("type");
            if(Pattern.compile(".+/(rss|rdf|atom)$",Pattern.CASE_INSENSITIVE).matcher(tmp).find()){
                putMap(rssMap, tmp,ele.attr("title"));
            }else if(Pattern.compile("^text/xml$",Pattern.CASE_INSENSITIVE).matcher(tmp).find()){
                putMap(rssMap, tmp,ele.attr("title"));
            }
        }
    }

    private void getFeedsFromBody(Document doc){
        String tmp;
        Elements elements;
        elements = doc.select("a[href]");
        for (Element ele: elements) {
            tmp = ele.attr("href");
            if(Pattern.compile("^(https|http|ftp|feed).*([./]rss([./]xml|\\.aspx|\\.jsp|/)?$|/node/feed$|/feed(\\.xml|/$|$)|/rss/[a-z0-9]+$|[?&;](rss|xml)=|[?&;]feed=rss[0-9.]*$|[?&;]action=rss_rc$|feeds\\.feedburner\\.com/[\\w\\W]+$)",Pattern.CASE_INSENSITIVE).matcher(tmp).find()){
                putMap(rssMap, tmp, ele.ownText());
            }else if(Pattern.compile("^(https|http|ftp|feed).*/atom(\\.xml|\\.aspx|\\.jsp|/)?$|[?&;]feed=atom[0-9.]*$",Pattern.CASE_INSENSITIVE).matcher(tmp).find()){
                putMap(rssMap, tmp, ele.ownText());
            }else if(Pattern.compile("^(https|http|ftp|feed).*(/feeds?/[^./]*\\.xml$|.*/index\\.xml$|feed/msgs\\.xml(\\?num=\\d+)?$)",Pattern.CASE_INSENSITIVE).matcher(tmp).find()){
                putMap(rssMap, tmp, ele.ownText());
            }else if(Pattern.compile("^(https|http|ftp|feed).*\\.rdf$",Pattern.CASE_INSENSITIVE).matcher(tmp).find()){
                putMap(rssMap, tmp, ele.ownText());
            }else if(Pattern.compile("^(rss|feed)://",Pattern.CASE_INSENSITIVE).matcher(tmp).find()){
                putMap(rssMap, tmp, ele.ownText());
            }else if(Pattern.compile("^(https|http)://feed\\.",Pattern.CASE_INSENSITIVE).matcher(tmp).find()){
                putMap(rssMap, tmp, ele.ownText());
            }
        }
    }

    private void guessFeedsByKeyword(Document doc){
        String tmp;
        Elements elements;
        elements = doc.select("html > head > link");
        for (Element ele: elements) {
            tmp = ele.attr("href");
            if(Pattern.compile("wp-content",Pattern.CASE_INSENSITIVE).matcher(tmp).find()){
                putMap(unKnowRSSMap, tmp, ele.ownText());
            }else if(Pattern.compile("(bitcron\\.com|typecho\\.org|hexo\\.io)",Pattern.CASE_INSENSITIVE).matcher(tmp).find()){
                putMap(unKnowRSSMap, tmp, ele.ownText());
            }
        }
    }

    private void checkUnKnowRSSMap(){
        String url;
        URL uri;
        String domain;
        String protocol;
        for (Map.Entry<String,String> entry:unKnowRSSMap.entrySet()) {
            try {
                url = entry.getKey();
                uri = new URL(url);
                protocol = uri.getProtocol();
                domain = uri.getHost();
                if(url.contains(domain + "/index.php")){
                    domain = domain + "/index.php";
                }else if(url.contains(domain + "/blog")){
                    domain = domain + "/blog";
                }
                for (String suffix: feedSuffix) {
                    checkUrlWithFeedSuffix(protocol + "://" + domain+ "/" + suffix, entry.getValue());
                }
            } catch (IOException e){
                XLog.e("检查RSS链接报错：" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    private void checkUrlWithFeedSuffix(String url,String title){
        XLog.d("检查RSS链接：" + url );
        okHttpClient.newCall(new Request.Builder().url(url).tag(TAG).head().build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.code() == 200){
                    putMap(rssMap,url,title);
                }
                response.close();
            }
        });
    }

    private void putMap(ArrayMap<String,String> map, String url, String title){
        if(!map.containsKey(url.toLowerCase())){
            map.put(url,title);
        }
    }

    public void destroy(){
        if(handler != null){
            handler.removeMessages(TIMEOUT);
        }
        if (okHttpClient != null) {
            Iterator<Call> it;
            it = okHttpClient.dispatcher().queuedCalls().iterator();
            Call call;
            while(it.hasNext()) {
                call = it.next();
                call.cancel();
            }

            it = okHttpClient.dispatcher().runningCalls().iterator();
            while(it.hasNext()) {
                call = it.next();
                call.cancel();
            }
        }
    }
    public interface Listener {
        void onResponse(ArrayMap<String, String> rssMap);
        void onFailure(String msg);
        // void onTimeout();
        // void onNotResponse();
        // void onNoTextFound();
    }
}
