package me.wizos.loread.db.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.yuweiguocn.library.greendao.MigrationHelper;
import com.socks.library.KLog;

import org.greenrobot.greendao.database.Database;

import me.wizos.loread.net.Api;

/**
 * @author Wizos on 2018/3/13.
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
        // arrayOf(COL_ID, COL_TITLE, COL_URL, COL_TAG, COL_CUSTOM_TITLE, COL_NOTIFY, COL_IMAGEURL, COL_UNREADCOUNT)
//        String[] coumle = new String[]{ FeedDao.Properties.Id , FeedDao.Properties.Title };
        String CREATE_COUNT_VIEW =
                "CREATE TEMP VIEW IF NOT EXISTS FEED_UNREAD_COUNT" +
//                        "  AS SELECT " + FeedDao.Properties.Id.columnName + "," + FeedDao.Properties.Title.columnName + "," + FeedDao.Properties.Url.columnName + "," + FeedDao.Properties.Categoryid.columnName + ",UNREADCOUNT" +
                        "  AS SELECT ID,TITLE,CATEGORYID,CATEGORYLABEL,SORTID,FIRSTITEMMSEC,URL,HTMLURL,ICONURL,OPEN_MODE,NEWEST_ITEM_TIMESTAMP_USEC,UNREADCOUNT" +
                        "  FROM " + FeedDao.TABLENAME +
                        "  LEFT JOIN (SELECT COUNT(1) AS UNREADCOUNT, " + ArticleDao.Properties.OriginStreamId.columnName +
                        "  FROM " + ArticleDao.TABLENAME +
                        "  WHERE " + ArticleDao.Properties.ReadState.columnName + " != '" + Api.ART_READED + "'" +
                        "  GROUP BY " + ArticleDao.Properties.OriginStreamId.columnName + " )" +
                        "  ON " + FeedDao.Properties.Id.columnName + " = " + ArticleDao.Properties.OriginStreamId.columnName;


        db.execSQL(CREATE_COUNT_VIEW);

        String CREATE_TAG_TRIGGER =
                "CREATE TEMP TRIGGER IF NOT EXISTS UNREAD" +
                        "  AFTER UPDATE OF READ_STATE" +
                        "  ON ARTICLE" +
                        "  WHEN" +
                        "  new.READ_STATE IS NOT old.READ_STATE" +
                        "  BEGIN" +
                        "  UPDATE FEED" +

                        "    SET UNREAD_COUNT = UNREAD_COUNT - 1;" +

                        "  WHERE ID IS old.ORIGIN_STREAM_ID; " +
                        "  END";
//        db.execSQL( CREATE_TAG_TRIGGER );
        KLog.e("数据库，创建触发器：" + CREATE_TAG_TRIGGER);
//                "        SET UNREAD_COUNT = new.$COL_TAG,\n" +
//                "                $COL_FEEDTITLE = new.$COL_TITLE\n" +
//                "        WHERE $COL_FEED IS old.$COL_ID;\n" +
//                "        END";
        // 当前值不等于旧值，如果新值为read，旧值可能为unread，unreading ， 减一
        // 如果旧值为read，加一
    }


    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
//        // 增加新表不能用这个函数，用下面那个 StatisticDao.createTable(db,true);
        // 后边填写所有的 Dao 类
        KLog.e("升级", "准备开始升级数据库" + oldVersion);
        MigrationHelper.migrate(db, ArticleDao.class, FeedDao.class, TagDao.class);

//        //记得要修改 DaoMaster 中的数据库版本号
//        switch (oldVersion) {
//            case 6:
//                break;
//            default:
//                break;
//        }
    }


//    private MaterialDialog materialDialog;
//    public void update() {
////        materialDialog = new MaterialDialog.Builder(App.i())
////                .title(R.string.is_update_title)
////                .content(R.string.dialog_please_wait)
////                .progress(true, 0)
////                .canceledOnTouchOutside(false)
////                .progressIndeterminateStyle(false)
////                .show();
////        new Thread(new Runnable() {
////            @Override
////            public void run() {
////
////
////                materialDialog.dismiss();
////            }
////        }).start();
//
////        updateHtmlDir();
//    }
//    public void updateHtmlDir(){
//        File dir = new File(App.i().getExternalFilesDir(null) + "/cache/");
//        File[] arts = dir.listFiles();
//        KLog.e("文件数量：" + arts.length);
//        String fileTitle;
//        for (File sourceFile : arts) {
//            if (sourceFile.isDirectory()){
//                fileTitle = sourceFile.getName().substring( 0,sourceFile.getName().lastIndexOf("_"));
//            }else {
//                fileTitle = sourceFile.getName().substring( 0,sourceFile.getName().lastIndexOf("."));
//            }
//            KLog.e("文件名：" + fileTitle);
//            FileUtil.moveDir(sourceFile.getAbsolutePath(), App.externalFilesDir + "/cache/" + fileTitle + "/" + sourceFile.getName());
//        }
//    }


}
