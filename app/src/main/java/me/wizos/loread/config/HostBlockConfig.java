package me.wizos.loread.config;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;

import com.elvishew.xlog.XLog;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import me.wizos.loread.App;
import me.wizos.loread.utils.FileUtils;

/**
 * 作用：在进行网络请求前，修改被请求的网址。
 * 格式：内容为 json 形式的 key : value 列表。key 为 host，value 为 js 脚本。
 * 运行机制：请求的网址前，系统会先将网址的 host 以 "." 为节点，拆出多个 host，
 *          例如将 zelda.game.qq.com.cn，拆为 zelda.game.qq.com.cn, game.qq.com.cn, qq.com.cn, com.cn（不包含最后一个 "." 的后部分）。
 *          依次将这 4 个 host 在规则文件中查找是否有符合的 key，若有则执行对应的 value 中的 js 脚本。（若既有 key 为 game.qq.com，又有 qq.com 的规则，则只会命中前者）。
 *          在执行 js 脚本时，会传入 url 参数，以及 call 对象。可以调用 call.valid(newUrl) 方法验证 newUrl 是否有效（即是否成功响应）
 * @author Wizos on 2021/01/16.
 */
public class HostBlockConfig {
    public static final String FILENAME = "ad_block.txt";
    private static HostBlockConfig instance;
    // private static final String FILE = "ad_block.txt";
    // private static ArraySet<String> rule;

    private static final Set<String> hosts = new HashSet<>();
    @SuppressLint("ConstantLocale")
    private static final Locale locale = Locale.getDefault();

    private HostBlockConfig() { }
    public static HostBlockConfig i() {
        if (instance == null) {
            synchronized (HostBlockConfig.class) {
                if (instance == null) {
                    instance = new HostBlockConfig();

                    loadHosts(new File(App.i().getGlobalConfigPath() + FILENAME));
                    loadHosts(new File(App.i().getUserConfigPath() + FILENAME));
                    // rule = new ArraySet<String>();
                    // Gson gson = new Gson();
                    // // if(CorePref.i().userPref().getBoolean(Contract.ENABLE_REMOTE_HOST_BLOCK_RULE, false)){
                    //     String remoteConfig = FileUtil.readFile(App.i().getGlobalConfigPath() + CONFIG_FILENAME);
                    //     if (!TextUtils.isEmpty(remoteConfig)) {
                    //         ArraySet<String> remoteRule = gson.fromJson(remoteConfig, new TypeToken<ArraySet<String>>() {}.getType());
                    //         rule.addAll(remoteRule);
                    //     }
                    // // }
                    // String userConfig = FileUtil.readFile(App.i().getUserConfigPath() + CONFIG_FILENAME);
                    // if (!TextUtils.isEmpty(userConfig)) {
                    //     ArraySet<String> userRule = gson.fromJson(userConfig, new TypeToken<ArraySet<String>>() {}.getType());
                    //     rule.addAll(userRule);
                    // }
                }
            }
        }
        return instance;
    }
    public void reset() {
        instance = null;
    }
    public void save() {
        FileUtils.save(App.i().getUserConfigPath() + FILENAME, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(instance));
    }


    private static void loadHosts(File file) {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if( !file.exists() ){
                        return;
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        hosts.add(line.toLowerCase(locale));
                    }
                } catch (IOException i) {
                    i.printStackTrace();
                    XLog.i(i);
                }
            }
        });
    }

    @SuppressLint("ConstantLocale")
    private static String getHost(String url) {
        url = url.toLowerCase(locale);
        Uri uri = Uri.parse(url);
        String host = uri.getHost();

        if (host == null) {
            int index = url.indexOf('/', 8); // -> http://(7) and https://(8)
            if (index != -1) {
                url = url.substring(0, index);
            }
            return url;
        }
        return host.startsWith("www.") ? host.substring(4) : host;
    }


    public boolean isAd(String url) {
        return hosts.contains(getHost(url));
    }
}