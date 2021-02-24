package me.wizos.loread.utils;

/**
 * Created by wizos on 2017/3/15.
 */
public enum FileTypeSVG {
    /**
     * svg，还有一种是xml文件头
     */
    SVG("3C73766720");

    private String value = "";

    FileTypeSVG(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}