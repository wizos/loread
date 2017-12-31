//package me.wizos.loread.bean.gson;
//
//import com.google.gson.annotations.SerializedName;
//
//import java.util.ArrayList;
//
//import me.wizos.loread.bean.gson.itemContents.Items;
//import me.wizos.loread.bean.gson.itemContents.Self;
//
///**
// * 该 api 貌似被官方弃用，改用 stream content 了
// * Created by Wizos on 2016/3/11.
// */
//public class GsItemContents {
//
//    @SerializedName("direction")
//    String direction;
//
//    @SerializedName("id")
//    String id;
//
//    @SerializedName("title")
//    String title;
//
//    @SerializedName("description")
//    String description;
//
//    @SerializedName("self")
//    Self self;
//
//    @SerializedName("updated")
//    long updated;
//
//    @SerializedName("updatedUsec")
//    long updatedUsec;
//
//    @SerializedName("items")
//    ArrayList<Items> items;
//
//    @SerializedName("continuation")
//    String continuation;
//
//
//    public String getDirection() {
//        return direction;
//    }
//    public void setDirection(String direction) {
//        this.direction = direction;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public Self getSelf() {
//        return self;
//    }
//    public void setSelf(Self self) {
//        this.self = self;
//    }
//
//    public long getUpdated() {
//        return updated;
//    }
//    public void setUpdated(long updated) {
//        this.updated = updated;
//    }
//
//    public long getUpdatedUsec() {
//        return updatedUsec;
//    }
//    public void setUpdatedUsec(long updatedUsec) {
//        this.updatedUsec = updatedUsec;
//    }
//
//    public ArrayList<Items> getItems() {
//        return items;
//    }
//    public void setItems(ArrayList<Items> items) {
//        this.items = items;
//    }
//
//    public String getContinuation() {
//        return continuation;
//    }
//    public void setContinuation(String continuation) {
//        this.continuation = continuation;
//    }
//}
//
//
//
//
//
