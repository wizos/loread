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

import me.wizos.loread.bean.CategoryFeeds;
import me.wizos.loread.bean.GroupedTriggerRules;
import me.wizos.loread.db.Collection;
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

    public static List<CategoryFeeds> group(List<Collection> categories, List<FeedCategory> feedCategories, List<Collection> feeds){
        ArrayMap<String, List<String>> feedCategoryMap = new ArrayMap<>();
        for (FeedCategory feedCategory: feedCategories){
            List<String> categoryIds = feedCategoryMap.get(feedCategory.getFeedId());
            if(categoryIds == null){
                categoryIds = new ArrayList<>();
                feedCategoryMap.put(feedCategory.getFeedId(), categoryIds);
            }
            categoryIds.add(feedCategory.getCategoryId());
        }

        LinkedHashMap<String, CategoryFeeds> categoryMap = new LinkedHashMap<>();
        for (Collection category: categories){
            CategoryFeeds groupedFeed = new CategoryFeeds();
            groupedFeed.setCategoryId(category.getId());
            groupedFeed.setCategoryName(category.getTitle());
            groupedFeed.setCount(category.getCount());
            categoryMap.put(category.getId(), groupedFeed);
        }

        for (Collection feed: feeds){
            List<String> categoryIds = feedCategoryMap.get(feed.getId());
            if(categoryIds == null){
                continue;
            }
            for (String id:categoryIds){
                CategoryFeeds groupedFeed = categoryMap.get(id);
                if(groupedFeed == null){
                    continue;
                }
                List<Collection> subFeeds = groupedFeed.getFeeds();
                if(subFeeds == null){
                    subFeeds = new ArrayList<>();
                    groupedFeed.setFeeds(subFeeds);
                }
                subFeeds.add(feed);
            }
        }

        List<CategoryFeeds> groupedFeeds = new ArrayList<>(categories.size());
        for (Map.Entry<String, CategoryFeeds> entry: categoryMap.entrySet()){
            groupedFeeds.add(entry.getValue());
        }
        return groupedFeeds;
    }

    // public static List<CategoryFeeds> group3(List<Collection> categories, List<FeedCategory> feedCategories, List<Collection> feeds){
    //     ArrayMap<String, Collection> feedArrayMap = new ArrayMap<>();
    //     for (Collection feed: feeds){
    //         feedArrayMap.put(feed.getId(), feed);
    //     }
    //     // XLog.i("得到的FeedsMap：" + feedArrayMap);
    //     // XLog.i("得到的 feedCategories：" + feedCategories);
    //
    //     ArrayMap<String, Collection> categoryArrayMap = new ArrayMap<>();
    //     for (Collection category: categories){
    //         categoryArrayMap.put(category.getId(), category);
    //     }
    //     // XLog.i("得到的 categoryArrayMap：" + categoryArrayMap);
    //
    //     LinkedHashMap<String, List<Collection>> categoryMap = new LinkedHashMap<>();
    //     for (FeedCategory feedCategory: feedCategories){
    //         List<Collection> subFeeds = categoryMap.get(feedCategory.getCategoryId());
    //         if(subFeeds == null){
    //             subFeeds = new ArrayList<>();
    //             categoryMap.put(feedCategory.getCategoryId(), subFeeds);
    //         }
    //         subFeeds.add(feedArrayMap.remove(feedCategory.getFeedId()));
    //     }
    //     // XLog.i("得到的 categoryMap：" + categoryMap);
    //
    //     List<CategoryFeeds> groupedFeeds = new ArrayList<>(categories.size());
    //     for (Map.Entry<String, List<Collection>> entry: categoryMap.entrySet()){
    //         CategoryFeeds groupedFeed = new CategoryFeeds();
    //         groupedFeed.setCategoryId(entry.getKey());
    //         groupedFeed.setCategoryName(categoryArrayMap.get(entry.getKey()).getTitle());
    //         groupedFeed.setCount(categoryArrayMap.get(entry.getKey()).getCount());
    //         groupedFeed.setFeeds(categoryMap.get(entry.getKey()));
    //         groupedFeeds.add(groupedFeed);
    //     }
    //     return groupedFeeds;
    // }
}
