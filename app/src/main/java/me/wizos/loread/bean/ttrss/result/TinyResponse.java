package me.wizos.loread.bean.ttrss.result;

import com.google.gson.annotations.SerializedName;

public class TinyResponse<T> {
    private int seq;
    private int status;
    @SerializedName(value = "msg", alternate = {"error"})
    private String msg;
    private T content;

    public boolean isSuccessful() {
        if (status == 0) {
            //KLog.i("请求正常");
            return true;
        }
        //KLog.i("请求异常：" + content);
        return false;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }


    @Override
    public String toString() {
        return "TTRSSResponse{" +
                "seq=" + seq +
                ", status=" + status +
                ", msg='" + msg + '\'' +
                ", content=" + content +
                '}';
    }
}
