package me.wizos.loread.bean.config;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.utils.FileUtil;

/**
 * @author Wizos on 2018/4/14.
 */

// TODO: 2018/4/14 全局的配置。可用于在启动时连接服务器，获取到搜索的接口？
public class GlobalConfig {
    private boolean blockAD = true;
    private int userAgentIndex; // 结果为-2 为智能UA（命中规则的走规则，未命中的走默认），-1是默认UA，其他的就是内置的UA
    private ArrayList<UserAgent> userAgents;
    private ArrayMap<String, String> userAgentsRouter; // 格式是 domain, userAgentIndex
    private ArrayMap<String, String> refererRouter;  // 格式是 domain, Referer
    private ArrayMap<String, String> displayRouter;  // 格式是 feedId, mode


    public ArrayList<UserAgent> getUserAgents() {
        return userAgents;
    }

    public void setUserAgents(ArrayList<UserAgent> userAgents) {
        this.userAgents = userAgents;
    }

    public int getUserAgentIndex() {
        return userAgentIndex;
    }

    public void setUserAgentIndex(int userAgentIndex) {
        this.userAgentIndex = userAgentIndex;
    }

    public boolean isBlockAD() {
        return blockAD;
    }

    public void setBlockAD(boolean blockAD) {
        this.blockAD = blockAD;
    }

    public ArrayMap<String, String> getUserAgentsRouter() {
        return userAgentsRouter;
    }

    public void setUserAgentsRouter(ArrayMap<String, String> userAgentsRouter) {
        this.userAgentsRouter = userAgentsRouter;
    }

    public ArrayMap<String, String> getRefererRouter() {
        return refererRouter;
    }

    public void setRefererRouter(ArrayMap<String, String> refererRouter) {
        this.refererRouter = refererRouter;
    }

    public void addRefererRouter(String key, String referer) {
        userAgentsRouter.put(key, referer);
    }

    public GlobalConfig removeRefererRouter(String key) {
        userAgentsRouter.remove(key);
        return this;
    }

    public ArrayMap<String, String> getDisplayRouter() {
        return displayRouter;
    }

    public void setDisplayRouter(ArrayMap<String, String> displayRouter) {
        this.displayRouter = displayRouter;
    }

    public void addDisplayRouter(String feedId, String displayMode) {
        displayRouter.put(feedId, displayMode);
    }

    public void removeDisplayRouter(String key) {
        displayRouter.remove(key);
    }


    public String getUserAgentString() {
        if (userAgentIndex < 0) {
            return "";
        }
        return userAgents.get(userAgentIndex).getValue();
    }


    public String getDisplayMode(String feedId) {
        if (TextUtils.isEmpty(feedId)) {
            return "";
        }
        if (displayRouter.containsKey(feedId)) {
            return displayRouter.get(feedId);
        }
        return "";
    }

    public String guessUserAgentByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        url = url.toLowerCase(Locale.getDefault());
        for (Map.Entry<String, String> entry : userAgentsRouter.entrySet()) {
            if (url.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return "";
    }

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
    public String guessRefererByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        url = url.toLowerCase(Locale.getDefault());
        for (Map.Entry<String, String> entry : refererRouter.entrySet()) {
            if (url.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
//        String domain;
//        try {
//            URI uri = new URI(url);
//            domain = uri.getHost();
//            if (domain == null) {
//                int index = url.indexOf('/', 8); // -> http://(7) and https://(8)
//                if (index != -1) {
//                    domain = url.substring(0, index);
//                }
//            }
//        }catch (URISyntaxException u){
//            return "";
//        }
//        if(refererRouter.containsKey(domain)){
//            return refererRouter.get(domain);
//        }
        return "";
    }

    private static GlobalConfig globalConfig;

    private GlobalConfig() {
    }

    public static GlobalConfig i() {
        if (globalConfig == null) {
            synchronized (GlobalConfig.class) {
                if (globalConfig == null) {
                    globalConfig = new GlobalConfig();
                    Gson gson = new Gson();
                    String config;
                    config = FileUtil.readFile(App.i().getExternalFilesDir(null) + "/config/global-config.json");
                    if (TextUtils.isEmpty(config)) {
                        ArrayList<UserAgent> userAgents = new ArrayList<>(2);
                        userAgents.add(new UserAgent("iPhone", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.0 Mobile/15E148 Safari/604.1"));
//                        userAgents.add(new UserAgent("Android", "Mozilla/5.0 (Linux; Android 5.1; zh-cn; MX5 Build/LMY47I) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.0 Mobile/15E148 Safari/604.1"));
                        userAgents.add(new UserAgent("Android", "Mozilla/5.0 (Linux; Android 5.1; MX5 Build/LMY47I) AppleWebKit/605.1.15 (KHTML, like Gecko) Chrome/66.0.3359.181 Mobile Safari/604.1"));
                        userAgents.add(new UserAgent("Chrome(PC)", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36"));
                        globalConfig.setUserAgents(userAgents);
                        globalConfig.setUserAgentIndex(-1);
                        globalConfig.setUserAgentsRouter(new ArrayMap<String, String>());
                        globalConfig.setRefererRouter(new ArrayMap<String, String>());
                        globalConfig.setDisplayRouter(new ArrayMap<String, String>());
                        globalConfig.setBlockAD(true);
                        globalConfig.save();
                    } else {
                        globalConfig = gson.fromJson(config, new TypeToken<GlobalConfig>() {
                        }.getType());
                    }
                }
            }
        }
        return globalConfig;
    }


    public void save() {
        FileUtil.save(App.i().getExternalFilesDir(null) + "/config/global-config.json", new GsonBuilder().setPrettyPrinting().create().toJson(globalConfig));
    }

    public void reInit() {
        globalConfig = null;
    }

}
