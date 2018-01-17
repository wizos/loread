package me.wizos.loread.bean.search;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Wizos on 2017/12/31.
 */

public class FeedlyFeedsSearchResult {
    @SerializedName("hint")
    String hint;

    @SerializedName("related")
    ArrayList<String> related;

    @SerializedName("results")
    ArrayList<FeedlyFeed> results;

    @SerializedName("queryType")
    String queryType;

    @SerializedName("scheme")
    String scheme;

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public ArrayList<String> getRelated() {
        return related;
    }

    public void setRelated(ArrayList<String> related) {
        this.related = related;
    }

    public ArrayList<FeedlyFeed> getResults() {
        return results;
    }

    public void setResults(ArrayList<FeedlyFeed> results) {
        this.results = results;
    }
}
