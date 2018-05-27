package me.wizos.loread.bean.config;

/**
 * Created by Wizos on 2018/4/11.
 */

public class FeedConfig {
//    private Integer unreadCount;
//    private Long newestItemTimestampUsec;

    private String openMode;
    private String referer; // 1.auto  2.具体值
    private String userAgent;

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

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
