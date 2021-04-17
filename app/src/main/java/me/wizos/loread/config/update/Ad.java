package me.wizos.loread.config.update;

public class Ad {
    private boolean open = false;
    private int version = 1;
    private String url;

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Ad{" +
                "open=" + open +
                ", version=" + version +
                ", url='" + url + '\'' +
                '}';
    }
}
