/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-03-21 10:25:20
 */

package me.wizos.loread.utils;

import java.util.HashSet;
import java.util.Set;

public class ImgFileTypeJudge {
    private static ImgFileTypeJudge instance;
    private Set<ImgFileType> sampleImgSet;
    private Set<ImgFileType> gifImgSet;
    private Set<ImgFileType> svgImgSet;

    private ImgFileTypeJudge(){}
    public static ImgFileTypeJudge i() {
        if (instance == null) {
            synchronized (ImgFileTypeJudge.class) {
                if (instance == null) {
                    instance = new ImgFileTypeJudge();

                    instance.sampleImgSet = new HashSet<>();
                    instance.sampleImgSet.add(ImgFileType.JPEG);
                    instance.sampleImgSet.add(ImgFileType.PNG);
                    instance.sampleImgSet.add(ImgFileType.TIFF);
                    instance.sampleImgSet.add(ImgFileType.BMP);
                    instance.sampleImgSet.add(ImgFileType.WEBP);

                    instance.gifImgSet = new HashSet<>();
                    instance.gifImgSet.add(ImgFileType.GIF);

                    instance.svgImgSet = new HashSet<>();
                    instance.svgImgSet.add(ImgFileType.SVG);
                    instance.svgImgSet.add(ImgFileType.XML);
                }
            }
        }
        return instance;
    }
    public boolean isSampleImg(ImgFileType imgFileType){
        return sampleImgSet.contains(imgFileType);
    }
    public boolean isGifImg(ImgFileType imgFileType){
        return gifImgSet.contains(imgFileType);
    }
    public boolean isSvgImg(ImgFileType imgFileType){
        return svgImgSet.contains(imgFileType);
    }
}
