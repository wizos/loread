package me.wizos.loread.bean.feedly;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Wizos on 2019/2/8.
 */

public class Unreadcount {
    @SerializedName("id")
    private String id;
    @SerializedName("count")
    private int count;
    @SerializedName("updated")
    private long updated;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }
}
