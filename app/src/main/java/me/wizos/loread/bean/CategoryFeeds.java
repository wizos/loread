/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-02-09 10:46:25
 */

package me.wizos.loread.bean;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import me.wizos.loread.db.Collection;

public class CategoryFeeds {
    private String categoryId;
    private String categoryName;
    private int count;

    private List<Collection> feeds;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public List<Collection> getFeeds() {
        return feeds;
    }

    public void setFeeds(List<Collection> feeds) {
        this.feeds = feeds;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @NotNull
    @Override
    public String toString() {
        return "GroupedFeed{" +
                "categoryId='" + categoryId + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", count='" + count + '\'' +
                ", feeds=" + feeds +
                '}';
    }
}
