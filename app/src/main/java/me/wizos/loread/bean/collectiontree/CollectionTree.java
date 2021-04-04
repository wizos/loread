/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-03-14 11:47:48
 */

package me.wizos.loread.bean.collectiontree;

import java.util.List;

public class CollectionTree {
    // public static final int ROOT = -2;
    // public static final int UNSUBSCRIBED = -1;
    public static final int SMART = -1;
    public static final int CATEGORY = 0;
    public static final int FEED = 1;

    private int type;

    private Collection parent;
    private List<Collection> children;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Collection getParent() {
        return parent;
    }

    public void setParent(Collection parent) {
        this.parent = parent;
    }

    public List<Collection> getChildren() {
        return children;
    }

    public void setChildren(List<Collection> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "CollectionTree{" +
                ", type=" + type +
                ", parent=" + parent +
                ", children=" + children +
                '}';
    }
}
