package me.wizos.loread.bean.ttrss.result;

import com.google.gson.annotations.SerializedName;

import me.wizos.loread.db.Category;

public class CategoryItem {
    @SerializedName("id")
    private String id;
    @SerializedName("title")
    private String title;

    private int unread;
    @SerializedName("order_id")
    private int orderId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }


    public Category convert() {
        Category category = new Category();
        category.setId(id);
        category.setTitle(title);
        return category;
    }


    @Override
    public String toString() {
        return "TTRSSCategoryItem{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", unread=" + unread +
                ", orderId=" + orderId +
                '}';
    }
}
