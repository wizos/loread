package me.wizos.loread.bean.fever;

import me.wizos.loread.db.Category;

public class Group {
    private int id;
    private String title;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Category convert(){
        Category category = new Category();
        category.setId(String.valueOf(id));
        category.setTitle(title);
        return category;
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", title='" + title + '\'' +
                '}';
    }
}
