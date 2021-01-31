package me.wizos.loread.utils;


import android.text.Html;

import com.elvishew.xlog.XLog;

import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.bean.Enclosure;
import me.wizos.loread.db.Article;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.Feed;

/**
 * 文章处理工具类
 *
 * @author by Wizos on 2020/3/16.
 */
public class ArticleUtils {
    public static void saveArticle(String dir, Article article) {
        String title = FileUtils.getSaveableName(article.getTitle());
        String filePathTitle = dir + title;

        String articleIdInMD5 = EncryptUtils.MD5(article.getId());

        String published = TimeUtils.format(article.getPubDate(), "yyyy-MM-dd HH:mm");
        String link = article.getLink();
        Document documentBody = Jsoup.parseBodyFragment(article.getContent());
        documentBody.outputSettings().prettyPrint(false);
        documentBody.select("[id]").removeAttr("id");
        documentBody.select("[class]").removeAttr("class");
        Elements elements = documentBody.getElementsByTag("img");
        String url, urlHash, imgName;
        File imgFileWithId, imgFileWithName;
        for (int i = 0, size = elements.size(); i < size; i++) {
            url = elements.get(i).attr("src");
            urlHash = String.valueOf(url.hashCode());
            imgName = UriUtils.guessFileNameExt(url);

            imgFileWithId = FileUtils.readOriginalFile(articleIdInMD5, url);
            if(imgFileWithId != null){
                imgFileWithName = new File(App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original/" + imgName);
                if(imgFileWithName.exists()){
                    if(imgFileWithId.length() != imgFileWithName.length()){
                        imgName = urlHash + "_" + imgName;
                        imgFileWithName.renameTo(new File(App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original/" + imgName));
                    }
                }else {
                    imgFileWithId.renameTo(imgFileWithName);
                }
            }

            elements.get(i).attr("src", "./" + title + "_files/" + imgName);
            elements.get(i).attr("original-src", url);
        }

        String content =  documentBody.body().html();


        Feed feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(), article.getFeedId());
        String author = getOptimizedAuthor(feed, article.getAuthor());

        String html = "<!DOCTYPE html><html><head>" +
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

        FileUtils.save(filePathTitle + ".html", html);
        FileUtils.moveDir(App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original", filePathTitle + "_files");
        XLog.e("保存文件夹：" + filePathTitle + " , " + App.i().getUserFilesDir() + "/cache/" + articleIdInMD5 + "/original");
    }

    public static String getPageForDisplay(Article article) {
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
        String themeCssPath;
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


        Feed feed = CoreDB.i().feedDao().getById(App.i().getUser().getId(), article.getFeedId());
        String author = getOptimizedAuthor(feed, article.getAuthor());

        String initImageHolderUrl =
                "var IMAGE_HOLDER_CLICK_TO_LOAD_URL = placeholder.getData({text: '" + App.i().getString(R.string.click_to_load_this_picture) + "'});" +
                "var IMAGE_HOLDER_LOADING_URL = placeholder.getData({text: '" + App.i().getString(R.string.loading) + "'});" +
                "var IMAGE_HOLDER_LOAD_FAILED_URL = placeholder.getData({text: '" + App.i().getString(R.string.loading_failed_click_here_to_retry) + "'});" +
                "var IMAGE_HOLDER_IMAGE_ERROR_URL = placeholder.getData({text: '" + App.i().getString(R.string.picture_error_click_here_to_retry) + "'});";
        String content = getFormatContentForDisplay2(article);

        String plyrI18n = ",i18n:{speed:'"+ App.i().getString(R.string.speed) +"',normal:'"+ App.i().getString(R.string.normal) +"'}";

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
                "<link rel='stylesheet' type='text/css' href='file:///android_asset/css/android_studio.css'/>" +
                "<link rel='stylesheet' type='text/css' href='file:///android_asset/css/plyr.css'/>" +
                "<script src='file:///android_asset/js/tex-mml-chtml.js' async></script>" +
                "<title>" + title + "</title>" +
                "</head><body>" +
                "<article id='" + article.getId() + "'>" +
                "<header id='header'>" +
                "<h1 id='title'><a href='" + article.getLink() + "'>" + title + "</a></h1>" +
                "<p id='author'>" + author + "</p>" +
                "<p id='pubDate'>" + TimeUtils.format(article.getPubDate(), "yyyy-MM-dd HH:mm") + "</p>" +
                "</header>" +
                //"<hr id=\"hr\">" +
                "<section id='content'>" + content +
                "</section>" +
                "</article>" +
                "<script src='file:///android_asset/js/zepto.min.js'></script>" + // defer
                "<script src='file:///android_asset/js/lozad.min.js'></script>" +
                "<script src='file:///android_asset/js/highlight.pack.js'></script>" +
                "<script src='file:///android_asset/js/placeholder.min.js'></script>" +
                "<script src='file:///android_asset/js/plyr.js'></script>" +
                "<script>" + initImageHolderUrl + "</script>" +
                "<script>const PlyrConfig = {controls: ['play-large','play','progress','current-time','duration','settings','download','fullscreen'],settings: ['captions', 'quality', 'speed'],speed : { selected: 2, options: [0.75, 1, 1.5, 1.75, 2] } " + plyrI18n + "}</script>" +
                "<script src='" + mediaJsPath + "'></script>" +
                "</body></html>";
    }


    public static String getContentForSpeak(Article article) {
        String html = article.getContent();
        Pattern pattern;
        pattern = Pattern.compile("(<br>|<hr>|<p>|<pre>|<table>|<td>|<h\\d>|<ul>|<ol>|<li>)", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(". $1");

        pattern = Pattern.compile("<img.*?>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.image_for_summary));
        pattern = Pattern.compile("<embed.*?>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.frame_for_summary));
        pattern = Pattern.compile("<(audio).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.audio_for_summary));
        pattern = Pattern.compile("<(video).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.video_for_summary));
        pattern = Pattern.compile("<(iframe).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.frame_for_summary));
        pattern = Pattern.compile("<(pre).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.pre_for_summary));
        pattern = Pattern.compile("<(table).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.table_for_summary));

        html = Jsoup.parse(html).text();

        // 将网址替换为
        pattern = Pattern.compile("https*://[\\w?-_=./&]*([\\s　]|&nbsp;|[^\\w?-_=./&]|$)", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.link_for_speak) + "$1");

        // XLog.i("初始化内容" + html );
        return App.i().getString(R.string.article_title_is) + article.getTitle() + html.trim();
    }

    private static Pattern endSymbol = Pattern.compile("(?:[。;；?？!！)）}｝\\]】…])" , Pattern.CASE_INSENSITIVE);
    private static Pattern stopSymbol = Pattern.compile("(?:[。;；?？!！)）}｝\\]】…])" , Pattern.CASE_INSENSITIVE);

    /**
     * 优化标题，去掉html转义、换行符
     * @param title 文章标题
     * @return
     */
    public static String getOptimizedTitle(String title, String summary) {
        if (!StringUtils.isEmpty(title)) {
            title = title.replace("\r", "").replace("\n", "");
            title = Html.fromHtml(Html.fromHtml(title).toString()).toString();
            return title;
        }else if (!StringUtils.isEmpty(summary)){
            int length = Math.min(summary.length(), 64);
            title = summary.substring(0,length).trim();
            String reverse = new StringBuilder(title).reverse().toString();
            length = reverse.length();
            // 结束符号
            Matcher endSymbolMatcher = endSymbol.matcher(reverse);
            // 暂停符号
            Matcher stopSymbolMatcher = stopSymbol.matcher(reverse);
            int endSymbolPosition = 0;
            int stopSymbolPosition = 0;
            if(endSymbolMatcher.find()){
                endSymbolPosition = length - endSymbolMatcher.start();
            }
            if(stopSymbolMatcher.find()){
                stopSymbolPosition = length - stopSymbolMatcher.start() -1;
            }

            if(stopSymbolPosition == 0){
                stopSymbolPosition = endSymbolPosition;
            }else if(endSymbolPosition == 0){
                endSymbolPosition = stopSymbolPosition;
            }
            int endPosition = Math.max(stopSymbolPosition, endSymbolPosition);
            if(endPosition > 0){
                title = title.substring(0, endPosition).trim();
            }
        }
        return title;
    }

    public static String getExtractedTitle(String content) {
        if (!StringUtils.isEmpty(content)) {
            content = content.replace(App.i().getString(R.string.image_for_summary),"")
                    .replace(App.i().getString(R.string.frame_for_summary).trim(),"")
                    .replace(App.i().getString(R.string.audio_for_summary).trim(),"")
                    .replace(App.i().getString(R.string.video_for_summary).trim(),"")
                    .replace(App.i().getString(R.string.pre_for_summary).trim(),"")
                    .replace(App.i().getString(R.string.table_for_summary).trim(),"");
            content = content.substring(0, Math.min(64, content.length())).trim();
            String reverse = new StringBuilder(content).reverse().toString();
            int length = reverse.length();

            // 结束符号
            Matcher endSymbolMatcher = endSymbol.matcher(reverse);
            // 暂停符号
            Matcher stopSymbolMatcher = stopSymbol.matcher(reverse);

            XLog.d("长度：" +  length);
            int endSymbolPosition = 0;
            int stopSymbolPosition = 0;
            if(endSymbolMatcher.find()){
                endSymbolPosition = length - endSymbolMatcher.start();
            }
            if(stopSymbolMatcher.find()){
                stopSymbolPosition = length - stopSymbolMatcher.start() -1;
            }

            XLog.d("标志：" +  stopSymbolPosition + " , " + endSymbolPosition);
            if(stopSymbolPosition == 0){
                stopSymbolPosition = endSymbolPosition;
            }else if(endSymbolPosition == 0){
                endSymbolPosition = stopSymbolPosition;
            }
            int endPosition = Math.max(stopSymbolPosition, endSymbolPosition);
            if(endPosition > 0){
                content = content.substring(0, endPosition).trim();
            }
        }
        return content;
    }


    final static Pattern p_inoreader_ad = Pattern.compile("(?=<center>)[\\s\\S]*?inoreader[\\s\\S]*?(?<=</center>)", Pattern.CASE_INSENSITIVE);
    final static Pattern p_space_between_codes = Pattern.compile("(\\s|　|&nbsp;)*<([^>/]+)>(\\s|　|&nbsp;)*([\\s\\S]+)(\\s|　|&nbsp;)*</\\1>(\\s|　|&nbsp;)*", Pattern.CASE_INSENSITIVE);
    // final static Pattern p_display_inline_block = Pattern.compile("display\\s*:\\s*inline-block\\s*(;|$)", Pattern.CASE_INSENSITIVE);

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
        content = p_inoreader_ad.matcher(content).replaceAll("");

        Element element;
        Elements elements;
        Document document = Jsoup.parseBodyFragment(content, articleUrl);
        document.outputSettings().prettyPrint(false);
        Element documentBody = document.body();

        // 去掉script标签
        documentBody.getElementsByTag("script").remove();
        // 去掉style标签
        documentBody.getElementsByTag("style").remove();
        // 去掉link标签
        documentBody.getElementsByTag("link").remove();

        // picture 元素下会有一个标准的 img 元素，以及多个在不同条件下适配的 source 元素。
        // 故先将 source 元素去掉，再将 picture unwrap，仅保留 img 元素
        documentBody.select("picture > source").remove();
        documentBody.getElementsByTag("picture").unwrap();

        documentBody.getElementsByTag("ignore_js_op").unwrap();
        // 将 noscript 标签 unwrap；noscript 内部标签可能会和外部的一样，导致重复
        documentBody.getElementsByTag("noscript").remove();
        // documentBody.getElementsByTag("noscript").unwrap();
        // elements = documentBody.getElementsByTag("noscript");
        // for (int i = 0, size = elements.size(); i < size; i++) {
        //    element = elements.get(i).tagName("details");
        //    element.insertChildren(0,new Element("summary").text("\uD83D\uDD17"));
        // }

        // 去除推荐部分，可能有误杀
        // documentBody.select(".sharedaddy").remove();

        // 将 href 属性为空的 a 标签 unwrap
        documentBody.select("[href=''],[href='about:blank'],a:not([href])").unwrap();

        // tabindex属性，会导致图片有边框
        documentBody.select("[tabindex]").removeAttr("tabindex");
        // video的controlslist属性可能会被配置为禁用下载/全屏，所以去掉
        documentBody.select("video[controlslist]").removeAttr("controlslist");
        documentBody.select("[dragable]").removeAttr("dragable");
        documentBody.select("[contenteditable]").removeAttr("contenteditable");
        // img的crossorigin属性导致图片无法正确展示
        documentBody.select("[crossorigin]").removeAttr("crossorigin");
        documentBody.select("[referrerpolicy]").removeAttr("referrerpolicy");
        // font 的字体属性
        documentBody.select("[face]").removeAttr("face");

        documentBody.select("[onload]").removeAttr("onload");
        documentBody.select("[onclick]").removeAttr("onclick");
        documentBody.select("[onmouseout]").removeAttr("onmouseout");
        documentBody.select("[onmouseover]").removeAttr("onmouseover");

        String tmp;

        // 去掉代码之间的空格
        elements = documentBody.getElementsByTag("code");
        for (int i = 0, size = elements.size(); i < size; i++) {
            tmp = elements.get(i).html().trim();
            matcher = p_space_between_codes.matcher(tmp);
            if (matcher.matches()) {
                tmp = matcher.replaceAll("<$2>$4</$2>");
                elements.get(i).html(tmp);
            }
        }


        // 将以下存放的原始src转为src的路径 (发现某些RSS源中是有data-src属性的，但是TTRSS服务商会删掉它，例如：https://pewae.com/2020/10/e6b7bb-e4b881.html)
        String[] oriSrcAttr = {"data-src", "data-original", "data-lazy-src", "zoomfile", "file"};
        for (String attr : oriSrcAttr) {
            elements = documentBody.select("img[" + attr + "],audio[" + attr + "],video[" + attr + "],embed[" + attr + "],iframe[" + attr + "]");
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
        pattern = Pattern.compile("white-space.*?(;|$)", Pattern.CASE_INSENSITIVE);
        for (int i = 0, size = elements.size(); i < size; i++) {
            tmp = elements.get(i).attr("style");
            tmp = pattern.matcher(tmp).replaceAll("");
            elements.get(i).attr("style", tmp);
        }
        // 去掉内联的css样式中的强制行块
        elements = documentBody.select("[style*=display]");
        pattern = Pattern.compile("display\\s*:\\s*inline-block\\s*(;|$)", Pattern.CASE_INSENSITIVE);
        for (int i = 0, size = elements.size(); i < size; i++) {
            tmp = elements.get(i).attr("style");
            tmp = pattern.matcher(tmp).replaceAll("");
            elements.get(i).attr("style", tmp);
        }

        // 去掉内联的css样式中的不展示
        elements = documentBody.select("[style*=display]");
        pattern = Pattern.compile("display\\s*:\\s*none\\s*(;|$)", Pattern.CASE_INSENSITIVE);
        for (int i = 0, size = elements.size(); i < size; i++) {
            element = elements.get(i);
            tmp = element.attr("style");
            if(!pattern.matcher(tmp).find()){
                continue;
            }
            if(StringUtils.isEmpty(element.text().trim())){
                element.remove();
            }else {
                tmp = pattern.matcher(tmp).replaceAll("");
                element.attr("style", tmp);
                element.wrap("<details></details>");
                element.insertChildren(0,new Element("summary").addClass("loread").text("\uD83D\uDD17"));
            }
        }
        elements = documentBody.select("[style*=visibility]");
        pattern = Pattern.compile("visibility\\s*:\\s*hidden\\s*(;|$)", Pattern.CASE_INSENSITIVE);
        for (int i = 0, size = elements.size(); i < size; i++) {
            element = elements.get(i);
            tmp = element.attr("style");
            if(!pattern.matcher(tmp).find()){
                continue;
            }
            if(StringUtils.isEmpty(element.text().trim())){
                element.remove();
            }else {
                tmp = pattern.matcher(tmp).replaceAll("");
                element.attr("style", tmp);
                element.wrap("<details></details>");
                element.insertChildren(0,new Element("summary").addClass("loread").text("\uD83D\uDD17"));
            }
        }

        // 将嵌套的details解构出来
        elements = documentBody.select("details details summary.loread");
        for (int i = 0, size = elements.size(); i < size; i++) {
            elements.get(i).remove();
        }
        documentBody.getElementsByClass("loread").removeClass("loread");
        elements = documentBody.select("details details");
        for (int i = 0, size = elements.size(); i < size; i++) {
            elements.get(i).unwrap();
        }


        // 去掉内联的css样式中的固定位置
        elements = documentBody.select("[style*=position]");
        for (int i = 0, size = elements.size(); i < size; i++) {
            tmp = elements.get(i).attr("style");
            pattern = Pattern.compile("position\\s*:\\s*absolute\\s*(;|$)", Pattern.CASE_INSENSITIVE);
            tmp = pattern.matcher(tmp).replaceAll("");
            elements.get(i).attr("style", tmp);
        }

        // 去掉内联的css样式中的固定宽度
        elements = documentBody.select("[style*=width]");
        for (int i = 0, size = elements.size(); i < size; i++) {
            tmp = elements.get(i).attr("style");
            pattern = Pattern.compile("width\\s*:\\s*.*?(;|$)", Pattern.CASE_INSENSITIVE);
            tmp = pattern.matcher(tmp).replaceAll("");
            elements.get(i).attr("style", tmp);
        }

        // 清除空的style
        documentBody.select("[style='']").removeAttr("style");

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

        elements = documentBody.select("video:not([src]), audio:not([src])");
        for (int i = 0, size = elements.size(); i < size; i++) {
            if(elements.get(i).getElementsByTag("source").size() == 0){
                elements.get(i).remove();
            }
        }

        // 由于部分原始网页使用了懒加载，只有data-src属性，所以要等上面的代码处理了data-src为src后，在执行以下代码
        documentBody.select("iframe:not([src]),embed:not([src]),img:not([src])").remove();
        // 去掉src为空的标签
        documentBody.select("[src=''],[src='about:blank']").remove();


        // 去掉空标签（无法去掉标签内的text内容是纯空字符串的）
        boolean circulate;
        do {
            elements = documentBody.select("a:empty, b:empty, blockquote:empty, details:empty, div:empty, dl:empty, dt:empty, figcaption:empty, figure:empty, font:empty, footer:empty, h1:empty, h2:empty, h3:empty, h4:empty, h5:empty, h6:empty, i:empty, ins:empty, li:empty, ol:empty, p:empty, section:empty, span:empty, string:empty, table:empty, tbody:empty, th:empty, tr:empty, ul:empty");
            if( elements != null && elements.size() > 0){
                elements.remove();
                circulate = true;
            }else {
                circulate = false;
            }
        }while (circulate);


        // 如果文章的开头就是 header 元素，或者是 article > header / section > header，则移除
        elements = documentBody.children();
        if( elements.size() > 0 ){
            element = elements.first();
            if(element.tagName().equalsIgnoreCase("article") || element.nodeName().equalsIgnoreCase("section")){
                element = element.children().first();
            }
            if(element.tagName().equalsIgnoreCase("header")){
                element.remove();
            }
        }

        // 如果根元素是个单元素，则把它去掉
        elements = documentBody.children();
        while (elements.size()==1){
            tmp = elements.first().tagName();
            if(tmp.equalsIgnoreCase("div")
                    || tmp.equalsIgnoreCase("article")
                    || tmp.equalsIgnoreCase("section")
                    || tmp.equalsIgnoreCase("table")
                    || tmp.equalsIgnoreCase("tbody")
                    || tmp.equalsIgnoreCase("tr")
                    || tmp.equalsIgnoreCase("td")){
                elements.unwrap();
                elements = documentBody.children();
            }else {
                elements = elements.first().children();
            }
        }

        // // 对从根标签开始，连续N个层级都是单个标签的元素（可以说是N代单传）进行unwrap。不过不能直接从根标签开始unwrap，必须从最里面的开始。
        // // 注意，如果单传的标签是有意义的，比如video, audio那必须排除
        // List<String> nodes = new ArrayList<>();
        // elements = documentBody.children();
        // pattern = Pattern.compile("div|font|p|span|article|section", Pattern.CASE_INSENSITIVE);
        // int singles = 0;
        // while (elements.size() == 1){
        //     tmp = elements.first().tagName();
        //     if(!pattern.matcher(tmp).find()){
        //         break;
        //     }
        //     nodes.add(tmp);
        //     elements = elements.first().children();
        //     singles++;
        // }
        // for (int i = 1; i < singles; i++){
        //     String query = StringUtils.join(" > ", nodes);
        //     nodes.remove(singles - i);
        //     documentBody.select(query).unwrap();
        // }

        content = documentBody.html().trim();

        // 将包含<br>的空标签给解脱出来
        pattern = Pattern.compile("(\\s|　|&nbsp;)*<([a-zA-Z0-9]{1,10})>(\\s|　|&nbsp;)*(<br>)+(\\s|　|&nbsp;)*</\\2>(\\s|　|&nbsp;)*", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("<br>");

        // 将包含<hr>的空标签给解脱出来
        pattern = Pattern.compile("(\\s|　|&nbsp;)*<([a-zA-Z0-9]{1,10})>(\\s|　|&nbsp;)*(<hr>)+(\\s|　|&nbsp;)*</\\2>(\\s|　|&nbsp;)*", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("<hr>");

        // 把空的块状标签（有属性的）替换为换行 // noframes|noscript|
        pattern = Pattern.compile("(\\s|　|&nbsp;)*<(address|blockquote|center|dir|div|dl|fieldset|form|h1|h2|h3|h4|h5|h6|hr|isindex|menu|ol|p|pre|table|ul) [^>/]+>(\\s|　|&nbsp;)*</\\2>(\\s|　|&nbsp;)*", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("<br>");

        // 删除无效的空标签（无任何属性的）
        pattern = Pattern.compile("(\\s|　|&nbsp;)*<([a-zA-Z0-9]{1,10})>(\\s|　|&nbsp;)*</\\2>(\\s|　|&nbsp;)*", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        while (matcher.find()){
            content = matcher.replaceAll("");
            matcher = pattern.matcher(content);
        }

        // 删除无效的空标签（有属性的）要排除video, audio，注意此处的空标签必须是指定的，不然会把一些类似图片/音频/视频等“有意义的带属性空标签”给去掉
        pattern = Pattern.compile("(\\s|　|&nbsp;)*<(i|p|section|div|figure|pre|table|blockquote) [^>/]+>(\\s|　|&nbsp;)*</\\2>(\\s|　|&nbsp;)*", Pattern.CASE_INSENSITIVE);
        // content = pattern.matcher(content).replaceAll("");
        matcher = pattern.matcher(content);
        while (matcher.find()){
            content = matcher.replaceAll("");
            matcher = pattern.matcher(content);
        }

        // 去掉没有属性的span，font标签（此时没有意义）
        pattern = Pattern.compile("[\\s　]*<(span|font)>([\\s\\S]*?)</\\1>[\\s　]*", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("$2");

        // 去掉块状元素之间的换行标签
        pattern = Pattern.compile("</(<h\\d>|p|div|figure|pre|img|audio|video|iframe|embed|table|blockquote)>(\\s|　|&nbsp;)*(<br/?>)+(\\s|　|&nbsp;)*<(<h\\d>|p|div|figure|pre|img|audio|video|iframe|embed|table|blockquote)>", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("</$1><$5>");

        pattern = Pattern.compile("<(img|embed)([^>]*?)/?>(\\s|　|<br/?>|&nbsp;)+<(img|embed)([^>]*?)/?>", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(content);
        while (matcher.find()){
            content = matcher.replaceAll("<$1$2><$4$5>");
            matcher = pattern.matcher(content);
        }

        // 去掉“文章开头、各种标题”后紧跟着的换行标签
        pattern = Pattern.compile("(^|<h\\d>|<p>|<div>|<figure>|<pre>|<blockquote>)(\\s|　|&nbsp;)*(<br>|<hr>)+(\\s|　|&nbsp;)*", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("$1");

        // 去掉文章末尾的换行标签
        pattern = Pattern.compile("(\\s|　|&nbsp;)*(<br>|<hr>|<b>)+(\\s|　|&nbsp;)*($|</h\\d>|</p>|</div>|</figure>|</pre>|</blockquote>)", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("$4");

        // 给两个连续的链接之间加一个换行符
        pattern = Pattern.compile("</a>(\\s|　|&nbsp;)*<a", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("</a><br><a");

        pattern = Pattern.compile("^(\\s|\\n|　|&nbsp;)+", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("");
        pattern = Pattern.compile("$\\n+", Pattern.CASE_INSENSITIVE);
        content = pattern.matcher(content).replaceAll("");
        return content;
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
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.image_for_summary));
        pattern = Pattern.compile("<embed.*?>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.frame_for_summary));
        pattern = Pattern.compile("<(audio).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.audio_for_summary));
        pattern = Pattern.compile("<(video).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.video_for_summary));
        pattern = Pattern.compile("<(iframe).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.frame_for_summary));
        pattern = Pattern.compile("<(pre).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.pre_for_summary));
        pattern = Pattern.compile("<(table).*?>.*?</\\1>", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(App.i().getString(R.string.table_for_summary));

        html = Jsoup.parse(html).text();

        // 将连续多个空格换为一个
        pattern = Pattern.compile("(\\s|　|&nbsp;){2,}", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(" ");

        // 将连续多个➤合并为一个
        pattern = Pattern.compile("((\\s|　|&nbsp;)*➤(\\s|　|&nbsp;)*){2,}", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll("➤");

        // 将某些符号前后的去掉
        pattern = Pattern.compile("(\\s|　|&nbsp;)*➤+(\\s|　|&nbsp;)*([+_\\-=%@#$^&,，.。…:：!！?？○●◎⊙☆★◇◆□■△▲〓\\[\\]“”()（）〔〕〈〉《》「」『』［］〖〗【】{}])", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll("$3");
        pattern = Pattern.compile("([+_\\-=%@#$^&,，.。…:：!！?？○●◎⊙☆★◇◆□■△▲〓\\[\\]“”()（）〔〕〈〉《》「」『』［］〖〗【】{}])(\\s|　|&nbsp;)*➤+(\\s|　|&nbsp;)*", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll("$1");

        // 将开头的去掉
        pattern = Pattern.compile("^\\s*➤*\\s*", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll("");

        // 将末尾的去掉
        pattern = Pattern.compile("\\s*➤*\\s*$", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll("");

        // 给前后增加空格
        pattern = Pattern.compile("(\\s|　|&nbsp;)*➤(\\s|　|&nbsp;)*", Pattern.CASE_INSENSITIVE);
        html = pattern.matcher(html).replaceAll(" ➤ ");

        html = html.substring(0,  Math.min(html.length(), 90) );
        return html.trim();
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

        Element element;
        Document document = Jsoup.parseBodyFragment(article.getContent(), article.getLink());
        document.outputSettings().prettyPrint(false);
        document = ColorModifier.i().modifyDocColor(document);

        Elements elements;
        String cacheUrl;
        String idInMD5 = EncryptUtils.MD5(article.getId());

        // 预加载，提前将图片的真实地址替换出来
        elements = document.getElementsByTag("img");
        for (int i = 0, size = elements.size(); i < size; i++) {
            element = elements.get(i);
            // 抽取图片的绝对连接
            originalUrl = element.attr("abs:src");
            element.attr("original-src", originalUrl);

            cacheUrl = FileUtils.readCacheFilePath(idInMD5, originalUrl);

            if (cacheUrl != null) {
                element.attr("src", cacheUrl);
            } else {
                element.attr("src", imgHolder);
                element.addClass("img-lozad");
            }
        }

        elements = document.getElementsByTag("input");
        for (int i = 0, size = elements.size(); i < size; i++) {
            elements.get(i).attr("disabled", "disabled");
        }

        elements = document.getElementsByTag("textarea");
        for (int i = 0, size = elements.size(); i < size; i++) {
            elements.get(i).attr("disabled", "disabled");
            // element = elements.get(i);
            // element.tagName("details");
            // element.insertChildren(0,new Element("summary").text("\uD83D\uDD17"));
        }

        elements = document.getElementsByTag("iframe");
        for (int i = 0, size = elements.size(); i < size; i++) {
            element = elements.get(i);
            element.removeAttr("sandbox");// sandbox 会限制 iframe 的各种能力
            element.attr("frameborder", "0");
            element.attr("allowfullscreen", "");
            element.attr("scrolling", "no");
            element.attr("src",
                    element.attr("src")
                            .replaceAll("/(width|height)=\\d+/ig","")
                            .replaceAll("/(&(amp;)*){2,}/ig","&")
            );
        }

        elements = document.getElementsByTag("embed");
        for (int i = 0, size = elements.size(); i < size; i++) {
            elements.get(i).attr("autostart", "false");
        }

        elements = document.getElementsByTag("video");
        for (int i = 0, size = elements.size(); i < size; i++) {
            element = elements.get(i);
            element.attr("controls", "true");
            element.attr("width", "100%");
            element.attr("height", "auto");
            element.attr("preload", "metadata");
        }

        elements = document.getElementsByTag("audio");
        for (int i = 0, size = elements.size(); i < size; i++) {
            element = elements.get(i);
            element.attr("controls", "true");
            element.attr("width", "100%");
        }

        ////// 给 table 包装 div 并配合 overflow-x: auto; ，让 table 内的 pre 不会撑出屏幕
        //elements = document.getElementsByTag("table");
        //for (Element element : elements) {
        //    element.wrap("<div class=\"table_wrap\"></div>");
        //}
        return document.body().html().trim();
    }
    public static String getCoverUrl(String articleUrl, String content) {
        // 获取第1个图片作为封面
        Document document = Jsoup.parseBodyFragment(content,articleUrl);
        document.outputSettings().prettyPrint(false);
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

    public static String getKeyword(String content) {
        String keyword = Jsoup.parseBodyFragment(content).body().text().trim();
        if( keyword.length() > 8){
            keyword = keyword.substring(0,8);
        }
        return keyword;
    }
    
    public static String getOptimizedAuthor(Feed feed, String articleAuthor) {
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


    public static String getOptimizedContentWithEnclosures(String content, @Nullable List<Enclosure> attachments){
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
}
