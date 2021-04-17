package me.wizos.loread.config.update;

public class Rule {
    private int version = 1;
    private int minAppVersion = 1;
    private String url;

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getMinAppVersion() {
        return minAppVersion;
    }

    public void setMinAppVersion(int minAppVersion) {
        this.minAppVersion = minAppVersion;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "version=" + version +
                ", minAppVersion=" + minAppVersion +
                ", url='" + url + '\'' +
                '}';
    }
}
