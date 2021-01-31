package me.wizos.loread.utils;

import androidx.sqlite.db.SimpleSQLiteQuery;

import com.elvishew.xlog.XLog;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Entry;
import me.wizos.loread.db.rule.Action;
import me.wizos.loread.db.rule.Condition;
import me.wizos.loread.db.rule.TriggerRule;
import me.wizos.loread.network.callback.CallbackX;

public class TriggerRuleUtils {
    public static String getOptimizedKeywords(String str) {
        if(StringUtils.isEmpty(str)){
            return str;
        }
        String[] strings = str.replaceAll("\n", "").split("\\|");
        Set<String> keySet = new HashSet<>(Arrays.asList(strings));
        List<String> keywords = new ArrayList<>(keySet);
        Collator comparator = Collator.getInstance(Locale.getDefault());
        Collections.sort(keywords, comparator::compare);
        return StringUtils.join("|", keywords);
    }

    /**
     * 执行规则
     */
    public static void exeAllRules(String uid, long minCrawlTimeMillis){
        List<TriggerRule> triggerRules = CoreDB.i().triggerRuleDao().getRules(uid);
        XLog.i("执行 triggerRules 规则："+ triggerRules);
        for (TriggerRule triggerRule:triggerRules){
            exeRule(uid, minCrawlTimeMillis, triggerRule);
        }
    }

    public static void exeRules(String uid, long minCrawlTimeMillis, List<TriggerRule> triggerRules){
        XLog.i("执行 triggerRules 规则："+ triggerRules);
        for (TriggerRule triggerRule:triggerRules){
            exeRule(uid, minCrawlTimeMillis, triggerRule);
        }
    }

    /**
     * 执行规则
     */
    public static void exeRule(String uid, long crawlTimeMillis, TriggerRule triggerRule){
        XLog.i("执行Action规则："+ triggerRule);
        if(Contract.TYPE_GLOBAL.equals(triggerRule.getScope().getType())){
            List<Condition> conditions = triggerRule.getConditions();
            Set<String> conditionSet = new HashSet<>();
            for (Condition condition:conditions) {
                // Set<String> subConditionSet = new HashSet<>();
                // String[] keywords = condition.getValue().split("\\|");
                // // TODO: 2021/1/30 检查 attr 和 judge 是否匹配
                // if("like".equals(condition.getJudge())){
                //     for (String keyword:keywords) {
                //         subConditionSet.add( condition.getAttr() + " like '%" + keyword + "%'");
                //     }
                //     conditionSet.add( "(" + StringUtils.join(" or ", subConditionSet) + ")" );
                // }else if("not like".equals(condition.getJudge())){
                //     for (String keyword:keywords) {
                //         subConditionSet.add( condition.getAttr() + " not like '%" + keyword + "%'");
                //     }
                //     conditionSet.add( "(" + StringUtils.join(" or ", subConditionSet) + ")");
                // }else if("is".equals(condition.getJudge())){
                //     for (String keyword:keywords) {
                //         subConditionSet.add( condition.getAttr() + " is '" + keyword + "'");
                //     }
                //     conditionSet.add( "(" + StringUtils.join(" or ", subConditionSet) + ")");
                //     // conditionSet.add( "(" +condition.getAttr() + "is '" + condition.getValue() + "')");
                // }else if("is not".equals(condition.getJudge())){
                //     for (String keyword:keywords) {
                //         subConditionSet.add( condition.getAttr() + " is not '" + keyword + "'");
                //     }
                //     conditionSet.add( "(" + StringUtils.join(" or ", subConditionSet) + ")");
                //     // conditionSet.add( "(" +condition.getAttr() + "is not '" + condition.getValue() + "')");
                // }else if("starts with".equals(condition.getJudge())){
                //     for (String keyword:keywords) {
                //         subConditionSet.add( condition.getAttr() + " like '" + keyword + "%'");
                //     }
                //     conditionSet.add( "(" + StringUtils.join(" or ", subConditionSet) + ")");
                //     // conditionSet.add( "(" +condition.getAttr() + "like '" + condition.getValue() + "%')");
                // }else if("ends with".equals(condition.getJudge())){
                //     for (String keyword:keywords) {
                //         subConditionSet.add( condition.getAttr() + " like '%" + keyword + "'");
                //     }
                //     conditionSet.add( "(" + StringUtils.join(" or ", subConditionSet) + ")");
                //     // conditionSet.add( "(" +condition.getAttr() + "like '%" + condition.getValue() + "')");
                // }
                // TODO: 2021/1/30 还有正则没有支持
                joinTextConditions(condition, conditionSet);
            }
            XLog.i("条件 为：" + conditionSet);
            if(conditionSet.size() == 0){
                return;
            }

            SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT article.id FROM article WHERE uid = '" + uid + "' AND crawlDate >= " + crawlTimeMillis + " AND " + StringUtils.join(" and ", conditionSet));
            List<String> articleIds = CoreDB.i().articleDao().getActionRuleArticleIds(query);

            // for (Condition condition:conditions){
            //     if("match".equals(condition.getJudge())){
            //         articleIds = filterArticleIdsWithRegexp(uid, articleIds, condition, true);
            //     }else if("not match".equals(condition.getJudge())){
            //         articleIds = filterArticleIdsWithRegexp(uid, articleIds, condition, false);
            //     }
            // }

            doActionWithArticles(articleIds, triggerRule.getActions());
            XLog.i("文章结果 为：" + query.getSql() + " == " + articleIds.size());
        }else if(Contract.TYPE_CATEGORY.equals(triggerRule.getScope().getType())){
            String categoryId = triggerRule.getScope().getTarget();
            List<Condition> conditions = triggerRule.getConditions();
            Set<String> conditionSet = new HashSet<>();
            for (Condition condition:conditions) {
                joinTextConditions(condition, conditionSet);
            }

            XLog.i("条件 为：" + conditionSet);
            if(conditionSet.size() == 0){
                return;
            }

            SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT article.id FROM article LEFT JOIN FeedCategory ON (article.uid = FeedCategory.uid AND article.feedId = FeedCategory.feedId) WHERE article.uid = '" + uid + "' AND article.crawlDate >= " + crawlTimeMillis + " AND FeedCategory.categoryId = '" + categoryId + "' AND " + StringUtils.join(" AND ", conditionSet));
            List<String> articleIds = CoreDB.i().articleDao().getActionRuleArticleIds(query);

            doActionWithArticles(articleIds, triggerRule.getActions());
            XLog.i("文章结果 为：" + query.getSql() + " == " + articleIds.size());
        }else if(Contract.TYPE_FEED.equals(triggerRule.getScope().getType())){
            String feedId = triggerRule.getScope().getTarget();
            List<Condition> conditions = triggerRule.getConditions();
            Set<String> conditionSet = new HashSet<>();
            for (Condition condition:conditions) {
                joinTextConditions(condition, conditionSet);
            }

            XLog.i("条件 为：" + conditionSet);
            if(conditionSet.size() == 0){
                return;
            }

            SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT article.id FROM article LEFT JOIN Feed ON (article.uid = Feed.uid AND article.feedId = Feed.id) WHERE article.uid = '" + uid + "' AND article.crawlDate >= " + crawlTimeMillis + " AND Feed.id = '" + feedId + "' AND " + StringUtils.join(" AND ", conditionSet));
            List<String> articleIds = CoreDB.i().articleDao().getActionRuleArticleIds(query);

            doActionWithArticles(articleIds, triggerRule.getActions());
            XLog.i("文章结果 为：" + query.getSql() + " == " + articleIds.size());
        }
    }


    private static void joinTextConditions(Condition condition, Set<String> conditionSet){
        String[] keywords = condition.getValue().split("\\|");
        Set<String> subConditionSet = new HashSet<>();
        if("like".equals(condition.getJudge())){
            for (String keyword:keywords) {
                subConditionSet.add( "article." + condition.getAttr() + " like '%" + keyword + "%'");
            }
            conditionSet.add( "(" + StringUtils.join(" or ", subConditionSet) + ")" );
        }else if("not like".equals(condition.getJudge())){
            for (String keyword:keywords) {
                subConditionSet.add( "article." + condition.getAttr() + " not like '%" + keyword + "%'");
            }
            conditionSet.add( "(" + StringUtils.join(" and ", subConditionSet) + ")" );
        }else if("is".equals(condition.getJudge())){
            for (String keyword:keywords) {
                subConditionSet.add( "article." + condition.getAttr() + " is '" + keyword + "'");
            }
            conditionSet.add( "(" + StringUtils.join(" or ", subConditionSet) + ")");
        }else if("is not".equals(condition.getJudge())){
            for (String keyword:keywords) {
                subConditionSet.add( "article." + condition.getAttr() + " is not '" + keyword + "'");
            }
            conditionSet.add( "(" + StringUtils.join(" and ", subConditionSet) + ")");
        }else if("starts with".equals(condition.getJudge())){
            for (String keyword:keywords) {
                subConditionSet.add( "article." + condition.getAttr() + " like '" + keyword + "%'");
            }
            conditionSet.add( "(" + StringUtils.join(" or ", subConditionSet) + ")");
        }else if("ends with".equals(condition.getJudge())){
            for (String keyword:keywords) {
                subConditionSet.add( "article." + condition.getAttr() + " like '%" + keyword + "'");
            }
            conditionSet.add( "(" + StringUtils.join(" or ", subConditionSet) + ")");
        }else if(">".equals(condition.getJudge())){
            ArrayList<String> slice = new ArrayList<>();
            for(int i=0, size=Integer.parseInt(condition.getValue()) + 1; i<size; i++){
                slice.add("<img");
            }
            if(slice.size() == 0){
                slice.add("<img");
            }
            conditionSet.add( "( article.content like '%" + StringUtils.join("%", slice) + "%')");
        }else if("<=".equals(condition.getJudge())){
            ArrayList<String> slice = new ArrayList<>();
            for(int i=0, size=Integer.parseInt(condition.getValue()) + 1; i<size; i++){
                slice.add("<img");
            }
            if(slice.size() == 0){
                slice.add("<img");
            }
            conditionSet.add( "( article.content not like '%" + StringUtils.join("%", slice) + "%')");
        }
    }


    private static void doTextJudgeForGlobal(String uid, List<Action> actions, String sqlSlice, long timeMillis){
        SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT article.id FROM article WHERE uid = '" + uid + "' AND crawlDate >= " + timeMillis + " AND " + sqlSlice);
        List<String> articles = CoreDB.i().articleDao().getActionRuleArticleIds(query);
        doActionWithArticles(articles, actions);
        XLog.d("文章结果 为：" + query.getSql() + " == " + articles.size());
    }
    private static void doTextJudgeForFeed(String uid, String feedId, List<Action> actions, String sqlSlice, long timeMillis){
        SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT article.id FROM article LEFT JOIN Feed ON (article.uid = Feed.uid AND article.feedId = Feed.id) WHERE article.uid = '" + uid + "' AND article.crawlDate >= " + timeMillis + " AND Feed.id = '" + feedId + "' AND " + sqlSlice);
        List<String> articles = CoreDB.i().articleDao().getActionRuleArticleIds(query);
        doActionWithArticles(articles, actions);
    }

    private static List<String> filterArticleIdsWithRegexp(String uid, List<String> articleIds, Condition condition, boolean match){
        // int needCount = articleIds.size();
        // int hadCount = 0;
        // int num = 0;
        // while (needCount > 0) {
        //     num = Math.min(50, needCount);
        //     subArticleIds = articleIds.subList(hadCount, hadCount + num);
        //
        //     hadCount = hadCount + num;
        //     needCount = articleIds.size() - hadCount;
        // }

        // 2.分页数据信息
        int totalSize = articleIds.size(); // 总记录数
        int pageSize = Math.min(50, totalSize); // 每页N条
        int totalPage = (int) Math.ceil((float) totalSize / pageSize); // 共N页（向上取整）

        System.out.println("循环保存的次数：" + totalPage);    // 循环多少次

        // for (int pageNum = 0; pageNum < totalPage; pageNum++) {
        //     int starNum = pageNum * pageSize;
        //     int endNum = Math.min((pageNum+1) * pageSize, totalSize);
        //
        //     System.out.println("起始：" + starNum + "-" + endNum);
        //     temList = oldList.subList(starNum, endNum);
        //     System.out.println("第" + pageNum + "批，执行insert：" + temList);
        // }

        List<String> needActionArticleIds = new ArrayList<>();
        List<String> subArticleIds;
        SimpleSQLiteQuery query;
        boolean result = false;
        List<Entry> entries;
        Iterator<Entry> iterator;
        Pattern pattern;
        for (int pageNum = 1; pageNum < totalPage + 1; pageNum++) {
            int starNum = (pageNum - 1) * pageSize;
            int endNum = Math.min(pageNum * pageSize, totalSize);
            subArticleIds = articleIds.subList(starNum, endNum);
            System.out.println("第" + pageNum + "批，执行insert：" + subArticleIds);

            query = new SimpleSQLiteQuery("SELECT id, " + condition.getAttr() + " as entry FROM article WHERE uid = '" + uid + "' AND id in (" + StringUtils.join(",", subArticleIds) + ")");
            entries = CoreDB.i().articleDao().getActionRuleArticlesEntry(query);
            iterator = entries.iterator();
            pattern = Pattern.compile(condition.getValue(), Pattern.CASE_INSENSITIVE);
            Entry entry;
            while (iterator.hasNext()){
                entry = iterator.next();
                result = pattern.matcher( entry.getEntry() ).find();
                if( (result && match) || (!result && !match) ){
                    needActionArticleIds.add(entry.getId());
                }
            }
            XLog.d("SQL结果 为：" + query.getSql() );
        }
        XLog.d("文章结果 为：" + needActionArticleIds.size());
        return needActionArticleIds;
    }



    private static List<String> getArticleIdsWithRegexp(String uid, List<String> inArticleIds, Condition condition, boolean match){
        int totalSize = inArticleIds.size(); // 总记录数
        int pageSize = Math.min(50, totalSize); // 每页N条
        int totalPage = (int) Math.ceil((float) totalSize / pageSize); // 共N页（向上取整）

        List<String> needActionArticleIds = new ArrayList<>();
        List<String> subArticleIds;
        SimpleSQLiteQuery query;
        boolean result = false;
        List<Entry> entries;
        Iterator<Entry> iterator;
        Pattern pattern;
        for (int pageNum = 1; pageNum < totalPage + 1; pageNum++) {
            int starNum = (pageNum - 1) * pageSize;
            int endNum = Math.min(pageNum * pageSize, totalSize);
            subArticleIds = inArticleIds.subList(starNum, endNum);
            System.out.println("第" + pageNum + "批，执行insert：" + subArticleIds);

            query = new SimpleSQLiteQuery("SELECT id, " + condition.getAttr() + " as entry FROM article WHERE uid = '" + uid + "' AND id in (" + StringUtils.join(",", subArticleIds) + ")");
            entries = CoreDB.i().articleDao().getActionRuleArticlesEntry(query);
            iterator = entries.iterator();
            pattern = Pattern.compile(condition.getValue(), Pattern.CASE_INSENSITIVE);
            Entry entry;
            while (iterator.hasNext()){
                entry = iterator.next();
                result = pattern.matcher( entry.getEntry() ).find();
                if( (result && match) || (!result && !match) ){
                    needActionArticleIds.add(entry.getId());
                }
            }
            XLog.d("SQL结果 为：" + query.getSql() );
        }
        XLog.d("文章结果 为：" + needActionArticleIds.size());
        return needActionArticleIds;
    }


    private static void doActionWithArticles(List<String> articleIds, List<Action> actions){
        if(actions==null ||actions.size() == 0 || articleIds == null || articleIds.size() == 0){
            return;
        }
        XLog.d("预计有" + articleIds.size() + "份文章被处理为：" + actions);
        int needCount = articleIds.size();
        int hadCount = 0;
        int num = 0;
        List<String> subArticleIds;
        List<String> handlingArticleIds;
        String uid = App.i().getUser().getId();

        while (needCount > 0) {
            num = Math.min(50, needCount);
            subArticleIds = articleIds.subList(hadCount, hadCount + num);
            for (Action action:actions) {
                if(action.getAction().equals(Contract.MARK_READ)){
                    handlingArticleIds = CoreDB.i().articleDao().getUnreadOrUnreadingArticleIds(uid,subArticleIds);
                    if(handlingArticleIds.size() != 0){
                        List<String> finalHandlingArticleIds = handlingArticleIds;
                        App.i().getApi().markArticleListReaded(handlingArticleIds, new CallbackX() {
                            @Override
                            public void onSuccess(Object result) {
                                CoreDB.i().articleDao().markArticlesRead(App.i().getUser().getId(), finalHandlingArticleIds);
                            }

                            @Override
                            public void onFailure(Object error) {
                            }
                        });
                        XLog.d("以下文章被处理为：mark read "  + handlingArticleIds);
                    }
                }

                // if(articleActionRule.getActions().contains("mark unreading")){
                //     CoreDB.i().articleDao().markArticlesUnreading(uid, subArticleIds);
                // }

                if(action.getAction().equals(Contract.MARK_STAR)){
                    handlingArticleIds = CoreDB.i().articleDao().getUnStarArticleIds(uid,subArticleIds);
                    for (String articleId:handlingArticleIds) {
                        App.i().getApi().markArticleStared(articleId, new CallbackX() {
                            @Override
                            public void onSuccess(Object result) {
                            }

                            @Override
                            public void onFailure(Object error) {
                                List<String> id = new ArrayList<>();
                                id.add(articleId);
                                CoreDB.i().articleDao().markArticlesUnStar(uid, id);
                            }
                        });
                    }
                    CoreDB.i().articleDao().markArticlesStar(uid, handlingArticleIds);
                }
            }

            hadCount = hadCount + num;
            needCount = articleIds.size() - hadCount;
        }
    }
}
