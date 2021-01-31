package me.wizos.loread.config.url_rewrite;

import android.util.ArrayMap;

import com.google.gson.annotations.SerializedName;

public class UrlRewriteRule {
    // @SerializedName("host_block")
    // public ArraySet<String> hostBlock;

    @SerializedName(value = "host_replace", alternate = {"url_match_domain_rewrite_domain"})
    public ArrayMap<String, String> hostReplace;

    @SerializedName(value = "url_rewrite", alternate = {"url_match_domain_rewrite_url"})
    public ArrayMap<String, String> urlRewrite;

    public UrlRewriteRule(){
        hostReplace = new ArrayMap<>();
        urlRewrite = new ArrayMap<>();
    }
}
