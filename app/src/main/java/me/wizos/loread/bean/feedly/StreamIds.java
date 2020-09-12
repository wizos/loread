package me.wizos.loread.bean.feedly;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Wizos on 2019/2/8.
 */

public class StreamIds {
    @SerializedName("ids")
    private ArrayList<String> ids;

    @SerializedName("continuation")
    private String continuation;

    public ArrayList<String> getIds() {
        return ids;
    }

    public void setIds(ArrayList<String> ids) {
        this.ids = ids;
    }

    public String getContinuation() {
        return continuation;
    }

    public void setContinuation(String continuation) {
        this.continuation = continuation;
    }
}
