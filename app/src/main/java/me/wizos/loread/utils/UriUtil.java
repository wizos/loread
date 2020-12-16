package me.wizos.loread.utils;

import android.net.Uri;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.elvishew.xlog.XLog;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class UriUtil {
    public static String getBaseUrl(String url){
        Uri uri = Uri.parse(url);
        return uri.getScheme() + "://" + uri.getHost();
    }

    // https://www.hao123.com/favicon.ico
    public static String getFaviconUrl(String url){
        Uri uri = Uri.parse(url);
        return uri.getScheme() + "://" + uri.getHost() + "/favicon.ico";
    }

    public static String guessFileNameExt(String url, String contentDisposition, String mimeType) {
        String fileNameByGuess;
        // 处理会把 epub 文件，识别为 bin 文件的 bug：https://blog.csdn.net/imesong/article/details/45568697
        if ("application/octet-stream".equals(mimeType)) {
            if (TextUtils.isEmpty(contentDisposition)) {
                // 从路径中获取
                fileNameByGuess = guessFileNameExt(url);
            } else {
                fileNameByGuess = contentDisposition.substring(contentDisposition.indexOf("filename=") + 9);
                if(fileNameByGuess.startsWith("'") && fileNameByGuess.length()> 1){
                    fileNameByGuess = fileNameByGuess.substring(1);
                }
                if(fileNameByGuess.endsWith("'") && fileNameByGuess.length()> 1){
                    fileNameByGuess = fileNameByGuess.substring(0,fileNameByGuess.length()-1);
                }
                if(fileNameByGuess.startsWith("\"") && fileNameByGuess.length()> 1){
                    fileNameByGuess = fileNameByGuess.substring(1);
                }
                if(fileNameByGuess.endsWith("\"") && fileNameByGuess.length()> 1){
                    fileNameByGuess = fileNameByGuess.substring(0,fileNameByGuess.length()-1);
                }
            }
        }else {
            fileNameByGuess = URLUtil.guessFileName(url, contentDisposition, mimeType);
        }

        XLog.i("猜测的文件名为：" + mimeType + " -- " + fileNameByGuess + " -- " + contentDisposition );
        // 处理 url 中包含乱码中文的问题
        try {
            fileNameByGuess = URLDecoder.decode(fileNameByGuess, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return fileNameByGuess;
    }

    /**
     * 从 url 中获取文件名(含后缀)
     *
     * @param url 网址
     * @return 文件名(含后缀)
     */
    public static String guessFileNameExt(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        String fileName,param = "";
        int end = url.lastIndexOf("?");
        if( end < 0 ){
            end = url.length();
        }else {
            param = SymbolUtil.filterUnsavedSymbol(url.substring(end+1));
            if( !TextUtils.isEmpty(param) ){
                param = param + "_";
            }
        }
        int start = url.lastIndexOf("/",end);
        fileName = param + url.substring(start + 1, end);

        // 减少文件名太长的情况
        if (fileName.length() > 64) {
            fileName = fileName.substring(fileName.length() - 64);
        }

        return FileUtil.getSaveableName(fileName);
    }

    public static String guessFileSuffix(String name) {
        if (TextUtils.isEmpty(name)) {
            return "";
        }
        return name.substring(name.lastIndexOf("."));
    }

    public static String guessImageSuffix(String url) {
        int typeIndex = url.lastIndexOf(".");
        String fileExt = url.substring(typeIndex);
        if (fileExt.contains(".jpg")) {
            url = url.substring(0, typeIndex) + ".jpg";
        } else if (fileExt.contains(".jpeg")) {
            url = url.substring(0, typeIndex) + ".jpeg";
        } else if (fileExt.contains(".png")) {
            url = url.substring(0, typeIndex) + ".png";
        } else if (fileExt.contains(".gif")) {
            url = url.substring(0, typeIndex) + ".gif";
        }
        XLog.d("【 修正后的url 】" + url);
        return url;
    }
}
