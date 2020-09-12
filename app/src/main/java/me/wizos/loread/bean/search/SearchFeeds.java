package me.wizos.loread.bean.search;

import java.util.ArrayList;

/**
 * Created by Wizos on 2017/12/31.
 */

public class SearchFeeds {
    private String hint;
    private ArrayList<String> related;
    private ArrayList<SearchFeedItem> results;
    private String queryType;
    private String scheme;

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

    public ArrayList<SearchFeedItem> getResults() {
        return results;
    }

    public void setResults(ArrayList<SearchFeedItem> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "SearchFeeds{" +
                "hint='" + hint + '\'' +
                ", related=" + related +
                ", results=" + results +
                ", queryType='" + queryType + '\'' +
                ", scheme='" + scheme + '\'' +
                '}';
    }
}
