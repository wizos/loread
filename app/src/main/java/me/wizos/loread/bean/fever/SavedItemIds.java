package me.wizos.loread.bean.fever;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class SavedItemIds extends BaseResponse {
    @SerializedName("saved_item_ids")
    private String savedItemIds;

    public String[] getSavedItemIds(){
        if(TextUtils.isEmpty(savedItemIds)){
            return null;
        }else {
            return savedItemIds.split(",");
        }
    }

    @Override
    public String toString() {
        return "SavedItemIds{" +
                "savedItemIds='" + savedItemIds + '\'' +
                '}';
    }
}
