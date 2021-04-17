package me.wizos.loread.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.elvishew.xlog.XLog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vdurmont.emoji.EmojiParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.CoreDB;

/**
 * @author Wizos on 2016/3/19.
 */
public class FileUtils {
    //判断外部存储(SD卡)是否可以读写
    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    //判断外部存储是否至少可以读
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }


    public static void deleteHtmlDirList(ArrayList<String> fileNameInMD5List) {
        String externalCacheDir = App.i().getUserFilesDir() + "/cache/";
        for (String fileNameInMD5 : fileNameInMD5List) {
            // XLog.e("删除文件：" +  externalCacheDir + fileNameInMD5 );
            deleteHtmlDir(new File(externalCacheDir + fileNameInMD5));
        }
    }


    /**
     * 递归删除应用下的缓存
     *
     * @param dir 需要删除的文件或者文件目录
     * @return 文件是否删除
     */
    public static boolean deleteHtmlDir(File dir) {
        if (dir.isDirectory()) {
//            XLog.i( dir + "是文件夹");
            File[] files = dir.listFiles();
            for (File file : files) {
                deleteHtmlDir(file);
            }
            return dir.delete(); // 删除目录
        } else {
//            XLog.i( dir + "只是文件");
            return dir.delete(); // 删除文件
        }
    }


    public static boolean moveFile(String srcFileName, String destFileName) {
        File srcFile = new File(srcFileName);
        //XLog.i("文件是否存在：" + srcFile.exists() + destFileName);
        if (!srcFile.exists() || !srcFile.isFile()) {
            return false;
        }

        File destFile = new File(destFileName);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        return srcFile.renameTo(destFile);
    }

    /**
     * 移动目录
     *
     * @param srcDirName  源目录完整路径
     * @param destDirName 目的目录完整路径
     * @return 目录移动成功返回true，否则返回false
     */
    public static boolean moveDir(String srcDirName, String destDirName) {
        //XLog.i("移动文件夹a");
        File srcDir = new File(srcDirName);
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            return false;
        }

        File destDir = new File(destDirName);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        //XLog.i("移动文件夹b");
        /**
         * 如果是文件则移动，否则递归移动文件夹。删除最终的空源文件夹
         * 注意移动文件夹时保持文件夹的树状结构
         */
        File[] sourceFiles = srcDir.listFiles();
        for (File sourceFile : sourceFiles) {
            if (sourceFile.isFile()) {
                moveFile(sourceFile.getAbsolutePath(), destDir.getAbsolutePath() + File.separator + sourceFile.getName());
            } else if (sourceFile.isDirectory()) {
                moveDir(sourceFile.getAbsolutePath(), destDir.getAbsolutePath() + File.separator + sourceFile.getName());
            }
        }
        return srcDir.delete();
    }

    public static void restore() {
        Gson gson = new Gson();
        String content;
        Article tmp;

        content = readFile(App.i().getUserFilesDir() + "/config/articles-backup.json");
        List<Article> articles = gson.fromJson(content, new TypeToken<List<Article>>() {}.getType());
        if (articles == null) {
            return;
        }
        XLog.e("文豪A：" + articles.size() );
        List<Article> unreadArticles = new ArrayList<>(articles.size());
        for (Article article : articles) {
            tmp = CoreDB.i().articleDao().getById(App.i().getUser().getId(), article.getId());
            if (tmp == null) {
                continue;
            }
            tmp.setReadStatus(article.getReadStatus());
            tmp.setSaveStatus(article.getSaveStatus());
            unreadArticles.add(tmp);
        }
        CoreDB.i().articleDao().update(unreadArticles);
    }

    public static void backup() {
        Gson gson = new Gson();
        String content;
        List<Article> articles = CoreDB.i().articleDao().getBackup(App.i().getUser().getId());
        List<Article> backups = new ArrayList<>(articles.size());
        Article tmp;
        for (Article article : articles) {
            tmp = new Article();
            tmp.setId(article.getId());
            tmp.setReadStatus(article.getReadStatus());
            tmp.setSaveStatus(article.getSaveStatus());
            backups.add(tmp);
        }
        content = gson.toJson(backups);
        save(App.i().getUserFilesDir() + "/config/articles-backup.json", content);
    }


    /**
     * 处理文件名中的特殊字符和表情，用于保存为文件
     *
     * @param fileName 文件名
     * @return 处理后的文件名
     */
    public static String getSaveableName(String fileName) {
        // 因为有些title会用 html中的转义。所以这里要改过来
        fileName = Html.fromHtml(fileName).toString();
        fileName = EmojiParser.removeAllEmojis(fileName);

        fileName = SymbolUtils.filterUnsavedSymbol(fileName).trim();
        if (StringUtils.isEmpty(fileName)) {
            fileName = TimeUtils.format(System.currentTimeMillis(),"yyyyMMddHHmmss");
        } else if (fileName.length() <= 2) {
            fileName = fileName + TimeUtils.format(System.currentTimeMillis(),"_yyyyMMddHHmmss");
        }
        return fileName.trim();
    }

    public static boolean save(String filePath, String fileContent) {
        return save(new File(filePath), fileContent);
    }

    // public static boolean save(String filePath, String fileContent, boolean append) {
    //     if (!isExternalStorageWritable()) {
    //         return false;
    //     }
    //     File file = new File(filePath);
    //
    //     try {
    //         if (file.exists()) {
    //             if (!append) {
    //                 return false;
    //             }
    //         } else {
    //             File folder = file.getParentFile();
    //             if (!folder.exists()) {
    //                 folder.mkdirs();
    //             }
    //         }
    //
    //         // XLog.d("【】" + file.toString() + "--"+ folder.toString());
    //         FileWriter fileWriter = new FileWriter(file, append); //在 (file,false) 后者表示在 fileWriter 对文件再次写入时，是否会在该文件的结尾续写，true 是续写，false 是覆盖。
    //         fileWriter.write(fileContent);
    //         fileWriter.flush();  // 刷新该流中的缓冲。将缓冲区中的字符数据保存到目的文件中去。
    //         fileWriter.close();  // 关闭此流。在关闭前会先刷新此流的缓冲区。在关闭后，再写入或者刷新的话，会抛IOException异常。
    //         return true;
    //     } catch (IOException e) {
    //         Tool.printCallStack(e);
    //     }
    //     return false;
    // }


    /**
     * 保存文本到公共目录(txt文本,其他文件同理)
     * 29 以下，需要提前申请文件读写权限
     * 29及29以上的，不需要权限
     * 保存的文件在 Download 目录下
     *
     * @param mContext 上下文
     * @param content  文本内容
     * @return 文件的 uri
     */
    public static Uri saveTextFile(Context mContext, String name, String content) {
        if (StringUtils.isEmpty(content))
            return null;
        if (Build.VERSION.SDK_INT < 29) {
            if (!isGranted(mContext)) {
                XLog.e("FileSaveUtil：save to file need storage permission");
                return null;
            }
            File destFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), name);
            if (!save(destFile, content))
                return null;
            Uri uri = null;
            if (destFile.exists())
                uri = Uri.parse("file://" + destFile.getAbsolutePath());
            return uri;
        } else {//android Q
            Uri contentUri;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
            } else
                contentUri = MediaStore.Downloads.INTERNAL_CONTENT_URI;
            //创建ContentValues对象，准备插入数据
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Downloads.MIME_TYPE, "text/plain");//文件格式
            contentValues.put(MediaStore.Downloads.DATE_TAKEN, System.currentTimeMillis());
            contentValues.put(MediaStore.Downloads.DISPLAY_NAME, name);//文件名字
            Uri fileUri = mContext.getContentResolver().insert(contentUri, contentValues);
            if (fileUri == null)
                return null;
            OutputStream outputStream = null;
            try {
                outputStream = mContext.getContentResolver().openOutputStream(fileUri);
                if (outputStream != null) {
                    outputStream.write(content.getBytes());
                    outputStream.flush();
                }
                return fileUri;
            } catch (Exception e) {
                e.printStackTrace();
                mContext.getContentResolver().delete(fileUri, null, null);  // 失败的时候，删除此 uri 记录
                return null;
            } finally {
                try {
                    if (outputStream != null)
                        outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    // ignore
                }
            }
        }
    }


    public static boolean save3(File file, String fileContent){
        if (!isExternalStorageWritable()) {
            return false;
        }
        File folder = file.getParentFile();
        try {
            if (folder != null && !folder.exists()) {
                boolean success = folder.mkdirs();
                XLog.i("创建目录结果：" + success);
            }

            XLog.i("保存文件路径：" + file.toString() + "，创建文件结果：" + file.createNewFile() );
            FileWriter fileWriter = new FileWriter(file, false); //在 (file,false) 后者表示在 fileWriter 对文件再次写入时，是否会在该文件的结尾续写，true 是续写，false 是覆盖。
            fileWriter.write(fileContent);
            fileWriter.flush();  // 刷新该流中的缓冲。将缓冲区中的字符数据保存到目的文件中去。
            fileWriter.close();  // 关闭此流。在关闭前会先刷新此流的缓冲区。在关闭后，再写入或者刷新的话，会抛IOException异常。
            return true;
        } catch (IOException e) {
            XLog.e("保存错误：" + e.getLocalizedMessage());
            Tool.printCallStack(e);
            return false;
        }
    }

    public static boolean save(File file, String content) {
        if (!createFile(file, true)) {
            XLog.e("FileSaveUtil： create or delete file <$file> failed.");
            return false;
        }
        FileOutputStream outStream = null;
        boolean ret;
        try {
            outStream = new FileOutputStream(file);
            outStream.write(content.getBytes());
            outStream.flush();
            ret = true;
        } catch (Exception e) {
            XLog.e("错误：" + e.getLocalizedMessage());
            Tool.printCallStack(e);
            e.printStackTrace();
            ret = false;
        } finally {
            try {
                if (outStream != null)
                    outStream.close();
            } catch (IOException e) {
                XLog.e("错误：" + e.getLocalizedMessage());
                Tool.printCallStack(e);
                e.printStackTrace();
                // ignore
            }
        }
        return ret;
    }

    private static boolean createFile(File file, boolean isDeleteOldFile) {
        if (file == null) return false;
        if (file.exists()) {
            if (isDeleteOldFile) {
                if (!file.delete()) return false;
            } else
                return file.isFile();
        }
        if (!createDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            XLog.e("错误");
            Tool.printCallStack(e);
            e.printStackTrace();
            return false;
        }
    }

    private static boolean createDir(File file) {
        if (file == null) return false;
        if (file.exists())
            return file.isDirectory();
        else
            return file.mkdirs();
    }

    private static boolean isGranted(Context context) {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    public static String readFile(String path) {
        return readFile(new File(path));
    }
    // public static String readFile(File file) {
    //     StringBuilder fileContent = new StringBuilder();
    //     String temp = "";
    //     if (!file.exists()) {
    //         return temp;
    //     }
    //     try {
    //         // FileReader fileReader = new FileReader(file);
    //         BufferedReader br = new BufferedReader(new FileReader(file));//一行一行读取 。在电子书程序上经常会用到。
    //         while ((temp = br.readLine()) != null) {
    //             fileContent.append(temp); // +"\r\n"
    //         }
    //         // fileReader.close();
    //         br.close();
    //     } catch (IOException e){
    //         Tool.printCallStack(e);
    //     }
    //     return fileContent.toString();
    // }
    public static String readFile(File file) {
        String temp = "";
        if (!file.exists()) {
            return temp;
        }
        try {
            FileInputStream in = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            return new String(out.toByteArray());
        }catch (IOException e){
            Tool.printCallStack(e);
        }
        return temp;
    }

    // public static String readText(InputStream inputStream, String charset){
    //     try {
    //         InputStreamReader inputStreamReader = new InputStreamReader(inputStream,charset);
    //         BufferedReader buffer = new BufferedReader(inputStreamReader);
    //         String s=null;
    //         StringBuilder builder = new StringBuilder();
    //         while ((s = buffer.readLine())!=null){
    //             builder.append(s);
    //         }
    //         buffer.close();
    //         inputStreamReader.close();
    //         return builder.toString();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    //     return null;
    // }
    public static byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, count);
        }
        return out.toByteArray();
    }
    public static String read(String path) throws FileNotFoundException {
        return read(new File(path));
    }
    public static String read(File file) throws FileNotFoundException {
        return read(new FileInputStream(file));
    }

    /**
     * https://www.cnblogs.com/lzl-sml/p/3497949.html
     * java 默认String在内存中的编码是ucs-2编码。当你要把byte[]转换成String时，这里就涉及到了
     * 编码转换的问题，假如你不指定byte[]里面的编码，那可能在转换后会有问题。假如你没有指定
     * byte[]里面所用到的编码，转换就会根据当前系统环境给你指定一个编码，在android系统中就会默认byte[]中的数据是
     * 用utf8编码的。在android中如果byte[]中的数据不是utf8编码，那么使用默认方式转换到String时，这时就造成转换后
     * 的数据出现了问题，再将出问题的String转换回byte[]时，同样会有问题。
     * 所以在获取一些不知道编码的二进制数据的时候，不要转换成String，在android中使用byte[]，或者InputStream
     * 来存储，传输，处理二进制数据就行。
     * @param in
     * @return
     */
    public static String read(InputStream in){
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            return new String(out.toByteArray());
        }catch (IOException e){
            Tool.printCallStack(e);
        }
        return "";
    }

    public static String readCacheFilePath(String articleIdInMD5, String originalUrl) {
        // 由于有些图片链接并不标准，导致不同的 url 获取的 FileNameExt 是一致的，所以采用originalUrl的hashCode作为图片名称来保证唯一
        String filePath;
        String imgId = String.valueOf(originalUrl.hashCode());

        // 改为 id 存储图片，在保存时才改为图片src中的名称
        filePath = App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/compressed/" + imgId;
        if (new File(filePath).exists()) {
            return filePath;
        }
        

        filePath = App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original/" + imgId;
        if (new File(filePath).exists()) {
            return filePath;
        }

        // 支持使用 img url 的 path 为 imgId
        imgId = Uri.parse(originalUrl).getPath();
        if(!StringUtils.isEmpty(imgId)){
            imgId = String.valueOf(imgId.hashCode());
            filePath = App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/compressed/" + imgId;
            if (new File(filePath).exists()) {
                return filePath;
            }
            filePath = App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original/" + imgId;
            if (new File(filePath).exists()) {
                return filePath;
            }
        }

        imgId = String.valueOf(UriUtils.getAllPath(originalUrl).hashCode());
        filePath = App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/compressed/" + imgId;
        if (new File(filePath).exists()) {
            return filePath;
        }
        filePath = App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original/" + imgId;
        if (new File(filePath).exists()) {
            return filePath;
        }

        String fileNameExt = UriUtils.guessFileNameExt(originalUrl);
        // 推测可能是svg格式的，该类文件必须有后缀名才能在webView中显示出来
        filePath = App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original/" + fileNameExt + ".svg";
        if (new File(filePath).exists()) {
            return filePath;
        }

        // 推测该图片在保存时，由于src有问题，导致获取的文件名有重复时自动加上 hashCode 的机制
        filePath = App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original/" + originalUrl.hashCode() + "_" + fileNameExt;
        if (new File(filePath).exists()) {
            return filePath;
        }

        filePath = App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/compressed/" + fileNameExt;
        if (new File(filePath).exists()) {
            return filePath;
        }

        filePath = App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original/" + fileNameExt;
        if (new File(filePath).exists()) {
            return filePath;
        }
        return null;
    }

    public static File readOriginalFile(String articleIdInMD5, String originalUrl) {
        // 由于有些图片链接并不标准，导致不同的 url 获取的 FileNameExt 是一致的，所以采用originalUrl的hashCode作为图片名称来保证唯一
        String imgId = String.valueOf(originalUrl.hashCode());
        File file;

        file = new File(App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original/" + imgId);
        if (file.exists()) return file;

        // 支持使用 img url 的 path 为 imgId
        imgId = Uri.parse(originalUrl).getPath();
        if(!StringUtils.isEmpty(imgId)){
            imgId = String.valueOf(imgId.hashCode());
            file = new File(App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original/" + imgId);
            if (file.exists()) return file;
        }

        // 未来将要去掉
        imgId = String.valueOf(UriUtils.getAllPath(originalUrl).hashCode());
        file = new File(App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original/" + imgId);
        if (file.exists()) return file;

        String fileNameExt = UriUtils.guessFileNameExt(originalUrl);
        // 推测可能是svg格式的，该类文件必须有后缀名才能在webView中显示出来
        file = new File(App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original/" + fileNameExt + ".svg");
        if (file.exists()) return file;

        file = new File(App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original/" + fileNameExt);
        if (file.exists()) return file;
        return null;
    }

    public static String readCacheFilePath2(String articleIdInMD5, String originalUrl) {
        // 为了避免我自己来获取 FileNameExt 时，由于得到的结果是重复的而导致图片也获取到一致的。所以采用 base64 的方式加密 originalUrl，来保证唯一
        String fileNameExt, filePath, imgId;
        fileNameExt = UriUtils.guessFileNameExt(originalUrl);
        imgId = String.valueOf(originalUrl.hashCode());

        File theFile;
        // 改为 id 存储图片，在保存时才改为图片src中的名称
        filePath = App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/compressed/" + imgId;

        theFile = new File(filePath);
        if (theFile.exists()) {
            Uri data;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                data = FileProvider.getUriForFile(App.i(), "me.wizos.loread", theFile);
            } else {
                data = Uri.fromFile(theFile);
            }
            App.i().grantUriPermission("me.wizos.loread", data, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            return data.toString();
        }

        filePath = App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original/" + imgId;

        theFile = new File(filePath);
        if (theFile.exists()) {
            Uri data;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                data = FileProvider.getUriForFile(App.i(), "me.wizos.loread", theFile);
            } else {
                data = Uri.fromFile(theFile);
            }
            // 给目标应用一个临时授权
            App.i().grantUriPermission("me.wizos.loread", data, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            return data.toString();
        }

        // 推测可能是svg格式的，该类文件必须有后缀名才能在webView中显示出来
        filePath = App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original/" + fileNameExt + ".svg";
        if (new File(filePath).exists()) {
            return filePath;
        }

        // 推测该图片在保存时，由于src有问题，导致获取的文件名有重复时自动加上 hashCode 的机制
        filePath = App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original/" + originalUrl.hashCode() + "_" + fileNameExt;
        //XLog.i("文件地址：" + filePath );
        //XLog.i("文件网址：" + originalUrl );
        if (new File(filePath).exists()) {
            return filePath;
        }

        filePath = App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/compressed/" + fileNameExt;
        if (new File(filePath).exists()) {
            return filePath;
        }

        filePath = App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original/" + fileNameExt;
        theFile = new File(filePath);
        if (theFile.exists()) {
            Uri data;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                data = FileProvider.getUriForFile(App.i(), "me.wizos.loread", theFile);
            } else {
                data = Uri.fromFile(theFile);
            }
            // 给目标应用一个临时授权
            App.i().grantUriPermission("me.wizos.loread", data, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            return data.toString();
        }

        //XLog.e("ImageBridge", "要读取的url：" + originalUrl + "    文件位置" + filePath);
        return null;
    }

    //文件拷贝
    //要复制的目录下的所有非子目录(文件夹)文件拷贝
    public static int copyFile(String fromFile, String toFile) {
        try {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();
            XLog.e("图片复制完成" + fosfrom.available() + new File(fromFile).exists());
            return 0;
        } catch (Exception ex) {
            XLog.e("报错", ex);
            Tool.printCallStack(ex);
            return -1;
        }
    }

    private static boolean copyFile(File srcFile, File destFile) {
        if (!isExternalStorageWritable()) {
            return false;
        }

        if ((srcFile == null) || (destFile == null) || !srcFile.exists()) {
            return false;
        }


        if (destFile.exists()) {
            return false;
        } else if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }

        FileChannel srcChannel;
        FileChannel dstChannel;

        try {
            srcChannel = new FileInputStream(srcFile).getChannel();
            dstChannel = new FileOutputStream(destFile).getChannel();
            srcChannel.transferTo(0, srcChannel.size(), dstChannel);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try {
            srcChannel.close();
            dstChannel.close();
        } catch (IOException e) {
            Tool.printCallStack(e);
        }
        return true;
    }


    public static boolean copyFileToPictures(File srcFile) {
        File loreadDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + "知微");
        if (!loreadDir.exists()) {
            loreadDir.mkdirs();
        }
        String fileName = srcFile.getName();
        String suffix = "";
        if (fileName.contains(".")) {
            suffix = fileName.substring(fileName.lastIndexOf("."));
        }
        if (suffix.length() > 5) {
            suffix = getImageSuffix(srcFile);
        }
        File destFile = new File(loreadDir.getAbsolutePath() + File.separator + TimeUtils.format(System.currentTimeMillis(),"yyyyMMdd_HHmmss") + suffix);
        return copyFile(srcFile, destFile);
    }



    private static String getImageSuffix(File imageFile) {
        try {
            return getImageSuffix(new FileInputStream(imageFile));
        } catch (FileNotFoundException e) {
            return ".jpg";
        }
    }

    private static String getImageSuffix(InputStream in) {
        try {
            // in.skip(9);//跳过前9个字节
            // byte[] b = getBytes(in, 10);
            byte[] b = new byte[10];
            in.read(b, 0, 10); //读取文件中的内容到b[]数组,//读取 nums 个字节赋值给 b
            in.close();
            byte b0 = b[0];
            byte b1 = b[1];
            byte b2 = b[2];
            byte b3 = b[3];
            byte b6 = b[6];
            byte b7 = b[7];
            byte b8 = b[8];
            byte b9 = b[9];
            if (b0 == (byte) 'G' && b1 == (byte) 'I' && b2 == (byte) 'F') {
                return ".gif";
            } else if (b1 == (byte) 'P' && b2 == (byte) 'N' && b3 == (byte) 'G') {
                return ".png";
            } else if (b6 == (byte) 'J' && b7 == (byte) 'F' && b8 == (byte) 'I' && b9 == (byte) 'F') {
                return ".jpg";
            } else if (b6 == (byte) 'E' && b7 == (byte) 'x' && b8 == (byte) 'i' && b9 == (byte) 'f') {
                return ".jpg";
            } else {
                return ".jpg";
            }
        } catch (Exception e) {
            return ".jpg";
        }
    }
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    /**
     * 在对sd卡进行读写操作之前调用这个方法
     * Checks if the app has permission to write to device storage
     * If the app does not has permission then the user will be prompted to grant permissions
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }


    public static String getFileSizeDescription(Context context, long size) {
        if (context != null && size == -1) {
            return context.getString(R.string.unknown);
        }
        StringBuilder bytes = new StringBuilder();
        DecimalFormat format = new DecimalFormat("###.0");
        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        } else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        } else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        } else {
            if (size <= 0) {
                bytes.append("0B");
            } else {
                bytes.append((int) size).append("B");
            }
        }
        return bytes.toString();
    }

    public static String readFileFromAssets(Context context, String fileName) {
        if (null == context || null == fileName) return null;
        try {
            AssetManager am = context.getAssets();
            InputStream input = am.open(fileName);
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
            output.close();
            input.close();
            return output.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static InputStream getInputStreamFromLocalOrAssets(Context context, String path) throws IOException {
        File localFile = new File(App.i().getUserConfigPath() + path);
        if(localFile.exists()){
            return new FileInputStream(localFile);
        }else {
            return context.getAssets().open(path);
        }
    }
}
