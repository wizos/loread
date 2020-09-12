package me.wizos.loread.bean.fever;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Items extends BaseResponse {
    @SerializedName("total_items")
    private String totalItems;

    @SerializedName("items")
    private List<Item> items;

    public String getTotalItems() {
        return totalItems;
    }

    public List<Item> getItems() {
        return items;
    }
}
