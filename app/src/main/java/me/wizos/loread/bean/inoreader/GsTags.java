package me.wizos.loread.bean.inoreader;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class GsTags {
    @SerializedName("tags")
    ArrayList<GsTag> categories;

    public ArrayList<GsTag> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<GsTag> categories) {
        this.categories = categories;
    }
}
