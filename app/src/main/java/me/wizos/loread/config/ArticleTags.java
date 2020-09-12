package me.wizos.loread.config;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.wizos.loread.App;
import me.wizos.loread.db.ArticleTag;
import me.wizos.loread.utils.FileUtil;

/**
 * 文章保存目录：
 *  1.默认目录（无分类）
 *  2.订阅源
 *  3.订阅源所属的分类（分类可能有多个，如何确定）
 *  4.保存时手动配置
 *
 *  自定义分类
 * @author Wizos on 2020/6/14.
 */
public class ArticleTags {
    private static ArticleTags instance;
    private ArticleTags() { }
    public static ArticleTags i() {
        if (instance == null) {
            synchronized (ArticleTags.class) {
                if (instance == null) {
                    Gson gson = new Gson();
                    String config = FileUtil.readFile(App.i().getUserFilesDir() + "/config/article_tags.json");
                    if (TextUtils.isEmpty(config)) {
                        instance = new ArticleTags();
                        instance.articleTags = new ArrayMap<>();
                        instance.tags = new HashSet<>();
                    } else {
                        instance = gson.fromJson(config, ArticleTags.class);
                    }
                }
            }
        }
        return instance;
    }
    public void reset() {
        instance = null;
    }
    public void save() {
        FileUtil.save(App.i().getUserFilesDir() + "/config/article_tags.json", new GsonBuilder().setPrettyPrinting().create().toJson(instance));
    }


    private ArrayMap<String, Set<String>> articleTags;
    private Set<String> tags;
    public void removeArticle(String articleId){
        articleTags.remove(articleId);
    }
    public void addArticleTags(List<ArticleTag> articleTags){
        for (ArticleTag articleTag:articleTags) {
            addArticleTag(articleTag);
        }
    }
    public void addArticleTag(ArticleTag articleTag){
        if(articleTag != null){
            Set<String> tags;
            if(articleTags.containsKey(articleTag.getArticleId())){
                tags = articleTags.get(articleTag.getArticleId());
            }else {
                tags = new HashSet<>();
            }
            tags.add(articleTag.getTagId());
            this.tags.add(articleTag.getTagId());
            articleTags.put(articleTag.getArticleId(),tags);
        }
    }

    public void newTag(String directory){
        tags.add(directory);
    }
}