/*
 * Copyright (c) 2021 wizos
 * 项目：loread
 * 邮箱：wizos@qq.com
 * 创建时间：2021-02-09 10:42:48
 */

package me.wizos.loread.utils;

import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import me.wizos.loread.bean.GroupedTriggerRules;
import me.wizos.loread.bean.collectiontree.Collection;
import me.wizos.loread.bean.collectiontree.CollectionFeed;
import me.wizos.loread.bean.collectiontree.CollectionTree;
import me.wizos.loread.db.FeedCategory;
import me.wizos.loread.db.rule.TriggerRule;

public class Classifier {
    public static List<GroupedTriggerRules> group(List<TriggerRule> triggerRules){
        List<GroupedTriggerRules> groupedTriggerRulesList = new ArrayList<>();
        if(triggerRules==null){
            return groupedTriggerRulesList;
        }else {
            ArrayMap<String, ArrayMap<String, List<TriggerRule>>> typeMap = new ArrayMap<>();
            ArrayMap<String, List<TriggerRule>> targetMap;
            List<TriggerRule> triggerRuleList;
            for (TriggerRule triggerRule:triggerRules) {
                targetMap = typeMap.get(triggerRule.getScope().getType());
                if(targetMap == null){
                    targetMap = new ArrayMap<>();
                    triggerRuleList = new ArrayList<>();
                }else {
                    triggerRuleList = targetMap.get(triggerRule.getScope().getTarget());
                    if(triggerRuleList == null){
                        triggerRuleList = new ArrayList<>();
                    }
                }
                triggerRuleList.add(triggerRule);
                targetMap.put(triggerRule.getScope().getTarget(), triggerRuleList);
                typeMap.put(triggerRule.getScope().getType(), targetMap);
            }

            for (Map.Entry<String, ArrayMap<String, List<TriggerRule>>> typeEntry : typeMap.entrySet()) {
                for (Map.Entry<String, List<TriggerRule>> targetEntry: typeEntry.getValue().entrySet()) {
                    GroupedTriggerRules groupedTriggerRules = new GroupedTriggerRules();
                    groupedTriggerRules.setType(typeEntry.getKey());
                    groupedTriggerRules.setTarget(targetEntry.getKey());
                    groupedTriggerRules.setTriggerRules(targetEntry.getValue());
                    groupedTriggerRulesList.add(groupedTriggerRules);
                }
            }
        }
        return groupedTriggerRulesList;
    }

    // public static List<CollectionTree> group(List<Collection> categories, List<FeedCategory> feedCategories, List<Collection> feeds){
    //     ArrayMap<String, List<String>> feedCategoryMap = new ArrayMap<>();
    //     for (FeedCategory feedCategory: feedCategories){
    //         List<String> categoryIds = feedCategoryMap.get(feedCategory.getFeedId());
    //         if(categoryIds == null){
    //             categoryIds = new ArrayList<>();
    //             feedCategoryMap.put(feedCategory.getFeedId(), categoryIds);
    //         }
    //         categoryIds.add(feedCategory.getCategoryId());
    //     }
    //
    //     LinkedHashMap<String, CollectionTree> treeLinkedHashMap = new LinkedHashMap<>();
    //     for (Collection category: categories){
    //         CollectionTree collectionTree = new CollectionTree();
    //         collectionTree.setParent(category);
    //         collectionTree.setType(CollectionTree.CATEGORY);
    //
    //         treeLinkedHashMap.put(category.getId(), collectionTree);
    //     }
    //
    //     for (Collection feed: feeds){
    //         List<String> categoryIds = feedCategoryMap.get(feed.getId());
    //         if(categoryIds == null){
    //             CollectionTree collectionTree = new CollectionTree();
    //             collectionTree.setParent(feed);
    //             collectionTree.setType(CollectionTree.FEED);
    //             treeLinkedHashMap.put(feed.getId(), collectionTree);
    //             continue;
    //         }
    //         for (String id:categoryIds){
    //             CollectionTree collectionTree = treeLinkedHashMap.get(id);
    //             if(collectionTree == null){
    //                 collectionTree = new CollectionTree();
    //                 collectionTree.setParent(feed);
    //                 collectionTree.setType(CollectionTree.FEED);
    //                 treeLinkedHashMap.put(feed.getId(), collectionTree);
    //                 continue;
    //             }
    //             List<Collection> subFeeds = collectionTree.getChildren();
    //             if(subFeeds == null){
    //                 subFeeds = new ArrayList<>();
    //                 collectionTree.setChildren(subFeeds);
    //             }
    //             subFeeds.add(feed);
    //         }
    //     }
    //
    //     List<CollectionTree> trees = new ArrayList<>(categories.size());
    //     for (Map.Entry<String, CollectionTree> entry: treeLinkedHashMap.entrySet()){
    //         trees.add(entry.getValue());
    //     }
    //     return trees;
    // }

    public static List<CollectionTree> group2(List<Collection> categories, List<FeedCategory> feedCategories, List<CollectionFeed> feeds){
        ArrayMap<String, List<String>> feedCategoryMap = new ArrayMap<>();
        for (FeedCategory feedCategory: feedCategories){
            List<String> categoryIds = feedCategoryMap.get(feedCategory.getFeedId());
            if(categoryIds == null){
                categoryIds = new ArrayList<>();
                feedCategoryMap.put(feedCategory.getFeedId(), categoryIds);
            }
            categoryIds.add(feedCategory.getCategoryId());
        }

        LinkedHashMap<String, CollectionTree> treeLinkedHashMap = new LinkedHashMap<>();
        for (Collection category: categories){
            CollectionTree collectionTree = new CollectionTree();
            collectionTree.setParent(category);
            collectionTree.setType(CollectionTree.CATEGORY);

            treeLinkedHashMap.put(category.getId(), collectionTree);
        }

        for (CollectionFeed feed: feeds){
            List<String> categoryIds = feedCategoryMap.get(feed.getId());
            if(categoryIds == null){
                CollectionTree collectionTree = new CollectionTree();
                collectionTree.setParent(feed);
                collectionTree.setType(CollectionTree.FEED);
                treeLinkedHashMap.put(feed.getId(), collectionTree);
                continue;
            }
            for (String id:categoryIds){
                CollectionTree collectionTree = treeLinkedHashMap.get(id);
                if(collectionTree == null){
                    collectionTree = new CollectionTree();
                    collectionTree.setParent(feed);
                    collectionTree.setType(CollectionTree.FEED);
                    treeLinkedHashMap.put(feed.getId(), collectionTree);
                    continue;
                }
                List<Collection> subFeeds = collectionTree.getChildren();
                if(subFeeds == null){
                    subFeeds = new ArrayList<>();
                    collectionTree.setChildren(subFeeds);
                }
                subFeeds.add(feed);
            }
        }

        List<CollectionTree> trees = new ArrayList<>(categories.size());
        for (Map.Entry<String, CollectionTree> entry: treeLinkedHashMap.entrySet()){
            trees.add(entry.getValue());
        }
        return trees;
    }
}
