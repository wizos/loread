package me.wizos.loread.utils;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Patterns;
import android.webkit.URLUtil;

import com.elvishew.xlog.XLog;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import me.wizos.loread.Contract;

public class UriUtils {
    public static String getBaseUrl(String url){
        Uri uri = Uri.parse(url);
        return uri.getScheme() + "://" + uri.getHost();
    }

    // https://www.hao123.com/favicon.ico
    public static String getFaviconUrl(String url){
        if(StringUtils.isEmpty(url)){
            return null;
        }
        Uri uri = Uri.parse(url);
        return uri.getScheme() + "://" + uri.getHost() + "/favicon.ico";
    }

    /**
     * 是否是https*格式的url
     * @param url
     * @return
     */
    public static boolean isHttpOrHttpsUrl(String url) {
        return !TextUtils.isEmpty(url) && (url.startsWith(Contract.SCHEMA_HTTP) || url.startsWith(Contract.SCHEMA_HTTPS)) & Patterns.WEB_URL.matcher(url).matches();
    }

    /**
     * 是否是https*格式的url
     * @param url
     * @return
     */
    public static boolean isWebUrl(String url) {
        return !StringUtils.isEmpty(url) && Patterns.WEB_URL.matcher(url).matches();
    }


    public static String getAbsolute(String baseUrl, String url) {
        if(StringUtils.isEmpty(url)){
            return url;
        }
        try {
            return new URL(url).toString();
        }catch (MalformedURLException e){
            try {
                return new URL(new URL(baseUrl),url).toString();
            }catch (MalformedURLException e2){
                return url;
            }
        }
    }

    public static List<String> reduceSlice(String host){
        String[] slices = host.split("\\.");
        List<String> hostList = new ArrayList<>();
        for (int i = 0, size = slices.length; i+1 < size; i++) {
            host = StringUtils.join(".", Arrays.copyOfRange(slices, i, size));
            hostList.add(host);
        }
        return hostList;
    }


    private final static Pattern P_URL_PATH = Pattern.compile("https*://.*?/", Pattern.CASE_INSENSITIVE);

    public static String getAllPath(String url){
        return P_URL_PATH.matcher(url).replaceFirst("");
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
            param = SymbolUtils.filterUnsavedSymbol(url.substring(end+1));
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

        return FileUtils.getSaveableName(fileName);
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
