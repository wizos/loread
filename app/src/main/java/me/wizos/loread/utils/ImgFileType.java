package me.wizos.loread.utils;

/**
 * Created by jiangzeyin on 2017/3/15.
 */
public enum ImgFileType {
    /**
     * JPEG,JPG
     */
    JPEG("FFD8FF", "jpg"),

    /**
     * PNG
     */
    PNG("89504E47", "png"),

    /**
     * GIF
     */
    GIF("47494638", "gif"),

    /**
     * TIFF
     */
    TIFF("49492A00"),

    /**
     * Windows bitmap
     */
    BMP("424D"),


    WEBP("52494646"),

    /**
     * svg，还有一种是xml文件头
     */
    SVG("3C73766720");

    private String value = "";
    private String ext = "";

    ImgFileType(String value) {
        this.value = value;
    }

    ImgFileType(String value, String ext) {
        this(value);
        this.ext = ext;
    }

    public String getExt() {
        return ext;
    }

    public String getValue() {
        return value;
    }

}