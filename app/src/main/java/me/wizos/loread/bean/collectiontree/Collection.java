/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-03-14 11:48:08
 */

package me.wizos.loread.bean.collectiontree;


// Category 和 Feed 的抽象
public class Collection {
    protected String uid;
    protected String id;
    protected String title;
    protected int count;
    // private boolean isExpanded = false;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    // public boolean isExpanded() {
    //     return isExpanded;
    // }
    //
    // public void setExpanded(boolean expanded) {
    //     isExpanded = expanded;
    // }

    @Override
    public String toString() {
        return "Collection{" +
                "id='" + id + '\'' +
                ", uid='" + uid + '\'' +
                ", title='" + title + '\'' +
                // ", isExpanded='" + isExpanded + '\'' +
                ", count=" + count +
                '}';
    }
}
