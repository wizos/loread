package me.wizos.loread.bean.inoreader;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;

import me.wizos.loread.db.Category;

@Parcel
public class GsTags {
    @SerializedName("tags")
    ArrayList<Category> categories;

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
    }
}
