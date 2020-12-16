package me.wizos.loread.bean.fever;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class UnreadItemIds extends BaseResponse {
    @SerializedName("unread_item_ids")
    private String unreadItemIds;

    public List<String> getUnreadItemIds(){
        if(TextUtils.isEmpty(unreadItemIds)){
            return null;
        }else {
            return Arrays.asList(unreadItemIds.split(","));
        }
    }

    @Override
    public String toString() {
        return "UnreadItemIds{" +
                "unreadItemIds='" + unreadItemIds + '\'' +
                '}';
    }
}
