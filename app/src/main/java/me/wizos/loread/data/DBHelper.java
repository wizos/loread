package me.wizos.loread.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.socks.library.KLog;

import me.wizos.loread.App;
import me.wizos.loread.data.dao.DaoMaster;
import me.wizos.loread.data.dao.StatisticDao;


/**
 * Created by Wizos on 2016/3/15.
 */
public class DBHelper extends DaoMaster.OpenHelper {
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    public static DaoMaster startUpgrade(Context context) {
        DBHelper helper = new DBHelper(context, App.DB_NAME, null);
        DaoMaster daoMaster = new DaoMaster(helper.getWritableDatabase());
        return daoMaster;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        KLog.e("正在执行数据库升级");
        // 增加新表不能用这个函数，用下面那个 StatisticDao.createTable(db,true);
//        MigrationHelper.migrate(db, ArticleDao.class, FeedDao.class, TagDao.class, ImgDao.class, RequestLogDao.class, StatisticDao.class );// 后边填写所有的 Dao 类

        //记得要修改 DaoMaster 中的数据库版本号
        switch (oldVersion) {
            case 5:

                //创建新表，注意createTable()是静态方法
                StatisticDao.createTable(db, true);

                // 加入新字段
//                db.execSQL("ALTER TABLE 'Article' DROP COLUMN 'ORIGIN';");
//                db.execSQL("ALTER TABLE 'Article' ADD COLUMN 'ORIGIN_STREAMID' TEXT;");
//                db.execSQL("ALTER TABLE 'Article' ADD COLUMN 'ORIGIN_TITLE' TEXT;");
//                db.execSQL("ALTER TABLE 'Article' ADD COLUMN 'ORIGIN_HTMLURL' TEXT;");
                // TODO
                break;
        }
    }

}
