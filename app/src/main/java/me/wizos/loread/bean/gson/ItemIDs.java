package me.wizos.loread.bean.gson;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Wizos on 2016/3/10.
 */
public class ItemIDs {

    @SerializedName("items")
    ArrayList<String> items;

    @SerializedName("itemRefs")
    ArrayList<ItemRefs> itemRefs;

    @SerializedName("continuation")
    String continuation;

    public ArrayList<String> getItems() {
        return items;
    }

    public void setItems(ArrayList<String> items) {
        this.items = items;
    }

    public void setItemRefs(ArrayList<ItemRefs> itemRefs) {
        this.itemRefs = itemRefs;
    }
    public ArrayList<ItemRefs> getItemRefs() {
        return itemRefs;
    }

    public void setContinuation(String continuation) {
        this.continuation = continuation;
    }
    public String getContinuation() {
        return continuation;
    }
}
