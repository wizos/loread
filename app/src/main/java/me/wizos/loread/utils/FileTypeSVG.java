package me.wizos.loread.utils;

/**
 * Created by wizos on 2017/3/15.
 */
public enum FileTypeSVG {
    /**
     * svg，还有一种是xml文件头
     */
    SVG("3C73766720"),
    XML("3C3F786D6C");

    private String value = "";

    FileTypeSVG(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}