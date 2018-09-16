package me.wizos.loread.db.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.yuweiguocn.library.greendao.MigrationHelper;
import com.socks.library.KLog;

import java.io.File;

import me.wizos.loread.App;
import me.wizos.loread.data.WithPref;
import me.wizos.loread.net.Api;
import me.wizos.loread.utils.FileUtil;

/**
 * @author Wizos on 2016/3/15.
 */

public class SQLiteOpenHelperS extends DaoMaster.OpenHelper {
    public SQLiteOpenHelperS(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }


    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // 启用外键约束
            db.setForeignKeyConstraintsEnabled(true);
        }
        createViewsAndTriggers(db);
    }

    /**
     * 创建视图与触发器
     *
     * @param db
     */
    private void createViewsAndTriggers(SQLiteDatabase db) {
        String CREATE_COUNT_VIEW =
                "CREATE TEMP VIEW IF NOT EXISTS FEED_UNREAD_COUNT" +
                        "  AS SELECT ID,TITLE,CATEGORYID,CATEGORYLABEL,SORTID,FIRSTITEMMSEC,URL,HTMLURL,ICONURL,OPEN_MODE,NEWEST_ITEM_TIMESTAMP_USEC,UNREADCOUNT" +
                        "  FROM FEED" +
                        "  LEFT JOIN (SELECT COUNT(1) AS UNREADCOUNT, ORIGIN_STREAM_ID" +
                        "  FROM ARTICLE WHERE READ_STATUS != " + Api.READED + " GROUP BY ORIGIN_STREAM_ID)" +
                        "  ON ID = ORIGIN_STREAM_ID";

//        String CREATE_COUNT_VIEW =
//                "CREATE TEMP VIEW IF NOT EXISTS FEED_COUNT" +
//                        "  AS SELECT ID,TITLE,CATEGORYID,CATEGORYLABEL,SORTID,FIRSTITEMMSEC,URL,HTMLURL,ICONURL,OPEN_MODE,NEWEST_ITEM_TIMESTAMP_USEC,UNREADCOUNT,STAREDCOUNT,ALLCOUNT" +
//                        "  FROM FEED" +
//                        "  LEFT JOIN (SELECT COUNT(1) AS UNREADCOUNT,ORIGIN_STREAM_ID FROM ARTICLE WHERE READ_STATUS != " + Api.READED + " GROUP BY ORIGIN_STREAM_ID) A" +
//                        "  ON ID = A.ORIGIN_STREAM_ID" +
//                        "  LEFT JOIN (SELECT COUNT(1) AS STAREDCOUNT,ORIGIN_STREAM_ID FROM ARTICLE WHERE STAR_STATUS == " + Api.STARED + " GROUP BY ORIGIN_STREAM_ID) B" +
//                        "  ON ID = B.ORIGIN_STREAM_ID" +
//                        "  LEFT JOIN (SELECT COUNT(1) AS ALLCOUNT,ORIGIN_STREAM_ID FROM ARTICLE GROUP BY ORIGIN_STREAM_ID) C" +
//                        "  ON ID = C.ORIGIN_STREAM_ID";
        db.execSQL(CREATE_COUNT_VIEW);


        String createTagUnreadCountView =
                "CREATE TEMP VIEW IF NOT EXISTS TAG_UNREAD_COUNT" +
                        "  AS SELECT ID,TITLE,SORTID,NEWEST_ITEM_TIMESTAMP_USEC,UNREADCOUNT" +
                        "  FROM TAG" +
                        "  LEFT JOIN (SELECT CATEGORYID,SUM(UNREAD_COUNT) AS UNREADCOUNT FROM FEED GROUP BY CATEGORYID )  ON ID = CATEGORYID";

        KLog.e("创建临时视图：" + createTagUnreadCountView);
        db.execSQL(createTagUnreadCountView);

//        String MARK_READ_FEED =
//                "    CREATE TEMP TRIGGER IF NOT EXISTS MARK_READ_FEED" +
//                        "      AFTER UPDATE OF READ_STATUS" +
//                        "      ON ARTICLE" +
//                        "      WHEN (old.READ_STATUS = 1 AND new.READ_STATUS = 2)" +
//                        "      BEGIN" +
//                        "        UPDATE FEED" +
//                        "          SET UNREAD_COUNT = UNREAD_COUNT - 1" +
//                        "          WHERE ID IS old.ORIGIN_STREAM_ID;" +
//                        "      END";
//        String MARK_READ_TAG =
//                "    CREATE TEMP TRIGGER IF NOT EXISTS MARK_READ_TAG" +
//                        "      AFTER UPDATE OF UNREAD_COUNT" +
//                        "      ON FEED" +
//                        "      WHEN (new.UNREAD_COUNT = old.UNREAD_COUNT - 1)" +
//                        "      BEGIN" +
//                        "        UPDATE TAG" +
//                        "          SET UNREAD_COUNT = UNREAD_COUNT - 1" +
//                        "          WHERE ID IS old.CATEGORYID;" +
//                        "      END";
//
//        String MARK_UNREAD_FEED =
//                "    CREATE TEMP TRIGGER IF NOT EXISTS MARK_UNREAD_FEED" +
//                        "      AFTER UPDATE OF READ_STATUS" +
//                        "      ON ARTICLE" +
//                        "      WHEN (old.READ_STATUS = 2 OR old.READ_STATUS = 1) AND new.READ_STATUS = 3" +
//                        "      BEGIN" +
//                        "        UPDATE FEED" +
//                        "          SET UNREAD_COUNT = UNREAD_COUNT + 1" +
//                        "          WHERE ID IS old.ORIGIN_STREAM_ID;" +
//                        "      END";
//
//        String MARK_UNREAD_TAG =
//                "    CREATE TEMP TRIGGER IF NOT EXISTS MARK_UNREAD_TAG" +
//                        "      AFTER UPDATE OF UNREAD_COUNT" +
//                        "      ON FEED" +
//                        "      WHEN (new.UNREAD_COUNT = old.UNREAD_COUNT + 1)" +
//                        "      BEGIN" +
//                        "        UPDATE TAG" +
//                        "          SET UNREAD_COUNT = UNREAD_COUNT + 1" +
//                        "          WHERE ID IS old.CATEGORYID;" +
//                        "      END";

//        db.execSQL( MARK_READ_FEED );
//        db.execSQL( MARK_READ_TAG );
//        db.execSQL( MARK_UNREAD_FEED );
//        db.execSQL( MARK_UNREAD_TAG );
//        KLog.e("数据库，创建触发器：" );

//        String Test  =
//                "    CREATE TEMP TRIGGER IF NOT EXISTS MARK_READ" +
//                        "      AFTER UPDATE OF READ_STATUS" +
//                        "      ON ARTICLE" +
//                        "      WHEN (old.READ_STATUS = 2 AND new.READ_STATUS = 3)" +
//                        "      BEGIN" +
//                        "        UPDATE FEED" +
//                        "          SET UNREAD_COUNT = UNREAD_COUNT + 1" +
//                        "          WHERE ID IS old.ORIGIN_STREAM_ID;" +
//                        "      END";

        // 当前值不等于旧值，如果新值为read，旧值可能为unread，unreading ， 减一
        // 如果旧值为read，加一
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        KLog.e("升级，准备开始升级数据库" + oldVersion + "  =  " + newVersion);

//        //记得要修改 DaoMaster 中的数据库版本号
        switch (oldVersion) {
            case 7:
                MigrationHelper.migrate(db, ArticleDao.class, FeedDao.class, TagDao.class);
                updateHtmlDir();
            case 8:
                MigrationHelper.migrate(db, ArticleDao.class, FeedDao.class, TagDao.class);
                updateData(db);
            default:
                break;
        }

    }


//    private MaterialDialog materialDialog;
//    public void update() {
//        materialDialog = new MaterialDialog.Builder(App.i())
//                .title(R.string.is_update_title)
//                .content(R.string.dialog_please_wait)
//                .progress(true, 0)
//                .canceledOnTouchOutside(false)
//                .progressIndeterminateStyle(false)
//                .show();
//        materialDialog.dismiss();
//    }
private void updateData(SQLiteDatabase db) {
    String sql = "UPDATE ARTICLE SET READ_STATUS = 2 WHERE READ_STATE = 'Readed';";
    db.execSQL(sql);
    sql = "UPDATE ARTICLE SET READ_STATUS = 1 WHERE READ_STATE = 'UnRead';";
    db.execSQL(sql);
    sql = "UPDATE ARTICLE SET READ_STATUS = 3 WHERE READ_STATE = 'UnReading';";
    db.execSQL(sql);
    sql = "UPDATE ARTICLE SET STAR_STATUS = 4 WHERE STAR_STATE = 'Stared';";
    db.execSQL(sql);
    sql = "UPDATE ARTICLE SET STAR_STATUS = 5 WHERE STAR_STATE = 'UnStar';";
    db.execSQL(sql);

    if (WithPref.i().getStreamState().equals("%")) {
        WithPref.i().setStreamStatus(Api.ALL);
    } else if (WithPref.i().getStreamState().equals("Readed")) {
        WithPref.i().setStreamStatus(Api.READED);
    } else if (WithPref.i().getStreamState().equals("UnRead")) {
        WithPref.i().setStreamStatus(Api.UNREAD);
    } else if (WithPref.i().getStreamState().equals("UnReading")) {
        WithPref.i().setStreamStatus(Api.UNREADING);
    } else if (WithPref.i().getStreamState().equals("Stared")) {
        WithPref.i().setStreamStatus(Api.STARED);
    } else if (WithPref.i().getStreamState().equals("UnStar")) {
        WithPref.i().setStreamStatus(Api.UNSTAR);
    }
}

    public void updateHtmlDir() {
        File dir = new File(App.i().getExternalFilesDir(null) + "/cache/");
        File[] arts = dir.listFiles();
        KLog.e("文件数量：" + arts.length);
        String fileTitle;
        for (File sourceFile : arts) {
            if (sourceFile.isDirectory()) {
                fileTitle = sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf("_"));
            } else {
                fileTitle = sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf("."));
            }
            KLog.e("文件名：" + fileTitle);
            FileUtil.moveDir(sourceFile.getAbsolutePath(), App.externalFilesDir + "/cache/" + fileTitle + "/" + sourceFile.getName());
        }
    }


}
