package me.wizos.loread.config;

import android.net.Uri;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.elvishew.xlog.XLog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import javax.script.Bindings;
import javax.script.SimpleBindings;

import me.wizos.loread.App;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.ScriptUtil;

/**
 * @author Wizos on 2020/4/14.
 */
public class LinkRewriteConfig {
    private static final String CONFIG_FILENAME = "link_rewrite.json";
    private LinkRewriteConfig() { }
    public static LinkRewriteConfig i() {
        if (instance == null) {
            synchronized (LinkRewriteConfig.class) {
                if (instance == null) {
                    Gson gson = new Gson();

                    String config = FileUtil.readFile(App.i().getUserConfigPath() + CONFIG_FILENAME);
                    if (TextUtils.isEmpty(config)) {
                        instance = new LinkRewriteConfig();
                        instance.domainRewrite = new ArrayMap<>();
                        instance.urlRewrite = new ArrayMap<>();
                    } else {
                        instance = gson.fromJson(config, LinkRewriteConfig.class);
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
        FileUtil.save(App.i().getUserConfigPath() + CONFIG_FILENAME, new GsonBuilder().setPrettyPrinting().create().toJson(instance));
    }

    @SerializedName("url_match_domain_rewrite_domain")
    private ArrayMap<String, String> domainRewrite;

    @SerializedName("url_match_domain_rewrite_url")
    private ArrayMap<String, String> urlRewrite;
    private static LinkRewriteConfig instance;

    public String getRedirectUrl(String url) {
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        if (domainRewrite.containsKey(host)) {
            return url.replaceFirst(host, domainRewrite.get(host));
        } else if (urlRewrite.containsKey(host)) {
            // Bindings接口可以理解为上下文，可以往上下文中设置一个Java对象或通过key获取一个对象，它有一个实现类，SimpleBindings，内部就是一个map。
            Bindings bindings = new SimpleBindings();
            bindings.put("url", url);
            ScriptUtil.i().eval(urlRewrite.get(host), bindings);
            XLog.i("重定向JS：" + urlRewrite.get(host));
            return (String) bindings.get("url");
        }
        return "";
    }
}