package me.wizos.loread.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.yuweiguocn.library.greendao.MigrationHelper;

import me.wizos.loread.App;
import me.wizos.loread.dao.ArticleDao;
import me.wizos.loread.dao.DaoMaster;
import me.wizos.loread.dao.FeedDao;
import me.wizos.loread.dao.RequestLogDao;
import me.wizos.loread.dao.TagDao;


/**
 * Created by Wizos on 2016/3/15.
 */
public class UpdateDB extends DaoMaster.OpenHelper {
    public UpdateDB(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    public static void upgrade(Context context){
        UpdateDB helper = new UpdateDB(context, App.DB_NAME,null);// 升级数据库成功
        DaoMaster daoMaster = new DaoMaster(helper.getWritableDatabase());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        MigrationHelper.getInstance().migrate(db,ArticleDao.class,FeedDao.class,RequestLogDao.class,TagDao.class);// 后边填写所有的 Dao 类
        //记得要修改 DaoMaster 中的数据库版本号
//        switch (oldVersion) {
//            case 6:
//                //创建新表，注意createTable()是静态方法
//                /*********************为了与上面几篇保持连贯，所以这几个生成表，我就没有删除，只是注掉了*******************************/
//                //infosDao.createTable(db, true);
//                //infoTypeDao.createTable(db,true);
//                //AreasDao.createTable(db,true);
//                //这里我们为我们的users表，添加一个地址的字段
//                // 加入新字段
//                db.execSQL("ALTER TABLE 'Article' ADD COLUMN 'COVER_SRC' TEXT;");
//                // TODO
//                break;
//        }
    }
}
