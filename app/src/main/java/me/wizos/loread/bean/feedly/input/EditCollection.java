package me.wizos.loread.bean.feedly.input;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Wizos on 2019/2/24.
 */

public class EditCollection {
    /**
     * 此集合的唯一标签；新类别所需，编辑现有类别时可选
     */
    @SerializedName("label")
    private String label;
    /**
     * 可空。Collection的标识。如果缺失，服务器将生成一个(新集合Collection)。
     */
    @SerializedName("id")
    private String id;
    /**
     * 这个集合的更详细的描述。
     */
    @SerializedName("description")
    private String description;
    /**
     * 可空。feed列表，表示要添加到此集合的feed列表。
     */
    // 必须没有categories
    @SerializedName("feeds")
    private ArrayList<EditFeed> feeds;

    public EditCollection(String id) {
        this.id = id;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<EditFeed> getFeeds() {
        return feeds;
    }

    public void setFeeds(ArrayList<EditFeed> feeds) {
        this.feeds = feeds;
    }
}
