//package me.wizos.loreadx.bean.fever;
//
//import com.google.gson.annotations.SerializedName;
//
//import me.wizos.loreadx.App;
//
//public class Feed {
//    @SerializedName("id")
//    private int id;
//    @SerializedName("favicon_id")
//    private int faviconId;
//    @SerializedName("title")
//    private String title;
//    @SerializedName("url")
//    private String url;
//    @SerializedName("site_url")
//    private String siteUrl;
//    @SerializedName("is_spark")
//    private int isSpark;
//    @SerializedName("last_updated_on_time")
//    private long lastUpdatedOnTime; // 到秒
//
//    public int getId() {
//        return id;
//    }
//
//    public int getFaviconId() {
//        return faviconId;
//    }
//
//    public String getTitle() {
//        return title;
//    }
//
//    public String getUrl() {
//        return url;
//    }
//
//    public String getSiteUrl() {
//        return siteUrl;
//    }
//
//    public int getIsSpark() {
//        return isSpark;
//    }
//
//    public long getLastUpdatedOnTime() {
//        return lastUpdatedOnTime;
//    }
//
//
//    public Feed convert(){
//        Feed feed = new Feed();
//        feed.setId("feed/" + id);
//        feed.setTitle(title);
//        feed.setFeedUrl(url);
//        feed.setHtmlUrl(siteUrl);
//        //feed.setIconUrl(visualUrl);
//        feed.setOpenMode(App.OPEN_MODE_RSS);
//        return feed;
//    }
//}
