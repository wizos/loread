package me.wizos.loread.event;

/**
 * Created by Wizos on 2018/4/26.
 */

public class Sync {
//    public static final String NOTICE = "me.wizos.loread.notice";
//    public static final String N_START = "me.wizos.loread.notice.on-start";
//    public static final String N_COMPLETED = "me.wizos.loread.notice.on-completed";
//    public static final String N_ERROR = "me.wizos.loread.notice.on-error";
//    public static final String N_NEWS = "me.wizos.loread.notice.news";

    public static final int START = 0;
    public static final int DOING = 1;
    public static final int END = 2;
    public static final int ERROR = 3;
    public static final int STOP = 4; // 通知 Service 暂停同步


    public String notice;
    public int result;

    public Sync(int success) {
        this.result = success;
    }

    public Sync(int success, String notice) {
        this.result = success;
        this.notice = notice;
    }


}
