package me.wizos.loread.config;

import android.net.Uri;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.db.CorePref;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.StringUtils;

public class NetworkUserAgentConfig {
    private static final String CONFIG_FILENAME = "network_user_agent.json";
    private static NetworkUserAgentConfig instance;
    private NetworkUserAgentConfig() {}
    public static NetworkUserAgentConfig i() {
        if (instance == null) {
            synchronized (NetworkUserAgentConfig.class) {
                if (instance == null) {
                    String config = FileUtil.readFile(App.i().getUserConfigPath() + CONFIG_FILENAME);
                    instance = new NetworkUserAgentConfig();
                    if (TextUtils.isEmpty(config)) {
                        instance.domainUserAgent = new ArrayMap<String, String>();
                        instance.userAgents = new ArrayMap<>();
                        instance.userAgents.put("iPhone", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.0 Mobile/15E148 Safari/604.1");
                        instance.userAgents.put("Android", "Mozilla/5.0 (Linux; Android 5.1; MX5 Build/LMY47I) AppleWebKit/605.1.15 (KHTML, like Gecko) Chrome/66.0.3359.181 Mobile Safari/604.1");
                        instance.userAgents.put("Chrome(PC)", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36");
                    } else {
                        instance.domainUserAgent = new Gson().fromJson(config, new TypeToken<ArrayMap<String,String>>() {}.getType());
                    }
                }
            }
        }
        return instance;
    }

    public void save() {
        FileUtil.save(App.i().getUserConfigPath() + CONFIG_FILENAME, new GsonBuilder().setPrettyPrinting().create().toJson(instance));
    }

    private ArrayMap<String, String> userAgents; // 格式是 Name, UA

    // 保持一直为该UA
    private String holdUserAgent;
    private ArrayMap<String, String> domainUserAgent; // 格式是 Domain, Name

    public String getHoldUserAgent() {
        return holdUserAgent;
    }

    public void setHoldUserAgent(String holdUserAgent) {
        this.holdUserAgent = holdUserAgent;
    }

    public ArrayMap<String, String> getUserAgents() {
        return userAgents;
    }


    public String guessUserAgentByUrl(String url) {
        if (!StringUtils.isEmpty(holdUserAgent)) {
            return userAgents.get(holdUserAgent);
        }
        String ua = guessUserAgentByUrl1(url);
        if(!StringUtils.isEmpty(ua)){
            return ua;
        }
        return CorePref.i().globalPref().getString(Contract.USER_AGENT,null);
    }
    //public String guessUserAgentByDefault() {
    //    if (!TextUtils.isEmpty(holdUserAgent)) {
    //        return userAgents.get(holdUserAgent);
    //    }
    //    return guessUserAgentByUrl1();
    //}
    /**
     * 用于手动下载图片
     * 有3中方法获取referer：
     * 1.根据feedid，推断出referer。。优点是简单，但是可能由于rss是第三方烧制的，可能会失效。
     * 2.根据文章url，推断出referer。
     * 2.根据图片url，猜测出referer，配置繁琐、低效，但是适应性较强。（可解决图片用的是第三方服务）
     *
     * @param url
     * @return
     */
    public String guessUserAgentByUrl1(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        if (TextUtils.isEmpty(host)) {
            return null;
        }
        if (domainUserAgent ==null) {
            return null;
        }

        if (domainUserAgent.containsKey(host)) {
            return StringUtils.urlEncode(domainUserAgent.get(host));
        }

        String[] slices = host.split("\\.");
        for (int i = 1, size = slices.length; i+1 < size; i++) {
            host = StringUtils.join(".", Arrays.copyOfRange(slices, i, size));
//            KLog.i("分割 Host 推测 UA：" + host );
            if (domainUserAgent.containsKey(host)) {
                return domainUserAgent.get(host);
            }
        }
        return null;
    }

    public String guessUserAgentByUrl2(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        url = url.toLowerCase(Locale.getDefault());
        for (Map.Entry<String, String> entry : domainUserAgent.entrySet()) {
            if (url.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void reset() {
        instance = null;
    }
}
