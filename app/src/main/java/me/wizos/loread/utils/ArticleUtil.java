package me.wizos.loread.utils;


import android.text.Html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.Enclosure;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.extractor.ExtractorUtil;
import okhttp3.MediaType;
import okhttp3.ResponseBody;

/**
 * 文章处理工具类
 *
 * @author by Wizos on 2020/3/16.
 */
public class ArticleUtil {
    /**
     * 将文章保存到内存
     * @param article
     * @param title
     * @return
     */
    public static String getPageForSave(Article article, String title) {
        String published = TimeUtil.format(article.getPubDate(), "yyyy-MM-dd HH:mm");
        String link = article.getLink();
        String content = getFormatContentForSave(title, article.getContent());
        Feed feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(), article.getFeedId());
        String author = getOptimizedAuthor(feed, article.getAuthor());

        return "<!DOCTYPE html><html><head>" +
                "<meta charset=\"UTF-8\">" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"./normalize.css\" />" +
                "<title>" + title + "</title>" +
                "</head><body>" +
                "<article id=\"article\" >" +
                "<header id=\"header\">" +
                "<h1 id=\"title\"><a href=\"" + link + "\">" + title + "</a></h1>" +
                "<p id=\"author\">" + author + "</p>" +
                "<p id=\"pubDate\">" + published + "</p>" +
                "</header>" +
                "<section id=\"content\">" + content + "</section>" +
                "</article>" +
                "</body></html>";
    }


//    public static String getPageForDisplay(Article article) {
//        return getPageForDisplay(article, App.DISPLAY_RSS);
//    }
    public static String getPageForDisplay(Article article) { //, String referer

        if (null == article) {
            return "";
        }
        // 获取排版文件路径（支持自定义的文件）
        String typesettingCssPath = App.i().getUserConfigPath() + "normalize.css";
        if (!new File(typesettingCssPath).exists()) {
            typesettingCssPath = "file:///android_asset/css/normalize.css";
        }

        // 获取排版文件路径（支持自定义的文件）
        String mediaJsPath = App.i().getUserConfigPath() + "media.js";
        if (!new File(mediaJsPath).exists()) {
            mediaJsPath = "file:///android_asset/js/media.js";
        }

        // 获取主题文件路径
        String themeCssPath, hljCSssPath;
        if (App.i().getUser().getThemeMode() == App.THEME_DAY) {
            themeCssPath = App.i().getUserConfigPath() + "article_theme_day.css";
            if (!new File(typesettingCssPath).exists()) {
                themeCssPath = "file:///android_asset/css/article_theme_day.css";
            }
        } else {
            themeCssPath = App.i().getUserConfigPath() + "article_theme_night.css";
            if (!new File(typesettingCssPath).exists()) {
                themeCssPath = "file:///android_asset/css/article_theme_night.css";
            }
        }
        hljCSssPath = "file:///android_asset/css/android_studio.css";

        Feed feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(), article.getFeedId());
        String author = getOptimizedAuthor(feed, article.getAuthor());

        String initImageHolderUrl =
                "var IMAGE_HOLDER_CLICK_TO_LOAD_URL = placeholder.getData({text: '" + App.i().getString(R.string.click_to_load_this_picture) + "'});" +
                "var IMAGE_HOLDER_LOADING_URL = placeholder.getData({text: '" + App.i().getString(R.string.loading) + "'});" +
                "var IMAGE_HOLDER_LOAD_FAILED_URL = placeholder.getData({text: '" + App.i().getString(R.string.loading_failed_click_here_to_retry) + "'});" +
                "var IMAGE_HOLDER_IMAGE_ERROR_URL = placeholder.getData({text: '" + App.i().getString(R.string.picture_error_click_here_to_retry) + "'});";
        String content = getFormatContentForDisplay2(article);

        String title = article.getTitle();
        if (StringUtils.isEmpty(title)) {
            title = App.i().getString(R.string.no_title);
        }

        return "<!DOCTYPE html><html><head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='referrer' content='origin'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0, user-scalable=no'>" +
                "<link rel='stylesheet' type='text/css' href='" + typesettingCssPath + "'/>" +
                "<link rel='stylesheet' type='text/css' href='" + themeCssPath + "'/>" +
                "<link rel='stylesheet' type='text/css' href='" + hljCSssPath + "'/>" +
                "<title>" + title + "</title>" +
                "</head><body>" +
                "<article id='" + article.getId() + "'>" +
                "<header id='header'>" +
                "<h1 id='title'><a href='" + article.getLink() + "'>" + title + "</a></h1>" +
                "<p id='author'>" + author + "</p>" +
                "<p id='pubDate'>" + TimeUtil.format(article.getPubDate(), "yyyy-MM-dd HH:mm") + "</p>" +
                "</header>" +
                //"<hr id=\"hr\">" +
                "<section id='content'>" + content +
                "</section>" +
                "</article>" +
                "<script src='file:///android_asset/js/zepto.min.js'></script>" + // defer
                //"<script src='file:///android_asset/js/lazyload.js'></script>" +
                //"<script src='file:///android_asset/js/intersection-observer.js'></script>" +
                //"<script src='file:///android_asset/js/lazyload.min.js'></script>" +
                "<script src='file:///android_asset/js/lozad.min.js'></script>" +
                "<script src='file:///android_asset/js/highlight.pack.js'></script>" +
                "<script src='file:///android_asset/js/placeholder.min.js'></script>" +
                "<script>" + initImageHolderUrl + "</script>" +
                "<script src='" + mediaJsPath + "' defer></script>" +
                "<script defer>hljs.initHighlightingOnLoad();</script>" +
                "</body></html>";
    }


    public static String getContentForSpeak(Article article) {
        String html = article.getContent();
        Pattern pattern;
        pattern = Pattern.compile("(<br>|<hr>|<p>|<pre>|<table>|<td>|<h\\d>|<ul>|<ol>|<li>)", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(". $1");

        pattern = Pattern.compile("<img.*?>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.image));
        pattern = Pattern.compile("<(audio).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.audio));
        pattern = Pattern.compile("<(video).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.video));
        pattern = Pattern.compile("<(iframe).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.frame));
        pattern = Pattern.compile("<(embed).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.frame));
        html = Jsoup.parse(html).text();

        // 将网址替换为
        pattern = Pattern.compile("https*://[\\w?-_=./&]*([\\s　]|&nbsp;|[^\\w?-_=./&]|$)", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.summary_link) + "$1");

        // KLog.i("初始化内容" + html );
        return App.i().getString(R.string.article_title_is) + article.getTitle() + html.trim();
    }

    /**
     * 获取修整后的概要
     *
     * @param html 原文
     * @return
     */
    public static String getOptimizedSummary(String html) {
        if (StringUtils.isEmpty(html)) {
            return html;
        }
        Pattern pattern;

        pattern = Pattern.compile("(<br>|<hr>|<p>|<pre>|<table>|<td>|<h\\d>|<ul>|<ol>|<li>)", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll("➤$1");

        pattern = Pattern.compile("<img.*?>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.image));
        pattern = Pattern.compile("<(audio).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.audio));
        pattern = Pattern.compile("<(video).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.video));
        pattern = Pattern.compile("<(iframe).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.frame));
        pattern = Pattern.compile("<(embed).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.frame));

        html = Jsoup.parse(html).text();

        // 将连续多个空格换为一个
        pattern = Pattern.compile("([\\s　]|&nbsp;){2,}", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(" ");

        // 将连续多个➤合并为一个
        pattern = Pattern.compile("(([\\s　]|&nbsp;)*➤([\\s　]|&nbsp;)*){2,}", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll("➤");

        // 将某些符号前后的去掉
        pattern = Pattern.compile("([\\s　]|&nbsp;)*➤+([\\s　]|&nbsp;)*([+_\\-=%@#$^&,，.。:：!！?？○●◎⊙☆★◇◆□■△▲〓\\[\\]“”()（）〔〕〈〉《》「」『』［］〖〗【】{}])", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll("$3");
        pattern = Pattern.compile("([+_\\-=%@#$^&,，.。:：!！?？○●◎⊙☆★◇◆□■△▲〓\\[\\]“”()（）〔〕〈〉《》「」『』［］〖〗【】{}])([\\s　]|&nbsp;)*➤+([\\s　]|&nbsp;)*", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll("$1");

        // 将开头的去掉
        pattern = Pattern.compile("^\\s*➤*\\s*", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll("");

        // 将末尾的去掉
        pattern = Pattern.compile("\\s*➤*\\s*$", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll("");

        // 给前后增加空格
        pattern = Pattern.compile("([\\s　]|&nbsp;)*➤([\\s　]|&nbsp;)*", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(" ➤ ");

        html = html.substring(0,  Math.min(html.length(), 90) );
        return html.trim();
    }

    /**
     * 优化标题，去掉html转义、换行符
     * @param title 文章标题
     * @return
     */
    public static String getOptimizedTitle(String title) {
        if (!StringUtils.isEmpty(title)) {
            title = title.replace("\r", "").replace("\n", "");
            title = Html.fromHtml(Html.fromHtml(title).toString()).toString();
            return title;
        }
        return title;
    }

    /**
     * 在将服务器的文章入库前，对文章进行修整，主要是过滤无用&有干扰的标签、属性
     * @param articleUrl 文章链接
     * @param content    原文
     * @return
     */
    public static String getOptimizedContent(String articleUrl, String content) {
        if (StringUtils.isEmpty(content)) {
            return content;
        }
        Pattern pattern;
        Matcher matcher;

        // 过滤Ino广告
        pattern = Pattern.compile("(?=<center>)[\\s\\S]*?inoreader[\\s\\S]*?(?<=</center>)", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("");

        // 删除无效的空标签（无任何属性的）
        pattern = Pattern.compile("([\\s　]|&nbsp;)*<([a-zA-Z0-9]{1,10})>([\\s　]|&nbsp;)*</\\2>([\\s　]|&nbsp;)*", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("");

        // 删除无效的空标签（有属性的），注意此处的空标签必须是指定的，不然会把一些类似图片/音频/视频等“有意义的带属性空标签”给去掉
        pattern = Pattern.compile("([\\s　]|&nbsp;)*<(i|p|section|div|figure|pre|table|blockquote) [^>/]+>([\\s　]|&nbsp;)*</\\2>([\\s　]|&nbsp;)*", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("");

        // 将包含<br>的空标签给解脱出来
        pattern = Pattern.compile("([\\s　]|&nbsp;)*<([a-zA-Z0-9]{1,10})>([\\s　]|&nbsp;)*(<br>)+([\\s　]|&nbsp;)*</\\2>([\\s　]|&nbsp;)*", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("<br>");

        // 将包含<hr>的空标签给解脱出来
        pattern = Pattern.compile("([\\s　]|&nbsp;)*<([a-zA-Z0-9]{1,10})>([\\s　]|&nbsp;)*(<hr>)+([\\s　]|&nbsp;)*</\\2>([\\s　]|&nbsp;)*", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("<hr>");

        // 去掉没有意义的span标签
        pattern = Pattern.compile("[\\s　]*<span>([\\s\\S]*?)</span>[\\s　]*", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("$1");

        // 去掉块状元素之间的换行标签
        pattern = Pattern.compile("</(<h\\d>|p|div|figure|pre|img|audio|video|iframe|embed|table|blockquote)>([\\s　]|&nbsp;)*(<br>)+([\\s　]|&nbsp;)*<(<h\\d>|p|div|figure|pre|img|audio|video|iframe|embed|table|blockquote)>", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("</$1><$5>");

        // 去掉文章开头以及，各种【标题头】后紧跟着的换行标签
        pattern = Pattern.compile("(^|<h\\d>|<p>|<div>|<figure>|<pre>|<blockquote>)([\\s　]|&nbsp;)*(<br>|<hr>|<b>)+([\\s　]|&nbsp;)*", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("$1");

        // 去掉文章末尾的换行标签
        pattern = Pattern.compile("([\\s　]|&nbsp;)*(<br>|<hr>|<b>)+([\\s　]|&nbsp;)*($|</h\\d>|</p>|</div>|</figure>|</pre>|</blockquote>)", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("$4");

        // 给两个连续的链接之间加一个换行符
        pattern = Pattern.compile("</a>([\\s　]|&nbsp;)*<a", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("</a><br><a");

        Element element;
        Elements elements;
        Element documentBody = Jsoup.parseBodyFragment(content, articleUrl).body();

        // 如果文章的开头就是 header 元素，则移除
        elements = documentBody.children();
        if( elements != null && elements.size() > 0 && elements.first().nodeName().equalsIgnoreCase("header") ){
            elements.first().remove();
        }

        // 去掉script标签
        documentBody.getElementsByTag("script").remove();
        // 去掉style标签
        documentBody.getElementsByTag("style").remove();

        // picture 元素下会有一个标准的 img 元素，以及多个在不同条件下适配的 source 元素。
        // 故先将 source 元素去掉，再将 picture unwrap，仅保留 img 元素
        documentBody.select("picture > source").remove();
        documentBody.getElementsByTag("picture").unwrap();

        // 将 noscript 标签 unwrap
        documentBody.getElementsByTag("noscript").unwrap();

        // 去掉src为空的标签
        documentBody.select("[src=''],[src='about:blank'],iframe:not([src]),embed:not([src]),video:not([src]),audio:not([src]),img:not([src])").remove();
        // 将 href 属性为空的 a 标签 unwrap
        documentBody.select("[href=''],[href='about:blank'],a:not([href])").unwrap();
        // 去掉空标签
        boolean circulate;
        do {
            elements = documentBody.select("p:empty, div:empty, blockquote:empty, details:empty, details:empty, figure:empty, figcaption:empty,  ul:empty, ol:empty, li:empty,  table:empty, tbody:empty, th:empty, tr:empty, dt:empty, dl:empty,  section:empty, h1:empty, h2:empty, h3:empty, h4:empty, h5:empty, h6:empty, ins:empty, a:empty, b:empty, string:empty, span:empty, i:empty,  section:empty, h1:empty, h2:empty, h3:empty, h4:empty, h5:empty, h6:empty, ins:empty, a:empty, b:empty, string:empty, span:empty, i:empty");
            if( elements != null && elements.size() > 0){
                //System.out.println("继续移除：" + elements.outerHtml());
                elements.remove();
                circulate = true;
            }else {
                circulate = false;
            }
        }while (circulate);

        // tabindex属性，会导致图片有边框
        documentBody.removeAttr("tabindex");
        // video的controlslist属性可能会被配置为禁用下载/全屏，所以去掉
        documentBody.removeAttr("controlslist");
        documentBody.removeAttr("dragable");
        documentBody.removeAttr("contenteditable");
        // img的crossorigin属性导致图片无法正确展示
        documentBody.removeAttr("crossorigin");
        documentBody.removeAttr("class");

        String tmp;
        // \s匹配的是 制表符\t,换行符\n,回车符\r，换页符\f以及半角空格
        elements = documentBody.getElementsByTag("pre");
        for (int i = 0, size = elements.size(); i < size; i++) {
            tmp = elements.get(i).html().trim();
            pattern = Pattern.compile("([\\s　]|&nbsp;)*<([^>/]+)>([\\s　]|&nbsp;)*([\\s\\S]+)([\\s　]|&nbsp;)*</\\1>([\\s　]|&nbsp;)*", Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(tmp);
            if (matcher.matches()) {
                tmp = pattern.matcher(tmp).replaceAll("<$2>$4</$2>");
                elements.get(i).html(tmp);
            }
        }

        elements = documentBody.getElementsByTag("code");
        for (int i = 0, size = elements.size(); i < size; i++) {
            tmp = elements.get(i).html().trim();
            pattern = Pattern.compile("([\\s　]|&nbsp;)*<([^>/]+)>([\\s　]|&nbsp;)*([\\s\\S]+)([\\s　]|&nbsp;)*</\\1>([\\s　]|&nbsp;)*", Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(tmp);
            if (matcher.matches()) {
                tmp = pattern.matcher(tmp).replaceAll("<$2>$4</$2>");
                elements.get(i).html(tmp);
            }
        }

        // 将以下存放的原始src转为src的路径
        String[] oriSrcAttr = {"data-src", "data-original", "data-lazy-src", "zoomfile", "file"};
        for (String attr : oriSrcAttr) {
            elements = documentBody.select("img[" + attr + "]");
            for (int i = 0, size = elements.size(); i < size; i++) {
                element = elements.get(i);
                tmp = element.attr(attr);
                element.removeAttr(attr).attr("src", tmp);
            }
        }

        // 只保留 srcset 属性中尺寸最大的一张图片（该属性会根据屏幕分辨率选择想要显示的src）
        elements = documentBody.select("img[srcset]");
        for (int i = 0, size = elements.size(); i < size; i++) {
            element = elements.get(i);
            String srcsetAttr = element.attr("srcset");
            if (StringUtils.isEmpty(srcsetAttr)) {
                continue;
            }
            String[] srcSet;
            if (srcsetAttr.contains(",")) {
                srcSet = srcsetAttr.split(",");
            } else {
                srcSet = new String[]{srcsetAttr};
            }
            int greaterDimen = 0;
            String greaterSrc = null;
            for (String srcDimen : srcSet) {
                pattern = Pattern.compile("(\\S+)\\s+(\\d*)[xXwW]", Pattern.CASE_INSENSITIVE);
                matcher = pattern.matcher(srcDimen);
                if (!matcher.find()) {
                    continue;
                }
                if (!StringUtils.isEmpty(matcher.group(2)) && Integer.parseInt(matcher.group(2)) > greaterDimen) {
                    greaterSrc = matcher.group(1);
                    greaterDimen = Integer.parseInt(matcher.group(2));
                }
            }
            if (!StringUtils.isEmpty(greaterSrc)) {
                elements.get(i).attr("src", greaterSrc);
            }
            element.removeAttr("srcset").removeAttr("sizes");
        }

        // 去掉内联的css样式中的强制不换行
        elements = documentBody.select("[style*=white-space]");
        for (int i = 0, size = elements.size(); i < size; i++) {
            tmp = elements.get(i).attr("style");
            pattern = Pattern.compile("white-space.*?(;|$)", Pattern.CASE_INSENSITIVE);
            tmp = pattern.matcher(tmp).replaceAll("");
            elements.get(i).attr("style", tmp);
        }

        // 将相对连接转为绝对链接
        elements = documentBody.getElementsByAttribute("src");
        for (int i = 0, size = elements.size(); i < size; i++) {
            element = elements.get(i);
            element.attr("src", element.attr("abs:src"));
        }
        elements = documentBody.getElementsByAttribute("href");
        for (int i = 0, size = elements.size(); i < size; i++) {
            element = elements.get(i);
            tmp = element.attr("href");
            if(StringUtils.isEmpty(tmp) || tmp.startsWith("magnet:?")){
                continue;
            }
            element.attr("href", element.attr("abs:href"));
        }

        return documentBody.html().trim();
    }

    private static String getFormatContentForSave(String title, String content) {
        Document document = Jsoup.parseBodyFragment(content);
        Elements elements = document.getElementsByTag("img");
        String url, filePath;
        for (int i = 0, size = elements.size(); i < size; i++) {
            url = elements.get(i).attr("src");
            filePath = "./" + title + "_files/" + UriUtil.guessFileNameExt(url);
            elements.get(i).attr("original-src", url);
            elements.get(i).attr("src", filePath);
        }
        return document.body().html();
    }


    /**
     * 格式化给定的文本，用于展示
     * 这里没有直接将原始的文章内容给到 webView 加载，再去 webView 中初始化占位图并懒加载。
     * 是因为这样 WebView 刚启动时，有的图片因为还没有被 js 替换为占位图，而展示一个错误图。
     * 这里直接将内容初始化好，再让 WebView 执行懒加载的 js 去给没有加载本地图的 src 执行下载任务。
     *
     * @param article
     * @return
     */
    private static String getFormatContentForDisplay2(Article article) {
        if (StringUtils.isEmpty(article.getContent())) {
            return "";
        }
        String originalUrl;
        String imgHolder = "file:///android_asset/image/image_holder.png";

        Element img;
        Document document = Jsoup.parseBodyFragment(article.getContent(), article.getLink());
        document = ColorModifier.i().modifyDocColor(document);

        Elements elements;
        String cacheUrl;
        String idInMD5 = EncryptUtil.MD5(article.getId());

        elements = document.getElementsByTag("img");
        for (int i = 0, size = elements.size(); i < size; i++) {
            img = elements.get(i);
            // 抽取图片的绝对连接
            originalUrl = img.attr("abs:src");
            img.attr("original-src", originalUrl);

            cacheUrl = FileUtil.readCacheFilePath(idInMD5, originalUrl);
            if (cacheUrl != null) {
                img.attr("src", cacheUrl);
            } else {
                img.attr("src", imgHolder);
                img.addClass("img-lozad");
            }
        }

        elements = document.getElementsByTag("input");
        for (Element element : elements) {
            element.attr("disabled", "disabled");
        }

        //elements = document.getElementsByTag("embed");
        //for (Element element : elements) {
        //    element.attr("autostart","1");
        //}
        //elements = document.getElementsByTag("video");
        //for (Element element : elements) {
        //    element.attr("controls", "true")
        //            .attr("width", "100%")
        //            .attr("height", "auto")
        //            .attr("preload", "metadata");
        //    //element.wrap("<div class=\"video_wrap\"></div>");
        //}
        //elements = document.getElementsByTag("audio");
        //for (Element element : elements) {
        //    element.attr("controls", "true")
        //            .attr("width", "100%");
        //}
        ////// 给 table 包装 div 并配合 overflow-x: auto; ，让 table 内的 pre 不会撑出屏幕
        //elements = document.getElementsByTag("table");
        //for (Element element : elements) {
        //    element.wrap("<div class=\"table_wrap\"></div>");
        //}
        //elements = document.getElementsByTag("video,audio");
        //for (Element element : elements) {
        //    element.attr("data-src", element.attr("src"));
        //    element.removeAttr("src");
        //    element.addClass("lozad");
        //}
        return document.body().html().trim();
    }
    public static String getCoverUrl(String articleUrl, String content) {
        // 获取第1个图片作为封面
        Document document = Jsoup.parseBodyFragment(content,articleUrl);
        Elements elements = document.getElementsByTag("img");
        String coverUrl = "";

        if( elements != null && elements.size() > 0 ){
            for (Element element:elements) {
                coverUrl = element.attr("abs:src");
                if(!coverUrl.endsWith(".svg")){
                    break;
                }else {
                    return coverUrl;
                }
            }
        }

        elements = document.select("video[poster]");
        if( elements != null && elements.size()>0 ){
            coverUrl = elements.attr("abs:poster");
        }
        return coverUrl;
    }
    
    
    private static String getOptimizedAuthor(Feed feed, String articleAuthor) {
        if (null == feed) {
            if (StringUtils.isEmpty(articleAuthor)) {
                return "";
            }else {
                return articleAuthor;
            }
        }

        String articleAuthorLowerCase = StringUtils.isEmpty(articleAuthor) ? "" : articleAuthor.toLowerCase();
        String feedTitleLowerCase = StringUtils.isEmpty(feed.getTitle()) ? "" : feed.getTitle().toLowerCase();

        if (feedTitleLowerCase.contains(articleAuthorLowerCase)) {
            return feed.getTitle();
        } else if (articleAuthorLowerCase.contains(feedTitleLowerCase)) {
            return articleAuthor;
        } else {
            return feed.getTitle() + " / " + articleAuthor;
        }
    }


    public static String getOptimizedContentWithEnclosures(String content, List<Enclosure> attachments){
        // 获取视频或者音频附件
        if (attachments != null && attachments.size() != 0) {
            for (Enclosure enclosure : attachments) {
                if (StringUtils.isEmpty(enclosure.getType()) || StringUtils.isEmpty(enclosure.getHref()) || content.contains(enclosure.getHref())) {
                    continue;
                }
                if (!StringUtils.isEmpty(content)){
                    content = content + "<br>";
                }
                if (enclosure.getType().startsWith("image")) {
                    content = content + "<img src=\"" + enclosure.getHref() + "\"/>";
                } else if (enclosure.getType().startsWith("audio")) {
                    content = content + "<audio src=\"" + enclosure.getHref() + "\" preload=\"auto\" type=\"" + enclosure.getType() + "\" controls></audio>";
                } else if (enclosure.getType().startsWith("video")) {
                    content = content + "<video src=\"" + enclosure.getHref() + "\" preload=\"auto\" type=\"" + enclosure.getType() + "\" controls></video>";
                } else if(enclosure.getType().equalsIgnoreCase("application/x-shockwave-flash")){
                    content = content + "<iframe src=\"" + enclosure.getHref() + "\"></iframe>";
                }
            }
        }
        return content;
    }

    public static Article getReadabilityArticle(Article article, ResponseBody responseBody) throws IOException{
        MediaType mediaType  = responseBody.contentType();
        String charset = null;
        if( mediaType != null ){
            charset = DataUtil.getCharsetFromContentType(mediaType.toString());
        }
        //KLog.i("解析得到的编码为：" + mediaType + " ， "+  charset );

        // 换parser吧，jsoup默认使用是htmlParser，它会对返回内容做些改动来符合html规范，所以一般实际使用时都用的是xmlParser，代码如下
        Document doc;
        try {
            doc = Jsoup.parse(responseBody.byteStream(), charset, article.getLink());
        }catch (IOException e){
            throw e;
        }
        doc.outputSettings().prettyPrint(false);
        String content =  ExtractorUtil.getContent(article.getLink(), doc);
        content = ArticleUtil.getOptimizedContent(article.getLink(), content);
        //KLog.e("内容B：" + content);
        //Article newArticle = (Article)article.clone();
        article.setContent(content);

        String summary = ArticleUtil.getOptimizedSummary(content);
        article.setSummary(summary);

        String coverUrl = ArticleUtil.getCoverUrl(article.getLink(), content);

        if(!StringUtils.isEmpty(coverUrl)){
            article.setImage(coverUrl);
        }else if( !StringUtils.isEmpty(article.getImage()) ){
            article.setImage(null);
        }
        return article;
    }


//    public static void autoSetArticleTags(Article article){
//        List<Category> categories = CoreDB.i().categoryDao().getByFeedId(article.getUid(),article.getFeedId());
//        List<ArticleTag> articleTags = CoreDB.i().articleTagDao().getByArticleId(article.getUid(),article.getId());
//        if(categories != null && categories.size() > 0 && (articleTags == null || articleTags.size() == 0)){
//            articleTags = new ArrayList<>(categories.size());
//            for (Category category:categories) {
//                Tag tag = new Tag();
//                tag.setUid(article.getUid());
//                tag.setId(category.getTitle());
//                tag.setTitle(category.getTitle());
//                ArticleTag articleTag = new ArticleTag(article.getUid(),article.getId(),tag.getId());
//                articleTags.add(articleTag);
//            }
//            CoreDB.i().articleTagDao().insert(articleTags);
//            ArticleTags.i().addArticleTags(articleTags);
//            ArticleTags.i().save();
//        }
//    }
}
