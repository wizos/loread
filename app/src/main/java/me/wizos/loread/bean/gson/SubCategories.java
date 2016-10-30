package me.wizos.loread.bean.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Wizos on 2016/3/11.
 */
public class SubCategories {
    @SerializedName("id")
    String id;

    @SerializedName("label")
    String label;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
