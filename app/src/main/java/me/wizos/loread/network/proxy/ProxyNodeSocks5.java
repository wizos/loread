package me.wizos.loread.network.proxy;

import java.io.Serializable;

/**
 * 代理节点并不能被用于翻墙，因为会被“GFW”给发现，并重置连接。
 * 只能用于国内反爬虫
 */
public class ProxyNodeSocks5 implements Serializable {
    private String server;
    private int port;
    private String username;
    private String password;

    public ProxyNodeSocks5(String server, int port, String username, String password) {
        this.server = server;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getServer() {
        return server != null ? server : "";
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username != null ? username : "";
    }

    public String getPassword() {
        return password != null ? password : "";
    }

    @Override
    public String toString() {
        return "Socks5ProxyNode{" +
                "server='" + server + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
