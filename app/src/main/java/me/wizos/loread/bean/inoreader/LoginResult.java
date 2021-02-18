package me.wizos.loread.bean.inoreader;

import android.text.TextUtils;

import me.wizos.loread.App;
import me.wizos.loread.R;

/**
 * Created by Wizos on 2019/3/27.
 */

public class LoginResult {
    public boolean success = false;

    private String error;
    private String auth;

    public LoginResult(String result) {
        if (TextUtils.isEmpty(result)) {
            return;
        }

        String[] info = result.split("\n");
        if (info.length == 0) {
            return;
        }

        if (info[0].startsWith("Error=")) {
            error = info[0].replace("Error=", "");
            if (error.toLowerCase().equals("wrong_username_or_password")) {
                error = App.i().getString(R.string.wrong_username_or_password);
            } else {
                error = App.i().getString(R.string.wrong_unknown);
            }
        } else {
            for (String tmp : info) {
                if (tmp.startsWith("Auth=")) {
                    success = true;
                    auth = "GoogleLogin " + tmp;
                }
            }
        }
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    @Override
    public String toString() {
        return "LoginResult{" +
                "success=" + success +
                ", error='" + error + '\'' +
                ", auth='" + auth + '\'' +
                '}';
    }
}
