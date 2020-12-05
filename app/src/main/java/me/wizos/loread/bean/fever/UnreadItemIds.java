package me.wizos.loread.bean.fever;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class UnreadItemIds extends BaseResponse {
    @SerializedName("unread_item_ids")
    private String unreadItemIds;
    public String[] getUnreadItemIds(){
        if(TextUtils.isEmpty(unreadItemIds)){
            return null;
        }else {
            return unreadItemIds.split(",");
        }
    }

    @Override
    public String toString() {
        return "UnreadItemIds{" +
                "unreadItemIds='" + unreadItemIds + '\'' +
                '}';
    }
}
