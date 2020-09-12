package me.wizos.loread.bean.inoreader;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Wizos on 2016/3/10.
 */
public class ItemRefs {

    @SerializedName("id")
    String id;

    @SerializedName("directStreamIds")
    ArrayList<String> directStreamIds;

    @SerializedName("timestampUsec")
    long timestampUsec;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDirectStreamIds(ArrayList<String> directStreamIds) {
        this.directStreamIds = directStreamIds;
    }

    public ArrayList<String> getDirectStreamIds() {
        return directStreamIds;
    }

    public void setTimestampUsec(long timestampUsec) {
        this.timestampUsec = timestampUsec;
    }

    public long getTimestampUsec() {
        return timestampUsec;
    }

    public String getLongId() {
        String idHex = Long.toHexString(Long.valueOf(id));
        return "tag:google.com,2005:reader/item/" + String.format("%0" + (16 - idHex.length()) + "d", 0) + idHex;
        // String.format("%0"+length+"d", arr) 中
        // (16 - id.length())代表的是格式化后字符串的总长度。
        // d是个占位符，会被arr所替换。arr必须是数字
        // 0是在arr转化为字符后，长度达不到length的时候，前面以0 不足。
        // 不能写出 "tag:google.com,2005:reader/item/" + String.format("%0" + 16 + "d", id)
    }
}
