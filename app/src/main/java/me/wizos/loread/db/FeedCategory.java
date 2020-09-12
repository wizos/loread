package me.wizos.loread.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Created by Wizos on 2020/3/17.
 */
@Entity(primaryKeys = {"uid","categoryId","feedId"},
        indices = {@Index({"uid"}),@Index({"categoryId","uid"}),@Index({"feedId","uid"})},
        foreignKeys = {
                @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "uid", onDelete = CASCADE)
//                @ForeignKey(entity = Feed.class, parentColumns = {"id","uid"}, childColumns = {"feedId","uid"}, onDelete = CASCADE),
//                @ForeignKey(entity = Category.class, parentColumns = {"id","uid"}, childColumns = {"categoryId","uid"}, onDelete = CASCADE)
        }
)
public class FeedCategory {
    @NonNull
    private String uid;
    @NonNull
    private String feedId;
    @NonNull
    private String categoryId;

    public FeedCategory(@NonNull String uid, @NonNull String feedId, @NonNull String categoryId){
        this.uid = uid;
        this.feedId = feedId;
        this.categoryId = categoryId;

//        if( feedId.startsWith("feed/") ){
//            this.feedId = feedId;
//        }else {
//            this.feedId = "feed/" + feedId;
//        }
//
//        if( categoryId.startsWith("user/")){
//            this.categoryId = categoryId;
//        }else {
//            this.categoryId = "user/" + categoryId;
//        }
    }

    @Override
    public String toString() {
        return "FeedCategory{" +
                "uid=" + uid +
                ", categoryId='" + categoryId + '\'' +
                ", feedId='" + feedId + '\'' +
                '}';
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    public String getId() {
        return this.uid;
    }


    public void setId(String uid) {
        this.uid = uid;
    }


    public String getCategoryId() {
        return this.categoryId;
    }


    public void setCategoryId(String categoryId) {
        if( !categoryId.startsWith("user/")){
            this.categoryId  = "user/" + categoryId;
        }else {
            this.categoryId = categoryId;
        }
    }


    public String getFeedId() {
        return this.feedId;
    }


    public void setFeedId(String feedId) {
        if( !feedId.startsWith("feed/") ){
            this.feedId = "feed/" + feedId;
        }else {
            this.feedId = feedId;
        }
    }
}
