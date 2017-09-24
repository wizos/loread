package me.wizos.loread.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.yuweiguocn.library.greendao.MigrationHelper;
import com.socks.library.KLog;

import me.wizos.loread.App;
import me.wizos.loread.data.dao.ArticleDao;
import me.wizos.loread.data.dao.DaoMaster;
import me.wizos.loread.data.dao.FeedDao;
import me.wizos.loread.data.dao.ImgDao;
import me.wizos.loread.data.dao.RequestLogDao;
import me.wizos.loread.data.dao.TagDao;


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
        MigrationHelper.migrate(db, ArticleDao.class, FeedDao.class, TagDao.class, ImgDao.class, RequestLogDao.class);// 后边填写所有的 Dao 类

        //记得要修改 DaoMaster 中的数据库版本号
//        switch (oldVersion) {
//            case 2:
//                //创建新表，注意createTable()是静态方法
//                /*********************为了与上面几篇保持连贯，所以这几个生成表，我就没有删除，只是注掉了*******************************/
//                //infosDao.createTable(db, true);
//                //infoTypeDao.createTable(db,true);
//                //AreasDao.createTable(db,true);
//                //这里我们为我们的users表，添加一个地址的字段
//                // 加入新字段
//                db.execSQL("ALTER TABLE 'Article' DROP COLUMN 'ORIGIN';");
//                db.execSQL("ALTER TABLE 'Article' ADD COLUMN 'ORIGIN_STREAMID' TEXT;");
//                db.execSQL("ALTER TABLE 'Article' ADD COLUMN 'ORIGIN_TITLE' TEXT;");
//                db.execSQL("ALTER TABLE 'Article' ADD COLUMN 'ORIGIN_HTMLURL' TEXT;");
//                // TODO
//                break;
//        }
    }

}
