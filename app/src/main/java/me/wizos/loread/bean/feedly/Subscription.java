//package me.wizos.loreadx.bean.feedly;
//
//import com.google.gson.annotations.SerializedName;
//
//import java.util.ArrayList;
//
///**
// * Created by Wizos on 2019/2/8.
// */
//
//public class Subscription {
//    private String id;
//    private String title;
//    private String website;
//    // 可能为空
//    private String iconUrl;
//    // 可能为空
//    private String visualUrl;
//    private String description;
//
//    // 值可能为：zh，en
//    private String language;
//    private int subscribers;
//    private long updated;
//    private float velocity;
//    // 可能为空；部分的; 偏爱的
//    private boolean partial;
//    // 可能为空；可能为 article， longform
//    private String contentType;
//    // 可能为空。值可能为：dead.stale，dormant
//    private String state;
//    // 可能为空
//    private ArrayList<String> topics;
//
//    private ArrayList<TTRSSCategoryItem> categories;
//
//    // estimatedEngagement
//    // coverUrl
//    // logo
//    // coverColor
//    // relatedLayout
//    // relatedTarget
//
//
//
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public void setTitle(String title) {
//        this.title = title;
//    }
//
//    public String getWebsite() {
//        return website;
//    }
//
//    public void setWebsite(String website) {
//        this.website = website;
//    }
//
//    public String getIconUrl() {
//        return iconUrl;
//    }
//
//    public void setIconUrl(String iconUrl) {
//        this.iconUrl = iconUrl;
//    }
//
//    public String getVisualUrl() {
//        return visualUrl;
//    }
//
//    public void setVisualUrl(String visualUrl) {
//        this.visualUrl = visualUrl;
//    }
//
//    public int getSubscribers() {
//        return subscribers;
//    }
//
//    public void setSubscribers(int subscribers) {
//        this.subscribers = subscribers;
//    }
//
//    public long getUpdated() {
//        return updated;
//    }
//
//    public void setUpdated(long updated) {
//        this.updated = updated;
//    }
//
//    public float getVelocity() {
//        return velocity;
//    }
//
//    public void setVelocity(float velocity) {
//        this.velocity = velocity;
//    }
//
//    public boolean isPartial() {
//        return partial;
//    }
//
//    public void setPartial(boolean partial) {
//        this.partial = partial;
//    }
//
//    public String getContentType() {
//        return contentType;
//    }
//
//    public void setContentType(String contentType) {
//        this.contentType = contentType;
//    }
//
//    public String getState() {
//        return state;
//    }
//
//    public void setState(String state) {
//        this.state = state;
//    }
//
//    public ArrayList<String> getTopics() {
//        return topics;
//    }
//
//    public void setTopics(ArrayList<String> topics) {
//        this.topics = topics;
//    }
//
//    public ArrayList<TTRSSCategoryItem> getCategoryItems() {
//        return categories;
//    }
//
//    public void setCategoryItems(ArrayList<TTRSSCategoryItem> categories) {
//        this.categories = categories;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public String getLanguage() {
//        return language;
//    }
//
//    public void setLanguage(String language) {
//        this.language = language;
//    }
//
//    @Override
//    public String toString() {
//        return "Subscription{" +
//                "id='" + id + '\'' +
//                ", title='" + title + '\'' +
//                ", website='" + website + '\'' +
//                ", iconUrl='" + iconUrl + '\'' +
//                ", visualUrl='" + visualUrl + '\'' +
//                ", description='" + description + '\'' +
//                ", language='" + language + '\'' +
//                ", subscribers=" + subscribers +
//                ", updated=" + updated +
//                ", velocity=" + velocity +
//                ", partial=" + partial +
//                ", contentType='" + contentType + '\'' +
//                ", state='" + state + '\'' +
//                ", topics=" + topics +
//                ", categories=" + categories +
//                '}';
//    }
//}
