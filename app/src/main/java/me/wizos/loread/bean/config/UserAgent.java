package me.wizos.loread.bean.config;


/**
 * @author Wizos on 2018/6/28.
 */

public class UserAgent {
    private String name;
    private String value;

    public UserAgent(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
