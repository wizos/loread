package me.wizos.loread.data;

import android.app.Activity;
import android.content.SharedPreferences;

import me.wizos.loread.App;

/**
 * @author Wizos
 * @date 2016/4/30
 * 内部设置
 */
public class PrefUtils {
    private static PrefUtils prefUtils;
    private static SharedPreferences mySharedPreferences;
    private static SharedPreferences.Editor editor;

    public static final String PREF_NAME = App.APP_NAME_EN;
//    public static final String REFRESH_INTERVAL = "refresh.interval";
//    public static final String SIXTY_MINUTES = "3600000";


    private PrefUtils() {
    }

    public static PrefUtils i() {
        // 双重锁定，只有在 mySharedPreferences 还没被初始化的时候才会进入到下一行，然后加上同步锁
        if (prefUtils == null) {
            // 同步锁，避免多线程时可能 new 出两个实例的情况
            synchronized (PrefUtils.class) {
                if (prefUtils == null) {
                    prefUtils = new PrefUtils();
                    mySharedPreferences = App.i().getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
                    editor = mySharedPreferences.edit();
                }
            }
        }
        return prefUtils;
    }

    public void clear() {
        editor.clear();
        editor.apply();
    }

    private String read(String key, String defaultValue) {
        return mySharedPreferences.getString(key, defaultValue);//getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值
    }

    private void save(String key, String value) {
//        SharedPreferences.Editor editor = mySharedPreferences.edit();//实例化SharedPreferences.Editor对象
        editor.putString(key, value); //用putString的方法保存数据
        editor.apply(); //提交当前数据
    }

    private boolean read(String key, boolean defaultValue) {
        return mySharedPreferences.getBoolean(key, defaultValue);
    }

    private void save(String key, boolean value) {
        //用putString的方法保存数据
        editor.putBoolean(key, value);
        editor.apply(); //提交当前数据
    }

    private int read(String key, int value) {
        return mySharedPreferences.getInt(key, value);
    }

    private void save(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    private long read(String key, long value) {
        return mySharedPreferences.getLong(key, value);
    }

    private void save(String key, long value) {
        editor.putLong(key, value);
        editor.apply();
    }



    public String getAuth() {
        return read("Auth", "");
    }

    public void setAuth(String auth) {
        save("Auth", auth);
    }


    public String getAccountID() {
        return read("AccountID", "");
    }

    public void setAccountID(String accountID) {
        save("AccountID", accountID);
    }

    public String getAccountPD() {
        return read("AccountPD", "");
    }

    public void setAccountPD(String accountPD) {
        save("AccountPD", accountPD);
    }

    public long getUseId() {
        return read("UserID", 0L);
    }
    public void setUseId(long useId) {
        save("UserID", useId);
    }

    public String getUseName() {
        return read("UserName", "");
    }

    public void setUseName(String useName) {
        save("UserName", useName);
    }


    public boolean isSyncFirstOpen() {
        return read("SyncFirstOpen", true);
    }

    public void setSyncFirstOpen(boolean syncFirstOpen) {
        save("SyncFirstOpen", syncFirstOpen);
    }

    public boolean isSyncAllStarred() {
        return read("SyncAllStarred", false);
    }
    public void setSyncAllStarred(boolean syncAllStarred) {
        save("SyncAllStarred", syncAllStarred);
    }

    public boolean isHadSyncAllStarred() {
        return read("HadSyncAllStarred", false);
    }
    public void setHadSyncAllStarred(boolean had) {
        save("HadSyncAllStarred", had);
    }

    public String getSyncFrequency() {
        return read("SyncFrequency", "");
    }

    public void setSyncFrequency(String syncFrequency) {
        save("SyncFrequency", syncFrequency);
    }

    public int getClearBeforeDay() {
        return read("ClearBeforeDay", 7);
    }

    public void setClearBeforeDay(int clearBeforeDay) {
        save("ClearBeforeDay", clearBeforeDay);
    }


    public boolean isDownImgWifi() {
        return read("DownImgWifi", true);
    }
    public void setDownImgWifi(boolean downImgMode) {
        save("DownImgWifi", downImgMode);
    }

    public boolean isInoreaderProxy() {
        return read("InoreaderProxy", false);
    }
    public void setInoreaderProxy(boolean proxyMode) {
        save("InoreaderProxy", proxyMode);
    }

    public String getInoreaderProxyHost() {
        return read("InoreaderProxyHost", "https://");
    }

    public void setInoreaderProxyHost(String host) {
        save("InoreaderProxyHost", host);
    }
    /**
     * 是否为滚动标记为已读
     */
    public boolean isScrollMark() {
        return read("ScrollMark", true);
    }

    /**
     * 设置滚动标记为已读
     */
    public void setScrollMark(boolean scrollMark) {
        save("ScrollMark", scrollMark);
    }


    public String getStreamState() {
        return read("ListState", "Unread");
    }

    public void setStreamState(String listState) {
        save("ListState", listState);
    }


    public String getStreamId() {
        return read("listTagId", "");
    }

    public void setStreamId(String listTagId) {
        save("listTagId", listTagId);
    }


    public boolean isOrderTagFeed() { // StreamPrefs
        return read("OrderTagFeed", true);
    }
    public void setOrderTagFeed(boolean is) {
        save("OrderTagFeed", is);
    }

    public boolean isSysBrowserOpenLink() {
        return read("SysBrowserOpenLink", true);
    }
    public void setSysBrowserOpenLink(boolean is) {
        save("SysBrowserOpenLink", is);
    }

//    public boolean isLeftRightSlideArticle() {
//        return read("LeftRightSlideArticle", true);
//    }
//    public void setLeftRightSlideArticle(boolean is) {
//        save("LeftRightSlideArticle", is);
//    }

    public boolean isAutoToggleTheme() {
        return read("AutoToggleTheme", true);
    }
    public void setAutoToggleTheme(boolean is) {
        save("AutoToggleTheme", is);
    }

    public int getThemeMode() {
        return read("ThemeMode", App.theme_Day);
    }
    public void setThemeMode(int themeMode) {
        save("ThemeMode", themeMode);
    }


    public int getAutoRefreshInterval() {
        return read("AutoRefreshInterval", 0);
    }

    public void setAutoRefreshInterval(int timeInterval) {
        save("AutoRefreshInterval", timeInterval);
    }

//    public boolean autoRefreshInterval() {
//        return read("AutoRefreshInterval", true);
//    }
//    public void enableAutoRefresh() {
//        save("AutoRefreshEnabled", true);
//    }


    public String getTagsSortId() {
        return read("TagsSortId", "");
    }

    public void setTagsSortId(String tagsSortId) {
        save("TagsSortId", tagsSortId);
    }


    public String getRequestLog() {
        return read("RequestLog", "");
    }

    public void setRequestLog(String requestLog) {
        save("RequestLog", requestLog);
    }


}
