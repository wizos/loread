package me.wizos.loread.bean.ttrss.request;

public class LoginParam {
    private String user = "admin";
    private String password;
    private String op = "login";

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "login{" +
                "user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", op='" + op + '\'' +
                '}';
    }
}
