package me.wizos.loread.utils;

/**
 * Created by jiangzeyin on 2017/3/15.
 */
public enum ImgFileType{
    /**
     * JPEG,JPG
     */
    JPEG("FFD8FF", "jpg"),

    /**
     * PNG
     */
    PNG("89504E47", "png"),

    /**
     * TIFF
     */
    TIFF("49492A00", "tiff"),

    /**
     * Windows bitmap
     */
    BMP("424D", "bmp"),

    WEBP("52494646", "webp"),

    /**
     * GIF
     */
    GIF("47494638", "gif"),

    /**
     * svg，还有一种是xml文件头
     */
    SVG("3C73766720", "svg"),

    XML("3C3F786D6C", "xml");

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