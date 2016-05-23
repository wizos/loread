package me.wizos.loread.net;

import java.util.ArrayList;

/**
 * Created by Wizos on 2016/4/22.
 */
public class Requests {
    String url;
    String method;
    long logTime;
    String headParamString;
    String bodyParamString;
    ArrayList<Param> headParamList;
    ArrayList<Param> bodyParamList;

    public Requests(String url,String method,long logTime,String headParamString,String bodyParamString){
        this.url = url;
        this.method = method;
        this.logTime = logTime;
        this.headParamString = headParamString;
        this.bodyParamString = bodyParamString;
    }


    void setUrl(String url){
        this.url = url;
    }
    String getUrl(){
        return url;
    }

    void setHeadParamString(String headParamString){
        this.headParamString = headParamString;
    }
    String getHeadParamString(){
        return headParamString;
    }

    void setBodyParamString(String bodyParamString){
        this.bodyParamString = bodyParamString;
    }
    String getBodyParamString(){
        return bodyParamString;
    }

    void setMethod(String method){
        this.method = method;
    }
    String getMethod(){
        return method;
    }

    void setHeadParamList(ArrayList<Param> headParamList){
        this.headParamList = headParamList;
    }
    ArrayList<Param> getHeadParamList(){
        return headParamList;
    }

    void setBodyParamList(ArrayList<Param> bodyParamList){
        this.bodyParamList = bodyParamList;
    }
    ArrayList<Param> getBodyParamList(){
        return bodyParamList;
    }


    private class Param {
        private String key;
        private String value;

        Param(String key, String value){
            this.key = key;
            this.value = value;
        }
        public void setKey(String key){
            this.key = key;
        }
        public String getKey(){
            return key;
        }

        public void setValue(String value){
            this.value = value;
        }
        public String getValue(){
            return value;
        }
    }



}
