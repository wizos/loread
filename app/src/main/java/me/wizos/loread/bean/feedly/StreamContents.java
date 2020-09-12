package me.wizos.loread.bean.feedly;

import java.util.ArrayList;

/**
 * Created by Wizos on 2019/2/8.
 */

public class StreamContents {
    private String id;
    private String title;
    private long updated;
    private String continuation;
    private ArrayList<Entry> items;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public String getContinuation() {
        return continuation;
    }

    public void setContinuation(String continuation) {
        this.continuation = continuation;
    }

    public ArrayList<Entry> getItems() {
        return items;
    }

    public void setItems(ArrayList<Entry> items) {
        this.items = items;
    }
}
