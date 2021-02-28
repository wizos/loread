package me.wizos.loread.config.header_useragent;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.LinkedHashMap;

import me.wizos.loread.App;
import me.wizos.loread.utils.FileUtils;

public class UserAgentConfig {
    public static final String FILE_NAME = "user_agent.json";
    private static UserAgentConfig instance;
    private UserAgentConfig() {}
    public static UserAgentConfig i() {
        if (instance == null) {
            synchronized (UserAgentConfig.class) {
                if (instance == null) {
                    instance = new UserAgentConfig();
                    String config = FileUtils.readFile(App.i().getUserConfigPath() + FILE_NAME);
                    if (TextUtils.isEmpty(config)) {
                        instance.userAgents = new LinkedHashMap<>();
                        instance.userAgents.put("iPhone iOS 11", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/11.0 Mobile/15E148 Safari/604.1");
                        instance.userAgents.put("Win Chrome 87", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36");
                    } else {
                        instance.userAgents = new Gson().fromJson(config, new TypeToken<LinkedHashMap<String,String>>() {}.getType());
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
        FileUtils.save(App.i().getUserConfigPath() + FILE_NAME, new GsonBuilder().setPrettyPrinting().create().toJson(instance.userAgents));
    }

    private LinkedHashMap<String, String> userAgents; // 格式是 Name, UA

    public void putCustomUserAgent(String name, String userAgent) {
        this.userAgents.put(name, userAgent);
    }

    public LinkedHashMap<String, String> getUserAgents() {
        return userAgents;
    }
}
