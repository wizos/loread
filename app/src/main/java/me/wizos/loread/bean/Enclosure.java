package me.wizos.loread.bean;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Wizos on 2019/2/8.
 */

public class Enclosure {
    @SerializedName(value = "href", alternate = {"content_url"})
    private String href;
    // 值有text/html、image/jpeg、application/rss+xml; charset=UTF-8（href是https://justyy.com/feed）
    @SerializedName(value = "type", alternate = {"content_type"})
    private String type;

    private long length;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    @NotNull
    @Override
    public String toString() {
        return "Enclosure{" +
                "href='" + href + '\'' +
                ", type='" + type + '\'' +
                ", length=" + length +
                '}';
    }
}
