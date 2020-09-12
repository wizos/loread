//package me.wizos.loread.bean.loread;
//
//import com.google.gson.annotations.SerializedName;
//import com.socks.library.KLog;
//
//public class Response<T> {
//    private int code;
//    @SerializedName(value = "msg", alternate = {"error"})
//    private String msg;
//    private T data;
//
//    public boolean isSuccessful() {
//        if (code == 0) {
//            KLog.i("请求正常");
//            return true;
//        }
//        KLog.i("请求异常：" + data);
//        return false;
//    }
//
//    public int getCode() {
//        return code;
//    }
//
//    public void setCode(int code) {
//        this.code = code;
//    }
//
//    public T getData() {
//        return data;
//    }
//
//    public void setData(T data) {
//        this.data = data;
//    }
//
//    public String getMsg() {
//        return msg;
//    }
//
//    public void setMsg(String msg) {
//        this.msg = msg;
//    }
//}
