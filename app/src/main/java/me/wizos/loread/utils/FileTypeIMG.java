package me.wizos.loread.utils;

/**
 * Created by jiangzeyin on 2017/3/15.
 */
public enum FileTypeIMG {
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


    WEBP("52494646");

    private String value = "";
    private String ext = "";

    FileTypeIMG(String value) {
        this.value = value;
    }

    FileTypeIMG(String value, String ext) {
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