package me.wizos.loread.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Wizos on 2016/5/28.
 */
public class SrcPair {

    public SrcPair(String netSrc,String localSrc){
        this.netSrc = netSrc;
        this.localSrc = localSrc;
    }

    @SerializedName("netSrc")
    String netSrc;
    @SerializedName("localSrc")
    String localSrc;

    public String getNetSrc() {
        return netSrc;
    }
    public void setNetSrc(String netSrc) {
        this.netSrc = netSrc;
    }
    public String getLocalSrc() {
        return localSrc;
    }
    public void setLocalSrc(String localSrc) {
        this.localSrc = localSrc;
    }
//    }
}
