package me.wizos.loread.bean.config;

/**
 * Created by Wizos on 2018/4/11.
 */

public class FeedConfig {
    private String openMode;
    private String referer; // // 已废弃，改用GlobalConfig来配置 。 1.auto  2.具体值
    private String userAgent; // 已废弃，改用GlobalConfig来配置

    public FeedConfig(String openMode, String referer, String userAgent) {
        this.openMode = openMode;
        this.referer = referer;
        this.userAgent = userAgent;
    }

    public String getOpenMode() {
        return openMode;
    }

    public void setOpenMode(String openMode) {
        this.openMode = openMode;
    }

    @Deprecated
    public String getReferer() {
        return referer;
    }

    @Deprecated
    public void setReferer(String referer) {
        this.referer = referer;
    }

    @Deprecated
    public String getUserAgent() {
        return userAgent;
    }

    @Deprecated
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
