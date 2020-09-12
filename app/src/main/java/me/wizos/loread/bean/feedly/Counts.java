package me.wizos.loread.bean.feedly;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Wizos on 2019/2/8.
 */

public class Counts {
    @SerializedName("unreadcounts")
    private ArrayList<Unreadcount> unreadcounts;
    @SerializedName("updated")
    private long updated;

    public ArrayList<Unreadcount> getUnreadcounts() {
        return unreadcounts;
    }

    public void setUnreadcounts(ArrayList<Unreadcount> unreadcounts) {
        this.unreadcounts = unreadcounts;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }
}
