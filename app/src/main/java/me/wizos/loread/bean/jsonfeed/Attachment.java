/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-02-09 05:16:49
 */

package me.wizos.loread.bean.jsonfeed;

import com.google.gson.annotations.SerializedName;

public class Attachment {
    String title;
    String url;
    @SerializedName(value = "mime_type")
    String mimeType;
    @SerializedName(value = "size_in_bytes")
    Long sizeInBytes;
    @SerializedName(value = "duration_in_seconds")
    Long durationInSeconds;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setSizeInBytes(Long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }

    public Long getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(Long durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }


    public boolean isValid() {
        /* Check the attachment fields */
        if ( getUrl() == null ) {
            return false;
        }

        return getMimeType() != null;
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", sizeInBytes=" + sizeInBytes +
                ", durationInSeconds=" + durationInSeconds +
                '}';
    }
}
