package me.wizos.loread.bean.feedly.input;

import com.google.gson.annotations.SerializedName;

import java.util.Collection;
import java.util.List;

/**
 * Created by Wizos on 2019/2/24.
 */

public class MarkerAction {
    public final static String MARK_AS_READ = "markAsRead";
    public final static String MARK_AS_UNREAD = "keepUnread";
    public final static String UNDO_MARK_AS_READ = "undoMarkAsRead";
    public final static String MARK_AS_SAVED = "markAsSaved";
    public final static String MARK_AS_UNSAVED = "markAsUnsaved";

    public final static String TYPE_ENTRIES = "entries";
    public final static String TYPE_FEEDS = "feeds";
    public final static String TYPE_CATEGORIES = "categories";
    public final static String TYPE_TAGS = "tags";

    /**
     * markAsRead，keepUnread，undoMarkAsRead，markAsSaved，markAsUnsaved
     */
    @SerializedName("action")
    private String action;
    /**
     * entries，feeds，categories，tags
     */
    @SerializedName("type")
    private String type;

    @SerializedName("entryIds")
    private Collection<String> entryIds;

    @SerializedName("feedIds")
    private Collection<String> feedIds;

    @SerializedName("categoryIds")
    private Collection<String> categoryIds;

   // @SerializedName("lastReadEntryId")
   // private String lastReadEntryId;
   //
   // // 时间戳替代(不太准确)
   // @SerializedName("asOf")
   // private long asOf;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Collection<String> getEntryIds() {
        return entryIds;
    }

    public void setEntryIds(Collection<String> entryIds) {
        this.entryIds = entryIds;
    }

    public Collection<String> getFeedIds() {
        return feedIds;
    }

    public void setFeedIds(List<String> feedIds) {
        this.feedIds = feedIds;
    }

    public Collection<String> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<String> categoryIds) {
        this.categoryIds = categoryIds;
    }

    @Override
    public String toString() {
        return "MarkerAction{" +
                "action='" + action + '\'' +
                ", type='" + type + '\'' +
                ", entryIds=" + entryIds +
                ", feedIds=" + feedIds +
                ", categoryIds=" + categoryIds +
                '}';
    }
}
