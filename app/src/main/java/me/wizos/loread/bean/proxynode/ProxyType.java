package me.wizos.loread.bean.proxynode;

import me.wizos.loread.gson.GsonEnum;

public enum ProxyType implements GsonEnum<ProxyType> {
    DIRECT("DIRECT"), HTTP("HTTP"), SOCKS("SOCKS");
    private final String proxyType;
    ProxyType(String proxyType) {
        this.proxyType = proxyType;
    }


    public String getProxyType() {
        return proxyType;
    }

    public static ProxyType parse(String type) {
        switch (type) {
            case "DIRECT":
                return ProxyType.DIRECT;
            case "HTTP":
                return ProxyType.HTTP;
            case "SOCKS":
                return ProxyType.SOCKS;
            default:
                throw new IllegalArgumentException("There is not enum names with [" + type + "] of type AnonymityLevel exists! ");
        }
    }

    @Override
    public ProxyType deserialize(String jsonEnum) {
        return ProxyType.parse(jsonEnum);
    }

    @Override
    public String serialize() {
        return this.getProxyType();
    }
}
