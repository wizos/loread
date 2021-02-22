package me.wizos.loread.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.wizos.loread.App;
import me.wizos.loread.bean.feedly.CategoryItem;
import me.wizos.loread.bean.feedly.input.EditFeed;
import me.wizos.loread.config.ArticleTags;
import me.wizos.loread.db.rule.Action;
import me.wizos.loread.db.rule.Condition;
import me.wizos.loread.db.rule.Scope;

/**
 * Database 标签用于告诉系统这是Room数据库对象。
 * entities 属性用于指定该数据库有哪些表，若需建立多张表，以逗号相隔开。
 * version 属性用于指定数据库版本号，后续数据库的升级正是依据版本号来判断的。
 * 该类需要继承自RoomDatabase，在类中，通过Room.databaseBuilder()结合单例设计模式，完成数据库的创建工作。
 */
@Database(
        entities = {User.class,Article.class,Feed.class,Category.class, FeedCategory.class, Tag.class, ArticleTag.class, Scope.class, Condition.class, Action.class},
        views = {FeedView.class,CategoryView.class},
        version = 4
)
public abstract class CoreDB extends RoomDatabase {
    public static final String DATABASE_NAME = "loread.db";
    private static CoreDB databaseInstance;
    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE ArticleFts");
            // database.execSQL("ALTER TABLE Article RENAME TO Article_old");
            // database.execSQL("CREATE TABLE IF NOT EXISTS `Article` (`id` TEXT NOT NULL, `uid` TEXT NOT NULL, `title` TEXT, `content` TEXT, `summary` TEXT, `image` TEXT, `enclosure` TEXT, `feedId` TEXT, `feedTitle` TEXT, `author` TEXT, `link` TEXT, `pubDate` INTEGER NOT NULL, `crawlDate` INTEGER, `readStatus` INTEGER NOT NULL, `starStatus` INTEGER NOT NULL, `saveStatus` INTEGER NOT NULL, `readUpdated` INTEGER NOT NULL, `starUpdated` INTEGER NOT NULL, PRIMARY KEY(`id`, `uid`), FOREIGN KEY(`uid`) REFERENCES `User`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )");
            // database.execSQL("INSERT INTO Article (id,uid,title,content,summary,image,enclosure,feedId,feedTitle,author,link,pubDate,crawlDate,readStatus,starStatus,saveStatus,readUpdated,starUpdated) select id,uid,title,content,summary,image,enclosure,feedId,feedTitle,author,link,pubDate,crawlDate,readStatus,starStatus,saveStatus,readUpdated,starUpdated from Article_old");
            // database.execSQL("DROP TABLE Article_old;");
        }
    };
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `Scope` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uid` TEXT, `type` TEXT, `target` TEXT, FOREIGN KEY(`uid`) REFERENCES `User`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_Target_uid` ON `Scope` (`uid`)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `Condition` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uid` TEXT, `scopeId` INTEGER NOT NULL, `attr` TEXT, `judge` TEXT, `value` TEXT, FOREIGN KEY(`scopeId`) REFERENCES `Scope`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_Condition_ruleId` ON `Condition` (`scopeId`)");

            database.execSQL("CREATE TABLE IF NOT EXISTS `Action` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `uid` TEXT, `scopeId` INTEGER NOT NULL, `action` TEXT, FOREIGN KEY(`scopeId`) REFERENCES `Scope`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_Action_ruleId` ON `Action` (`scopeId`)");
        }
    };
    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Feed ADD syncInterval INTEGER NOT NULL DEFAULT -1");
            database.execSQL("ALTER TABLE Feed ADD lastSyncTime INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE Feed ADD lastSyncError TEXT");
            database.execSQL("ALTER TABLE Feed ADD lastErrorCount INTEGER NOT NULL DEFAULT 0");

            // database.execSQL("ALTER TABLE User ADD syncInterval INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE User ADD lastSyncTime INTEGER NOT NULL DEFAULT 0");

            database.execSQL("DROP VIEW IF EXISTS FeedView;");
            database.execSQL("CREATE VIEW `FeedView` AS SELECT uid,id,title,feedUrl,htmlUrl,iconUrl,displayMode,UNREAD_SUM AS unreadCount,STAR_SUM AS starCount,ALL_SUM AS allCount,state,syncInterval, lastSyncTime, lastSyncError, lastErrorCount FROM FEED LEFT JOIN (SELECT uid AS article_uid, feedId, COUNT(1) AS UNREAD_SUM FROM article WHERE readStatus != 2 GROUP BY uid,feedId) A ON FEED.uid = A.article_uid AND FEED.id = A.feedId LEFT JOIN (SELECT uid AS article_uid, feedId, COUNT(1) AS STAR_SUM FROM article WHERE starStatus = 4 GROUP BY uid,feedId) B ON FEED.uid = B.article_uid AND FEED.id = B.feedId LEFT JOIN (SELECT uid AS article_uid, feedId, COUNT(1) AS ALL_SUM FROM article GROUP BY uid,feedId) C ON FEED.uid = c.article_uid AND FEED.id = C.feedId");
        }
    };
    // 设置爬取时间可以为null，但是实际验证是不允许为null的

    public static synchronized void init(Context context) {
        if(databaseInstance == null) {
            synchronized (CoreDB.class) { // 同步锁，避免多线程时可能 new 出两个实例的情况
                if (databaseInstance == null) {
                    databaseInstance = Room.databaseBuilder(context.getApplicationContext(), CoreDB.class, DATABASE_NAME)
                            .addCallback(new Callback() {
                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    createTriggers(db);
                                }
                            })
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .addMigrations(MIGRATION_3_4)
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

        String updateTagAllCountWhenDeleteFeed =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagAllCountWhenDeleteFeed" +
                        " AFTER DELETE ON FEED" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY" +
                        "        SET ALLCOUNT = ALLCOUNT - old.ALLCOUNT" +
                        "        WHERE ID = (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
                        "      END";
        db.execSQL(updateTagAllCountWhenDeleteFeed);
        String updateTagUnreadCountWhenDeleteFeed =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagUnreadCountWhenDeleteFeed" +
                        " AFTER DELETE ON FEED" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY" +
                        "        SET UNREADCOUNT = UNREADCOUNT - old.UNREADCOUNT" +
                        "        WHERE ID = (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
                        "      END";
        db.execSQL(updateTagUnreadCountWhenDeleteFeed);
        String updateTagStarCountWhenDeleteFeed =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagStarCountWhenDeleteFeed" +
                        " AFTER DELETE ON FEED" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY" +
                        "        SET STARCOUNT = STARCOUNT - old.STARCOUNT" +
                        "        WHERE ID = (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
                        "      END";
        db.execSQL(updateTagStarCountWhenDeleteFeed);


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
        // 当feed的星标计数减一时，更新tag的未读计数
        String updateTagStarCountWhenMinus =
                "    CREATE TEMP TRIGGER IF NOT EXISTS updateTagStarCountWhenMinus" +
                        " AFTER UPDATE OF STARCOUNT ON FEED" +
                        " WHEN (new.STARCOUNT = old.STARCOUNT - 1)" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY SET STARCOUNT = STARCOUNT - 1" +
                        "        WHERE ID IN (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
                        "      END";
        db.execSQL(updateTagStarCountWhenMinus);



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


        // 增加该触发器是因为当我在未读模式下，进入文章页时，是先从数据中获取到 article（此时 readUpdated 是0），再修改 readStatus 状态。
        // 此时因修改 readStatus 触发的修改 readUpdated 规则，是无法同步到在文章页中的 article（除非使用LiveData）。
        // 如果此时我去获取全文，因为会复制一份 article ，并将全文数据注入，并保存到数据库，从而导致数据库中，readUpdated 被覆盖为 0 （由于获取全文时 readStatus 没有更新，不会触发更新 readUpdated）
        // String readUpdatedNoChange =
        //         "CREATE TEMP TRIGGER IF NOT EXISTS readUpdatedNoChange" +
        //                 " AFTER UPDATE OF readUpdated ON ARTICLE" +
        //                 " WHEN old.readUpdated > new.readUpdated" +
        //                 " BEGIN" +
        //                 "  UPDATE ARTICLE SET readUpdated = old.readUpdated" +
        //                 "  WHERE UID IS old.UID AND ID IS old.ID;" +
        //                 " END";
        // db.execSQL(readUpdatedNoChange);
        //
        // // 当updateTime因为
        // String starUpdatedNoChange =
        //         "CREATE TEMP TRIGGER IF NOT EXISTS starUpdatedNoChange" +
        //                 " AFTER UPDATE OF starUpdated ON ARTICLE" +
        //                 " WHEN old.starUpdated > new.starUpdated" +
        //                 " BEGIN" +
        //                 "  UPDATE ARTICLE SET starUpdated = old.starUpdated" +
        //                 "  WHERE UID IS old.UID AND ID IS old.ID;" +
        //                 " END";
        // db.execSQL(starUpdatedNoChange);


        String onCascadeWhenDeleteCategory =
                "CREATE TEMP TRIGGER IF NOT EXISTS onCascadeWhenDeleteCategory" +
                        "  AFTER DELETE ON CATEGORY" +
                        "    BEGIN" +
                        "        DELETE FROM Scope WHERE UID IS old.UID AND type IS 'category' AND target IS old.id;" +
                        "    END";
        db.execSQL(onCascadeWhenDeleteCategory);

        String onCascadeWhenDeleteFeed =
                "CREATE TEMP TRIGGER IF NOT EXISTS onCascadeWhenDeleteFeed" +
                        "  AFTER DELETE ON FEED" +
                        "    BEGIN" +
                        "        DELETE FROM Scope WHERE UID IS old.UID AND type IS 'feed' AND target IS old.id;" +
                        "    END";
        db.execSQL(onCascadeWhenDeleteFeed);


        // 当插入新文章时，自动给 feedtitle 字段赋值
        String updateArticleFeedTitleWhenInsertArticle =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateArticleFeedTitleWhenInsertArticle" +
                        " AFTER INSERT ON ARTICLE" +
                        "  BEGIN" +
                        "   UPDATE ARTICLE SET FEEDTITLE = (select TITLE from FEED where ID = new.FEEDID AND UID IS new.UID) WHERE ID IS new.ID AND UID IS new.UID;" +
                        "  END";
        db.execSQL(updateArticleFeedTitleWhenInsertArticle);
        // 当 feed.title 更新时，自动更新 feedtitle 字段
        String updateArticleFeedTitleWhenUpdateFeed =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateArticleFeedTitleWhenUpdateFeed" +
                        " AFTER UPDATE OF TITLE ON FEED" +
                        "  BEGIN" +
                        "   UPDATE Article SET feedTitle = new.title WHERE FEEDID IS new.ID AND UID IS new.UID;" +
                        "  END";
        db.execSQL(updateArticleFeedTitleWhenUpdateFeed);

        // TODO: 2021/2/10 当订阅源删除时，自动删除关联的feedCategory记录
        // TODO: 2021/2/10 当分类删除时，自动删除关联的feedCategory记录

        // TODO: 2021/2/10 当分类修改id（通常是由改名引起的）时，自动更新关联的feedCategory记录
    }


    /**
     * 我们创建的Dao对象，在这里以抽象方法的形式返回，只需一行代码即可。
     */
    public abstract UserDao userDao();
    public abstract ArticleDao articleDao();
    public abstract FeedDao feedDao();
    public abstract CategoryDao categoryDao();
    public abstract FeedCategoryDao feedCategoryDao();

    public abstract TriggerRuleDao triggerRuleDao();
    // public abstract ArticleActionRuleDao articleActionRuleDao();
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
