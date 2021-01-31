package me.wizos.loread.bean.fever;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Items extends FeverResponse {
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

    @NotNull
    @Override
    public String toString() {
        return "Items{" +
                "totalItems='" + totalItems + '\'' +
                ", items=" + items +
                '}';
    }
}
