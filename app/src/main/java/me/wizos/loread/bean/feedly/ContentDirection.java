package me.wizos.loread.bean.feedly;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Wizos on 2019/2/8.
 */

public class ContentDirection {
    @SerializedName("content")
    String content;
    @SerializedName("direction")
    String direction;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
