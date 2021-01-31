package me.wizos.loread.utils;

import android.content.Context;
import android.os.Environment;

import com.elvishew.xlog.XLog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.ArticleTag;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.Tag;
import me.wizos.loread.db.User;

public class BackupUtils {
    public static void backupFile() {
        Gson gson = new Gson();
        String content;
        String uid = App.i().getUser().getId();
        List<Article> articles = CoreDB.i().articleDao().getAll(uid);

        content = gson.toJson(articles);
        FileUtils.save(App.i().getUserFilesDir() + "/backup/articles.json", content);

        List<Tag> tags = CoreDB.i().tagDao().getAll(uid);
        content = gson.toJson(tags);
        FileUtils.save(App.i().getUserFilesDir() + "/backup/tags.json", content);

        List<ArticleTag> articleTags = CoreDB.i().articleTagDao().getAll(uid);
        content = gson.toJson(articleTags);
        FileUtils.save(App.i().getUserFilesDir() + "/backup/articleTags.json", content);
    }

    public static void restoreFile() {
        Gson gson = new Gson();
        String content;
        content = FileUtils.readFile(App.i().getUserFilesDir() + "/backup/articles.json");
        List<Article> articles = gson.fromJson(content, new TypeToken<List<Article>>() {}.getType());
        if (articles == null) {
            return;
        }
        XLog.i("文章数量：" + articles.size() );
        CoreDB.i().articleDao().update(articles);

        content = FileUtils.readFile(App.i().getUserFilesDir() + "/backup/tags.json");
        List<Tag> tags = gson.fromJson(content, new TypeToken<List<Tag>>() {}.getType());
        if (tags == null) {
            return;
        }
        XLog.i("tag数量：" + tags.size() );
        CoreDB.i().tagDao().update(tags);

        content = FileUtils.readFile(App.i().getUserFilesDir() + "/backup/articleTags.json");
        List<ArticleTag> articleTags = gson.fromJson(content, new TypeToken<List<ArticleTag>>() {}.getType());
        if (articleTags == null) {
            return;
        }
        XLog.i("articleTags数量：" + articleTags.size() );
        CoreDB.i().articleTagDao().update(articleTags);
    }

    public static void restoreOPML() {
    }

    public static void backupOPML() {
        User user = App.i().getUser();
        if(user == null){
            return;
        }
        List<Feed> feeds = CoreDB.i().feedDao().getAll(user.getId());
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            XLog.w("外置存储设备不可用");
            return;
        }

        File file = new File(getExternalStorageBackupDir(), user.getId() + ".opml");
        String title = StringUtils.getString(R.string.opml_content_title, user.getUserName(), user.getSource());
        OPMLUtils.export(title, file, feeds);
    }

    public static void backupUnsubscribeFeed(User user, List<Feed> feeds) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            XLog.w("外置存储设备不可用");
            return;
        }
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!dir.isDirectory()) {
            if(!dir.mkdirs()){
                XLog.w("无法创建 Documents 文件夹");
                return;
            }
        }
        File file;
        String title;
        if(user != null){
            title = StringUtils.getString(R.string.opml_content_title, user.getUserName(), user.getSource());
            file = new File(dir.getAbsolutePath() + File.separator + user.getSource() + "_" + user.getUserName() + "_unsubscribe.opml");
        }else {
            title = StringUtils.getString(R.string.opml_content_sample_title);
            file = new File(dir.getAbsolutePath() + File.separator + "unsubscribe.opml");
        }
        OPMLUtils.export(title, file, feeds);
    }



    public static final String BACKUP = "backup";
    public static final String RESTORE = "restore";
    private static final String BACKUP_FOLDER = "backup";

    private static File getUserBackupDir(User user) {//    /sdcard/Never Forget/
        String path = Environment.getExternalStorageDirectory() + File.separator + BACKUP_FOLDER + File.separator + Contract.PACKAGE_NAME + File.separator + user.getId();
        File dir = new File(path);
        if (!dir.exists()){
            if(!dir.mkdirs()){
                XLog.w("无法创建 UserBackup 文件夹");
                return dir;
            }
        }
        return dir;
    }

    private static File getExternalStorageBackupDir() {//    /sdcard/Never Forget/
        String path = Environment.getExternalStorageDirectory() + File.separator + BACKUP_FOLDER + File.separator;
        File dir = new File(path);
        if (!dir.exists()){
            if(!dir.mkdirs()){
                XLog.w("无法创建 ExternalStorageBackup 文件夹");
                return dir;
            }
        }
        return dir;
    }

    public static void db(Context mContext, String command) {
        File dbFile = mContext.getDatabasePath(CoreDB.DATABASE_NAME);// 默认路径是 /data/data/(包名)/databases/*
        File dbFile_shm = mContext.getDatabasePath(CoreDB.DATABASE_NAME + "-shm");// 默认路径是 /data/data/(包名)/databases/*
        File dbFile_wal = mContext.getDatabasePath(CoreDB.DATABASE_NAME + "-wal");// 默认路径是 /data/data/(包名)/databases/*
        File exportDir = new File(getExternalStorageBackupDir(), Contract.PACKAGE_NAME);//    /sdcard/backup/me.wizos.loread
        if (!exportDir.exists()){
            exportDir.mkdirs();
        }
        File backup = new File(exportDir, dbFile.getName());//备份文件与原数据库文件名一致
        File backup_shm = new File(exportDir, dbFile_shm.getName());//备份文件与原数据库文件名一致
        File backup_wal = new File(exportDir, dbFile_wal.getName());//备份文件与原数据库文件名一致
        if (command.equals(BACKUP)) {
            try {
                backup.createNewFile();
                backup_shm.createNewFile();
                backup_wal.createNewFile();
                fileCopy(dbFile, backup);//数据库文件拷贝至备份文件
                fileCopy(dbFile_shm, backup_shm);//数据库文件拷贝至备份文件
                fileCopy(dbFile_wal, backup_wal);//数据库文件拷贝至备份文件
                String backup_version = TimeUtils.format(System.currentTimeMillis(),"yyyy.MM.dd_HH:mm:ss");
                //backup.setLastModified(MyTimeUtils.getTimeLong());
                XLog.d("backup ok! 备份文件名："+ backup.getName()+"\t"+backup_version);
            } catch (Exception e) {
                e.printStackTrace();
                XLog.d("backup fail! 备份文件名："+ backup.getName());
            }
        } else if (command.equals(RESTORE)) {
            try {
                fileCopy(backup, dbFile);//备份文件拷贝至数据库文件
                fileCopy(backup_shm, dbFile_shm);//备份文件拷贝至数据库文件
                fileCopy(backup_wal, dbFile_wal);//备份文件拷贝至数据库文件
                String backup_version = TimeUtils.format(backup.lastModified(),"yyyy.MM.dd_HH:mm:ss");
                XLog.d("restore success! 数据库文件名："+dbFile.getName()+"\t"+backup_version);
            } catch (Exception e) {
                e.printStackTrace();
                XLog.d( "restore fail! 数据库文件名："+ dbFile.getName());
            }
        }
    }


    private static void fileCopy(File dbFile, File backup) throws IOException {
        try (FileChannel inChannel = new FileInputStream(dbFile).getChannel(); FileChannel outChannel = new FileOutputStream(backup).getChannel()) {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
