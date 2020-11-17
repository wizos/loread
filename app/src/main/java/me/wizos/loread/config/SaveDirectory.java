package me.wizos.loread.config;

import android.text.TextUtils;
import android.util.ArrayMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.db.Category;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.utils.FileUtil;
import me.wizos.loread.utils.StringUtils;

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
public class SaveDirectory {
    // 按 feed 或者 category 级别来设置保存目录是只能设置为 root, feed, category
    // 手动修改某个 文章 的保存目录时，可以设置为 root, feed, category, 目录名称
    private String defaultDirectory; // 如果为null，代表根目录
    @SerializedName("category")
    private ArrayMap<String, String> settingByCategory; // root, feed, category, 目录名称
    @SerializedName("feed")
    private ArrayMap<String, String> settingByFeed; // root, feed, category, 目录名称
    @SerializedName("article")
    private ArrayMap<String, String> settingByArticle;

    private Set<String> directories;


    private static SaveDirectory instance;

    private SaveDirectory() { }

    public static SaveDirectory i() {
        if (instance == null) {
            synchronized (SaveDirectory.class) {
                if (instance == null) {
                    Gson gson = new Gson();
                    String config = FileUtil.readFile(App.i().getUserConfigPath() + "article_save_directory.json");
                    if (TextUtils.isEmpty(config)) {
                        instance = new SaveDirectory();
                        instance.settingByFeed = new ArrayMap<>();
                        instance.settingByArticle = new ArrayMap<>();
                        instance.directories = new HashSet<>();
                    } else {
                        instance = gson.fromJson(config, SaveDirectory.class);
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
        FileUtil.save(App.i().getUserConfigPath() + "article_save_directory.json", new GsonBuilder().setPrettyPrinting().create().toJson(instance));
    }


    public String getDirNameSettingByFeed(String feedId){
        String value;
        if( settingByFeed != null && settingByFeed.containsKey(feedId) ){
            value = settingByFeed.get(feedId);
        }else {
            value = defaultDirectory;
        }
        String name;
        if(StringUtils.isEmpty(value) || "loread_root".equalsIgnoreCase(value)){
            name = App.i().getString(R.string.root_directory);
        }else if("loread_feed_title".equalsIgnoreCase(value)){
            name = App.i().getString(R.string.feed_title_as_directory);
        }else if("loread_category_title".equalsIgnoreCase(value)){
            name = App.i().getString(R.string.category_title_as_directory);
        }else {
            name = value;
        }
        return name;
    }
//    public String getSavedDirectory(String feedId, String articleId) {
//        if(settingByArticle != null && settingByArticle.containsKey(articleId)){
//            return settingByArticle.get(articleId);
//        }
//
//        if(settingByFeed != null && settingByFeed.containsKey(feedId)){
//            String dir = settingByFeed.get(feedId);
//            if("loread_feed".equalsIgnoreCase(dir)){
//                Feed feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(), feedId);
//                if(feed != null && !StringUtils.isEmpty(feed.getTitle())){
//                    return feed.getTitle();
//                }
//            }
////            else if("loread_category".equalsIgnoreCase(dir)){
////                List<Category> categories = CoreDB.i().categoryDao().getByFeedId(App.i().getUser().getId(), feedId);
////                if(categories != null && !StringUtils.isEmpty(categories)){
////                    List<String> titles = new ArrayList<>(categories.size());
////                    for (Category category:categories) {
////                        titles.add(category.getTitle());
////                    }
////                    return StringUtils.join("_",titles);
////                }
////            }
//            return settingByFeed.get(feedId);
//        }
//        return defaultDirectory;
//    }

//    String categoryId,
    public String getSaveDir(String feedId, String articleId) {
        // loread_root, loread_feed, loread_category, tag
        String value;
        if(settingByArticle != null && settingByArticle.containsKey(articleId)){
            value = settingByArticle.get(articleId);
        }else if(settingByFeed != null && settingByFeed.containsKey(feedId)){
            value = settingByFeed.get(feedId);
//        }else if( settingByCategory != null && settingByCategory.containsKey(categoryId) ){
//            value = settingByCategory.get(categoryId);
        }else {
            value = defaultDirectory;
        }

        String dir = null;
        if("loread_root".equalsIgnoreCase(value)){
        }else if("loread_feed_title".equalsIgnoreCase(value)){
            Feed feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(), feedId);
            if(feed != null){
                dir = feed.getTitle();
            }
        }else if("loread_category_title".equalsIgnoreCase(value)){
            List<Category> categories = CoreDB.i().categoryDao().getByFeedId(App.i().getUser().getId(), feedId);
            if(categories != null && !StringUtils.isEmpty(categories)){
                List<String> titles = new ArrayList<>(categories.size());
                for (Category category:categories) {
                    titles.add(category.getTitle());
                }
                dir = StringUtils.join("_",titles);
            }
        }else {
            dir = value;
        }
        return dir;
    }

    public List<String> getDirectoriesOptionValue(){
        List<String> dirs = new ArrayList<>();
        dirs.add("loread_root");
        dirs.add("loread_feed_title");
        dirs.add("loread_category_title");
//        String[] dirs = new String[3];
//        dirs[0] = "loread_root";
//        dirs[1] = "loread_feed_title";
//        dirs[2] = "loread_category_title";
        return dirs;
    }
    public String[] getDirectoriesOptionName(){
//        List<String> dirs = new ArrayList<>();
//        dirs.add(App.i().getString(R.string.default_directory));
//        dirs.add(App.i().getString(R.string.feed_title_as_directory));
//        dirs.add(App.i().getString(R.string.category_title_as_directory));
        //dirs.add(App.i().getString(R.string.custom_save_directory));

        String[] dirs = new String[3];
        dirs[0] = App.i().getString(R.string.root_directory);
        dirs[1] = App.i().getString(R.string.feed_title_as_directory);
        dirs[2] = App.i().getString(R.string.category_title_as_directory);
        return dirs;
    }

    public void setFeedDirectory(String feedId, String directory){
        if(StringUtils.isEmpty(directory)){
            settingByFeed.remove(feedId);
        }else {
            settingByFeed.put(feedId,directory);
        }
    }

    public void setArticleDirectory(String articleId, String directory){
        if(StringUtils.isEmpty(directory)){
            settingByArticle.remove(articleId);
        }else {
            settingByArticle.put(articleId,directory);
        }
    }

    public void newDirectory(String directory){
        directories.add(directory);
    }
}