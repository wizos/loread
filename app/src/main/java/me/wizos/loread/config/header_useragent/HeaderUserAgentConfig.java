package me.wizos.loread.config.header_useragent;

import android.net.Uri;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;

import me.wizos.loread.App;
import me.wizos.loread.utils.FileUtils;
import me.wizos.loread.utils.StringUtils;

public class HeaderUserAgentConfig {
    public static final String FILE_NAME = "header_user_agent.json";

    private static HeaderUserAgentConfig instance;
    // private static UserAgentRule rule;
    private static ArrayMap<String, String> rule; // 格式是 domain, Referer

    private HeaderUserAgentConfig() {}
    public static HeaderUserAgentConfig i() {
        if (instance == null) {
            synchronized (HeaderUserAgentConfig.class) {
                if (instance == null) {
                    instance = new HeaderUserAgentConfig();
                    rule = new ArrayMap<String,String>();
                    Gson gson = new Gson();

                    String includeConfig = FileUtils.readFileFromAssets(App.i(), "rule/" + FILE_NAME);
                    if (!TextUtils.isEmpty(includeConfig)) {
                        ArrayMap<String,String> ruleObj = gson.fromJson(includeConfig, new TypeToken<ArrayMap<String,String>>() {}.getType());
                        rule.putAll(ruleObj);
                    }

                    String remoteConfig = FileUtils.readFile(App.i().getGlobalConfigPath() + FILE_NAME);
                    if (!TextUtils.isEmpty(remoteConfig)) {
                        ArrayMap<String,String> ruleObj = gson.fromJson(remoteConfig, new TypeToken<ArrayMap<String,String>>() {}.getType());
                        rule.putAll(ruleObj);
                    }

                    String userConfig = FileUtils.readFile(App.i().getUserConfigPath() + FILE_NAME);
                    if (!TextUtils.isEmpty(userConfig)) {
                        ArrayMap<String,String> ruleObj = gson.fromJson(userConfig, new TypeToken<ArrayMap<String,String>>() {}.getType());
                        rule.putAll(ruleObj);
                    }
                 }
            }
        }
        return instance;
    }

    public void reset() {
        instance = null;
    }
    public void save() {
        FileUtils.save(App.i().getUserConfigPath() + FILE_NAME, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(rule));
    }

    // public String guessUserAgentByUrl(String url) {
    //     String ua = CorePref.i().userPref().getString(Contract.ENABLE_REMOTE_URL_REWRITE_RULE, "");
    //     if(!StringUtils.isEmpty(ua)){
    //         ua = rule.get(ua);
    //         if(!StringUtils.isEmpty(ua)){
    //             return ua;
    //         }
    //     }
    //
    //
    //     if(rule != null){
    //         ua = guessUserAgentByUrl1(url);
    //         if(!StringUtils.isEmpty(ua)){
    //             ua = rule.get(ua);
    //             if(!StringUtils.isEmpty(ua)){
    //                 return ua;
    //             }
    //         }
    //     }
    //     return CorePref.i().globalPref().getString(Contract.USER_AGENT,null);
    // }

    public String guessUserAgentByUrl(String url) {
        // XLog.d(rule);
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        String host = Uri.parse(url).getHost();
        if (TextUtils.isEmpty(host)) {
            return null;
        }

        if (rule == null) {
            return null;
        }

        if (rule.containsKey(host)) {
            return StringUtils.urlEncode(rule.get(host));
        }

        String[] slices = host.split("\\.");
        for (int i = 1, size = slices.length; i+1 < size; i++) {
            host = StringUtils.join(".", Arrays.copyOfRange(slices, i, size));
            // XLog.i("分割 Host 推测 UA：" + host );
            if (rule.containsKey(host)) {
                return rule.get(host);
            }
        }
        return null;
    }

}
