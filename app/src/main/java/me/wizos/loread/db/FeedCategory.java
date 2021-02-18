package me.wizos.loread.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

import static androidx.room.ForeignKey.CASCADE;

/**
 * Created by Wizos on 2020/3/17.
 */
@Entity(primaryKeys = {"uid","categoryId","feedId"},
        indices = {@Index({"uid"}),@Index({"categoryId","uid"}),@Index({"feedId","uid"})},
        foreignKeys = {
                @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "uid", onDelete = CASCADE)
                // @ForeignKey(entity = Feed.class, parentColumns = {"id","uid"}, childColumns = {"feedId","uid"}, onDelete = CASCADE),
                // @ForeignKey(entity = Category.class, parentColumns = {"id","uid"}, childColumns = {"categoryId","uid"}, onDelete = CASCADE)
        }
)
public class FeedCategory {
    @NonNull
    private String uid;
    @NonNull
    private String feedId;
    @NonNull
    private String categoryId;

    @Ignore
    public FeedCategory() {}

    public FeedCategory(@NonNull String uid, @NonNull String feedId, @NonNull String categoryId){
        this.uid = uid;
        this.feedId = feedId;
        this.categoryId = categoryId;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    public void setFeedId(@NonNull String feedId) {
        this.feedId = feedId;
    }

    public void setCategoryId(@NonNull String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryId() {
        return this.categoryId;
    }


    public String getFeedId() {
        return this.feedId;
    }

    @Override
    public String toString() {
        return "FeedCategory{" +
                "uid=" + uid +
                ", categoryId='" + categoryId + '\'' +
                ", feedId='" + feedId + '\'' +
                '}';
    }

}
