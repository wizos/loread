package me.wizos.loread.network.proxy;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.net.Proxy;

/**
 * 代理节点并不能被用于翻墙，因为会被“GFW”给发现，并重置连接。
 * 只能用于国内反爬虫
 */
public class ProxyNode implements Serializable {
    public ProxyType type;
    public AnonymityLevel level;
    public String hostname;
    public int port;

    public ProxyNode() {
    }

    public ProxyNode(ProxyType type, AnonymityLevel level, String hostname, int port) {
        this.type = type;
        this.level = level;
        this.hostname = hostname;
        this.port = port;
    }

    @NotNull
    @Override
    public String toString() {
        return "ProxyConfig{" +
                "type=" + type +
                ", level=" + level +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                '}';
    }


    public Proxy.Type getProxyType() {
        switch (type) {
            case DIRECT:
                return Proxy.Type.DIRECT;
            case HTTP:
                return Proxy.Type.HTTP;
            case SOCKS:
                return Proxy.Type.SOCKS;
            default:
                throw new IllegalArgumentException("There is not enum names with [" + level + "] of type AnonymityLevel exists! ");
        }
    }
}
