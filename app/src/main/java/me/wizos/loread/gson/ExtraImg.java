package me.wizos.loread.gson;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

/**
 * Created by Wizos on 2016/10/7.
 */
public class ExtraImg {
    // -1 是无图，0 是在还有待下载的， 1 是全部下载完成
    public final static int DOWNLOAD_ING = 0;
    public final static int DOWNLOAD_OVER = 1;

    @SerializedName("imgStatus")
    private int imgStatus;

    // 每个src要记录，网络src和要保存到本地的src
    @SerializedName("lossImgs")
    private HashMap<Integer,SrcPair> lossImgs;

    @SerializedName("obtainImgs")
    private HashMap<Integer,SrcPair> obtainImgs;

    public int getImgStatus() {
        return imgStatus;
    }

    public void setImgStatus(int imgStatus) {
        this.imgStatus = imgStatus;
    }

    public HashMap<Integer, SrcPair> getLossImgs() {
        return lossImgs;
    }

    public void setLossImgs(HashMap<Integer, SrcPair> lossImgs) {
        this.lossImgs = lossImgs;
    }

    public HashMap<Integer, SrcPair> getObtainImgs() {
        return obtainImgs;
    }

    public void setObtainImgs(HashMap<Integer, SrcPair> obtainImgs) {
        this.obtainImgs = obtainImgs;
    }
}
