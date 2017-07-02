//package me.wizos.loread.bean;
//
//import android.support.v4.util.ArrayMap;
//
//import com.google.gson.annotations.SerializedName;
//
///**
// * 主要是为了将无网状态下的一些请求，转为 json 保存到本地文件<br/>
// * Created by Wizos on 2016/10/30.
// */
//
//public class RequestCache {
//    @SerializedName("logTime")
//    private long logTime;
//    @SerializedName("url")
//    private String url;
//    @SerializedName("method")
//    private String method;
//    @SerializedName("headerMap")
//    private ArrayMap<String,String> headerMap;
//    @SerializedName("bodyParamMap")
//    private ArrayMap<String,String> bodyParamMap;
//
//    public long getLogTime() {
//        return logTime;
//    }
//
//    public void setLogTime(long logTime) {
//        this.logTime = logTime;
//    }
//
//    public String getUrl() {
//        return url;
//    }
//
//    public void setUrl(String url) {
//        this.url = url;
//    }
//
//    public String getMethod() {
//        return method;
//    }
//
//    public void setMethod(String method) {
//        this.method = method;
//    }
//
//    public ArrayMap<String, String> getHeaderMap() {
//        return headerMap;
//    }
//
//    public void setHeaderMap(ArrayMap<String, String> headerMap) {
//        this.headerMap = headerMap;
//    }
//
//    public ArrayMap<String, String> getBodyParamMap() {
//        return bodyParamMap;
//    }
//
//    public void setBodyParamMap(ArrayMap<String, String> bodyParamMap) {
//        this.bodyParamMap = bodyParamMap;
//    }
//}
