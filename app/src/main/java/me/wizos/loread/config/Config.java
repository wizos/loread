package me.wizos.loread.config;

import com.google.gson.annotations.SerializedName;

import me.wizos.loread.config.update.AppUpdateModel;

/**
 * @author Wizos on 2020/5/17.
 */

public class Config {
    // 上次更新时间
    // private long update = 0;
    // 启动时检查更新
    // 更新间隔，单位分钟（60*24=1440）
    // private int updateInterval = 1440;
    // private String url = "https://raw.githubusercontent.com/wizos/loread/master/config.json";

    @SerializedName("app")
    private AppUpdateModel appUpdateModel;

    public Config(AppUpdateModel appUpdateModel) {
        this.appUpdateModel = appUpdateModel;
    }

    // public long getUpdate() {
    //     return update;
    // }
    //
    // public void setUpdate(long update) {
    //     this.update = update;
    // }
    //
    // public int getUpdateInterval() {
    //     return updateInterval;
    // }
    //
    // public void setUpdateInterval(int updateInterval) {
    //     this.updateInterval = updateInterval;
    // }

    // public String getUrl() {
    //     return url;
    // }
    //
    // public void setUrl(String url) {
    //     this.url = url;
    // }

    public AppUpdateModel getAppUpdateModel() {
        return appUpdateModel;
    }

    public void setAppUpdateModel(AppUpdateModel appUpdateModel) {
        this.appUpdateModel = appUpdateModel;
    }

    @Override
    public String toString() {
        return "Config{" +
                // "update=" + update +
                // ", updateInterval=" + updateInterval +
                // ", updateUrl='" + url + '\'' +
                ", app=" + appUpdateModel +
                '}';
    }
}
