/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-04-17 04:53:41
 */

package me.wizos.loread.utils;

import android.content.Context;

import androidx.annotation.ArrayRes;

import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.bean.SpinnerData;

public class SpinnerUtils {
    public static List<SpinnerData> getSpinnerData(Context context, @ArrayRes int textsId, @ArrayRes int valuesId){
        return getSpinnerData(context.getResources().getStringArray(textsId), context.getResources().getStringArray(valuesId));
    }
    public static List<SpinnerData> getSpinnerData(String[] texts, String[] values){
        if(texts ==null || values==null || texts.length != values.length){
            return null;
        }
        List<SpinnerData> spinnerDataList = new ArrayList<>();
        for (int i = 0, size = texts.length; i < size; i++) {
            spinnerDataList.add(new SpinnerData(texts[i], values[i]));
        }
        return spinnerDataList;
    }
}
