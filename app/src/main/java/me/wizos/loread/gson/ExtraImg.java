package me.wizos.loread.gson;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Wizos on 2016/10/7.
 */
public class ExtraImg {
    @SerializedName("imgState")
    int imgState;

    @SerializedName("lossImgs")
    ArrayList<String> lossImgs;

    @SerializedName("obtainImgs")
    ArrayList<String> obtainImgs;

    public ArrayList<String> getObtainImgs() {
        return obtainImgs;
    }

    public void setObtainImgs(ArrayList<String> obtainImgs) {
        this.obtainImgs = obtainImgs;
    }

    public int getImgState() {
        return imgState;
    }

    public void setImgState(int imgState) {
        this.imgState = imgState;
    }

    public ArrayList<String> getLossImgs() {
        return lossImgs;
    }

    public void setLossImgs(ArrayList<String> lossImgs) {
        this.lossImgs = lossImgs;
    }
}
