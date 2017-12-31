package me.wizos.loread.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

/**
 * Entity mapped to table "REQUEST_LOG".
 */
@Entity
public class RequestLog {

    @Id
    private long logTime;
    private String url;
    private String method;
    private String headParamString;
    private String bodyParamString;

    @Generated
    public RequestLog() {
    }

    public RequestLog(long logTime) {
        this.logTime = logTime;
    }

    @Generated
    public RequestLog(long logTime, String url, String method, String headParamString, String bodyParamString) {
        this.logTime = logTime;
        this.url = url;
        this.method = method;
        this.headParamString = headParamString;
        this.bodyParamString = bodyParamString;
    }

    public long getLogTime() {
        return logTime;
    }

    public void setLogTime(long logTime) {
        this.logTime = logTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getHeadParamString() {
        return headParamString;
    }

    public void setHeadParamString(String headParamString) {
        this.headParamString = headParamString;
    }

    public String getBodyParamString() {
        return bodyParamString;
    }

    public void setBodyParamString(String bodyParamString) {
        this.bodyParamString = bodyParamString;
    }

}
