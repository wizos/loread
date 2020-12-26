package me.wizos.loread.config;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import me.wizos.loread.App;
import me.wizos.loread.utils.FileUtil;

/**
 * @author Wizos on 2018/4/14.
 */

public class TestConfig {
    private static final String CONFIG_FILENAME = "config.json";
    private static TestConfig instance;
    private TestConfig() { }

    public static TestConfig i() {
        if (instance == null) {
            synchronized (TestConfig.class) {
                if (instance == null) {
                    instance = new TestConfig();
                    Gson gson = new Gson();
                    String config;
                    config = FileUtil.readFile(App.i().getUserConfigPath() + CONFIG_FILENAME);
                    if (TextUtils.isEmpty(config)) {
                        instance.save();
                    } else {
                        instance = gson.fromJson(config, new TypeToken<TestConfig>() {}.getType());
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

    public boolean ttsFile = false;
    public boolean useEmojiFilter = false;
    public int time = 60;
    public String rssFinderUser = null;
    public boolean useArticleMod = true;

}
