package me.wizos.loread.event;

/**
 * Created by Wizos on 2018/4/26.
 */

public class Login {
    boolean success = false;
    String info;

    public Login(boolean success) {
        this.success = success;
    }

    public Login(boolean success, String info) {
        this.success = success;
        this.info = info;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
