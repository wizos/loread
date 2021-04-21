package me.wizos.loread.extractor;

import com.elvishew.xlog.XLog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.config.article_extract.ArticleExtractConfig;
import me.wizos.loread.config.article_extract.ArticleExtractRule;
import me.wizos.loread.log.Console;
import me.wizos.loread.network.Getting;
import me.wizos.loread.utils.HttpCall;
import me.wizos.loread.utils.InputStreamCache;
import me.wizos.loread.utils.ScriptUtils;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.utils.UriUtils;

public class Distill {
    private String url;
    private String originalUrl;
    private String keyword;
    private Listener dispatcher;
    private Getting getting;



    public Distill(@NotNull String url, @Nullable String originalUrl, @Nullable String keyword, @NotNull Listener callback) {
        this.url = url;
        this.originalUrl = originalUrl;
        this.keyword = keyword;
        this.dispatcher = callback;
    }


    public void getContent(){
        if(!UriUtils.isHttpOrHttpsUrl(url)){
            dispatcher.onFailure(App.i().getString(R.string.article_link_is_wrong_with_reason, url));
            return;
        }

        getting = new Getting(url, new Getting.Listener() {
            @Override
            public void onResponse(InputStreamCache inputStreamCache) {
                XLog.w("OkHttp 获取全文成功：" + keyword + " = " );
                try {
                    Document doc = Jsoup.parse(inputStreamCache.getInputStream(), inputStreamCache.getCharset().displayName(), url);
                    doc.outputSettings().prettyPrint(false);
                    XLog.i("易读，keyword：" + keyword);
                    if(!StringUtils.isEmpty(originalUrl)){
                        url = originalUrl;
                    }
                    ExtractPage extractPage =  getArticles(url, doc, keyword);
                    // XLog.e("获取易读，原文：" + content);
                    if(!StringUtils.isEmpty(extractPage.getMsg())){
                        dispatcher.onFailure(extractPage.getMsg());
                    }else if(StringUtils.isEmpty(extractPage.getContent())){
                        dispatcher.onFailure( App.i().getString(R.string.no_text_found) );
                    }else {
                        dispatcher.onResponse(extractPage);
                    }
                }catch (IOException e){
                    XLog.e(e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String msg) {
                dispatcher.onFailure(msg);
            }
        });

        getting.policy(Getting.BOTH_WITH_KEYWORD);
        getting.keyword(keyword);
        getting.start();

        XLog.d("开始用 Getting 获取全文");
    }

    /*输入Jsoup的Document，获取正文文本*/
    public ExtractPage getArticles(String url, Document doc, String keyword) throws MalformedURLException {
        URL uri = new URL(url);
        ArticleExtractRule rule;
        String content = null;
        ExtractPage extractPage = null;
        rule = ArticleExtractConfig.i().getRule(uri.getHost(),doc);
        XLog.i("抓取规则："  + uri.getHost() + " ==  " + rule );
        try {
            if(rule == null){
                extractPage = new Extractor(doc).getNews(keyword);
                if(StringUtils.isEmpty(extractPage.getContent())){
                    extractPage.setMsg(App.i().getString(R.string.no_text_found));
                }else {
                    ArticleExtractConfig.i().saveRuleByHost(doc, uri, extractPage.getContentElement().cssSelector());
                }
            }else {
                content = getContentByRule(uri, doc, rule);
                extractPage = new Extractor(doc).getNews(keyword);
                XLog.d("获取到的内容：" + content);
                if(!StringUtils.isEmpty(content)){
                    extractPage.setContent(content);
                }
                if(StringUtils.isEmpty(extractPage.getContent())){
                    extractPage.setMsg(App.i().getString(R.string.no_text_found_by_rule_and_extractor, uri.getHost()));
                }
            }
        }catch (Exception e){
            XLog.e("异常：" + e.getMessage());
            e.printStackTrace();
            extractPage = new ExtractPage();
            extractPage.setMsg(App.i().getString(R.string.get_readability_failure_with_reason, e.getMessage()));
        }
        return extractPage;
    }

    private String getContentByRule(URL uri, Document doc, ArticleExtractRule rule) {
        if( !StringUtils.isEmpty(rule.getDocumentTrim()) ){
            Bindings bindings = new SimpleBindings();
            bindings.put("document", doc);
            bindings.put("uri", uri);
            bindings.put("call", HttpCall.i());
            bindings.put("console", new Console());
            ScriptUtils.i().eval(rule.getDocumentTrim(), bindings);
        }

        if( !StringUtils.isEmpty(rule.getContent()) ){
            XLog.d("提取规则 - 正文规则：" + rule.getContent() );
            Elements contentElements = doc.select(rule.getContent());
            if (!StringUtils.isEmpty(rule.getContentStrip())) {
                XLog.d("提取规则 - 正文过滤：" + rule.getContentStrip() );
                // 移除不需要的内容，注意规则为空
                contentElements.select(rule.getContentStrip()).remove();
            }

            if( !StringUtils.isEmpty(rule.getContentTrim()) ){
                Bindings bindings = new SimpleBindings();
                bindings.put("document", doc);
                bindings.put("uri", uri);
                bindings.put("content", contentElements.html());
                bindings.put("call", HttpCall.i());
                bindings.put("console", new Console());
                ScriptUtils.i().eval(rule.getContentTrim(), bindings);
                XLog.d("提取规则 - 正文处理：" + rule.getContentTrim() );
                return (String)bindings.get("content");
            }
            return contentElements.html().trim();
        }
        return null;
    }

    public void cancel(){
        if(getting != null){
            getting.destroy();
        }
    }
    public interface Listener {
        void onResponse(ExtractPage page);
        void onFailure(String msg);
    }
}
