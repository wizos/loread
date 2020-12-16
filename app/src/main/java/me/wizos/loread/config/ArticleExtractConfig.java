package me.wizos.loread.config;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.wizos.loread.App;
import me.wizos.loread.config.article_extract_rule.ArticleExtractRule;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.StringUtils;

public class ArticleExtractConfig {
    private static final String CONFIG_FOLDER = "article_extract_rule";
    private static final String CONFIG_FILENAME = "article_extract_rule.json";
    private transient static ArticleExtractConfig instance;
    private ArticleExtractConfig() { }
    public static ArticleExtractConfig i() {
        if (instance == null) {
            synchronized (ArticleExtractConfig.class) {
                if (instance == null) {
                    instance = new ArticleExtractConfig();
                    String json = FileUtil.readFile(App.i().getUserConfigPath() + CONFIG_FILENAME);
                    if (TextUtils.isEmpty(json)) {
                        instance.hostContainKeyword = new ArrayMap<>();
                        instance.pageMatchCssSelector = new ArrayMap<>();
                        instance.pageMatchRegex = new ArrayMap<>();
                        instance.save();
                    }else {
                        instance = new Gson().fromJson(json, ArticleExtractConfig.class);
                        instance.save();
                    }
                }
            }
        }
        return instance;
    }
    public void save() {
        FileUtil.save(App.i().getUserConfigPath() + CONFIG_FILENAME, new GsonBuilder().setPrettyPrinting().create().toJson(instance));
    }
    public void reset() {
        instance = null;
    }

    @SerializedName("host_contain_keyword")
    private ArrayMap<String, String> hostContainKeyword;
    @SerializedName("page_match_css_selector")
    private ArrayMap<String, String> pageMatchCssSelector;
    @SerializedName("page_match_regex")
    private ArrayMap<String, String> pageMatchRegex;

    public ArticleExtractRule getRule(String host, Document document){
        ArticleExtractRule rule = null;
        rule = getRuleByDomain(host);
        if(rule == null){
            String ruleFileName = getRuleFileNameByHost(host);
            if(StringUtils.isEmpty(ruleFileName)){
                ruleFileName = getRuleFileNameByCssSelector(document);
            }
            if(StringUtils.isEmpty(ruleFileName)){
                rule = getRuleByDomain(ruleFileName);
            }
        }
        return rule;
    }

    public ArticleExtractRule getRuleByDomain(String host){
        String rules = FileUtil.readFile(  App.i().getUserConfigPath() + CONFIG_FOLDER  + "/" + host + ".json");
        if (!StringUtils.isEmpty(rules)) {
            return new Gson().fromJson(rules, ArticleExtractRule.class);
        }
        return null;
    }

    // public String getRuleFileName(String url, Document document){
    //     String host = Uri.parse(url).getHost();
    //     String ruleFileName = getRuleFileNameByHost(host);
    //     if(StringUtils.isEmpty(ruleFileName)){
    //         ruleFileName = getRuleFileNameByCssSelector(document);
    //     }
    //     return ruleFileName;
    // }

    public String getRuleFileNameByHost(String host){
        if(hostContainKeyword == null || StringUtils.isEmpty(host)){
            return null;
        }

        for (Map.Entry<String, String> entry:hostContainKeyword.entrySet()) {
            if(host.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public String getRuleFileNameByCssSelector(Document document){
        if(pageMatchCssSelector == null || document == null){
            return null;
        }
        Elements elements;
        for (Map.Entry<String, String> entry:pageMatchCssSelector.entrySet()) {
            elements = document.select(entry.getKey());
            if(elements != null && elements.size() > 0) {
                return entry.getValue();
            }
        }
        return null;
    }

    public String getRuleFileNameByRegex(String page){
        if(pageMatchRegex == null || StringUtils.isEmpty(page)){
            return null;
        }
        Pattern pattern;
        for (Map.Entry<String, String> entry:pageMatchRegex.entrySet()) {
            pattern = Pattern.compile(entry.getKey(),Pattern.CASE_INSENSITIVE);
            if(pattern.matcher(page).find()){
                return entry.getValue();
            }
        }
        return null;
    }
    // public void invalidRuleByDomain(String domain){
    //     File file = new File(App.i().getUserConfigPath() + "article_extract_rule/" + domain + ".json");
    //     if(file.exists()){
    //         file.renameTo(new File(App.i().getUserConfigPath() + "article_extract_rule_invalid/" + domain + ".json"));
    //     }
    // }

    public void saveRuleByDomain(Document document, URL uri, String originalCssSelector){
        String optimizedCssSelector = optimizeCSSSelector(originalCssSelector);
        if (document.select(optimizedCssSelector).size() == 1) {
            saveSiteRule(uri, optimizedCssSelector);
        } else {
            saveSiteRule(uri, originalCssSelector);
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

    // private static void saveSiteRule2(URL uri, String cssSelector) {
    //     ArticleExtractRule articleExtractRule = new ArticleExtractRule();
    //     articleExtractRule.setContent(cssSelector);
    //
    //     Gson gson = new GsonBuilder()
    //             .setPrettyPrinting() //对结果进行格式化，增加换行
    //             .disableHtmlEscaping() //避免Gson使用时将一些字符自动转换为Unicode转义字符
    //             .create();
    //     FileUtil.save(App.i().getUserConfigPath() + CONFIG_FOLDER  + "/" + uri.getHost() + ".new", gson.toJson(articleExtractRule, ArticleExtractRule.class));
    // }

    private static void saveSiteRule(URL uri, String cssSelector) {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting() //对结果进行格式化，增加换行
                .disableHtmlEscaping() //避免Gson使用时将一些字符自动转换为Unicode转义字符
                .create();

        File logFile = new File(App.i().getUserConfigPath() + CONFIG_FOLDER  + "/" + uri.getHost() + ".log");
        ArrayMap<String,String> cssSelectorMap;
        if(logFile.exists()){
            cssSelectorMap = gson.fromJson(FileUtil.readFile(logFile), new TypeToken<ArrayMap<String, String>>() {}.getType());
        }else {
            cssSelectorMap = new ArrayMap<>();
        }
        cssSelectorMap.put(uri.toString(),cssSelector);

        if(cssSelectorMap.size() >= 4){
            Map<String,Integer> frequency = new ArrayMap<>();
            for (Map.Entry<String,String> entry:cssSelectorMap.entrySet()) {
                Integer times = frequency.get(entry.getValue());
                if(times == null){
                    frequency.put(entry.getValue(),1);
                }else {
                    frequency.put(entry.getValue(),times + 1);
                }
            }

            // 大部分历史cssSelector都是相同的，占比>0.5
            if ( (float)(frequency.size()/cssSelectorMap.size()) < 0.5){
                int max = 0;
                String maxCssSelector = null;

                for (Map.Entry<String,Integer> entry:frequency.entrySet()) {
                    if(entry.getValue() > max){
                        maxCssSelector = entry.getKey();
                        max = entry.getValue();
                    }
                }

                ArticleExtractRule articleExtractRule = new ArticleExtractRule();
                articleExtractRule.setContent(maxCssSelector);
                File file = new File(App.i().getUserConfigPath() + CONFIG_FOLDER  + "/" + uri.getHost() + ".json");
                if(file.exists()){
                    file.renameTo(new File(App.i().getUserConfigPath() + CONFIG_FOLDER  + "/" + uri.getHost() + ".old"));
                }
                FileUtil.save(App.i().getUserConfigPath() + CONFIG_FOLDER  + "/" + uri.getHost() + ".json", gson.toJson(articleExtractRule, ArticleExtractRule.class));
            }
        }

        FileUtil.save(logFile, gson.toJson(cssSelectorMap));
    }

}
