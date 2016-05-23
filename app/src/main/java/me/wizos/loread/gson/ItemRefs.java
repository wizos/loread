package me.wizos.loread.gson;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Wizos on 2016/3/10.
 */
public class ItemRefs {

    @SerializedName("id")
    String id;

    @SerializedName("directStreamIds")
    ArrayList<String> directStreamIds;

    @SerializedName("timestampUsec")
    long timestampUsec;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDirectStreamIds(ArrayList<String> directStreamIds) {
        this.directStreamIds = directStreamIds;
    }
    public ArrayList<String> getDirectStreamIds() {
        return directStreamIds;
    }

    public void setTimestampUsec(long timestampUsec) {
        this.timestampUsec = timestampUsec;
    }
    public long getTimestampUsec() {
        return timestampUsec;
    }
}
