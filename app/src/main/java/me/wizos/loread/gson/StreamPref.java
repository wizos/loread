package me.wizos.loread.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Wizos on 2016/3/5.
 */
public class StreamPref {

    @SerializedName("value")
    String value;

    @SerializedName("id")
    String id;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
