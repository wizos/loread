package me.wizos.loread.bean.inoreader;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Wizos on 2019/2/20.
 */

public class GsTag {
    @SerializedName("id")
    String id;
    @SerializedName("sortid")
    String sortid;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSortid() {
        return sortid;
    }

    public void setSortid(String sortid) {
        this.sortid = sortid;
    }

    @Override
    public String toString() {
        return "GsTag{" +
                "id='" + id + '\'' +
                ", sortid='" + sortid + '\'' +
                '}';
    }
}
