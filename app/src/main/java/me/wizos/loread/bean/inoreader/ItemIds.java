package me.wizos.loread.bean.inoreader;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Wizos on 2016/3/10.
 */
public class ItemIds {
    @SerializedName("items")
    ArrayList<String> items;

    @SerializedName("itemRefs")
    ArrayList<ItemRefs> itemRefs;

    @SerializedName("continuation")
    String continuation;

    public ItemIds() {
        items = new ArrayList<>();
        itemRefs = new ArrayList<>();
        continuation = null;
    }


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

    public void addItemRefs(ArrayList<ItemRefs> itemRefs) {
        this.itemRefs.addAll(itemRefs);
    }


}
