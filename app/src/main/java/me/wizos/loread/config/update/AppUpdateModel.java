package me.wizos.loread.config.update;

import com.cretin.www.cretinautoupdatelibrary.model.LibraryUpdateEntity;

public class AppUpdateModel implements LibraryUpdateEntity {
    public int version;
    public String versionName;
    public String url;
    public String content;

    @Override
    public int getAppVersionCode() {
        return version;
    }

    @Override
    public int forceAppUpdateFlag() {
        return 0;
    }

    @Override
    public String getAppVersionName() {
        return versionName;
    }

    @Override
    public String getAppApkUrls() {
        return url;
    }

    @Override
    public String getAppUpdateLog() {
        return content;
    }

    @Override
    public String getAppApkSize() {
        return null;
    }

    @Override
    public String getAppHasAffectCodes() {
        return null;
    }

    @Override
    public String getFileMd5Check() {
        return null;
    }

    @Override
    public String toString() {
        return "App{" +
                "version=" + version +
                ", url='" + url + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
