package me.wizos.loread.bean.ttrss.result;

import com.google.gson.annotations.SerializedName;

import org.jetbrains.annotations.NotNull;

public class TinyResponse<T> {
    private int seq;
    private int status;
    @SerializedName(value = "msg", alternate = {"error"})
    private String msg;
    private T content;

    public boolean isSuccessful() {
        return status == 0;
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


    @NotNull
    @Override
    public String toString() {
        return "TinyResponse{" +
                "seq=" + seq +
                ", status=" + status +
                ", msg='" + msg + '\'' +
                ", content=" + content.toString() +
                '}';
    }
}
