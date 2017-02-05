package me.wizos.loread.utils;


import android.support.v4.util.ArrayMap;

import com.socks.library.KLog;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.bean.gson.SrcPair;
import me.wizos.loread.net.API;

/**
 * 字符处理工具类
 * Created by Wizos on 2016/3/16.
 */
public class UString {
    public static String toLongID(String id) {
        id = Long.toHexString(Long.valueOf(id));
        return "tag:google.com,2005:reader/item/" + String.format("%0" + (16 - id.length()) + "d", 0) + id;
//        return "tag:google.com,2005:reader/item/" + String.format("%1$0"+( 16 -id.length())+"d",0)   ;
    }

    public static String toLongID15(String id) {
        id = Long.toHexString(Long.valueOf(id));
        return "tag:google.com,2005:reader/item/" + String.format("%0" + (14 - id.length()) + "d", 0) + id;
    }

    /**
     * 将字符串转成MD5值
     *
     * @param string 字符串
     * @return MD5 后的字符串
     */
    public static String stringToMD5(String string) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

//    public static String tagIdToName(String content){
//    }

    public static boolean isBlank(String content){
        return content==null || content.isEmpty() || content.equals("");
    }
    public static boolean isBlank(List list){return  list==null || list.isEmpty() || list.size()==0;}

    /**
     * @param oldHtml 原始 html
     * @param fileNameInMD5 MD5 加密后的文件名，用于有图片的文章内 src 的 **FileName_files 路径
     * @return 修改后的 src 下载地址和保存地址 + 修改后的 html
     */
    public static ArrayMap<Integer,SrcPair> getListOfSrcAndHtml(String oldHtml, String fileNameInMD5) {
        if (UString.isBlank(oldHtml))
            return null;
        int num = 0;
        StringBuilder tempHtml = new StringBuilder(oldHtml);
        KLog.d("getListOfSrcAndHtml");
        tempHtml = reviseHtmlNoAd(tempHtml);
        String srcLocal,srcNet,srcSavePath,imgExt,imgName,temp;
        ArrayMap<Integer,SrcPair> srcMap = new ArrayMap<>();
        srcMap.put(0, new SrcPair("","","")); // 先存一个空的，方便后面把修改后的正文放进来
        int indexB, indexA = tempHtml.indexOf("<img ", 0);
        while (indexA != -1) {
            indexA = tempHtml.indexOf(" src=\"", indexA);
            if(indexA == -1){break;}
            indexB = tempHtml.indexOf("\"", indexA + 6);
            if(indexB == -1){break;}
            srcNet = tempHtml.substring( indexA + 6, indexB );
            if ( srcNet.substring(0,3).equals("file")){  // 这段代码可以优化，没必要每次都判断相等
//                indexA = tempHtml.indexOf("<img ", indexB);
                break;
            }
            num++;
            imgExt = UString.getFileExtByUrl( srcNet );
            imgName = UString.getFileNameByUrl( srcNet )+ "_" + num; // 之所以要加 num ，是为了防止有些图片url是 /img.php?1212 等参数形式，导致得到的文件名都为 img
            KLog.d("【获取src和html】" + imgExt + num );
//            srcLocal = App.cacheAbsolutePath + fileNameInMD5  + File.separator + fileNameInMD5 + "_files" + File.separator + fileNameInMD5 + "_" + num + fileExt + API.MyFileType;
//            srcLoading  = App.cacheRelativePath  + fileNameInMD5 + File.separator + fileNameInMD5 + "_files" + File.separator + fileNameInMD5 + "_" + num + fileExt + API.MyFileType;
            srcLocal = "./" + fileNameInMD5 + "_files"  + File.separator + imgName + imgExt + API.MyFileType;
            srcSavePath  = App.cacheRelativePath  + fileNameInMD5 + "_files" + File.separator + imgName + imgExt + API.MyFileType;

            srcMap.put( num , new SrcPair( srcNet,srcSavePath ,srcLocal ));
//            temp = " src=\"" + srcLocal + "\"" + " netsrc=\"" + srcNet + "\"";
            temp = " src=\"" + srcLocal + "\"" + " netsrc=\"" + srcNet + "\"";
            tempHtml = tempHtml.replace( indexA, indexB + 1, temp ) ;
            indexB = indexA + 6 + srcLocal.length() + srcNet.length() + 10;
            indexA = tempHtml.indexOf("<img ", indexB);
        }
        if(srcMap.size()==1){return null;}
        srcMap.put(0,new SrcPair( String.valueOf(srcMap.size()-1),tempHtml.toString() ,""));

        KLog.d("【文章2】" + srcMap.size() );
        return srcMap;
    }

    public static StringBuilder reviseHtmlNoAd(StringBuilder tempHtml) {
//        KLog.d("去广告" + tempHtml);
        int indexA = tempHtml.indexOf("<center>", 0);
        int indexB = tempHtml.indexOf("</center>", indexA);
        KLog.d("去广告 = " + indexA + ":" + indexB);
        if (indexA < 0 || indexB < 0) {
            return tempHtml;
        }
        String temp = tempHtml.substring(indexA + 8, indexB);
//        KLog.d("广告" + temp);
        if (temp.contains("Ads") && temp.contains("Inoreader")) {
            tempHtml = tempHtml.replace(indexA, indexB + 9, "");
        }
//        KLog.d("修正后的文章" + tempHtml);
        return tempHtml;
    }




    /**
     * 将 cache html 中的 src 的 **MD5_files 文件夹由 MD5 加密，改为正常的 **Name_files，防止图片不能显示
     *
     * @param oldHtml
     * @param fileName
     * @return
     */
    public static String reviseHtmlForBox(String oldHtml, String fileName  ){
        StringBuilder boxHtml = new StringBuilder(oldHtml);
        String srcPath, boxSrcPath;
        int indexB = 0,indexA;
        do  {
            indexA = boxHtml.indexOf(" src=\"", indexB);
            if (indexA == -1) {
                break;
            }
            indexB = boxHtml.indexOf("\"", indexA + 6);
            if (indexB == -1) {
                break;
            }
            srcPath = boxHtml.substring(indexA + 6, indexB);
            String FileNameExt = getFileNameExtByUrl(srcPath);
            boxSrcPath = "./" + fileName + "_files" + File.separator + FileNameExt;
            boxHtml = boxHtml.replace( indexA + 6, indexB, boxSrcPath );
//            KLog.e( indexA + 6 + " - " + indexB + " - " + boxHtml.length() + " - " );
            KLog.e( "=" + boxSrcPath );
            indexB = indexA + 6 + boxSrcPath.length() + 1;
        }while (true);
        return boxHtml.toString();
    }



    public static String getFileExtByUrl(String url){
        int dotIndex = url.lastIndexOf(".");
        int extLength = url.length() - dotIndex;
        String fileExt = "";
        if(extLength<6){
            fileExt = url.substring( dotIndex ,url.length());
        }else {
//            fileExt = url.substring( typeIndex ,url.length());
            if(url.contains(".jpg")){
                fileExt = ".jpg";
            }else if(url.contains(".jpeg")){
                fileExt = ".jpeg";
            }else if(url.contains(".png")){
                fileExt = ".png";
            }else if(url.contains(".gif")){
                fileExt = ".gif";
            }else {
                fileExt = "";
            }
        }
        KLog.d( "【获取 FileExtByUrl 】" + url.substring( dotIndex ,url.length()) + extLength );
        KLog.d( "【修正正文内的SRC】的格式" + fileExt + url );
        return fileExt;
    }

    /**
     * 从 url 中获取文件名(不含后缀)
     *
     * @param url 网址
     * @return 文件名
     */
    public static String getFileNameByUrl(String url){
        if(UString.isBlank(url)){
            return null;
        }
        String fileName;
        int dotIndex = url.lastIndexOf(".");
        int separatorIndex = url.lastIndexOf("/") + 1;
//        int extLength = separatorIndex - dotIndex; extLength +
        KLog.e("【文件名】" + dotIndex + '='+ separatorIndex + '='+ '=' + url.length() );

        if( separatorIndex > dotIndex ){
            dotIndex = url.length();
        }
        fileName = url.substring(separatorIndex, dotIndex);
        fileName = handleSpecialChar(fileName );
        KLog.e("【文件名】" + fileName);
        return fileName;
    }

    /**
     * 从 url 中获取文件名(含后缀)
     * @param url 网址
     * @return 文件名
     */
    public static String getFileNameExtByUrl(String url){
        if(UString.isBlank(url)){
            return null;
        }
        String fileName;
        int separatorIndex = url.lastIndexOf("/") + 1;
        fileName = url.substring(separatorIndex, url.length());
        fileName = handleSpecialChar(fileName);
        KLog.e("【文件名与后缀名】" + fileName);
        return fileName;
    }

    /**
     * 处理文件名中的特殊字符和表情
     *
     * @param fileName 文件名
     * @return 处理后的文件名
     */
    public static String handleSpecialChar(String fileName ){
        fileName = fileName.replace("\\","");
        fileName = fileName.replace("/","");
        fileName = fileName.replace(":","");
        fileName = fileName.replace("*","");
        fileName = fileName.replace("?","");
        fileName = fileName.replace("\"","");
        fileName = fileName.replace("<","");
        fileName = fileName.replace(">","");
        fileName = fileName.replace("|","");
        fileName = fileName.replace("%","_");
        return fileName;
    }


    public static ArrayList<String[]> asList(String[] array){
        if(array==null || array.length==0){return null;}
        long xx = System.currentTimeMillis();
        ArrayList<String[]> arrayList = new ArrayList<>(array.length);
        String[] srcPair;
        for(String s:array){
            srcPair = s.split("|");
            arrayList.add( srcPair );
            KLog.d("【测试】" + s );
            KLog.d("【测试】" + srcPair[0] );
            KLog.d("【测试】" + srcPair[1] );
        }
        KLog.d("【时间】1测试" + (System.currentTimeMillis() - xx));
        return arrayList;
    }
    public static String[][] asArray(String[] array){
        if(array==null || array.length==0){return null;}
        long xx = System.currentTimeMillis();
        String[][] arrayList = new String[array.length][2];
        String[] srcPair;
        int num = array.length;
        for(int i=0 ; i<num ; i++){
            srcPair = array[i].split("|");
            arrayList[i] = srcPair;
        }
        KLog.d("【时间】2测试" + (System.currentTimeMillis() - xx));
        return arrayList;
    }




    public static ArrayList<String[]> formStringToParamList(String paramString){
        if( paramString == null || isBlank(paramString) ){
            return null;
        }
        String[] paramStringArray = paramString.split("_");
        String[] paramPair;
        ArrayList<String[]> paramList = new ArrayList<>();
        for(String string : paramStringArray){
            paramPair = string.split("#");
            if(paramPair.length!=2){continue;}
            paramList.add(paramPair);
            KLog.d("【1】" + paramPair[0] + paramPair[1]);
        }
        return paramList;
    }

    public static String formParamListToString(ArrayList<String[]> paramList){
        if( paramList==null){
            return "";
        }
        if(paramList.size()==0){
            return "";
        }
        StringBuilder sb = new StringBuilder("");
        for( String[] paramPair:paramList){
            sb.append(paramPair[0] + "#" + paramPair[1] + "_");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }


}
