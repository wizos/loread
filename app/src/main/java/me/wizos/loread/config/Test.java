package me.wizos.loread.config;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import me.wizos.loread.App;
import me.wizos.loread.utils.FileUtils;

/**
 * @author Wizos on 2018/4/14.
 */

public class Test {
    private static final String CONFIG_FILENAME = "test.json";
    private static Test instance;
    private Test() { }

    public static Test i() {
        if (instance == null) {
            synchronized (Test.class) {
                if (instance == null) {
                    instance = new Test();
                    Gson gson = new Gson();
                    String config;
                    config = FileUtils.readFile(App.i().getUserConfigPath() + CONFIG_FILENAME);
                    if (TextUtils.isEmpty(config)) {
                        instance.save();
                    } else {
                        instance = gson.fromJson(config, new TypeToken<Test>() {}.getType());
                    }
                }
            }
        }
        return instance;
    }

    public void save() {
        FileUtils.save(App.i().getUserConfigPath() + CONFIG_FILENAME, new GsonBuilder().setPrettyPrinting().create().toJson(instance));
    }
    public void reset() {
        instance = null;
    }

    public boolean ttsFile = false;
    public boolean frame = false;

    public boolean useJsoup = true;
    public boolean downloadNow = false;
    public boolean useLoread = false;
    public String rssFinderUser = null;
}
