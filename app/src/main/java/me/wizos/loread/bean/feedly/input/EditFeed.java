package me.wizos.loread.bean.feedly.input;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.bean.feedly.CategoryItem;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;

/**
 * Created by Wizos on 2019/2/24.
 */

public class EditFeed {
    /**
     * 此id并非feedId，而是 feed/xxxx
     */
    private String id;
    private String title;
    private ArrayList<CategoryItem> categoryItems = new ArrayList<>();

    public EditFeed() {
    }


    public EditFeed(String feedId) {
        Feed feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(), feedId);
        id = feed.getId();
        title = feed.getTitle();
        List<Category> categories = CoreDB.i().categoryDao().getByFeedId(App.i().getUser().getId(), feedId);
        for (Category category : categories) {
            categoryItems.add(category.convert2CategoryItem());
        }
    }


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

    public ArrayList<CategoryItem> getCategoryItems() {
        return categoryItems;
    }

    public void setCategoryItems(ArrayList<CategoryItem> categoryItems) {
        this.categoryItems = categoryItems;
    }


    @Override
    public String toString() {
        return "EditFeed{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", categoryItems=" + categoryItems +
                '}';
    }
}
