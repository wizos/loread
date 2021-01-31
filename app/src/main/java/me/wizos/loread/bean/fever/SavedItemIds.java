package me.wizos.loread.bean.fever;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SavedItemIds extends FeverResponse {
    @SerializedName("saved_item_ids")
    private String savedItemIds;

    public List<String> getSavedItemIds(){
        if(TextUtils.isEmpty(savedItemIds)){
            return new ArrayList<>();
        }else {
            return Arrays.asList(savedItemIds.split(","));
        }
    }

    @NotNull
    @Override
    public String toString() {
        return "SavedItemIds{" +
                "savedItemIds='" + savedItemIds + '\'' +
                '}';
    }
}
