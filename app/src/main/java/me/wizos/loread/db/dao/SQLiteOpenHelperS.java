package me.wizos.loread.db.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.yuweiguocn.library.greendao.MigrationHelper;
import com.socks.library.KLog;

import org.greenrobot.greendao.database.Database;

/**
 * Created by Wizos on 2018/3/13.
 */

public class SQLiteOpenHelperS extends DaoMaster.OpenHelper {
    public SQLiteOpenHelperS(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
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
