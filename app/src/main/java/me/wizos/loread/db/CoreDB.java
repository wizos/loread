package me.wizos.loread.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.wizos.loread.App;
import me.wizos.loread.bean.feedly.CategoryItem;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.config.ArticleTags;

/**
 * @Database标签用于告诉系统这是Room数据库对象。
 * entities属性用于指定该数据库有哪些表，若需建立多张表，以逗号相隔开。
 * version属性用于指定数据库版本号，后续数据库的升级正是依据版本号来判断的。
 * 该类需要继承自RoomDatabase，在类中，通过Room.databaseBuilder()结合单例设计模式，完成数据库的创建工作。
 */
@Database(
        entities = {User.class,Article.class,Feed.class,Category.class, FeedCategory.class, Tag.class, ArticleTag.class, ArticleFts.class},
        views = {FeedView.class,CategoryView.class},
        version = 1,
        exportSchema = false
)
public abstract class CoreDB extends RoomDatabase {
    public static final String DATABASE_NAME = "loread.db";
    private static CoreDB databaseInstance;

    public static synchronized void init(Context context) {
        if(databaseInstance == null) {
            synchronized (CoreDB.class) { // 同步锁，避免多线程时可能 new 出两个实例的情况
                if (databaseInstance == null) {
                    databaseInstance = Room.databaseBuilder(context.getApplicationContext(), CoreDB.class, DATABASE_NAME)
                            .addCallback(new Callback() {
                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    KLog.e("创建触发器");
                                    createTriggers(db);
                                }
                            })
                            //.addMigrations(MIGRATION_1_2)
                            .addMigrations()
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }
    }

    public static CoreDB i(){
        if( databaseInstance == null ){
            throw new RuntimeException("CoreBD must init in Application class");
        }
        return databaseInstance;
    }

    /**
     * 创建触发器
     * 在 INSERT 型触发器中，只有NEW是合法的，NEW 用来表示将要（BEFORE）或已经（AFTER）插入的新数据；
     * 在 UPDATE 型触发器中，NEW、OLD可以同时使用，OLD 用来表示将要或已经被修改的原数据，NEW 用来表示将要或已经修改为的新数据；
     * 在 DELETE 型触发器中，只有 OLD 才合法，OLD 用来表示将要或已经被删除的原数据；
     * 使用方法： NEW.columnName （columnName 为相应数据表某一列名）
     * 另外，OLD 是只读的，而 NEW 则可以在触发器中使用 SET 赋值，这样不会再次触发触发器，造成循环调用（如每插入一个学生前，都在其学号前加“2013”）。
     * @param db
     */
    private static void createTriggers(SupportSQLiteDatabase db) {
        //【当插入文章时】
        String updateFeedAllCountWhenInsertArticle =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateFeedAllCountWhenInsertArticle" +
                        " AFTER INSERT ON ARTICLE" +
                        "  BEGIN" +
                        "   UPDATE FEED SET ALLCOUNT = ALLCOUNT + 1 WHERE ID IS new.FEEDID AND UID IS new.UID;" +
                        "  END";
        db.execSQL(updateFeedAllCountWhenInsertArticle);
        String updateFeedUnreadCountWhenInsertArticle =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateFeedUnreadCountWhenInsertArticle" +
                        " AFTER INSERT ON ARTICLE" +
                        " WHEN (new.READSTATUS = 1)" +
                        "   BEGIN" +
                        "        UPDATE FEED SET UNREADCOUNT = UNREADCOUNT + 1 WHERE ID IS new.FEEDID AND UID IS new.UID;" +
                        "   END" ;
        db.execSQL(updateFeedUnreadCountWhenInsertArticle);
        String updateFeedStarCountWhenInsertArticle =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateFeedStarCountWhenInsertArticle" +
                        " AFTER INSERT ON ARTICLE" +
                        " WHEN (new.STARSTATUS = 4)" +
                        "      BEGIN" +
                        "        UPDATE FEED SET STARCOUNT = STARCOUNT + 1 WHERE ID IS new.FEEDID AND UID IS new.UID;" +
                        "      END";
        db.execSQL(updateFeedStarCountWhenInsertArticle);



        //【当删除文章时】
        String updateFeedAllCountWhenDeleteArticle =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateFeedAllCountWhenDeleteArticle" +
                        "  AFTER DELETE ON ARTICLE" +
                        "    BEGIN" +
                        "        UPDATE FEED SET ALLCOUNT = ALLCOUNT - 1 WHERE ID IS old.FEEDID AND UID IS old.UID;" +
                        "    END";
        db.execSQL(updateFeedAllCountWhenDeleteArticle);
        String updateFeedUnreadCountWhenDeleteArticle =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateFeedUnreadCountWhenDeleteArticle" +
                        " AFTER DELETE ON ARTICLE" +
                        " WHEN (old.READSTATUS = 1 OR old.READSTATUS = 3)" +
                        "   BEGIN" +
                        "        UPDATE FEED SET UNREADCOUNT = UNREADCOUNT - 1 WHERE ID IS old.FEEDID AND UID IS old.UID;" +
                        "   END" ;
        db.execSQL(updateFeedUnreadCountWhenDeleteArticle);
        String updateFeedStarCountWhenDeleteArticle =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateFeedStarCountWhenDeleteArticle" +
                        " AFTER DELETE ON ARTICLE" +
                        " WHEN (old.STARSTATUS = 4)" +
                        "      BEGIN" +
                        "        UPDATE FEED SET STARCOUNT = STARCOUNT - 1 WHERE ID IS old.FEEDID AND UID IS old.UID;" +
                        "      END";
        db.execSQL(updateFeedStarCountWhenDeleteArticle);

//        // 当 READSTATUS 状态更新时，标注其更新时间
//        String updatedWhenArticleChange =
//                "CREATE TEMP TRIGGER IF NOT EXISTS updatedWhenArticleChange" +
//                        " AFTER UPDATE OF READSTATUS,STARSTATUS ON ARTICLE" +
//                        " WHEN old.READSTATUS != new.READSTATUS OR old.STARSTATUS != new.STARSTATUS" +
//                        " BEGIN" +
//                        "  UPDATE ARTICLE SET updateTime = (strftime('%s','now') || substr(strftime('%f','now'),4))" +
//                        "  WHERE UID IS old.UID AND ID IS old.ID;" +
//                        " END";
//        db.execSQL(updatedWhenArticleChange);

        // 当 READSTATUS 状态更新时，标注其更新时间
        String updatedWhenReadStatusChange =
                "CREATE TEMP TRIGGER IF NOT EXISTS updatedWhenReadStatusChange" +
                        " AFTER UPDATE OF READSTATUS ON ARTICLE" +
                        " WHEN old.READSTATUS != new.READSTATUS" +
                        " BEGIN" +
                        "  UPDATE ARTICLE SET readUpdated = (strftime('%s','now') || substr(strftime('%f','now'),4))" +
                        "  WHERE ID IS old.ID AND UID IS old.UID;" +
                        " END";
        db.execSQL(updatedWhenReadStatusChange);
        // 当 STARSTATUS 状态更新时，标注其更新时间
        String updatedWhenStarStatusChange =
                "CREATE TEMP TRIGGER IF NOT EXISTS updatedWhenStarStatusChange" +
                        " AFTER UPDATE OF STARSTATUS ON ARTICLE" +
                        " WHEN old.STARSTATUS != new.STARSTATUS" +
                        " BEGIN" +
                        "  UPDATE ARTICLE SET starUpdated = (strftime('%s','now') || substr(strftime('%f','now'),4))" +
                        "  WHERE ID IS old.ID AND UID IS old.UID;" +
                        " END";
        db.execSQL(updatedWhenStarStatusChange);


        // 当updateTime因为
        String readUpdatedNoChange =
                "CREATE TEMP TRIGGER IF NOT EXISTS readUpdatedNoChange" +
                        " AFTER UPDATE OF readUpdated ON ARTICLE" +
                        " WHEN old.readUpdated > new.readUpdated" +
                        " BEGIN" +
                        "  UPDATE ARTICLE SET readUpdated = old.readUpdated" +
                        "  WHERE UID IS old.UID AND ID IS old.ID;" +
                        " END";
        db.execSQL(readUpdatedNoChange);

        // 当updateTime因为
        String starUpdatedNoChange =
                "CREATE TEMP TRIGGER IF NOT EXISTS starUpdatedNoChange" +
                        " AFTER UPDATE OF starUpdated ON ARTICLE" +
                        " WHEN old.starUpdated > new.starUpdated" +
                        " BEGIN" +
                        "  UPDATE ARTICLE SET starUpdated = old.starUpdated" +
                        "  WHERE UID IS old.UID AND ID IS old.ID;" +
                        " END";
        db.execSQL(starUpdatedNoChange);


        // 标记文章为已读时，更新feed的未读计数
        String updateFeedUnreadCountWhenReadArticle =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateFeedUnreadCountWhenReadArticle" +
                        " AFTER UPDATE OF READSTATUS ON ARTICLE" +
                        " WHEN (old.READSTATUS != 2 AND new.READSTATUS = 2)" +
                        " BEGIN" +
                        "  UPDATE FEED SET UNREADCOUNT = UNREADCOUNT - 1 WHERE ID IS old.FEEDID AND UID IS old.UID;" +
                        " END";
        db.execSQL(updateFeedUnreadCountWhenReadArticle);
        // 标记文章为未读时，更新feed的未读计数
        String updateFeedUnreadCountWhenUnreadArticle =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateFeedUnreadCountWhenUnreadArticle" +
                        "      AFTER UPDATE OF READSTATUS ON ARTICLE" +
                        "      WHEN (old.READSTATUS = 2 AND new.READSTATUS != 2 )" +
                        "      BEGIN" +
                        "        UPDATE FEED" +
                        "          SET UNREADCOUNT = UNREADCOUNT + 1" +
                        "          WHERE ID IS old.FEEDID AND UID IS old.UID;" +
                        "      END";
        db.execSQL(updateFeedUnreadCountWhenUnreadArticle);

        // 标记文章为加星时，更新feed的计数
        String updateFeedUnreadCountWhenStaredArticle =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateFeedUnreadCountWhenStaredArticle" +
                        "      AFTER UPDATE OF STARSTATUS ON ARTICLE" +
                        "      WHEN (old.STARSTATUS != 4 AND new.STARSTATUS = 4)" +
                        "      BEGIN" +
                        "        UPDATE FEED SET STARCOUNT = STARCOUNT + 1" +
                        "        WHERE ID IS old.FEEDID AND UID IS old.UID;" +
                        "      END";
        db.execSQL(updateFeedUnreadCountWhenStaredArticle);
        // 标记文章为无星时，更新feed的计数
        String updateFeedUnreadCountWhenUnstarArticle =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateFeedUnreadCountWhenUnstarArticle" +
                        "      AFTER UPDATE OF STARSTATUS ON ARTICLE" +
                        "      WHEN (old.STARSTATUS != 5 AND new.STARSTATUS = 5)" +
                        "      BEGIN" +
                        "        UPDATE FEED SET STARCOUNT = STARCOUNT - 1" +
                        "        WHERE ID IS old.FEEDID AND UID IS old.UID;" +
                        "      END";
        db.execSQL(updateFeedUnreadCountWhenUnstarArticle);


        // 当feed的总计数加一时，更新tag的总计数
        String updateTagAllCountWhenInsertArticle =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagAllCountWhenInsertArticle" +
                        " AFTER UPDATE OF ALLCOUNT ON FEED" +
                        " WHEN (new.ALLCOUNT = old.ALLCOUNT + 1)" +
                        "   BEGIN" +
                        "        UPDATE CATEGORY SET ALLCOUNT = ALLCOUNT + 1" +
                        "        WHERE ID IN (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
                        "   END";
        db.execSQL(updateTagAllCountWhenInsertArticle);
        // 当feed的总计数减一时，更新tag的总计数
        String updateTagAllCountWhenDeleteArticle =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagAllCountWhenDeleteArticle" +
                        " AFTER UPDATE OF ALLCOUNT ON FEED" +
                        " WHEN (new.ALLCOUNT = old.ALLCOUNT - 1)" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY SET ALLCOUNT = ALLCOUNT - 1" +
                        "        WHERE ID IN (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
                        "      END";
        db.execSQL(updateTagAllCountWhenDeleteArticle);


        // 当feed的未读计数加一时，更新tag的未读计数
        String updateTagUnreadCountWhenAdd =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagUnreadCountWhenAdd" +
                        " AFTER UPDATE OF UNREADCOUNT ON FEED" +
                        " WHEN (new.UNREADCOUNT = old.UNREADCOUNT + 1)" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY" +
                        "          SET UNREADCOUNT = UNREADCOUNT + 1" +
                        "          WHERE ID IN (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
                        "      END";
        db.execSQL(updateTagUnreadCountWhenAdd);
        // 当feed的未读计数减一时，更新tag的未读计数
        String updateTagUnreadCountWhenMinus =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagUnreadCountWhenMinus" +
                        "      AFTER UPDATE OF UNREADCOUNT ON FEED" +
                        "      WHEN (new.UNREADCOUNT = old.UNREADCOUNT - 1)" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY" +
                        "          SET UNREADCOUNT = UNREADCOUNT - 1" +
                        "          WHERE ID IN (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
                        "      END";
        db.execSQL(updateTagUnreadCountWhenMinus);


        // 当feed的星标计数加一时，更新tag的星标计数
        String updateTagStarCountWhenAdd =
                "    CREATE TEMP TRIGGER IF NOT EXISTS updateTagStarCountWhenAdd" +
                        " AFTER UPDATE OF STARCOUNT ON FEED" +
                        " WHEN (new.STARCOUNT = old.STARCOUNT + 1)" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY SET STARCOUNT = STARCOUNT + 1" +
                        "        WHERE ID IN (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
                        "      END";
        db.execSQL(updateTagStarCountWhenAdd);

        String updateTagStarCountWhenMinus =
                "    CREATE TEMP TRIGGER IF NOT EXISTS updateTagStarCountWhenMinus" +
                        " AFTER UPDATE OF STARCOUNT ON FEED" +
                        " WHEN (new.STARCOUNT = old.STARCOUNT - 1)" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY SET STARCOUNT = STARCOUNT - 1" +
                        "        WHERE ID IN (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
                        "      END";
        db.execSQL(updateTagStarCountWhenMinus);
    }


    /**
     * 我们创建的Dao对象，在这里以抽象方法的形式返回，只需一行代码即可。
     */
    public abstract UserDao userDao();
    public abstract ArticleDao articleDao();
    public abstract FeedDao feedDao();
    public abstract CategoryDao categoryDao();
    public abstract FeedCategoryDao feedCategoryDao();

    public abstract TagDao tagDao();
    public abstract ArticleTagDao articleTagDao();

    public void coverFeedCategories(EditFeed editFeed) {
        String uid = App.i().getUser().getId();
        List<CategoryItem> cloudyCategoryItems = editFeed.getCategoryItems();
        ArrayList<FeedCategory> feedCategories = new ArrayList<>(cloudyCategoryItems.size());
        FeedCategory feedCategory;
        for (CategoryItem categoryItem : cloudyCategoryItems) {
            feedCategory = new FeedCategory(uid, editFeed.getId(), categoryItem.getId());
            feedCategories.add(feedCategory);
        }

        CoreDB.i().feedCategoryDao().deleteByFeedId(uid, editFeed.getId());
        CoreDB.i().feedCategoryDao().insert(feedCategories);
    }

    public void deleteFeed(Feed feed){
        List<Article> articles = CoreDB.i().articleDao().getStaredByFeedId(feed.getUid(),feed.getId());
        List<Category> categories = CoreDB.i().categoryDao().getByFeedId(feed.getUid(),feed.getId());

        List<ArticleTag> articleTags = new ArrayList<>();
        Set<String> tagTitleSet = new HashSet<>();
        for (Article article: articles){
            for (Category category:categories) {
                articleTags.add( new ArticleTag(feed.getUid(), article.getId(), category.getTitle()) );
                tagTitleSet.add(category.getTitle());
            }
        }
        List<Tag> tags = new ArrayList<>(tagTitleSet.size());
        for (String title:tagTitleSet) {
            Tag tag = new Tag();
            tag.setUid(feed.getUid());
            tag.setId(title);
            tag.setTitle(title);
            tags.add(tag);
            //KLog.e("设置 Tag 数据：" + tag);
        }

        CoreDB.i().tagDao().insert(tags);
        CoreDB.i().articleTagDao().insert(articleTags);
        ArticleTags.i().addArticleTags(articleTags);
        ArticleTags.i().save();
        feedDao().delete(feed);
    }

}
