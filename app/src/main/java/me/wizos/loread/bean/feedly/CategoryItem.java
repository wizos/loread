package me.wizos.loread.bean.feedly;

import com.google.gson.annotations.SerializedName;

import me.wizos.loread.db.Category;

/**
 * Created by Wizos on 2019/2/8.
 */

public class CategoryItem {
    @SerializedName("id")
    private String id;

    @SerializedName("label")
    private String label;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }


    public Category convert() {
        Category category = new Category();
        category.setId(id);
        category.setTitle(label);
        return category;
    }

    @Override
    public String toString() {
        return "CategoryItem{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
