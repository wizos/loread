package me.wizos.loread.utils;

import android.content.Context;
import android.os.Environment;

import com.socks.library.KLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import me.wizos.loread.App;

/**
 * Created by Wizos on 2016/3/19.
 */
public class UFile {

    protected static Context mContext;

    public static void setContext(Context context){
        mContext = context;
    }
    protected Context getContext(){
        return mContext;
    }


    //判断外部存储(SD卡)是否可以读写
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {  // 判断SD卡是否插入
            return true;
        }
        return false;
    }
    //判断外部存储是否至少可以读
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static String getCacheFileRelativePath(String fileNameInMD5){
        return fileNameInMD5;
    }


    /**
     * 递归删除应用下的缓存
     * @param dir 需要删除的文件或者文件目录
     * @return 文件是否删除
     */
    public static boolean deleteHtmlDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean isSuccess = deleteHtmlDir(new File(dir, children[i]));
                if (!isSuccess) {
                    return false;
                }
            }
        }
//        KLog.i("删除：" + dir.delete());
        return dir.delete();
    }

    public static void deleteHtmlDirList(ArrayList<String> fileNameInMD5List) {
        for (String fileNameInMD5:fileNameInMD5List){
            File folder =  new File( App.cacheRelativePath + fileNameInMD5 ) ;
            deleteHtmlDir( folder );
        }
    }


    public static void saveHtml( String fileNameInMD5 ,String fileContent){
        if( !isExternalStorageWritable() ){return;}
//        添加文件写入和创建的权限
//        String aaa = Environment.getExternalStorageDirectory() + File.separator + "aaa.txt";
//        Environment.getExternalStorageDirectory() 获得sd卡根目录   File.separator 代表 / 分隔符
        String filePathName =  App.cacheRelativePath + fileNameInMD5 + File.separator + fileNameInMD5 + ".html";
        String folderPathName =  App.cacheRelativePath + fileNameInMD5;
        File file = new File( filePathName );
        File folder =  new File( folderPathName );

        try {
            if(!folder.exists())
                folder.mkdirs();
//            System.out.println("【】" + file.toString() + "--"+ folder.toString());
            FileWriter fileWriter = new FileWriter(file,false); //在 (file,false) 后者表示在 fileWriter 对文件再次写入时，是否会在该文件的结尾续写，true 是续写，false 是覆盖。
            fileWriter.write( fileContent );
            fileWriter.flush();  // 刷新该流中的缓冲。将缓冲区中的字符数据保存到目的文件中去。
            fileWriter.close();  // 关闭此流。在关闭前会先刷新此流的缓冲区。在关闭后，再写入或者刷新的话，会抛IOException异常。
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readHtml( String fileNameInMD5 ){
        if( !isExternalStorageWritable() ){return null;}
        String filePathName =   App.cacheRelativePath + fileNameInMD5 + File.separator + fileNameInMD5 + ".html";
        String folderPathName =  App.cacheRelativePath + fileNameInMD5 ;
        File file = new File( filePathName );
        File folder = new File( folderPathName );
        String fileContent ="" , temp = "";
        System.out.println("【】" + file.toString() + "--"+ folder.toString());
        try {
            if(!folder.exists()){
                return null;
            }

            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader( fileReader );//一行一行读取 。在电子书程序上经常会用到。
            while(( temp = br.readLine())!= null){
                fileContent += temp+"\r\n";
            }
            fileReader.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileContent;
    }


    /**
     * 此方法在使用完InputStream后会关闭它。
     *
     * @param is
     * @param filePath
     * @throws IOException
     */
    public static void saveFromStream( InputStream is, String filePath) throws IOException {
        System.out.println("【 saveFromStream 1】" + filePath);
        File file = new File(filePath);
        BufferedInputStream bis = new BufferedInputStream(is);
        FileOutputStream os = null;
        BufferedOutputStream bos = null;  // BufferedReader buffered = null.，故此时之关闭了in
//        System.out.println("【 saveFromStream 4】");
        // TODO 保存文件，会遇到存储空间满的问题，如果批量保存文件，会一直尝试保存
        try {
            if (file.exists()) {  //  如果目标文件已存在，不保存
                return;
            }
            File dir = file.getParentFile();
            dir.mkdirs();
//            System.out.println("【saveFromStream 8】" );

            byte[] buff = new byte[8192];
            int size = 0;
            os = new FileOutputStream( file );
            bos = new BufferedOutputStream(os);
            while ((size = bis.read(buff)) != -1) {  // NullPointerException: Attempt to invoke virtual method 'okio.Segment okio.Segment.push(okio.Segment)' on a null object reference
                bos.write(buff, 0, size);
            }
            bos.flush();
        } finally {
            if (is != null) {
                is.close();
            }
            if (bis != null) {
                bis.close();
            }
            if (os != null) {
                os.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
    }

//    public static ImageType checkImageType(String filePath) {
//        try {
//            byte[] b = getBytes(new FileInputStream(filePath), 10);
//            byte b0 = b[0];
//            byte b1 = b[1];
//            byte b2 = b[2];
//            byte b3 = b[3];
//            byte b6 = b[6];
//            byte b7 = b[7];
//            byte b8 = b[8];
//            byte b9 = b[9];
//            if (b0 == (byte) 'G' && b1 == (byte) 'I' && b2 == (byte) 'F') {
//                return ImageType.GIF;
//            } else if (b1 == (byte) 'P' && b2 == (byte) 'N' && b3 == (byte) 'G') {
//                return ImageType.PNG;
//            } else if (b6 == (byte) 'J' && b7 == (byte) 'F' && b8 == (byte) 'I' && b9 == (byte) 'F') {
//                return ImageType.JPG;
//            } else if (b6 == (byte) 'E' && b7 == (byte) 'x' && b8 == (byte) 'i' && b9 == (byte) 'f') {
//                return ImageType.JPG;
//            } else {
//                return ImageType.UNKNOW;
//            }
//        } catch (Exception e) {
//            return ImageType.UNKNOW;
//        }
//    }
//    public enum ImageType {
//        GIF, JPG, PNG, UNKNOW
//    }

    public static boolean isFileExists(String filePath){
        File file = new File(filePath);
        if (file.exists()) {  //  如果目标文件已存在，不保存
            return true;
        }
        return false;
    }


    public static String getImageType(InputStream in) {
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
                return "";
            }
        } catch (Exception e) {
            return "";
        }
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
        System.out.println( "【 修正后的url 】" + url );
        return url;
    }

    public static String getFileExtByUrl(String url){
        int typeIndex = url.lastIndexOf(".");
        int extLength = url.length() - typeIndex;
        String fileExt ="";
        if(extLength<6){
            fileExt = url.substring( typeIndex ,url.length());
        }else {
            System.out.println( "【获取 FileExtByUrl 】失败" + extLength );
            fileExt = url.substring( typeIndex ,url.length());
            if(fileExt.contains(".jpg")){
                fileExt = ".jpg";
            }else if(fileExt.contains(".jpeg")){
                fileExt = ".jpeg";
            }else if(fileExt.contains(".png")){
                fileExt = ".png";
            }else if(fileExt.contains(".gif")){
                fileExt = ".gif";
            }
        }
        System.out.println( "【修正正文内的SRC】的格式" + fileExt );
        return fileExt;
    }



    private static byte[] getBytes(InputStream in,int nums){
        byte b[]= new byte[nums];     //创建合适文件大小的数组
        try {
//            in.skip(9);//跳过前9个字节
            int read  = in.read(b,0,nums); //读取文件中的内容到b[]数组,//读取 nums 个字节赋值给 b
//            in.close();
            System.out.println("read ="+ read );
            System.out.println("b ="+ b[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return b;
    }

//    java 根据输入流检查图片格式
//    /**
//     * File extensions.
//     */
//    private static final String[] FILE_EXTS = {"JPG", "PNG", "GIF"};
//    /**
//     * Magic bytes in a file with above extension.
//     */
//    private static final byte[][] FILE_MAGS = new byte[][] {
//            new byte[] {(byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0}, //JPG
//            new byte[] {(byte)0x89, (byte)0x50, (byte)0x4E, (byte)0x47}, //PNG
//            new byte[] {(byte)0x47, (byte)0x49, (byte)0x46, (byte)0x38}  //GIF
//    };
//    /**
//     * Get file format from file name.
//     * @param fileName file name
//     * @return file format, null if unsupported.
//     */
//    public static String getFileFormat(String fileName) {
//        int dp = fileName.lastIndexOf(".");
//        if (dp == -1) return null;
//        String ext = fileName.substring(dp + 1).toUpperCase();
//        if (ext.equals("JPEG")) ext = "JPG"; //JPEG is JPG
//        if (ArrayUtils.indexOf(FILE_EXTS, ext) == -1) return null;
//        return ext;
//    }
//    /**
//     * Get file format by contents.
//     * @param contents file contents
//     * @return file format, null if unsupported.
//     */
//    public static String getFileFormat(byte[] contents) {
//        for (int i = 0; i < FILE_MAGS.length; i++) {
//            byte[] mag = FILE_MAGS[i];
//            if (contents.length >= mag.length) {
//                if (Arrays.equals(Arrays.copyOf(contents, mag.length), mag)) {
//                    return FILE_EXTS[i];
//                }
//            }
//        }
//        return null;
//    }




//    /**
//     *
//     *TODO：保存文件
//     *Author：Andy.Liu
//     *Create Time：2012-7-10 上午08:42:40
//     *TAG：@param str 文件的内容
//     *TAG：@param filePath 保存路径
//     *Return：void
//     */
//    public static void saveFilesss(String str,String filePath){
//        FileOutputStream fos = null;
//        try {
//            File file = new File(filePath);
//            if(!file.exists())
//                file.mkdirs();
//            fos = new FileOutputStream(file);
//            fos.write(str.getBytes());
//            fos.flush();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }catch(IOException e){
//            e.printStackTrace();
//        }finally{
//            try {
//                if(null!=fos)
//                    fos.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    FileOutputStream是用来输出原始字节流的，如图像数据。要输出字符流，则使用FileWriter。
//    如果你对于设计模式也熟悉的话，FileWriter是典型的装饰者模式。我已经写过教程来解释装饰者模式了，因为它对于很多设计来说都很重要。
//    FileOutputStream的一个应用就是将文件转换成字节数组。
//    FileWriter 用来写入字符文件的便捷类，此类的构造方法假定默认字符编码和默认字节缓冲区大小都是可接受的

//
//    /**
//     *
//     *TODO：读取文件
//     *Author：Andy.Liu
//     *Create Time：2012-7-10 上午08:48:40
//     *TAG：@param filePath
//     *TAG：@return
//     *Return：String
//     */
//    public static String readFile(String filePath){
//        FileInputStream fis = null;
//        byte[] mByte = new byte[512];
//        try {
//            fis = new FileInputStream(new File(filePath));
//            fis.read(mByte);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally{
//            try {
//                if(null!=fis)
//                    fis.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return new String(mByte).toString();
//    }



//    mkdir()
//    只能在已经存在的目录中创建创建文件夹。
//    mkdirs()
//    可以在不存在的目录中创建文件夹。诸如：a\\b,既可以创建多级目录。
//
//    mkdirs
//    public boolean mkdirs()
//
//    创建一个目录，它的路径名由当前 File 对象指定，包括任一必须的父路径。
//
//    返回值：
//    如果该目录(或多级目录)能被创建则为 true；否则为 false。
//
//    mkdir
//    public boolean mkdir()
//
//    创建一个目录，它的路径名由当前 File 对象指定。
//
//    返回值：
//    如果该目录能被创建则为 true；否则为 false。
}
