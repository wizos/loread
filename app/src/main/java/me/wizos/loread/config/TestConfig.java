package me.wizos.loread.config;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import me.wizos.loread.App;
import me.wizos.loread.utils.FileUtil;

/**
 * @author Wizos on 2018/4/14.
 */

public class TestConfig {
    private static TestConfig instance;
    private TestConfig() { }

    public static TestConfig i() {
        if (instance == null) {
            synchronized (TestConfig.class) {
                if (instance == null) {
                    instance = new TestConfig();
                    Gson gson = new Gson();
                    String config;
                    config = FileUtil.readFile(App.i().getUserConfigPath() + "config.json");
                    if (TextUtils.isEmpty(config)) {
                        instance.displayRouter = new ArrayMap<String, String>();
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
        FileUtil.save(App.i().getUserConfigPath() + "config.json", new GsonBuilder().setPrettyPrinting().create().toJson(instance));
    }
    public void reset() {
        instance = null;
    }



    private boolean ttsFile = false;
    public int time = 60;

    private ArrayMap<String, String> displayRouter;    // 格式是 feedId, mode


    public boolean isTtsFile() {
        return ttsFile;
    }


    public String getDisplayMode(String feedId) {
        if (!TextUtils.isEmpty(feedId) && displayRouter != null && displayRouter.containsKey(feedId)) {
            return displayRouter.get(feedId);
        }
        return App.DISPLAY_RSS;
    }

    public void addDisplayRouter(String feedId, String displayMode) {
        displayRouter.put(feedId, displayMode);
    }

    public void removeDisplayRouter(String key) {
        displayRouter.remove(key);
    }


}
