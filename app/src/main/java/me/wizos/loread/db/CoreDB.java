package me.wizos.loread.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.elvishew.xlog.XLog;
import com.tencent.wcdb.room.db.WCDBOpenHelperFactory;

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
        views = {FeedViewAllCount.class, FeedViewUnreadCount.class, FeedViewStarCount.class},
        version = 10
)
public abstract class CoreDB extends RoomDatabase {
    public static final String DATABASE_NAME = "loread.db";
    private static volatile CoreDB databaseInstance;
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
            database.execSQL("ALTER TABLE User ADD lastSyncTime INTEGER NOT NULL DEFAULT 0");

            database.execSQL("DROP VIEW IF EXISTS FeedView;");
            database.execSQL("CREATE VIEW `FeedView` AS SELECT uid,id,title,feedUrl,htmlUrl,iconUrl,displayMode,UNREAD_SUM AS unreadCount,STAR_SUM AS starCount,ALL_SUM AS allCount,state,syncInterval, lastSyncTime, lastSyncError, lastErrorCount FROM FEED LEFT JOIN (SELECT uid AS article_uid, feedId, COUNT(1) AS UNREAD_SUM FROM article WHERE readStatus != 2 GROUP BY uid,feedId) A ON FEED.uid = A.article_uid AND FEED.id = A.feedId LEFT JOIN (SELECT uid AS article_uid, feedId, COUNT(1) AS STAR_SUM FROM article WHERE starStatus = 4 GROUP BY uid,feedId) B ON FEED.uid = B.article_uid AND FEED.id = B.feedId LEFT JOIN (SELECT uid AS article_uid, feedId, COUNT(1) AS ALL_SUM FROM article GROUP BY uid,feedId) C ON FEED.uid = c.article_uid AND FEED.id = C.feedId");
        }
    };
    private static final Migration MIGRATION_4_5 = new Migration(4, 5) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Article ADD feedUrl TEXT");
        }
    };
    private static final Migration MIGRATION_5_6 = new Migration(5, 6) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("UPDATE Feed SET syncInterval = 0 WHERE syncInterval = -1");
        }
    };
    private static final Migration MIGRATION_6_7 = new Migration(6, 7) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Article ADD guid TEXT");
            database.execSQL("DROP VIEW IF EXISTS FeedView;");
            database.execSQL("DROP VIEW IF EXISTS CategoryView;");
        }
    };

    private static final Migration MIGRATION_7_8 = new Migration(7, 8) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Feed ADD lastPubDate INTEGER NOT NULL DEFAULT 0");
            database.execSQL("UPDATE Article SET feedUrl = (SELECT Feed.feedUrl FROM FEED WHERE Feed.uid = Article.uid AND Feed.id = Article.feedId) WHERE feedUrl IS NULL");
            database.execSQL("UPDATE Article SET feedTitle = (SELECT Feed.title FROM FEED WHERE Feed.uid = Article.uid AND Feed.id = Article.feedId) WHERE feedTitle IS NULL");
        }
    };

    // 增加以下索引，对于加快搜索速度并无帮助
    private static final Migration MIGRATION_8_9 = new Migration(8, 9) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // database.execSQL("CREATE INDEX IF NOT EXISTS `index_Article_readUpdated` ON `Article` (`readUpdated`)");
            // database.execSQL("CREATE INDEX IF NOT EXISTS `index_Article_starUpdated` ON `Article` (`starUpdated`)");
            // database.execSQL("CREATE INDEX IF NOT EXISTS `index_Article_pubDate` ON `Article` (`pubDate`)");
            // database.execSQL("CREATE INDEX IF NOT EXISTS `index_Article_crawlDate` ON `Article` (`crawlDate`)");
            // database.execSQL("CREATE INDEX IF NOT EXISTS `index_Feed_displayMode` ON `Feed` (`displayMode`)");

            database.execSQL("DROP INDEX `index_Article_readStatus`");
            database.execSQL("DROP INDEX `index_Article_starStatus`");
            database.execSQL("DROP INDEX `index_Article_saveStatus`");

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_Article_uid_readStatus` ON `Article` (`uid`, `readStatus`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_Article_uid_starStatus` ON `Article` (`uid`, `starStatus`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_Article_uid_crawlDate` ON `Article` (`uid`, `crawlDate`)");

            database.execSQL("CREATE VIEW `FeedViewAllCount` AS SELECT feed.uid, feed.id, a.allCount FROM feed INNER JOIN ( SELECT uid, feedId, COUNT(1) AS allCount FROM article GROUP BY uid, feedId ) a ON feed.uid = a.uid AND feed.id = a.feedId");
            database.execSQL("CREATE VIEW `FeedViewUnreadCount` AS SELECT feed.uid, feed.id, a.unreadCount FROM feed INNER JOIN ( SELECT uid, feedId, COUNT(1) AS unreadCount FROM article WHERE readStatus = 1 GROUP BY uid, feedId UNION SELECT uid, feedId, COUNT(1) AS unreadCount FROM article WHERE readStatus = 3 GROUP BY uid, feedId ) a ON feed.uid = a.uid AND feed.id = a.feedId");
            database.execSQL("CREATE VIEW `FeedViewStarCount` AS SELECT feed.uid, feed.id, a.starCount FROM feed INNER JOIN ( SELECT uid, feedId, COUNT(1) AS starCount FROM article WHERE starStatus = 4 GROUP BY uid, feedId ) a ON feed.uid = a.uid AND feed.id = a.feedId");
            // 使用execSQL在我的MX5上会报异常
            database.query("PRAGMA mmap_size=268435456;");
        }
    };


    private static final Migration MIGRATION_9_10 = new Migration(9, 10) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("DROP VIEW IF EXISTS FeedViewUnreadCount");
            database.execSQL("CREATE VIEW `FeedViewUnreadCount` AS SELECT feed.uid, feed.id, a.unreadCount FROM feed INNER JOIN ( SELECT uid, feedId, COUNT(1) AS unreadCount FROM article WHERE readStatus IN (1, 3) GROUP BY uid, feedId ) a ON feed.uid = a.uid AND feed.id = a.feedId");
        }
    };

    // 设置爬取时间可以为null，但是实际验证是不允许为null的
    public static synchronized void init(Context context) {
        if(databaseInstance == null) {
            synchronized (CoreDB.class) { // 同步锁，避免多线程时可能 new 出两个实例的情况
                if (databaseInstance == null) {
                    WCDBOpenHelperFactory factory = new WCDBOpenHelperFactory()
                            .writeAheadLoggingEnabled(true)       // 打开WAL以及读写并发，可以省略让Room决定是否要打开
                            .asyncCheckpointEnabled(true);        // 打开异步Checkpoint优化，不需要可以省略
                    XLog.i("数据库初始化");
                    databaseInstance = Room.databaseBuilder(context.getApplicationContext(), CoreDB.class, DATABASE_NAME)
                            .addCallback(new Callback() {
                                @Override
                                public void onOpen(@NonNull SupportSQLiteDatabase db) {
                                    super.onOpen(db);
                                    XLog.i("数据库版本号：" + db.getVersion());
                                    createTriggers(db);
                                }
                            })
                            .addMigrations(MIGRATION_1_2)
                            .addMigrations(MIGRATION_2_3)
                            .addMigrations(MIGRATION_3_4)
                            .addMigrations(MIGRATION_4_5)
                            .addMigrations(MIGRATION_5_6)
                            .addMigrations(MIGRATION_6_7)
                            .addMigrations(MIGRATION_7_8)
                            .addMigrations(MIGRATION_8_9)
                            .addMigrations(MIGRATION_9_10)
                            .allowMainThreadQueries()
                            .openHelperFactory(factory)
                            // // 设置日志模式, AUTOMATIC是默认行为, RAM低或者API16以下则无日志
                            // .setJournalMode(JournalMode.AUTOMATIC)
                            // //设置查询的线程池，一般不需要设置
                            // .setQueryExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                            // .setTransactionExecutor(AsyncTask.SERIAL_EXECUTOR)
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
        // //【当插入文章时】
        // String updateFeedAllCountWhenInsertArticle =
        //         "CREATE TEMP TRIGGER IF NOT EXISTS updateFeedAllCountWhenInsertArticle" +
        //                 " AFTER INSERT ON ARTICLE" +
        //                 "  BEGIN" +
        //                 "   UPDATE FEED SET ALLCOUNT = ALLCOUNT + 1 WHERE ID IS new.FEEDID AND UID IS new.UID;" +
        //                 "  END";
        // db.execSQL(updateFeedAllCountWhenInsertArticle);
        // String updateFeedUnreadCountWhenInsertArticle =
        //         "CREATE TEMP TRIGGER IF NOT EXISTS updateFeedUnreadCountWhenInsertArticle" +
        //                 " AFTER INSERT ON ARTICLE" +
        //                 " WHEN (new.READSTATUS = 1)" +
        //                 "   BEGIN" +
        //                 "        UPDATE FEED SET UNREADCOUNT = UNREADCOUNT + 1 WHERE ID IS new.FEEDID AND UID IS new.UID;" +
        //                 "   END" ;
        // db.execSQL(updateFeedUnreadCountWhenInsertArticle);
        // String updateFeedStarCountWhenInsertArticle =
        //         "CREATE TEMP TRIGGER IF NOT EXISTS updateFeedStarCountWhenInsertArticle" +
        //                 " AFTER INSERT ON ARTICLE" +
        //                 " WHEN (new.STARSTATUS = 4)" +
        //                 "      BEGIN" +
        //                 "        UPDATE FEED SET STARCOUNT = STARCOUNT + 1 WHERE ID IS new.FEEDID AND UID IS new.UID;" +
        //                 "      END";
        // db.execSQL(updateFeedStarCountWhenInsertArticle);

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


        // // 当插入新文章时，自动给 feedUrl 字段赋值
        // String updateArticleFeedUrlWhenInsertArticle =
        //         "CREATE TEMP TRIGGER IF NOT EXISTS updateArticleFeedUrlWhenInsertArticle" +
        //                 " AFTER INSERT ON ARTICLE" +
        //                 " WHEN (new.feedUrl = null)" +
        //                 "      BEGIN" +
        //                 "        UPDATE ARTICLE SET FEEDURL = (select FEEDURL from FEED where ID = new.FEEDID AND UID IS new.UID) WHERE ID IS new.ID AND UID IS new.UID;" +
        //                 "      END";
        // db.execSQL(updateArticleFeedUrlWhenInsertArticle);
        // 当 feed.feedUrl 更新时，自动更新 article.feedUrl 字段
        String updateArticleFeedUrlWhenWhenUpdateFeed =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateArticleFeedUrlWhenWhenUpdateFeed" +
                        " AFTER UPDATE OF FEEDURL ON FEED" +
                        "  BEGIN" +
                        "   UPDATE Article SET feedUrl = new.feedUrl WHERE FEEDID IS new.ID AND UID IS new.UID;" +
                        "  END";
        db.execSQL(updateArticleFeedUrlWhenWhenUpdateFeed);

        // 当 feedTitle 更新时，自动更新 feedtitle 字段
        String updateArticleFeedTitleWhenUpdateFeed =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateArticleFeedTitleWhenUpdateFeed" +
                        " AFTER UPDATE OF TITLE ON FEED" +
                        "  BEGIN" +
                        "   UPDATE Article SET feedTitle = new.title WHERE FEEDID IS new.ID AND UID IS new.UID;" +
                        "  END";
        db.execSQL(updateArticleFeedTitleWhenUpdateFeed);

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
                        "        UPDATE FEED SET UNREADCOUNT = UNREADCOUNT + 1" +
                        "        WHERE ID IS old.FEEDID AND UID IS old.UID;" +
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


        String updateTagAllCountWhenDeleteFeed =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagAllCountWhenDeleteFeed" +
                        " AFTER DELETE ON FEED" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY SET ALLCOUNT = ALLCOUNT - old.ALLCOUNT" +
                        "        WHERE ID = (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
                        "      END";
        db.execSQL(updateTagAllCountWhenDeleteFeed);
        String updateTagUnreadCountWhenDeleteFeed =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagUnreadCountWhenDeleteFeed" +
                        " AFTER DELETE ON FEED" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY SET UNREADCOUNT = UNREADCOUNT - old.UNREADCOUNT" +
                        "        WHERE ID = (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
                        "      END";
        db.execSQL(updateTagUnreadCountWhenDeleteFeed);
        String updateTagStarCountWhenDeleteFeed =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagStarCountWhenDeleteFeed" +
                        " AFTER DELETE ON FEED" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY SET STARCOUNT = STARCOUNT - old.STARCOUNT" +
                        "        WHERE ID = (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
                        "      END";
        db.execSQL(updateTagStarCountWhenDeleteFeed);

        // 当feed的总计数变动时，更新tag的总计数
        String updateTagAllCountWhenInsertArticle =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagAllCountWhenInsertArticle" +
                        " AFTER UPDATE OF ALLCOUNT ON FEED" +
                        " WHEN (new.ALLCOUNT != old.ALLCOUNT)" +
                        "   BEGIN" +
                        "        UPDATE CATEGORY SET ALLCOUNT = ALLCOUNT + (new.ALLCOUNT - old.ALLCOUNT)" +
                        "        WHERE ID IN (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
                        "   END";
        db.execSQL(updateTagAllCountWhenInsertArticle);
        // // 当feed的总计数加一时，更新tag的总计数
        // String updateTagAllCountWhenInsertArticle =
        //         "CREATE TEMP TRIGGER IF NOT EXISTS updateTagAllCountWhenInsertArticle" +
        //                 " AFTER UPDATE OF ALLCOUNT ON FEED" +
        //                 " WHEN (new.ALLCOUNT > old.ALLCOUNT)" +
        //                 "   BEGIN" +
        //                 "        UPDATE CATEGORY SET ALLCOUNT = ALLCOUNT + (new.ALLCOUNT - old.ALLCOUNT)" +
        //                 "        WHERE ID IN (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
        //                 "   END";
        // db.execSQL(updateTagAllCountWhenInsertArticle);
        // // 当feed的总计数减一时，更新tag的总计数
        // String updateTagAllCountWhenDeleteArticle =
        //         "CREATE TEMP TRIGGER IF NOT EXISTS updateTagAllCountWhenDeleteArticle" +
        //                 " AFTER UPDATE OF ALLCOUNT ON FEED" +
        //                 " WHEN (new.ALLCOUNT < old.ALLCOUNT)" +
        //                 "      BEGIN" +
        //                 "        UPDATE CATEGORY SET ALLCOUNT = ALLCOUNT - (old.ALLCOUNT - new.ALLCOUNT)" +
        //                 "        WHERE ID IN (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
        //                 "      END";
        // db.execSQL(updateTagAllCountWhenDeleteArticle);


        // 当feed的未读计数变动时，更新tag的未读计数
        String updateTagUnreadCountWhenAdd =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagUnreadCountWhenAdd" +
                        " AFTER UPDATE OF UNREADCOUNT ON FEED" +
                        " WHEN (new.UNREADCOUNT != old.UNREADCOUNT)" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY SET UNREADCOUNT = UNREADCOUNT + new.UNREADCOUNT - old.UNREADCOUNT" +
                        "        WHERE ID IN (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
                        "      END";
        db.execSQL(updateTagUnreadCountWhenAdd);
        // // 当feed的未读计数加一时，更新tag的未读计数
        // String updateTagUnreadCountWhenAdd =
        //         "CREATE TEMP TRIGGER IF NOT EXISTS updateTagUnreadCountWhenAdd" +
        //                 " AFTER UPDATE OF UNREADCOUNT ON FEED" +
        //                 " WHEN (new.UNREADCOUNT = old.UNREADCOUNT + 1)" +
        //                 "      BEGIN" +
        //                 "        UPDATE CATEGORY" +
        //                 "          SET UNREADCOUNT = UNREADCOUNT + 1" +
        //                 "          WHERE ID IN (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
        //                 "      END";
        // db.execSQL(updateTagUnreadCountWhenAdd);
        // // 当feed的未读计数减一时，更新tag的未读计数
        // String updateTagUnreadCountWhenMinus =
        //         "CREATE TEMP TRIGGER IF NOT EXISTS updateTagUnreadCountWhenMinus" +
        //                 "      AFTER UPDATE OF UNREADCOUNT ON FEED" +
        //                 "      WHEN (new.UNREADCOUNT = old.UNREADCOUNT - 1)" +
        //                 "      BEGIN" +
        //                 "        UPDATE CATEGORY" +
        //                 "          SET UNREADCOUNT = UNREADCOUNT - 1" +
        //                 "          WHERE ID IN (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
        //                 "      END";
        // db.execSQL(updateTagUnreadCountWhenMinus);

        // 当feed的星标计数变动时，更新tag的星标计数
        String updateTagStarCountWhenAdd =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagStarCountWhenAdd" +
                        " AFTER UPDATE OF STARCOUNT ON FEED" +
                        " WHEN (new.STARCOUNT != old.STARCOUNT)" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY SET STARCOUNT = STARCOUNT + new.STARCOUNT - old.STARCOUNT" +
                        "        WHERE ID IN (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
                        "      END";
        db.execSQL(updateTagStarCountWhenAdd);
        // // 当feed的星标计数加一时，更新tag的星标计数
        // String updateTagStarCountWhenAdd =
        //         "    CREATE TEMP TRIGGER IF NOT EXISTS updateTagStarCountWhenAdd" +
        //                 " AFTER UPDATE OF STARCOUNT ON FEED" +
        //                 " WHEN (new.STARCOUNT = old.STARCOUNT + 1)" +
        //                 "      BEGIN" +
        //                 "        UPDATE CATEGORY SET STARCOUNT = STARCOUNT + 1" +
        //                 "        WHERE ID IN (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
        //                 "      END";
        // db.execSQL(updateTagStarCountWhenAdd);
        // // 当feed的星标计数减一时，更新tag的未读计数
        // String updateTagStarCountWhenMinus =
        //         "    CREATE TEMP TRIGGER IF NOT EXISTS updateTagStarCountWhenMinus" +
        //                 " AFTER UPDATE OF STARCOUNT ON FEED" +
        //                 " WHEN (new.STARCOUNT = old.STARCOUNT - 1)" +
        //                 "      BEGIN" +
        //                 "        UPDATE CATEGORY SET STARCOUNT = STARCOUNT - 1" +
        //                 "        WHERE ID IN (select CATEGORYID from FEEDCATEGORY where FEEDID = old.ID AND UID IS old.UID);" +
        //                 "      END";
        // db.execSQL(updateTagStarCountWhenMinus);



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


        // 当 Feed 从 Category 中移出时，更新 CATEGORY 计数
        String updateTagUnreadCountWhenUngroup =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagUnreadCountWhenUngroup" +
                        " AFTER DELETE ON FEEDCATEGORY" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY" +
                        "        SET UNREADCOUNT = UNREADCOUNT - (select UNREADCOUNT from FEED where ID = old.FEEDID AND UID IS old.UID)" +
                        "        WHERE ID = old.CATEGORYID AND UID IS old.UID;" +
                        "      END";
        db.execSQL(updateTagUnreadCountWhenUngroup);
        String updateTagStarCountWhenUngroup =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagStarCountWhenUngroup" +
                        " AFTER DELETE ON FEEDCATEGORY" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY" +
                        "        SET STARCOUNT = STARCOUNT - (select STARCOUNT from FEED where ID = old.FEEDID AND UID IS old.UID)" +
                        "        WHERE ID = old.CATEGORYID AND UID IS old.UID;" +
                        "      END";
        db.execSQL(updateTagStarCountWhenUngroup);
        String updateTagAllCountWhenUngroup =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagAllCountWhenUngroup" +
                        " AFTER DELETE ON FEEDCATEGORY" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY" +
                        "        SET ALLCOUNT = ALLCOUNT - (select ALLCOUNT from FEED where ID = old.FEEDID AND UID IS old.UID)" +
                        "        WHERE ID = old.CATEGORYID AND UID IS old.UID;" +
                        "      END";
        db.execSQL(updateTagAllCountWhenUngroup);

        // 当 Feed 从 Category 中移入时，更新计数
        String updateTagUnreadCountWhenMakeGroup =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagUnreadCountWhenMakeGroup" +
                        " AFTER INSERT ON FEEDCATEGORY" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY" +
                        "        SET UNREADCOUNT = UNREADCOUNT + (select UNREADCOUNT from FEED where ID = new.FEEDID AND UID IS new.UID)" +
                        "        WHERE ID = new.CATEGORYID AND UID IS new.UID;" +
                        "      END";
        db.execSQL(updateTagUnreadCountWhenMakeGroup);
        String updateTagStarCountWhenMakeGroup =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagStarCountWhenMakeGroup" +
                        " AFTER INSERT ON FEEDCATEGORY" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY" +
                        "        SET STARCOUNT = STARCOUNT + (select STARCOUNT from FEED where ID = new.FEEDID AND UID IS new.UID)" +
                        "        WHERE ID = new.CATEGORYID AND UID IS new.UID;" +
                        "      END";
        db.execSQL(updateTagStarCountWhenMakeGroup);
        String updateTagAllCountWhenMakeGroup =
                "CREATE TEMP TRIGGER IF NOT EXISTS updateTagAllCountWhenMakeGroup" +
                        " AFTER INSERT ON FEEDCATEGORY" +
                        "      BEGIN" +
                        "        UPDATE CATEGORY" +
                        "        SET ALLCOUNT = ALLCOUNT + (select ALLCOUNT from FEED where ID = new.FEEDID AND UID IS new.UID)" +
                        "        WHERE ID = new.CATEGORYID AND UID IS new.UID;" +
                        "      END";
        db.execSQL(updateTagAllCountWhenMakeGroup);

        // TODO: 2021/2/10 当订阅源删除时，自动删除关联的feedCategory记录
        // TODO: 2021/2/10 当分类删除时，自动删除关联的feedCategory记录
        // TODO: 2021/2/10 当分类修改id（通常是由改名引起的）时，自动更新关联的feedCategory记录
    }


    /**
     * 我们创建的Dao对象，在这里以抽象方法的形式返回，只需一行代码即可。
     */
    public abstract UserDao userDao();
    public abstract ArticleDao articleDao();
    // public abstract Article2Dao article2Dao();
    public abstract FeedDao feedDao();
    public abstract CategoryDao categoryDao();
    public abstract FeedCategoryDao feedCategoryDao();

    public abstract TriggerRuleDao triggerRuleDao();
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
