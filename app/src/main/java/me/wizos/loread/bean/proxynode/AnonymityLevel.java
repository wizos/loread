package me.wizos.loread.bean.proxynode;

import me.wizos.loread.gson.GsonEnum;

public enum AnonymityLevel implements GsonEnum<AnonymityLevel> {
//    TRANSPARENT, //"透明代理"
//    ANONYMOUS, // "匿名代理"
//    DISTORTING, // "欺骗性代理"
//    ELITE, // "高匿代理"
//    UNKNOW //"未知代理"

    TRANSPARENT("TRANSPARENT"), ANONYMOUS("ANONYMOUS"), DISTORTING("DISTORTING"), ELITE("ELITE"), UNKNOW("UNKNOW");
    private final String anonymityLevel;
    AnonymityLevel(String anonymityLevel) {
        this.anonymityLevel = anonymityLevel;
    }


    public String getAnonymityLevel() {
        return anonymityLevel;
    }

    public static AnonymityLevel parse(String level) {
        switch (level) {
            case "TRANSPARENT":
                return AnonymityLevel.TRANSPARENT;
            case "ANONYMOUS":
                return AnonymityLevel.ANONYMOUS;
            case "DISTORTING":
                return AnonymityLevel.DISTORTING;
            case "ELITE":
                return AnonymityLevel.ELITE;
            case "UNKNOW":
                return AnonymityLevel.UNKNOW;
            default:
                throw new IllegalArgumentException("There is not enum names with [" + level + "] of type AnonymityLevel exists! ");
        }
    }

    @Override
    public AnonymityLevel deserialize(String jsonEnum) {
        return AnonymityLevel.parse(jsonEnum);
    }

    @Override
    public String serialize() {
        return this.getAnonymityLevel();
    }
}
