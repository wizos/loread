/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-02-09 05:13:18
 */

package me.wizos.loread.bean.jsonfeed;

public class Hub {
    String type;
    String url;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Hub{" +
                "type='" + type + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
