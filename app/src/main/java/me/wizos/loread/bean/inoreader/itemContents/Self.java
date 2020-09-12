package me.wizos.loread.bean.inoreader.itemContents;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Self {
    @SerializedName("href")
    String href;

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }
}
