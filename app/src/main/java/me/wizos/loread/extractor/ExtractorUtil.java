// package me.wizos.loread.extractor;
//
// import com.hjq.toast.ToastUtils;
// import com.socks.library.KLog;
//
// import org.jsoup.nodes.Document;
// import org.jsoup.nodes.Element;
// import org.jsoup.select.Elements;
// import org.jsoup.select.Selector;
//
// import java.net.MalformedURLException;
// import java.net.URL;
//
// import javax.script.Bindings;
// import javax.script.SimpleBindings;
//
// import me.wizos.loread.App;
// import me.wizos.loread.R;
// import me.wizos.loread.config.ArticleExtractConfig;
// import me.wizos.loread.config.article_extract_rule.ArticleExtractRule;
// import me.wizos.loread.utils.ScriptUtil;
// import me.wizos.loread.utils.StringUtils;
//
// public class ExtractorUtil {
//     /*输入Jsoup的Document，获取正文文本*/
//     // public static String getContent(String url, Document doc) throws MalformedURLException {
//     //     URL uri = new URL(url);
//     //     ArticleExtractRule rule;
//     //     String content;
//     //     rule = ArticleExtractConfig.i().getRuleByDomain(uri.getHost());
//     //     if(rule == null){
//     //         rule = ArticleExtractConfig.i().getRuleByCssSelector(doc);
//     //     }
//     //     KLog.e("抓取规则："  + uri.getHost() + " ==  " + rule);
//     //     if(rule != null){
//     //         content = getContentByRule(uri, doc, rule);
//     //         if(!StringUtils.isEmpty(content)){
//     //             return content;
//     //         }else {
//     //             ToastUtils.show(App.i().getString(R.string.the_rule_of_full_text_extraction_has_expired, uri.getHost()));
//     //         }
//     //     }
//     //     return getContentByExtractor(uri, doc);
//     // }
//     /*输入Jsoup的Document，获取正文文本*/
//     // private static String getContentByExtractor(URL uri, Document doc) { // throws Exception
//     //     Element newDoc = new Extractor(doc).getContentElement();
//     //     if (newDoc == null) {
//     //         return "";
//     //     }
//     //     try {
//     //         ArticleExtractConfig.i().saveRuleByDomain(doc, uri, newDoc.cssSelector());
//     //     }catch (Selector.SelectorParseException e){
//     //         e.printStackTrace();
//     //     }
//     //     return newDoc.html();
//     // }
//
//     /*输入Jsoup的Document，获取正文文本*/
//     public static String getContentWithKeyword(String url, Document doc, String keyword) throws MalformedURLException {
//         URL uri = new URL(url);
//         ArticleExtractRule rule;
//         String content;
//         rule = ArticleExtractConfig.i().getRuleByDomain(uri.getHost());
//         if(rule == null){
//             rule = ArticleExtractConfig.i().getRuleByCssSelector(doc);
//         }
//         KLog.e("抓取规则："  + uri.getHost() + " ==  " + rule );
//         if(rule == null){
//             Element newDoc = new Extractor(doc).getContentElementWithKeyword(keyword);
//             try {
//                 content = newDoc.html();
//                 ArticleExtractConfig.i().saveRuleByDomain(doc, uri, newDoc.cssSelector());
//             }catch (Selector.SelectorParseException | NullPointerException e){
//                 content = "";
//                 e.printStackTrace();
//             }
//         }else {
//             content = getContentByRule(uri, doc, rule);
//         }
//
//         if(StringUtils.isEmpty(content)){
//             ToastUtils.show(App.i().getString(R.string.the_rule_of_full_text_extraction_has_expired, uri.getHost()));
//         }
//         return content;
//     }
//
//     private static String getContentByRule(URL uri, Document doc, ArticleExtractRule rule) {
//         if( !StringUtils.isEmpty(rule.getDocumentTrim()) ){
//             Bindings bindings = new SimpleBindings();
//             bindings.put("document", doc);
//             bindings.put("uri", uri);
//             ScriptUtil.i().eval(rule.getDocumentTrim(), bindings);
//         }
//
//         if( !StringUtils.isEmpty(rule.getContent()) ){
//             KLog.i("提取规则", "正文规则：" + rule.getContent() );
//             Elements contentElements = doc.select(rule.getContent());
//             if (!StringUtils.isEmpty(rule.getContentStrip())) {
//                 KLog.i("提取规则", "正文过滤：" + rule.getContentStrip() );
//                 // 移除不需要的内容，注意规则为空
//                 contentElements.select(rule.getContentStrip()).remove();
//             }
//
//             if( !StringUtils.isEmpty(rule.getContentTrim()) ){
//                 Bindings bindings = new SimpleBindings();
//                 bindings.put("content", contentElements.html());
//                 ScriptUtil.i().eval(rule.getContentTrim(), bindings);
//                 KLog.i("提取规则", "正文处理：" + rule.getContentTrim() );
//                 return (String)bindings.get("content");
//             }
//             return contentElements.html().trim();
//         }
//         return null;
//     }
// }
