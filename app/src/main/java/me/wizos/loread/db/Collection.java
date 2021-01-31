package me.wizos.loread.db;

// Category 和 Feed 的抽象
public class Collection {
    private String id;
    private String title;
    private int count;
    transient public boolean isExpand = false;



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

    @Override
    public String toString() {
        return "Collection{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", count=" + count +
                '}';
    }
}
