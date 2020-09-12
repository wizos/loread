package me.wizos.loread.db;

import me.wizos.loread.bean.feedly.CategoryItem;

// Category 和 Feed 的抽象
public class Collection {
    private String id;
    private String title;
    private int count;

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public CategoryItem convert2CategoryItem() {
        CategoryItem category = new CategoryItem();
        category.setId(id);
        category.setLabel(title);
        return category;
    }

    @Override
    public String toString() {
        return "Collection{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", count=" + count +
                '}';
    }
}
