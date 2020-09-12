package me.wizos.loread.bean.fever;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import me.wizos.loread.db.Category;

public class Group {
    private int id;
    private String title;
    @SerializedName("feed_ids")
    private String feedIds;

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String[] getFeedIds(){
        if(TextUtils.isEmpty(feedIds)){
            return null;
        }else {
            return feedIds.split(",");
        }
    }

    public Category getCategry(){
        Category category = new Category();
        category.setId("user/" + id);
        category.setTitle(title);
        return category;
    }

}
