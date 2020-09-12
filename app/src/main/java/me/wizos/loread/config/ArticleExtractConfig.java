package me.wizos.loread.config;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.socks.library.KLog;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.wizos.loread.App;
import me.wizos.loread.config.article_extract_rule.ArticleExtractRule;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.StringUtils;

public class ArticleExtractConfig {
    private transient static ArticleExtractConfig instance;
    private ArticleExtractConfig() { }
    public static ArticleExtractConfig i() {
        if (instance == null) {
            synchronized (ArticleExtractConfig.class) {
                if (instance == null) {
                    instance = new ArticleExtractConfig();
                    String json = FileUtil.readFile(App.i().getUserConfigPath() + "article_extract_rule.json");
                    if (TextUtils.isEmpty(json)) {
                        instance.pageMatchRegex = new ArrayMap<String, ArticleExtractRule>();
                        instance.pageMatchCssSelector = new ArrayMap<String, ArticleExtractRule>();
                        instance.save();
                    }else {
                        instance = new Gson().fromJson(json, ArticleExtractConfig.class);
                    }
                }
            }
        }
        return instance;
    }
    public void save() {
        FileUtil.save(App.i().getUserConfigPath() + "article_extract_rule.json", new GsonBuilder().setPrettyPrinting().create().toJson(instance));
    }
    public void reset() {
        instance = null;
    }


    @SerializedName("page_match_css_selector")
    private ArrayMap<String, ArticleExtractRule> pageMatchCssSelector;
    @SerializedName("page_match_regex")
    private ArrayMap<String, ArticleExtractRule> pageMatchRegex;

    public ArticleExtractRule getRuleByDomain(String domain){
        String rules = FileUtil.readFile(  App.i().getUserConfigPath() + "article_extract_rule/" + domain + ".json");
        KLog.e("获取到的抓取规则内容："  + domain + " ==  " + rules);
        if (!StringUtils.isEmpty(rules)) {
            return new Gson().fromJson(rules, ArticleExtractRule.class);
        }
        return null;
    }

//    public void invalidRuleByDomain(String domain){
//        File file = new File(App.i().getUserConfigPath() + "article_extract_rule/" + domain + ".json");
//        if(file.exists()){
//            file.renameTo(new File(App.i().getUserConfigPath() + "article_extract_rule_invalid/" + domain + ".json"));
//        }
//    }

    public void saveRuleByDomain(Document document, String domain,  String oriCssSelector){
        String optimizedCssSelector = optimizeCSSSelector(oriCssSelector);
        if (document.select(optimizedCssSelector).size() == 1) {
            saveSiteRule(domain, optimizedCssSelector);
        } else {
            saveSiteRule(domain, oriCssSelector);
        }
    }
    private static final String RE_RULE1 = " *(div|post|entry|article)(\\.[A-z0-9-_]+)*([.#])(entry|post|article)([-_])(content|article|body)([. ]|$)";
    private static final String RE_RULE2 = " *(div|post|entry|article)(\\.[A-z0-9-_]+)*([.#])(entry|post|article|content|body)([. ]|$)";
    private static String optimizeCSSSelector(String cssQuery) {
        Pattern pattern = Pattern.compile(RE_RULE1, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(cssQuery);
        if (matcher.find()) {
            return matcher.group(1) + matcher.group(3) + matcher.group(4) + matcher.group(5) + matcher.group(6);
        }
        pattern = Pattern.compile(RE_RULE2, Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(cssQuery);
        if (matcher.find()) {
            return matcher.group(1) + matcher.group(3) + matcher.group(4);
        }
        return cssQuery;
    }

    private static void saveSiteRule(String domain, String cssSelector) {
        ArticleExtractRule articleExtractRule = new ArticleExtractRule();
        articleExtractRule.setContent(cssSelector);

        Gson gson = new GsonBuilder()
                .setPrettyPrinting() //对结果进行格式化，增加换行
                .disableHtmlEscaping() //避免Gson使用时将一些字符自动转换为Unicode转义字符
                .create();
        FileUtil.save(App.i().getUserConfigPath() + "article_extract_rule/" + domain + "_new.json", gson.toJson(articleExtractRule, ArticleExtractRule.class));
    }


    public ArticleExtractRule getRuleByCssSelector(Document document){
        if(pageMatchCssSelector == null || document == null){
            return null;
        }
        Elements elements;
        for (Map.Entry<String, ArticleExtractRule> entry:pageMatchCssSelector.entrySet()) {
            elements = document.select(entry.getKey());
            if(elements != null && elements.size() > 0) {
                return entry.getValue();
            }
        }
        return null;
    }

    public ArticleExtractRule getRuleByRegex(String page){
        if(pageMatchRegex == null || StringUtils.isEmpty(page)){
            return null;
        }
        Pattern pattern;
        for (Map.Entry<String, ArticleExtractRule> entry:pageMatchRegex.entrySet()) {
            pattern = Pattern.compile(entry.getKey(),Pattern.CASE_INSENSITIVE);
            if(pattern.matcher(page).find()){
                return entry.getValue();
            }
        }
        return null;
    }
}
