package me.wizos.loread.gson;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Wizos on 2016/3/11.
 */
public class GsUnreadCount {
    @SerializedName("max")
    int max;

    @SerializedName("unreadcounts")
    ArrayList<UnreadCounts> unreadcounts;

    public int getMax() {
        return max;
    }
    public void setMax(int max) {
        this.max = max;
    }

    public ArrayList<UnreadCounts> getUnreadcounts() {
        return unreadcounts;
    }
    public void setUnreadcounts(ArrayList<UnreadCounts> unreadcounts) {
        this.unreadcounts = unreadcounts;
    }
}
