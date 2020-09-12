package me.wizos.loread.bean.inoreader;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;

import me.wizos.loread.bean.inoreader.itemContents.Item;
import me.wizos.loread.bean.inoreader.itemContents.Self;


@Parcel
public class StreamContents {
    @SerializedName("id")
    String id;
    @SerializedName("title")
    String title;

    @SerializedName("items")
    ArrayList<Item> items;

    @SerializedName("continuation")
    String continuation;

    @SerializedName("updated")
    long updated;
    // ino专用
    String description;
    // ino专用
    @SerializedName("updatedUsec")
    long updatedUsec;
    // ino专用
    @SerializedName("self")
    Self self;
    // ino专用
    @SerializedName("direction")
    String direction;

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Self getSelf() {
        return self;
    }

    public void setSelf(Self self) {
        this.self = self;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public long getUpdatedUsec() {
        return updatedUsec;
    }

    public void setUpdatedUsec(long updatedUsec) {
        this.updatedUsec = updatedUsec;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public String getContinuation() {
        return continuation;
    }

    public void setContinuation(String continuation) {
        this.continuation = continuation;
    }
}
