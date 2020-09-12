package me.wizos.loread.activity.login;

import androidx.annotation.Nullable;

/**
 * Authentication result : success (user details) or error message.
 */
public class LoginResult {
    @Nullable
    private boolean success;
    @Nullable
    private String data;

    public LoginResult() {
    }

    @Nullable
    public LoginResult setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    @Nullable
    public boolean isSuccess() {
        return success;
    }

    @Nullable
    public String getData() {
        return data;
    }

    public LoginResult setData(@Nullable String data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "LoginResult{" +
                "success=" + success +
                ", data='" + data + '\'' +
                '}';
    }
}
