package me.wizos.loread.config;

import android.net.Uri;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;

import me.wizos.loread.App;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.StringUtils;

public class NetworkRefererConfig {
    private static final String CONFIG_FILENAME = "network_referer.json";
    private transient static NetworkRefererConfig instance;
    public static NetworkRefererConfig i() {
        if (instance == null) {
            synchronized (NetworkRefererConfig.class) {
                if (instance == null) {
                    String json = FileUtil.readFile(App.i().getUserConfigPath() + CONFIG_FILENAME);
                    instance = new NetworkRefererConfig();
                    if (TextUtils.isEmpty(json)) {
                        instance.domainReferer = new ArrayMap<String, String>();
                    } else {
                        instance.domainReferer = new Gson().fromJson(json, new TypeToken<ArrayMap<String,String>>() {}.getType());
                    }
                }
            }
        }
        return instance;
    }
    public void save() {
        FileUtil.save(App.i().getUserConfigPath() + CONFIG_FILENAME, new GsonBuilder().setPrettyPrinting().create().toJson(instance.domainReferer));
    }
    public void reset() {
        instance = null;
    }

    private ArrayMap<String, String> domainReferer; // 格式是 domain, Referer

    /**
     * 用于手动下载图片
     * 有3中方法获取referer：
     * 1.根据feedid，推断出referer。。优点是简单，但是可能由于rss是第三方烧制的，可能会失效。
     * 2.根据文章url，推断出referer。
     * 2.根据图片url，猜测出referer，配置繁琐、低效，但是适应性较强。（可解决图片用的是第三方服务）
     *
     * @param imgUrl
     * @return
     */
    public String guessRefererByUrl(String imgUrl) {
        if (TextUtils.isEmpty(imgUrl)) {
            return null;
        }

        Uri uri = Uri.parse(imgUrl);
        String host = uri.getHost();
        if (TextUtils.isEmpty(host)) {
            return null;
        }
        if (domainReferer==null) {
            return null;
        }

        if (domainReferer.containsKey(host)) {
            return domainReferer.get(host);
        }

        String[] slices = host.split("\\.");
        for (int i = 1, size = slices.length; i+1 < size; i++) {
            host = StringUtils.join(".", Arrays.copyOfRange(slices, i, size));
            // KLog.i("分割 Host 推测 Referer：" + host );
            if (domainReferer.containsKey(host)) {
                return domainReferer.get(host); // StringUtils.urlEncode();
            }
        }
        return null;
    }

    public void addReferer(String imgUrl, String articleUrl){
        Uri imgUri = Uri.parse(imgUrl);
        String host = imgUri.getHost();
        Uri articleUri = Uri.parse(articleUrl);
        domainReferer.put(host, articleUri.getScheme() + "://" + articleUri.getHost());
        save();
    }


    // https://blog.lyz810.com/article/2016/08/referrer-policy-and-anti-leech/
    // https://www.jianshu.com/p/92bd520c0f8f
    // https://www.jianshu.com/p/1be1f97167f8
    public String getRefererByPolicy2(String refererPolicy, String articleUrl){
        if(StringUtils.isEmpty(refererPolicy) || refererPolicy.equalsIgnoreCase("no-referrer") || refererPolicy.equalsIgnoreCase("undefined")){
            return null;
        }
        if(refererPolicy.equalsIgnoreCase("no-referrer-when-downgrade") || refererPolicy.equalsIgnoreCase("strict-origin")){
            if(!StringUtils.isEmpty(articleUrl) && articleUrl.startsWith("https://")){
                return articleUrl;
            }
            return null;
        }

        if(refererPolicy.equalsIgnoreCase("unsafe-url")){
            return articleUrl;
        }

        if(refererPolicy.equalsIgnoreCase("origin")){
            Uri uri = Uri.parse(articleUrl);
            return uri.getScheme() + "://" + uri.getHost();
        }
        return null;
    }

    public String getRefererByPolicy(String refererPolicy, String articleUrl){
        if(StringUtils.isEmpty(refererPolicy)){
            return null;
        }
        if(refererPolicy.equalsIgnoreCase("no-referrer") || refererPolicy.equalsIgnoreCase("undefined")){
            return null;
        }
        if(refererPolicy.equalsIgnoreCase("no-referrer-when-downgrade") || refererPolicy.equalsIgnoreCase("strict-origin") || refererPolicy.equalsIgnoreCase("origin") || refererPolicy.equalsIgnoreCase("unsafe-url")){
            return articleUrl;
        }
        return null;
    }

}
