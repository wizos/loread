package me.wizos.loread.bean.inoreader;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Wizos on 2016/3/11.
 */
public class UnreadCounts {

    @SerializedName("id")
    String id;

    @SerializedName("count")
    int count;

    @SerializedName("newestItemTimestampUsec")
    long newestItemTimestampUsec;

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

    public long getNewestItemTimestampUsec() {
        return newestItemTimestampUsec;
    }

    public void setNewestItemTimestampUsec(long newestItemTimestampUsec) {
        this.newestItemTimestampUsec = newestItemTimestampUsec;
    }

}
