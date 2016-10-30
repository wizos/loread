package me.wizos.loread.bean.gson;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Wizos on 2016/3/11.
 */
public class GsSubscriptions {
    @SerializedName("subscriptions")
    ArrayList<Sub> subscriptions;

    public ArrayList<Sub> getSubscriptions() {
        return subscriptions;
    }
    public void setSubscriptions(ArrayList<Sub> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
