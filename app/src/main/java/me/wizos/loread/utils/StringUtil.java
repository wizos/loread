package me.wizos.loread.utils;


import android.support.v4.util.ArrayMap;
import android.text.Html;
import android.text.TextUtils;

import com.socks.library.KLog;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.wizos.loread.App;
import me.wizos.loread.bean.Article;
import me.wizos.loread.bean.Img;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.net.Api;

/**
 * 字符处理工具类
 * Created by Wizos on 2016/3/16.
 */
public class StringUtil {

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



    public static boolean isBlank(List list){return  list==null || list.isEmpty() || list.size()==0;}

    public static String getFooter() { // <script src="file:///android_asset/rich_text_view/text.js"></script>
        return "<div id=\"footer\"></div>" +
                "<script src=\"file:///android_asset/rich_text_view/zepto.min.js\"></script>" +
//                "<script src=\"file:///android_asset/rich_text_view/lazyload.js\"></script>" +
                "<script src=\"file:///android_asset/rich_text_view/javascript.js\"></script>" +
                "</body></html>";
    }
    public static String getHtmlHeader() {
        // 获取排版文件路径（支持自定义的文件）
        String typesettingCssPath = App.i().getExternalFilesDir(null) + File.separator + "config" + File.separator + "article.css";
//        if (!FileUtil.isFileExists(typesettingCssPath)) {
//            typesettingCssPath = "file:///android_asset/article.css";
//        }
        if (!new File(typesettingCssPath).exists()) {
            typesettingCssPath = "file:///android_asset/article.css";
        }

        // 获取主题文件路径
        String themeCssPath;
        if (WithSet.i().getThemeMode() == App.theme_Day) {
            themeCssPath = "file:///android_asset/article_theme_day.css";
        } else {
            themeCssPath = "file:///android_asset/article_theme_night.css";
        }

        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\" name=\"viewport\" content=\"width=device-width\">" + // , initial-scale=1.0, maximum-scale=4.0, user-scalable=1
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + typesettingCssPath + "\" />" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + themeCssPath + "\" />" +
                "</head><body>";
    }

    private static String getModHtml(Article article, String articleHtml) {
        String author = article.getAuthor();
        if (author == null ||
                author.equals("") ||
                article.getOriginTitle().toLowerCase().contains(author.toLowerCase()) ||
                author.toLowerCase().contains(article.getOriginTitle().toLowerCase())) {
            author = article.getOriginTitle();
        } else {
            author = article.getOriginTitle() + "@" + article.getAuthor();
        }


        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"./normalize.css\" />" +
//                "<link rel=\"stylesheet\" type=\"text/css\" href=\"./markdown.css\" />" +
//                "<link rel=\"stylesheet\" type=\"text/css\" href=\"./customer.css\" />" +
                "</head><body>" +
                "<article id=\"art\">" +
                "<header id=\"art_header\">" +
                "<h1 id=\"art_h1\"><a href=\"" + article.getCanonical() + "\">" + article.getTitle() + "</a></h1>" +
                "<p id=\"art_author\">" + author + "</p><p id=\"art_pubDate\">" + TimeUtil.getDateSec(article.getPublished()) + "</p>" +
                "</header>" +
                "<hr id=\"art_hr\">" +
                "<section id=\"art_section\">" + articleHtml + "</section>" +
                "</article>" +
                "</body></html>";
    }


    /**
     * 获取文章正文（并修饰）
     *
     * @param article
     * @return
     */
    public static String getArticleHtml(Article article) {
        // 获取 文章的 fileTitle
        String fileTitle, articleHtml;
        if (article.getSaveDir().equals(Api.SAVE_DIR_CACHE)) {
            fileTitle = StringUtil.stringToMD5(article.getId());
        } else {
            fileTitle = article.getTitle();
        }
        articleHtml = FileUtil.readHtml(FileUtil.getRelativeDir(article.getSaveDir()) + fileTitle + ".html");
//        if(articleHtml ==null){ // TODO: 2017/10/15 文章文件被删时候，重新去获取
//            return null;
//        }
//        if (article.getSummary().length() == 0) {
//            // TODO: 2017/2/19  加载没有正文的占位画面
//            ToastUtil.showShort("正文内容为空");
//        }

        if (article.getImgState() == null) { // 文章没有被打开过
            ArrayMap<Integer, Img> lossSrcList = StringUtil.getListOfSrcAndHtml(article.getId(), articleHtml, fileTitle);
            articleHtml = lossSrcList.get(0).getSrc();
            articleHtml = StringUtil.getModHtml(article, articleHtml);

            lossSrcList.remove(0);
            if (lossSrcList.size() != 0) {
                article.setCoverSrc(FileUtil.getAbsoluteDir(Api.SAVE_DIR_CACHE) + fileTitle + "_files" + File.separator + lossSrcList.get(1).getName());
                WithDB.i().saveImg(lossSrcList); // Note: 这里保存很慢
                article.setImgState(Api.ImgState_Downing);
                KLog.e("不为空");
            } else {
                article.setImgState(Api.ImgState_NoImg);
                KLog.i("为空");
            }
            KLog.e("获取文章正文getArticleHtml：" + article.getId() + lossSrcList);
//            article.setTitle( StringUtil.getOptimizedNameForSave(article.getTitle()) );
//            String summary = Html.fromHtml(articleHtml).toString(); // 可以去掉标签
//            article.setSummary(StringUtil.getSummary(summary));
            FileUtil.saveCacheHtml(fileTitle, articleHtml); // 保存修改好后的文章
            WithDB.i().saveArticle(article);
        }
//        KLog.e( "getArticleHtml", "测试" + articleHtml.length() );
        return articleHtml;
    }


    /**
     * @param oldHtml 原始 html
     * @param fileName MD5 加密后的文件名，用于有图片的文章内 src 的 **FileName_files 路径
     * @return 修改后的 src 下载地址和保存地址 + 修改后的 html
     *  有 2 种情况会返回 null：1，传入的文章为空；2，文章中没有图片
     */
    private static ArrayMap<Integer, Img> getListOfSrcAndHtml(String articleId, String oldHtml, String fileName) {
        ArrayMap<Integer, Img> imgMap = new ArrayMap<>();
        imgMap.put(0, new Img(0L, 0, "", oldHtml, "", 0));// 先存一个空的，方便后面把修改后的正文放进来

        if (TextUtils.isEmpty(oldHtml))
            return imgMap;

        KLog.i("getListOfSrcAndHtml修饰文章：" + articleId);
        Img imgMeta;
        // 先去广告
        StringBuilder tempHtml = new StringBuilder(delHtmlAd(oldHtml));
        int num = 0, indexB, indexA = tempHtml.indexOf("<img ", 0);
        String srcLocal, srcNet, temp, FileNameExt;// imgExt,imgName,
        while (indexA != -1) {
            indexA = tempHtml.indexOf(" src=\"", indexA);
            if(indexA == -1){break;}
            indexB = tempHtml.indexOf("\"", indexA + 6);
            if(indexB == -1){break;}
            srcNet = tempHtml.substring( indexA + 6, indexB );
            if (srcNet.substring(0, 3).equals("file")) {
                KLog.i("判断是否存在 file 开头的图片");
                break;
            }
            num++;
//            imgExt = StringUtil.getFileExtByUrl( srcNet );
//            imgName = StringUtil.getFileNameByUrl( srcNet );
            FileNameExt = getFileNameExtByUrl(srcNet) + "_" + num + Api.MyFileType;
//             KLog.i("【获取src和html】" + imgExt + num );
//            srcLocal = "./" + fileName + "_files"  + File.separator + imgName +  "_" + num  + imgExt  + Api.MyFileType;
            srcLocal = "./" + fileName + "_files" + File.separator + FileNameExt;  // 之所以要加 num ，是为了防止有些图片url是 /img.php?1212 等参数形式，导致得到的文件名都为 img

//            srcMap.put( num , new SrcPair( srcNet,imgName ));
            imgMeta = new Img();
            imgMeta.setNo(num);
            imgMeta.setName(FileNameExt);
            imgMeta.setSrc(srcNet);
            imgMeta.setArticleId(articleId);
            imgMeta.setDownState(Api.ImgMeta_Downing);
            imgMap.put(num, imgMeta);

            temp = " src=\"" + srcLocal + "\"" + " netsrc=\"" + srcNet + "\"";
            tempHtml = tempHtml.replace( indexA, indexB + 1, temp ) ;
            indexB = indexA + 6 + srcLocal.length() + srcNet.length() + 10;
            indexA = tempHtml.indexOf("<img ", indexB);
        }
//        map.put( tempHtml.toString(), imgList);
        if (imgMap.size() == 1) {
            imgMap.put(0, new Img(0L, 0, "", tempHtml.toString(), "", 0));
            return imgMap;
        }
        imgMap.put(0, new Img(0L, imgMap.size() - 1, "", tempHtml.toString(), "", 0));

        KLog.i("【文章2】" + imgMap.size());
        return imgMap;
    }



    /**
     * 将 cache html 中的 src 的 **MD5_files 文件夹由 MD5 加密，改为正常的 **Name_files，防止图片不能显示
     *
     * @param fileName 用于保存的文件名，所以去过滤特殊字符
     * @param oldHtml
     * @return
     */
    public static String reviseHtmlForBox(String fileName, String oldHtml) {
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


    /**
     * 从 url 中获取文件名(含后缀)
     * @param url 网址
     * @return 文件名(含后缀)
     */
    public static String getFileNameExtByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String fileName;
        int separatorIndex = url.lastIndexOf("/") + 1;
        fileName = url.substring(separatorIndex, url.length());
        fileName = StringUtil.getOptimizedNameForSave(fileName); //
//        KLog.e("【文件名与后缀名】" + fileName);
        return fileName;
    }

//    /**
//     * 从 url 中获取文件名(不含后缀)
//     *
//     * @param url 网址
//     * @return 文件名
//     */
//    public static String getFileNameByUrl(String url){
//        if (TextUtils.isEmpty(url)) {
//            return null;
//        }
//        int dotIndex = url.lastIndexOf(".");
//        int separatorIndex = url.lastIndexOf("/") + 1;
//        String fileName;
//        if( separatorIndex > dotIndex ){
//            dotIndex = url.length();
//        }
//        fileName = url.substring(separatorIndex, dotIndex);
//        fileName = handleSpecialChar(fileName );
////        KLog.e("【文件名】" + fileName);
////        int extLength = separatorIndex - dotIndex; extLength +
////        KLog.e("【文件名】" + dotIndex + '='+ separatorIndex + '='+ '=' + url.length() );
//        return fileName;
//    }

//    /**
//     * 从 url 中获取文件后缀名
//     * @param url 网址
//     * @return 文件后缀名
//     */
//    public static String getFileExtByUrl(String url) {
//        if (TextUtils.isEmpty(url)) {
//            return null;
//        }
//        int dotIndex = url.lastIndexOf(".");
//        int extLength = url.length() - dotIndex;
//        String fileExt;
//        if (extLength < 6) {
//            fileExt = url.substring(dotIndex, url.length());
//        } else {
//            if (url.contains(".jpg")) {
//                fileExt = ".jpg";
//            } else if (url.contains(".jpeg")) {
//                fileExt = ".jpeg";
//            } else if (url.contains(".png")) {
//                fileExt = ".png";
//            } else if (url.contains(".gif")) {
//                fileExt = ".gif";
//            } else {
//                fileExt = "";
//            }
//        }
////         KLog.i( "【获取 FileExtByUrl 】" + url.substring( dotIndex ,url.length()) + extLength );
////         KLog.i( "【修正正文内的SRC】的格式" + fileExt + url );
//        return fileExt;
//    }


    /**
     * 处理文件名中的特殊字符和表情
     *
     * @param fileName 文件名
     * @return 处理后的文件名
     */
    public static String getOptimizedNameForSave(String fileName) {
        KLog.e("优化后的文件名A：" + fileName);
        fileName = Html.fromHtml(fileName).toString(); // 因为有些title会用 html中的转义。随意这里要改过来
        fileName = StringUtil.filterChar(fileName);
        KLog.e("优化后的文件名B：" + fileName);
        fileName = EmojiUtil.filterEmoji(fileName);
        KLog.e("优化后的文件名C：" + fileName + "=" + fileName.trim());
//        if (fileName.equals("")) {
//            fileName = "WuTi_" + getRandomString(6).toLowerCase();
//        }
        return fileName.trim();
    }


    private static String filterChar(String source) {
        return source
                .replace("\\", "")
                .replace("/", "")
                .replace(":", "")
                .replace("*", "")
                .replace("?", "")
                .replace("\"", "")
                .replace("<", "")
                .replace(">", "")
                .replace("|", "")
                .replace("%", "_")
                .replace("#", "_")
                .replace("&", "_")
                .replace("&amp;", "_")
                .replace("\n", "_");
    }


    private static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public static ArrayList<String[]> formStringToParamList(String paramString) {
        if (TextUtils.isEmpty(paramString)) {
            return null;
        }
        String[] paramStringArray = paramString.split("_");
        String[] paramPair;
        ArrayList<String[]> paramList = new ArrayList<>();
        for (String string : paramStringArray) {
            paramPair = string.split("#");
            if (paramPair.length != 2) {
                continue;
            }
            paramList.add(paramPair);
            KLog.i("【1】" + paramPair[0] + paramPair[1]);
        }
        return paramList;
    }

    public static String formParamListToString(ArrayList<String[]> paramList) {
        if (paramList == null) {
            return "";
        }
        if (paramList.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder("");
        for (String[] paramPair : paramList) {
            sb.append(paramPair[0] + "#" + paramPair[1] + "_");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }


    public static String[] poll(Queue<String> paramQueue, int paramInt) {
        int i = Math.min(paramQueue.size(), paramInt);
        String[] arrayOfString = new String[i];
        for (paramInt = 0; paramInt < i; paramInt++) {
            arrayOfString[paramInt] = paramQueue.poll();
        }
        return arrayOfString;
    }


    /**
     * 获取修整后的概要
     *
     * @param tempHtml 原文
     * @return
     */
    public static String getOptimizedSummary(String tempHtml) {
        // Shrink string to optimize render time
        String result = "";
        if (TextUtils.isEmpty(tempHtml)) {
            return result;
        }
        // 过滤广告
        tempHtml = delHtmlAd(tempHtml);
        // 过滤其他标签
        tempHtml = delHtmlTag(tempHtml);

//        tempHtml = Jsoup.parse(tempHtml).text();
        int showLength = tempHtml.length() < 90 ? tempHtml.length() : 90;
        if (showLength > 0) {
            result = tempHtml.substring(0, showLength);
        }
        return result;
    }

    public static String getSummary11(String summary) {
        if (summary.length() > 92) {
            return summary.substring(0, 92);
        } else {
            return summary.substring(0, summary.length());
        }
    }

    // 过滤标签
    /**
     * 定义script的正则表达式
     */
    private static final String REGEX_SCRIPT = "<script[^>]*?>[\\s\\S]*?<\\/script>";
    /**
     * 定义style的正则表达式
     */
    private static final String REGEX_STYLE = "<style[^>]*?>[\\s\\S]*?<\\/style>";
    /**
     * 定义HTML标签的正则表达式
     */
    private static final String REGEX_HTML = "<[^>]+>";
    /**
     * 定义空格回车换行符
     */
    private static final String REGEX_SPACE = "\\s*|\t|\r|\n";

    public static String delHtmlTag(String htmlStr) {
        // 过滤script标签
        Pattern p_script = Pattern.compile(REGEX_SCRIPT, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll("");
        // 过滤style标签
        Pattern p_style = Pattern.compile(REGEX_STYLE, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll("");
        // 过滤html标签
        Pattern p_html = Pattern.compile(REGEX_HTML, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll("");
//        // 过滤空格回车标签
//        Pattern p_space = Pattern.compile(REGEX_SPACE, Pattern.CASE_INSENSITIVE);
//        Matcher m_space = p_space.matcher(htmlStr);
//        htmlStr = m_space.replaceAll("");
        return htmlStr.trim(); // 返回文本字符串
    }

    /**
     * 定义广告标签的正则表达式(这段正则压根不生效)
     * // 过滤广告标签
     */
    private static final String REGEX_AD = "(?=\\<center>)[\\s\\S]*?inoreader[\\s\\S]*?(?<=<\\/center>)";

    private static String delHtmlAd(String htmlStr) {
        Pattern p_ad = Pattern.compile(REGEX_AD, Pattern.CASE_INSENSITIVE);
        Matcher m_ad = p_ad.matcher(htmlStr);
        htmlStr = m_ad.replaceAll("");
        return htmlStr.trim();
    }


    public static String delHtmlAd2(String oldHtml) {
//         KLog.i("去广告" + tempHtml);
        StringBuilder tempHtml = new StringBuilder(oldHtml);
        int indexA = tempHtml.indexOf("<center>", 0);
        int indexB = tempHtml.indexOf("</center>", indexA);
        KLog.i("去广告 = " + indexA + ":" + indexB);
        if (indexA < 0 || indexB < 0) {
            return tempHtml.toString();
        }
        String temp = tempHtml.substring(indexA + 8, indexB);
//         KLog.i("广告" + temp);
        if (temp.contains("Ads") && temp.contains("Inoreader")) {
            tempHtml = tempHtml.replace(indexA, indexB + 9, "");
        }
//         KLog.i("修正后的文章" + tempHtml);
        return tempHtml.toString();
    }


    /**
     * 获取字符串编码格式
     *
     * @param str
     * @return
     */
    private static String getEncode(String str) {
        final String[] encodes = new String[]{"UTF-8", "GBK", "GB2312", "ISO-8859-1", "ISO-8859-2"};
        byte[] data = str.getBytes();
        byte[] b = null;
        a:
        for (int i = 0; i < encodes.length; i++) {
            try {
                b = str.getBytes(encodes[i]);
                if (b.length != data.length)
                    continue;
                for (int j = 0; j < b.length; j++) {
                    if (b[j] != data[j]) {
                        continue a;
                    }
                }
                return encodes[i];
            } catch (UnsupportedEncodingException e) {
                continue;
            }
        }
        return null;
    }

    /**
     * 将字符串转换成指定编码格式
     *
     * @param str
     * @param encode
     * @return
     */
    public static String transcoding(String str, String encode) {
        String df = "ISO-8859-1";
        try {
            String en = getEncode(str);
            if (en == null)
                en = df;
            return new String(str.getBytes(en), encode);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }


}
