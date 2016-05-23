//package me.wizos.loread.data;
//
//import android.content.Context;
//import android.database.sqlite.SQLiteDatabase;
//
//import com.github.yuweiguocn.library.greendao.MigrationHelper;
//
//import me.wizos.loread.dao.ArticleDao;
//import me.wizos.loread.dao.DaoMaster;
//
///**
// * Created by Wizos on 2016/3/15.
// */
//public class UpdateDB extends DaoMaster.OpenHelper {
//    public UpdateDB(Context context, String name, SQLiteDatabase.CursorFactory factory) {
//        super(context, name, factory);
//    }
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        MigrationHelper.getInstance().migrate(db,ArticleDao.class);
//    }
//}
