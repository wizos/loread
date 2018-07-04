package me.wizos.loread.utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.webkit.URLUtil;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.socks.library.KLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import me.wizos.loread.App;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.db.Article;
import me.wizos.loread.net.Api;

/**
 * @author Wizos on 2016/3/19.
 */
public class FileUtil {
    //判断外部存储(SD卡)是否可以读写
    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
    //判断外部存储是否至少可以读
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }


    public static void deleteHtmlDirList(ArrayList<String> fileNameInMD5List) {
        String externalCacheDir = App.externalFilesDir + "/cache/";
        for (String fileNameInMD5 : fileNameInMD5List) {
//            KLog.e("删除文件：" +  externalCacheDir + fileNameInMD5 );
            deleteHtmlDir(new File(externalCacheDir + fileNameInMD5));
        }
    }


    /**
     * 递归删除应用下的缓存
     * @param dir 需要删除的文件或者文件目录
     * @return 文件是否删除
     */
    public static boolean deleteHtmlDir( File dir ) {
        if ( dir.isDirectory() ) {
//            KLog.i( dir + "是文件夹");
            File[] files = dir.listFiles();
            for (File file : files) {
                deleteHtmlDir( file );
            }
            return dir.delete(); // 删除目录
        }else{
//            KLog.i( dir + "只是文件");
            return dir.delete(); // 删除文件
        }
    }



    public static boolean moveFile(String srcFileName, String destFileName) {
        File srcFile = new File(srcFileName);
        KLog.e("文件是否存在：" + srcFile.exists() + destFileName);
        if (!srcFile.exists() || !srcFile.isFile()) {
            return false;
        }

        File destFile = new File(destFileName);
//        if ( destFile.exists() && destFile.getParent()!=null){
//            destFile.getParentFile().mkdirs();
//        }

        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        return srcFile.renameTo( destFile );
    }

    /**
     * 移动目录
     * @param srcDirName     源目录完整路径
     * @param destDirName    目的目录完整路径
     * @return 目录移动成功返回true，否则返回false
     */
    public static boolean moveDir(String srcDirName, String destDirName) {
        KLog.i("移动文件夹a");
        File srcDir = new File(srcDirName);
        if (!srcDir.exists() || !srcDir.isDirectory()) {
            return false;
        }

        File destDir = new File(destDirName);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        KLog.i("移动文件夹b");
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
        Article article;

        content = readFile(App.externalFilesDir + "/config/unreadIds-backup.json");
        List<String> unreadIds = gson.fromJson(content, new TypeToken<List<String>>() {
        }.getType());
        if (unreadIds == null) {
            return;
        }
        List<Article> unreadArticles = new ArrayList<>(unreadIds.size());
        for (String id : unreadIds) {
            article = WithDB.i().getArticle(id);
            if (article == null) {
                continue;
            }
            article.setReadState(Api.ART_UNREADING);
            unreadArticles.add(article);
        }
        WithDB.i().saveArticles(unreadArticles);

//        content = readFile(App.boxRelativePath + "staredIds-backup.json");
//        List<String> staredIds = gson.fromJson(content,new TypeToken<List<String>>() {}.getType());
//        List<Article> staredArticles = new ArrayList<>(staredIds.size());
//        for (String id: staredIds) {
//            article = WithDB.i().getArticle(id);
//            if(article==null){
//                continue;
//            }
//            article.setStarState(Api.ART_STARED);
//            staredArticles.add(article);
//        }
//        WithDB.i().saveArticles(staredArticles);
    }


    public static void backup() {
        Gson gson = new Gson();
        String content;
        List<Article> articles = WithDB.i().getArtsUnreading();
        List<String> unreadIds = new ArrayList<>(articles.size());
        for (Article article : articles) {
            unreadIds.add(article.getId());
        }
        content = gson.toJson(unreadIds);
        saveStringToFile(App.externalFilesDir + "/config/unreadIds-backup.json", content);

//        articles = WithDB.i().getArtsStared();
//        List<String> staredIds = new ArrayList<>(articles.size());
//        for (Article article: articles) {
//            staredIds.add(article.getId());
//        }
//        content = gson.toJson(staredIds);
//        saveStringToFile(App.storeRelativePath + "staredIds.backup",content);
    }


    public static void saveArticle(String dir, Article article) {
        String title = StringUtil.getOptimizedNameForSave(article.getTitle());
        String filePathTitle = dir + title;
        String html = StringUtil.getHtmlForSave(article, title);

        String idInMD5 = StringUtil.str2MD5(article.getId());
        saveStringToFile(filePathTitle + ".html", html);
        moveDir(App.externalFilesDir + "/cache/" + idInMD5 + "/" + idInMD5 + "_files", filePathTitle + "_files");
    }

    public static String readFile(String filePath) {
        return readFile(new File(filePath));
    }

    public static String readFile(File file) {
        String fileContent ="" , temp = "";
        if (!file.exists()) {
            return null;
        }
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader( fileReader );//一行一行读取 。在电子书程序上经常会用到。
            while(( temp = br.readLine())!= null){
                fileContent += temp; // +"\r\n"
            }
            fileReader.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return fileContent;
    }

    public static String readCacheFilePath(String articleIdInMD5, int index, String originalUrl) {
        // 为了避免我自己来获取 FileNameExt 时，由于得到的结果是重复的而导致图片也获取到一致的。所以采用 base64 的方式加密 originalUrl，来保证唯一
        String fileNameExt, filePath;
        fileNameExt = index + "-" + StringUtil.getFileNameExtByUrl(originalUrl);
        filePath = App.externalFilesDir + "/cache/" + articleIdInMD5 + "/" + articleIdInMD5 + "_files/" + fileNameExt;
        if (new File(filePath).exists()) {
            return filePath;
        }
//        KLog.e("ImageBridge", "要读取的url：" + originalUrl + "    文件位置" + filePath);
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
            KLog.e("图片复制完成" + fosfrom.available() + new File(fromFile).exists());
            return 0;
        } catch (Exception ex) {
            KLog.e("报错", ex);
            ex.printStackTrace();
            return -1;
        }
    }


    /**
     * 根据文件路径拷贝文件
     *
     * @param srcFile  源文件
     * @param destPath 目标文件路径
     * @return boolean 成功true、失败false
     */
    public static boolean copyFile(File srcFile, String destPath) {
        return copyFile(srcFile, new File(destPath));
    }


    /**
     * 使用 FileChannels 来复制文件，效率最好，但是 Channel 经常会中途关闭，导致复制吃的图片文件不完整
     *
     * @throws IOException
     */
    public static void copy(File source, File dest) throws IOException {
        if (!isExternalStorageWritable()) {
            return;
        }
        if (!source.exists()) {
            return;
        }
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputChannel != null) {
                inputChannel.close();
            }
            if (outputChannel != null) {
                outputChannel.close();
            }
        }
    }

    public static boolean copyFile(File srcFile, File destFile) {
        boolean result = false;
        if (!isExternalStorageWritable()) {
            return false;
        }

        if ((srcFile == null) || (destFile == null) || !srcFile.exists()) {
            return false;
        }


        if (destFile.exists()) {
//            dest.delete(); // delete file
            return false;
        } else if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }

        FileChannel srcChannel = null;
        FileChannel dstChannel = null;

        try {
            srcChannel = new FileInputStream(srcFile).getChannel();
            dstChannel = new FileOutputStream(destFile).getChannel();
            srcChannel.transferTo(0, srcChannel.size(), dstChannel);
            result = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        }
        try {
            srcChannel.close();
            dstChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
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
        } else {
            suffix = getImageSuffix(srcFile);
        }
        File destFile = new File(loreadDir.getAbsolutePath() + File.separator + TimeUtil.getCurrentDate("yyyyMMdd_HHmmss") + suffix);
        return copyFile(srcFile, destFile);
    }


    public static void saveStringToFile(String filePath, String fileContent) {
        if (!isExternalStorageWritable()) {
            return;
        }
        File file = new File(filePath);
        File folder = file.getParentFile();

        try {
            if (!folder.exists()) {
                folder.mkdirs();
            }
//            KLog.d("【】" + file.toString() + "--"+ folder.toString());
            FileWriter fileWriter = new FileWriter(file, false); //在 (file,false) 后者表示在 fileWriter 对文件再次写入时，是否会在该文件的结尾续写，true 是续写，false 是覆盖。
            fileWriter.write(fileContent);
            fileWriter.flush();  // 刷新该流中的缓冲。将缓冲区中的字符数据保存到目的文件中去。
            fileWriter.close();  // 关闭此流。在关闭前会先刷新此流的缓冲区。在关闭后，再写入或者刷新的话，会抛IOException异常。
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 保存图片到手机指定目录
    public static void saveBitmap(String filePath, String fileName, byte[] bytes) {
        // 判断SD卡是否存在，并且是否具有读写权限
//        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//        }
        FileOutputStream fos = null;
        try {
            File imgDir = new File(filePath);
            if (!imgDir.exists()) {
                imgDir.mkdirs();
            }
            fileName = filePath + "/" + fileName;
            fos = new FileOutputStream(fileName);
            fos.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 此方法在使用完InputStream后会关闭它。
     *
     * @param is 输入流
     * @param filePath 文件路径
     * @throws IOException
     */
    public static boolean saveFromStream( InputStream is, String filePath) throws IOException {
        KLog.e("saveFromStream", "当前线程为：" + Thread.currentThread().getId() + "--" + Thread.currentThread().getName() + "==" + filePath);
        File file = new File(filePath);
        BufferedInputStream bis = new BufferedInputStream(is);
        FileOutputStream os = null;
        BufferedOutputStream bos = null;  // BufferedReader buffered = null.，故此时之关闭了in
        // TODO 保存文件，会遇到存储空间满的问题，如果批量保存文件，会一直尝试保存
        try {
            File dir = file.getParentFile();
            dir.mkdirs();
            byte[] buff = new byte[8192];
            /*
             * 这个取决于硬盘的扇区大小是512byte/sec，8192/512 = 16，表明写入了16扇区。write 在底层是调用scsi write （10)来写入数据的。
             这个可能协议有关，系统做了优化。所以你在写入数据的时候最好是512字节的倍数。
             */
            int size = 0;
            os = new FileOutputStream( file );
            bos = new BufferedOutputStream(os);
            while ((size = bis.read(buff)) != -1) {  // NullPointerException: Attempt to invoke virtual method 'okio.Segment okio.Segment.push(okio.Segment)' on a null object reference
                bos.write(buff, 0, size);
            }
            bos.flush();
            return true;
        } finally {
            // 关闭通道使用close()方法，调用close()方法根据操作系统的网络实现不同可能会出现阻塞，可以在任何时候多次调用close()；若出现阻塞，第一次调用close()后会一直等待；
            // 若第一次调用close()成功关闭后，之后再调用close()会立即返回，不会执行任何操作。
            if (is != null) {
                is.close();
            }
            if (bis != null) {
                bis.close();
            }
            if (bos != null) {
                bos.close();
            }
            if (os != null) {
                os.close();
            }
//            return false;
        }
    }


    /**
     * @param dir 目录名称
     * @return 带有完整的相对路径的 path
     * "/storage/emulated/0/Android/data/me.wizos.loread/files/" + Dir + "/"
     */
    public static String getRelativeDir(String dir) {
        return App.externalFilesDir + File.separator + dir + File.separator;
    }

    public static String getAbsoluteDir(String dir) {
        return "file://" + App.externalFilesDir + File.separator + dir + File.separator;
    }


    public static String getImageSuffix(File imageFile) {
        try {
            FileInputStream in = new FileInputStream(imageFile);
//            byte[] b = getBytes(in, 10);
            byte[] b = new byte[10];
            in.read(b, 0, 10); //读取文件中的内容到b[]数组,//读取 nums 个字节赋值给 b
            byte b0 = b[0];
            byte b1 = b[1];
            byte b2 = b[2];
            byte b3 = b[3];
            byte b6 = b[6];
            byte b7 = b[7];
            byte b8 = b[8];
            byte b9 = b[9];
            in.close();
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
            e.printStackTrace();
            return "";
        }
    }

    public static String getImageSuffix(InputStream in) {
        try {
            byte[] b = getBytes(in, 10);
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

    private static byte[] getBytes(InputStream in, int nums) {
        byte b[] = new byte[nums];     //创建合适文件大小的数组
        try {
//            in.skip(9);//跳过前9个字节
            int read = in.read(b, 0, nums); //读取文件中的内容到b[]数组,//读取 nums 个字节赋值给 b
//            in.close();
            KLog.e("read =" + read);
            KLog.e("b =" + b[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }



    public static String reviseSrc(String url){
        int typeIndex = url.lastIndexOf(".");
        String fileExt = url.substring(typeIndex, url.length());
        if(fileExt.contains(".jpg")){
            url =  url.substring(0,typeIndex)  + ".jpg";
        }else if(fileExt.contains(".jpeg")){
            url =  url.substring(0,typeIndex)  + ".jpeg";
        }else if(fileExt.contains(".png")){
            url =  url.substring(0,typeIndex)  + ".png";
        }else if(fileExt.contains(".gif")){
            url =  url.substring(0,typeIndex)  + ".gif";
        }
        KLog.d( "【 修正后的url 】" + url );
        return url;
    }


    public static String guessFileName(String url, String contentDisposition, String mimeType) {
        // 处理会把 epub 文件，识别为 bin 文件的 bug：https://blog.csdn.net/imesong/article/details/45568697
        String fileNameByGuess = URLUtil.guessFileName(url, contentDisposition, mimeType);
        if ("application/octet-stream".equals(mimeType)) {
            if (TextUtils.isEmpty(contentDisposition)) {
                // 从路径中获取
                fileNameByGuess = url.substring(url.lastIndexOf("/") + 1);
            } else {
                fileNameByGuess = contentDisposition.substring(contentDisposition.indexOf("filename=") + 9);
            }

//            int index = contentDisposition.indexOf("filename=");
//            if( index != -1 ){
//                fileNameByGuess = contentDisposition.substring( index + 9 );
//            }
        }
        // 处理 url 中包含乱码中文的问题
        try {
            fileNameByGuess = URLDecoder.decode(fileNameByGuess, "UTF-8");
        } catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }

        return fileNameByGuess;
    }


    public static void clear(Context context) {
        List<Article> articles = WithDB.i().getArtsAll();
        Article article = new Article();
        ArrayMap<String, Integer> idsMap = new ArrayMap<>(articles.size());
        for (int i = 0, size = articles.size(); i < size; i++) {
            article = articles.get(i);
            idsMap.put(StringUtil.str2MD5(article.getId()), 1);
        }

        File dir = new File(context.getExternalFilesDir(null) + "/cache/");
        KLog.e("数量：" + dir.listFiles().length);
        File[] files = dir.listFiles();
        File file = null;
        String idInMD5 = "";
        for (int i = 0, size = files.length; i < size; i++) {
            file = files[i];
            if (idsMap.get(file.getName()) != null) {
                idsMap.put(file.getName(), 2);
            }
        }

        for (Map.Entry<String, Integer> entry : idsMap.entrySet()) {
//            KLog.e("最终" + entry.getKey() );
            if (entry.getValue() == 2) {
                KLog.e("获得文章标题：" + entry.getKey());
                deleteHtmlDir(new File(context.getExternalFilesDir(null) + "/cache/" + entry.getKey()));
            }
        }
    }

}
