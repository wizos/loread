package me.wizos.loread.bean.feedly;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Wizos on 2019/2/24.
 */

public class Visual {
    /**
     * 可能为none
     */
    @SerializedName("url")
    private Origin url;

    /**
     * 可空(可能性中)
     */
    @SerializedName("contentType")
    private Origin contentType;

    /**
     * 可空(可能性高)
     */
    @SerializedName("width")
    private int width;

    /**
     * 可空(可能性高)
     */
    @SerializedName("height")
    private int height;

    public Origin getUrl() {
        return url;
    }

    public void setUrl(Origin url) {
        this.url = url;
    }

    public Origin getContentType() {
        return contentType;
    }

    public void setContentType(Origin contentType) {
        this.contentType = contentType;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
