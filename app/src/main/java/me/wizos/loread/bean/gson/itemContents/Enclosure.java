package me.wizos.loread.bean.gson.itemContents;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * 文章中的附件
 * Created by Wizos on 2017/9/3.
 */

@Parcel
public class Enclosure {

    @SerializedName("href")
    String href;

    @SerializedName("type") // 值有image/jpeg、application/rss+xml; charset=UTF-8（href是https://justyy.com/feed）
            String type;

    @SerializedName("length")
    int length;

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

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}
