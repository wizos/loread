package me.wizos.loread.bean.inoreader;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Wizos on 2016/3/11.
 */
public class Subscriptions {
    @SerializedName("subscriptions")
    private ArrayList<Subscription> subscriptions;
    public ArrayList<Subscription> getSubscriptions() {
        return subscriptions;
    }
}
