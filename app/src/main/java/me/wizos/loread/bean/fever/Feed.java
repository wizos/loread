package me.wizos.loread.bean.fever;

import com.google.gson.annotations.SerializedName;

import me.wizos.loread.App;

public class Feed {
   @SerializedName("id")
   private int id;
   @SerializedName("favicon_id")
   private int faviconId;
   @SerializedName("title")
   private String title;
   @SerializedName("url")
   private String url;
   @SerializedName("site_url")
   private String siteUrl;
   @SerializedName("is_spark")
   private int isSpark;
   @SerializedName("last_updated_on_time")
   private long lastUpdatedOnTime; // 到秒

   public int getId() {
       return id;
   }

   public int getFaviconId() {
       return faviconId;
   }

   public String getTitle() {
       return title;
   }

   public String getUrl() {
       return url;
   }

   public String getSiteUrl() {
       return siteUrl;
   }

   public int getIsSpark() {
       return isSpark;
   }

   public long getLastUpdatedOnTime() {
       return lastUpdatedOnTime;
   }


   public me.wizos.loread.db.Feed convert(){
       me.wizos.loread.db.Feed feed = new me.wizos.loread.db.Feed();
       feed.setId(String.valueOf(id));
       feed.setTitle(title);
       feed.setFeedUrl(url);
       feed.setHtmlUrl(siteUrl);
       // feed.setIconUrl(visualUrl);
       feed.setDisplayMode(App.OPEN_MODE_RSS);
       return feed;
   }

    @Override
    public String toString() {
        return "Feed{" +
                "id=" + id +
                ", faviconId=" + faviconId +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", siteUrl='" + siteUrl + '\'' +
                ", isSpark=" + isSpark +
                ", lastUpdatedOnTime=" + lastUpdatedOnTime +
                '}';
    }
}
