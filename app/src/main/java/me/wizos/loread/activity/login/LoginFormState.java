package me.wizos.loread.activity.login;

import androidx.annotation.Nullable;

/**
 * Data validation state of the login form.
 */
public class LoginFormState {
    @Nullable
    private Integer hostHint;
    @Nullable
    private Integer usernameHint;
    @Nullable
    private Integer passwordHint;
    private boolean isDataValid;

    public LoginFormState() { }

    public LoginFormState(@Nullable Integer usernameHint, @Nullable Integer passwordHint) {
        this.usernameHint = usernameHint;
        this.passwordHint = passwordHint;
        this.isDataValid = false;
    }

    public LoginFormState(boolean isDataValid) {
        this.hostHint = null;
        this.usernameHint = null;
        this.passwordHint = null;
        this.isDataValid = isDataValid;
    }


    public void setHostHint(@Nullable Integer hostHint) {
        this.hostHint = hostHint;
    }

    public void setUsernameHint(@Nullable Integer usernameHint) {
        this.usernameHint = usernameHint;
    }

    public void setPasswordHint(@Nullable Integer passwordHint) {
        this.passwordHint = passwordHint;
    }

    @Nullable
    Integer getHostHint() {
        return hostHint;
    }
    @Nullable
    Integer getUsernameHint() {
        return usernameHint;
    }

    @Nullable
    Integer getPasswordHint() {
        return passwordHint;
    }

    public void setDataValid(boolean dataValid) {
        isDataValid = dataValid;
    }

    boolean isDataValid() {
        return isDataValid;
    }
}
