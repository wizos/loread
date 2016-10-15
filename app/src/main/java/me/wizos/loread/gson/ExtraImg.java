package me.wizos.loread.gson;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Wizos on 2016/10/7.
 */
public class ExtraImg {
    // 0 是无图，0 是在还有待下载的， 1 是全部下载完成
    @SerializedName("imgState")
    int imgState;

    // 每个src要记录，网络src和要保存到本地的src
    @SerializedName("lossImgs")
    ArrayList<SrcPair> lossImgs;

    @SerializedName("obtainImgs")
    ArrayList<SrcPair> obtainImgs;

    public int getImgState() {
        return imgState;
    }

    public void setImgState(int imgState) {
        this.imgState = imgState;
    }

    public ArrayList<SrcPair> getLossImgs() {
        return lossImgs;
    }

    public void setLossImgs(ArrayList<SrcPair> lossImgs) {
        this.lossImgs = lossImgs;
    }

    public ArrayList<SrcPair> getObtainImgs() {
        return obtainImgs;
    }

    public void setObtainImgs(ArrayList<SrcPair> obtainImgs) {
        this.obtainImgs = obtainImgs;
    }
}
