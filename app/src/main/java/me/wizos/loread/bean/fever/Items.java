package me.wizos.loread.bean.fever;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Items extends BaseResponse {
    // 获取数据库中所有的项
    @SerializedName("total_items")
    private String totalItems;

    // 获取数据库中指定的项
    @SerializedName("items")
    private List<Item> items;

    public String getTotalItems() {
        return totalItems;
    }

    public List<Item> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "Items{" +
                "totalItems='" + totalItems + '\'' +
                ", items=" + items +
                '}';
    }
}
