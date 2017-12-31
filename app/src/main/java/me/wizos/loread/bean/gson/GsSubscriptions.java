package me.wizos.loread.bean.gson;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Wizos on 2016/3/11.
 */
public class GsSubscriptions {
    @SerializedName("subscriptions")
    ArrayList<Subscriptions> subscriptions;

    public ArrayList<Subscriptions> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(ArrayList<Subscriptions> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
