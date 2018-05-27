package me.wizos.loread.utils;


import android.text.Html;
import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.wizos.loread.App;
import me.wizos.loread.data.WithPref;
import me.wizos.loread.db.Article;

/**
 * 字符处理工具类
 * @author by Wizos on 2016/3/16.
 */
public class StringUtil {

    public static String toLongID(String id) {
        id = Long.toHexString(Long.valueOf(id));
        return "tag:google.com,2005:reader/item/" + String.format("%0" + (16 - id.length()) + "d", 0) + id;
    }

    /**
     * 将字符串转成MD5值
     *
     * @param string 字符串
     * @return MD5 后的字符串
     */
    public static String str2MD5(String string) {
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
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }



    public static boolean isBlank(List list){return  list==null || list.isEmpty() || list.size()==0;}


    public static String formatContentForSave(String title, String content) {
        Document document = Jsoup.parseBodyFragment(content);
        Elements elements = document.getElementsByTag("img");
        String url, filePath;
        for (int i = 0, size = elements.size(); i < size; i++) {
            url = elements.get(i).attr("src");
            filePath = "./" + title + "_files/" + i + "-" + getFileNameExtByUrl(url);
            elements.get(i).attr("original-src", url);
            elements.get(i).attr("src", filePath);
        }
        return document.body().html();
    }


    /**
     * 这里没有直接将原始的文章内容给到 webView 加载，再去 webView 中初始化占位图并懒加载。
     * 是因为这样 WebView 刚启动时，有的图片因为还没有被 js 替换为占位图，而展示一个错误图。
     * <p>
     * 这里直接将内容初始化好，再让 WebView 执行懒加载的 js 去给没有加载本地图的 src 执行下载任务
     */
    public static String initContent(Article article) {
        String cacheUrl;
        String originalUrl;
        String imageHolder;
        if (!HttpUtil.isNetworkAvailable()) { // 没有网络
            imageHolder = "file:///android_asset/image/image_holder_load_failed.png";
        } else if (WithPref.i().isDownImgWifi() && !HttpUtil.isWiFiUsed()) { // 开启省流量，蜂窝模式
            imageHolder = "file:///android_asset/image/image_holder_click_to_load.png";
        } else {
            imageHolder = "file:///android_asset/image/image_holder_loading.gif";
        }

        String articleContent = article.getContent();
        if (TextUtils.isEmpty(articleContent)) {
            return "";
        }
        Document document = Jsoup.parseBodyFragment(articleContent);
        Elements elements = document.getElementsByTag("img");
        String idInMD5 = StringUtil.str2MD5(article.getId());
        for (int i = 0, size = elements.size(); i < size; i++) {
            originalUrl = elements.get(i).attr("src");
            elements.get(i).attr("original-src", originalUrl);
            cacheUrl = FileUtil.readCacheFilePath(idInMD5, i, originalUrl);
            if (cacheUrl != null) {
                elements.get(i).attr("src", cacheUrl);
            } else {
                elements.get(i).attr("src", imageHolder);
            }
        }

        articleContent = document.body().html();
        return articleContent;
    }

    public static String getHtml(Article article) {
        return getHtml(article, null);
    }

    public static String getHtml(Article article, String content) {
        if (null == article) {
            return "";
        }
        // 获取排版文件路径（支持自定义的文件）
        String typesettingCssPath = App.i().getExternalFilesDir(null) + File.separator + "config" + File.separator + "normalize.css";
        if (!new File(typesettingCssPath).exists()) {
            typesettingCssPath = "file:///android_asset/css/normalize.css";
        }

        // 获取主题文件路径
        String themeCssPath;
        if (WithPref.i().getThemeMode() == App.Theme_Day) {
            themeCssPath = "file:///android_asset/css/article_theme_day.css";
        } else {
            themeCssPath = "file:///android_asset/css/article_theme_night.css";
        }

        String author = article.getAuthor();
        if (TextUtils.isEmpty(author) ||
                article.getOriginTitle().toLowerCase().contains(author.toLowerCase()) ||
                author.toLowerCase().contains(article.getOriginTitle().toLowerCase())) {
            author = article.getOriginTitle();
        } else {
            author = article.getOriginTitle() + "@" + article.getAuthor();
        }
        if (TextUtils.isEmpty(content)) {
            content = initContent(article);
        }
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + typesettingCssPath + "\" />" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + themeCssPath + "\" />" +
                "</head><body>" +
//                "<article id=\"article\" >" +
                "<article id=\"" + article.getId() + "\" >" +
                "<header id=\"header\">" +
                "<h1 id=\"title\"><a href=\"" + article.getCanonical() + "\">" + article.getTitle() + "</a></h1>" +
                "<p id=\"author\">" + author + "</p>" +
                "<p id=\"pubDate\">" + TimeUtil.stampToTime(article.getPublished() * 1000, "yyyy-MM-dd HH:mm") + "</p>" +
                "</header>" +
                "<hr id=\"hr\">" +
                "<section id=\"content\">" + content +
                "</section>" +
                "</article>" +
                "<script src=\"file:///android_asset/js/zepto.min.js\"></script>" +
                "<script src=\"file:///android_asset/js/lazyload.js\"></script>" +
                "<script src=\"file:///android_asset/js/image.js\"></script>" +
                "</body></html>";
    }


    private static List<String> format = new ArrayList<>();
    private static final String JPG = ".jpg";
    private static final String JPEG = ".jpeg";
    private static final String PNG = ".png";
    private static final String WEBP = ".webp";
    private static final String GIF = ".gif";

    static {
        format.add(JPG);
        format.add(JPEG);
        format.add(PNG);
        format.add(WEBP);
        format.add(GIF);
    }

    /**
     * 根据url获取图片文件的后缀
     *
     * @param url 网址
     * @return 后缀名
     */
    public static String getImageSuffix(String url) {
        String suffix = url.substring(url.lastIndexOf("."), url.length());
        if (!format.contains(suffix.toLowerCase())) {
            return JPG;
        }
        return suffix;
    }



    /**
     * 从 url 中获取文件名(含后缀)
     * @param url 网址
     * @return 文件名(含后缀)
     */
    public static String getFileNameExtByUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        String fileName;
        int separatorIndex = url.lastIndexOf("/") + 1;
        fileName = url.substring(separatorIndex, url.length());
        fileName = StringUtil.getOptimizedNameForSave(fileName);
        return fileName;
    }

    /**
     * 处理文件名中的特殊字符和表情
     *
     * @param fileName 文件名
     * @return 处理后的文件名
     */
    public static String getOptimizedNameForSave(String fileName) {
        // 因为有些title会用 html中的转义。所以这里要改过来
        fileName = Html.fromHtml(fileName).toString();
        fileName = EmojiUtil.filterEmoji(fileName);
        fileName = StringUtil.filterChar(fileName);
//        KLog.e("优化后的文件名C：" + fileName + "=" + fileName.trim());
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
//                .replace("%", "_")
//                .replace("#", "_")
//                .replace("&", "_")
                .replace("&amp;", "&")
                .replace("\n", "_");
    }



    /**
     * 获取修整后的概要
     *
     * @param tempHtml 原文
     * @return
     */
    public static String getOptimizedSummary(String tempHtml) {
        String result = "";
        if (TextUtils.isEmpty(tempHtml)) {
            return result;
        }
        // 过滤其他标签
        tempHtml = delAllTag(tempHtml);

        // 过滤空格回车标签
        Pattern p_enter = Pattern.compile(REGEX_ENTER, Pattern.CASE_INSENSITIVE);
        Matcher m_enter = p_enter.matcher(tempHtml);
        tempHtml = m_enter.replaceAll("");

        Pattern p_space = Pattern.compile(REGEX_SPACE, Pattern.CASE_INSENSITIVE);
        Matcher m_space = p_space.matcher(tempHtml);
        tempHtml = m_space.replaceAll(" ");

        tempHtml = tempHtml.trim();
        int showLength = tempHtml.length() < 90 ? tempHtml.length() : 90;
        if (showLength > 0) {
            result = tempHtml.substring(0, showLength);
        }
        return result;
    }

    /**
     * 获取修整后的概要
     *
     * @param tempHtml 原文
     * @return
     */
    public static String getOptimizedContent(String tempHtml) {
        String result = "";
        if (TextUtils.isEmpty(tempHtml)) {
            return result;
        }
        // 过滤广告
        tempHtml = delInoReaderAd(tempHtml);
        // 过滤其他标签
        tempHtml = delScriptTag(tempHtml);
        tempHtml = delStyleTag(tempHtml);
        return tempHtml.trim();
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
    private static final String REGEX_SPACE = "\\s+";
    private static final String REGEX_ENTER = "\t|\r|\n";

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

    public static String delScriptTag(String htmlStr) {
        // 过滤script标签
        Pattern p_script = Pattern.compile(REGEX_SCRIPT, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        return m_script.replaceAll("");
    }

    public static String delStyleTag(String htmlStr) {
        Pattern p_style = Pattern.compile(REGEX_STYLE, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        return m_style.replaceAll("");
    }

    public static String delAllTag(String htmlStr) {
        // 过滤html标签
        Pattern p_html = Pattern.compile(REGEX_HTML, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        return m_html.replaceAll("");
    }

    /**
     * 定义广告标签的正则表达式(这段正则压根不生效)
     * // 过滤广告标签
     */
    private static final String REGEX_AD = "(?=\\<center>)[\\s\\S]*?inoreader[\\s\\S]*?(?<=<\\/center>)";

    private static String delInoReaderAd(String htmlStr) {
        Pattern p_ad = Pattern.compile(REGEX_AD, Pattern.CASE_INSENSITIVE);
        Matcher m_ad = p_ad.matcher(htmlStr);
        return m_ad.replaceAll("");
    }


//    private static String getRandomString(int length) {
//        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
//        Random random = new Random();
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < length; i++) {
//            int number = random.nextInt(62);
//            sb.append(str.charAt(number));
//        }
//        return sb.toString();
//    }



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
                if (b.length != data.length) {
                    continue;}
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
            if (en == null) {
                en = df;}
            return new String(str.getBytes(en), encode);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }


}
