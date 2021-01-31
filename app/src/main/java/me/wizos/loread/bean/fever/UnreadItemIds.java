package me.wizos.loread.bean.fever;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class UnreadItemIds extends FeverResponse {
    @SerializedName("unread_item_ids")
    private String unreadItemIds;

    public List<String> getUnreadItemIds(){
        if(TextUtils.isEmpty(unreadItemIds)){
            return null;
        }else {
            return Arrays.asList(unreadItemIds.split(","));
        }
    }

    @NotNull
    @Override
    public String toString() {
        return "UnreadItemIds{" +
                "unreadItemIds='" + unreadItemIds + '\'' +
                '}';
    }
}
