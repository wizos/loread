package me.wizos.loread.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.Token;


/**
 * Created by Wizos on 2020/3/17.
 */
@Entity(
        indices = {@Index({"id"}),@Index({"source"}),@Index({"userId"})} )
public class User {
    @NonNull
    @PrimaryKey
    private String id;
    // 账户信息
    private String source;
    private String userId; // 该用户在服务商那的id
    private String userName; // 该用户在服务商那的name
    private String userEmail;
    private String userPassword;

    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private String auth;
    private long expiresTimestamp = 0;

    // 上次操作的状态
    private String streamId = "user/" + userId + App.CATEGORY_ALL;
    private String streamTitle = App.i().getString(R.string.all);

    private int streamType = App.TYPE_GROUP;
    private int streamStatus = App.STATUS_ALL;

    // 个人设置偏好
    private boolean autoSync = true;

    // 自动同步的时间间隔，单位为分钟
    private int autoSyncFrequency = 30;
    private boolean autoSyncOnlyWifi = false;

    private boolean downloadImgOnlyWifi = false;
    private boolean openLinkBySysBrowser = false;
    //是否滚动标记为已读
    private boolean markReadOnScroll = false;
    // 缓存的天数
    private int cachePeriod = 7;
    private boolean autoToggleTheme = true;
    private int themeMode = App.THEME_DAY;
    private float audioSpeed = 1.0f;
    private String host;

    public void setToken(Token token) {
        if (null == token) {
            return;
        }
        accessToken = token.getAccess_token();
        refreshToken = token.getRefresh_token();
        tokenType = token.getToken_type();
        expiresTimestamp = token.getExpires_in() + (System.currentTimeMillis() / 1000);
        auth = token.getAuth();
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public long getExpiresTimestamp() {
        return expiresTimestamp;
    }

    public void setExpiresTimestamp(long expiresTimestamp) {
        this.expiresTimestamp = expiresTimestamp;
    }

    public int getStreamType(){
        return streamType;
    }
    public void setStreamType(int streamType) {
        this.streamType = streamType;
    }

    public String getStreamId() {
        return streamId;
    }
    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getStreamTitle() {
        return streamTitle;
    }

    public void setStreamTitle(String streamTitle) {
        this.streamTitle = streamTitle;
    }

    public int getStreamStatus() {
        return streamStatus;
    }

    public void setStreamStatus(int streamStatus) {
        this.streamStatus = streamStatus;
    }

    public boolean isAutoSync() {
        return autoSync;
    }

    public void setAutoSync(boolean autoSync) {
        this.autoSync = autoSync;
    }

    public int getAutoSyncFrequency() {
        return autoSyncFrequency;
    }

    /**
     * @param autoSyncFrequency 分钟
     */
    public void setAutoSyncFrequency(int autoSyncFrequency) {
        this.autoSyncFrequency = autoSyncFrequency;
    }

    public boolean isAutoSyncOnlyWifi() {
        return autoSyncOnlyWifi;
    }

    public void setAutoSyncOnlyWifi(boolean autoSyncOnlyWifi) {
        this.autoSyncOnlyWifi = autoSyncOnlyWifi;
    }

    public boolean isDownloadImgOnlyWifi() {
        return downloadImgOnlyWifi;
    }

    public void setDownloadImgOnlyWifi(boolean downloadImgOnlyWifi) {
        this.downloadImgOnlyWifi = downloadImgOnlyWifi;
    }

    public boolean isOpenLinkBySysBrowser() {
        return openLinkBySysBrowser;
    }

    public void setOpenLinkBySysBrowser(boolean openLinkBySysBrowser) {
        this.openLinkBySysBrowser = openLinkBySysBrowser;
    }

    public boolean isMarkReadOnScroll() {
        return markReadOnScroll;
    }

    public void setMarkReadOnScroll(boolean markReadOnScroll) {
        this.markReadOnScroll = markReadOnScroll;
    }

    public int getCachePeriod() {
        return cachePeriod;
    }

    public void setCachePeriod(int cachePeriod) {
        this.cachePeriod = cachePeriod;
    }

    public boolean isAutoToggleTheme() {
        return autoToggleTheme;
    }

    public void setAutoToggleTheme(boolean autoToggleTheme) {
        this.autoToggleTheme = autoToggleTheme;
    }

    public int getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(int themeMode) {
        this.themeMode = themeMode;
    }

    public float getAudioSpeed() {
        return audioSpeed;
    }

    public void setAudioSpeed(float audioSpeed) {
        this.audioSpeed = audioSpeed;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", source='" + source + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", userPassword='" + userPassword + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", auth='" + auth + '\'' +
                ", expiresTimestamp=" + expiresTimestamp +
                ", streamId='" + streamId + '\'' +
                ", streamTitle='" + streamTitle + '\'' +
                ", streamStatus=" + streamStatus +
                ", autoSync=" + autoSync +
                ", autoSyncFrequency=" + autoSyncFrequency +
                ", autoSyncOnlyWifi=" + autoSyncOnlyWifi +
                ", downloadImgOnlyWifi=" + downloadImgOnlyWifi +
                ", openLinkBySysBrowser=" + openLinkBySysBrowser +
                ", markReadOnScroll=" + markReadOnScroll +
                ", cachePeriod=" + cachePeriod +
                ", autoToggleTheme=" + autoToggleTheme +
                ", themeMode=" + themeMode +
                ", audioSpeed=" + audioSpeed +
                '}';
    }
}
