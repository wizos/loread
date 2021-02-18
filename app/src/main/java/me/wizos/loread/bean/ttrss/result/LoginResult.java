package me.wizos.loread.bean.ttrss.result;

import com.google.gson.annotations.SerializedName;

public class LoginResult {
    @SerializedName("session_id")
    private String sessionId;
    @SerializedName("api_level")
    private int apiLevel;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getApiLevel() {
        return apiLevel;
    }

    public void setApiLevel(int apiLevel) {
        this.apiLevel = apiLevel;
    }
}
