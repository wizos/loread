/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-03-10 11:50:10
 */

package me.wizos.loread.utils;

import java.util.List;
public class PagingUtils {
    public static<T> void processing(List<T> list, int unit, PagingListener<T> pagingListener){
        if(list == null || unit <= 0 || pagingListener == null){
            return;
        }
        // 1.分页数据信息
        int totalSize = list.size(); // 总记录数
        int pageSize = Math.min(unit, totalSize); // 每页N条
        int totalPage = (int) Math.ceil((float) totalSize / pageSize); // 共N页（向上取整）
        List<T> childList;
        for (int pageNum = 1; pageNum < totalPage + 1; pageNum++) {
            int starNum = (pageNum - 1) * pageSize;
            int endNum = Math.min(pageNum * pageSize, totalSize);
            childList = list.subList(starNum, endNum);
            pagingListener.onPage(childList);
        }
    }

    public interface PagingListener<T>{
        void onPage(List<T> childList);
    }
}
