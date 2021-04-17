package me.wizos.loread.config.url_rewrite;

import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import me.wizos.loread.App;
import me.wizos.loread.log.Console;
import me.wizos.loread.utils.FileUtils;
import me.wizos.loread.utils.HttpCall;
import me.wizos.loread.utils.JavaScriptCompressor;
import me.wizos.loread.utils.ScriptUtils;
import me.wizos.loread.utils.StringUtils;

/**
 * 作用：在进行网络请求前，修改被请求的网址。
 * 格式：内容为 json 形式的 key : value 列表。key 为 host，value 为 js 脚本。
 * 运行机制：请求的网址前，系统会先将网址的 host 以 "." 为节点，拆出多个 host，
 *          例如将 zelda.game.qq.com.cn，拆为 zelda.game.qq.com.cn, game.qq.com.cn, qq.com.cn, com.cn（不包含最后一个 "." 的后部分）。
 *          依次将这 4 个 host 在规则文件中查找是否有符合的 key，若有则执行对应的 value 中的 js 脚本。（若既有 key 为 game.qq.com，又有 qq.com 的规则，则只会命中前者）。
 *          在执行 js 脚本时，会传入 url 参数，以及 call 对象。可以调用 call.valid(newUrl) 方法验证 newUrl 是否有效（即是否成功响应）
 * @author Wizos on 2021/01/16.
 */
public class UrlRewriteConfig {
    public static final String FILENAME = "url_rewrite.json";
    private static UrlRewriteConfig instance;
    private static UrlRewriteRule rule;

    private UrlRewriteConfig() { }
    public static UrlRewriteConfig i() {
        if (instance == null) {
            synchronized (UrlRewriteConfig.class) {
                if (instance == null) {
                    instance = new UrlRewriteConfig();
                    rule = new UrlRewriteRule();
                    Gson gson = new Gson();
                    // if(CorePref.i().userPref().getBoolean(Contract.ENABLE_REMOTE_URL_REWRITE_RULE, false)){

                    // String includeConfig = FileUtils.readFileFromAssets(App.i(), "rule/" + FILENAME);
                    // if (!TextUtils.isEmpty(includeConfig)) {
                    //     UrlRewriteRule remoteRule = gson.fromJson(includeConfig, UrlRewriteRule.class);
                    //     rule.hostReplace.putAll(remoteRule.hostReplace);
                    //     rule.urlRewrite.putAll(remoteRule.urlRewrite);
                    // }

                    String remoteConfig = FileUtils.readFile(App.i().getGlobalConfigPath() + FILENAME);
                    if (!TextUtils.isEmpty(remoteConfig)) {
                        UrlRewriteRule remoteRule = gson.fromJson(remoteConfig, UrlRewriteRule.class);
                        // rule.hostBlock.addAll(remoteRule.hostBlock);
                        rule.hostReplace.putAll(remoteRule.hostReplace);
                        rule.urlRewrite.putAll(remoteRule.urlRewrite);
                    }

                    String userConfig = FileUtils.readFile(App.i().getUserConfigPath() + FILENAME);
                    if (!TextUtils.isEmpty(userConfig)) {
                        UrlRewriteRule userRule = gson.fromJson(userConfig, UrlRewriteRule.class);
                        // rule.hostBlock.addAll(userRule.hostBlock);
                        rule.hostReplace.putAll(userRule.hostReplace);
                        rule.urlRewrite.putAll(userRule.urlRewrite);
                    }
                }
            }
        }
        return instance;
    }
    public void reset() {
        instance = null;
    }
    // public void save() {
    //     FileUtil.save(App.i().getUserConfigPath() + CONFIG_FILENAME, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(instance));
    // }

    public boolean addRule(boolean isReplaceHost, String host, String value){
        if(StringUtils.isEmpty(value)){
            return false;
        }
        // value = SymbolUtils.filterLineSymbol(value);
        value = JavaScriptCompressor.compress(value);
        UrlRewriteRule userRule;
        String userConfig = FileUtils.readFile(App.i().getUserConfigPath() + FILENAME);
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        if (!TextUtils.isEmpty(userConfig)) {
            userRule = gson.fromJson(userConfig, UrlRewriteRule.class);
        }else {
            userRule = new UrlRewriteRule();
        }

        if(isReplaceHost){
            userRule.hostReplace.put(host,value);
        }else {
            userRule.urlRewrite.put(host,value);
        }
        boolean success = FileUtils.save(App.i().getUserConfigPath() + FILENAME, gson.toJson(userRule));
        reset();
        return success;
    }

    public String getValue(String key){
        if(rule == null){
            return null;
        }
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        if (rule.hostReplace.containsKey(key)) {
            return rule.hostReplace.get(key);
        }
        if (rule.urlRewrite.containsKey(key)) {
            return rule.urlRewrite.get(key);
        }
        return null;
    }

    public String getRedirectUrl(String url) {
        if(rule == null){
            return null;
        }
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        // String host = Uri.parse(url).getHost();

        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        // XLog.e("Host ：" + host );
        if (TextUtils.isEmpty(host)) {
            return null;
        }

        if (rule.hostReplace.containsKey(host)) {
            return url.replaceFirst(host, rule.hostReplace.get(host));
        }

        String schemeHost = uri.getScheme() + "://" + host;
        if (rule.urlRewrite.containsKey(schemeHost)) {
            Bindings bindings = new SimpleBindings();
            bindings.put("url", url);
            bindings.put("call", HttpCall.i());
            bindings.put("console", new Console());
            ScriptUtils.i().eval(rule.urlRewrite.get(schemeHost), bindings);
            // XLog.d("重定向JS：" + bindings.get("url"));
            return (String) bindings.get("url");
        }

        if(host.contains(".")){
            String[] slices = host.split("\\.");
            for (int i = 0, size = slices.length; i+1 < size; i++) {
                host = StringUtils.join(".", Arrays.copyOfRange(slices, i, size));
                // XLog.d("Host 推测：" + host );
                if (rule.urlRewrite.containsKey(host)) {
                    // Bindings接口可以理解为上下文，可以往上下文中设置一个Java对象或通过key获取一个对象，它有一个实现类，SimpleBindings，内部就是一个map。
                    Bindings bindings = new SimpleBindings();
                    bindings.put("url", url);
                    bindings.put("call", HttpCall.i());
                    bindings.put("console", new Console());
                    ScriptUtils.i().eval(rule.urlRewrite.get(host), bindings);
                    // XLog.d("重定向JS：" + bindings.get("url"));
                    return (String) bindings.get("url");
                }
            }
        }else if (rule.urlRewrite.containsKey(host)) {
            // Bindings接口可以理解为上下文，可以往上下文中设置一个Java对象或通过key获取一个对象，它有一个实现类，SimpleBindings，内部就是一个map。
            Bindings bindings = new SimpleBindings();
            bindings.put("url", url);
            bindings.put("call", HttpCall.i());
            bindings.put("console", new Console());
            ScriptUtils.i().eval(rule.urlRewrite.get(host), bindings);
            // XLog.d("重定向JS：" + bindings.get("url"));
            return (String) bindings.get("url");
        }
        return null;
    }


    public static List<String> getReduceSlice(String url){
        List<String> hostList = new ArrayList<>();

        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        hostList.add(uri.getScheme() + "://" + host);

        if(host.contains(".")){
            String[] slices = host.split("\\.");
            for (int i = 0, size = slices.length; i+1 < size; i++) {
                host = StringUtils.join(".", Arrays.copyOfRange(slices, i, size));
                hostList.add(host);
            }
        }else {
            hostList.add(host);
        }
        return hostList;
    }
}