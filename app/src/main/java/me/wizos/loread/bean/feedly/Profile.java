package me.wizos.loread.bean.feedly;

import com.google.gson.annotations.SerializedName;

import me.wizos.loread.Contract;
import me.wizos.loread.db.User;

/**
 * @author Wizos on 2019/2/11.
 */

public class Profile {

    /*
    "id": "12cc057f-9891-4ab3-99da-86f2dee7f2f5",
    "client": "feedly",
    "created": 1457934974286,
    "email": "wizos@qq.com",
    "wave": "2016.12",
    "verified": true,
    "login": "wizos@qq.com",
    "logins": [
        {
            "id": "wizos@qq.com",
            "verified": false,
            "provider": "FeedlyLogin",
            "providerId": "wizos@qq.com"
        }
    ],
    "refPage": "welcome",
    "landingPage": "welcome",
    "dropboxConnected": false,
    "twitterConnected": false,
    "facebookConnected": false,
    "evernoteConnected": false,
    "pocketConnected": false,
    "wordPressConnected": false,
    "windowsLiveConnected": false,
    "instapaperConnected": false,
    "source": "feedly.desktop 30.0.1117",
    "fullName": "wizos"
     */

    @SerializedName("id")
    private String id;
    @SerializedName("login")
    private String login;
    @SerializedName("email")
    private String email;
    @SerializedName("fullName")
    private String fullName;

    public User getUser() {
        User user = new User();
        user.setSource(Contract.PROVIDER_FEEDLY);
        user.setId(Contract.PROVIDER_FEEDLY + "_" + id);
        user.setUserId(id);
        user.setUserEmail(email);
        user.setUserName(fullName);
        return user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
}
