package me.wizos.loread.config;

import android.text.TextUtils;
import android.util.ArrayMap;

import androidx.sqlite.db.SimpleSQLiteQuery;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import me.wizos.loread.App;
import me.wizos.loread.config.article_action_rule.ArticleActionRule;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Entry;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.StringUtils;

public class ArticleActionConfig {
    private static final String CONFIG_FILENAME = "article_action_rule.json";
    private static ArticleActionConfig instance;
    private ArticleActionConfig() { }
    public static ArticleActionConfig i() {
        if (instance == null) {
            synchronized (ArticleActionConfig.class) {
                if (instance == null) {
                    instance = new ArticleActionConfig();
                    String json = FileUtil.readFile(App.i().getUserConfigPath() + CONFIG_FILENAME);
                    if (TextUtils.isEmpty(json)) {
                        instance.actionRuleArrayMap = new ArrayMap<String, ArticleActionRule>();
                        instance.save();
                    }else {
                        instance.actionRuleArrayMap = new Gson().fromJson(json, new TypeToken<ArrayMap<String, ArticleActionRule>>() {}.getType());
                    }
                }
            }
        }
        return instance;
    }
    public void save() {
        FileUtil.save(App.i().getUserConfigPath() + CONFIG_FILENAME, new GsonBuilder().setPrettyPrinting().create().toJson(instance.actionRuleArrayMap));
    }
    public void reset() {
        instance = null;
    }


    private ArrayMap<String, ArticleActionRule> actionRuleArrayMap;


    public void exeRules(String uid, long timeMillis){
        // 1.执行规则
        for (Map.Entry<String, ArticleActionRule> entry: actionRuleArrayMap.entrySet()) {
            exeRule(uid, entry.getValue(), timeMillis);
        }
    }
    private void exeRule(String uid, ArticleActionRule articleActionRule, long timeMillis){
        KLog.e("用户："+ uid + " , " + articleActionRule);
        if("all".equals(articleActionRule.getTarget())){
            String sql = "";
            if("contain".equals(articleActionRule.getJudge())){
                String[] keywords = articleActionRule.getValue().split("\\|");
                Set<String> conditions = new HashSet<>();
                for (String keyword:keywords) {
                    conditions.add( articleActionRule.getAttr() + " like '%" + keyword + "%'");
                }
                sql = StringUtils.join(" or ", conditions);
                SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT * FROM article WHERE uid = ? AND crawlDate >= ? AND " + sql, new Object[]{uid,timeMillis});
                List<Article> articles = CoreDB.i().articleDao().getActionRuleArticlesRaw(query);
                doActionWithArticles(articles, articleActionRule);
                //KLog.e("文章结果 为：" + query.getSql() + " == " + articles.size());
            }else if("not contain".equals(articleActionRule.getJudge())){
                String[] keywords = articleActionRule.getValue().split("\\|");
                Set<String> conditions = new HashSet<>();
                for (String keyword:keywords) {
                    conditions.add( articleActionRule.getAttr() + " not like '%" + keyword + "%'");
                }
                sql = StringUtils.join(" and ", conditions);
                SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT * FROM article WHERE uid = ? AND crawlDate >= ? AND " + sql, new Object[]{uid,timeMillis});
                List<Article> articles = CoreDB.i().articleDao().getActionRuleArticlesRaw(query);
                doActionWithArticles(articles, articleActionRule);
                //KLog.e("文章结果 为：" + query.getSql() + " == " + articles.size());
            }else if("match".equals(articleActionRule.getJudge())){
                List<String> needActionArticleIds = new ArrayList<>();
                SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT id, " + articleActionRule.getAttr() + " as entry FROM article WHERE uid = ? AND crawlDate >= ?" + sql, new Object[]{uid,timeMillis});
                List<Entry> entries = CoreDB.i().articleDao().getActionRuleArticlesRaw2(query);
                Iterator<Entry> iterator = entries.iterator();
                Pattern pattern = Pattern.compile(articleActionRule.getValue(), Pattern.CASE_INSENSITIVE);
                Entry entry;
                while (iterator.hasNext()){
                    entry = iterator.next();
                    if(pattern.matcher( entry.getEntry() ).find()){
                        needActionArticleIds.add(entry.getId());
                        iterator.remove();
                    }
                }
                List<Article> articles = CoreDB.i().articleDao().getArticles(uid, needActionArticleIds);
                doActionWithArticles(articles, articleActionRule);
                //KLog.e("文章结果 为：" + query.getSql() + " == " + articles.size());
            }else if("not match".equals(articleActionRule.getJudge())){
                List<String> needActionArticleIds = new ArrayList<>();
                SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT id, " + articleActionRule.getAttr() + " as entry FROM article WHERE uid = ? AND crawlDate >= ?" + sql, new Object[]{uid,timeMillis});
                List<Entry> entries = CoreDB.i().articleDao().getActionRuleArticlesRaw2(query);
                Iterator<Entry> iterator = entries.iterator();
                Pattern pattern = Pattern.compile(articleActionRule.getValue(), Pattern.CASE_INSENSITIVE);
                Entry entry;
                while (iterator.hasNext()){
                    entry = iterator.next();
                    if(!pattern.matcher( entry.getEntry() ).find()){
                        needActionArticleIds.add(entry.getId());
                        iterator.remove();
                    }
                }
                List<Article> articles = CoreDB.i().articleDao().getArticles(uid, needActionArticleIds);
                doActionWithArticles(articles, articleActionRule);
                //KLog.e("文章结果 为：" + query.getSql() + " == " + articles.size());
            }
        }else if(articleActionRule.getTarget().startsWith("feed/")){
            String feedUrl = articleActionRule.getTarget().substring(5);
            KLog.e("被处理的feedUrl为：" + feedUrl);
            String sql = "";
            if("contain".equals(articleActionRule.getJudge())){
                String[] keywords = articleActionRule.getValue().split("\\|");
                Set<String> conditions = new HashSet<>();
                for (String keyword:keywords) {
                    conditions.add( "article." + articleActionRule.getAttr() + " like '%" + keyword + "%'");
                }
                sql = StringUtils.join(" or ", conditions);
                SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT article.* FROM article LEFT JOIN Feed ON (article.uid = Feed.uid AND article.feedId = Feed.id) WHERE article.uid = ? AND crawlDate >= ? AND Feed.feedUrl = ? AND " + sql, new Object[]{uid, timeMillis, feedUrl});
                List<Article> articles = CoreDB.i().articleDao().getActionRuleArticlesRaw(query);
                doActionWithArticles(articles, articleActionRule);
                //KLog.e("文章结果 为：" + query.getSql() + " == " + articles.size());
            }else if("not contain".equals(articleActionRule.getJudge())){
                String[] keywords = articleActionRule.getValue().split("\\|");
                Set<String> conditions = new HashSet<>();
                for (String keyword:keywords) {
                    conditions.add( "article." + articleActionRule.getAttr() + " not like '%" + keyword + "%'");
                }
                sql = StringUtils.join(" and ", conditions);
                SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT article.* FROM article LEFT JOIN Feed ON (article.uid = Feed.uid AND article.feedId = Feed.id) WHERE article.uid = ? AND crawlDate >= ? AND Feed.feedUrl = ? AND " + sql, new Object[]{uid,timeMillis, feedUrl});
                List<Article> articles = CoreDB.i().articleDao().getActionRuleArticlesRaw(query);
                doActionWithArticles(articles, articleActionRule);
                //KLog.e("文章结果 为：" + query.getSql() + " == " + articles.size());
            }else if("match".equals(articleActionRule.getJudge())){
                List<String> needActionArticleIds = new ArrayList<>();
                SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT article.id, article." + articleActionRule.getAttr() + " as entry FROM article LEFT JOIN Feed ON (article.uid = Feed.uid AND article.feedId = Feed.id) WHERE article.uid = ? AND crawlDate >= ? AND Feed.feedUrl = ? " + sql, new Object[]{App.i().getUser().getId(),timeMillis, feedUrl});
                List<Entry> entries = CoreDB.i().articleDao().getActionRuleArticlesRaw2(query);
                Iterator<Entry> iterator = entries.iterator();
                Pattern pattern = Pattern.compile(articleActionRule.getValue(), Pattern.CASE_INSENSITIVE);
                Entry entry;
                while (iterator.hasNext()){
                    entry = iterator.next();
                    if(pattern.matcher( entry.getEntry() ).find()){
                        needActionArticleIds.add(entry.getId());
                        iterator.remove();
                    }
                }
                List<Article> articles = CoreDB.i().articleDao().getArticles(uid, needActionArticleIds);
                doActionWithArticles(articles, articleActionRule);
                //KLog.e("文章结果 为：" + query.getSql() + " == " + articles.size());
            }else if("not match".equals(articleActionRule.getJudge())){
                List<String> needActionArticleIds = new ArrayList<>();
                SimpleSQLiteQuery query = new SimpleSQLiteQuery("SELECT article.id, article." + articleActionRule.getAttr() + " as entry FROM article LEFT JOIN Feed ON (article.uid = Feed.uid AND article.feedId = Feed.id) WHERE article.uid = ? AND crawlDate >= ? AND Feed.feedUrl = ? " + sql, new Object[]{uid,timeMillis, feedUrl});
                List<Entry> entries = CoreDB.i().articleDao().getActionRuleArticlesRaw2(query);
                Iterator<Entry> iterator = entries.iterator();
                Pattern pattern = Pattern.compile(articleActionRule.getValue(), Pattern.CASE_INSENSITIVE);
                Entry entry;
                while (iterator.hasNext()){
                    entry = iterator.next();
                    if(!pattern.matcher( entry.getEntry() ).find()){
                        needActionArticleIds.add(entry.getId());
                        iterator.remove();
                    }
                }
                List<Article> articles = CoreDB.i().articleDao().getArticles(uid, needActionArticleIds);
                doActionWithArticles(articles, articleActionRule);
                //KLog.e("文章结果 为：" + query.getSql() + " == " + articles.size());
            }
        }
    }

    private void doActionWithArticles(List<Article> articles, ArticleActionRule articleActionRule){
        if(articleActionRule ==null || articleActionRule.getActions() == null || articleActionRule.getActions().size() == 0){
            return;
        }
        if(articleActionRule.getActions().contains("mark read")){
            List<String> articleIds = new ArrayList<>();
            for (Article article:articles) {
                article.setReadStatus(App.STATUS_READED);
                articleIds.add(article.getId());
            }
            App.i().getApi().markArticleListReaded(articleIds, new CallbackX() {
                @Override
                public void onSuccess(Object result) {
                }

                @Override
                public void onFailure(Object error) {
                }
            });
            CoreDB.i().articleDao().update(articles);
        }

        if(articleActionRule.getActions().contains("mark unreading")){
            for (Article article:articles) {
                article.setReadStatus(App.STATUS_UNREADING);
            }
            CoreDB.i().articleDao().update(articles);
        }

        if(articleActionRule.getActions().contains("mark star")){
            for (Article article:articles) {
                article.setReadStatus(App.STATUS_STARED);
                App.i().getApi().markArticleStared(article.getId(), new CallbackX() {
                    @Override
                    public void onSuccess(Object result) {
                    }

                    @Override
                    public void onFailure(Object error) {
                    }
                });
            }
            CoreDB.i().articleDao().update(articles);
        }

        if(articleActionRule.getActions().contains("delete")){
            CoreDB.i().articleDao().delete(articles);
        }
    }



    private List<Article> getNeedActionArticlesWithNotMatch(List<Article> articles, Pattern pattern, String input){
        Article article;
        Iterator<Article> iterator = articles.iterator();
        List<Article> needActionArticles = new ArrayList<>();
        while (iterator.hasNext()){
            article = iterator.next();
            if(!pattern.matcher( input ).find()){
                needActionArticles.add(article);
                iterator.remove();
            }
        }
        return needActionArticles;
    }

}
